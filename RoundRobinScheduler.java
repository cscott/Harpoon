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

    protected RoundRobinScheduler() {
	super();
	setQuanta(1000); // Start switching after 10 milliseconds
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
	setQuanta(100000); // Switch every 100 milliseconds
	try {
	    return ((Long)iterator.next()).longValue();
	} catch (NoSuchElementException e) {
	    return 0; // End of the program... or the beginning... 
	}
    }

    protected void addThread(RealtimeThread thread) {
	threadList.add(thread.getUID());
    }

    protected void removeThread(RealtimeThread thread) {
	threadList.remove(thread.getUID());
    }

    protected void disableThread(long threadID) {
	threadList.remove(threadID);
	disabledThreads.add(threadID);
    }

    protected void enableThread(long threadID) {
	threadList.add(threadID);
	disabledThreads.remove(threadID);
    }

    protected boolean noThreads() {
	return threadList.isEmpty();
    }
    
    /** RoundRobinScheduler is too dumb to deal w/periods. */
    protected void waitForNextPeriod(RealtimeThread rt) {
    }

    public String toString() {
	return "["+threadList+","+disabledThreads+"]";
    }
}
