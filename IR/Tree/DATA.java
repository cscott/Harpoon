package harpoon.IR.Tree;

import harpoon.ClassFile.HCodeElement;
import harpoon.Temp.CloningTempMap;
import harpoon.Util.Util;

/**
 * <code>DATA</code> objects are statements which write a value to memory
 * at the time when a program is loaded.  The location written is 
 * calculated using this formula:
 *
 * <PRE>
 *           location = base + offset
 * 
 *       where base   = location of nearest LABEL, l,  which precedes this DATA
 *             offset = the total size of all instructions between l and this
 *                      DATA
 *
 * </PRE>
 *
 * @author  Duncan Bryce <duncan@lcs.mit.edu>
 * @version $Id: DATA.java,v 1.1.2.2 1999-07-30 20:20:15 pnkfelix Exp $
 */
public class DATA extends Stm { 
    /** The expression to write to memory */
    public final Exp data;
    
    /** Class constructor. 
     *  <br><code>data</code> must be a constant.  Allowed <code>Tree</code>
     *  trees are <code>harpoon.IR.Tree.CONST</code> and 
     *  <code>harpoon.IR.Tree.NAME</code>.
     */
    public DATA(TreeFactory tf, HCodeElement source, Exp data) {
	super(tf, source);
	this.data = data; 
	Util.assert(data.kind()==TreeKind.CONST || data.kind()==TreeKind.NAME);
	Util.assert(tf == data.tf, "Dest and Src must have same tree factory");
    }

    public ExpList kids() { return new ExpList(data, null); } 
    
    public int kind() { return TreeKind.DATA; } 

    public Stm build(ExpList kids) { 
	return new DATA(tf, this, kids.head);
    }

    // Typed interface:
    public int type() { return data.type(); } 

    /** Accept a visitor */
    public void visit(TreeVisitor v) { v.visit(this); } 

    public Tree rename(TreeFactory tf, CloningTempMap ctm) { 
	return new DATA(tf, this, (Exp)data.rename(tf, ctm));
    }    

    public String toString() { 
	StringBuffer sb = new StringBuffer("DATA<");
	sb.append(Type.toString(type()));
	sb.append(">(#");
	sb.append(data.getID());
	sb.append(")");
	return sb.toString();
    }
}
