/*
 * %W% %G%
 *
 * Copyright (c) 1998 Standard Performance Evaluation Corporation (SPEC)
 *               All rights reserved.
 * Copyright (c) 1997,1998 Sun Microsystems, Inc. All rights reserved.
 *
 * This source code is provided as is, without any express or implied warranty.
 */

/**
 * This class implements a special from of OutputStream that is used to
 * direct console output from Context.out to a special window.
 *
 * @see spec.io.PrintStream
 */ 
public
class ConsoleOutputStream extends java.io.OutputStream {   
    /**
     * Temporary line buffer
     */
    StringBuffer lineBuffer = new StringBuffer(256);
      
   /**
     * Change the validity checking value. This is only for compatability with
     * ValidityCheckOutputStream, 
     * @param v the value (between 0 and 8)
     * @return the value we replaced
     */
    public char setValidityCheckValue(char v) {
	return v;
    }    
     
    /**
     * Writes a byte. 
     * @param b the byte
     * @exception IOException If an I/O error has occurred.
     */
    public void write(int b) throws java.io.IOException {
	/*
	 * Just let old fashoned ASCII through
	 */
	if ((b >= 0x20 && b <= 0x7f) || b == '\t' || b == '\n') {		
	    lineBuffer.append((char)b);
	    if (b == '\n') {
		flush();
	    }
	}
    }

    /**
     * Flushes the stream. This will write any buffered
     * output bytes.
     * @exception IOException If an I/O error has occurred.
     */
    public void flush() throws java.io.IOException {
	Context.appendWindow(lineBuffer.toString());
	lineBuffer = new StringBuffer(256);    
    }

    /**
     * Writes an array of bytes.
     * @param b	the data to be written
     * @exception IOException If an I/O error has occurred.
     */
    public void write(byte b[]) throws java.io.IOException {
	write(b, 0, b.length);
    }

    /**
     * Writes a subarray of bytes.
     * @param b	the data to be written
     * @param off the start offset in the data
     * @param len the number of bytes that are written
     * @exception IOException If an I/O error has occurred.
     */
    public void write(byte b[], int off, int len) throws java.io.IOException {
	for (int i = 0 ; i < len ; i++) {
	    write(b[off + i]);
	}
    }
}
