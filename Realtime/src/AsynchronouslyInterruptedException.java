// AsynchronouslyInterruptedException.java, created by Harvey Jones, documented by Dumitru Daniliuc
// Copyright (C) 2003 Harvey Jones, Dumitru Daniliuc
// Licensed under the terms of the GNU GPL; see COPYING for details.
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
    public boolean pending = false;
    private boolean doingInterruptible = false;
    private static AsynchronouslyInterruptedException generic;

    /** Create an instance of <code>AsynchronouslyInterruptedException</code>. */
    public AsynchronouslyInterruptedException() {}

    /** Defer the throwing of this exception. If <code>interrupt()</code> is
     *  called when this exception is disabled, the exception is put in
     *  pending state. The exception will be thrown if this exception is
     *  subsequently enabled. This is valid only within a call to
     *  <code>doInterruptible()</code>. Otherwise it returns false and
     *  does nothing.
     *
     *  @return True if <code>this</code> is disabled and invoked within a call to
     *          <code>doInterruptable()</code>. False if <code>this</code> is enabled
     *          and invoked within a call to <code>doInterruptable()</code> or
     *          invoked outside of a call to <code>doInterruptable()</code>.
     */
    // I think this is OK
    public boolean disable() {
	if(!doingInterruptible || this.enabled){
	    return false;
	}
	this.enabled = false;
	return true;
    }

    /** Execute the <code>run()</code> method of the given <code>Interruptible</code>.
     *  This method may be on the stack in exacly one <code>RealtimeThread</code>.
     *  An attempt to invoke this method in a thread while it is on the stack of
     *  another of the same thread will cause an immediate return with a value of false.
     *
     *  @param logic An instance of an <code>Interruptable</code> whose <code>run()</code>
     *               method will be called.
     *  @return True if the method call completed normally. False if another call to
     *          <code>doInterruptible()</code> has not completed.
     */
    // This might be OK
    public boolean doInterruptible(Interruptible logic) {
	if(doingInterruptible){
	    return false;
	}
	doingInterruptible = true;
	try{
	    logic.run(this);
	} catch(AsynchronouslyInterruptedException aie){ // Think this will work
	    aie.happened(false);
	    logic.interruptAction(aie);
	    return false;
	}
	doingInterruptible = false;
	return true;
    }

    /** Enable the throwing of this exception. This method is valid only within a call
     *  to <code>doInterruptible()</code>. If invoked outside of a call to
     *  <code>doInterruptible()</code> this method returns false and does nothing.
     *
     *  @return True if <code>this</code> is enabled and invoked within a call to
     *          <code>doInterruptible()</code>. False if <code>this</code> is disabled
     *          and invoked within a call to <code>doInterruptible()</code> or invoked
     *          outside of a call to <code>doInterruptible()</code>.
     */
    // Umm, sure, why not?
    public boolean enable() {
	if(!doingInterruptible){
	    return false;
	}

	if(!enabled){
	    return enabled = true;
	}

	return false;
    }

    /** Make this exception the current exception if <code>doInterruptible()</code>
     *  has been invoked and not completed.
     *
     *  @return True if <code>this</code> was fired. False if there is no current
     *          invocation of <code>doInterruptible()</code> (with no other effect),
     *          if there is already a current <code>doInterruptible()</code>, or if
     *          <code>disable()</code> has been called.
     */
    // Mostly unimplemented
    public boolean fire() {
	boolean thisWasFired = true; // How do we check this?!
	if(!doingInterruptible){
	    return false;
	}
	
	// TODO: Hey, it's the hard stuff!
	
	return thisWasFired;
    }

    /** Gets the system generic <code>AsynchronouslyInterruptedException</code>,
     *  which is generated when <code>RealtimeThread.interrupt()</code> is invoked.
     *
     *  @return The generic <code>AsynchronouslyInterruptedException</code>.
     */
    public static AsynchronouslyInterruptedException getGeneric() {
	return generic;
    }

    /** Used with an instance of this exception to see if the current
     *  exception is this exception.
     *
     *  @param propagate If true and this exception is not the current one propagate
     *                   the exception. If false, then the state of this is set to
     *                   nonpending (i.e., it will stop propagating).
     *  @return True if this is the current exception. False if this is not the
     *          current exception.
     */
    // Implement this algorighm from the Dibble book/spec. Go with the spec where there's a conflict
    public boolean happened(boolean propagate) {
	boolean match = (this == generic); // TODO: Figure out what the hell their match() semantics are.
	if(getGeneric() == null){
	    return false;
	}
	if(match){
	    pending = false;
	    return propagate;  // Book says return false, spec says return propagate.
	} else {
	    if(propagate){
		propagate();
		return true;
	    } else {
		return true;
	    }
	}
	
    }

    /** Query the enabled status of this exception.
     *
     *  @return True if this is enabled. False otherwise.
     */
    public boolean isEnabled() {
	return enabled;
    }

    /** Cause the pending exception to continue up the stack. */
    public static void propagate() {
	// TODO: Deep VM magic. Here there be dragons.
    }
}
