// PriorityScheduler.java, created by cata
// Copyright (C) 2001 Catalin Francu <cata@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package javax.realtime;

// These are the runtime-constraints associated with a thread. 
public class ThreadConstraints {
	Schedulable schedulable;
    long threadID;
	RelativeTime workLeft;
	AbsoluteTime beginPeriod, endPeriod, deadline;
	// Is the deadline Relative?
}
