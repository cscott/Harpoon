// Quad.java, created Tue Dec  1  7:36:43 1998 by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.Quads;

import harpoon.ClassFile.HCodeAndMaps;
import harpoon.ClassFile.HCodeElement;
import harpoon.IR.Properties.CFGEdge; 
import harpoon.Temp.CloningTempMap;
import harpoon.Temp.Temp;
import harpoon.Temp.TempMap;
import harpoon.Util.ArrayFactory;
import harpoon.Util.ArrayIterator;
import harpoon.Util.CombineIterator;
import harpoon.Util.EnumerationIterator;
import harpoon.Util.Util;

import java.util.AbstractCollection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
/**
 * <code>Quad</code> is the base class for the quadruple representation.<p>
 *
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: Quad.java,v 1.3 2002-02-26 22:45:57 cananian Exp $
 */
public abstract class Quad 
    implements harpoon.ClassFile.HCodeElement, 
               harpoon.IR.Properties.UseDefable,
               harpoon.IR.Properties.CFGraphable,
               Cloneable, Comparable, java.io.Serializable
{
    final QuadFactory qf;
    final String source_file;
    final int    source_line;
    final int    id;
    // cached.
    final private int hashCode;

    /** Initializes a quad with <code>prev_arity</code> input edges and
     *  <code>next_arity</code> output edges. */
    protected Quad(QuadFactory qf, HCodeElement source,
		   int prev_arity, int next_arity) {
	Util.ASSERT(qf!=null); // QuadFactory must be valid.
	this.source_file = (source!=null)?source.getSourceFile():"unknown";
	this.source_line = (source!=null)?source.getLineNumber(): 0;
	this.id = qf.getUniqueID();
	this.qf = qf;

	this.prev = new Edge[prev_arity];
	this.next = new Edge[next_arity];

	this.hashCode = (id<<5) ^ kind() ^
	    qf.getParent().getName().hashCode() ^ qf.getMethod().hashCode();
    }
    /** Initializes a quad with exactly one input edge and exactly one
     *  output edge. */
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
    public abstract void accept(QuadVisitor v);

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
     *  The new <code>Quad</code> will have no edges. <p>
     *  The new <code>Quad</code> will come from the same
     *  <code>QuadFactory</code> as the receiver.
     */
    public final Quad rename(TempMap defMap,TempMap useMap) {
	return rename(this.qf, defMap, useMap);
    }

    /*----------------------------------------------------------*/
    /** Return all the Temps used by this Quad. */
    public Temp[] use() { return new Temp[0]; }
    /** Return all the Temps defined by this Quad. */
    public Temp[] def() { return new Temp[0]; }
    public Collection useC() { return Arrays.asList(use()); }
    public Collection defC() { return Arrays.asList(def()); }
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
    /** Return the number of successors of this quad. */
    public int nextLength() { return next.length; }
    /** Return the number of predecessors of this quad. */
    public int prevLength() { return prev.length; }

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

    // from CFGraphable:
    public CFGEdge[] edges() {
	Edge[] e = new Edge[next.length+prev.length];
	System.arraycopy(next,0,e,0,next.length);
	System.arraycopy(prev,0,e,next.length,prev.length);
	return (CFGEdge[]) e;
    }
    public CFGEdge[] pred() { return prevEdge(); }
    public CFGEdge[] succ() { return nextEdge(); }

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

    /** Adds an edge between two Quads.  The <code>from_index</code>ed
     *  outgoing edge of <code>from</code> is connected to the 
     *  <code>to_index</code>ed incoming edge of <code>to</code>. 
     *  @return the added <code>Edge</code>.*/
    public static Edge addEdge(Quad from, int from_index,
			       Quad to, int to_index) {
	// assert validity
	Util.ASSERT(from.qf == to.qf, "QuadFactories should always be same");
	//  [HEADERs connect only to FOOTER and METHOD]
	if (from instanceof HEADER)
	    Util.ASSERT((to instanceof FOOTER && from_index==0) || 
			(to instanceof METHOD && from_index==1) );
	//  [METHOD connects to HANDLERs on all but first edge]
	if (from instanceof METHOD && from_index > 0)
	    Util.ASSERT(to instanceof HANDLER);
	//  [ONLY HEADER, THROW and RETURN connects to FOOTER]
	if (to instanceof FOOTER)
	    Util.ASSERT((from instanceof HEADER && to_index==0) ||
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
	Util.ASSERT(oldQ.next.length == newQ.next.length);
	Util.ASSERT(oldQ.prev.length == newQ.prev.length);
	for (int i=0; i<oldQ.next.length; i++) {
	    Edge e = oldQ.next[i];
	    Quad to = (Quad) e.to();
	    if (to == oldQ) to = newQ;// detect & fixup self-loops
	    addEdge(newQ, i, to, e.which_pred());
	    oldQ.next[i] = null;
	}
	for (int i=0; i<oldQ.prev.length; i++) {
	    Edge e = oldQ.prev[i];
	    Quad from = (Quad) e.from();
	    if (from == oldQ) from = newQ;// detect & fixup self-loops
	    addEdge(from, e.which_succ(), newQ, i);
	    oldQ.prev[i] = null;
	}
    }
    /** Remove this quad from the graph.  The given quad must have
     *  exactly one predecessor and one successor. Also removes the
     *  quad from any handler sets it may belong to.  Returns the
     *  new edge which replaces this quad. */
    public Edge remove() {
	Util.ASSERT(this.next.length == 1);
	Util.ASSERT(this.prev.length == 1);
	this.removeHandlers(this.handlers());
	Edge in = this.prev[0], out = this.next[0];
	Edge result = addEdge((Quad)in.from(), in.which_succ(),
			      (Quad)out.to(), out.which_pred());
	this.prev[0] = this.next[0] = null;
	return result;
    }
    /** Update the handlers for newQ to match the handlers for oldQ,
     *  removing handlers from oldQ in the process.
     */
    public static void transferHandlers(Quad oldQ, Quad newQ) {
	// replace in HANDLERs.
	HandlerSet hs = oldQ.handlers();
	oldQ.removeHandlers(hs);
	newQ.addHandlers(hs);
    }
    /** Add this quad to the given handler sets. */
    public final void addHandlers(HandlerSet handlers) {
	for (HandlerSet hs=handlers; hs!=null; hs=hs.next)
	    hs.h.protectedSet.insert(this);
    }
    /** Remove this quad from the given handler sets. */
    public final void removeHandlers(HandlerSet handlers) {
	for (HandlerSet hs=handlers; hs!=null; hs=hs.next)
	    hs.h.protectedSet.remove(this);
    }
    /** Return a set of all the handlers guarding this <code>Quad</code>. */
    public final HandlerSet handlers() {
	METHOD Qm = (METHOD)qf.getParent().quads.next(1);
	HandlerSet hs=null;
	Quad ql[] = Qm.next();
	for (int i=ql.length-1; i > 0; i--)  // next(0) is not a HANDLER
	    if (((HANDLER)ql[i]).isProtected(this))
		hs=new HandlerSet((HANDLER)ql[i], hs);
	return hs;
    }
    //-----------------------------------------------------
    // Comparable interface.
    public int compareTo(Object o) {
	int cmp = ((Quad)o).getID() - this.getID();
	if (cmp==0 && !this.equals(o))
	    throw new ClassCastException("Comparing uncomparable Quads.");
	return cmp;
    }
    //-----------------------------------------------------
    // support cloning.  The pred/succ quads are not cloned, but the
    // array holding them is.
    public final Object clone() { return rename(this.qf, null, null); }
    /** Clone a quad into a new quad factory, renaming all of the temps
     *  according to <code>tm</code> (which ought to ensure that all
     *  the new temps belong to the <code>TempFactory</code> of the
     *  new <code>QuadFactory</code>). */
    public final Object clone(QuadFactory qf, CloningTempMap tm) {
	Quad qc = rename(qf, tm, tm);
	// verify that cloning is legit.
	for (int j=0; j<2; j++) {
	    Temp[] ta = (j==0)?qc.use():qc.def();
	    for (int i=0; i<ta.length; i++)
		Util.ASSERT(ta[i].tempFactory()==qf.tempFactory(), "TempFactories should be same");
	}
	return qc;
    }

    //-----------------------------------------------------
    /** Create a new copy of a string of <code>Quad</code>s starting at
     *  the given header using <code>QuadFactory</code>. */
    public static Quad clone(QuadFactory qf, Quad header)
    {
	Util.ASSERT(header instanceof HEADER, 
		    "Argument to Quad.clone() should be a HEADER.");
	return copyone(qf, header, new HashMap(),
		       new CloningTempMap(header.qf.tempFactory(),
					  qf.tempFactory()));
    }
    /** Create a new copy of a string of <code>Quad</code>s starting
     * at the given header using the specified
     * <code>QuadFactory</code>, and returns a set
     * of mappings.  The cloned quads will be rooted at
     *  <code>elementMap().get(header)</code>.
     */
    static HCodeAndMaps cloneWithMaps(QuadFactory qf, Quad header)
    {
	Util.ASSERT(header instanceof HEADER, 
		    "Argument to Quad.clone() should be a HEADER.");
	Map qm = new HashMap();
	CloningTempMap ctm = new CloningTempMap(header.qf.tempFactory(),
						qf.tempFactory());
	copyone(qf, header, qm, ctm);
	// make new-to-old mappings from old-to-new mappings.
	final Map n2oQuad = new HashMap(), n2oTemp = new HashMap();
	for (Iterator it=qm.entrySet().iterator(); it.hasNext(); ) {
	    Map.Entry me = (Map.Entry) it.next();
	    Quad qO = (Quad) me.getKey(), qN = (Quad) me.getValue();
	    n2oQuad.put(qN, qO);
	}
	for (Iterator it=ctm.asMap().entrySet().iterator(); it.hasNext(); ) {
	    Map.Entry me = (Map.Entry) it.next();
	    Temp tO = (Temp) me.getKey(), tN = (Temp) me.getValue();
	    n2oTemp.put(tN, tO);
	}
	// make type-safe tuple of immutable maps to return.
	// NOTE THE NULLS: THIS IS NOT A FULL RESULT: YOU SHOULD BE
	// USING HCode.clone() or Code.cloneHelper() IF YOU WANT A
	// VALID HCODEANDMAPS!
	return new HCodeAndMaps(null,
				Collections.unmodifiableMap(qm),
				ctm.unmodifiable(),
				null,
				Collections.unmodifiableMap(n2oQuad),
				new TempMap() {
	    public Temp tempMap(Temp t) { return (Temp) n2oTemp.get(t); }
	});
    }
    private static Quad copyone(QuadFactory qf, Quad q, Map old2new,
				CloningTempMap ctm)
    {
	Quad r = (Quad) old2new.get(q);
	// if we've already done this one, return previous clone.
	if (r!=null) return r;
	// clone the fields, add to map.
	r = (Quad) q.clone(qf, ctm);
	old2new.put(q, r);
	// fixup the edges.
	for (int i=0; i<q.next.length; i++) {
	    Util.ASSERT(q.next[i].from == q);
	    Quad to = copyone(qf, q.next[i].to, old2new, ctm);
	    Quad.addEdge(r, q.next[i].from_index, to, q.next[i].to_index);
	}
	for (int i=0; i<q.prev.length; i++) {
	    Util.ASSERT(q.prev[i].to == q);
	    Quad from = copyone(qf, q.prev[i].from, old2new, ctm);
	    Quad.addEdge(from, q.prev[i].from_index, r, q.prev[i].to_index);
	}
	// for HANDLER quads, fixup the protectedSet.
	if (r instanceof HANDLER) {
	    HANDLER h = (HANDLER) r;
	    HANDLER.ProtectedSet ps = h.protectedSet;
	    // map all protected quads.
	    Quad[] oldqs=(Quad[])h.protectedSet().toArray(new Quad[ps.size()]);
	    for (int i=0; i < oldqs.length; i++) {
		ps.remove(oldqs[i]);
		ps.insert(copyone(qf, oldqs[i], old2new, ctm));
	    }
	}
	return r;
    }
    // ----------------------------------------------------
    // Useful for temp renaming.  Exported only to subclasses.
    /** Apply <code>TempMap</code> <code>tm</code> to <code>Temp</code>
     *  <code>t</code>.
     *  @return <code>tm.tempMap(t)</code> if <code>t</code> is
     *  non-<code>null</code>, or <code>null</code> if <code>t</code> is
     *  <code>null</code>. */
    protected final static Temp map(TempMap tm, Temp t) {
	return (t==null)?null:(tm==null)?t:tm.tempMap(t);
    }
    /** Apply <code>TempMap</code> to array of <code>Temp</code>s.
     *  Null <code>Temp</code>s get mapped to <code>null</code>. */
    protected final static Temp[] map(TempMap tm, Temp[] ta) {
	Temp[] r = new Temp[ta.length];
	for (int i=0; i<r.length; i++)
	    r[i] = map(tm, ta[i]);
	return r;
    }
    /** Apply <code>TempMap</code> to 2-d array of <code>Temp</code>s.
     *  Null <code>Temp</code>s get mapped to <code>null</code>. */
    protected final static Temp[][] map(TempMap tm, Temp[][] taa) {
	Temp[][] r = new Temp[taa.length][];
	for (int i=0; i<r.length; i++)
	    r[i] = map(tm, taa[i]);
	return r;
    }
}
