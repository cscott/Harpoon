/*
 * @(#)ServletConfig.java	1.16 97/10/08
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

import java.util.Enumeration;

/**
 * This interface is implemented by services in order to pass
 * configuration information to a servlet when it is first loaded.  A
 * service writer implementing this interface must write methods for the
 * servlet to use to get its initialization parameters and the context
 * in which it is running. 
 *
 * <p>The ServletConfig interface can also be implemented by servlets.
 * (GenericServlet does this.)  When implemented by a servlet, the
 * methods in the interface make getting the configuration data more
 * convenient.  For example, a servlet could implement
 * getServletContext by writing,
 * 
 * <pre>
 *   public ServletContext getServletContext() {
 *	return getServletConfig().getServletContext();
 *   }
 * </pre>
 * making access to the servlet's context object a single method
 * invocation (a call to <code>getServletContext()</code>).
 *
 * @version     1.16, 10/08/97
 */

public
interface ServletConfig {
    /**
     * Returns the context for the servlet.
     */
    public ServletContext getServletContext();

    /**
     *
     * Returns a string containing the value of the named
     * initialization parameter of the servlet, or null if the
     * parameter does not exist.  Init parameters have a single string
     * value; it is the responsibility of the servlet writer to
     * interpret the string.
     *
     * @param name the name of the parameter whose value is requested
     */
    public String getInitParameter(String name);

    /**
     * Returns the names of the servlet's initialization parameters
     * as an enumeration of strings, or an empty enumeration if there
     * are no initialization parameters.
     */
    public Enumeration getInitParameterNames();
    
}
