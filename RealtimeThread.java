// RealtimeThread.java, created by wbeebee
// Copyright (C) 2001 Wes Beebee <wbeebee@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package javax.realtime; 

/**
 * @author Wes Beebee <<a href="mailto:wbeebee@mit.edu">wbeebee@mit.edu</a>>
 */

/** <code>RealtimeThread</code> extends <code>java.lang.Thread</code>
 *  and includes classes and methods to get and set parameterer objects,
 *  manage the execution of those threads with a
 *  <code>ReleaseParameters</code> type of <code>PeriodicParameters</code>,
 *  and waiting.
 *  <p>
 *  A <code>RealtimeThread</code> object must be placed in a memory area
 *  such that thread logic may unexceptionally access instance variables
 *  and such that Java methods on <code>java.lang.Thread</code> (e.g.,
 *  enumerate and join) complete normally except where such execution
 *  would cause access violations.
 *  <p>
 *  Parameters for constructors may be <code>null</code>. In such cases
 *  the default value will be the default value set for the particular
 *  type by the associated instance of <code>Schedulable</code>.
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
    }

    /** Create a real-time thread with the given <code>SchedulingParameters</code>. */
    public RealtimeThread(SchedulingParameters scheduling) {
	this();
	schedulingParameters = scheduling;
    }

    /** Create a real-time thread with the given <code>SchedulingParameters</code>
     *  and <code>ReleaseParameters</code>.
     */
    public RealtimeThread(SchedulingParameters scheduling,
			  ReleaseParameters release) {
	this();
	schedulingParameters = scheduling;
	releaseParameters = release;
    }

    /** Create a real-time thread with the given characteristics and a 
     *  <code>java.lang.Runnable</code>.
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

// 	public RealtimeThread(ThreadGroup group, Runnable target) {
// 		super(group, target);
// 		mem = null;
// 		setup();
// 	}
    
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
    }

    /** Add to the feasibility of the already set scheduler if the resulting
     *  feasibility set is schedulable. If successful return true, if not
     *  return false. If there is not an assigned scheduler it will return false. */
    public boolean addIfFeasible() {
	if ((currentScheduler == null) ||
	    (!currentScheduler.isFeasible(this, getReleaseParameters()))) return false;
	else return addToFeasibility();
    }

    /** Inform the scheduler and cooperating facilities that the resource
     *  demands (as expressed in the associated instances of
     *  <code>SchedulingParameters, ReleaseParameters, MemoryParameters</code>
     *  and <code>ProcessingGroupParameters</code>) of this instance of
     *  <code>Schedulable</code> will be considered in the feasibility analysis
     *  of the associated <code>Scheduler</code> until further notice. Whether
     *  the resulting system is feasible or not, the addition is completed.
     */
    public boolean addToFeasibility() {
	if(currentScheduler != null) {
	    currentScheduler.addToFeasibility(this);
	    return currentScheduler.isFeasible();
	} else return false;
    }
    
    /** This will throw a <code>ClassCastException</code> if the current thread
     *  is not a <code>RealtimeThread</code>.
     */
    public static RealtimeThread currentRealtimeThread()
	throws ClassCastException {
	return (RealtimeThread)currentThread();
    }

    /** Stop unblocking <code>waitForNextPeriod()</code> for a periodic schedulable
     *  object. If this does not have a type of <code>PeriodicParameters</code> as
     *  its <code>ReleaseParameters</code>, nothing happens.
     */
    public void deschedulePeriodic() {
	blocked = true;
    }
    /** Return the instance of <code>MemoryArea</code> which is the current memory
     *  area for this.
     */
    public MemoryArea getCurrentMemoryArea() {
	return mem;
    }

    /** Memory area stacks include inherited stacks from parent threads. The initial
     *  memory area for the current <code>RealtimeThread</code> is the memory area
     *  given as a parameter to the constructor. This method returns the position in
     *  the memory area stack of that initial memory area.
     */
    public /*static*/ int getInitialMemoryAreaIndex() {
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

    /** Get the size of the stack of <code>MemoryArea</code> instances to which
     *  this <code>RealtimeThread</code> has access.
     */
    public /*static*/ int getMemoryAreaStackDepth() {
	MemAreaStack temp = memAreaStack;
	int count = 0;
	while (temp != null) {
	    count++;
	    temp = temp.next;
	}
	return count;
    }

    /** Return a reference to the <code>MemoryParameters</code> object. */
    public MemoryParameters getMemoryParameters() {
	return memoryParameters;
    }
    
    /** Get the instance of <code>MemoryArea</code> in the memory area stack
     *  at the index given. If the given index does not exist in the memory
     *  area scope stack then null is returned.
     */
    public /*static*/ MemoryArea getOuterMemoryArea(int index) {
	MemAreaStack temp = memAreaStack;
	for (int i = 0; i < index; i++)
	    if (temp != null) temp = temp.next;
	    else return null;
	if (temp != null) return temp.entry;
	else return null;
    }

    /** Return a reference to the <code>ProcessingGroupParameters</code> object. */
    public ProcessingGroupParameters getProcessingGroupParameters() {
	return processingGroupParameters;
    }

    /** Returns a reference to the <code>ReleaseParameters</code> object. */
    public ReleaseParameters getReleaseParameters() {
	return releaseParameters;
    }

    /** Get the scheduler for this thread. */
    public Scheduler getScheduler() {
	return currentScheduler;
    }

    /** Return a reference to the <code>SchedulingParameters</code> object. */
    public SchedulingParameters getSchedulingParameters() {
	return schedulingParameters;
    }

    /** Throw the generic <code>AsynchronouslyInterruptedException</code> at this. */
    public void interrupt() {
	// TODO

	super.interrupt();
    }

    /** Inform the scheduler and cooperating facilities that the resource demands, as
     *  expressed in the associated instances of <code>SchedulingParameters,
     *  ReleaseParameters, MemoryParameters</code> and <code>ProcessingGroupParameters</code>,
     *  of this instance of <code>Schedulable</code> should no longer be considered
     *  in the feasibility analysis of the associated <code>Scheduler</code>.
     *  Whether the resulting system is feasible or not, the subtraction is completed.
     */
    public void removeFromFeasibility() {
	if (currentScheduler != null)
	    currentScheduler.removeFromFeasibility(this);
    }

    /** Begin unblocking <code>waitForNextPeriod()</code> for a periodic thread.
     *  Typically used when a periodic schedulable object is in an overrun condition.
     *  The scheduler should recompute the schedule and perform admission control.
     *  if this does not have a type of <code>PeriodicParameters</code> as its
     *  <code>ReleaseParameters</code>, nothing happens.
     */
    public void schedulePeriodic() {
	blocked = false;
    }

    /** Returns true if, after considering the values of the parameters, the task set
     *  would still be feasible. In this case the values of the parameters are changed.
     *  Returns false if, after considering the values of the parameters, the task set
     *  would not be feasible. In this case the values of the parameters are not changed.
     */
    public boolean setIfFeasible(ReleaseParameters release, MemoryParameters memory) {
	return setIfFeasible(release, memory, processingGroupParameters);
    }

    /** Returns true if, after considering the values of the parameters, the task set
     *  would still be feasible. In this case the values of the parameters are changed.
     *  Returns false if, after considering the values of the parameters, the task set
     *  would not be feasible. In this case the values of the parameters are not changed.
     */
    public boolean setIfFeasible(ReleaseParameters release, MemoryParameters memory,
				 ProcessingGroupParameters group) {
	if (currentScheduler == null) return false;
	else return currentScheduler.setIfFeasible(this, release, memory, group);
    }

    /** Returns true if, after considering the values of the parameters, the task set
     *  would still be feasible. In this case the values of the parameters are changed.
     *  Returns false if, after considering the values of the parameters, the task set
     *  would not be feasible. In this case the values of the parameters are not changed.
     */
    public boolean setIfFeasible(ReleaseParameters release, ProcessingGroupParameters group) {
	return setIfFeasible(release, memoryParameters, group);
    }

    /** Set the reference to the <code>MemoryParameters</code> object. */
    public void setMemoryParameters(MemoryParameters parameters)
	throws IllegalThreadStateException {
	this.memoryParameters = memoryParameters;
    }
    
    /** Returns true if, after considering the values of the parameters, the task set
     *  would still be feasible. In this case the values of the parameters are changed.
     *  Returns false if, after considering the values of the parameters, the task set
     *  would not be feasible. In this case the values of the parameters are not changed.
     */
    public boolean setMemoryParametersIfFeasible(MemoryParameters memParam) {
	return setIfFeasible(releaseParameters, memParam, processingGroupParameters);
    }

    /** Set the reference to the <code>ProcessingGroupParameters</code> object. */
    public void setProcessingGroupParameters(ProcessingGroupParameters parameters) {
	this.processingGroupParameters = parameters;
    }

    /** Returns true if, after considering the values of the parameters, the task set
     *  would still be feasible. In this case the values of the parameters are changed.
     *  Returns false if, after considering the values of the parameters, the task set
     *  would not be feasible. In this case the values of the parameters are not changed.
     */
    public boolean setProcessingGroupParametersIfFeasible(ProcessingGroupParameters groupParameters) {
	return setIfFeasible(releaseParameters, memoryParameters, groupParameters);
    }

    /** Set the reference to the <code>ReleaseParameteres</code> object. */
    public void setReleaseParameters(ReleaseParameters parameters)
	throws IllegalThreadStateException {
	this.releaseParameters = releaseParameters;
    }
    
    /** Returns true if, after considering the values of the parameters, the task set
     *  would still be feasible. In this case the values of the parameters are changed.
     *  Returns false if, after considering the values of the parameters, the task set
     *  would not be feasible. In this case the values of the parameters are not changed.
     */
    public boolean setReleaseParametersIfFeasible(ReleaseParameters release) {
	return setIfFeasible(release, memoryParameters, processingGroupParameters);
    }

    /** Set the scheduler. This is a reference to the scheduler that will manage the
     *  execution of this thread.
     */
    public void setScheduler(Scheduler scheduler)
	throws IllegalThreadStateException {
	if (currentScheduler != null) currentScheduler.removeFromFeasibility(this);
	currentScheduler = scheduler;
    }
    
    /** Set the scheduler. This is a reference to the scheduler that will manage the
     *  execution of this thread.
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

    /** Set the reference to the <code>SchedulingParameters</code> object. */
    public void setSchedulingParameters(SchedulingParameters scheduling)
	throws IllegalThreadStateException {
	this.schedulingParameters = scheduling;
    }
    
    /** Returns true if, after considering the values of the parameters, the task set
     *  would still be feasible. In this case the values of the parameters are changed.
     *  Returns false if, after considering the values of the parameters, the task set
     *  would not be feasible. In this case the values of the parameters are not changed.
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
     */
    public static void sleep(HighResolutionTime time)
	throws InterruptedException {
	sleep(Clock.getRealtimeClock(), time);
    }

    /** Checks if the instance of <code>RealtimeThread</code> is startable and
     *  starts it if it is.
     */
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
	// RealtimeThread points to the current scope.
	// Note that there is no exit()... this is actually legal.
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

    /** */
    public MemoryArea getMemoryArea() {
	if (mem == null) {
	    mem = (original = HeapMemory.instance()).shadow;
	}
	return original;
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
