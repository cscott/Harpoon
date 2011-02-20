/*
 * @(#)HttpUtils.java	1.14 98/04/15
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

import javax.servlet.ServletInputStream;
import java.util.Hashtable;
import java.util.StringTokenizer;
import java.io.IOException;

/**
 * A collection of static utility methods useful to HTTP servlets.
 *
 * @version 1.14
 */
public class HttpUtils {

    static Hashtable nullHashtable = new Hashtable();
    
    /**
     * Creates an empty HttpUtils object.
     */
    public HttpUtils() {}

    /**
     * Parses a query string and builds a hashtable of key-value
     * pairs, where the values are arrays of strings.  The query string
     * should have the form of a string packaged by the GET or POST
     * method.  (For example, it should have its key-value pairs
     * delimited by ampersands (&) and its keys separated from its
     * values by equal signs (=).)
     * 
     * <p> A key can appear one or more times in the query string.
     * Each time a key appears, its corresponding value is inserted
     * into its string array in the hash table.  (So keys that appear
     * once in the query string have, in the hash table, a string array
     * of length one as their value, keys that appear twice have a
     * string array of length two, etc.)
     * 
     * <p> When the keys and values are moved into the hashtable, any
     * plus signs (+) are returned to spaces and characters sent in
     * hexadecimal notation (%xx) are converted back to characters.
     * 
     * @param s query string to be parsed
     * @return a hashtable built from the parsed key-value pairs; the
     *.hashtable's values are arrays of strings
     * @exception IllegalArgumentException if the query string is
     * invalid.
     */
    static public Hashtable parseQueryString(String s) {

	String valArray[] = null;
	
	if (s == null) {
	    throw new IllegalArgumentException();
	}
	Hashtable ht = new Hashtable();
	StringBuffer sb = new StringBuffer();
	StringTokenizer st = new StringTokenizer(s, "&");
	while (st.hasMoreTokens()) {
	    String pair = (String)st.nextToken();
	    int pos = pair.indexOf('=');
	    if (pos == -1) {
		throw new IllegalArgumentException();
	    }
	    String key = parseName(pair.substring(0, pos), sb);
	    String val = parseName(pair.substring(pos+1, pair.length()), sb);
	    if (ht.containsKey(key)) {
		String oldVals[] = (String []) ht.get(key);
		valArray = new String[oldVals.length + 1];
		for (int i = 0; i < oldVals.length; i++) 
		    valArray[i] = oldVals[i];
		valArray[oldVals.length] = val;
	    } else {
		valArray = new String[1];
		valArray[0] = val;
	    }
	    ht.put(key, valArray);
	}
	return ht;
    }

    /**
     * 
     * Parses FORM data that is posted to the server using the HTTP
     * POST method and the application/x-www-form-urlencoded mime
     * type.
     *
     * @param len the length of the data in the input stream.
     * @param in the input stream
     * @return a hashtable of the parsed key, value pairs.  Keys
     * with multiple values have their values stored as an array of strings
     * @exception IllegalArgumentException if the POST data is invalid.
     */
    static public Hashtable parsePostData(int len, 
					  ServletInputStream in) {

	int	inputLen, offset;
	byte[] postedBytes = null;
	String postedBody;

	if (len <=0)
	    return null;

	try {
	    //
	    // Make sure we read the entire POSTed body.
	    //
	    postedBytes = new byte [len];
	    offset = 0;
	    do {
		inputLen = in.read (postedBytes, offset, len - offset);
		if (inputLen <= 0)
		    throw new IOException ("short read");
		offset += inputLen;
	    } while ((len - offset) > 0);

	} catch (IOException e) {
	    return nullHashtable;
	}

	//
	// XXX we shouldn't assume that the only kind of POST body
	// is FORM data encoded using ASCII or ISO Latin/1 ... or
	// that the body should always be treated as FORM data.
	//
	postedBody = new String (postedBytes, 0, 0, len);

	return parseQueryString(postedBody); 
    }




    /*
     * Parse a name in the query string.
     */
    static private String parseName(String s, StringBuffer sb) {
	sb.setLength(0);
	for (int i = 0; i < s.length(); i++) {
	    char c = s.charAt(i);
	    switch (c) {
	      case '+':
		sb.append(' ');
		break;
	      case '%':
		try {
		    sb.append((char) Integer.parseInt(s.substring(i+1, i+3), 
                        16));
		} catch (NumberFormatException e) {
		    throw new IllegalArgumentException();
		}
		i += 2;
		break;
	      default:
		sb.append(c);
		break;
	    }
	}
	return sb.toString();
    }

    /**
     * Reconstructs the URL used by the client used to make the
     * request.  This accounts for differences such as addressing
     * scheme (http, https) and default ports, but does not attempt to
     * include query parameters.  Since it returns a StringBuffer, not
     * a String, the URL can be modified efficiently (for example, by
     * appending query parameters).
     *
     * <P> This method is useful for creating redirect messages and for
     * reporting errors.  */
    public static StringBuffer getRequestURL (HttpServletRequest req) {
	StringBuffer	url = new StringBuffer ();
	String		scheme = req.getScheme ();
	int		port = req.getServerPort ();

	String		servletPath = req.getServletPath ();
	String		pathInfo = req.getPathInfo ();

	url.append (scheme);		// http, https
	url.append ("://");
	url.append (req.getServerName ());
	if ((scheme.equals ("http") && port != 80)
		|| (scheme.equals ("https") && port != 443)) {
	    url.append (':');
	    url.append (req.getServerPort ());
	}
	if (servletPath != null)
	    url.append (servletPath);
	if (pathInfo != null)
	    url.append (pathInfo);
	return url;
    }
}
