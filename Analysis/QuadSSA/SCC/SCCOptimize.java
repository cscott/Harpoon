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
import harpoon.IR.QuadSSA.*;
import harpoon.Temp.Temp;
import harpoon.Util.Set;
import harpoon.Util.Util;
/**
 * <code>SCCOptimize</code> optimizes the code after SCCAnalysis.
 * The optimization invalidates the ExecMap used.  All edges in the
 * graph after optimization are executable.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: SCCOptimize.java,v 1.5 1998-10-11 02:37:08 cananian Exp $
 */
public class SCCOptimize {
    TypeMap  ti;
    ConstMap cm;
    ExecMap  em;
    
    /** Creates a <code>SCCOptimize</code>. */
    public SCCOptimize(TypeMap ti, ConstMap cm, ExecMap em) {
	this.ti = ti;
	this.cm = cm;
	this.em = em;
    }

    Set Ee = new Set();
    boolean execMap(HCode hc, HCodeEdge e) {
	if (Ee.contains(e)) return true;
	else return em.execMap(hc, e);
    }
    boolean execMap(HCode hc, HCodeElement node) {
	HCodeEdge[] pred = ((harpoon.IR.Properties.Edges)node).pred();
	for (int i=0; i<pred.length; i++)
	    if (execMap(hc, pred[i]))
		return true;
	return false;
    }
    
    // Utility class
    private CONST newCONST(HCodeElement source, 
			   Temp dst, Object val, HClass type) {
	if (type==HClass.Boolean) type=HClass.Int;
	return new CONST(source, dst, val, type);
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
		    Quad qq = newCONST(q.getSourceElement(), d[i],
				       cm.constMap(hc, d[i]),
				       ti.typeMap(hc, d[i]) );
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
		for (int i=0; i < q.prev().length; )
		    if (!execMap(hc, q.prevEdge(i)))
			q.remove(i);
		    else i++;
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
		    for (i=0; i < q.src.length; i++) {
			Quad qq = new MOVE(q.getSourceElement(),
					   q.dst[i][liveEdge], q.src[i]);
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
			q.remove(i);
		    else i++;
		}
		// replace any phi's with constant args with a CONST.
		for (int i=0; i < q.dst.length; ) {
		    if (cm.isConst(hc, q.dst[i])) {
			// insert CONST.
			Quad qq = newCONST(q.getSourceElement(), q.dst[i], 
					   cm.constMap(hc, q.dst[i]),
					   ti.typeMap(hc, q.dst[i]) );
			Edge edge = q.nextEdge(0);
			Quad.addEdge(qq, 0,(Quad)edge.to(), edge.which_pred());
			Quad.addEdge(q, 0, qq, 0);
			Ee.union(q.nextEdge(0));
			Ee.union(qq.nextEdge(0));
			// remove phi.
			q.dst = (Temp[])   Util.shrink(q.dst, i);
			q.src = (Temp[][]) Util.shrink(q.src, i);
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
