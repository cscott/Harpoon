// SEQ.java, created Wed Jan 13 21:14:57 1999 by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.Tree;

import harpoon.ClassFile.HCodeElement;
import harpoon.Temp.CloningTempMap;
import harpoon.Util.Util;

import java.util.Collections;
import java.util.Set;

/**
 * <code>SEQ</code> evaluates the left statement followed by the right
 * statement.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>, based on
 *          <i>Modern Compiler Implementation in Java</i> by Andrew Appel.
 * @version $Id: SEQ.java,v 1.1.2.13 1999-10-19 19:53:10 cananian Exp $
 */
public class SEQ extends Stm implements harpoon.ClassFile.HDataElement {
    /** The statement to evaluate first. */
    public Stm left;
    /** The statement to evaluate last. */
    public Stm right;
    /** Constructor. */
    public SEQ(TreeFactory tf, HCodeElement source,
	       Stm left, Stm right) {
	super(tf, source, 0); // No edges in or out of SEQ
	this.left=left; this.right=right;
	Util.assert(left!=null && right!=null);
	Util.assert(left.tf == right.tf, "Left and Right must have same tree factory");
	Util.assert(tf == right.tf, "This and Right must have same tree factory");
    }

    protected Set defSet() { return Collections.EMPTY_SET; }

    protected Set useSet() { return Collections.EMPTY_SET; }
    
    public ExpList kids() {throw new Error("kids() not applicable to SEQ");}
    public int kind() { return TreeKind.SEQ; }

    public Stm build(ExpList kids) {throw new Error("build() not applicable to SEQ");}
    public Stm build(TreeFactory tf, ExpList kids) {throw new Error("build() not applicable to SEQ");}
    /** Accept a visitor */
    public void accept(TreeVisitor v) { v.visit(this); }

    public Tree rename(TreeFactory tf, CloningTempMap ctm) {
        return new SEQ(tf, this, 
		       (Stm)left.rename(tf, ctm),
		       (Stm)right.rename(tf, ctm));
    }

    public String toString() {
        return "SEQ(#"+left.getID()+", #"+right.getID()+")";
    }
}

