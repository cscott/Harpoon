package javax.realtime;

public class MemoryScopeException extends Exception
    implements java.io.Serializable {
    /** Thrown if construction of any of the wait-free queues is
     *  attempted with the ends of the queues in incompatible memory areas.
     */

    public MemoryScopeException() {
	super();
    }

    public MemoryScopeException(String s) {
	super(s);
    }
}
