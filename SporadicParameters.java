// AperiodicParameters.java, created by cata
// Copyright (C) 2001 Catalin Francu <cata@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.

package javax.realtime;

public abstract class SporadicParameters extends AperiodicParameters {
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
    
    public String getArrivalTimeQueueOverflowBeharior() {
	return arrivalTimeQueueOverflowBehavior;
    }

    public int getInitialArrivalTimeQueueLength() {
	return initialArrivalTimeQueueLength;
    }

    public RelativeTime getMinimumInterarrival() {
	return minInterarrival;
    }

    public String getMitViolationBehavior() {
	return mitViolationBehavior;
    }

    public void setArrivalTimeQueueOverflowBehavior(String behavior) {
	arrivalTimeQueueOverflowBehavior = new String(behavior);
    }

    public void setInitialArrivalTimeQueueLength(int initial) {
	initialArrivalTimeQueueLength = initial;
    }

    public void setMinimumInterarrival(RelativeTime minInterarrival) {
	minInterarrival.set(minInterarrival);
    }

    public void setMitViolationBehavior(String behavior) {
	mitViolationBehavior = new String(behavior);
    }

    public boolean setIfFeasible(RelativeTime interarrival,
				 RelativeTime cost, RelativeTime deadline) {
	if (sch == null) return false;
	else {  // this class is abstract, so we have to find another approach to this method...
	    RelativeTime old_interarrival = this.minInterarrival;
	    RelativeTime old_cost = this.cost;
	    RelativeTime old_deadline = this.deadline;
	    setMinimumInterarrival(interarrival);
	    this.cost = cost;
	    this.deadline = deadline;
	    boolean b = sch.setReleaseParametersIfFeasible(this);
	    if (!b) {
		setMinimumInterarrival(old_interarrival);
		this.cost = old_cost;
		this.deadline = old_deadline;
	    }
	    return b;
	}
    }

    public Schedulable bindSchedulable(Schedulable sch) {
	Schedulable old_sch = this.sch;
	this.sch = sch;
	return old_sch;
    }

    public Schedulable unbindSchedulable() {
	return bindSchedulable(null);
    }
}
