// AsyncEventHandler.java, created by Dumitru Daniliuc
// Copyright (C) 2003 Dumitru Daniliuc
// Licensed under the terms of the GNU GPL; see COPYING for details.
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

    /** Create an instance of <code>AsyncEventHandler</code> whose
     *  <code>SchedulingParameters</code> are inherited from the current
     *  thread and does not have either <code>ReleaseParameters</code> or
     *  <code>MemoryParameters</code>.
     */
    public AsyncEventHandler() {
	if (Thread.currentThread() instanceof RealtimeThread)
	    scheduling = RealtimeThread.currentRealtimeThread().getSchedulingParameters();
	else scheduling = null;
	release = null;
	memParams = null;
    }

    /** Create an instance of <code>AsyncEventHandler</code> whose
     *  parameters are inherited from the current thread, if the current
     *  thread is a <code>RealtimeThread</code>, or null, otherwise.
     *
     *  @param nonheap A flag meaning, when true, that this will have
     *                 characteristics identical to a
     *                 <code>NoHeapRealtimeThread</code>. A false value
     *                 means this will have characteristics identical to a
     *                 <code>RealtimeThread</code>. If true and the current
     *                 thread is <i>not</i> a <code>NoHeapRealtimeThread</code>
     *                 of a <code>RealtimeThread</code> executing within a
     *                 <code>ScopedMemory</code> or <code>ImmortalMemory</code>
     *                 scope then an <code>IllegalArgumentException<code> is thrown.
     *  @throws java.lang.IllegalArgumentException If the initial memory area is
     *                                             in heap memory, and the
     *                                             <code>nonheap</code> parameter
     *                                             is true.
     */
    public AsyncEventHandler(boolean nonheap) {
	this();
	this.nonheap = nonheap;
    }

    /** Create an instance of <code>AsyncEventHandler</code> whose
     *  <code>SchedulingParameters</code> are inherited from the current
     *  thread and does not have either <code>ReleaseParameters</code> or
     *  <code>MemoryParameters</code>.
     *
     *  @param logic The <code>java.lang.Runnable</code> object whose
     *               <code>run()</code> method is executed by
     *               <code>handleAsyncEvent()</code>.
     */
    public AsyncEventHandler(Runnable logic) {
	this();
	this.logic = logic;
    }

    /** Create an instance of <code>AsyncEventHandler</code> whose
     *  parameters are inherited from the current thread, if the current
     *  thread is a <code>RealtimeThread</code>, or null, otherwise.
     *
     *  @param lobic The <code>java.lang.Runnable</code> object whose
     *               <code>run()</code> method is executed by
     *               <code>handleAsyncEvent()</code>.
     *  @param nonheap A flag meaning, when true, that this will have
     *                 characteristics identical to a
     *                 <code>NoHeapRealtimeThread</code>. A false value
     *                 means this will have characteristics identical to a
     *                 <code>RealtimeThread</code>. If true and the current
     *                 thread is <i>not</i> a <code>NoHeapRealtimeThread</code>
     *                 of a <code>RealtimeThread</code> executing within a
     *                 <code>ScopedMemory</code> or <code>ImmortalMemory</code>
     *                 scope then an <code>IllegalArgumentException<code> is thrown.
     *  @throws java.lang.IllegalArgumentException If the initial memory area is
     *                                             in heap memory, and the
     *                                             <code>nonheap</code> parameter
     *                                             is true.
     */
    public AsyncEventHandler(Runnable logic, boolean nonheap) {
	this(nonheap);
	this.logic = logic;
    }

    /** Create an instance of <code>AsyncEventHandler</code> whose
     *  parameters are inherited from the current thread, if the current
     *  thread is a <code>RealtimeThread</code>, or null, otherwise.
     *
     *  @param scheduling A <code>SchedulingParameters</code> object which
     *                    will be associated with the constructed instance.
     *                    If null, <code>this</code> will be assigned the
     *                    reference to the <code>SchedulingParameters</code>
     *                    of the current thread.
     *  @param release A <code>ReleaseParameters</code> obejct which will be
     *                 associated with the constructed isntance. If null,
     *                 <code>this</code> will have no <code>ReleaseParameters</code>.
     *  @param memory A <code>MemoryParameters</code> object which will be
     *                associated with the constructed intance. If null,
     *                <code>this</code> will have no <code>MemoryParameters</code>.
     *  @param area The <code>MemoryArea</code> for <code>this</code>. If null,
     *              the memory area will be that of the current thread.
     *  @param group A <code>ProcessingGroupParamters</code> object which
     *               will be associated with the constructed instance. If null,
     *               will not be associated with any processing group.
     *  @param nonheap A flag meaning, when true, that this will have
     *                 characteristics identical to a
     *                 <code>NoHeapRealtimeThread</code>. A false value
     *                 means this will have characteristics identical to a
     *                 <code>RealtimeThread</code>. If true and the current
     *                 thread is <i>not</i> a <code>NoHeapRealtimeThread</code>
     *                 of a <code>RealtimeThread</code> executing within a
     *                 <code>ScopedMemory</code> or <code>ImmortalMemory</code>
     *                 scope then an <code>IllegalArgumentException<code> is thrown.
     *  @throws java.lang.IllegalArgumentException If the initial memory area is
     *                                             in heap memory, and the
     *                                             <code>nonheap</code> parameter
     *                                             is true.
     */
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

    /** Create an instance of <code>AsyncEventHandler</code> whose
     *  parameters are inherited from the current thread, if the current
     *  thread is a <code>RealtimeThread</code>, or null, otherwise.
     *
     *  @param scheduling A <code>SchedulingParameters</code> object which
     *                    will be associated with the constructed instance.
     *                    If null, <code>this</code> will be assigned the
     *                    reference to the <code>SchedulingParameters</code>
     *                    of the current thread.
     *  @param release A <code>ReleaseParameters</code> obejct which will be
     *                 associated with the constructed isntance. If null,
     *                 <code>this</code> will have no <code>ReleaseParameters</code>.
     *  @param memory A <code>MemoryParameters</code> object which will be
     *                associated with the constructed intance. If null,
     *                <code>this</code> will have no <code>MemoryParameters</code>.
     *  @param area The <code>MemoryArea</code> for <code>this</code>. If null,
     *              the memory area will be that of the current thread.
     *  @param group A <code>ProcessingGroupParamters</code> object which
     *               will be associated with the constructed instance. If null,
     *               will not be associated with any processing group.
     *  @param logic The <code>java.lang.Runnable</code> object whose <code>run()</code>
     *               method is executed by <code>handleAsyncEvent()</code>.
     *  @throws java.lang.IllegalArgumentException If the initial memory area is
     *                                             in heap memory, and the
     *                                             <code>nonheap</code> parameter
     *                                             is true.
     */
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

    /** Create an instance of <code>AsyncEventHandler</code> whose
     *  parameters are inherited from the current thread, if the current
     *  thread is a <code>RealtimeThread</code>, or null, otherwise.
     *
     *  @param scheduling A <code>SchedulingParameters</code> object which
     *                    will be associated with the constructed instance.
     *                    If null, <code>this</code> will be assigned the
     *                    reference to the <code>SchedulingParameters</code>
     *                    of the current thread.
     *  @param release A <code>ReleaseParameters</code> obejct which will be
     *                 associated with the constructed isntance. If null,
     *                 <code>this</code> will have no <code>ReleaseParameters</code>.
     *  @param memory A <code>MemoryParameters</code> object which will be
     *                associated with the constructed intance. If null,
     *                <code>this</code> will have no <code>MemoryParameters</code>.
     *  @param area The <code>MemoryArea</code> for <code>this</code>. If null,
     *              the memory area will be that of the current thread.
     *  @param group A <code>ProcessingGroupParamters</code> object which
     *               will be associated with the constructed instance. If null,
     *               will not be associated with any processing group.
     *  @param logic The <code>java.lang.Runnable</code> object whose <code>run()</code>
     *               method is executed by <code>handleAsyncEvent()</code>.
     *  @param nonheap A flag meaning, when true, that this will have
     *                 characteristics identical to a
     *                 <code>NoHeapRealtimeThread</code>. A false value
     *                 means this will have characteristics identical to a
     *                 <code>RealtimeThread</code>. If true and the current
     *                 thread is <i>not</i> a <code>NoHeapRealtimeThread</code>
     *                 of a <code>RealtimeThread</code> executing within a
     *                 <code>ScopedMemory</code> or <code>ImmortalMemory</code>
     *                 scope then an <code>IllegalArgumentException<code> is thrown.
     *  @throws java.lang.IllegalArgumentException If the initial memory area is
     *                                             in heap memory, and the
     *                                             <code>nonheap</code> parameter
     *                                             is true.
     */
    public AsyncEventHandler(SchedulingParameters scheduling,
			     ReleaseParameters release,
			     MemoryParameters memory,
			     MemoryArea area,
			     ProcessingGroupParameters group,
			     boolean nonheap, Runnable logic) {
	this(scheduling, release, memory, area, group, logic);
	this.nonheap = nonheap;
	
    }

    /** Inform the scheduler and cooperating facilities that the feasibility
     *  parameters associated with <code>this</code> should be considered in
     *  feasibility analyses until further notified, only if the new set of
     *  parameters is feasible.
     *
     *  @return True if the additions is successful. False if the addition is
     *          not successful or there is no assigned scheduler.
     */
    public boolean addIfFeasible() {
	if ((currentScheduler == null) ||
	    (!currentScheduler.isFeasible(this, getReleaseParameters()))) return false;
	else return addToFeasibility();
    }

    /** Inform the scheduler and cooperating facilities that the feasibility
     *  parameters associated with <code>this</code> should be considered in
     *  feasibility analyses until further notified.
     */
    public boolean addToFeasibility() {
	if (currentScheduler != null) {
	    currentScheduler.addToFeasibility(this);
	    return currentScheduler.isFeasible();
	}
	else return false;
    }

    /** This is an accessor method for <code>fireCount</code>. This method
     *  atomically sets the value of <code>fireCount</code> to zero and
     *  returns the value from before it was set to zero. This may be used
     *  by handlers for which the logic can accommodate multiple firings in
     *  a single execution. The general form for using this is:
     *  <p>
     *  <code>
     *  public void handleAsyncEvent() {
     *      int numberOfFirings = getAndClearPendingFireCount();
     *      <handle the events>
     *  }
     *  </code>
     *
     *  @return The value held by <code>fireCount</code> prior to setting
     *          the value to zero.
     */  
    protected final int getAndClearPendingFireCount() {
	int x = fireCount;
	fireCount = 0;
	return x;
    }

    /** This is an accessor method for <code>fireCount</code>. This method
     *  atomically decrements, by one, the value of <code>fireCount</code>
     *  (if it was greater than zero) and returns the value from before
     *  the decrement. This method can be used in the <code>handleAsyncEvent</code>
     *  method to handle multiple firings:
     *  <p>
     *  <code>
     *  public void handleAsyncEvent() {
     *      <setup>
     *      do {
     *          <handle the events>
     *      } while (getAndDecrementPendingFireCounts() > 0);
     *  }
     *  </code>
     *  <p>
     *  This construction is necessary only in the case where one wishes to
     *  avoid the setup costs since the framework guarantees that
     *  <code>handleAsyncEvent()</code> will be invoked the appropriate number of times.
     *
     *  @return The value held by <code>fireCount</code> prior to decrementing it by one.
     */
    protected int getAndDecrementPendingFireCount() {
	if (fireCount > 0) return fireCount--;
	else return 0;
    }

    /** This is an accessor method for <code>fireCount</code>. This method
     *  atomically increments, by one, the value of <code>fireCount</code>
     *  and return the value from before the increment.
     *
     *  @return The value held by <code>fireCount</code> prior to incrementing it by one.
     */
    protected int getAndIncrementPendingFireCount() {
	return fireCount++;
    }

    /** This is an accessor method for the intance of <code>MemoryArea</code>
     *  associated with <code>this</code>.
     *
     *  @return The instance of <code>MemoryArea</code> which is the current
     *          area for <code>this</code>.
     */
    public MemoryArea getMemoryArea() {
	return memArea;
    }

    /** Gets the memory parameters associated with this instance of <code>Schedulable</code>.
     *
     *  @return The <code>MemoryParameters</code> object associated with <code>this</code>.
     */
    public MemoryParameters getMemoryParameters() {
	return memParams;
    }

    /** This is an accessor method for <code>fireCount</code>. The <code>fireCount</code>
     *  field nominally holds the number of times associated instance of <code>AsyncEvent</code>
     *  have occured that have not had the method <code>handleAsyncEvent()</code> invoked.
     *  Due to accessor methods the pplication logic may manipulate the value in this field
     *  for application specific reasons.
     *
     *  @return The value held by <code>fireCount</code>.
     */
    protected final int getPendingFireCount() {
	return fireCount;
    }

    /** Gets the processing group parameters associated with this intance of <code>Schedulable</code>.
     *
     *  @return The <code>ProcessingGroupParameters</code> object associated with <code>this</code>.
     */
    public ProcessingGroupParameters getProcessingGroupParameters() {
	return group;
    }

    /** Gets the release parameters associated with this instance of <code>Schedulable</code>.
     *
     *  @return The <code>ReleaseParameters</code> object associated with <code>this</code>.
     */
    public ReleaseParameters getReleaseParameters() {
	return release;
    }

    /** Gets the instance of <code>Scheduler</code> associated with this instance of <code>Schedulable</code>.
     *
     *  @return The instance of <code>Scheduler</code> associated with <code>this</code>.
     */
    public Scheduler getScheduler() {
	return currentScheduler;
    }

    /** Gets the scheduling parameters associated with this instance of <code>Schedulable</code>.
     *
     *  @return The <code>SchedulingParameters</code> object associated with <code>this</code>.
     */
    public SchedulingParameters getSchedulingParameters() {
	return scheduling;
    }

    /** This method holds the logic which is to be executed when assiciated instances of
     *  <code>AsyncEvent</code> occur. If this handler was constructed using an instance of
     *  <code>java.lang.Runnable</code> as an argument to the constructor, then that instance's
     *  <code>run()</code> method will be invoked from this method. This method will be invoked
     *  repreadedly while <code>fireCount</code> is greater than zero.
     */
    public void handleAsyncEvent() {
	if (logic != null) logic.run();
    }

    /** Inform the scheduler and cooperating facilities that the scheduling characteristics
     *  of this instance of <code>Schedulable</code> should not  be considered in feasibility
     *  analyses until further notified.
     *
     *  @return True, if the removal was successful. False, if the removal was unsuccessful.
     */
    public void removeFromFeasibility() {
	if (currentScheduler != null)
	    currentScheduler.removeFromFeasibility(this);
    }

    /** Used by the asynchronous event mechanism, see <code>AsyncEvent</code>.
     *  This method invokes <code>handleAsyncEvent()</code> repeatedly while
     *  fire count is greater than zero. Applications cannot override this
     *  method and should thus override <code>handleAsyncEvent()</code> in
     *  subclasses with the logic of the handler.
     */
    public final void run() {
	while (getAndDecrementPendingFireCount() > 0)
	    handleAsyncEvent();
    }   

    /** This method appears in many classes in the RTSJ and with various parameters.
     *  The parameters are either new scheduling characteristics for an instance
     *  <code>Schedulable</code> or an instance of <code>Schedulable</code>. The method
     *  first performs a feasibility analysis using the new scheduling characteristics
     *  as replacements for the matching scheduling characteristics of either
     *  <code>this</code> or the given instance of <code>Schedulable</code>. If the
     *  resulting system is feasible the method replaces the current scheduling
     *  characteristics, of either <code>this</code> or the given instance of
     *  <code>Schedulable</code> as appropriate, with the new scheduling characteristics.
     *
     *  @param release The proposed release parameters.
     *  @param memory The proposed memory parameters.
     *  @return True, if the resulting system is feasible and the changes are made.
     *          False, if the resulting system is not feasible and no changes are made.
     */
    public boolean setIfFeasible(ReleaseParameters release,
				 MemoryParameters memory) {
	return setIfFeasible(release, memory, getProcessingGroupParameters());
    }

    /** This method appears in many classes in the RTSJ and with various parameters.
     *  The parameters are either new scheduling characteristics for an instance
     *  <code>Schedulable</code> or an instance of <code>Schedulable</code>. The method
     *  first performs a feasibility analysis using the new scheduling characteristics
     *  as replacements for the matching scheduling characteristics of either
     *  <code>this</code> or the given instance of <code>Schedulable</code>. If the
     *  resulting system is feasible the method replaces the current scheduling
     *  characteristics, of either <code>this</code> or the given instance of
     *  <code>Schedulable</code> as appropriate, with the new scheduling characteristics.
     *
     *  @param release The proposed release parameters.
     *  @param memory The proposed memory parameters.
     *  @param group The proposed processing group parameters.
     *  @return True, if the resulting system is feasible and the changes are made.
     *          False, if the resulting system is not feasible and no changes are made.
     */
    public boolean setIfFeasible(ReleaseParameters release,
				 MemoryParameters memory,
				 ProcessingGroupParameters group) {
	if (currentScheduler == null) return false;
	else return currentScheduler.setIfFeasible(this, release, memory, group);
    }

    /** This method appears in many classes in the RTSJ and with various parameters.
     *  The parameters are either new scheduling characteristics for an instance
     *  <code>Schedulable</code> or an instance of <code>Schedulable</code>. The method
     *  first performs a feasibility analysis using the new scheduling characteristics
     *  as replacements for the matching scheduling characteristics of either
     *  <code>this</code> or the given instance of <code>Schedulable</code>. If the
     *  resulting system is feasible the method replaces the current scheduling
     *  characteristics, of either <code>this</code> or the given instance of
     *  <code>Schedulable</code> as appropriate, with the new scheduling characteristics.
     *
     *  @param release The proposed release parameters.
     *  @param group The proposed processing group parameters.
     *  @return True, if the resulting system is feasible and the changes are made.
     *          False, if the resulting system is not feasible and no changes are made.
     */
    public boolean setIfFeasible(ReleaseParameters release,
				 ProcessingGroupParameters group) {
	return setIfFeasible(release, getMemoryParameters(), group);
    }

    /** Sets the memory parameters associated with this instance of <code>Schedulable</code>.
     *  When is is next executed, that execution will use the new parameters to control memory
     *  allocation. Does not affect the current invocation of the <code>run()</code> of this handler.
     *
     *  @param memory A <code>MemoryParameters</code> object which will become the memory
     *                parameters associated with <code>this</code> after the method call.
     */
    public void setMemoryParameters(MemoryParameters memory) {
	memParams = memory;
    }

    /** The method first performs a feasibility analysis using the given memory parameters
     *  as replacements for the memory parameters of <code>this</code>. If the resulting
     *  system is feasible the method replaces the current memory parameters of
     *  <code>this</code> with the new memory parameters.
     *
     *  @param memory The proposed memory parameters.
     *  @return True, if the resulting system is feasible and the changes are made.
     *          False, if the resulting system is not feasible and no changes are made.
     */
    public boolean setMemoryParametersIfFeasible(MemoryParameters memory) {
	return setIfFeasible(getReleaseParameters(), memory,
			     getProcessingGroupParameters());
    }

    /** Sets the processing group parameters associated with this instance of <code>Schedulable</code>.
     *
     *  @param parameters A <code>ProcessingGroupParameters</code> object which will become
     *                    the processing group parameters associated with <code>this</code>
     *                    after the method call.
     */
    public void setProcessingGroupParameters(ProcessingGroupParameters group) {
	this.group = group;
    }

    /** The method first performs a feasibility analysis using the given processing group
     *  parameters as replacements for the processing group parameters of <code>this</code>.
     *  If the resulting system is feasible the method replaces the current processing group
     *  parameters of <code>this</code> with the new processing group parameters.
     *
     *  @param group The proposed processing group parameters.
     *  @return True, if the resulting system is feasible and the changes are made.
     *          False, if the resulting system is not feasible and no changes are made.
     */
    public boolean setProcessingGroupParametersIfFeasible(ProcessingGroupParameters group) {
	return setIfFeasible(getReleaseParameters(),
			     getMemoryParameters(), group);
    }

    /** Set the realease parameters associated with this instance of <code>Schedulable</code>.
     *  When it is next executed, that execution will use the new parameters to control
     *  scheduling. If the scheduling parameters of a handler is set to null, the handler will
     *  be executed immediately when any associated <code>AsyncEvent</code> is fired, in the
     *  context of the thread invoking the <code>fire()</code> method. Does not affect the
     *  current invocation of the <code>run()</code> of this handler.
     *  
     *  @param release A <code>ReleaseParameters</code> object which will become the release
     *                 parameters associated with this after the method call.
     */
    public void setReleaseParameters(ReleaseParameters release) {
	this.release = release;
    }

    /** The method first performs a feasibility analysis using the given release parameters
     *  as replacements for the release parameters of <code>this</code>. If the resulting
     *  system is feasible the method replaces the current release parameters of
     *  <code>this</code> with the new release parameters.
     *
     *  @param release The proposed release parameters.
     *  @return True, if the resulting system is feasible and the changes are made.
     *          False, if the resulting system is not feasible and no changes are made.
     */
    public boolean setReleaseParametersIfFeasible(ReleaseParameters release) {
	return setIfFeasible(release, getMemoryParameters(),
			     getProcessingGroupParameters());
    }

    /** Sets the scheduler associated with this instance of <code>Schedulable</code>.
     *
     *  @param scheduler An instance of <code>Scheduler</code> which will manage the
     *                   execution of this thread. If <code>scheduler</code> is null
     *                   nothing happens.
     *  @throws java.langIllegalThreadStateException
     */
    public void setScheduler(Scheduler scheduler)
	throws IllegalThreadStateException {
	setScheduler(scheduler, getSchedulingParameters(), getReleaseParameters(),
		     getMemoryParameters(), getProcessingGroupParameters());
    }

    /** Sets the scheduler associated with this instance of <code>Schedulable</code>.
     *
     *  @param scheduler An instance of <code>Scheduler</code> which will manage the
     *                   execution of this thread. If <code>scheduler</code> is null
     *                   nothing happens.
     *  @param scheduling A <code>SchedulingParameters</code> object which will be
     *                    associated with <code>this</code>. If null, <code>this</code>
     *                    will be assigned the reference to the instance of
     *                    <code>SchedulingParameters</code> of the current thread.
     *  @param release A <code>ReleaseParameters</code> object which will be associated
     *                 with <code>this</code>. If null, <code>this</code> will have no
     *                 associated instance of <code>ReleaseParameters</code>.
     *  @param memoryParameters A <code>MemoryParameters</code> object which will be
     *                          associated with <code>this</code>. If null, <code>this</code>
     *                          will have no associated instance of <code>MemoryParameters</code>.
     *  @throws java.lang.IllegalThreadStateException
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

    /** Sets the scheduling parameters associated with this instance of <code>Schedulable</code>.
     *  When it is next executed, that execution will use the new parameters to control releases.
     *  If the scheduling parameters of a handler is set to null, the handler will be executed
     *  immediately when any associated <code>AsycnEvent</code> is fired, in the context of the
     *  thread invoking the <code>fire()</code> method. Does not affect the current invocation of
     *  the <code>run()</code> of this handler.
     *
     *  @param scheduling A <code>SchedulingParameters</code> object which will become the
     *                    scheduling parameters associated with <code>this</code> after the method call.
     */
    public void setSchedulingParameters(SchedulingParameters scheduling) {
	this.scheduling = scheduling;
    }

    /** The method first performs a feasibility analysis using the given scheduling parameters as
     *  replacements for the scheduling parameters of <code>this</code>. If the resulting system
     *  is feasible the method replaces the current scheduling parameters of <code>this</code>
     *  with new scheduling parameters.
     *
     *  @param scheduling The proposed scheduling parameters.
     *  @return True, if the resulting system is feasible and the changes are made.
     *          False, if the resulting system is not feasible and no changes are made.
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
