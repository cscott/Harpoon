// ALIGN.java, created Tue Oct 19 13:52:16 1999 by cananian
// Copyright (C) 1999 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.Tree;

import harpoon.ClassFile.HCodeElement;
import harpoon.Temp.TempMap;
import harpoon.Util.Util;

import java.util.Collections;
import java.util.Set;
/**
 * <code>ALIGN</code> statements are used to enforce a given alignment on
 * the following data items.  Its effect on code is undefined.  The next
 * <code>DATUM</code> element (and any <code>LABEL</code> between the
 * <code>ALIGN</code> and the <code>DATUM</code>) will be aligned on the
 * specified n-byte boundary.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: ALIGN.java,v 1.3.2.1 2002-02-27 08:36:41 cananian Exp $
 */
public class ALIGN extends Stm implements harpoon.ClassFile.HDataElement {
    /** The alignment to enforce, in bytes. Zero or one specify no
     *  particular alignment. */
    public final int alignment;

    /** Creates a <code>ALIGN</code>. */
    public ALIGN(TreeFactory tf, HCodeElement source, int alignment) {
	super(tf, source, 0);
	this.alignment = alignment;
	assert alignment >=0;
	
	// FSK: debugging hack
	// this.accept(TreeVerifyingVisitor.norepeats());
    }

    public int     kind() { return TreeKind.ALIGN; }

    public Stm build(TreeFactory tf, ExpList kids) {
	assert kids==null;
	return new ALIGN(tf, this, alignment);
    }

    /** Accept a visitor. */
    public void accept(TreeVisitor v) { v.visit(this); }

    public Tree rename(TreeFactory tf, TempMap tm, CloneCallback cb) {
	return cb.callback(this, new ALIGN(tf, this, alignment), tm);
    }

    public String toString() {
	return "ALIGN<"+alignment+">";
    }
}
