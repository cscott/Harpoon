// MOVE.java, created Wed Jan 13 21:14:57 1999 by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.Tree;

import harpoon.ClassFile.HCodeElement;
import harpoon.Temp.CloningTempMap;
import harpoon.Util.Util;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;


/**
 * <code>MOVE</code> statements assign a value to a location.
 * <code>MOVE(TEMP <i>t</i>, <i>e</i>)</code> evaluates expression <i>e</i>
 * and assigns its value into temporary <i>t</i>.
 * <code>MOVE(MEM(<i>e1</i>, <i>k</i>), <i>e2</i>)</code> evaluates
 * expression <i>e1</i> yielding address <i>a</i>, then evaluates <i>e2</i>
 * and assigns its value into memory starting at address <i>a</i>.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>, based on
 *          <i>Modern Compiler Implementation in Java</i> by Andrew Appel.
 * @version $Id: MOVE.java,v 1.1.2.20 2000-01-29 01:27:28 pnkfelix Exp $
 */
public class MOVE extends Stm implements Typed {
    /** The expression giving the destination for the computed value. */
    private Exp dst;
    /** The expression for the computed value. */
    private Exp src;
    /** Constructor. 
     * <p>The type of the <code>src</code> expression and of the
     * <code>dst</code> expression must be identical.  (Use
     * a <code>UNOP</code> to convert, if they are necessary.)
     */
    public MOVE(TreeFactory tf, HCodeElement source,
		Exp dst, Exp src) {
	super(tf, source);
	this.dst = dst; this.src = src; 
	this.setDst(dst); this.setSrc(src); 
	Util.assert(dst != src, "dst and src cannot be same");
	Util.assert(dst!=null && src!=null, "Dest and Source cannot be null");
	Util.assert(dst.type()==src.type(), 
		    "Dest (type:"+Type.toString(dst.type()) + 
		    ") and Source (type:" + Type.toString(src.type()) + 
		    ") must have same type");
	Util.assert(dst.tf == src.tf, "Dest and Src must have same tree factory");
	Util.assert(tf == src.tf, "This and Src must have same tree factory");

	// FSK: debugging hack
	// this.accept(TreeVerifyingVisitor.norepeats());
    }
  
    public Tree getFirstChild() { return this.dst; } 
    public Exp getDst() { return this.dst; } 
    public Exp getSrc() { return this.src; } 

    public void setDst(Exp dst) { 
	this.dst = dst; 
	this.dst.parent = this;
	this.dst.sibling = src; 
    }

    public void setSrc(Exp src) { 
	this.src = src; 
	this.src.parent = this;
	this.src.sibling = null;
	this.dst.sibling = this.src;
    }

    protected Set defSet() { 
	Set def = new HashSet();
	if (dst.kind()==TreeKind.TEMP) def.add(((TEMP)dst).temp);
	return def;
    }
	
    protected Set useSet() { 
	Set uses = new HashSet();
	uses.addAll(src.useSet());
	if (!(dst.kind()==TreeKind.TEMP)) uses.addAll(dst.useSet());

	return uses;
    }

    public int kind() { return TreeKind.MOVE; }

    public Stm build(ExpList kids) { return build(tf, kids); } 
    public Stm build(TreeFactory tf, ExpList kids) {
	Util.assert(tf == kids.head.tf);
	Util.assert(tf == kids.tail.head.tf);
	return new MOVE(tf, this, kids.head, kids.tail.head);
    }
    /** Accept a visitor */
    public void accept(TreeVisitor v) { v.visit(this); } 

    public Tree rename(TreeFactory tf, CloningTempMap ctm) {
        return new MOVE(tf, this,
			(Exp)dst.rename(tf, ctm),
			(Exp)src.rename(tf, ctm));
    }

    /** @return the type of <code>dst</code> expression. */
    public int type() { return dst.type(); }
    public boolean isDoubleWord() { return dst.isDoubleWord(); }
    public boolean isFloatingPoint() { return dst.isFloatingPoint(); }

    public String toString() {
        return "MOVE(#"+dst.getID()+", #"+src.getID()+")";
    }
}

