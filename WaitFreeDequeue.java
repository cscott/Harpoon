package javax.realtime;

/** The wait-free classes facilitate communication and synchronization
 *  between instances of <code>RealtimeThread</code> and <code>Thread</code>.
 */
public class WaitFreeDequeue {

    protected Object[] queue = null;
    protected int queueSize;
    protected int currentIndex = 0;
    
    protected Thread writerThread = null, readerThread = null;
    protected MemoryArea memArea;

    /** A queue with unsynchronized and nonblocking <code>read()</code>
     *  and <code>write()</code> methods and synchronized and blocking
     *  <code>read()</code> and <code>write()</code> methods.
     */
    public WaitFreeDequeue(Thread writer, Thread reader,
			   int maximum, MemoryArea area)
	throws IllegalArgumentException, IllegalAccessException,
	       ClassNotFoundException, InstantiationException {
	writerThread = writer;
	readerThread = reader;
	queueSize = maximum;
	memArea = area;

	// TODO (?)
    }

    /** Used to check if the queue is empty. */
    private boolean isEmpty() {
	return (currentIndex == 0);
    }

    /** Used to check if the queue is full. */
    private boolean isFull() {
	return (currentIndex == queueSize - 1);
    }

    /** A synchronized call of the <code>read()</code> method of the
     *  underlying <code>WaitFreeWriteQueue</code>. This call blocks on
     *  queue empty and will wait until there is an element in the queue.
     */
    public synchronized Object blockingRead() {
	while (isEmpty())
	    try {
		Thread.sleep(100);
	    } catch (Exception e) {};
	
	Object temp = queue[0];
	for (int i = 0; i < currentIndex; i++)
	    queue[i] = queue[i+1];
	currentIndex--;
	return temp;
    }

    /** A synchronized call of the <code>write()</code> method of the
     *  underlying <code>WaitFreeReadQueue</code>. This call blocks on
     *  queue full and waits until there is space in <code>this</code>.
     */
    public synchronized boolean blockingWrite(Object object)
	throws MemoryScopeException {
	while (isFull())
	    try {
		Thread.sleep(100);
	    } catch (Exception e) {};

	queue[++currentIndex] = object;
	return true;
    }

    /** If this is full then this call overwrites the last object
     *  written to <code>this</code> with the given object. If this
     *  is not full this call is equivalent to the
     *  <code>nonBlockingWrite()</code> call.
     */
    public boolean force(Object object) {
	if (!isFull()) {
	    boolean b = false;
	    try {
		b = nonBlockingWrite(object);
	    } catch (Exception e) {}
	    return b;
	}
	else {
	    queue[currentIndex] = object;
	    return true;
	}
    }

    /** An unsynchronized call of the <code>read()</code> method of the
     *  underlying <code>WaitFreeReadQueue</code>.
     */
    public Object nonBlockingRead() {
	if (isEmpty()) return null;
	else {
	    Object temp = queue[0];
	    for (int i = 0; i < currentIndex; i++)
		queue[i] = queue[i+1];
	    currentIndex--;
	    return temp;
	}
    }

    /** An unsynchronized call of the <code>write()</code> method of the
     *  underlying <code>WaitFreeWriteQueue</code>. This call does not
     *  block on queue full.
     */
    public boolean nonBlockingWrite(Object object)
	throws MemoryScopeException {
	if (isFull()) return false;
	else {
	    queue[++currentIndex] = object;
	    return true;
	}
    }
}
