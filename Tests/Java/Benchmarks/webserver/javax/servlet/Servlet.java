/*
 * @(#)Servlet.java	1.25 97/10/08
 * 
 * Copyright (c) 1995-1997 Sun Microsystems, Inc. All Rights Reserved.
 * 
 * This software is the confidential and proprietary information of Sun
 * Microsystems, Inc. ("Confidential Information").  You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Sun.
 * 
 * SUN MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT THE SUITABILITY OF THE
 * SOFTWARE, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
 * PURPOSE, OR NON-INFRINGEMENT. SUN SHALL NOT BE LIABLE FOR ANY DAMAGES
 * SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING OR DISTRIBUTING
 * THIS SOFTWARE OR ITS DERIVATIVES.
 * 
 * CopyrightVersion 1.0
 */

package javax.servlet;

import java.io.IOException;

/**
 * This interface is for developing servlets.  A servlet is a body of
 * Java code that is loaded into and runs inside a network service,
 * such as a web server.  It receives and responds to requests from
 * clients.  For example, a client may need information from a
 * database; a servlet can be written that receives the request, gets
 * and processes the data as needed by the client, and then returns it
 * to the client.
 * 
 * <P>All servlets implement this interface.  Servlet writers typically
 * do this by subclassing either GenericServlet, which implements the
 * Servlet interface, or by subclassing GenericServlet's descendent,
 * HttpServlet.  Developers need to directly implement this interface
 * only if their servlets cannot (or choose not to) inherit from
 * GenericServlet or HttpServlet.  For example, RMI or CORBA objects
 * that act as servlets will directly implement this interface.
 *
 * <p>The Servlet interface defines methods to initialize a servlet, to
 * receive and respond to client requests, and to destroy a servlet and
 * its resources.  These are known as life-cycle methods, and are called
 * by the network service in the following manner:
 *
 * <ol>
 * <li>Servlet is created then <b>init</b>ialized.
 * <li>Zero or more <b>service</b> calls from clients are handled
 * <li>Servlet is <b>destroy</b>ed then garbage collected and finalized
 * </ol>
 *
 * Initializing a servlet involves doing any expensive one-time setup,
 * such as loading configuration data from files or starting helper
 * threads.  Service calls from clients are handled using a request and
 * response paradigm.  They rely on the underlying network transport to
 * provide quality of service guarantees, such as reordering,
 * duplication, message integrity, privacy, etc. Destroying a servlet
 * involves undoing any initialization work and synchronizing
 * persistent state with the current in-memory state.
 * 
 * <p>In addition to the life-cycle methods, the Servlet interface
 * provides for a method for the servlet to use to get any startup
 * information, and a method that allows the servlet to return basic
 * information about itself, such as its author, version and copyright.
 *
 * @see GenericServlet
 * @see javax.servlet.http.HttpServlet
 * 
 * @version     1.25, 10/08/97
 */

public
interface Servlet {

    /**
     * Initializes the servlet. The method is called once,
     * automatically, by the network service when it loads the servlet.
     * It is guaranteed to finish before any service requests are
     * accepted.  After initialization, the network service does not
     * call the init method again unless it reloads the servlet after
     * it has unloaded and destroyed it.
     * 
     * <p>The init method should save the ServletConfig object so that
     * it can be returned by the getServletConfig method.  If a fatal
     * initialization error occurs, the init method should throw an
     * appropriate "UnavailableException" exception.  It should never
     * call the method System.exit.
     *
     * @see UnavailableException
     * @see javax.servlet.Servlet#getServletConfig()
     * @param config object containing the servlet's startup
     * configuration and initialization parameters
     * @exception ServletException if a servlet exception has occurred
     */
    public void init(ServletConfig config) throws ServletException;

    /**
     * Returns a servlet config object, which contains any
     * initialization parameters and startup configuration for this
     * servlet.  This is the ServletConfig object passed to the init
     * method; the init method should have stored this object so that
     * this method could return it.
     *
     * @see javax.servlet.Servlet#init
     */
    public ServletConfig getServletConfig();

    /**
     * Carries out a single request from the client.  The method
     * implements a request and response paradigm.  The request object
     * contains information about the service request, including
     * parameters provided by the client.  The response object is used
     * to return information to the client.  The request and response
     * objects rely on the underlying network transport for quality of
     * service guarantees, such as reordering, duplication, privacy,
     * and authentication.
     *
     * <p>Service requests are not handled until servlet initialization
     * has completed.  Any requests for service that are received
     * during initialization block until it is complete.  Note that
     * servlets typically run inside multi-threaded servers; servers
     * can handle multiple service requests simultaneously.  It is the
     * servlet writer's responsibility to synchronize access to any
     * shared resources, such as network connections or the servlet's
     * class and instance variables.  Information on multi-threaded
     * programming in Java can be found in <a
     * href="http://java.sun.com/Series/Tutorial/java/threads/multithreaded.html">the
     * Java tutorial on multi-threaded programming</a>.
     *
     * @param req the client's request of the servlet
     * @param res the servlet's response to the client
     * @exception ServletException if a servlet exception has occurred
     * @exception IOException if an I/O exception has occurred
     */
    public void service(ServletRequest req, ServletResponse res)
      throws ServletException, IOException;

    /**
     * Returns a string containing information about the servlet, such as
     * its author, version, and copyright.
     */
    public String getServletInfo();

    /**
     * Cleans up whatever resources are being held (e.g., memory, file
     * handles, threads) and makes sure that any persistent state is
     * synchronized with the servlet's current in-memory state.  The
     * method is called once, automatically, by the network service
     * when it unloads the servlet. After destroy is run, it cannot be
     * called again until the network service reloads the servlet.
     *
     * <p>When the network service removes a servlet, it calls destroy
     * after all service calls have been completed, or a
     * service-specific number of seconds have passed, whichever comes
     * first.  In the case of long-running operations, there could be
     * other threads running service requests when destroy is called.
     * The servlet writer is responsible for making sure that any
     * threads still in the service method complete.
     */
    public void destroy();
}
