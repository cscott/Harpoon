// JUMP.java, created Wed Jan 13 21:14:57 1999 by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.Tree;

import harpoon.ClassFile.HCodeElement;
import harpoon.Temp.TempMap;
import harpoon.Temp.Label;
import harpoon.Temp.LabelList;
import harpoon.Util.Util;

/**
 * <code>JUMP</code> objects are statements which stand for unconditional
 * computed branches.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>, based on
 *          <i>Modern Compiler Implementation in Java</i> by Andrew Appel.
 * @version $Id: JUMP.java,v 1.2 2002-02-25 21:05:31 cananian Exp $
 */
public class JUMP extends Stm {
    /** A list of possible branch targets. */
    public final LabelList targets;
    /** Full constructor. */
    public JUMP(TreeFactory tf, HCodeElement source,
		Exp exp, LabelList targets) {
	super(tf, source, 1);
	Util.assert(exp!=null && targets!=null);
	this.setExp(exp); this.targets=targets;
	Util.assert(tf == exp.tf, "This and Exp must have same tree factory");

    }
    /** Abbreviated constructor for a non-computed branch. */
    public JUMP(TreeFactory tf, HCodeElement source,
		Label target) {
	this(tf, source,
	     new NAME(tf, source, target), new LabelList(target,null));
    }

    /** Returns an expression giving the address to jump to. */
    public Exp getExp() { return (Exp) getChild(0); }
    /** Set the expression giving the address to jump to. */
    public void setExp(Exp exp) { setChild(0, exp); }
    
    public int kind() { return TreeKind.JUMP; }

    public Stm build(TreeFactory tf, ExpList kids) {
	Util.assert(kids!=null && kids.tail==null);
	Util.assert(tf == kids.head.tf);
	return new JUMP(tf, this, kids.head,targets);
    }
    /** Accept a visitor */
    public void accept(TreeVisitor v) { v.visit(this); }

    public Tree rename(TreeFactory tf, TempMap tm, CloneCallback cb) {
        return cb.callback(this, new JUMP(tf, this, (Exp)getExp().rename(tf, tm, cb), this.targets), tm);
    }  

    public String toString() {
        LabelList list = targets;
        StringBuffer s = new StringBuffer();
        
        s.append("JUMP(#" + getExp().getID() + ", {");
        while (list != null) {
            s.append(" "+list.head);
            if (list.tail != null) 
                s.append(",");
            list = list.tail;
        }
        s.append(" })");
        return new String(s);
    }
}

