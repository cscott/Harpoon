package javax.realtime;

public class AsyncEventHandler implements Schedulable {
    /** Encapsulates code that gets run at some time after an
     *  <code>AsyncEvent</code> occurs.
     */
    
    protected int fireCount = 0;
    protected boolean nonheap = false;
    protected Runnable logic;

    protected Scheduler defaultScheduler;

    protected SchedulingParameters scheduling;
    protected ReleaseParameters release;
    protected MemoryParameters memParams;
    protected MemoryArea memArea;
    protected ProcessingGroupParameters group;

    public AsyncEventHandler() {
	// TODO
    }

    public AsyncEventHandler(boolean nonheap) {
	this();
	this.nonheap = nonheap;
    }
    
    public AsyncEventHandler(boolean nonheap, Runnable logic) {
	this(nonheap);
	this.logic = logic;
    }
    
    public AsyncEventHandler(Runnable logic) {
	this();
	this.logic = logic;
    }

    public AsyncEventHandler(SchedulingParameters scheduling,
			     ReleaseParameters release,
			     MemoryParameters memory,
			     MemoryArea area,
			     ProcessingGroupParameters group,
			     boolean nonheap) {
	this(nonheap);
	this.scheduling = scheduling;
	this.release = release;
	this.memParams = memory;
	this.memArea = area;
	this.group = group;
	// TODO (?)
    }

    public AsyncEventHandler(SchedulingParameters scheduling,
			     ReleaseParameters release,
			     MemoryParameters memory,
			     MemoryArea area,
			     ProcessingGroupParameters group,
			     Runnable logic) {
	this(logic);
	this.scheduling = scheduling;
	this.release = release;
	this.memParams = memory;
	this.memArea = area;
	this.group = group;
	// TODO (?)
    }

    // NOT IN SPECS
    //    public AsyncEventHandler(SchedulingParameters sp, ReleaseParameters rp) {
    //	super(sp, rp);
    //    }
    
    public boolean addIfFeasible() {
	// TODO

	return false;
    }

    public boolean addToFeasibility() {
	// TODO

	return false;
    }
    
    protected final int getAndClearPendingFireCount() {
	int x = fireCount;
	fireCount = 0;
	return x;
    }
    
    protected int getAndDecrementPendingFireCount() {
	return fireCount--;
    }
    
    protected int getAndIncrementPendingFireCount() {
	return fireCount++;
    }
    
    public MemoryArea getMemoryArea() {
	return memArea;
    }

    public MemoryParameters getMemoryParameters() {
	return memParams;
    }

    protected final int getPendingFireCount() {
	return fireCount;
    }

    public ProcessingGroupParameters getProcessingGroupParameters() {
	return group;
    }

    public ReleaseParameters getReleaseParameters() {
	return release;
    }

    public Scheduler getScheduler() {
	return defaultScheduler;
    }

    public SchedulingParameters getSchedulingParameters() {
	return scheduling;
    }

    public void handleAsyncEvent() {
	// TODO
    }

    public void removeFromFeasibility() {
	// TODO
    }
    
    public final void run() {
	while (fireCount > 0)
	    handleAsyncEvent();
    }   

    public boolean setIfFeasible(ReleaseParameters release,
				 MemoryParameters memory) {
	return setIfFeasible(release, memory,
			     getProcessingGroupParameters());
    }

    public boolean setIfFeasible(ReleaseParameters release,
				 MemoryParameters memory,
				 ProcessingGroupParameters group) {
	// TODO

	return false;
    }

    public boolean setIfFeasible(ReleaseParameters release,
				 ProcessingGroupParameters group) {
	return setIfFeasible(release, getMemoryParameters(), group);
    }

    public void setMemoryParameters(MemoryParameters memory) {
	memParams = memory;
    }

    public boolean setMemoryParametersIfFeasible(MemoryParameters memory) {
	return setIfFeasible(getReleaseParameters(), memory,
			     getProcessingGroupParameters());
    }

    public void setProcessingGroupParameters(ProcessingGroupParameters group) {
	this.group = group;
    }

    public boolean setProcessingGroupParametersIfFeasible(ProcessingGroupParameters group) {
	return setIfFeasible(getReleaseParameters(),
			     getMemoryParameters(), group);
    }

    public void setReleaseParameters(ReleaseParameters release) {
	this.release = release;
    }

    public boolean setReleaseParametersIfFeasible(ReleaseParameters release) {
	return setIfFeasible(release, getMemoryParameters(),
			     getProcessingGroupParameters());
    }

    public void setScheduler(Scheduler scheduler)
	throws IllegalThreadStateException {
	defaultScheduler = scheduler;
	// TODO (?)
    }

    public void setScheduler(Scheduler scheduler,
			     SchedulingParameters scheduling,
			     ReleaseParameters release,
			     MemoryParameters memoryParameters,
			     ProcessingGroupParameters rpocessingGroup)
	throws IllegalThreadStateException {
	// TODO
    }

    public void setSchedulingParameters(SchedulingParameters scheduling) {
	this.scheduling = scheduling;
    }

    public boolean setSchedulingParametersIfFeasible(SchedulingParameters scheduling) {
	// TODO

	return false;
    }
}
