package javax.realtime;

public class WaitFreeWriteQueue {
    /** The wait-free queue facilitate communication and synchronization
     *  between instances of <code>RealtimeThread</code> and
     *  <code>java.lang.Thread</code>. The problem is that synchronized
     *  access objects shared between real-time threads and threads
     *  might cause the real-time threads to incur delays due to
     *  execution of the garbage collector.
     */

    protected Object[] writeQueue = null;
    protected int queueSize;
    protected int currentIndex = 0;

    public WaitFreeWriteQueue(Thread writer, Thread reader,
			      int maximum, MemoryArea memory)
	throws IllegalArgumentException, InstantiationException,
	       ClassNotFoundException, IllegalAccessException {
	// TODO

	queueSize = maximum;
    }

    public void clear() {
	currentIndex = 0;
    }

    public boolean force(Object object) throws MemoryScopeException {
	if (!isFull()) return write(object);
	else {
	    writeQueue[currentIndex] = object;
	    return true;
	}
    }

    public boolean isEmpty() {
	return (currentIndex == 0);
    }

    public boolean isFull() {
	return (currentIndex == queueSize - 1);
    }

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

    public int size() {
	return currentIndex;
    }

    public boolean write(Object object) throws MemoryScopeException {
	if (isFull()) return false;
	else {
	    writeQueue[++currentIndex] = object;
	    return true;
	}
    }
}
