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

import java.lang.reflect.Modifier;
import java.util.*;
/**
 * <code>SyncTransformer</code> transforms synchronized code to
 * atomic transactions.  Works on <code>LowQuadNoSSA</code> form.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: SyncTransformer.java,v 1.1.2.8 2000-11-14 19:37:33 cananian Exp $
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
    /** Our new methods of java.lang.Object */
    private final HMethod HMrVersion;
    private final HMethod HMrwVersion;
    /*
    private final HMethod HMrCommitted;
    private final HMethod HMwCommitted;
    */
    /* flag value */
    private final HField HFflagvalue;

    /** Creates a <code>SyncTransformer</code>. */
    public SyncTransformer(HCodeFactory hcf, ClassHierarchy ch, Linker l) {
        super(harpoon.IR.Quads.QuadSSA.codeFactory(hcf), ch, false);
	// and output is NoSSA
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
	// now create methods of java.lang.Object.
	HClass HCobj = l.forName("java.lang.Object");
	HClassMutator objM = HCobj.getMutator();
	this.HMrVersion = objM.addDeclaredMethod
	    ("getReadableVersion", new HClass[] { HCcommitrec }, HCobj);
	HMrVersion.getMutator().addModifiers(Modifier.FINAL);
	this.HMrwVersion = objM.addDeclaredMethod
	    ("getReadWritableVersion", new HClass[] { HCcommitrec }, HCobj);
	HMrwVersion.getMutator().addModifiers(Modifier.FINAL);
	// create a static final field in java.lang.Object that will hold
	// our 'unique' value.
	this.HFflagvalue = objM.addDeclaredField("flagValue", HCobj);
	HFflagvalue.getMutator().addModifiers(Modifier.FINAL|Modifier.STATIC);
    }
    protected String mutateDescriptor(HMethod hm, Token which) {
	if (which==WITH_TRANSACTION)
	    // add CommitRecord as first arg.
	    return "(" + HCcommitrec.getDescriptor() +
		hm.getDescriptor().substring(1);
	else return super.mutateDescriptor(hm, which);
    }
    protected HCodeAndMaps cloneHCode(HCode hc, HMethod newmethod) {
	// make SSA into RSSx.
	Util.assert(hc.getName().equals(QuadSSA.codename));
	return MyRSSx.cloneToRSSx((harpoon.IR.Quads.Code)hc, newmethod);
    }
    private static class MyRSSx extends QuadRSSx {
	private MyRSSx(HMethod m) { super(m, null); }
	public static HCodeAndMaps cloneToRSSx(harpoon.IR.Quads.Code c,
					       HMethod m) {
	    MyRSSx r = new MyRSSx(m);
	    return r.cloneHelper(c, r);
	}
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
	final CheckOracle co=null;//XXX
	final TempSplitter ts=null;//XXX
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
	private Edge addAt(Edge e, Quad q) { return addAt(e, 0, q, 0); }
	private Edge addAt(Edge e, int which_pred, Quad q, int which_succ) {
	    Quad frm = (Quad) e.from(); int frm_succ = e.which_succ();
	    Quad to  = (Quad) e.to();   int to_pred = e.which_pred();
	    Quad.addEdge(frm, frm_succ, q, which_pred);
	    Quad.addEdge(q, which_succ, to, to_pred);
	    return to.prevEdge(to_pred);
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

	public void visit(Quad q) { addChecks(q); }

	public void visit(METHOD q) {
	    addChecks(q);
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
	    addChecks(q);
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
	    addChecks(q);
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
	    addChecks(q);
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
	public void visit(AGET q) {
	    addChecks(q);
	    if (currtrans==null) { // non-transactional read
		// XXX: write me
	    } else { // transactional read
		Quad.replace(q, new AGET(qf, q, q.dst(),
					ts.versioned(q.objectref()),
					 q.index(), q.type()));
	    }
	}
	public void visit(ASET q) {
	    addChecks(q);
	    if (currtrans==null) { // non-transactional read
		// XXX: write me
	    } else { // transactional read
		Quad.replace(q, new ASET(qf, q, ts.versioned(q.objectref()),
					 q.index(), q.src(),
					 q.type()));
	    }
	}
	public void visit(GET q) {
	    addChecks(q);
	    if (currtrans==null) { // non-transactional read
		// XXX: write me
	    } else { // transactional read
		Quad.replace(q, new GET(qf, q, q.dst(), q.field(),
					ts.versioned(q.objectref())));
	    }
	}
	public void visit(SET q) {
	    addChecks(q);
	    if (currtrans==null) { // non-transactional read
		// XXX: write me
	    } else { // transactional read
		Quad.replace(q, new SET(qf, q, q.field(),
					ts.versioned(q.objectref()),
					q.src()));
	    }
	}
	/*
	public void visit(GET q) {
	    if (currtrans==null) { // non-transactional read
		Temp x = q.dst(); HClass type = q.type();
		Temp flag = new Temp(tf);
		Quad q0 = makeFlagConst(qf, q, flag, type);
		// XXX: all different types of comparisons! =(
		//Quad q1 = new POPER(qf, q, LQop.XXX);
	    }
	}
	*/
	void addChecks(Quad q) {
	    // don't add checks if we're not currently in transaction context.
	    if (handlers==null) return;
	    // only deal with quads where "just before" makes sense.
	    Util.assert(q.prevLength()==1);
	    Edge in = q.prevEdge(0);
	    // create read/write versions for objects that need it.
	    Set rS = co.createReadVersions(q);
	    Set wS = co.createWriteVersions(q);
	    wS.removeAll(rS); // write really is read-write.
	    for (int i=0; i<2; i++) {
		// iteration 0 for read; iteration 1 for write versions.
		Iterator it = (i==0) ? rS.iterator() : wS.iterator();
		HMethod hm = (i==0) ? HMrVersion : HMrwVersion ;
		while (it.hasNext()) {
		    Temp t = (Temp) it.next();
		    CALL q0= new CALL(qf, q, hm, new Temp[] { t, currtrans },
		                      ts.versioned(t), retex,
				      false/*final, not virtual*/, false,
				      new Temp[0]);
		    THROW q1= new THROW(qf, q, retex);
		    in = addAt(in, q0);
		    Quad.addEdge(q0, 1, q1, 0);
		    footer.attach(q1, 0);
		    checkForAbort(q0.nextEdge(1), q, retex);
		}
	    }
	    // do field checks where necessary.
	    for (Iterator it=co.checkField(q).iterator(); it.hasNext(); ) {
		CheckOracle.RefAndField raf=(CheckOracle.RefAndField)it.next();
		HClass ty = raf.field.getType();
		Temp t0 = new Temp(tf, "fieldcheck");
		Temp t1 = new Temp(tf, "fieldcheck");
		Quad q0 = new GET(qf, q, t0, raf.field, raf.objref);
		Quad q1 = makeFlagConst(qf, q, t1, ty);
		Quad q2 = new OPER(qf, q, cmpop(ty), t1, new Temp[]{t0,t1});
		Quad q3 = new CJMP(qf, q, t1, new Temp[0]);
		in = addAt(in, q0);
		in = addAt(in, q1);
		in = addAt(in, q2);
		in = addAt(in, 0, q3, 1);
		// XXX handle case that field is not already correct.
	    }
	    // do array index checks where necessary.
	    for (Iterator it=co.checkArrayElement(q).iterator();it.hasNext();){
		CheckOracle.RefAndIndexAndType rit =
		    (CheckOracle.RefAndIndexAndType) it.next();
		Temp t0 = new Temp(tf, "arraycheck");
		Temp t1 = new Temp(tf, "arraycheck");
		Quad q0 = new AGET(qf, q, t0, rit.objref, rit.index, rit.type);
		Quad q1 = makeFlagConst(qf, q, t1, rit.type);
		Quad q2 = new OPER(qf, q, cmpop(rit.type), t1,
				   new Temp[] { t0, t1 } );
		Quad q3 = new CJMP(qf, q, t1, new Temp[0]);
		in = addAt(in, q0);
		in = addAt(in, q1);
		in = addAt(in, q2);
		in = addAt(in, 0, q3, 1);
		// XXX handle case that array element is not already correct.
	    }
	}

	private int cmpop(HClass type) {
	    if (!type.isPrimitive()) return Qop.ACMPEQ;
	    else if (type==HClass.Boolean || type==HClass.Byte ||
		     type==HClass.Char || type==HClass.Short ||
		     type==HClass.Int) return Qop.ICMPEQ;
	    else if (type==HClass.Long) return Qop.LCMPEQ;
	    else if (type==HClass.Float) return Qop.FCMPEQ;
	    else if (type==HClass.Double) return Qop.DCMPEQ;
	    else throw new Error("ACK: "+type);
	}	    
	// flag values.
	private static final long FLAG_VALUE = 0xCACACACACACACACAL;
	private static final Integer booleanFlag=new Integer((int)FLAG_VALUE&3);
	private static final Integer byteFlag = new Integer((byte)FLAG_VALUE);
	private static final Integer charFlag = new Integer((char)FLAG_VALUE);
	private static final Integer shortFlag= new Integer((short)FLAG_VALUE);
	private static final Integer intFlag = new Integer((int)FLAG_VALUE);
	private static final Long longFlag = new Long(FLAG_VALUE);
	private static final Float floatFlag = new Float(1976.0927);
	private static final Double doubleFlag = new Double(1976.0927);

	private Quad makeFlagConst(QuadFactory qf, HCodeElement src,
				    Temp dst, HClass type) {
	    if (!type.isPrimitive())
		/* address of FLAG_VALUE field is used as marker. */
		return new GET(qf, src, dst, HFflagvalue, null);
	    else if (type==HClass.Boolean) //XXX
		return new CONST(qf, src, dst, booleanFlag, HClass.Int);
	    else if (type==HClass.Byte)
		return new CONST(qf, src, dst, byteFlag, HClass.Int);
	    else if (type==HClass.Char)
		return new CONST(qf, src, dst, charFlag, HClass.Int);
	    else if (type==HClass.Short)
		return new CONST(qf, src, dst, shortFlag, HClass.Int);
	    else if (type==HClass.Int)
		return new CONST(qf, src, dst, intFlag, HClass.Int);
	    else if (type==HClass.Long)
		return new CONST(qf, src, dst, longFlag, HClass.Long);
	    else if (type==HClass.Float)
		return new CONST(qf, src, dst, floatFlag, HClass.Float);
	    else if (type==HClass.Double)
		return new CONST(qf, src, dst, doubleFlag, HClass.Double);
	    else throw new Error("ACK: "+type);
	}
    }
    private class TempSplitter {
	private final Map m = new HashMap();
	public Temp versioned(Temp t) {
	    if (!m.containsKey(t))
		m.put(t, new Temp(t));
	    return (Temp) m.get(t);
	}
    }
}
