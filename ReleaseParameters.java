// ReleaseParameters.java, created by cata
// Copyright (C) 2001 Catalin Francu <cata@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.

package javax.realtime;

import java.util.LinkedList;
import java.util.Iterator;

/** The abstract top-level class for release characteristics of threads.
 *  When a reference to a <code>ReleaseParameters</code> is given as a
 *  parameter to a constructor, the <code>ReleaseParameters</code> object
 *  becomes bound to the object being created. Changes to the values in
 *  the <code>ReleaseParameters</code> object affect the constructed
 *  object. If given to more than one constructor, then changes to the
 *  values in the <code>ReleaseParameters</code> object affect ALL of the
 *  associated objects. Note that this is a one-to-many relationship and
 *  NOT a many-to-many.
 */
public class ReleaseParameters {
    RelativeTime cost;
    RelativeTime deadline;
    AsyncEventHandler overrunHandler;
    AsyncEventHandler missHandler;
    LinkedList schList = new LinkedList();
    
    protected ReleaseParameters() {}

    /** Makes a copy of the argument. */
    protected ReleaseParameters(ReleaseParameters release) {
	this(release.cost, release.deadline, release.overrunHandler, release.missHandler);
    }
    
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

    /** Get the cost. */
    public RelativeTime getCost() {
	return new RelativeTime(cost);
    }
    /** Get the cost overrun handler. */
    public AsyncEventHandler getCostOverrunHandler() {
	return overrunHandler;
    }

    /** Get the deadline. */
    public RelativeTime getDeadline() {
	return new RelativeTime(deadline);
    }

    /** Get the deadline miss handler. */
    public AsyncEventHandler getDeadlineMissHandler() {
	return missHandler;
    }

    /** Set the cost value. */
    public void setCost(RelativeTime cost) {
	this.cost.set(cost);
    }

    /** Set the cot overrun handler */
    public void setCostOverrunHandler(AsyncEventHandler handler) {
	this.overrunHandler = handler;
    }

    /** Set the deadline value. */
    public void setDeadline(RelativeTime deadline) {
	this.deadline.set(deadline);
    }

    /** Set the deadline miss handler. */
    public void setDeadlineMissHandler(AsyncEventHandler handler) {
	this.missHandler = handler;
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
	if (!b) {   // something is not feasible
	    setCost(old_cost);   /// returning the values back
	    setDeadline(old_deadline);
	    for (it = schList.iterator(); it.hasNext(); )   // undoing all changes
		((Schedulable)it.next()).setReleaseParameters(this);
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
