package javax.realtime;

/** <code>Interruptible</code> is an interface implemented by classes
 *  that will be used as arguments on the <code>doInterruptible()</code>
 *  of <code>AsynchronouslyInterruptedException</code> and its
 *  subclasses. <code>doInterruptible()</code> invokes the implementation
 *  of the method in this interface. Thus the system can ensure
 *  correctness before invoking <code>run()</code> and correctly
 *  cleaned up after <code>run()</code> returns.
 */
public interface Interruptible {

    /** This method is called by the system if the <code>run()</code> method
     *  is excepted. Using this the program logic can determine if the
     *  <code>run()</code> method completed normally or had its control
     *  asynchronously transferred to its caller.
     *
     *  @param exception Used to invoke methods on
     *                   <code>AsynchronouslyInterruptedException</code> from
     *                   within the <code>interruptAction()</code> method.
     */
    public void interruptAction(AsynchronouslyInterruptedException exception);

    /** The main piece of code that is executed when an implementation is
     *  given to <code>doInterruptible()</code>. When you create a class
     *  that implements this interface (usually through an anonymous inner
     *  class) you must remember to include the <code>throws</code> clause
     *  to make the method interruptible. If the throws clause is omitted
     *  the <code>run()</code> method will not be interruptible.
     *
     *  @param exception Used to invoke methods on
     *                   <code>AsynchronouslyInterruptedException</code> from
     *                   within the <code>interruptAction()</code> method.
     */
    public void run(AsynchronouslyInterruptedException exception)
	throws AsynchronouslyInterruptedException;
}
