// RealtimeClock.java, created by cata
// Copyright (C) 2001 Catalin Francu <cata@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.

package javax.realtime;

/** <code>RealtimeClock</code> implements a real-time clock. */
public class RealtimeClock extends Clock {
    
    private RelativeTime resolution;

    /** Create an instance of <code>RealtimeClock</code>. */
    public RealtimeClock() {
	this(1, 0);
    }
    
    /** Create an instance of <code>RealtimeClock</code> using given parameters. */
    public RealtimeClock(long millis, int nanos) {
	super();
	debug("Entering the constructor of RealtimeClock... (after calling super())");
	resolution = new RelativeTime(millis, nanos);
	debug("Exiting the constructor of RealtimeClock...");
    }

    /** Return the resolution of this <code>RealtimeClock</code> instance. */
    public RelativeTime getResolution() {
	return new RelativeTime(resolution);
    }

    /** Sets the resolution of this <code>RealtimeClock</code> instance. */
    public void setResolution(RelativeTime res) {
	resolution.set(res);
    }
    
    protected static native long getTimeInC();

    /** Returns the time in an <code>AbstractTime</code>. */
    public void getTime(AbsoluteTime time) {
	long micros  = getTimeInC();
	time.set(micros / 1000, (int)((micros % 1000) * 1000));
    }

    // For debugging
    public static void debug(String msg) {
	System.out.println(msg);
    }
}
