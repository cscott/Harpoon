// Tree.java, created Fri Feb  5 05:53:33 1999 by cananian
// Copyright (C) 1999 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.Tree;

import harpoon.Util.ArrayFactory;
import harpoon.Util.Util;

/**
 * <code>Tree</code> is the base class for the tree representation.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: Tree.java,v 1.1.2.1 1999-02-05 11:48:54 cananian Exp $
 */
public abstract class Tree 
    implements harpoon.ClassFile.HCodeElement
{
    final TreeFactory tf;
    final String source_file;
    final int source_line;
    final int id;
    final private int hashCode;
    
    /** Creates a <code>Tree</code>. */
    protected Tree(TreeFactory tf, harpoon.ClassFile.HCodeElement source) {
        Util.assert(tf!=null);
	this.source_file = (source!=null)?source.getSourceFile():"unknown";
	this.source_line = (source!=null)?source.getLineNumber(): 0;
	this.id = tf.getUniqueID();
	this.tf = tf;
	// cache hashcode for efficiency.
	this.hashCode = this.id ^ tf.getParent().hashCode();
    }
    public int hashCode() { return hashCode; }

    /** Returns the <code>TreeFactory</code> that generated this
     *  <code>Tree</code>. */
    public TreeFactory getFactory() { return tf; }
    /** Returns the original source file name that this <code>Tree</code> is
     *  derived from. */
    public String getSourceFile() { return source_file; }
    /** Returns the line in the original source file that this
     *  <code>Tree</code> is derived from. */
    public int getLineNumber() { return source_line; }
    /** Returns a unique numeric identifier for this <code>Tree</code>. */
    public int getID() { return id; }

    /** Return a list of subexpressions of this <code>Tree</code>. */
    public abstract ExpList kids();
    /** Accept a visitor. */
    public abstract void visit(TreeVisitor v);

    /** Array factory: returns <code>Tree[]</code>. */
    public static final ArrayFactory arrayFactory =
	new ArrayFactory() {
	    public Object[] newArray(int len) { return new Tree[len]; }
	};
}
