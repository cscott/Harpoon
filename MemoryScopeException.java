package javax.realtime;

/** Thrown if construction of any of the wait-free queues is
 *  attempted with the ends of the queues in incompatible memory areas.
 */
public class MemoryScopeException extends Exception {

    /** A constructor for <code>MemoryScopeException</code>. */
    public MemoryScopeException() {
	super();
    }

    /** A descriptive constructor for <code>MemoryScopeException</code>. */
    public MemoryScopeException(String s) {
	super(s);
    }
}
