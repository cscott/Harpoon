package javax.realtime;

public abstract class Timer extends AsyncEvent {
    /** This class defines basic functionality available to all timers. */

    protected boolean enabled = true;
    protected boolean started = false;
    protected Clock defaultClock;

    protected Timer(HighResolutionTime t, Clock c,
		    AsyncEventHandler handler) {
	// TODO
    }

    public ReleaseParameters createReleaseParameters() {
	// TODO

	return null;
    }

    public void destroy() {
	// TODO
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
	// TODO

	return null;
    }

    public boolean isRunning() {
	return (started && enabled);
    }

    public void reschedule(HighResolutionTime time) {
	// TODO
    }

    public void start() {
	started = true;
	// TODO
    }

    public boolean stop() {
	boolean wasStarted = started;
	started = false;
	// TODO

	return (wasStarted && enabled);
    }
}
