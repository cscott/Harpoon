// DATUM.java, created Fri Jul 23 13:39:22 1999 by duncan
// Copyright (C) 1998 Duncan Bryce <duncan@lcs.mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.Tree;

import harpoon.ClassFile.HCodeElement;
import harpoon.Temp.TempMap;
import harpoon.Util.Util;

/**
 * <code>DATUM</code> objects are statements which write a value to memory
 * at the time when a program is loaded.  The location written is 
 * calculated using this formula:
 *
 * <PRE>
 *         location = base + offset
 * 
 * where 
 *         base   = location of nearest LABEL, l,  which precedes this DATUM
 *         offset = the total size of all instructions between l and this DATUM
 *                      
 * </PRE>
 *
 * @author  Duncan Bryce <duncan@lcs.mit.edu>
 * @version $Id: DATUM.java,v 1.3.2.1 2002-02-27 08:36:47 cananian Exp $
 */
public class DATUM extends Stm implements harpoon.ClassFile.HDataElement { 
    /** If false, the memory is not initialized; instead it is reserved
     *  with an unspecified value. */
    public final boolean initialized;
    
    /** Class constructor. 
     *  The parameter <br><code>data</code> must be an instance of either
     *  <code>harpoon.IR.Tree.CONST</code> or 
     *  <code>harpoon.IR.Tree.NAME</code>.  Passing <code>null</code> for
     *  the parameter <code>data</code> reserves a word of memory at the
     *  location of this <code>DATUM</code> without assigning it a value.
     */
    public DATUM(TreeFactory tf, HCodeElement source, Exp data) {
	super(tf, source, 1);
	assert data!=null;
	this.setData(data);
	this.initialized = true;
	assert data.kind()==TreeKind.CONST || 
		    data.kind()==TreeKind.NAME;
	assert tf==data.tf : "Dest and Src must have same tree factory";
    }

    /** Class constructor. 
     *  Reserves memory at the location of this <code>DATUM</code>
     *  of the size of the specified type without assigning it a value. 
     */
    public DATUM(TreeFactory tf, HCodeElement source, int type) { 
	super(tf, source, 1);
	assert Type.isValid(type);
	if (type==Type.INT)
	    this.setData(new CONST(tf, source, (int)0));
	else if (type==Type.LONG)
	    this.setData(new CONST(tf, source, (long)0));
	else if (type==Type.FLOAT)
	    this.setData(new CONST(tf, source, (float)0));
	else if (type==Type.DOUBLE)
	    this.setData(new CONST(tf, source, (double)0));
	else if (type==Type.POINTER)
	    this.setData(new CONST(tf, source)); // null
	else throw new Error("Impossible!");
	this.initialized = false;
    }

    /** Class constructor. 
     *  Reserves memory at the location of this <code>DATUM</code>
     *  of the specified small without assigning it a value. 
     */
    public DATUM(TreeFactory tf, HCodeElement source,
		int bitwidth, boolean signed) { 
	super(tf, source, 1);
	this.setData(new CONST(tf, source, bitwidth, signed, 0));
	this.initialized = false;
    }

    private DATUM(TreeFactory tf, HCodeElement source,
		 Exp data, boolean initialized) {
	super(tf, source, 1);
	this.setData(data);
	this.initialized = initialized;
	assert data.kind()==TreeKind.CONST || 
		    data.kind()==TreeKind.NAME;
	assert tf==data.tf : "Dest and Src must have same tree factory";
    }

    /** Returns the expression to write to memory.  Never null. */
    public Exp getData() { return (Exp) getChild(0); }
    
    /** Sets the expression to write to memory.  Never null. */
    public void setData(Exp data) { setChild(0, data); }

    public int kind() { return TreeKind.DATUM; } 

    public Stm build(TreeFactory tf, ExpList kids) { 
	assert kids!=null && kids.tail==null;
	assert kids.head == null || tf == kids.head.tf;
	return new DATUM(tf, this, kids.head, initialized);
    }

    /** Accept a visitor */
    public void accept(TreeVisitor v) { v.visit(this); } 

    public Tree rename(TreeFactory tf, TempMap tm, CloneCallback cb) { 
	return cb.callback(this, new DATUM(tf, this, (Exp)getData().rename(tf, tm, cb),initialized), tm);
    }    

    public String toString() { 
	StringBuffer sb = new StringBuffer("DATUM<");
	sb.append(getData() instanceof PreciselyTyped ?
		  Type.toString((PreciselyTyped)getData()) :
		  Type.toString(getData().type()));
	sb.append(">(#"); sb.append(getID()); sb.append(")");
	return sb.toString();
    }
}
