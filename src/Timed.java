package javax.realtime;

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
 *  <p>
 *  Usage: <code>new Timed(T).doInterruptible(interruptible);</code>
 */
public class Timed extends AsynchronouslyInterruptedException {

    HighResolutionTime timeout;

    /** Create an instance of <code>Timed</code> with a timer set
     *  to timeout. If the time is in the past the
     *  <code>AsynchronouslyInterruptedException</code> mechanism
     *  is immediately activated.
     *
     *  @param time The interval of time between the invocation of
     *              <code>doInterruptible()</code> and when
     *              <code>interrupt()</code> is called on
     *              <code>currentRealtimeThread()</code>. If null the
     *              <code>java.lang.IllegalArgumentException</code> is thrown.
     *  @throws java.lang.IllegalArgumentException
     */
    public Timed(HighResolutionTime time)
	throws IllegalArgumentException {
	timeout = time;
	if (time instanceof AbsoluteTime) {
	    AbsoluteTime a_time = (AbsoluteTime)time;
	    AbsoluteTime at = new AbsoluteTime();
	    Clock.getRealtimeClock().getTime(at);
	    if ((a_time.getMilliseconds() * 1000000 + a_time.getNanoseconds()) <
		(at.getMilliseconds() * 1000000 + a_time.getNanoseconds())) {
		// DO SOMETHING...
	    }
	}
	else {   // time should be instance of RelativeTime
	    RelativeTime r_time = (RelativeTime)time;
	    if ((r_time.getMilliseconds() * 1000000 + r_time.getNanoseconds()) < 0) {
		// DO SOMETHING...
	    }
	}
    }

    /** Execute a timeout method. Starts the timer and executes the
     *  <code>run()</code> method of the given <code>Interruptible</code> object.
     *
     *  @param logic Implements an <code>Interruptible run()</code> method. If
     *               null nothing happens.
     */
    public boolean doInterruptible(Interruptible logic) {
	// TODO

	return false;
    }

    /** To reschedule the timeout for the next invocation of
     *  <code>doInterruptible()</code>.
     *
     *  @param time This can be an absolute time or a relative time. If null
     *              the timeout is not changed.
     */
    public void resetTime(HighResolutionTime time) {
	timeout = time;
    }
}
