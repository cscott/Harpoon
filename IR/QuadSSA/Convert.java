// Convert.java, created Sat Aug  8 10:53:03 1998 by cananian
package harpoon.IR.QuadSSA;

import harpoon.ClassFile.*;
/**
 * <code>Convert</code> is a utility class to implement the
 * actual Bytecode-to-QuadSSA conversion.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: Convert.java,v 1.2 1998-08-08 15:13:56 cananian Exp $
 */

class Convert  {
    // Disable constructor.
    private Convert() { }

    static final Quad convert(harpoon.ClassFile.Bytecode.Code bytecode) {
	Quad quads = new HEADER();
	// FIXME do schtuff here.
	return quads;
    }
}
