// Clock.java, created by cata
// Copyright (C) 2001 Catalin Francu <cata@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.

package javax.realtime;

/** A clock advances from the past, through the present, into the future.
 *  It has a concept of now that can be queried through
 *  <code>Clock.getTime()</code>, and it can have events queued on it
 *  which will be fired when their appointed time is reached. There are
 *  many possible subclasses of clocks: real-time clocks, user time
 *  clocks, simulation time clocks. The idea of using multiple clocks
 *  may at first seem unusual but we allow it as a possible resource
 *  allocation strategy. Consider a real-time system where the natural
 *  events of the system have different tolerances for jitter (jitter
 *  refers to the distribution of the differences between when the
 *  events are actually raised or noticed by the software and when they
 *  should have really occurred according to time in the real-world).
 *  Assume the system functions properly if event A is noticed or raised
 *  within plus or minus 100 seconds of the actual time it should occur
 *  but event B must be noticed or raised within 100 microseconds of its
 *  actual time. Further assume, without loss of generality, that events
 *  A and B are periodic. An application could then create two instances
 *  of <code>PeriodicTimer</code> based on two clocks. The timer for
 *  event B should be based on a Clock which checks its queue at least
 *  every 100 microseconds but the timer for event A could be based on a
 *  Clock that checked its queue only every 100 seconds. This use of two
 *  clocks reduces the queue size of the accurate clock and thus queue
 *  management overhead is reduced.
 */
public abstract class Clock {
    
    private RelativeTime resolution;
    private static RealtimeClock rtc = null;
    private static long entered = 0;   // debugging flag...
    
    public Clock() {
	debug("Entering and leaving Clock()");
    }
    
    /** There is always one clock object available: a realtime clock that
     *  advances in sync with the external world. This is the default Clock.
     */
    public static Clock getRealtimeClock() {
	debug("Entered Clock.getRealtimeClock()... ");
	// This should never be printed (unless there's a bug).
	if (entered++ > 0) debug("Where is that ... loop?");
	if (rtc == null) rtc = new RealtimeClock();
	entered--;
	debug("Leaving Clock.getRealtimeClock()...");
	return rtc;
    }

    /** Return the resolution of the clock -- the interval between ticks. */
    public abstract RelativeTime getResolution();

    /** Return the current time in a freshly allocated object. */
    public AbsoluteTime getTime() {
	AbsoluteTime time = new AbsoluteTime();
	getTime(time);
	return time;
    }

    /** Return the current time in an existing object. The time represented
     *  by the given <code>AbsoluteTime</code> is changed some time between
     *  the invocation of the method and the return of the method.
     */
    public abstract void getTime(AbsoluteTime time);

    /** Set the resolution of <code>this</code>. For some hardware clocks
     *  setting resolution impossible and if called on those nothing happens.
     */
    public abstract void setResolution(RelativeTime resolution);

    // For debugging
    public static void debug(String msg) {
	System.out.println(msg);
    }
}
