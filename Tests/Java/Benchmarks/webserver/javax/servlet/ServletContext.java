/*
 * @(#)ServletContext.java	1.20 97/10/09
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
import java.util.Enumeration;

/**
 * The ServletContext interface gives servlets access to information about
 * their environment, and allows them to log significant events.  Servlet
 * writers decide what data to log.  The interface is implemented by
 * services, and used by servlets.  Different virtual hosts should have
 * different servlet contexts.
 *
 * <p>Servlets get the ServletContext object via the getServletContext
 * method of ServletConfig.  The ServletConfig object is provided to
 * the servlet at initialization, and is accessible via the servlet's
 * getServletConfig method.
 *
 * @see Servlet#getServletConfig
 * @see ServletConfig#getServletContext
 * @version     1.20, 10/09/97
 */
public
interface ServletContext {
    /**
     * Returns the servlet of the specified name, or null if not
     * found.  When the servlet is returned it is initialized and
     * ready to accept service requests.
     * <p>
     * <i>Note:</i> This is a <b>dangerous</b> method to call for the
     * following reasons.
     * <p>
     * <UL><LI> When this method is called the state of the servlet may not
     *      be known, and this could cause problems with the server's
     *      servlet state machine.
     * <LI> It is a security risk to allow any servlet to be able to
     *      access the methods of another servlet.
     * </UL>
     *
     * @param name the name of the desired servlet
     * @exception ServletException if the servlet could not be initialized
     */
    public Servlet getServlet(String name) throws ServletException;

    /**
     * Returns an enumeration of the Servlet objects in this server.
     * Only servlets that are accessible (i.e., from the same namespace)
     * will be returned.  The enumeration always includes the servlet
     * itself.
     * <p>
     * <i>Note:</i> This is a <b>dangerous</b> method to call for the
     * following reasons.
     * <p>
     * <UL><LI> When this method is called the state of the servlet may not
     *      be known, and this could cause problems with the server's
     *      servlet state machine.
     * <LI> It is a security risk to allow any servlet to be able to
     *      access the methods of another servlet.
     * </UL>
     * @deprecated
     * Please use getServletNames in conjunction with getServlet
     * @see #getServletNames 
     * @see #getServlet
     */
    public Enumeration getServlets();

    /**
     * Returns an enumeration of the Servlet object names in this server.
     * Only servlets that are accessible (i.e., from the same namespace)
     * will be returned.  The enumeration always includes the servlet
     * itself.
     * <p>
     * <i>Note:</i> This is a <b>dangerous</b> method to call for the
     * following reasons.
     * <p>
     * <UL><LI> When this method is called the state of the servlet may not
     *      be known, and this could cause problems with the server's
     *      servlet state machine.
     * <LI> It is a security risk to allow any servlet to be able to
     *      access the methods of another servlet.
     * </UL>
     */
    public Enumeration getServletNames();
    
    /**
     * Writes the given message string to the servlet log file.
     * The name of the servlet log file is server specific; it
     * is normally an event log.
     * @param msg the message to be written
     */
    public void log(String msg);

	/**
	 * Write the stacktrace and the given message string to the 
	 * servlet log file. The name of the servlet log file is 
	 * server specific; it is normally an event log.
	 * @param exception the exception to be written
	 * @param msg the message to be written
	 */
    public void log(Exception exception, String msg);

    /**
     * Applies alias rules to the specified virtual path and returns the
     * corresponding real path.  For example, in an HTTP servlet,
     * this method would resolve the path against the HTTP service's
     * docroot.  Returns null if virtual paths are not supported, or if the
     * translation could not be performed for any reason.
     * @param path the virtual path to be translated into a real path
     */
    public String getRealPath(String path);

    /**
     * Returns the mime type of the specified file, or null if not known.
     * @param file name of the file whose mime type is required
     */
    public String getMimeType(String file);

    /**
     * Returns the name and version of the network service under which
     * the servlet is running. For example, if the network service was
     * an HTTP service, then this would be the same as the CGI variable 
     * SERVER_SOFTWARE.
     */
    public String getServerInfo();

    /**
     * Returns the value of the named attribute of the network service,
     * or null if the attribute does not exist.  This method allows
     * access to additional information about the service, not already
     * provided by the other methods in this interface. Attribute names
     * should follow the same convention as package names.  The package
     * names java.* and javax.* are reserved for use by Javasoft, and
     * com.sun.* is reserved for use by Sun Microsystems.
     *
     * @param name the name of the attribute whose value is required
     * @return the value of the attribute, or null if the attribute
     * does not exist.
     */
    public Object getAttribute(String name);
}
