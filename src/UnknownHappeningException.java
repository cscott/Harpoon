package javax.realtime;

/** This exception is used to indicate a situation where an instance of
 *  <code>AsyncEvent</code> attempts to bind to a happening that does not exist.
 */
public class UnknownHappeningException extends Exception {

    /** A constructor for <code>UnknownHappeningException</code>. */
    public UnknownHappeningException() {
	super();
    }

    /** A descriptive constructor for <code>UnknownHappeningException</code>
     *
     *  @param s Description of the error.
     */
    public UnknownHappeningException(String s) {
	super(s);
    }
}
