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
import harpoon.Temp.TempFactory;
import harpoon.Util.Util;
/**
 * <code>SyncTransformer</code> transforms synchronized code to
 * atomic transactions.  Works on <code>LowQuadNoSSA</code> form.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: SyncTransformer.java,v 1.1.2.2 2000-11-07 02:11:34 cananian Exp $
 */
public class SyncTransformer
    extends harpoon.Analysis.Transformation.MethodSplitter {
    static final Token WITH_TRANSACTION = new Token("withtrans");
    protected boolean isValidToken(Token which) {
	return super.isValidToken(which) || which==WITH_TRANSACTION;
    }
    /** Cache the <code>CommitRecord</code> <code>HClass</code>. */
    private final HClass HCcommitrec;
    private final HMethod HMcommitrec_init;
    private final HMethod HMcommitrec_abort;
    private final HMethod HMcommitrec_commit;
    /* flag value */
    private final HField HFflagvalue;

    /** Creates a <code>SyncTransformer</code>. */
    public SyncTransformer(HCodeFactory hcf, ClassHierarchy ch, Linker l) {
        super(harpoon.IR.LowQuad.LowQuadNoSSA.codeFactory(hcf), ch);
	Util.assert(codeFactory().getCodeName()
		    .equals(harpoon.IR.LowQuad.LowQuadNoSSA.codename));
	String pkg = "harpoon.Runtime.Transactions.";
	this.HCcommitrec = l.forName(pkg+"CommitRecord");
	this.HMcommitrec_init =
	    HCcommitrec.getConstructor(new HClass[] { HCcommitrec });
	this.HMcommitrec_abort =
	    HCcommitrec.getMethod("abort", new HClass[] { HCcommitrec });
	this.HMcommitrec_commit =
	    HCcommitrec.getMethod("commit", new HClass[] { HCcommitrec });
	this.HFflagvalue = l.forName(pkg+"Constants").getField("FLAG_VALUE");
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
	if (! ("harpoon.Runtime.Transactions".equals
	       (hc.getMethod().getDeclaringClass().getPackage())))
	    tweak(new DomTree(), hc, qM,
		  new Tweaker((LowQuadFactory)qM.getFactory(), currtrans));
	// done!
	return hc;
    }

    /** MONITORENTER must dominate all associated MONITOREXITs */
    private void tweak(DomTree dt, HCode hc, Quad q, Tweaker tw) {
	Quad[] nxt = (Quad[]) dt.children(hc, q);
	// tweak q here, update currtrans, etc.
	q.accept(tw);
	// done, recurse.
	Temp currtrans = tw.currtrans; // save this value.
	for (int i=0; i<nxt.length; i++, tw.currtrans=currtrans/*restore*/)
	    tweak(dt, hc, nxt[i], tw);
    }
    class Tweaker extends LowQuadVisitor {
	// immutable.
	final LowQuadFactory lqf;
	final TempFactory tf;
	// mutable.
	Temp currtrans; // current transaction.
	Tweaker(LowQuadFactory lqf, Temp currtrans) {
	    this.lqf = lqf;
	    this.tf = lqf.tempFactory();
	    this.currtrans = currtrans;
	}
	public void visit(Quad q) { /* do nothing */ }
	/*
	public void visit(MONITORENTER q) {
	    if (currtrans==null) {
		currtrans = new Temp(tf);
		Quad q0 = new CONST(lqf, q, currtrans, null, HClass.Void);
	    }
	    Temp newtrans = new Temp(tf);
	    Quad q1 = new NEW(lqf, q, newtrans, HCcommitref);
	    Temp methref = new Temp(tf);
	    Quad q2 = new PMCONST(lqf, q, methref, HMcommitrec_init);
	    Quad q3 = new PCALL(lqf, q, methref, new Temp[] { currtrans },
				null, / *retex!* /, new Temp[0], false, false);
	    // XXX: link and replace.
	    currtrans = newtrans;
	}
	public void visit(MONITOREXIT q) {
	    Util.assert(currtrans!=null);
	    Temp methref = new Temp(tf);
	    Quad q0 = new PMCONST(lqf, q, methref, HMcommitrec_commit);
	    Temp status = new Temp(tf);
	    Quad q1 = new PCALL(lqf, q, methref, new Temp[] { currtrans },
	                        status, / *retex!* /, new Temp[0], false, false);
	}
	*/
	public void visit(PGET q) {
	    if (currtrans==null) { // non-transactional read
		Temp x = q.dst(); HClass type = q.type();
		Temp flag = new Temp(tf);
		Quad q0 = makeFlagConst(lqf, q, flag, type);
		// XXX: all different types of comparisons! =(
		//Quad q1 = new POPER(lqf, q, LQop.XXX);
	    }
	}
	public void visit(PSET q) {
	}

	private Quad makeFlagConst(LowQuadFactory lqf, HCodeElement src,
				    Temp dst, HClass type) {
	    long FLAG_VALUE = 0xCACACACACACACACAL;
	    // = harpoon.Runtime.Transactions.Constants.FLAG_VALUE;
	    if (!type.isPrimitive())
		/* address of FLAG_VALUE field is used as marker. */
		return new PFCONST(lqf, src, dst, HFflagvalue);
	    else if (type==HClass.Boolean)
		return new CONST(lqf, src, dst,
				 new Integer((int)FLAG_VALUE&1), HClass.Int);
	    else if (type==HClass.Byte)
		return new CONST(lqf, src, dst,
				 new Integer((byte)FLAG_VALUE), HClass.Int);
	    else if (type==HClass.Char)
		return new CONST(lqf, src, dst,
				 new Integer((char)FLAG_VALUE), HClass.Int);
	    else if (type==HClass.Short)
		return new CONST(lqf, src, dst,
				 new Integer((short)FLAG_VALUE), HClass.Int);
	    else if (type==HClass.Int)
		return new CONST(lqf, src, dst,
				 new Integer((int)FLAG_VALUE), HClass.Int);
	    else if (type==HClass.Long)
		return new CONST(lqf, src, dst,
				 new Long(FLAG_VALUE), HClass.Long);
	    else if (type==HClass.Float)
		return new CONST(lqf, src, dst,
				 new Float(Float.intBitsToFloat((int)FLAG_VALUE)), HClass.Float);
	    else if (type==HClass.Double)
		return new CONST(lqf, src, dst,
				 new Double(Double.longBitsToDouble(FLAG_VALUE)), HClass.Double);
	    else throw new Error("ACK: "+type);
	}
    }

    static abstract class FutureWriteIdentifier {
	/** Returns <code>true</code> if a <code>PSET</code> may ever
	 *  be done on a value derived from this <code>PPTR</code>. */
	public abstract boolean futureWrite(PPTR pptr);
    }
}
