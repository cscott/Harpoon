package javax.realtime;

public class ResourceLimitError extends Error
    implements  java.io.Serializable {
    /** Thrown if an attempt is made to exceed a system resource limit,
     *  such as the maximum number of locks.
     */

    public ResourceLimitError() {
	super();
    }

    public ResourceLimitError(String s) {
	super(s);
    }
}
