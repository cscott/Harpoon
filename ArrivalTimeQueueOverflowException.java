package javax.realtime;

/** If an arrival time occurs and should be queued but the queue already holds
 *  a number of times equal to the initial queue length defined by this then
 *  the <code>fire()</code> method shall throw an
 *  <code>ArrivalTimeQueueOverflowException</code>. If the arrival time is a
 *  result of a happening to which the instance of <code>AsyncEventHandler</code>
 *  is bound then the arrival time is ignored.
 */
public class ArrivalTimeQueueOverflowException extends Exception {

    /** A constructor for <code>ScopedCycleException</code>. */
    public ArrivalTimeQueueOverflowException() {
	super();
    }

    /** A descriptive constructor for <code>ScopedCycleException</code>.
     *
     *  @param s A description of the exception.
     */
    public ArrivalTimeQueueOverflowException(String s) {
	super(s);
    }
}
