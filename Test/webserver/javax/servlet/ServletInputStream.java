/*
 * @(#)ServletInputStream.java	1.12 97/07/15
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

import java.io.InputStream;
import java.io.IOException;

/**
 * 
 * An input stream for reading servlet requests, it provides an
 * efficient readLine method.  This is an abstract class, to be
 * implemented by a network services writer.  For some application
 * protocols, such as the HTTP POST and PUT methods, servlet writers
 * use the input stream to get data from clients.  They access the
 * input stream via the ServletRequest's getInputStream method,
 * available from within the servlet's service method.  Subclasses of
 * ServletInputStream must provide an implementation of the read()
 * method.
 *
 * @see java.io.InputStream#read() 
 *
 * @version	1.12, 07/15/97
 */
public abstract
class ServletInputStream extends InputStream {

    /**
     * The default constructor does no work.
     */
    protected ServletInputStream () { }


    /**
     * Starting at the specified offset, reads into the given array of
     * bytes until all requested bytes have been read or a '\n' is
     * encountered, in which case the '\n' is read into the array as well.
     * @param b the buffer into which the data is read
     * @param off the start offset of the data
     * @param len the maximum number of bytes to read
     * @return the actual number of bytes read, or -1 if the end of the
     *         stream is reached
     * @exception IOException if an I/O error has occurred
     */
    public int readLine(byte[] b, int off, int len) throws IOException {
	if (len <= 0) {
	    return 0;
	}
	int count = 0, c;
	while ((c = read()) != -1) {
	    b[off++] = (byte)c;
	    count++;
	    if (c == '\n') {
		break;
	    }
	}
	return count > 0 ? count : -1;
    }
}
