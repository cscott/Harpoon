package javax.realtime;

public class WaitFreeReadQueue {
    /** The wiat-free queue class facilitate communication and 
     *  synchronization between instances of <code>RealtimeThread</code>
     *  and <code>Thread</code>. The problem is that synchronized access
     *  objects shared between real-time threads and threads might cause
     *  the real-time threads to incur delays due to execution of the
     *  garbage collector.
     */

    protected boolean notify;
    protected Object[] readQueue = null;
    protected int queueSize;
    protected int currentIndex = 0;

    public WaitFreeReadQueue(Thread writer, Thread reader,
			     int maximum, MemoryArea memory)
	throws IllegalArgumentException, InstantiationException,
	       ClassNotFoundException, IllegalAccessException {
	// TODO

	queueSize = maximum;
    }

    public WaitFreeReadQueue(Thread writer, Thread reader,
			     int maximum, MemoryArea memory,
			     boolean notify)
	throws IllegalArgumentException, InstantiationException,
	       ClassNotFoundException, IllegalAccessException {
	this(writer, reader, maximum, memory);
	this.notify = notify;
    }

    public void clear() {
	currentIndex = 0;
    }

    public boolean isEmpty() {
	return (currentIndex == 0);
    }

    public boolean isFull() {
	return (currentIndex == queueSize - 1);
    }

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

    public int size() {
	return currentIndex;
    }

    public void waitForData() {
	// TODO
    }

    public synchronized boolean write(Object object) throws MemoryScopeException {
	while (isFull())
	    try {
		Thread.sleep(100);
	    } catch (Exception e) {};

	readQueue[++currentIndex] = object;
	return true;
    }
}
