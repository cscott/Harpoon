// AperiodicParameters.java, created by cata
// Copyright (C) 2001 Catalin Francu <cata@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.

package javax.realtime;

import java.util.LinkedList;
import java.util.Iterator;

/** The release parameter object characterizes a schedulable object that may become
 *  active at atny time. When a reference to a <code>AperiodicParameters</code>
 *  object is given as a parameter to a constructor the
 *  <code>AperiodicParameters</code> object becomes bound to the object being created.
 *  Changed to the values in the <code>AperiodicParameters</code> object affect the
 *  constructed object. If given to more than one constructor then changes to the
 *  values in the <code>AperiodicParameters</code> object affect ALL of the associated
 *  objects. Note that this is a one-to-many relationship and NOT a many-to-many.
 */
public class AperiodicParameters extends ReleaseParameters {
    LinkedList schList = new LinkedList();
    
    public AperiodicParameters(RelativeTime cost, RelativeTime deadline,
			       AsyncEventHandler overrunHandler,
			       AsyncEventHandler missHandler) {
	super(cost,
	      (deadline == null) ? new RelativeTime(Long.MAX_VALUE, 999999) :
	      deadline,
	      overrunHandler, missHandler);
    }

    /** Returns true if, after considering the values of the parameters, the task set
     *  would still be feasible. In this case the values of the parameters are changed.
     *  Returns false if, after considering the values of the parameters, the task set
     *  would not be feasible. In this case the values of the parameters are not changed.
     */
    public boolean setIfFeasible(RelativeTime cost,
				 RelativeTime deadline) {
	boolean b = true;
	Iterator it = schList.iterator();
	RelativeTime old_cost = this.cost;
	RelativeTime old_deadline = this.deadline;
	setCost(cost);
	setDeadline(deadline);
	while (b && it.hasNext())
	    b = ((Schedulable)it.next()).setReleaseParametersIfFeasible(this);
	if (!b) {
	    setCost(old_cost);
	    setDeadline(old_deadline);
	}
	return b;
    }

    public boolean bindSchedulable(Schedulable sch) {
	return schList.add(sch);
    }

    public boolean unbindSchedulable(Schedulable sch) {
	return schList.remove(sch);
    }
}
