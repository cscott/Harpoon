// RelativeTime.java, created by cata
// Copyright (C) 2001 Catalin Francu <cata@mit.edu>
//    taking over Bryan Fink's code
// Licensed under the terms of the GNU GPL; see COPYING for details.

// Class Invariant:
//   - Times are consistent at any time. That is, 0<=nanos<1000000. If not,
//     we normalize them, e.g 3ms 3200000ns --> 6ms 200000ns

package javax.realtime;
import java.lang.Math;

public abstract class HighResolutionTime implements Comparable {
    /** Class <code>HighResolutiontime</code> is the base class for
     *  <code>AbsoluteTime, RelativeTime, RationalTime</code>.
     */
    
    private long millis;
    private int nanos;
    protected Clock defaultClock = Clock.getRealtimeClock();
    
    public abstract AbsoluteTime absolute(Clock clock);
    public abstract AbsoluteTime absolute(Clock c, AbsoluteTime dest);
    
    // compareTo(HighResolutionTime)
    //   Compares us to another time. Returns:
    //   -1 if we are less than the other time
    //    0 if we are equal to the other time
    //   +1 if we are greater than the other time
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
    
    // This method added to please the Comparable interface
    public int compareTo(Object b) {
	return compareTo((HighResolutionTime) b);
    }
    
    public boolean equals(HighResolutionTime b)	{
	return (millis == b.getMilliseconds() &&
		nanos == b.getNanoseconds());
    }
    
    // This method added to please the Comparable interface
    public boolean equals(Object b) {
	return equals((HighResolutionTime) b);
    }
    
    public final long getMilliseconds()	{
	return millis;
    }
    
    public final int getNanoseconds()	{
	return nanos;
    }
    
    public int hashCode()	{
	return (int)(nanos+millis*1000000);
    }

    public abstract RelativeTime relative(Clock clock);
    public abstract RelativeTime relative(Clock clock,
					  HighResolutionTime time);
    
    public void set(HighResolutionTime t) {
	millis = t.getMilliseconds();
	nanos = t.getNanoseconds();
    }
    
    public void set(long millisecs) {
	millis = millisecs;
	nanos = 0;
    }
    
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

    public static void waitForObject(Object target, HighResolutionTime time)
	throws InterruptedException {
	target.wait(time.getMilliseconds(), time.getNanoseconds());
    }
}
