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
 * @version $Id: SyncTransformer.java,v 1.1.2.7 2000-11-10 21:57:25 cananian Exp $
 */
public class SyncTransformer
    extends harpoon.Analysis.Transformation.MethodSplitter {
    static final Token WITH_TRANSACTION = new Token("withtrans") {
	public Object readResolve() { return WITH_TRANSACTION; }
    };
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
    //private final HField HFflagvalue;

    /** Creates a <code>SyncTransformer</code>. */
    public SyncTransformer(HCodeFactory hcf, ClassHierarchy ch, Linker l) {
        super(harpoon.IR.Quads.QuadNoSSA.codeFactory(hcf), ch, false);
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
	//this.HFflagvalue = l.forName(pkg+"Constants").getField("FLAG_VALUE");
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
	FOOTER qF = qH.footer();
	METHOD qM = qH.method();
	// recursively decend the dominator tree, rewriting as we go.
	if (! ("harpoon.Runtime.Transactions".equals
	       (hc.getMethod().getDeclaringClass().getPackage()))) {
	    Tweaker tw = new Tweaker(qF, (which==WITH_TRANSACTION));
	    tweak(new DomTree(hc, false), qM, tw);
	    tw.fixup();
	}
	// done!
	return hc;
    }

    /** MONITORENTER must dominate all associated MONITOREXITs */
    private void tweak(DomTree dt, Quad q, Tweaker tw) {
	HCodeElement[] nxt = dt.children(q);
	// tweak q here, update currtrans, etc.
	q.accept(tw);
	// done, recurse.
	ListList handlers = tw.handlers; // save this value.
	for (int i=0; i<nxt.length; i++, tw.handlers=handlers/*restore*/)
	    tweak(dt, (Quad) nxt[i], tw);
    }
    static class ListList {
	public final List head;
	public final ListList tail;
	public ListList(List head, ListList tail) {
	    this.head = head; this.tail = tail;
	}
    }
    class Tweaker extends QuadVisitor {
	// immutable.
	final QuadFactory qf;
	final TempFactory tf;
	final Temp retex;
	final Temp currtrans; // current transaction.
	private final Map fixupmap = new HashMap();
	// mutable.
	FOOTER footer; // we attach new stuff to the footer.
	ListList handlers = null; // points to current abort handler
	Tweaker(FOOTER qF, boolean with_transaction) {
	    this.footer = qF;
	    this.qf = qF.getFactory();;
	    this.tf = this.qf.tempFactory();
	    // indicate that we're inside transaction context, but
	    // that we need to rethrow TransactionAbortExceptions
	    if (with_transaction)
		handlers = new ListList(null, handlers);
	    this.currtrans = new Temp(tf, "transid");
	    this.retex = new Temp(tf, "trabex"); // transaction abort exception
	}
	/** helper routine to add a quad on an edge. */
	private Edge addAt(Edge e, Quad q) {
	    Quad frm = (Quad) e.from(); int which_succ = e.which_succ();
	    Quad to  = (Quad) e.to();   int which_pred = e.which_pred();
	    Quad.addEdge(frm, which_succ, q, 0);
	    Quad.addEdge(q, 0, to, which_pred);
	    return to.prevEdge(which_pred);
	}
	/** Insert abort exception check on the given edge. */
	private Edge checkForAbort(Edge e, HCodeElement src, Temp tex) {
	    Temp tst = new Temp(tf);
	    e = addAt(e, new INSTANCEOF(qf, src, tst, tex, HCabortex));
	    e = addAt(e, new CJMP(qf, src, tst, new Temp[0]));
	    Quad q0 = new THROW(qf, src, tex);
	    Quad.addEdge((Quad)e.from(), 1, q0, 0);
	    handlers.head.add(q0);
	    return e;
	}
	/** Fix up PHIs leading to abort handler after we're all done. */
	void fixup() {
	    for(Iterator it=fixupmap.entrySet().iterator(); it.hasNext(); ) {
		Map.Entry me = (Map.Entry) it.next();
		PHI phi = (PHI) me.getKey();
		List throwlist = (List) me.getValue();
		PHI nphi = new PHI(qf, phi, new Temp[0], throwlist.size());
		Edge out = phi.nextEdge(0);
		Quad.addEdge(nphi, 0, (Quad)out.to(), out.which_pred());
		int n=0;
		for (Iterator it2 = throwlist.iterator(); it2.hasNext(); n++) {
		    THROW thr = (THROW) it2.next();
		    Temp tex = thr.throwable();
		    Edge in = thr.prevEdge(0);
		    if (tex!=retex)
			in = addAt(in, new MOVE(qf, thr, retex, tex));
		    Quad.addEdge((Quad)in.from(), in.which_succ(), nphi, n);
		    // NOTE THAT WE ARE NOT DELINKING THE THROW FROM THE
		    // FOOTER: this should be done before it is added to the
		    // list.
		}
	    }
	}

	public void visit(Quad q) { /* do nothing */ }

	public void visit(METHOD q) {
	    if (handlers==null) return; // don't rewrite if not in trans
	    Temp[] nparams = new Temp[q.paramsLength()+1];
	    int i=0;
	    if (!q.isStatic())
		nparams[i++] = q.params(0);
	    nparams[i++] = currtrans;
	    for ( ; i<nparams.length; i++)
		nparams[i] = q.params(i-1);
	    Quad.replace(q, new METHOD(qf, q, nparams, q.arity()));
	}

	public void visit(CALL q) {
	    // if in a transaction, call the transaction version &
	    // deal with possible abort.
	    if (handlers==null) return;
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
	    // now check for abort case.
	    if (handlers.head!=null) // unless we rethrow directly...
		checkForAbort(ncall.nextEdge(1), ncall, ncall.retex());
	    // done.
	}
	public void visit(MONITORENTER q) {
	    Edge in = q.prevEdge(0), out = q.nextEdge(0);
	    if (handlers==null)
		in = addAt(in, new CONST(qf, q, currtrans, null, HClass.Void));
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
	    Quad q3 = new PHI(qf, q, new Temp[0], 3);
	    Quad q4 = new THROW(qf, q, retex);
	    Quad.addEdge((Quad)in.from(), in.which_succ(), q0, 0);
	    Quad.addEdge(q0, 0, q1, 0);
	    Quad.addEdge(q0, 1, q3, 0);
	    Quad.addEdge(q1, 0, (Quad)out.to(), out.which_pred());//delink q
	    Quad.addEdge(q2, 0, q1, 1);
	    Quad.addEdge(q2, 1, q3, 1);
	    Quad.addEdge(q3, 0, q4, 0);
	    footer = footer.attach(q4, 0); // attach throw to FOOTER.
	    // add test to TransactionAbortException;
	    Quad q5 = new PHI(qf, q, new Temp[0], 0); // stub
	    Temp tst = new Temp(tf), stop = new Temp(tf);
	    Quad q6 = new GET(qf, q, stop, HFabortex_upto, retex);
	    Quad q7 = new OPER(qf, q, Qop.ACMPEQ, tst,
			       new Temp[] { stop, currtrans });
	    Quad q8= new CJMP(qf, q, tst, new Temp[0]);
	    Quad.addEdges(new Quad[] { q5, q6, q7, q8 });
	    Quad.addEdge(q8, 0, q3, 2); // not equal: rethrow exception
	    Quad.addEdge(q8, 1, q2, 0); // else, retry.
	    // all transactionabortexceptions need to link to q5,
	    // with the exception in retex.
	    handlers = new ListList(new ArrayList(), handlers);
	    fixupmap.put(q5, handlers.head);
	}
	public void visit(MONITOREXIT q) {
	    Edge in = q.prevEdge(0), out = q.nextEdge(0);
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
	    footer = footer.attach(q2, 0); // add q2 to FOOTER.
	    checkForAbort(q0.nextEdge(1), q, retex);
	    handlers = handlers.tail;
	    if (handlers==null) q1.remove(); // unneccessary.
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
