package javax.realtime;

public abstract class BoundAsyncEventHandler extends AsyncEventHandler {
    /** An asynchronous event handler that is permanently bound to a thread. */

    public BoundAsyncEventHandler() {
	// TODO
    }

    public BoundAsyncEventHandler(SchedulingParameters scheduling,
				  ReleaseParameters release,
				  MemoryParameters memory,
				  MemoryArea area,
				  ProcessingGroupParameters group,
				  boolean nonHeap, Runnable logic) {
	// TODO
    }
}
