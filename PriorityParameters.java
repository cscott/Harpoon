// PriorityParameters.java, created by cata
// Copyright (C) 2001 Catalin Francu <cata@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.

package javax.realtime;

/** Instances of this class should be assigned to threads that are
 *  managed by schedulers which use a single integer to determine
 *  execution order. The base scheduler required by this specification
 *  and represented by the class <code>PriorityScheduler</code> is
 *  such a scheduler.
 */
public class PriorityParameters extends SchedulingParameters {
    
    private int priority;

    public PriorityParameters(int priority) {
	this.priority = priority;
    }

    /** Get the priority */
    public int getPriority() {
	return priority;
    }

    /** Set the priority */
    public void setPriority(int priority)
	throws IllegalArgumentException {
	this.priority = priority;
    }
    
    public String toString() {
	return "PriorityParameters: " + priority;
    }
}
