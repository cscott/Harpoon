package javax.realtime;

/** <code>PhysicalMemoryManger</code> can only accommodate one
 *  filter object for each type of memory. It throws this exception
 *  if an attempt is made to register more than one filter for a
 *  type of memory.
 */
public class DuplicateFilterException extends Exception {

    /** A constructor for <code>DuplicateFilterException</code>. */
    public DuplicateFilterException() {
	super();
    }

    /** A descriptive constructor for <code>DuplicateFilterException</code>.
     *
     *  @param s Description of the error.
     */
    public DuplicateFilterException(String s) {
	super(s);
    }
}
