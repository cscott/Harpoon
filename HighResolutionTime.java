// RelativeTime.java, created by cata
// Copyright (C) 2001 Catalin Francu <cata@mit.edu>
//    taking over Bryan Fink's code
// Licensed under the terms of the GNU GPL; see COPYING for details.

// Class Invariant:
//   - Times are consistent at any time. That is, 0<=nanos<1000000. If not,
//     we normalize them, e.g 3ms 3200000ns --> 6ms 200000ns

package javax.realtime;
import java.lang.Math;

/** Class <code>HighResolutiontime</code> is the base class for
 *  <code>AbsoluteTime, RelativeTime, RationalTime</code>.
 */
public abstract class HighResolutionTime implements Comparable {
    
    private long millis;
    private int nanos;
    static protected Clock defaultClock = Clock.getRealtimeClock();
    
    /** Convert the time of <code>this</code> to an absolute time, relative to
     *  the given instance of <code>Clock</code>. Convenient for situations
     *  where you really need an absolute time. Aloocates a destination object
     *  if necessary.
     *
     *  @param clock An instance of <code>Clock</code> is used to convert the
     *               time of <code>this</code> into absolute time.
     */
    public abstract AbsoluteTime absolute(Clock clock);

    /** Convert the time of <code>this</code> to an absolute time, relative to
     *  the given instance of <code>Clock</code>. Convenient for situations
     *  where you really need an absolute time. Aloocates a destination object
     *  if necessary.
     *
     *  @param clock An instance of <code>Clock</code> is used to convert the
     *               time of <code>this</code> into absolute time.
     *  @param dest If null, a new object is created and returned as result,
     *              else <code>dest</code> is returned.
     */
    public abstract AbsoluteTime absolute(Clock c, AbsoluteTime dest);
    
    /** Compares this <code>HighResolutionTime</code> with the specified
     *  <code>HighResolutionTime</code>.
     *
     *  @param time Compares with the time of <code>this</code>.
     */
    public int compareTo(HighResolutionTime b) {
	if (millis > b.getMilliseconds())
	    return 1;
	
	if (millis < b.getMilliseconds())
	    return -1;
	
	if (nanos > b.getNanoseconds())
	    return 1;
	
	if (nanos < b.getNanoseconds())
	    return -1;
	
	return 0;
    }
    
    /** Compares this <code>HighResolutionTime</code> with the specified
     *  <code>HighResolutionTime</code>.
     */
    public int compareTo(Object b) {
	return compareTo((HighResolutionTime) b);
    }
    
    /** Returns true if the argument object has the same values as this. */
    public boolean equals(HighResolutionTime b)	{
	return (millis == b.getMilliseconds() &&
		nanos == b.getNanoseconds());
    }
    
    /** Returns true if the argument object has the same values as <code>this<code>.
     *
     *  @param b Value compared to <code>this</code>.
     */
    public boolean equals(Object b) {
	return equals((HighResolutionTime) b);
    }
    
    /** Returns the milliseconds component of <code>this</code>.
     *
     *  @return The milliseconds component of the time past the epoch
     *          represented by <code>this</code>.
     */
    public final long getMilliseconds()	{
	return millis;
    }
    
    /** Returns the nanoseconds component of <code>this</code>. */
    public final int getNanoseconds()	{
	return nanos;
    }
    
    public int hashCode()	{
	return (int)(nanos+millis*1000000);
    }

    /** Convert the time of <code>this</code> to a relative time, with
     *  respect to the given instance of <code>Clock</code>. Convenient
     *  for situations where you really need a relative time. Allocates
     *  a destination object if necessary.
     *
     *  @param clock An instance of <code>Clock</code> is used to convert
     *               the time of <code>this</code> into realtive time.
     */
    public abstract RelativeTime relative(Clock clock);

    /** Convert the time of <code>this</code> to a relative time, with
     *  respect to the given instance of <code>Clock</code>. Convenient
     *  for situations where you really need a relative time. Allocates
     *  a destination object if necessary.
     *
     *  @param clock An instance of <code>Clock</code> is used to convert
     *               the time of <code>this</code> into realtive time.
     *  @param time If null, a new object is created and returned as result,
     *              else <code>time</code> is returned.
     */
    public abstract RelativeTime relative(Clock clock,
					  HighResolutionTime time);

    /** Change the value represented by <code>this</code> to that of the given
     *  time. If the type of <code>this</code> and the type of the given time
     *  are not the same this method will throw IllegalArgumentException.
     *
     *  @param t The new value for <code>this</code>.
     */
    public void set(HighResolutionTime t) {
	millis = t.getMilliseconds();
	nanos = t.getNanoseconds();
    }

    /** Sets the millisecond component of <code>this</code> to the given argument.
     *
     *  @param millis This value will be the value of the milliseconds component
     *                of <code>this</code> at the completion of the call. If
     *                <code>millis</code> is negative the millisecond value of
     *                <code>this</code> is set to negative value. Although
     *                logically this may represent time before the epoch, invalid
     *                results may occur if a <code>HighResolutionTime</code>
     *                representing time before the epoch is given as a parameter
     *                to the method.
     */
    public void set(long millisecs) {
	millis = millisecs;
	nanos = 0;
    }
    
    /** Sets the millisecond and nanosecond compenonets of <code>this</code>.
     *
     *  @param millis Value to set millisecond part of <code>this</code>. If
     *                <code>millis</code> is negative the millisecond value
     *                of <code>this</code> is set to negative value. Although
     *                logically this may represent time before the epoch,
     *                invalid results may occur if a <code>HighResolutionTime</code>
     *                representing time before the epoch is given as a parameter
     *                to the method.
     *  @param nanos Value to set nanosecond part of <code>this</code>. If
     *               <code>nanos</code> is negative, the nanosecond valur of
     *               <code>this</code> is set to negative value. Although
     *               logically this may represent time before the epoch, invalid
     *               results may occur if a <code>HighResolutionTime</code>
     *               representing time before the epoch is given as a paramter
     *               to the method.
     */
    public void set(long millisecs, int nanosecs) {
	// Do we have negative nanos? Java cannot take negative moduli properly
	if (nanosecs < 0) {
	    nanos = Math.abs(nanosecs);
	    millis = millisecs - nanos/1000000;
	    nanos %= 1000000;
	    if (nanos > 0) {
		millis--;
		nanos = 1000000 - nanos;
	    }
	}
	else {
	    millis = millisecs + nanosecs/1000000;
	    nanos = nanosecs%1000000;
	}
    }

    /** Behaves exactly like <code>target.wait()</code> but with the enhacement
     *  that it waits with a precision of <code>HighResolutionTime</code>.
     *
     *  @param target The object on which to wait. The current thread must have
     *                a lock on the object.
     *  @param time The time for which to wait. If this is
     *              <code>RelativeTime(0, 0)</code> then wait indefinetly.
     *  @throws java.lang.InterruptedException If another thread interrupts this
     *                                         thread while it is waiting.
     */
    public static void waitForObject(Object target, HighResolutionTime time)
	throws InterruptedException {
	target.wait(time.getMilliseconds(), time.getNanoseconds());
    }

    /** Returns <code>millis * 1000000 + nanos</code>. */
    public long time() {
	return (millis * 1000000 + nanos);
    }
}
