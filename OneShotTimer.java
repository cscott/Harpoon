package javax.realtime;

/** A timed <code>AsyncEvent</code> that is driven by a clock. It will
 *  fire off once, when the clock time reaches the timeout time. If the
 *  clock time has already passed the timeout time, it will fire
 *  immediately.
 */
public class OneShotTimer extends Timer {

    /** Create an instance of <code>AsyncEvent</code> that will execute
     *  its fire method at the expiration of the given time.
     */
    public OneShotTimer(HighResolutionTime time,
			AsyncEventHandler handler) {
	super(time, Clock.getRealtimeClock(), handler);
    }

    /** Create an instance of <code>AsyncEvent</code>, based on the
     *  given clock, that will execute its fire method at the expiration
     *  of the given time.
     */
    public OneShotTimer(HighResolutionTime start, Clock clock,
			AsyncEventHandler handler) {
	super(start, clock, handler);
    }
}
