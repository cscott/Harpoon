// EDFScheduler.java, created by wbeebee
// Copyright (C) 2003 Wes Beebee <wbeebee@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package javax.realtime;

/** <code>EDFScheduler</code> is an example of the dumbest possible EDF scheduler.
 *
 * @author Wes Beebee <<a href="mailto:wbeebee@mit.edu">wbeebee@mit.edu</a>>
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
    
    /** It is not always feasible to add another thread to a EDFScheduler. */
    protected void addToFeasibility(Schedulable schedulable) {
    }

    public void fireSchedulable(Schedulable schedulable) {
	schedulable.run();
    }

    /** Used to determine the policy of the <code>Scheduler</code>. */
    public String getPolicyName() {
	return "EDF Scheduler";
    }

    /** It is not always feasible to add another thread to a EDFScheduler. */
    public boolean isFeasible() {
	return true;
    }

    /** It is not always feasible to add another thread to a EDFScheduler. */
    protected boolean isFeasible(Schedulable s, ReleaseParameters rp) {
	return true;
    }

    /** It is not always feasible to add another thread to a EDFScheduler. */
    protected void removeFromFeasibility(Schedulable schedulable) {}

    /** It is not always feasible to add another thread to a EDFScheduler. */
    public boolean setIfFeasible(Schedulable schedulable,
				 ReleaseParameters release,
				 MemoryParameters memory) {
	return true;
    }

    /** It is not always feasible to add another thread to a EDFScheduler. */
    public boolean setIfFeasible(Schedulable schedulable,
				 ReleaseParameters release,
				 MemoryParameters memory,
				 ProcessingGroupParameters group) {
	return true;
    }

    protected long chooseThread(long currentTime) {
 	for (int threadID = 0; threadID <= numThreads; threadID++) {
	    long p = period[threadID];
 	    if ((p!=0)&&
		(startPeriod[threadID] + p < currentTime)) { 
		// Period past, update startPeriod to start of current period.
		// Reset work done to zero.
		startPeriod[threadID] = currentTime - ((currentTime - startPeriod[threadID])%p);
 		work[threadID] = 0;
 	    }
 	}
	long minPeriod = Long.MAX_VALUE;
	long minPeriodWithWork = Long.MAX_VALUE;
	int tid = -1;
	for (int threadID=OFFSET+1; threadID <= numThreads; threadID++) {
	    long p = period[threadID];
	    if (p == 0) continue;
	    long newPeriod = (p+startPeriod[threadID])-currentTime;
	    if (newPeriod < minPeriod) { 
		// Find the time of the soonest period ender...
		minPeriod = newPeriod;
	    }
	    if ((newPeriod < minPeriodWithWork)&&(enabled[threadID])&&(cost[threadID]!=0)&&(cost[threadID] > work[threadID])) { 
		// Find the time of the soonest period ender with work left to be done...
		minPeriodWithWork = newPeriod;
		tid = threadID;
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

	if (currentThreadID != 0) {
	    int threadID = (int)(currentThreadID+OFFSET);
	    if (lastTime == 0) {
		lastTime = startPeriod[threadID];
	    }
	    work[threadID]+= currentTime - lastTime; // I've done some work...
	}
	lastTime = currentTime;
	return currentThreadID=chooseThread2(minPeriod, tid);
    }

    protected long chooseThread2(long nextPeriod, int tid) {
	long minPeriod = Long.MAX_VALUE;

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
