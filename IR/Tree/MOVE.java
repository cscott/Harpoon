// MOVE.java, created Wed Jan 13 21:14:57 1999 by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.Tree;

import harpoon.ClassFile.HCodeElement;
import harpoon.Temp.TempMap;
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
 * @version $Id: MOVE.java,v 1.2 2002-02-25 21:05:33 cananian Exp $
 */
public class MOVE extends Stm implements Typed {
    /** Constructor. 
     * <p>The type of the <code>src</code> expression and of the
     * <code>dst</code> expression must be identical.  (Use
     * a <code>UNOP</code> to convert, if they are necessary.)
     */
    public MOVE(TreeFactory tf, HCodeElement source,
		Exp dst, Exp src) {
	super(tf, source, 2);
	Util.assert(dst != src, "dst and src cannot be same");
	Util.assert(dst!=null && src!=null, "Dest and Source cannot be null");
	Util.assert(dst.type()==src.type(), 
		    "Dest (type:"+Type.toString(dst.type()) + 
		    ") and Source (type:" + Type.toString(src.type()) + 
		    ") must have same type");
	Util.assert(dst.tf == src.tf, "Dest and Src must have same tree factory");
	Util.assert(tf == src.tf, "This and Src must have same tree factory");
	this.setDst(dst); this.setSrc(src); 

	// FSK: debugging hack
	// this.accept(TreeVerifyingVisitor.norepeats());
    }
  
    /** Returns the expression giving the destination for the
     *  computed value. */
    public Exp getDst() { return (Exp) getChild(0); }
    /** Returns the expression for the computed value. */
    public Exp getSrc() { return (Exp) getChild(1); } 

    /** Sets the expression giving the destination for the
     *  computed value. */
    public void setDst(Exp dst) {
	Util.assert(dst.kind()==TreeKind.MEM || dst.kind()==TreeKind.TEMP);
	setChild(0, dst);
    }
    /** Sets the expression for the computed value. */
    public void setSrc(Exp src) { setChild(1, src); }

    public int kind() { return TreeKind.MOVE; }

    public ExpList kids() {
	ExpList commontail = new ExpList(getSrc(), null);
	if (getDst().kind()==TreeKind.MEM)
	    return new ExpList(getDst().kids().head, commontail);
	else return commontail;
    }
    public Stm build(TreeFactory tf, ExpList kids) {
	Util.assert(kids!=null);
	Util.assert(tf == kids.head.tf);
	Util.assert(tf == this.tf, "cloning src not yet implemented");
	if (getDst().kind()==TreeKind.MEM)
	    return new MOVE(tf, this, 
			    getDst().build(tf, new ExpList(kids.head, null)),
			    kids.tail.head);
	else return new MOVE(tf, this, getDst(), kids.head);
    }
    /** Accept a visitor */
    public void accept(TreeVisitor v) { v.visit(this); } 

    public Tree rename(TreeFactory tf, TempMap tm, CloneCallback cb) {
        return cb.callback(this, new MOVE(tf, this,
					  (Exp)getDst().rename(tf, tm, cb),
					  (Exp)getSrc().rename(tf, tm, cb)),
			   tm);
    }

    /** @return the type of <code>dst</code> expression. */
    public int type() { return getDst().type(); }
    public boolean isDoubleWord() { return getDst().isDoubleWord(); }
    public boolean isFloatingPoint() { return getDst().isFloatingPoint(); }

    public String toString() {
        return "MOVE(#"+getDst().getID()+", #"+getSrc().getID()+")";
    }
}

