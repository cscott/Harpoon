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
 */
public class WaitFreeWriteQueue {

    protected Object[] writeQueue = null;
    protected int queueSize;
    protected int currentIndex = 0;

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
	writerThread = writer;
	readerThread = reader;
	queueSize = maximum;
	memArea = memory;

	// TODO (?)
    }

    /** Set <code>this</code> to empty. */
    public void clear() {
	currentIndex = 0;
    }

    /** Force this <code>java.lang.Object</code> to replace the last one.
     *  If the reader should happen to have just removed the other
     *  <code>java.lang.Object</code> just as we were updating it, we will
     *  return false. False may mean that it just saw that we put in there.
     *  Either way, the best thing to do is to just write again -- which
     *  will succeed, and check on the readers side for consecutive
     *  identical read values.
     *
     *  @return True, if an element was overwritten. False, if there is an
     *          empty element into which the write occured.
     *  @throws MemoryScopeException
     */
    public boolean force(Object object) throws MemoryScopeException {
	if (!isFull()) return write(object);
	else {
	    writeQueue[currentIndex] = object;
	    return true;
	}
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

    /** A synchronized read on the queue.
     *
     *  @return The <code>java.lang.Object</code> read or null if
     *          <code>this</code> is empty.
     */
    public synchronized Object read() {
	while (isEmpty())
	    try {
		Thread.sleep(100);
	    } catch (Exception e) {};

	Object temp = writeQueue[0];
	for (int i = 0; i < currentIndex; i++)
	    writeQueue[i] = writeQueue[i+1];
	currentIndex--;
	return temp;
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

    /** Attempt to insert an element into the queue.
     *
     *  @param object The <code>java.lang.Object</code> to insert.
     *  @return True, if the write succeeded. False, if not.
     *  @throws MemoryScopeException
     */
    public boolean write(Object object) throws MemoryScopeException {
	if (isFull()) return false;
	else {
	    writeQueue[++currentIndex] = object;
	    return true;
	}
    }
}
