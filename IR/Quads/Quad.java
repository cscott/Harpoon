// Quad.java, created by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.Quads;

import harpoon.ClassFile.*;
import harpoon.Temp.Temp;
import harpoon.Temp.TempMap;
import harpoon.Util.Util;
import harpoon.Util.ArrayFactory;

import java.util.Hashtable;
/**
 * <code>Quad</code> is the base class for the quadruple representation.<p>
 * No <code>Quad</code>s throw exceptions implicitly.
 *
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: Quad.java,v 1.1.2.6 1998-12-24 03:23:10 cananian Exp $
 */
public abstract class Quad 
    implements harpoon.ClassFile.HCodeElement, 
               harpoon.IR.Properties.UseDef, harpoon.IR.Properties.Edges,
               /*harpoon.IR.Properties.Renameable,*/ Cloneable
{
    // These should all be final.
    QuadFactory qf;
    String source_file;
    int    source_line;
    int    id;
    // cached.
    private int hashCode;

    /** Constructor. */
    protected Quad(QuadFactory qf, HCodeElement source,
		   int prev_arity, int next_arity) {
	Util.assert(qf!=null); // QuadFactory must be valid.
	this.source_file = (source!=null)?source.getSourceFile():"unknown";
	this.source_line = (source!=null)?source.getLineNumber(): 0;
	this.id = qf.getUniqueID();
	this.qf = qf;

	this.prev = new Edge[prev_arity];
	this.next = new Edge[next_arity];

	this.hashCode = (id<<5) ^ kind() ^
	    qf.getParent().getName().hashCode() ^
	    qf.getParent().getMethod().hashCode();
    }
    protected Quad(QuadFactory qf, HCodeElement source) {
	this(qf, source, 1, 1);
    }
    public int hashCode() { return hashCode; }

    /** Returns the <code>QuadFactory</code> that generated this
     *  <code>Quad</code>. */
    public QuadFactory getFactory() { return qf; }
    /** Returns the original source file name that this <code>Quad</code>
     *  is derived from. */
    public String getSourceFile() { return source_file; }
    /** Returns the line in the original source file that this 
     *  <code>Quad</code> is derived from. */
    public int getLineNumber() { return source_line; }
    /** Returns a unique numeric identifier for this <code>Quad</code>. */
    public int getID() { return id; }
    /** Force everyone to reimplement toString() */
    public abstract String toString();

    /** Accept a visitor. */
    public abstract void visit(QuadVisitor v);

    /** Return an integer enumeration of the kind of this 
     *  <code>Quad</code>.  The enumerated values are defined in
     *  <code>QuadKind</code>. */
    public abstract int kind();

    /** Create a new <code>Quad</code> identical to the receiver, but 
     *  with all <code>Temp</code>s renamed according to a mapping.
     *  The new <code>Quad</code> will have no edges. <p>
     *  The new <code>Quad</code> will come from the specified
     *  <code>QuadFactory</code>.
     */
    public abstract Quad rename(QuadFactory qf, TempMap defMap,TempMap useMap);
    /** Create a new <code>Quad</code> identical to the receiver, but 
     *  with all <code>Temp</code>s renamed according to a mapping.
     *  The new <code>Quad</code> will have no edges.<p>
     *  The new <code>Quad</code> will come from the same 
     *  <code>QuadFactory</code> as the receiver.
     */
    public final Quad rename(TempMap tm) { return rename(qf, tm, tm); }

    // I want to get rid of these functions eventually.
    /* @deprecated does not preserve immutability. */
    void renameUses(TempMap tm) { }
    /* @deprecated does not preserve immutability. */
    void renameDefs(TempMap tm) { }

    /*----------------------------------------------------------*/
    /** Return all the Temps used by this Quad. */
    public Temp[] use() { return new Temp[0]; }
    /** Return all the Temps defined by this Quad. */
    public Temp[] def() { return new Temp[0]; }

    /*----------------------------------------------------------*/
    /** Array factory: returns <code>Quad[]</code>s */
    public static final ArrayFactory arrayFactory =
	new ArrayFactory() {
	    public Object[] newArray(int len) { return new Quad[len]; }
	};

    /*----------------------------------------------------------*/
    // Graph structure.
    // Can modify links, but not *number of links*.
    Edge next[], prev[];

    /** Returns the <code>i</code>th successor of this quad. */
    public Quad next(int i) { return (Quad) next[i].to(); }
    /** Returns the <code>i</code>th predecessor of this quad. */
    public Quad prev(int i) { return (Quad) prev[i].from(); }

    /** Returns an array containing all the successors of this quad,
     *  in order. */
    public Quad[] next() { 
	Quad[] r = new Quad[next.length];
	for (int i=0; i<r.length; i++)
	    r[i] = (next[i]==null)?null:(Quad)next[i].to();
	return r;
    }
    /** Returns an array containing all the predecessors of this quad,
     *  in order. */
    public Quad[] prev() {
	Quad[] r = new Quad[prev.length];
	for (int i=0; i<r.length; i++)
	    r[i] = (prev[i]==null)?null:(Quad)prev[i].from();
	return r;
    }
    
    /** Returns an array containing all the outgoing edges from this quad. */
    public Edge[] nextEdge() 
    { return (Edge[]) Util.safeCopy(Edge.arrayFactory, next); }
    /** Returns an array containing all the incoming edges of this quad. */
    public Edge[] prevEdge() 
    { return (Edge[]) Util.safeCopy(Edge.arrayFactory, prev); }
    /** Returns the <code>i</code>th outgoing edge for this quad. */
    public Edge nextEdge(int i) { return next[i]; }
    /** Returns the <code>i</code>th incoming edge of this quad. */
    public Edge prevEdge(int i) { return prev[i]; }

    /** Returns an array with all the edges to and from this 
     *  <code>Quad</code>. */
    public HCodeEdge[] edges() {
	Edge[] e = new Edge[next.length+prev.length];
	System.arraycopy(next,0,e,0,next.length);
	System.arraycopy(prev,0,e,next.length,prev.length);
	return (HCodeEdge[]) e;
    }
    public HCodeEdge[] pred() { return prevEdge(); }
    public HCodeEdge[] succ() { return nextEdge(); }

    /** Adds an edge between two Quads.  The <code>from_index</code>ed
     *  outgoing edge of <code>from</code> is connected to the 
     *  <code>to_index</code>ed incoming edge of <code>to</code>. 
     *  @return the added <code>Edge</code>.*/
    public static Edge addEdge(Quad from, int from_index,
			       Quad to, int to_index) {
	// assert validity
	Util.assert(from.qf == to.qf); // quadfactories should always be same.
	//  [HEADERs connect only to FOOTER and METHOD]
	if (from instanceof HEADER)
	    Util.assert((to instanceof FOOTER && from_index==0) || 
			(to instanceof METHOD && from_index==1) );
	//  [METHOD connects to HANDLERs on all but first edge]
	if (from instanceof METHOD && from_index > 0)
	    Util.assert(to instanceof HANDLER);
	//  [ONLY HEADER, THROW and RETURN connects to FOOTER]
	if (to instanceof FOOTER)
	    Util.assert((from instanceoxff HEADER && to_index==0) ||
			(from instanceof THROW  && to_index >0) ||
			(from instanceof RETURN && to_index >0) );
	// OK, add the edge.
	Edge e = new Edge(from, from_index, to, to_index);
	from.next[from_index] = e;
	to.prev[to_index] = e;
	return e;
    }
    /** Add edges between a string of Quads.  The first outgoing edge
     *  is connected to the first incoming edge for all edges added.
     *  The same as multiple <code>addEdge(q[i], 0, q[i+1], 0)</code>
     *  calls. */
    public static void addEdges(Quad[] quadlist) {
	for (int i=0; i<quadlist.length-1; i++)
	    addEdge(quadlist[i], 0, quadlist[i+1], 0);
    }
    /** Replace one quad with another. The number of in and out edges of
     *  the new and old quads must match exactly. */
    public static void replace(Quad oldQ, Quad newQ) {
	Util.assert(oldQ.next.length == newQ.next.length);
	Util.assert(oldQ.prev.length == newQ.prev.length);
	for (int i=0; i<oldQ.next.length; i++) {
	    Edge e = oldQ.next[i];
	    addEdge(newQ, i, (Quad) e.to(), e.which_pred());
	    oldQ.next[i] = null;
	}
	for (int i=0; i<oldQ.prev.length; i++) {
	    Edge e = oldQ.prev[i];
	    addEdge((Quad) e.from(), e.which_succ(), newQ, i);
	    oldQ.prev[i] = null;
	}
    }

    //-----------------------------------------------------
    // support cloning.  The pred/succ quads are not cloned, but the
    // array holding them is.
    public Object clone() {
	try {
	    Quad q = (Quad) super.clone();
	    q.next = (Edge[]) next.clone();
	    q.prev = (Edge[]) prev.clone();
	    return q;
	} catch (CloneNotSupportedException e) {
	    // The compiler is too stupid to see that this can't ever happen.
	    Util.assert(false, "This should never ever happen.");
	    return null;
	}
    }

    //-----------------------------------------------------
    /** Create a new copy of a string of <code>Quad</code>s starting at
     *  the given header. */
    public static Quad clone(Quad header)
    {
	Util.assert(header instanceof HEADER, 
		    "Argument to Quad.clone() should be a HEADER.");
	return copyone(header, new Hashtable());
    }
    private static Quad copyone(Quad q, Hashtable old2new)
    {
	Quad r = (Quad) old2new.get(q);
	// if we've already done this one, return previous clone.
	if (r!=null) return r;
	// clone the fields, add to hashtable.
	r = (Quad) q.clone();
	old2new.put(q, r);
	// fixup the edges.
	for (int i=0; i<q.next.length; i++) {
	    Util.assert(q.next[i].from == q);
	    Quad to = copyone(q.next[i].to, old2new);
	    Quad.addEdge(r, q.next[i].from_index, to, q.next[i].to_index);
	}
	for (int i=0; i<q.prev.length; i++) {
	    Util.assert(q.prev[i].to == q);
	    Quad from = copyone(q.prev[i].from, old2new);
	    Quad.addEdge(from, q.prev[i].from_index, r, q.prev[i].to_index);
	}
	return r;
    }
    // ----------------------------------------------------
    // Useful for temp renaming.  Not exported.
    final static Temp map(TempMap tm, Temp t) {
	return (t==null)?null:(tm==null)?t:tm.tempMap(t);
    }
    final static Temp[] map(TempMap tm, Temp[] ta) {
	Temp[] r = new Temp[ta.length];
	for (int i=0; i<r.length; i++)
	    r[i] = map(tm, ta[i]);
	return r;
    }
    final static Temp[][] map(TempMap tm, Temp[][] taa) {
	Temp[][] r = new Temp[taa.length][];
	for (int i=0; i<r.length; i++)
	    r[i] = map(tm, taa[i]);
	return r;
    }
}
