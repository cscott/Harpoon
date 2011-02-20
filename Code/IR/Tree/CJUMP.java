// CJUMP.java, created Wed Jan 13 21:14:57 1999 by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.Tree;

import harpoon.ClassFile.HCodeElement;
import harpoon.Temp.TempMap;
import harpoon.Temp.Label;
import harpoon.Util.Util;

/**
 * <code>CJUMP</code> objects are statements which stand for conditional
 * branches.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>, based on
 *          <i>Modern Compiler Implementation in Java</i> by Andrew Appel.
 * @version $Id: CJUMP.java,v 1.4 2002-04-10 03:05:39 cananian Exp $
 */
public class CJUMP extends Stm {
    /** The label to jump to if <code>test</code> is <code>true</code>. */
    public final Label iftrue;
    /** The label to jump to if <code>test</code> is <code>false</code>. */
    public final Label iffalse;
    
    /** Constructor. */
    public CJUMP(TreeFactory tf, HCodeElement source,
		 Exp test, Label iftrue, Label iffalse) {
	super(tf, source, 1);
	assert test!=null && iftrue!=null && iffalse!=null;
	this.setTest(test); this.iftrue = iftrue; this.iffalse = iffalse;
	assert tf == test.tf : "This and Test must have same tree factory";
    }

    /** Returns the test condition for this <code>CJUMP</code>.
     *  The expression should evaluate into a boolean result. */
    public Exp getTest() { return (Exp) getChild(0); } 

    /** Returns the test condition for this <code>CJUMP</code>.
     *  The given expression should evaluate into a boolean result. */
    public void setTest(Exp test) { setChild(0, test); }
    
    public int kind() { return TreeKind.CJUMP; }

    public Stm build(TreeFactory tf, ExpList kids) {
	assert kids!=null && kids.tail==null;
	assert tf == kids.head.tf;
	return new CJUMP(tf, this, kids.head, iftrue, iffalse);
    }
    /** Accept a visitor */
    public void accept(TreeVisitor v) { v.visit(this); }

    public Tree rename(TreeFactory tf, TempMap tm, CloneCallback cb) {
        return cb.callback(this, new CJUMP(tf, this,
					   (Exp)getTest().rename(tf, tm, cb),
					   iftrue, iffalse),
			   tm);
    }

    public String toString() {
        return "CJUMP(#"+getTest().getID()+", "+iftrue+", "+iffalse+")";
    }
}

