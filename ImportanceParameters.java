// ImportanceParameters.java, created by cata
// Copyright (C) 2001 Catalin Francu <cata@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.

package javax.realtime;

/** Importance is an additional scheduling metric that may be used
 *  by some priority-based scheduling algorithms during overload
 *  conditions to differenciate execution order among threads of the
 *  same priority.
 */
public class ImportanceParameters extends PriorityParameters {
    
    private int importance;
    
    public ImportanceParameters(int priority, int importance) {
	super(priority);
	this.importance = importance;
    }

    /** Get the importance value. */
    public int getImportance() {
	return importance;
    }

    /** Set the importance. */
    public void setImportance(int importance) {
	this.importance = importance;
    }
    
    public String toString() {
	return "ImportanceParameters: Priority = " + getPriority()
	    + ", Importance = " + getImportance();
    }
}
