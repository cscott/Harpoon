// PriorityScheduler.java, created by cata
// Copyright (C) 2001 Catalin Francu <cata@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package javax.realtime;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Iterator;

/** Class which represents the required (by RTSJ) priority-based scheduler.
 *  The default instance is the required priority scheduler which does
 *  fixed priority, preemptive scheduling.
 */
public class PriorityScheduler extends Scheduler {
    // Real-time thread priorities
    /** The maximum priority value used by the implementation. */
    static final int MAX_PRIORITY = 38;
    /** The minimum priority value used by the implementation. */
    static final int MIN_PRIORITY = 11;
    static final int NORM_PRIORITY =
	(MAX_PRIORITY - MIN_PRIORITY) / 3 + MIN_PRIORITY;
    
    static HashSet allThreads = null;
    static HashSet disabledThreads = null;
    // The runtime constraints for the threads we're maintaining
    static ThreadConstraints thread[] =	null;
    public static RelativeTime defaultQuanta = null;
    static PeriodicParameters mainThreadParameters = null;
    static PeriodicParameters noRTParameters = null;
    
    // The first threadID that we can allocate
    long nextThreadID = 0;
    // The number of threads we are maintaining
    int nThreads = 0;
    
    // The number of missed deadlines and thenumber of times that the
    // scheduler was invoked
    public static long missed = 0;
    public static long invocations = 0;
    public static long runningTimeMicros = 0;
    
    // The thread that chooseThread selected upon the previous invocation,
    // and how long we've allowed that thread to run
    long runningThread = 0;
    RelativeTime runningTime = null;
    static PriorityScheduler instance = null;

    /** Constructor for the required scheduler. */
    protected PriorityScheduler() {
	super();
	if (thread == null) {
	    thread = new ThreadConstraints[10];
	    allThreads = new HashSet();
	    disabledThreads = new HashSet();
	    defaultQuanta = new RelativeTime(2, 0);
	    runningTime = new RelativeTime(0, 0);
	    mainThreadParameters =
		new PeriodicParameters(null, new RelativeTime(5, 0),
				       new RelativeTime(1, 0), null, null, null);
	    noRTParameters =
		new PeriodicParameters(null, new RelativeTime(50, 0),
				       new RelativeTime(1, 0), null, null, null);
	}
    }

    /** Return a reference to an instance of <code>PriorityScheduler</code>.
     *
     *  @return A reference to an instance of <code>PriorityScheduler</code>.
     */
    public static PriorityScheduler instance() {
	if (instance == null) 
	    ImmortalMemory.instance().enter(new Runnable() {
		public void run() {
		    instance = new PriorityScheduler();
		}
	    });
	return instance;
    }
    
    /** Inform the scheduler and cooperating facilities that the resource demands,
     *  as expressed in associated instances of <code>SchedulingParameters,
     *  ReleaseParameters, MemoryParameters</code> and <code>ProcessingGroupParameters</code>,
     *  of this instance of <code>Schedulable</code> will be considered in the
     *  feasibility analysis of the associated <code>Scheduler</code> until
     *  further notice. Whether the resulting system is feasible or not, the addition
     *  is completed.
     *
     *  @param schedulable The instance of <code>Schedulable</code> for which the
     *                     changes are proposed.
     */
    protected void addToFeasibility(final Schedulable schedulable) {
	allThreads.add(schedulable);
	thread[nThreads] = new ThreadConstraints();
	thread[nThreads].threadID = schedulable.getUID();
	thread[nThreads].schedulable = schedulable;
	thread[nThreads].beginPeriod = null;
	nThreads++;
    }

    protected native void addThreadInC(Schedulable t, long threadID);

    /** Trigger the execution of a schedulable object (like
     *  an instance of <code>AsyncEventHandler</code>).
     *
     *  @param schedulable The <code>Schedulable</code> object to make active.
     */
    public void fireSchedulable(Schedulable schedulable) {
	schedulable.run();
    }

    /** Gets the maximum priority available for a thread managed by this scheduler.
     *
     *  @return The value of the maximum priority.
     */
    public int getMaxPriority() {
	return MAX_PRIORITY;
    }

    /** Gets the maximum priority of the given instance of
     *  <code>java.lang.Thread</code>. If the given thread is scheduled by the
     *  required <code>PriorityScheduler</code> the maximum priority of the
     *  <code>PriorityScheduler</code> is returned; otherwise
     *  <code>Thread.MAX_PRIORITY</code> is returned.
     *
     *  @param thread An instance of <code>java.lang.Thread</code>. If null,
     *                the maximum priority of the required
     *                <code>PriorityScheduler</code> is returned.
     *  @return The maximum priority of the given instance of
     *          <code>java.lang.Thread</code>.
     */
    public static int getMaxPriority(Thread thread) {
	return (allThreads.contains(thread)) ?
	    MAX_PRIORITY : Thread.MAX_PRIORITY;
    }

    /** Gets the minimum priority available for a thread managed by this scheduler.
     *
     *  @return The value of the minimum priority.
     */
    public int getMinPriority() {
	return MIN_PRIORITY;
    }
    
    /** Gets the minimum priority of the given instance of
     *  <code>java.lang.Thread</code>. If the given thread is scheduled by the
     *  required <code>PriorityScheduler</code> the minimum priority of the
     *  <code>PriorityScheduler</code> is returned; otherwise
     *  <code>Thread.MIN_PRIORITY</code> is returned.
     *
     *  @return The value of the minimum priority of the given instance of
     *          <code>java.lang.Thread</code>.
     */
    public static int getMinPriority(Thread thread) {
	return (allThreads.contains(thread)) ?
	    MIN_PRIORITY : Thread.MIN_PRIORITY;
    }
    
    /** Gets the normal priority available for a thread managed by this scheduler.
     *
     *  @return The value of the normal priority.
     */
    public int getNormPriority() {
	return NORM_PRIORITY;
    }

    /** Gets the normal priority of the given instance of
     *  <code>java.lang.Thread</code>. If the given thread is scheduled by the
     *  required <code>PriorityScheduler</code> the normal priority of the
     *  <code>PriorityScheduler</code> is returned; otherwise
     *  <code>Thread.NORM_PRIORITY</code> is returned.
     */
    public static int getNormPriority(Thread thread) {
	return (allThreads.contains(thread)) ?
	    NORM_PRIORITY : Thread.NORM_PRIORITY;
    }
    
    /** Gets the policy name of <code>this</code>.
     *
     *  @return The policy name (Fixed Priority) as a string.
     */
    public String getPolicyName() {
	return "EDF";
    }
    
    /** Determines the load on the system. If the load is less then 1, the
     *  system is feasible. If the load is greater than or equal to 1, the
     *  system is not feasible.
     *
     *  @param releaseParams The list of the <code>ReleaseParameters</code>
     *                       to be considered when determining the load.
     *  @return The load of the system.
     */
    private float load(LinkedList releaseParams) {
	float l = 0.0f;
	for (Iterator it = releaseParams.iterator(); it.hasNext(); ) {
	    ReleaseParameters rp = (ReleaseParameters)it.next();
	    if (rp instanceof PeriodicParameters)
		l += ((PeriodicParameters)rp).getCost().time() /
		    ((PeriodicParameters)rp).getPeriod().time();
	    else l+= ((AperiodicParameters)rp).getCost().time() /
		     ((AperiodicParameters)rp).getDeadline().time();
	}
	
	return l;
    }

    /** Queries the system about the feasibility of the set of
     *  <code>Schedulable</code> objects with respect to their include in the
     *  feasibility set and the constraints expressed by their associated
     *  parameter objects.
     *
     *  @return True if the system is able to satisfy the constraints expressed
     *          by the parameter object of all instances of <code>Schedulable</code>
     *          currently in the feasibility set. False if the system cannot
     *          satisfy those constraints.
     */
    public boolean isFeasible() {
	return isFeasible(null, null);
    }

    protected boolean isFeasible(Schedulable s, ReleaseParameters rp) {
	LinkedList params = new LinkedList();
	int i = 0;
	boolean found = false;
	for (Iterator it = allThreads.iterator(); it.hasNext(); ) {
	    Object obj = it.next();
	    if (obj instanceof Schedulable) {
		Schedulable sch = (Schedulable)obj;
		if (sch == s) {
		    params.add(new ReleaseParameters(rp));
		    found = true;
		}
		else if (sch.getReleaseParameters() != null) {
		    params.add(new ReleaseParameters(sch.getReleaseParameters()));
		}
	    }
	}
	if ((!found) && (s != null) && (rp != null)) params.add(new ReleaseParameters(rp));

	return (load(params) < 1.0);
    }
    
    /** Inform <code>this</code> and cooperating facilities that the
     *  <code>ReleaseParameters</code> of the ginve instance of <code>Schedulable</code>
     *  should <i>not</i> be considered in feasibility analysis until further notified.
     *
     *  @param schedulable The instance of <code>Schedulable</code> whose
     *                     <code>ReleaseParameters</code> are to be removed from the
     *                     feasibility set.
     *  @return True, if the removal was ssuccessful. False, if the removal was
     *          unsuccessful.
     */
    protected void removeFromFeasibility(Schedulable schedulable) {
	allThreads.remove(schedulable);
	
	int i = 0;
	while ((thread[i] == null || thread[i].schedulable != schedulable)
	       && i < nThreads)
	    i++;
	if (i < nThreads) {
	    thread[i] = null; // To ensure deallocation
	    thread[i] = thread[--nThreads];
	}
    }
    
    /** The method appears in many classe in the RTSJ and with various parameters.
     *  The parameters are either new scheduling characteristics for an instance
     *  <code>Schedulable</code> or an instance of <code>Schedulable</code>. The
     *  method first performs a feasibility analysis using the new scheduling
     *  characteristics of the given instance of <code>Schedulable</code>. If the
     *  resultingsystem is feasible the method replaces the current scheduling
     *  characteristics, of either <code>this</code> or the given instance of
     *  <code>Schedulable</code> as appropriate, with the new scheduling characteristics.
     *
     *  @param schedulable The instance of <code>Schedulable</code> for which the
     *                     changes are proposed
     *  @param release The proposed release parameters.
     *  @param memory The proposed memory parameters.
     *  @return True, if the resulting system is feasible and the changes are made.
     *          False, if the resulting system is not feasible and no changes are made.
     */
    public boolean setIfFeasible(Schedulable schedulable,
				 ReleaseParameters release,
				 MemoryParameters memory) {
	return setIfFeasible(schedulable, release, memory, schedulable.getProcessingGroupParameters());
    }

    /** The method appears in many classe in the RTSJ and with various parameters.
     *  The parameters are either new scheduling characteristics for an instance
     *  <code>Schedulable</code> or an instance of <code>Schedulable</code>. The
     *  method first performs a feasibility analysis using the new scheduling
     *  characteristics of the given instance of <code>Schedulable</code>. If the
     *  resultingsystem is feasible the method replaces the current scheduling
     *  characteristics, of either <code>this</code> or the given instance of
     *  <code>Schedulable</code> as appropriate, with the new scheduling characteristics.
     *
     *  @param schedulable The instance of <code>Schedulable</code> for which the
     *                     changes are proposed
     *  @param release The proposed release parameters.
     *  @param memory The proposed memory parameters.
     *  @param group The proposed processing group parameters.
     *  @return True, if the resulting system is feasible and the changes are made.
     *          False, if the resulting system is not feasible and no changes are made.
     */
    public boolean setIfFeasible(Schedulable schedulable,
				 ReleaseParameters release,
				 MemoryParameters memory,
				 ProcessingGroupParameters group) {
	if (isFeasible(schedulable, release)) {
	    schedulable.setReleaseParameters(release);
	    schedulable.setMemoryParameters(memory);
	    schedulable.setProcessingGroupParameters(group);
	    return true;
	}
	else return false;
    }


    /** Implements EDF (Earliest Deadline First) Algorithm */
    protected long chooseThread(long micros) {
	long microsBegin = RealtimeClock.getTimeInC();
	invocations++;
	AbsoluteTime now = new AbsoluteTime(0, (int)micros*1000);
	
	if (nThreads == 0)
	    return (runningThread = 0);
	
	ReleaseParameters rp = null;
	int earliest = -1; // The periodic thread returned by EDF
	// If earliest = -1, we'll choose the sporadic thread with the
	// earliest starting time
	int earliestSporadic = -1;
	for (int i = 0; i < nThreads; i++)
	    if (thread[i] != null) { // if this thread is still alive
		// First, if this was the running thread, reduce its workLeft
		if (runningThread == thread[i].threadID)
		    thread[i].workLeft = thread[i].workLeft.subtract(runningTime);
		
		// Second, update the thread parameters
		if (thread[i].threadID == 1)
		    rp = mainThreadParameters;
		else if (thread[i].schedulable instanceof RealtimeThread)
		    rp = thread[i].schedulable.getReleaseParameters();
		
		// If the thread has no real time parameters, make it aperiodic
		if (rp == null)
		    rp = noRTParameters;
		//				System.out.println(rp);
		if (thread[i].beginPeriod == null) {
		    // This is the first time we're handling this thread.
		    // If the thread is sporadic, we'll set its endPeriod when we run
		    // it for the first time.
		    thread[i].beginPeriod = now;
		    thread[i].endPeriod = (rp instanceof PeriodicParameters) ?
			thread[i].beginPeriod.add(((PeriodicParameters)rp).getPeriod()) :
			new AbsoluteTime(AbsoluteTime.endOfDays);
		    thread[i].workLeft = rp.getCost();
		    thread[i].deadline = thread[i].beginPeriod.add(rp.getDeadline());
		}
		else if (now.compareTo(thread[i].endPeriod) >= 0) {
		    // This thread is passing into a new period
		    // (1) Check to see if the thread missed its deadline
		    if (thread[i].schedulable instanceof RealtimeThread &&
			thread[i].workLeft.compareTo(RelativeTime.ZERO) == 1) {
			missed++;
			//						AsyncEventHandler h = rp.getDeadlineMissHandler();
			//						if (h != null) h.run();
		    }
		    // (2) Update the thread constraints
		    thread[i].beginPeriod.set(thread[i].endPeriod);
		    if (rp instanceof PeriodicParameters)
			thread[i].beginPeriod.add(((PeriodicParameters)rp).getPeriod(),
						  thread[i].endPeriod);
		    else
			thread[i].endPeriod.set(AbsoluteTime.endOfDays);
		    thread[i].workLeft.set(rp.getCost());
		    thread[i].beginPeriod.add(rp.getDeadline(), thread[i].deadline);
		}
		// Third, use the thread for the EDF algorithm The thread must
		// (1) not be disabled, (2) have some work left to do during
		// this period, (3) have the earliest deadline among all threads
		if (!disabledThreads.contains(new Long(thread[i].threadID)))
		    if (rp instanceof PeriodicParameters ||
			(rp instanceof SporadicParameters &&
			 thread[i].workLeft.compareTo(rp.getCost()) == -1)) {
			// This thread is either periodic, or it is sporadic AND we have
			// started running it, so now we have to finish this period in time
			if (thread[i].workLeft.compareTo(RelativeTime.ZERO) == 1 &&
			    (earliest == -1 ||
			     thread[i].deadline.compareTo(thread[earliest].deadline)==-1))
			    earliest = i;
		    }
		    else if (rp instanceof SporadicParameters &&
			     thread[i].workLeft.compareTo(rp.getCost()) == 0) {
			// This thread is sporadic and we haven't started this period yet,
			// So we'll remember it in case we have nothing urgent to do
			if (earliestSporadic == -1 ||
			    thread[i].beginPeriod.
			    compareTo(thread[earliestSporadic].beginPeriod) == -1)
			    earliestSporadic = i;
		    }
		
	    }
	
	// If nothing urgent, run a sporadic thread
	if (earliest == -1 && earliestSporadic != -1) {
	    earliest = earliestSporadic;
	    // We're activating a new period for this sporadic thread, so we have to
	    // set startPeriod and endPeriod
	    thread[earliest].beginPeriod.set(now);
	    thread[earliest].beginPeriod
		.add(((SporadicParameters)thread[earliest].schedulable.
		      getReleaseParameters()).getMinimumInterarrival(),
		     thread[earliest].endPeriod);
	}
	
	// If the thread has enough work left to do, give it a full
	// quanta. Otherwise, give it only the time it needs.
	if (earliest != -1) {
	    runningThread = thread[earliest].threadID;
	    if (thread[earliest].workLeft.compareTo(defaultQuanta) == -1)
		runningTime.set(thread[earliest].workLeft.getMilliseconds(),
				thread[earliest].workLeft.getNanoseconds());
	    else
		runningTime.set(defaultQuanta.getMilliseconds(),
				defaultQuanta.getNanoseconds());
	    setQuanta(runningTime.getMilliseconds()*1000);
	    runningTimeMicros += RealtimeClock.getTimeInC() - microsBegin;
	    return runningThread;
	}
	
	// Nothing to do, remain idle
	runningTimeMicros += RealtimeClock.getTimeInC() - microsBegin;
	return runningThread = 0;
    }
    
    protected void disableThread(long threadID) {
	disabledThreads.add(new Long(threadID));
    }	
    
    protected void enableThread(long threadID) {
	disabledThreads.remove(new Long(threadID));
    }	
    
    public void waitForNextPeriod(RealtimeThread rt) {
	for (int i = 0; i < nThreads; i++)
	    if (thread[i] != null && thread[i].schedulable == rt)
                thread[i].workLeft.set(0,0);
    }

    public void addThread(RealtimeThread rt) {
    }

    public void addThread(long threadID) {
    }

    public void removeThread(RealtimeThread rt) {
    }

    public void removeThread(long threadID) {
    }

    public String toString() {
	return "";
    }
}
