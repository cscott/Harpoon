package javax.realtime;

public class OneShotTimer extends Timer {
    /** A timed <code>AsyncEvent</code> that is driven by a clock. It will
     *  fire off once, when the clock time reaches the timeout time. If the
     *  clock time has already passed the timeout time, it will fire
     *  immediately.
     */

    public OneShotTimer(HighResolutionTime time,
			AsyncEventHandler handler) {
	// TODO

	// This line inserted, just to make everything compile!
	super(time, null, handler);
    }

    public OneShotTimer(HighResolutionTime start, Clock clock,
			AsyncEventHandler handler) {
	this(start, handler);
	defaultClock = clock;
    }
}
