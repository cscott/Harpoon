// Interface.java, created Fri Aug  7 05:08:23 1998 by cananian
package silicon.JavaChip;

/**
 * Defines the external interfaces of the java chip.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: Interface.java,v 1.1 1998-08-07 09:29:15 cananian Exp $
 */

public class Interface {
    Thread memory_interface = new Thread() {
	// define memory interface here.
    };
    Thread interrupt_interface = new Thread() {
	// define interrupt system interface here.
    };
}
