/*
 * @(#)InputStream.java	1.34 98/08/16
 *
 * Copyright 1994-1998 by Sun Microsystems, Inc.,
 * 901 San Antonio Road, Palo Alto, California, 94303, U.S.A.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of Sun Microsystems, Inc. ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with Sun.
 */


/**
 * This abstract class is the superclass of all classes representing
 * an input stream of bytes.
 *
 * <p> Applications that need to define a subclass of <code>InputStream</code>
 * must always provide a method that returns the next byte of input.
 *
 * @author  Arthur van Hoff
 * @version 1.34, 08/16/98
 * @see     java.io.BufferedInputStream
 * @see     java.io.ByteArrayInputStream
 * @see     java.io.DataInputStream
 * @see     java.io.FilterInputStream
 * @see     java.io.InputStream#read()
 * @see     java.io.OutputStream
 * @see     java.io.PushbackInputStream
 * @since   JDK1.0
 */
 
package java.io;

import harpoon.Analysis.ContBuilder.*;

public abstract class InputStream {

    // SKIP_BUFFER_SIZE is used to determine the size of skipBuffer
    private static final int SKIP_BUFFER_SIZE = 2048;
    // skipBuffer is initialized in skip(long), if needed.
    private static byte[] skipBuffer;

    /**
     * Reads the next byte of data from the input stream. The value byte is
     * returned as an <code>int</code> in the range <code>0</code> to
     * <code>255</code>. If no byte is available because the end of the stream
     * has been reached, the value <code>-1</code> is returned. This method
     * blocks until input data is available, the end of the stream is detected,
     * or an exception is thrown.
     *
     * <p> A subclass must provide an implementation of this method.
     *
     * @return     the next byte of data, or <code>-1</code> if the end of the
     *             stream is reached.
     * @exception  IOException  if an I/O error occurs.
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
     * Reads some number of bytes from the input stream and stores them into
     * the buffer array <code>b</code>. The number of bytes actually read is
     * returned as an integer.  This method blocks until input data is
     * available, end of file is detected, or an exception is thrown.
     *
     * <p> If <code>b</code> is <code>null</code>, a
     * <code>NullPointerException</code> is thrown.  If the length of
     * <code>b</code> is zero, then no bytes are read and <code>0</code> is
     * returned; otherwise, there is an attempt to read at least one byte. If
     * no byte is available because the stream is at end of file, the value
     * <code>-1</code> is returned; otherwise, at least one byte is read and
     * stored into <code>b</code>.
     *
     * <p> The first byte read is stored into element <code>b[0]</code>, the
     * next one into <code>b[1]</code>, and so on. The number of bytes read is,
     * at most, equal to the length of <code>b</code>. Let <i>k</i> be the
     * number of bytes actually read; these bytes will be stored in elements
     * <code>b[0]</code> through <code>b[</code><i>k</i><code>-1]</code>,
     * leaving elements <code>b[</code><i>k</i><code>]</code> through
     * <code>b[b.length-1]</code> unaffected.
     *
     * <p> If the first byte cannot be read for any reason other than end of
     * file, then an <code>IOException</code> is thrown. In particular, an
     * <code>IOException</code> is thrown if the input stream has been closed.
     *
     * <p> The <code>read(b)</code> method for class <code>InputStream</code>
     * has the same effect as: <pre><code> read(b, 0, b.length) </code></pre>
     *
     * @param      b   the buffer into which the data is read.
     * @return     the total number of bytes read into the buffer, or
     *             <code>-1</code> is there is no more data because the end of
     *             the stream has been reached.
     * @exception  IOException  if an I/O error occurs.
     * @see        java.io.InputStream#read(byte[], int, int)
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
     * Reads up to <code>len</code> bytes of data from the input stream into
     * an array of bytes.  An attempt is made to read as many as
     * <code>len</code> bytes, but a smaller number may be read, possibly
     * zero. The number of bytes actually read is returned as an integer.
     *
     * <p> This method blocks until input data is available, end of file is
     * detected, or an exception is thrown.
     *
     * <p> If <code>b</code> is <code>null</code>, a
     * <code>NullPointerException</code> is thrown.
     *
     * <p> If <code>off</code> is negative, or <code>len</code> is negative, or
     * <code>off+len</code> is greater than the length of the array
     * <code>b</code>, then an <code>IndexOutOfBoundsException</code> is
     * thrown.
     *
     * <p> If <code>len</code> is zero, then no bytes are read and
     * <code>0</code> is returned; otherwise, there is an attempt to read at
     * least one byte. If no byte is available because the stream is at end of
     * file, the value <code>-1</code> is returned; otherwise, at least one
     * byte is read and stored into <code>b</code>.
     *
     * <p> The first byte read is stored into element <code>b[off]</code>, the
     * next one into <code>b[off+1]</code>, and so on. The number of bytes read
     * is, at most, equal to <code>len</code>. Let <i>k</i> be the number of
     * bytes actually read; these bytes will be stored in elements
     * <code>b[off]</code> through <code>b[off+</code><i>k</i><code>-1]</code>,
     * leaving elements <code>b[off+</code><i>k</i><code>]</code> through
     * <code>b[off+len-1]</code> unaffected.
     *
     * <p> In every case, elements <code>b[0]</code> through
     * <code>b[off]</code> and elements <code>b[off+len]</code> through
     * <code>b[b.length-1]</code> are unaffected.
     *
     * <p> If the first byte cannot be read for any reason other than end of
     * file, then an <code>IOException</code> is thrown. In particular, an
     * <code>IOException</code> is thrown if the input stream has been closed.
     *
     * <p> The <code>read(b,</code> <code>off,</code> <code>len)</code> method
     * for class <code>InputStream</code> simply calls the method
     * <code>read()</code> repeatedly. If the first such call results in an
     * <code>IOException</code>, that exception is returned from the call to
     * the <code>read(b,</code> <code>off,</code> <code>len)</code> method.  If
     * any subsequent call to <code>read()</code> results in a
     * <code>IOException</code>, the exception is caught and treated as if it
     * were end of file; the bytes read up to that point are stored into
     * <code>b</code> and the number of bytes read before the exception
     * occurred is returned.  Subclasses are encouraged to provide a more
     * efficient implementation of this method.
     *
     * @param      b     the buffer into which the data is read.
     * @param      off   the start offset in array <code>b</code>
     *                   at which the data is written.
     * @param      len   the maximum number of bytes to read.
     * @return     the total number of bytes read into the buffer, or
     *             <code>-1</code> if there is no more data because the end of
     *             the stream has been reached.
     * @exception  IOException  if an I/O error occurs.
     * @see        java.io.InputStream#read()
     */
    public int read(byte b[], int off, int len) throws IOException {
	if (b == null) {
	    throw new NullPointerException();
	} else if ((off < 0) || (off > b.length) || (len < 0) ||
		   ((off + len) > b.length) || ((off + len) < 0)) {
	    throw new IndexOutOfBoundsException();
	} else if (len == 0) {
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
	if (!c.done) {
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
	public readAsync2C(byte b[], int off) { this.b= b; this.off= off; }
		
	public void resume(int result)
	{
	    b[off]= (byte) result;
	    next.resume(1);
	}
    }
	
    /**
     * Skips over and discards <code>n</code> bytes of data from this input
     * stream. The <code>skip</code> method may, for a variety of reasons, end
     * up skipping over some smaller number of bytes, possibly <code>0</code>.
     * This may result from any of a number of conditions; reaching end of file
     * before <code>n</code> bytes have been skipped is only one possibility.
     * The actual number of bytes skipped is returned.  If <code>n</code> is
     * negative, no bytes are skipped.
     *
     * <p> The <code>skip</code> method of <code>InputStream</code> creates a
     * byte array and then repeatedly reads into it until <code>n</code> bytes
     * have been read or the end of the stream has been reached. Subclasses are
     * encouraged to provide a more efficient implementation of this method.
     *
     * @param      n   the number of bytes to be skipped.
     * @return     the actual number of bytes skipped.
     * @exception  IOException  if an I/O error occurs.
     */
    public long skip(long n) throws IOException {

	long remaining = n;
	int nr;
	if (skipBuffer == null)
	    skipBuffer = new byte[SKIP_BUFFER_SIZE];
	
	// I have NO IDEA why they do this here. So I kinda skipped it
	byte[] localSkipBuffer = skipBuffer;
		
	if (n <= 0) {
	    return 0;
	}

	while (remaining > 0) {
	    nr = read(localSkipBuffer, 0,
		      (int) Math.min(SKIP_BUFFER_SIZE, remaining));
	    if (nr < 0) {
		break;
	    }
	    remaining -= nr;
	}
	
	return n - remaining;
    }
    
    // I use readAsync(byte[], int, int). Right now, I'd be better off using readAsync(), but the former is supposed to get more efficient
    public LongContinuation skipAsyncO(long n) throws IOException
    {
	long remaining = n;
	int nr;
	
	if (skipBuffer == null)
	    skipBuffer = new byte[SKIP_BUFFER_SIZE];
		
	if (n <= 0) {
	    return new LongContinuationOpt(0);
	}

	while (remaining > 0) {
	    IntContinuation c= readAsyncO(skipBuffer, 0, (int) Math.min(SKIP_BUFFER_SIZE, remaining));
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
	return new LongContinuationOpt((int) (n - remaining));    	
    }
    
    // note: the current instance of InputStream is implicitly stored by java.
    class skipAsyncC extends LongContinuation implements IntResultContinuation
    {
	public void exception(Throwable t) {
	}

	long n;
	long remaining;
	public skipAsyncC(long n, long remaining)
	{
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
		    IntContinuation c=readAsyncO(skipBuffer, 0, (int) Math.min(SKIP_BUFFER_SIZE, remaining));
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
     * Returns the number of bytes that can be read (or skipped over) from
     * this input stream without blocking by the next caller of a method for
     * this input stream.  The next caller might be the same thread or or
     * another thread.
     *
     * <p> The <code>available</code> method for class <code>InputStream</code>
     * always returns <code>0</code>.
     *
     * <p> This method should be overridden by subclasses.
     *
     * @return     the number of bytes that can be read from this input stream
     *             without blocking.
     * @exception  IOException  if an I/O error occurs.
     */
    public int available() throws IOException {
	return 0;
    }

    /**
     * Closes this input stream and releases any system resources associated
     * with the stream.
     *
     * <p> The <code>close</code> method of <code>InputStream</code> does
     * nothing.
     *
     * @exception  IOException  if an I/O error occurs.
     */
    public void close() throws IOException {}

    /**
     * Marks the current position in this input stream. A subsequent call to
     * the <code>reset</code> method repositions this stream at the last marked
     * position so that subsequent reads re-read the same bytes.
     *
     * <p> The <code>readlimit</code> arguments tells this input stream to
     * allow that many bytes to be read before the mark position gets
     * invalidated.
     *
     * <p> The general contract of <code>mark</code> is that, if the method
     * <code>markSupported</code> returns <code>true</code>, the stream somehow
     * remembers all the bytes read after the call to <code>mark</code> and
     * stands ready to supply those same bytes again if and whenever the method
     * <code>reset</code> is called.  However, the stream is not required to
     * remember any data at all if more than <code>readlimit</code> bytes are
     * read from the stream before <code>reset</code> is called.
     *
     * <p> The <code>mark</code> method of <code>InputStream</code> does
     * nothing.
     *
     * @param   readlimit   the maximum limit of bytes that can be read before
     *                      the mark position becomes invalid.
     * @see     java.io.InputStream#reset()
     */
    public synchronized void mark(int readlimit) {}

    /**
     * Repositions this stream to the position at the time the
     * <code>mark</code> method was last called on this input stream.
     *
     * <p> The general contract of <code>reset</code> is:
     *
     * <p><ul>
     *
     * <li> If the method <code>markSupported</code> returns
     * <code>true</code>, then:
     *
     *     <ul><li> If the method <code>mark</code> has not been called since
     *     the stream was created, or the number of bytes read from the stream
     *     since <code>mark</code> was last called is larger than the argument
     *     to <code>mark</code> at that last call, then an
     *     <code>IOException</code> might be thrown.
     *
     *     <li> If such an <code>IOException</code> is not thrown, then the
     *     stream is reset to a state such that all the bytes read since the
     *     most recent call to <code>mark</code> (or since the start of the
     *     file, if <code>mark</code> has not been called) will be resupplied
     *     to subsequent callers of the <code>read</code> method, followed by
     *     any bytes that otherwise would have been the next input data as of
     *     the time of the call to <code>reset</code>. </ul>
     *
     * <li> If the method <code>markSupported</code> returns
     * <code>false</code>, then:
     *
     *     <ul><li> The call to <code>reset</code> may throw an
     *     <code>IOException</code>.
     *
     *     <li> If an <code>IOException</code> is not thrown, then the stream
     *     is reset to a fixed state that depends on the particular type of the
     *     input stream and how it was created. The bytes that will be supplied
     *     to subsequent callers of the <code>read</code> method depend on the
     *     particular type of the input stream. </ul></ul>
     *
     * <p> The method <code>reset</code> for class <code>InputStream</code>
     * does nothing and always throws an <code>IOException</code>.
     *
     * @exception  IOException  if this stream has not been marked or if the
     *               mark has been invalidated.
     * @see     java.io.InputStream#mark(int)
     * @see     java.io.IOException
     */
    public synchronized void reset() throws IOException {
	throw new IOException("mark/reset not supported");
    }

    /**
     * Tests if this input stream supports the <code>mark</code> and
     * <code>reset</code> methods. The <code>markSupported</code> method of
     * <code>InputStream</code> returns <code>false</code>.
     *
     * @return  <code>true</code> if this true type supports the mark and reset
     *          method; <code>false</code> otherwise.
     * @see     java.io.InputStream#mark(int)
     * @see     java.io.InputStream#reset()
     */
    public boolean markSupported() {
	return false;
    }

}

