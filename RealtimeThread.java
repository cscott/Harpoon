// RealtimeThread.java, created by wbeebee
// Copyright (C) 2001 Wes Beebee <wbeebee@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package javax.realtime; 

/** <code>RealtimeThread</code>
 * 
 * @author Wes Beebee <<a href="mailto:wbeebee@mit.edu">wbeebee@mit.edu</a>>
 */

public class RealtimeThread extends Thread implements Schedulable {

    public static Scheduler currentScheduler = null;

    /** Contains the cactus stack of previous memoryAreas... */

    MemAreaStack memAreaStack;

    /** The top of the stack for this RealtimeThread 
     */
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

    /** Specifies whether the initialization code has finished setting up RTJ. 
     */
    
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

    public RealtimeThread() {  // All RealtimeThreads default to heap.
	this((MemoryArea)null);
    }

    public RealtimeThread(SchedulingParameters scheduling) {
	this();
	schedulingParameters = scheduling;
    }

    public RealtimeThread(SchedulingParameters scheduling,
			  ReleaseParameters release) {
	this();
	schedulingParameters = scheduling;
	releaseParameters = release;
    }

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

    // -------------------------------
    // Other constructors not in specs
    // -------------------------------

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

    // ------------------
    // methods from specs
    // ------------------

    public boolean addIfFeasible() {
	if (currentScheduler == null) return false;
	currentScheduler.addToFeasibility(this);
	if (currentScheduler.isFeasible()) return true;
	else {
	    currentScheduler.removeFromFeasibility(this);
	    return false;
	}
    }

    public boolean addToFeasibility() {
	if(currentScheduler != null) {
	    currentScheduler.addToFeasibility(this);
	    return currentScheduler.isFeasible();
	} else {
	    return false;
	}
    }
    
    public static RealtimeThread currentRealtimeThread()
	throws ClassCastException {
	return (RealtimeThread)currentThread();
    }
    
    public void deschedulePeriodic() {
	blocked = true;
    }

    public MemoryArea getCurrentMemoryArea() {
	return mem;
    }

    // I don't get why this method should be static
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

    // I don't get why this method should be static
    public /*static*/ int getMemoryAreaStackDepth() {
	MemAreaStack temp = memAreaStack;
	int count = 0;
	while (temp != null) {
	    count++;
	    temp = temp.next;
	}
	return count;
    }

    public MemoryParameters getMemoryParameters() {
	return memoryParameters;
    }
    
    // I don't get why this method should be static
    public /*static*/ MemoryArea getOuterMemoryArea(int index) {
	MemAreaStack temp = memAreaStack;
	for (int i = 0; i < index; i++)
	    if (temp != null) temp = temp.next;
	    else return null;
	if (temp != null) return temp.entry;
	else return null;
    }

    public ProcessingGroupParameters getProcessingGroupParameters() {
	return processingGroupParameters;
    }
    
    public ReleaseParameters getReleaseParameters() {
	return releaseParameters;
    }
    
    public Scheduler getScheduler() {
	return currentScheduler;
    }
    
    public SchedulingParameters getSchedulingParameters() {
	return schedulingParameters;
    }
    
    public void interrupt() {
	// TODO
    }

    public void removeFromFeasibility() {
	if (currentScheduler != null)
	    currentScheduler.removeFromFeasibility(this);
    }
    
    public void schedulePeriodic() {
	blocked = false;
    }

    public boolean setIfFeasible(ReleaseParameters release, MemoryParameters memory) {
	return setIfFeasible(release, memory, processingGroupParameters);
    }

    public boolean setIfFeasible(ReleaseParameters release, MemoryParameters memory,
				 ProcessingGroupParameters group) {
	if (currentScheduler == null) return false;
	ReleaseParameters oldReleaseParameters = releaseParameters;
	MemoryParameters oldMemoryParameters = memoryParameters;
	ProcessingGroupParameters oldProcessingGroupParameters = processingGroupParameters;

	setReleaseParameters(release);
	setMemoryParameters(memory);
	setProcessingGroupParameters(group);
	if (currentScheduler.isFeasible()) return true;
	else {
	    setReleaseParameters(oldReleaseParameters);
	    setMemoryParameters(oldMemoryParameters);
	    setProcessingGroupParameters(oldProcessingGroupParameters);
	    return false;
	}
    }

    public boolean setIfFeasible(ReleaseParameters release, ProcessingGroupParameters group) {
	return setIfFeasible(release, memoryParameters, group);
    }

    public void setMemoryParameters(MemoryParameters parameters)
	throws IllegalThreadStateException {
	this.memoryParameters = memoryParameters;
    }
    
    public boolean setMemoryParametersIfFeasible(MemoryParameters memParam) {
	return setIfFeasible(releaseParameters, memParam, processingGroupParameters);
    }

    public void setProcessingGroupParameters(ProcessingGroupParameters parameters) {
	this.processingGroupParameters = parameters;
    }

    public boolean setProcessingGroupParametersIfFeasible(ProcessingGroupParameters groupParameters) {
	return setIfFeasible(releaseParameters, memoryParameters, groupParameters);
    }

    public void setReleaseParameters(ReleaseParameters parameters)
	throws IllegalThreadStateException {
	this.releaseParameters = releaseParameters;
    }
    
    public boolean setReleaseParametersIfFeasible(ReleaseParameters release) {
	return setIfFeasible(release, memoryParameters, processingGroupParameters);
    }

    public void setScheduler(Scheduler scheduler)
	throws IllegalThreadStateException {
	currentScheduler = scheduler;
	// TODO
    }
    
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

    public void setSchedulingParameters(SchedulingParameters scheduling)
	throws IllegalThreadStateException {
	this.schedulingParameters = scheduling;
    }
    
    public boolean setSchedulingParametersIfFeasible(SchedulingParameters scheduling) {
	if (currentScheduler == null) return false;
	SchedulingParameters oldSchedulingParameters = schedulingParameters;
	schedulingParameters = scheduling;
	if (currentScheduler.isFeasible()) return true;
	else {
	    schedulingParameters = oldSchedulingParameters;
	    return false;
	}
    }

    public static void sleep(Clock clock, HighResolutionTime time)
	throws InterruptedException {
	if (time instanceof AbsoluteTime) {
	    RelativeTime temp = ((AbsoluteTime)time).subtract(clock.getTime());
	    Thread.sleep(temp.getMilliseconds(), temp.getNanoseconds());
	}
	else 
	    Thread.sleep(time.getMilliseconds(), time.getNanoseconds());
    }

    public static void sleep(HighResolutionTime time)
	throws InterruptedException {
	sleep(Clock.getRealtimeClock(), time);
    }

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

    public boolean waitForNextPeriod() throws IllegalThreadStateException {
	if ((releaseParameters instanceof PeriodicParameters) && (!blocked))
	    PriorityScheduler.getScheduler().waitForNextPeriod(this);
	return false;
    }

    // Not in specs, but needed for methods in specs
    public void bindSchedulable() {
	releaseParameters.bindSchedulable(this);
	processingGroupParameters.bindSchedulable(this);
    }

    public void unbindSchedulable() {
	releaseParameters.unbindSchedulable(this);
	processingGroupParameters.unbindSchedulable(this);
    }

    
    // --------------------
    // Methods not in specs
    // --------------------

    public static int activeCount() {
	return Thread.activeCount();
    }

    /** */    

    public static Thread currentThread() {
	return Thread.currentThread();
    }

    /** */
    
    public static int enumerate(Thread tarray[]) {
	return Thread.enumerate(tarray);
    }
    
    /** */
    
    public static boolean interrupted() {
	return Thread.interrupted();
    }
    
    /** */

    public static void yield() {
	Thread.yield();
    }
    
    /** */

    public static void sleep(long millis) throws InterruptedException {
	Thread.sleep(millis);
    }
    
    /** */

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
    
    // The following methods are part of the RTJ spec.

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
