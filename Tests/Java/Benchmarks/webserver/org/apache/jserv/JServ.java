/*
 * Copyright (c) 1997-1999 The Java Apache Project.  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. All advertising materials mentioning features or use of this
 *    software must display the following acknowledgment:
 *    "This product includes software developed by the Java Apache 
 *    Project for use in the Apache JServ servlet engine project
 *    <http://java.apache.org/>."
 *
 * 4. The names "Apache JServ", "Apache JServ Servlet Engine" and 
 *    "Java Apache Project" must not be used to endorse or promote products 
 *    derived from this software without prior written permission.
 *
 * 5. Products derived from this software may not be called "Apache JServ"
 *    nor may "Apache" nor "Apache JServ" appear in their names without 
 *    prior written permission of the Java Apache Project.
 *
 * 6. Redistributions of any form whatsoever must retain the following
 *    acknowledgment:
 *    "This product includes software developed by the Java Apache 
 *    Project for use in the Apache JServ servlet engine project
 *    <http://java.apache.org/>."
 *    
 * THIS SOFTWARE IS PROVIDED BY THE JAVA APACHE PROJECT "AS IS" AND ANY
 * EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE JAVA APACHE PROJECT OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Java Apache Group. For more information
 * on the Java Apache Project and the Apache JServ Servlet Engine project,
 * please see <http://java.apache.org/>.
 *
 */

package org.apache.jserv;

import java.io.*;
import java.net.*;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;
import org.apache.java.io.*;
import org.apache.java.net.*;
import org.apache.java.util.*;
import org.apache.java.security.*;
import org.apache.java.recycle.pool.*;
import org.apache.java.lang.Semaphore;

/**
 * <code>JServ</code> is the entry point to the Java part of <b>JServ</b>
 * <p>
 * It sets up the server, initalizes everything, and listens on a TCP
 * port for requests for the server. When it gets a request, it
 * launches a JServConnection thread.
 *
 * @author Stefano Mazzocchi
 * @version $Revision: 1.1 $ $Date: 2000-06-29 01:41:56 $
 */
public class JServ extends HttpServlet implements JServLogChannels, JServDefs {

    /**
     * Default port to listen to.
     *
     * There are two cases when this value is engaged:
     * <ul>
     * <li>No <code>port</code> parameter is specified in the
     * <code>jserv.properties</code>
     * <li>Value specified is outside the valid range (1024-65535).
     * The lower one is the minimum allowed for non-root listening socket,
     * the upper one is imposed by the operating system limitations.
     * The latter restriction may be lifted in the future (there are UNIX
     * flavors which already don't have it).
     * </ul>
     * Initialized in {@link #init init}.
     */
    public final static int DEFAULT_PORT = 8007;
    public final static String DEFAULT_CONTROLLER = "org.apache.java.recycle.DefaultController";
    
    public final static String[] colors = { "#f0f0f0", "#e0e0e0" };
    public final static String version() {
        return PACKAGE + "/" + VERSION;
    }

    protected static Hashtable servletManagerTable = new Hashtable();
	protected static Hashtable zoneMap = new Hashtable();
	protected static String defaultZone = null;
    protected static Configurations confs = null;
    protected static JServLog log = null;
    protected static WorkerPool pool = null;

    private static ServerSocket listenSocket = null;
    private static Hashtable table = new Hashtable();
    private static Semaphore semaphore;

    static String confFile = null;
        
    /**
     * Start up JServ.
     * <p>
     * The command line usage is the following:
     * <pre>java org.apache.jserv.JServ configFile</pre>
     * <br>where <i>configurationFile</i> if JServ configuration file.
     * @param arguments Command line parsed into strings.
     */
    public static void main(String[] argument) {

        // Parse command line arguments
        for (int i = 0; i < argument.length; i++) {
            String arg = argument[i];
            if (arg.charAt(0) == '-') {
                switch (arg.charAt(1)) {
                    case 'v': System.out.println("Server version: " + version()); break;
                    case 'V': System.out.println("Server version: " + version());
                              System.out.println("Turbo mode: " + TURBO);
                              System.out.println("Profile mode: " + PROFILE); break;
                    case 'r': if (confFile == null) usage();
                              signal("restart"); break;
                    case 's': if (confFile == null) usage();
                              signal("terminate"); break;
                    default: usage();
                }
                System.exit(0);
            } else {
                if (confFile == null) {
                    confFile = arg;
                } else {
                    usage();
                }
            }
        }
        
        // check if the configuration file was set
        if (confFile == null) usage();

        // print welcome message
        welcome();

        // Start the server
        start();

        // Profiler stuff
        if (JServ.PROFILE) {
            // Runtime.getRuntime().traceInstructions(true);
            Runtime.getRuntime().traceMethodCalls(true);
        }

        while (true) {
            //Here we make sure that the number of 
            //parallel connections is limited
	        semaphore.throttle();
            try {
                JServConnection connection = new JServConnection();
                Thread t = new Thread(connection);
                t.setDaemon(true);
                Socket clientSocket = listenSocket.accept();
                connection.init(clientSocket, semaphore);
                t.start();
                if (!JServ.TURBO && log.active) log.log(CH_DEBUG, "Connection from " + clientSocket.getInetAddress());
            } catch (AuthenticationException e) {
                if (log.active) log.log(CH_CONTAINER_EXCEPTION, e.getMessage());
            } catch (BindException portNotAvailable) {
                fail("Could not listen on specified port", portNotAvailable);
            } catch (SocketException socketWasClosed) {
                if (log.active) log.log(CH_CONTAINER_EXCEPTION, socketWasClosed.getMessage());
                restart();
            } catch (Throwable e) {
                fail("An error occurred listening to the port", e);
            }
        }
    }

    /**
     * Prints the welcome message on standard output.
     * This is seen only from manual operation.
     *
     * This method is called from {@link #main main}.
     */
    private static void welcome() {
        System.out.println(version());
    }
    
    /**
     * Prints the help on usage message on standard error.
     *
     * This method is called from {@link #main main}.
     */
    private static void usage() {
        System.err.println("Usage: java org.apache.jserv.JServ [config file] [options]");
        System.err.println("\nOptions:");
        System.err.println("  -v : show version number");
        System.err.println("  -V : show compile settings");
        System.err.println("  -s : tell running ApacheJServ to shutdown");
        System.err.println("  -r : tell running ApacheJServ to do a graceful restart");
        System.err.println("\nNote: please, specify the configuration file for the [-s] [-r] options.");
        System.exit(1);
    }
    
    /**
     * Read the host configuration property file and perform all the
     * specified actions.
     *
     * This method is called from {@link #main main}.
     */
    static synchronized void start() {

        // Load configuration parameters
        try {
            confs = new Configurations(new ExtendedProperties(confFile));
        } catch (IOException e) {
            fail("Error while reading configuration file", e);
        }

        // Create log/trace writer
        log = new JServLog("log", confs);

        if (log.active) log.log(CH_INFO, JServ.version() + " is starting...");

        // Get port JServ will listen to
        int port = confs.getInteger("port", DEFAULT_PORT);
        if ((port < 1024) || (65535 < port)) {
            if (log.active) log.log(CH_WARNING, Integer.toString(port)
                + ": invalid port, reset to default " + DEFAULT_PORT );
            port = DEFAULT_PORT;
        }

        // Check the thread pool usage
        if (confs.getBoolean("pool", false)) {
            int capacity = confs.getInteger("pool.capacity", 10);
            String controller = confs.getString("pool.controller", DEFAULT_CONTROLLER);
            pool = new WorkerPool(capacity, controller);
        }
        
        // Get maximum allowed simultaneous connections
        int maxConnections = confs.getInteger("security.maxConnections", 50);
        // And check it for meaningless values
        if (maxConnections<2) maxConnections = 2;
	    semaphore = new Semaphore(maxConnections);

        // Get maximum allowed simultaneous connections
        int backlog = confs.getInteger("security.backlog", 5);
        // And check it for meaningless values
        if (backlog<1) backlog = 1;

        byte[] secret = null;
        MessageDigest md = null;
        int challengeSize = 0;
        int maxPacketDelay = 0;
        Vector addressList = new Vector();

        boolean authenticate = confs.getBoolean("security.authentication", true);

        if (authenticate) {
            if (log.active) log.log(CH_INFO, "Connection authentication enabled");

            // get secret key from a file
            try {
                File f = new File(confs.getString("security.secretKey"));
                InputStream is = new BufferedInputStream(new FileInputStream(f));
                secret = new byte[is.available()];
                is.read(secret);
                is.close();
            } catch (NullPointerException e) {
                fail("No secret key file defined", e);
            } catch (FileNotFoundException e) {
                fail("Could not open secret key file", e);
            } catch (IOException e) {
                fail("Error found reading the secret key file", e);
            }

            // create the message digest algorithm
            md = new MD5();

            // get the challenge size
            challengeSize = confs.getInteger("security.challengeSize", 5);
        } else {
            if (log.active)
                log.log(CH_INFO,
                    "Connection authentication is disabled");
        }

        // Get IP addresses allowed to connect
        Enumeration addresses = confs.getList("security.allowedAddresses");

        String address;
        do {
            try { 
                address = (String) addresses.nextElement();
                if (address.equals("DISABLED")) {
                    addressList = null;
                    break;
                }
            } catch (NoSuchElementException noAddressSpecified) {
                address = "127.0.0.1";
            }
            
            try {
                InetAddress inetAddress = InetAddress.getByName(address);
                if (log.active)
                    log.log(CH_INFO, "Connection allowed from "
                        + inetAddress);
                addressList.addElement(inetAddress);
            } catch (UnknownHostException wrongSyntax) {
                if (log.active) log.log(CH_WARNING, "Host "
                    + address + " not found and not allowed to connect");
            }
        } while (addresses.hasMoreElements());

        // Create the server socket
        try {
            // Get default hostname to pass to AuthenticatedServerSocket
            // so that we bind properly to a single IP address instead 
            // of all IP addresses on the box
            InetAddress ia = bindTo(confs.getString("bindaddress"));

            if (log.active) log.log(CH_INFO, "Listening on port " + port
                + " accepting " + backlog + " maximum connections");
            if (authenticate) {
                listenSocket = new AuthenticatedServerSocket(port,
                    backlog, addressList, md, secret, challengeSize, 
                        ia);
            } else {
                listenSocket = new AuthenticatedServerSocket(port,
                    backlog, addressList, ia);
            }
        } catch (IOException e) {
            fail("Exception creating the server socket", e);
        }

        // Get Servlet Zones
        if (log.active) log.log(CH_INFO, "Creating Servlet Zones");
        Enumeration zones = confs.getList("zones");
        if (zones == null) {
            fail("No servlet zones defined in configuration file");
        }

        // Get properties file for each defined servlet zone
        while (zones.hasMoreElements()) {
            String servletZone = (String) zones.nextElement();
            if (log.active)
                log.log(CH_INFO, "Servlet Zone " + servletZone
                    + " initializing...");
            String confFile = confs.getString(servletZone + ".properties");
            if (confFile == null) {
                fail("No configuration file named \"" + servletZone + 
                  ".properties\" for servlet zone " + servletZone);
            }
            if (log.active)
                log.log(CH_DEBUG, " - Using configuration file: " + confFile);

            // Create servlet manager for this zone
            JServServletManager manager =
                new JServServletManager(servletZone, confFile);
            manager.init(log);
            servletManagerTable.put(servletZone, manager);
			if (manager.webroot.compareTo("DEFAULT") == 0)
				defaultZone = servletZone;
			zoneMap.put(manager.webroot, servletZone);
            if (log.active)
                log.log(CH_DEBUG, "Servlet Zone " + servletZone
                    + " initialization complete");
        }
    }

		/**
		 * finds zone for a given uri
		 */
	protected static synchronized String findZonePath(String uri) {
		int ndx = -1;
		String path = null;
		for (Enumeration e = zoneMap.keys() ; e.hasMoreElements() ;) {
			path = (String) e.nextElement();			
			if (uri.startsWith(path))
				return path;
		}
		return "DEFAULT";  // DEFAULT zone is not in zoneMap
	}
	
    /**
     * Clears JServ and prepare it for restart of termination
     */
    protected static synchronized void clear() {
        try {
            if (log.active) log.log(CH_DEBUG, "Closing the server socket");
            listenSocket.close();
        } catch (IOException ignored) {}

        // Tell each manager to destroy its servlet.
        Enumeration enum = servletManagerTable.elements();
        while (enum.hasMoreElements()) {
            JServServletManager manager =
                (JServServletManager) enum.nextElement();
            if (log.active) log.log(CH_DEBUG, "Terminating "
                + manager.getName());
            manager.destroyServlets();
        }
        
        if (log != null) log.flush();
    }

    /**
     * returns the InetAddress this JServ is binding to
     * read from properties file.
     */
    private static InetAddress bindTo(String HostName) {
        // allows to bind either to all IP addresses or one of them
        // default = 127.0.0.1 (localhost)
        // if Hostname=* return null
        // if Hostname=unresolved and localhost unresolved return null
        InetAddress ia = null;
        try {
            if (HostName == null || 
                HostName.equals("localhost")) {
                ia = InetAddress.getByName("localhost");
            }
            else {
                if (HostName.equals("*"))
                    ia = null;
                else {
                    try {
                        ia = InetAddress.getByName(HostName);
                    } catch (UnknownHostException uhe) {
                        ia = InetAddress.getByName("localhost");
                    }
                }
            }
        } catch (UnknownHostException e) { 
            ia = null;
        }
        return ia;
    }

    /**
     * Restart JServ.
     *
     * <b>Note:</b> due to a bug in the JavaSoft JVM on some 
     * platforms (win32), closing a  server socket and 
     * rebinding to the same port produces a chain of hidden exception
     * being thrown when the accept() method is created.
     *
     * <p>The JVM tries to bind on the port and starts an hidden
     * loop that consumes all the CPU and makes JServ appear
     * burried in an infinite loop. This doesn't prevent JServ 
     * to restart gracefully, but it suddently appears dead from 
     * a network point of view and consumes all the CPU
     * resources
     *
     * <p>Unfortunately, there is nothing we can do but wait for
     * JVM implementors to fix this nasty bug. :-( (SM)
     */
    protected static synchronized void restart() {
        if (log.active) log.log(CH_INFO, "Restarting " + JServ.version() + "...");
        clear();
        start();
    }

    /**
     * Terminate JServ.
     */
    protected static synchronized void terminate() {
        if (log.active) log.log(CH_INFO, "Terminating " + JServ.version() + "...");
        clear();
        if (log.active) log.log(CH_DEBUG, JServ.version() + " terminated.");
        log.flush();
        System.exit(0);
    }

    /**
     * Signal a running JServ.
     */
    private static synchronized void signal(String signal) {
        Configurations confs = null;
        AuthenticatedSocket socket = null;
        byte[] signalBytes = new byte[2];
        signalBytes[0] = (byte) 254;

        if (signal.equals("restart")) {
            signalBytes[1] = (byte) 1; 
        } else {
            signalBytes[1] = (byte) 15;
        }

        try {
            confs = new Configurations(new ExtendedProperties(confFile));
        } catch (IOException e) {
            fail("Error while reading configuration file", e);
        }

        int port = confs.getInteger("port",JServ.DEFAULT_PORT);
        InetAddress ia = bindTo(confs.getString("bindaddress"));
        boolean authenticate = confs.getBoolean("security.authentication", true);
        byte[] secret = new byte[0];

        if (authenticate) {
            try {
                File f = new File(confs.getString("security.secretKey"));
                InputStream is = new BufferedInputStream(new FileInputStream(f));
                secret = new byte[is.available()];
                is.read(secret);
                is.close();
            } catch (NullPointerException e) {
                fail("No secret key file defined", e);
            } catch (FileNotFoundException e) {
                fail("Could not open secret key file", e);
            } catch (IOException e) {
                fail("Error found reading the secret key file", e);
            }

            try {
                socket = new AuthenticatedSocket(ia, port, new MD5(), secret);
            } catch (IOException e) {
                fail("Error found creating the socket", e);
            }
        } else {
            try {
                socket = new AuthenticatedSocket(ia, port);
            } catch (IOException e) {
                fail("Error found creating the socket", e);
            }
        }

        try {
            socket.getOutputStream().write(signalBytes);
            socket.close();
        } catch (IOException e) {
            fail("Error found signaling the running ApacheJServ", e);
        }
        
        System.out.println("Signal sent.");
    }
    
    /**
     * Exit with an error message formatted using the exception message.
     */
    protected static void fail(String msg, Throwable e) {
        fail(msg + ": " + e);
    }

    /**
     * Exit with an error message.
     */
    protected static void fail(String msg) {
        try {
            if (log.active) log.log(CH_CRITICAL, JServ.version() + ": " + msg);
        } catch (Exception ignored) {}
 
        System.err.println(JServ.version() + ": " + msg);
        System.exit(1);
    }

    private static final String SINGLE_COLUMN = "***";
    private static final String STATUS_SERVLET = "org.apache.jserv.JServ";
    
    /**
     * JServ is also a servlet that returns info to the client about its
     * status. Security checks are performed by the Apache side but we
     * have the chance to disable this behavior from this side, too.
     * <p>
     * This servlet is used as installation and configuration feedback, since
     * its correct functioning tells the user JServ is correctly working.
     * Since this class will always be in the executing JVM's classpath,
     * the user receive this output if Apache and JServ are setup correctly,
     * forgetting about the servletzone configurations that may be done
     * later on.
     *
     * @exception ServletException when servlet request fails
     */
    public void service(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {

        res.setContentType("text/html");
        ServletOutputStream out = res.getOutputStream();

        // Send graphic header (banner, ...)
        sendHead(out);
        
        // Check if execution as servlet is permitted
        if (!confs.getBoolean("security.selfservlet", false)) {
            out.println("<h2 align=center>Sorry, the execution of "
                + "this service is denied for security reasons. "
                + "You can enable this feature by setting the " 
                + "boolean configuration variable security.selfservlet"
                + "</h2>");
            return;
        }

        // Dispatch query to the right handler
        try {
            String query = req.getQueryString();

            if (query.startsWith("status")) {
                handleStatus(out, query);
            } else if (query.startsWith("zones")) {
                handleZones(out, query);
            } else {
                showMenu(out);
            }
        } catch (NullPointerException noQuery) {    
            showMenu(out);
        }
        
        // Send graphic tail (links, copyright, ...)
        sendTail(out);
    }

    private void sendHead(ServletOutputStream out) throws IOException {
        out.println("<html>");
        out.println("<head>");
        out.println("<meta name=\"GENERATOR\" CONTENT=\"" + JServ.version() + "\">");
        out.println("<title>" + JServ.version() + " status</title>");
        out.println("</head>");
        out.println("<body bgcolor=\"#ffffff\" text=\"#000000\">");
        out.println("<p align=center><img src=\"../../status?image\"></p>");
    }

    private void sendTail(ServletOutputStream out) throws IOException {
        out.println("<br><center><a href=\"../../status?menu\">Back to menu</a></center>");
        out.println("<p align=\"center\"><font size=-1>");
        out.println("Copyright (c) 1997-99 <a href=\"http://java.apache.org\">");
        out.println("The Java Apache Project</a>.<br>");
        out.println("All rights reserved.");
        out.println("</font></p>");
        out.println("</body>");
        out.println("</html>");    
    }
    
    private void showMenu(ServletOutputStream out) throws IOException  {
        // Write menu table
        table.clear();
        table.put("<a href=./" + STATUS_SERVLET + "?status>"
            + "<big>Current Status</big></a>", SINGLE_COLUMN);
        writeTable(out, JServ.version() + " Servlet Engine Status", table, colors);
        
        // Write Servlet Zones Table
        table.clear();
        Enumeration zones = servletManagerTable.keys();
        for (int i = 0; zones.hasMoreElements(); i++) {
            String name = (String) zones.nextElement();
            table.put("<a href=./" + STATUS_SERVLET + "?zones="
                + name + "><big>" + name + "</big></a>", SINGLE_COLUMN);
        }
        writeTable(out, "Servlet Zones", table, colors);        
    }

    private void handleStatus(ServletOutputStream out, String query)
        throws IOException
    {
        // Write Operating System Table
        table.clear();
        try {
            table.put("Name", System.getProperty("os.name"));
        } catch (Exception ignoreItem) {};
        try {
            table.put("Architecture", System.getProperty("os.arch"));
        } catch (Exception ignoreItem) {};
        try {
            table.put("Version", System.getProperty("os.version"));
        } catch (Exception ignoreItem) {};
        writeTable(out, "Operating System", table, colors);

        // Write Java Virtual Machine Table
        table.clear();
        try {
            table.put("Version", System.getProperty("java.version"));
        } catch (Exception ignoreItem) {};
        try {
			table.put("Vendor", "<a href=\"" 
				+ System.getProperty("java.vendor.url") + "\">" 
				+ System.getProperty("java.vendor") + "</a>");
		} catch (Exception ignoreItem) {};
		try {
			table.put("JVM Compiler", System.getProperty("java.compiler"));
        } catch (Exception ignoreItem) {};
        try {
            table.put("Class Format Version", 
            	System.getProperty("java.class.version"));
        } catch (Exception ignoreItem) {};
        try {
            table.put("Class Path", new StringTokenizer(
                System.getProperty("java.class.path"),
                System.getProperty("path.separator")));
        } catch (Exception ignoreItem) {};
        writeTable(out, "Java Virtual Machine", table, colors);

        // Write General JServ Configurations Table
        table.clear();
        try {
            table.put("Bindaddress", listenSocket.getInetAddress());
        } catch (Exception ignoreItem) {};

        try {
            table.put("Port", new Integer(listenSocket.getLocalPort()));
        } catch (Exception ignoreItem) {};
        try {
            table.put("Maximum Connections",
                new Integer(((AuthenticatedServerSocket)
                listenSocket).getMaxConnections()));
        } catch (Exception ignoreItem) {};
        try {
            table.put("Allowed IP Addresses", ((AuthenticatedServerSocket)
                listenSocket).getFilterList());
        } catch (Exception ignoreItem) {};
        try {
            table.put("Authentication",
                (confs.getBoolean("security.authentication", true))
                    ? "<font color=#00c000>Enabled</font>"
                    : "<font color=#c00000>Disabled</font>");
        } catch (Exception ignoreItem) {};
        // Note: this information is not security sensible because it is sent
        // with the AJP authentication challenge packet and is therefore
        // known by every client
        try {
            table.put("Challenge Size",
                new Integer(((AuthenticatedServerSocket)
                    listenSocket).getChallengeSize()));
        } catch (Exception ignoreItem) {};
	
	// status of the logging subsystem
	StringBuffer logStatus = new StringBuffer (log.active
						   ? "Enabled" 
						   : "Disabled");
	if (!log.isSane()) {
	  logStatus.insert (0, "<font color='#c00000'>");
	  if (log.getSubsystemError() != null) {
	    logStatus.append (" - ");
	    logStatus.append (log.getSubsystemError());
	  }
	  logStatus.append ("</font>");
	}
	else if (log.isActive()) {
	  logStatus.append (" ( ");
	  logStatus.append (confs.getString ("log.file", "?"));
	  logStatus.append (" )");
	}
	table.put("Logging", logStatus.toString());

        writeTable(out, "General Configurations", table, colors);
    }

    private void handleZones(ServletOutputStream out, String query)
        throws IOException
    {
        StringTokenizer t = new StringTokenizer(query, "=");
        t.nextToken();

        try {
            String zone = t.nextToken();

            // Write Servlet Zone Configurations Table
            try {
                Configurations zoneConfs = ((JServServletManager)
                    servletManagerTable.get(zone)).confs;
                writeTable(out, "<font color=#ff0000>" + zone
                    + "</font> Servlet Zone</a>",
                        zoneConfs.getRepository(), colors);
            } catch (Exception zoneNotFound) {
                out.println("<h3 align=center>Error, servlet zone <b>"
                    + zone + "</b> was not found</h3>");
            }
        } catch (NoSuchElementException noZoneRequested) {
            // Write Servlet Zones Table
            table.clear();
            Enumeration zones = servletManagerTable.keys();
            for (int i = 0; zones.hasMoreElements(); i++) {
                String name = (String) zones.nextElement();
                table.put("<a href=./" + STATUS_SERVLET + "?zones="
                    + name + "><big>" + name + "</big></a>", SINGLE_COLUMN);
            }
            writeTable(out, "Servlet Zones", table, colors);
        }
    }

    private void writeTable(ServletOutputStream out, String name,
            Hashtable table,
        String[] rowColors) throws IOException {

        Enumeration names = table.keys();

        out.println("<p><center><table bgcolor=#000000 border=0 cellpadding=0 cellspacing=0 width=50%>");
        out.println("<tr><td><table border=0 cellpadding=4 cellspacing=2 width=100%>");
        out.println("<tr><td align=right valign=middle colspan=2 bgcolor=#c0c0c0>");
        out.println("<h3>" + name + "</td></tr>");

        for (int i = 0, j = 0; names.hasMoreElements(); i++) {
            Object key = names.nextElement();
            Object value = table.get(key);

            try {
                out.println("<tr><td align=right ");

                if (value.equals(SINGLE_COLUMN)) {
                    out.println("colspan=2 ");
                }

                out.println("bgcolor=" + rowColors[j & 1] + ">");
                out.println("<font size=-1>" + key.toString() + "</font></td>");

                if (!value.equals(SINGLE_COLUMN)) {
                    out.println("<td align=left bgcolor="
                        + rowColors[j & 1] + ">");

                    if (value instanceof Enumeration) {
                        Enumeration e = (Enumeration) value;
                        while (e.hasMoreElements()) {
                            out.println("<b><font size=-1>" 
                                + e.nextElement().toString() + "</font></b>");
                        }
                    } else {
                        out.println("<b><font size=-1>" + value.toString() 
                            + "</font></b>");
                    }

                    out.println("</td></tr>");
                }

                j++;
            } catch (Exception valueIgnored) {}
        }

        out.println("</table></td></tr></table></center></p>");
    }
}
