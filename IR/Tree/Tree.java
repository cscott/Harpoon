// Tree.java, created Fri Feb  5 05:53:33 1999 by cananian
// Copyright (C) 1999 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.Tree;

import harpoon.ClassFile.HCodeEdge;
import harpoon.Temp.CloningTempMap;
import harpoon.Temp.Temp;
import harpoon.Temp.TempMap;
import harpoon.Util.ArrayFactory;
import harpoon.Util.HashSet;
import harpoon.Util.Set;
import harpoon.Util.Util;

/**
 * <code>Tree</code> is the base class for the tree representation.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: Tree.java,v 1.1.2.5 1999-04-23 08:34:58 duncan Exp $
 */
public abstract class Tree 
    implements harpoon.ClassFile.HCodeElement, 
	       harpoon.IR.Properties.UseDef,
	       harpoon.IR.Properties.Edges
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

    public Temp[] def() { 
	throw new Error("not implemented");
    }

    public Temp[] use() { 
	Set useSet = useSet();
	Temp[] use = new Temp[useSet.size()];
	useSet.copyInto(use);
	
	return use;
    }

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
    /** Accept a visitor. */
    public abstract void visit(TreeVisitor v);

    /** Array factory: returns <code>Tree[]</code>. */
    public static final ArrayFactory arrayFactory =
	new ArrayFactory() {
	    public Object[] newArray(int len) { return new Tree[len]; }
	};
  
    
    public Object clone(TreeFactory tf) { 
        return rename(tf, new CloningTempMap(this.tf.tempFactory(), 
					     tf.tempFactory()));
    }

    public Object clone(TreeFactory tf, CloningTempMap ctm) { 
        return rename(tf, ctm);
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
    public Tree next(int i) { return (Tree) next[i].to(); }
    /** Returns the <code>i</code>th predecessor of this tree. */
    public Tree prev(int i) { return (Tree) prev[i].from(); }
    /** Return the number of successors of this tree. */
    public int nextLength() { return next.length; }
    /** Return the number of predecessors of this tree. */
    public int prevLength() { return prev.length; }

    /** Returns an array containing all the successors of this tree,
     *  in order. */
    public Tree[] next() { 
	Tree[] r = new Tree[next.length];
	for (int i=0; i<r.length; i++)
	    r[i] = (next[i]==null)?null:(Tree)next[i].to();
	return r;
    }
    /** Returns an array containing all the predecessors of this tree,
     *  in order. */
    public Tree[] prev() {
	Tree[] r = new Tree[prev.length];
	for (int i=0; i<r.length; i++)
	    r[i] = (prev[i]==null)?null:(Tree)prev[i].from();
	return r;
    }
    
    /** Returns an array containing all the outgoing edges from this tree. */
    public Edge[] nextEdge() 
    { return (Edge[]) Util.safeCopy(Edge.arrayFactory, next); }
    /** Returns an array containing all the incoming edges of this tree. */
    public Edge[] prevEdge() 
    { return (Edge[]) Util.safeCopy(Edge.arrayFactory, prev); }
    /** Returns the <code>i</code>th outgoing edge for this tree. */
    public Edge nextEdge(int i) { return next[i]; }
    /** Returns the <code>i</code>th incoming edge of this tree. */
    public Edge prevEdge(int i) { return prev[i]; }

    /** Returns an array with all the edges to and from this 
     *  <code>Tree</code>. */
    public HCodeEdge[] edges() {
	Edge[] e = new Edge[next.length+prev.length];
	System.arraycopy(next,0,e,0,next.length);
	System.arraycopy(prev,0,e,next.length,prev.length);
	return (HCodeEdge[]) e;
    }

    public HCodeEdge[] pred() { return prevEdge(); }
    public HCodeEdge[] succ() { return nextEdge(); }

    /** Adds an edge between two Trees.  The <code>from_index</code>ed
     *  outgoing edge of <code>from</code> is connected to the 
     *  <code>to_index</code>ed incoming edge of <code>to</code>. 
     *  @return the added <code>Edge</code>.*/
    public static Edge addEdge(Tree from, int from_index,
			       Tree to, int to_index) {
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
    public static void replace(Tree oldQ, Tree newQ) {
	Util.assert(oldQ.next.length == newQ.next.length);
	Util.assert(oldQ.prev.length == newQ.prev.length);
	for (int i=0; i<oldQ.next.length; i++) {
	    Edge e = oldQ.next[i];
	    addEdge(newQ, i, (Tree) e.to(), e.which_pred());
	    oldQ.next[i] = null;
	}
	for (int i=0; i<oldQ.prev.length; i++) {
	    Edge e = oldQ.prev[i];
	    addEdge((Tree) e.from(), e.which_succ(), newQ, i);
	    oldQ.prev[i] = null;
	}
    }
}



