// ReleaseParameters.java, created by cata
// Copyright (C) 2001 Catalin Francu <cata@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.

package javax.realtime;

public class ReleaseParameters {
    RelativeTime cost;
    RelativeTime deadline;
    AsyncEventHandler overrunHandler;
    AsyncEventHandler missHandler;
    
    protected ReleaseParameters() {}
    
    protected ReleaseParameters(RelativeTime cost, RelativeTime deadline,
				AsyncEventHandler overrunHandler,
				AsyncEventHandler missHandler) {
	if (cost != null)
	    this.cost = new RelativeTime(cost);
	else
	    this.cost = new RelativeTime(0, 0);
	this.deadline = new RelativeTime(deadline);
	this.overrunHandler = overrunHandler;
	this.missHandler = missHandler;
    }
    
    public RelativeTime getCost() {
	return new RelativeTime(cost);
    }
    
    public AsyncEventHandler getCostOverrunHandler() {
	return overrunHandler;
    }
    
    public RelativeTime getDeadline() {
	return new RelativeTime(deadline);
    }
    
    public AsyncEventHandler getDeadlineMissHandler() {
	return missHandler;
    }
    
    public void setCost(RelativeTime cost) {
	this.cost.set(cost);
    }
    
    public void setCostOverrunHandler(AsyncEventHandler handler) {
	this.overrunHandler = handler;
    }
    
    public void setDeadline(RelativeTime deadline) {
	this.deadline.set(deadline);
    }
    
    public void setDeadlineMissHandler(AsyncEventHandler handler) {
	this.missHandler = handler;
    }

    // What is the scheduler that has to be feasible?
    public boolean setIfFeasible(RelativeTime cost,
				 RelativeTime deadline) {
	// TODO
	return false;
    }
}
