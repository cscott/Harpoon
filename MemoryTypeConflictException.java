package javax.realtime;

public class MemoryTypeConflictException extends Exception
    implements java.io.Serializable {
    /** This exception is thrown when the <code>PhysicalMemoryManager</code>
     *  is given conflicting specifications for memory. The conflict
     *  can be between types in an array of memory type specifiers,
     *  or between the specifiers and a specified base address.
     */

    public MemoryTypeConflictException() {
	super();
    }

    public MemoryTypeConflictException(String s) {
	super(s);
    }
}