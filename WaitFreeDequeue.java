package javax.realtime;

public class WaitFreeDequeue {
    /** The wait-free classes facilitate communication and synchronization
     *  between instances of <code>RealtimeThread</code> and
     *  <code>Thread</code>.
     */

    public WaitFreeDequeue(Thread writer, Thread reader,
			   int maximum, MemoryArea area)
	throws IllegalArgumentException, IllegalAccessException,
	       ClassNotFoundException, InstantiationException {
	// TODO
    }

    public Object blockingRead() {
	// TODO

	return null;
    }

    public boolean blockingWrite(Object object)
	throws MemoryScopeException {
	// TODO

	return false;
    }

    public boolean force(Object object) {
	// TODO

	return false;
    }

    public Object nonBlockingRead() {
	// TODO

	return null;
    }

    public boolean nonBlockingWrite(Object object)
	throws MemoryScopeException {
	// TODO

	return false;
    }
}
