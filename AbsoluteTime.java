// RelativeTime.java, created by cata
// Copyright (C) 2001 Catalin Francu <cata@mit.edu>
//    taking over Bryan Fink's code
// Licensed under the terms of the GNU GPL; see COPYING for details.

package javax.realtime;

import java.util.Date;

/** An object that represents a specific point in time given by
 *  milliseconds plus nanoseconds past the epoch (Jenuary 1, 1970,
 *  00:00:00 GMT). This representation was designed to be compatible
 *  with the standard Java representation of an absolute time in the
 *  <code>java.util.Date</code> class.
 */
public class AbsoluteTime extends HighResolutionTime {
    
    public static AbsoluteTime endOfDays =
	new AbsoluteTime(Long.MAX_VALUE, 0);
    
    public AbsoluteTime()	{
	this(0, 0);
    }
 
    /** Make a new <code>AbsoluteTime</code> object from the given
     *  <code>AbsoluteTime</code> object.
     */
    public AbsoluteTime(AbsoluteTime t)	{
	this(t.getMilliseconds(), t.getNanoseconds());
    }

    /** Equivalent to <code>new AbsoluteTime(date.getTime(), 0)</code>. */
    public AbsoluteTime(Date date) {
	this(date.getTime(), 0);
    }

    /** Constructs an <code>AbsoluteTime</code> object which means a time
     *  <code>millis</code> milliseconds plus <code>nanos</code> nanoseconds
     *  past 00:00:00 GMT on January 1, 1970.
     *
     * Notice: the time is normalized by converting every 10^6 ns to 1 ms.
     */
    public AbsoluteTime(long millis, int nanos)	{
	super();
	set(millis, nanos);
    }
    
    /** Converts this time to an absolute time relative to a given clock. */
    public AbsoluteTime absolute(Clock clock) {
	AbsoluteTime tempTime = clock.getTime();
	long tempMillis = getMilliseconds() - tempTime.getMilliseconds();
	int tempNanos = getNanoseconds() - tempTime.getNanoseconds();
	if (tempNanos < 0) {
	    tempMillis--;
	    tempNanos += 1000000;
	}
	set(tempMillis, tempNanos);

	return this;
    }
    
    /** Convert this time to an absolute time. For an <code>AbsoluteTime</code>,
     *  this is really easy: it just return itself. Presumes that this is already
     *  relative to the given clock.
     */
    public AbsoluteTime absolute(Clock clock, AbsoluteTime destination)	{
	if (destination != null)
	    destination.set(this);
	return this;
    }
    
    /** Add <code>millis</code> and <code>nanos</code> to this. A new object is
     *  allocated for the result.
     */
    public AbsoluteTime add(long millis, int nanos) {
	return new AbsoluteTime(getMilliseconds() + millis,
				getNanoseconds() + nanos);
    }

    /** If a destination is non-null, the result is placed there and the destination
     *  is returned. Otherwise a new object is allocated for the result. */
    public AbsoluteTime add(long millis, int nanos, AbsoluteTime destination) {
	if (destination == null)
	    destination = new AbsoluteTime();
	
	destination.set(getMilliseconds() + millis,
			getNanoseconds() + nanos);
	return destination;
    }
    
    /** Return <code>this + time</code>. A new object is allocated for the result. */
    public final AbsoluteTime add(RelativeTime time) {
	return new AbsoluteTime(getMilliseconds() + time.getMilliseconds(),
				getNanoseconds() + time.getNanoseconds());
    }

    /** Return <code>this + time</code>. If destination is non-null, the result is
     *  placed there and destination is returned. Otherwise a new object is allocated
     *  for the result.
     */
    public AbsoluteTime add(RelativeTime time, AbsoluteTime destination) {
	if (destination == null)
	    destination = new AbsoluteTime();
	
	destination.set(getMilliseconds() + time.getMilliseconds(),
			getNanoseconds() + time.getNanoseconds());
	return destination;
    }
    
    /** Returns the time past the epoch represented by <code>this</code> as a
     *  <code>java.util.Date</code>.
     */
    public Date getDate() {
	return new Date(getMilliseconds());
    }

    /** Change the association of this from the currently associated clock to
     *  the given clock.
     */
    public RelativeTime relative(Clock clock) {
	return new RelativeTime(getMilliseconds() -
				clock.getTime().getMilliseconds(),
				getNanoseconds() - 
				clock.getTime().getNanoseconds());
    }

    /** Convert the fiven instance of <code>RelativeTime</code> to an instance
     *  of <code>RelativeTime</code> relative to the given instance of <code>Clock</code>.
     */
    public RelativeTime relative(Clock clock, AbsoluteTime destination) {
	return new RelativeTime(destination.getMilliseconds() -
				clock.getTime().getMilliseconds(),
				destination.getNanoseconds() -
				clock.getTime().getNanoseconds());
    }
    
    // Not in specs, but must be defined, since it was declared as abstract in HighResolutionTime
    public RelativeTime relative(Clock clock, HighResolutionTime time) {
	return new RelativeTime(time.getMilliseconds() -
				clock.getTime().getMilliseconds(),
				time.getNanoseconds() -
				clock.getTime().getNanoseconds());
    }

    /** Change the time represented by <code>this</code>. */
    public void set(Date d) {
	set(d.getTime());
    }

    /** Subtracts <code>time</code> from <code>this</code>. */
    public final RelativeTime subtract(AbsoluteTime time) {
	return new RelativeTime(getMilliseconds() - time.getMilliseconds(),
				getNanoseconds() - time.getNanoseconds());
    }

    /** Subtracts <code>time</code> from <code>this</code> and places the result
     *  in <code>destination</code>. If <code>destination</code> is null, allocates
     *  new object for the result.
     */
    public RelativeTime subtract(AbsoluteTime time, RelativeTime destination) {
	if (destination == null)
	    destination = new RelativeTime();
	
	destination.set(getMilliseconds() - time.getMilliseconds(),
			getNanoseconds() - time.getNanoseconds());
	return destination;
    }

    /** Subtracts <code>time</code> from <code>this</code>. */
    public final AbsoluteTime subtract(RelativeTime time) {
	return new AbsoluteTime(getMilliseconds() - time.getMilliseconds(),
				getNanoseconds() - time.getNanoseconds());
    }

    /** Subtracts <code>time</code> from <code>this</code> and places the result
     *  in <code>destination</code>. If <code>destination</code> is null, allocates
     *  new object for the result.
     */
    public AbsoluteTime subtract(RelativeTime time, AbsoluteTime destination) {
	if (destination == null)
	    destination = new AbsoluteTime();
	
	destination.set(getMilliseconds() - time.getMilliseconds(),
			getNanoseconds() - time.getNanoseconds());
	return destination;
    }
    
    /** Return a printable version of this time, in a format that matches
     *  <code>java.util.Date.toString()</code> with a postfix to the detail the
     *  sub-second value.
     */
    public String toString() {
	Date result = new Date();
	result.setTime(getMilliseconds());
	
	return "AbsoluteTime: " + result.toString() + " millis: " +
	    getMilliseconds()%1000 + " nanos: " + getNanoseconds();
    }
}
