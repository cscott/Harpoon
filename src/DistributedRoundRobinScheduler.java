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
    private long[] threadList = new long[MAX_THREADS];
    private int currentIndex = 0;
    private int maxIndex = 1;
    private final String name = "18.111.1.28:30001";
    private final String remoteName = "18.111.1.28:30002";
    
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
	setQuanta(1000); // Switch again after a specified number of microseconds.
	
	if (!RealtimeThread.RTJ_init_in_progress) {
	    if (serverID == 0) {
		serverID = bind(name);
	    }
	    
	    if (remoteObj == null) {
		remoteObj = resolve(remoteName);
	    }

	    if ((++times)%100 == 0) {
		generateDistributedEvent(remoteObj, currentTime, new byte[0]);
	    }
	}
	
	for (int newIndex = (currentIndex+1)%maxIndex; newIndex != currentIndex; 
	     newIndex=(newIndex+1)%maxIndex) {
	    if (threadList[newIndex]!=0) {
		return threadList[currentIndex = newIndex];
	    }
	}
	return threadList[currentIndex];
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
	for (int i=0; i<maxIndex; i++) {
	    if (threadList[i]==0) {
		threadList[i] = threadID;
		return;
	    }
	}
	if ((++maxIndex)>MAX_THREADS) {
	    throw new Error("Too many threads!");
	} else {
	    threadList[maxIndex-1] = threadID;
	}
    }

    protected void removeThread(long threadID) {
	for (int i=0; i<maxIndex; i++) {
	    if (threadList[i]==threadID) {
		threadList[i] = 0;
		return;
	    }
	}
	throw new Error("No such thread!");
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
	for (int i=0; i<maxIndex; i++) {
	    if (threadList[i]!=0) {
		if (first) {
		    first = false;
		} else {
		    NoHeapRealtimeThread.print(" ");
		}
		NoHeapRealtimeThread.print(threadList[i]);
	    }
	}
	NoHeapRealtimeThread.print("]");
    }
}
