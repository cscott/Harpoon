// AperiodicParameters.java, created by cata
// Copyright (C) 2001 Catalin Francu <cata@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.

package javax.realtime;

public class AperiodicParameters extends ReleaseParameters {
    Schedulable sch;
    
    public AperiodicParameters(RelativeTime cost, RelativeTime deadline,
			       AsyncEventHandler overrunHandler,
			       AsyncEventHandler missHandler) {
	super(cost,
	      (deadline == null) ? new RelativeTime(Long.MAX_VALUE, 999999) :
	      deadline,
	      overrunHandler, missHandler);
    }

    public boolean setIfFeasible(RelativeTime cost, RelativeTime deadline) {
	if (sch == null) return false;
	else return sch.setReleaseParametersIfFeasible(new AperiodicParameters(cost, deadline,
									       overrunHandler,
									       missHandler));
    }

    public Schedulable bindSchedulable(Schedulable sch) {
	Schedulable old_sch = this.sch;
	this.sch = sch;
	return sch;
    }

    public Schedulable unbindSchedulable() {
	return bindSchedulable(null);
    }
}
