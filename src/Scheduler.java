// Scheduler.java, created by cata
// Copyright (C) 2001 Catalin Francu <cata@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.

package javax.realtime;
import java.lang.reflect.Method;

/** An instance of <code>Scheduler</code> manages the execution of 
 *  schedulable objects and may implement a feasibility algorithm. The
 *  feasibility algorithm may determine if the known set of schedulable
 *  objects, given their particular exection ordering (or priority
 *  assignment), is a feasible schedule. Subclasses of <code>Scheduler</code>
 *  are used for alternative scheduling policies and should define an
 *  <code>instance()</code> class method to return the default
 *  instance of the subclass. The name of the subclass should be
 *  descriptive of the policy, allowing applications to deduce the
 *  policy available for the scheduler obtained via
 *  <code>getDefaultScheduler()</code> (e.g., <code>EDFScheduler</code>).
 */
public abstract class Scheduler {
    protected static Scheduler defaultScheduler = null;
    private static VTMemory vt = null;
    
    /** Create an instance of <code>Scheduler</code>. */
    protected Scheduler() {
	addToRootSet();
	if (vt == null) {
	    vt = new VTMemory();
	}
    }

    /** Inform the scheduler and cooperating facilities that the resource
     *  demands (as expressed in the associated instances of
     *  <code>SchedulingParameters, ReleaseParameters, MemoryParameters</code>
     *  and <code>ProcessingGroupParameters</code>) of this instance of
     *  <code>Schedulable</code> will be considered in the feasibility analysis
     *  of the associated <code>Scheduler</code> until further notice. Whether
     *  the resulting system is feasible or not, the addition is completed.
     *
     *  @param schedulable A reference to the given instance of <code>Schedulable</code>.
     *  @return True, if the addition was successful. False, if not.
     */
    protected abstract void addToFeasibility(Schedulable schedulable);

    /** Trigger the execution of a schedulable object (like an
     *  <code>AsyncEventHandler</code>.
     *
     *  @param schedulable The Schedulable object to make active.
     */
    public abstract void fireSchedulable(Schedulable schedulable);

    /** Gets a reference to the default scheduler.
     *  In the RTJ spec, this is the PriorityScheduler, but we'll set it to whatever
     *  we wish to focus on at the moment, since this is a hot research target.
     *
     *  Currently, there's a PriorityScheduler (for compliance), and a 
     *  RoundRobinScheduler (for debugging).  Conceivably, one can put Schedulers 
     *  in a containment hierarchy, and getDefaultScheduler() would
     *  return the root.
     *
     *  @return A reference to the default scheduler.
     */
    public static Scheduler getDefaultScheduler() {
	if (defaultScheduler == null) {
//	    setDefaultScheduler(EDFScheduler.instance());
	    setDefaultScheduler(NativeScheduler.instance());
	    return getDefaultScheduler();
	}
	return defaultScheduler;
    }

    /** Gets a string representing the policy of <code>this</code>.
     *
     *  @return A <code>java.lang.String</code> object which is the name
     *          of the scheduling polixy used by <code>this</code>.
     */
    public abstract String getPolicyName();

    /** Queries the system about the feasibility of the set of scheduling
     *  and release characteristics currently being considered.
     */
    public abstract boolean isFeasible();
    protected abstract boolean isFeasible(Schedulable s, ReleaseParameters rp);

    /** Inform the scheduler and cooperating facilities that the resource
     *  demands (as expressed in the associated instances of
     *  <code>SchedulingParameters, ReleaseParameters, MemoryParameters</code>
     *  and <code>ProcessingGroupParameters</code>) of this instance of
     *  <code>Schedulable</code> should no longer be considered in the
     *  feasibility analysis of the associated <code>Scheduler</code>
     *  until further notice. Whether the resulting system is feasible
     *  or not, the subtraction is completed.
     *
     *  @return True, if the removal was successful. False, if the removal
     *          was unsuccessful.
     */
    protected abstract void removeFromFeasibility(Schedulable schedulable);

    /** Set the default scheduler. This is the scheduler given to instances
     *  of <code>RealtimeThread</code> when they are constructed. The default
     *  scheduler is set to the required <code>PriorityScheduler</code> at
     *  startup.
     *
     *  @param scheduler The <code>Scheduler</code> that becomes the default
     *                   scheduler assigned to new threads. If null nothing happens.
     */
    public static void setDefaultScheduler(Scheduler scheduler) {
	defaultScheduler = scheduler;
    }

    /** This method appears in many classes in the RTSJ and with various parameters.
     *  The parameters are either new scheduling characteristics for an instance of
     *  <code>Schedulable</code> or an instance of <code>Schedulable</code>. The
     *  method first performs a feasibility analysis using the new scheduling
     *  characteristics of either <code>this</code> or the given instance of
     *  <code>Schedulable</code>. If the resulting system is feasible the method
     *  replaces the current sheduling characteristics, of either <code>this</code>
     *  or the given instance of <code>Schedulable</code> as appropriate, with the
     *  new scheduling characteristics.
     *
     *  @param schedulable The instance of <code>Schedulable</code> to which the
     *                     parameters will be assigned.
     *  @param release The proposed release parameters.
     *  @param memory The proposed memory parameters.
     *  @return True, if the resulting system is feasible and the changes are made.
     *          False, if the resulting system is not feasible and no changes are made.
     */
    public abstract boolean setIfFeasible(Schedulable schedulable,
					  ReleaseParameters release,
					  MemoryParameters memory);

    /** This method appears in many classes in the RTSJ and with various parameters.
     *  The parameters are either new scheduling characteristics for an instance of
     *  <code>Schedulable</code> or an instance of <code>Schedulable</code>. The
     *  method first performs a feasibility analysis using the new scheduling
     *  characteristics of either <code>this</code> or the given instance of
     *  <code>Schedulable</code>. If the resulting system is feasible the method
     *  replaces the current sheduling characteristics, of either <code>this</code>
     *  or the given instance of <code>Schedulable</code> as appropriate, with the
     *  new scheduling characteristics.
     *
     *  @param schedulable The instance of <code>Schedulable</code> to which the
     *                     parameters will be assigned.
     *  @param release The proposed release parameters.
     *  @param memory The proposed memory parameters.
     *  @param group The proposed processing group parameters.
     *  @return True, if the resulting system is feasible and the changes are made.
     *          False, if the resulting system is not feasible and no changes are made.
     */

    public abstract boolean setIfFeasible(Schedulable schedulable,
					  ReleaseParameters release,
					  MemoryParameters memory,
					  ProcessingGroupParameters group);

    /** Chooses a thread to run */
    protected abstract long chooseThread(long currentTime);

    /** Adds a thread to the thread list */
    protected abstract void addThread(RealtimeThread thread);

    /** Removes a thread from the thread list */
    protected abstract void removeThread(RealtimeThread thread);

    /** Used for adding a C thread that doesn't have any Java counterpart.
     *  The convention is that the threadID for C threads is negative.
     */
    protected abstract void addThread(long threadID);

    /** Used for removing a C thread that doesn't have any Java counterpart. 
     *  The convention is that the threadID for C threads is negative.
     */
    protected abstract void removeThread(long threadID);

    /** Stop running <code>threadID</code> until enableThread - 
     *  used in the lock implementation to wait on a lock.
     */
    protected abstract void disableThread(long threadID);

    /** Enable <code>threadID</code>, allowing it run again - used in notify */
    protected abstract void enableThread(long threadID);

    /** Cause the thread to block until the next period */
    protected abstract void waitForNextPeriod(RealtimeThread rt);

    /** Print out the scheduler without allocating from the heap. */
    public abstract void printNoAlloc();

    /** Switch every <code>microsecs</code> microseconds */
    protected final native void setQuanta(long microsecs);

    /** Force a context switch at the earliest legal time */
    protected final void contextSwitch() {
	setQuanta(1);
    }

    /** Turns on/off timed thread switching */
    protected final native void setTimer(boolean state);

    /** Reserve <code>compute</code> CPU every <code>period</code>, 
     *  starting at time <code>time</code>.
     *
     *  This function is only available on the TimeSys operating system
     *  with the Real-Time CPU development kit.
     *
     *  @param compute The compute time for each period in nanoseconds
     *  @param period The length of each period in nanoseconds
     *  @param begin The absolute time to start the reservation in nanoseconds 
     *               (0 indicates as soon as possible)
     *
     *  @return Whether admission control was successful
     */
    protected final native boolean reserveCPU(long compute, long period, long begin);

    /** Reserve Network 
     *
     *  This function is only available on the TimeSys operating system
     *  with the Real-Time NET development kit.
     */
    protected final native boolean reserveNET(long bytes, long transfer, long period, long begin);

    /** Sleep the process for the given number of microseconds.
     */
    protected final native void sleep(long microsecs);

    /** The amount of CPU time since the start of the process
     *  (for estimating work done by threads in the presence of
     *   system load).
     */
    protected final native int clock();

    /** This is the list of handlers that can be registered with a new scheduler to
	intercept events.  When these run out, register multiplexer events and send messages. */
    public static final long HANDLE_MUTEX_INIT = 1;
    public static final long HANDLE_MUTEX_DESTROY = 2;
    public static final long HANDLE_MUTEX_TRYLOCK = 4;
    public static final long HANDLE_MUTEX_LOCK = 8;
    public static final long HANDLE_MUTEX_UNLOCK = 16;
    public static final long HANDLE_COND_INIT = 32;
    public static final long HANDLE_COND_DESTROY = 64;
    public static final long HANDLE_COND_BROADCAST = 128;
    public static final long HANDLE_COND_SIGNAL = 256;
    public static final long HANDLE_COND_TIMEDWAIT = 512;
    public static final long HANDLE_COND_WAIT = 1024;
    public static final long HANDLE_ALL = 2047;

    private static final String[] handlerNames = new String[] { /* This must match the order above */
	"handle_mutex_init", "handle_mutex_destroy", "handle_mutex_trylock", "handle_mutex_lock", "handle_mutex_unlock",
	"handle_cond_init", "handle_cond_destroy", "handle_cond_broadcast", "handle_cond_signal", "handle_cond_timedwait", 
	"handle_cond_wait"
    };

    private long handlerMask = -1;

    /** By default, this method uses reflection to determine what the handler mask should be.
     *  However, a scheduler can override this if it chooses to not handle an event even if the
     *  appropriate handler method is overridden. */
    public long handler_mask() {
	if (handlerMask != -1) {
	    return handlerMask;
	} else {
	    Method[] m = getClass().getMethods();
	    long mask = HANDLE_ALL;
	    for (int i=0; i<m.length; i++) {
		for (int j=0; j<handlerNames.length; j++) {
		    if ((m[i].getDeclaringClass()==Scheduler.class)&&
			(m[i].getName().equals(handlerNames[j]))) {
			mask-=(int)Math.pow(2.0, (double)j);
		    }
		}
	    }
	    return handlerMask = mask;
	}
    }

    public void handle_mutex_init() { throw new Error("Should never be called!"); }
    public void handle_mutex_destroy() { throw new Error("Should never be called!"); }
    public void handle_mutex_trylock() { throw new Error("Should never be called!"); }
    public void handle_mutex_lock() { throw new Error("Should never be called!"); } 
    public void handle_mutex_unlock() { throw new Error("Should never be called!"); }
    public void handle_cond_init() { throw new Error("Should never be called!"); }
    public void handle_cond_destroy() { throw new Error("Should never be called!"); }
    public void handle_cond_broadcast() { throw new Error("Should never be called!"); }
    public void handle_cond_signal() { throw new Error("Should never be called!"); }
    public void handle_cond_timedwait() { throw new Error("Should never be called!"); }
    public void handle_cond_wait() { throw new Error("Should never be called!"); }

    public static void jhandle_mutex_init() { 
	getScheduler().handle_mutex_init(); 
    }
    public static void jhandle_mutex_destroy() { 
	getScheduler().handle_mutex_destroy(); 
    }
    public static void jhandle_mutex_trylock() {
	getScheduler().handle_mutex_trylock();
    }
    public static void jhandle_mutex_lock() { 
	getScheduler().handle_mutex_lock(); 
    }
    public static void jhandle_mutex_unlock() { 
	getScheduler().handle_mutex_unlock(); 
    }
    public static void jhandle_cond_init() { 
	getScheduler().handle_cond_init();
    }
    public static void jhandle_cond_destroy() {
	getScheduler().handle_cond_destroy();
    }
    public static void jhandle_cond_broadcast() {
	getScheduler().handle_cond_broadcast();
    }
    public static void jhandle_cond_signal() {
	getScheduler().handle_cond_signal();
    }
    public static void jhandle_cond_timedwait() {
	getScheduler().handle_cond_timedwait();
    }
    public static void jhandle_cond_wait() {
	getScheduler().handle_cond_wait();
    }

    public static Scheduler getScheduler() {
	return getScheduler(RealtimeThread.currentRealtimeThread());
    }

    public static Scheduler getScheduler(RealtimeThread rt) {
	Scheduler sched = null;
	if (rt == null) {
	    sched = Scheduler.getDefaultScheduler();
	} 
	if (sched == null) {
	    sched = rt.getScheduler();
	}
	if (sched == null) {
	    sched = Scheduler.getDefaultScheduler();
	}
	return sched;
    }

    /** Print out the status of the scheduler */
    public final static void print() {
	Scheduler sched = getScheduler();
	NoHeapRealtimeThread.print("\n");
	NoHeapRealtimeThread.print(sched.getPolicyName());
	NoHeapRealtimeThread.print(": ");
	sched.printNoAlloc();
    }

   /** Run the runnable in an atomic section */
    public final static void atomic(Runnable run) {
	int state = beginAtomic();
	run.run();
	endAtomic(state);
    }

    private final void addToRootSet() {
	if (Math.sqrt(4)==0) {
	    jhandle_mutex_init();
	    jhandle_mutex_destroy();
	    jhandle_mutex_trylock();
	    jhandle_mutex_lock();
	    jhandle_mutex_unlock();
	    jhandle_cond_init();
	    jhandle_cond_destroy();
	    jhandle_cond_broadcast();
	    jhandle_cond_signal();
	    jhandle_cond_timedwait();
	    jhandle_cond_wait();
	    handler_mask();
	    jDisableThread(null, 0);
	    jEnableThread(null, 0);
	    jChooseThread(0);
	    jRemoveCThread(0);
	    jAddCThread(0);
	    jNumThreads();
	    (new RealtimeThread()).schedule();
	    (new RealtimeThread()).unschedule();
	    new NoSuchMethodException();
	    new NoSuchMethodException("");
	    print();
	}
    }

    final void addThreadToLists(final RealtimeThread thread) {
	totalThreads++;
//	MemoryArea.startMem(vt);
	int state = beginAtomic();
	addToFeasibility(thread);
	addThreadInC(thread, thread.getUID());
	addThread(thread);
	endAtomic(state);
//	MemoryArea.stopMem();
    }

    final void removeThreadFromLists(final RealtimeThread thread) {
	totalThreads--;
//	MemoryArea.startMem(vt);
	int state = beginAtomic();
	removeThread(thread);
	removeThreadInC(thread);
	removeFromFeasibility(thread);
	endAtomic(state);
//	MemoryArea.stopMem();
    }

    private final native void addThreadInC(Schedulable t, long threadID);
    private final native long removeThreadInC(Schedulable t);
    private final native static int beginAtomic();
    private final native static void endAtomic(int state);

    final static void jDisableThread(final RealtimeThread rt, 
				     final long threadID) {
	disabledThreads++;
//	MemoryArea.startMem(vt);
	Scheduler sched = getScheduler(rt);
	if (sched != null) {
	    sched.disableThread(threadID);
	} else {
	    //	MemoryArea.stopMem();
	    throw new RuntimeException("\nNo scheduler!!!");
	}
//	MemoryArea.stopMem();
    }
    
    final static void jEnableThread(final RealtimeThread rt, 
				    final long threadID) {
	disabledThreads--;
	// MemoryArea.startMem(vt);
	Scheduler sched = getScheduler(rt);
	if (sched != null) {
	    sched.enableThread(threadID);
	} else {
	    // MemoryArea.stopMem();
	    throw new RuntimeException("\nNo scheduler!!!");
	}
	// MemoryArea.stopMem();
    }

    final static void jAddCThread(final long threadID) {
	totalThreads++;
	getDefaultScheduler().addThread(threadID);
    }

    final static void jRemoveCThread(final long threadID) {
	totalThreads--;
	getDefaultScheduler().removeThread(threadID);
    }

    private static long totalThreads = 0;
    private static long disabledThreads = 0;

    /** Return the total number of active threads in the system. */
    final static long jNumThreads() { 
	return totalThreads-disabledThreads;
    }

    final protected long jChooseThread(final long currentTime) {
	// MemoryArea.startMem(vt);
	long temp = chooseThread(currentTime);
	// MemoryArea.stopMem();
	return temp;
    }
  
}
