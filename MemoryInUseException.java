package javax.realtime;

public class MemoryInUseException extends Exception
    implements java.io.Serializable {
    /** Thrown when an attempt is made to allocate a range of
     *  physical or virtual memory that is already in use.
     */

    public MemoryInUseException() {
	super();
    }

    public MemoryInUseException(String s) {
	super(s);
    }
}
