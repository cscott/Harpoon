// Timer.java, created by Dumitru Daniliuc
// Copyright (C) 2003 Dumitru Daniliuc
// Licensed under the terms of the GNU GPL; see COPYING for details.
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
    protected RelativeTime timePassed = new RelativeTime(0, 0);
    protected AbsoluteTime lastUpdated = new AbsoluteTime();

    /** Create a timer that fires at the given time based on the given
     *  instance of <code>Clock</code> and is handled by the specified
     *  handler.
     *
     *  @param t The time to fire the event. Will be converted to
     *           absolute time.
     *  @param clock The clock on which to base this time. If null, the
     *               system realtime clock is used.
     *  @param handler The default handler to use for this event. If null, no
     *                 handler is associated with it and nothing will happen
     *                 when this event fires until a handler is provided.
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
     *  will fill in the parts of the release parameters for which it has
     *  values, e.g, cost.
     *
     *  @return A new <code>ReleaseParameters</code> object.
     */
    public ReleaseParameters createReleaseParameters() {
	return new AperiodicParameters(null, fireAfter, handler, null);
    }

    /** Destroy the timer and return all possible resources to the system. */
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
     *  occurs, it will fire.
     */
    public void disable() {
	enabled = false;
    }

    /** Re-enable this timer after it has been disabled. */
    public void enable() {
	enabled = true;
    }

    /** Gets the instance of <code>Clock</code> that this timer is based on.
     *
     *  @return The instance of <code>Clock</code>.
     */
    public Clock getClock() {
	return defaultClock;
    }

    /** Get the time at which this event will fire.
     *
     *  @return An instance of <code>AbsoluteTime</code> object representing
     *          the absolute time at which this will fire.
     */
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
     *
     *  @param time The time to reschedule for this event firing. If null, the
     *              previous fire time is still the time at which this will fire.
     */
    public void reschedule(HighResolutionTime time) {
	if (time instanceof AbsoluteTime)
	    fireAfter.set(time.getMilliseconds() - defaultClock.getTime().getMilliseconds(),
			  time.getNanoseconds() - defaultClock.getTime().getNanoseconds());
	else fireAfter = (RelativeTime)time;
    }

    /** Starts this time. A <code>Timer</code> starts measuring time from
     *  when it is started.
     */
    public void start() {
	started = true;
	Clock.getRealtimeClock().getTime(lastUpdated);
	while (started) {
	    AbsoluteTime temp = new AbsoluteTime();
	    Clock.getRealtimeClock().getTime(temp);
	    RelativeTime difference = temp.subtract(lastUpdated);
	    timePassed = timePassed.add(difference);
	    lastUpdated = temp;
	}
    }

    /** Stops a timer that is running and changes its state to
     *  <i>not started</i>.
     *
     *  @return True, if this was <b>started and enabled</b> and stops this.
     *          False, if this was not <b>started or disabled</b>.
     */
    public boolean stop() {
	boolean wasStarted = started && enabled;
	started = false;
	return wasStarted;
    }
}
