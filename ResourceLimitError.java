package javax.realtime;

/** Thrown if an attempt is made to exceed a system resource limit,
 *  such as the maximum number of locks.
 */
public class ResourceLimitError extends Error {

    /** A constructor for <code>ResourceLimitError</code>. */
    public ResourceLimitError() {
	super();
    }

    /** A descriptive constructor for <code>ResourceLimitError</code>. */
    public ResourceLimitError(String s) {
	super(s);
    }
}
