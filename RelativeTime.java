// RelativeTime.java, created by cata
// Copyright (C) 2001 Catalin Francu <cata@mit.edu>
//    taking over Bryan Fink's code
// Licensed under the terms of the GNU GPL; see COPYING for details.

package javax.realtime;

import java.util.Date;

/** An object that represents a time interval millis/1E3+nanos/1E9
 *  seconds long. It generally is used to represent a time relative
 * to now.
 */
public class RelativeTime extends HighResolutionTime {
    
    public static RelativeTime ZERO = new RelativeTime(0, 0);
    
    /** Equivalent to <code>new RelativeTime(0, 0)</code>. */
    public RelativeTime() {
	this(0, 0);
    }

    /** Constructs a <code>RelativeTime</code> object which means a time
     *  <code>millis</code> milliseconds plus <code>nanos</code> nanoseconds
     *  past the <code>Clock</code> time.
     *
     *  Notice: the time is normalized by converting every 10^6 ns to 1 ms.
     */
    public RelativeTime(long millis, int nanos) {
	set(millis, nanos);
    }
    
    /** Make a new <code>RelativeTime</code> object from the given
     *  <code>RelativeTime</code> object.
     */
    public RelativeTime(RelativeTime t)	{
	this(t.getMilliseconds(), t.getNanoseconds());
    }

    /** Converts this time to an absolute time. */
    public AbsoluteTime absolute(Clock clock) {
	return new AbsoluteTime(clock.getTime().add(this));
    }

    /** Converts this time to an absolute time and stores it into <code>destination</code>.
     *  If <code>destination</code> is null, allocates a new object.
     */
    public AbsoluteTime absolute(Clock clock, AbsoluteTime destination)	{
	if (destination == null)
	    destination = new AbsoluteTime();
	if (clock == null)
	    clock = Clock.getRealtimeClock();
	
	destination.set(clock.getTime().add(this));
	return destination;
    }

    /** Add a specific number of milliseconds and nanoseconds to <code>this</code>.
     *  A new object is allocated.
     */
    public RelativeTime add(long millis, int nanos) {
	return new RelativeTime(getMilliseconds() + millis,
				getNanoseconds() + nanos);
    }

    /** Add a specific number of milliseconds and nanoseconds to <code>this</code>.
     *  A new object is allocated if destination is null, otherwise store there.
     */
    public RelativeTime add(long millis, int nanos, RelativeTime destination) {
	if (destination == null)
	    destination = new RelativeTime();
	
	destination.set(getMilliseconds() + millis,
			getNanoseconds() + nanos);
	return destination;
    }
    
    /** Return <code>this + time</code>. A new object is allocated for the result. */
    public final RelativeTime add(RelativeTime time) {
	return new RelativeTime(getMilliseconds() + time.getMilliseconds(),
				getNanoseconds() + time.getNanoseconds());
    }

    /** Return <code>this + time</code>. If destination is non-null, the result is
     *  placed there and <code>destination</code> is returned. Otherwise a new
     *  object is allocated for the result.
     */
    public RelativeTime add(RelativeTime time, RelativeTime destination) {
	if (destination == null)
	    destination = new RelativeTime();
	
	destination.set(getMilliseconds() + time.getMilliseconds(),
			getNanoseconds() + time.getNanoseconds());
	return destination;
    }
    
    /** Add this time to an <code>AbsoluteTime</code>. It is almost the same as
     *  <code>destination.add(this, destination)</code> except that it accounts
     *  for (ie. divides by) the frequency. If <code>destination</code> is equal
     *  to null, <code>NullPointerException</code> is thrown.
     */
    public void addInterarrivalTo(AbsoluteTime destination) {
	try {
	    destination.add(this, destination);
	} catch (NullPointerException e) {
	    System.out.println("A NullPointerException occured in " +
			       "RelativeTime.addInterarrivalTo(AbsoluteTime destination)");
	}
    }

    /** Return the interarrival time that is the result of dividing this interval
     *  by its frequency. For a <code>RelativeTime</code>, or a <code>RationalTime</code>
     *  with a frequency of 1 it just returns <code>this</code> The interarrival time
     *  is necessarily an approximation.
     */
    public RelativeTime getInterarrivalTime() {
	return this;
    }

    /** Return the interarrival time that is the result of dividing this interval
     *  by its frequency. For a <code>RelativeTime</code>, or a <code>RationalTime</code>
     *  with a frequency of 1 it just returns <code>this</code> The interarrival time
     *  is necessarily an approximation.
     */
    public RelativeTime getInterarrivalTime(RelativeTime destination) {
	destination = this;
	return destination;
    }
    
    /** Change the association of this from the currently associated clock to the
     *  given clock.
     */
    public RelativeTime relative(Clock clock) {
	set(getMilliseconds() +
	    defaultClock.getTime().getMilliseconds() -
	    clock.getTime().getMilliseconds(),
	    getNanoseconds() +
	    defaultClock.getTime().getNanoseconds() -
	    clock.getTime().getNanoseconds());
	return this;
    }

    /** Set the time of this to the time of the given instance fo <code>RelativeTime</code>
     *  with respect to the given instance of <code>Clock</code>.
     */
    public RelativeTime relative(Clock clock, RelativeTime time) {
	set(time.getMilliseconds() +
	    defaultClock.getTime().getMilliseconds() -
	    clock.getTime().getMilliseconds(),
	    time.getNanoseconds() +
	    defaultClock.getTime().getNanoseconds() -
	    clock.getTime().getNanoseconds());
	return this;
    }

    // Not in specs, but must be defined, since it was declared as abstract in HighResolutionTime
    public RelativeTime relative(Clock clock, HighResolutionTime time) {
	set(time.getMilliseconds() +
	    defaultClock.getTime().getMilliseconds() -
	    clock.getTime().getMilliseconds(),
	    time.getNanoseconds() +
	    defaultClock.getTime().getNanoseconds() -
	    clock.getTime().getNanoseconds());
	return this;
    }
    
    /** Subtracts <code>time</code> from <code>this</code>. */
    public final RelativeTime subtract(RelativeTime time) {
	return new RelativeTime(getMilliseconds() - time.getMilliseconds(),
				getNanoseconds() - time.getNanoseconds());
    }
    
    /** Subtracts <code>time</code> from <code>this</code> and places the result
     *  in <code>destination</code>. If <code>destination</code> is null, allocates
     *  new object for the result.
     */
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
