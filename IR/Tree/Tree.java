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
import java.util.Set;

/**
 * <code>Tree</code> is the base class for the tree representation.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: Tree.java,v 1.1.2.16 1999-10-19 19:53:10 cananian Exp $
 */
public abstract class Tree 
    implements HCodeElement, 
	       harpoon.IR.Properties.UseDef,
	       harpoon.IR.Properties.HasEdges
{
    /*final*/ TreeFactory tf; // JDK 1.1 has problems with final fields.
    /*final*/ String source_file;
    /*final*/ int source_line;
    /*final*/ int id;
    /*final*/ private int hashCode;

    protected Tree(TreeFactory tf, HCodeElement source, int next_arity) { 
        Util.assert(tf!=null);
	this.source_file = (source!=null)?source.getSourceFile():"unknown";
	this.source_line = (source!=null)?source.getLineNumber(): 0;
	this.id = tf.getUniqueID();
	this.tf = tf;
	// cache hashcode for efficiency.
	this.hashCode = this.id ^ tf.hashCode();
	
	// Only next_arity can be determined from the type of the Tree object.
	// We must use the EdgeInitializer class to compute prev_arity
	this.next = new Edge[next_arity];
    }
	
    /** Creates a <code>Tree</code>. */
    protected Tree(TreeFactory tf, HCodeElement source) {
	this(tf, source, 1);
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
    static Tree clone(TreeFactory tf, CloningTempMap ctm, Tree root) { 
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

    /*----------------------------------------------------------*/
    // Graph structure.

    // Can modify links, but not *number of links*.
    // Unlike the quad form, the next and prev arrays are not calculated 
    // immediately.  Rather, they are calculated by codeview
    Edge next[], prev[];

    /** Returns the <code>i</code>th successor of this tree. */
    public Tree next(int i) { 
	Util.assert(tf instanceof Code.TreeFactory);
	Util.assert(((Code.TreeFactory)tf).getParent().isCanonical());
	return (Tree) next[i].to(); 
    }

    /** Returns the <code>i</code>th predecessor of this tree. */
    public Tree prev(int i) { 
	Util.assert(tf instanceof Code.TreeFactory);
	Util.assert(((Code.TreeFactory)tf).getParent().isCanonical());
	return (Tree) prev[i].from(); 
    }

    /** Return the number of successors of this tree. */
    public int nextLength() { 
	Util.assert(tf instanceof Code.TreeFactory);
	Util.assert(((Code.TreeFactory)tf).getParent().isCanonical());
	return next.length; 
    }

    /** Return the number of predecessors of this tree. */
    public int prevLength() { 
	Util.assert(tf instanceof Code.TreeFactory);
	Util.assert(((Code.TreeFactory)tf).getParent().isCanonical());
	return prev.length; 
    }

    /** Returns an array containing all the successors of this tree,
     *  in order. */
    public Tree[] next() { 
	Util.assert(tf instanceof Code.TreeFactory);
	Util.assert(((Code.TreeFactory)tf).getParent().isCanonical());
	Tree[] r = new Tree[next.length];
	for (int i=0; i<r.length; i++)
	    r[i] = (next[i]==null)?null:(Tree)next[i].to();
	return r;
    }
    /** Returns an array containing all the predecessors of this tree,
     *  in order. */
    public Tree[] prev() {
	Util.assert(tf instanceof Code.TreeFactory);
	Util.assert(((Code.TreeFactory)tf).getParent().isCanonical());
	Tree[] r = new Tree[prev.length];
	for (int i=0; i<r.length; i++)
	    r[i] = (prev[i]==null)?null:(Tree)prev[i].from();
	return r;
    }
    
    /** Returns an array containing all the outgoing edges from this tree. */
    public Edge[] nextEdge() { 
	Util.assert(tf instanceof Code.TreeFactory);
	Util.assert(((Code.TreeFactory)tf).getParent().isCanonical());
	return (Edge[]) Util.safeCopy(Edge.arrayFactory, next); 
    }

    /** Returns an array containing all the incoming edges of this tree. */
    public Edge[] prevEdge() {
	Util.assert(tf instanceof Code.TreeFactory);
	Util.assert(((Code.TreeFactory)tf).getParent().isCanonical());
	return (Edge[]) Util.safeCopy(Edge.arrayFactory, prev); 
    }
    
    /** Returns the <code>i</code>th outgoing edge for this tree. */
    public Edge nextEdge(int i) { 
	Util.assert(tf instanceof Code.TreeFactory);
	Util.assert(((Code.TreeFactory)tf).getParent().isCanonical());
	return next[i]; 
    }

    /** Returns the <code>i</code>th incoming edge of this tree. */
    public Edge prevEdge(int i) { 
	Util.assert(tf instanceof Code.TreeFactory);
	Util.assert(((Code.TreeFactory)tf).getParent().isCanonical());
	return prev[i]; 
    }

    /** Returns an array with all the edges to and from this 
     *  <code>Tree</code>. */
    public HCodeEdge[] edges() {
	Util.assert(tf instanceof Code.TreeFactory);
	Util.assert(((Code.TreeFactory)tf).getParent().isCanonical());
	Edge[] e = new Edge[next.length+prev.length];
	System.arraycopy(next,0,e,0,next.length);
	System.arraycopy(prev,0,e,next.length,prev.length);
	return (HCodeEdge[]) e;
    }

    public HCodeEdge[] pred() { 
	Util.assert(tf instanceof Code.TreeFactory);
	Util.assert(((Code.TreeFactory)tf).getParent().isCanonical());
	return prevEdge(); 
    }
    
    public HCodeEdge[] succ() { 
	Util.assert(tf instanceof Code.TreeFactory);
	Util.assert(((Code.TreeFactory)tf).getParent().isCanonical());
	return nextEdge(); 
    }

    public Collection edgeC() {
	return new AbstractCollection() {
	    public int size() { return next.length + prev.length; }
	    public Iterator iterator() {
		return new CombineIterator(new Iterator[] {
		    new ArrayIterator(next), new ArrayIterator(prev) });
	    }
	};
    }
    public Collection predC() {
	return Collections.unmodifiableList(Arrays.asList(prev));
    }
    public Collection succC() {
	return Collections.unmodifiableList(Arrays.asList(next));
    }

    /** Adds an edge between two Trees.  The <code>from_index</code>ed
     *  outgoing edge of <code>from</code> is connected to the 
     *  <code>to_index</code>ed incoming edge of <code>to</code>. 
     *  @return the added <code>Edge</code>.*/
    public static Edge addEdge(Tree from, int from_index,
			       Tree to, int to_index) {
	Util.assert(from.tf instanceof Code.TreeFactory);
	Util.assert(((Code.TreeFactory)from.tf).getParent().isCanonical());
	
	// assert validity
	Util.assert(from.tf == to.tf, "TreeFactories should always be same");
	Util.assert(from instanceof Stm, "Exps can't have edges");
	Util.assert(to instanceof Stm, "Exps can't have edges");

	// OK, add the edge.
	Edge e = new Edge(from, from_index, to, to_index);
	from.next[from_index] = e;
	to.prev[to_index] = e;
	return e;
    }
    /** Add edges between a string of Trees.  The first outgoing edge
     *  is connected to the first incoming edge for all edges added.
     *  The same as multiple <code>addEdge(q[i], 0, q[i+1], 0)</code>
     *  calls. */
    public static void addEdges(Tree[] treelist) {
	for (int i=0; i<treelist.length-1; i++)	    
	    addEdge(treelist[i], 0, treelist[i+1], 0);
    }

    /** Replace one tree with another. The number of in and out edges of
     *  the new and old trees must match exactly. */
    public static void replace(Tree oldT, Tree newT) {
	Util.assert(oldT.tf instanceof Code.TreeFactory);
	Util.assert(((Code.TreeFactory)oldT.tf).getParent().isCanonical());
	Util.assert(oldT.tf==newT.tf, "TreeFactories should always be same");
	Util.assert(oldT.next.length == newT.next.length);
	Util.assert(oldT.prev.length == newT.prev.length);

	for (int i=0; i<oldT.next.length; i++) {
	    Edge e = oldT.next[i];
	    addEdge(newT, i, (Tree) e.to(), e.which_pred());
	    oldT.next[i] = null;
	}
	for (int i=0; i<oldT.prev.length; i++) {
	    Edge e = oldT.prev[i];
	    addEdge((Tree) e.from(), e.which_succ(), newT, i);
	    oldT.prev[i] = null;
	}
    }
}












