// CONST.java, created Mon Aug 24 16:46:52 1998 by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.Quads;

import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HCodeElement;
import harpoon.Temp.Temp;
import harpoon.Temp.TempMap;
import harpoon.Util.Util;
/**
 * <code>CONST</code> objects represent an assignment of a constant value
 * to a compiler temporary. <p>
 * The <code>type</code> field of a <code>CONST</code> must be one of:
 * <code>Class</code>, <code>Field</code>, <code>Method</code>,
 * <code>String</code>, <code>int</code>, <code>long</code>,
 * <code>float</code>, <code>double</code> or <code>void</code>.
 * A <code>void</code> type corresponds to a <code>null</code> literal
 * constant, and in this case the <code>value</code> field will be
 * <code>null</code>.  For class, field, and method constants, the
 * <code>value</code> field will contain an instance of <code>HClass</code>,
 * <code>HField</code>, or <code>HMethod</code>, respectively.
 * In all other cases, <code>value</code> will contain
 * an object of the specified type; values of primitive types will use
 * the standard wrapper class.  An example may clarify:
 * <UL>
 *  <LI>The integer constant 0 is represented by <BR>
 *     <CODE>new CONST(qf, hce, dst, new Integer(0), HClass.Int);</code>
 *  <LI>The string constant "hello, world" is represented by <BR>
 *     <CODE>new CONST(qf, hce, dst, "hello, world",
 *           linker.forName("java.lang.String") );</code>
 *  <LI>The class constant that would be returned by
 *       <code>Class.forName("java.lang.Object")</code> is represented by <BR>
 *     <CODE>new CONST(qf, hce, dst, linker.forName("java.lang.Object"),
 *           linker.forName("java.lang.Class") );</code>
 *  <LI>The null literal is represented by <BR>
 *     <CODE>new CONST(qf, hce, dst, null, HClass.Void);</code>
 * </UL>
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: CONST.java,v 1.2 2002-02-25 21:05:12 cananian Exp $
 */

public class CONST extends Quad {
    /** The destination <code>Temp</code>. */
    protected Temp dst;
    /** An object representing the constant value. */
    final protected Object value;
    /** The type of the constant value. */
    final protected HClass type;

    /** Creates a <code>CONST</code> from a destination temporary, and object
     *  value and its class type.
     * @param dst
     *        the <code>Temp</code> which will contain the specified 
     *        constant value.
     * @param value
     *        an object representing the constant value.
     * @param type
     *        the type of the constant value.
     */
    public CONST(QuadFactory qf, HCodeElement source,
		 Temp dst, Object value, HClass type) {
	super(qf, source);
	this.dst = dst;
	this.value = value;
	this.type = type;
	// VERIFY legality of this CONST.
	Util.assert(dst!=null && type!=null);
	Util.assert(type.equals(HClass.Int)   || type.equals(HClass.Long)   ||
		    type.equals(HClass.Float) || type.equals(HClass.Double) ||
		    type.equals(HClass.Void)  || 
		    type.getName().equals("java.lang.Class") ||
		    type.getName().equals("java.lang.reflect.Field") ||
		    type.getName().equals("java.lang.reflect.Method") ||
		    type.getName().equals("java.lang.String"));
	if (type.equals(HClass.Void))
	    Util.assert(value==null);
	else
	    Util.assert(value!=null);
    }
    // ACCESSOR METHODS:
    /** Returns the <code>Temp</code> which will contain the specified
     *  constant value. */
    public Temp dst() { return dst; }
    /** Returns the object representing the constant value. */
    public Object value() { return value; }
    /** Returns the type of the constant value. */
    public HClass type() { return type; }

    /** Returns the Temp defined by this Quad.
     * @return the <code>dst</code> field. */
    public Temp[] def() { return new Temp[] { dst }; }

    public int kind() { return QuadKind.CONST; }

    public Quad rename(QuadFactory qqf, TempMap defMap, TempMap useMap) {
	return new CONST(qqf, this, map(defMap,dst), value, type);
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

    /** Returns a human-readable representation of this Quad. */
    public String toString() {
	StringBuffer sb = new StringBuffer(dst.toString());
	sb.append(" = CONST ");
	if (type.getName().equals("java.lang.String"))
	    sb.append("(String)\""+Util.escape(value.toString())+"\"");
	else if (type.equals(HClass.Void) && value == null)
	    sb.append("null");
	else
	    sb.append("("+type.getName()+")"+value.toString());
	return sb.toString();
    }
}
