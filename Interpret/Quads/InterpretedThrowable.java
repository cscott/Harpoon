// InterpretedThrowable.java, created Mon Dec 28 01:27:53 1998 by cananian
package harpoon.Interpret.Quads;

import harpoon.IR.Quads.Quad;
/**
 * <code>InterpretedThrowable</code> is a wrapper for an exception within
 * the interpreter.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: InterpretedThrowable.java,v 1.1.2.1 1998-12-28 23:43:21 cananian Exp $
 */
final class InterpretedThrowable extends RuntimeException {
    final ObjectRef ex;
    final String[] stackTrace;
    /** Creates a <code>InterpretedThrowable</code>. */
    InterpretedThrowable(ObjectRef ex, String[] st) {
        this.ex = ex; this.stackTrace = st;
    }
    InterpretedThrowable(ObjectRef ex, StaticState ss) {
	this.ex = ex; this.stackTrace = ss.stackTrace();
    }
}
