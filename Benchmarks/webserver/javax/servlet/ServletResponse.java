/*
 * @(#)ServletResponse.java	1.25 97/11/21
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
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;


/**
 * Interface for sending MIME data from the servlet's service method
 * to the client.  Network service developers implement this interface;
 * its methods are then used by servlets when the service method is
 * run, to return data to clients.  The ServletResponse object is passed
 * as an argument to the service method.
 *
 * <P> To write MIME bodies which consist of binary data, use the
 * output stream returned by <code><em>getOutputStream</em></code>.  To
 * write MIME bodies consisting of text data, use the writer returned
 * by <code><em>getWriter</em></code>.  If you need to mix binary and
 * text data, for example because you're creating a multipart response,
 * use the output stream to write the multipart headers, and use that
 * to build your own text bodies.
 * 
 * <p>If you don't explicitly set the character set in your MIME media
 * type, with <code><em>setContentType</em></code>, one will be
 * selected and the content type will be modified accordingly.  If you
 * will be using a writer, and want to call the
 * <code>setContentType</code> method, you must do so before calling
 * the <code>getWriter</code> method. If you will be using the output
 * stream, and want to call <code>setContentType</code>, you must do so
 * before using the output stream to write the MIME body.
 *
 * <P> For more information about MIME, see the Internet RFCs such as
 * <a href="http://info.internet.isi.edu/in-notes/rfc/files/rfc2045.txt">
 * RFC 2045</a>, the first in a series which defines MIME.  Note that
 * protocols such SMTP and HTTP define application-specific profiles of
 * MIME, and that standards in this area are evolving.
 * 
 * @version 1.25, 11/21/97 */

public
interface ServletResponse {
    /**
     * Sets the content length for this response.
     *
     * @param len the content length
     */
    public void setContentLength(int len);

    /**
     * Sets the content type for this response.  This type may later
     * be implicitly modified by addition of properties such as the MIME
     * <em>charset=&lt;value&gt;</em> if the service finds it necessary,
     * and the appropriate media type property has not been set.
     *
     * <p>This response property may only be assigned one time.  If a
     * writer is to be used to write a text response, this method must
     * be called before the method <code>getWriter</code>.  If an
     * output stream will be used to write a response, this method must
     * be called before the output stream is used to write response
     * data.
     *
     * @param type the content's MIME type
     * @see getOutputStream
     * @see getWriter */
    public void setContentType(String type);

    /**
     * Returns an output stream for writing binary response data.
     *
     * @see getWriter
     * @exception IllegalStateException if getWriter has been
     *	called on this same request.
     * @exception IOException if an I/O exception has occurred
     */
    public ServletOutputStream getOutputStream() throws IOException;

    /**
     * Returns a print writer for writing formatted text responses.  The
     * MIME type of the response will be modified, if necessary, to reflect
     * the character encoding used, through the <em>charset=...</em>
     * property.  This means that the content type must be set before
     * calling this method.
     *
     * @see getOutputStream
     * @see setContentType
     *
     * @exception UnsupportedEncodingException if no such encoding can
     * be provided
     * @exception IllegalStateException if getOutputStream has been
     *	called on this same request.
     * @exception IOException on other errors.
     */
    public PrintWriter getWriter () throws IOException;

    /**
     * Returns the character set encoding used for this MIME body.
     * The character encoding is either the one specified in the
     * assigned content type, or one which the client understands.
     * If no content type has yet been assigned, it is implicitly
     * set to <em>text/plain</em>
     */
    public String getCharacterEncoding ();
}
