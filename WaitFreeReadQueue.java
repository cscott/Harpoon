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
    protected boolean empty = true;
    protected boolean full = false;
    protected int size = 0;

    public WaitFreeReadQueue(Thread writer, Thread reader,
			     int maximum, MemoryArea memory)
	throws IllegalArgumentException, InstantiationException,
	       ClassNotFoundException, IllegalAccessException {
	// TODO
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
	empty = true;
	full = false;
	// TODO
    }

    public boolean isEmpty() {
	return empty;
    }

    public boolean isFull() {
	return full;
    }

    public Object read() {
	// TODO

	return null;
    }

    public int size() {
	return size;
    }

    public void waitForData() {
	// TODO
    }

    public boolean write(Object object) throws MemoryScopeException {
	// TODO 

	return false;
    }
}
