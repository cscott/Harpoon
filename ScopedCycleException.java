package javax.realtime;

/** Never thrown! */
public class ScopedCycleException extends Exception
    implements java.io.Serializable {

    /** A constructor for <code>ScopedCycleException</code>. */
    public ScopedCycleException() {
	super();
    }

    /** A descriptive constructor for <code>ScopedCycleException</code>. */
    public ScopedCycleException(String s) {
	super(s);
    }
}
