// CleanUp.java, created Sat Sep 12 20:23:54 1998 by cananian
package harpoon.IR.QuadSSA;

import java.util.Hashtable;

import harpoon.ClassFile.*;
import harpoon.Temp.Temp;
import harpoon.Temp.TempMap;
import harpoon.Util.Util;
import harpoon.Util.UniqueFIFO;

/**
 * <code>CleanUp</code> cleans up the phi functions of the IR generated
 * by the <code>Translate</code> class.<p>
 * It: <UL>
 * <LI> Removes phi functions that define a temp that is never used
 *      (which magically removes undefined temps as well).
 * <LI> Shrinks phi functions that have phantom limbs 
 *      (from impossible catches).
 * </UL>
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: CleanUp.java,v 1.2 1998-09-15 11:15:48 cananian Exp $
 * @see Translate
 */

class CleanUp  {
    static void cleanup(HCode c) {
	//cleanup((Quad[]) c.getElements());
    }
    private static void cleanup(Quad[] ql) {
	// collect all the temps that are used somewhere reachable.
	UniqueFIFO used = new UniqueFIFO();
	for (int i=0; i<ql.length; i++) {
	    Temp[] u = ql[i].use();
	    for (int j=0; j<u.length; j++)
		if (u[j]!=null) // nasty phi functions that we'll clean up soon
		    used.push(u[j]);
	}
	// iterate over all quads.
	for (int i=0; i<ql.length; i++) {
	    if (! (ql[i] instanceof PHI) ) continue;
	    PHI phi = (PHI) ql[i];
	    // shrink phi functions with unused limbs.
	    for (int j=0; j<phi.prev.length; )
		if (phi.prev[j]==null)
		    phi.remove(j);
		else j++;
	    // remove unused phi functions.
	    for (int j=0; j<phi.dst.length; )
		if (!used.contains(phi.dst[j])) {
		    phi.dst = (Temp[]) Util.shrink(phi.dst, j);
		    phi.src = (Temp[][]) Util.shrink(phi.src, j);
		} else j++;
	}
	// Make renaming table for phi functions where all args are the same.
	final Hashtable rename = new Hashtable();
	for (int i=0; i<ql.length; i++) {
	    if ( ! (ql[i] instanceof PHI) ) continue;
	    PHI phi = (PHI) ql[i];
	    for (int j=0; j<phi.dst.length; ) {
		Temp arg=null;
		int k;
		for (k=0; k<phi.src[j].length; k++) {
		    if (phi.dst[j] == phi.src[j][k])
			continue; // these don't count.
		    if (arg==null) 
			arg=phi.src[j][k];
		    else if (arg != phi.src[j][k])
			break; // not all args the same.
		}
		if (k==phi.src[j].length) { // all args the same
		    // make renaming entries.
		    if (arg!=null)
			rename.put(/*oldname*/phi.dst[j],
				   /*newname*/arg);
		    // delete this phi function.
		    phi.dst = (Temp[]) Util.shrink(phi.dst, j);
		    phi.src = (Temp[][]) Util.shrink(phi.src, j);
		} else j++;
	    }
	}
	// Rename variables according to table.
	TempMap tm = new TempMap() {
	    public Temp tempMap(Temp t) {
		while (rename.containsKey(t))
		    t = (Temp) rename.get(t);
		return t;
	    }
	};
	for (int i=0; i<ql.length; i++)
	    ql[i].rename(tm);
	
	// Remove arity-1 phi functions that are leftover.
	for (int i=0; i<ql.length; i++)
	    if ( (ql[i] instanceof PHI) &&
		 (ql[i].prev.length == 1) ) {
		// unlink this phi node.
		Edge in = ql[i].prev[0];
		Edge out= ql[i].next[0];
		Quad.addEdge((Quad)in.from(), in.which_succ(),
			     (Quad)out.to(), out.which_pred());
	    }
	// done.
    }
}
