package javax.realtime;

/** A bound asynchronous event handler is an asynchronous event handler
 *  that is permanently bound to a thread. Bound asynchronous event
 *  handlers are meant for use in situations where the added timeliness
 *  is worth the overhead of binding the handler to a thread.
 */
public abstract class BoundAsyncEventHandler extends AsyncEventHandler {

    protected boolean nonHeap = false;
    protected Runnable logic = null;

    protected SchedulingParameters scheduling;
    protected ReleaseParameters release;
    protected MemoryParameters memory;
    protected ProcessingGroupParameters group;
    protected MemoryArea area;

    /** Create a handler whose parameters are inherited from the current
     *  thread, if it is a <code>RealtimeThread</code>, or null otherwise.
     */
    public BoundAsyncEventHandler() {
	if (Thread.currentThread() instanceof RealtimeThread) {
	    RealtimeThread t = RealtimeThread.currentRealtimeThread();
	    scheduling = t.getSchedulingParameters();
	    release = t.getReleaseParameters();
	    memory = t.getMemoryParameters();
	    group = t.getProcessingGroupParameters();
	    area = t.getMemoryArea();
	}
	else {
	    scheduling = null;
	    release = null;
	    memory = null;
	    group = null;
	    area = null;
	}
    }

    /** Create a handler with the specified parameters. */
    public BoundAsyncEventHandler(SchedulingParameters scheduling,
				  ReleaseParameters release,
				  MemoryParameters memory,
				  MemoryArea area,
				  ProcessingGroupParameters group,
				  boolean nonHeap, Runnable logic) {
	this.scheduling = scheduling;
	this.release = release;
	this.memParams = memory;
	this.group = group;
	this.memArea = area;
	this.nonHeap = nonHeap;
	this.logic = logic;
    }
}
