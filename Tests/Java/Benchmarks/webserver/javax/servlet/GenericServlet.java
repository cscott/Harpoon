/*
 * @(#)GenericServlet.java	1.23 97/11/05
 * 
 * Copyright (c) 1996-1997 Sun Microsystems, Inc. All Rights Reserved.
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
import java.util.Enumeration;

/**
 * The GenericServlet class implements the Servlet interface and, for
 * convenience, the ServletConfig interface.  Servlet developers
 * typically subclass GenericServlet, or its descendent HttpServlet,
 * unless the servlet needs another class as a parent.  (If a servlet
 * does need to subclass another class, the servlet must implement the
 * Servlet interface directly.  This would be necessary when, for
 * example, RMI or CORBA objects act as servlets.)
 *
 * <p>The GenericServlet class was created to make writing servlets
 * easier.  It provides simple versions of the lifecycle methods init
 * and destroy, and of the methods in the ServletConfig interface.  It
 * also provides a log method, from the ServletContext interface.  The
 * servlet writer must override only the service method, which is
 * abstract.  Though not required, the servlet implementer should also
 * override the getServletInfo method, and will want to specialize the
 * init and destroy methods if expensive servlet-wide resources are to
 * be managed.
 *
 * @version     1.23, 11/05/97
 */

public abstract class GenericServlet 
    implements Servlet, ServletConfig, java.io.Serializable { 

    private transient ServletConfig config;

    /**
     * The default constructor does no work.
     */
    public GenericServlet () { }

    /**
     * Returns a ServletContext object, which contains information
     * about the network service in which the servlet is running.  This
     * is a convenience method; it gets the ServletContext object from
     * the ServletConfig object.  (The ServletConfig object was passed
     * into and stored by the init method.)
     */
    public ServletContext getServletContext() {
	return getServletConfig().getServletContext();
    }

    /**
     * Returns a string containing the value of the named
     * initialization parameter, or null if the requested parameter
     * does not exist.  Init parameters have a single string value; it
     * is the responsibility of the servlet writer to interpret the
     * string.
     *
     * <p>This is a convenience method; it gets the parameter's value
     * from the ServletConfig object.  (The ServletConfig object was
     * passed into and stored by the init method.)
     *
     * @param name the name of the parameter whose value is requested
     */
    public String getInitParameter(String name) {
	return getServletConfig().getInitParameter(name);
    }

    /**
     * Returns the names of the servlet's initialization parameters as
     * an enumeration of strings, or an empty enumeration if there are
     * no initialization parameters.  The getInitParameterNames method
     * is supplied for convenience; it gets the parameter names from
     * the ServletConfig object.  (The ServletConfig object was passed
     * into and stored by the init method.)
     */
    public Enumeration getInitParameterNames() {
	return getServletConfig().getInitParameterNames();
    }

    /**
     * 
     * Writes the class name of the servlet and the given message to
     * the servlet log file.  The name of the servlet log file is
     * server specific; it is normally an event log.
     *
     * <p>If a servlet will have multiple instances (for example, if
     * the network service runs the servlet for multiple virtual
     * hosts), the servlet writer should override this method.  The
     * specialized method should log an instance identifier, along with
     * the requested message.  The default message prefix, the class
     * name of the servlet, does not allow the log entries of the
     * instances to be distinguished from one another.
     *
     * @param msg the message string to be logged
     */
    public void log(String msg) {
	getServletContext().log(getClass().getName() + ": "+ msg);
    }

    /**
     * Returns a string that contains information about the servlet,
     * such as its author, version, and copyright.  This method
     * must be overridden in order to return this information.
     * If it is not overridden, null is returned.
     */
    public String getServletInfo() {
	return null;
    }

    /**
     *
     * Initializes the servlet and logs the initialization. The init
     * method is called once, automatically, by the network service
     * each time it loads the servlet.  It is guaranteed to finish
     * before any service requests are accepted.  On fatal
     * initialization errors, an UnavailableException should be
     * thrown.  Do not call call the method System.exit.
     *
     * <p>The init method stores the ServletConfig object.  Servlet
     * writers who specialize this method should call either
     * super.init, or store the ServletConfig object themselves.  If an
     * implementor decides to store the ServletConfig object in a
     * different location, then the getServletConfig method must also
     * be overridden.
     *
     * @see UnavailableException
     * @param config servlet configuration information
     * @exception ServletException if a servlet exception has occurred
     */
    public void init(ServletConfig config) throws ServletException {
	this.config = config;
	log("init");
    }

    /**
     * Returns a servletConfig object containing any startup
     * configuration information for this servlet.
     */
    public ServletConfig getServletConfig() {
	return config;
    }

    /**
     * 
     * Carries out a single request from the client.  The request
     * object contains parameters provided by the client, and an input
     * stream, which can also bring data to the servlet.  To return
     * information to the client, write to the output stream of the
     * response object.
     *
     * <p>Service requests handled after servlet initialization has
     * completed.  Any requests for service that are received during
     * initialization block until it is complete.
     *
     * <p>Note that servlets typically run inside multi-threaded
     * network services, which can handle multiple service requests
     * simultaneously.  It is the servlet writer's responsibility to
     * synchronize access to any shared resources, such as database or
     * network connections.  The simplest way to do this is to
     * synchronize the entire service call.  This can have a major
     * performance impact, however, and should be avoided whenever
     * possible in favor of techniques that are less coarse.  For more
     * information on synchronization, see the <a
     * href="http://java.sun.com/Series/Tutorial/java/threads/multithreaded.html">the
     * Java tutorial on multithreaded programming</a>.
     *
     *
     * @param req the servlet request
     * @param res the servlet response
     * @exception ServletException if a servlet exception has occurred
     * @exception IOException if an I/O exception has occurred
     */
    public abstract void service(ServletRequest req, ServletResponse res)
	throws ServletException, IOException;

    /**
     *
     * Destroys the servlet, cleaning up whatever resources are being
     * held, and logs the destruction in the servlet log file.  This
     * method is called, once, automatically, by the network service
     * each time it removes the servlet.  After destroy is run, it
     * cannot be called again until the network service reloads the
     * servlet.
     *
     * <p>When the network service removes a servlet, it calls destroy
     * after all service calls have been completed, or a
     * service-specific number of seconds have passed, whichever comes
     * first.  In the case of long-running operations, there could be
     * other threads running service requests when destroy is called.
     * The servlet writer is responsible for making sure that any
     * threads still in the service method complete. 
     */
    public void destroy() {
	log("destroy");
    }

}
