// CJMP.java, created Wed Aug  5 07:07:32 1998 by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.Quads;

import harpoon.ClassFile.*;
import harpoon.Temp.Temp;
import harpoon.Temp.TempMap;
/**
 * <code>CJMP</code> represents conditional branches.<p>
 * <code>next[0]</code> is if-false, which is taken if 
 *                         the operand is equal to zero.
 * <code>next[1]</code> is if-true branch, taken when 
 *                         the operand is not equal to zero.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: CJMP.java,v 1.1.2.1 1998-12-01 12:36:41 cananian Exp $
 */

public class CJMP extends SIGMA {
    public Temp test;

    /** Creates a <code>CJMP</code>. */
    public CJMP(HCodeElement source, Temp test, Temp dst[][], Temp src[]) {
        super(source, dst, src, 2 /* two branch targets */);
	this.test = test;
    }
    public CJMP(HCodeElement source, Temp test, Temp src[]) {
	this(source, test, new Temp[src.length][2], src);
    }

    /** Swaps if-true and if-false targets. */
    public void invert() {
	Edge iftrue = nextEdge(0);
	Edge iffalse= nextEdge(1);

	Quad.addEdge(this, 0, (Quad)iffalse.to(), iffalse.which_pred());
	Quad.addEdge(this, 1, (Quad)iftrue.to(), iftrue.which_pred());

	for (int i=0; i<src.length; i++) {
	    Temp Ttrue = dst[i][0];
	    Temp Tfalse= dst[i][1];
	    dst[i][0] = Tfalse;
	    dst[i][1] = Ttrue;
	}
    }
    /** Returns all the Temps used by this Quad.
     * @return the <code>test</code> field.
     */
    public Temp[] use() { 
	Temp[] u = super.use();
	Temp[] r = new Temp[u.length+1];
	System.arraycopy(u, 0, r, 0, u.length);
	// add 'test' to end of use array.
	r[u.length] = test;
	return r;
    }

    /** Rename all used variables in this Quad according to a mapping. */
    public void renameUses(TempMap tm) {
	super.renameUses(tm);
	test = tm.tempMap(test);
    }
    /** Rename all defined variables in this Quad according to a mapping. */
    public void renameDefs(TempMap tm) {
	super.renameDefs(tm);
    }

    public void visit(QuadVisitor v) { v.visit(this); }

    /** Returns human-readable representation. */
    public String toString() {
	return "CJMP: if " + test + 
	    " then " + next(1).getID() + 
	    " else " + next(0).getID() +
	    " / " + super.toString();
    }
}
