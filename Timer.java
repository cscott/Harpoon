package javax.realtime;

public abstract class Timer extends AsyncEvent {
    /** This class defines basic functionality available to all timers. */

    protected boolean enabled = true;
    protected boolean started = false;
    protected Clock defaultClock;
    protected RelativeTime fireAfter;
    protected AsyncEventHandler handler;

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
    }

    public ReleaseParameters createReleaseParameters() {
	return new AperiodicParameters(null, fireAfter, handler, null);
    }

    public void destroy() {
	defaultClock = null;
	fireAfter = null;
	handler = null;
    }

    public void disable() {
	enabled = false;
    }

    public void enable() {
	enabled = true;
    }

    public Clock getClock() {
	return defaultClock;
    }

    public AbsoluteTime getFireTime() {
	return new AbsoluteTime(fireAfter.getMilliseconds() -
				defaultClock.getTime().getMilliseconds() +
				Clock.getRealtimeClock().getTime().getMilliseconds(),
				fireAfter.getNanoseconds() -
				defaultClock.getTime().getNanoseconds() +
				Clock.getRealtimeClock().getTime().getNanoseconds());
    }

    public boolean isRunning() {
	return (started && enabled);
    }

    public void reschedule(HighResolutionTime time) {
	if (time instanceof AbsoluteTime)
	    fireAfter.set(time.getMilliseconds() - defaultClock.getTime().getMilliseconds(),
			  time.getNanoseconds() - defaultClock.getTime().getNanoseconds());
	else fireAfter = (RelativeTime)time;
    }

    public void start() {
	started = true;
	// TODO
    }

    public boolean stop() {
	boolean wasStarted = started && enabled;
	started = false;
	// TODO

	return wasStarted;
    }
}
