package javax.realtime;

/** An asynchronous event handler encapsulates code that gets run
 *  at some time after an <code>AsyncEvent</code> occurs.
 *  <p>
 *  It is essentially a <code>java.lang.Runnable</code> with a set
 *  of parameter objects, making it very much like
 *  <code>RealtimeThread</code>. The expectation is that there may
 *  be thousands of events, with corresponding handlers, averaging
 *  about one handler per event. The number of unblocked
 *  (i.e., scheduled) handlers is expected to be relatively small.
 *  <p>
 *  It is guaranteed that multiple firings of an event handler will
 *  be serialized. It is also guaranteed that (unless the handler
 *  explicitly chooses otherwise) for each firing of the handler,
 *  there will be one execution of the <code>handleAsyncEvent</code>
 *  method.
 *  <p>
 *  For instances of <code>AsyncEventHandler</code> with a release
 *  parameter of type <code>SporadicParameters</code> have a list of
 *  release times which correspond to execution times of
 *  <code>AsyncEvent.fire()</code>. The minimum interarrival time
 *  specified in <code>SporadicParameters</code> is enforced as
 *  defined there. Unless the handler explicitly chooses otherwise
 *  there will be one execution of the code in <code>handleAsyncEvent</code>
 *  for each entry in the list. The i-th execution of
 *  <code>handleAsyncEvent</code> will be realeased for scheduling
 *  at the time of the i-th entry in the list.
 *  <p>
 *  The is no restriction on what handlers may do. They may run for
 *  a long or short time, and they may block. (Note: blocked handlers
 *  may hold system resources).
 *  <p>
 *  Normally, handlers are bound to an execution context dynamically,
 *  when their <code>AsyncEvent</code> occurs. This can introduce a
 *  (small) time penalty. For critical handlers that can not afford
 *  the expense, and where this penalty is a problem, use a
 *  <code>BoundAsyncEventHandler</code>.
 *  <p>
 *  The semantics for memory areas that were defined for realtime
 *  threads apply in the same way to instances of <code>AsyncEventHandler</code>.
 *  They may inherit a scope stack when they are created, and the single
 *  parent rule applies to the use of memory scopes for instances of
 *  <code>AsyncEventHandler</code> just as it does in realtime threads.
 */
public class AsyncEventHandler implements Schedulable {
    
    protected int fireCount = 0;
    protected boolean nonheap = false;
    protected Runnable logic;

    protected Scheduler currentScheduler;

    protected SchedulingParameters scheduling;
    protected ReleaseParameters release;
    protected MemoryParameters memParams;
    protected MemoryArea memArea;
    protected ProcessingGroupParameters group;
    private static long UID = 0;
    private long myUID = ++UID;

    /** Create a handler whose <code>SchedulingParameters</code> are
     *  inherited from the current thread and does not have either
     *  <code>ReleaseParameters</code> or <code>MemoryParameters</code>.
     */
    public AsyncEventHandler() {
	if (Thread.currentThread() instanceof RealtimeThread)
	    scheduling = RealtimeThread.currentRealtimeThread().getSchedulingParameters();
	else scheduling = null;
	release = null;
	memParams = null;
    }

    /** Create a handler whose parameters are inherited from the current
     *  thread, if it is a <code>RealtimeThread</code>, or null otherwise.
     */
    public AsyncEventHandler(boolean nonheap) {
	this();
	this.nonheap = nonheap;
    }

    /** Create a handler whose <code>SchedulingParameters</code> are inherited
     *  from the current thread and does not have either
     *  <code>ReleaseParameters</code> or <code>MemoryParameters</code>.
     */
    public AsyncEventHandler(Runnable logic) {
	this();
	this.logic = logic;
    }

    /** Create a handler whose parameters are inherited from the current thread,
     *  if it is a <code>RealtimeThread</code>, or null otherwise.
     */
    public AsyncEventHandler(Runnable logic, boolean nonheap) {
	this(nonheap);
	this.logic = logic;
    }

    /** Create a handler with the specified parameters. */
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
    }

    /** Create a handler with the specified parameters. */
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
    }

    /** Create a handler with the specified parameters. */
    public AsyncEventHandler(SchedulingParameters scheduling,
			     ReleaseParameters release,
			     MemoryParameters memory,
			     MemoryArea area,
			     ProcessingGroupParameters group,
			     boolean nonheap, Runnable logic) {
	this(scheduling, release, memory, area, group, logic);
	this.nonheap = nonheap;
	
    }

    /** Add to the feasibility of the associated scheduler if the
     *  resulting feasibility is schedulable. If successful return
     *  true, if not return false. If there is no assigned
     *  scheduler, false is returned.
     */
    public boolean addIfFeasible() {
	if ((currentScheduler == null) ||
	    (!currentScheduler.isFeasible(this, getReleaseParameters()))) return false;
	else return addToFeasibility();
    }

    /** Inform the scheduler and cooperating facilities that the
     *  resource demands (as expressed in the associated instances of
     *  <code>SchedulingParameters, ReleaseParameters, MemoryParameters</code>
     *  and <code>ProcessingGroupParameteres</code>) of this instance of
     *  <code>Schedulable</code> will be considered in the feasibility
     *  analysis of the associated <code>Scheduler</code> until
     *  further notice. Whether the resulting system is feasible or
     *  not, the addition is completed.
     */
    public boolean addToFeasibility() {
	if (currentScheduler != null) {
	    currentScheduler.addToFeasibility(this);
	    return currentScheduler.isFeasible();
	}
	else return false;
    }

    /** Atomically set to zero the number of pending executions of
     *  this handler and returns the value from before it was cleared.
     *  This is used in handlers that can handle multiple firings and
     *  that want to collapse them together. The general form for
     *  using this is:
     *  <code>
     *  public void handleAsyncEvent() {
     *      int fireCount = getAndClearPendingFireCount();
     *      <handle the events>
     *  }
     *  </code>
     */
    protected final int getAndClearPendingFireCount() {
	int x = fireCount;
	fireCount = 0;
	return x;
    }

    /** Atomically decrements the number of pending executions of
     *  this handler (if it was non-zero) and returns the value
     *  from before the decrement. This can be used in the
     *  <code>handleAsyncEvent()</code> method in this form to
     *  handle multiple firings:
     *  <p>
     *  <code>
     *  public void handleAsyncEvent() {
     *      <setup>
     *      do {
     *          <handle the events>
     *      } while (getAndDecrementPendingFireCounts() > 0);
     *  }
     *</code>
     */
    protected int getAndDecrementPendingFireCount() {
	if (fireCount > 0) return fireCount--;
	else return 0;
    }

    /** Atomically increments the number of pending executions of
     *  this handler and returns the value form before the increment.
     *  The <code>handleAsyncEvent()</code> method does not need to
     *  do this, since the surrounding framework guarantees that the
     *  handler will be re-executed the appropriate number of times.
     *  It is only of value when there is common setup code that is
     *  expensive.
     */
    protected int getAndIncrementPendingFireCount() {
	return fireCount++;
    }

    /** Get the current memory area. */
    public MemoryArea getMemoryArea() {
	return memArea;
    }

    /** Get the memory parameters associated with this handler. */
    public MemoryParameters getMemoryParameters() {
	return memParams;
    }

    /** Return the number of pending executions of this handler. */
    protected final int getPendingFireCount() {
	return fireCount;
    }

    /** Returns a reference to the <code>ProcessingGroupParameters</code> object. */
    public ProcessingGroupParameters getProcessingGroupParameters() {
	return group;
    }

    /** Get the release parameters associated with this handler. */
    public ReleaseParameters getReleaseParameters() {
	return release;
    }

    /** Return the <code>Scheduler</code> for this handler. */
    public Scheduler getScheduler() {
	return currentScheduler;
    }

    /** Returns a reference to the scheduling parameters object. */
    public SchedulingParameters getSchedulingParameters() {
	return scheduling;
    }

    /** If this handler was constructed using a separate <code>Runnable</code>
     *  logic object, then that <code>Runnable</code> object's <code>run()</code>
     *  method is called. This method will be invoked repeatedly while
     *  <code>fireCount</code> is greater than zero.
     */
    public void handleAsyncEvent() {
	if (logic != null) logic.run();
    }

    /** Inform the scheduler and cooperating facilities that the resource
     *  demands, as expressed in the associated instances of
     *  <code>SchedulingParameters, ReleaseParameters, MemoryParameters,</code>
     *  and <code>ProcessingGroupParameters</code>, of this instance of
     *  <code>Schedulable</code> should no longer be considered in the
     *  feasibility analysis of the associated <code>Scheduler</code>.
     *  Whether the resulting system is feasible or not, the subtraction
     *  is completed.
     */
    public void removeFromFeasibility() {
	if (currentScheduler != null)
	    currentScheduler.removeFromFeasibility(this);
    }

    /** Used by the asynchronous event mechanism, see <code>AsyncEvent</code>.
     *  This method invokes <code>handleSdyncEvent()</code> repeatedly while
     *  fire count is greater than zero. Applications cannot override this
     *  method and should thus override <code>handleAsyncEvent()</code> in
     *  subclasses with the logic of the handler.
     */
    public final void run() {
	while (getAndDecrementPendingFireCount() > 0)
	    handleAsyncEvent();
    }   

    /** Returns true if, after considering the values of the parameters, the
     *  task set would still be feasible. In this case the values of the
     *  parameters are changed. Returns false, if after considering the
     *  values of the parameters, the task set would not be feasible. In
     *  this case the values of the parameters are not changed.
     */
    public boolean setIfFeasible(ReleaseParameters release,
				 MemoryParameters memory) {
	return setIfFeasible(release, memory, getProcessingGroupParameters());
    }

    /** Returns true if, after considering the values of the parameters, the
     *  task set would still be feasible. In this case the values of the
     *  parameters are changed. Returns false, if after considering the
     *  values of the parameters, the task set would not be feasible. In
     *  this case the values of the parameters are not changed.
     */
    public boolean setIfFeasible(ReleaseParameters release,
				 MemoryParameters memory,
				 ProcessingGroupParameters group) {
	if (currentScheduler == null) return false;
	else return currentScheduler.setIfFeasible(this, release, memory, group);
    }

    /** Returns true if, after considering the values of the parameters, the
     *  task set would still be feasible. In this case the values of the
     *  parameters are changed. Returns false, if after considering the
     *  values of the parameters, the task set would not be feasible. In
     *  this case the values of the parameters are not changed.
     */
    public boolean setIfFeasible(ReleaseParameters release,
				 ProcessingGroupParameters group) {
	return setIfFeasible(release, getMemoryParameters(), group);
    }

    /** Set the memory parameters associated with this handler. When it is
     *  next fired, the executing thread will use these parameters to control
     *  memory allocation. Does not affect the current invocation of the
     *  <code>run()</code> of this handler.
     */
    public void setMemoryParameters(MemoryParameters memory) {
	memParams = memory;
    }

    /** Changes the <code>MemoryParameters</code> only if the task set is still
     *  feasible after that.
     */
    public boolean setMemoryParametersIfFeasible(MemoryParameters memory) {
	return setIfFeasible(getReleaseParameters(), memory,
			     getProcessingGroupParameters());
    }

    /** Sets the reference to the <code>ProcessingGroupParameters</code> object. */
    public void setProcessingGroupParameters(ProcessingGroupParameters group) {
	this.group = group;
    }

    /** Changes the <code>ProcessingGroupParameters</code> only if the task set is
     *  still feasible after that.
     */
    public boolean setProcessingGroupParametersIfFeasible(ProcessingGroupParameters group) {
	return setIfFeasible(getReleaseParameters(),
			     getMemoryParameters(), group);
    }
    /** Set the release parameters associated with this handler. When it is
     *  next fired, the executing thread will use these parameters to control
     *  scheduling. If the scheduling parameters of a handler is set to null,
     *  the handler will be executed immediately when it is fired, in the
     *  thread of the firer. Does not affect the current invocation of the
     *  <code>run()</code> of this handler.
     *  <p>
     *  Since this affects the constraints expressed in the realease parameters
     *  of the existing schedulable objects, this may change the feasibility
     *  of the current schedule.
     */
    public void setReleaseParameters(ReleaseParameters release) {
	this.release = release;
    }

    /** Changes the <code>ReleaseParameters</code> only if the task set is still
     *  feasible after that.
     */
    public boolean setReleaseParametersIfFeasible(ReleaseParameters release) {
	return setIfFeasible(release, getMemoryParameters(),
			     getProcessingGroupParameters());
    }

    /** Set the scheduler for this handler. A reference to the scheduler which
     *  will manage the execution of this thread.
     */
    public void setScheduler(Scheduler scheduler)
	throws IllegalThreadStateException {
	setScheduler(scheduler, getSchedulingParameters(), getReleaseParameters(),
		     getMemoryParameters(), getProcessingGroupParameters());
    }

    /** Set the scheduler for this handler. A reference to the scheduler which
     *  will manage the execution of this thread.
     */
    public void setScheduler(Scheduler scheduler,
			     SchedulingParameters scheduling,
			     ReleaseParameters release,
			     MemoryParameters memoryParameters,
			     ProcessingGroupParameters processingGroup)
	throws IllegalThreadStateException {
	currentScheduler = scheduler;
	this.scheduling = scheduling;
	this.release = release;
	this.memParams = memoryParameters;
	this.group = processingGroup;
    }

    /** Set the scheduling parameters associated with this handler. When it is
     *  next fired, the executing thread will use these parameters to control
     *  scheduling. Does not affect the current invocation of the
     *  <code>run()</code> of this handler.
     */
    public void setSchedulingParameters(SchedulingParameters scheduling) {
	this.scheduling = scheduling;
    }

    /** Set the <code>SchedulingParameters</code> of this schedulable object
     *  only if the resulting task set is feasible.
     */
    public boolean setSchedulingParametersIfFeasible(SchedulingParameters scheduling) {
	// How do scheduling parameters affect the feasibility of the task set?
	this.scheduling = scheduling;
	return true;
// 	if (currentScheduler == null) return false;
// 	SchedulingParameters old_scheduling = this.scheduling;
// 	setSchedulingParameters(scheduling);
// 	if (currentScheduler.isFeasible()) return true;
// 	else {
// 	    setSchedulingParameters(old_scheduling);
// 	    return false;
// 	}
    }

    public long getUID() {
	return myUID;
    }
}
