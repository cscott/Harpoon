package javax.realtime;

public class DuplicateFilterException extends Exception
    implements java.io.Serializable {
    /** <code>PhysicalMemoryManger</code> can only accommodate one
     *  filter object for each type of memory. It throws this exception
     *  if an attempt is made to register more than one filter for a
     *  type of memory
     */

    public DuplicateFilterException() {
	super();
    }

    public DuplicateFilterException(String s) {
	super(s);
    }
}
