// QuadStackFrame.java, created Mon Dec 28 17:16:11 1998 by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Interpret.Quads;

import harpoon.ClassFile.HMethod;
import harpoon.IR.Quads.Quad;
import harpoon.Temp.Temp;
import harpoon.Util.Util;

import java.util.Hashtable;
/**
 * <code>QuadStackFrame</code> is a stack frame for an interpreted method.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: QuadStackFrame.java,v 1.2 2002-02-25 21:05:46 cananian Exp $
 */
final class QuadStackFrame extends StackFrame {
    /** current location in the method. */
    Quad pc;
    /** current mapping from <code>Temp</code>s to values. */
    final Hashtable state = new Hashtable();
    /** an object to stand for <code>null</code>. */
    static final private Object Onull = new Object();

    QuadStackFrame(Quad initial_pc) { this.pc = initial_pc; }
    void update(Temp t, Object value) {
	state.put(t, (value==null)?Onull:value);
    }
    Object get(Temp t) {
	Object o = state.get(t);
	if (o==null)
	    throw new Error("Use before def of "+t+" at " + getMethod() +
			    "("+ getSourceFile() +":"+ getLineNumber() +")"+
			    "::" + pc);
	return (o==Onull)?null:o;
    }
    void undefine(Temp t) { state.remove(t); }

    final HMethod getMethod() { return pc.getFactory().getMethod(); }
    final String  getSourceFile() { return pc.getSourceFile(); }
    final int     getLineNumber() { return pc.getLineNumber(); }
}
