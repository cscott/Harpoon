package javax.realtime;

public class WaitFreeDequeue {
    /** The wait-free classes facilitate communication and synchronization
     *  between instances of <code>RealtimeThread</code> and
     *  <code>Thread</code>.
     */

    protected Object[] queue = null;
    protected int queueSize;
    protected int currentIndex = 0;
    
    public WaitFreeDequeue(Thread writer, Thread reader,
			   int maximum, MemoryArea area)
	throws IllegalArgumentException, IllegalAccessException,
	       ClassNotFoundException, InstantiationException {
	// TODO

	queueSize = maximum;
    }

    private boolean isEmpty() {
	return (currentIndex == 0);
    }

    private boolean isFull() {
	return (currentIndex == queueSize - 1);
    }

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

    public synchronized boolean blockingWrite(Object object)
	throws MemoryScopeException {
	while (isFull())
	    try {
		Thread.sleep(100);
	    } catch (Exception e) {};

	queue[++currentIndex] = object;
	return true;
    }

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

    public boolean nonBlockingWrite(Object object)
	throws MemoryScopeException {
	if (isFull()) return false;
	else {
	    queue[++currentIndex] = object;
	    return true;
	}
    }
}
