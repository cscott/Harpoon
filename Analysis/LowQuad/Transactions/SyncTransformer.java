// SyncTransformer.java, created Fri Oct 27 16:50:14 2000 by cananian
// Copyright (C) 2000 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.LowQuad.Transactions;

import harpoon.Analysis.ClassHierarchy;
import harpoon.Analysis.DomTree;
import harpoon.ClassFile.*;
import harpoon.IR.Quads.*;
import harpoon.Temp.Temp;
import harpoon.Temp.TempFactory;
import harpoon.Util.Util;

import java.util.*;
/**
 * <code>SyncTransformer</code> transforms synchronized code to
 * atomic transactions.  Works on <code>LowQuadNoSSA</code> form.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: SyncTransformer.java,v 1.1.2.3 2000-11-07 21:02:38 cananian Exp $
 */
public class SyncTransformer
    extends harpoon.Analysis.Transformation.MethodSplitter {
    static final Token WITH_TRANSACTION = new Token("withtrans");
    protected boolean isValidToken(Token which) {
	return super.isValidToken(which) || which==WITH_TRANSACTION;
    }
    /** Cache the <code>CommitRecord</code> <code>HClass</code>. */
    private final HClass  HCcommitrec;
    private final HMethod HMcommitrec_new;
    private final HMethod HMcommitrec_retry;
    private final HMethod HMcommitrec_commit;
    private final HField  HFcommitrec_parent;
    private final HClass  HCabortex;
    private final HField  HFabortex_upto;
    /* flag value */
    private final HField HFflagvalue;

    /** Creates a <code>SyncTransformer</code>. */
    public SyncTransformer(HCodeFactory hcf, ClassHierarchy ch, Linker l) {
        super(harpoon.IR.Quads.QuadNoSSA.codeFactory(hcf), ch);
	Util.assert(codeFactory().getCodeName()
		    .equals(harpoon.IR.Quads.QuadNoSSA.codename));
	String pkg = "harpoon.Runtime.Transactions.";
	this.HCcommitrec = l.forName(pkg+"CommitRecord");
	this.HMcommitrec_new =
	    HCcommitrec.getMethod("newTransaction", new HClass[]{HCcommitrec});
	this.HMcommitrec_retry =
	    HCcommitrec.getMethod("retryTransaction", new HClass[0]);
	this.HMcommitrec_commit =
	    HCcommitrec.getMethod("commitTransaction", new HClass[0]);
	this.HFcommitrec_parent = HCcommitrec.getField("parent");
	this.HCabortex = l.forName(pkg+"TransactionAbortException");
	this.HFabortex_upto = HCabortex.getField("abortUpTo");
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
	HEADER qH = (HEADER) hc.getRootElement();
	FOOTER qF = (FOOTER) qH.next(0);
	METHOD qM = (METHOD) qH.next(1);
	Temp currtrans = null;
	if (which==WITH_TRANSACTION) // get the transaction context object
	    currtrans = qM.params(qM.isStatic() ? 0 : 1);
	// recursively decend the dominator tree, rewriting as we go.
	if (! ("harpoon.Runtime.Transactions".equals
	       (hc.getMethod().getDeclaringClass().getPackage())))
	    tweak(new DomTree(), hc, qM,
		  new Tweaker(qF, currtrans));
	// done!
	return hc;
    }

    /** MONITORENTER must dominate all associated MONITOREXITs */
    private void tweak(DomTree dt, HCode hc, Quad q, Tweaker tw) {
	Quad[] nxt = (Quad[]) dt.children(hc, q);
	// tweak q here, update currtrans, etc.
	q.accept(tw);
	// done, recurse.
	int depth = tw.depth; // save this value.
	for (int i=0; i<nxt.length; i++, tw.depth=depth/*restore*/)
	    tweak(dt, hc, nxt[i], tw);
    }
    class Tweaker extends QuadVisitor {
	// immutable.
	final FOOTER footer;
	final QuadFactory qf;
	final TempFactory tf;
	final Temp retex;
	final Temp currtrans; // current transaction.
	private final Map fixupmap = new HashMap();
	// mutable.
	int depth; // depth of transaction nesting.
	List linktohandler; //list of THROWs to link to the abortexcpt. handler
	Tweaker(FOOTER qF, Temp currtrans) {
	    this.footer = qF;
	    this.qf = qF.getFactory();;
	    this.tf = this.qf.tempFactory();
	    this.depth = (currtrans==null) ? 0 : 1;
	    this.currtrans =
		(currtrans!=null) ? currtrans : new Temp(tf, "transid");
	    this.retex = new Temp(tf, "trabex"); // transaction abort exception
	}
	public void visit(Quad q) { /* do nothing */ }

	public void visit(CALL q) {
	    // if in a transaction, call the transaction version &
	    // deal with possible abort.
	    if (depth==0) return;
	    Temp[] nparams = new Temp[q.paramsLength()+1];
	    int i=0;
	    if (!q.isStatic())
		nparams[i++] = q.params(0);
	    nparams[i++] = currtrans;
	    for ( ; i<nparams.length; i++)
		nparams[i] = q.params(i-1);
	    CALL ncall = new CALL(qf, q, select(q.method(), WITH_TRANSACTION),
				  nparams, q.retval(), q.retex(),
				  q.isVirtual(), q.isTailCall(),
				  q.dst(), q.src());
	    Quad.replace(q, ncall);
	    // abort case is handled when exception is eventually THROWn.
	}
	private Edge addAt(Edge e, Quad q) {
	    Quad frm = (Quad) e.from(); int which_succ = e.which_succ();
	    Quad to  = (Quad) e.to();   int which_pred = e.which_pred();
	    Quad.addEdge(frm, which_succ, q, 0);
	    Quad.addEdge(q, 0, to, which_pred);
	    return to.prevEdge(which_pred);
	}
	public void visit(MONITORENTER q) {
	    Edge in = q.prevEdge(0), out = q.nextEdge(0);
	    if (depth==0)
		in = addAt(in, new CONST(qf, q, currtrans, null, HClass.Void));
	    depth++;
	    // loop looks like:
	    // c=newTransaction(c);
	    // L1: try {
	    //  ...
	    //  c.commitTransaction();
	    // } catch (TransactionAbortException ex) {
	    //   if (ex.which==c) { // allows us to abort up to a parent trans
            //      c = c.retryTransaction();
	    //      goto L1;
	    //   } else throw ex;
	    // }
	    Quad q0 = new CALL(qf, q, HMcommitrec_new,
			       new Temp[] { currtrans }, currtrans, retex,
			       false, false, new Temp[0]);
	    Quad q1 = new PHI(qf, q, new Temp[0], 2);
	    Quad q2 = new CALL(qf, q, HMcommitrec_retry,
			       new Temp[] { currtrans }, currtrans, retex,
			       false, false, new Temp[0]);
	    Quad q3 = new PHI(qf, q, new Temp[0], 4);
	    Quad q4 = new THROW(qf, q, retex);
	    Quad.addEdge((Quad)in.from(), in.which_succ(), q0, 0);
	    Quad.addEdge(q0, 0, q1, 0);
	    Quad.addEdge(q0, 1, q3, 0);
	    Quad.addEdge(q1, 0, (Quad)out.to(), out.which_pred());//delink q
	    Quad.addEdge(q2, 0, q1, 1);
	    Quad.addEdge(q2, 1, q3, 1);
	    Quad.addEdge(q3, 0, q4, 0);
	    footer.attach(q4, 0); // attach throw to FOOTER.
	    // add test to TransactionAbortException;
	    Quad q5 = new PHI(qf, q, new Temp[0], 0); // stub
	    Temp tst = new Temp(tf);
	    Quad q6 = new INSTANCEOF(qf, q, tst, retex, HCabortex);
	    Quad q7 = new CJMP(qf, q, tst, new Temp[0]);
	    Temp stop = new Temp(tf);
	    Quad q8 = new GET(qf, q, stop, HFabortex_upto, retex);
	    Quad q9 = new OPER(qf, q, Qop.ACMPEQ, tst,
			       new Temp[] { stop, currtrans });
	    Quad q10= new CJMP(qf, q, tst, new Temp[0]);
	    Quad.addEdges(new Quad[] { q5, q6, q7 });
	    Quad.addEdge(q7, 0, q3, 2); // not abort exception: rethrow
	    Quad.addEdge(q7, 1, q8, 0); // is abort ex: check for upto.
	    Quad.addEdges(new Quad[] { q8, q9, q10 });
	    Quad.addEdge(q10, 0, q3, 3); // not equal: rethrow exception
	    Quad.addEdge(q10, 1, q2, 0); // else, retry.
	    // all transactionabortexceptions need to link to q5,
	    // with the exception in retex.
	    linktohandler = new ArrayList();
	    fixupmap.put(q5, linktohandler);
	}
	public void visit(MONITOREXIT q) {
	    Util.assert(depth>0);
	    Edge in = q.prevEdge(0), out = q.nextEdge(0);
	    depth--;
	    // call c.commitTransaction(), linking to abort code if fails.
	    Quad q0 = new CALL(qf, q, HMcommitrec_commit,
			       new Temp[] { currtrans }, null, retex,
			       false, false, new Temp[0]);
	    Quad q1 = new GET(qf, q, currtrans, HFcommitrec_parent, currtrans);
	    Quad q2 = new THROW(qf, q, retex);
	    Quad.addEdge((Quad)in.from(), in.which_succ(), q0, 0);
	    Quad.addEdge(q0, 0, q1, 0);
	    Quad.addEdge(q0, 1, q2, 0);
	    Quad.addEdge(q1, 0, (Quad)out.to(), out.which_pred());
	    footer.attach(q2, 0); // add q2 to FOOTER.
	    linktohandler.add(q2);
	    if (depth==0) q1.remove(); // unneccessary.
	}

	/*
	public void visit(PGET q) {
	    if (currtrans==null) { // non-transactional read
		Temp x = q.dst(); HClass type = q.type();
		Temp flag = new Temp(tf);
		Quad q0 = makeFlagConst(qf, q, flag, type);
		// XXX: all different types of comparisons! =(
		//Quad q1 = new POPER(qf, q, LQop.XXX);
	    }
	}
	public void visit(PSET q) {
	}

	private Quad makeFlagConst(LowQuadFactory qf, HCodeElement src,
				    Temp dst, HClass type) {
	    long FLAG_VALUE = 0xCACACACACACACACAL;
	    // = harpoon.Runtime.Transactions.Constants.FLAG_VALUE;
	    if (!type.isPrimitive())
		/ * address of FLAG_VALUE field is used as marker. * /
		return new PFCONST(qf, src, dst, HFflagvalue);
	    else if (type==HClass.Boolean)
		return new CONST(qf, src, dst,
				 new Integer((int)FLAG_VALUE&1), HClass.Int);
	    else if (type==HClass.Byte)
		return new CONST(qf, src, dst,
				 new Integer((byte)FLAG_VALUE), HClass.Int);
	    else if (type==HClass.Char)
		return new CONST(qf, src, dst,
				 new Integer((char)FLAG_VALUE), HClass.Int);
	    else if (type==HClass.Short)
		return new CONST(qf, src, dst,
				 new Integer((short)FLAG_VALUE), HClass.Int);
	    else if (type==HClass.Int)
		return new CONST(qf, src, dst,
				 new Integer((int)FLAG_VALUE), HClass.Int);
	    else if (type==HClass.Long)
		return new CONST(qf, src, dst,
				 new Long(FLAG_VALUE), HClass.Long);
	    else if (type==HClass.Float)
		return new CONST(qf, src, dst,
				 new Float(Float.intBitsToFloat((int)FLAG_VALUE)), HClass.Float);
	    else if (type==HClass.Double)
		return new CONST(qf, src, dst,
				 new Double(Double.longBitsToDouble(FLAG_VALUE)), HClass.Double);
	    else throw new Error("ACK: "+type);
	}
    */
    }

    static abstract class FutureWriteIdentifier {
	/** Returns <code>true</code> if a <code>PSET</code> may ever
	 *  be done on a value derived from this <code>PPTR</code>. */
	public abstract boolean futureWrite(/*PPTR pptr*/);
    }
}
