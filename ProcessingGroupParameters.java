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
 *  <i>all</i> of the associated objects. Note that this is a one-to-many
 *  relationship and <i>not</i> a many-to-many.
 *  <p>
 *  <b>Caution:</b> This class is explicitly unsafe in multithreaded
 *  situations when it is being changed. No synchronization is done. It is
 *  assumed that users of this class who are mutating instances will be
 *  doing their own synchronization at a higher level.
 *  <p>
 *  <b>Caution:</b> The <code>cost</code> parameter time should be
 *  considered to be measured against the target platform.
 */
public class ProcessingGroupParameters {
    HighResolutionTime start;
    RelativeTime period;
    RelativeTime cost;
    RelativeTime deadline;
    AsyncEventHandler overrunHandler;
    AsyncEventHandler missHandler;
    LinkedList schList = new LinkedList();

    /** Create a <code>ProcessingGroupParameters</code> objects.
     *
     *  @param start Time at which the first period begins.
     *  @param period The period is the interval between successive unblocks
     *                of <code>waitForNextPeriod()</code>.
     *  @param cost Processing time per period.
     *  @param deadline The latest permissible completion time measured from
     *                  the start of the current period. Changing the 
     *                  deadline might not take effect after the expiration
     *                  of the current deadline.
     *  @param overrunHandler This handler is invoked if the <code>run()</code>
     *                        method of the schedulable object of the previous
     *                        period is still executing at the start of the
     *                        current period.
     *  @param missHandler This handler is invoked if the <code>run()</code>
     *                     method of the schedulable object is still executing
     *                     after the deadline has passed.
     */
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

    /** Gets the value of <code>cost</code>.
     *
     *  @return The value of <code>cost</code>.
     */
    public RelativeTime getCost() {
	return new RelativeTime(cost);
    }

    /** Get the cost overrun handler.
     *
     *  @return A reference to an instance of <code>AsyncEventHandler</code>
     *          that is cost overrun handler of <code>this</code>.
     */
    public AsyncEventHandler getCostOverrunHandler() {
	return overrunHandler;
    }

    /** Gets the value of <code>deadline</code>.
     *
     *  @return A reference to an instance of <code>RelativeTime</code> that
     *          is the deadline of <code>this</code>.
     */
    public RelativeTime getDeadline() {
	return new RelativeTime(deadline);
    }

    /** Gets the deadline missed handler.
     *
     *  @return A reference to an instance of <code>AsynchEventHandler</code>
     *          that is deadline miss handler of <code>this</code>.
     */
    public AsyncEventHandler getDeadlineMissHandler() {
	return missHandler;
    }

    /** Gets the value of <code>period</code>.
     *
     *  @return A reference of an instance of <code>RelativeTime</code> that
     *          represents the value of <code>period</code>.
     */
    public RelativeTime getPeriod() {
	return new RelativeTime(period);
    }

    /** Gets the value of <code>start</code>.
     *
     *  @return A reference to an instance of <code>HighResolutionTime</code>
     *          that represents the value of <code>start</code>.
     */
    public HighResolutionTime getStart() {
	return start;
    }

    /** Sets the value of <code>cost</code>.
     *
     *  @param cost The new value for <code>cost</code>.
     */
    public void setCost(RelativeTime cost) {
	this.cost = cost;
    }

    /** Sets the cost overrun handler.
     *
     *  @param handler This handler is invoked if the <code>run()</code>
     *                 method of any of the schedulable objects attempt to
     *                 execute for more than <code>cost</code> time units
     *                 in any period.
     */
    public void setCostOverrunHandler(AsyncEventHandler handler) {
	overrunHandler = handler;
    }

    /** Sets the value of <code>deadline</code>.
     *
     *  @param deadline The new value for <code>deadline</code>.
     */
    public void setDeadline(RelativeTime deadline) {
	this.deadline = deadline;
    }

    /** Sets the deadline miss handler.
     *
     *  @param handler This handler is invoked if the <code>run()</code>
     *                 method of any of the schedulable objects still
     *                 expect to execute after the deadline has passed.
     */
    public void setDeadlineMissHandler(AsyncEventHandler handler) {
	missHandler = handler;
    }

    /** Sets the value of <code>period</code>.
     *
     *  @param period The new value for <code>period</code>.
     */
    public void setPeriod(RelativeTime period) {
	this.period = period;
    }

    /** Sets the value of <code>start</code>.
     *
     *  @param start The new value for <code>start</code>.
     */
    public void setStart(HighResolutionTime start) {
	this.start = start;
    }

    /** This method appears in many classes in the RTSJ and with various parameters.
     *  The parameters are either new scheduling characteristics for an instance
     *  <code>Schedulable</code> or an instance of <code>Schedulable</code>. The
     *  method first performs a feasibility analysis using the new scheudling
     *  characteristics as replacements for the matching scheduling characteristics
     *  of either <code>this</code> or the given instance of <code>Schedulable</code>
     *  as appropriate, with the new scheduling characteristics.
     *
     *  @param period The proposed period.
     *  @param cost The proposed cost.
     *  @param deadline The proposed deadline.
     *  @return True, if the resulting system is feasible and the changes are made.
     *          False, if the resulting system is not feasible and no changes are made.
     */
    public boolean setIfFeasible(RelativeTime period,
				 RelativeTime cost,
				 RelativeTime deadline) {
	boolean b = true;
	for (Iterator it = schList.iterator(); it.hasNext(); ) {
	    Schedulable sch = (Schedulable)it.next();
	    Scheduler sched = sch.getScheduler();
	    if (!sched.isFeasible(sch, new PeriodicParameters(getStart(), period, cost, deadline,
							      overrunHandler, missHandler))) {
		b = false;
		break;
	    }
	}

	if (b) {
	    setPeriod(period);
	    setCost(cost);
	    setDeadline(deadline);
	}
	return b;

// 	boolean b = true;
// 	Iterator it = schList.iterator();
// 	RelativeTime old_period = this.period;
// 	RelativeTime old_cost = this.cost;
// 	RelativeTime old_deadline = this.deadline;
// 	setPeriod(period);
// 	setCost(cost);
// 	setDeadline(deadline);
// 	while (b && it.hasNext())
// 	    b = ((Schedulable)it.next()).setProcessingGroupParametersIfFeasible(this);
// 	if (!b) {   // something is not feasible
// 	    setPeriod(old_period);   // returning the values back
// 	    setCost(old_cost);
// 	    setDeadline(old_deadline);
// 	    for (it = schList.iterator(); it.hasNext(); )   // undoing all changes
// 		((Schedulable)it.next()).setProcessingGroupParameters(this);
// 	}
// 	return b;
    }

    /** Informs <code>this</code> that there is one more instance of <code>Schedulable</code>
     *  that uses <code>this</code> as its <code>ReleaseParameters</code>.
     */
    public boolean bindSchedulable(Schedulable sch) {
	return schList.add(sch);
    }

    /** Informs <code>this</code> that <code>Schedulable sch</code>
     *  that uses <code>this</code> as its <code>ReleaseParameters</code>.
     */
    public boolean unbindSchedulable(Schedulable sch) {
	return schList.remove(sch);
    }
}
