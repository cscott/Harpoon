// DebugScheduler.java, created by wbeebee
// Copyright (C) 2002 Wes Beebee <wbeebee@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package javax.realtime;

import java.util.List;
import java.util.ArrayList;
import java.util.NoSuchElementException;
import java.util.Iterator;

/** <code>DebugScheduler</code> is a scheduler designed to test the rest of the system.
 *  It produces a prodigious amount of debugging output as well as asserting many invariants.
 */
public class DebugScheduler extends Scheduler {
    static DebugScheduler instance = null;
    RefList threadList = new RefList();
    RefList disabledThreads = new RefList();
    Iterator iterator = threadList.roundIterator();

    protected DebugScheduler() {
	super();
	setQuanta(0); // Start switching after a specified number of milliseconds
    }

    /** Return an instance of a DebugScheduler */
    public static DebugScheduler instance() {
	if (instance == null) {
	    ImmortalMemory.instance().enter(new Runnable() {
		public void run() {
		    instance = new DebugScheduler();
		}
	    });
	}

	return instance;
    }
    
    /** It is always feasible to add another thread to a DebugScheduler. */
    protected void addToFeasibility(Schedulable schedulable) {}

    public void fireSchedulable(Schedulable schedulable) {
	schedulable.run();
    }

    /** Used to determine the policy of the <code>Scheduler</code>. */
    public String getPolicyName() {
	return "Debug scheduler";
    }

    /** It is always feasible to add another thread to a DebugScheduler. */
    public boolean isFeasible() {
	return true;
    }

    /** It is always feasible to add another thread to a DebugScheduler. */
    protected boolean isFeasible(Schedulable s, ReleaseParameters rp) {
	return true;
    }

    /** It is always feasible to add another thread to a DebugScheduler. */
    protected void removeFromFeasibility(Schedulable schedulable) {}

    /** It is always feasible to add another thread to a DebugScheduler. */
    public boolean setIfFeasible(Schedulable schedulable,
				 ReleaseParameters release,
				 MemoryParameters memory) {
	return true;
    }

    /** It is always feasible to add another thread to a DebugScheduler. */
    public boolean setIfFeasible(Schedulable schedulable,
				 ReleaseParameters release,
				 MemoryParameters memory,
				 ProcessingGroupParameters group) {
	return true;
    }

    protected long chooseThread(long currentTime) {
	setQuanta(0); // Switch again after a specified number of milliseconds.
	try {
	    return ((Long)iterator.next()).longValue();
	} catch (NoSuchElementException e) {
	    NoHeapRealtimeThread.print("No threads left to run!");
	    System.exit(-1);
	    return 0;
	}
    }

    protected void addThread(RealtimeThread thread) {
	NoHeapRealtimeThread.print("\naddThread1("+thread.getUID()+")");
	if (thread.getUID()<0) {
	    NoHeapRealtimeThread.print("assert: addThread 2");
	    print();
	    System.exit(-1);
	}
	if (disabledThreads.contains(thread.getUID())) {
	    NoHeapRealtimeThread.print("assert: addThread ");
	    print();
	    System.exit(-1);
	}
	threadList.add(thread.getUID());
    }

    protected void removeThread(RealtimeThread thread) {
	NoHeapRealtimeThread.print("\nremoveThread1("+thread.getUID()+")");
	if (thread.getUID()<0) {
	    NoHeapRealtimeThread.print("assert: removeThread 2");
	    print();
	    System.exit(-1);
	}
	if (!threadList.contains(thread.getUID())) {
	    NoHeapRealtimeThread.print("assert: removeThread ");
	    print();
	    System.exit(-1);
	}
	threadList.remove(thread.getUID());
    }

    protected void addThread(long threadID) {
	NoHeapRealtimeThread.print("\naddThread2("+threadID+")");
	if (threadID>0) {
	    NoHeapRealtimeThread.print("assert: addThread 1");
	    print();
	    System.exit(-1);
	}
	threadList.add(threadID);
    }

    protected void removeThread(long threadID) {
	NoHeapRealtimeThread.print("\nremoveThread2("+threadID+")");
	if (threadID>0) {
	    NoHeapRealtimeThread.print("assert: removeThread 1");
	    print();
	    System.exit(-1);
	}
	threadList.remove(threadID);
    }

    protected void disableThread(long threadID) {
	NoHeapRealtimeThread.print("\ndisableThread("+threadID+")");
	if (!threadList.contains(threadID)) {
	    NoHeapRealtimeThread.print("assert: disableThread ");
	    print();
	    System.exit(-1);
	}
	threadList.remove(threadID);
	disabledThreads.add(threadID);
    }

    protected void enableThread(long threadID) {
	NoHeapRealtimeThread.print("\nenableThread("+threadID+")");
	if (!disabledThreads.contains(threadID)) {
	    NoHeapRealtimeThread.print("assert: enableThread ");
	    print();
	    System.exit(-1);
	}
	threadList.add(threadID);
	disabledThreads.remove(threadID);
    }

    protected void waitForNextPeriod(RealtimeThread rt) {
    }

    public String toString() {
	return "["+threadList+","+disabledThreads+"]";
    }

    public void printNoAlloc() {
	NoHeapRealtimeThread.print("[");
	threadList.printNoAlloc();
	NoHeapRealtimeThread.print(",");
	disabledThreads.printNoAlloc();
	NoHeapRealtimeThread.print("]");
    }
}
