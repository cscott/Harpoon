package javax.realtime;

public class Timed extends AsynchronouslyInterruptedException {
    /** Create a scope in a <code>RealtimeThread</code> for which
     *  <code>interrupt()</code> will be called at the expiration
     *  of a timer. This timer will begin measuring time at some
     *  point between the time <code>doInterruptible()</code> is
     *  invoked and the time the <code>run()</code> method of the
     *  <code>Interruptible</code> object is invoked. Each call of
     *  <code>doInterruptible()</code> on an instance of <code>Timed</code>
     *  will restart the timer for the amount of time given in the
     *  constructor or the most recent invocation of <code>resetTime()</code>.
     *  All memory use of <code>Timed</code> occurs during construction
     *  or the first invocation of <code>doInterruptible()</code>.
     *  Subsequent invokes of <code>doInterruptible()</code> do not
     *  allocate memory.
     */

    public Timed(HighResolutionTime time)
	throws IllegalArgumentException {
	// TODO
    }

    public boolean doInterruptible(Interruptible logic) {
	// TODO

	return false;
    }

    public void resetTime(HighResolutionTime time) {
	// TODO
    }
}
