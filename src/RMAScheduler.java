// RMAScheduler.java, created by wbeebee
// Copyright (C) 2003 Wes Beebee <wbeebee@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package javax.realtime;

/** <code>RMAScheduler</code> is an example of the dumbest possible RMA scheduler.
 *
 * @author Wes Beebee <<a href="mailto:wbeebee@mit.edu">wbeebee@mit.edu</a>>
 */
public class RMAScheduler extends Scheduler {
    static RMAScheduler instance = null;

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

    protected RMAScheduler() {
	super();
	setQuanta(0); // Start switching after a specified number of microseconds
    }

    /** Return an instance of an RMAScheduler */
    public static RMAScheduler instance() {
	if (instance == null) {
	    ImmortalMemory.instance().enter(new Runnable() {
		public void run() {
		    instance = new RMAScheduler();
		}
	    });
	}

	return instance;
    }
    
    /** It is not always feasible to add another thread to a RMAScheduler. */
    protected void addToFeasibility(Schedulable schedulable) {
    }

    public void fireSchedulable(Schedulable schedulable) {
	schedulable.run();
    }

    /** Used to determine the policy of the <code>Scheduler</code>. */
    public String getPolicyName() {
	return "RMA Scheduler";
    }

    /** It is not always feasible to add another thread to a RMAScheduler. */
    public boolean isFeasible() {
	return true;
    }

    /** It is not always feasible to add another thread to a RMAScheduler. */
    protected boolean isFeasible(Schedulable s, ReleaseParameters rp) {
	return true;
    }

    /** It is not always feasible to add another thread to a RMAScheduler. */
    protected void removeFromFeasibility(Schedulable schedulable) {}

    /** It is not always feasible to add another thread to a RMAScheduler. */
    public boolean setIfFeasible(Schedulable schedulable,
				 ReleaseParameters release,
				 MemoryParameters memory) {
	return true;
    }

    /** It is not always feasible to add another thread to a RMAScheduler. */
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

	if (currentThreadID != 0) {
	    int threadID = (int)(currentThreadID+OFFSET);
	    if (lastTime == 0) {
		lastTime = startPeriod[threadID];
	    }
	    
	    long clock = WALLCLOCK_WORK?currentTime:(long)clock();
	    work[threadID]+= clock - lastTime; // I've done some work...
	    lastTime = clock;

	    if (enabled[threadID]) {
		long timeLeft = (cost[threadID] - work[threadID])*1000;
 		if ((cost[threadID]!=0)&&(timeLeft > 0)) { // I'm not done yet with the current thread, choose one (including current thread).
 		    return currentThreadID=chooseThread2(timeLeft<minPeriod?timeLeft:minPeriod);
 		} else { // I'm done with this thread, choose another to run...
		    return currentThreadID=chooseThread2(minPeriod);
 		}
	    } else { // Thread blocked, choose another to run
		return currentThreadID=chooseThread2(minPeriod);
	    }
	} else { // Choose the first to run
	    lastTime = WALLCLOCK_WORK?currentTime:(long)clock();
	    return currentThreadID=chooseThread2(minPeriod);
	}
    }

    protected long chooseThread2(long nextPeriod) {
	long minPeriod = Long.MAX_VALUE;
	int tid = -1;

	// Iterate through the Java threads
	for (int threadID=OFFSET+1; threadID <= numThreads; threadID++) { 
	    if (enabled[threadID] && (period[threadID]!=0) && (cost[threadID] > work[threadID])) {
		if (period[threadID] < minPeriod) { // Choose minimum period periodic thread
		    minPeriod = period[threadID];
		    tid = threadID;
		}
	    }
	}

	if (tid == -1) { // No periodic threads chosen
	    // Iterate through the Java threads
	    for (int threadID=OFFSET+1; threadID <= numThreads; threadID++) { 
		if (enabled[threadID] && (period[threadID]==0)) {
		    setQuanta(nextPeriod);
		    return threadID-OFFSET; // Run the non-periodic until next period starts
		}
	    }
	}

	if (tid == -1) { // No threads chosen
	    for (int threadID=0; threadID<OFFSET; threadID++) { // Go through C threads
		if (enabled[threadID]) {
		    setQuanta(nextPeriod);
		    return threadID-OFFSET; // Run that one until next period starts
		}
	    }
	}
	if (tid == -1) { // No native or Java threads available
	    NoHeapRealtimeThread.print("Deadlock!\n");
	    System.exit(-1);
	    return 0;
	}

	long timeLeft = (cost[tid] - work[tid])*1000;
	setQuanta(timeLeft<nextPeriod?timeLeft:nextPeriod);
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

    /** RMAScheduler is too dumb to deal w/periods. */
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
