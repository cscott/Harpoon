// SyncTransformer.java, created Fri Oct 27 16:50:14 2000 by cananian
// Copyright (C) 2000 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.Transactions;

import harpoon.Analysis.ClassHierarchy;
import harpoon.Analysis.DomTree;
import harpoon.Analysis.Counters.CounterFactory;
import harpoon.Analysis.Maps.ExactTypeMap;
import harpoon.Analysis.Maps.ExactTypeMapProxy;
import harpoon.Analysis.Quads.TypeInfo;
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
import harpoon.IR.Quads.SSIToSSA;
import harpoon.IR.Quads.THROW;
import harpoon.IR.Quads.TYPESWITCH;
import harpoon.Temp.Temp;
import harpoon.Temp.TempFactory;
import harpoon.Temp.TempMap;
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
 * atomic transactions.  Works on <code>QuadSSI</code> form
 * (via an internal conversion to SSA).  Outputs <code>QuadRSSx</code>.
 * Use the <code>SyncTransformer.treeCodeFactory()</code> to clean 
 * up the transformed code by doing low-level tree form optimizations.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: SyncTransformer.java,v 1.5.2.8 2003-07-15 17:09:39 cananian Exp $
 */
//     we can apply sync-elimination analysis to remove unnecessary
//     atomic operations.  this may reduce the overall cost by a *lot*,
//     but it would make it much harder to come up w/ realistic benchmarks.
//     maybe barnes/water? (actually, only about 30% syncs are eliminated)
//  ACTUALLY we should skip ALL transformation on objects which are
//     marked as non-escaping.  this too would be a big win.
//     (not done yet)

// stages:
// X1) guts into C code.
// X2) code dup for sync check (actually, separate pass)
//  3) indirectize trans to support object.wait()
//  4) separate out sync transform and implement general trans mech.
//  5) handle 'recovery' transaction?
//  6) separate pass to virtualize static fields?
//  7) strictness optimizations?
//  8) changes to caching read version over commit of subtransaction
//     (possibly parallel commit?)

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
    /** Cache the <code>java.lang.Object</code> <code>HClass</code>. */
    private final HClass HCobj;
    /** Cache the <code>struct vinfo *</code> <code>HClass</code>. */
    private final HClass HCvinfo;
    /** Cache the <code>CommitRecord</code> <code>HClass</code>. */
    private final HClass  HCcommitrec;
    private final HMethod HMcommitrec_new;
    private final HMethod HMcommitrec_retry;
    private final HMethod HMcommitrec_commit;
    private final HField  HFcommitrec_parent;
    private final HClass  HCabortex;
    private final HField  HFabortex_upto;
    private final HMethod HMabortex_cons;
    /** flag value */
    private final HField HFflagvalue;
    /** last *reading* transaction */
    private final HField HFlastRTrans;
    /** last *writing* transaction */
    private final HField HFlastWTrans;
    /** Set of safe methods. */
    private final Set<HMethod> safeMethods;
    /** Our version of the codefactory. */
    private final HCodeFactory hcf;

    private final Linker linker;
    private final MethodGenerator gen;
    private final Set<HField> transFields = new HashSet<HField>();

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
			   Set<HMethod> safeMethods) {
	// our input is SSI.  We'll convert it to SSA in the 'clone' method.
        super(hcf, ch, false);
	// and output is RSSx
	assert hcf.getCodeName()
		    .equals(harpoon.IR.Quads.QuadSSI.codename);
	assert super.codeFactory().getCodeName()
		    .equals(harpoon.IR.Quads.QuadRSSx.codename);
	this.linker = l;
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
	this.HMabortex_cons =
	    HCabortex.getConstructor(new HClass[] { HCcommitrec });
	// now create methods of java.lang.Object.
	HCobj = l.forName("java.lang.Object");
	HClassMutator objM = HCobj.getMutator();
	int mod = Modifier.FINAL | Modifier.NATIVE;
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

	// lookup the type of a 'struct vinfo *'
	HCvinfo = HCobj; // hack.  Also not strictly true. Maybe HClass.Int?
	// create a method generator in the 'ImplHelper' class.
	gen = new MethodGenerator(l.forName(pkg+"ImplHelper"));

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
	for (Iterator<HClass> it=ch.classes().iterator(); it.hasNext(); ) {
	    HClass hc = it.next();
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
		else if (gen.generatedMethodSet.contains(m))
		    return emptyCode(m);
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
    protected MyHCodeAndMaps cloneHCode(HCode hc, HMethod newmethod) {
	// make SSI into RSSx.
	assert hc.getName().equals(QuadSSI.codename);
	return MyRSSx.cloneToRSSx((harpoon.IR.Quads.Code)hc, newmethod);
    }
    private static class MyRSSx extends QuadRSSx {
	private MyRSSx(HMethod m) { super(m, null); }
	public static MyHCodeAndMaps cloneToRSSx(harpoon.IR.Quads.Code c,
					       HMethod m) {
	    // c should be SSI.
	    assert c.getName().equals(QuadSSI.codename) : c.getName();
	    // we first create a type info on the SSI form.
	    TypeInfo ti = new TypeInfo(c);
	    // now we convert to SSA
	    MyRSSx r = new MyRSSx(m);
	    SSIToSSA ssi2ssa = new SSIToSSA(c, r.qf);
	    r.quads = ssi2ssa.rootQuad;
	    r.setAllocationInformation(ssi2ssa.allocInfo);
	    // make the HCodeAndMaps
	    return new MyHCodeAndMaps
		(r, ssi2ssa.quadMap, ssi2ssa.tempMap,
		 c, ssi2ssa.revQuadMap, ssi2ssa.revTempMap,
		 ti);
	}
    }
    static class MyHCodeAndMaps extends HCodeAndMaps<Quad> {
	final ExactTypeMap<Quad> ancestorTypeMap;
	final ExactTypeMap<Quad> typeMap;
	MyHCodeAndMaps(Code hc, Map<Quad,Quad> em, TempMap tm,
		       Code ahc, Map<Quad,Quad> aem, TempMap atm,
		       ExactTypeMap<Quad> ancestorTypeMap) {
	    super(hc, em, tm, ahc, aem, atm);
	    this.ancestorTypeMap = ancestorTypeMap;
	    this.typeMap = new ExactTypeMapProxy<Quad>(this, ancestorTypeMap);
	}
    }

    protected String mutateCodeName(String codeName) {
	assert codeName.equals(QuadSSI.codename);
	return MyRSSx.codename;
    }
    protected HCode mutateHCode(HCodeAndMaps input, Token which) {
	MyHCodeAndMaps hcam = (MyHCodeAndMaps) input;
	HCode<Quad> hc = hcam.hcode();
	ExactTypeMap<Quad> etm = hcam.typeMap;
	HEADER qH = (HEADER) hc.getRootElement();
	FOOTER qF = qH.footer();
	METHOD qM = qH.method();
	// recursively decend the dominator tree, rewriting as we go.
	if (enabled &&
	    // don't transform the transactions runtime support
	    (! "harpoon.Runtime.Transactions".equals
	       (hc.getMethod().getDeclaringClass().getPackage())) &&
	    // don't transform static initialization code.
	    /* xxx runtime calls other startup code interspersed with static
	     *     initialization; probably not safe to skip transform.
	    (! (hc.getMethod() instanceof HInitializer ||
		hc.getMethod().getName().endsWith("$$initcheck"))) &&
	    */
	    // don't transform the counter/statistic code.
	    (!excludeCounters || ! "harpoon.Runtime.Counters".equals
	     (hc.getMethod().getDeclaringClass().getName()))) {
	    CheckOracle co = new SimpleCheckOracle(noArrayModification);
	    if (useSmartCheckOracle) {
		DomTree dt = new DomTree(hc, false);
		// XXX don't allow hoisting past CALL or MONITOREXIT
		co = new DominatingCheckOracle(dt, co);
		co = new HoistingCheckOracle
		    (hc, CFGrapher.DEFAULT, UseDefer.DEFAULT, dt, co);
	    }
	    Tweaker tw = new Tweaker(co, qF, (which==WITH_TRANSACTION), etm);
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
    static class ListList<T> {
	public final List<T> head;
	public final ListList<T> tail;
	public ListList(List<T> head, ListList<T> tail) {
	    this.head = head; this.tail = tail;
	}
    }
    class Tweaker extends QuadVisitor {
	// immutable.
	final QuadFactory qf;
	final TempFactory tf;
	final Temp retex;
	final Temp currtrans; // current transaction.
	private final Map<PHI,List<THROW>> fixupmap =
	    new HashMap<PHI,List<THROW>>();
	private final Set<NOP> typecheckset = new HashSet<NOP>();
	final CheckOracle co;
	final FieldOracle fo;
	final TempSplitter ts=new TempSplitter();
	final ExactTypeMap<Quad> etm;
	// mutable.
	FOOTER footer; // we attach new stuff to the footer.
	ListList<THROW> handlers = null; // points to current abort handler
	Tweaker(CheckOracle co, FOOTER qF, boolean with_transaction,
		ExactTypeMap<Quad> etm) {
	    this.co = co;
	    this.fo = fieldOracle; // cache in this object.
	    this.footer = qF;
	    this.qf = qF.getFactory();
	    this.tf = this.qf.tempFactory();
	    this.etm = etm;
	    // indicate that we're inside transaction context, but
	    // that we need to rethrow TransactionAbortExceptions
	    if (with_transaction)
		handlers = new ListList<THROW>(null, handlers);
	    this.currtrans = new Temp(tf, "transid");
	    this.retex = new Temp(tf, "trabex"); // transaction abort exception
	}
	/** helper routine to add a quad on an edge. */
	private Edge addAt(Edge e, Quad q) { return addAt(e, 0, q, 0); }
	private Edge addAt(Edge e, int which_pred, Quad q, int which_succ) {
	    Quad frm = e.from(); int frm_succ = e.which_succ();
	    Quad to  = e.to();   int to_pred = e.which_pred();
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
	    THROW q0 = new THROW(qf, src, tex);
	    Quad.addEdge(e.from(), 1, q0, 0);
	    handlers.head.add(q0);
	    CounterFactory.spliceIncrement(qf, q0.prevEdge(0),
					   "synctrans.aborts");
	    return e;
	}
	private void throwAbort(Quad from, int which_succ, HCodeElement src) {
	    assert handlers!=null;
	    Quad q0 = new NEW(qf, src, retex, HCabortex);
	    Quad q1 = new CALL(qf, src, HMabortex_cons,
			       new Temp[] { retex, currtrans }, null,
			       retex, false, false, new Temp[0]);
	    THROW q2 = new THROW(qf, src, retex);
	    THROW q3 = new THROW(qf, src, retex);
	    Quad.addEdge(from, which_succ, q0, 0);
	    Quad.addEdge(q0, 0, q1, 0);
	    Quad.addEdge(q1, 0, q2, 0);
	    Quad.addEdge(q1, 1, q3, 0);
	    footer = footer.attach(q3, 0); // attach exc. throw to FOOTER.
	    if (handlers.head!=null)
		handlers.head.add(q2); // "normal" throw is added to list.
	    else
		footer = footer.attach(q2, 0); // really throw abort exception
	    CounterFactory.spliceIncrement(qf, q2.prevEdge(0),
					   "synctrans.aborts");
	    // done!
	}
	/** Fix up PHIs leading to abort handler after we're all done. */
	void fixup() {
	    for(Iterator<Map.Entry<PHI,List<THROW>>> it =
		    fixupmap.entrySet().iterator(); it.hasNext(); ) {
		Map.Entry<PHI,List<THROW>> me = it.next();
		PHI phi = me.getKey();
		List<THROW> throwlist = me.getValue();
		PHI nphi = new PHI(qf, phi, new Temp[0], throwlist.size());
		Edge out = phi.nextEdge(0);
		Quad.addEdge(nphi, 0, out.to(), out.which_pred());
		int n=0;
		for (Iterator<THROW> it2 = throwlist.iterator();
		     it2.hasNext(); n++) {
		    THROW thr = it2.next();
		    Temp tex = thr.throwable();
		    Edge in = thr.prevEdge(0);
		    if (tex!=retex)
			in = addAt(in, new MOVE(qf, thr, retex, tex));
		    Quad.addEdge(in.from(), in.which_succ(), nphi, n);
		    // NOTE THAT WE ARE NOT DELINKING THE THROW FROM THE
		    // FOOTER: this should be done before it is added to the
		    // list.
		}
	    }
	    // fixup the dangling array type check edges
	    if (typecheckset.size()==0) return;
	    PHI phi = new PHI(qf, footer, new Temp[0], typecheckset.size()+1);
	    int i=0;
	    for (Iterator<NOP> it=typecheckset.iterator(); it.hasNext(); ) {
		Edge in = it.next().prevEdge(0);
		Quad.addEdge(in.from(), in.which_succ(), phi, i++);
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
	    Quad.addEdge(in.from(), in.which_succ(), q0, 0);
	    Quad.addEdge(q0, 0, q1, 0);
	    Quad.addEdge(q0, 1, q3, 0);
	    Quad.addEdge(q1, 0, out.to(), out.which_pred());//delink q
	    Quad.addEdge(q2, 0, q1, 1);
	    Quad.addEdge(q2, 1, q3, 1);
	    Quad.addEdge(q3, 0, q4, 0);
	    footer = footer.attach(q4, 0); // attach throw to FOOTER.
	    // add test to TransactionAbortException;
	    PHI q5 = new PHI(qf, q, new Temp[0], 0); // stub
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
	    handlers = new ListList<THROW>(new ArrayList<THROW>(), handlers);
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
	    Quad.addEdge(in.from(), in.which_succ(), q0, 0);
	    Quad.addEdge(q0, 0, q1, 0);
	    Quad.addEdge(q0, 1, q2, 0);
	    Quad.addEdge(q1, 0, out.to(), out.which_pred());
	    footer = footer.attach(q2, 0); // add q2 to FOOTER.
	    checkForAbort(q0.nextEdge(1), q, retex);
	    handlers = handlers.tail;
	    if (handlers==null) q1.remove(); // unneccessary.
	}
	public void visit(AGET q) {
	    addChecks(q);
	    if (noFieldModification || noArrayModification) return;
	    addUniqueRWCounters(q.prevEdge(0), q, q.objectref(), true, true);
	    CounterFactory.spliceIncrement
		(qf, q.prevEdge(0),(handlers==null) ?
		 "synctrans.read_nt_array" : "synctrans.read_t_array");

	    HClass compType = etm.typeMap(q, q.dst());
	    // this is great for object arrays, but sub-integer components
	    // are all squashed into HClass.Int.  So ask the quad in this case
	    if (compType.isPrimitive()) compType = q.type();
	    HClass arrType = HClassUtil.arrayClass(linker, compType, 1);
	    Edge in = q.prevEdge(0), out = q.nextEdge(0);
	    Temp t1 = new Temp(tf, "retex");
	    Quad q1;
	    if (handlers==null) { // non-transactional read
		// VALUETYPE TA(EXACT_readNT)(struct oobj *obj, int offset,
		//                            int flag_offset, int flag_bit)
		Temp t2 = new Temp(tf, "flag_field");
		Temp t3 = new Temp(tf, "flag_bit");
		Temp t4 = new Temp(tf, "index_mod32");
		HField arrayCheckField = bfn.arrayBitField(arrType);
		in = addAt(in, new CONST(qf, q, t2, arrayCheckField, HCfield));
		in = addAt(in, new CONST(qf, q, t4, new Integer(31),
					 HClass.Int));
		in = addAt(in, new OPER(qf, q, Qop.IAND, t4,
					new Temp[] { q.index(), t4 }));
		in = addAt(in, new CONST(qf, q, t3, new Integer(1),
					 HClass.Int));
		in = addAt(in, new OPER(qf, q, Qop.ISHL, t3,
					new Temp[] { t3, t4 }));
		q1 = new CALL(qf, q, gen.lookupMethod
			      ("readNT_Array", new HClass[]
				  { arrType, HClass.Int, HCfield, HClass.Int },
			       compType),
			      new Temp[] { q.objectref(), q.index(), t2, t3 },
			      q.dst(), t1, false, false, new Temp[0]);
	    } else { // transactional read
		// VALUETYPE TA(EXACT_readT)(struct oobj *obj, int offset,
		//		             struct vinfo *version,
		//                           struct commitrec *cr)
		q1 = new CALL(qf, q, gen.lookupMethod
			      ("readT_Array", new HClass[]
				  {arrType, HClass.Int, HCvinfo, HCcommitrec},
			       compType),
			      new Temp[] { q.objectref(), q.index(),
					   ts.versioned(q.objectref()),
					   currtrans },
			      q.dst(), t1, false, false, new Temp[0]);
	    }
	    Quad q2 = new THROW(qf, q, t1);

	    Quad.addEdge(in.from(), in.which_succ(), q1, 0);
	    Quad.addEdge(q1, 0, out.to(), out.which_pred());
	    Quad.addEdge(q1, 1, q2, 0);
	    footer = footer.attach(q2, 0); // add q2 to FOOTER
	    /* // native call is not going to abort.
	    if (handlers!=null) // only trans can abort
		checkForAbort(q1.nextEdge(1), q, t1);
	    */
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

	    transFields.add(q.field());
	    Edge in = q.prevEdge(0), out = q.nextEdge(0);
	    Temp t0 = new Temp(tf, "read_field");
	    Temp t1 = new Temp(tf, "retex");
	    Quad q0 = new CONST(qf, q, t0, q.field(), HCfield);
	    Quad q1;
	    in = addAt(in, q0);
	    if (handlers==null) { // non-transactional read
		// VALUETYPE TA(EXACT_readNT)(struct oobj *obj, int offset,
		//                            int flag_offset, int flag_bit)
		Temp t2 = new Temp(tf, "flag_field");
		Temp t3 = new Temp(tf, "flag_bit");
		BitFieldTuple bft = bfn.bfLoc(q.field());
		in = addAt(in, new CONST(qf, q, t2, bft.field, HCfield));
		in = addAt(in, new CONST(qf, q, t3, new Integer(1<<bft.bit),
				     HClass.Int));
		q1 = new CALL(qf, q, gen.lookupMethod
			      ("readNT", new HClass[] { HCobj, HCfield,
							HCfield, HClass.Int },
			       q.field().getType()),
			      new Temp[] { q.objectref(), t0, t2, t3 },
			      q.dst(), t1, false, false, new Temp[0]);
	    } else { // transactional read
		// VALUETYPE TA(EXACT_readT)(struct oobj *obj, int offset,
		//		             struct vinfo *version,
		//                           struct commitrec *cr)
		q1 = new CALL(qf, q, gen.lookupMethod
			      ("readT", new HClass[] { HCobj, HCfield,
						       HCvinfo, HCcommitrec },
			       q.field().getType()),
			      new Temp[] { q.objectref(), t0,
					   ts.versioned(q.objectref()),
					   currtrans },
			      q.dst(), t1, false, false, new Temp[0]);
	    }
	    Quad q2 = new THROW(qf, q, t1);

	    Quad.addEdge(in.from(), in.which_succ(), q1, 0);
	    Quad.addEdge(q1, 0, out.to(), out.which_pred());
	    Quad.addEdge(q1, 1, q2, 0);
	    footer = footer.attach(q2, 0); // add q2 to FOOTER
	    /* // native call is not going to abort.
	    if (handlers!=null) // only trans can abort
		checkForAbort(q1.nextEdge(1), q, t1);
	    */
	    // done.
	}

	public void visit(ASET q) {
	    addChecks(q);
	    if (noFieldModification || noArrayModification) return;
	    addUniqueRWCounters(q.prevEdge(0), q, q.objectref(), false, true);
	    CounterFactory.spliceIncrement
		(qf, q.prevEdge(0),(handlers==null) ?
		 "synctrans.write_nt_array" : "synctrans.write_t_array");

	    HClass compType = q.type(); // don't need extra precision here.
	    HClass arrType = HClassUtil.arrayClass(linker, compType, 1);
	    Edge in = q.prevEdge(0), out = q.nextEdge(0);
	    Temp t1 = new Temp(tf, "retex");
	    Quad q1;
	    if (handlers==null) { // non-transactional write
		// void TA(EXACT_writeNT)(struct oobj *obj, int offset,
		//                        VALUETYPE value,
		//	                  int flag_offset, int flag_bit);
		Temp t2 = new Temp(tf, "flag_field");
		Temp t3 = new Temp(tf, "flag_bit");
		Temp t4 = new Temp(tf, "index_mod32");
		HField arrayCheckField = bfn.arrayBitField(arrType);
		in = addAt(in, new CONST(qf, q, t2, arrayCheckField, HCfield));
		in = addAt(in, new CONST(qf, q, t4, new Integer(31),
					 HClass.Int));
		in = addAt(in, new OPER(qf, q, Qop.IAND, t4,
					new Temp[] { q.index(), t4 }));
		in = addAt(in, new CONST(qf, q, t3, new Integer(1),
					 HClass.Int));
		in = addAt(in, new OPER(qf, q, Qop.ISHL, t3,
					new Temp[] { t3, t4 }));
		q1 = new CALL(qf, q, gen.lookupMethod
			      ("writeNT_Array", new HClass[]
				  { arrType, HClass.Int, compType,
				    HCfield, HClass.Int },
			       HClass.Void),
			      new Temp[]{ q.objectref(), q.index(), q.src(),
					  t2, t3 },
			      null, t1, false, false, new Temp[0]);
	    } else { // transactional write
		// void TA(EXACT_writeT)(struct oobj *obj, int offset,
		//		         VALUETYPE value,
		//                       struct vinfo *version);
		q1 = new CALL(qf, q, gen.lookupMethod
			      ("writeT_Array", new HClass[]
				  { arrType, HClass.Int, compType, HCvinfo },
			       HClass.Void),
			      new Temp[] { q.objectref(), q.index(), q.src(),
					   ts.versioned(q.objectref()) },
			      null, t1, false, false, new Temp[0]);
	    }
	    Quad q2 = new THROW(qf, q, t1);

	    Quad.addEdge(in.from(), in.which_succ(), q1, 0);
	    Quad.addEdge(q1, 0, out.to(), out.which_pred());
	    Quad.addEdge(q1, 1, q2, 0);
	    footer = footer.attach(q2, 0); // add q2 to FOOTER
	    /* // native call is not going to abort.
	    if (handlers!=null) // only trans can abort
		checkForAbort(q1.nextEdge(1), q, t1);
	    */
	    // done.
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

	    transFields.add(q.field());
	    Edge in = q.prevEdge(0), out = q.nextEdge(0);
	    Temp t0 = new Temp(tf, "write_field");
	    Temp t1 = new Temp(tf, "retex");
	    Quad q0 = new CONST(qf, q, t0, q.field(), HCfield);
	    Quad q1;
	    in = addAt(in, q0);
	    if (handlers==null) { // non-transactional read
		// void TA(EXACT_writeNT)(struct oobj *obj, int offset,
		//                        VALUETYPE value,
		//	                  int flag_offset, int flag_bit);
		Temp t2 = new Temp(tf, "flag_field");
		Temp t3 = new Temp(tf, "flag_bit");
		BitFieldTuple bft = bfn.bfLoc(q.field());
		in = addAt(in, new CONST(qf, q, t2, bft.field, HCfield));
		in = addAt(in, new CONST(qf, q, t3, new Integer(1<<bft.bit),
				     HClass.Int));
		q1 = new CALL(qf, q, gen.lookupMethod
			      ("writeNT", new HClass[] { HCobj, HCfield,
							 q.field().getType(),
							 HCfield, HClass.Int },
			       HClass.Void),
			      new Temp[]{ q.objectref(), t0, q.src(), t2, t3 },
			      null, t1, false, false, new Temp[0]);
	    } else { // transactional read
		// void TA(EXACT_writeT)(struct oobj *obj, int offset,
		//		         VALUETYPE value,
		//                       struct vinfo *version);
		q1 = new CALL(qf, q, gen.lookupMethod
			      ("writeT", new HClass[] { HCobj, HCfield,
							q.field().getType(),
							HCvinfo },
			       HClass.Void),
			      new Temp[] { q.objectref(), t0, q.src(),
					   ts.versioned(q.objectref()) },
			      null, t1, false, false, new Temp[0]);
	    }
	    Quad q2 = new THROW(qf, q, t1);

	    Quad.addEdge(in.from(), in.which_succ(), q1, 0);
	    Quad.addEdge(q1, 0, out.to(), out.which_pred());
	    Quad.addEdge(q1, 1, q2, 0);
	    footer = footer.attach(q2, 0); // add q2 to FOOTER
	    /* // native call is not going to abort.
	    if (handlers!=null) // only trans can abort
		checkForAbort(q1.nextEdge(1), q, t1);
	    */
	    // done.
	}
	public void visit(ARRAYINIT q) {
	    addChecks(q);
	    if (noFieldModification || noArrayModification) return;
	    // XXX: we don't handle ARRAYINIT yet.
	    assert false : "ARRAYINIT transformation unimplemented.";
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
	    // note that reading is different from writing: if you want to
	    // read *and* write, you must call ensureReader *and* ensureWriter
	    for (int i=0; i<2; i++) {
		// iteration 0 for read; iteration 1 for write versions.
		Iterator<Temp> it = (i==0) ? rS.iterator() : wS.iterator();
		// void EXACT_ensureReader(struct oobj *obj,
		//                         struct commitrec *cr);
		// struct vinfo *EXACT_ensureWriter(struct oobj *obj,
		//				    struct commitrec *cr);
		HMethod hm = 
		    gen.lookupMethod((i==0) ? "ensureReader" : "ensureWriter",
				     new HClass[] { HCobj, HCcommitrec },
				     (i==0) ? HClass.Void : HCvinfo);
		while (it.hasNext()) {
		    // for each temp, a call to 'ensureReader' or
		    // 'ensureWriter'.
		    Temp t = it.next();
		    CALL q0= new CALL(qf, q, hm, new Temp[] { t, currtrans },
		                      (i==0) ? null : ts.versioned(t), retex,
				      false/*final, not virtual*/, false,
				      new Temp[0]);
		    THROW q1= new THROW(qf, q, retex);
		    in = addAt(in, q0);
		    Quad.addEdge(q0, 1, q1, 0);
		    footer = footer.attach(q1, 0);
		    checkForAbort(q0.nextEdge(1), q, retex);
		    if (i==0) { // start reader version at NULL.
			in = addAt(in, new CONST(qf, q, ts.versioned(t),
					       null, HClass.Void));
		    } else { // check return value from writer
			// NULL version indicates suicide request.
			Quad q2;
			Temp tnull = new Temp(tf, "null");
			Temp tcmp = new Temp(tf, "nullchk");
			in = addAt(in, new CONST(qf, q, tnull,
						 null, HClass.Void));
			in = addAt(in, new OPER(qf, q, Qop.ACMPEQ, tcmp,
						new Temp[] { ts.versioned(t),
							     tnull }));
			in = addAt(in, q2=new CJMP(qf, q, tcmp, new Temp[0]));
			throwAbort(q2, 1, q);
		    }
		    in = CounterFactory.spliceIncrement
			(qf, in,
			 "synctrans."+((i==0)?"read":"write")+"_versions");
		}
	    }
	    // do field-read checks...
	    for (Iterator<CheckOracle.RefAndField> it =
		     co.checkFieldReads(q).iterator(); it.hasNext();) {
		CheckOracle.RefAndField raf = it.next();
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

		// struct vinfo *TA(EXACT_setReadFlags)
		//      (struct oobj *obj, int offset,
		//       int flag_offset, int flag_bit,
		//       struct vinfo *version,
		//       struct commitrec*cr/*this trans*/);
		HMethod hm = gen.lookupMethod
		    ("setReadFlags", new HClass[]
			{ raf.field.getDeclaringClass(), HCfield,
			  HCfield, HClass.Int, HCvinfo, HCcommitrec },
		     HCvinfo);

		Temp t0 = new Temp(tf, "readcheck_field");
		Temp t1 = new Temp(tf, "readcheck_flag_field");
		Temp t2 = new Temp(tf, "readcheck_flag_bit");
		in = addAt(in, new CONST(qf, q, t0, raf.field, HCfield));
		in = addAt(in, new CONST(qf, q, t1, bft.field, HCfield));
		in = addAt(in, new CONST(qf, q, t2, new Integer(1<<bft.bit),
				    HClass.Int));
		CALL q0= new CALL(qf, q, hm,
				  new Temp[] { raf.objref, t0, t1, t2,
					       ts.versioned(raf.objref),
					       currtrans },
				  ts.versioned(raf.objref), retex,
				  false, false, new Temp[0]);
		// never throws exception.
		Quad q1 = new THROW(qf, q, retex);
		in = addAt(in, q0);
		Quad.addEdge(q0, 1, q1, 0);
		footer = footer.attach(q1, 0);
		/* can't see the 'bad read check' case from here anymore.
		CounterFactory.spliceIncrement
		    (qf, q6.prevEdge(0), "synctrans.field_read_checks_bad");
		*/
	    }
	    // do field-write checks...
	    for (Iterator<CheckOracle.RefAndField> it =
		     co.checkFieldWrites(q).iterator(); it.hasNext();) {
		CheckOracle.RefAndField raf = it.next();
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
		BitFieldTuple bft = bfn.bfLoc(raf.field);

		// void TA(EXACT_setWriteFlags)(struct oobj *obj, int offset,
		//                              int flag_offset, int flag_bit,
		//                              struct vinfo *version);
		HMethod hm = gen.lookupMethod
		    ("setWriteFlags", new HClass[]
			{ raf.field.getDeclaringClass(), HCfield,
			  HCfield, HClass.Int, HCvinfo }, HClass.Void);

		Temp t0 = new Temp(tf, "writecheck_field");
		Temp t1 = new Temp(tf, "writecheck_flag_field");
		Temp t2 = new Temp(tf, "writecheck_flag_bit");
		in = addAt(in, new CONST(qf, q, t0, raf.field, HCfield));
		in = addAt(in, new CONST(qf, q, t1, bft.field, HCfield));
		in = addAt(in, new CONST(qf, q, t2, new Integer(1<<bft.bit),
					 HClass.Int));
		CALL q0= new CALL(qf, q, hm,
				  new Temp[] { raf.objref, t0, t1, t2,
					       ts.versioned(raf.objref) },
				  null, retex, false, false, new Temp[0]);
		// never throws exception.
		Quad q1 = new THROW(qf, q, retex);
		in = addAt(in, q0);
		Quad.addEdge(q0, 1, q1, 0);
		footer = footer.attach(q1, 0);
		// XXX maybe this method should check whether aborted?
		/* can't see the 'bad write check' case from here anymore.
		CounterFactory.spliceIncrement
		    (qf, q5.prevEdge(0), "synctrans.field_write_checks_bad");
		*/
	    }
	    // do array index read checks....
	    for (Iterator<CheckOracle.RefAndIndexAndType> it =
		     co.checkArrayElementReads(q).iterator(); it.hasNext(); ) {
		// arrays have one check field (32 bits) which stand in
		// (modulo 32) for all the elements in the array.
		// we check that the appropriate read-bit is set, else
		// call the fixup code, which will do an atomic-set of 
		// the bit.
		in = CounterFactory.spliceIncrement
		    (qf, in, "synctrans.element_read_checks");
		CheckOracle.RefAndIndexAndType rit = it.next();
		HClass arrayClass =
		    HClassUtil.arrayClass(qf.getLinker(), rit.type, 1);
		HField arrayCheckField = bfn.arrayBitField(arrayClass);
		// struct vinfo *TA(EXACT_setReadFlags)
		//      (struct oobj *obj, int offset,
		//       int flag_offset, int flag_bit,
		//       struct vinfo *version,
		//       struct commitrec*cr/*this trans*/);
		HMethod hm = gen.lookupMethod
		    ("setReadFlags_Array", new HClass[]
			{ arrayClass, HClass.Int, HCfield, HClass.Int,
			  HCvinfo, HCcommitrec }, HCvinfo);

		Temp t0 = new Temp(tf, "arrayreadcheck");
		Temp t1 = new Temp(tf, "arrayreadcheck_flag_field");
		Temp t2 = new Temp(tf, "arrayreadcheck_flag_bit");
		in = addAt(in, new CONST(qf, q, t0, new Integer(31),
					 HClass.Int));
		in = addAt(in, new OPER(qf, q, Qop.IAND, t0,
				   new Temp[]{ rit.index, t0 }));
		in = addAt(in, new CONST(qf, q, t2, new Integer(1),
					 HClass.Int));
		in = addAt(in, new OPER(qf, q, Qop.ISHL, t2,
					new Temp[]{t2, t0}));
		in = addAt(in, new CONST(qf, q, t1, arrayCheckField, HCfield));
		CALL q0= new CALL(qf, q, hm,
				  new Temp[] { rit.objref, rit.index, t1, t2,
					       ts.versioned(rit.objref),
					       currtrans },
				  ts.versioned(rit.objref), retex,
				  false, false, new Temp[0]);
		// never throws exception.
		Quad q1 = new THROW(qf, q, retex);
		in = addAt(in, q0);
		Quad.addEdge(q0, 1, q1, 0);
		footer = footer.attach(q1, 0);
		/* can't see the 'bad read check' case from here anymore.
		CounterFactory.spliceIncrement
		    (qf, q6.prevEdge(0), "synctrans.element_read_checks_bad");
		*/
	    }
	    // do array index write checks.
	    for (Iterator<CheckOracle.RefAndIndexAndType> it =
		     co.checkArrayElementWrites(q).iterator(); it.hasNext(); ){
		// (check that element==FLAG is set, else call fixup code)
		in = CounterFactory.spliceIncrement
		    (qf, in, "synctrans.element_write_checks");
		CheckOracle.RefAndIndexAndType rit = it.next();
		HClass arrayClass =
		    HClassUtil.arrayClass(qf.getLinker(), rit.type, 1);
		HField arrayCheckField = bfn.arrayBitField(arrayClass);
		// void TA(EXACT_setWriteFlags)(struct oobj *obj, int offset,
		//                              int flag_offset, int flag_bit,
		//                              struct vinfo *version);
		HMethod hm = gen.lookupMethod
		    ("setWriteFlags_Array", new HClass[]
			{ arrayClass, HClass.Int, HCfield, HClass.Int,
			  HCvinfo }, HClass.Void);

		Temp t0 = new Temp(tf, "arraywritecheck");
		Temp t1 = new Temp(tf, "arraywritecheck_flag_field");
		Temp t2 = new Temp(tf, "arraywritecheck_flag_bit");
		in = addAt(in, new CONST(qf, q, t0, new Integer(31),
					 HClass.Int));
		in = addAt(in, new OPER(qf, q, Qop.IAND, t0,
				   new Temp[]{ rit.index, t0 }));
		in = addAt(in, new CONST(qf, q, t2, new Integer(1),
					 HClass.Int));
		in = addAt(in, new OPER(qf, q, Qop.ISHL, t2,
					new Temp[]{t2, t0}));
		in = addAt(in, new CONST(qf, q, t1, arrayCheckField, HCfield));
		CALL q0= new CALL(qf, q, hm,
				  new Temp[] { rit.objref, rit.index, t1, t2,
					       ts.versioned(rit.objref) },
				  null, retex, false, false, new Temp[0]);
		// never throws exception.
		Quad q1 = new THROW(qf, q, retex);
		in = addAt(in, q0);
		Quad.addEdge(q0, 1, q1, 0);
		footer = footer.attach(q1, 0);
		/* can't see the 'bad write check' case from here anymore.
		CounterFactory.spliceIncrement
		    (qf, q6.prevEdge(0), "synctrans.element_write_checks_bad");
		*/
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
	    Quad.addEdge(in.from(), in.which_succ(), q0, 0);
	    Quad.addEdge(q2, 0, in.to(), in.which_pred());
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
	transFields.addAll(bfn.bitfields);
	return new TreePostPass(f, FLAG_VALUE, HFflagvalue, gen, transFields)
	    .codeFactory(hcf);
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
    /** Create an empty stub for generated placeholder methods. */
    private QuadRSSx emptyCode(final HMethod hm) {
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

	    HClass type = hm.getReturnType();
	    Temp retval = new Temp(qf.tempFactory(), "retval");

	    Quad q0 = new HEADER(qf, null);
	    Quad q1 = new METHOD(qf, null, params, 1);
	    Quad q2=null;
	    if (type==HClass.Void || !type.isPrimitive())
		q2 = new CONST(qf, null, retval, null, HClass.Void);
	    else if (type==HClass.Boolean ||
		     type==HClass.Byte ||
		     type==HClass.Char ||
		     type==HClass.Short ||
		     type==HClass.Int)
		q2 = new CONST(qf, null, retval, new Integer(0), HClass.Int);
	    else if (type==HClass.Long)
		q2 = new CONST(qf, null, retval, new Long(0), HClass.Long);
	    else if (type==HClass.Float)
		q2 = new CONST(qf, null, retval, new Float(0), HClass.Float);
	    else if (type==HClass.Double)
		q2 = new CONST(qf, null, retval, new Double(0), HClass.Double);
	    else assert false;
	    Quad q3 = new RETURN(qf, null, (type==HClass.Void)? null : retval);
	    Quad q4 = new FOOTER(qf, null, 2);
	    Quad.addEdge(q0, 0, q4, 0);
	    Quad.addEdge(q0, 1, q1, 0);
	    Quad.addEdge(q1, 0, q2, 0);
	    Quad.addEdge(q2, 0, q3, 0);
	    Quad.addEdge(q3, 0, q4, 1);
	    this.quads = q0;
	    // done!
	} };
    }
    
    private class TempSplitter {
	private final Map<Temp,Temp> m = new HashMap<Temp,Temp>();
	public Temp versioned(Temp t) {
	    if (!m.containsKey(t))
		m.put(t, new Temp(t));
	    return m.get(t);
	}
    }

    private static Set<HMethod> parseResource(final Linker l,
					      String resourceName) {
	final Set<HMethod> result = new HashSet<HMethod>();
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
