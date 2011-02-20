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

import java.net.URLEncoder;
import java.util.Date;
import java.util.Vector;
import java.util.StringTokenizer;
import java.util.Locale;
import java.util.TimeZone;
import java.util.NoSuchElementException;
import java.text.SimpleDateFormat;
import javax.servlet.http.Cookie;

/**
 * Various utility methods used by the servlet engine.
 *
 * @author Francis J. Lacoste
 * @author Ian Kluft
 * @version $Revision: 1.1 $ $Date: 2000-06-29 01:41:56 $
 */
public final class JServUtils {

    /**
     * This method urlencodes the given string. This method is here for
     * symmetry and simplicity reasons and just calls URLEncoder.encode().
     *
     * @param  str the string
     * @return the url-encoded string
     */
    public final static String URLEncode(String str) {
        if (str == null)  return  null;
        return URLEncoder.encode(str);
    }

    /**
     * This method decodes the given urlencoded string.
     *
     * @param  str the url-encoded string
     * @return the decoded string
     * @exception IllegalArgumentException If a '%' is not
     * followed by a valid 2-digit hex number.
     */
    public final static String URLDecode(String str)
            throws IllegalArgumentException {
        if (str == null)  return  null;

        StringBuffer dec = new StringBuffer();    // decoded string output
        int strPos = 0;
        int strLen = str.length();

        dec.ensureCapacity(str.length());
        while (strPos < strLen) {
            int laPos;        // lookahead position

            // look ahead to next URLencoded metacharacter, if any
            for (laPos = strPos; laPos < strLen; laPos++) {
                char laChar = str.charAt(laPos);
                if ((laChar == '+') || (laChar == '%')) {
                    break;
                }
            }

            // if there were non-metacharacters, copy them all as a block
            if (laPos > strPos) {
                dec.append(str.substring(strPos,laPos));
                strPos = laPos;
            }

            // shortcut out of here if we're at the end of the string
            if (strPos >= strLen) {
                break;
            }

            // process next metacharacter
            char metaChar = str.charAt(strPos);
            if (metaChar == '+') {
                dec.append(' ');
                strPos++;
                continue;
            } else if (metaChar == '%') {
                try {
                    dec.append((char) Integer.parseInt(
                    str.substring(strPos + 1, strPos + 3), 16));
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("invalid hexadecimal "
                    + str.substring(strPos + 1, strPos + 3)
                    + " in URLencoded string (illegal unescaped '%'?)" );
                } catch (StringIndexOutOfBoundsException e) {
                    throw new IllegalArgumentException("illegal unescaped '%' "
                    + " in URLencoded string" );
                }
                strPos += 3;
            }
        }

        return dec.toString();
    }

    /**
     * Parse a cookie header into an array of cookies as per
     * RFC2109 - HTTP Cookies
     *
     * @param cookieHdr The Cookie header value.
     */
    public static Cookie[] parseCookieHeader(String cookieHdr) {
        Vector cookieJar = new Vector();

        if(cookieHdr == null || cookieHdr.length() == 0)
            return new Cookie[0];

        StringTokenizer stok = new StringTokenizer(cookieHdr, "; ");
        while (stok.hasMoreTokens()) {
            try {
                String tok = stok.nextToken();
                int equals_pos = tok.indexOf('=');
                if (equals_pos > 0) {
                    String name = tok.substring(0, equals_pos);
                    String value = tok.substring(equals_pos + 1);
                    cookieJar.addElement(new Cookie(name, value));
                }
                else if ( tok.length() > 0 && equals_pos == -1 ) {
                    String name = URLDecode(tok);
                    cookieJar.addElement(new Cookie(name, ""));
                }
            } catch (IllegalArgumentException badcookie) {
            } catch (NoSuchElementException badcookie) {
            }
        }
    
        Cookie[] cookies = new Cookie[cookieJar.size()];
        cookieJar.copyInto(cookies);
        return cookies;
    }

    private static SimpleDateFormat cookieDate =
        new SimpleDateFormat("EEE, dd-MMM-yyyy HH:mm:ss zz", Locale.US );

    static {
        cookieDate.setTimeZone(TimeZone.getTimeZone("GMT"));
    }

    /**
     * Encode a cookie as per the Netscape Cookies specification. The
     * resulting string can be used in a Set-Cookie header.
     *
     * @param cookie The cookie to encode.
     * @return A string following Netscape Cookies specification.
     */
    public static String encodeCookie(Cookie cookie) {
        StringBuffer buf = new StringBuffer( cookie.getName() );
        buf.append('=');
        buf.append(cookie.getValue());

        long age = cookie.getMaxAge();
        if (age > 0) {
            buf.append("; expires=");
            buf.append(cookieDate.format(
                new Date(System.currentTimeMillis() + (long)age * 1000 )));
        } else if (age == 0) {
            buf.append("; expires=");
            // Set expiration to the epoch to delete the cookie
            buf.append(cookieDate.format(new Date(1000)));
        }

        if (cookie.getDomain() != null) {
            buf.append("; domain=");
            buf.append(cookie.getDomain());
        }

        if (cookie.getPath() != null) {
            buf.append("; path=");
            buf.append(cookie.getPath());
        }

        if (cookie.getSecure()) {
            buf.append("; secure");
        }

        return buf.toString();
    }

    /**
     * Parse a content-type header for the character encoding. If the
     * content-type is null or there is no explicit character encoding,
     * ISO-8859-1 is returned.
     *
     * @param contentType a content type header.
     */
    public static String parseCharacterEncoding(String contentType) {
        int start;
        int end;

        if ((contentType == null)
                || ((start = contentType.indexOf("charset="))) == -1 ) {
            return "ISO-8859-1";
        }

        String encoding = contentType.substring(start + 8);

        if ((end = encoding.indexOf(";")) > -1) {
            return encoding.substring(0, end);
        } else {
            return encoding;
        }
    }
}
