// CleanUp.java, created Sat Sep 12 20:23:54 1998 by cananian
package harpoon.IR.QuadSSA;

import java.util.Hashtable;
import java.util.Enumeration;

import harpoon.ClassFile.*;
import harpoon.Temp.Temp;
import harpoon.Temp.TempMap;
import harpoon.Util.NullEnumerator;
import harpoon.Util.Util;
import harpoon.Util.Set;
import harpoon.Util.Worklist;

/**
 * <code>CleanUp</code> cleans up the phi functions of the IR generated
 * by the <code>Translate</code> class.<p>
 * It: <UL>
 * <LI> Removes phi/sigma functions that define temps that are never used
 *      (which magically removes undefined temps as well).
 * <LI> Shrinks phi functions that have phantom limbs 
 *      (from impossible catches).
 * </UL>
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: CleanUp.java,v 1.8 1998-09-21 02:31:26 cananian Exp $
 * @see Translate
 */

class CleanUp  {
    static void cleanup1(Code c) {
	// iterate over all phis.
	for(Enumeration e=c.getElementsE(); e.hasMoreElements(); )
	    cleanupPhantomLimbs((Quad) e.nextElement());
    }
    static void cleanup2(Code c) {
	cleanupUnused(c);
    }
    private static void cleanupPhantomLimbs(Quad q) {
	if (! (q instanceof PHI) ) return;
	PHI phi = (PHI) q;
	// shrink phi functions with null limbs.
	for (int j=0; j<phi.prev.length; )
	    if (phi.prev[j]==null)
		phi.remove(j);
	    else j++;
    }

    static class UsedTable {
	Hashtable sH = new Hashtable(); // associate set of defs.
	Hashtable iH = new Hashtable(); // associate # of uses.

	void addUse(Temp t) {
	    Integer I = (Integer) iH.get(t);
	    int i = (I==null)?0:I.intValue();
	    iH.put(t, new Integer(i+1));
	}
	void addDef(Temp t, Quad q) {
	    Set s = (Set) sH.get(t);
	    if (s==null) { s=new Set(); sH.put(t, s); }
	    s.union(q);
	}
	int uses(Temp t) {
	    Integer I = (Integer) iH.get(t);
	    return (I==null)?0:I.intValue();
	}
	Enumeration dec(Temp t) {
	    int i = uses(t);
	    iH.put(t, new Integer(i-1));
	    Set s = (Set) sH.get(t);
	    if (i>1 || s==null)
		return NullEnumerator.STATIC;
	    else
		return s.elements();
	}
    }

    private static void cleanupUnused(Code c) {
	// collect all the temps that are used somewhere reachable.
	// and put all phis and sigmas on a worklist.
	UsedTable used = new UsedTable();
	Worklist W = new Set();

	for (Enumeration e = c.getElementsE(); e.hasMoreElements(); ) {
	    Quad q = (Quad) e.nextElement();
	    // collect uses/defs
	    Temp[] u = q.use();
	    for (int j=0; j<u.length; j++)
		used.addUse(u[j]);
	    Temp[] d = q.def();
	    for (int j=0; j<d.length; j++)
		used.addDef(d[j], q);
	    // put phis and sigmas on worklist.
	    if (q instanceof PHI || q instanceof SIGMA)
		W.push(q);
	}

	// now iterate until we've removed all the phis/sigmas we can.
	while (!W.isEmpty()) {
	    Quad q = (Quad) W.pull();
	    if (q instanceof PHI) {
		PHI phi = (PHI) q;
		// remove unused phi functions.
		for (int j=0; j<phi.dst.length; )
		    if (used.uses(phi.dst[j])==0) {
			// decrement the uses of the phi args.
			for (int k=0; k<phi.src[j].length; k++)
			    for (Enumeration e=used.dec(phi.src[j][k]);
				 e.hasMoreElements(); )
				// and push nodes on the list to be examined.
				W.push(e.nextElement());
			// shrink the actual phi function.
			phi.dst = (Temp[]) Util.shrink(phi.dst, j);
			phi.src = (Temp[][]) Util.shrink(phi.src, j);
		    } else j++;
	    } else if (q instanceof SIGMA) {
		SIGMA sigma = (SIGMA) q;
		// an unused phi function has no used destinations.
		for (int j=0; j<sigma.dst.length; ) {
		    int k; for (k=0; k < sigma.dst[j].length; k++)
			if (used.uses(sigma.dst[j][k]) != 0)
			    break;
		    if (k==sigma.dst[j].length) { // no used variables found.
			// decrement the uses of the sigma source
			for (Enumeration e = used.dec(sigma.src[j]);
			     e.hasMoreElements(); )
			    // and push nodes on the worklist to be examined
			    W.push(e.nextElement());
			// shrink the actual sigma function.
			sigma.dst = (Temp[][]) Util.shrink(sigma.dst, j);
			sigma.src = (Temp[]) Util.shrink(sigma.src, j);
		    } else j++;
		}
	    } // end IF INSTANCEOF PHI || SIGMA
	} // end WHILE (W IS NOT EMPTY)
	// done.
    }
}
