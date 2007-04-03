// DomTree.java, created Mon Sep 14 17:38:43 1998 by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis;

import harpoon.ClassFile.HCode;
import harpoon.ClassFile.HCodeEdge;
import harpoon.ClassFile.HCodeElement;
import harpoon.IR.Properties.CFGrapher;
import harpoon.Util.ArrayFactory;
import harpoon.Util.ArrayIterator;
import net.cscott.jutil.AggregateSetFactory;
import net.cscott.jutil.GenericMultiMap;
import net.cscott.jutil.MultiMap;
import harpoon.Util.Util;
import net.cscott.jutil.Default;

import java.util.Collection;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;
import java.util.List;
import java.util.Stack;

/**
 * <code>DomTree</code> computes the dominator tree of a flowgraph-structured
 * IR.  The <code>HCode</code> must have a valid
 * <code>CFGrapher</code>.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: DomTree.java,v 1.13 2007-04-03 19:59:21 cananian Exp $
 */

public class DomTree<HCE extends HCodeElement> /*implements Graph*/ {
    /** <code>HCode</code> this <code>DomTree</code> corresponds to. */
    final HCode<HCE> hcode; // included only for DomFrontier's use.
    /** <code>CFGrapher</code> used to construct this <code>DomTree</code>.*/
    final CFGrapher<HCE> grapher; // included only for DomFrontier's use.
    /** Mapping of HCodeElements to their immediate dominators. */
    Map<HCE,HCE> idom  = new HashMap<HCE,HCE>();
    /** Reverse mapping: mapping of an HCodeElement to the elements which
     *  have this element as their immediate dominator. */
    final SetHTable<HCE> children;

    /** Creates a new <code>DomTree</code> with the dominator
     *  tree for the given <code>HCode</code>; if <code>isPost</code> is
     *  true, creates a postdominator tree instead. Uses the default
     *  <code>CFGrapher</code>, which means the elements of the
     *  <code>HCode</code> must implement <code>CFGraphable</code>. */
    public DomTree(HCode<HCE> hcode, boolean isPost) {
	this(hcode, (CFGrapher<HCE>) CFGrapher.DEFAULT, isPost);
    }
    /** Creates a new <code>DomTree</code> with the dominator
     *  tree for the given <code>HCode</code>; if <code>isPost</code> is
     *  true, creates a postdominator tree instead. Uses the specified
     *  <code>CFGrapher</code> to construct the control flow graph of
     *  the <code>HCode</code>. */
    public DomTree(HCode<HCE> hcode, CFGrapher<HCE> grapher, boolean isPost) {
	this(hcode, isPost?grapher.edgeReversed():grapher);
    }
    /** Common constructor. Not for external use: we want people to be aware
     *  whether they're requesting a dominator or post-dominator tree. */
    private DomTree(HCode<HCE> hcode, CFGrapher<HCE> grapher) {
	this.hcode = hcode;
	this.grapher = grapher;
	this.children = new SetHTable<HCE>(hcode.elementArrayFactory());
	analyze(hcode, grapher);
	
    }

    /** Return the roots of the (post-)dominator tree (forest). */
    public HCE[] roots() {
	return grapher.getFirstElements(hcode);
    }

    /** Return the immediate (post)dominator of an <code>HCodeElement</code>.
     * @return the immediate (post)dominator of <code>n</code>, or
     *         <code>null</code> if <code>n</code> is the root (a leaf)
     *         of the flow graph.  */
    public HCE idom(HCE n) {
	return idom.get(n);
    }
    /** Return the children of an <code>HCodeElement</code> in the immediate
     *  (post)dominator tree. 
     *  @return an array with all the children of <code>n</code> in the
     *          immediate dominator tree, or a zero-length array if 
     *          <code>n</code> is a tree leaf. */
    public HCE[] children(HCE n) {
	return children.getSet(n);
    }
    
    /** Analyze an <code>HCode</code>. */
    private void analyze(final HCode<HCE> hc, final CFGrapher<HCE> grapher) {
	// Setup lists and tables.
	final IntHTable<HCE> dfnum = new IntHTable<HCE>();
	final Map<HCE,HCE> semi  = new HashMap<HCE,HCE>();
	final Map<HCE,HCE> ancestor = new HashMap<HCE,HCE>();
	final Map<HCE,HCE> parent = new HashMap<HCE,HCE>();
	final Map<HCE,HCE> best  = new HashMap<HCE,HCE>();
	
	SetHTable<HCE> bucket = new SetHTable<HCE>(hc.elementArrayFactory());
	Map<HCE,HCE> samedom = new HashMap<HCE,HCE>();

	final Vector<HCE> vertex = new Vector<HCE>();

	/** Utility class wraps analysis subroutines. */
	class Utility {
	    /** Number nodes of depth-first spanning tree 
		@requires: n_orig != null.  (note p_orig may be null)
	    */
	    void DFS(HCE p_orig, HCE n_orig) {
		// Does an depth first iteration by keeping partially
		// traversed iterators on the stack.
		Stack<Pair<HCE,Iterator<HCE>>> stk =
		    new Stack<Pair<HCE,Iterator<HCE>>>();
		stk.push( cons(p_orig, Default.singletonIterator(n_orig)) );
		while( ! stk.isEmpty() ){
		    Pair<HCE,Iterator<HCE>> pair = stk.peek();
		    HCE p = pair.left;
		    Iterator<HCE> niter = pair.right;
		    if (niter.hasNext()){
			HCE n = niter.next();
			if (dfnum.getInt(n)==0) {
			    int N = vertex.size()+1;
			    dfnum.putInt(n, N);
			    if (p!=null) 
				parent.put(n, p);
			    vertex.addElement(n);
			    Iterator<HCE> succIter = grapher.succElemC(n).iterator();
			    stk.push( cons(n, succIter) );
			}
			continue;
		    } else {
			stk.pop();
		    }
		}
	    }
	    
	    /** Add edge p->n to spanning forest. */
	    void Link(HCE p, HCE n) {
		ancestor.put(n, p);
		best.put(n, n);
	    }
	    /** In the forest, find nonroot ancestor of n that has
	     *  lowest-numbered semidominator. */
	    HCE Eval(HCE vOrig) {
		HCE v, a, b;
		Stack<Pair<HCE,HCE>> stk = new Stack<Pair<HCE,HCE>>();
		v = vOrig; 
		a = ancestor.get(v);
		while( ancestor.get(a) != null ){
		    stk.push( cons(v, a) );
		    v = a; a = ancestor.get(a);
		}
		b = best.get(v); // base case return
		while( ! stk.isEmpty() ){
		    Pair<HCE,HCE> pair = stk.pop();
		    v = pair.left;
		    a = pair.right;
		    ancestor.put(v, ancestor.get(a));
		    if (dfnum.getInt(semi.get(b)) < 
			dfnum.getInt(semi.get(best.get(v)))){
			best.put(v, b);
		    }
		    b = best.get(v); // recursive return
		}
		assert v == vOrig;
		assert b == best.get(v);
		return b;
	    }
	}
	Utility u = new Utility();

	// Dominators algorithm:
	for (Iterator<HCE> it = new ArrayIterator<HCE>
		 (grapher.getFirstElements(hc)); it.hasNext(); )
	    u.DFS(null, it.next());
	    
	for (int i=vertex.size()-1; i>=0; i--) {
	    // calculate the semidominator of vertex[i]
	    HCE n = vertex.elementAt(i);
	    HCE p = parent.get(n);
	    HCE s = p;
	    HCE sprime;

	    if (p == null) continue; // skip root(s).

	    //   (for each predecessor v of n)
	    for (Iterator<HCodeEdge<HCE>> it=grapher.predC(n).iterator(); it.hasNext(); ) {
		HCE v = it.next().from();
		// ignore unreachable nodes.
		if (!dfnum.containsKey(v)) continue;
		if (dfnum.getInt(v) <= dfnum.getInt(n))
		    sprime = v;
		else
		    sprime = semi.get(u.Eval(v));
		if (dfnum.getInt(sprime) < dfnum.getInt(s))
		    s = sprime;
	    }
	    semi.put(n, s);

	    // Calculation of n's dominator is deferred until the path
	    // from s to n has been linked into the forest.
	    bucket.unionSet(s, n);
	    u.Link(p, n);

	    // Now that the path from p to n has been linked into the
	    // spanning forest, calculate the dominator of v (or else defer
	    // the calculation).
	    //   (for each v in bucket[p])
	    HCE[] hcel = bucket.getSet(p);
	    for (int j=0; j<hcel.length; j++) {
		HCE v = hcel[j];
		HCE y = (HCE) u.Eval(v);//XXX: cast is JAVAC BUG
		if (semi.get(y) == semi.get(v))
		    idom.put(v, p);
		else
		    samedom.put(v, y);
	    }
	    bucket.clearSet(p);
	}
	// Now all the deferred dominator calculations are performed.
	for (int i=0; i<vertex.size(); i++) {
	    HCE n = vertex.elementAt(i);
	    if (parent.get(n)==null) continue; // skip root(s).
	    if (samedom.get(n)!=null)
		idom.put(n, idom.get(samedom.get(n)));
	}
	// done.  Make inverse mapping.
	for (int i=0; i<vertex.size(); i++) {
	    HCE n  = vertex.elementAt(i);
	    HCE id = idom.get(n);
	    if (parent.get(n)==null) continue; // skip root(s).
	    children.unionSet(id, n);
	}
    }

    // utility class.
    static class Pair<A,B> {
	public final A left;
	public final B right;
	Pair(A a, B b) { this.left = a; this.right = b; }
    }
    private static <A,B> Pair<A,B> cons(A a, B b) { return new Pair(a, b); }

    // Useful Hashtable extensions.
    static class IntHTable<V> {
	private Map<V,Integer> m = new HashMap<V,Integer>();
	void putInt(V o, int n) {
	    m.put(o, new Integer(n));
	}
	int getInt(V o) {
	    if (!m.containsKey(o)) return 0;
	    return m.get(o).intValue();
	}
	boolean containsKey(Object o) {
	    return m.containsKey(o);
	}
    }
    static class SetHTable<HCE extends HCodeElement> {
	ArrayFactory<HCE> af;
	MultiMap<HCE,HCE> mm =
	    new GenericMultiMap<HCE,HCE>(new AggregateSetFactory<HCE>());
	SetHTable(ArrayFactory<HCE> af) { super(); this.af = af; }
	void clearSet(HCE hce) {
	    mm.remove(hce);
	}
	HCE[] getSet(HCE hce) {
	    Collection<HCE> c = mm.getValues(hce);
	    return c.toArray(af.newArray(c.size()));
	}
	void unionSet(HCE hce, HCE newEl) {
	    mm.add(hce, newEl);
	}
    }
}
