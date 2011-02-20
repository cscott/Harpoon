// NEW.java, created Wed Aug  5 07:08:20 1998 by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.Quads;

import java.lang.reflect.Modifier;

import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HCodeElement;
import harpoon.Temp.Temp;
import harpoon.Temp.TempMap;
import harpoon.Util.Util;

/**
 * <code>NEW</code> represents an object creation operation.
 * It creates an <b>uninitialized</b> instance of an object.  One of the
 * object's constructors must then be called (using a non-virtual
 * <code>Quads.CALL</code>) to complete the instantiation.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: NEW.java,v 1.5 2002-04-11 04:00:34 cananian Exp $
 */
public class NEW extends Quad {
    /** The <code>Temp</code> in which to store the new object. */
    protected Temp dst;
    /** Description of the class to create. */
    final protected HClass hclass;

    /** Creates a <code>NEW</code> object.  <code>NEW</code> creates
     *  a new instance of the class <code>hclass</code>.
     * @param dst
     *        the <code>Temp</code> in which to store the new object.
     * @param hclass
     *        the class to create.
     */
    public NEW(QuadFactory qf, HCodeElement source,
	       Temp dst, HClass hclass) {
        super(qf, source);
	this.dst = dst;
	this.hclass = hclass;
	// VERIFY legality of NEW
	assert dst!=null && hclass!=null;
	// from JVM spec:
	assert !hclass.isArray() && !hclass.isInterface();
	assert !hclass.isPrimitive();
	assert !Modifier.isAbstract(hclass.getModifiers());
    }
    /** Returns the <code>Temp</code> in which to store the new object. */
    public Temp dst() { return dst; }
    /** Returns the class this <code>NEW</code> will create. */
    public HClass hclass() { return hclass; }

    /** Returns the <code>Temp</code> defined by this <code>Quad</code>.
     * @return the <code>dst</code> field. */
    public Temp[] def() { return new Temp[] { dst }; }

    public int kind() { return QuadKind.NEW; }

    public Quad rename(QuadFactory qqf, TempMap defMap, TempMap useMap) {
	return new NEW(qqf, this, map(defMap,dst), hclass);
    }
    /** Rename all used variables in this Quad according to a mapping.
     * @deprecated does not preserve immutability. */
    void renameUses(TempMap tm) {
    }
    /** Rename all defined variables in this Quad according to a mapping.
     * @deprecated does not preserve immutability. */
    void renameDefs(TempMap tm) {
	dst = tm.tempMap(dst);
    }

    public void accept(QuadVisitor v) { v.visit(this); }
    public <T> T accept(QuadValueVisitor<T> v) { return v.visit(this); }

    /** Returns a human-readable representation of this quad. */
    public String toString() {
	return dst.toString() + " = NEW " + hclass.getName();
    }
}
