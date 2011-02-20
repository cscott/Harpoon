/*
 * @(#)InputStream.java	1.22 98/07/01
 *
 * Copyright 1995-1998 by Sun Microsystems, Inc.,
 * 901 San Antonio Road, Palo Alto, California, 94303, U.S.A.
 * All rights reserved.
 * 
 * This software is the confidential and proprietary information
 * of Sun Microsystems, Inc. ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with Sun.
 */

package java.io;

import harpoon.Analysis.ContBuilder.*;

/**
 * This abstract class is the superclass of all classes representing 
 * an input stream of bytes. 
 * <p>
 * Applications that need to define a subclass of 
 * <code>InputStream</code> must always provide a method that returns 
 * the next byte of input.
 * 
 * @author  Arthur van Hoff
 * @version 1.22, 07/01/98
 * @see     java.io.BufferedInputStream
 * @see     java.io.ByteArrayInputStream
 * @see     java.io.DataInputStream
 * @see     java.io.FilterInputStream
 * @see     java.io.InputStream#read()
 * @see     java.io.OutputStream
 * @see     java.io.PushbackInputStream
 * @since   JDK1.0
 */
public abstract class InputStream {

    // SKIP_BUFFER_SIZE is used to determine the size of skipBuffer
    private static final int SKIP_BUFFER_SIZE = 2048;
    // skipBuffer is initialized in skip(long), if needed.
    private static byte[] skipBuffer;

    /**
     * Reads the next byte of data from this input stream. The value 
     * byte is returned as an <code>int</code> in the range 
     * <code>0</code> to <code>255</code>. If no byte is available 
     * because the end of the stream has been reached, the value 
     * <code>-1</code> is returned. This method blocks until input data 
     * is available, the end of the stream is detected, or an exception 
     * is thrown. 
     * <p>
     * A subclass must provide an implementation of this method. 
     *
     * @return     the next byte of data, or <code>-1</code> if the end of the
     *             stream is reached.
     * @exception  IOException  if an I/O error occurs.
     * @since      JDK1.0
     */
    public abstract int read() throws IOException;

    // Here's the deal: I've got optimistic versions that I feel we're going
    // to use again soon. Rather than writing new, pesimistic ones, I wrap them
    // up in pesimistic procedures.
    public IntContinuation readAsyncO() throws IOException
	// default version: blocks
    {
	return new IntContinuationOpt(read());
    }
    
    // pesimistic versions are guarranteed not to return null;
    public IntContinuation readAsync()
    {
	try {
	    return IntDoneContinuation.pesimistic(readAsyncO());
	} catch (IOException e) {
	    return new IntDoneContinuation(e);
	}
    }
    
    // make this input stream asynchronous
    public void makeAsync() { };
    
    
    /**
     * Reads up to <code>b.length</code> bytes of data from this input 
     * stream into an array of bytes. 
     * <p>
     * The <code>read</code> method of <code>InputStream</code> calls 
     * the <code>read</code> method of three arguments with the arguments 
     * <code>b</code>, <code>0</code>, and <code>b.length</code>. 
     *
     * @param      b   the buffer into which the data is read.
     * @return     the total number of bytes read into the buffer, or
     *             <code>-1</code> is there is no more data because the end of
     *             the stream has been reached.
     * @exception  IOException  if an I/O error occurs.
     * @see        java.io.InputStream#read(byte[], int, int)
     * @since      JDK1.0
     */
    public int read(byte b[]) throws IOException {
	return read(b, 0, b.length);
    }

    public IntContinuation readAsync(byte b[]) throws IOException {
	return readAsync(b, 0, b.length);
    }

    public IntContinuation readAsyncO(byte b[]) throws IOException {
	return readAsyncO(b, 0, b.length);
    }


    /**
     * Reads up to <code>len</code> bytes of data from this input stream 
     * into an array of bytes. This method blocks until some input is 
     * available. If the argument <code>b</code> is <code>null</code>, a  
     * <code>NullPointerException</code> is thrown.
     * <p>
     * The <code>read</code> method of <code>InputStream</code> reads a 
     * single byte at a time using the read method of zero arguments to 
     * fill in the array. Subclasses are encouraged to provide a more 
     * efficient implementation of this method. 
     *
     * @param      b     the buffer into which the data is read.
     * @param      off   the start offset of the data.
     * @param      len   the maximum number of bytes read.
     * @return     the total number of bytes read into the buffer, or
     *             <code>-1</code> if there is no more data because the end of
     *             the stream has been reached.
     * @exception  IOException  if an I/O error occurs.
     * @see        java.io.InputStream#read()
     * @since      JDK1.0
     */
    public int read(byte b[], int off, int len) throws IOException {
	if (len <= 0) {
	    return 0;
	}

	int c = read();
	if (c == -1) {
	    return -1;
	}
	b[off] = (byte)c;

	int i = 1;
	try {
	    for (; i < len ; i++) {
		c = read();
		if (c == -1) {
		    break;
		}
		if (b != null) {
		    b[off + i] = (byte)c;
		}
	    }
	} catch (IOException ee) {
	}
	return i;
    }

    
    // default implementation: uses read();
    // kinda dumb: always reads 1 byte
   

    public IntContinuation readAsyncO(byte b[], int off, int len) throws IOException {
	if (b == null) {
	    throw new NullPointerException();
	} else if ((off < 0) || (off > b.length) || (len < 0) ||
		   ((off + len) > b.length) || ((off + len) < 0)) {
	    throw new IndexOutOfBoundsException();
	} else if (len == 0) {
	    return new IntContinuationOpt(0);
	}
	  	

	IntContinuation c = readAsyncO();
	if (c.done) {
	    b[off]= (byte) c.result;
	    return new IntContinuationOpt(1);
	}
	
	// I don't need the length     :)
	readAsync2C thisC= new readAsync2C(b, off);
	c.setNext(thisC);
	return thisC;
    }

    public IntContinuation readAsync(byte b[], int off, int len) {
	try {
	    return IntDoneContinuation.pesimistic(readAsyncO(b, off, len));
	} catch (IOException e) {
	    return new IntDoneContinuation(e);
	}
    }
    
    
    // Proposed style of naming Continuations: <classname>$<methodname><methodnr>C<contnr> 
    // <classname> : here: added implicitly by javac
    // <methodnr>  : the number of this method among the methods with the same name (here, two read methods);
    // <contnr>    : the number of this Continuation in the sequence
    // I ommit any of the last two if they are 1.
    class readAsync2C extends IntContinuation implements IntResultContinuation
    {
	public void exception(Throwable t) {
	}
	
	
	byte b[]; int off;
	public readAsync2C(byte b[], int off) { done=false; this.b= b; this.off= off; }
	
	public void resume(int result)
	{
	    b[off]= (byte) result;
	    next.resume(1);
	}
    }
    
    /**
     * Skips over and discards <code>n</code> bytes of data from this 
     * input stream. The <code>skip</code> method may, for a variety of 
     * reasons, end up skipping over some smaller number of bytes, 
     * possibly <code>0</code>. The actual number of bytes skipped is 
     * returned. 
     * <p>
     * The <code>skip</code> method of <code>InputStream</code> creates 
     * a byte array of length <code>n</code> and then reads into it until 
     * <code>n</code> bytes have been read or the end of the stream has 
     * been reached. Subclasses are encouraged to provide a more 
     * efficient implementation of this method. 
     *
     * @param      n   the number of bytes to be skipped.
     * @return     the actual number of bytes skipped.
     * @exception  IOException  if an I/O error occurs.
     * @since      JDK1.0
     */
    public long skip(long n) throws IOException {
    	/* ensure that the number is a positive int */
    	byte data[] = new byte[(int) (n & 0xEFFFFFFF)];
    	return read(data);
    }

    // I use readAsync(byte[], int, int). Right now, I'd be better off using readAsync(), but the former is supposed to get more efficient
    public LongContinuation skipAsyncO(long n) throws IOException
    {
	long remaining = n;
	int nr;
	
	if (skipBuffer == null)
	    skipBuffer = new byte[SKIP_BUFFER_SIZE];
	
	byte[] localSkipBuffer = skipBuffer;

	if (n <= 0) {
	    return new LongContinuationOpt(0);
	}
	
	while (remaining > 0) {
	    IntContinuation c= readAsyncO(localSkipBuffer, 0, (int) Math.min(SKIP_BUFFER_SIZE, remaining));
	    if (!c.done)
		{
		    skipAsyncC thisC= new skipAsyncC(n, remaining);
		    c.setNext(thisC);
		    return thisC;
		}
	    else nr= c.result;
	    if (nr < 0) {
		break;
	    }
	    remaining -= nr;
	}
	
	// got away with no contns.
	return new LongContinuationOpt((int) (n-remaining));
    }
    
    // note: the current instance of InputStream is implicitly stored by java.
    class skipAsyncC extends LongContinuation implements IntResultContinuation
    {
	public void exception(Throwable t) {
	}

	private Continuation link;
	
	public void setLink(Continuation newLink) { 
	    link= newLink;
	}
	
	public Continuation getLink() { 
	    return link;
	}
	long n;
	long remaining;
	public skipAsyncC(long n, long remaining)
	{
	    done=false;
	    this.n= n; this.remaining= remaining;
	}
          
	public void resume(int nr)
	{
            try{
		// rewrite what's left of the loop; if you guys have some sort of goto, that would be nice indeed
		if (nr<0) {
	    			// just happens that there's not much after the loop, otherwise: new continuation or goto really needed
                    next.resume(n-remaining);
		}
		remaining-= nr;
     		
     		while (remaining>0) {
		    IntContinuation c= readAsyncO(skipBuffer, 0, (int) Math.min(SKIP_BUFFER_SIZE, remaining));
		    if (!c.done)
			{
			    c.setNext(this);
			    return;
			}
		    else nr= c.result;
		    
		    if (nr < 0) {
			break;
		    }
		    remaining -= nr;
		}
		
		next.resume(n-remaining);
	    } catch (IOException e) { next.exception(e); }
	}
    }
    
    public LongContinuation skipAsync(long n)
    {
	try {
	    return LongDoneContinuation.pesimistic(skipAsyncO(n));
	} catch (IOException e) {
	    return new LongDoneContinuation(e);
	}
    }
    
    /**
     * Returns the number of bytes that can be read from this input 
     * stream without blocking. The available method of 
     * <code>InputStream</code> returns <code>0</code>. This method 
     * <B>should</B> be overridden by subclasses. 
     *
     * @return     the number of bytes that can be read from this input stream
     *             without blocking.
     * @exception  IOException  if an I/O error occurs.
     * @since	   JDK1.0
     */
    public int available() throws IOException {
	return 0;
    }

    /**
     * Closes this input stream and releases any system resources 
     * associated with the stream. 
     * <p>
     * The <code>close</code> method of <code>InputStream</code> does nothing.
     *
     * @exception  IOException  if an I/O error occurs.
     * @since      JDK1.0
     */
    public void close() throws IOException {}

    /**
     * Marks the current position in this input stream. A subsequent 
     * call to the <code>reset</code> method repositions this stream at 
     * the last marked position so that subsequent reads re-read the same 
     * bytes. 
     * <p>
     * The <code>readlimit</code> arguments tells this input stream to 
     * allow that many bytes to be read before the mark position gets 
     * invalidated. 
     * <p>
     * The <code>mark</code> method of <code>InputStream</code> does nothing.
     *
     * @param   readlimit   the maximum limit of bytes that can be read before
     *                      the mark position becomes invalid.
     * @see     java.io.InputStream#reset()
     * @since   JDK1.0
     */
    public synchronized void mark(int readlimit) {}

    /**
     * Repositions this stream to the position at the time the 
     * <code>mark</code> method was last called on this input stream. 
     * <p>
     * The <code>reset</code> method of <code>InputStream</code> throws 
     * an <code>IOException</code>, because input streams, by default, do 
     * not support <code>mark</code> and <code>reset</code>.
     * <p>
     * Stream marks are intended to be used in
     * situations where you need to read ahead a little to see what's in
     * the stream. Often this is most easily done by invoking some
     * general parser. If the stream is of the type handled by the
     * parser, it just chugs along happily. If the stream is not of
     * that type, the parser should toss an exception when it fails,
     * which, if it happens within readlimit bytes, allows the outer
     * code to reset the stream and try another parser.
     *
     * @exception  IOException  if this stream has not been marked or if the
     *               mark has been invalidated.
     * @see     java.io.InputStream#mark(int)
     * @see     java.io.IOException
     * @since   JDK1.0
     */
    public synchronized void reset() throws IOException {
	throw new IOException("mark/reset not supported");
    }

    /**
     * Tests if this input stream supports the <code>mark</code> 
     * and <code>reset</code> methods. The <code>markSupported</code> 
     * method of <code>InputStream</code> returns <code>false</code>. 
     *
     * @return  <code>true</code> if this true type supports the mark and reset
     *          method; <code>false</code> otherwise.
     * @see     java.io.InputStream#mark(int)
     * @see     java.io.InputStream#reset()
     * @since   JDK1.0
     */
    public boolean markSupported() {
	return false;
    }
}
