// CONST.java, created Mon Aug 24 16:46:52 1998 by cananian
package harpoon.IR.QuadSSA;

import harpoon.ClassFile.*;
import harpoon.Temp.Temp;
import harpoon.Temp.TempMap;
import harpoon.Util.Util;
/**
 * <code>CONST</code> objects represent an assignment of a constant value
 * to a compiler temporary.<p>
 * The <code>type</code> field of a <code>CONST</code> must be one of:
 * <code>String</code>, <code>int</code>, <code>long</code>,
 * <code>float</code>, <code>double</code> or <code>void</code.
 * A <code>void</code> type corresponds to a <code>null</code> literal
 * constant, and in this case the <code>value</code> field will be
 * <code>null</code>.  In all other cases, <code>value</code> will contain
 * an object of the specified type; values of primitive types will use
 * the standard wrapper class.  An example may clarify:
 * <UL>
 *  <LI>The integer constant 0 is represented by <BR>
 *     <CODE>new CONST(source, dst, new Integer(0), HClass.Int);</code>
 *  <LI>The string constant "hello, world" is represented by <BR>
 *     <CODE>new CONST(source, dst, "hello, world",
 *           HClass.forName("java.lang.String") );</code>
 *  <LI>The null literal is represented by <BR>
 *     <CODE>new CONST(source, dst, null, HClass.Void);</code>
 * </UL>
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: CONST.java,v 1.11 1998-10-02 04:44:25 cananian Exp $
 */

public class CONST extends Quad {
    public Temp dst;
    public Object value;
    public HClass type;
    /** Creates a <code>CONST</code> from a destination temporary, and object
     *  value and its class type. */
    public CONST(HCodeElement source,
		 Temp dst, Object value, HClass type) {
	super(source);
	this.dst = dst;
	this.value = value;
	this.type = type;
	Util.assert(type==HClass.Int   || type==HClass.Long   ||
		    type==HClass.Float || type==HClass.Double ||
		    type==HClass.Void  || 
		    type==HClass.forName("java.lang.String"));
    }

    /** Returns the Temp defined by this Quad.
     * @return The <code>dst</code> field.
     */
    public Temp[] def() { return new Temp[] { dst }; }

    /** Rename all used variables in this Quad according to a mapping. */
    public void renameUses(TempMap tm) {
    }
    /** Rename all defined variables in this Quad according to a mapping. */
    public void renameDefs(TempMap tm) {
	dst = tm.tempMap(dst);
    }

    public void visit(QuadVisitor v) { v.visit(this); }

    /** Returns a human-readable representation of this Quad. */
    public String toString() {
	StringBuffer sb = new StringBuffer(dst.toString());
	sb.append(" = CONST ");
	if (type == HClass.forName("java.lang.String"))
	    sb.append("(String)\""+Util.escape(value.toString())+"\"");
	else if (type == HClass.Void && value==null)
	    sb.append("null");
	else
	    sb.append("("+type.getName()+")"+value.toString());
	return sb.toString();
    }
}
