// WaitFreeReadQueue.java, created by Dumitru Daniliuc
// Copyright (C) 2003 Dumitru Daniliuc
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
public class WaitFreeReadQueue {

    protected boolean notify;
    protected Object[] readQueue = null;
    protected int queueSize;
    protected int currentIndex = 0;

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
	writerThread = writer;
	readerThread = reader;
	queueSize = maximum;
	memArea = memory;

	// TODO (?)
    }

    /** A queue with an unsynchronizes and nonblocking <code>read()</code>
     *  method and a synchronized and blocking <code>write()</code> method.
     *
     *  @param writer An instance of <code>java.lang.Thread</code>.
     *  @param reader An instance of <code>java.lang.Thread</code>.
     *  @param maximum The maximum number of elements in the queue.
     *  @param memory The <code>MemoryArea</code> in which this object and
     *                internal elements are alocated.
     *  @param notify Whether or not the reader is notified when data is added.
     *  @throws java.lang.IllegalArgumentException
     */
    public WaitFreeReadQueue(Thread writer, Thread reader,
			     int maximum, MemoryArea memory,
			     boolean notify)
	throws IllegalArgumentException {
	this(writer, reader, maximum, memory);
	this.notify = notify;
    }

    /** Set <code>this</code> to empty. */
    public void clear() {
	currentIndex = 0;
    }

    /** Queries the system to determine if <code>this</code> is empty.
     *
     *  @return True, if <code>this</code> is empty. False, if
     *          <code>this</code> is not empty.
     */
    public boolean isEmpty() {
	return (currentIndex == 0);
    }

    /** Queries the system to determine if <code>this</code> is full.
     *
     *  @return True, if <code>this</code> is full. False, if
     *          <code>this</code> is not full.
     */
    public boolean isFull() {
	return (currentIndex == queueSize - 1);
    }

    /** Reads the next element in the queue unless the queue is empty.
     *  If the queue is empty null is returned.
     *
     *  @return The instance of <code>java.lang.Object</code> read.
     *          Null, if <code>this</code> was empty.
     */
    public Object read() {
	if (isEmpty()) return null;
	else {
	    Object temp = readQueue[0];
	    for (int i = 0; i < currentIndex; i++)
		readQueue[i] = readQueue[i+1];
	    currentIndex--;
	    return temp;
	}
    }

    /** Queries the system to determine the number of elements in
     *  <code>this</code>.
     *
     *  @return An integer which is the number of non-empty positions in
     *          <code>this</code>.
     */
    public int size() {
	return currentIndex;
    }

    /** If <code>this</code> is empty <code>waitForData()</code> waits on
     *  the event until the writer inserts data. Note that true priority
     *  inversion does not occur since the writer locks a different object
     *  and the notify is executed by the <code>AsyncEventHandler</code>
     *  which has <code>noHeap</code> characteristics.
     */
    public void waitForData() {
	// TODO
    }

    /** The synchronized and blocking write. This call blocks on queue full
     *  and will wait until there is space in the queue.
     *
     *  @param object The <code>java.lang.Object</code> that is placed in
     *                <code>this</code>.
     *  @return True, if the write occured. False, if it did not.
     *  @throws MemoryScopeException
     */
    public synchronized boolean write(Object object) throws MemoryScopeException {
	while (isFull())
	    try {
		Thread.sleep(100);
	    } catch (Exception e) {};

	readQueue[++currentIndex] = object;
	return true;
    }
}
