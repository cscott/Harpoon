// ALIGN.java, created Tue Oct 19 13:52:16 1999 by cananian
// Copyright (C) 1999 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.Tree;

import harpoon.ClassFile.HCodeElement;
import harpoon.Temp.CloningTempMap;
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
 * @version $Id: ALIGN.java,v 1.1.4.4 2000-01-10 05:08:41 cananian Exp $
 */
public class ALIGN extends Stm implements harpoon.ClassFile.HDataElement {
    /** The alignment to enforce, in bytes. Zero or one specify no
     *  particular alignment. */
    public final int alignment;

    /** Creates a <code>ALIGN</code>. */
    public ALIGN(TreeFactory tf, HCodeElement source, int alignment) {
	super(tf, source);
	this.alignment = alignment;
	Util.assert(alignment >=0);
    }

    public Tree getFirstChild() { return null; } 

    protected Set defSet() { return Collections.EMPTY_SET; }
    protected Set useSet() { return Collections.EMPTY_SET; }

    public int     kind() { return TreeKind.ALIGN; }

    public Stm build(ExpList kids) { return build(tf, kids); }
    public Stm build(TreeFactory tf, ExpList kids) {
	return new ALIGN(tf, this, alignment);
    }

    /** Accept a visitor. */
    public void accept(TreeVisitor v) { v.visit(this); }

    public Tree rename(TreeFactory tf, CloningTempMap ctm) {
	return new ALIGN(tf, this, alignment);
    }

    public String toString() {
	return "ALIGN<"+alignment+">";
    }
}
