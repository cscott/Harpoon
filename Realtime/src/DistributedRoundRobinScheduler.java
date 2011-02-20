// DistributedRoundRobinScheduler.java, created by wbeebee
// Copyright (C) 2002 Wes Beebee <wbeebee@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package javax.realtime;

/** <code>DistributedRoundRobinScheduler</code> is a round-robin scheduler that
 *  preallocates all of its memory up front and runs in a distributed fashion.
 *
 * @author Wes Beebee <<a href="mailto:wbeebee@mit.edu">wbeebee@mit.edu</a>>
 */
public class DistributedRoundRobinScheduler extends Scheduler {
    static DistributedRoundRobinScheduler instance = null;

    public static final int MAX_THREADS = 1000;
    public static final int OFFSET = 2;

    private boolean[] enabled = new boolean[MAX_THREADS];
    private int currentThreadID;
    private int numThreads = 10;
    public String name = "30008";
    public String remoteName = "18.111.1.28:30007";
    
    private Object remoteObj = null;
    private long serverID = 0;

    protected DistributedRoundRobinScheduler() {
	super();
	setQuanta(0); // Start switching after a specified number of microseconds
    }

    /** Return an instance of a DistributedRoundRobinScheduler */
    public static DistributedRoundRobinScheduler instance() {
	if (instance == null) {
	    ImmortalMemory.instance().enter(new Runnable() {
		public void run() {
		    instance = new DistributedRoundRobinScheduler();
		}
	    });
	}

	return instance;
    }
    
    /** It is always feasible to add another thread to a DistributedRoundRobinScheduler. */
    protected void addToFeasibility(Schedulable schedulable) {}

    public void fireSchedulable(Schedulable schedulable) {
	schedulable.run();
    }

    /** Used to determine the policy of the <code>Scheduler</code>. */
    public String getPolicyName() {
	return "Distributed round robin";
    }

    /** It is always feasible to add another thread to a DistributedRoundRobinScheduler. */
    public boolean isFeasible() {
	return true;
    }

    /** It is always feasible to add another thread to a DistributedRoundRobinScheduler. */
    protected boolean isFeasible(Schedulable s, ReleaseParameters rp) {
	return true;
    }

    /** It is always feasible to add another thread to a DistributedRoundRobinScheduler. */
    protected void removeFromFeasibility(Schedulable schedulable) {}

    /** It is always feasible to add another thread to a DistributedRoundRobinScheduler. */
    public boolean setIfFeasible(Schedulable schedulable,
				 ReleaseParameters release,
				 MemoryParameters memory) {
	return true;
    }

    /** It is always feasible to add another thread to a DistributedRoundRobinScheduler. */
    public boolean setIfFeasible(Schedulable schedulable,
				 ReleaseParameters release,
				 MemoryParameters memory,
				 ProcessingGroupParameters group) {
	return true;
    }

    private long times = 0;

    protected long chooseThread(long currentTime) {
	if (connectionManagerStarted) {
	    if (serverID == 0) {
		serverID = bind(name);
	    }
	    
	    if (remoteObj == null) {
		remoteObj = resolve(remoteName);
	    }

	    generateDistributedEvent(remoteObj, currentTime, new byte[0]);
	}
	
	setQuanta(10000); // Switch again after a specified number of microseconds.
	
	for (int newIndex = (currentThreadID+1)%(numThreads+1); newIndex != currentThreadID; 
	     newIndex=(newIndex+1)%(numThreads+1)) {
	    if (enabled[newIndex]) {
		return (currentThreadID=newIndex)-OFFSET;
	    }
	}
	if (!enabled[currentThreadID]) {
	    NoHeapRealtimeThread.print("Deadlock!\n");
	    System.exit(-1);
	    return 0;
	}
	return currentThreadID-OFFSET;
    }

    protected void handleDistributedEvent(String name, long messageID, byte[] data) {
	NoHeapRealtimeThread.print("From ");
	NoHeapRealtimeThread.print(name);
	NoHeapRealtimeThread.print(": Time: ");
	NoHeapRealtimeThread.print(messageID);
	NoHeapRealtimeThread.print("\n");
    }

    protected void addThread(RealtimeThread thread) {
	addThread(thread.getUID());
    }

    protected void removeThread(RealtimeThread thread) {
	removeThread(thread.getUID());
    }

    protected void addThread(long threadID) {
	int tid = (int)(threadID+OFFSET);
	numThreads = (int)(tid>numThreads?tid:numThreads);

	if (enabled[tid]) {
	    NoHeapRealtimeThread.print("Already added thread #");
	    NoHeapRealtimeThread.print(threadID);
	    NoHeapRealtimeThread.print("!\n");
	    System.exit(-1);
	} else {
	    enabled[tid]=true;
	}
    }

    protected void removeThread(long threadID) {
	int tid = (int)(threadID+OFFSET);

	if (enabled[tid]) {
	    enabled[tid]=false;
	} else {
	    NoHeapRealtimeThread.print("No such thread #");
	    NoHeapRealtimeThread.print(threadID);
	    NoHeapRealtimeThread.print("!\n");
	    System.exit(-1);
	}
    }

    protected void disableThread(long threadID) {
	removeThread(threadID);
    }

    protected void enableThread(long threadID) {
	addThread(threadID);
    }

    /** DistributedRoundRobinScheduler is too dumb to deal w/periods. */
    protected void waitForNextPeriod(RealtimeThread rt) {
    }

    public String toString() {
	return getPolicyName();
    }

    public void printNoAlloc() {
	boolean first = true;
	NoHeapRealtimeThread.print("[");
	for (int i=0; i<=numThreads; i++) {
	    if (enabled[i]) {
		if (first) {
		    first = false;
		} else {
		    NoHeapRealtimeThread.print(" ");
		}
		NoHeapRealtimeThread.print(i-OFFSET);
	    }
	}
	NoHeapRealtimeThread.print("]");
    }
}
