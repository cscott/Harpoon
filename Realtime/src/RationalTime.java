// RationalTime.java, created by Dumitru Daniliuc
// Copyright (C) 2003 Dumitru Daniliuc
// Licensed under the terms of the GNU GPL; see COPYING for details.
package javax.realtime;

/** An object that represents a time interval millis/1E3+nanos/1E9
 *  seconds long that is divided into subintervals by some frequency.
 *  This is generally used in periodic events, threads, and
 *  feasibility analysis to specify periods where there is a basic
 *  period that must be adhered to strictly (the interval), but
 *  within that interval the periodic events are supposed to happen
 *  <code>frequency</code> times, as u niformly spaced as possible,
 *  but clock and scheduling jitter is moderately acceptable.
 *  <p>
 *  If the value of any of the millisecond or nanosecond fields is
 *  negative the variable is set to negative value. Although logically
 *  this may represent time before the epoch, invalid results may
 *  occur if an instance of <code>AbsoluteTime</code> representing
 *  time before the epoch is given as a parameter to a method.
 *  <p>
 *  <b>Caution:</b> This class is explicitly unsafe in multithreaded
 *  situations when it is being changed. No synchronization is done. It
 *  is assumed that users of this class who are mutating instances will
 *  be doing their own synchronization at a higher level.
 */
public class RationalTime extends RelativeTime {

    private int frequency;

    /** Constructs an instance of <code>RationalTime</code>. Equivalent to
     *  <code>new RationalTime(frequency, 1000, 0)</code> -- essentially a
     *  cycle-per-seconds value.
     *
     *  @param frequency The frequency value.
     */
    public RationalTime(int frequency) {
	// I think they messed up the order of the parameters in the spec.
	this(frequency, 1000, 0);
    }

    /** Constructs an instance of <code>RationalTime</code>. All arguments
     *  must be greater than or equal to zero.
     *
     *  @param frequency The frequency value.
     *  @param millis The milliseconds value.
     *  @param nanos The nanoseconds value.
     *  @throws java.lang.IllegalArgumentException If any of the argument values
     *                                             are less than zero.
     */
    public RationalTime(int frequency, long millis, int nanos)
	throws IllegalArgumentException {
	super(millis, nanos);
	this.frequency = frequency;
    }

    /** Constructs an instance of <code>RationalTime</code> from the given
     *  <code>RelativeTime</code>.
     *
     *  @param frequency The frequency value.
     *  @param interval The given instance of <code>RelativeTime</code>.
     *  @throws java.lang.IllegalArgumentException If any of the argument values
     *                                             are less than zero.
     */
    public RationalTime(int frequency, RelativeTime interval)
	throws IllegalArgumentException {
	this(frequency, interval.getMilliseconds(),
	     interval.getNanoseconds());
    }

    /** Convert this time to an absolute time.
     *
     *  @param clock The reference clock. If null, <code>Clock.getRealTimeClock()</code>
     *               is used.
     *  @param destination A reference to the destination istance.
     */
    public AbsoluteTime absolute(Clock clock, AbsoluteTime destination) {
	if (destination == null)
	    destination = new AbsoluteTime();
	if (clock == null)
	    clock = Clock.getRealtimeClock();
	
	destination.set(clock.getTime().add(this));
	return destination;
    }

    /** Add this time to an <code>AbsoluteTime</code>. It is almost the same as
     *  <code>destination.add(this, destination)</code> except that it accounts
     *  for (ie. divides by) the frequency.
     */
    public void addInterarrivalTo(AbsoluteTime destination) {
	destination.add(this, destination);
    }

    /** Gets the value of <code>frequency</code>.
     *
     *  @return The value of <code>frequency</code> as an integer.
     */
    public int getFrequency() {
	return frequency;
    }

    /** Gets the interarrival time. This time is
     *  <code>(milliseconds/10^3 + nanoseconds/10^9)/frequency<code> rounded
     *  down to the nearest expressible value of the fields and their types 
     *  of <code>RelativeTime</code>.
     */
    public RelativeTime getInterarrivalTime() {
	RelativeTime temp = new RelativeTime();
	long t =  Math.round((1000000 * getMilliseconds() + getNanoseconds()) /
			     getFrequency());
	temp.set(t / 1000000, (int) (t % 1000000));
	return temp;
    }

    /** Gets the interarrival time. This time is
     *  <code>(milliseconds/10^3 + nanoseconds/10^9)/frequency<code> rounded
     *  down to the nearest expressible value of the fields and their types 
     *  of <code>RelativeTime</code>.
     *
     *  @param dest Result is stored in <code>dest</code> and returned; if null,
     *              a new object is returned.
     */
    public RelativeTime getInterarrivalTime(RelativeTime dest) {
	if (dest == null) dest = new RelativeTime();
	long t =  Math.round((1000000 * getMilliseconds() + getNanoseconds()) /
			     getFrequency());
	dest.set(t / 1000000, (int) (t % 1000000));
	return dest;
    }

    /** Sets the indicated fields to the given values.
     *
     *  @param millis The new value for the millisecond field.
     *  @param nanos The new value for the nanosecond field.
     *  @throws java.lang.IllegalArgumentException
     */
    public void set(long millis, int nanos)
	throws IllegalArgumentException {
	super.set(millis, nanos);
    }

    /** Sets the value of the <code>frequency</code> field.
     *
     *  @param frequency The new value for the <code>frequency</code>.
     *  @throws java.lang.ArithmeticException
     */
    public void setFrequency(int frequency) throws ArithmeticException {
	this.frequency = frequency;
    }
}
