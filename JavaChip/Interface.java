// Interface.java, created Fri Aug  7 05:08:23 1998 by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package silicon.JavaChip;

/**
 * Defines the external interfaces of the java chip.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: Interface.java,v 1.2 1998-10-11 02:37:57 cananian Exp $
 */

public class Interface {
    Thread memory_interface = new Thread() {
	// define memory interface here.
    };
    Thread interrupt_interface = new Thread() {
	// define interrupt system interface here.
    };
}
