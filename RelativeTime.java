// RelativeTime.java, created by cata
// Copyright (C) 2001 Catalin Francu <cata@mit.edu>
//    taking over Bryan Fink's code
// Licensed under the terms of the GNU GPL; see COPYING for details.

package javax.realtime;
import java.util.Date;

public class RelativeTime extends HighResolutionTime {
    /** An object that represents a time interval millis/1E3+nanos/1E9
     *  seconds long. It generally is used to represent a time relative
     * to now.
     */
    
    public static RelativeTime ZERO = new RelativeTime(0, 0);
    
    public RelativeTime() {
	this(0, 0);
    }
    
    // Notice: we normalize the time, converting every 10^6 ns to 1 ms.
    public RelativeTime(long millis, int nanos) {
	super();
	set(millis, nanos);
    }
    
    public RelativeTime(RelativeTime t)	{
	this(t.getMilliseconds(), t.getNanoseconds());
    }
    
    public AbsoluteTime absolute(Clock clock) {
	return new AbsoluteTime(clock.getTime().add(this));
    }

    public AbsoluteTime absolute(Clock clock, AbsoluteTime destination)	{
	if (destination == null)
	    destination = new AbsoluteTime();
	if (clock == null)
	    clock = Clock.getRealtimeClock();
	
	destination.set(clock.getTime().add(this));
	return destination;
    }
    
    public RelativeTime add(long millis, int nanos) {
	return new RelativeTime(getMilliseconds() + millis,
				getNanoseconds() + nanos);
    }
    
    public RelativeTime add(long millis, int nanos, RelativeTime destination) {
	if (destination == null)
	    destination = new RelativeTime();
	
	destination.set(getMilliseconds() + millis,
			getNanoseconds() + nanos);
	return destination;
    }
    
    // Why does this one have to be final?
    public final RelativeTime add(RelativeTime time) {
	return new RelativeTime(getMilliseconds() + time.getMilliseconds(),
				getNanoseconds() + time.getNanoseconds());
    }
    
    public RelativeTime add(RelativeTime time, RelativeTime destination) {
	if (destination == null)
	    destination = new RelativeTime();
	
	destination.set(getMilliseconds() + time.getMilliseconds(),
			getNanoseconds() + time.getNanoseconds());
	return destination;
    }
    
    // REQUIRES: destination != null
    public void addInterarrivalTo(AbsoluteTime destination) {
	destination.add(this, destination);
    }
    
    public RelativeTime getInterarrivalTime() {
	// TODO

	return null;
    }

    public RelativeTime getInterarrivalTime(RelativeTime destination) {
	// TODO

	return null;
    }
    
    public RelativeTime relative(Clock clock) {
	// TODO

	return this;
    }

    public RelativeTime relative(Clock clock, RelativeTime destination) {
	// TODO

	return destination;
    }

    // Not in specs, but must be defined, since it was declared in HighResolutionTime
    public RelativeTime relative(Clock clock, HighResolutionTime time) {
	// TODO

	return null;
    }
    
    public final RelativeTime subtract(RelativeTime time) {
	return new RelativeTime(getMilliseconds() - time.getMilliseconds(),
				getNanoseconds() - time.getNanoseconds());
    }
    
    public RelativeTime subtract(RelativeTime time, RelativeTime destination) {
	if (destination == null)
	    destination = new RelativeTime();
	
	destination.set(getMilliseconds() - time.getMilliseconds(),
			getNanoseconds() - time.getNanoseconds());
	return destination;
    }
    
    public String toString() {
	return "RelativeTime: millis: " + getMilliseconds() +
	    " nanos: " + getNanoseconds();
    }
}
