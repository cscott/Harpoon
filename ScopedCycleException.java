package javax.realtime;

/** Never thrown! */
public class ScopedCycleException extends Exception
    implements java.io.Serializable {

    /** A constructor for <code>ScopedCycleException</code>. */
    public ScopedCycleException() {
	assert false: "ScopedCycleException should never be thrown.";
	super();
    }

    /** A descriptive constructor for <code>ScopedCycleException</code>. */
    public ScopedCycleException(String s) {
	assert false: "ScopedCycleException should never be thrown: " + s;
	super(s);
    }
}
