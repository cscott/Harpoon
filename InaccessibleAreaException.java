package javax.realtime;

/** The specified memory area is not above the current
 *  allocation context on the current thread scope stack.
 */
public class InaccessibleAreaException extends Exception {

    /** A constructor for <code>InaccessibleAreaException</code>. */
    public InaccessibleAreaException() {
	super();
    }

    /** A descriptive constructor for <codeInaccessibleAreaException</code>.
     *
     *  @param s Description of the error.
     */
    public InaccessibleAreaException(String s) {
	super(s);
    }
}
