// CleanUp.java, created Sat Sep 12 20:23:54 1998 by cananian
package harpoon.IR.QuadSSA;

import java.util.Hashtable;
import java.util.Enumeration;

import harpoon.ClassFile.*;
import harpoon.Temp.Temp;
import harpoon.Temp.TempMap;
import harpoon.Util.Util;
import harpoon.Util.Set;
import harpoon.Util.Worklist;

/**
 * <code>CleanUp</code> cleans up the phi functions of the IR generated
 * by the <code>Translate</code> class.<p>
 * It: <UL>
 * <LI> Removes phi/lambda functions that define temps that are never used
 *      (which magically removes undefined temps as well).
 * <LI> Shrinks phi functions that have phantom limbs 
 *      (from impossible catches).
 * </UL>
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: CleanUp.java,v 1.5 1998-09-16 15:57:19 cananian Exp $
 * @see Translate
 */

class CleanUp  {
    static void cleanup1(Code c) {
	cleanupPhantomLimbs((Quad[]) c.getElements());
    }
    static void cleanup2(HCode c) {
	cleanupUnused((Quad[]) c.getElements());
    }
    private static void cleanupPhantomLimbs(Quad[] ql) {
	// iterate over all phis.
	for (int i=0; i<ql.length; i++) {
	    if (! (ql[i] instanceof PHI) ) continue;
	    PHI phi = (PHI) ql[i];
	    // shrink phi functions with null limbs.
	    for (int j=0; j<phi.prev.length; )
		if (phi.prev[j]==null)
		    phi.remove(j);
		else j++;
	}
    }

    static class UsedTable {
	Hashtable h = new Hashtable();

	void inc(Temp t, Quad q) {
	    Set s = (Set) h.get(t);
	    if (s==null) { s = new Set(); h.put(t, s); }
	    s.push(q);
	}
	int uses(Temp t) {
	    Set s = (Set) h.get(t);
	    return (s==null)?0:s.size();
	}
	Enumeration dec(Temp t, Quad q) {
	    Set s = (Set) h.get(t);
	    if (s==null) 
		return new Enumeration() {
		    public boolean hasMoreElements() { return false; }
		    public Object nextElement() { return null; }
	        };
	    s.remove(q);
	    if (s.size()==0) h.remove(t);
	    return s.elements();
	}
    }

    private static void cleanupUnused(Quad[] ql) {
	// collect all the temps that are used somewhere reachable.
	UsedTable used = new UsedTable();
	for (int i=0; i<ql.length; i++) {
	    Temp[] u = ql[i].use();
	    for (int j=0; j<u.length; j++)
		used.inc(u[j], ql[i]);
	}
	// put all phis and lambdas on a worklist.
	Worklist W = new Set();
	for (int i=0; i<ql.length; i++)
	    if (ql[i] instanceof PHI || ql[i] instanceof LAMBDA)
		W.push(ql[i]);
	
	// now iterate until we've removed all the phis/lambdas we can.
	while (!W.isEmpty()) {
	    Quad q = (Quad) W.pull();
	    if (q instanceof PHI) {
		PHI phi = (PHI) q;
		// remove unused phi functions.
		for (int j=0; j<phi.dst.length; )
		    if (used.uses(phi.dst[j])==0) {
			// decrement the uses of the phi args.
			for (int k=0; k<phi.src[j].length; k++)
			    for (Enumeration e=used.dec(phi.src[j][k], phi);
				 e.hasMoreElements(); )
				// and push nodes on the list to be examined.
				W.push(e.nextElement());
			// shrink the actual phi function.
			phi.dst = (Temp[]) Util.shrink(phi.dst, j);
			phi.src = (Temp[][]) Util.shrink(phi.src, j);
		    } else j++;
	    } else if (q instanceof LAMBDA) {
		LAMBDA lambda = (LAMBDA) q;
		// an unused phi function has no used destinations.
		for (int j=0; j<lambda.dst.length; ) {
		    int k; for (k=0; k < lambda.dst[j].length; k++)
			if (used.uses(lambda.dst[j][k]) != 0)
			    break;
		    if (k==lambda.dst[j].length) { // no used variables found.
			// decrement the uses of the lambda source
			for (Enumeration e = used.dec(lambda.src[j], lambda);
			     e.hasMoreElements(); )
			    // and push nodes on the worklist to be examined
			    W.push(e.nextElement());
			// shrink the actual lambda function.
			lambda.dst = (Temp[][]) Util.shrink(lambda.dst, j);
			lambda.src = (Temp[]) Util.shrink(lambda.src, j);
		    } else j++;
		}
	    } // end IF INSTANCEOF PHI || LAMBDA
	} // end WHILE (W IS NOT EMPTY)
	// done.
    }
}
