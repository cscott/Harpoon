// LABEL.java, created Wed Jan 13 21:14:57 1999 by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.Tree;

import harpoon.ClassFile.HCodeElement;
import harpoon.Temp.TempMap;
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
 * @version $Id: LABEL.java,v 1.4 2002-04-10 03:05:45 cananian Exp $
 */
public class LABEL extends Stm implements harpoon.ClassFile.HDataElement { 
    /** The symbolic name to define. */
    public final Label label;
    /** Flag indicating whether the label should be exported.
     *  Only exported labels are visible from other classes and the
     *  runtime.  Unexported labels *may* be visible from other methods
     *  in the same class, but are not required to be. */
    public final boolean exported;
    /** Constructor. */
    public LABEL(TreeFactory tf, HCodeElement source,
		 Label label, boolean exported) {
	super(tf, source, 0);
	this.label=label;
	this.exported=exported;
	assert label!=null;
    }

    public int kind() { return TreeKind.LABEL; }

    public Stm build(TreeFactory tf, ExpList kids) {
	assert kids==null;
	return new LABEL(tf, this, label, exported);
    }
    /** Accept a visitor */
    public void accept(TreeVisitor v) { v.visit(this); }

    public Tree rename(TreeFactory tf, TempMap tm, CloneCallback cb) {
        return cb.callback(this, new LABEL(tf, this, this.label, exported), tm);
    }

    public String toString() {
        return "LABEL("+label+")";
    }
}

