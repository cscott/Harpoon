package javax.realtime;

public interface Interruptible {
    /** <code>Interruptible</code> is an interface implemented by classes
     *  that will be used as arguments on the <code>doInterruptible()</code>
     *  of <code>AsynchronouslyInterruptedException</code> and its
     *  subclasses. <code>doInterruptible()</code> invokes the implementation
     *  of the method in this interface. Thus the system can ensure
     *  correctness before invoking <code>run()</code> and correctly
     *  cleaned up after <code>run()</code> returns.
     */

    public void interruptAction(AsynchronouslyInterruptedException exception);

    public void run(AsynchronouslyInterruptedException exception)
	throws AsynchronouslyInterruptedException;
}
