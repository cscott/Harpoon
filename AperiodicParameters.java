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
 *  constructed object. If given to more than one constructor then changes to the values
 *  in the <code>AperiodicParameters</code> object affect <i>all</i> of the associated
 *  objects. Note that this is a one-to-many relationship and <i>not</i> a many-to-many.
 *  <p>
 *  <b>Caution:</b> This class is explicitly unsafe in multithreaded situations when it
 *  is being changed. No synchronization is done. It is assumed that users of this class
 *  who are mutating instances will be doing their own synchronization at a higher level.
 */
public class AperiodicParameters extends ReleaseParameters {
    LinkedList schList = new LinkedList();

    /** Create an <code>AperiodicParameters</code> object.
     *
     *  @param cost Processing time per invocation. On implementations which can measure
     *              the amount of time a schedulable object is executed, this value is the
     *              maximum amount of time a schedulable object receives. On implementations
     *              which cannot measure execution time, this value is used as a hint to the
     *              feasibility algorithm. On such systems it is not possible to determine
     *              when any particular object exceeds cost. Equivalent to
     *              <code>RelativeTime(0, 0)</code> if null.
     *  @param deadline The latest permissible completion time measured from the release
     *                  time of the associated invocation of the schedulable object. Not
     *                  used in feasibility analysis for minimum implementation. If null,
     *                  the deadline will be <code>RelativeTime(Long.MAX_VALUE, 999999)</code>.
     *  @param overrunHandler This handler is invoked if an invocation of the schedulable
     *                        object exceeds cost. Not required for minimum implementation.
     *                        If null, nothing happens on the overrun condition.
     *  @param missHandler This handler is invoked if the <code>run()</code> method of the
     *                     schedulable object is still executing after the deadline has passed.
     *                     Although minimum implementations do not consider deadlines in
     *                     feasibility calculations, they must recognize variable deadlines
     *                     and invoke the miss handler as appropriate. If null, nothing
     *                     happens on the miss deadline condition.
     */
    public AperiodicParameters(RelativeTime cost, RelativeTime deadline,
			       AsyncEventHandler overrunHandler,
			       AsyncEventHandler missHandler) {
	super(cost,
	      (deadline == null) ? new RelativeTime(Long.MAX_VALUE, 999999) :
	      deadline,
	      overrunHandler, missHandler);
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
     *  @param cost The proposed cost.
     *  @param deadline The proposed deadline.
     *  @return True, if the resulting system is feasible and the changes are made.
     *          False, if the resulting system is not feasible and no changes are made.
     */
    public boolean setIfFeasible(RelativeTime cost,
				 RelativeTime deadline) {
	boolean b = true;
	for (Iterator it = schList.iterator(); it.hasNext(); ) {
	    Schedulable sch = (Schedulable)it.next();
	    Scheduler sched = sch.getScheduler();
	    if (!sched.isFeasible(sch, new AperiodicParameters(cost, deadline, overrunHandler, missHandler))) {
		b = false;
		break;
	    }
	}

	if (b) {
	    setCost(cost);
	    setDeadline(deadline);
	}
	return b;
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
