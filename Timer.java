package javax.realtime;

/** A <code>Timer</code> is a timed event that measures time
 *  relative to a given <code>Clock</code>. This class defines
 *  basic functionality available to all timers. Applications
 *  will generally use either <code>PeriodicTimer</code> to
 *  create an event that is fired repeatedly at regular intervals,
 *  of <code>OneShotTimer</code>, which provides the basic
 *  facilities of something that ticks along following some
 *  time line (real-time, cpu-time, user-time, simulation-time,
 *  etc.). All timers are created disabled and do nothing
 *  until <code>start()</code> is called.
 */
public abstract class Timer extends AsyncEvent {

    protected boolean enabled = true;
    protected boolean started = false;
    protected Clock defaultClock;
    protected RelativeTime fireAfter;
    protected AsyncEventHandler handler;

    /** Create a timer that fires at time <code>t</code>, according
     *  to <code>Clock c</code> and is handled by the specified handler.
     */
    protected Timer(HighResolutionTime t, Clock c,
		    AsyncEventHandler handler) {
	if (t instanceof AbsoluteTime)
	    fireAfter = new RelativeTime(((RelativeTime)t).getMilliseconds() -
					 c.getTime().getMilliseconds(),
					 ((RelativeTime)t).getNanoseconds() -
					 c.getTime().getNanoseconds());
	else fireAfter = (RelativeTime)t;

	defaultClock = c;
	this.handler = handler;
	enabled = false;
    }

    /** Create a <code>ReleaseParameters</code> block appropriate to the
     *  timing characteristics of this event. The default is the most
     *  pessimistic: <code>AperiodicParameters</code>. This is typically
     *  called by code that is setting up a handler for this event that
     *  will fill in the parts of the release parameters that it knows
     *  the values for, like cost.
     */
    public ReleaseParameters createReleaseParameters() {
	return new AperiodicParameters(null, fireAfter, handler, null);
    }

    /** Stop this from counting and return as many of its resources as
     *  possible back to the system.
     */
    public void destroy() {
	defaultClock = null;
	fireAfter = null;
	handler = null;
	enabled = false;
    }

    /** Disable this timer, preventing it from firing. It may subsequently
     *  be re-enabled. If the timer is disabled when its fire time occurs
     *  then it will not fire. However, a disabled timer continues to
     *  count while it is disabled and if it is subsequently reabled
     *  before its fire time occures and is enabled when its fire time
     *  occurs, it will fire. However, it is important to note that this
     *  method does not delay the time before a possible firing. For
     *  example, if the timer is set to fire at time 42 and the
     *  <code>disable()</code> is called at time 30 and <code>enabled()</code>
     *  is called at time 40, the firing will occur at time 42 (not time 52).
     *  These semantics imply also, that firings are not queued. Using
     *  the above example, if <code>enabled()</code> was called at time
     *  43 no firing will occur, since at time 43 the timer is disabled.
     */
    public void disable() {
	enabled = false;
    }

    /** Re-enable this timer after it has been disabled. */
    public void enable() {
	enabled = true;
    }

    /** Return the Clock that this timer is based on. */
    public Clock getClock() {
	return defaultClock;
    }

    /** Get the time at which this event will fire. */
    public AbsoluteTime getFireTime() {
	return new AbsoluteTime(fireAfter.getMilliseconds() -
				defaultClock.getTime().getMilliseconds() +
				Clock.getRealtimeClock().getTime().getMilliseconds(),
				fireAfter.getNanoseconds() -
				defaultClock.getTime().getNanoseconds() +
				Clock.getRealtimeClock().getTime().getNanoseconds());
    }

    /** Tests this to determine if this has been started and is in a
     *  state (enabled) such that when the given time occurs it will
     *  fire the event.
     *
     *  @return True if the timer has been started and is in the enabled
     *          state. False, if the timer has either not been started,
     *          started and is in the disabled state, or started and stopped.
     */
    public boolean isRunning() {
	return (started && enabled);
    }

    /** Change the schedule time for this event; can take either absolute
     *  or relative times.
     */
    public void reschedule(HighResolutionTime time) {
	if (time instanceof AbsoluteTime)
	    fireAfter.set(time.getMilliseconds() - defaultClock.getTime().getMilliseconds(),
			  time.getNanoseconds() - defaultClock.getTime().getNanoseconds());
	else fireAfter = (RelativeTime)time;
    }

    /** A Timer starts measuring time from when it is started. */
    public void start() {
	started = true;
	// TODO
    }

    /** Stops a timer that is running and changes its state to
     *  <i>not started</i>.
     *  @return True, if this was started and enabled and stops this.
     *          False, if this was not started or disabled.
     */
    public boolean stop() {
	boolean wasStarted = started && enabled;
	started = false;
	// TODO

	return wasStarted;
    }
}
