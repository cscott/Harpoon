// RelativeTime.java, created by cata
// Copyright (C) 2001 Catalin Francu <cata@mit.edu>
//    taking over Bryan Fink's code
// Licensed under the terms of the GNU GPL; see COPYING for details.

package javax.realtime;

import java.util.Date;

/** An object that represents a time interval millis/1E3+nanos/1E9
 *  seconds long. It generally is used to represent a time relative
 *  to now.
 *  <p>
 *  If the values of any of the millisecond or nanosecond fields is
 *  negative the variable is set to negative value. Although logically,
 *  and correctly, this may represent time before the epoch, invalid
 *  results <i>may</i> occur if an instance of <code>RelativeTime</code>
 *  representing time before the epoch is given as a paramter to some
 *  method. For <code>add</code> and <code>subtract</code> negative values
 *  behave just like they do in arithmetic.
 *  <p>
 *  <b>Caution:</b> This class is explicitly unsafe in multithreaded
 *  situations when it is being changed. No synchronization is done. It is
 *  assumed that users of this class who are mutating instances will be
 *  doing their own synchronization at a higher level
 */
public class RelativeTime extends HighResolutionTime {
    
    public static RelativeTime ZERO = new RelativeTime(0, 0);
    
    /** Equivalent to <code>new RelativeTime(0, 0)</code>. */
    public RelativeTime() {
	this(0, 0);
    }

    /** Constructs a <code>RelativeTime</code> object representing an interval
     *  defined by the given parameter values.
     *
     *  @param millis The milliseconds component.
     *  @param nanos The nanoseconds component.
     */
    public RelativeTime(long millis, int nanos) {
	set(millis, nanos);
    }
    
    /** Make a new <code>RelativeTime</code> object from the given
     *  <code>RelativeTime</code> object.
     *
     *  @param t The <code>RelativeTime</code> object used as the source
     *           for the copy.
     */
    public RelativeTime(RelativeTime t)	{
	this(t.getMilliseconds(), t.getNanoseconds());
    }

    /** Convert the time given by <code>this</code> to an absolute time. The
     *  calculation is the current time indicated by the given instance of
     *  <code>Clock</code> plus the interval given by <code>this</code>.
     *
     *  @param clock The given instance of <code>Clock</code> If null,
     *               <code>Clock.getRealTimeClock()</code> is used.
     *  @return The new instance of <code>AbsoluteTime</code> containing the result.
     */
    public AbsoluteTime absolute(Clock clock) {
	return new AbsoluteTime(clock.getTime().add(this));
    }

    /** Convert the time given by <code>this</code> to an absolute time. The
     *  calculation is the current time indicated by the given instance of
     *  <code>Clock</code> plus the interval given by <code>this</code>.
     *
     *  @param clock The given instance of <code>Clock</code> If null,
     *               <code>Clock.getRealTimeClock()</code> is used.
     *  @param destination A reference to the result instance of
     *                     <code>AbsoluteTime</code>.
     *  @return The new instance of <code>AbsoluteTime</code> containing the result.
     */
    public AbsoluteTime absolute(Clock clock, AbsoluteTime destination)	{
	if (destination == null)
	    destination = new AbsoluteTime();
	if (clock == null)
	    clock = Clock.getRealtimeClock();
	
	destination.set(clock.getTime().add(this));
	return destination;
    }

    /** Add a specific number of milliseconds and nanoseconds to the appropriate
     *  fields of <code>this</code>. A new object is allocated.
     *
     *  @param millis The number of milliseconds to add.
     *  @param nanos The number of nanoseconds to add.
     *  @return A new object containing the result of the addition.
     */
    public RelativeTime add(long millis, int nanos) {
	return new RelativeTime(getMilliseconds() + millis,
				getNanoseconds() + nanos);
    }

    /** Add a specific number of milliseconds and nanoseconds to the appropriate
     *  fields of <code>this</code>. A new object is allocated.
     *
     *  @param millis The number of milliseconds to add.
     *  @param nanos The number of nanoseconds to add.
     *  @param destination A reference to the result instance of
     *                     <code>RelativeTime</code>.
     *  @return A new object containing the result of the addition
     */
    public RelativeTime add(long millis, int nanos, RelativeTime destination) {
	if (destination == null)
	    destination = new RelativeTime();
	
	destination.set(getMilliseconds() + millis,
			getNanoseconds() + nanos);
	return destination;
    }
    
    /** Add the interval of <code>this</code> to the interval of the given
     *  instance of <code>RelativeTime</code>.
     *
     *  @param time A reference to the given instance of <code>RelativeTime</code>.
     *  @return A new object containing the result of the addition.
     */
    public final RelativeTime add(RelativeTime time) {
	return new RelativeTime(getMilliseconds() + time.getMilliseconds(),
				getNanoseconds() + time.getNanoseconds());
    }

    /** Add the interval of <code>this</code> to the interval of the given
     *  instance of <code>RelativeTime</code>.
     *
     *  @param time A reference to the given instance of <code>RelativeTime</code>.
     *  @param destination A reference to the result instance of <code>RelativeTime</code>.
     *  @return A new object containing the result of the addition.
     */
    public RelativeTime add(RelativeTime time, RelativeTime destination) {
	if (destination == null)
	    destination = new RelativeTime();
	
	destination.set(getMilliseconds() + time.getMilliseconds(),
			getNanoseconds() + time.getNanoseconds());
	return destination;
    }
    
    /** Add the interval of <code>this</code> to the given instance of
     *  <code>AbsoluteTime</code>.
     *
     *  @param destination A reference to the given instance of
     *                     <code>AbsoluteTime</code> and the result.
     */
    public void addInterarrivalTo(AbsoluteTime destination) {
	try {
	    destination.add(this, destination);
	} catch (NullPointerException e) {
	    System.out.println("A NullPointerException occured in " +
			       "RelativeTime.addInterarrivalTo(AbsoluteTime destination)");
	}
    }

    /** Gets the interval defined by <code>this</code>. For an instance of
     *  <code>RationalTime</code> it is the interval divided by the frequency.
     *
     *  @return A reference to a new instance of <code>RelativeTime</code>
     *          with the same interval as <code>this</code>.
     */
    public RelativeTime getInterarrivalTime() {
	return new RelativeTime(this);
    }

    /** Gets the interval defined by <code>this</code> For an instance of
     *  <code>RelativeTime</code> it is the interval divided by the frequency.
     *
     *  @param destination A reference to the new object holding the result.
     *  @return A reference to an object holding the result.
     */
    public RelativeTime getInterarrivalTime(RelativeTime destination) {
	destination = new RelativeTime(this);
	return destination;
    }
    
    /** Make a new <code>RelativeTime</code> object from the time given by
     *  <code>this</code> but based on the given instance of <code>Clock</code>.
     *
     *  @param clock The given instance of <code>Clock</code>.
     *  @return A reference to the new instance of <code>RelativeTime</code>.
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

    /** Make a new <code>RelativeTime</code> object from the time given by
     *  <code>this</code> but based on the given instance of <code>Clock</code>.
     *
     *  @param clock The given instance of <code>Clock</code>.
     *  @param time A reference to the result instance of <code>RelativeTime</code>.
     *  @return A reference to the new instance of <code>RelativeTime</code>.
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

    /** Make a new <code>RelativeTime</code> object from the time given by
     *  <code>this</code> but based on the given instance of <code>Clock</code>.
     *
     *  @param clock The given instance of <code>Clock</code>.
     *  @param time A reference to the result instance of <code>RelativeTime</code>.
     *  @return A reference to the new instance of <code>HighResolutionTime</code>.
     */
    public RelativeTime relative(Clock clock, HighResolutionTime time) {
	set(time.getMilliseconds() +
	    defaultClock.getTime().getMilliseconds() -
	    clock.getTime().getMilliseconds(),
	    time.getNanoseconds() +
	    defaultClock.getTime().getNanoseconds() -
	    clock.getTime().getNanoseconds());
	return this;
    }
    
    /** Subtract the interval defined by the given instance of
     *  <code>RelativeTime</code> from the interval defined by <code>this</code>.
     *
     *  @param time A reference to the given instance of <code>RelativeTime</code>.
     *  @return A new object holding the result of the subtraction.
     */
    public final RelativeTime subtract(RelativeTime time) {
	return new RelativeTime(getMilliseconds() - time.getMilliseconds(),
				getNanoseconds() - time.getNanoseconds());
    }
    
    /** Subtract the interval defined by the given instance of
     *  <code>RelativeTime</code> from the interval defined by <code>this</code>.
     *
     *  @param time A reference to the given instance of <code>RelativeTime</code>.
     *  @param destination A reference to the object holding the result.
     *  @return A new object holding the result of the subtraction.
     */
    public RelativeTime subtract(RelativeTime time, RelativeTime destination) {
	if (destination == null)
	    destination = new RelativeTime();
	
	destination.set(getMilliseconds() - time.getMilliseconds(),
			getNanoseconds() - time.getNanoseconds());
	return destination;
    }
    
    /** Return a printable version of the time defined by <code>this</code>.
     *
     *  @return An instance of <code>java.lang.String</code> representing the
     *          time defined by <code>this</code>.
     */
    public String toString() {
	return "RelativeTime: millis: " + getMilliseconds() +
	    " nanos: " + getNanoseconds();
    }
}
