// CleanUp.java, created Sat Sep 12 20:23:54 1998 by cananian
package harpoon.IR.QuadSSA;

import java.util.Hashtable;

import harpoon.ClassFile.*;
import harpoon.Temp.Temp;
import harpoon.Temp.TempMap;
import harpoon.Util.Util;
import harpoon.Util.Set;

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
 * @version $Id: CleanUp.java,v 1.3 1998-09-16 13:03:02 cananian Exp $
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
	    // shrink phi functions with unused limbs.
	    for (int j=0; j<phi.prev.length; )
		if (phi.prev[j]==null)
		    phi.remove(j);
		else j++;
	}
    }
    private static void cleanupUnused(Quad[] ql) {
	// collect all the temps that are used somewhere reachable.
	Set used = new Set();
	for (int i=0; i<ql.length; i++) {
	    Temp[] u = ql[i].use();
	    for (int j=0; j<u.length; j++)
		used.push(u[j]);
	}
	// iterate over all phis and lambdas.
	for (int i=0; i<ql.length; i++) {
	    if (ql[i] instanceof PHI) {
		PHI phi = (PHI) ql[i];
		// remove unused phi functions.
		for (int j=0; j<phi.dst.length; )
		    if (!used.contains(phi.dst[j])) {
			phi.dst = (Temp[]) Util.shrink(phi.dst, j);
			phi.src = (Temp[][]) Util.shrink(phi.src, j);
		    } else j++;
	    } else if (ql[i] instanceof LAMBDA) {
		LAMBDA lambda = (LAMBDA) ql[i];
		// an unused phi function has no used destinations.
		for (int j=0; j<lambda.dst.length; ) {
		    int k; for (k=0; k < lambda.dst[j].length; k++)
			if (used.contains(lambda.dst[j][k]))
			    break;
		    if (k==lambda.dst[j].length) { // no used variables found.
			lambda.dst = (Temp[][]) Util.shrink(lambda.dst, j);
			lambda.src = (Temp[]) Util.shrink(lambda.src, j);
		    } else j++;
		}
	    }
	}
	// done.
    }
}
