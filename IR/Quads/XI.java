// XI.java, created Thu May  6 00:13:40 1999 by cananian
// Copyright (C) 1999 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.Quads;

import harpoon.ClassFile.HCodeElement;
import harpoon.Temp.Temp;
import harpoon.Temp.TempMap;
import harpoon.Util.Util;

/**
 * <code>XI</code> objects represent phi functions prefixed by a xi
 * function.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: XI.java,v 1.5 2002-04-11 04:00:35 cananian Exp $
 */
class XI extends PHI {
    protected Temp invariantDst[];
    protected Temp invariantSrc[];
    protected Temp backedgeDst[];
    protected Temp backedgeSrc[];
    
    /** Creates a <code>XI</code> object representing a xi function
     *  prefixing a block of phi functions.
     * @param invariantDst
     *        outgoing loop invariants.
     * @param invariantSrc
     *        incoming loop invariants.
     * @param backedgeDst
     *        outgoing backedge loop variables.
     * @param backedgeSrc
     *        incoming backedge loop variables.
     * @param phiDst
     *        the left hand sides of a phi function assignment block.
     * @param phiSrc
     *        the phi function parameters in a phi function assignment block.
     */
    public XI(QuadFactory qf, HCodeElement source,
	      Temp invariantDst[], Temp invariantSrc[],
	      Temp backedgeDst[], Temp backedgeSrc[],
	      Temp phiDst[], Temp phiSrc[][], int arity) {
        super(qf, source, phiDst, phiSrc, arity);
	this.invariantDst = invariantDst;
	this.invariantSrc = invariantSrc;
	this.backedgeDst  = backedgeDst;
	this.backedgeSrc  = backedgeSrc;
	// VERIFY legality of XI function.
	assert invariantDst!=null && invariantSrc!=null;
	assert backedgeDst !=null && backedgeSrc !=null;
	assert invariantDst.length == invariantSrc.length;
	assert backedgeDst.length  == backedgeSrc.length;
    }
    public Temp[] invariantDst()
    { return (Temp[]) Util.safeCopy(Temp.arrayFactory, invariantDst); }
    public Temp[] invariantSrc()
    { return (Temp[]) Util.safeCopy(Temp.arrayFactory, invariantSrc); }
    public Temp[] backedgeDst()
    { return (Temp[]) Util.safeCopy(Temp.arrayFactory, backedgeDst); }
    public Temp[] backedgeSrc()
    { return (Temp[]) Util.safeCopy(Temp.arrayFactory, backedgeSrc); }

    public Temp[] use() {
	Temp[] phiUse = super.use();
	Temp[] u = new Temp[phiUse.length + invariantSrc.length +
			   backedgeSrc.length];
	int j=0;
	for (int i=0; i<phiUse.length; i++)
	    u[j++] = phiUse[i];
	for (int i=0; i<invariantSrc.length; i++)
	    u[j++] = invariantSrc[i];
	for (int i=0; i<backedgeSrc.length; i++)
	    u[j++] = backedgeSrc[i];
	return u;
    }
    public Temp[] def() {
	Temp[] phiDef = super.def();
	Temp[] u = new Temp[phiDef.length + invariantDst.length +
			   backedgeDst.length];
	int j=0;
	for (int i=0; i<phiDef.length; i++)
	    u[j++] = phiDef[i];
	for (int i=0; i<invariantDst.length; i++)
	    u[j++] = invariantDst[i];
	for (int i=0; i<backedgeDst.length; i++)
	    u[j++] = backedgeDst[i];
	return u;
    }

    public int kind() { return QuadKind.XI; }

    public Quad rename(QuadFactory qqf, TempMap defMap, TempMap useMap) {
	return new XI(qqf, this,
		      map(defMap,invariantDst), map(useMap,invariantSrc),
		      map(defMap,backedgeDst),  map(useMap,backedgeSrc),
		      map(defMap,/*phi*/dst),   map(useMap,/*phi*/src),
		      arity());
    }
    void renameUses(TempMap tm) { assert false; }
    void renameDefs(TempMap tm) { assert false; }

    public void accept(QuadVisitor v) { v.visit(this); }
    public <T> T accept(QuadValueVisitor<T> v) { return v.visit(this); }

    public String toString() {
	StringBuffer sb = new StringBuffer();
	sb.append("[<");
	for (int i=0; i<invariantDst.length; i++) {
	    sb.append(invariantDst[i].toString());
	    if (i < invariantDst.length-1) sb.append(",");
	}
	sb.append(">, <");
	for (int i=0; i<backedgeDst.length; i++) {
	    sb.append(backedgeDst[i].toString());
	    if (i < backedgeDst.length-1) sb.append(",");
	}
	sb.append(">] = XI([<");
	for (int i=0; i<invariantSrc.length; i++) {
	    sb.append(invariantSrc[i].toString());
	    if (i < invariantSrc.length-1) sb.append(",");
	}
	sb.append(">, <");
	for (int i=0; i<backedgeSrc.length; i++) {
	    sb.append(backedgeSrc[i].toString());
	    if (i < backedgeSrc.length-1) sb.append(",");
	}
	sb.append(">]) / ");
	sb.append(super.toString());
	return sb.toString();
    }
}
