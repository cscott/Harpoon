package javax.realtime;

public class UnknownHappeningException extends Exception
    implements java.io.Serializable {
    /** Thrown when <code>bindTo()</code> is called with an
     *  illegal <code>happening</code>.
     */

    public UnknownHappeningException() {
	super();
    }

    public UnknownHappeningException(String s) {
	super(s);
    }
}
