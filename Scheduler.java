// Scheduler.java, created by cata
// Copyright (C) 2001 Catalin Francu <cata@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.

package javax.realtime;
import java.util.HashSet;

public abstract class Scheduler {
    protected static Scheduler defaultScheduler;
    
    
    protected Scheduler() {}

    protected abstract boolean addToFeasibility(Schedulable schedulable);

    public abstract void fireSchedulable(Schedulable schedulable);

    public static Scheduler getDefaultScheduler() {
	return defaultScheduler;
    }
    
    public abstract String getPolicyName();

    public abstract boolean isFeasible();

    protected abstract boolean removeFromFeasibility(Schedulable schedulable);

    public static void setDefaultScheduler(Scheduler scheduler) {
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
