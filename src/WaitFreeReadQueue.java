// WaitFreeReadQueue.java, created by Dumitru Daniliuc, rewritten by Harvey Jones
// Copyright (C) 2003 Dumitru Daniliuc, Harvey Jones
// Licensed under the terms of the GNU GPL; see COPYING for details.
package javax.realtime;

/** The wait-free queue class facilitate communication and 
 *  synchronization between instances of <code>RealtimeThread</code>
 *  and <code>Thread</code>. The problem is that synchronized access
 *  objects shared between real-time threads and threads might cause
 *  the real-time threads to incur delays due to execution of the
 *  garbage collector.
 *  <p>
 *  The <code>read()</code> method of this class does not block on an
 *  imagined queue-empty condition variable. If the <code>read()</code>
 *  is called on an empty queue null is returned. If two real-time
 *  threads intend to read from this queue they must provide their
 *  own synchronization.
 *  <p>
 *  The <code>write()</code> method of this queue is synchronized and
 *  may be called by more than one writer and will block on queue empty.
 *  <p>
 *  This class is provided for compliance with the RTSJ, see
 *  <code>Queue</code>.
 */
public class WaitFreeReadQueue extends Queue{
    protected Thread writerThread = null, readerThread = null;
    protected MemoryArea memArea;
    /** A queue with an unsynchronizes and nonblocking <code>read()</code>
     *  method and a synchronized and blocking <code>write()</code> method.
     *
     *  @param writer An instance of <code>java.lang.Thread</code>.
     *  @param reader An instance of <code>java.lang.Thread</code>.
     *  @param maximum The maximum number of elements in the queue.
     *  @param memory The <code>MemoryArea</code> in which this object and
     *                internal elements are alocated.
     *  @throws java.lang.IllegalArgumentException
     */
    public WaitFreeReadQueue(Thread writer, Thread reader,
			     int maximum, MemoryArea memory)
	throws IllegalArgumentException {
	super(maximum, null, memory);
	writerThread = writer;
	readerThread = reader;
	memArea = memory;
	

    }

    /** A queue with an unsynchronizes and nonblocking <code>read()</code>
     *  method and a synchronized and blocking <code>write()</code> method.
     *
     *  @param writer An instance of <code>java.lang.Thread</code>.
     *  @param reader An instance of <code>java.lang.Thread</code>.
     *  @param maximum The maximum number of elements in the queue.
     *  @param memory The <code>MemoryArea</code> in which this object and
     *                internal elements are alocated.
     *  @param notify Whether or not the reader is notified when data is added. Not implemented.
     *  @throws java.lang.IllegalArgumentException
     */
    public WaitFreeReadQueue(Thread writer, Thread reader,
			     int maximum, MemoryArea memory,
			     boolean notify)
	throws IllegalArgumentException {
	    super(maximum, reader, memory);
    }
    public Object read(){
	return super.nonBlockingRead();
    }
    
    public boolean write(Object data) throws MemoryScopeException{
	return super.blockingWrite(data);
    }
}
