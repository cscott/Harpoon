// RealtimeThread.java, created by wbeebee
// Copyright (C) 2001 Wes Beebee <wbeebee@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package javax.realtime; 

/**
 * @author Wes Beebee <<a href="mailto:wbeebee@mit.edu">wbeebee@mit.edu</a>>
 */

/** Class <code>RealtimeThread</code> extends <code>java.lang.Thread</code>
 *  and includes classes and methods to get and set parameterer objects,
 *  manage the execution of those threads with a
 *  <code>ReleaseParameters</code> type of <code>PeriodicParameters</code>,
 *  and manage waiting.
 *  <p>
 *  A <code>RealtimeThread</code> object must be placed in a memory area
 *  such that thread logic may unexceptionally access instance variables
 *  and such that Java methods on <code>java.lang.Thread</code> (e.g.,
 *  enumerate and join) complete normally except where such execution
 *  would cause access violations.
 *  <p>
 *  Parameters for constructors may be <code>null</code>. In such cases
 *  the default value will be the default value set for the particular
 *  type by the associated instance of <code>Scheduler</code>.
 */
public class RealtimeThread extends Thread implements Schedulable {

    public Scheduler currentScheduler = null;

    /** Contains the cactus stack of previous memoryAreas... */
    MemAreaStack memAreaStack;

    /** The top of the stack for this RealtimeThread. */
    int heapMemCount;

    MemAreaStack topStack;

    /** Specifies whether this RealtimeThread has access to the heap. */
    boolean noHeap;

    private boolean blocked = false;

    /** Contains the memoryArea associated with this mem. */
    MemoryArea mem, original;

    /** Realtime parameters for this thread */
    ReleaseParameters releaseParameters = null;
    MemoryParameters memoryParameters = null;
    SchedulingParameters schedulingParameters = null;
    ProcessingGroupParameters processingGroupParameters = null;

    /** Specifies whether the initialization code has finished setting up RTJ. */
    static boolean RTJ_init_in_progress;

    /** The target to run. */
    
    private Runnable target;

    static void checkInit() {
	if (RTJ_init_in_progress) {
	    System.out.println("Cannot use any MemoryArea except heap until" +
			       "RTJ initialization has completed!");
	    System.exit(-1);
	}
    }

    /** Create a real-time thread. All parameter values are null. */
    public RealtimeThread() {
	this((MemoryArea)null);
	setup();
    }

    /** Create a real-time thread with the given <code>SchedulingParameters</code>.
     *
     *  @param scheduling The <code>SchedulingParameters</code> associated with
     *                    <code>this</code> (and possibly other instances of
     *                    <code>Schedulable</code>).
     */
    public RealtimeThread(SchedulingParameters scheduling) {
	this();
	schedulingParameters = scheduling;
    }

    /** Create a real-time thread with the given <code>SchedulingParameters</code>
     *  and <code>ReleaseParameters</code>.
     *
     *  @param scheduling The <code>SchedulingParameters</code> associated with
     *                    <code>this</code> (and possibly other instances of
     *                    <code>Schedulable</code>).
     *  @param release The <code>ReleaseParameters</code> associated with
     *                 <code>this</code> (and possibly other instances of
     *                 <code>Schedulable</code>).
     */
    public RealtimeThread(SchedulingParameters scheduling,
			  ReleaseParameters release) {
	this();
	schedulingParameters = scheduling;
	releaseParameters = release;
    }

    /** Create a real-time thread with the given characteristics and a 
     *  <code>java.lang.Runnable</code>.
     *
     *  @param scheduling The <code>SchedulingParameters</code> associated with
     *                    <code>this</code> (and possibly other instances of
     *                    <code>Schedulable</code>).
     *  @param release The <code>ReleaseParameters</code> associated with
     *                 <code>this</code> (and possibly other instances of
     *                 <code>Schedulable</code>).
     *  @param memory The <code>MemoryParameters</code> associated with
     *                <code>this</code> (and possibly other instances of
     *                <code>Schedulable</code>).
     *  @param area The <code>MemoryArea</code> associated with <code>this</code>.
     *  @param group The <code>ProcessingGroupParameters</code> associated with
     *               <code>this</code> (and possibly other instances of
     *               <code>Schedulable</code>).
     *  @param logic A <code>Runnable</code> whose <code>run()</code> method will
     *               be executed for <code>this</code>.
     */
    public RealtimeThread(SchedulingParameters scheduling,
			  ReleaseParameters release, MemoryParameters memory,
			  MemoryArea area, ProcessingGroupParameters group,
			  Runnable logic) {
	this(area, logic);
	schedulingParameters = scheduling;
	releaseParameters = release;
	memoryParameters = memory;
	processingGroupParameters = group;
    }

    public RealtimeThread(ThreadGroup group, Runnable target) {
	super(group, (Runnable)null);
	this.target = target;
	mem = null;
	setup();
    }
    
    public RealtimeThread(MemoryArea memory) {
	super();
	target = null;
	mem = ((original=memory)==null)?null:(memory.shadow);
	setup();
    }
    
    public RealtimeThread(MemoryParameters mp, Runnable target) {
	this(mp.getMemoryArea(), target);
    }

    public RealtimeThread(Runnable target) {
	this((MemoryArea)null, target);
    }
    
    public RealtimeThread(String name) {
	this((Runnable)null, name);
    }
    
    public RealtimeThread(ThreadGroup group, Runnable target, String name) {
	this(group, target, name, null);
    }

    public RealtimeThread(ThreadGroup group, String name) {
	this(group, null, name);
    }
    
    public RealtimeThread(Runnable target, String name) {
	super(name);
	this.target = target;
	mem = null;
	setup();
    }
    
    public RealtimeThread(MemoryArea memory, Runnable target) {
	super(target);
	mem = memory;
	setup();
    }

    public RealtimeThread(ThreadGroup group, Runnable target,
			  String name, MemoryArea memory) {
	super(group, target, name);
	this.target = target;
	mem = ((original=memory)==null)?null:(memory.shadow);
	setup();
    }

    /** This sets up the unspecified local variables for the constructor. */
    private void setup() {
	memAreaStack = null;
	noHeap = false;
	heapMemCount = 0;
	topStack = null;
	if (!RTJ_init_in_progress) {
	    currentScheduler = Scheduler.getDefaultScheduler();
	}
    }

    /** Add the scheduling and release characteristics of <code>this</code>
     *  to the sed of such characteristics already being considered, if the
     *  addition would result in the new, larger set being feasible.
     *
     *  @return True, if the addition would result in the set of considered
     *          characteristics being feasible. False, if the addition would
     *          result in the set of considered characteristics being
     *          infeasible or there is no assigned instance of <code>Scheduler</code>.
     */
    public boolean addIfFeasible() {
	if ((currentScheduler == null) ||
	    (!currentScheduler.isFeasible(this, getReleaseParameters()))) return false;
	else return addToFeasibility();
    }

    /** Inform the scheduler and cooperating facilities that scheduling and
     *  release characteristics of this instance of <code>Schedulable</code>
     *  should be considered in feasibility analysis until further notified.
     *
     *  @return True, if the addition was successful. False, if not.
     */
    public boolean addToFeasibility() {
	if(currentScheduler != null) {
	    currentScheduler.addToFeasibility(this);
	    return currentScheduler.isFeasible();
	} else return false;
    }
    
    /** Gets a reference to the current instance of <code>RealtimeThread</code>.
     *  
     *  @return A reference to the current instance of <code>RealtimeThread</code>.
     *  @throws java.lang.ClassCastException If the current thread is not a
     *                                       <code>RealtimeThread</code>.
     */
    public static RealtimeThread currentRealtimeThread()
	throws ClassCastException {
	return (RealtimeThread)currentThread();
    }

    /** Stop unblocking <code>waitForNextPeriod()</code> for <code>this</code> if
     *  the type of the associated instance of <code>ReleaseParameters</code> is
     *  <code>PeriodicParameters</code>. If <code>this</code> does not have a type
     *  of <code>PeriodicParameters</code>, nothing happens.
     */
    public void deschedulePeriodic() {
	if (releaseParameters instanceof PeriodicParameters) blocked = true;
    }
    
    /** Gets the current memory area of <code>this</code>.
     *
     *  @return A reference to the current <code>MemoryArea</code> object.
     */
    public MemoryArea getCurrentMemoryArea() {
	return mem;
    }

    /** Memory area stacks include inherited stacks from parent threads. The initial
     *  memory area for the current <code>RealtimeThread</code> is the memory area
     *  given as a parameter to the constructor. This method returns the position in
     *  the memory area stack of that initial memory area.
     *
     *  @return The index into the memory area stack of the initial memory area of
     *          the current <code>RealtimeThread</code>.
     */
    public /* spec says it should be static */ int getInitialMemoryAreaIndex() {
	MemAreaStack temp = memAreaStack;
	int index = 0;
	while (temp != null) {
	    if (temp.entry == mem) return index;
	    else {
		index++;
		temp = temp.next;
	    }
	}

	return -1;
    }
    
    /** Gets a reference to the current <code>MemoryArea</code>.
     *
     *  @return A reference to the current memory area in which allocations occur.
     */
    public MemoryArea getMemoryArea() {
	if (mem == null) {
	    mem = (original = HeapMemory.instance()).shadow;
	}
	return original;
    }

    /** Get the size of the stack of <code>MemoryArea</code> instances to which
     *  this <code>RealtimeThread</code> has access.
     *
     *  @return The size of the stack of <code>MemoryArea</code> instances.
     */
    public /* spec says it should be static */ int getMemoryAreaStackDepth() {
	MemAreaStack temp = memAreaStack;
	int count = 0;
	while (temp != null) {
	    count++;
	    temp = temp.next;
	}
	return count;
    }

    /** Gets a reference to the <code>MemoryParameters</code> object.
     *
     *  @return A reference to the current <code>MemoryParameters</code> object.
     */
    public MemoryParameters getMemoryParameters() {
	return memoryParameters;
    }
    
    /** Gets the instance of <code>MemoryArea</code> in the memory area stack
     *  at the index given. If the given index does not exist in the memory
     *  area scope stack then null is returned.
     *
     *  @param index The offset into the memory area stack.
     *  @return The instance of <code>MemoryArea</code> at index or <code>null</code>
     *          if the given value does not correspond to a position in the stack.
     */
    public /* specs says it should be static */ MemoryArea getOuterMemoryArea(int index) {
	MemAreaStack temp = memAreaStack;
	for (int i = 0; i < index; i++)
	    if (temp != null) temp = temp.next;
	    else return null;
	if (temp != null) return temp.entry;
	else return null;
    }

    /** Gets a reference to the <code>ProcessingGroupParameters</code> object.
     *
     *  @return A reference to the current <code>ProcessingGroupParameters</code> object.
     */
    public ProcessingGroupParameters getProcessingGroupParameters() {
	return processingGroupParameters;
    }

    /** Gets a reference to the <code>ReleaseParameters</code> object.
     *
     *  @return A reference to the current <code>ReleaseParameters</code> object.
     */
    public ReleaseParameters getReleaseParameters() {
	return releaseParameters;
    }

    /** Get a reference to the <code>Scheduler</code> object.
     *
     *  @return A reference to the current <code>Scheduler</code> object.
     */
    public Scheduler getScheduler() {
	return currentScheduler;
    }

    /** Gets a reference to the <code>SchedulingParameters</code> object.
     *
     *  @return A reference to the current <code>SchedulingParameters</code> object.
     */
    public SchedulingParameters getSchedulingParameters() {
	return schedulingParameters;
    }

    /** Sets the state of the generic <code>AsynchronouslyInterruptedException<code>
     *  to pending.
     */
    public void interrupt() {
	// TODO

	super.interrupt();
    }

    /** Inform the scheduler and cooperating facilities that the scheduling and
     *  release characteristics of this instance of <code>Schedulable</code>
     *  should <i>not</i> be considered in feasibility analyses until further
     *  notified.
     *
     *  @return True, if the removal was successful. False, if the removal was
     *          unsuccessful.
     */
    public void removeFromFeasibility() {
	if (currentScheduler != null)
	    currentScheduler.removeFromFeasibility(this);
    }

    /** Begin unblocking <code>waitForNextPeriod()</code> for a periodic thread.
     *  Typically used when a periodic schedulable object is in an overrun condition.
     *  The scheduler should recompute the schedule and perform admission control.
     *  If this does not have a type of <code>PeriodicParameters</code> as its
     *  <code>ReleaseParameters</code>, nothing happens.
     */
    public void schedulePeriodic() {
	blocked = false;
    }

    /** This method appears in many classes in the RTSJ and with various parameters.
     *  The parameters are either new scheduling characteristics for an instance
     *  <code>Schedulable</code> or an instance of <code>Schedulable</code>. The
     *  method first performs a feasibility analysis using the new scheduling
     *  characteristics as replacements for the matching scheduling characteristics
     *  of either <code>this</code> or the given instance of <code>Schedulable</code>.
     *  If the resulting system is feasible the method replaces the current scheduling
     *  characteristics, of either <code>this</code> or the given instance of
     *  <code>Schedulable</code> as appropriate, with the new scheduling characteristics.
     *
     *  @param release The proposed release parameters.
     *  @param memory The proposed memory parameters.
     *  @return True, if the resulting system is feasible and the changes are made.
     *          False, if the resulting system is not feasible and no changes are made.
     */
    public boolean setIfFeasible(ReleaseParameters release, MemoryParameters memory) {
	return setIfFeasible(release, memory, processingGroupParameters);
    }

    /** This method appears in many classes in the RTSJ and with various parameters.
     *  The parameters are either new scheduling characteristics for an instance
     *  <code>Schedulable</code> or an instance of <code>Schedulable</code>. The
     *  method first performs a feasibility analysis using the new scheduling
     *  characteristics as replacements for the matching scheduling characteristics
     *  of either <code>this</code> or the given instance of <code>Schedulable</code>.
     *  If the resulting system is feasible the method replaces the current scheduling
     *  characteristics, of either <code>this</code> or the given instance of
     *  <code>Schedulable</code> as appropriate, with the new scheduling characteristics.
     *
     *  @param release The proposed release parameters.
     *  @param memory The proposed memory parameters.
     *  @param group The proposed processing group parameters.
     *  @return True, if the resulting system is feasible and the changes are made.
     *          False, if the resulting system is not feasible and no changes are made.
     */
    public boolean setIfFeasible(ReleaseParameters release, MemoryParameters memory,
				 ProcessingGroupParameters group) {
	if (currentScheduler == null) return false;
	else return currentScheduler.setIfFeasible(this, release, memory, group);
    }

    /** This method appears in many classes in the RTSJ and with various parameters.
     *  The parameters are either new scheduling characteristics for an instance
     *  <code>Schedulable</code> or an instance of <code>Schedulable</code>. The
     *  method first performs a feasibility analysis using the new scheduling
     *  characteristics as replacements for the matching scheduling characteristics
     *  of either <code>this</code> or the given instance of <code>Schedulable</code>.
     *  If the resulting system is feasible the method replaces the current scheduling
     *  characteristics, of either <code>this</code> or the given instance of
     *  <code>Schedulable</code> as appropriate, with the new scheduling characteristics.
     *
     *  @param release The proposed release parameters.
     *  @param group The proposed processing group parameters.
     *  @return True, if the resulting system is feasible and the changes are made.
     *          False, if the resulting system is not feasible and no changes are made.
     */
    public boolean setIfFeasible(ReleaseParameters release, ProcessingGroupParameters group) {
	return setIfFeasible(release, memoryParameters, group);
    }

    /** Sets the memory parameters associated with this instance of <code>Schedulable</code>.
     *
     *  @param memory A <code>MemoryParameters</code> object which will become the memory
     *                parameters associated with <code>this</code> after the method call.
     *  @throws java.lang.IllegalThreadStateException
     */
    public void setMemoryParameters(MemoryParameters memory)
	throws IllegalThreadStateException {
	this.memoryParameters = memory;
    }
    
    /** The method first performs a feasibility analysis using the ginve memory parameters
     *  as replacements for the memory parameters of <code>this</code>. If the resulting
     *  system is feasible the method replaces the current memory parameters of
     *  <code>this</code> with the new memory parameters.
     *
     *  @param memory The proposed memory parameters. If <code>null</code>, nothing happens.
     *  @return True, if the resulting system is fesible and the changes are made. False,
     *          if the resulting system is not feasible and no changes are made.
     */
    public boolean setMemoryParametersIfFeasible(MemoryParameters memParam) {
	return setIfFeasible(releaseParameters, memParam, processingGroupParameters);
    }

    /** Sets the reference to the <code>ProcessingGroupParameters</code> object.
     *
     *  @param parameters A reference to the <code>ProcessingGroupParameters</code> object.
     */
    public void setProcessingGroupParameters(ProcessingGroupParameters parameters) {
	this.processingGroupParameters = parameters;
    }

    /** Sets the <code>ProcessingGroupParameters</code> of <code>this</code> only if the
     *  resulting set of scheduling and release characteristics is feasible.
     *
     *  @param groupParameters The <code>ProcessingGroupParameters</code> object. If
     *                         <code>null</code>, nothing happens.
     *  @return True, if the resulting system is feasible and the changes are made.
     *          False, if the resulting system is not feasible and no changes are made.
     */
    public boolean setProcessingGroupParametersIfFeasible(ProcessingGroupParameters groupParameters) {
	return setIfFeasible(releaseParameters, memoryParameters, groupParameters);
    }

    /** Since this affects the constraints expressed in the release parameters of the
     *  existing schedulable objects, this may change the feasibility of the current
     *  schedule.
     *
     *  @param release A <code>ReleaseParameters</code> object which will become the
     *                 release parameters associated with <code>this</code> afther the
     *                 method call.
     *  @throws java.lang.IllegalThreadStateException Thrown if the state of this instance
     *                                                of <code>Schedulable</code> is not
     *                                                appropriate to changing the release
     *                                                parameters.
     */
    public void setReleaseParameters(ReleaseParameters release)
	throws IllegalThreadStateException {
	this.releaseParameters = releaseParameters;
    }
    
    /** Set the <code>ReleaseParameters</code> for this schedulable object only if
     *  the resulting set of scheduling and release characteristics is feasible.
     *
     *  @param release The <code>ReleaseParameters</code> object. If <code>null</code>
     *                 nothing happens.
     *  @return True, if the resulting system is feasible and the changes are made.
     *          False, if the resulting system is not feasible and no changes are made.
     */
    public boolean setReleaseParametersIfFeasible(ReleaseParameters release) {
	return setIfFeasible(release, memoryParameters, processingGroupParameters);
    }

    /** Sets the reference to the <code>Scheduler</code> object.
     *
     *  @param scheduler A reference to the <code>Scheduler</code> object.
     *  @throws java.lang.IllegalThreadStateException Thrown when:
     *                                                <code>((Thread.isAlive() &&
     *                                                Not Blocked) == true)</code>.
     *                                                (Where blocked means waiting in
     *                                                <code>Thread.wait(), Thread.join()</code>
     *                                                or <code>Thread.sleep()</code>).
     */
    public void setScheduler(Scheduler scheduler)
	throws IllegalThreadStateException {
	if (currentScheduler != null) currentScheduler.removeFromFeasibility(this);
	currentScheduler = scheduler;
    }
    
    /** Sets the scheduler and associated parameter objects.
     *
     *  @param scheduler A reference to the scheduler that will manage the execution
     *                   of this thread. If <code>null</code>, no change to current
     *                   value of this parameter is made.
     *  @param scheduling A reference to the <code>SchedulingParameters</code> which
     *                    will be associated with <code>this</code>. If null, no
     *                    change to current value of this parameter is made.
     *  @param release A reference to the <code>ReleasePrameters</code> which will
     *                 be associated with <code>this</code>. If null, no change to
     *                 current value of this parameter is made.
     *  @param memory A reference to the <code>MemoryParameters</code> which will be
     *                associated with <code>this</code>. If null, no change to
     *                current value of this parameter is made.
     *  @param group A reference to the <code>ProcessingGroupParameters</code> which
     *               will be associated with <code>this</code>. If null, no change
     *               to current value of this parameter is made.
     *  @throws java.lang.IllegalThreadStateException Thrown when:
     *                                                <code>((Thread.isAlive() &&
     *                                                Not Blocked) == true).</code>
     *                                                (Where blocked means waiting
     *                                                in <code>Thread.wait(),
     *                                                Thread.join()</code> or
     *                                                <code>Thread.sleep()</code>).
     */
    public void setScheduler(Scheduler scheduler,
			     SchedulingParameters scheduling,
			     ReleaseParameters release,
			     MemoryParameters memParameters,
			     ProcessingGroupParameters processingGroup)
	throws IllegalThreadStateException {

	currentScheduler = scheduler;
	schedulingParameters = scheduling;
	releaseParameters = release;
	memoryParameters = memParameters;
	processingGroupParameters = processingGroup;
    }

    /** Sets the reference to the <code>SchedulingParameters</code> object.
     *
     *  @param scheduling A reference to the <code>SchedulingParameters</code> in
     *                    interface <code>Schedulable</code>.
     *  @throws java.lang.IllegalThreadStateException Thrown when:
     *                                                <code>((Thread.isAlive() &&
     *                                                Not Blocked) == true).</code>
     *                                                (Where blocked means waiting
     *                                                in <code>Thread.wait(),
     *                                                Thread.join()</code> or
     *                                                <code>Thread.sleep()</code>).
     */
    public void setSchedulingParameters(SchedulingParameters scheduling)
	throws IllegalThreadStateException {
	this.schedulingParameters = scheduling;
    }
    
    /** The method first performs a feasiblity analysis using the given scheduling
     *  parameters as replacements for the scheduling parameters of <code>this</code>.
     *  If the resulting system is feasible the method replaces the current scheduling
     *  parameters of <code>this</code> with the new scheduling parameters.
     *
     *  @param scheduling The proposed scheduling parameters. If null, nothing happens.
     *  @return True, if the resulting system is feasible and the changes are made.
     *          False, if the resulting system is not feasible and no changes are made.
     */
    public boolean setSchedulingParametersIfFeasible(SchedulingParameters scheduling) {
	//How do scheduling parameters affect the feasibility of the task set?
	this.schedulingParameters = scheduling;
	return true;
// 	if (currentScheduler == null) return false;
// 	SchedulingParameters oldSchedulingParameters = schedulingParameters;
// 	schedulingParameters = scheduling;
// 	if (currentScheduler.isFeasible()) return true;
// 	else {
// 	    schedulingParameters = oldSchedulingParameters;
// 	    return false;
// 	}
    }

    /** An accurate timer with nanoseconds granularity. The actual resolution
     *  available for the clock must be queried form somewhere else. The time
     *  base is the given <code>Clock</code>. The sleep time may be relative
     *  of absolute. If relative, then the calling thread is blocked for the
     *  amount of time given by the parameter. If absolute, then the calling
     *  thread is blocked until the indicated point in time. If the given
     *  absolute time is before the current time, the call to sleep returns
     *  immediately.
     *
     *  @param clock The instance of <code>Clock</code> used as the base.
     *  @param time The amount of time to sleep or the point in time at
     *              which to awaken.
     *  @throws java.lang.InterruptedException If interrupted.
     */
    public static void sleep(Clock clock, HighResolutionTime time)
	throws InterruptedException {
	if (time instanceof AbsoluteTime) {
	    RelativeTime temp = ((AbsoluteTime)time).subtract(clock.getTime());
	    Thread.sleep(temp.getMilliseconds(), temp.getNanoseconds());
	}
	else 
	    Thread.sleep(time.getMilliseconds(), time.getNanoseconds());
    }

    /** An accurate timer with nanoseconds granularity. The actual resolution
     *  available for the clock must be queried form somewhere else. The time
     *  base is the given <code>Clock</code>. The sleep time may be relative
     *  of absolute. If relative, then the calling thread is blocked for the
     *  amount of time given by the parameter. If absolute, then the calling
     *  thread is blocked until the indicated point in time. If the given
     *  absolute time is before the current time, the call to sleep returns
     *  immediately.
     *
     *  @param time The amount of time to sleep or the point in time at
     *              which to awaken.
     *  @throws java.lang.InterruptedException If interrupted.
     */
    public static void sleep(HighResolutionTime time)
	throws InterruptedException {
	sleep(Clock.getRealtimeClock(), time);
    }

    /** Starts the thread. */
    public void start() {
	if ((mem != null)&&(!mem.heap)) {
	    checkInit();
	}
	RealtimeThread previousThread = currentRealtimeThread();
	memAreaStack = previousThread.memAreaStack;
	MemoryArea newMem = previousThread.getMemoryArea();
	if (mem == null) {
	    enter(newMem, previousThread.getMemoryArea());
	} else {
	    enter(mem, original);
	}
	mem = getMemoryArea();
	super.start();// When the run method is called, 
	addToFeasibility();
    }

    /** Used by threads that have a reference to a <code>ReleaseParameters</code>
     *  type of <code>PeriodicParameters</code> to block until the start of each
     *  period. Periods start at either the start time in <code>PeriodicParameters</code>
     *  of when <cod>this.start()</code> is called. This method will block until
     *  the start of the next period unless the thread is in either an overrun or
     *  deadline miss condition. If both overrun and miss handlers are null and
     *  the thread has overrun its cost of missed a deadline <code>waitForNextPeriod</code>
     *  will immediately return false once per overrun or deadline miss. It will
     *  then again block until the start of the next period (unless, of course,
     *  the thread has overrun of missed again). If either the overrun of deadline
     *  miss handlers are not null and the thread is in either an overrun or
     *  deadline miss condition <code>waitForNextObject()</code> will block until
     *  handler corrects the situation (possibly by calling <code>schedulePeriodic</code>).
     *  <code>waitForNextPeriod()</code> throws <code>IllegalThreadStateException</code>
     *  if this does not have a reference to a <code>ReleaseParameters</code>
     *  type of <code>PeriodicParameters</code>.
     *
     *  @return True when the thread is not in an overrun or deadline miss
     *          condition and unblocks at the start of the next period.
     *  @throws java.lang.IllegalThreadStateException If <code>this</code> does not
     *                                                have a reference to a
     *                                                <code>ReleaseParameters</code>
     *                                                type of <code>PeriodicParameters</code>.
     */
    public boolean waitForNextPeriod() throws IllegalThreadStateException {
	if ((releaseParameters instanceof PeriodicParameters) && (!blocked))
	    PriorityScheduler.getScheduler().waitForNextPeriod(this);
	return false;
    }

    /** Informs <code>ReleaseParameters, ProcessingGroupParameters</code> and
     *  <code>MemoryParameters</code> that <code>this</code> has them as its parameters.
     */
    public void bindSchedulable() {
	releaseParameters.bindSchedulable(this);
	processingGroupParameters.bindSchedulable(this);
	memoryParameters.bindSchedulable(this);
    }

    /** Informs <code>ReleaseParameters, ProcessingGroupParameters</code> and
     *  <code>MemoryParameters</code> that <code>this</code> does not have them
     *  any longer as its parameters.
     */
    public void unbindSchedulable() {
	releaseParameters.unbindSchedulable(this);
	processingGroupParameters.unbindSchedulable(this);
	memoryParameters.unbindSchedulable(this);
    }

    public static int activeCount() {
	return Thread.activeCount();
    }

    /** Returns the current running thread. */    

    public static Thread currentThread() {
	return Thread.currentThread();
    }

    /** Same as <code>java.lang.Thread.enumerate()</code>. */
    
    public static int enumerate(Thread tarray[]) {
	return Thread.enumerate(tarray);
    }
    
    /** Same as <code>java.lang.Thread.interrupted()</code>. */
    
    public static boolean interrupted() {
	return Thread.interrupted();
    }
    
    /** Same as <code>java.lang.Thread.yield()</code>. */

    public static void yield() {
	Thread.yield();
    }
    
    /** Same as <code>java.lang.Thread.sleep(long)</code>. */

    public static void sleep(long millis) throws InterruptedException {
	Thread.sleep(millis);
    }
    
    /** Same as <code>java.lang.Thread.sleep(long, int)</code>. */

    public static void sleep(long millis, int nanos) 
	throws InterruptedException {
	Thread.sleep(millis, nanos);
    }
    
    /** */

    public static void dumpStack() {
	//		System.out.println("MemoryArea stack:");
	System.out.println(currentRealtimeThread().memAreaStack.toString());
	Thread.dumpStack();
    }
    
    /** Override the Thread.run() method, because Thread.run() doesn't work. */
    public void run() {
	if (target != null) {
	    target.run();
	}
    }

    /** For internal use only. */
    public MemoryArea memoryArea() {
	if (mem == null) { // Bypass static initializer problem.
	    mem = (original = HeapMemory.instance()).shadow;
	}
	return mem;
    }  

    void enter(MemoryArea mem, MemoryArea original) {
	memAreaStack = MemAreaStack.PUSH(this.mem, this.original, memAreaStack);
	this.original = original;
	/* Think about whether this should be original or mem... */
	(this.mem = mem).enterMemBlock(this, memAreaStack);
    }

    /** */
    void exitMem() {
	mem.exitMemBlock(this, memAreaStack);
	mem = memAreaStack.entry;
	original = memAreaStack.original;
	memAreaStack = MemAreaStack.POP(memAreaStack);
    }
    
    /** */
    void cleanup() {
        while (memAreaStack != topStack) {
	    exitMem();
	}
    }

    /** Get the outerScope of a given MemoryArea for the current 
     *  RealtimeThread. 
     */
    
    MemoryArea outerScope(MemoryArea child) {
	MemAreaStack current = memAreaStack.first(child.shadow);
	if (current != null) {
	    current = current.next;
	}
	while ((current != null) && (!current.entry.scoped)) {
	    current = current.next;
	}
	if (current == null) {
	    return getMemoryArea();
	} else {
	    return current.original;
	}
    }
    
    /** */
    boolean checkAccess(MemoryArea source, MemoryArea target) {
	MemAreaStack sourceStack = (source == memoryArea()) ? 
	    memAreaStack : memAreaStack.first(source);
	return (sourceStack != null) && (sourceStack.first(target) != null);
    }
    
    public void checkNoHeapWrite(Object obj) {}
    
    public void checkNoHeapRead(Object obj) {}
    
    public String toString() {
	return "RealtimeThread";
    }
}
