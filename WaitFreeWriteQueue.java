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
     */
    public WaitFreeWriteQueue(Thread writer, Thread reader,
			      int maximum, MemoryArea memory)
	throws IllegalArgumentException, InstantiationException,
	       ClassNotFoundException, IllegalAccessException {
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
     */
    public boolean force(Object object) throws MemoryScopeException {
	if (!isFull()) return write(object);
	else {
	    writeQueue[currentIndex] = object;
	    return true;
	}
    }

    /** Used to determine if <code>this</code> is empty. */
    public boolean isEmpty() {
	return (currentIndex == 0);
    }

    /** Used to determine if <code>this</code> is full. */
    public boolean isFull() {
	return (currentIndex == queueSize - 1);
    }

    /** A synchronized read on the queue. */
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

    /** Used to determine the number of elements in <code>this</code>. */
    public int size() {
	return currentIndex;
    }

    /** Try to insert an element into the queue. */
    public boolean write(Object object) throws MemoryScopeException {
	if (isFull()) return false;
	else {
	    writeQueue[++currentIndex] = object;
	    return true;
	}
    }
}
