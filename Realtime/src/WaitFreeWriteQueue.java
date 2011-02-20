// WaitFreeWriteQueue.java, created by Dumitru Daniliuc, rewritten by Harvey Jones
// Copyright (C) 2003 Dumitru Daniliuc, Harvey Jones
// Licensed under the terms of the GNU GPL; see COPYING for details.
package javax.realtime;

/** The wait-free queue facilitate communication and synchronization
 *  between instances of <code>RealtimeThread</code> and
 *  <code>java.lang.Thread</code>. The problem is that synchronized
 *  access objects shared between real-time threads and threads
 *  might cause the real-time threads to incur delays due to
 *  execution of the garbage collector.
 *  <p>
 *  The <code>write()</code> method of this class does not block on
 *  an imagined queue-full condition variable. If the <code>write()</code>
 *  method is called on a full queue false is returned. If two real-time
 *  threads intend to read from this queue they must provide their
 *  own synchronization.
 *  <p>
 *  The <code>read()</code> method of this queue is synchronized and
 *  may be called by more than one writer and will block on queue empty.
 *  <p>
 *  This class is provided for compliance with the RTSJ, see
 *  <code>Queue</code> for details.
 */
public class WaitFreeWriteQueue extends Queue{
    protected Thread writerThread = null, readerThread = null;
    protected MemoryArea memArea;

    /** A queue with an unsynchronized and nonblocking <code>write()</code>
     *  method and a synchronized and blocking <code>read()</code> method.
     *
     *  @param writer An instance of <code>java.lang.Thread</code>.
     *  @param reader An instance of <code>java.lang.Thread</code>.
     *  @param maximum The maximum number of elements in the queue.
     *  @param memory The <code>MemoryArea</code> in which this object and
     *                internal elements are allocated.
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
    public WaitFreeWriteQueue(Thread writer, Thread reader,
			      int maximum, MemoryArea memory)
	throws IllegalArgumentException {
	super(maximum, reader, memory);
	writerThread = writer;
	readerThread = reader;

    }

    public Object read() {
	return super.blockingRead();
    }
    
    public boolean write(Object data)
	throws MemoryScopeException{
	return super.nonBlockingWrite(data);
    }
}
