// EDFScheduler.java, created by wbeebee, laporte
// Copyright (C) 2003 Wes Beebee <wbeebee@mit.edu>, Nathan LaPorte <laporte@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package javax.realtime;

/** <code>EDFScheduler</code> is an example of the dumbest possible EDF scheduler.
 *
 * @author Wes Beebee <<a href="mailto:wbeebee@mit.edu">wbeebee@mit.edu</a>>
 * @author Nathan La Porte <<a href="mailto:laporte@mit.edu">laporte@mit.edu</a>>
 */
public class EDFScheduler extends Scheduler {
    static EDFScheduler instance = null;

    public static final int MAX_THREADS = 100; // The maximum thread ID in the entire system
    public static final int OFFSET = 2; // The maximum number of C threads

    private boolean[] enabled = new boolean[MAX_THREADS]; 
    private long[] cost = new long[MAX_THREADS]; // in microseconds
    private long[] period = new long[MAX_THREADS];
    private long[] work = new long[MAX_THREADS];
    private long[] startPeriod = new long[MAX_THREADS];
    private long numThreads;

    private long currentThreadID; /* What thread was running */
    private long lastTime; /* When I last chose to start it */

    private static final boolean WALLCLOCK_WORK = false; /* Use the wall clock to estimate work. */
    private static final boolean COUNT_MISSED_DEADLINES = true; 

    private long deadlinesMissed = 0;

    protected EDFScheduler() {
	super();
	setQuanta(0); // Start switching after a specified number of microseconds
    }

    /** Return an instance of an EDFScheduler */
    public static EDFScheduler instance() {
	if (instance == null) {
	    ImmortalMemory.instance().enter(new Runnable() {
		public void run() {
		    instance = new EDFScheduler();
		}
	    });
	}

	return instance;
    }
    
    /** Inform the scheduler and cooperating facilities that the resource demands,
     *  as expressed in associated instances of <code>SchedulingParameters,
     *  ReleaseParameters, MemoryParameters</code> and <code>ProcessingGroupParameters</code>,
     *  of this instance of <code>Schedulable</code> will be considered in the 
     *  feasibility analysis of the associated <code>Scheduler</code> until
     *  further notice.  Whether the resulting system is feasible or not, the addition 
     *  is completed.
     *
     *  @param schedulable The instance of <code>Schedulable</code> for which the changes
     *                     are proposed.
     */

    protected void addToFeasibility(Schedulable schedulable) {
    }

    public void fireSchedulable(Schedulable schedulable) {
	schedulable.run();
    }

    /** Used to determine the policy of the <code>Scheduler</code>. */
    public String getPolicyName() {
	return "EDF Scheduler";
    }

    /** Queries the system about the feasibility of the set of 
     *  <code>Schedulable</code> objects with respect to their include in the 
     *  feasibility set and the constraints expressed by their associated
     *  parameter objects.
     *
     *  @return True if the system is able to satisfy the constraints expressed
     *          by the parameter object of all instances of <code>Schedulable</code>
     *          currently in the feasibility set.  False if the system cannot
     *          satisfy those constraints.
     */
    public boolean isFeasible() {
	class CalcLoad implements Runnable {
	    public float l = 0.0f;

	    public CalcLoad() {}

	    public void run() {
		EDFScheduler edf = EDFScheduler.this;
		int curThread;
		for (curThread = 0; curThread <= edf.numThreads; curThread++) {
		    if (edf.enabled[curThread])
			l += (edf.cost[curThread]-edf.work[curThread])/
			    ((edf.startPeriod[curThread] + edf.period[curThread])-
			     System.currentTimeMillis());
		}
	    }
	}
	
	CalcLoad c = new CalcLoad();
	atomic(c);
	return (c.l < 1.0);
    }

    /** It is not always feasible to add another thread to a EDFScheduler. */
    protected boolean isFeasible(final Schedulable s, final ReleaseParameters rp) {
	class CalcLoad implements Runnable {
	    public boolean feas;

	    public CalcLoad() {}

	    public void run() {
		EDFScheduler edf = EDFScheduler.this;
		int curThread = (int)(s.getUID() + edf.OFFSET);
		edf.enabled[curThread] = true;
		long oldCost = edf.cost[curThread];  // Save the old values
		long oldPeriod = edf.period[curThread];
		// Put in the test values
		edf.cost[curThread] = rp.getCost().getMilliseconds();  
		edf.period[curThread] = rp.getDeadline().getMilliseconds();
		feas = edf.isFeasible(); // does it work?
		edf.cost[curThread] = oldCost; // Either way, restore old ones
		edf.period[curThread] = oldPeriod;
	    }
	}
	
	CalcLoad c = new CalcLoad();
	atomic(c);
	return c.feas;
    }

    /** Inform <code>this</code> and cooperating facilities that the
     *  <code>ReleaseParameters</code> of the given instance of <code>Schedulable</code>
     *  should <i>not</i> be considered in feasibility analysis until further notified.
     *
     *  @param schedulable The instance of <code>Schedulable</code> whose
     *                     <code>ReleaseParameters</code> are to be removed from the 
     *                     feasibility set.
     *  @return True, if the removal was successful. False, if the removal was
     *          unsucessful.
     */
    protected void removeFromFeasibility(Schedulable schedulable) {}

    /** This method appears in many classes in the RTSJ and with various parameters.
     *  The parameters are either new scheduling characteristics for an instance
     *  <code>Schedulable</code> or an instance of <code>Schedulable</code>. The
     *  method first performs a feasibility analysis using the new scheduling
     *  characteristics of the given instance of <code>Schedulable</code>.  If the
     *  resuling system is feasible, the method replaces the current scheduling
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

    /** The method appears in many classes in the RTSJ and with various parameters.
     *  The parameters are either new scheduling characteristics for an instance
     *  <code>Schedulable</code> or an instance of <code>Schedulable</code>. The
     *  method first performs a feasibility analysis using the new scheduling
     *  characteristics, of either <code>this</code> or the given instance of
     *  <code>Schedulable</code> as appropriate, with the new scheduling characterics.
     *
     *  @param schedulable The instance of <code>Schedulable</code> for which the
     *                     changes are proposed
     *  @param release The proposed release parameters.
     *  @param memory The proposed memory paramaters.
     *  @param group The proposed processing group parameters.
     *  @return True, if the resulting system is feasible and the changes are made.
     *          False, if the resulting system is not feasible and no changes are made.
     */
    public boolean setIfFeasible(final Schedulable schedulable,
				 final ReleaseParameters release,
				 final MemoryParameters memory,
				 final ProcessingGroupParameters group) {
	class CalcLoad implements Runnable {
	    public boolean feas;
	    
	    public CalcLoad() {}

	    public void run() {
		EDFScheduler edf = EDFScheduler.this;
		int curThread = (int)(schedulable.getUID() + EDFScheduler.OFFSET);
		if (isFeasible(schedulable, release)) {
		    schedulable.setReleaseParameters(release);
		    edf.cost[curThread] = release.getCost().getMilliseconds();
		    edf.period[curThread] = release.getDeadline().getMilliseconds();
		    schedulable.setMemoryParameters(memory);
		    schedulable.setProcessingGroupParameters(group);
		    feas = true;
		} else {
		    feas = false;
		}
	    }
	}

	CalcLoad c = new CalcLoad();
	atomic(c);
	return c.feas;
    }

    protected long chooseThread(long currentTime) {
 	for (int threadID = 0; threadID <= numThreads; threadID++) {
	    long p = period[threadID];
 	    if ((p!=0)&&
		(startPeriod[threadID] + p < currentTime)) { 
		// Period past, update startPeriod to start of current period.
		// Reset work done to zero.
		startPeriod[threadID] = currentTime - ((currentTime - startPeriod[threadID])%p);

		if (COUNT_MISSED_DEADLINES && (work[threadID]<cost[threadID])) {
		    deadlinesMissed++;
		    NoHeapRealtimeThread.print("Missed deadline #");
		    NoHeapRealtimeThread.print(deadlinesMissed);
		    NoHeapRealtimeThread.print("\n");
		}
 		work[threadID] = 0;
 	    }
 	}
	long minPeriod = Long.MAX_VALUE;
	for (int threadID=OFFSET+1; threadID <= numThreads; threadID++) {
	    long p = period[threadID];
	    if (p == 0) continue;
	    long newPeriod = (p+startPeriod[threadID])-currentTime;
	    if (newPeriod < minPeriod) { 
		// Find the time of the soonest period ender...
		minPeriod = newPeriod;
	    }
	}

	// Period ended, trigger context switch to rechoose
	if (minPeriod < 1) {
	    minPeriod = 1;
	} else if (minPeriod == Long.MAX_VALUE) { 	
	    // No soonest period ender, run until block
	    minPeriod = 0;
	} else {
	    // Convert from milliseconds to microseconds...
	    minPeriod *= 1000;
	}
	
	long clock = WALLCLOCK_WORK?currentTime:(long)(clock()/1000);

	if (currentThreadID != 0) {
	    int threadID = (int)(currentThreadID+OFFSET);
	    if (lastTime == 0) {
		lastTime = startPeriod[threadID];
	    }
	    work[threadID]+= clock - lastTime; // I've done some work...
	}
	lastTime = clock;
	return currentThreadID=chooseThread2(minPeriod, currentTime);
    }

    protected long chooseThread2(long nextPeriod, long currentTime) {
	long minPeriod = Long.MAX_VALUE;
	int tid = -1;

	for (int threadID=OFFSET+1; threadID <= numThreads; threadID++) {
	    long p = period[threadID];
	    if ((p == 0)||(!enabled[threadID])||(cost[threadID] <= work[threadID])) continue;
	    long newPeriod = (p+startPeriod[threadID])-currentTime;
	    if (newPeriod < minPeriod) {
		minPeriod = newPeriod; // Find minimum period thread with work.
		tid = threadID;
	    }
	}

	if (tid == -1) { // No periodic threads chosen
	    // Iterate through the Java threads
	    for (int threadID=OFFSET+1; threadID <= numThreads; threadID++) { 
		if (enabled[threadID] && (period[threadID]==0)) {
		    tid = threadID; // Run the non-periodic until next period starts
		    break;
		}
	    }
	}

	if (tid == -1) { // No threads chosen
	    for (int threadID=0; threadID<OFFSET; threadID++) { // Go through C threads
		if (enabled[threadID]) {
		    tid = threadID; // Run that one until next period starts
		    break;
		}
	    }
	}
	if (tid == -1) { // No native or Java threads available
	    NoHeapRealtimeThread.print("Deadlock!\n");
	    System.exit(-1);
	    return 0;
	}

	long timeLeft = (cost[tid] - work[tid])*1000;
	setQuanta((timeLeft>0)?(timeLeft<nextPeriod?timeLeft:nextPeriod):nextPeriod);
	return tid-OFFSET;
    }

    protected void addThread(RealtimeThread thread) {
	int threadID = (int)(thread.getUID()+OFFSET);

	numThreads = threadID>numThreads?threadID:numThreads;

	ReleaseParameters r = thread.getReleaseParameters();
	enabled[threadID] = true;
	startPeriod[threadID] = System.currentTimeMillis();

	if (r != null) {
	    cost[threadID] = r.getCost().getMilliseconds();
	    period[threadID] = r.getDeadline().getMilliseconds();
	    contextSwitch();
	}
    }

    protected void removeThread(RealtimeThread thread) {
	enabled[(int)(thread.getUID()+OFFSET)] = false;
    }

    protected void addThread(long threadID) {
	enabled[(int)(threadID+OFFSET)] = true;
    }

    protected void removeThread(long threadID) {
	enabled[(int)(threadID+OFFSET)] = false;
    }

    protected void disableThread(long threadID) {
	enabled[(int)(threadID+OFFSET)] = false;
    }

    protected void enableThread(long threadID) {
	enabled[(int)(threadID+OFFSET)] = true;
    }

    /** EDFScheduler is too dumb to deal w/periods. */
    protected void waitForNextPeriod(RealtimeThread rt) {
    }

    public String toString() {
	String s = "[";
	for (int threadID = 0; threadID <= numThreads; threadID++) {
	    if (enabled[threadID]) {
		s+="[id: "+(threadID-OFFSET)+" p: "+period[threadID]+" c: "+
		    cost[threadID]+" w: "+work[threadID]+" sp: "+
		    startPeriod[threadID]+"]";
	    }
	}

	return s+"]";
    }

    public void printNoAlloc() {
	NoHeapRealtimeThread.print("[");
	for (int threadID = 0; threadID <= numThreads; threadID++) {
	    if (enabled[threadID]) {
		NoHeapRealtimeThread.print("[id: ");
		NoHeapRealtimeThread.print(threadID-OFFSET);
		NoHeapRealtimeThread.print(" p: ");
		NoHeapRealtimeThread.print(period[threadID]);
		NoHeapRealtimeThread.print(" c: ");
		NoHeapRealtimeThread.print(cost[threadID]);
		NoHeapRealtimeThread.print(" w: ");
		NoHeapRealtimeThread.print(work[threadID]);
		NoHeapRealtimeThread.print(" sp: ");
		NoHeapRealtimeThread.print(startPeriod[threadID]);
		NoHeapRealtimeThread.print("]");
	    }
	}

	NoHeapRealtimeThread.print("]");
    }
}
