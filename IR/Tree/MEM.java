// MEM.java, created Wed Jan 13 21:14:57 1999 by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.Tree;

import harpoon.ClassFile.HCodeElement;
import harpoon.Temp.CloningTempMap;
import harpoon.Util.Util;

/**
 * <code>MEM</code> objects are expressions which stand for the contents of
 * a value in memory starting at the address specified by the
 * subexpression.  Note that when <code>MEM</code> is used as the left child
 * of a <code>MOVE</code> or <code>CALL</code>, it means "store," but
 * anywhere else it means "fetch."
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>, based on
 *          <i>Modern Compiler Implementation in Java</i> by Andrew Appel.
 * @version $Id: MEM.java,v 1.1.2.20 2000-01-09 00:21:56 duncan Exp $
 */
public class MEM extends Exp implements PreciselyTyped {
    /** A subexpression evaluating to a memory reference. */
    private Exp exp;
    /** The type of this memory reference expression. */
    public int type;

    // PreciselyTyped interface
    public final boolean isSmall;
    private int bitwidth   = -1;
    private boolean signed = true;

    /** Constructor. */
    public MEM(TreeFactory tf, HCodeElement source,
	       int type, Exp exp) {
	super(tf, source);
	this.type=type; this.setExp(exp); this.isSmall=false;
	Util.assert(Type.isValid(type) && exp!=null);
	Util.assert(tf == exp.tf, "This and Exp must have same tree factory");
    }

    /** Creates a MEM with a precisely defined type.  
     *  @param bitwidth    the width in bits of this <code>MEM</code>'s type.
     *                     Fails unless <code>0 <= bitwidth < 32</code>.
     *  @param signed      whether this <code>MEM</code> is signed
     *  @param exp         the location at which to load or store 
     */
    public MEM(TreeFactory tf, HCodeElement source, 
	       int bitwidth, boolean signed, Exp exp) { 
	super(tf, source);
	this.type=INT; this.setExp(exp); this.isSmall=true;
	this.bitwidth=bitwidth; this.signed=signed;
	Util.assert(exp!=null);
	Util.assert(tf == exp.tf, "This and Exp must have same tree factory");
	Util.assert((0<=bitwidth)&&(bitwidth<32), "Invalid bitwidth");
    }
    
    private MEM(TreeFactory tf, HCodeElement source, int type, Exp exp, 
		boolean isSmall, int bitwidth, boolean signed) { 
	super(tf, source);
	this.type=type; this.setExp(exp); this.isSmall=isSmall;
	this.bitwidth=bitwidth; this.signed=signed;
	Util.assert(exp!=null);
	Util.assert(tf == exp.tf, "This and Exp must have same tree factory");
	Util.assert(Type.isValid(type));
	Util.assert(!isSmall || type==INT);
    }

    public Tree getFirstChild() { return this.exp; } 
    public Exp getExp() { return this.exp; } 

    public void setExp(Exp exp) { 
	this.exp = exp;
	this.exp.parent = this; 
	this.exp.sibling = null;
    }

    public ExpList kids() {return new ExpList(exp,null);}

    public int kind() { return TreeKind.MEM; }

    public Exp build(ExpList kids) { return build(tf, kids); } 

    public Exp build(TreeFactory tf, ExpList kids) {
	Util.assert(tf == kids.head.tf);
	return new MEM(tf, this, type, kids.head, isSmall, bitwidth, signed);
    }

    // Typed interface:
    public int type() { Util.assert(Type.isValid(type)); return type; }
    
    // PreciselyTyped interface:
    /** Returns true if this is a sub-integer expression. */
    public boolean isSmall() { return isSmall; }
    /** Returns the size of the expression, in bits.
     *  Only valid if the <code>isSmall()==true</code>. */
    public int bitwidth() { Util.assert(type==INT&&isSmall); return bitwidth; }
    /** Returns true if this is a signed expression, false otherwise.
     *  Only valid if the <code>isSmall()==true</code>. */
    public boolean signed() { Util.assert(type==INT&&isSmall); return signed; }

    /** Accept a visitor */
    public void accept(TreeVisitor v) { v.visit(this); }

    public Tree rename(TreeFactory tf, CloningTempMap ctm) {
        return new MEM(tf, this, type, (Exp)exp.rename(tf, ctm), 
		       isSmall, bitwidth, signed);
    }

    public String toString() {
        return "MEM<" + Type.toString(this) + ">(#" + exp.getID() + ")";
    }
}

