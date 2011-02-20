/*
 * @(#)ServletRequest.java	1.49 98/04/15
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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Enumeration;

/**
 * This interface is for getting data from the client to the servlet
 * for a service request.  Network service developers implement the
 * ServletRequest interface.  The methods are then used by servlets
 * when the service method is executed; the ServletRequest object is
 * passed as an argument to the service method.
 *
 * <P> Some of the data provided by the ServletRequest object includes
 * parameter names and values, attributes, and an input stream.
 * Subclasses of ServletRequest can provide additional
 * protocol-specific data.  For example, HTTP data is provided by the
 * interface HttpServletRequest, which extends ServletRequest.  This
 * framework provides the servlet's only access to this data.
 *
 * <P> MIME bodies are either text or binary data.  Use getReader to
 * handle text, including the character encodings.  The getInputStream
 * call should be used to handle binary data.  Multipart MIME bodies
 * are treated as binary data, since the headers are US-ASCII data.
 * 
 * @see javax.servlet.http.HttpServletRequest
 *
 * @version	1.49,04/15/98
 */

public
interface ServletRequest {
    /**
     * Returns the size of the request entity data, or -1 if not known.
     * Same as the CGI variable CONTENT_LENGTH.
     */
    public int getContentLength();

    /**
     * Returns the Internet Media Type of the request entity data, or
     * null if not known. Same as the CGI variable CONTENT_TYPE.
     */
    public String getContentType();

    /**
     * Returns the protocol and version of the request as a string of
     * the form <code>&lt;protocol&gt;/&lt;major version&gt;.&lt;minor
     * version&gt</code>.  Same as the CGI variable SERVER_PROTOCOL.
     */
    public String getProtocol();

    /**
     * Returns the scheme of the URL used in this request, for example
     * "http", "https", or "ftp".  Different schemes have different
     * rules for constructing URLs, as noted in RFC 1738.  The URL used
     * to create a request may be reconstructed using this scheme, the
     * server name and port, and additional information such as URIs.
     */
    public String getScheme();

    /**
     * Returns the host name of the server that received the request.
     * Same as the CGI variable SERVER_NAME.
     */
    public String getServerName();

    /**
     * Returns the port number on which this request was received.
     * Same as the CGI variable SERVER_PORT.
     */
    public int getServerPort();

    /**
     * Returns the IP address of the agent that sent the request.
     * Same as the CGI variable REMOTE_ADDR.
     */
    public String getRemoteAddr();

    /**
     * Returns the fully qualified host name of the agent that sent the
     * request. Same as the CGI variable REMOTE_HOST.
     */
    public String getRemoteHost();

    /**
     * Applies alias rules to the specified virtual path and returns
     * the corresponding real path, or null if the translation can not
     * be performed for any reason.  For example, an HTTP servlet would
     * resolve the path using the virtual docroot, if virtual hosting
     * is enabled, and with the default docroot otherwise.  Calling
     * this method with the string "/" as an argument returns the
     * document root.
     *
     * @param path the virtual path to be translated to a real path
     */
    public String getRealPath(String path);

    /**
     * Returns an input stream for reading binary data in the request body.
     *
     * @see getReader
     * @exception IllegalStateException if getReader has been
     *	called on this same request.
     * @exception IOException on other I/O related errors.
     */
    public ServletInputStream getInputStream() throws IOException;  

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
     *
     * @see #getParameterValues
     *
     * @param name the name of the parameter whose value is required.
     */
    public String getParameter(String name);

    /**
     * Returns the values of the specified parameter for the request as
     * an array of strings, or null if the named parameter does not
     * exist. For example, in an HTTP servlet this method would return
     * the values of the specified query string or posted form as an
     * array of strings.
     *
     * @param name the name of the parameter whose value is required.
     * @see javax.servlet.ServletRequest#getParameter
     */
    public String[] getParameterValues(String name);

    /**
     * Returns the parameter names for this request as an enumeration
     * of strings, or an empty enumeration if there are no parameters
     * or the input stream is empty.  The input stream would be empty
     * if all the data had been read from the stream returned by the
     * method getInputStream.
     */
    public Enumeration getParameterNames();

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
     *	<th>Attribute Name</th>
     *	<th>Attribute Type</th>
     *	<th>Description</th>
     *	</tr>
     *
     * <tr>
     *	<td VALIGN=TOP>javax.net.ssl.cipher_suite</td>
     *	<td VALIGN=TOP>string</td>
     *	<td>The string name of the SSL cipher suite in use, if the
     *		request was made using SSL</td>
     *	</tr>
     *
     * <tr>
     *	<td VALIGN=TOP>javax.net.ssl.peer_certificates</td>
     *	<td VALIGN=TOP>array of javax.security.cert.X509Certificate</td>
     *	<td>The chain of X.509 certificates which authenticates the client.
     *		This is only available when SSL is used with client
     *		authentication is used.</td>
     *	</tr>
     *
     * <tr>
     *	<td VALIGN=TOP>javax.net.ssl.session</td>
     *	<td VALIGN=TOP>javax.net.ssl.SSLSession</td>
     *	<td>An SSL session object, if the request was made using SSL.</td>
     *	</tr>
     *
     * </TABLE>
     *
     * <BR>
     * <P>The package (and hence attribute) names beginning with java.*,
     * and javax.* are reserved for use by Javasoft. Similarly, com.sun.*
     * is reserved for use by Sun Microsystems.
     *
     * @param name the name of the attribute whose value is required
     */
    public Object getAttribute(String name);

    /**
     * Returns a buffered reader for reading text in the request body.
     * This translates character set encodings as appropriate. 
     *
     * @see getInputStream
     *
     * @exception UnsupportedEncodingException if the character set encoding
     *  is unsupported, so the text can't be correctly decoded.
     * @exception IllegalStateException if getInputStream has been
     *	called on this same request.
     * @exception IOException on other I/O related errors.
     */
    public BufferedReader getReader () throws IOException;

    /**
     * Returns the character set encoding for the input of this request.
     */
    public String getCharacterEncoding ();
}
