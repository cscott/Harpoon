// WaitFreeDequeue.java, created by Dumitru Daniliuc, rewritten by Harvey Jones
// Copyright (C) 2003 Dumitru Daniliuc, Harvey Jones
// Licensed under the terms of the GNU GPL; see COPYING for details.
package javax.realtime;

/** The wait-free classes facilitate communication and synchronization
 *  between instances of <code>RealtimeThread</code> and <code>Thread</code>.
 *  <p>
 *  This class is provided for compliance with the RTSJ, see
 *  <code>Queue</code> for details.
 */
public class WaitFreeDequeue {

    protected WaitFreeReadQueue readQueue= null;
    protected WaitFreeWriteQueue writeQueue = null;
    
    protected Thread writerThread = null, readerThread = null;
    protected MemoryArea memArea;

    /** A queue with unsynchronized and nonblocking <code>read()</code>
     *  and <code>write()</code> methods and synchronized and blocking
     *  <code>read()</code> and <code>write()</code> methods.
     *
     *  @param writer An instance of <code>Thread</code>.
     *  @param reader An instance of <code>Thread</code>.
     *  @param maximum The maximum number of elements in both the
     *                 <code>WaitFreeReadQueue</code> and the
     *                 <code>WaitFreeWriteQueue</code>.
     *  @param area The <code>MemoryArea</code> in which this object and
     *              internal elements are allocated
     *  @throws java.lang.IllegalArgumentException If an argument holds an
     *                                             invalid value. The current
     *                                             memory areas of
     *                                             <code>writer</code>,
     *                                             <code>reader</code>, and
     *                                             <code>memory</code> must be
     *                                             compatible with respect to
     *                                             the assignment and access
     *                                             rules for memory areas.
     */
    public WaitFreeDequeue(Thread writer, Thread reader,
			   int maximum, MemoryArea area)
	throws IllegalArgumentException {
	writerThread = writer;
	readerThread = reader;
	memArea = area;
	try{
	    readQueue=new WaitFreeReadQueue(writer, reader, maximum, area, null);
	    writeQueue=new WaitFreeWriteQueue(writer, reader, maximum, area, null);
	} catch (Exception e){
	    System.out.println(e);
	    System.exit(-1);
	}
   }

    /** A synchronized call of the <code>read()</code> method of the
     *  underlying <code>WaitFreeWriteQueue</code>. This call blocks on
     *  queue empty and will wait until there is an element in the queue
     *  to return.
     *
     *  @return The <code>java.lang.Object</code> read.
     */
    public synchronized Object blockingRead() {
	return writeQueue.blockingRead();
    }

    /** A synchronized call of the <code>write()</code> method of the
     *  underlying <code>WaitFreeReadQueue</code>. This call blocks on
     *  queue full and waits until there is space in <code>this</code>.
     *
     *  @param object The <code>java.lang.Object</code> to place in
     *                <code>this</code>.
     *  @return True, if the write succeeded. False, if not.
     *  @throws MemoryScopeException If the write causes an access or
     *                               assignment violation.
     */
    public synchronized boolean blockingWrite(Object object)
	throws MemoryScopeException {
	return readQueue.blockingWrite(object);
    }

    /** If <code>this</code> is full then this call overwrites the
     *  last object written to <code>this</code> with the given
     *  object. If this is not full this call is equivalent to the
     *  <code>nonBlockingWrite()</code> call.
     *
     *  @param object The <code>java.lang.Object which will overwrite
     *                the last object if <code>this</code> is full.
     *                Otherwise <code>object</code> will be placed in
     *                <code>this</code.
     *  @return True, if an element was overwritten. False, if there
     *          is an empty element into which the write occured.
     */
    // TODO: Figure out where to force.
    public boolean force(Object object) {
	if (!isFull()) {
	    boolean b = false;
	    try {
		b = nonBlockingWrite(object);
	    } catch (Exception e) {}
	    return b;
	}
	else {
	    writeQueue.force(object);
	    return true;
	}
    }

    /** An unsynchronized call of the <code>read()</code> method of the
     *  underlying <code>WaitFreeReadQueue</code>.
     *
     *  @return A <code>java.lang.Object</code> object read from
     *          <code>this</code>. If there are no elements in
     *          <code>this</code> then null is returned.
     */
    public Object nonBlockingRead() {
	return readQueue.nonBlockingRead();
    }

    /** An unsynchronized call of the <code>write()</code> method of the
     *  underlying <code>WaitFreeWriteQueue</code>. This call does not
     *  block on queue full.
     *
     *  @param object The <code>java.lang.Object</code> to attempt to
     *                place in <code>this</code>.
     *  @return True, if the object is now in <code>this</code>, otherwise
     *          returns false.
     *  @throws MemoryScopeException If the write causes an access or
     *                               assignment violation.
     */
    public boolean nonBlockingWrite(Object object)
	throws MemoryScopeException {
	return writeQueue.nonBlockingWrite(object);
    }
}
