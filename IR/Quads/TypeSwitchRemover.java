// TypeSwitchRemover.java, created Tue Oct 10 13:32:03 2000 by cananian
// Copyright (C) 2000 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.Quads;

import harpoon.Analysis.Quads.Unreachable;
import harpoon.ClassFile.*;
import harpoon.Temp.Temp;
import harpoon.Temp.TempFactory;
import harpoon.Util.Util;

import java.util.*;
/**
 * <code>TypeSwitchRemover</code> converts <code>TYPESWITCH</code> quads
 * into chains of <code>INSTANCEOF</code> and <code>CJMP</code> quads.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: TypeSwitchRemover.java,v 1.1.2.1 2000-10-10 21:33:49 cananian Exp $
 */
public final class TypeSwitchRemover
    extends harpoon.Analysis.Transformation.MethodMutator {
    
    /** Creates a <code>TypeSwitchRemover</code>. */
    public TypeSwitchRemover(HCodeFactory parent) { super(parent); }

    protected HCode mutateHCode(HCodeAndMaps input) {
	HCode hc = input.hcode();
	// we put all elements in array to avoid screwing up the
	// iterator as we mutate the quad graph in-place.
	Quad[] allquads = (Quad[]) hc.getElements();
	for (int i=0; i<allquads.length; i++)
	    if (allquads[i] instanceof TYPESWITCH)
		replace((TYPESWITCH) allquads[i]);
	// now we have to prune off any newly-unreachable TYPESWITCH cases.
	Unreachable.prune((HEADER)hc.getRootElement());
	// yay, done!
	return hc;
    }

    private static void replace(TYPESWITCH ts) {
	/* construct instanceof chain */
	Edge e = ts.prevEdge(0);
	makeTest(constructCT(ts), ts, (Quad) e.from(), e.which_succ(),
		 ts.src());
    }
    /** Construct INSTANCEOF chain from a proper & pruned ClassTree */
    private static void makeTest(ClassTree ct, TYPESWITCH ts,
			  Quad head, int which_succ, Temp[] src) {
	QuadFactory qf = ts.getFactory();
	TempFactory tf = qf.tempFactory();
	for (Iterator it=ct.children(); it.hasNext(); ) {
	    // for each child c of ct...
	    ClassTree c = (ClassTree) it.next();
	    // tweak sigma functions.
	    Temp[][] dst = mkdst(src);
	    if (!c.children().hasNext())
		tweak(dst, 1, ts, c.edgenum);// copy into slice 1
	    if (!it.hasNext())
		tweak(dst, 0, ts, ct.edgenum);// copy into slice 0
	    // make INSTANCEOF(c.key) then makeTest(c) else...
	    Temp Textra = new Temp(tf);
	    Quad q0 = new INSTANCEOF(qf, ts, Textra, ts.index(), c.key);
	    CJMP q1 = new CJMP(qf, ts, Textra, dst, src);
	    Quad.addEdge(head, which_succ, q0, 0);
	    Quad.addEdge(q0, 0, q1, 0);
	    // then...
	    makeTest(c, ts, q1, 1, slice(q1, 1));
	    // else...
	    head=q1; which_succ=0; src=slice(q1, 0);
	}
	// link remaining else... to TYPESWITCH edge ct.edgenum
	Edge e = ts.nextEdge(ct.edgenum);
	Quad.addEdge(head, which_succ, (Quad) e.to(), e.which_pred());
	// check that all sigma functions have been handled
	if (ct.key==null && !ct.children().hasNext())
	    Util.assert(ts.numSigmas()==0);// um, 1-arity TS should not have
	                                   // sigma functions
	// done.
    }
    /** Copy a slice of the TYPESWITCH's sigma function to the specified
     *  slice of the dst array. */
    private static void tweak(Temp[][] dst, int dslice,
			      TYPESWITCH ts, int edgenum) {
	for (int i=0; i<dst.length; i++)
	    dst[i][dslice] = ts.dst(i, edgenum);
    }
    /** make a slice of the given sigma function dsts */
    private static Temp[] slice(SIGMA s, int nTuple) {
	Temp[] r = new Temp[s.numSigmas()];
	for (int i=0; i<s.numSigmas(); i++)
	    r[i] = s.dst(i, nTuple);
	return r;
    }
    /** clone the src array temps twice to make an appropriate dst array */
    private static Temp[][] mkdst(Temp[] src) {
	Temp[][] r = new Temp[src.length][2];
	for (int i=0; i<src.length; i++)
	    for (int j=0; j<2; j++)
		r[i][j] = new Temp(src[i]);
	return r;
    }

    /** Make pruned ClassTree from cases of the TYPESWITCH */
    private static ClassTree constructCT(TYPESWITCH ts) {
	ClassTree root = new ClassTree(null, ts.keysLength());
	for (int i=0; i<ts.keysLength(); i++)
	    addNode(root, new ClassTree(ts.keys(i), i));
	return root;
    }
    /** Recursively descend ClassTree to find where to place this node */
    private static void addNode(ClassTree root, ClassTree node) {
	Util.assert(root.key==null || node.key.isInstanceOf(root.key));
	Util.assert(!node.children().hasNext());
	if (node.edgenum > root.edgenum) return;
	// traverse child list. either node is a subclass of a child,
	// or one-or-more children are subclasses of node, or neither.
	boolean linkme=true;
	for (Iterator it=root.children(); it.hasNext(); ) {
	    ClassTree ctp = (ClassTree) it.next();
	    if (node.key.isInstanceOf(ctp.key)) {
		addNode(ctp, node);
		linkme=false;
	    } else if (ctp.key.isInstanceOf(node.key)) {
		/* unlink this node from parent */
		it.remove();
		/* now, if propitious, link to this. */
		if (ctp.edgenum < node.edgenum)
		    node.addChild(ctp);
		continue;
	    }
	}
	if (linkme)
	    root.addChild(node);
    }

    private static class ClassTree {
	final HClass key;
	final int edgenum;
	private List children;
	ClassTree(HClass key, int edgenum) {
	    this.key = key; this.edgenum = edgenum;
	    this.children = new LinkedList();
	}
	Iterator children() { return children.iterator(); }
	void addChild(ClassTree nchild) {
	    Util.assert(this.edgenum > nchild.edgenum);
	    children.add(nchild);
	}	    
	// pretty-printing.
	public String toString() { return "CT<"+key+","+edgenum+">"; }
	public void dump(java.io.PrintWriter pw) { dump(pw, 0); pw.flush(); }
	private void dump(java.io.PrintWriter pw, int indent) {
	    for (int i=0; i<indent; i++) pw.print("  ");
	    pw.println(toString());
	    for (Iterator it=children(); it.hasNext(); )
		((ClassTree)it.next()).dump(pw, indent+1);
	}
	public void dump(java.io.Writer w) {
	    dump(new java.io.PrintWriter(w));
	}
	public void dump(java.io.OutputStream os) {
	    dump(new java.io.PrintWriter(os));
	}
    }
}
