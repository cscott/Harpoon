// NAME.java, created Wed Jan 13 21:14:57 1999 by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.Tree;

import harpoon.ClassFile.HCodeElement;
import harpoon.Temp.TempMap;
import harpoon.Temp.Label;
import harpoon.Util.Util;

/**
 * <code>NAME</code> objects are expressions which stand for symbolic
 * constants.  They usually correspond to some assembly language label
 * in the code or data segment.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>, based on
 *          <i>Modern Compiler Implementation in Java</i> by Andrew Appel.
 * @version $Id: NAME.java,v 1.4 2002-04-10 03:05:45 cananian Exp $
 */
public class NAME extends Exp implements harpoon.ClassFile.HDataElement {
    /** The label which this NAME refers to. */
    public final Label label;
    /** Constructor. */
    public NAME(TreeFactory tf, HCodeElement source,
		Label label) {
	super(tf, source, 0);
	this.label=label;
	assert label!=null;
    }
    
    public int kind() { return TreeKind.NAME; }
	
    public Exp build(TreeFactory tf, ExpList kids) { 
	assert kids==null;
	return new NAME(tf, this, label); 
    }

    /** Accept a visitor */
    public void accept(TreeVisitor v) { v.visit(this); }

    public Tree rename(TreeFactory tf, TempMap tm, CloneCallback cb) {
        return cb.callback(this, new NAME(tf, this, this.label), tm);
    }

    /** @return <code>Type.POINTER</code> */
    public int type() { return POINTER; }
    
    public String toString() {
        return "NAME("+label+")";
    }
}

