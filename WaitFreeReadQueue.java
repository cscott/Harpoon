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
     *  The memory areas of the given threads are found. If these memory
     *  areas are the same the queue is created in that memory area. If
     *  these memory areas are different the queue is created in the area
     *  accessible by the most restricted thread type.
     */
    public WaitFreeReadQueue(Thread writer, Thread reader,
			     int maximum, MemoryArea memory)
	throws IllegalArgumentException, InstantiationException,
	       ClassNotFoundException, IllegalAccessException {
	writerThread = writer;
	readerThread = reader;
	queueSize = maximum;
	memArea = memory;

	// TODO (?)
    }

    /** A queue with an unsynchronized and nonblocking <code>read()</code>
     *  method and a synchronized and blocking <code>write()</code> method.
     */
    public WaitFreeReadQueue(Thread writer, Thread reader,
			     int maximum, MemoryArea memory,
			     boolean notify)
	throws IllegalArgumentException, InstantiationException,
	       ClassNotFoundException, IllegalAccessException {
	this(writer, reader, maximum, memory);
	this.notify = notify;
    }

    /** Set <code>this</code> to empty. */
    public void clear() {
	currentIndex = 0;
    }

    /** Used to determine if <code>this</code> is empty. */
    public boolean isEmpty() {
	return (currentIndex == 0);
    }

    /** Used to determine if <code>this</code> is full. */
    public boolean isFull() {
	return (currentIndex == queueSize - 1);
    }

    /** Returns the next element in the queue unless the queue is empty.
     *  If the queue is empty null is returned.
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

    /** Used to determine the number of elements in <code>this</code>. */
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
