// LABEL.java, created Wed Jan 13 21:14:57 1999 by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.Tree;

import harpoon.ClassFile.HCodeElement;
import harpoon.Temp.CloningTempMap;
import harpoon.Temp.Label;
import harpoon.Util.Util;

/**
 * <code>LABEL</code> objects define the constant value of the given
 * label to be the current machine code address.  This is like a label
 * definition in assembly language.  The value <code>NAME(Label l)</code>
 * may be the target of jumps, calls, etc.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>, based on
 *          <i>Modern Compiler Implementation in Java</i> by Andrew Appel.
 * @version $Id: LABEL.java,v 1.1.2.8 1999-08-04 05:52:30 cananian Exp $
 */
public class LABEL extends Stm { 
    /** The symbolic name to define. */
    public final Label label;
    /** Constructor. */
    public LABEL(TreeFactory tf, HCodeElement source,
		 Label label) {
	super(tf, source);
	this.label=label;
	Util.assert(label!=null);
    }
    public ExpList kids() {return null;}

    public int kind() { return TreeKind.LABEL; }

    public Stm build(ExpList kids) { return build(tf, kids); } 

    public Stm build(TreeFactory tf, ExpList kids) {
	return new LABEL(tf, this, label);
    }
    /** Accept a visitor */
    public void visit(TreeVisitor v) { v.visit(this); }

    public Tree rename(TreeFactory tf, CloningTempMap ctm) {
        return new LABEL(tf, this, this.label);
    }

    public String toString() {
        return "LABEL("+label+")";
    }
}

