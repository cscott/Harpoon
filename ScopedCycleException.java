package javax.realtime;

/** Thrown when a user tries to enter a <code>ScopedMemory</code>
 *  that is alread accessible (<code>ScopedMemory</code> is present
 *  on stack) or when a user tries to create <code>ScopedMemory</code>
 *  cycle spanning threads (tries to make cycle in the
 *  <code>VMScpedMemory</code> tree structure).
 */
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
