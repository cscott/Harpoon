// ProcessingGroupParameters.java, created by cata
// Copyright (C) 2001 Catalin Francu <cata@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.

package javax.realtime;
import java.util.Date;

public class ProcessingGroupParameters {
    HighResolutionTime start;
    RelativeTime period;
    RelativeTime cost;
    RelativeTime deadline;
    AsyncEventHandler overrunHandler;
    AsyncEventHandler missHandler;

    public ProcessingGroupParameters(HighResolutionTime start,
				     RelativeTime period, RelativeTime cost,
				     RelativeTime deadline,
				     AsyncEventHandler overrunHandler,
				     AsyncEventHandler missHandler) {
	this.start = start;
	this.period = period;
	this.cost = cost;
	this.deadline = deadline;
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
    
    public RelativeTime getPeriod() {
	return new RelativeTime(period);
    }
    
    public HighResolutionTime getStart() {
	return start;
    }
    
    public void setCost(RelativeTime cost) {
	this.cost = cost;
    }
    
    public void setCostOverrunHandler(AsyncEventHandler handler) {
	overrunHandler = handler;
    }
    
    public void setDeadline(RelativeTime deadline) {
	this.deadline = deadline;
    }
    
    public void setDeadlineMissHandler(AsyncEventHandler handler) {
	missHandler = handler;
    }

    public void setPeriod(RelativeTime period) {
	this.period = period;
    }
    
    public void setStart(HighResolutionTime start) {
	this.start = start;
    }

    // What is the scheduler that has to be feasible?
    public boolean setIfFeasible(RelativeTime period, RelativeTime cost,
				 RelativeTime deadline) {
	// TODO
	return false;
    }
}
