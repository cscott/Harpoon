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
 *  <p>
 *  <b>Caution:</b> This class is explicitly unsafe in multithreaded situations
 *  when it is being changed. No synchronization is done. It is assumed that
 *  users of this class who are mutating instances will be doing their own
 *  synchronization at a higher level.
 *  <p>
 *  Correct initiation of the deadline miss and cost overrun handlers require
 *  that the underlying system know the arrival time of each sporadic task.
 *  For an instance of <code>RealtimeThread</code> the arrival time is the
 *  time at which the <code>start()</code> is invoked. For other instances of
 *  <code>Schedulable</code> it may be required for the implementation to
 *  save the arrival times. For instances of <code>AsyncEventHandler</code>
 *  with a <code>ReleaseParameters</code> type of <code>SporadicParameters</code>
 *  the implementation must maintain a queue of monotonically incraesing arrival
 *  times which correspond to the execution of the <code>fire()</code> method of
 *  the instance of <code>AsyncEvent</code> bound to the instance of
 *  <code>AsyncEventHandler</code>
 *  <p>
 *  This class allows the application to specify one of four possible behaviors
 *  that indicate what to do if an arrival occurs that is closer in time to the
 *  previous arrival than the value given in this class as minimum interarrival
 *  time, what to do if, for any reason, the queue overflows, and the initial
 *  size of the queue.
 */
public class SporadicParameters extends AperiodicParameters {
    private RelativeTime minInterarrival;
    private String arrivalTimeQueueOverflowBehavior;
    private int initialArrivalTimeQueueLength;
    private String mitViolationBehavior;
    Schedulable sch;

    // Fields from specs
    
    /** If an arrival time occurs and should be queued but the queue already
     *  holds a number of times equal to the initial queue length defined by
     *  <code>this</code> then the <code>fire()</code> method shall throw a
     *  <code>ResourceLimitError</code>. If the arrival time is a result of
     *  a happening to which the instance of <code>AsyncEventHandler</code>
     *  is bound then the arrival time is ignored.
     */
    public static final String arrivalTimeQueueOverflowExcept =
	"ARRIVAL_TIME_QUEUE_OVERFLOW_EXCEPT";
    /** If an arrival time occurs and should be queued but the queue already
     *  holds a number of times equal to the initial queue length defined by
     *  <code>this</code> then the arrival time is ignored.
     */
    public static final String arrivalTimeQueueOverflowIgnore =
	"ARRIVAL_TIME_QUEUE_OVERFLOW_IGNORE";
    /** If an arrival time occurs and should be queued but the queue already
     *  holds a number of times equal to the initial queue length defined by
     *  <code>this</code> then the previous arrival time is overwritten by
     *  the new arrival time. However, the new time is adjusted so that the
     *  difference between it and the previous time is equal to the minimum
     *  intearrival time.
     */
    public static final String arrivalTimeQueueOverflowReplace =
	"ARRIVAL_TIME_QUEUE_OVERFLOW_REPLACE";
    /** If an arrival time occurs and should be queued but the queue already
     *  holds a number of times equal to the initial queue length defined by
     *  <code>this</code> then the queue is lengthened and the arrival time
     *  is saved.
     */
    public static final String arrivalTimeQueueOverflowSave =
	"ARRIVAL_TIME_QUEUE_OVERFLOW_SAVE";
    /** If an arrival time for any instance of <code>Schedulable</code> which
     *  has <code>this</code> as its instance of <code>ReleaseParameters</code>
     *  occurs at a time les then the minimum interarrival time defined here
     *  then the <code>fire()</code> method shall throw 
     *  <code>MITViolationException</code>. If the arrival time is a result of
     *  a happening to which the instance of <code>AsyncEventHandler</code> is
     *  bound then the arrival time is ignored.
     */
    public static final String mitViolationExcept =
	"MIT_VIOLATION_EXCEPT";
    /** If an arrival time for any instance of <code>Schedulable</code> which
     *  has <code>this</code> as its instance of </code>ReleaseParameters</code>
     *  occurs at a time less then the minimum interarrival time defined here
     *  then the new arrival time is ignored.
     */
    public static final String mitViolationIgnore =
	"MIT_VIOLATION_IGNORE";
    /** If an arrival time for any instance of <code>Schedulable</code> which
     *  has <code>this</code> as its instance of <code>ReleaseParameters</code>
     *  occurs at a time less than the minimum interarrival time defined here
     *  then the previous arrival time is overwritten with the new arrival time.
     */
    public static final String mitViolationReplace =
	"MIT_VIOLATION_REPLACE";
    /** If an arrival time for any instance of <code>Schedulble</code> which
     *  has <code>this</code> as its instance of <code>ReleaseParameters</code>
     *  occurs at a time less then the minimum interarrival time defined here
     *  then the new arrival time is added to the queue of arrival times.
     *  However, the new time is adjusted so that the difference between it
     *  and the previous time is equal to the minimum interarrival time.
     */
    public static final String mitViolationSave =
	"MIT_VIOLATION_SAVE";

    /** Create a <code>SporadicParameters</code> object.
     *
     *  @param minInterarrival The release times of the schedulable object will
     *                         occur no closer that this interval. Must be greater
     *                         than zero when entering feasibility analysis.
     *  @param cost Processing time per minimum interarrival interval. On
     *              implementations which can measure the amount of time a
     *              schedulable object is executed, this value is the maximum
     *              amount of time a schedulable object receives per interval. On
     *              implementations which cannot measure execution time, this value
     *              is used s a hint to the feasibility altorithm. On such systems
     *              it is not possible to determine when any particular object
     *              exceeds cost. Equivalent to <code>RelativeTime(0, 0)</code> if null.
     *  @param deadline The latest permissible completion time measured from the
     *                  release time of the associated invocation of the schedulable
     *                  object. For a minimum implementation for purposes of feasibility
     *                  anaysis, the deadline is equal to the minimum interarrival
     *                  interval. Other implementations may use this parameter to compute
     *                  execution eligibility. If null, deadline will equal the
     *                  minimum interarrival time.
     *  @param overrunHandler This handler is invoked if an invocation of the schedulable
     *                        object exceeds cost. Not required for minimum implementation.
     *                        If null, nothing happens on the overrun condition.
     *  @param missHandler This handler is invoked if the <code>run()</code> method of the
     *                     schedulable object is still executing after the deadline has
     *                     passed. Although minimum implementations do not consider
     *                     deadlines in feasibility calculations, they must recognize
     *                     variable deadlines and invoke the miss handler as appropriate.
     *                     If null, nothing happens on the miss deadline condition.
     */
    public SporadicParameters(RelativeTime minInterarrival, RelativeTime cost,
			      RelativeTime deadline,
			      AsyncEventHandler overrunHandler,
			      AsyncEventHandler missHandler) {
	super(cost, deadline, overrunHandler, missHandler);
	this.minInterarrival = new RelativeTime(minInterarrival);
    }

    /** Get the behavior of the arrival time queue in the event of an overflow.
     *
     *  @return The behavior of the arrival time queue as a string.
     */
    public String getArrivalTimeQueueOverflowBeharior() {
	return arrivalTimeQueueOverflowBehavior;
    }

    /** Get the initial number of elements the arrival time queue can hold.
     *
     *  @return The initial length of the queue.
     */
    public int getInitialArrivalTimeQueueLength() {
	return initialArrivalTimeQueueLength;
    }

    /** Get the minimum interarrival time.
     *
     *  @return The minimum interarrival time.
     */
    public RelativeTime getMinimumInterarrival() {
	return minInterarrival;
    }

    /** Get the arrival time queue behavior in the event of a minimum
     *  interarrival time violation.
     *
     *  @return The minimum interarrival time violation behavior as a string.
     */
    public String getMitViolationBehavior() {
	return mitViolationBehavior;
    }

    /** Set the behavior of the arrival time queue in the case where the insertion
     *  of a new element would make the queue size greater than the initial size
     *  given in this.
     *
     *  @param behavior A string representing the behavior.
     */
    public void setArrivalTimeQueueOverflowBehavior(String behavior) {
	arrivalTimeQueueOverflowBehavior = new String(behavior);
    }

    /** Set the initial number of elements the arrival time queue can hold
     *  without lengthening the queue.
     *
     *  @param initial The initial length of the queue.
     */
    public void setInitialArrivalTimeQueueLength(int initial) {
	initialArrivalTimeQueueLength = initial;
    }

    /** Set the minimum interarrival time.
     *
     *  @param minInterarrival The release times of the schedulable object will
     *                         occur no closer than this interval. Must be greater
     *                         than zero when entering feasibility analysis.
     */
    public void setMinimumInterarrival(RelativeTime minInterarrival) {
	minInterarrival.set(minInterarrival);
    }

    /** Set the behavior of the arrival time queue in the case where the new
     *  arrival time is closer to the previous arrival time than the minimum
     *  interarrival time given in this.
     *
     *  @param behavior A string representing the behavior.
     */
    public void setMitViolationBehavior(String behavior) {
	mitViolationBehavior = new String(behavior);
    }

    /** This method appears in many classes in the RTSJ and with varous parameters.
     *  The parameters are either new scheduling characteristics for an instance
     *  <code>Schedulable</code> or an instance of <code>Schedulable</code>. The
     *  method first performs a feasibility analysis using the new scheduling
     *  characteristics of either <code>this</code> or the given instance of
     *  <code>Schedulable</code>. If the resulting system is feasible the method
     *  replaces the current scheduling characteristics, of either <code>this</code>
     *  or the given instance of <code>Schedulable</code> as appropriate, with the
     *  new scheduling characteristics.
     *
     *  @param interarrival The proposed interarrival time.
     *  @param cost The proposed cost.
     *  @param deadline The proposed deadline.
     *  @return True, if the resulting system is feasible and the changes are made.
     *          False, if the resulting system is not feasible and no changes are made.
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
