// DeadCode.java, created Mon Sep 21 15:36:01 1998 by cananian
package harpoon.Analysis.QuadSSA;

import harpoon.ClassFile.*;
import harpoon.IR.QuadSSA.*;
import harpoon.Temp.Temp;
import harpoon.Util.Set;
import harpoon.Util.Util;
import harpoon.Util.Worklist;

import java.util.Enumeration;
import java.util.Hashtable;
/**
 * <code>DeadCode</code> removes dead code 
 * (unused definitions/useless jmps/one-argument phi functions) from
 * a method.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: DeadCode.java,v 1.7 1998-09-25 20:12:42 cananian Exp $
 */

public class DeadCode  {
    /** Hide the constructor. */
    private DeadCode() { }

    public static void optimize(HCode hc) {
	Hashtable defs = new Hashtable();
	IntTable uses = new IntTable();
	Set W = new Set();

	// make visitor
	Visitor v = new Visitor(W, uses, defs);

	// collect uses/defs; construct initial worklist.
	for (Enumeration e = hc.getElementsE(); e.hasMoreElements(); ) {
	    Quad q = (Quad) e.nextElement();
	    // locate defs
	    Temp[] d = q.def();
	    for (int i=0; i<d.length; i++)
		defs.put(d[i], q);
	    // all defining statements put on worklist.
	    if (d.length>0)
		W.push(q);
	    // count uses
	    Temp[] u = q.use();
	    for (int i=0; i<u.length; i++)
		uses.putInt(u[i], uses.getInt(u[i])+1);
	    
	    // all phis and cjmps are put on worklist, too.
	    if (q instanceof PHI || q instanceof CJMP)
		W.push(q);
	}

	// now examine each defining statement to see whether it is used.
	while (!W.isEmpty()) {
	    Quad q = (Quad) W.pull();
	    q.visit(v);
	} // end WHILE WORKLIST NOT EMPTY
    } // end OPTIMIZE METHOD

    static class Visitor extends QuadVisitor {
	Set W;
	IntTable uses;
	Hashtable defs;
	Visitor(Set W, IntTable uses, Hashtable defs) {
	    this.W = W;
	    this.uses = uses;
	    this.defs = defs;
	}
	public void visit(METHODHEADER q) 
	{ /* don't throw our headers away!! */ }
	public void visit(CALL q) 
	{ /* call statements may have side-effects. */ }
	public void visit(CJMP q) {
	    // if this is a useless cjmp remove it; otherwise 
	    // do the standard sigma optimizations on it.
	    if (q.next(0) == q.next(1) && removeCJMP(q)) return;
	    visit((SIGMA)q);
	}

	public void visit(PHI q) { // phis are specially compound.
	    // arity-one phi functions are stomped on.
	    if (q.prev().length==1) { removePhi(q); return; }
	    // check each function in a PHI
	    for (int i=0; i < q.dst.length; i++) {
		if (uses.getInt(q.dst[i]) > 0) continue;
		// erase this phi! (fun for the whole family)
		//   decrement uses of the phi args.
		for (int j=0; j < q.src[i].length; j++)
		    markAbsent(q.src[i][j]);
		//   shrink the phi function (secret headhunter's ritual)
		q.dst = (Temp[])   Util.shrink(q.dst, i);
		q.src = (Temp[][]) Util.shrink(q.src, i);
		//   decrement i so we're at the right place still.
		i--;
	    }
	}
	public void visit(SIGMA q) { // sigmas are specially compound.
	    // check each function in the sigma
	L1:
	    for (int i=0; i < q.dst.length; i++) {
		// if any variable in this sigma is used, skip it.
		for (int j=0; j < q.dst[i].length; j++)
		    if (uses.getInt(q.dst[i][j]) > 0) continue L1;
		// ok.  no used variables found.  ERASER MAN appears.
		//   decrement uses of the sigma source.
		markAbsent(q.src[i]);
		//   shrink the sigma function in our secret laboratory.
		q.dst = (Temp[][]) Util.shrink(q.dst, i);
		q.src = (Temp[])   Util.shrink(q.src, i);
		// decrement index so we're still at the right place.
		i--;
	    }
	}
	public void visit(Quad q) { // ordinary statements, ripe for plucking.
	    Temp d[] = q.def();
	    // statements that define no variables are safe.
	    if (d.length==0) return;

	    int i; for (i=0; i<d.length; i++)
		if (uses.getInt(d[i])>0)
		    break;
	    if (i<d.length) return; // some of the defs are used.
	    // okay.  This statement is worthless.  
	    // removing statement could make predecessor useless
	    if (q.prev(0) instanceof CJMP) W.push(q.prev(0));
	    // Unlink it.
	    Util.assert(q.next().length==1 && q.prev().length==1);
	    Edge before = q.prevEdge(0);
	    Edge after  = q.nextEdge(0);
	    Quad.addEdge((Quad)before.from(), before.which_succ(),
			 (Quad)after.to(), after.which_pred() );
	    // decrement uses & possibly add to worklist.
	    Temp u[] = q.use();
	    for (i=0; i<u.length; i++)
		markAbsent(u[i]);
	}

	void removePhi(PHI q) {
	    Util.assert(q.prev().length==1);
	    Edge predE = q.prevEdge(0);
	    Quad header = (Quad)predE.from();
	    int  which_succ =   predE.which_succ();
	    // make MOVE chain.
	    for (int i=0; i < q.dst.length; i++) {
		Quad qq = new MOVE(q.getSourceElement(),
				   q.dst[i], q.src[i][0]);
		Quad.addEdge(header, which_succ, qq, 0);
		header = qq; which_succ = 0;
		// update definition pointer; push new MOVEs on worklist.
		defs.put(q.dst[i], qq);
		W.push(qq);
	    }
	    // removing this node could have made a CJMP useless.
	    if (q.dst.length==0 && q.prev(0) instanceof CJMP)
		W.push(q.prev(0));
	    // now link PHI out of the graph.
	    Edge succE = q.nextEdge(0);
	    Quad.addEdge(header, which_succ, 
			 (Quad) succE.to(), succE.which_pred());
	    W.remove(q); // can't optimize this any more.
	}
	boolean removeCJMP(CJMP q) { // remove useless jumps.
	    Util.assert(q.next(0)==q.next(1));
	    // if the targets are the same, it ought to be a phi.
	    Util.assert(q.next(0) instanceof PHI);

	    PHI phi = (PHI) q.next(0);
	    // check that sigma and phi args match up.
	    // a hashtable makes this easier.
	    Hashtable h = new Hashtable();
	    for (int i=0; i<q.dst.length; i++)
		for (int j=0; j<q.dst[i].length; j++)
		    h.put(q.dst[i][j], q.src[i]);

	    int which_pred0 = q.nextEdge(0).which_pred();
	    int which_pred1 = q.nextEdge(1).which_pred();
	    for (int i=0; i<phi.src.length; i++)
		if (h.get(phi.src[i][which_pred0]) !=
		    h.get(phi.src[i][which_pred1]) )
		    return false; // cjmp matters, either in sigma or in phi.
	    
	    // we've proven that removing this CJMP is harmless. Remove it.
	    // Removing the CJMP could make a predecessor useless.
	    if (q.prev(0) instanceof CJMP) W.push(q.prev(0));
	    // Remove one of the phi entries & put phi on worklist
	    phi.remove(which_pred1);
	    W.push(phi); // might be arity 1 now.
	    int which_pred = q.nextEdge(0).which_pred(); // diff from wp0

	    // update phi args.
	    for (int i=0; i<phi.src.length; i++) {
		Temp t = (Temp) h.get(phi.src[i][which_pred]);
		if (t!=null) { phi.src[i][which_pred] = t; }
	    }

	    // link out CJMP
	    Quad.addEdge(q.prev(0), q.prevEdge(0).which_succ(), 
			 phi, which_pred);
	    markAbsent(q.test);
	    W.remove(q); // can't optimize this any more.
	    return true;
	}

	void markAbsent(Temp t) {
	    int n = uses.getInt(t)-1;
	    uses.putInt(t, n);
	    Util.assert(n >= 0);
	    // protect against undefined variables:
	    if (n==0 && defs.get(t) != null)
		W.push(defs.get(t));
	}
    }

    // utility class.
    static class IntTable extends Hashtable {
	int putInt(Object o, int v) {
	    Integer I = (v==0) ?
		(Integer) remove(o) :
		(Integer) put(o, new Integer(v));
	    return (I==null)?0:I.intValue();
	}
	int getInt(Object o) {
	    Integer I = (Integer) get(o);
	    return (I==null)?0:I.intValue();
	}
    }
}
