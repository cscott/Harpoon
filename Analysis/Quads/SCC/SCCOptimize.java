// SCCOptimize.java, created Sun Sep 20 21:41:44 1998 by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.Quads.SCC;

import harpoon.Analysis.Maps.ConstMap;
import harpoon.Analysis.Maps.ExecMap;
import harpoon.Analysis.Maps.TypeMap;
import harpoon.Analysis.Maps.UseDefMap;
import harpoon.Analysis.Quads.DeadCode;
import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HCode;
import harpoon.ClassFile.HCodeEdge;
import harpoon.ClassFile.HCodeElement;
import harpoon.ClassFile.HCodeFactory;
import harpoon.ClassFile.HMethod;
import harpoon.IR.Quads.CONST;
import harpoon.IR.Quads.Edge;
import harpoon.IR.Quads.FOOTER;
import harpoon.IR.Quads.METHOD;
import harpoon.IR.Quads.MOVE;
import harpoon.IR.Quads.PHI;
import harpoon.IR.Quads.Quad;
import harpoon.IR.Quads.QuadFactory;
import harpoon.IR.Quads.QuadVisitor;
import harpoon.IR.Quads.SIGMA;
import harpoon.IR.Quads.TYPESWITCH;
import harpoon.Temp.Temp;
import harpoon.Util.Util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
/**
 * <code>SCCOptimize</code> optimizes the code after <code>SCCAnalysis</code>.
 * The optimization invalidates the <code>ExecMap</code> used.
 * All edges in the graph after optimization are executable.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: SCCOptimize.java,v 1.1.2.10 2001-07-20 03:30:36 cananian Exp $
 */
public final class SCCOptimize implements ExecMap {
    TypeMap  ti;
    ConstMap cm;
    ExecMap  em;
    
    /** Creates an <code>SCCOptimize</code>. */
    public SCCOptimize(TypeMap ti, ConstMap cm, ExecMap em) {
	this.ti = ti;
	this.cm = cm;
	this.em = em;
    }
    public SCCOptimize(SCCAnalysis scc) { this(scc, scc, scc); }

    /** Returns a code factory that uses SCCOptimize. */
    public static HCodeFactory codeFactory(final HCodeFactory parent) {
	return new HCodeFactory() {
	    public HCode convert(HMethod m) {
		HCode hc = parent.convert(m);
		if (hc!=null) {
		    harpoon.Analysis.UseDef ud = new harpoon.Analysis.UseDef();
		    (new SCCOptimize(new SCCAnalysis(hc, ud))).optimize(hc);
		}
		return hc;
	    }
	    public String getCodeName() { return parent.getCodeName(); }
	    public void clear(HMethod m) { parent.clear(m); }
	};
    }

    Set Ee = new HashSet();
    public boolean execMap(HCodeEdge e) {
	if (Ee.contains(e)) return true;
	else return em.execMap(e);
    }
    public boolean execMap(HCodeElement node) {
	HCodeEdge[] pred = ((harpoon.IR.Properties.CFGraphable)node).pred();
	for (int i=0; i<pred.length; i++)
	    if (execMap(pred[i]))
		return true;
	return false;
    }
    
    // Utility class
    private CONST newCONST(QuadFactory qf, HCodeElement source, 
			   Temp dst, Object val, HClass type) {
	if (type==HClass.Boolean) type=HClass.Int;
	return new CONST(qf, source, dst, val, type);
    }

    public void optimize(final HCode hc) {
    
	QuadVisitor visitor = new QuadVisitor() {
	    public void visit(Quad q) {
		// if all defs are constants, replace the statement
		// with a series of CONSTs.
		Temp d[] = q.def();
		if (d.length == 0) return; // nothing to do here.
		int i; for (i=0; i<d.length; i++)
		    if (!cm.isConst(q, d[i]))
			break;
		if (i!=d.length) // not all args are constant
		    return;

		// ok.  Replace with a series of CONSTs.
		Util.assert(q.next().length==1 && q.prev().length==1);
		Quad header = q.prev(0);
		int which_succ = q.prevEdge(0).which_succ();
		Quad successor = q.next(0);
		int which_pred = q.nextEdge(0).which_pred();

		for (i=0; i<d.length; i++) {
		    Quad qq = newCONST(q.getFactory(), q, d[i],
				       cm.constMap(q, d[i]),
				       ti.typeMap(q, d[i]) );
		    Quad.addEdge(header, which_succ, qq, 0);
		    Ee.add(header.nextEdge(which_succ));
		    header = qq; which_succ = 0;
		}
		// link to successor.
		Quad.addEdge(header, which_succ, successor, which_pred);
		Ee.add(header.nextEdge(which_succ));
		// done.
	    } // END VISIT quad.
	    public void visit(CONST q) { /* do nothing. */ }
	    public void visit(METHOD q) { /* do nothing. */ }
	    public void visit(FOOTER q) {
		// remove unexecutable FOOTER edges.
		FOOTER newF = q;
		Edge[] prv = q.prevEdge();
		for (int i=prv.length-1; i>=0; i--)
		    if (!execMap(prv[i]))
			newF = newF.remove(i);
		// add new executable edges to set.
		for (int i=0; i<newF.prevLength(); i++)
		    Ee.add(newF.prevEdge(i));
	    }
	    public void visit(TYPESWITCH q) {
		/* multiple edges of this SIGMA may be executable */
		List keylist = new ArrayList(q.arity());
		List edgelist = new ArrayList(q.arity());
		// collect executable edges.
		for (int i=0; i < q.arity(); i++)
		    if (execMap(q.nextEdge(i))) {
			if (i<q.keysLength())
			    keylist.add(q.keys(i));
			edgelist.add(q.nextEdge(i));
		    }
		// default edge may not be executable.
		boolean hasDefault = !(keylist.size() == edgelist.size());
		// make new keys and edge array.
		HClass[] nkeys =
		    (HClass[]) keylist.toArray(new HClass[keylist.size()]);
		Edge[] edges =
		    (Edge[]) edgelist.toArray(new Edge[edgelist.size()]);
		// make new dst[][] array for sigmas
		Temp[][] ndst = new Temp[q.numSigmas()][edgelist.size()];
		for (int i=0; i < q.numSigmas(); i++)
		    for (int j=0; j < edges.length; j++)
			ndst[i][j] = q.dst(i, edges[j].which_succ());
		// make new TYPESWITCH
		TYPESWITCH nts = new TYPESWITCH(q.getFactory(), q, q.index(),
						nkeys, ndst, q.src(),
						hasDefault);
		// and link the new TYPESWITCH.
		Edge pedge = q.prevEdge(0);
		Quad.addEdge((Quad)pedge.from(), pedge.which_succ(), nts, 0);
		Ee.add(nts.prevEdge(0));
		for (int i=0; i < edges.length; i++) {
		    Quad.addEdge(nts, i,
				 (Quad) edges[i].to(), edges[i].which_pred());
		    Ee.add(nts.nextEdge(i));
		}
		// visit(SIGMA) to trim out TYPESWITCH iff only one edge is
		// executable
		// (no-default typeswitch with one edge is an *assertion*.
		//  we don't want to delete it.)
		if (hasDefault) visit((SIGMA)nts);
		// ta-da!
	    }
	    public void visit(SIGMA q) {
		// if the condition is constant, link this sigma (cjmp/switch)
		// out of existence.  Either all or exactly one edge will be
		// executable.
		Edge[] next = q.nextEdge();
		int i; for (i=0; i < next.length; i++)
		    if (execMap(next[i]))
			break;
		Util.assert(i!=next.length, q/*NO EDGES EXECUTABLE!*/);
		if (i==next.length-1 || !execMap(next[i+1])) {
		    // only one edge is executable.
		    int liveEdge = i;

		    // Grab the link information from the original CJMP/SWITCH.
		    Quad header = q.prev(0);
		    int which_succ = q.prevEdge(0).which_succ();
		    Quad successor = q.next(liveEdge);
		    int which_pred = q.nextEdge(liveEdge).which_pred();

		    // insert a series of MOVEs to implement SIGMAs
		    for (i=0; i < q.numSigmas(); i++) {
			Quad qq = new MOVE(q.getFactory(), q,
					   q.dst(i,liveEdge), q.src(i));
			Quad.addEdge(header, which_succ, qq, 0);
			Ee.add(header.nextEdge(which_succ));
			header = qq; which_succ = 0;
		    }
		    // link to successor.
		    Quad.addEdge(header, which_succ, successor, which_pred);
		    Ee.add(header.nextEdge(which_succ));
		}
	    } // end VISIT SIGMA
	    public void visit(PHI q) {
		// remove non-executable edges into this PHI.
		for (int i=0; i < q.prev().length; ) {
		    if (!execMap(q.prevEdge(i)))
			q.removePred(i);
		    else i++;
		}
		// replace any phi's with constant args with a CONST.
		for (int i=0; i < q.numPhis(); ) {
		    if (cm.isConst(q, q.dst(i))) {
			// insert CONST.
			Quad qq = newCONST(q.getFactory(), q, q.dst(i), 
					   cm.constMap(q, q.dst(i)),
					   ti.typeMap(q, q.dst(i)) );
			Edge edge = q.nextEdge(0);
			Quad.addEdge(qq, 0,(Quad)edge.to(), edge.which_pred());
			Quad.addEdge(q, 0, qq, 0);
			Ee.add(q.nextEdge(0));
			Ee.add(qq.nextEdge(0));
			q.removePhi(i); // remove i'th phi function.
		    } else i++;
		}
	    } // end VISIT PHI.
	};
	
	// actual traversal code.
	Quad[] ql = (Quad[]) hc.getElements();
	for (int i=0; i<ql.length; i++)
	    if (execMap(ql[i]))
		ql[i].accept(visitor);

	// clean up the mess
	DeadCode.optimize((harpoon.IR.Quads.Code)hc,
			  null /* throw away AllocationInformation */);
    }
}
