// SyncTransformer.java, created Fri Oct 27 16:50:14 2000 by cananian
// Copyright (C) 2000 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.LowQuad.Transactions;

import harpoon.Analysis.ClassHierarchy;
import harpoon.Analysis.DomTree;
import harpoon.ClassFile.*;
import harpoon.IR.Quads.*;
import harpoon.IR.LowQuad.*;
import harpoon.Temp.Temp;
import harpoon.Util.Util;
/**
 * <code>SyncTransformer</code> transforms synchronized code to
 * atomic transactions.  Works on <code>LowQuadNoSSA</code> form.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: SyncTransformer.java,v 1.1.2.1 2000-10-27 22:30:51 cananian Exp $
 */
public class SyncTransformer
    extends harpoon.Analysis.Transformation.MethodSplitter {
    static final Token WITH_TRANSACTION = new Token("withtrans");
    protected boolean isValidToken(Token which) {
	return super.isValidToken(which) || which==WITH_TRANSACTION;
    }
    /** Cache the <code>CommitRecord</code> <code>HClass</code>. */
    private final HClass HCcommitrec;
    /** Creates a <code>SyncTransformer</code>. */
    public SyncTransformer(HCodeFactory hcf, ClassHierarchy ch, Linker l) {
        super(harpoon.IR.LowQuad.LowQuadNoSSA.codeFactory(hcf), ch);
	Util.assert(codeFactory().getCodeName()
		    .equals(harpoon.IR.LowQuad.LowQuadNoSSA.codename));
	String pkg = "harpoon.Runtime.Transactions.";
	this.HCcommitrec = l.forName(pkg+"CommitRecord");
    }
    protected String mutateDescriptor(HMethod hm, Token which) {
	if (which==WITH_TRANSACTION)
	    // add CommitRecord as first arg.
	    return "(" + HCcommitrec.getDescriptor() +
		hm.getDescriptor().substring(1);
	else return super.mutateDescriptor(hm, which);
    }
    protected HCode mutateHCode(HCodeAndMaps input, Token which) {
	HCode hc = input.hcode();
	METHOD qM = (METHOD) ((HEADER) hc.getRootElement()).next(1);
	Temp currtrans = null;
	if (which==WITH_TRANSACTION) // get the transaction context object
	    currtrans = qM.params(qM.isStatic() ? 0 : 1);
	// recursively decend the dominator tree, rewriting as we go.
	tweak(new DomTree(), hc, qM, currtrans);
	// done!
	return hc;
    }

    /** MONITORENTER must dominate all associated MONITOREXITs */
    private void tweak(DomTree dt, HCode hc, Quad q, Temp currtrans) {
	Quad[] nxt = (Quad[]) dt.children(hc, q);
	// XXX: tweak q here, update currtrans, etc.
	for (int i=0; i<nxt.length; i++)
	    tweak(dt, hc, nxt[i], currtrans);
    }
}
