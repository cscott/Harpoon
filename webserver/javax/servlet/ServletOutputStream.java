/*
 * @(#)ServletOutputStream.java	1.12 98/04/15
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

import java.io.OutputStream;
import java.io.IOException;
import java.io.CharConversionException;


/**
 * An output stream for writing servlet responses.  This is an
 * abstract class, to be implemented by a network services
 * implementor.  Servlet writers use the output stream to return data
 * to clients.  They access it via the ServletResponse's
 * getOutputStream method, available from within the servlet's service
 * method.  Subclasses of ServletOutputStream must provide an
 * implementation of the write(int) method.
 * 
 * @see java.io.OutputStream#write(int)
 *
 * @version	1.12, 04/15/98
 */

public abstract
class ServletOutputStream extends OutputStream {

    /**
     * The default constructor does no work.
     */
    protected ServletOutputStream () { }


    /**
     * Prints the string provided.
     * @exception IOException if an I/O error has occurred
     */
    public void print(String s) throws IOException {
	int len = s.length();
	for (int i = 0; i < len; i++) {
	    char c = s.charAt (i);

	    //
	    // XXX NOTE:  This is clearly incorrect for many strings,
	    // but is the only consistent approach within the current
	    // servlet framework.  It must suffice until servlet output
	    // streams properly encode their output.
	    //
	    if ((c & 0xff00) != 0)	// high order byte must be zero
		throw new CharConversionException (
		    "Not an ISO 8859/1 character:  " + c);
	    write (c);
	}
    }

    /**
     * Prints the boolean provided.
     * @exception IOException if an I/O error has occurred.
     */
    public void print(boolean b) throws IOException {
	print(b ? "true" : "false");
    }

    /**
     * Prints the character provided.
     * @exception IOException if an I/O error has occurred
     */
    public void print(char c) throws IOException {
	print(String.valueOf(c));
    }

    /**
     * Prints the integer provided.
     * @exception IOException if an I/O error has occurred
     */  
    public void print(int i) throws IOException {
	print(String.valueOf(i));
    }
 
    /**
     * Prints the long provided.
     * @exception IOException if an I/O error has occurred
     */
    public void print(long l) throws IOException {
	print(String.valueOf(l));
    }

    /**
     * Prints the float provided.
     * @exception IOException if an I/O error has occurred
     */
    public void print(float f) throws IOException {
	print(String.valueOf(f));
    }

    /**
     * Prints the double provided.
     * @exception IOException if an I/O error has occurred
     */
    public void print(double d) throws IOException {
	print(String.valueOf(d));
    }

    /**
     * Prints a CRLF.
     * @exception IOException if an I/O error has occurred
     */
    public void println() throws IOException {
	print("\r\n");
    }

    /**
     * Prints the string provided, followed by a CRLF.
     * @exception IOException if an I/O error has occurred
     */
    public void println(String s) throws IOException {
	print(s);
	println();
    }

    /**
     * Prints the boolean provided, followed by a CRLF.
     * @exception IOException if an I/O error has occurred.
     */
    public void println(boolean b) throws IOException {
	print(b);
	println();
    }

    /**
     * Prints the character provided, followed by a CRLF.
     * @exception IOException if an I/O error has occurred
     */
    public void println(char c) throws IOException {
	print(c);
	println();
    }

    /**
     * Prints the integer provided, followed by a CRLF.
     * @exception IOException if an I/O error has occurred
     */
    public void println(int i) throws IOException {
	print(i);
	println();
    }

    /**  
     * Prints the long provided, followed by a CRLF.
     * @exception IOException if an I/O error has occurred
     */  
    public void println(long l) throws IOException {
	print(l);
	println();
    }

    /**
     * Prints the float provided, followed by a CRLF.
     * @exception IOException if an I/O error has occurred
     */
    public void println(float f) throws IOException {
	print(f);
	println();
    }

    /**
     * Prints the double provided, followed by a CRLF.
     * @exception IOException if an I/O error has occurred
     */
    public void println(double d) throws IOException {
	print(d);
	println();
    }
}
