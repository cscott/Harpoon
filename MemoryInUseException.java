package javax.realtime;

/** Thrown when an attempt is made to allocate a range of
 *  physical or virtual memory that is already in use.
 */
public class MemoryInUseException extends Exception
    implements java.io.Serializable {

    /** A constructor for <code>MemoryInUseException</code>. */
    public MemoryInUseException() {
	super();
    }

    /** A descriptive constructor for <code>MemoryInUseException</code>. */
    public MemoryInUseException(String s) {
	super(s);
    }
}
