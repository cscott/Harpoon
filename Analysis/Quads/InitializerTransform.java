// InitializerTransform.java, created Tue Oct 17 14:35:29 2000 by cananian
// Copyright (C) 2000 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.Quads;

import harpoon.Analysis.ClassHierarchy;
import harpoon.ClassFile.CachingCodeFactory;
import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HClassMutator;
import harpoon.ClassFile.HCode;
import harpoon.ClassFile.HCodeAndMaps;
import harpoon.ClassFile.HCodeFactory;
import harpoon.ClassFile.HConstructor;
import harpoon.ClassFile.HField;
import harpoon.ClassFile.HFieldMutator;
import harpoon.ClassFile.HInitializer;
import harpoon.ClassFile.HMethod;
import harpoon.ClassFile.Linker;
import harpoon.ClassFile.NoSuchClassException;
import harpoon.ClassFile.SerializableCodeFactory;
import harpoon.IR.Quads.ANEW;
import harpoon.IR.Quads.CALL;
import harpoon.IR.Quads.CJMP;
import harpoon.IR.Quads.CONST;
import harpoon.IR.Quads.Code;
import harpoon.IR.Quads.Edge;
import harpoon.IR.Quads.FOOTER;
import harpoon.IR.Quads.GET;
import harpoon.IR.Quads.HEADER;
import harpoon.IR.Quads.METHOD;
import harpoon.IR.Quads.MONITORENTER;
import harpoon.IR.Quads.MONITOREXIT;
import harpoon.IR.Quads.NEW;
import harpoon.IR.Quads.PHI;
import harpoon.IR.Quads.Quad;
import harpoon.IR.Quads.QuadFactory;
import harpoon.IR.Quads.QuadVisitor;
import harpoon.IR.Quads.QuadWithTry;
import harpoon.IR.Quads.RETURN;
import harpoon.IR.Quads.SET;
import harpoon.Temp.Temp;
import net.cscott.jutil.Default;
import net.cscott.jutil.Environment;
import net.cscott.jutil.HashEnvironment;
import harpoon.Util.ParseUtil;
import harpoon.Util.Util;

import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
/**
 * <code>InitializerTransform</code> transforms class initializers so
 * that they are idempotent and so that they perform all needed
 * initializer ordering checks before accessing non-local data.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: InitializerTransform.java,v 1.9 2006-01-07 15:11:57 salcianu Exp $
 */
public class InitializerTransform
    extends harpoon.Analysis.Transformation.MethodSplitter {
    /** Token for the initializer-ordering-check version of a method. */
    public static final Token CHECKED = new Token("initcheck") {
	public Object readResolve() { return CHECKED; }
    };
    /** Set of dependent native methods.  We know the dependencies of
     *  these methods statically; this is a Map from HMethods to
     *  Sets of HInitializers. HMethods with no dependencies are
     *  "safe". */
    private final Map dependentMethods;
    /** Our version of the codefactory. */
    private final HCodeFactory hcf;
    /** Runtime property determining whether we should unsynchronize
     *  all initializer methods.  This works except when initializers
     *  create threads, which we don't currently allow.  The default
     *  is to unsynchronize initializer methods. */
    private final static boolean unsyncInitializers =
	System.getProperty("harpoon.inittrans.unsync", "yes")
	.equalsIgnoreCase("yes");

    /** Creates a <code>InitializerTransform</code> with no information
     *  about which native methods are 'safe'. */
    public InitializerTransform(HCodeFactory parent, ClassHierarchy ch) {
	this(parent, ch, Default.EMPTY_MAP);
    }
    /** Creates a <code>InitializerTransform</code> using the specified
     *  named resource to specify the safe and dependent
     *  native methods of this runtime. */
    public InitializerTransform(HCodeFactory parent, ClassHierarchy ch,
				Linker linker, String resourceName) {
	this(parent, ch, parseProperties(linker, resourceName));
    }
    /** Creates a <code>InitializerTransform</code> using the given
     *  information about safe and dependent methods.
     *  @param parent The input code factory. Will be converted to QuadWithTry.
     *  @param ch A class hierarchy for the application.
     *  @param dependentMethods a map from <code>HMethod</code>s specifying
     *         native methods to a <code>java.util.Set</code> of the 
     *         <code>HInitializer</code>s of the classes whose static
     *         data this method may reference.  <code>HMethod</code>s
     *         which map to zero-size <code>Set</code>s are 'safe'
     *         to call within initializers (that is, they do not reference
     *         any static data).
     */
    public InitializerTransform(HCodeFactory parent, ClassHierarchy ch,
				final Map dependentMethods) {
	// we only allow quad with try as input.
	super(QuadWithTry.codeFactory(parent), ch, true/*doesn't matter*/);
	this.dependentMethods = dependentMethods;
	final HCodeFactory superfactory = super.codeFactory();
	assert superfactory.getCodeName().equals(QuadWithTry.codename);
	this.hcf = new CachingCodeFactory(new SerializableCodeFactory() {
	    public String getCodeName() { return superfactory.getCodeName(); }
	    public void clear(HMethod m) { superfactory.clear(m); }
	    public HCode convert(HMethod m) {
		if (Modifier.isNative(m.getModifiers()) &&
		    dependentMethods.containsKey(select(m, ORIGINAL)) &&
		    select(select(m, ORIGINAL), CHECKED).equals(m))
		    // call the initializers for the dependent classes,
		    // then call the original method.
		    return redirectCode(m);
		else return superfactory.convert(m);
	    }
	}) {    // make sure method is in safety cache before we clear
		// it from top-level cache (since isSafe needs to refer
		// to copy stored in top-level cache)
		public void clear(HMethod hm) {
		    if (select(hm, ORIGINAL).equals(hm) &&
			!(hm instanceof HInitializer))
			isSafe(hm);
		    super.clear(hm);
		}
	};
    }
    // override parent's codefactory with ours! (which uses theirs)
    public HCodeFactory codeFactory() { return hcf; }
    /** Checks the token types handled by this 
     *  <code>MethodSplitter</code> subclass. */
    protected boolean isValidToken(Token which) {
	return which==CHECKED || super.isValidToken(which);
    }
    /** Mutate a given <code>HCode</code> to produce the version
     *  specified by <code>which</code>. */
    protected HCode mutateHCode(HCodeAndMaps input, Token which) {
	Code hc = (QuadWithTry) input.hcode();
	if (which==CHECKED)
	    return addChecks(hc);
	else if (which==ORIGINAL && hc.getMethod() instanceof HInitializer)
	    return mutateInitializer(hc);
	return hc;
    }
    /** Add idempotency to initializer and add checks. */
    private Code mutateInitializer(Code hc) {
	HMethod hm = hc.getMethod();
	assert hm.getReturnType()==HClass.Void;
	// add checks.
	hc = addChecks(hc);
	// make idempotent.
	HEADER qH = (HEADER) hc.getRootElement();
	FOOTER qF = (FOOTER) qH.next(0);
	METHOD qM = (METHOD) qH.next(1);
	QuadFactory qf = qH.getFactory();
	HClass declcls = hc.getMethod().getDeclaringClass();
	HField ifield = declcls.getMutator().addDeclaredField
	    ("$$has$been$initialized$$", HClass.Boolean);
	ifield.getMutator().setSynthetic(true);
	ifield.getMutator().setModifiers(Modifier.STATIC | Modifier.PUBLIC);
	Temp tst = new Temp(qf.tempFactory(), "uniq");
	Quad q0 = new GET(qf, qM, tst, ifield, null);
	Quad q1= new CJMP(qf, qM, tst, new Temp[0]);
	Quad q2 = new RETURN(qf, qM, null);
	Quad q3 = new CONST(qf, qM, tst, new Integer(1), HClass.Int);
	Quad q4 = new SET(qf, qM, ifield, null, tst);
	Edge splitedge = qM.nextEdge(0);
	Quad.addEdges(new Quad[] { qM, q0, q1, q3, q4 });
	Quad.addEdge(q1, 1, q2, 0);
	Quad.addEdge(q4, 0, (Quad)splitedge.to(), splitedge.which_pred());
	qF = qF.attach(q2, 0);
	// done.
	return hc;
    }
    /** Determine if this method is 'safe' (will never need initializers
     *  inserted) or not. */
    private boolean isSafe(HMethod hm) {
	if (safetyCache.containsKey(hm))
	    return ((Boolean) safetyCache.get(hm)).booleanValue();
	safetyCache.put(hm, new Boolean(true));// deals with cycles.
	boolean isSafe = _isSafe_(hm); // split to make caching more readable.
	safetyCache.put(hm, new Boolean(isSafe));
	return isSafe;
    }
    private boolean _isSafe_(HMethod hm) {
	assert !(hm instanceof HInitializer) : hm;
	final HClass hc = hm.getDeclaringClass();
	if (hc.isArray()) return true; // all array methods (clone()) are safe.
	// native methods are safe if they don't depend on anything.
	if (dependentMethods.containsKey(hm) &&
	    ((Set) dependentMethods.get(hm)).size()==0)
	    return true;
	// okay, scan the code for this method.
	class BooleanVisitor extends QuadVisitor {
	    boolean unsafe = false;
	    public void visit(Quad q) { /* ignore */ }
	}
	BooleanVisitor bv = new BooleanVisitor() {
	    // look for static references outside 'hc' (GET/SET/ANEW/NEW/CALL)
	    public void visit(GET q) {
		if (q.isStatic()) check(q.field().getDeclaringClass());
	    }
	    public void visit(SET q) {
		if (q.isStatic()) check(q.field().getDeclaringClass());
	    }
	    public void visit(ANEW q) {
		check(q.hclass());
	    }
	    public void visit(NEW q) {
		check(q.hclass());
	    }
	    public void visit(CALL q) {
		if ( !isVirtual(q) ) {
		    check(q.method().getDeclaringClass());
		    unsafe = unsafe || !isSafe(q.method());
		} else unsafe = true; // virtual calls aren't safe.
	    }
	    void check(HClass c) {
		if (c==hc) return;
		if (c.getClassInitializer()==null) return;
		unsafe = true;
	    }
	};
	HCode code = codeFactory().convert(hm);
	if (code==null) return false; // no clue what this does!
	for (Iterator it=code.getElementsI();
	     (!bv.unsafe) && it.hasNext(); )
	    ((Quad) it.next()).accept(bv);
	return !bv.unsafe;
    }
    /** Cache for safety tests. */
    private Map safetyCache = new HashMap();
    /** Add initialization checks to every static use of a class. */
    private Code addChecks(Code hc) {
	final HEADER qH = (HEADER) hc.getRootElement();
	final Set phisSeen = new HashSet();
	// static references are found in GET/SET/ANEW/NEW/CALL
	QuadVisitor qv = new QuadVisitor() {
	    /** classes already initialized in this method. */
	    Environment seenSet = new HashEnvironment();
	    /* constructor */ { traverse((METHOD)qH.next(1)); }
	    // recursive traversal.
	    private void traverse(Quad q) {
		Quad[] nxt = q.next(); // cache before q is (possibly) replaced
		q.accept(this);
		Environment.Mark m = seenSet.getMark();
		for (int i=0; i<nxt.length; i++) {
		    if (!phisSeen.contains(nxt[i]))
			traverse(nxt[i]);
		    if (i+1<q.nextLength())
			seenSet.undoToMark(m);
		}
	    }
	    public void visit(Quad q) { /* default, do nothing. */ }
	    public void visit(PHI q) {
		phisSeen.add(q);
		// XXX: merging at phis (instead of throwing away
		// seenset) would lead to less unnecessary
		// initializations.  cost may be prohibitive?
		seenSet.clear();
	    }
	    public void visit(ANEW q) {
		addCheckBeforeAll(q, q.hclass(), seenSet);
	    }
	    public void visit(CALL q) {
		if (q.isStatic())
		    addCheckBefore(q, q.method().getDeclaringClass(), seenSet);
		if ( (!isVirtual(q)) && isSafe(q.method()))
		    return;

		// use a 'checking' version of this method.
		Quad ncall = new CALL
		    (q.getFactory(), q, select(q.method(), CHECKED),
		     q.params(), q.retval(), q.retex(), q.isVirtual(),
		     q.isTailCall(), q.dst(), q.src());
		Quad.replace(q, ncall);
		Quad.transferHandlers(q, ncall);
	    }
	    public void visit(GET q) {
		if (q.isStatic())
		    addCheckBefore(q, q.field().getDeclaringClass(), seenSet);
	    }
	    public void visit(NEW q) {
		addCheckBeforeAll(q, q.hclass(), seenSet);
	    }
	    public void visit(SET q) {
		if (q.isStatic())
		    addCheckBefore(q, q.field().getDeclaringClass(), seenSet);
	    }
	    // also (optionally) remove synchronization from initializers.
	    public void visit(MONITORENTER q) {
		if (unsyncInitializers) q.remove();
	    }
	    public void visit(MONITOREXIT q) {
		if (unsyncInitializers) q.remove();
	    }
	};
	return hc;
    }
    /** Recursive method to add checks for hc *and* all its superclasses
     *  and interfaces. */
    private static void addCheckBeforeAll(Quad q, HClass hc, Environment seen){
	// first add check for superclass (and all its superclasses)
	HClass su = hc.getSuperclass();
	if (su!=null) addCheckBeforeAll(q, su, seen);
	// then add checks for all interfaces (and all their interfaces)
	HClass[] in = hc.getInterfaces();
	for (int i=0; i<in.length; i++)
	    addCheckBeforeAll(q, in[i], seen);
	// now finally, add check for this.
	// (this order is important so that superclass initializers
	//  get executed first)
	addCheckBefore(q, hc, seen);
    }
    /** Add a call to this class initializer, if it exists and has not
     *  already been invoked on all execution paths leading to q. */
    private static void addCheckBefore(Quad q, HClass class2check,
				       Environment seenSet) {
	QuadFactory qf = q.getFactory();
	if (qf.getMethod().getDeclaringClass().equals(class2check))
	    return; // we've already initialized (or are initializing) this.
	if (seenSet.containsKey(class2check))
	    return; // already checked on this execution path.
	else seenSet.put(class2check, class2check); // don't double check.
	HMethod clinit = class2check.getClassInitializer();
	if (clinit==null) return; // no class initializer for this class.
	assert q.prevLength()==1; // otherwise don't know where to link
	Quad q0 = new CALL(qf, q, clinit, new Temp[0], null, null, false,
			   false, new Temp[0]);
	// insert the new call on an edge
	Edge splitedge = q.prevEdge(0);
	Quad.addEdge((Quad)splitedge.from(), splitedge.which_succ(), q0, 0);
	Quad.addEdge(q0, 0, (Quad)splitedge.to(), splitedge.which_pred());
	// cover this call with the handlers of q
	q0.addHandlers(q.handlers());
	// done.
	return;
    }
    private boolean isVirtual(CALL q) {
	return q.isVirtual() && isVirtual(q.method());
    }
    /** Create a redirection method for native methods we "know" the
     *  dependencies of. */
    private QuadWithTry redirectCode(final HMethod hm) {
	final HMethod orig = select(hm, ORIGINAL);
	final Set inits = (Set) dependentMethods.get(orig);
	// make the Code for this method (note how we work around the
	// protected fields).
	return new QuadWithTry(hm, null) { /* constructor */ {
	    // figure out how many temps we need, then make them.
	    int nargs = hm.getParameterTypes().length + (hm.isStatic()? 0: 1);
	    Temp[] params = new Temp[nargs];
	    for (int i=0; i<params.length; i++)
		params[i] = new Temp(qf.tempFactory());
	    Temp retval = (hm.getReturnType()==HClass.Void) ? null :
		new Temp(qf.tempFactory());
	    // okay, make the dispatch core.
	    Quad q0 = new HEADER(qf, null);
	    Quad q1 = new METHOD(qf, null, params, 1);
	    Quad q2 = new CALL(qf, null, orig, params,
	    		       retval, null, false, true, new Temp[0]);
	    Quad q3 = new RETURN(qf, null, retval);
	    Quad q4 = new FOOTER(qf, null, 2);
	    Quad.addEdge(q0, 0, q4, 0);
	    Quad.addEdge(q0, 1, q1, 0);
	    // leaving out the edge from q1 to q2 for the moment.
	    Quad.addEdge(q2, 0, q3, 0);
	    Quad.addEdge(q3, 0, q4, 1);
	    this.quads = q0;
	    // now make the calls to the static initializers
	    Quad last = q1;
	    for (Object hiO : inits) {
		HInitializer hi = (HInitializer) hiO;
		Quad qq = new CALL(qf, null, hi, new Temp[0], null, null,
				   false, false, new Temp[0]);
		Quad.addEdge(last, 0, qq, 0);
		last = qq;
	    }
	    // and the final link:
	    Quad.addEdge(last, 0, q2, 0);
	    // done!
	} };
    }
    private static Map parseProperties(final Linker linker,
				       final String resourceName) {
	final Map result = new HashMap();
	try {
	    ParseUtil.readResource(resourceName, new ParseUtil.StringParser() {
		public void parseString(String s) throws ParseUtil.BadLineException {
		    int equals = s.indexOf('=');
		    String mname = (equals<0)? s:s.substring(0, equals).trim();
		    HMethod hm = ParseUtil.parseMethod(linker, mname);
		    final Set dep = new HashSet();
		    String depstr = (equals<0) ? "" : s.substring(equals+1);
		    ParseUtil.parseSet(depstr, new ParseUtil.StringParser() {
			public void parseString(String ss)
			    throws ParseUtil.BadLineException {
			    HClass hc = ParseUtil.parseClass(linker, ss);
				// FIXME: SHOULD ADD ALL SUPERCLASS/INTERFACE INITIALIZERS
				// of hc to map, too, and 'dep' should be a *ordered list*
				// not just a set.
			    HInitializer hi = hc.getClassInitializer();
			    if (hi!=null) dep.add(hi);
			}
		    });
		    // if no dependencies, then it's a safe method.
		    // (optimize for space by using a canonical EMPTY_SET)
		    if (dep.size()==0) result.put(hm, Collections.EMPTY_SET);
		    // otherwise, add set to dependent methods map.
		    else result.put(hm, dep);
		    // yay!
		}
	    });
	} catch(java.io.IOException ex) {
	    throw new RuntimeException("FATAL: ERROR READING PROPERTIES FROM " + resourceName, ex);
	}
	// done.
	return result;
    }

    /** Return the handle for the verion of hm with "init-check" tests. */
    public HMethod methodWithInitCheck(HMethod hm) {
	return select(hm, CHECKED);
    }
}
