// Translate.java, created Sat Aug  8 10:53:03 1998 by cananian
package harpoon.IR.QuadSSA;

import harpoon.ClassFile.*;
/**
 * <code>Translate</code> is a utility class to implement the
 * actual Bytecode-to-QuadSSA translation.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: Translate.java,v 1.1 1998-08-19 05:13:10 cananian Exp $
 */

class Translate  { // not public.
    static final Quad trans(harpoon.ClassFile.Bytecode.Code bytecode) {
	Quad quads = new HEADER();
	// FIXME do schtuff here.
	return quads;
    }
}
