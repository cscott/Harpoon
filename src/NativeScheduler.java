// NativeScheduler.java, created by wbeebee
// Copyright (C) 2002 Wes Beebee <wbeebee@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.

package javax.realtime;

/** A <code>NativeScheduler</code> represents a scheduler that is
 *  implemented entirely in native code.  Examples may include 
 *  heavy threads and user threads.
 */

public class NativeScheduler extends Scheduler {
    private static NativeScheduler instance = null;

    protected NativeScheduler() {
	super();
    } 

    public static NativeScheduler instance() {
	if (instance == null) {
	    ImmortalMemory.instance().enter(new Runnable() {
		public void run() {
		    instance = new NativeScheduler();
		}
	    });
	}
	return instance;
    }

    protected void addToFeasibility(Schedulable schedulable) {}

    public void fireSchedulable(Schedulable schedulable) {
	schedulable.run();
    }

    public String getPolicyName() {
	return "native scheduler";
    }

    public boolean isFeasible() {
	return true;
    }

    protected boolean isFeasible(Schedulable s, ReleaseParameters rp) {
	return true;
    }

    protected void removeFromFeasibility(Schedulable schedulable) {}

    public boolean setIfFeasible(Schedulable schedulable,
				 ReleaseParameters release,
				 MemoryParameters memory) {
	return true;
    }

    public boolean setIfFeasible(Schedulable schedulable,
				 ReleaseParameters release,
				 MemoryParameters memory,
				 ProcessingGroupParameters group) {
	return true;
    }

    protected long chooseThread(long currentTime) {
	throw new Error("chooseThread should never be called!");
    }

    protected void addThread(RealtimeThread thread) {}

    protected void removeThread(RealtimeThread thread) {}

    protected void addThread(long threadID) {
	throw new Error("addThread should never be called!");
    }

    protected void removeThread(long threadID) {
	throw new Error("removeThread should never be called!");
    }

    protected void disableThread(long threadID) {
	throw new Error("disableThread should never be called!");
    }
    
    protected void enableThread(long threadID) {
	throw new Error("enableThread should never be called!");
    }

    protected void waitForNextPeriod(RealtimeThread rt) {}


}
