// Scheduler.java, created by cata
// Copyright (C) 2001 Catalin Francu <cata@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.

package javax.realtime;
import java.util.HashSet;

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
	    setDefaultScheduler(RoundRobinScheduler.instance());
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
    protected final native void contextSwitch();

    /** Turns on/off timed thread switching */
    protected final native void setTimer(boolean state);

//  Notes for reflection optimization... - WSB
//      private static final String[] s = new String[] {
//  	"method"
//      };
//      public boolean overridden() {
//  	Method[] m = getClass().getMethods();
//  	for (int i=0; i<m.length; i++) {
//  	    for (int j=0; j<s.length; j++) {
//  		if ((m[i].getDeclaringClass()==Foo.class)&&
//  		    (m[i].getName().equals(s[j]))) {
//  		    return false;
//  		}
//  	    }
//  	}
//  	return true;
//      }
    
    protected void handleLock(long threadID) {
    }

    protected void handleTryLock(long threadID) {
    }

    protected void handleSignal(long threadID) {
    }

    protected void handleWait(long threadID) {
    }

    /** Print out the status of the scheduler */
    public final static void print() {
	Scheduler sched = RealtimeThread.currentRealtimeThread().getScheduler();
	if (sched == null) {
	    sched = Scheduler.getDefaultScheduler();
	}
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
	Scheduler sched;
	if (rt != null) { 
	    sched = rt.getScheduler();
	} else {
	    sched = getDefaultScheduler();
	}
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
	Scheduler sched;
	if (rt != null) { 
	    sched = rt.getScheduler();
	} else {
	    sched = getDefaultScheduler();
	}
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
