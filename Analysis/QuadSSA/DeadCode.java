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
 * <code>DeadCode</code> removes dead code (unused definitions) from
 * a method.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: DeadCode.java,v 1.4 1998-09-23 19:42:53 cananian Exp $
 */

public class DeadCode  {
    /** Hide the constructor. */
    private DeadCode() { }

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

    private static void markAbsent(Temp t, Worklist W, 
			    IntTable uses, Hashtable defs) {
	int n = uses.getInt(t)-1;
	uses.putInt(t, n);
	Util.assert(n >= 0);
	if (n==0 && defs.get(t) != null)// protect against undefined variables.
	    W.push(defs.get(t));
    }

    static void removePhis(HCode hc) {
	for (Enumeration e = hc.getElementsE(); e.hasMoreElements(); ) {
	    Quad q = (Quad) e.nextElement();
	    if (q instanceof PHI && q.prev().length==1) {
		PHI Q = (PHI) q;
		Edge predE = q.prevEdge(0);
		Quad header = (Quad)predE.from();
		int  which_succ =   predE.which_succ();
		// make MOVE chain.
		for (int i=0; i < Q.dst.length; i++) {
		    Quad qq = new MOVE(Q.getSourceElement(),
				       Q.dst[i], Q.src[i][0]);
		    Quad.addEdge(header, which_succ, qq, 0);
		    header = qq; which_succ = 0;
		}
		// now link PHI out of the graph.
		Edge succE = Q.nextEdge(0);
		Quad.addEdge(header, which_succ, 
			     (Quad) succE.to(), succE.which_pred());
	    }
	}
    }

    public static void optimize(HCode hc) {
	Hashtable defs = new Hashtable();
	IntTable uses = new IntTable();
	Worklist W = new Set();

	// get rid of arity-1 phi functions.
	removePhis(hc);

	// collect uses/defs
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
	}
	// now examine each defining statement to see whether it is used.
	while (!W.isEmpty()) {
	    Quad q = (Quad) W.pull();
	    Temp d[] = q.def();
	    // statements that define no variables are safe.
	    if (d.length==0) continue;
	    // don't throw our headers away!!
	    else if (q instanceof METHODHEADER) continue;
	    // call statements may have side-effects.
	    else if (q instanceof CALL) continue;
	    else if (q instanceof PHI) { // phis are specially compound.
		PHI Q = (PHI) q;
		// check each function in a PHI
		for (int i=0; i < Q.dst.length; i++) {
		    if (uses.getInt(Q.dst[i]) > 0) continue;
		    // erase this phi! (fun for the whole family)
		    //   decrement uses of the phi args.
		    for (int j=0; j < Q.src[i].length; j++)
			markAbsent(Q.src[i][j], W, uses, defs);
		    //   shrink the phi function (secret headhunter's ritual)
		    Q.dst = (Temp[])   Util.shrink(Q.dst, i);
		    Q.src = (Temp[][]) Util.shrink(Q.src, i);
		    //   decrement i so we're at the right place still.
		    i--;
		}
	    }
	    else if (q instanceof SIGMA) { // sigmas are specially compound.
		SIGMA Q = (SIGMA) q;
		// check each function in the sigma
		L1: for (int i=0; i < Q.dst.length; i++) {
		    // if any variable in this sigma is used, skip it.
		    for (int j=0; j < Q.dst[i].length; j++)
			if (uses.getInt(Q.dst[i][j]) > 0) continue L1;
		    // ok.  no used variables found.  ERASER MAN appears.
		    //   decrement uses of the sigma source.
		    markAbsent(Q.src[i], W, uses, defs);
		    //   shrink the sigma function in our secret laboratory.
		    Q.dst = (Temp[][]) Util.shrink(Q.dst, i);
		    Q.src = (Temp[])   Util.shrink(Q.src, i);
		    // decrement index so we're still at the right place.
		    i--;
		}
	    } else { // an ordinary statement, ripe for plucking.
		int i; for (i=0; i<d.length; i++)
		    if (uses.getInt(d[i])>0)
			break;
		if (i<d.length) continue; // some of the defs are used.
		// okay.  This statement is worthless.  Unlink it.
		Util.assert(q.next().length==1 && q.prev().length==1);
		Edge before = q.prevEdge(0);
		Edge after  = q.nextEdge(0);
		Quad.addEdge((Quad)before.from(), before.which_succ(),
			     (Quad)after.to(), after.which_pred() );
		// decrement uses & possible add to worklist.
		Temp u[] = q.use();
		for (i=0; i<u.length; i++)
		    markAbsent(u[i], W, uses, defs);
	    }
	} // end WHILE WORKLIST NOT EMPTY
    } // end OPTIMIZE METHOD
}
