// TypeSwitchRemover.java, created Tue Oct 10 13:32:03 2000 by cananian
// Copyright (C) 2000 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.Quads;

import harpoon.Analysis.Maps.Derivation;
import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HCode;
import harpoon.ClassFile.HCodeAndMaps;
import harpoon.ClassFile.HCodeElement;
import harpoon.ClassFile.HCodeFactory;
import harpoon.IR.Quads.CJMP;
import harpoon.IR.Quads.Code;
import harpoon.IR.Quads.Edge;
import harpoon.IR.Quads.INSTANCEOF;
import harpoon.IR.Quads.Quad;
import harpoon.IR.Quads.QuadFactory;
import harpoon.IR.Quads.SIGMA;
import harpoon.IR.Quads.TYPESWITCH;
import harpoon.IR.LowQuad.DerivationMap;
import harpoon.Temp.Temp;
import harpoon.Temp.TempFactory;
import net.cscott.jutil.CombineIterator;
import harpoon.Util.HClassUtil;
import harpoon.Util.Util;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
/**
 * <code>TypeSwitchRemover</code> converts <code>TYPESWITCH</code> quads
 * into chains of <code>INSTANCEOF</code> and <code>CJMP</code> quads.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: TypeSwitchRemover.java,v 1.6 2004-02-08 01:53:14 cananian Exp $
 */
public final class TypeSwitchRemover
    extends harpoon.Analysis.Transformation.MethodMutator<Quad> {
    
    /** Creates a <code>TypeSwitchRemover</code>. */
    public TypeSwitchRemover(HCodeFactory parent) { super(parent); }

    protected HCode<Quad> mutateHCode(HCodeAndMaps<Quad> input) {
	Code hc = (Code) input.hcode();
	// get mutable derivation map.
	// (no changes necessary to allocation information map)
	DerivationMap<Quad> dm = (DerivationMap<Quad>) hc.getDerivation();
	// dm (if non-null) will be updated as TYPESWITCHes are replaced.

	// we put all elements in array to avoid screwing up the
	// iterator as we mutate the quad graph in-place.
	Quad[] allquads = hc.getElements();
	for (int i=0; i<allquads.length; i++)
	    if (allquads[i] instanceof TYPESWITCH)
		replace((TYPESWITCH) allquads[i], dm);
	// now we have to prune off any newly-unreachable TYPESWITCH cases.
	Unreachable.prune(hc);
	// yay, done!
	return hc;
    }

    private static void replace(TYPESWITCH ts, DerivationMap<Quad> dm) {
	/* construct instanceof chain */
	Edge e = ts.prevEdge(0);
	TypeTree tt =
	    makeTest(constructCT(ts), ts, e.from(), e.which_succ(),
		     ts.src(), dm);
	// fixup derivations if present.
	if (dm!=null) tt.fixupSigmaDerivations(ts, dm);
    }
    /** Construct INSTANCEOF chain from a proper & pruned ClassTree */
    private static TypeTree makeTest(ClassTree ct, TYPESWITCH ts,
				     Quad head, int which_succ, Temp[] src,
				     DerivationMap<Quad> dm) {
	QuadFactory qf = ts.getFactory();
	TempFactory tf = qf.tempFactory();
	TypeNode ftn = new TypeNode(null), ptn = ftn; // keep track of TypeTree
	for (Iterator<ClassTree> it=ct.children(); it.hasNext(); ) {
	    // for each child c of ct...
	    ClassTree c = it.next();
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
	    if (dm!=null) // update derivation w/ info about Textra
		dm.putType(q0, Textra, HClass.Int/*internal form of boolean*/);
	    ptn = (TypeNode) (ptn.child[0] = new TypeNode(q1));
	    // then...
	    ptn.child[1] = makeTest(c, ts, q1, 1, slice(q1, 1), dm);
	    // else...
	    head=q1; which_succ=0; src=slice(q1, 0);
	}
	// link remaining else... to TYPESWITCH edge ct.edgenum
	Edge e = ts.nextEdge(ct.edgenum);
	Quad.addEdge(head, which_succ, e.to(), e.which_pred());
	ptn.child[0] = new TypeLeaf(ct.edgenum);
	// check that all sigma functions have been handled
	if (ct.key==null && !ct.children().hasNext())
	    assert ts.numSigmas()==0;// um, 1-arity TS should not have
	                                   // sigma functions
	// done.
	return ftn.child[0]; // return root of TypeTree.
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
	// if the TYPESWITCH has no default case, fixup the ClassTree.
	if (!ts.hasDefault()) {
	    Iterator<ClassTree> it = root.children();
	    // must have at least one key if it has no default.
	    // make key with highest edgenum the new default.
	    ClassTree newdefault = it.next();
	    while (it.hasNext()) {
		ClassTree ct = it.next();
		if (ct.edgenum > newdefault.edgenum)
		    newdefault = ct;
	    }
	    // now make new root node and reparent children of newdefault
	    // and other children of root.
	    ClassTree newroot = new ClassTree(null, newdefault.edgenum);
	    it = new CombineIterator<ClassTree>
		(root.children(), newdefault.children());
	    while (it.hasNext()) {
		ClassTree ct = it.next();
		it.remove();
		if (ct!=newdefault) newroot.addChild(ct);
	    }
	    root = newroot;
	}
	// okay, done.
	return root;
    }
    /** Recursively descend ClassTree to find where to place this node */
    private static void addNode(ClassTree root, ClassTree node) {
	assert root.key==null || node.key.isInstanceOf(root.key);
	assert !node.children().hasNext();
	if (node.edgenum > root.edgenum) return;
	// traverse child list. either node is a subclass of a child,
	// or one-or-more children are subclasses of node, or neither.
	boolean linkme=true;
	for (Iterator<ClassTree> it=root.children(); it.hasNext(); ) {
	    ClassTree ctp = it.next();
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

    /** The class tree structure represents the class hierarchy relation
     *  over the subset of the classes mentioned in the TYPESWITCH.
     *  Each node also contains an edgenum which corresponds to the
     *  key number in the TYPESWITCH which mentions this class; nodes
     *  are pruned on insertion such that parent.edgenum &gt; child.edgenum,
     *  which ensures that all nodes correspond to reachable cases of
     *  the TYPESWITCH.  The root corresponds to the default case. */
    private static class ClassTree {
	final HClass key;
	final int edgenum;
	private List<ClassTree> children;
	ClassTree(HClass key, int edgenum) {
	    this.key = key; this.edgenum = edgenum;
	    this.children = new LinkedList();
	}
	Iterator<ClassTree> children() { return children.iterator(); }
	void addChild(ClassTree nchild) {
	    assert this.edgenum > nchild.edgenum;
	    children.add(nchild);
	}	    
	// pretty-printing.
	public String toString() { return "CT<"+key+","+edgenum+">"; }
	public void dump(java.io.PrintWriter pw) { dump(pw, 0); pw.flush(); }
	private void dump(java.io.PrintWriter pw, int indent) {
	    for (int i=0; i<indent; i++) pw.print("  ");
	    pw.println(toString());
	    for (Iterator<ClassTree> it=children(); it.hasNext(); )
		it.next().dump(pw, indent+1);
	}
	public void dump(java.io.Writer w) {
	    dump(new java.io.PrintWriter(w));
	}
	public void dump(java.io.OutputStream os) {
	    dump(new java.io.PrintWriter(os));
	}
    }

    // UGH.  Lots of cruft for proper derivations. ------------

    /** The typetree keeps a record of the created CJMP structure
     *  so that we can go back (and with fixupSigmaDerivations()) add
     *  the proper derivation information in a post-pass. */
    private static abstract class TypeTree {
	/** add proper derivation information to the CJMPs described by this.*/
	abstract TypeAndDerivation[] fixupSigmaDerivations(TYPESWITCH ts,
							   DerivationMap<Quad> dm);
    }
    /** A TypeNode represents a CJMP, with sigmas that need derivations. */
    private static class TypeNode extends TypeTree {
	final CJMP cjmp;
	final TypeTree[] child = new TypeTree[2];
	TypeNode(CJMP cjmp) { this.cjmp = cjmp; }
	TypeAndDerivation[] fixupSigmaDerivations(TYPESWITCH ts,
						  DerivationMap<Quad> dm) {
	    TypeAndDerivation[] result=new TypeAndDerivation[ts.numSigmas()];
	    for (int i=0; i<2; i++) {
		TypeAndDerivation[] tad=child[i].fixupSigmaDerivations(ts,dm);
		for (int j=0; j<tad.length; j++) {
		    tad[j].apply(dm, cjmp, cjmp.dst(j, i));
		    result[j] = (result[j]==null) ? tad[j] :
			TypeAndDerivation.merge(result[j], tad[j]);
		}
	    }
	    return result;
	}
    }
    /** A TypeLeaf represents an edge which corresponds to an edge of
     *  the original TYPESWITCH -- i.e. the sigma derivations for this
     *  edge should be identical to those on the original TYPESWITCH edge. */
    private static class TypeLeaf extends TypeTree {
	final int edgenum;
	TypeLeaf(int edgenum) { this.edgenum = edgenum; }
	TypeAndDerivation[] fixupSigmaDerivations(TYPESWITCH ts,
						  DerivationMap<Quad> dm) {
	    // fetch types from appropriate slice of TYPESWITCH
	    TypeAndDerivation[] r = new TypeAndDerivation[ts.numSigmas()];
	    for (int i=0; i<r.length; i++) {
		r[i]=new TypeAndDerivation(dm, ts, ts.dst(i, edgenum));
		dm.remove(ts, ts.dst(i, edgenum)); // clean up.
	    }
	    return r;
	}
    }
    /** unified type/derivation information. */
    private static class TypeAndDerivation {
	/** non-null for base pointers */
	public final HClass type;
	/** non-null for derived pointers */ 
	public final Derivation.DList derivation;
	// public constructors
	TypeAndDerivation(HClass type) { this(type, null); }
	TypeAndDerivation(Derivation.DList deriv) { this(null, deriv); }
	// helpful.
	<HCE extends HCodeElement> TypeAndDerivation(Derivation<HCE> d,
						     HCE hce, Temp t) {
	    this(d.typeMap(hce, t), d.derivation(hce, t));
	}
	/** private constructor */
	private TypeAndDerivation(HClass type, Derivation.DList derivation) {
	    assert type!=null ^ derivation!=null;
	    this.type = type;
	    this.derivation = derivation;
	}
	/** store this TypeAndDerivation information in the given
	 *  DerivationMap for the given HCodeElement/Temp pair. */
	<HCE extends HCodeElement> void apply(DerivationMap<HCE> dm,
					      HCE hce, Temp t) {
	    if (type!=null) dm.putType(hce, t, type);
	    else dm.putDerivation(hce, t, derivation);
	}
	/** Merge the given TypeAndDerivations. */
	static TypeAndDerivation merge(TypeAndDerivation a,
				       TypeAndDerivation b) {
	    if (a.type!=null && b.type!=null)
		return new TypeAndDerivation
		    (HClassUtil.commonParent(a.type, b.type));
	    // both ought to be derivations.
	    assert a.derivation!=null && b.derivation!=null : "can't merge type with derivation";
	    assert a.derivation.equals(b.derivation) : "can't merge derivations";
	    return a;
	}
    }
}
