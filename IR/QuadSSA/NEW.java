// NEW.java, created Wed Aug  5 07:08:20 1998 by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.QuadSSA;

import java.lang.reflect.Modifier;

import harpoon.ClassFile.*;
import harpoon.Temp.Temp;
import harpoon.Temp.TempMap;
import harpoon.Util.Util;
/**
 * <code>NEW</code> represents an object creation operation.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: NEW.java,v 1.14 1998-10-11 02:37:57 cananian Exp $
 */

public class NEW extends Quad {
    /** The Temp in which to store the new object. */
    public Temp dst;
    /** Description of the class to create. */
    public HClass hclass;
    /** Creates a <code>NEW</code> object.  <code>NEW</code> creates
     *  a new instance of the class <code>hclass</code>. */
    public NEW(HCodeElement source,
	       Temp dst, HClass hclass) {
        super(source);
	this.dst = dst;
	this.hclass = hclass;
	// from JVM spec:
	Util.assert(!hclass.isArray() && !hclass.isInterface());
	Util.assert(!hclass.isPrimitive());
	Util.assert(!Modifier.isAbstract(hclass.getModifiers()));
    }

    /** Returns the Temp defined by this Quad.
     * @return the <code>dst</code> field. */
    public Temp[] def() { return new Temp[] { dst }; }

    /** Rename all used variables in this Quad according to a mapping. */
    public void renameUses(TempMap tm) {
    }
    /** Rename all defined variables in this Quad according to a mapping. */
    public void renameDefs(TempMap tm) {
	dst = tm.tempMap(dst);
    }

    public void visit(QuadVisitor v) { v.visit(this); }

    /** Returns a human-readable representation of this quad. */
    public String toString() {
	return dst.toString() + " = NEW " + hclass.getName();
    }
}
