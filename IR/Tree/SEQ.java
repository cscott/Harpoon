// SEQ.java, created Wed Jan 13 21:14:57 1999 by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.Tree;

import harpoon.ClassFile.HCodeElement;
import harpoon.Temp.TempMap;
import harpoon.Util.Util;

import java.util.Collections;
import java.util.Set;

/**
 * <code>SEQ</code> evaluates the left statement followed by the right
 * statement.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>, based on
 *          <i>Modern Compiler Implementation in Java</i> by Andrew Appel.
 * @version $Id: SEQ.java,v 1.6 2003-02-08 00:42:40 salcianu Exp $
 */
public class SEQ extends Stm implements harpoon.ClassFile.HDataElement {
    /** Constructor. */
    public SEQ(TreeFactory tf, HCodeElement source,
	       Stm left, Stm right) {
	super(tf, source, 2);
	assert left!=null && right!=null;
	assert left.tf == right.tf : "Left and Right must have same tree factory";
	assert tf == right.tf : "This and Right must have same tree factory";
	setLeft(left); setRight(right);

	// FSK: debugging hack
	// this.accept(TreeVerifyingVisitor.norepeats());

    }
    
    /** Convenient constructor: the tree factory and the source
        arguments are identical to those for the left and right
        statements.

	@param left Statement executed first
	@param right Statement executed second */
    public SEQ(Stm left, Stm right) {
	this(left.tf, left, left, right);
    }

    /** Returns the statement to evaluate first. */
    public Stm getLeft() { return (Stm) getChild(0); }
    /** Returns the statement to evaluate last. */
    public Stm getRight() { return (Stm) getChild(1); } 

    /** Sets the statement to evaluate first. */
    public void setLeft(Stm left) { setChild(0, left); }
    /** Sets the statement to evaluate last. */
    public void setRight(Stm right) { setChild(1, right); }

    public ExpList kids() {throw new Error("kids() not applicable to SEQ");}
    public int kind() { return TreeKind.SEQ; }

    public Stm build(TreeFactory tf, ExpList kids) {throw new Error("build() not applicable to SEQ");}
    /** Accept a visitor */
    public void accept(TreeVisitor v) { v.visit(this); }

    public Tree rename(TreeFactory tf, TempMap tm, CloneCallback cb) {
        return cb.callback(this, new SEQ(tf, this, 
					 (Stm)getLeft().rename(tf, tm, cb),
					 (Stm)getRight().rename(tf, tm, cb)),
			   tm);
    }

    public String toString() {
        return "SEQ(#"+getLeft().getID()+", #"+getRight().getID()+")";
    }
}

