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
 * @version $Id: MEM.java,v 1.1.2.15 1999-08-11 20:40:09 duncan Exp $
 */
public class MEM extends Exp implements PreciselyTyped {
    /** A subexpression evaluating to a memory reference. */
    public final Exp exp;
    /** The type of this memory reference expression. */
    public final int type;

    // Force access through the PreciselyTyped interface, so we can assert
    // that type==SMALL.  
    private int bitwidth   = -1;
    private boolean signed = false;

    /** Constructor. */
    public MEM(TreeFactory tf, HCodeElement source,
	       int type, Exp exp) {
	super(tf, source);
	this.type=type; this.exp=exp;
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
	this.type=SMALL; this.exp=exp; 
	this.bitwidth=bitwidth; this.signed=signed;
	Util.assert(exp!=null);
	Util.assert(tf == exp.tf, "This and Exp must have same tree factory");
	Util.assert((0<=bitwidth)&&(bitwidth<32), "Invalid bitwidth");
    }
    
    private MEM(TreeFactory tf, HCodeElement source, int type, Exp exp, 
		int bitwidth, boolean signed) { 
	super(tf, source);
	this.type=type; this.exp=exp; 
	this.bitwidth=bitwidth; this.signed=signed;
	Util.assert(exp!=null);
	Util.assert(tf == exp.tf, "This and Exp must have same tree factory");
    }

    public ExpList kids() {return new ExpList(exp,null);}

    public int kind() { return TreeKind.MEM; }

    public Exp build(ExpList kids) { return build(tf, kids); } 

    public Exp build(TreeFactory tf, ExpList kids) {
	Util.assert(tf == kids.head.tf);
	return new MEM(tf, this, type, kids.head, bitwidth, signed);
    }

    // Typed interface:
    public int type() { return type; }
    
    // PreciselyTyped interface:
    public int bitwidth() { Util.assert(type==SMALL); return bitwidth; } 
    public boolean signed() { Util.assert(type==SMALL); return signed; } 

    /** Accept a visitor */
    public void visit(TreeVisitor v) { v.visit(this); }

    public Tree rename(TreeFactory tf, CloningTempMap ctm) {
        return new MEM(tf, this, type, (Exp)exp.rename(tf, ctm), 
		       bitwidth, signed);
    }

    public String toString() {
        return "MEM<" + Type.toString(type) + 
	    ((type==SMALL) ? (":" +  bitwidth + (signed ? "sext" : "")) : "") +
	    ">(#" + exp.getID() + ")";
    }
}

