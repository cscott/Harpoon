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
import harpoon.Util.Collections.AggregateSetFactory;
import harpoon.Util.Collections.GenericMultiMap;
import harpoon.Util.Collections.MultiMap;
import harpoon.Util.Util;

import java.util.Collection;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;
/**
 * <code>DomTree</code> computes the dominator tree of a flowgraph-structured
 * IR.  The <code>HCode</code> must have a valid
 * <code>CFGrapher</code>.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: DomTree.java,v 1.8.2.8 2000-11-10 23:38:43 cananian Exp $
 */

public class DomTree /*implements Graph*/ {
    /** <code>HCode</code> this <code>DomTree</code> corresponds to. */
    final HCode hcode; // included only for DomFrontier's use.
    /** <code>CFGrapher</code> used to construct this <code>DomTree</code>.*/
    final CFGrapher grapher; // included only for DomFrontier's use.
    /** Mapping of HCodeElements to their immediate dominators. */
    Hashtable idom  = new Hashtable();
    /** Reverse mapping: mapping of an HCodeElement to the elements which
     *  have this element as their immediate dominator. */
    final SetHTable children;

    /** Creates a new <code>DomTree</code> with the dominator
     *  tree for the given <code>HCode</code>; if <code>isPost</code> is
     *  true, creates a postdominator tree instead. Uses the default
     *  <code>CFGrapher</code>, which means the elements of the
     *  <code>HCode</code> must implement <code>CFGraphable</code>. */
    public DomTree(HCode hcode, boolean isPost) {
	this(hcode, CFGrapher.DEFAULT, isPost);
    }
    /** Creates a new <code>DomTree</code> with the dominator
     *  tree for the given <code>HCode</code>; if <code>isPost</code> is
     *  true, creates a postdominator tree instead. Uses the specified
     *  <code>CFGrapher</code> to construct the control flow graph of
     *  the <code>HCode</code>. */
    public DomTree(HCode hcode, CFGrapher grapher, boolean isPost) {
	this(hcode, isPost?grapher.edgeReversed():grapher);
    }
    /** Common constructor. Not for external use: we want people to be aware
     *  whether they're requesting a dominator or post-dominator tree. */
    private DomTree(HCode hcode, CFGrapher grapher) {
	this.hcode = hcode;
	this.grapher = grapher;
	this.children = new SetHTable(hcode.elementArrayFactory());
	analyze(hcode, grapher);
	
    }

    /** Return the roots of the (post-)dominator tree (forest). */
    public HCodeElement[] roots() {
	return grapher.getFirstElements(hcode);
    }

    /** Return the immediate (post)dominator of an <code>HCodeElement</code>.
     * @return the immediate (post)dominator of <code>n</code>, or
     *         <code>null</code> if <code>n</code> is the root (a leaf)
     *         of the flow graph.  */
    public HCodeElement idom(HCodeElement n) {
	return (HCodeElement) idom.get(n);
    }
    /** Return the children of an <code>HCodeElement</code> in the immediate
     *  (post)dominator tree. 
     *  @return an array with all the children of <code>n</code> in the
     *          immediate dominator tree, or a zero-length array if 
     *          <code>n</code> is a tree leaf. */
    public HCodeElement[] children(HCodeElement n) {
	return children.getSet(n);
    }
    
    /** Analyze an <code>HCode</code>. */
    private void analyze(final HCode hc, final CFGrapher grapher) {
	// Setup lists and tables.
	final IntHTable dfnum = new IntHTable();
	final Hashtable semi  = new Hashtable();
	final Hashtable ancestor = new Hashtable();
	final Hashtable parent = new Hashtable();
	final Hashtable best  = new Hashtable();
	
	SetHTable bucket = new SetHTable(hc.elementArrayFactory());
	Hashtable samedom = new Hashtable();

	final Vector vertex = new Vector();

	/** Utility class wraps analysis subroutines. */
	class Utility {
	    /** Number nodes of depth-first spanning tree */
	    void DFS(HCodeElement p, HCodeElement n) {
		if (dfnum.getInt(n)==0) {
		    int N = vertex.size()+1;
		    dfnum.putInt(n, N);
		    if (p!=null) parent.put(n, p);
		    vertex.addElement(n);
		    // for each successor of n...
		    for (Iterator it=grapher.succC(n).iterator();it.hasNext();)
			DFS(n, ((HCodeEdge)it.next()).to());
		}
	    }
	    /** Add edge p->n to spanning forest. */
	    void Link(HCodeElement p, HCodeElement n) {
		ancestor.put(n, p);
		best.put(n, n);
	    }
	    /** In the forest, find nonroot ancestor of n that has
	     *  lowest-numbered semidominator. */
	    HCodeElement Eval(HCodeElement v) {
		HCodeElement a = (HCodeElement) ancestor.get(v);
		if (ancestor.get(a) != null) {
		    HCodeElement b = Eval(a);
		    ancestor.put(v, ancestor.get(a));
		    if (dfnum.getInt(semi.get(b)) <
			dfnum.getInt(semi.get(best.get(v))))
			best.put(v, b);
		}
		return (HCodeElement) best.get(v);
	    }
	}
	Utility u = new Utility();

	// Dominators algorithm:
	for (Iterator it=new ArrayIterator(grapher.getFirstElements(hc));
	     it.hasNext(); )
	    u.DFS(null, (HCodeElement) it.next());
	    
	for (int i=vertex.size()-1; i>=0; i--) {
	    // calculate the semidominator of vertex[i]
	    HCodeElement n = (HCodeElement) vertex.elementAt(i);
	    HCodeElement p = (HCodeElement) parent.get(n);
	    HCodeElement s = p;
	    HCodeElement sprime;

	    if (p == null) continue; // skip root(s).

	    //   (for each predecessor v of n)
	    for (Iterator it=grapher.predC(n).iterator(); it.hasNext(); ) {
		HCodeElement v = ((HCodeEdge) it.next()).from();
		// ignore unreachable nodes.
		if (!dfnum.containsKey(v)) continue;
		if (dfnum.getInt(v) <= dfnum.getInt(n))
		    sprime = v;
		else
		    sprime = (HCodeElement) semi.get(u.Eval(v));
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
	    HCodeElement[] hcel = bucket.getSet(p);
	    for (int j=0; j<hcel.length; j++) {
		HCodeElement v = hcel[j];
		HCodeElement y = u.Eval(v);
		if (semi.get(y) == semi.get(v))
		    idom.put(v, p);
		else
		    samedom.put(v, y);
	    }
	    bucket.clearSet(p);
	}
	// Now all the deferred dominator calculations are performed.
	for (int i=0; i<vertex.size(); i++) {
	    HCodeElement n = (HCodeElement) vertex.elementAt(i);
	    if (parent.get(n)==null) continue; // skip root(s).
	    if (samedom.get(n)!=null)
		idom.put(n, idom.get(samedom.get(n)));
	}
	// done.  Make inverse mapping.
	for (int i=0; i<vertex.size(); i++) {
	    HCodeElement n  = (HCodeElement) vertex.elementAt(i);
	    HCodeElement id = (HCodeElement) idom.get(n);
	    if (parent.get(n)==null) continue; // skip root(s).
	    children.unionSet(id, n);
	}
    }


    // Useful Hashtable extensions.
    static class IntHTable {
	private Map m = new HashMap();
	void putInt(Object o, int n) {
	    m.put(o, new Integer(n));
	}
	int getInt(Object o) {
	    if (!m.containsKey(o)) return 0;
	    return ((Integer)m.get(o)).intValue();
	}
	boolean containsKey(Object o) {
	    return m.containsKey(o);
	}
    }
    static class SetHTable {
	ArrayFactory af;
	MultiMap mm = new GenericMultiMap(new AggregateSetFactory());
	SetHTable(ArrayFactory af) { super(); this.af = af; }
	void clearSet(HCodeElement hce) {
	    mm.remove(hce);
	}
	HCodeElement[] getSet(HCodeElement hce) {
	    Collection c = mm.getValues(hce);
	    return (HCodeElement[]) c.toArray(af.newArray(c.size()));
	}
	void unionSet(HCodeElement hce, HCodeElement newEl) {
	    mm.add(hce, newEl);
	}
    }
}


