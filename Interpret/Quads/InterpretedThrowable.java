// InterpretedThrowable.java, created Mon Dec 28 01:27:53 1998 by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Interpret.Quads;

import harpoon.ClassFile.HClass;
import harpoon.IR.Quads.Quad;
import harpoon.Util.Util;
/**
 * <code>InterpretedThrowable</code> is a wrapper for an exception within
 * the interpreter.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: InterpretedThrowable.java,v 1.3 2002-02-26 22:46:24 cananian Exp $
 */
final class InterpretedThrowable extends RuntimeException {
    final ObjectRef ex;
    final String[] stackTrace;
    /** Creates a <code>InterpretedThrowable</code>. */
    InterpretedThrowable(ObjectRef ex, String[] st) {
        this.ex = ex; this.stackTrace = st;
	Util.ASSERT(ex.type.isInstanceOf(ex.ss.HCthrowable));
    }
    InterpretedThrowable(ObjectRef ex, StaticState ss) {
	this.ex = ex; this.stackTrace = ss.stackTrace();
	Util.ASSERT(ex.type.isInstanceOf(ss.HCthrowable));
    }
}
