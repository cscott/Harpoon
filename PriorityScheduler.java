// PriorityScheduler.java, created by cata
// Copyright (C) 2001 Catalin Francu <cata@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package javax.realtime;
import java.util.HashSet;
import java.util.Iterator;

public class PriorityScheduler extends Scheduler {
    // Real-time thread priorities
    static final int MAX_PRIORITY = 38;
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
    
    static PriorityScheduler thisScheduler = null;
    
    // Do not call this constructor; instead, call
    // PriorityScheduler.getScheduler().
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
    
    // Creates and returns a scheduler of this type
    // NOT IN SPECS -----------------------

    // This method is not thread-safe!
    public static PriorityScheduler getScheduler() {
	if (thisScheduler == null) 
	    try {
		ImmortalMemory.instance().enter(new Runnable() {
			public void run() {
			    PriorityScheduler.thisScheduler = new PriorityScheduler();
			}
		    });
	    } catch (ScopedCycleException e) {
		System.out.println("ScopeCycleException should never occur " + e);
	    }
	
	return thisScheduler;
    }
    
    // Start maintaining this thread (called by the runtime system during
    // execution of the thread.start() method).
    protected boolean addToFeasibility(final Schedulable schedulable) {
	allThreads.add(schedulable);
	thread[nThreads] = new ThreadConstraints();
	thread[nThreads].threadID = ++nextThreadID;
	thread[nThreads].schedulable = schedulable;
	thread[nThreads].beginPeriod = null;
	nThreads++;
	
	try {
	    ImmortalMemory.instance().enter(new Runnable() {
		    public void run() {
			// Give the runtime system a chance to update its data structures
			addThreadInC(schedulable, nextThreadID);
		    }
		});
	} catch (ScopedCycleException e) {
	    System.out.println("ScopedCycleException should never occur : " + e);
	}

	// SPECS CHANGED -- return 'boolean' instead of 'void'
	return false;
    }

    // NOT IN SPECS --------------------------
    protected native void addThreadInC(Schedulable t, long threadID);

    public void fireSchedulable(Schedulable schedulable) {
	schedulable.run();
    }
    
    public int getMaxPriority() {
	return MAX_PRIORITY;
    }
    
    public static int getMaxPriority(Thread thread) {
	return (allThreads.contains(thread)) ?
	    MAX_PRIORITY : Thread.MAX_PRIORITY;
    }
    
    public int getMinPriority() {
	return MIN_PRIORITY;
    }
    
    public static int getMinPriority(Thread thread) {
	return (allThreads.contains(thread)) ?
	    MIN_PRIORITY : Thread.MIN_PRIORITY;
    }
    
    public int getNormPriority() {
	return NORM_PRIORITY;
    }
    
    public static int getNormPriority(Thread thread) {
	return (allThreads.contains(thread)) ?
	    NORM_PRIORITY : Thread.NORM_PRIORITY;
    }
    
    public String getPolicyName() {
	return "EDF";
    }
    
    public static PriorityScheduler instance() {
	if (defaultScheduler == null) return new PriorityScheduler();
	else return (PriorityScheduler)defaultScheduler;
    }

    // This one's a bit twisted, but it's much more
    // implementation-friendly. We decide feasibility by calling
    // changeIfFeasible with no parameters.
    public boolean isFeasible() {
	return changeIfFeasible(null, null, null);
    }
    
    // changeIfFeasible()
    //   This is where the actual feasibility decision is made.
    //   Algorithm: Plain EDF -- add up the fractions cost/period for
    //   periodic threads, cost/minInterarrival for sporadic threads.
    public boolean changeIfFeasible(Schedulable schedulable,
				    ReleaseParameters release,
				    MemoryParameters memory) {
	if (schedulable != null &&
	    !allThreads.contains(schedulable)) {
	    return false; // We are not responsible for this thread.
	}
	int switchingState = stopSwitchingInC();
	double load = 0.0;
	HashSet groupsSeen = new HashSet();
	for (Iterator i = allThreads.iterator(); i.hasNext(); ) {
	    Schedulable s = (Schedulable) i.next();
	    // Use the release parameters for each Schedulable, except for the one
	    // we are trying to change, for which we'll use the newly proposed
	    // parameters.
	    ReleaseParameters rp = (s == schedulable) ?
		release : s.getReleaseParameters();
	    
	    if (rp != null) // Main thread (and possibly other threads) have null rp
		// Periodic threads already have a cost and a period
		if (rp instanceof PeriodicParameters) {
		    load += (double)((PeriodicParameters)rp).getCost().getMilliseconds()/
			((PeriodicParameters)rp).getPeriod().getMilliseconds();
		}
		else if (rp instanceof SporadicParameters) {
		    load += (double)((SporadicParameters)rp).getCost().getMilliseconds()/
			((SporadicParameters)rp).getMinimumInterarrival().getMilliseconds();
		}
	    // We must look up the period in the ProcessingGroupParameters
	    // of the thread.
		else {
		    ProcessingGroupParameters pgp = s.getProcessingGroupParameters();
		    if (!groupsSeen.contains(pgp)) { // Haven't seen this group before
			groupsSeen.add(pgp);
			load += (double)pgp.getCost().getMilliseconds() /
			    pgp.getPeriod().getMilliseconds();
		    }
		}
	}
	
	System.out.println("Load = " + load);
	if (load <= 1.0 && schedulable != null) {
	    schedulable.setReleaseParameters(release);
	    schedulable.setMemoryParameters(memory);
	}
	
	restoreSwitchingInC(switchingState);
	return (load <= 1.0);
    }

    protected boolean removeFromFeasibility(Schedulable schedulable) {
	long threadID = removeThreadInC(schedulable);
	allThreads.remove(schedulable);
	
	int i = 0;
	while ((thread[i] == null || thread[i].schedulable != schedulable)
	       && i < nThreads)
	    i++;
	if (i < nThreads) {
	    thread[i] = null; // To ensure deallocation
	    thread[i] = thread[--nThreads];
	}

	// SPECS CHANGED -- return 'boolean' instead of 'void'
	return false;
    }
    
    // NOT IN SPECS ---------------
    protected native long removeThreadInC(Schedulable t);
    
    public boolean setIfFeasible(Schedulable schedulable,
				 ReleaseParameters release,
				 MemoryParameters memory) {
	return setIfFeasible(schedulable, release, memory, schedulable.getProcessingGroupParameters());
    }

    public boolean setIfFeasible(Schedulable schedulable,
				 ReleaseParameters release,
				 MemoryParameters memory,
				 ProcessingGroupParameters group) {
	// TODO
	return false;
    }


    // ----------------------------
    // NOT IN SPECS
    // ----------------------------
    
    protected native void setQuantaInC(long microsecs);
    protected native long getTimeInC();
    
    public long chooseThread(long micros) {
	long microsBegin = getTimeInC();
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
	    setQuantaInC(runningTime.getMilliseconds()*1000);
	    runningTimeMicros += getTimeInC() - microsBegin;
	    return runningThread;
	}
	
	// Nothing to do, remain idle
	runningTimeMicros += getTimeInC() - microsBegin;
	return runningThread = 0;
    }
    
    public void stopAll()	{
    }
    
    public boolean noThreads() {
	return nThreads == 0;
    }
    
    public void disableThread(long threadID) {
	disabledThreads.add(new Long(threadID));
    }	
    
    public void enableThread(long threadID) {
	disabledThreads.remove(new Long(threadID));
    }	
    
    public void waitForNextPeriod(RealtimeThread rt) {
	for (int i = 0; i < nThreads; i++)
	    if (thread[i] != null && thread[i].schedulable == rt)
                thread[i].workLeft.set(0,0);
    }
    
    protected static native int stopSwitchingInC();
    protected static native int restoreSwitchingInC(int state);
}
