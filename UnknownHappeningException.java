package javax.realtime;

/** Thrown when <code>bindTo()</code> is called with an
 *  illegal <code>happening</code>.
 */
public class UnknownHappeningException extends Exception
    implements java.io.Serializable {

    /** A constructor for <code>UnknownHappeningException</code>. */
    public UnknownHappeningException() {
	super();
    }

    /** A descriptive constructor for <code>UnknownHappeningException</code> */
    public UnknownHappeningException(String s) {
	super(s);
    }
}
