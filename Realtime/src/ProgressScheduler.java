// ProgressScheduler.java, created by nlaporte
// Copyright (C) 2003 Nathan La Porte <laporte@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package javax.realtime;


/** <code>ProgessScheduler</code> is a scheduler that attempts to provide
 *  constant latency for some number of threads. 
 *
 * @author Nathan La Porte <<a href="mailto:laporte@mit.edu">laporte@mit.edu</a>>
 */
public class ProgressScheduler extends Scheduler {
    static ProgressScheduler instance = null;

    public static final int MAX_THREADS = 1000;
    public static final int MAX_PROGRESSPOINTS = 15;
    public static int RUN_THREADS = 5; //Max number of enabled threads
    private static final int OFFSET = 2; //Max number of C threads
    private boolean[] enabled = new boolean[MAX_THREADS];
    private int[] threadProgress = new int[MAX_THREADS]; //keeps track of what progressPoint the thread is in (which is the last point called + 1)
    private long[][] pointTime = new long[MAX_THREADS][MAX_PROGRESSPOINTS]; //Keeps track of how long, on average, each area between progressPoints takes to run
    private int currentIndex = 1; //ID of running thread + OFFSET
    private int maxIndex = 0; //Highest thread ID + OFFSET
    private int numPoints = 15; //Number of progressPoints (max progressPoint number + 1)
    private int enabledThreads = 0; //Number of enabled threads, should never be greater than RUN_THREADS
    private long currentThreadID;
    private int theQuanta = 1000;

    protected ProgressScheduler() {
	super();
	setQuanta(0); // Start switching after a specified number of microseconds
    }

    /** Return an instance of a ProgressScheduler */
    public static ProgressScheduler instance() {
	if (instance == null) {
	    ImmortalMemory.instance().enter(new Runnable() {
		public void run() {
		    instance = new ProgressScheduler();
		}
	    });
	}

	return instance;
    }
    
    /** It is always feasible to add another thread to a ProgressScheduler. */
    protected void addToFeasibility(Schedulable schedulable) {}

    public void fireSchedulable(Schedulable schedulable) {
	schedulable.run();
    }

    /** Used to determine the policy of the <code>Scheduler</code>. */
    public String getPolicyName() {
	return "Progress-based";
    }

    /** It is always feasible to add another thread to a ProgressScheduler. */
    public boolean isFeasible() {
	return true;
    }

    /** It is always feasible to add another thread to a ProgressScheduler. */
    protected boolean isFeasible(Schedulable s, ReleaseParameters rp) {
	return true;
    }

    /** It is always feasible to add another thread to a ProgressScheduler. */
    protected void removeFromFeasibility(Schedulable schedulable) {}

    /** It is always feasible to add another thread to a ProgressScheduler. */
    public boolean setIfFeasible(Schedulable schedulable,
				 ReleaseParameters release,
				 MemoryParameters memory) {
	return true;
    }

    /** It is always feasible to add another thread to a ProgressScheduler. */
    public boolean setIfFeasible(Schedulable schedulable,
				 ReleaseParameters release,
				 MemoryParameters memory,
				 ProcessingGroupParameters group) {
	return true;
    }
    public void progressPoint(int point, long threadID, long time){
	int tid = (int) threadID + OFFSET;
	threadProgress[tid] = (point + 1)%numPoints; //When we call progressPoint(foo), we've finished foo and we're now in point foo+1
	pointTime[tid][point] = ((pointTime[tid][point] * 9) + time)/10; //weighted average, so that the time doesn't jump all around
	printTimingDebug(point, tid);
       	contextSwitch();
    }

    protected long chooseThread(long currentTime) {
	int count = 0;
	for (int newIndex = (currentIndex+1)%(maxIndex + 1); count<=(maxIndex + 1); newIndex=(newIndex+1)%(maxIndex + 1)) {
	    if (enabled[newIndex]) {
		currentIndex = newIndex;
		theQuanta = pointTime[currentIndex][threadProgress[currentIndex]]==0?1000:(((int) pointTime[currentIndex][threadProgress[currentIndex]]) * 10); //Give the thread 10 microsecs for every millisec it takes to run
		setQuanta(theQuanta);
      		return (currentIndex - OFFSET);
	    }
	    count++;
	}
	setQuanta(theQuanta); // Switch again after the specified time 
    	return (currentIndex - OFFSET);
    }

    protected void addThread(RealtimeThread thread) {
	addThread(thread.getUID());
    }

    protected void removeThread(RealtimeThread thread) {
	removeThread(thread.getUID());
    }

    protected void addThread(long threadID) {
	int tid = (int) threadID + OFFSET;
	if (++enabledThreads <= RUN_THREADS){
	    enabled[tid] = true;
	} else {
	    enabledThreads--;
	}
	maxIndex = tid>maxIndex?tid:maxIndex;
	if (maxIndex>=MAX_THREADS) {
	    throw new Error("Too many threads!");
      	}
	return;
    }

    protected void removeThread(long threadID) {
	int tid = (int) threadID + OFFSET;
	if (enabled[tid]) {
	    enabled[tid] = false;
	    enabledThreads--;
	    return;
	}
	throw new Error("No such thread!");
    }

    protected void disableThread(long threadID) {
	removeThread(threadID);
    }

    protected void enableThread(long threadID) {
	addThread(threadID);
    }

    /** ProgressScheduler is too dumb to deal w/periods. */
    protected void waitForNextPeriod(RealtimeThread rt) {
    }

    public String toString() {
	return getPolicyName();
    }
    
    /** Tells the scheduler how many threads should be anointed 
     * @param numAnointed How many threads we should anoint
     * @return  The number of threads that can be anointed.*/
     public int setAnointed(int numAnointed){
 	RUN_THREADS = numAnointed;
 	return RUN_THREADS;
     }
    /** Gets the number of anointed threads
     * @return The number of threads that can be anointed.*/
     public int getAnointed(){
 	return RUN_THREADS;
     }

     /** Tells the scheduler how many progressPoints there are 
     * @param numPoints How many progressPoints there are
     * @return  Number of progressPoints (max number progressPoint + 1)*/
     public int setNumPoints(int points){
 	numPoints = points;
 	return numPoints;
     }
   

    /** Print a whole bunch of debugging info */
    public void printNoAlloc() {
	boolean first = true;
	NoHeapRealtimeThread.print("[");
	for (int i=0; i<=maxIndex; i++) {
	    if (enabled[i]) {
		if (first) {
		    first = false;
		} else {
		    NoHeapRealtimeThread.print(" ");
		}
		NoHeapRealtimeThread.print(i);
	    }
	}
	NoHeapRealtimeThread.print("]");
	NoHeapRealtimeThread.print("\nMax index: ");
	NoHeapRealtimeThread.print(maxIndex);
	NoHeapRealtimeThread.print("\nNumber of Points: ");
	NoHeapRealtimeThread.print(numPoints);
	NoHeapRealtimeThread.print("\n&&&Thread Progress: [ ");
	for (int l=0; l<MAX_THREADS; l++) {
	    if (enabled[l]){
		NoHeapRealtimeThread.print(l);
		NoHeapRealtimeThread.print(":");
		NoHeapRealtimeThread.print(threadProgress[l]);
		NoHeapRealtimeThread.print(", ");
	    }
	}
	NoHeapRealtimeThread.print("]\n  ");
    }
    
    public void printTimingDebug(int point, int tid){
	NoHeapRealtimeThread.print("\n@@@ProgressPoint #"+point+" in thread "+tid);
	NoHeapRealtimeThread.print("\n@@@Thread timings:");
	for (int m=0; m<maxIndex; m++){
	    if (enabled[m] || pointTime[m][0] != 0){
		NoHeapRealtimeThread.print("\n@@@Thread ID "+m);
		NoHeapRealtimeThread.print(": (");
		for (int n=0; n<MAX_PROGRESSPOINTS; n++){
		    if (pointTime[m][n] != 0){
			NoHeapRealtimeThread.print("Point "+n+":"+pointTime[m][n]+", ");
		    }
		}
		NoHeapRealtimeThread.print(")");
	    }
	}
    }	
}

