package javax.realtime;

/** Thrown by the <code>fire()</code> method of an instance of
 *  <code>AsyncEvent</code> when the bound instance of
 *  <code>AsyncEventHandler</code> with a <code>ReleaseParameters</code>
 *  type of <code>SporadicParameters</code> has
 *  <code>mitViolationExcept</code> behavior and the minimum
 *  interarrival time gets violated.
 */
public class MITViolationException extends Exception
    implements java.io.Serializable {

    /** A constructor for <code>MITViolationException</code>. */
    public MITViolationException() {
	super();
    }

    /** A descriptive constructor for <code>MITViolationException</code>. */
    public MITViolationException(String s) {
	super(s);
    }
}
