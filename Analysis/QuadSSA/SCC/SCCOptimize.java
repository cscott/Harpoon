// SCCOptimize.java, created Sun Sep 20 21:41:44 1998 by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.QuadSSA.SCC;

import harpoon.Analysis.Maps.ConstMap;
import harpoon.Analysis.Maps.ExecMap;
import harpoon.Analysis.Maps.TypeMap;
import harpoon.Analysis.Maps.UseDefMap;
import harpoon.Analysis.QuadSSA.DeadCode;
import harpoon.ClassFile.*;
import harpoon.IR.Quads.*;
import harpoon.Temp.Temp;
import harpoon.Util.Set;
import harpoon.Util.HashSet;
import harpoon.Util.Util;
/**
 * <code>SCCOptimize</code> optimizes the code after <code>SCCAnalysis</code>.
 * The optimization invalidates the <code>ExecMap</code> used.
 * All edges in the graph after optimization are executable.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: SCCOptimize.java,v 1.5.2.10 1999-08-09 20:26:24 duncan Exp $
 */
public final class SCCOptimize {
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
    boolean execMap(HCode hc, HCodeEdge e) {
	if (Ee.contains(e)) return true;
	else return em.execMap(hc, e);
    }
    boolean execMap(HCode hc, HCodeElement node) {
	HCodeEdge[] pred = ((harpoon.IR.Properties.HasEdges)node).pred();
	for (int i=0; i<pred.length; i++)
	    if (execMap(hc, pred[i]))
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
		    if (!cm.isConst(hc, d[i]))
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
				       cm.constMap(hc, d[i]),
				       ti.typeMap(q, d[i]) );
		    Quad.addEdge(header, which_succ, qq, 0);
		    Ee.union(header.nextEdge(which_succ));
		    header = qq; which_succ = 0;
		}
		// link to successor.
		Quad.addEdge(header, which_succ, successor, which_pred);
		Ee.union(header.nextEdge(which_succ));
		// done.
	    } // END VISIT quad.
	    public void visit(CONST q) { /* do nothing. */ }
	    public void visit(FOOTER q) {
		// remove unexecutable FOOTER edges.
		FOOTER newF = q;
		Edge[] prv = q.prevEdge();
		for (int i=prv.length-1; i>=0; i--)
		    if (!execMap(hc, prv[i]))
			newF = newF.remove(i);
		// add new executable edges to set.
		for (int i=0; i<newF.prevLength(); i++)
		    Ee.union(newF.prevEdge(i));
	    }
	    public void visit(SIGMA q) {
		// if the condition is constant, link this sigma (cjmp/switch)
		// out of existence.  Either all or exactly one edge will be
		// executable.
		Edge[] next = q.nextEdge();
		int i; for (i=0; i < next.length; i++)
		    if (execMap(hc, next[i]))
			break;
		if (i==next.length-1 || !execMap(hc, next[i+1])) {
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
			Ee.union(header.nextEdge(which_succ));
			header = qq; which_succ = 0;
		    }
		    // link to successor.
		    Quad.addEdge(header, which_succ, successor, which_pred);
		    Ee.union(header.nextEdge(which_succ));
		}
	    } // end VISIT SIGMA
	    public void visit(PHI q) {
		// remove non-executable edges into this PHI.
		for (int i=0; i < q.prev().length; ) {
		    if (!execMap(hc, q.prevEdge(i)))
			q.removePred(i);
		    else i++;
		}
		// replace any phi's with constant args with a CONST.
		for (int i=0; i < q.numPhis(); ) {
		    if (cm.isConst(hc, q.dst(i))) {
			// insert CONST.
			Quad qq = newCONST(q.getFactory(), q, q.dst(i), 
					   cm.constMap(hc, q.dst(i)),
					   ti.typeMap(q, q.dst(i)) );
			Edge edge = q.nextEdge(0);
			Quad.addEdge(qq, 0,(Quad)edge.to(), edge.which_pred());
			Quad.addEdge(q, 0, qq, 0);
			Ee.union(q.nextEdge(0));
			Ee.union(qq.nextEdge(0));
			q.removePhi(i); // remove i'th phi function.
		    } else i++;
		}
	    } // end VISIT PHI.
	};
	
	// actual traversal code.
	Quad[] ql = (Quad[]) hc.getElements();
	for (int i=0; i<ql.length; i++)
	    if (execMap(hc, ql[i]))
		ql[i].visit(visitor);

	// clean up the mess
	DeadCode.optimize(hc);
    }
}
