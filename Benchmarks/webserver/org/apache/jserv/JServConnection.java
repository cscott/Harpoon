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
import java.text.*;
import javax.servlet.*;
import javax.servlet.http.*;
import org.apache.java.io.*;
import org.apache.java.lang.*;

/**
 * This class is the thread that handles all communications between
 * the Java VM and the web server.
 *
 * <b>Note:</b> this class is highly optimized for speed and we
 * sacrificed some code readability for that. We already know this
 * code is a mess and it's a huge class with C-like design style.
 * So, we apologize for the lack of elegance in this code but we
 * hope you'll appreciate its stability and performance. (SM)
 *
 * @author Alexei Kosut
 * @author Francis J. Lacoste
 * @author Stefano Mazzocchi
 * @author Vadim Tkachenko
 * @author Ed Korthof
 * @author Michal Mosiewicz
 * @version $Revision: 1.1 $ $Date: 2000-07-18 22:01:52 $
 **/
public class JServConnection
implements Stoppable, HttpServletRequest, HttpServletResponse,
        JServSendError, JServLogChannels {

    protected Socket client;
		//protected Ajpv12InputStream in;
	protected InputStream in;
    protected OutputStream out;

    // Servlet setup stuff
    protected JServServletManager mgr;
    protected JServContext context;
    protected Servlet servlet;
    protected String servletmethod;
    protected String servletname;
    protected String servletzone;
    protected String hostname;
    protected boolean stm_servlet;

    protected String auth = null;
    protected int signal = -1;

    protected JServInputStream servlet_in;
    protected JServOutputStream servlet_out;

    protected BufferedReader servlet_reader;
    protected boolean called_getInput = false;

    protected PrintWriter servlet_writer;
    protected boolean called_getOutput = false;

    // HTTP Stuff
    protected Hashtable headers_in = new Hashtable(10, 0.9f);
    protected Hashtable headers_out = new Hashtable(15, 0.9f);
    // Hashtable which ignores put()'s of 'null':
    protected Hashtable env_vars = new Hashtable(40, 0.9f) {
	public synchronized Object put(Object key,
				       Object value) {
	    if (key == null || value == null) return null;
	    return super.put (key, value);
	}
    };
    
    protected Cookie[] cookies_in;
    protected Vector cookies_out = new Vector(5);

    protected int status = SC_OK;
    protected String status_string = null;

    protected Hashtable params = null;
    protected boolean got_input = false;

    protected boolean sent_header = false;

    // Session stuff
    protected JServSession session;
    protected String requestedSessionId;
    protected boolean idCameAsCookie = false;
    protected boolean idCameAsUrl = false;

    private Semaphore semaphore;
    
    /**
     * Initalize the connection handler with the client socket.
     */
    public void init(Socket clientSocket, Semaphore s) {
        this.client = clientSocket;
        this.semaphore = s;
        try {
            this.in = client.getInputStream();
            this.out = new BufferedOutputStream(client.getOutputStream(),2048);
        } catch(IOException e) {
            try {
                client.close();
            } catch(IOException ignored) {}
            if (JServ.log.active)
				JServ.log.log(CH_CONTAINER_EXCEPTION, "Exception while getting socket streams: " + e);
        }
    }
    
    /**
     * This methods provides and incapsulates the Servlet service.
     */
    public void run() {
        if (!JServ.TURBO && JServ.log.active)
			JServ.log.log(CH_DEBUG, "Initializing servlet request");

        if (!JServ.TURBO && JServ.log.active)
			JServ.log.log(CH_DEBUG, "Reading request data");
		try {
			semaphore.entry();
			try {
				readData();
			} catch (Exception e) {
				sendError(SC_BAD_REQUEST, "Malformed data sent to JServ");
				return;
			}
			processRequest();			
		} finally {
			semaphore.exit();
		}
    }

    protected void processRequest() {
        // override environemnt values
        this.env_vars.put("GATEWAY_INTERFACE", JServDefs.PACKAGE + "/" + JServDefs.VERSION);
		this.env_vars.put("SERVER_SOFTWARE",  JServDefs.PACKAGE + "/" + JServDefs.VERSION);

        // Look for the servlet zone
        if (servletzone == null) {
            // If servletzone is null, probably the client asks for
            // some system class so we pick the first available
            // servlet zone.
            servletzone = (String) JServ.servletManagerTable.keys().nextElement();
        }
		
        // Look for the hostname
        if (hostname == null) {
            sendError(SC_BAD_REQUEST, "Received empty host name");
            return;
        }

        // Find the servlet manager that handles this zone
        mgr = (JServServletManager) JServ.servletManagerTable.get(servletzone);

        if (mgr == null) {
            sendError(SC_NOT_FOUND, "Servlet zone \"" + servletzone + "\" not found.");
            return;
        }

        // Parse the cookies
        if (!JServ.TURBO && JServ.log.active) JServ.log.log(CH_DEBUG, "Parsing cookies");
        cookies_in = JServUtils.parseCookieHeader(getHeader("Cookie"));

        // Now to the session stuff
        requestedSessionId = mgr.getUrlSessionId(getQueryString());

        idCameAsUrl = (requestedSessionId != null);
        String cookieSessionId = mgr.getCookieSessionId(cookies_in);
        idCameAsCookie = (cookieSessionId != null);

        // FIXME: What do we do if url and cookie don't have same id ?
        requestedSessionId = (requestedSessionId == null)
            ? cookieSessionId
            : requestedSessionId;

        if (requestedSessionId != null) {
            if (!JServ.TURBO && JServ.log.active) JServ.log.log(CH_DEBUG, "Request is in session " + requestedSessionId);
            JServSession s = (JServSession) mgr.getSession(requestedSessionId);
            if ((requestedSessionId != null) && (s != null)) {
                s.access();
            }
        }

        try {
            // Let the mgr check for changes.
            mgr.checkReload(this);
        } catch (IllegalArgumentException ex) {
            // this exception may be thrown by JServClassLoader when
            // it's repository was altered in a dangerous way
            // (removed directories or zip/jar files; corrupted zip/jar
            // archives etc.)
            sendError(ex);
            return;
        }

		if (mgr.modules == false) {

				// Look for the servlet
			if (servletname == null) {
				sendError(SC_BAD_REQUEST, "Received empty servlet name");
				return;
			}
			
			try {
				context = mgr.loadServlet(servletname, this);
			} catch ( ServletException initError ) {
				sendError(SC_INTERNAL_SERVER_ERROR,
						  "Initialization error while loading the servlet: "
						  + initError.getMessage());
			}
			
			if ((context == null) || (context.servlet == null)) {
				sendError(SC_INTERNAL_SERVER_ERROR,
						  "An unknown error occured loading the servlet.");
				return;
			}
			
			servlet = context.servlet;
			
				// is this a SingleThreadModel servlet?
			boolean stm_servlet = servlet instanceof SingleThreadModel;
			if (!JServ.TURBO && stm_servlet && JServ.log.active) {
				JServ.log.log(CH_DEBUG,
							  "We've got a SingleThreadModel servlet.");
			}
			
				// Set up a read lock on the servlet. Note that anytime
				// we return, we need to make sure to unlock this. Otherwise,
				// we'll end up holding onto the lock forever. Oops.
				// This is done in the finally clause.
				// The lock is acquired outside of the try block so that
				// we do not unlock a lock that was never acquired.
			try {
				context.lock.readLock();
			} catch (InterruptedException stop) {
				if (!JServ.TURBO && JServ.log.active) JServ.log.log(CH_CONTAINER_EXCEPTION, "Caught interrupted exception while waiting for servlet " + servletname + ": sending error.");
				sendError(SC_INTERNAL_SERVER_ERROR, "InterruptedException while waiting for servlet lock.");
				if (stm_servlet) {
					if (!JServ.TURBO && JServ.log.active) JServ.log.log(CH_DEBUG, "Returning the SingleThreadModel servlet.");
					mgr.returnSTMS(servletname, context);
				}
				return;
			}
			
			try {
					// Set up the servlet's I/O streams
				servlet_in = new JServInputStream(getContentLength(), in);
				servlet_out = new JServOutputStream(out,in);
				
					// Start up the servlet
				try {
					if (!JServ.TURBO && JServ.log.active) JServ.log.log(CH_DEBUG, "Calling service()");
					servlet.service(this, this);
				} catch(Exception e) {
					sendError(e);
					return;
				} catch(Error e) {
					sendError(e);
					throw (Error) e.fillInStackTrace();
				}
				
					// Make sure we've send the HTTP header, even if no
					// entity data has been
					//
					// - KNOWN BUG - After the header has been sent to Apache
					// all other headers are lost (and cookies).
					// - FIXME - The new protocol AJPv2.1 will fix this.
				sendHttpHeaders();
				
					// All done close the streams and the connection
				try {
					if (servlet_writer != null) { 
						servlet_writer.close();
					}
					servlet_out.close();
					if(client!=null) client.close();
				} catch(IOException ignored) {
					if (!JServ.TURBO && JServ.log.active) JServ.log.log(CH_DEBUG, "Ignored IOException.");
				}
				
			} finally {
					// Clean up
				context.lock.readUnlock();
				if (stm_servlet) {
					if (!JServ.TURBO && JServ.log.active) JServ.log.log(CH_DEBUG, "Returning the SingleThreadModel servlet.");
					mgr.returnSTMS(servletname, context);
				}
				
					// Profiler stuff
				if (JServ.PROFILE) {
						// Runtime.getRuntime().traceInstructions(false);
					Runtime.getRuntime().traceMethodCalls(false);
				}
			}			
		}
		
		else if ((mgr.modules) == true && (mgr.module_list != null)) {
			
				// fixup the path info.. modules are not named on url
			if (servletname == null)
				servletname = new String("");
			if (env_vars.get("PATH_INFO") != null)
				env_vars.put("PATH_INFO", "/" + servletname + env_vars.get("PATH_INFO"));
			else
				env_vars.put("PATH_INFO", "/" + servletname);
			
			servlet_in = new JServInputStream(getContentLength(), in);
			servlet_out = new JServOutputStream(out,in);
			for(int i = 0; i < mgr.module_list.length; i++) {
				servletname = mgr.module_list[i];
				if (servletname == null)
					continue;
				
				try {
					context = mgr.loadServlet(servletname, this);
				} catch ( ServletException initError ) {
					sendError(SC_INTERNAL_SERVER_ERROR,
							  "Initialization error while loading the servlet: "
							  + initError.getMessage());
				}
				
				if ((context == null) || (context.servlet == null)) {
					sendError(SC_INTERNAL_SERVER_ERROR,
							  "An unknown error occured loading the servlet.");
					return;
				}

				servlet = context.servlet;
				
					// is this a SingleThreadModel servlet?
				boolean stm_servlet = servlet instanceof SingleThreadModel;
				if (!JServ.TURBO && stm_servlet && JServ.log.active) {
					JServ.log.log(CH_DEBUG,
								  "We've got a SingleThreadModel servlet.");
				}
				
					// Set up a read lock on the servlet. Note that anytime
					// we return, we need to make sure to unlock this. Otherwise,
					// we'll end up holding onto the lock forever. Oops.
					// This is done in the finally clause.
					// The lock is acquired outside of the try block so that
					// we do not unlock a lock that was never acquired.
				try {
					context.lock.readLock();
				} catch (InterruptedException stop) {
					if (!JServ.TURBO && JServ.log.active) JServ.log.log(CH_CONTAINER_EXCEPTION, "Caught interrupted exception while waiting for servlet " + servletname + ": sending error.");
					sendError(SC_INTERNAL_SERVER_ERROR, "InterruptedException while waiting for servlet lock.");
					if (stm_servlet) {
						if (!JServ.TURBO && JServ.log.active) JServ.log.log(CH_DEBUG, "Returning the SingleThreadModel servlet.");
						mgr.returnSTMS(servletname, context);
					}
					return;
				}
				
				try {
					try {
						if (!JServ.TURBO && JServ.log.active) JServ.log.log(CH_DEBUG, "Calling service()");
						servlet.service(this, this);
					} catch(Exception e) {
						sendError(e);
						return;
					} catch(Error e) {
						sendError(e);
						throw (Error) e.fillInStackTrace();
					}

				} finally {
						// Clean up
					context.lock.readUnlock();
					if (stm_servlet) {
						if (!JServ.TURBO && JServ.log.active) JServ.log.log(CH_DEBUG, "Returning the SingleThreadModel servlet.");
						mgr.returnSTMS(servletname, context);
					}
					
						// Profiler stuff
					if (JServ.PROFILE) {
							// Runtime.getRuntime().traceInstructions(false);
						Runtime.getRuntime().traceMethodCalls(false);
					}
				}
			}
			sendHttpHeaders();
			try {
				if (servlet_writer != null) { 
					servlet_writer.close();
				}
				servlet_out.close();
				if(client!=null) client.close();
			} catch(IOException ignored) {
				if (!JServ.TURBO && JServ.log.active) JServ.log.log(CH_DEBUG, "Ignored IOException.");
			}
		}
    }

    /**
     * This method is now empty but will be used when thread timing
     * will be in place to allow graceful signaling of execution timeout.
     */
    public void stop() {}
            
    /**
     * Read all the data.
     */
    protected void readData() throws Exception {
		int ndx;
		String line = null;
		String uri = null;
		String path = null;
		BufferedReader reader;		
        try {
            reader = new BufferedReader(new InputStreamReader(client.getInputStream()));

				/*
				 * parse the request line
				 */
			line = reader.readLine();
			if (line == null) {
				sendError(SC_BAD_REQUEST, "Invalid request");
				return;
			}

			ndx = line.indexOf(' ');
			if (ndx == -1) {
				sendError(SC_BAD_REQUEST, "Invalid request");
				return;
			}
			servletmethod = line.substring(0, ndx);
			line = line.substring(ndx + 1);
			
			ndx = line.indexOf(' ');
			if (ndx == -1) {
				uri = line;
				env_vars.put("SERVER_PROTOCOL", "HTTP/1.0");
			} else {
				uri = line.substring(0, ndx);
				env_vars.put("SERVER_PROTOCOL", line.substring(ndx+1));
			}
            env_vars.put("REQUEST_METHOD", servletmethod);
            env_vars.put("REQUEST_URI", uri);
			
			ndx = uri.indexOf('?');
			if (ndx != -1)
				env_vars.put("QUERY_STRING", uri.substring(ndx+1));			
			
				/*
				 * lookup zone and path info
				 */
			path = JServ.findZonePath(uri);
			servletzone = (String)JServ.zoneMap.get(path);
			if (servletzone == null) {
				sendError(SC_BAD_REQUEST, "No Zone for Path");
				return;
			}

			if (path.compareTo("DEFAULT") == 0) { //defaut zone
				path = "/";
			}

			if (!path.endsWith("/"))
				path = path.concat("/");

			if (path.length() > uri.length()) {
				sendError(SC_BAD_REQUEST, "Invalid Zone for Path");
				return;
			}
			
			servletname = uri.substring(path.length());
			ndx = servletname.indexOf('/');
			if (ndx != -1)
				servletname = servletname.substring(0, ndx);

			if (path.endsWith("/")) {
				env_vars.put("SCRIPT_NAME", path + servletname);
				ndx = path.length() + servletname.length();
			} else {
				env_vars.put("SCRIPT_NAME", path  + servletname);
				ndx = path.length() + servletname.length() + 1;
			}

			if (uri.length() <= ndx) {
				env_vars.put("PATH_INFO", null);
				env_vars.put("PATH_TRANSLATED", null);
			} else {
				env_vars.put("PATH_INFO", getRequestURI().substring(ndx));
				env_vars.put("PATH_TRANSLATED", getRequestURI().substring(ndx));
			}

				/*
				 * socket related info
				 */
			hostname = client.getLocalAddress().getHostName();
			env_vars.put("SERVER_NAME", hostname);
			env_vars.put("SERVER_PORT", "" + client.getLocalPort());
            env_vars.put("REMOTE_ADDR", client.getInetAddress().getHostAddress());
            env_vars.put("REMOTE_HOST", "" + client.getPort());
			env_vars.put("AUTH_TYPE", null);
			
				/*
            env_vars.put("DOCUMENT_ROOT", in.readString(""));
            env_vars.put("REMOTE_USER", in.readString(null));
            env_vars.put("REMOTE_PORT", in.readString(""));
            env_vars.put("SCRIPT_FILENAME", in.readString(""));
            env_vars.put("SERVER_SIGNATURE", in.readString(""));
            env_vars.put("JSERV_ROUTE", in.readString(""));     
            env_vars.put("SSL_CLIENT_DN", in.readString(""));     
            env_vars.put("SSL_CLIENT_IDN", in.readString(""));     
				*/


				/*
				 * process http headers
				 */
			line = reader.readLine();
			while(line != null) {
				String h,v;
				ndx = line.indexOf(':');
				if (ndx == -1) break;
			
				h = line.substring(0, ndx).toLowerCase();
				v = line.substring(ndx + 1);
				headers_in.put(h, v);
				if (h.compareTo("content-type") == 0)
					env_vars.put("CONTENT_TYPE", v);
				line = reader.readLine();
			}
		} catch (Exception e) {
			if (JServ.log.active) {
				JServ.log.log(CH_CONTAINER_EXCEPTION, "AJP Protocol Error: " + e);
			}
			throw e;
		}
	}
    
    /**
     * Send the HTTP headers and prepare to send response.
     */
    protected void sendHttpHeaders() {
        if (sent_header) {
            return;
        } else {
            sent_header = true;
        }

        // Use a PrintWriter around the socket out.
        PrintWriter printOut = new PrintWriter(this.out);

        if (!JServ.TURBO && JServ.log.active) {
            JServ.log.log(CH_DEBUG, "Sending response headers.");
        }

        // Send the status info
        if (status_string == null) {
            status_string = findStatusString(status);
        }

        String statusLine = "Status: " + status + " " + status_string;
		printOut.print("HTTP/1.1 " + status + " " + status_string + "\r\n");
        printOut.print(statusLine + "\r\n");
        if (!JServ.TURBO && JServ.log.active) {
            JServ.log.log(CH_DEBUG, statusLine);
        }

        if (headers_out != null) {
            // Send the headers
            for (Enumeration e = headers_out.keys(); e.hasMoreElements();) {
                String key = (String) e.nextElement();
                String hdr = key + ": " + headers_out.get(key);
                printOut.print(hdr + "\r\n");
                if (!JServ.TURBO && JServ.log.active) {
                    JServ.log.log(CH_DEBUG, hdr);
                }
            }
        }

        // Send the cookies
        Enumeration cookies = cookies_out.elements();
        while (cookies.hasMoreElements()) {
            Cookie cookie = (Cookie) cookies.nextElement();
            String cookieHdr = "Set-Cookie: " +
                JServUtils.encodeCookie(cookie);
            printOut.print(cookieHdr + "\r\n");
            if (!JServ.TURBO && JServ.log.active) {
                JServ.log.log(CH_DEBUG, cookieHdr);
            }
        }

        // Send a terminating blank line
        printOut.print("\r\n");
        // Flush the PrintWriter
        printOut.flush();
    }

    //---------------------------------------- Implementation of ServletRequest

    /**
     * Return the hostname
     */
    public String getHostName() {
        return hostname;
    }

    /**
     * Returns the JServ Identifier for this server
     * it is passed in through the env_vars as "JSERV_ROUTE"
     */
    public String getJServRoute() {
     return (String) env_vars.get("JSERV_ROUTE");
    }

    /**
     * Returns the size of the request entity data, or -1 if not
     * known. Same as the CGI variable CONTENT_LENGTH.
     */
    public int getContentLength() {
        String lenstr = (String) headers_in.get("content-length");

        if (lenstr == null) {
            return -1;
        }

        try {
            return Integer.parseInt(lenstr);
        } catch(NumberFormatException e) {
            return -1;
        }
    }

    /**
     * Returns the Internet Media Type of the request entity data,
     * or null if not known. Same as the CGI variable
     * CONTENT_TYPE.
     */
    public String getContentType() {
        return (String) headers_in.get("content-type");
    }

    /**
     * Returns the protocol and version of the request as a string of
     * the form <code>&lt;protocol&gt;/&lt;major version&gt;.&lt;minor
     * version&gt</code>.  Same as the CGI variable SERVER_PROTOCOL.
     */
    public String getProtocol() {
        return (String) env_vars.get("SERVER_PROTOCOL");
    }

    /**
     * Returns the scheme of the URL used in this request, for example
     * "http", "https", or "ftp".  Different schemes have different
     * rules for constructing URLs, as noted in RFC 1738.  The URL used
     * to create a request may be reconstructed using this scheme, the
     * server name and port, and additional information such as URIs.
     */
    public String getScheme() {
 
        // FIXME: We just don't have this information available. We'll
        // look at the port, and return https if it's 443, but that's
        // not a real solution.
        // Mike: There is no generic solution available in apache
        // let's leave it as below and wait.
        if (getServerPort() == 443) {
            return "https";
        } else {
            return "http";
        }
    }

    /**
     * Returns the host name of the server that received the request.
     * Same as the CGI variable SERVER_NAME.
     */
    public String getServerName() {
        return (String) env_vars.get("SERVER_NAME");
    }

    /**
     * Returns the port number on which this request was received.
     * Same as the CGI variable SERVER_PORT.
     */
    public int getServerPort() {
        String portstr = (String) env_vars.get("SERVER_PORT");
        int port;

        if (portstr == null) {
            return -1;
        }

        try {
            port = Integer.parseInt(portstr);
        } catch(NumberFormatException e) {
            return -1;
        }

        return port;
    }

    /**
     * Returns the IP address of the agent that sent the request.
     * Same as the CGI variable REMOTE_ADDR.
     */
    public String getRemoteAddr() {
        return (String) env_vars.get("REMOTE_ADDR");
    }

    /**
     * Returns the fully qualified host name of the agent that sent the
     * request. Same as the CGI variable REMOTE_HOST.
     */
    public String getRemoteHost() {
        return (String) env_vars.get("REMOTE_HOST");
    }

    /**
     * Applies alias rules to the specified virtual path and returns
     * the corresponding real path, or null if the translation can not
     * be performed for any reason.  For example, an HTTP servlet would
     * resolve the path using the virtual docroot, if virtual hosting
     * is enabled, and with the default docroot otherwise.  Calling
     * this method with the string "/" as an argument returns the
     * document root.
     * @param path The virtual path to be translated to a real path.
     */
    public String getRealPath(String path) {
        // FIXME: Make this somehow talk to Apache, do a subrequest
        // and get the real filename. Until then, we just tack the path onto
        // the doc root and hope it's right. *sigh*

        // DOCUMENT_ROOT is not a standard CGI var, although Apache always
        // gives it. So we allow for it to be not present.
        String doc_root = (String) env_vars.get("DOCUMENT_ROOT");

        if (doc_root == null) {
            return null;
        } else {
            return doc_root + path;
        }
    }

    /**
     * Returns an input stream for reading binary data in the request body.
     *
     * @exception IllegalStateException if {@link #getReader getReader} has
     * been called on this same request.
     * @exception IOException n other I/O related errors.
     */
    public ServletInputStream getInputStream() throws IOException {
        if (servlet_reader != null) {
            throw new IllegalStateException(
                "getReader() has already been called.");
        }

        got_input = true;
        called_getInput = true;
        return servlet_in;
    }

    /**
     * Parse parameter stuff.  This code now implements parameter merging
     * when parameters are found in both the query string and the input
     * stream.  In such cases, parameter values retrieved from the query
     * string will be the first one(s) in the result from calling
     * getParameterValues().
     * <p>
     * This method unconditionally creates the <code>params</code>
     * Hashtable.  If a parsing error occurred, the Hashtable will be
     * empty.
     */
    protected void parseParams() {

        // Have we already done it?
        if (params != null) {
            return;
        }

        // Parse any query string parameters from the request
        Hashtable queryParameters = null;
        try {
            queryParameters = HttpUtils.parseQueryString(getQueryString());
        } catch (IllegalArgumentException e) {
            queryParameters = null;
        }

        // Parse any posted parameters in the input stream
        Hashtable postParameters = null;
        // Mozilla sends 
        // Content-type: application/x-www-form-urlencoded; charset=ISO-8859-1
        String contentType = getContentType();
        if ("POST".equals(getMethod()) &&
            contentType != null &&
            contentType.startsWith (
                "application/x-www-form-urlencoded")) {
            try {
                ServletInputStream is = getInputStream();
                postParameters =
                    HttpUtils.parsePostData(getContentLength(), is);
            } catch (IllegalArgumentException e) {
                postParameters = null;
            } catch (IOException e) {
                postParameters = null;
            }
        }

        // Handle the simple cases that require no merging
        if ((queryParameters == null) && (postParameters == null)) {
            params = new Hashtable();
            return;
        } else if (queryParameters == null) {
            params = postParameters;
            return;
        } else if (postParameters == null) {
            params = queryParameters;
            return;
        }

        // Merge the parameters retrieved from both sources
        Enumeration postKeys = postParameters.keys();
        while (postKeys.hasMoreElements()) {
            String postKey = (String) postKeys.nextElement();
            Object postValue = postParameters.get(postKey);
            Object queryValue = queryParameters.get(postKey);
            if (queryValue == null) {
                queryParameters.put(postKey, postValue);
                continue;
            }
            Vector queryValues = new Vector();
            if (queryValue instanceof String) {
                queryValues.addElement(queryValue);
            } else if (queryValue instanceof String[]) {
                String queryArray[] = (String[]) queryValue;
                for (int i = 0; i < queryArray.length; i++) {
                    queryValues.addElement(queryArray[i]);
                }
            }
            if (postValue instanceof String) {
                queryValues.addElement(postValue);
            } else if (postValue instanceof String[]) {
                String postArray[] = (String[]) postValue;
                for (int i = 0; i < postArray.length; i++) {
                    queryValues.addElement(postArray[i]);
                }
            }
            String queryArray[] = new String[queryValues.size()];
            for (int i = 0; i < queryArray.length; i++) {
                queryArray[i] = (String) queryValues.elementAt(i);
            }
            queryParameters.put(postKey, queryArray);
        }
        params = queryParameters;

    }

    /**
     * Returns a string containing the lone value of the specified
     * parameter, or null if the parameter does not exist. For example,
     * in an HTTP servlet this method would return the value of the
     * specified query string parameter. Servlet writers should use
     * this method only when they are sure that there is only one value
     * for the parameter.  If the parameter has (or could have)
     * multiple values, servlet writers should use
     * getParameterValues. If a multiple valued parameter name is
     * passed as an argument, the return value is implementation
     * dependent.
     * @param name the name of the parameter whose value is required.
     * @deprecated Please use getParameterValues
     */
    public String getParameter(String name) {

        parseParams();

        Object val = params.get(name);

        if (val == null) {
            return null;
        } else if (val instanceof String[]) {
            // It's an array, return the first element
            return ((String[])val)[0];
        } else {
            // It's a string so return it
            return (String) val;
        }
    }

    /**
     * Added for the <servlet> tag support - RZ.
     * Provides the ability to add to the request object
     * the parameter of an embedded servlet.
     */
    public void setParameter(String name, String value) {
        // add the parameter in the hashtable, overrides any previous value
        parseParams();

        if(params != null) {
            params.put(name, value);
        }
    }

    /**
     * Returns the values of the specified parameter for the request as
     * an array of strings, or null if the named parameter does not
     * exist. For example, in an HTTP servlet this method would return
     * the values of the specified query string or posted form as an
     * array of strings.
     * @param name the name of the parameter whose value is required.
     */
    public String[] getParameterValues(String name) {

        parseParams();

        Object val = params.get(name);

        if (val == null) {
            return null;
        } else if (val instanceof String) {
            // It's a string, convert to an array and return
            String va[] = {(String) val};
            return va;
        } else {
            // It's an array so return it
            return (String[]) val;
        }
    }

    /**
     * Returns the parameter names for this request as an enumeration
     * of strings, or an empty enumeration if there are no parameters
     * or the input stream is empty.  The input stream would be empty
     * if all the data had been read from the stream returned by the
     * method getInputStream.
     */
    public Enumeration getParameterNames() {

        parseParams();
        return params.keys();
    }

    /**
     * Returns the value of the named attribute of the request, or
     * null if the attribute does not exist.  This method allows
     * access to request information not already provided by the other
     * methods in this interface.  Attribute names should follow the
     * same convention as package names.
     * The following predefined attributes are provided.
     *
     * <TABLE BORDER>
     * <tr>
     *   <th>Attribute Name</th>
     *   <th>Attribute Type</th>
     *   <th>Description</th>
     * </tr>
     * <tr>
     *   <td VALIGN=TOP>javax.net.ssl.cipher_suite</td>
     *   <td VALIGN=TOP>string</td>
     *   <td>The string name of the SSL cipher suite in use, if the
     *       request was made using SSL</td>
     * </tr>
     * <tr>
     *   <td VALIGN=TOP>javax.net.ssl.peer_certificates</td>
     *   <td VALIGN=TOP>array of java.security.cert.X509Certificate</td>
     *   <td>The chain of X.509 certificates which authenticates the client.
     *       This is only available when SSL is used with client
     *       authentication is used.</td>
     * </tr>
     * <tr>
     *   <td VALIGN=TOP>javax.net.ssl.session</td>
     *   <td VALIGN=TOP>javax.net.ssl.SSLSession</td>
     *   <td>An SSL session object, if the request was made using SSL.</td>
     * </tr>
     *
     * </TABLE>
     *
     * <BR>
     * <P>The package (and hence attribute) names beginning with java.*,
     * and javax.* are reserved for use by Javasoft. Similarly, com.sun.*
     * is reserved for use by Sun Microsystems.
     *
     * <p><b>Note</b> The above attributes are not yet implemented by
     * JServ.
     * <p>On the other hand, attribute named
     * "org.apache.jserv.&lt;variable&gt;" returns the content of the
     * environment (CGI) variable "&lt;variable&gt;".
     */
    public Object getAttribute(String name) {
        // We return "org.apache.jserv.<variable>" as the contents
        // of environment (CGI) variable "<variable>"
        if (!name.startsWith("org.apache.jserv.")) {
            return null;
        }

        // interface to get attribute names
        if (name.equals("org.apache.jserv.attribute_names")) {
            return env_vars.keys();
        }

        return env_vars.get(name.substring("org.apache.jserv.".length()));
    }

    /**
     * Returns a buffered reader for reading text in the request body.
     * This translates character set encodings as appropriate.
     * @exception IllegalStateException if getOutputStream has been
     *        called on this same request.
     * @exception IOException on other I/O related errors.
     * @exception UnsupportedEncodingException if the character set encoding
     *  is unsupported, so the text can't be correctly decoded.
     */
    public BufferedReader getReader() throws IOException {
        if (called_getInput) {
            throw new IllegalStateException("Already called getInputStream");
        } else if (servlet_reader == null) {
            // UnsupportedEncodingException is thrown not by
            // getCharacterEncoding, which only parses the content-type header
            // which specifies the encoding, but by the Reader constructor,
            // which possibly cannot support the encoding specified.
            String encoding =
                JServUtils.parseCharacterEncoding(getContentType());
            InputStreamReader reader =
                new InputStreamReader(servlet_in, encoding);
            servlet_reader = new BufferedReader(reader);
            got_input = true;
        }
        return servlet_reader;
    }

    //------------------------------------ Implementation of HttpServletRequest

    /**
     * Gets the array of cookies found in this request.
     * @return the array of cookies found in this request.
     */
    public Cookie[] getCookies() {
        return cookies_in;
    }

    /**
     * Gets the HTTP method (for example, GET, POST, PUT) with which
     * this request was made. Same as the CGI variable REQUEST_METHOD.
     * @return the HTTP method with which this request was made.
     */
    public String getMethod() {
        return servletmethod;
    }

    /**
     * Gets this request's URI as a URL.
     * @return this request's URI as a URL.
     */
    public String getRequestURI() {
        // If the web server's version is available, use it
        String uri = (String) env_vars.get("REQUEST_URI");

        if (uri != null) {
            int queryStringOffset = uri.indexOf('?');
            // Remove any query string at the end
            if (queryStringOffset >= 0) {
                return uri.substring(0, queryStringOffset);
            } else {
                return uri;
            }
        }

        if (getPathInfo() != null) {
            return getServletPath() + getPathInfo();
        } else {
            return getServletPath();
        }
    }

    /**
     * Gets the part of this request's URI that refers to the servlet
     * being invoked. Analogous to the CGI variable SCRIPT_NAME.
     * @return the servlet being invoked, as contained in this
     * request's URI.
     */
    public String getServletPath() {
        return (String) env_vars.get("SCRIPT_NAME");
    }

    /**
     * Gets any optional extra path information following the servlet
     * path of this request's URI, but immediately preceding its query
     * string. Same as the CGI variable PATH_INFO.
     *
     * @return the optional path information following the servlet
     * path, but before the query string, in this request's URI; null
     * if this request's URI contains no extra path information.
     */
    public String getPathInfo() {
        return (String) env_vars.get("PATH_INFO");
    }

    /**
     * Gets any optional extra path information following the servlet
     * path of this request's URI, but immediately preceding its query
     * string, and translates it to a real path.  Same as the CGI
     * variable PATH_TRANSLATED.
     *
     * @return extra path information translated to a real path or null
     * if no extra path information is in the request's URI.
     */
    public String getPathTranslated() {
        return (String) env_vars.get("PATH_TRANSLATED");
    }

    /**
     * Gets any query string that is part of the servlet URI.  Same as
     * the CGI variable QUERY_STRING.
     * @return query string that is part of this request's URI, or null
     * if it contains no query string.
     */
    public String getQueryString() {
        return (String) env_vars.get("QUERY_STRING");
    }

    /**
     * Gets the name of the user making this request.  The user name is
     * set with HTTP authentication.  Whether the user name will
     * continue to be sent with each subsequent communication is
     * browser-dependent.  Same as the CGI variable REMOTE_USER.
     *
     * @return the name of the user making this request, or null if not
     * known.
     */
    public String getRemoteUser() {
        return (String) env_vars.get("REMOTE_USER");
    }

    /**
     * Gets the authentication scheme of this request.  Same as the CGI
     * variable AUTH_TYPE.
     *
     * @return this request's authentication scheme, or null if none.
     */
    public String getAuthType() {
        return (String) env_vars.get("AUTH_TYPE");
    }

    /**
     * Gets the value of the requested header field of this request.
     * The case of the header field name is ignored.
     * @param name the String containing the name of the requested
     * header field.
     * @return the value of the requested header field, or null if not
     * known.
     */
    public String getHeader(String name) {
        return (String) headers_in.get(name.toLowerCase());
    }

    /**
     * Gets the value of the specified integer header field of this
     * request.  The case of the header field name is ignored.  If the
     * header can't be converted to an integer, the method throws a
     * NumberFormatException.
     * @param name  the String containing the name of the requested
     * header field.
     * @return the value of the requested header field, or -1 if not
     * found.
     */
    public int getIntHeader(String name) {
        String hdrstr = (String) headers_in.get(name.toLowerCase());
        if (hdrstr == null) {
            return -1;
        }

        return Integer.parseInt(hdrstr);
    }

    /**
     * Gets the value of the requested date header field of this
     * request.  If the header can't be converted to a date, the method
     * throws an IllegalArgumentException.  The case of the header
     * field name is ignored.
     *
     * <PRE>  From RFC2068:
     *  3.3.1 Full Date
     *
     *
     *   HTTP applications have historically allowed three different formats
     *   for the representation of date/time stamps:
     *
     *    Sun, 06 Nov 1994 08:49:37 GMT  ; RFC 822, updated by RFC 1123
     *    Sunday, 06-Nov-94 08:49:37 GMT ; RFC 850, obsoleted by RFC 1036
     *    Sun Nov  6 08:49:37 1994       ; ANSI C's asctime() format
     *
     *   The first format is preferred as an Internet standard and
     *   represents a fixed-length subset of that defined by RFC 1123
     *   (an update to RFC 822).  The second format is in common use,
     *   but is based on the obsolete RFC 850 [12] date format and
     *   lacks a four-digit year.  HTTP/1.1 clients and servers that
     *   parse the date value MUST accept all three formats (for
     *   compatibility with HTTP/1.0), though they MUST only generate
     *   the RFC 1123 format for representing HTTP-date values in
     *   header fields
     * <pre>
     * @param name  the String containing the name of the requested
     * header field.
     * @return the value the requested date header field, or -1 if not
     * found.
     */
    public long getDateHeader(String name) {
        String val = (String) headers_in.get(name.toLowerCase());
        SimpleDateFormat sdf;

        if ( val == null ) {
            return -1;
        }

        // workaround bug in SimpleDateFormat in pre-JDK1.2b4
        // see: http://developer.java.sun.com
        //      /developer/bugParade/bugs/4106807.html
        val = val + " ";

        sdf = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.US);
        try {
            Date date = sdf.parse(val);
            return date.getTime();
        } catch(ParseException formatNotValid) {
            // try another format
        }

        sdf = new SimpleDateFormat("EEEEEE, dd-MMM-yy HH:mm:ss zzz", Locale.US);
        try {
            Date date = sdf.parse(val);
            return date.getTime();
        } catch(ParseException formatNotValid) {
            // Try another format
        }

        sdf = new SimpleDateFormat("EEE MMMM d HH:mm:ss yyyy", Locale.US);
        try {
            Date date = sdf.parse(val);
            return date.getTime();
        } catch(ParseException formatStillNotValid) {
            throw new IllegalArgumentException(val);
        }
    }

    /**
     * Gets the header names for this request.
     * @return an enumeration of strings representing the header names
     * for this request. Some server implementations do not allow
     * headers to be accessed in this way, in which case this method
     * will return null.
     */
    public Enumeration getHeaderNames() {
        return headers_in.keys();
    }

    /**
     * Gets the current valid session associated with this request, if
     * create is false or, if necessary, creates a new session for the
     * request, if create is true.
     *
     * <p><b>Note</b>: to ensure the session is properly maintained,
     * the servlet developer must call this method (at least once)
     * before any output is written to the response.
     *
     * <p>Additionally, application-writers need to be aware that newly
     * created sessions (that is, sessions for which
     * <code>HttpSession.isNew</code> returns true) do not have any
     * application-specific state.
     *
     * @return the session associated with this request or null if
     * create was false and no valid session is associated
     * with this request.
     */
    public HttpSession getSession( boolean create ) {

        // FIXME: What happen if someone calls invalidate on a new session
        // and calls this method again ? Should we create another one, or
        // return the invalid one ?

        if (session != null && session.isValid()) {
          return session;
        }

        if (requestedSessionId != null) {
          session = (JServSession) mgr.getSession(requestedSessionId);
          if (session != null && session.isValid()) {
            return session;
          }
        }

        if (create == true) {
          String jsRoute = getJServRoute();
          if (jsRoute != null) {
            session = mgr.createSession(this, jsRoute);
          }
          else {
            session = mgr.createSession(this);
          }
          return session;
        }

        return null;
    }

    /**
     * Gets the session id specified with this request.  This may
     * differ from the actual session id.  For example, if the request
     * specified an id for an invalid session, then this will get a new
     * session with a new id.
     *
     * @return the session id specified by this request, or null if the
     * request did not specify a session id.
     * @see #isRequestedSessionIdValid()
     */
    public String getRequestedSessionId() {
        return requestedSessionId;
    }

    /**
     * Checks whether this request is associated with a session that
     * is valid in the current session context.  If it is not valid,
     * the requested session will never be returned from the
     * <code>getSession</code> method.
     * @return true if this request is assocated with a session that is
     * valid in the current session context.
     * @see #getRequestedSessionId()
     */
    public boolean isRequestedSessionIdValid() {
        if (requestedSessionId == null) {
            return false;
        } else {
            return mgr.getSession(requestedSessionId) != null;
        }
    }

    /**
     * Checks whether the session id specified by this request came in
     * as a cookie.  (The requested session may not be one returned by
     * the <code>getSession</code> method.)
     * @return true if the session id specified by this request came in
     * as a cookie; false otherwise.
     * @see #getSession( boolean )
     */
    public boolean isRequestedSessionIdFromCookie() {
        return idCameAsCookie;
    }

    /**
     * Checks whether the session id specified by this request came in
     * as part of the URL.  (The requested session may not be the one
     * returned by the <code>getSession</code> method.)
     * @return true if the session id specified by the request for this
     * session came in as part of the URL; false otherwise.
     * @see #getSession( boolean )
     */
    public boolean isRequestedSessionIdFromUrl() {
        return idCameAsUrl;
    }

    //--------------------------------------- Implementation of ServletResponse

    /**
     * Sets the content length for this response.
     * @param len the content length.
     */
    public void setContentLength(int len) {
        Integer length = new Integer(len);
        headers_out.put("Content-Length", length.toString());
    }

    /**
     * Sets the content type for this response.  This type may later
     * be implicitly modified by addition of properties such as the MIME
     * <em>charset=&lt;value&gt;</em> if the service finds it necessary,
     * and the appropriate media type property has not been set.  This
     * response property may only be assigned one time.
     * @param type the content's MIME type
     */
    public void setContentType(String type) {
        headers_out.put("Content-Type", type);
    }

    /**
     * Returns an output stream for writing binary response data.
     * @exception IllegalStateException if getWriter has been
     *      called on this same request.
     * @exception IOException if an I/O exception has occurred.
     * @see #getWriter
     */
    public ServletOutputStream getOutputStream() throws IOException {
        if ( servlet_writer != null ) {
            throw new IllegalStateException( "Already called getWriter" );
        } else {
            called_getOutput = true;
            return servlet_out;
        }
    }

    /**
     * Returns a print writer for writing formatted text responses.  The
     * MIME type of the response will be modified, if necessary, to reflect
     * the character encoding used, through the <em>charset=...</em>
     * property.  This means that the content type must be set before
     * calling this method.
     * @exception IllegalStateException if getOutputStream has been
     *        called on this same request.
     * @exception IOException on other errors.
     * @exception UnsupportedEncodingException if the character set encoding
     * @see #getOutputStream
     * @see #setContentType
     */
    public PrintWriter getWriter() throws IOException {
        if (called_getOutput) {
            throw new IllegalStateException("Already called getOutputStream.");
        } else if (servlet_writer == null) {
            // UnsupportedEncodingException is thrown not by
            // getCharacterEncoding, which only parses the content-type header
            // which specifies the encoding, but by the Writer constructor,
            // which possibly cannot support the encoding specified.
            OutputStreamWriter out =
                new OutputStreamWriter(servlet_out, getCharacterEncoding());
            servlet_writer = new PrintWriter(out);
        }
        return servlet_writer;
    }

    /**
     * Returns the character set encoding used for this MIME body.
     * The character encoding is either the one specified in the
     * assigned content type, or one which the client understands.
     * If no content type has yet been assigned, it is implicitly
     * set to <em>text/plain</em>
     */
    public String getCharacterEncoding() {
        String contentType = (String)headers_out.get("Content-Type");
        if (contentType == null) {
            contentType = "text/plain";
            setContentType(contentType);
        }

        return JServUtils.parseCharacterEncoding(contentType);
    }

    //--------------------------------- Implementation of HttpServletResponse

    /**
     * Adds the specified cookie to the response.  It can be called
     * multiple times to set more than one cookie.
     * @param cookie the Cookie to return to the client
     */
    public void addCookie(Cookie cookie) {
        cookies_out.addElement(cookie);
    }

    /**
     * Checks whether the response message header has a field with
     * the specified name.
     * @param name the header field name.
     * @return true if the response message header has a field with
     * the specified name; false otherwise.
     */
    public boolean containsHeader(String name) {
        return headers_out.contains(name.toLowerCase());
    }

    /**
     * Sets the status code and message for this response.  If the
     * field had already been set, the new value overwrites the
     * previous one.  The message is sent as the body of an HTML
     * page, which is returned to the user to describe the problem.
     * The page is sent with a default HTML header; the message
     * is enclosed in simple body tags (&lt;body&gt;&lt;/body&gt;).
     * @param sc the status code.
     * @param sm the status message.
     */
    public void setStatus(int sc, String sm) {
        status = sc;
        status_string = sm;
    }

    /**
     * Sets the status code for this response.  This method is used to
     * set the return status code when there is no error (for example,
     * for the status codes SC_OK or SC_MOVED_TEMPORARILY).  If there
     * is an error, the <code>sendError</code> method should be used
     * instead.
     * @param sc the status code
     * @see #sendError
     */
    public void setStatus(int sc) {
        setStatus(sc, null);
    }

    /**
     * Adds a field to the response header with the given name and value.
     * If the field had already been set, the new value overwrites the
     * previous one.  The <code>containsHeader</code> method can be
     * used to test for the presence of a header before setting its
     * value.
     * @param name the name of the header field
     * @param value the header field's value
     * @see #containsHeader
     */
    public void setHeader(String name, String value) {
        int offset_of_newline;

        // We need to make sure no newlines are present in the header:
        if ((offset_of_newline = value.indexOf((int)'\n')) > 0) {
            char msgAsArray[] = value.toCharArray();
            msgAsArray[offset_of_newline] = ' ';

            while ((offset_of_newline =
                value.indexOf((int)'\n',offset_of_newline+1)) > 0) {
                msgAsArray[offset_of_newline] = ' ';
            }
            value = new String(msgAsArray);
        }

        headers_out.put(name, value);
    }

    /**
     * Adds a field to the response header with the given name and
     * integer value.  If the field had already been set, the new value
     * overwrites the previous one.  The <code>containsHeader</code>
     * method can be used to test for the presence of a header before
     * setting its value.
     * @param name the name of the header field
     * @param value the header field's integer value
     * @see #containsHeader
     */
    public void setIntHeader(String name, int value) {
        Integer val = new Integer(value);
        headers_out.put(name, val.toString());
    }

    /**
     * Adds a field to the response header with the given name and
     * date-valued field.  The date is specified in terms of
     * milliseconds since the epoch.  If the date field had already
     * been set, the new value overwrites the previous one.  The
     * <code>containsHeader</code> method can be used to test for the
     * presence of a header before setting its value.
     * @param name the name of the header field
     * @param value the header field's date value
     * @see #containsHeader
     */
    public void setDateHeader(String name, long date) {
        SimpleDateFormat sdf =
            new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.US);
        TimeZone tz = TimeZone.getTimeZone("GMT");

        sdf.setTimeZone(tz);

        headers_out.put(name, sdf.format(new Date(date)));
    }

    /**
     * Sends an error response to the client using the specified status
     * code and descriptive message.  If setStatus has previously been
     * called, it is reset to the error status code.  The message is
     * sent as the body of an HTML page, which is returned to the user
     * to describe the problem.  The page is sent with a default HTML
     * header; the message is enclosed in simple body tags
     * (&lt;body&gt;&lt;/body&gt;).
     * @param sc the status code
     * @param msg the detail message
     */
    public void sendError(int sc, String msg) {
        try {
            // Tell Apache to send an error
            status = sc;
            setHeader("Servlet-Error", msg);
            sendHttpHeaders();

            // Flush and close, so the error can be returned right
            // away, and so any additional data sent is ignored
            out.flush();
            if(client!=null) client.close();
        } catch (IOException e) {
            // Not much more we can do...
            if (JServ.log.active) JServ.log.log(CH_SERVLET_EXCEPTION,e);
        }
    }

    /**
     * Sends an error response to the client using the specified
     * status code and a default message.
     * @param sc the status code
     */
    public void sendError(int sc) {
        sendError(sc, findStatusString( sc ) );
    }

    /**
     * JServSendError method. This sends an error message to Apache
     * when an exception occur in the ServletEngine.
     */
    public void sendError(Throwable e) {
        sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
            e.toString() + ": " + e.getMessage());
        if (JServ.log.active) JServ.log.log(CH_SERVLET_EXCEPTION,e);
    }

    /**
     * Sends a temporary redirect response to the client using the
     * specified redirect location URL.  The URL must be absolute (for
     * example, <code><em>https://hostname/path/file.html</em></code>).
     * Relative URLs are not permitted here.
     * @param location the redirect location URL
     * @exception IOException If an I/O error has occurred.
     */
    public void sendRedirect(String location) throws IOException {
        // We use Apache's internal status mechanism: set the status to
        // 200, with a Location: header and no body.

        setStatus(SC_OK);
        setHeader("Location", location);
        sendHttpHeaders();
    }

    /**
     * Encodes the specified URL by including the session ID in it,
     * or, if encoding is not needed, returns the URL unchanged.
     * The implementation of this method should include the logic to
     * determine whether the session ID needs to be encoded in the URL.
     * For example, if the browser supports cookies, or session
     * tracking is turned off, URL encoding is unnecessary.
     *
     * <p>All URLs emitted by a Servlet should be run through this
     * method.  Otherwise, URL rewriting cannot be used with browsers
     * which do not support cookies.
     *
     * @param url the url to be encoded.
     * @return the encoded URL if encoding is needed; the unchanged URL
     * otherwise.
     */
    public String encodeUrl(String url) {
        // Encode only if there is no cookie support
        // and there is a valid session associated with this request
        if (isRequestedSessionIdFromCookie()) {
            return url;
        } else if (session == null) {
            return url;
        } else {
            return mgr.encodeUrl(url, session.id);
        }
    }

    /**
     * Encodes the specified URL for use in the
     * <code>sendRedirect</code> method or, if encoding is not needed,
     * returns the URL unchanged.  The implementation of this method
     * should include the logic to determine whether the session ID
     * needs to be encoded in the URL.  Because the rules for making
     * this determination differ from those used to decide whether to
     * encode a normal link, this method is seperate from the
     * <code>encodeUrl</code> method.
     *
     * <p>All URLs sent to the HttpServletResponse.sendRedirect
     * method should be run through this method.  Otherwise, URL
     * rewriting canont be used with browsers which do not support
     * cookies.
     *
     * @param url the url to be encoded.
     * @return the encoded URL if encoding is needed; the unchanged URL
     * otherwise.
     * @see #sendRedirect
     * @see #encodeUrl
     */
    public String encodeRedirectUrl(String url) {
        // Encode only if there is a session associated to the request
        // And if the redirection will come back here
        if (isRequestedSessionIdFromCookie()) {
            return url;
        } else if (session == null) {
            return url;
        } else if (url.indexOf(hostname) == -1) {
            return url;
        } else {
            return mgr.encodeUrl(url, session.id);
        }
    }

    /**
     * Finds a status string from one of the standard
     * status code.
     * @param sc The status code to find a descriptive string.
     * @return A string describing this status code.
     */
    public static final String findStatusString(int sc) {
        switch (sc) {
        case SC_ACCEPTED:
            return "Accepted";
        case SC_BAD_GATEWAY:
            return "Bad Gateway";
        case SC_BAD_REQUEST:
            return "Bad Request";
        case SC_CREATED:
            return "Created";
        case SC_FORBIDDEN:
            return "Forbidden";
        case SC_INTERNAL_SERVER_ERROR:
            return "Internal Server Error";
        case SC_MOVED_PERMANENTLY:
            return "Moved Permanently";
        case SC_MOVED_TEMPORARILY:
            return "Moved Temporarily";
        case SC_NO_CONTENT:
            return "No Content";
        case SC_NOT_FOUND:
            return "Not Found";
        case SC_NOT_IMPLEMENTED:
            return "Method Not Implemented";
        case SC_NOT_MODIFIED:
            return "Not Modified";
        case SC_OK:
            return "OK";
        case SC_SERVICE_UNAVAILABLE:
            return "Service Temporarily Unavailable";
        case SC_UNAUTHORIZED:
            return "Authorization Required";
        default:
            return "Response";
        }
    }

    /**
     * ServletInputStream implementation as inner class
     */
    protected class JServInputStream extends ServletInputStream {
    
        protected InputStream in;
        protected int length;
    
        public JServInputStream(int length, InputStream in) {
            this.length = length;
            this.in = in;
        }
    
        public int read() throws IOException {
          if ( length == -1 )
            return in.read();
          if (length > 0) {
            int i = in.read();
            if (i != -1)
              length -= 1;
            return i;
          }
          return -1;
        }
    
        public int read(byte b[]) throws IOException {
          if (length == -1)
            return in.read(b, 0, b.length);
    
          int len = b.length;
          if (len > length)
              len = length;
          if (len > 0) {
            int i = in.read(b,0,len);
            if (i != -1)
              length -= i;
            return i;
          }
          return -1;
        }
    
        public int read(byte b[], int off, int len) throws IOException {
            if (length == -1)
                return in.read(b, off, len);
    
            if (len > length)
                len = length;
            if (len > 0) {
              int i = in.read(b,off,len);
              if (i != -1)
                length -= i;
              return i;
            }
            return -1;
        }
    
        public long skip(long len) throws IOException {
            if (length == -1)
                return in.skip(len);
    
            if (len > length)
                len = length;
            if (len > 0) {
              long i = in.skip(len);
              if (i != -1)
                length -= i;
              return i;
            }
            return -1;
        }
    
        public void close() throws IOException {
            // Ignore closing of the input stream since it also
            // close the output stream.
            // conn.in.close();
        }
    
        /**
            We must implement this method because java.io.InputStream
            javadocs says that this will return 0. Since we use a long
            internally, it must be cast to an int. ugly. -JSS
        */
        public int available() throws IOException {
            if (length == -1)
                return in.available();
            return length;
        }
    }

    /**
     * ServletOutputStream implementation as inner class
     */
    class JServOutputStream extends ServletOutputStream {
        protected OutputStream out;
        protected InputStream in;

        public JServOutputStream(OutputStream out, InputStream in) {
            this.out = out;
            this.in = in;
        }

        public void write(int b) throws IOException {
            sendHttpHeaders();
            out.write(b);
        }

        public void write(byte b[], int off, int len) throws IOException {
            sendHttpHeaders();
            out.write(b, off, len);
        }

        public void flush() throws IOException {
            sendHttpHeaders();
            out.flush();
        }

        public void close() throws IOException {
            int l;
            do {
                l = in.available();
                in.skip(l);
            } while (l > 0);
            
            sendHttpHeaders();
            out.close();
        }
    }
}
