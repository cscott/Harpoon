package javax.realtime;

/** Never thrown! */
public class ScopedCycleException extends Exception {

    /** A constructor for <code>ScopedCycleException</code>. */
    public ScopedCycleException() {
	super();
    }

    /** A descriptive constructor for <code>ScopedCycleException</code>.
     *
     *  @param s Description of the error.
     */
    public ScopedCycleException(String s) {
	super(s);
    }
}
