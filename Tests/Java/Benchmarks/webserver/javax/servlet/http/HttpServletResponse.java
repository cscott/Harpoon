/*
 * @(#)HttpServletResponse.java	1.26 97/10/20
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

import javax.servlet.ServletResponse;
import java.io.IOException;

/**
 * An HTTP servlet response.  This interface allows a servlet's
 * <code>service</code> method to manipulate HTTP-protocol specified
 * header information and return data to its client.  It is implemented
 * by network service developers for use within servlets.
 * 
 *
 * @version 1.26, 10/20/97
 */
public
interface HttpServletResponse extends ServletResponse {

    /**
     * Adds the specified cookie to the response.  It can be called
     * multiple times to set more than one cookie.
     *
     * @param cookie the Cookie to return to the client
     */
    public void addCookie(Cookie cookie);

    /**
     * Checks whether the response message header has a field with
     * the specified name.
     * 
     * @param name the header field name
     * @return true if the response message header has a field with
     * the specified name; false otherwise
     */
    public boolean containsHeader(String name);

    /**
     * Sets the status code and message for this response.  If the
     * field had already been set, the new value overwrites the
     * previous one.  The message is sent as the body of an HTML
     * page, which is returned to the user to describe the problem.
     * The page is sent with a default HTML header; the message
     * is enclosed in simple body tags (&lt;body&gt;&lt;/body&gt;).
     * 
     * @param sc the status code
     * @param sm the status message
     */
    public void setStatus(int sc, String sm);

    /**
     * Sets the status code for this response.  This method is used to
     * set the return status code when there is no error (for example,
     * for the status codes SC_OK or SC_MOVED_TEMPORARILY).  If there
     * is an error, the <code>sendError</code> method should be used
     * instead.
     *
     * @param sc the status code
     *
     * @see #sendError
     */
    public void setStatus(int sc);

    /**
     *
     * Adds a field to the response header with the given name and value.
     * If the field had already been set, the new value overwrites the
     * previous one.  The <code>containsHeader</code> method can be
     * used to test for the presence of a header before setting its
     * value.
     * 
     * @param name the name of the header field
     * @param value the header field's value
     *
     * @see #containsHeader
     */
    public void setHeader(String name, String value);

    /**
     * Adds a field to the response header with the given name and
     * integer value.  If the field had already been set, the new value
     * overwrites the previous one.  The <code>containsHeader</code>
     * method can be used to test for the presence of a header before
     * setting its value.
     *
     * @param name the name of the header field
     * @param value the header field's integer value
     *
     * @see #containsHeader
     */
    public void setIntHeader(String name, int value);

    /**
     * 
     * Adds a field to the response header with the given name and
     * date-valued field.  The date is specified in terms of
     * milliseconds since the epoch.  If the date field had already
     * been set, the new value overwrites the previous one.  The
     * <code>containsHeader</code> method can be used to test for the
     * presence of a header before setting its value.
     * 
     * @param name the name of the header field
     * @param value the header field's date value
     * 
     * @see #containsHeader
     */
    public void setDateHeader(String name, long date);

    /**
     * Sends an error response to the client using the specified status
     * code and descriptive message.  If setStatus has previously been
     * called, it is reset to the error status code.  The message is
     * sent as the body of an HTML page, which is returned to the user
     * to describe the problem.  The page is sent with a default HTML
     * header; the message is enclosed in simple body tags
     * (&lt;body&gt;&lt;/body&gt;).
     * 
     * @param sc the status code
     * @param msg the detail message
     * @exception IOException If an I/O error has occurred.  */
    public void sendError(int sc, String msg) throws IOException;

    /**
     * Sends an error response to the client using the specified
     * status code and a default message.
     * @param sc the status code
     * @exception IOException If an I/O error has occurred.
     */
    public void sendError(int sc) throws IOException;

    /**
     * Sends a temporary redirect response to the client using the
     * specified redirect location URL.  The URL must be absolute (for
     * example, <code><em>https://hostname/path/file.html</em></code>).
     * Relative URLs are not permitted here.
     *
     * @param location the redirect location URL
     * @exception IOException If an I/O error has occurred.
     */
    public void sendRedirect(String location) throws IOException;

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
    public String encodeUrl (String url);

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
     *
     * @see #sendRedirect
     * @see #encodeUrl
     */
    public String encodeRedirectUrl (String url);

    /*
     * Server status codes; see RFC 2068.
     */

    /**
     * Status code (100) indicating the client can continue.
     */
    public static final int SC_CONTINUE = 100;

    /**
     * Status code (101) indicating the server is switching protocols
     * according to Upgrade header.
     */
    public static final int SC_SWITCHING_PROTOCOLS = 101;

    /**
     * Status code (200) indicating the request succeeded normally.
     */
    public static final int SC_OK = 200;

    /**
     * Status code (201) indicating the request succeeded and created
     * a new resource on the server.
     */
    public static final int SC_CREATED = 201;

    /**
     * Status code (202) indicating that a request was accepted for
     * processing, but was not completed.
     */
    public static final int SC_ACCEPTED = 202;

    /**
     * Status code (203) indicating that the meta information presented
     * by the client did not originate from the server.
     */
    public static final int SC_NON_AUTHORITATIVE_INFORMATION = 203;

    /**
     * Status code (204) indicating that the request succeeded but that
     * there was no new information to return.
     */
    public static final int SC_NO_CONTENT = 204;

    /**
     * Status code (205) indicating that the agent <em>SHOULD</em> reset
     * the document view which caused the request to be sent.
     */
    public static final int SC_RESET_CONTENT = 205;

    /**
     * Status code (206) indicating that the server has fulfilled
     * the partial GET request for the resource.
     */
    public static final int SC_PARTIAL_CONTENT = 206;

    /**
     * Status code (300) indicating that the requested resource
     * corresponds to any one of a set of representations, each with
     * its own specific location.
     */
    public static final int SC_MULTIPLE_CHOICES = 300;

    /**
     * Status code (301) indicating that the resource has permanently
     * moved to a new location, and that future references should use a
     * new URI with their requests.
     */
    public static final int SC_MOVED_PERMANENTLY = 301;

    /**
     * Status code (302) indicating that the resource has temporarily
     * moved to another location, but that future references should
     * still use the original URI to access the resource.
     */
    public static final int SC_MOVED_TEMPORARILY = 302;

    /**
     * Status code (303) indicating that the response to the request
     * can be found under a different URI.
     */
    public static final int SC_SEE_OTHER = 303;

    /**
     * Status code (304) indicating that a conditional GET operation
     * found that the resource was available and not modified.
     */
    public static final int SC_NOT_MODIFIED = 304;

    /**
     * Status code (305) indicating that the requested resource
     * <em>MUST</em> be accessed through the proxy given by the
     * <code><em>Location</em></code> field.
     */
    public static final int SC_USE_PROXY = 305;

    /**
     * Status code (400) indicating the request sent by the client was
     * syntactically incorrect.
     */
    public static final int SC_BAD_REQUEST = 400;

    /**
     * Status code (401) indicating that the request requires HTTP
     * authentication.
     */
    public static final int SC_UNAUTHORIZED = 401;

    /**
     * Status code (402) reserved for future use.
     */
    public static final int SC_PAYMENT_REQUIRED = 402;

    /**
     * Status code (403) indicating the server understood the request
     * but refused to fulfill it.
     */
    public static final int SC_FORBIDDEN = 403;

    /**
     * Status code (404) indicating that the requested resource is not
     * available.
     */
    public static final int SC_NOT_FOUND = 404;

    /**
     * Status code (405) indicating that the method specified in the
     * <code><em>Request-Line</em></code> is not allowed for the resource
     * identified by the <code><em>Request-URI</em></code>.
     */
    public static final int SC_METHOD_NOT_ALLOWED = 405;

    /**
     * Status code (406) indicating that the resource identified by the
     * request is only capable of generating response entities which have
     * content characteristics not acceptable according to the accept
     * headerssent in the request.
     */
    public static final int SC_NOT_ACCEPTABLE = 406;

    /**
     * Status code (407) indicating that the client <em>MUST</em> first
     * authenticate itself with the proxy.
     */
    public static final int SC_PROXY_AUTHENTICATION_REQUIRED = 407;

    /**
     * Status code (408) indicating that the client did not produce a
     * requestwithin the time that the server was prepared to wait.
     */
    public static final int SC_REQUEST_TIMEOUT = 408;

    /**
     * Status code (409) indicating that the request could not be
     * completed due to a conflict with the current state of the
     * resource.
     */
    public static final int SC_CONFLICT = 409;

    /**
     * Status code (410) indicating that the resource is no longer
     * available at the server and no forwarding address is known.
     * This condition <em>SHOULD</em> be considered permanent.
     */
    public static final int SC_GONE = 410;

    /**
     * Status code (411) indicating that the request cannot be handled
     * without a defined <code><em>Content-Length</em></code>.
     */
    public static final int SC_LENGTH_REQUIRED = 411;

    /**
     * Status code (412) indicating that the precondition given in one
     * or more of the request-header fields evaluated to false when it
     * was tested on the server.
     */
    public static final int SC_PRECONDITION_FAILED = 412;

    /**
     * Status code (413) indicating that the server is refusing to process
     * the request because the request entity is larger than the server is
     * willing or able to process.
     */
    public static final int SC_REQUEST_ENTITY_TOO_LARGE = 413;

    /**
     * Status code (414) indicating that the server is refusing to service
     * the request because the <code><em>Request-URI</em></code> is longer
     * than the server is willing to interpret.
     */
    public static final int SC_REQUEST_URI_TOO_LONG = 414;

    /**
     * Status code (415) indicating that the server is refusing to service
     * the request because the entity of the request is in a format not
     * supported by the requested resource for the requested method.
     */
    public static final int SC_UNSUPPORTED_MEDIA_TYPE = 415;

    /**
     * Status code (500) indicating an error inside the HTTP server
     * which prevented it from fulfilling the request.
     */
    public static final int SC_INTERNAL_SERVER_ERROR = 500;

    /**
     * Status code (501) indicating the HTTP server does not support
     * the functionality needed to fulfill the request.
     */
    public static final int SC_NOT_IMPLEMENTED = 501;

    /**
     * Status code (502) indicating that the HTTP server received an
     * invalid response from a server it consulted when acting as a
     * proxy or gateway.
     */
    public static final int SC_BAD_GATEWAY = 502;

    /**
     * Status code (503) indicating that the HTTP server is
     * temporarily overloaded, and unable to handle the request.
     */
    public static final int SC_SERVICE_UNAVAILABLE = 503;

    /**
     * Status code (504) indicating that the server did not receive
     * a timely response from the upstream server while acting as
     * a gateway or proxy.
     */
    public static final int SC_GATEWAY_TIMEOUT = 504;

    /**
     * Status code (505) indicating that the server does not support
     * or refuses to support the HTTP protocol version that was used
     * in the request message.
     */
    public static final int SC_HTTP_VERSION_NOT_SUPPORTED = 505;
}
