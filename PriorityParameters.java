// PriorityParameters.java, created by cata
// Copyright (C) 2001 Catalin Francu <cata@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.

package javax.realtime;

public class PriorityParameters extends SchedulingParameters {
    
    private int priority;
    
    public PriorityParameters(int priority) {
	this.priority = priority;
    }
    
    public int getPriority() {
	return priority;
    }
    
    public void setPriority(int priority)
	throws IllegalArgumentException {
	this.priority = priority;
    }
    
    public String toString() {
	return "PriorityParameters: " + priority;
    }
}
