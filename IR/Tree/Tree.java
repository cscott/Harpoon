// Tree.java, created Fri Feb  5  6:48:54 1999 by cananian
// Tree.java, created Fri Feb  5 05:53:33 1999 by cananian
// Copyright (C) 1999 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.Tree;

import harpoon.ClassFile.HCodeEdge;
import harpoon.ClassFile.HCodeElement;
import harpoon.Temp.CloningTempMap;
import harpoon.Temp.Temp;
import harpoon.Temp.TempMap;
import harpoon.Util.ArrayFactory;
import harpoon.Util.ArrayIterator;
import harpoon.Util.CombineIterator;
import harpoon.Util.Util;

import java.util.AbstractCollection;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * <code>Tree</code> is the base class for the tree representation.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: Tree.java,v 1.1.2.26 2000-02-14 21:49:34 cananian Exp $
 */
public abstract class Tree 
    implements HCodeElement, 
	       harpoon.IR.Properties.UseDef
{
    final TreeFactory tf;
    final String source_file;
    final int source_line;
    final int id;
    final private int hashCode;

    private Tree parent = null; 
    private int which_child_of_parent;
    protected final Tree[] child;

    protected Tree(TreeFactory tf, HCodeElement source, int arity) { 
        Util.assert(tf!=null);
	this.source_file = (source!=null)?source.getSourceFile():"unknown";
	this.source_line = (source!=null)?source.getLineNumber(): 0;
	this.id = tf.getUniqueID();
	this.tf = tf;
	this.child = new Tree[arity];
	// cache hashcode for efficiency.
	this.hashCode = this.id ^ tf.hashCode();
    }

    /** Returns the Temps defined by this tree.  Can only be used in
     *  codeviews which have been canonicalized.
     */
    public Temp[] def() { 
	Util.assert(tf instanceof Code.TreeFactory);
	Util.assert(((Code.TreeFactory)tf).getParent().isCanonical());

	Set defSet = defSet();
	return (Temp[])defSet.toArray(new Temp[defSet.size()]); 
    }

    /** Returns the Temps used by this tree.  Can only be used in
     *  codeviews which have been canonicalized.
     */
    public Temp[] use() { 
	Util.assert(tf instanceof Code.TreeFactory);
	Util.assert(((Code.TreeFactory)tf).getParent().isCanonical());

	Set useSet = useSet();
	return (Temp[])useSet.toArray(new Temp[useSet.size()]); 
    }
    public Collection useC() { return Collections.unmodifiableSet(useSet()); }
    public Collection defC() { return Collections.unmodifiableSet(defSet()); }

    abstract protected Set defSet();
    abstract protected Set useSet();   

    public final int hashCode() { return hashCode; }

    /** Returns the <code>TreeFactory</code> that generated this
     *  <code>Tree</code>. */
    public final TreeFactory getFactory() { return tf; }

    //
    // FIXME:  add more documentation 
    // to the sibling/parent methods 
    //

    /**
     * Returns the leftmost child of this tree, or null if this
     * node has no children.
     */ 
    public final Tree getFirstChild() {
	return child.length > 0 ? child[0] : null;
    }

    /**
     * Returns the right sibling of this tree, null if there are no
     * siblings to the right of this node. 
     */ 
    public final Tree getSibling() {
	Util.assert(this==parent.child[which_child_of_parent]);
	int c = which_child_of_parent + 1;
	return parent.child.length > c ? parent.child[c] : null;
    } 

    /**
     * Returns the parent of this tree.  If this tree is the
     * root node, then returns <code>null</code>. 
     */ 
    public final Tree getParent() { return this.parent; } 

    /** Fetch from the child array -- for subclass use only.  The subclass
     *  will provide named accessors to the public (ie, getDst(), getLeft()).
     */
    protected final Tree getChild(int which) { return child[which]; }
    /**
     * Modify the child array -- for subclass use only.  The subclass will
     * provide named accessors to the public (ie, setDst(), setLeft() ).
     */
    protected final void setChild(int which, Tree newChild) {
	if (child[which]!=null) child[which].unlink();
	child[which] = newChild;
	newChild.parent = this;
	newChild.which_child_of_parent = which;
    }
    /** Replace the tree rooted at <code>this</code> with a new tree. */
    public final void replace(Tree newTree) {
	parent.setChild(which_child_of_parent, newTree);
    }
    /** Make <code>this</code> a root-level tree, unlinking it from
     *  its parent. */
    final void unlink() { this.parent=null; this.which_child_of_parent=0; }

    /** Returns the original source file name that this <code>Tree</code> is
     *  derived from. */
    public final String getSourceFile() { return source_file; }
    /** Returns the line in the original source file that this
     *  <code>Tree</code> is derived from. */
    public final int getLineNumber() { return source_line; }
    /** Returns a unique numeric identifier for this <code>Tree</code>. */
    public final int getID() { return id; }

    /** Return an integer enumeration of the kind of this 
     *  <code>Tree</code>.  The enumerated values are defined in
     *  <code>TreeKind</code>. */
    public abstract int kind();

    /** Accept a visitor. */
    public abstract void accept(TreeVisitor v);

    /** Array factory: returns <code>Tree[]</code>. */
    public static final ArrayFactory arrayFactory =
	new ArrayFactory() {
	    public Object[] newArray(int len) { return new Tree[len]; }
	};
  
    
    /** 
     * Returns a clone of <code>root</code>.  
     * NOTE:  tree objects may actually contain temps from two different
     *        temp factories.  The first temp factory with which a tree's 
     *        temps may be associated is the <code>TempFactory</code>
     *        stored in their <code>TreeFactory</code>.  The second
     *        is the <code>TempFactory</code> used by the tree's 
     *        <code>Frame</code> to generate registers.  Since these 
     *        registers are assumed to be immutable, no temps from that
     *        temp factory will be cloned by this method.  All other temps
     *        will be cloned using <code>ctm</code>.  
     */
    public static Tree clone(TreeFactory tf, CloningTempMap ctm, Tree root) { 
	if (root==null) return null;
	else return root.rename(tf, ctm);
    }

    public abstract Tree rename(TreeFactory tf, CloningTempMap ctm);
    public Tree rename(CloningTempMap ctm) {
        return rename(this.tf, ctm);
    }

    protected final static Temp map(TempMap tm, Temp t) {
	return (t==null)?null:(tm==null)?t:tm.tempMap(t);
    }

    /** Return a list of subexpressions of this <code>Tree</code>. */
    public ExpList kids() { // by default, make kids list from all children.
	ExpList r=null;
	for (int i=child.length-1; i>=0; i--)
	    r = new ExpList((Exp)child[i], r);
	return r;
    }
}













