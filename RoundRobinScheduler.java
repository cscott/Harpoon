// RoundRobinScheduler.java, created by wbeebee
// Copyright (C) 2002 Wes Beebee <wbeebee@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package javax.realtime;

import java.util.List;
import java.util.ArrayList;
import java.util.NoSuchElementException;
import java.util.Iterator;

/** <code>RoundRobinScheduler</code> is an example of the dumbest possible
 *  scheduler that actually switches between threads.
 *  It's great for debugging and to get an idea of what's required in a scheduler.
 */
public class RoundRobinScheduler extends Scheduler {
    static RoundRobinScheduler instance = null;
    RefList threadList = new RefList();
    RefList disabledThreads = new RefList();
    Iterator iterator = threadList.roundIterator();
    private static final boolean debug = true;

    protected RoundRobinScheduler() {
	super();
	setQuanta(1); // Start switching after 1 milliseconds
    }

    /** Return an instance of a RoundRobinScheduler */
    public static RoundRobinScheduler instance() {
	if (instance == null) {
	    ImmortalMemory.instance().enter(new Runnable() {
		public void run() {
		    instance = new RoundRobinScheduler();
		}
	    });
	}

	return instance;
    }
    
    /** It is always feasible to add another thread to a RoundRobinScheduler. */
    protected void addToFeasibility(Schedulable schedulable) {}

    public void fireSchedulable(Schedulable schedulable) {
	schedulable.run();
    }

    /** Used to determine the policy of the <code>Scheduler</code>. */
    public String getPolicyName() {
	return "Round robin";
    }

    /** It is always feasible to add another thread to a RoundRobinScheduler. */
    public boolean isFeasible() {
	return true;
    }

    /** It is always feasible to add another thread to a RoundRobinScheduler. */
    protected boolean isFeasible(Schedulable s, ReleaseParameters rp) {
	return true;
    }

    /** It is always feasible to add another thread to a RoundRobinScheduler. */
    protected void removeFromFeasibility(Schedulable schedulable) {}

    /** It is always feasible to add another thread to a RoundRobinScheduler. */
    public boolean setIfFeasible(Schedulable schedulable,
				 ReleaseParameters release,
				 MemoryParameters memory) {
	return true;
    }

    /** It is always feasible to add another thread to a RoundRobinScheduler. */
    public boolean setIfFeasible(Schedulable schedulable,
				 ReleaseParameters release,
				 MemoryParameters memory,
				 ProcessingGroupParameters group) {
	return true;
    }

    protected long chooseThread(long currentTime) {
	setQuanta(1); // Switch every 100 milliseconds
	try {
	    return ((Long)iterator.next()).longValue();
	} catch (NoSuchElementException e) {
	    return 0; // End of the program... or the beginning... 
	}
    }

    protected void addThread(RealtimeThread thread) {
	if (debug) NoHeapRealtimeThread.print("\naddThread1("+thread.getUID()+")");
	if (debug&&(thread.getUID()<0)) {
	    NoHeapRealtimeThread.print("assert: addThread 2");
	    print();
	    System.exit(-1);
	}
	if (debug&&disabledThreads.contains(thread.getUID())) {
	    NoHeapRealtimeThread.print("assert: addThread ");
	    print();
	    System.exit(-1);
	}
	threadList.add(thread.getUID());
    }

    protected void removeThread(RealtimeThread thread) {
	if (debug) NoHeapRealtimeThread.print("\nremoveThread1("+thread.getUID()+")");
	if (debug&&(thread.getUID()<0)) {
	    NoHeapRealtimeThread.print("assert: removeThread 2");
	    print();
	    System.exit(-1);
	}
	if (debug&&(!threadList.contains(thread.getUID()))) {
	    NoHeapRealtimeThread.print("assert: removeThread ");
	    print();
	    System.exit(-1);
	}
	threadList.remove(thread.getUID());
    }

    protected void addThread(long threadID) {
	if (debug) NoHeapRealtimeThread.print("\naddThread2("+threadID+")");
	if (debug&&(threadID>0)) {
	    NoHeapRealtimeThread.print("assert: addThread 1");
	    print();
	    System.exit(-1);
	}
	threadList.add(threadID);
    }

    protected void removeThread(long threadID) {
	if (debug) NoHeapRealtimeThread.print("\nremoveThread2("+threadID+")");
	if (debug&&(threadID>0)) {
	    NoHeapRealtimeThread.print("assert: removeThread 1");
	    print();
	    System.exit(-1);
	}
	threadList.remove(threadID);
    }

    protected void disableThread(long threadID) {
	if (debug) NoHeapRealtimeThread.print("\ndisableThread("+threadID+")");
	if (debug&&(!threadList.contains(threadID))) {
	    NoHeapRealtimeThread.print("assert: disableThread ");
	    print();
	    System.exit(-1);
	}
	threadList.remove(threadID);
	disabledThreads.add(threadID);
    }

    protected void enableThread(long threadID) {
	if (debug) NoHeapRealtimeThread.print("\nenableThread("+threadID+")");
	if (debug&&(!disabledThreads.contains(threadID))) {
	    NoHeapRealtimeThread.print("assert: enableThread ");
	    print();
	    System.exit(-1);
	}
	threadList.add(threadID);
	disabledThreads.remove(threadID);
    }

    /** RoundRobinScheduler is too dumb to deal w/periods. */
    protected void waitForNextPeriod(RealtimeThread rt) {
    }

    public String toString() {
	return "["+threadList+","+disabledThreads+"]";
    }
}
