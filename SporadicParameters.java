// AperiodicParameters.java, created by cata
// Copyright (C) 2001 Catalin Francu <cata@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.

package javax.realtime;

import java.util.LinkedList;
import java.util.Iterator;

/** A notice to the scheduler that the associated schedulable object's run
 *  method witll be released aperiodically but with a minimum time between
 *  releases. When a reference toa <code>SporadicParameters</code> object
 *  is given as a parameter to a constructor, the <code>SporadicParameters</code>
 *  object becomes bound to the object being created. Changes to the values
 *  in the <code>SporadicParameters</code> object affect the constructed
 *  object. If given to more than one constructor, then changes to the
 *  values in the <code>SporadicParameters</code> object affect <i>all</i>
 *  of the associated objects. Note that this is a one-to-many relationship
 *  and <i>not</i> a many-to-many.
 */
public class SporadicParameters extends AperiodicParameters {
    private RelativeTime minInterarrival;
    private String arrivalTimeQueueOverflowBehavior;
    private int initialArrivalTimeQueueLength;
    private String mitViolationBehavior;
    Schedulable sch;

    // Fields from specs
    public static final String arrivalTimeQueueOverflowExcept =
	"ARRIVAL_TIME_QUEUE_OVERFLOW_EXCEPT";
    public static final String arrivalTimeQueueOverflowIgnore =
	"ARRIVAL_TIME_QUEUE_OVERFLOW_IGNORE";
    public static final String arrivalTimeQueueOverflowReplace =
	"ARRIVAL_TIME_QUEUE_OVERFLOW_REPLACE"; 
    public static final String arrivalTimeQueueOverflowSave =
	"ARRIVAL_TIME_QUEUE_OVERFLOW_SAVE";
    public static final String mitViolationExcept =
	"MIT_VIOLATION_EXCEPT";
    public static final String mitViolationIgnore =
	"MIT_VIOLATION_IGNORE";
    public static final String mitViolationReplace =
	"MIT_VIOLATION_REPLACE";
    public static final String mitViolationSave =
	"MIT_VIOLATION_SAVE";
    
    public SporadicParameters(RelativeTime minInterarrival, RelativeTime cost,
			      RelativeTime deadline,
			      AsyncEventHandler overrunHandler,
			      AsyncEventHandler missHandler) {
	super(cost, deadline, overrunHandler, missHandler);
	this.minInterarrival = new RelativeTime(minInterarrival);
    }

    /** Get the behavior of the arrival time queue in the event of an overflow. */
    public String getArrivalTimeQueueOverflowBeharior() {
	return arrivalTimeQueueOverflowBehavior;
    }

    /** Get the initial number of elements the arrival time queue can hold. */
    public int getInitialArrivalTimeQueueLength() {
	return initialArrivalTimeQueueLength;
    }

    /** Get the minimum interarrival time. */
    public RelativeTime getMinimumInterarrival() {
	return minInterarrival;
    }

    /** Get the arrival time queue behavior in the event of a minimum
     *  interarrival time violation.
     */
    public String getMitViolationBehavior() {
	return mitViolationBehavior;
    }

    /** Set the behavior of the arrival time queue in the case where the insertion
     *  of a new element would make the queue size greater than the initial size
     *  given in this.
     */
    public void setArrivalTimeQueueOverflowBehavior(String behavior) {
	arrivalTimeQueueOverflowBehavior = new String(behavior);
    }

    /** Set the initial number of elements the arrival time queue can hold
     *  without lengthening the queue.
     */
    public void setInitialArrivalTimeQueueLength(int initial) {
	initialArrivalTimeQueueLength = initial;
    }

    /** Set the minimum interarrival time. */
    public void setMinimumInterarrival(RelativeTime minInterarrival) {
	minInterarrival.set(minInterarrival);
    }

    /** Set the behavior of the arrival time queue in the case where the new
     *  arrival time is closer to the previous arrival time than the minimum
     *  interarrival time given in this.
     */
    public void setMitViolationBehavior(String behavior) {
	mitViolationBehavior = new String(behavior);
    }

    /** Returns true if, after considering the values of the parameters, the task set
     *  would still be feasible. In this case the values of the parameters are changed.
     *  Returns false if, after considering the values of the parameters, the task set
     *  would not be feasible. In this case the values of the parameters are not changed.
     */
    public boolean setIfFeasible(RelativeTime interarrival,
				 RelativeTime cost,
				 RelativeTime deadline) {
	boolean b = true;
	Iterator it = schList.iterator();
	RelativeTime old_interarrival = this.minInterarrival;
	RelativeTime old_cost = this.cost;
	RelativeTime old_deadline = this.deadline;
	setMinimumInterarrival(interarrival);
	setCost(cost);
	setDeadline(deadline);
	while (b && it.hasNext())
	    b = ((Schedulable)it.next()).setReleaseParametersIfFeasible(this);
	if (!b) {
	    setMinimumInterarrival(old_interarrival);
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
