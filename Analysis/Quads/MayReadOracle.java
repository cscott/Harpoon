// MayReadOracle.java, created Wed Nov  7 10:38:43 2001 by cananian
// Copyright (C) 2000 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.Quads;

import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HCode;
import harpoon.ClassFile.HConstructor;
import harpoon.ClassFile.HField;
import harpoon.ClassFile.HMethod;
import harpoon.IR.Quads.CALL;
import harpoon.IR.Quads.Edge;
import harpoon.IR.Quads.GET;
import harpoon.IR.Quads.HEADER;
import harpoon.IR.Quads.PHI;
import harpoon.IR.Quads.Quad;
import harpoon.IR.Quads.QuadVisitor;
import harpoon.IR.Quads.RETURN;
import harpoon.IR.Quads.SIGMA;
import harpoon.IR.Quads.THROW;
import harpoon.Util.Collections.DisjointSet;
import harpoon.Util.Util;
import harpoon.Util.Collections.WorkSet;
import harpoon.Util.Collections.AggregateSetFactory;
import harpoon.Util.Collections.SetFactory;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * A <code>MayReadOracle</code> tells you which fields of a given
 * class 'may' be read on the given edge.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: MayReadOracle.java,v 1.2 2002-02-25 20:59:23 cananian Exp $
 */
public class MayReadOracle {
    final Map results = new HashMap();
    final DisjointSet aliasMap = new DisjointSet();

    /** Returns the set of fields possibly-read before edge <code>e</code>
     *  is executed.  The edge must belong to the <code>HCode</code>
     *  given to this class' constructor. */
    public Set mayReadAt(Edge e) {
	EdgeProp ep = (EdgeProp) results.get(aliasMap.find(e));
	Util.assert(ep!=null, e);
	return Collections.unmodifiableSet(ep.reads);
    }
    
    /** Creates a <code>MayReadOracle</code> using the quad-ssi representation
     *  <code>hc</code> of some method and global information about read fields
     *  provided by <code>fso</code>.  We will return results for only
     *  fields declared by class <code>which</code>, unless <code>which</code>
     *  is null, in which case we'll return results for all fields.
     *  If <code>specialHandlingForConstructors</code> is <code>true</code>,
     *  then when analyzing a constructor we will only consider fields of
     *  objects which *may* be <code>this</code> (parameter 0 of the
     *  constructor) as reads.  This is useful if we are trying to determine
     *  whether a field is read before it is defined in a constructor,
     *  for example.  With <code>specialHandlingForConstructors</code> equal
     *  to <code>false</code>, no reads are ignored.
     */
    public MayReadOracle(HCode hc, FieldSyncOracle fso, 
			 CallGraph cg, HClass which,
			 boolean specialHandlingForConstructors) {
	// we only need a mustparamoracle if special handling and hc
	// corresponds to a constructor.
	MustParamOracle mpo = null;
	if (specialHandlingForConstructors && isConstructor(hc.getMethod()))
	    mpo = new MustParamOracle(hc);
	// create new visitor
        ReadVisitor rv = new ReadVisitor(which, hc.getMethod(), fso, cg, mpo);
	// add HEADER quad to work list.
	rv.W.add(hc.getRootElement());
	// iterate until worklist is empty.
	while (!rv.W.isEmpty())
	    ((Quad)rv.W.pop()).accept(rv);
	// done!  all results are in rv.results map (w/ 'aliasMap')
    }
    /** 'reads' set of a edge, along with which 'next' quad to
     *  evaluate if/when the reads-set changes. */
    static class EdgeProp {
	final Set reads;
	Quad next; // updatable.
	EdgeProp(Set reads, Quad next) {
	    this.reads=reads; this.next=next;
	}
    }
    /** this is the visitor class which does the real work.
     *  Theory of operation: straight data-flow on edges.  If any
     *  quad modifies its outset, it adds the next quad to the worklist.
     *  We continue until worklist is empty.  To speed iteration, any
     *  quad which does not change the outset (i.e. the transfer function
     *  between its in-set and out-set is the identity function) is
     *  skipped over in subsequent passes by aliasing its in-edge to its
     *  out-edge. */
    class ReadVisitor extends QuadVisitor {
	final HClass declaringClass;
	final HMethod thisMethod;
	final WorkSet W = new WorkSet();
	final FieldSyncOracle fso;
	final MustParamOracle mpo; // only non-null if special handling.
	final CallGraph cg;
	final SetFactory sf = new AggregateSetFactory();
	// our task: given an inset, create an outset.
	// the inset corresponds to q.prev(x) (which is 'in')
	// filtered through the aliasMap (which is 'real').
	ReadVisitor(HClass declaringClass, HMethod thisMethod,
		    FieldSyncOracle fso, CallGraph cg, MustParamOracle mpo) {
	    this.declaringClass = declaringClass;
	    this.thisMethod = thisMethod;
	    this.fso = fso;
	    this.mpo = mpo;
	    this.cg = cg;
	}
	/** initialize the analysis.  HEADER generates our initial out-set. */
	public void visit(HEADER q) {
	    // no in-set.  empty out-set.  continue with METHOD (edge 1).
	    Edge out=q.nextEdge(1);
	    Util.assert(!results.containsKey(aliasMap.find(out)));
	    EdgeProp nep = new EdgeProp(Collections.EMPTY_SET, (Quad)out.to());
	    results.put(aliasMap.find(out), nep);
	    W.add(nep.next);
	}
	/** this method handles all 'no-change' in-to-out transfer functions.
	 *  we basically just map the in edge to the out edge, with some
	 *  magic involved to get the results maps and such to work out
	 *  right. */
	public void visit(Quad q) {
	    // one input, one output.  add alias from in to out edge.
	    Util.assert(q.prevLength()==1 && q.nextLength()==1);
	    Edge in = q.prevEdge(0), out=q.nextEdge(0);
	    // fetch inset and next.
	    EdgeProp oep = (EdgeProp) results.remove(aliasMap.find(in));
	    Util.assert(oep!=null);
	    // inset becomes outset (no change) but update 'next'.
	    EdgeProp nep = oep;
	    nep.next = q.next(0);
	    // merge 'out' with 'in'...
	    aliasMap.union(in, out);
	    // ...re-put outset.
	    results.put(aliasMap.find(out), nep);
	    // ta-da!
	    W.add(nep.next);
	}
	/** GET possibly adds a field to its out-set. */
	public void visit(GET q) {
	    // ignore if this get is not from the class we're interested in.
	    if (declaringClass != null &&
		!q.field().getDeclaringClass().equals(declaringClass)) {
		visit((Quad)q);
		return;
	    }
	    // outset = inset + this field.
	    boolean changed = false;
	    Edge in = q.prevEdge(0), out=q.nextEdge(0);
	    EdgeProp oep = (EdgeProp) results.get(aliasMap.find(in));
	    Util.assert(oep!=null);
	    EdgeProp nep = (EdgeProp) results.get(aliasMap.find(out));
	    if (nep==null) {
		nep = new EdgeProp(sf.makeSet(oep.reads), q.next(0));
		results.put(aliasMap.find(out), nep);
		changed = true;
	    }
	    if (isOkay(q) && nep.reads.add(q.field()))
		changed = true;
	    // if something changed, add nep.next to work set.
	    if (changed)
		W.add(nep.next);
	}
	/** this implements the 'special handling for constructors' */
	boolean isOkay(GET q) {
	    if (mpo==null) return true; // always okay if not special handling
	    return mpo.isMustParam(q.objectref()) &&
		mpo.whichMustParam(q.objectref())==0;
	}
	/** A CALL adds all fields possibly-read by the methods
	 *  possibly-called by it to its out-set. */
	public void visit(CALL q) {
	    Edge in = q.prevEdge(0);
	    EdgeProp oep = (EdgeProp) results.get(aliasMap.find(in));
	    Util.assert(oep!=null);
	    // find methods callable from q.
	    HMethod[] callable = cg.calls(thisMethod, q);
	    // any any fields which *may* be read by one of these methods.
	    Set mayRead = new HashSet();
	    for (int i=0; i<callable.length; i++)
		mayRead.addAll(fso.fieldsRead(callable[i]));
	    // filter out the fields we're not interested in.
	    if (declaringClass!=null)
		for (Iterator it=mayRead.iterator(); it.hasNext(); )
		    if (!((HField)it.next()).getDeclaringClass()
			.equals(declaringClass))
			it.remove();
	    // add to all out-sets
	    for (int i=0; i<q.nextLength(); i++) {
		boolean changed = false;
		Edge out = q.nextEdge(i);
		EdgeProp nep = (EdgeProp) results.get(aliasMap.find(out));
		if (nep==null) {
		    nep = new EdgeProp(sf.makeSet(oep.reads), (Quad)out.to());
		    results.put(aliasMap.find(out), nep);
		    changed=true;
		}
		if (nep.reads.addAll(mayRead))
		    changed=true;
		if (changed)
		    W.add(nep.next);
	    }
	}
	// we could refactor both the PHI and SIGMA *and* one-in-one-out
	// non-GET cases together into one method, but I think it's
	// easier to read if we keep them a *little* separate, at least.
	// I *did* combine PHI and SIGMA into the following method,
	// which deals with both.  all out-sets get all in-sets added
	// to them.
	void visitPhiSigma(Quad q) {
	    // for all 'out' edges...
	    for (int i=0; i<q.nextLength(); i++) {
		boolean changed=false;
		// get current out-set.
		Edge out = q.nextEdge(i);
		EdgeProp nep = (EdgeProp) results.get(aliasMap.find(out));
		// merge over all 'in' edges...
		for (int j=0; j<q.prevLength(); j++) {
		    // get current in-set.
		    Edge in = q.prevEdge(j);
		    EdgeProp oep = (EdgeProp) results.get(aliasMap.find(in));
		    if (oep==null) {
			Util.assert(q.prevLength()>1); // only for PHIs.
			continue; // skip this edge; nothing yet known.
		    }
		    if (nep==null) {
			// if we don't have an out-set yet, create one.
			// the one-in-one-out non-quad special case
			// would go in here.  but we're going to keep
			// this clean.
			changed=true;
			// (new out set with 'reads' of the in-set.
			nep = new EdgeProp(sf.makeSet(oep.reads),
					   (Quad) out.to());
			results.put(aliasMap.find(out), nep);
		    } else if (nep.reads.addAll(oep.reads))
			// we added stuff to the out-set.
			changed = true;
		}
		// if anything changed, add our successor to the worklist.
		if (changed)
		    W.add(nep.next);
	    }
	}
	public void visit(PHI q) { visitPhiSigma(q); }
	public void visit(SIGMA q) { visitPhiSigma(q); }

	// RETURN and THROW are the end of the line.  no out-set.
	public void visit(RETURN q) { /* do nothing */ }
	public void visit(THROW q) { /* do nothing */ }
    }

    /** return a conservative approximation to whether this is a constructor
     *  or not.  it's always safe to return true. */
    private static boolean isConstructor(HMethod hm) {
	// this is tricky, because we want split constructors to count, too,
	// even though renamed constructors (such as generated by initcheck,
	// for instance) won't always be instanceof HConstructor.  Look
	// for names starting with '<init>', as well.
	if (hm instanceof HConstructor) return true;
	if (hm.getName().startsWith("<init>")) return true;
	// XXX: what about methods generated by RuntimeMethod Cloner?
	// we could try methods ending with <init>, but then the
	// declaringclass information would be wrong.
	//if (hm.getName().endsWidth("<init>")) return true;//not safe yet.
	return false;
    }
}
