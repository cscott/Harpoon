package javax.realtime;

/** A timed <code>AsyncEvent</code> that is driven by a clock. It will
 *  fire off once, when the clock time reaches the timeout time. If the
 *  clock time has already passed the timeout time, it will fire
 *  immediately.
 */
public class OneShotTimer extends Timer {

    /** Create an instance of <code>AsyncEvent</code> that will execute
     *  its fire method at the expiration of the given time.
     *
     *  @param time After timeout <code>time</code> units from 'now'
     *              fire will be executed.
     *  @param handler The <code>AsyncEventHandler</code> that will be
     *                 scheduled when fire is executed.
     */
    public OneShotTimer(HighResolutionTime time,
			AsyncEventHandler handler) {
	super(time, Clock.getRealtimeClock(), handler);
    }

    /** Create an instance of <code>AsyncEvent</code>, based on the
     *  given clock, that will execute its fire method at the expiration
     *  of the given time.
     *
     *  @param start Start time for timer.
     *  @param clock The timer will increment based on this clock.
     *  @param handler The <code>AsyncEventHandler</code> that will be
     *                 scheduled when fire is executed.
     */
    public OneShotTimer(HighResolutionTime start, Clock clock,
			AsyncEventHandler handler) {
	super(start, clock, handler);
    }
}
