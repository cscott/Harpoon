// AperiodicParameters.java, created by cata
// Copyright (C) 2001 Catalin Francu <cata@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.

package javax.realtime;

public class AperiodicParameters extends ReleaseParameters {
    
    public AperiodicParameters(RelativeTime cost, RelativeTime deadline,
			       AsyncEventHandler overrunHandler,
			       AsyncEventHandler missHandler) {
	super(cost,
	      (deadline == null) ? new RelativeTime(Long.MAX_VALUE, 999999) :
	      deadline,
	      overrunHandler, missHandler);
    }

    // What is the scheduler that has to be feasible?
    public boolean setIfFeasible(RelativeTime cost, RelativeTime deadline) {
	// TODO
	return false;
    }
}
