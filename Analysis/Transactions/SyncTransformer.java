// SyncTransformer.java, created Fri Oct 27 16:50:14 2000 by cananian
// Copyright (C) 2000 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.Transactions;

import harpoon.Analysis.ClassHierarchy;
import harpoon.Analysis.DomTree;
import harpoon.Analysis.Counters.CounterFactory;
import harpoon.Analysis.Transactions.BitFieldNumbering.BitFieldTuple;
import harpoon.Backend.Generic.Frame;
import harpoon.ClassFile.CachingCodeFactory;
import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HClassMutator;
import harpoon.ClassFile.HCode;
import harpoon.ClassFile.HCodeAndMaps;
import harpoon.ClassFile.HCodeElement;
import harpoon.ClassFile.HCodeFactory;
import harpoon.ClassFile.HField;
import harpoon.ClassFile.HFieldMutator;
import harpoon.ClassFile.HMethod;
import harpoon.ClassFile.HMethodMutator;
import harpoon.ClassFile.Linker;
import harpoon.ClassFile.SerializableCodeFactory;
import harpoon.IR.Properties.CFGrapher;
import harpoon.IR.Properties.UseDefer;
import harpoon.IR.Quads.AGET;
import harpoon.IR.Quads.ANEW;
import harpoon.IR.Quads.ARRAYINIT;
import harpoon.IR.Quads.ASET;
import harpoon.IR.Quads.CALL;
import harpoon.IR.Quads.CJMP;
import harpoon.IR.Quads.CONST;
import harpoon.IR.Quads.Code;
import harpoon.IR.Quads.Edge;
import harpoon.IR.Quads.FOOTER;
import harpoon.IR.Quads.GET;
import harpoon.IR.Quads.HEADER;
import harpoon.IR.Quads.INSTANCEOF;
import harpoon.IR.Quads.METHOD;
import harpoon.IR.Quads.MONITORENTER;
import harpoon.IR.Quads.MONITOREXIT;
import harpoon.IR.Quads.MOVE;
import harpoon.IR.Quads.NEW;
import harpoon.IR.Quads.NOP;
import harpoon.IR.Quads.OPER;
import harpoon.IR.Quads.PHI;
import harpoon.IR.Quads.Qop;
import harpoon.IR.Quads.Quad;
import harpoon.IR.Quads.QuadFactory;
import harpoon.IR.Quads.QuadRSSx;
import harpoon.IR.Quads.QuadSSA;
import harpoon.IR.Quads.QuadSSI;
import harpoon.IR.Quads.QuadVisitor;
import harpoon.IR.Quads.RETURN;
import harpoon.IR.Quads.SET;
import harpoon.IR.Quads.THROW;
import harpoon.IR.Quads.TYPESWITCH;
import harpoon.Temp.Temp;
import harpoon.Temp.TempFactory;
import harpoon.Util.HClassUtil;
import harpoon.Util.ParseUtil;
import harpoon.Util.Util;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
/**
 * <code>SyncTransformer</code> transforms synchronized code to
 * atomic transactions.  Works on <code>QuadSSA</code> form.
 * Use the <code>SyncTransformer.treeCodeFactory()</code> to clean 
 * up the transformed code by doing low-level tree form optimizations.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: SyncTransformer.java,v 1.4 2002-04-10 03:01:43 cananian Exp $
 */
//XXX: we currently have this issue with the code:
// original input which looks like
//     t1 = AGET(t0, ...)
//     t2 = AGET(t1, ...)
// doesn't type check in the conversion to LowQuad form because t0
// is cast to Object[] type when it is created by getReadCommittedVersion().
// It "should" be Object[][] type.  Thus in the second AGET when it is
// deferenced t1 is of Object type when it should be of Object[] type.
// We *could* do a typeinfo pass before we enter SyncTransformer to
// generate "correct" types but this doesn't always get the properly
// precise type when run in *SSA form. We currently work around the problem by
// adding an extra type cast before the AGET.  This *should* be
// optimized out in most cases.

//XXX: could use sync-elimination analysis to remove unnecessary
//     atomic operations?  this would reduce the overall cost by a *lot*,
//     but it would make it much harder to come up w/ realistic benchmarks.
//     maybe barnes/water?
//  ACTUALLY we should skip ALL transformation on objects which are
//     marked as non-escaping.  this too would be a big win.

public class SyncTransformer
    extends harpoon.Analysis.Transformation.MethodSplitter {
    static final Token WITH_TRANSACTION = new Token("withtrans") {
	public Object readResolve() { return WITH_TRANSACTION; }
    };
    protected boolean isValidToken(Token which) {
	return super.isValidToken(which) || which==WITH_TRANSACTION;
    }

    // for statistics:
    private final boolean enabled = // turns off the transformation.
	!Boolean.getBoolean("harpoon.synctrans.disabled");
    private final boolean noFieldModification = // only do monitorenter/exit
	Boolean.getBoolean("harpoon.synctrans.nofieldmods");
    private final boolean noArrayModification = // only do regular objects
	Boolean.getBoolean("harpoon.synctrans.noarraymods");
    private final boolean useSmartFieldOracle = // dumb down field oracle
	!Boolean.getBoolean("harpoon.synctrans.nofieldoracle");
    private final boolean useSmartCheckOracle = // dumb down check oracle
	!Boolean.getBoolean("harpoon.synctrans.nocheckoracle");
    private final boolean useUniqueRWCounters = // high-overhead counters
	Boolean.getBoolean("harpoon.synctrans.uniquerwcounters");
    // this might have to be tweaked if we're using counters which are
    // added *before* SyncTransformer gets the code.
    private final boolean excludeCounters = true;

    // FieldOracle to use.
    final FieldOracle fieldOracle;
    // BitFieldNumbering to use
    final BitFieldNumbering bfn;

    /** Cache the <code>java.lang.Class</code> <code>HClass</code>. */
    private final HClass HCclass;
    /** Cache the <code>java.lang.reflect.Field</code> <code>HClass</code>. */
    private final HClass HCfield;
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
    private final HMethod HMrCommitted;
    private final HMethod HMwCommitted;
    private final HMethod HMmkVersion;
    private final HMethod HMsetReadFlag;
    private final HMethod HMsetWriteFlag;
    private final HMethod HMsetWriteFlagA;
    /** flag value */
    private final HField HFflagvalue;
    /** last *reading* transaction */
    private final HField HFlastRTrans;
    /** last *writing* transaction */
    private final HField HFlastWTrans;
    /** Set of safe methods. */
    private final Set safeMethods;
    /** Our version of the codefactory. */
    private final HCodeFactory hcf;

    /** Creates a <code>SyncTransformer</code> with no safe methods. */
    public SyncTransformer(HCodeFactory hcf, ClassHierarchy ch, Linker l,
			   HMethod mainM, Set roots) {
	this(hcf, ch, l, mainM, roots, Collections.EMPTY_SET);
    }
    /** Creates a <code>SyncTransformer</code> with a safe method set loaded
     *  from the specified resource name. */
    public SyncTransformer(HCodeFactory hcf, ClassHierarchy ch, Linker l,
			   HMethod mainM, Set roots,
			   String resourceName) {
	this(hcf, ch, l, mainM, roots, parseResource(l, resourceName));
    }
    /** Creates a <code>SyncTransformer</code> with the specified safe
     *  method set. */
    public SyncTransformer(HCodeFactory hcf, ClassHierarchy ch, Linker l,
			   HMethod mainM, Set roots,
			   Set safeMethods) {
	// hcf should be SSI. our input is SSA...
        super(harpoon.IR.Quads.QuadSSA.codeFactory(hcf), ch, false);
	// and output is NoSSA
	assert hcf.getCodeName()
		    .equals(harpoon.IR.Quads.QuadSSI.codename);
	assert super.codeFactory().getCodeName()
		    .equals(harpoon.IR.Quads.QuadRSSx.codename);
	this.safeMethods = safeMethods;
	this.HCclass = l.forName("java.lang.Class");
	this.HCfield = l.forName("java.lang.reflect.Field");
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
	int mod = Modifier.FINAL | Modifier.NATIVE;
	this.HMrVersion = objM.addDeclaredMethod
	    ("getReadableVersion", new HClass[] { HCcommitrec }, HCobj);
	HMrVersion.getMutator().addModifiers(mod);
	this.HMrwVersion = objM.addDeclaredMethod
	    ("getReadWritableVersion", new HClass[] { HCcommitrec }, HCobj);
	HMrwVersion.getMutator().addModifiers(mod);
	this.HMrCommitted = objM.addDeclaredMethod
	    ("getReadCommittedVersion", new HClass[0], HCobj);
	HMrCommitted.getMutator().addModifiers(mod);
	this.HMwCommitted = objM.addDeclaredMethod
	    ("getWriteCommittedVersion", new HClass[0], HCobj);
	HMwCommitted.getMutator().addModifiers(mod);
	this.HMmkVersion = objM.addDeclaredMethod
	    ("makeCommittedVersion", new HClass[0], HCobj);
	HMmkVersion.getMutator().addModifiers(mod);
	this.HMsetReadFlag = objM.addDeclaredMethod
	    ("setFieldReadFlag", new HClass[] { HCfield, HClass.Int },
	    HClass.Void);
	HMsetReadFlag.getMutator().addModifiers(mod);
	this.HMsetWriteFlag = objM.addDeclaredMethod
	    ("setFieldWriteFlag", new HClass[] { HCfield },
	    HClass.Void);
	HMsetWriteFlag.getMutator().addModifiers(mod);
	this.HMsetWriteFlagA = objM.addDeclaredMethod
	    ("setArrayElementWriteFlag", new HClass[] { HClass.Int, HCclass },
	    HClass.Void);
	HMsetWriteFlagA.getMutator().addModifiers(mod);
	// create a pair of instance fields in java.lang.Object for
	// statistics gathering.
	if (useUniqueRWCounters) {
	    this.HFlastRTrans=objM.addDeclaredField("lastRTrans", HCcommitrec);
	    this.HFlastWTrans=objM.addDeclaredField("lastWTrans", HCcommitrec);
	} else { this.HFlastRTrans=this.HFlastWTrans=null; }
	// create a static final field in java.lang.Object that will hold
	// our 'unique' value.
	this.HFflagvalue = objM.addDeclaredField("flagValue", HCobj);
	HFflagvalue.getMutator().addModifiers(Modifier.FINAL|Modifier.STATIC);

	// set up our field oracle.
	if (!useSmartFieldOracle) {
	    this.fieldOracle = new SimpleFieldOracle();
	} else {
	    // filter out HClasses from rootset
	    Set myroots = new HashSet(roots);
	    for (Iterator it=myroots.iterator(); it.hasNext(); )
		if (!(it.next() instanceof HMethod)) it.remove();
	    // create fieldoracle
	    this.fieldOracle = new GlobalFieldOracle(ch, mainM, myroots, hcf);
	}
	// set up our BitFieldNumbering (and create array-check fields in
	// all array classes)
	this.bfn = new BitFieldNumbering(l);
	for (Iterator it=ch.classes().iterator(); it.hasNext(); ) {
	    HClass hc = (HClass) it.next();
	    if (hc.isArray()) bfn.arrayBitField(hc);
	}

	// fixup code factory for 'known safe' methods.
	final HCodeFactory superfactory = super.codeFactory();
	assert superfactory.getCodeName().equals(QuadRSSx.codename);
	this.hcf = new CachingCodeFactory(new SerializableCodeFactory() {
	    public String getCodeName() { return superfactory.getCodeName(); }
	    public void clear(HMethod m) { superfactory.clear(m); }
	    public HCode convert(HMethod m) {
		if (Modifier.isNative(m.getModifiers()) &&
		    SyncTransformer.this.safeMethods
		    	.contains(select(m, ORIGINAL)) &&
		    select(select(m, ORIGINAL), WITH_TRANSACTION).equals(m))
		    // call the original, 'safe' method.
		    return redirectCode(m);
		else return superfactory.convert(m);
	    }
	});
    }
    // override parent's codefactory with ours! (which uses theirs)
    public HCodeFactory codeFactory() { return hcf; }

    protected String mutateDescriptor(HMethod hm, Token which) {
	if (which==WITH_TRANSACTION)
	    // add CommitRecord as first arg.
	    return "(" + HCcommitrec.getDescriptor() +
		hm.getDescriptor().substring(1);
	else return super.mutateDescriptor(hm, which);
    }
    protected HCodeAndMaps cloneHCode(HCode hc, HMethod newmethod) {
	// make SSA into RSSx.
	assert hc.getName().equals(QuadSSA.codename);
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
    protected String mutateCodeName(String codeName) {
	assert codeName.equals(QuadSSA.codename);
	return MyRSSx.codename;
    }
    protected HCode mutateHCode(HCodeAndMaps input, Token which) {
	HCode hc = input.hcode();
	HEADER qH = (HEADER) hc.getRootElement();
	FOOTER qF = qH.footer();
	METHOD qM = qH.method();
	// recursively decend the dominator tree, rewriting as we go.
	if (enabled &&
	    (! "harpoon.Runtime.Transactions".equals
	       (hc.getMethod().getDeclaringClass().getPackage())) &&
	    (!excludeCounters || ! "harpoon.Runtime.Counters".equals
	     (hc.getMethod().getDeclaringClass().getName()))) {
	    CheckOracle co = new SimpleCheckOracle(noArrayModification);
	    if (useSmartCheckOracle) {
		DomTree dt = new DomTree(hc, false);
		co = new DominatingCheckOracle(dt, co);
		co = new HoistingCheckOracle
		    (hc, CFGrapher.DEFAULT, UseDefer.DEFAULT, dt, co);
	    }
	    Tweaker tw = new Tweaker(co, qF, (which==WITH_TRANSACTION));
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
	private final Set typecheckset = new HashSet();
	final CheckOracle co;
	final FieldOracle fo;
	final TempSplitter ts=new TempSplitter();
	// mutable.
	FOOTER footer; // we attach new stuff to the footer.
	ListList handlers = null; // points to current abort handler
	Tweaker(CheckOracle co, FOOTER qF, boolean with_transaction) {
	    this.co = co;
	    this.fo = fieldOracle; // cache in this object.
	    this.footer = qF;
	    this.qf = qF.getFactory();
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
	    if (handlers.head==null) return e; // rethrow directly.
	    Temp tst = new Temp(tf);
	    e = addAt(e, new INSTANCEOF(qf, src, tst, tex, HCabortex));
	    e = addAt(e, new CJMP(qf, src, tst, new Temp[0]));
	    Quad q0 = new THROW(qf, src, tex);
	    Quad.addEdge((Quad)e.from(), 1, q0, 0);
	    handlers.head.add(q0);
	    CounterFactory.spliceIncrement(qf, q0.prevEdge(0),
					   "synctrans.aborts");
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
	    // fixup the dangling array type check edges
	    if (typecheckset.size()==0) return;
	    PHI phi = new PHI(qf, footer, new Temp[0], typecheckset.size()+1);
	    int i=0;
	    for (Iterator it=typecheckset.iterator(); it.hasNext(); ) {
		Edge in = ((NOP) it.next()).prevEdge(0);
		Quad.addEdge((Quad)in.from(), in.which_succ(), phi, i++);
	    }
	    // this is a hack: create an infinite loop.
	    // this path should never be executed.
	    Quad.addEdge(phi, 0, phi, i);
	}

	public void visit(Quad q) { addChecks(q); }
	public void visit(ANEW q) {
	    CounterFactory.spliceIncrement
		(qf, q.prevEdge(0), "synctrans.new_array");
	    visit((Quad)q);
	}
	public void visit(NEW q) {
	    CounterFactory.spliceIncrement
		(qf, q.prevEdge(0), "synctrans.new_object");
	    visit((Quad)q);
	}

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

	    // hack around for object info loss if receiver came from transact.
	    if (!q.isStatic())
		addTypeCheck(q.prevEdge(0), q, q.params(0),
			     q.method().getDeclaringClass());
	    // end hack.

	    if (handlers==null) return;
	    if (safeMethods.contains(q.method()) && !q.isVirtual())
		return; // it's safe. (this is an optimization)
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
	    checkForAbort(ncall.nextEdge(1), ncall, ncall.retex());
	    // done.
	}
	public void visit(MONITORENTER q) {
	    addChecks(q);
	    Edge in = q.prevEdge(0), out = q.nextEdge(0);
	    if (handlers==null)
		in = addAt(in, new CONST(qf, q, currtrans, null, HClass.Void));
	    // counters!
	    in = CounterFactory.spliceIncrement
		(qf, in, "synctrans.transactions");
	    if (handlers!=null)
		in = CounterFactory.spliceIncrement
		    (qf, in, "synctrans.nested_transactions");
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
	    assert handlers!=null : "MONITOREXIT not dominated by "+
			"MONITORENTER in "+q.getFactory().getParent();
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
	// at entry, we've already done the (initial?) read, of the
	// original object.  The 'out' edge is the edge immediately
	// following the read, and we'll return an edge on which we
	// want to insert a "correcting" read of the versioned object.
	// all this method does is insert code to determine whether
	// we need to execute the correcting read or bypass it.
	Edge readCheck(Edge out, HCodeElement src,
			  Temp dst, Temp objectref, HClass type) {
	    // if value read==FLAG, then do correcting read.
	    Temp t0 = new Temp(tf, "readnt");
	    out = addAt(out, makeFlagConst(qf, src, t0, type));
	    out = addAt(out, new OPER(qf, src, cmpop(type), t0,
				      new Temp[]{ t0, dst }));
	    Quad q0 = new CJMP(qf, src, t0, new Temp[0]);
	    out = addAt(out, q0);
	    Quad q1 = new PHI(qf, src, new Temp[0], 2);
	    out = addAt(out, q1);
	    // transactional case: put another read here.
	    if (handlers!=null) return Quad.addEdge(q0, 1, q1, 1);
	    // non-transactional case: call fixup method first.
	    // call 'getReadCommittedVersion' for the most recently
	    // committed version of this object (probably the 'backup'
	    // copy of the object)
	    Quad q2 = new CALL(qf, src, HMrCommitted,
			       new Temp[] { objectref },
			       ts.versioned(objectref), retex,
			       false, false, new Temp[0]);
	    Quad q3 = new THROW(qf, src, retex);
	    Quad.addEdge(q0, 1, q2, 0);
	    Quad.addEdge(q2, 0, q1, 1);
	    Quad.addEdge(q2, 1, q3, 0);
	    footer = footer.attach(q3, 0); // add q4 to FOOTER
	    // no check for abort because we are non-trans!
	    return q2.nextEdge(0);// new READ goes on this edge.
	}
	public void visit(AGET q) {
	    addChecks(q);
	    if (noFieldModification || noArrayModification) return;
	    addUniqueRWCounters(q.prevEdge(0), q, q.objectref(), true, true);
	    CounterFactory.spliceIncrement
		(qf, q.prevEdge(0),(handlers==null) ?
		 "synctrans.read_nt_array" : "synctrans.read_t_array");
	    Edge e = readCheck(q.nextEdge(0), q,
			       q.dst(), q.objectref(), q.type());
	    e = addArrayTypeCheck(e, q, ts.versioned(q.objectref()),
				  q.type());
	    addAt(e, new AGET(qf, q, q.dst(),
			      ts.versioned(q.objectref()),
			      q.index(), q.type()));
	    // workaround for multi-dim arrays. yucky.
	    addArrayTypeCheck(q.prevEdge(0), q, q.objectref(), q.type());
	    // done.
	}
	public void visit(GET q) {
	    addChecks(q);
	    if (noFieldModification) return;
	    if (handlers==null &&
		!fo.isSyncRead(q.field()) && !fo.isSyncWrite(q.field())) {
		// we can simply read/write fields with no sync access
		CounterFactory.spliceIncrement
		    (qf, q.prevEdge(0), "synctrans.read_nt_skipped");
		return;
	    }
	    if (q.isStatic()) {
		if (handlers==null) return;
		System.err.println("WARNING: read of "+q.field()+" in "+
				   qf.getMethod());
		return;
	    } else
	    addUniqueRWCounters(q.prevEdge(0), q, q.objectref(), true, false);
	    CounterFactory.spliceIncrement
		(qf, q.prevEdge(0),(handlers==null) ?
		 "synctrans.read_nt_object" : "synctrans.read_t_object");
	    Edge e = readCheck(q.nextEdge(0), q,
			       q.dst(), q.objectref(),
			       q.field().getType());
	    addAt(e, new GET(qf, q, q.dst(), q.field(),
			     ts.versioned(q.objectref())));
	    // done.
	}
        // this routine does the necessary checks to see if any
	// transactions need to be aborted as a result of a non-transactional
	// write.  at entry, oldval contains the previous value of the
	// field; if it is FLAG it means someone has written (or read?) this
	// inside a transaction. if newval==FLAG, we need to create a
	// (committed) version object to put this value in.
	// readbit==1 implies that oldval==FLAG, apparently.
	void writeNonTrans(Edge out, HCodeElement src, Temp objectref,
			   Temp oldval, Temp newval, HClass type) {
	    // we only have to abort things if the field has been
	    // *read* by a transaction.  If it's been written, we
	    // can write to our special 'committed' version without
	    // aborting anything.
	    // XXX think about this one: obviously better to be able
	    // to check just one flag, right?  can we gain anything
	    // by making the 'read=0, field=FLAG' case special?
	    Temp t0 = new Temp(tf, "writent");
	    out = addAt(out, makeFlagConst(qf, src, t0, type));
	    out = addAt(out, new OPER(qf, src, cmpop(type), oldval,
				      new Temp[]{ oldval, t0 }));
	    Quad q0 = new CJMP(qf, src, oldval, new Temp[0]);
	    out = addAt(out, q0);
	    out = addAt(out, new OPER(qf, src, cmpop(type), oldval,
				      new Temp[]{ newval, t0 }));
	    Quad q1 = new CJMP(qf, src, oldval, new Temp[0]);
	    out = addAt(out, q1);
	    out = addAt(out, new MOVE(qf, src, ts.versioned(objectref),
				      objectref));
	    Quad q2 = new PHI(qf, src, new Temp[0], 3);
	    out = addAt(out, q2);
	    // now handle exceptional cases:
	    Quad q3 = new CALL(qf, src, HMwCommitted,
			       new Temp[] { objectref },
			       ts.versioned(objectref), retex,
			       false, false, new Temp[0]);
	    Quad q4 = new THROW(qf, src, retex);
	    Quad.addEdge(q0, 1, q3, 0);
	    Quad.addEdge(q3, 0, q2, 1);
	    Quad.addEdge(q3, 1, q4, 0);
	    footer = footer.attach(q4, 0); // add q4 to FOOTER
	    // no check for abort because we are non-trans!
	    Quad q5 = new CALL(qf, src, HMmkVersion,
			       new Temp[] { objectref },
			       ts.versioned(objectref), retex,
			       false, false, new Temp[0]);
	    Quad q6 = new THROW(qf, src, retex);
	    Quad.addEdge(q1, 1, q5, 0);
	    Quad.addEdge(q5, 0, q2, 2);
	    Quad.addEdge(q5, 1, q6, 0);
	    footer = footer.attach(q6, 0); // add q6 to FOOTER.
	}
	public void visit(ASET q) {
	    addChecks(q);
	    if (noFieldModification || noArrayModification) return;
	    addUniqueRWCounters(q.prevEdge(0), q, q.objectref(), false, true);
	    CounterFactory.spliceIncrement
		(qf, q.prevEdge(0),(handlers==null) ?
		 "synctrans.write_nt_array" : "synctrans.write_t_array");
	    if (handlers==null) { // non-transactional write
		Temp t0 = new Temp(tf, "oldval");
		Edge in = q.prevEdge(0);
		in = addArrayTypeCheck(in, q, q.objectref(), q.type());//workaround
		in = addAt(in, new AGET(qf, q, t0, q.objectref(),
					q.index(), q.type()));
		writeNonTrans(in, q, q.objectref(), t0, q.src(), q.type());
	    }
	    // both transactional and non-transactional write.
	    ASET q0 = new ASET(qf, q, ts.versioned(q.objectref()),
			       q.index(), q.src(), q.type());
	    Quad.replace(q, q0);
	    addArrayTypeCheck(q0.prevEdge(0), q0, q0.objectref(), q0.type());
	}
	public void visit(SET q) {
	    addChecks(q);
	    if (noFieldModification) return;
	    if (handlers==null &&
		!fo.isSyncRead(q.field()) && !fo.isSyncWrite(q.field())) {
		// we can simply read/write fields with no sync access
		CounterFactory.spliceIncrement
		    (qf, q.prevEdge(0), "synctrans.write_nt_skipped");
		return;
	    }
	    if (q.isStatic()) {
		if (handlers==null) return;
		System.err.println("WARNING: write of "+q.field()+" in "+
				   qf.getMethod());
		return;
	    } else
	    addUniqueRWCounters(q.prevEdge(0), q, q.objectref(), false, false);
	    CounterFactory.spliceIncrement
		(qf, q.prevEdge(0),(handlers==null) ?
		 "synctrans.write_nt_object" : "synctrans.write_t_object");

	    // always write to 'versioned' copy of object (which may well
	    // be the object itself).  In non-transactional case, we
	    // need to read the field first and do special checks to
	    // determine whether aborting some transaction in progress is
	    // necessary.
	    if (handlers==null) { // non-transactional write
		Temp t0 = new Temp(tf, "oldval");
		Edge in = q.prevEdge(0);
		in = addAt(in, new GET(qf, q, t0, q.field(), q.objectref()));
		writeNonTrans(in, q, q.objectref(), t0, q.src(),
			      q.field().getType());
	    }
	    // both transactional and non-transactional write.
	    Quad.replace(q, new SET(qf, q, q.field(),
				    ts.versioned(q.objectref()),
				    q.src()));
	}
	public void visit(ARRAYINIT q) {
	    addChecks(q);
	    if (noFieldModification || noArrayModification) return;
	    // XXX: we don't handle ARRAYINIT yet.
	    assert false : "ARRAYINIT transformation unimplemented.";
	}
	// add a type check to edge e so that TypeInfo later knows the
	// component type of this array access.
	Edge addArrayTypeCheck(Edge e, HCodeElement src, Temp t,
				 HClass type) {
	    HClass arraytype = HClassUtil.arrayClass(qf.getLinker(), type, 1);
	    return addTypeCheck(e, src, t, arraytype);
	}
	Edge addTypeCheck(Edge e, HCodeElement src, Temp t, HClass type) {
	    Quad q0 = new TYPESWITCH(qf, src, t, new HClass[] { type },
				     new Temp[0], true);
	    Quad q1 = new NOP(qf, src);
	    Quad.addEdge(q0, 1, q1, 0);
	    typecheckset.add(q1);
	    return addAt(e, 0, q0, 0);
	}

	void addChecks(Quad q) {
	    // don't add checks if we're not currently in transaction context.
	    if (handlers==null) return;
	    // don't add checks if we're compiling monitorenter/exit stats.
	    if (noFieldModification) return;
	    // only deal with quads where "just before" makes sense.
	    if (q.prevLength()!=1) {
		assert co.createReadVersions(q).size()==0;
		assert co.createWriteVersions(q).size()==0;
		assert co.checkFieldReads(q).size()==0;
		assert co.checkFieldWrites(q).size()==0;
		assert co.checkArrayElementReads(q).size()==0;
		assert co.checkArrayElementWrites(q).size()==0;
		return;
	    }
	    Edge in = q.prevEdge(0);
	    // create read/write versions for objects that need it.
	    Set rS = co.createReadVersions(q);
	    Set wS = co.createWriteVersions(q);
	    rS.removeAll(wS); // write really is read-write.
	    for (int i=0; i<2; i++) {
		// iteration 0 for read; iteration 1 for write versions.
		Iterator it = (i==0) ? rS.iterator() : wS.iterator();
		HMethod hm = (i==0) ? HMrVersion : HMrwVersion ;
		while (it.hasNext()) {
		    // for each temp, a call to 'getReadableVersion' or
		    // 'getReadWritableVersion'.
		    Temp t = (Temp) it.next();
		    CALL q0= new CALL(qf, q, hm, new Temp[] { t, currtrans },
		                      ts.versioned(t), retex,
				      false/*final, not virtual*/, false,
				      new Temp[0]);
		    THROW q1= new THROW(qf, q, retex);
		    in = addAt(in, q0);
		    Quad.addEdge(q0, 1, q1, 0);
		    footer = footer.attach(q1, 0);
		    checkForAbort(q0.nextEdge(1), q, retex);
		    in = CounterFactory.spliceIncrement
			(qf, in,
			 "synctrans."+((i==0)?"read":"write")+"_versions");
		}
	    }
	    // do field-read checks...
	    for (Iterator it=co.checkFieldReads(q).iterator(); it.hasNext();) {
		CheckOracle.RefAndField raf=(CheckOracle.RefAndField)it.next();
		// skip check for fields unaccessed outside a sync context.
		if (!fo.isUnsyncRead(raf.field) &&
		    !fo.isUnsyncWrite(raf.field)) {
		    in = CounterFactory.spliceIncrement
			(qf, in, "synctrans.field_read_checks_skipped");
		    continue;
		}
		in = CounterFactory.spliceIncrement
		    (qf, in, "synctrans.field_read_checks");
		// create read check code (set read-bit to one).
		// (check that read-bit is set, else call fixup code,
		//  which will do atomic-set of this bit.)
		BitFieldTuple bft = bfn.bfLoc(raf.field);
		Temp t0 = new Temp(tf, "readcheck");
		Temp t1 = new Temp(tf, "readcheck");
		Quad q0 = new GET(qf, q, t0, bft.field, raf.objref);
		Quad q1 = new CONST(qf, q, t1,
				    new Integer(1<<bft.bit), HClass.Int);
		Quad q2 = new OPER(qf, q, Qop.IAND, t0, new Temp[]{t0,t1});
		Quad q3 = new OPER(qf, q, Qop.ICMPEQ, t0, new Temp[]{t0,t1});
		Quad q4 = new CJMP(qf, q, t0, new Temp[0]);
		Quad q5 = new PHI(qf, q, new Temp[0], 2);
		in = addAt(in, q0);
		in = addAt(in, q1);
		in = addAt(in, q2);
		in = addAt(in, q3);
		in = addAt(in, 0, q4, 1);
		in = addAt(in, q5);
		// handle case that field is not already correct.
		Quad q6 = new CONST(qf, q, t0, bft.field, HCfield);
		Quad q7 = new CALL(qf, q, HMsetReadFlag,
				   new Temp[] { raf.objref, t0, t1 },
				   null, retex, false, false, new Temp[0]);
		Quad q8 = new THROW(qf, q, retex);
		Quad.addEdges(new Quad[] { q4, q6, q7 });
		Quad.addEdge(q7, 0, q5, 1);
		Quad.addEdge(q7, 1, q8, 0);
		footer = footer.attach(q8, 0);
		checkForAbort(q7.nextEdge(1), q, retex);
		CounterFactory.spliceIncrement
		    (qf, q6.prevEdge(0), "synctrans.field_read_checks_bad");
	    }
	    // do field-write checks...
	    for (Iterator it=co.checkFieldWrites(q).iterator(); it.hasNext();){
		CheckOracle.RefAndField raf=(CheckOracle.RefAndField)it.next();
		// skip check for fields unaccessed outside a sync context.
		if (!fo.isUnsyncRead(raf.field) &&
		    !fo.isUnsyncWrite(raf.field)) {
		    in = CounterFactory.spliceIncrement
			(qf, in, "synctrans.field_write_checks_skipped");
		    continue;
		}
		in = CounterFactory.spliceIncrement
		    (qf, in, "synctrans.field_write_checks");
		// create write check code (set field to FLAG).
		// (check that field==FLAG is set, else call fixup code)
		HClass ty = raf.field.getType();
		Temp t0 = new Temp(tf, "writecheck");
		Temp t1 = new Temp(tf, "writecheck");
		Quad q0 = new GET(qf, q, t0, raf.field, raf.objref);
		Quad q1 = makeFlagConst(qf, q, t1, ty);
		Quad q2 = new OPER(qf, q, cmpop(ty), t1, new Temp[]{t0,t1});
		Quad q3 = new CJMP(qf, q, t1, new Temp[0]);
		Quad q4 = new PHI(qf, q, new Temp[0], 2);
		in = addAt(in, q0);
		in = addAt(in, q1);
		in = addAt(in, q2);
		in = addAt(in, 0, q3, 1);
		in = addAt(in, q4);
		// handle case that field is not already correct.
		Quad q5 = new CONST(qf, q, t0, raf.field, HCfield);
		Quad q7 = new CALL(qf, q, HMsetWriteFlag,
				   new Temp[] { raf.objref, t0 },
				   null, retex, false, false, new Temp[0]);
		Quad q8 = new THROW(qf, q, retex);
		Quad.addEdges(new Quad[] { q3, q5, q7 });
		Quad.addEdge(q7, 0, q4, 1);
		Quad.addEdge(q7, 1, q8, 0);
		footer = footer.attach(q8, 0);
		checkForAbort(q7.nextEdge(1), q, retex);
		CounterFactory.spliceIncrement
		    (qf, q5.prevEdge(0), "synctrans.field_write_checks_bad");
	    }
	    // do array index read checks....
	    for (Iterator it=co.checkArrayElementReads(q).iterator();
		 it.hasNext(); ) {
		// arrays have one check field (32 bits) which stand in
		// (modulo 32) for all the elements in the array.
		// we check that the appropriate read-bit is set, else
		// call the fixup code, which will do an atomic-set of 
		// the bit.
		in = CounterFactory.spliceIncrement
		    (qf, in, "synctrans.element_read_checks");
		CheckOracle.RefAndIndexAndType rit =
		    (CheckOracle.RefAndIndexAndType) it.next();
		HField arrayCheckField = bfn.arrayBitField
		    (HClassUtil.arrayClass(qf.getLinker(), rit.type, 1));
		Temp t0 = new Temp(tf, "arrayreadcheck");
		Temp t1 = new Temp(tf, "arrayreadcheck");
		Temp t2 = new Temp(tf, "arrayreadcheck");
		Quad q0 = new GET(qf, q, t0, arrayCheckField, rit.objref);
		Quad q1a= new CONST(qf, q, t1, new Integer(31), HClass.Int);
		Quad q1b= new OPER(qf, q, Qop.IAND, t1,
				   new Temp[]{ rit.index, t1 });
		Quad q1c= new CONST(qf, q, t2, new Integer(1), HClass.Int);
		Quad q1d= new OPER(qf, q, Qop.ISHL, t1, new Temp[]{t2, t1});
		Quad q2 = new OPER(qf, q, Qop.IAND, t0, new Temp[]{t0,t1});
		Quad q3 = new OPER(qf, q, Qop.ICMPEQ, t0, new Temp[]{t0,t1});
		Quad q4 = new CJMP(qf, q, t0, new Temp[0]);
		Quad q5 = new PHI(qf, q, new Temp[0], 2);
		in = addAt(in, q0);
		in = addAt(in, q1a);
		in = addAt(in, q1b);
		in = addAt(in, q1c);
		in = addAt(in, q1d);
		in = addAt(in, q2);
		in = addAt(in, q3);
		in = addAt(in, 0, q4, 1);
		in = addAt(in, q5);
		// handle case that field is not already correct.
		Quad q6 = new CONST(qf, q, t0, arrayCheckField, HCfield);
		Quad q7 = new CALL(qf, q, HMsetReadFlag,
				   new Temp[] { rit.objref, t0, t1 },
				   null, retex, false, false, new Temp[0]);
		Quad q8 = new THROW(qf, q, retex);
		Quad.addEdges(new Quad[] { q4, q6, q7 });
		Quad.addEdge(q7, 0, q5, 1);
		Quad.addEdge(q7, 1, q8, 0);
		footer = footer.attach(q8, 0);
		checkForAbort(q7.nextEdge(1), q, retex);
		CounterFactory.spliceIncrement
		    (qf, q6.prevEdge(0), "synctrans.element_read_checks_bad");
	    }
	    // do array index write checks.
	    for (Iterator it=co.checkArrayElementWrites(q).iterator();
		 it.hasNext(); ) {
		// (check that element==FLAG is set, else call fixup code)
		in = CounterFactory.spliceIncrement
		    (qf, in, "synctrans.element_write_checks");
		CheckOracle.RefAndIndexAndType rit =
		    (CheckOracle.RefAndIndexAndType) it.next();
		Temp t0 = new Temp(tf, "arraywritecheck");
		Temp t1 = new Temp(tf, "arraywritecheck");
		Quad q0 = new AGET(qf, q, t0, rit.objref, rit.index, rit.type);
		Quad q1 = makeFlagConst(qf, q, t1, rit.type);
		Quad q2 = new OPER(qf, q, cmpop(rit.type), t1,
				   new Temp[] { t0, t1 } );
		Quad q3 = new CJMP(qf, q, t1, new Temp[0]);
		Quad q4 = new PHI(qf, q, new Temp[0], 2);
		in = addAt(in, q0);
		in = addAt(in, q1);
		in = addAt(in, q2);
		in = addAt(in, 0, q3, 1);
		in = addAt(in, q4);
		// handle case that array element is not already correct.
		Quad q6 = new CONST(qf, q, t1, rit.type, HCclass);
		Quad q7 = new CALL(qf, q, HMsetWriteFlagA,
				   new Temp[] { rit.objref, rit.index, t1 },
				   null, retex, false, false, new Temp[0]);
		Quad q8 = new THROW(qf, q, retex);
		Quad.addEdges(new Quad[] { q3, q6, q7 });
		Quad.addEdge(q7, 0, q4, 1);
		Quad.addEdge(q7, 1, q8, 0);
		footer = footer.attach(q8, 0);
		checkForAbort(q7.nextEdge(1), q, retex);
		CounterFactory.spliceIncrement
		    (qf, q6.prevEdge(0), "synctrans.element_write_checks_bad");
	    }
	}
	private Edge addUniqueRWCounters(Edge in, HCodeElement src, Temp Tobj,
					 boolean isRead, boolean isArray) {
	    if (!useUniqueRWCounters) return in; // disabled
	    if (handlers==null) return in; // not a transaction.
	    String suffix = isArray ? "_array" : "_object";
	    // check whether last read/written is the same as this transaction.
	    Temp Ttmp0 = new Temp(tf);
	    in = addAt(in, new GET(qf, src, Ttmp0, HFlastRTrans, Tobj));
	    Temp TsameR = new Temp(tf);
	    in = addAt(in, new OPER(qf, src, Qop.ACMPEQ, TsameR,
				    new Temp[] { Ttmp0, currtrans }));
	    Temp Ttmp1 = new Temp(tf);
	    in = addAt(in, new GET(qf, src, Ttmp1, HFlastWTrans, Tobj));
	    Temp TsameW = new Temp(tf);
	    in = addAt(in, new OPER(qf, src, Qop.ACMPEQ, TsameW,
				    new Temp[] { Ttmp1, currtrans }));
	    // update last read/written according to whether we're r/writing.
	    in = addAt(in, new SET(qf, src, isRead? HFlastRTrans: HFlastWTrans,
				   Tobj, currtrans));
	    // test for counters
	    Quad q0 = new CJMP(qf, src, isRead ? TsameR : TsameW, new Temp[0]);
	    Quad q1 = new CJMP(qf, src, isRead ? TsameW : TsameR, new Temp[0]);
	    Quad q2 = new PHI(qf, src, new Temp[0], 3);
	    Quad.addEdge(q0, 0, q1, 0);
	    Quad.addEdge(q0, 1, q2, 0);
	    Quad.addEdge(q1, 0, q2, 1);
	    Quad.addEdge(q1, 1, q2, 2);
	    Quad.addEdge((Quad)in.from(), in.which_succ(), q0, 0);
	    Quad.addEdge(q2, 0, (Quad)in.to(), in.which_pred());
	    // increment counters
	    CounterFactory.spliceIncrement
		(qf, q1.nextEdge(0), "synctrans." +
		 (isRead ? "virgin_read" : "virgin_write") + suffix);
	    CounterFactory.spliceIncrement
		(qf, q1.nextEdge(1), "synctrans." +
		 (isRead ? "read_of_written" : "write_of_read") + suffix);
	    // done.
	    return q2.nextEdge(0);
	}
	// make a non-static equivalent field for static fields.
	// (IDEAS: new object type for static fields of each class.
	//  then only one static field per class, which points to
	//  the (singleton) object-of-fields.  One dereference, but
	//  from this point the fields behave 'normally'.  Since the
	//  single static field is now final, no transactions need
	//  be done on it.) [maybe this is a separate pre-pass]
	private HField nonstatic(HField hf) {
	    if (!hf.isStatic()) return hf;
	    HClass hc = hf.getDeclaringClass();
	    // XXX: how exactly does this work?
	    return null;
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
    }
    // flag values: repeating sequence of single bytes, must be valid double
    // and float (ie, not 00 or FF)
    private static final long FLAG_VALUE = 0xCACACACACACACACAL;
    private static final Integer booleanFlag=new Integer((byte)FLAG_VALUE);
    private static final Integer byteFlag = new Integer((byte)FLAG_VALUE);
    private static final Integer charFlag = new Integer((char)FLAG_VALUE);
    private static final Integer shortFlag= new Integer((short)FLAG_VALUE);
    private static final Integer intFlag = new Integer((int)FLAG_VALUE);
    private static final Long longFlag = new Long(FLAG_VALUE);
    private static final Float floatFlag =
	new Float(Float.intBitsToFloat(intFlag.intValue()));
    private static final Double doubleFlag =
	new Double(Double.longBitsToDouble(longFlag.longValue()));
    
    private Quad makeFlagConst(QuadFactory qf, HCodeElement src,
			       Temp dst, HClass type) {
	if (!type.isPrimitive())
	    /* value in FLAG_VALUE field is used as marker. */
	    /* (a post-pass converts this to a constant) */
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

    /** Return an <code>HCodeFactory</code> that will clean up the
     *  tree form of the transformed code by performing some optimizations
     *  which can't be represented in quad form. */
    public HCodeFactory treeCodeFactory(Frame f, HCodeFactory hcf) {
	return new TreePostPass(f, FLAG_VALUE, HFflagvalue).codeFactory(hcf);
    }
    /** Create a redirection method for native methods we "know" are safe. */
    // parts borrowed from InitializerTransform.java
    private QuadRSSx redirectCode(final HMethod hm) {
	final HMethod orig = select(hm, ORIGINAL);
	// make the Code for this method (note how we work around the
	// protected fields).
	return new QuadRSSx(hm, null) { /* constructor */ {
	    // figure out how many temps we need, then make them.
	    int nargs = hm.getParameterTypes().length + (hm.isStatic()? 0: 1);
	    Temp[] params = new Temp[nargs];
	    for (int i=0; i<params.length; i++)
		params[i] = new Temp(qf.tempFactory(), "param"+i);
	    Temp[] nparams = new Temp[nargs-1];
	    int i=0;
	    if (!hm.isStatic())
		nparams[i++] = params[0];
	    for (i++ ; i<params.length; i++)
		nparams[i-1] = params[i];
	    Temp retex = new Temp(qf.tempFactory(), "retex");
	    Temp retval = (hm.getReturnType()==HClass.Void) ? null :
		new Temp(qf.tempFactory(), "retval");
	    // okay, make the dispatch core.
	    Quad q0 = new HEADER(qf, null);
	    Quad q1 = new METHOD(qf, null, params, 1);
	    Quad q2 = new CALL(qf, null, orig, nparams,
	    		       retval, retex, false, true, new Temp[0]);
	    Quad q3 = new RETURN(qf, null, retval);
	    Quad q4 = new THROW(qf, null, retex);
	    Quad q5 = new FOOTER(qf, null, 3);
	    Quad.addEdge(q0, 0, q5, 0);
	    Quad.addEdge(q0, 1, q1, 0);
	    Quad.addEdge(q1, 0, q2, 0);
	    Quad.addEdge(q2, 0, q3, 0);
	    Quad.addEdge(q2, 1, q4, 0);
	    Quad.addEdge(q3, 0, q5, 1);
	    Quad.addEdge(q4, 0, q5, 2);
	    this.quads = q0;
	    // done!
	} };
    }
    
    private class TempSplitter {
	private final Map m = new HashMap();
	public Temp versioned(Temp t) {
	    if (!m.containsKey(t))
		m.put(t, new Temp(t));
	    return (Temp) m.get(t);
	}
    }

    private static Set parseResource(final Linker l, String resourceName) {
	final Set result = new HashSet();
	try {
	    ParseUtil.readResource(resourceName, new ParseUtil.StringParser() {
		public void parseString(String s)
		    throws ParseUtil.BadLineException {
		    result.add(ParseUtil.parseMethod(l, s));
		}
	    });
	} catch (java.io.IOException ex) {
	    System.err.println("ERROR READING SAFE SET, SKIPPING REST.");
	    System.err.println(ex.toString());
	}
	// done.
	return result;
    }
}
