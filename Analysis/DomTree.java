// DomTree.java, created Mon Sep 14 17:38:43 1998 by cananian
package harpoon.Analysis;

import harpoon.ClassFile.*;
import harpoon.IR.Properties.Edges;

import java.util.Hashtable;
import java.util.Vector;
/**
 * <code>DomTree</code> computes the dominator tree of a flowgraph-structured
 * IR.  The <code>HCodeElement</code>s must implement the 
 * <code>harpoon.IR.Properties.Edges</code> interface.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: DomTree.java,v 1.4 1998-09-15 05:59:22 cananian Exp $
 */

public class DomTree /*implements Graph*/ {
    /** Set of analyzed HCodes for caching. */
    Hashtable analyzed = new Hashtable();
    /** Mapping of HCodeElements to their immediate dominators. */
    Hashtable idom  = new Hashtable();
    /** Reverse mapping: mapping of an HCodeElement to the elements which
     *  have this element as their immediate dominator. */
    SetHTable children = new SetHTable();
    /** Is this a dominator or post-dominator tree? */
    boolean isPost = false;

    /** Creates a new, empty <code>DomTree</code>. */
    public DomTree() {
	this(false);
    }
    /** Creates a new, empty <code>DomTree</code>; if <code>isPost</code> is
     *  true, creates a postdominator tree instead. */
    public DomTree(boolean isPost) {
	this.isPost = isPost;
    }

    /** Return the immediate dominator of an <code>HCodeElement</code>.
     * @return the immediate dominator of <code>n</code>, or
     *         <code>null</code> if <code>n</code> is the root
     *         of the flow graph.  */
    public HCodeElement idom(HCode hc, HCodeElement n) {
	analyze(hc); return (HCodeElement) idom.get(n);
    }
    /** Return the children of an <code>HCodeElement</code> in the immediate
     *  dominator tree. 
     *  @return an array with all the children of <code>n</code> in the
     *          immediate dominator tree, or a zero-length array if 
     *          <code>n</code> is a tree leaf. */
    public HCodeElement[] children(HCode hc, HCodeElement n) {
	analyze(hc); return children.getSet(n);
    }
    
    /** Analyze an <code>HCode</code> which implements </code>Edges</code>. */
    void analyze(HCode hc) {
	if (analyzed.get(hc) != null) return;
	analyzed.put(hc, hc);

	// Check interfaces
	if (! (hc.getRootElement() instanceof Edges) )
	    throw new Error(hc.getName() + " does not implement Edges");

	// Setup lists and tables.
	final IntHTable dfnum = new IntHTable();
	final Hashtable semi  = new Hashtable();
	final Hashtable ancestor = new Hashtable();
	final Hashtable parent = new Hashtable();
	final Hashtable best  = new Hashtable();
	
	SetHTable bucket = new SetHTable();
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
		    if (!isPost) {
			// for each successor of n...
			HCodeEdge[] el = ((Edges)n).succ();
			for (int i=0; i<el.length; i++)
			    DFS(n, el[i].to());
		    } else {
			// for each predecessor of n...
			HCodeEdge[] el = ((Edges)n).pred();
			for (int i=0; i<el.length; i++)
			    DFS(n, el[i].from());
		    }
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
	if (!isPost)
	    u.DFS(null, hc.getRootElement());
	else {
	    HCodeElement[] leaves = hc.getLeafElements();
	    for (int i=0; i<leaves.length; i++)
		u.DFS(null, leaves[i]);
	}
	    
	for (int i=vertex.size()-1; i>=0; i--) {
	    // calculate the semidominator of vertex[i]
	    HCodeElement n = (HCodeElement) vertex.elementAt(i);
	    HCodeElement p = (HCodeElement) parent.get(n);
	    HCodeElement s = p;
	    HCodeElement sprime;

	    if (p == null) continue; // skip root(s).

	    //   (for each predecessor v of n)
	    HCodeEdge el[] = (!isPost) ? ((Edges)n).pred() : ((Edges)n).succ();
	    for (int j=0; j<el.length; j++) {
		HCodeElement v = (!isPost) ? el[j].from() : el[j].to();
		// ignore unreachable nodes.
		if (!dfnum.containsKey(v)) continue;
		if (dfnum.getInt(v) < dfnum.getInt(n))
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
    static class IntHTable extends Hashtable {
	void putInt(Object o, int n) {
	    put(o, new Integer(n));
	}
	int getInt(Object o) {
	    if (!containsKey(o)) return 0;
	    return ((Integer)get(o)).intValue();
	}
    }
    static class SetHTable extends Hashtable {
	void clearSet(HCodeElement hce) {
	    remove(hce);
	}
	HCodeElement[] getSet(HCodeElement hce) {
	    if (!containsKey(hce)) return new HCodeElement[0];
	    return (HCodeElement[]) get(hce);
	}
	void unionSet(HCodeElement hce, HCodeElement newEl) {
	    if (!containsKey(hce)) {
		put(hce, new HCodeElement[] { newEl });
	    } else {
		HCodeElement[] oldset = (HCodeElement[]) get(hce);
		HCodeElement[] newset = new HCodeElement[oldset.length+1];
		for (int i=0; i < oldset.length; i++)
		    if (oldset[i] == newEl)
			return; // don't add; already present.
		    else
			newset[i] = oldset[i];
		newset[oldset.length] = newEl;
		put(hce, newset);
	    }
	}
    }
}


