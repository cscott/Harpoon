package javax.realtime;

/** This exception is thrown when the <code>PhysicalMemoryManager</code>
 *  is given conflicting specifications for memory. The conflict
 *  can be between types in an array of memory type specifiers,
 *  or between the specifiers and a specified base address.
 */
public class MemoryTypeConflictException extends Exception {

    /** A constructor for <code>MemoryTypeConflictException</code>. */
    public MemoryTypeConflictException() {
	super();
    }

    /** A descriptive constructor for <code>MemoryTypeConflictException</code>.
     *
     *  @param s A description of the exception.
     */
    public MemoryTypeConflictException(String s) {
	super(s);
    }
}
