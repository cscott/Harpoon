// ProcessingGroupParameters.java, created by cata
// Copyright (C) 2001 Catalin Francu <cata@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.

package javax.realtime;
import java.util.Date;
import java.util.LinkedList;
import java.util.Iterator;

/** This is associated with one or more schedulable objects for which the
 *  system guarantees that the associated objects will not be fiven more
 *  time per period than indicated by cost. For all threads with a 
 *  reference to an instance of <code>ProcessingGroupParameters p</code>
 *  and a reference to an instance of <code>AperiodicParameters</code> no
 *  more than p.cost will be allocated to the execution of these threads
 *  in each interval of time given by p.period after the time indicated
 *  by p.start. When a reference to a <code>ProcessingGroupParameters</code>
 *  object is given as a parameter to a constructor the
 *  <code>ProcessingGroupParameters</code> object becomes bound to the
 *  object being created. Changes to the values in the
 *  <code>ProcessingGroupParameters</code> object affect the constructed
 *  object. If given to more than one constructor, then changes to the
 *  values in the <code>ProcessingGroupParameters</code> object affect
 *  ALL of the associated objects. Note that this is a one-to-many
 *  relationship and NOT a many-to-many.
 */
public class ProcessingGroupParameters {
    HighResolutionTime start;
    RelativeTime period;
    RelativeTime cost;
    RelativeTime deadline;
    AsyncEventHandler overrunHandler;
    AsyncEventHandler missHandler;
    LinkedList schList = new LinkedList();
    
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

    /** Get the cost value. */
    public RelativeTime getCost() {
	return new RelativeTime(cost);
    }

    /** Get the cost overrun handler. */
    public AsyncEventHandler getCostOverrunHandler() {
	return overrunHandler;
    }

    /** Get the deadline value. */
    public RelativeTime getDeadline() {
	return new RelativeTime(deadline);
    }

    /** Get the deadline missed handler. */
    public AsyncEventHandler getDeadlineMissHandler() {
	return missHandler;
    }

    /** Get the period. */
    public RelativeTime getPeriod() {
	return new RelativeTime(period);
    }

    /** Get the start time. */
    public HighResolutionTime getStart() {
	return start;
    }

    /** Set the cost value. */
    public void setCost(RelativeTime cost) {
	this.cost = cost;
    }

    /** Set the cost overrun handler. */
    public void setCostOverrunHandler(AsyncEventHandler handler) {
	overrunHandler = handler;
    }

    /** Set the deadline value. */
    public void setDeadline(RelativeTime deadline) {
	this.deadline = deadline;
    }

    /** Set the deadline miss handler. */
    public void setDeadlineMissHandler(AsyncEventHandler handler) {
	missHandler = handler;
    }

    /** Set the period. */
    public void setPeriod(RelativeTime period) {
	this.period = period;
    }

    /** Set the start time. */
    public void setStart(HighResolutionTime start) {
	this.start = start;
    }

    /** Returns true if, after considering the values of the parameters, the task set
     *  would still be feasible. In this case the values of the parameters are changed.
     *  Returns false if, after considering the values of the parameters, the task set
     *  would not be feasible. In this case the values of the parameters are not changed.
     */
    public boolean setIfFeasible(RelativeTime period,
				 RelativeTime cost,
				 RelativeTime deadline) {
	boolean b = true;
	Iterator it = schList.iterator();
	RelativeTime old_period = this.period;
	RelativeTime old_cost = this.cost;
	RelativeTime old_deadline = this.deadline;
	setPeriod(period);
	setCost(cost);
	setDeadline(deadline);
	while (b && it.hasNext())
	    b = ((Schedulable)it.next()).setProcessingGroupParametersIfFeasible(this);
	if (!b) {   // something is not feasible
	    setPeriod(old_period);   // returning the values back
	    setCost(old_cost);
	    setDeadline(old_deadline);
	    for (it = schList.iterator(); it.hasNext(); )   // undoing all changes
		((Schedulable)it.next()).setProcessingGroupParameters(this);
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
