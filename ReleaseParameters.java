// ReleaseParameters.java, created by cata
// Copyright (C) 2001 Catalin Francu <cata@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.

package javax.realtime;

public class ReleaseParameters {
    RelativeTime cost;
    RelativeTime deadline;
    AsyncEventHandler overrunHandler;
    AsyncEventHandler missHandler;
    Schedulable sch;
    
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

    public boolean setIfFeasible(RelativeTime cost,
				 RelativeTime deadline) {
	if (sch == null) return false;
	else return sch.setReleaseParametersIfFeasible(new ReleaseParameters(cost, deadline,
									     overrunHandler,
									     missHandler));
    }

    public Schedulable bindSchedulable(Schedulable sch) {
	Schedulable old_sch = this.sch;
	this.sch = sch;
	return old_sch;
    }

    public Schedulable unbindSchedulable() {
	return bindSchedulable(null);
    }
}
