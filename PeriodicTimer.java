package javax.realtime;

public class PeriodicTimer extends Timer {
    /** An <code>AsyncEvent</code> whose fire method is executed periodically
     *  according to the given parameters. If a clock is given, calculation
     *  of the period uses the increments of the clock. If an interval is
     *  given or set the system guarantees that the fire method will execute
     *  <code>interval</code> time units after the last execution or its
     *  given start time as appropriate. If one of the
     *  <code>HighResolutionTime</code> argument types is
     *  <code>RationalTime</code> then the system guarantees that the fire
     *  method will be executed exactly <code>frequence</code> times every
     *  unit time by adjusting the interval between executions of
     *  <code>fire()</code>. This is similar to a thread with
     *  <code>PeriodicParameters</code> except that it is lighter weight.
     *  If a <code>PeriodicTimer</code> is disabled, it still counts, and
     *  if enabled at some later time, it will fire at its next scheduled
     *  fire time.
     */

    private RelativeTime interval;

    public PeriodicTimer(HighResolutionTime start,
			 RelativeTime interval,
			 AsyncEventHandler handler) {
	super(start, Clock.getRealtimeClock(), handler);
	this.interval = interval;
    }

    public PeriodicTimer(HighResolutionTime start,
			 RelativeTime interval, Clock clock,
			 AsyncEventHandler handler) {
	super(start, clock, handler);
	this.interval = interval;
    }

    public ReleaseParameters createReleaseParameters() {
	return new PeriodicParameters(getFireTime(), interval, null, null,
				      handler, null);
    }

    public void fire() {
	handler.run();
    }

    public AbsoluteTime getFireTime() {
	return super.getFireTime();
    }

    public RelativeTime getInterval() {
	return interval;
    }

    public void setInterval(RelativeTime interval) {
	this.interval = interval;
    }
}
