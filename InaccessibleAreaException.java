package javax.realtime;

public class InaccessibleAreaException extends Exception
    implements java.io.Serializable {
    /** The specified memory area is not above the current
     *  allocation context on the current thread scope stack.
     */

    public InaccessibleAreaException() {
	super();
    }

    public InaccessibleAreaException(String s) {
	super(s);
    }
}
