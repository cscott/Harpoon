/*
 * @(#)HttpServletRequest.java	1.26 98/04/16
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

package javax.servlet.http;

import javax.servlet.ServletRequest;
import java.util.Enumeration;

/**
 * An HTTP servlet request.  This interface gets data from the client
 * to the servlet for use in the <code>HttpServlet.service</code>
 * method.  It allows the HTTP-protocol specified header information to
 * be accessed from the <code>service</code> method.  This interface is
 * implemented by network-service developers for use within servlets.
 *
 * @version 1.26, 04/16/98
 */
public
interface HttpServletRequest extends ServletRequest {

    /**
     * Gets the array of cookies found in this request.
     *
     * @return the array of cookies found in this request
     */
    public Cookie[] getCookies();

    /**
     * Gets the HTTP method (for example, GET, POST, PUT) with which
     * this request was made. Same as the CGI variable REQUEST_METHOD.
     *
     * @return the HTTP method with which this request was made
     */
    public String getMethod();

    /**
     * Gets, from the first line of the HTTP request, the part of this
     * request's URI that is to the left of any query string.
     * For example,
     *
     * <blockquote>
     * <table>
     * <tr align=left><th>First line of HTTP request<th>
     * <th>Return from <code>getRequestURI</code>
     * <tr><td>POST /some/path.html HTTP/1.1<td><td>/some/path.html
     * <tr><td>GET http://foo.bar/a.html HTTP/1.0
     * <td><td>http://foo.bar/a.html
     * <tr><td>HEAD /xyz?a=b HTTP/1.1<td><td>/xyz
     * </table>
     * </blockquote>
     * 
     * <p>To reconstruct a URL with a URL scheme and host, use the
     * method javax.servlet.http.HttpUtils.getRequestURL, which returns
     * a StringBuffer.
     *
     * @return this request's URI
     * @see javax.servlet.http.HttpUtils#getRequestURL
     */
    public String getRequestURI();

    /**
     * Gets the part of this request's URI that refers to the servlet
     * being invoked. Analogous to the CGI variable SCRIPT_NAME.
     *
     * @return the servlet being invoked, as contained in this
     * request's URI
     */
    public String getServletPath();

    /**
     * Gets any optional extra path information following the servlet
     * path of this request's URI, but immediately preceding its query
     * string. Same as the CGI variable PATH_INFO.
     *
     * @return the optional path information following the servlet
     * path, but before the query string, in this request's URI; null
     * if this request's URI contains no extra path information
     */
    public String getPathInfo();

    /**
     * Gets any optional extra path information following the servlet
     * path of this request's URI, but immediately preceding its query
     * string, and translates it to a real path.  Similar to the CGI
     * variable PATH_TRANSLATED
     *
     * @return extra path information translated to a real path or null
     * if no extra path information is in the request's URI
     */
    public String getPathTranslated();

    /**
     * Gets any query string that is part of the HTTP request URI.
     * Same as the CGI variable QUERY_STRING.
     *
     * @return query string that is part of this request's URI, or null
     * if it contains no query string
     */
    public String getQueryString();

    /**
     * Gets the name of the user making this request.  The user name is
     * set with HTTP authentication.  Whether the user name will
     * continue to be sent with each subsequent communication is
     * browser-dependent.  Same as the CGI variable REMOTE_USER.
     *
     * @return the name of the user making this request, or null if not
     * known.
     */
    public String getRemoteUser();

    /**
     * Gets the authentication scheme of this request.  Same as the CGI
     * variable AUTH_TYPE.
     *
     * @return this request's authentication scheme, or null if none.
     */
    public String getAuthType();

    /**
     * Gets the value of the requested header field of this request.
     * The case of the header field name is ignored.
     * 
     * @param name the String containing the name of the requested
     * header field
     * @return the value of the requested header field, or null if not
     * known.
     */
    public String getHeader(String name); 

    /**
     * Gets the value of the specified integer header field of this
     * request.  The case of the header field name is ignored.  If the
     * header can't be converted to an integer, the method throws a
     * NumberFormatException.
     * 
     * @param name the String containing the name of the requested
     * header field
     * @return the value of the requested header field, or -1 if not
     * found.
     */
    public int getIntHeader(String name);

    /**
     * Gets the value of the requested date header field of this
     * request.  If the header can't be converted to a date, the method
     * throws an IllegalArgumentException.  The case of the header
     * field name is ignored.
     * 
     * @param name the String containing the name of the requested
     * header field
     * @return the value the requested date header field, or -1 if not
     * found.
     */
    public long getDateHeader(String name);

    /**
     * Gets the header names for this request.
     *
     * @return an enumeration of strings representing the header names
     * for this request. Some server implementations do not allow
     * headers to be accessed in this way, in which case this method
     * will return null.
     */
    public Enumeration getHeaderNames();

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
    public HttpSession getSession (boolean create);
   
    /**
     * Gets the session id specified with this request.  This may
     * differ from the actual session id.  For example, if the request
     * specified an id for an invalid session, then this will get a new
     * session with a new id.
     *
     * @return the session id specified by this request, or null if the
     * request did not specify a session id
     * 
     * @see #isRequestedSessionIdValid */
    public String getRequestedSessionId ();

    /**
     * Checks whether this request is associated with a session that
     * is valid in the current session context.  If it is not valid,
     * the requested session will never be returned from the
     * <code>getSession</code> method.
     * 
     * @return true if this request is assocated with a session that is
     * valid in the current session context.
     *
     * @see #getRequestedSessionId
     * @see javax.servlet.http.HttpSessionContext
     * @see #getSession
     */
    public boolean isRequestedSessionIdValid ();

    /**
     * Checks whether the session id specified by this request came in
     * as a cookie.  (The requested session may not be one returned by
     * the <code>getSession</code> method.)
     * 
     * @return true if the session id specified by this request came in
     * as a cookie; false otherwise
     *
     * @see #getSession
     */
    public boolean isRequestedSessionIdFromCookie ();

    /**
     * Checks whether the session id specified by this request came in
     * as part of the URL.  (The requested session may not be the one
     * returned by the <code>getSession</code> method.)
     * 
     * @return true if the session id specified by the request for this
     * session came in as part of the URL; false otherwise
     *
     * @see #getSession
     */
    public boolean isRequestedSessionIdFromUrl ();

}
