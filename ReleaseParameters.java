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
 *  values in the <code>ReleaseParameters</code> object affect <i>all</i>
 *  of the associated objects. Note that this is a one-to-many relationship
 *  and <i>not</i> a many-to-many.
 *  <p>
 *  <b>Caution:</b> This class is explicitly unsafe in multihtreaded
 *  situations when it is being changed. No synchronizations is done. It is
 *  assumed that users of this class who are mutating instances will be
 *  doing their own synchronization at a higher level.
 *  <p>
 *  <b>Caution:</b> The <code>cost</code> parameter time should be
 *  considered to be measured against the target platform.
 */
public class ReleaseParameters {
    RelativeTime cost;
    RelativeTime deadline;
    AsyncEventHandler overrunHandler;
    AsyncEventHandler missHandler;
    LinkedList schList = new LinkedList();

    /** Create a new instance of <code>ReleaseParameters</code>. */
    protected ReleaseParameters() {}

    /** Makes a copy of the argument. */
    protected ReleaseParameters(ReleaseParameters release) {
	this(release.cost, release.deadline, release.overrunHandler, release.missHandler);
    }
    
    /** Create a new instance of <code>ReleaseParameters</code> with
     *  the given parameter values.
     *
     *  @param cost Processing time units per interval. On implementations
     *              which can measure the amount of time a schedulable
     *              object is executed, this value is the maximum amount
     *              of time a schedulable object receives per interval.
     *              On implementations which cannot measure execution time,
     *              this value is used as a hint to the feasibility
     *              algorithm. On such systems it is not possible to
     *              determine when any particular object exceeds cost.
     *              Equivalent to <code>RelativeTime(0, 0)</code> if null.
     *  @param deadline The latest permissible completion time measured
     *                  from the release time of the associated invocation
     *                  of the schedulable object. Changing the deadline
     *                  might not take effect after the expiration of the
     *                  current deadline. More detail provided in the
     *                  subclasses.
     *  @param overrunHandler This handler is invoked if an invocation of
     *                        the schedulable object exceeds cost. Not
     *                        required for minimum implementation. If null,
     *                        nothing happens on the overrun condition, and
     *                        <code>waitForNextPeriod()</code> returns
     *                        false immediately and updates the start time
     *                        for the next period.
     *  @param missHandler This handler is invoked if the <code>run()</code>
     *                     method of the schedulable object is still
     *                     executing after the deadline has passed. Although
     *                     minimum implementations do not consider deadlines
     *                     in feasibility calculations, they must recognize
     *                     variable deadlines and invoke the miss handler as
     *                     appropriate. If null, nothing happens on the miss
     *                     deadline condition.
     */
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

    /** Gets the value of the cost field.
     *
     *  @return The value of cost.
     */
    public RelativeTime getCost() {
	return new RelativeTime(cost);
    }

    /** Gets a reference to the cost overrun handler.
     *
     *  @return A reference to the associated cost overrun handler.
     */
    public AsyncEventHandler getCostOverrunHandler() {
	return overrunHandler;
    }

    /** Gets the value of the deadline field.
     *
     *  @return The value of the deadline.
     */
    public RelativeTime getDeadline() {
	return new RelativeTime(deadline);
    }

    /** Gets a reference to the deadline miss handler.
     *
     *  @return A reference to the deadline miss handler.
     */
    public AsyncEventHandler getDeadlineMissHandler() {
	return missHandler;
    }

    /** Sets the cost value.
     *
     *  @param cost Processing time units per period or per minimum
     *              interarrival interval. On implementations which
     *              can measure the amount of time a schedulable
     *              object is executed, this value is the maximum
     *              amount of time a schedulable object receives per
     *              period or per minimum interarrival interval. On
     *              implementations which cannot measure execution
     *              time, this value is used as a hint to the
     *              feasibility algorithm. On such systems it is not
     *              possible to determine when any particular object
     *              exceeds or will exceed cost time units in a
     *              period or interval. Equivalent to
     *              <code>RelativeTime(0, 0)</code> if null.
     */
    public void setCost(RelativeTime cost) {
	this.cost.set(cost);
    }

    /** Sets the cost overrun handler.
     *
     *  @param handler This handler is invoked if an invocation of the
     *                 schedulable object attempts to exceed <code>cost</code>
     *                 time units in a period. Not required for minimum
     *                 implementation. See comments in <code>setCost()</code>.
     */
    public void setCostOverrunHandler(AsyncEventHandler handler) {
	this.overrunHandler = handler;
    }

    /** Sets the deadline value.
     *
     *  @param deadline The latest permissible completion time measured from
     *                  the release time of the associated invocation of the
     *                  schedulable object. For a minimum implementation for
     *                  purposes of feasibility analysis, the deadline is
     *                  equal to the period or minimum interarrival interval.
     *                  Other implementations may use this parameter to
     *                  compute execution eligibility.
     */
    public void setDeadline(RelativeTime deadline) {
	this.deadline.set(deadline);
    }

    /** Sets the deadline miss handler.
     *
     *  @param handler This handler is invoked if the <code>run()</code> method
     *                 of the schedulable object is still executing after the
     *                 deadline has passed. Although minimum implementations
     *                 do not consider deadlines in feasibility calculations,
     *                 they must recognize variable deadlines and invoke the
     *                 miss handler as appropriate.
     */
    public void setDeadlineMissHandler(AsyncEventHandler handler) {
	this.missHandler = handler;
    }

    /** This method appears in many classes in the RTSJ and with various parameters.
     *  The parameters are either new scheduling characteristics for an instance
     *  <code>Schedulable</code> or an instance of <code>Schedulable</code>. The
     *  method first performs a feasibility analysis using the new scheduling
     *  characteristics as replacements for the matching scheduling characteristics
     *  of either <code>this</code> or the given instance of <code>Schedulable</code>.
     *  If the resulting system is feasible the method replaces the current scheduling
     *  characteristics, of either <code>this</code> or the given instance of
     *  <code>Schedulable</code> as appropriate, with the new scheduling characteristics.
     *
     *  @param cost The poposed cost.
     *  @param deadline The proposed deadline.
     *  @return True, if the resulting system is feasible and the changes are made.
     *          False, if the resulting system is not feasible and no changes are made.
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
