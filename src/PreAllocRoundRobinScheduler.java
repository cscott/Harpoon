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
 *  It's great for getting an idea of what's required in a scheduler.
 */
public class PreAllocRoundRobinScheduler extends Scheduler {
    static PreAllocRoundRobinScheduler instance = null;
    

    protected PreAllocRoundRobinScheduler() {
	super();
	setQuanta(0); // Start switching after a specified number of microseconds
    }

    /** Return an instance of a RoundRobinScheduler */
    public static PreAllocRoundRobinScheduler instance() {
	if (instance == null) {
	    ImmortalMemory.instance().enter(new Runnable() {
		public void run() {
		    instance = new PreAllocRoundRobinScheduler();
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
	return "Pre-alloc round robin";
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
	setQuanta(1000); // Switch again after a specified number of microseconds.
	
    }

    protected void addThread(RealtimeThread thread) {
	
	threadList.add(thread.getUID());
    }

    protected void removeThread(RealtimeThread thread) {
	threadList.remove(thread.getUID());
    }

    protected void addThread(long threadID) {
	threadList.add(threadID);
    }

    protected void removeThread(long threadID) {
	threadList.remove(threadID);
    }

    protected void disableThread(long threadID) {
	threadList.remove(threadID);
    }

    protected void enableThread(long threadID) {
	threadList.add(threadID);
    }

    /** RoundRobinScheduler is too dumb to deal w/periods. */
    protected void waitForNextPeriod(RealtimeThread rt) {
    }

    public String toString() {
	return "";
    }

    public void printNoAlloc() {
	NoHeapRealtimeThread.print("[");
	threadList.printNoAlloc();
	NoHeapRealtimeThread.print("]");
    }
}
