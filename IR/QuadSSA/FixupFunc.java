// FixupFunc.java, created Tue Sep 15 15:10:05 1998 by cananian
package harpoon.IR.QuadSSA;

import harpoon.ClassFile.*;
import harpoon.Analysis.Place;
import harpoon.Temp.Temp;
import harpoon.Util.Util;
/**
 * <code>FixupFunc</code> places appropriate Phi and Lambda functions
 * in the Quads.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: FixupFunc.java,v 1.1 1998-09-15 20:15:26 cananian Exp $
 * @see Translate
 */

public class FixupFunc  {
    static void place(Code c) {
	Place Pphi = new Place(false);
        Place Plam = new Place(true);
	
	Quad[] ql = (Quad[]) c.getElements();
	for (int i=0; i< ql.length; i++) {
	    Temp[] neededPhi = Pphi.neededFunc(c, ql[i]);
	    Temp[] neededLam = Plam.neededFunc(c, ql[i]);
	    // algorithm wants to place phis on FOOTER.
	    if (neededPhi.length > 0 && (!(ql[i] instanceof FOOTER))) {
		// place phi functions.
		PHI q = (PHI) ql[i]; // better be a phi!
		q.dst = neededPhi;
		q.src = new Temp[q.dst.length][q.prev.length];
		for (int j=0; j < q.src.length; j++)
		    for (int k=0; k < q.src[j].length; k++)
			q.src[j][k] = q.dst[j];
	    }
	    if (neededLam.length > 0) {
		// place lambda functions.
		LAMBDA q = (LAMBDA) ql[i]; // better be a lambda!
		q.src = neededLam;
		q.dst = new Temp[q.src.length][q.next.length];
		for (int j=0; j < q.dst.length; j++)
		    for (int k=0; k < q.dst[j].length; k++)
			q.dst[j][k] = q.src[j];
	    }
	}
    }
}
