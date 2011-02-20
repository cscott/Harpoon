// PeriodicParameters.java, created by cata
// Copyright (C) 2001 Catalin Francu <cata@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.

package javax.realtime;

import java.util.Date;
import java.util.LinkedList;
import java.util.Iterator;

/** This release parameter indicates that the <code>waitForNextPeriod()</code>
 *  method on the associated <code>Schedulable</code> object will be unblocked
 *  at the start of each period. When a reference to a
 *  <code>PeriodicParameters</code> object is given as a parameter to a
 *  constructor, the <code>PeriodicParameters</code> object becomes bound to
 *  the object being created. Changes to the values in the
 *  <code>PeriodicParameters</code> object affect the constructed object. If
 *  given to more than one constructor then changes to the values in the
 *  <code>PeriodicParameters</code> object affect <i>all</i> the associated objects.
 *  Note that this is a one-to-many relationship, <i>not</i> many-to-many.
 *  <p>
 *  <b>Caution:</b> This class is explicitly unsafe in multithreaded situations
 *  when it is being changed. No synchronizations is done. It is assumed that users
 *  of this class who are mutating instances will be doing their own synchronization
 *  at a higher level.
 */
public class PeriodicParameters extends ReleaseParameters {
    
    HighResolutionTime start;
    RelativeTime period;
    LinkedList schList = new LinkedList();

    /** Create a <code>PeriodicParameters</code> object.
     *
     *  @param start Time at which the first period begins. If a
     *               <code>RelativeTime</code>, this time is relative to the
     *               first time the schedulable object becomes schedulable
     *               (<i>schedulable time</i>) (e.g., when <code>start()</code>
     *               is called on a thread). If an <code>AbsoluteTime</code>
     *               and it is before the schedulable time, start is equivalent
     *               to the schedulable time.
     *  @param period The period is the interval between successive unblocks
     *                of <code>RealtimeThread.waitForNextPeriod()</code>. Must
     *                be greater than zero when entering feasibility analysis.
     *  @param cost Processing time per period. On implementations which can
     *              measure the amount of time a schedulable object is executed,
     *              this value is the maximum amount of time a schedulable object
     *              receives per period. On implementations which cannot measure
     *              execution time, this value is used as a hint to the feasibility
     *              algorithm. On such systems it is not possible to determine when
     *              any particular object exceeds or will exceed cost time units in
     *              a period. Equivalent to <code>RelativeTime(0, 0)</code> if null.
     *  @param deadline The latest permissible completion time measured from the
     *                  release time of the associated invocation of the schedulable
     *                  object. For a minimum implementation for purposes of 
     *                  feasibility analysis, the deadline is equal to the period.
     *                  Other implementations may use this parameter to compute
     *                  execution eligibility. If null, deadline will equal the period.
     *  @param overrunHandler This handler is invoked if an invocation of the
     *                        schedulable object exceeds cost in the given period.
     *                        Not required for minimum implementation. If null,
     *                        nothing happens on the overrun condition.
     *  @param missHandler This handler is invoked if the <code>run()</code> method of
     *                     the schedulable object is still executing after the deadline
     *                     has passed. Although minimum implementations do not consider
     *                     deadlines in feasibility calculations, they must recognize
     *                     variable deadlines and invoke the miss handler as appropriate.
     *                     If null, nothing happens on the miss deadline condition.
     */
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

    /** Gets the period.
     *
     *  @return The current value in <code>period</code>.
     */
    public RelativeTime getPeriod() {
	return period;
    }

    /** Gets the start time.
     *
     *  @return The current value in <code>start</code>.
     */
    public HighResolutionTime getStart() {
	return start;
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
    }

    /** Sets the period.
     *
     *  @param period The value to which <code>period</code> is set.
     */
    public void setPeriod(RelativeTime period) {
	this.period.set(period);
    }

    /** Sets the start time.
     *
     *  @param start The value to which <code>start</code> is set.
     */
    public void setStart(HighResolutionTime start) {
	this.start.set(start);
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
