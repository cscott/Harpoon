// RMAScheduler.java, created by wbeebee
// Copyright (C) 2003 Wes Beebee <wbeebee@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package javax.realtime;

import java.util.List;
import java.util.ArrayList;
import java.util.NoSuchElementException;
import java.util.Iterator;

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
	    if (startPeriod[threadID] + period[threadID] < currentTime) {
		startPeriod[threadID] += period[threadID];
		work[threadID] = 0;
	    }
	}
	long minPeriod = Long.MAX_VALUE;
	for (int threadID=OFFSET+1; threadID <= numThreads; threadID++) {
	    if (startPeriod[threadID] + period[threadID] - currentTime < minPeriod) { 
		// Find the time of the soonest period ender...
		minPeriod = startPeriod[threadID] + period[threadID] - currentTime;
	    }
	}
	if (minPeriod < 1) {
	    minPeriod = 1;
	} // Period ended, trigger context switch to rechoose
	if (currentThreadID != 0) {
	    int threadID = (int)(currentThreadID+OFFSET);
	    work[threadID]+= currentTime - lastTime; // I've done some work...
	    lastTime = currentTime;
	    if (enabled[threadID]) {
		long timeLeft = cost[threadID] - work[threadID];
		if (timeLeft > 0) { // I'm not done yet with the current thread, keep running...
		    setQuanta(timeLeft<minPeriod?timeLeft:minPeriod);
		    return 0;
		} else { // I'm done with this thread, choose another to run...
		    return currentThreadID=chooseThread2(minPeriod);
		}
	    } else { // Thread blocked, choose another to run
		return currentThreadID=chooseThread2(minPeriod);
	    }
	} else { // Choose the first to run
	    lastTime = currentTime;
	    return currentThreadID=chooseThread2(minPeriod);
	}
    }

    protected long chooseThread2(long nextPeriod) {
	long minPeriod = Long.MAX_VALUE;
	int tid = -1;
	for (int threadID=OFFSET+1; threadID <= numThreads; threadID++) { // Iterate through the Java threads
	    if (enabled[threadID] && (cost[threadID] > work[threadID])) {
		if (period[threadID] < minPeriod) { // Choose minimum period thread
		    minPeriod = period[threadID];
		    tid = threadID;
		}
	    }
	}
	if (tid == -1) { // No threads chosen
	    for (int threadID=0; threadID<OFFSET; threadID++) { // Go through C threads
		if (enabled[threadID]) {
		    setQuanta(0);
		    return threadID-OFFSET; // Run that one until it blocks
		}
	    }
	}
	if (tid == -1) { // No native or Java threads available
	    NoHeapRealtimeThread.print("Deadlock!\n");
	    System.exit(-1);
	    return 0;
	}

	long timeLeft = cost[tid] - work[tid];
	setQuanta(timeLeft<nextPeriod?timeLeft:nextPeriod);
	return tid-OFFSET;
    }

    protected void addThread(RealtimeThread thread) {
	int threadID = (int)(thread.getUID()+OFFSET);

	numThreads = threadID>numThreads?threadID:numThreads;
	work[threadID] = 0;

	ReleaseParameters r = thread.getReleaseParameters();
	cost[threadID] = r.getCost().getMilliseconds();
	period[threadID] = r.getDeadline().getMilliseconds();
	startPeriod[threadID] = System.currentTimeMillis();
	enabled[threadID] = true;
	contextSwitch();
    }

    protected void removeThread(RealtimeThread thread) {
	int threadID = (int)(thread.getUID()+OFFSET);
	enabled[threadID] = false;
    }

    protected void addThread(long threadID) {
	int tid = (int)(threadID+OFFSET);
	work[tid] = 0;
	cost[tid] = 0;
	period[tid] = 0;
	startPeriod[tid] = 0;
	enabled[tid] = true;
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
	return "foo";
    }

    public void printNoAlloc() {
	NoHeapRealtimeThread.print("[");
	for (int threadID = 0; threadID <= numThreads; threadID++) {
	    NoHeapRealtimeThread.print("[ id: ");
	    NoHeapRealtimeThread.print(threadID-OFFSET);
	    NoHeapRealtimeThread.print(" e: ");
	    NoHeapRealtimeThread.print(enabled[threadID]?"t":"f");
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

	NoHeapRealtimeThread.print("]");
    }
}
