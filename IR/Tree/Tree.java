// Tree.java, created Fri Feb  5  6:48:54 1999 by cananian
// Tree.java, created Fri Feb  5 05:53:33 1999 by cananian
// Copyright (C) 1999 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.Tree;

import harpoon.Backend.Generic.RegFileInfo;
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
 * @version $Id: Tree.java,v 1.4 2002-04-10 03:05:46 cananian Exp $
 */
public abstract class Tree 
    implements HCodeElement
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
        assert tf!=null;
	this.source_file = (source!=null)?source.getSourceFile():"unknown";
	this.source_line = (source!=null)?source.getLineNumber(): 0;
	this.id = tf.getUniqueID();
	this.tf = tf;
	this.child = new Tree[arity];
	// cache hashcode for efficiency.
	this.hashCode = this.id ^ tf.hashCode();
    }

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
	assert parent!=null; // don't call getSibling() on the root!
	assert this==parent.child[which_child_of_parent];
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
	assert newChild!=null : "you can't set a tree child to null";
	assert newChild.tf==this.tf : "tree factories must match";
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
    public static final ArrayFactory<Tree> arrayFactory =
	new ArrayFactory<Tree>() {
	    public Tree[] newArray(int len) { return new Tree[len]; }
	};
  
    
    /** 
     * Returns a clone of <code>root</code>.  The <code>callback()</code>
     * method of the supplied <code>CloneCallback</code> will be invoked
     * with every cloned subtree, from the bottom up to the root.  The
     * cloned subtree will be generated using the supplied
     * <code>TreeFactory</code>, <code>ntf</code>.
     * @return the root of the cloned tree.
     * <p>
     * NOTE:  tree objects may actually contain temps from two different
     *        temp factories.  The first temp factory with which a tree's 
     *        temps may be associated is the <code>TempFactory</code>
     *        stored in their <code>TreeFactory</code>.  The second
     *        is the <code>TempFactory</code> used by the tree's 
     *        <code>Frame</code> to generate registers.  Since these 
     *        registers are assumed to be immutable, no temps from that
     *        temp factory will be cloned by this method.  All other temps
     *        will be cloned using a new <code>CloningTempMap</code>.
     */
    public static Tree clone(TreeFactory ntf, Tree root, CloneCallback cb) {
	if (root==null) return null;
	if (cb==null) cb = nullCallback;
	final RegFileInfo rfi = root.tf.getFrame().getRegFileInfo();
	CloningTempMap ctm = (ntf==root.tf) ? null :
	    new CloningTempMap(root.tf.tempFactory(), ntf.tempFactory()) {
		public Temp tempMap(Temp t) { // don't clone registers!
		    return rfi.isRegister(t) ? t : super.tempMap(t);
		}
	    };
	return root.rename(ntf, ctm, cb);
    }
    /** Clone a subtree.  This is a *deep* copy -- ie, this node and
     *  nodes rooted here are copied, all the way down to the leaves.
     *  The cloned subtree will have the same tree factory as
     *  <code>this</code>. */
    public final Tree clone() { return rename(null); }
    /** Rename while cloning a subtree.  This node and all child nodes
     *  are cloned; the 'temp' information of all <code>TEMP</code> nodes
     *  are renamed according to the supplied <code>TempMap</code>.
     *  Note that <code>Temp</code>s not belonging to
     *  <code>this.getFactory().tempFactory()</code> are not affected. */
    public final Tree rename(TempMap tm) {
        return rename(this.tf, tm, nullCallback);
    }
    /** Rename while cloning a subtree.  This node and all child nodes
     *  are cloned; the 'temp' information of all <code>TEMP</code> nodes
     *  are renamed according to the supplied <code>TempMap</code>.
     *  Note that <code>Temp</code>s not belonging to
     *  <code>this.getFactory().tempFactory()</code> are not affected.
     *  The <code>callback()</code> method of the supplied
     *  <code>CloneCallback</code> is invoked once on each subtree cloned,
     *  starting from the leaves and working back to the root in a
     *  post-order depth-first manner. */
    // --- this is what each tree subclass must implement ---
    public abstract Tree rename(TreeFactory tf, TempMap tm, CloneCallback cb);

    /** Callback interface to tree cloning code to allow you to update
     *  type and other annotations as the tree is cloned. */
    public interface CloneCallback {
	/** This method will be called once for every cloned/renamed subtree.
	 *  The original tree will be passed in as <code>oldTree</code> and
	 *  the new, cloned/renamed tree will be passed in as
	 *  <code>newTree</code>.  The return value of this function will
	 *  be linked in to the cloned parent, so substitutions on the
	 *  cloned-tree-in-progress can be made by returning something
	 *  other than <code>newTree</code>.  In normal use, however,
	 *  <code>newTree</code> should always be returned.
	 *  <p>The supplied <code>TempMap</code> specifies the
	 *  <code>Temp</code> mapping in effect for the cloning
	 *  operation.  This allows you to update tree information
	 *  which is tied to <code>Temp</code>s. */
	public Tree callback(Tree oldTree, Tree newTree, TempMap tm);
    }
    /** A do-nothing callback.  Comes in handy sometimes. */
    private static final CloneCallback nullCallback = new CloneCallback() {
	public Tree callback(Tree o, Tree n, TempMap tm) { return n; }
    };

    /** Return a list of subexpressions of this <code>Tree</code>. */
    public ExpList kids() { // by default, make kids list from all children.
	ExpList r=null;
	for (int i=child.length-1; i>=0; i--)
	    r = new ExpList((Exp)child[i], r);
	return r;
    }
}













