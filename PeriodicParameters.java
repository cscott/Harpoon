// PeriodicParameters.java, created by cata
// Copyright (C) 2001 Catalin Francu <cata@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.

package javax.realtime;
import java.util.Date;

public class PeriodicParameters extends ReleaseParameters {
    
    HighResolutionTime start;
    RelativeTime period;
    
    public PeriodicParameters(HighResolutionTime start, RelativeTime period,
			      RelativeTime cost, RelativeTime deadline,
			      AsyncEventHandler overrunHandler,
			      AsyncEventHandler missHandler) {
	super(cost, (deadline == null) ? period : deadline,
	      overrunHandler, missHandler);
	
	// For now, we leave start as it is. When start() is called on a thread,
	// then we will set start accordingly. The problem is what do we do when
	// several threads share the same ReleaseParameters.
	this.start = start;
	this.period = new RelativeTime(period);
    }
    
    public RelativeTime getPeriod() {
	return period;
    }
    
    public HighResolutionTime getStart() {
	return start;
    }

    // What is the scheduler that has to be feasible?
    public boolean setIfFeasible(RelativeTime period, RelativeTime cost, 
				 RelativeTime deadline) {
	// TODO
	return false;
    }

    public void setPeriod(RelativeTime period) {
	this.period.set(period);
    }
    
    public void setStart(HighResolutionTime start) {
	this.start.set(start);
    }
}
