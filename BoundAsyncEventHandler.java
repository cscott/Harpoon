package javax.realtime;

public abstract class BoundAsyncEventHandler extends AsyncEventHandler {
    /** An asynchronous event handler that is permanently bound to a thread. */

    protected boolean nonHeap = false;
    protected Runnable logic = null;

    protected SchedulingParameters scheduling;
    protected ReleaseParameters release;
    protected MemoryParameters memory;
    protected MemoryArea area;
    protected ProcessingGroupParameters group;
    
    public BoundAsyncEventHandler() {
	// TODO
    }

    public BoundAsyncEventHandler(SchedulingParameters scheduling,
				  ReleaseParameters release,
				  MemoryParameters memory,
				  MemoryArea area,
				  ProcessingGroupParameters group,
				  boolean nonHeap, Runnable logic) {
	this();
	this.release = release;
	this.memParams = memory;
	this.memArea = area;
	this.group = group;
	this.nonHeap = nonHeap;
	this.logic = logic;
    }
}
