// AperiodicParameters.java, created by cata
// Copyright (C) 2001 Catalin Francu <cata@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.

package javax.realtime;

public abstract class SporadicParameters extends AperiodicParameters {
    private RelativeTime minInterarrival;
    private String arrivalTimeQueueOverflowBehavior;
    private int initialArrivalTimeQueueLength;
    private String mitViolationBehavior;

    // Fields from specs
    public static final String arrivalTimeQueueOverflowExcept = "";
    public static final String arrivalTimeQueueOverflowIgnore = "";
    public static final String arrivalTimeQueueSverflowReplace = ""; 
    public static final String arrivalTimeQueueOverflowSave = "";
    public static final String mitViolationExcept = "";
    public static final String mitViolationIgnore = "";
    public static final String mitViolationReplace = "";
    public static final String mitViolationSave = "";
    
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

    public void setMinInterarrival(RelativeTime minInterarrival) {
	minInterarrival.set(minInterarrival);
    }

    public setMitViolationBehavior(String behavior) {
	mitViolationBehavior = new String(behavior);
    }

    public boolean setIfFeasible(RelativeTime interarrival,
				 RelativeTime cost, RelativeTime deadline) {
	// TODO
	return false;
    }
}
