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
    
    /** Convert this time to an absolute time, relative to some clock.
     *  Convenient for situations where you really need an absolute time.
     *   Aloocates a destination object if necessary.
     */
    public abstract AbsoluteTime absolute(Clock clock);

    /** Convert this time to an absolute time, relative to some clock.
     *  Convenient for situations where you really need an absolute time.
     *   Aloocates a destination object if necessary.
     */
    public abstract AbsoluteTime absolute(Clock c, AbsoluteTime dest);
    
    /** Compares this <code>HighResolutionTime</code> with the specified
     *  <code>HighResolutionTime</code>.
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
    
    /** Returns true if the argument object has the same values as this. */
    public boolean equals(Object b) {
	return equals((HighResolutionTime) b);
    }
    
    /** Returns the milliseconds component of this. */
    public final long getMilliseconds()	{
	return millis;
    }
    
    /** Returns the nanoseconds component of this. */
    public final int getNanoseconds()	{
	return nanos;
    }
    
    public int hashCode()	{
	return (int)(nanos+millis*1000000);
    }

    /** Change the association of this from the currently associated
     *  clock to the given clock.
     */
    public abstract RelativeTime relative(Clock clock);

    /** Convert the given instance of <code>HighResolutionTime</code>
     *  to an instance of <code>RelativeTime</code> relative to the
     *  given instance of <code>Clock</code>.
     */
    public abstract RelativeTime relative(Clock clock,
					  HighResolutionTime time);

    /** Changes the time represented by the argument to some time between
     *  the invocation of the method and the return of the method.
     */    
    public void set(HighResolutionTime t) {
	millis = t.getMilliseconds();
	nanos = t.getNanoseconds();
    }

    /** Sets the millisecond component of this to the given argument. */
    public void set(long millisecs) {
	millis = millisecs;
	nanos = 0;
    }
    
    /** Sets the millisecond and nanosecond compenonets of this. */
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

