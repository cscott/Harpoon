package javax.realtime;

/** A special exception that is thrown in response to an attempt to
 *  asynchronously transfer the locus of control of a <code>RealtimeThread</code>.
 *  <p>
 *  When a method is declared with <code>AsynchronouslyInterruptedException</code>
 *  in its <code>throws</code> clause the platform is expected to
 *  asynchronously throw this exception if <code>RealtimeThread.interrupt()</code>
 *  is called while the method is executing, or if such an interrupt
 *  is pending any time control returns to the method. The interrupt
 *  is <i>not</i> thrown while any methods it invokes are executing,
 *  unless they are, in turn, declared to throw the exception. This
 *  is intended to allow long-running compuations to be terminated
 *  without the overhead or latency of polling with
 *  <code>java.lang.Thread.interrupted()</code>.
 *  <p>
 *  The <code>throws AsynchronouslyInterruptedException</code> clause
 *  is a marker on a stack frame which allows a method to be statically
 *  marked as asynchronously interruptible. Only methods that are
 *  marked this way can be interrupted.
 *  <p>
 *  When <code>Thread.interrupt(), interrupt()</code>, or <code>this.fire</code>
 *  is called, the <code>AsynchronouslyInterruptedException</code> is
 *  compared against any currently pending <code>AsynchronouslyInterruptedException</code>
 *  on the thread. If there none, or the depth of the
 *  <code>AsynchronouslyInterruptedException</code> is less than the
 *  currently pending <code>AsynchronouslyInterruptedException</code>
 *  -- i.e., it is targeted at a less deeply nested method call --
 *  it becomes the currently pending interrupt. Otherwise, it is discarded.
 *  <p>
 *  If the current method is interruptible, the exception is thrown on
 *  the thread. Otherwise, it just remains pending until control
 *  returns to an interruptible method, at which point the
 *  <code>AsynchronouslyInterruptedException</code> is thrown. When
 *  an interrupt is caught, the caller should invoke the
 *  <code>happened</code> method on the <code>AsynchronouslyInterruptedException</code>
 *  in which it is interested to see if it matches the pending
 *  <code>AsynchronouslyInterruptedException</code>. If so, the
 *  pending <code>AsynchronouslyInterruptedException</code> is cleared
 *  from the thread. Otherwise, it will continue to propagate outward.
 *  <p>
 *  <code>Thread.interrupt()</code> and <code>RealtimeThread.interrupt()</code>
 *  generate a system available generic <code>AsynchronouslyInterruptedException</code>
 *  which will always propagate outward through interruptible methods until
 *  the generic <code>AsynchronouslyInterruptedException</code> is
 *  identified and stopped. Other sources (e.g., <code>this.fire()</code>
 *  and <code>Timed</code>) will generate a specific instance of
 *  <code>AsynchronouslyInterruptedException</code> which applications
 *  can identify and thus limit propagation.
 */
public class AsynchronouslyInterruptedException extends InterruptedException {

    private boolean enabled = true;

    /** Create an instance of <code>AsynchronouslyInterruptedException</code>. */
    public AsynchronouslyInterruptedException() {}

    /** Defer the throwing of this exception. If <code>interrupt()</code> is
     *  called when this exception is disabled, the exception is put in
     *  pending state. The exception will be thrown if this exception is
     *  subsequently enabled. This is valid only within a call to
     *  <code>doInterruptible()</code>. Otherwise it returns false and
     *  does nothing.
     */
    public boolean disable() {
	enabled = false;
	// TODO

	return false;
    }

    /** Execute the <code>run()</code> method of the given <code>Interruptible</code>.
     *  This method may be on the stack in exacly one <code>RealtimeThread</code>.
     *  An attempt to invoke this method in a thread while it is on the stack of
     *  another of the same thread will cause an immediate return with a value of false.
     */
    public boolean doInterruptible(Interruptible logic) {
	// TODO

	return false;
    }

    /** Enable the throwing of this exception. This is valid only within a call to
     *  <code>doInterruptible()</code>. Otherwise it returns false and does nothing.
     */
    public boolean enable() {
	enabled = true;
	// TODO

	return false;
    }

    /** Make this exception the current exception if <code>doInterruptible()</code>
     *  has been invoked and not completed.
     */
    public boolean fire() {
	// TODO

	return false;
    }

    /** Return the system generic <code>AsynchronouslyInterruptedException</code>,
     *  which is generated when <code>RealtimeThread.interrupt()</code> is invoked.
     */
    public static AsynchronouslyInterruptedException getGeneric() {
	// TODO

	return null;
    }

    /** Used with an instance of this exception to see if the current
     *  exception is this exception.
     */
    public boolean happened(boolean propagate) {
	// TODO

	return false;
    }

    /** Query the enabled status of this exception. */
    public boolean isEnabled() {
	return enabled;
    }

    /** Cause the pending exception to continue up the stack. */
    public static void propagate() {
	// TODO
    }
}
