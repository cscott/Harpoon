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
 *  <p>
 *  If the value of any of the millisecond or nanosecond fields is negative
 *  the variable is set to negative value. Although logically this may
 *  represent time before the epoch, invalid results may occur if an instance
 *  of <code>AbsoluteTime</code> representing time before the epoch is given
 *  as a parameter to a method. For <code>add</code> and <code>subtract</code>
 *  negative values behave just like they do in arithmetic.
 *  <p>
 *  <b>Caution:</b> This class is explicitly unsafe in multithreaded situations
 *  when it is being changed. No synchronization is done. It is assumed that
 *  users of this class who are mutating instances will be doing their own
 *  synchronization at a higher level.
 */
public class AbsoluteTime extends HighResolutionTime {
    
    public static AbsoluteTime endOfDays =
	new AbsoluteTime(Long.MAX_VALUE, 0);
    
    /** Equal to new <code>AbsoluteTime(0,0)</code>. */
    public AbsoluteTime()	{
	this(0, 0);
    }
 
    /** Make a new <code>AbsoluteTime</code> object from the given
     *  <code>AbsoluteTime</code> object.
     *
     *  @param time The <code>AbsoluteTime</code> object which is the source
     *              for the copy.
     */
    public AbsoluteTime(AbsoluteTime t)	{
	this(t.getMilliseconds(), t.getNanoseconds());
    }

    /** Equivalent to <code>new AbsoluteTime(date.getTime(), 0)</code>.
     *
     *  @param date The <code>java.util.Date</code> representation of the time
     *              past the epoch.
     */
    public AbsoluteTime(Date date) {
	this(date.getTime(), 0);
    }

    /** Construct an <code>AbsoluteTime</code> object which means a time
     *  <code>millis</code> milliseconds plus <code>nanos</code> nanoseconds
     *  past 00:00:00 GMT on January 1, 1970.
     *
     *  @param millis The milliseconds component of the time past the epoch.
     *  @param nanos The nanosecond component of the time past the epoch.
     */
    public AbsoluteTime(long millis, int nanos)	{
	super();
	set(millis, nanos);
    }
    
    /** Convert time given by <code>this</code> to an absolute time relative
     *  to a given clock.
     *
     *  @param clock The clock on which <code>this</code> will be based.
     *  @return A reference to <code>this</code>.
     */
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
     *
     *  @param clock The clock on which this is based.
     *  @param destination Converted to an absolute time.
     *  @return A reference to <code>this</code>.
     */
    public AbsoluteTime absolute(Clock clock, AbsoluteTime destination)	{
	if (destination != null)
	    destination.set(this);
	return this;
    }
    
    /** Add <code>millis</code> and <code>nanos</code> to <code>this</code>. A new
     *  object is allocated for the result.
     *
     *  @param millis The number of milliseconds to be added to <code>this</code>.
     *  @param nanos The number of nanoseconds to be added to <code>this</code>.
     *  @return A new <code>AbsoluteTime</code> object whose time is <code>this</code>
     *          plus <code>millis</code> and <code>nanos</code>.
     */
    public AbsoluteTime add(long millis, int nanos) {
	return new AbsoluteTime(getMilliseconds() + millis,
				getNanoseconds() + nanos);
    }

    /** Add <code>millis</code> and <code>nanos</code> to <code>this</code>. If the
     *  <code>destination</code> is non-null, the result is placed there and returned.
     *  Otherwise, a new object is allocated for the result.
     *
     *  @param millis The number of milliseconds to be added to <code>this</code>.
     *  @param nanos The number of nanoseconds to be added to <code>this</code>.
     *  @param destination A reference to an <code>AbsoluteTime</code> object into
     *                     which the result of the addition may be placed.
     */
    public AbsoluteTime add(long millis, int nanos, AbsoluteTime destination) {
	if (destination == null)
	    destination = new AbsoluteTime();
	
	destination.set(getMilliseconds() + millis,
			getNanoseconds() + nanos);
	return destination;
    }
    
    /** Add the time given by the parameter to <code>this</code>.
     *
     *  @param time The time to add to <code>this</code>.
     *  @return A reference to <code>this</code>.
     */
    public final AbsoluteTime add(RelativeTime time) {
	return new AbsoluteTime(getMilliseconds() + time.getMilliseconds(),
				getNanoseconds() + time.getNanoseconds());
    }

    /** Add the time given by the parameter to <code>this</code>. If the
     *  <code>destination</code> is non-null, the result is placed there
     *  and returned. Otherwise, a new object is allocated for the result.
     *
     *  @param time The time to add to <code>this</code>.
     *  @param destination A reference to an <code>AbsoluteTime</code> object
     *                     into which the result of the addition may be placed.
     *  @return A reference to <code>destination</code> or a new object whose
     *          time is <code>this</code> plus <code>millis</codeN and <code>nano</code>.
     */
    public AbsoluteTime add(RelativeTime time, AbsoluteTime destination) {
	if (destination == null)
	    destination = new AbsoluteTime();
	
	destination.set(getMilliseconds() + time.getMilliseconds(),
			getNanoseconds() + time.getNanoseconds());
	return destination;
    }
    
    /** Convert the time given by <code>this</code> to a <code>java.utilDate</code>
     *  format. Note that <code>java.util.Date</code> represents time as milliseconds
     *  so the nanoseconds of <code>this</code> will be lost.
     *
     *  @return A reference to a <code>java.util.Date</code> object with a value of
     *          the time past the epoch represented by <code>this</code>.
     */
    public Date getDate() {
	return new Date(getMilliseconds());
    }

    /** Create a <code>RelativeTime</code> object with current time given by
     *  <code>this</code> with reference to the parameter.
     *
     *  @param clock The clock reference used as a reference for this.
     *  @return A reference to a new <code>RelativeTime</code> object whose time is
     *          set to the time given by <code>this</code> referencing the parameter.
     */
    public RelativeTime relative(Clock clock) {
	return new RelativeTime(getMilliseconds() -
				clock.getTime().getMilliseconds(),
				getNanoseconds() - 
				clock.getTime().getNanoseconds());
    }

    /** Create a <code>RelativeTime</code> object with current time given by
     *  <code>this</code> with reference to <code>Clock</code> parameter. If the
     *  <code>destination</code> is non-null, the result is placed there and
     *  returned. Otherwise, a new object is allocated for the result
     *
     *  @param clock The clock reference used as a reference for this.
     *  @param destination A reference to a <code>RelativeTime</code> object into
     *                     which the result of the subtraction may be placed.
     *  @return A reference to a new <code>RelativeTime</code> object whose time is
     *          set to the time given by <code>this</code> referencing the parameter.
     */
    public RelativeTime relative(Clock clock, AbsoluteTime destination) {
	return new RelativeTime(destination.getMilliseconds() -
				clock.getTime().getMilliseconds(),
				destination.getNanoseconds() -
				clock.getTime().getNanoseconds());
    }
    
    /** Create a <code>RelativeTime</code> object with current time given by
     *  <code>this</code> with reference to <code>Clock</code> parameter. If the
     *  <code>destination</code> is non-null, the result is placed there and
     *  returned. Otherwise, a new object is allocated for the result
     *
     *  @param clock The clock reference used as a reference for this.
     *  @param time A reference to a <code>HighResolutionTime</code> object into
     *              which the result of the subtraction may be placed.
     *  @return A reference to a new <code>RelativeTime</code> object whose time is
     *          set to the time given by <code>this</code> referencing the parameter.
     */
    public RelativeTime relative(Clock clock, HighResolutionTime time) {
	return new RelativeTime(time.getMilliseconds() -
				clock.getTime().getMilliseconds(),
				time.getNanoseconds() -
				clock.getTime().getNanoseconds());
    }

    /** Change the time represented by <code>this</code> to that given by the parameter.
     *
     *  @param date A reference to a <code>java.util.Date</code> which will become the
     *              time represented by <code>this</code> after the completion of this method.
     */
    public void set(Date d) {
	set(d.getTime());
    }

    /** Finds the difference between the time given by <code>this</code> and the time
     *  given by the parameter. The difference is, of course, a <code>RelativeTime</code>.
     *
     *  @param time A reference to an <code>AbsoluteTime</code> object whose time is
     *              subtracted from <code>this</code>.
     *  @return A reference to a new <code>RelativeTime</code> object whose time is the
     *          difference.
     */
    public final RelativeTime subtract(AbsoluteTime time) {
	return new RelativeTime(getMilliseconds() - time.getMilliseconds(),
				getNanoseconds() - time.getNanoseconds());
    }

    /** Finds the difference between the time given by <code>this</code> and the time
     *  given by the parameter. The difference is, of course, a <code>RelativeTime</code>.
     *  If the <code>destination</code> is non-null, the result is placed there and
     *  returned. Otherwise, a new object is allocated for the result.
     *
     *  @param time A reference to an <code>AbsoluteTime</code> object whose time is
     *              subtracted from <code>this</code>.
     *  @param destination A reference to an <code>RelativeTime</code> object into which
     *                     the result of the addition may be placed.
     *  @return A reference to a new <code>RelativeTime</code> object whose time is the
     *          difference between <code>this</code> and the <code>time</code> paramter.
     */
    public RelativeTime subtract(AbsoluteTime time, RelativeTime destination) {
	if (destination == null)
	    destination = new RelativeTime();
	
	destination.set(getMilliseconds() - time.getMilliseconds(),
			getNanoseconds() - time.getNanoseconds());
	return destination;
    }

    /** Finds the difference between the time given by <code>this</code> and the time
     *  given by the parameter.
     *
     *  @param time A reference to an <code>AbsoluteTime</code> object whose time is
     *              subtracted from <code>this</code>.
     *  @return A reference to a new <code>AbsoluteTime</code> object whose time is the
     *          difference.
     */
    public final AbsoluteTime subtract(RelativeTime time) {
	return new AbsoluteTime(getMilliseconds() - time.getMilliseconds(),
				getNanoseconds() - time.getNanoseconds());
    }

    /** Finds the difference between the time given by <code>this</code> and the time
     *  given by the parameter. The difference is, of course, a <code>RelativeTime</code>.
     *  If the <code>destination</code> is non-null, the result is placed there and
     *  returned. Otherwise, a new object is allocated for the result.
     *
     *  @param time A reference to an <code>AbsoluteTime</code> object whose time is
     *              subtracted from <code>this</code>.
     *  @param destination A reference to an <code>AbsoluteTime</code> object into which
     *                     the result of the addition may be placed.
     *  @return A reference to a new <code>AbsoluteTime</code> object whose time is the
     *          difference between <code>this</code> and <code>time</code> parameter.
     */
    public AbsoluteTime subtract(RelativeTime time, AbsoluteTime destination) {
	if (destination == null)
	    destination = new AbsoluteTime();
	
	destination.set(getMilliseconds() - time.getMilliseconds(),
			getNanoseconds() - time.getNanoseconds());
	return destination;
    }
    
    /** Create a printable string of the time given by <code>this</code>, in a format
     *  that matches <code>java.util.Date.toString()</code> with a postfix to the
     *  detail the nanosecond value.
     *
     *  @return String object converted from the time given ty <code>this</code>.
     */
    public String toString() {
	Date result = new Date();
	result.setTime(getMilliseconds());
	
	return "AbsoluteTime: " + result.toString() + " millis: " +
	    getMilliseconds()%1000 + " nanos: " + getNanoseconds();
    }
}
