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
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;
import org.apache.java.lang.*;
import org.apache.java.util.*;

/**
 * Class that encapsulates the loading and managing of servlets per
 * servlet zone.
 *
 * <P><b>Note about synchronization</b> :
 *
 * <p>All the method that modifies the servlet table are synchronized
 * on the JServServletManager. Since this table is private there no needs
 * to synchronized on it anymore.
 *
 * @author Alexei Kosut
 * @author Francis J. Lacoste
 * @author Martin Pool
 * @author Ed Korthof
 * @author Vincent Partington
 * @author Stefano Mazzocchi
 * @version $Revision: 1.1 $ $Date: 2000-07-18 22:01:52 $
 */
public class JServServletManager implements HttpSessionContext, Runnable, JServLogChannels {

    private static final int NAME = 0;
    private static final int VALUE = 1;

    /**
     * The name of the session parameter.
     */
    static final String SESSION_IDENTIFIER_BASE = "JServSessionId";

    /**
     * The amount of time to wait before giving up on lock when initializing a
     * servlet. This is taken from the property
     * <var>init.timeout</var> and defaults to 10000 (10 seconds).
     */
    long initTimeout;

    /**
     * The amount of time to wait before giving up on lock when destroying
     * servlet. This is taken from the property
     * <var>destroy.timeout</var> and defaults to 10000 (10 seconds).
     */
    long destroyTimeout;

    /**
     * The amount of time to wait before invalidating an unused session.
     * This is taken from the property
     * <var>session.timeout</var> and defaults to 1800000 (30 minutes)
     */
    long sessionTimeout;

    /**
     * The amount of time to wait before invalidating a new session that
     * has never been "used" by the client. Setting this lower than sessionTimeout
     * can help avoid an attack based on filling available memory with new sessions.
     * This is taken from the property
     * <var>session.newtimeout</var> and defaults to 1800000 (30 minutes)
     */
    long newSessionTimeout;

    /**
     * How frequently to check for the existence of timed-out sessions.
     * This is taken from the property
     * <var>session.checkFrequency</var> and defaults to 5000 (5 seconds)
     */
    long sessionCheckFrequency;

    /**
     * Determines wheter to check the property file for changes.
     */
    boolean checkFile;

    /**
     * Determines whether to check the classes for changes.
     */
    boolean checkClasses;

		/**
		 * specifies whether this is a module zone or a servlet zone
		 */
	boolean modules;

		/**
		 * contains the web root of this zone
		 */
	String webroot;

		/**
		 * modules for this zone if any
		 */
	String[] module_list;
	
	
    /**
     * The file that contains the servlet properties.
     */
    protected File confFile;

    /**
     * The time of the last initialization.
     */
    protected long lastInitialization;

    /**
     * The configurations containing information
     * for these servlets.
     */
    protected Configurations confs;

    /**
     * The default init arguments for all the servlets
     * in this name space.
     */
    protected Properties defaultArgs;

    /**
     * The class loader used for loading new servlets
     */
    protected AdaptiveClassLoader loader;

    /**
     * The cache of loaded servlets
     */
    private Hashtable servletContexts;

    /**
     * The ThreadGroup in which the servlets are run.
     */
    protected ThreadGroup tGroup;

    /**
     * A (slightly more) unique session identifier derived from
     * SESSION_IDENTIFIER_BASE and <code>name</code>.
     */
    protected String session_identifier;

    /**
     * The name of this ServletManager.
     */
    protected String name;

    /**
     * The servlets to load on startup.
     */
    protected String[] startups;

    /**
     * The names of all the named servlet.
     */
    protected Vector servletNames;

    /**
     * The sessions in this manager.
     */
    protected Hashtable sessions;

    /**
     * This flag determines how sessions are created.
     * By default, cookies are used if possible, else URL-rewriting will
     * be used.
     * By setting this flag to false (session.useCookies=false) you can 
     * force Apache JServ to use URL-rewriting only.
     * Default value : true
     */
    boolean sessionUseCookies = true;

    /**
     * Creates a new servlet manager.
     *
     * @param name The name of this ServletManager.
     * @param propFile The name of the property file to use.
     */
    JServServletManager(String name, String confFile) {

        this.name = name;
        this.confFile = new File(confFile);
        this.session_identifier = SESSION_IDENTIFIER_BASE + name;

        // Creates servlet context map
        servletContexts = new Hashtable();

        // Creates thread group.
        tGroup = new ThreadGroup(name + "-Servlets");
    }

    /**
     * When deserializing the sessions during a class
     * loader reload, override the resolveClass() method 
     * so that it uses the AdaptiveClassLoader to deserialize
     * the sessions. This has the benefit of allowing 
     * objects that are only within the ACL's classpath 
     * to be found and deserialized.
     */
    class ACLObjectInputStream extends ObjectInputStream {
        ACLObjectInputStream(InputStream bIn) throws IOException {
            super(bIn);
        }
	protected Class resolveClass(ObjectStreamClass v)
          throws IOException, ClassNotFoundException {
            return loader.loadClass(v.getName());
        }
    }

    /**
     * Load the configuration from the property file and load the
     * startup servlets.
     *
     * @param JServSendError An object that can handle errors.
     */
    public synchronized void init(JServSendError errorHandler) {

        // Get the configurations from confFile
        try {
            confs = new Configurations(new ExtendedProperties(confFile.getAbsolutePath()));
        } catch (Exception e) {
            JServ.fail("Could not read servlet zone configuration file", e);
        }

        this.initTimeout = confs.getLong("init.timeout", 10000);
        this.destroyTimeout = confs.getLong("destroy.timeout", 10000);
        this.sessionTimeout = confs.getLong("session.timeout", 1800000);
        this.newSessionTimeout = confs.getLong("session.newtimeout", 1800000);
        this.sessionCheckFrequency = confs.getLong("session.checkFrequency", 5000);
        this.sessionUseCookies = confs.getBoolean("session.useCookies", true);
        this.checkFile = confs.getBoolean("autoreload.file", true);
        this.checkClasses = confs.getBoolean("autoreload.classes", true);
        this.defaultArgs = confs.getProperties("servlets.default.initArgs");
        this.startups = confs.getStringArray("servlets.startup");

		this.modules = confs.getBoolean("modules", false);
		this.webroot = confs.getString("webroot", "");
		this.module_list = confs.getStringArray("modules.list");

        if (JServ.log.active) {
            JServ.log.log(CH_INFO, "Initialisation timeout: " + (this.initTimeout / 1000) + " seconds");
            JServ.log.log(CH_INFO, "Destroy timeout: " + (this.destroyTimeout / 1000) + " seconds");
            JServ.log.log(CH_INFO, "Session timeout: " + (this.sessionTimeout / 1000) + " seconds");
            JServ.log.log(CH_INFO, "New session timeout: " + (this.newSessionTimeout / 1000) + " seconds");
            JServ.log.log(CH_INFO, "Session check frequency: " + (this.sessionCheckFrequency / 1000) + " seconds");
            JServ.log.log(CH_INFO, "Autoreload on zone file changes: " + this.checkFile);
            JServ.log.log(CH_INFO, "Autoreload on classfile changes: " + this.checkClasses);
            JServ.log.log(CH_INFO, "Default initArgs: " + this.defaultArgs);

			JServ.log.log(CH_INFO, "Modules: " + this.modules);
			JServ.log.log(CH_INFO, "Web Root: " + this.webroot);
        }

        // Load servlet repository for this servlet zone
        Enumeration reps = confs.getList("repositories");
        if (reps == null) {
            JServ.fail("Please define a servlet repository for servlet zone " + name + "." +
              "\nThis is done by adding a \"repositories=\" line in the " + name + 
                ".properties file for the zone and defining it to be a path to a " +
                "directory that exists on disk.");
        }

        // Build the class repository
        Vector repository = new Vector();
        while (reps.hasMoreElements()) {
            repository.addElement(new File((String) reps.nextElement()));
        }

        // Build the class loader
        try {
            loader = new AdaptiveClassLoader(repository);
        } catch (IllegalArgumentException e) {
            JServ.log.log(CH_WARNING,"Error creating classloader for servlet zone " + name + " : " + e.toString());
        }

        // Get all the named servlet in the property file
        servletNames = new Vector();
        Enumeration names = confs.getKeys();
        while (names.hasMoreElements()) {
            String prop = (String) names.nextElement();
            // Servlet name are property of the form
            // servlet.<name>.code
            if (prop.startsWith("servlet.") && prop.endsWith(".code")) {
                String name = prop.substring(8, prop.length() - 5);
                if (JServ.log.active) {
                    JServ.log.log(CH_INFO, "Servlet name: " + name);
                }
                servletNames.addElement(name);
            }
        }

        lastInitialization = confFile.lastModified();

        // This code allows the sessions to be restored even after 
        // the AdaptiveClassLoader has been re-instantiated.
        if (sessions != null) {
            try {
                // save the contexts...they are not serializable, but 
                // we need to save them anyways.
                Hashtable theContexts = new Hashtable(sessions.size());
                
                Enumeration keys = sessions.keys();
                String key = null;
                JServSession value = null;

                while(keys.hasMoreElements()) {
                    key = (String) keys.nextElement();
                    value = (JServSession) sessions.get(key);

                    theContexts.put (key, (JServServletManager) value.getSessionContext());
                }
                
                // writes the session data out, but loses the contexts
                // because they cannot be serialized
                ByteArrayOutputStream b = new ByteArrayOutputStream();
                ObjectOutputStream o = new ObjectOutputStream(b);
                
                o.writeObject(sessions);
                o.flush();
                
                ByteArrayInputStream bIn = new ByteArrayInputStream (b.toByteArray());
                ObjectInputStream oOut= new ACLObjectInputStream(bIn);
                
                // unserialize the sessions
                sessions = (Hashtable) oOut.readObject();

                // restore the contexts
                keys = sessions.keys();
                while(keys.hasMoreElements()) {
                    key = (String) keys.nextElement();
                    value = (JServSession) sessions.get(key);

                    value.setSessionContext((JServServletManager) theContexts.get ( key ));
                }

                if (JServ.log.active)
                    JServ.log.log(CH_DEBUG, "Restoring sessions hashtable.");

            } 
            catch (Exception e) {
                if (JServ.log.active)
                    JServ.log.log(CH_DEBUG, "Restoring sessions hashtable failed:" + e.toString());
                                    
                sessions = new Hashtable();
            }
        } else {
            if (JServ.log.active)
                JServ.log.log(CH_DEBUG, "Creating new sessions hashtable.");

            sessions = new Hashtable();
        }

        // Load startup servlets
        loadStartupServlets(errorHandler);

        // Start housekeeping thread
        Thread housekeeping = new Thread(this);
        housekeeping.setDaemon(true);
        housekeeping.start();
    }

    /**
     * Reinstantiate the classloader if necessary.  Check if any of
     * the classes has changed or if the property file has been modified.
     * If this is the case, this method does the following :
     * <ol>
     * <li>Destroy all the loaded servlets.
     * <li>Re-read its configuration file.
     * <li>Reload the startup servlets.
     * </ol>
     *
     * @param errorHandler The object that knows what to do with errors.
     */
    public synchronized void checkReload(JServSendError errorHandler) {
        if ((checkClasses && loader.shouldReload()) ||
            (checkFile && confFile.lastModified() != lastInitialization)) {

            if (!JServ.TURBO && JServ.log.active) {
                JServ.log.log(CH_INFO, "Re-initing ServletManager "
                    + name);
            }

            destroyServlets();

            // Create a new class loader so that the class
            // definitions for this servlet and all the
            // classes it depends upon, aside from system
            // classes, are reloaded.
            loader = loader.reinstantiate();

            // Reread configuration and load servlets
            init(errorHandler);
        }
    }

    /**
     * Get all the name that are defined in this ServletManager
     */
    public Enumeration getServletNames() {
        return servletNames.elements();
    }

    /**
     * Get an enumeration of all the servlets that have been loaded.
     */
    public synchronized Enumeration getLoadedServlets() {
        Vector servlets = new Vector();
        Enumeration loadedServlets = servletContexts.elements();
        while (loadedServlets.hasMoreElements()) {
            Object tmp = loadedServlets.nextElement();
            if (tmp instanceof JServContext) {
                servlets.addElement(((JServContext) tmp).servlet);
            } else if (tmp instanceof JServSTMStore) {
                Servlet svls[] = ((JServSTMStore) tmp).getServlets();
                for (int i = svls.length - 1; i >= 0; i--) {
                    servlets.addElement(svls[i]);
                }
            }
        }
        return servlets.elements();
    }

    /**
     * Loads and initialize a servlet.  If the servlet is already
     * loaded and initialized, a reference to the existing context
     * is returned.
     *
     * @return the ServletContext object for the servlet.
     * @param servletName The name of the servlet to load.
     * @param errorHandler The error handler to call back if there is an error.
     * @exception ServletException If there is an error while initializing the
     * servlet.
     */
    public synchronized JServContext loadServlet(String name, JServSendError se)
        throws ServletException
    {
        // Check whether the servlet is already loaded, initialized,
        // and cached.
        JServContext context = null;
        Servlet servlet;

        Object tmp = servletContexts.get(name);
        if (tmp != null) {
            if (tmp instanceof JServContext) {
                return (JServContext) tmp;
            } else if (tmp instanceof JServSTMStore) {
                return ((JServSTMStore) tmp).getContext(se);
            } else {
                // what have we in the servletContexts hash?
                return null;
            }
        }

        // ask the class loader to load and init the servlet
        context = load_init(name, se);
        if (context == null) {
            return null;
        }

        if (context.servlet != null
            && context.servlet instanceof SingleThreadModel) {
            JServSTMStore store =
                new JServSTMStore(confs, this, name, se, context);
            context = store.getContext(se);
            // Add the store to the cache
            servletContexts.put(name, store);
        } else {
            // Add the servlet to the cache, so we can use it again
            servletContexts.put(name, context);
        }

        return context;
    }


    protected JServContext load_init(String name, JServSendError se)
        throws ServletException
    {
        JServContext context = null;
        Servlet servlet;

        // Find the servlet's full name if an alias
        String sdname = "servlet." + name;
        String classname = confs.getString(sdname + ".code", null);
        boolean isAlias = true;
        if (classname == null) {
            classname = name;
            isAlias = false;
        }

        // Load the servlet
        try {
            servlet = (Servlet) loader.loadClass(classname).newInstance();
        } catch(NoClassDefFoundError e) {
            se.sendError(HttpServletResponse.SC_NOT_FOUND, "NoClassDefFoundError: " + classname);
            return null;
        } catch(ClassNotFoundException e) {
            se.sendError(HttpServletResponse.SC_NOT_FOUND, "ClassNotFoundException: " + classname);
            return null;
        } catch(ClassFormatError e) {
            se.sendError(e);
            return null;
        } catch(IllegalAccessException e) {
            se.sendError(e);
            return null;
        } catch(InstantiationException e) {
            se.sendError(e);
            return null;
        }

        // Setup the init parameters
        Properties initargs = confs.getProperties(sdname + ".initArgs", defaultArgs);

        // Try to load a property file classname.initArgs
        try {
            InputStream argsIn =
                loader.getResourceAsStream(classname.replace('.', 
                    File.separatorChar) + ".initArgs");
            if (argsIn != null) {
                try {
                    initargs.load(new BufferedInputStream(argsIn));
                } finally {
                    argsIn.close();
                }
            }
        } catch(IOException ignored) {}

        // Init the servlet
        // TODO: implement timeout on init() execution.
        try {
            context = new JServContext(servlet, this, initargs, (isAlias?name:null));
            if (!JServ.TURBO && JServ.log.active) {
                JServ.log.log(CH_DEBUG, "Initializing servlet: '" 
                    + name + "' ; initArgs: '" + initargs + "'");
            }
            servlet.init(context);
        } catch (ServletException initError) {
            throw initError;
        } catch(Exception e) {
            // Something happened.
            se.sendError(e);
            return null;
        } catch(Error e) {
            // Something really bad happened...
            se.sendError(e);
            throw (Error)e.fillInStackTrace();
        }

        return context;
    }

    /**
     * Loads and initialize all the startup servlets.
     *
     * @param se The sendError handler to call back in case of error.
     */
    private void loadStartupServlets(JServSendError se) {
        if (startups == null) {
            return;
        }

        for(int i = 0; i < startups.length; i++) {
            String servname = startups[i];

            if (servname == null) {
                continue;
            }

            try {
                loadServlet(servname, se);
            } catch (ServletException initError) {
                se.sendError(initError);
            }
        }
    }

    /**
     * Get the name of this ServletManager.
     */
    public String getName() {
        return name;
    }

    /**
     * Return a context into a set of SingleThredModel servlets.
     */
    void returnSTMS(String servletname, JServContext context) {
        if (servletname != null && context != null) {
            Object tmp = servletContexts.get(servletname);
            if (tmp != null && tmp instanceof JServSTMStore) {
                ((JServSTMStore) tmp).returnContext(context);
            }
        }
    }

    /**
     * Destroy one servlet or a set of SingleThreadModel servlets.
     *
     * @param servletName the name of the servlet
     */
    public synchronized void destroyServlet(String servletName) {

        if (servletName == null) {
            return;
        }

        Object tmp = servletContexts.get(servletName);

        JServContext contexts[];
        if (tmp == null) {
            return;
        } else if (tmp instanceof JServContext) {
            contexts = new JServContext[1];
            contexts[0] = (JServContext) tmp;
        } else if (tmp instanceof JServSTMStore) {
            contexts = ((JServSTMStore) tmp).clear();
            if (JServ.log.active) {
                JServ.log.log(CH_DEBUG,
                    "Destroying a set of SingleThreadModel servlets");
            }
        } else {
            return;
        }

        for (int i = contexts.length - 1; i >= 0; i--) {
            JServContext context = contexts[i];
	    
            try {
                // Wait until all pending requests have completed
                // or timeout expires.
                try {
                    context.lock.writeLock(destroyTimeout);
                } catch (InterruptedException stop) {
                    if (JServ.log.active) {
                        JServ.log.log(CH_WARNING,
                            "Caught interrupted exception while waiting for "
                             + servletName + ". Skipping destroy().");
                    }
                    continue;
                } catch (TimeoutException stilllocked) {
                    if (JServ.log.active) {
                        JServ.log.log(CH_WARNING,
                            "Timeout for servlet " + servletName +
                            " expired. Probable deadlock. Skipping destroy().");
                    }
                    continue;
                }

                // Destroy the servlet
                try {
                    if (JServ.log.active) {
                        JServ.log.log(CH_INFO,
                            "Destroying servlet " + servletName);
                    }
                    context.servlet.destroy();
                } catch(Exception e) {
                    JServ.log.log( null, e );
                } catch(Error e) {
                    JServ.log.log( null, e );
                    throw e; // too bad to continue
                } finally {
                    context.lock.writeUnlock();
                }
            } finally {
                servletContexts.remove(servletName);
            }
        }
    }

    /**
     * Destroy all the servlets and servlet contexts.
     */
    public synchronized void destroyServlets() {
        if (JServ.log.active) {
            JServ.log.log(CH_INFO, "Destroying Servlets");
        }

        // copy the name of the servlets in a separate array.
        // we need to do this because in destroyServlet() the servlet
        // is removed from the servletContexts and we must not
        // mess the things walking through the hash with an enumerator.
        String[] tmpServlets = new String[servletContexts.size()];
        int i = 0;
        Enumeration contextEnum = servletContexts.keys();
        while (contextEnum.hasMoreElements()) {
          tmpServlets[i++] = (String)contextEnum.nextElement();
        }
        i--;

        try {
          while (i >= 0) {
            destroyServlet(tmpServlets[i--]);
          }
        } finally {
          //Make sure that the servlet tables is empty
          servletContexts.clear();
        }
    }

    //-----------------------------Static utility methods for session handling

    /**
     * Get the session identifier in a query  string.
     *
     * @param queryStr The query string that came in from the url.
     * @return The session identifier encoded in the url, or null if there
     * is no session identifier in the url.
     */
     public final String getUrlSessionId(String queryStr) {
        if (queryStr == null) {
            return null;
        }

        try {
            Hashtable params = HttpUtils.parseQueryString(queryStr);
            Object o = params.get(session_identifier);
            if (o == null) {
                return null;
            } else if (o instanceof String) {
                return (String) o;
            } else {
                return ((String[]) o)[0];
            }
        } catch (IllegalArgumentException badquerystr) {
            return null;
        }
    }

    /**
     * Get the session identifier set in cookies.
     *
     * @param cookies The cookies to search for a session identifier.
     * @return The session identifier found in the cookies, or null if there
     * is none.
     */
    public final String getCookieSessionId(Cookie[] cookies) {
	/*
	 * If we should not use cookies we may not look for a
	 * SessionID in the given cookies: If the browser still sends
	 * a SessionID as cookie (from an earlier session).
	 */
	if (!sessionUseCookies)
	    return null;

        if (cookies == null || cookies.length == 0) {
            return null;
        }

        for (int i = 0; i < cookies.length; i++) {
            if (cookies[i].getName().equals(session_identifier)) {
                return cookies[i].getValue();
            }
        }

        return null;
    }

    /**
     * Encode a URL with a session identifier.
     *
     * @param url The url to encode.
     * @param id The session identifier to encode with the url.
     */
    public String encodeUrl (String url, String id) {
        int hashPos = url.indexOf('#');
        // do not rewrite URL's referring to the current document
        // (they start with the '#')
        if (hashPos == 0)
            return url;
        
        // if we have a ? in the URL, there are already
        // GET parameters attached to the URL, so we've
        // to append our parameters with '&'
        int questPos = url.indexOf('?');
        boolean addonParameter = (questPos >= 0);
        
        int insertPos = url.length ();
        /*
         * GET Parameters should be placed _before_ the #subsection part of
         * the URL, so correct the position we found (end of URL) here.
         *
         * If there are already  GET parameters attached (see '?' test 
         * before)
         * to the URL _after_ the #-part, 
         * ( HREF="foobar.html#subsect?bar=baz" 
         *                 instead of
         *   HREF="foobar.html?bar=baz#subsect")
         * do _not_ correct position in this case.
         * (note that, if there is no GET parameter in the first place, 
         * questPos=-1)
         */
        if (hashPos > 0 && questPos < hashPos) {
            // move backwards the length of the subsection part
            insertPos -= (url.length() - hashPos);
        }
        StringBuffer urlBuffer = new StringBuffer (url);
        urlBuffer.insert (insertPos++, addonParameter ? '&' : '?');
        urlBuffer.insert (insertPos, session_identifier + '=' + id);
        return urlBuffer.toString();
    }

    // this should probably be put in an external class ..
    /*
     * Create a suitable string for session identification
     * Use synchronized count and time to ensure uniqueness.
     * Use random string to ensure timestamp cannot be guessed
     * by programmed attack.
     *
     * format of id is <6 chars random><3 chars time><1 char count%36>
     */
    static private int session_count = 0;
    static private java.util.Random randomSource = new java.util.Random();

    // MAX_RADIX is 36
    /*
     * we want to have a random string with a length of
     * 6 characters. Since we encode it BASE 36, we've to
     * modulo it with the following value:
     */
    public final static long maxRandomLen = 2176782336L; // 36 ** 6

    /*
     * The session identifier must be unique within the typical lifespan
     * of a Session, the value can roll over after that. 3 characters:
     * (this means a roll over after over an day which is much larger
     *  than a typical lifespan)
     */
    public final static long maxSessionLifespanTics = 46656; // 36 ** 3

    /*
     *  millisecons between different tics. So this means that the
     *  3-character time string has a new value every 2 seconds:
     */
    public final static long ticDifference = 2000;

    // ** NOTE that this must work together with get_jserv_session_balance()
    // ** in jserv_balance.c
    static synchronized private String getIdentifier () {
	StringBuffer sessionId = new StringBuffer();

	// random value ..
	long n = randomSource.nextLong();
	if (n < 0) n = -n;
	n %= maxRandomLen;
	// add maxLen to pad the leading characters with '0'; remove
	// first digit with substring.
	sessionId.append (Long.toString(n+maxRandomLen, Character.MAX_RADIX)
			  .substring(1));

	long timeVal = (System.currentTimeMillis() / ticDifference)
	    % maxSessionLifespanTics + maxSessionLifespanTics;
	
	sessionId.append (Long.toString (timeVal, Character.MAX_RADIX)
			  .substring(1));

	String random = Long.toString(randomSource.nextLong());
	String time = Long.toString(System.currentTimeMillis());
        
        // we append the session count, mod 36 to this string. So we
        // don't expose the real session count to the world (which may
        // be a problem on some sites). If the server generates less than
        // 36 sessions per 2 seconds, the last 4 characters are uniqe for
        // itself ..
	session_count = (session_count + 1) % Character.MAX_RADIX;
	sessionId.append (Long.toString (session_count, Character.MAX_RADIX));

	return sessionId.toString();
    }

    synchronized private String getIdentifier(String jsIdent) {
      if (jsIdent != null && jsIdent.length() > 0) {
        return getIdentifier()+"."+jsIdent;
      }
      return getIdentifier();
    }

    //----------------------------------- Implementation of HttpSessionContext

    /**
     * Returns the session bound to the specified session ID.
     *
     * @param sessionID the ID of a particular session object.
     * @return the session name. Returns null if the session ID does not refer
     * to a valid session.
     */
    public synchronized HttpSession getSession(String sessionId) {
        return (HttpSession) sessions.get(sessionId);
    }

    /**
     * Returns an enumeration of all of the session IDs in this context.
     *
     * @return an enumeration of all session IDs in this context.
     */
    public synchronized Enumeration getIds() {
        Vector ids = new Vector();
        Enumeration idEnum = sessions.keys();
        while (idEnum.hasMoreElements()) {
            ids.addElement( idEnum.nextElement() );
        }
        return ids.elements();
    }

    /**
     * Creates a new session.
     *
     * @param response The response used to send a cookie to the client.
     * @return A new session.
     */
    public synchronized JServSession createSession(HttpServletResponse response) {
        return createSession(response, null);
    }
    
    /**
     * Creates a new session.
     *
     * @param response The response used to send a cookie to the client.
     * @param route Label to append to the id sent from jserv client.
     * @return A new session.
     */
    public synchronized JServSession createSession(HttpServletResponse response, String route) {
        JServSession s = new JServSession(getIdentifier(route), this);
        sessions.put(s.id, s);
        if (this.sessionUseCookies) {
          Cookie c = new Cookie(session_identifier, s.id);

          // Removed to avoid BUG #2593 even if changing the behavior from
          // virtual hosts to servlet zones already solved that problem.
          // I don't know if a domain is ever needed in a cookie and if
          // it is possible to set that domain using the servlet zone 
          // instead of virtual hosts (that we don't know since multiple 
          // hosts may share the same servlet zone)
          // c.setDomain(name);
          c.setPath("/");
          response.addCookie(c);
        }
        if (!JServ.TURBO && JServ.log.active)
          JServ.log.log(CH_DEBUG, "Created session: " + s.id);
        return s;
    }

    /**
     * Remove a session from the context. This is called by the session
     * when it is invalidated.
     *
     * @param s The session to remove from this context.
     */
    public synchronized void removeSession(JServSession s) {
        sessions.remove(s.id);
    }

    /**
     * The housekeeping thread
     * Checks for sessions that have not been used for a certain
     * amount of time and invalidates them.
     */
    public void run() {
        Enumeration sesses;
        JServSession sess;
        long sysMillis;

        while(true) {
            // sleep for 5 seconds.
            try {
                Thread.sleep(sessionCheckFrequency);
            } catch(InterruptedException exc) { }

            // walk through all sessions and invalidate old ones
            sesses = sessions.elements();
            sysMillis = System.currentTimeMillis();            
            while(sesses.hasMoreElements()) {
                sess = (JServSession) sesses.nextElement();
                synchronized (sess) {
                    try {
                        if ((sysMillis - sess.lastAccessTime > sessionTimeout) ||
                               ((sess.isNew()) && 
                               (sysMillis - sess.lastAccessTime > newSessionTimeout))) {
                                sess.invalidate();
                        }
                    }
                    catch (IllegalStateException ignored) {}
                }
            }
        }
    }
}
