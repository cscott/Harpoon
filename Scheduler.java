// Scheduler.java, created by cata
// Copyright (C) 2001 Catalin Francu <cata@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.

package javax.realtime;
import java.util.HashSet;

public abstract class Scheduler {
    // shoud this field be here?
    private static Scheduler defaultScheduler;
    
    
    protected Scheduler() {}

    protected abstract boolean addtoFeasibility(Schedulable schedulable) {
	return false;
    }

    public abstract void fireSchedulable(Schedulable schedulable);

    public static Scheduler getDefaultScheduler() {}
    
    public abstract String getPolicyName();

    public abstract boolean isFeasible() {
	return false;
    }

    protected abstract boolean removeFromFeasibility(Schedulable schedulable) {
	return false;
    }

    public static void setDefaultScheduler(Scheduler scheduler) {
	// should we do this?
	defaultScheduler = scheduler;
    }

    public boolean setIfFeasible(Schedulable schedulable,
				 ReleaseParameters release,
				 MemoryParameters memory) {
	return false;
    }

    public boolean setIfFeasible(Schedulable schedulable,
				 ReleaseParameters release,
				 MemoryParameters memory,
				 ProcessingGroupParameters group) {
	return false;
    }
}
