/*
 * @(#)Cookie.java	1.31 97/10/08
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

package javax.servlet.http;

import java.io.IOException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Enumeration;
import java.util.StringTokenizer;
import java.util.Vector;


/**
 * This class represents a "Cookie", as used for session management with
 * HTTP and HTTPS protocols.  Cookies are used to get user agents (web
 * browsers etc) to hold small amounts of state associated with a user's
 * web browsing.  Common applications for cookies include storing user
 * preferences, automating low security user signon facilities, and helping
 * collect data used for "shopping cart" style applications.
 *
 * <HR>
 *
 * <P> Cookies are named, and have a single value.  They may have optional
 * attributes, including a comment presented to the user, path and domain
 * qualifiers for which hosts see the cookie, a maximum age, and a version.
 * Current web browsers often have bugs in how they treat those attributes,
 * so interoperability can be improved by not relying on them heavily.
 *
 * <P> Cookies are assigned by servers, using fields added to HTTP response
 * headers.  In this API, cookies are saved one at a time into such HTTP
 * response headers, using the 
 * <em>javax.servlet.http.HttpServletResponse.addCookie</em> method.  User
 * agents are expected to support twenty cookies per host, of at least four
 * kilobytes each; use of large numbers of cookies is discouraged.  
 *
 * <P> Cookies are passed back to those servers using fields added to HTTP
 * request headers.  In this API, HTTP request fields are retrieved using
 * the cookie module's
 * <em>javax.servlet.http.HttpServletRequest.getCookies</em> method. 
 * This returns all of the cookies found in the request.  Several cookies
 * with the same name can be returned; they have different path attributes,
 * but those attributes will not be visible when using "old format" cookies.
 *
 * <P> Cookies affect the caching of the web pages used to set their values.
 * At this time, none of the sophisticated HTTP/1.1 cache control models
 * are supported by this class.  Standard HTTP/1.0 caches will not cache
 * pages which contain cookies created by this class.
 *
 * <HR>
 *
 * <P> Cookies are being standardized by the IETF.  This class supports
 * the original Cookie specification (from Netscape Communications Corp.)
 * as well as the updated RFC 2109 specification.  By default, cookies
 * are stored using the original specification.  This promotes maximal
 * interoperability; an updated RFC will provide better interoperability
 * by defining a new HTTP header field for setting cookies.
 *
 * @version	1.31, 10/08/97
 */
public class Cookie implements Cloneable
    // XXX would implement java.io.Serializable too, but can't do that
    // so long as sun.servlet.* must run on older JDK 1.02 JVMs which
    // don't include that support.
{

    //
    // The value of the cookie itself.
    //
    private String name;	// NAME= ... "$Name" style is reserved
    private String value;	// value of NAME

    //
    // Attributes encoded in the header's cookie fields.
    //
    private String comment;	// ;Comment=VALUE ... describes cookie's use
				// ;Discard ... implied by maxAge < 0
    private String domain;	// ;Domain=VALUE ... domain that sees cookie
    private int maxAge = -1;	// ;Max-Age=VALUE ... cookies auto-expire
    private String path;	// ;Path=VALUE ... URLs that see the cookie
    private boolean secure;	// ;Secure ... e.g. use SSL
    private int version = 0;	// ;Version=1 ... means RFC 2109++ style


    /**
     * Defines a cookie with an initial name/value pair.  The name must
     * be an HTTP/1.1 "token" value; alphanumeric ASCII strings work.
     * Names starting with a "$" character are reserved by RFC 2109.
     *
     * @param name name of the cookie
     * @param value value of the cookie
     * @throws IllegalArgumentException if the cookie name is not
     *	an HTTP/1.1 "token", or if it is one of the tokens reserved
     *	for use by the cookie protocol
     */
    public Cookie (String name, String value) {
	if (!isToken (name)
		|| name.equalsIgnoreCase ("Comment")	// rfc2019
		|| name.equalsIgnoreCase ("Discard")	// 2019++
		|| name.equalsIgnoreCase ("Domain")
		|| name.equalsIgnoreCase ("Expires")	// (old cookies)
		|| name.equalsIgnoreCase ("Max-Age")	// rfc2019
		|| name.equalsIgnoreCase ("Path")
		|| name.equalsIgnoreCase ("Secure")
		|| name.equalsIgnoreCase ("Version")
		)
	    throw new IllegalArgumentException ("cookie name: " + name);
	this.name = name;
	this.value = value;
    }



    /**
     * If a user agent (web browser) presents this cookie to a user, the
     * cookie's purpose will be described using this comment.  This is
     * not supported by version zero cookies.
     *
     * @see getComment
     */
    public void setComment (String purpose) {
	comment = purpose;
    }

    /**
     * Returns the comment describing the purpose of this cookie, or
     * null if no such comment has been defined.
     *
     * @see setComment
     */ 
    public String getComment () {
	return comment;
    }


    /**
     * This cookie should be presented only to hosts satisfying this domain
     * name pattern.  Read RFC 2109 for specific details of the syntax.
     * Briefly, a domain name name begins with a dot (".foo.com") and means
     * that hosts in that DNS zone ("www.foo.com", but not "a.b.foo.com")
     * should see the cookie.  By default, cookies are only returned to
     * the host which saved them.
     *
     * @see getDomain
     */
    public void setDomain (String pattern) {
	domain = pattern.toLowerCase ();	// IE allegedly needs this
    }

    /**
     * Returns the domain of this cookie.
     *
     * @see setDomain
     */ 
    public String getDomain () {
	return domain;
    }


    /**
     * Sets the maximum age of the cookie.  The cookie will expire
     * after that many seconds have passed.  Negative values indicate
     * the default behaviour:  the cookie is not stored persistently,
     * and will be deleted when the user agent (web browser) exits.
     * A zero value causes the cookie to be deleted.
     *
     * @see getMaxAge
     */
    public void setMaxAge (int expiry) {
	maxAge = expiry;
    }

    /**
     * Returns the maximum specified age of the cookie.  If none was
     * specified, a negative value is returned, indicating the default
     * behaviour described with <em>setMaxAge</em>.
     *
     * @see setMaxAge
     */
    public int getMaxAge () {
	return maxAge;
    }


    /**
     * This cookie should be presented only with requests beginning
     * with this URL.  Read RFC 2109 for a specification of the default
     * behaviour.  Basically, URLs in the same "directory" as the one
     * which set the cookie, and in subdirectories, can all see the
     * cookie unless a different path is set.
     *
     * @see getPath
     */
    public void setPath (String uri) {
	path = uri;
    }

    /**
     * Returns the prefix of all URLs for which this cookie is targetted.
     *
     * @see setPath
     */ 
    public String getPath () {
	return path;
    }


    /**
     * Indicates to the user agent that the cookie should only be sent
     * using a secure protocol (https).  This should only be set when
     * the cookie's originating server used a secure protocol to set the
     * cookie's value.
     *
     * @see getSecure
     */
    public void setSecure (boolean flag) {
	secure = flag;
    }

    /**
     * Returns the value of the 'secure' flag.
     *
     * @see setSecure
     */
    public boolean getSecure () {
	return secure;
    }


    /**
     * Returns the name of the cookie.  This name may not be changed
     * after the cookie is created.
     */
    public String getName () {
	return name;
    }


    /**
     * Sets the value of the cookie.  BASE64 encoding is suggested for
     * use with binary values.
     *
     * <P> With version zero cookies, you need to be careful about the
     * kinds of values you use.  Values with various special characters
     * (whitespace, brackets and parentheses, the equals sign, comma,
     * double quote, slashes, question marks, the "at" sign, colon, and
     * semicolon) should be avoided.  Empty values may not behave the
     * same way on all browsers.
     *
     * @see getValue
     */
    public void setValue (String newValue) {
	value = newValue;
    }

    /**
     * Returns the value of the cookie.
     *
     * @see setValue
     */
    public String getValue () {
	return value;
    }


    /**
     * Returns the version of the cookie.  Version 1 complies with
     * RFC 2109, version 0 indicates the original version, as specified
     * by Netscape.  Newly constructed cookies use version 0 by default,
     * to maximize interoperability.  Cookies provided by a user agent
     * will identify the cookie version used by the browser.
     *
     * @see setVersion
     */
    public int getVersion () {
	return version;
    }

    /**
     * Sets the version of the cookie protocol used when this cookie saves
     * itself.  Since the IETF standards are still being finalized, consider
     * version 1 as experimental; do not use it (yet) on production sites.
     *
     * @see getVersion
     */
    public void setVersion (int v) {
	version = v;
    }


    //
    // from RFC 2068, token special case characters
    //
    private static final String tspecials = "()<>@,;:\\\"/[]?={} \t";


    /*
     * Return true iff the string counts as an HTTP/1.1 "token".
     */
    private boolean isToken (String value) {
	int len = value.length ();

	for (int i = 0; i < len; i++) {
	    char c = value.charAt (i);

	    if (c < 0x20 || c >= 0x7f || tspecials.indexOf (c) != -1)
		return false;
	}
	return true;
    }


    /**
     * Returns a copy of this object.
     */
    public Object clone () {
	try {
	    return super.clone ();
	} catch (CloneNotSupportedException e) {
	    throw new RuntimeException (e.getMessage ());
	}
    }
}

