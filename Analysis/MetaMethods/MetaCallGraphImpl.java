// MetaCallGraphImpl.java, created Wed Mar  8 15:20:29 2000 by salcianu
// Copyright (C) 2000 Alexandru SALCIANU <salcianu@retezat.lcs.mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.MetaMethods;

import java.util.Arrays;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;
import java.util.List;
import java.util.LinkedList;
import java.util.Iterator;
import java.util.Collection;
import java.util.Collections;

import java.lang.reflect.Modifier;

import harpoon.Analysis.ClassHierarchy;
import harpoon.Analysis.ReachingDefs;
import harpoon.Analysis.ReachingDefsImpl;
import harpoon.Analysis.BasicBlock;
import harpoon.ClassFile.HCodeElement;
import harpoon.ClassFile.HCodeFactory;
import harpoon.ClassFile.CachingCodeFactory;
import harpoon.ClassFile.HMethod;
import harpoon.ClassFile.HCode;
import harpoon.ClassFile.HClass;
import harpoon.ClassFile.Linker;
import harpoon.ClassFile.Loader;

import harpoon.IR.Quads.Code;
import harpoon.IR.Quads.QuadVisitor;
import harpoon.IR.Quads.Quad;
import harpoon.IR.Quads.HEADER;
import harpoon.IR.Quads.METHOD;
import harpoon.IR.Quads.CALL;
import harpoon.IR.Quads.MOVE;
import harpoon.IR.Quads.GET;
import harpoon.IR.Quads.AGET;
import harpoon.IR.Quads.NEW;
import harpoon.IR.Quads.ANEW;
import harpoon.IR.Quads.TYPECAST;
import harpoon.IR.Quads.CONST;
import harpoon.IR.Quads.SIGMA;
import harpoon.IR.Quads.PHI;
import harpoon.IR.Quads.CJMP;
import harpoon.IR.Quads.INSTANCEOF;

import harpoon.Temp.Temp;

import harpoon.Util.Graphs.SCComponent;
import harpoon.Util.Graphs.SCCTopSortedGraph;
import harpoon.Analysis.PointerAnalysis.PAWorkList;
import harpoon.Analysis.PointerAnalysis.Debug;

import harpoon.Util.Util;
import harpoon.Util.UComp;

import harpoon.Util.DataStructs.Relation;
import harpoon.Util.DataStructs.RelationImpl;
import harpoon.Util.DataStructs.RelationEntryVisitor;


/**
 * <code>MetaCallGraphImpl</code> is a full-power implementation of the
 <code>MetaCallGraph</code> interface. This is <i>the</i> class to use
 if you want to play with meta-methods.<br>

 Otherwise, you can simply use
 <code>FakeCallGraph</code> which allows you to run things that need
 meta method representation of the program even without generating them
 by simply providing a meta methods-like interface for the standard
 <code>CallGraph</code>.
 * 
 * @author  Alexandru SALCIANU <salcianu@retezat.lcs.mit.edu>
 * @version $Id: MetaCallGraphImpl.java,v 1.6 2002-04-20 20:22:56 salcianu Exp $
 */
public class MetaCallGraphImpl extends MetaCallGraphAbstr {

    private static boolean DEBUG = false;
    private static boolean DEBUG_CH = false;
    private static boolean COUNTER = true;

    /** Make sure the results of the query methods (getCalles like) don't
	depend on the run; facilitate the debugging. */
    public static final boolean DETERMINISTIC = true;

    private static int SPEC_BOUND = 3;

    // in "caution" mode, plenty of tests are done to protect ourselves
    // against errors in other components (ex: ReachingDefs)
    private static final boolean CAUTION = true;
    
    private CachingCodeFactory hcf;
    private ClassHierarchy ch;

    // the set of the classes T such that a new T instruction might be executed
    // by the analyzed application.
    private Set instantiated_classes = null;

    /** Creates a <code>MetaCallGraphImpl</code>. It must receive, in its
	last parameter, the <code>main</code> method of the program. */
    public MetaCallGraphImpl(CachingCodeFactory hcf, ClassHierarchy ch,
			     Set hmroots) {

	assert
	    hcf.getCodeName().equals(harpoon.IR.Quads.QuadNoSSA.codename) ||
	    hcf.getCodeName().equals(harpoon.IR.Quads.QuadSSA.codename) ||
	    hcf.getCodeName().equals(harpoon.IR.Quads.QuadSSI.codename) :
	    "Cannot work with " + hcf.getCodeName();

	System.out.println("MetaCallGraphImpl started with " +
			   hcf.getCodeName());

        this.hcf = hcf;
	this.ch  = ch;

	// HACK: Normally, the call graph and the set instantiated_classes
	// should be computed simultaneously; we use the ClassHierarchy
	// to get the latter.
	// For any type T such that a "new T" might be executed, there is at
	// least one method of T executed. So, we can approximate the set
	// of actually instantiated classes by looking at the declaring
	// class of all callable methods.
	instantiated_classes = new HashSet();
	Set old_ic = ch.instantiatedClasses();
	for(Iterator it = ch.callableMethods().iterator(); it.hasNext(); ) {
	    HMethod hm = (HMethod) it.next();
	    if(Modifier.isStatic(hm.getModifiers())) continue;
	    HClass hclass = hm.getDeclaringClass();
	    if(Modifier.isAbstract(hclass.getModifiers())) continue;
	    if(old_ic.contains(hclass))
		instantiated_classes.add(hclass);
	}
	// however, there is an exception: the arrays (they are objects too) 
	for(Iterator it = ch.instantiatedClasses().iterator(); it.hasNext();) {
	    HClass hclass = (HClass) it.next();
	    if(hclass.isArray())
		instantiated_classes.add(hclass);
	}
	
	if(DEBUG_CH) {
	    Set cls = instantiated_classes;
	    System.out.println("INSTANTIATED CLASS(ES) (" +
			       cls.size() + ") : {");
	    for(Iterator it = cls.iterator(); it.hasNext(); )
		System.out.println("  " + ((HClass) it.next()));
	    System.out.println("}");
	}

	if(COUNTER)
	    System.out.println();
	
	// analyze all the roots
	for(Iterator it = hmroots.iterator(); it.hasNext(); )
	    analyze((HMethod) it.next());

	System.out.println("Avg. call site processing time " +
			   ((double) call_time / nb_calls));
	System.out.println("#calls = " + nb_calls);
	System.out.println("#BIG calls = " + nb_big_calls);

	// convert the big format (Set oriented) into the compact one (arrays)
	compact();
	// activate the GC
	callees1    = null;
	callees2    = null;
	this.ch     = null;
	this.hcf = null;
	analyzed_mm = null;
	WMM         = null;
	mm_work     = null;
	ets2et      = null;
	mh2md       = null;
	param_types = null;
	// null these out so that we can serialize the object [CSA]
	// (alternatively, could mark all of these as 'transient')
	qvis_dd     = null;
	qvis_ti     = null;
	ets2et      = null;
	ets_test    = null;
	implementers = null;
	kids = null;
	instantiated_classes = null;
	// okay, now garbage-collect.
	System.gc();
    }

    // Converts the big format (Set oriented) into the compact one (arrays)
    // Takes the data from callees1(2) and puts it into callees1(2)_cmpct
    private void compact(){
	for(Iterator it = callees1.keys().iterator(); it.hasNext();){
	    MetaMethod mm = (MetaMethod) it.next();
	    Set callees = callees1.getValues(mm);
	    MetaMethod[] mms = 
		(MetaMethod[]) callees.toArray(new MetaMethod[callees.size()]);
	    if(DETERMINISTIC)
		Arrays.sort(mms, UComp.uc);
	    callees1_cmpct.put(mm, mms);
	}

	for(Iterator it = callees2.keySet().iterator(); it.hasNext();){
	    MetaMethod mm = (MetaMethod) it.next();
	    Relation  rel = (Relation) callees2.get(mm);
	    Map map_cmpct = (Map) callees2_cmpct.get(mm);
	    if(map_cmpct == null)
		callees2_cmpct.put(mm,map_cmpct = new HashMap());
	    for(Iterator it_cs = rel.keys().iterator(); it_cs.hasNext();){
		CALL cs = (CALL) it_cs.next();
		// cees is the set of callees in "mm" at call site "cs".
		Set cees = rel.getValues(cs);
		MetaMethod[] mms =
		    (MetaMethod[]) cees.toArray(new MetaMethod[cees.size()]);
		if(DETERMINISTIC)
		    Arrays.sort(mms, UComp.uc); 
		map_cmpct.put(cs, mms);
	    }
	}
    }

    // Relation<MetaMethod, MetaMethod>  stores the association between
    //  the caller and its callees
    private Relation callees1 = new RelationImpl();

    // Map<MetaMethod,Relation<CALL,MetaMethod>> stores the association
    // between the caller and its calees at a specific call site
    private Map      callees2 = new HashMap();    
    
    // Build up the meta call graph, starting from a "root" method.
    // The classic example of a root method is "main", but there could
    // be others called by the JVM before main.
    private void analyze(HMethod main_method){
	// we are extremely conservative: a root method could be called
	// with any subtypes for its arguments, so we treat it as a 
	// polymorphic one.
	analyze(new MetaMethod(main_method, true));
    }

    private PAWorkList WMM = null;
    private Set analyzed_mm = null;
    private MetaMethod mm_work = null;

    private int mm_count = 0;

    private void analyze(MetaMethod root_mm){
	if(COUNTER)
	    System.out.print(root_mm + "  ");

	analyzed_mm = new HashSet();
	WMM = new PAWorkList();
	WMM.add(root_mm);
	while(!WMM.isEmpty()){
	    mm_work = (MetaMethod) WMM.remove();

	    if(COUNTER) {
		mm_count++;
		if(mm_count % 10 == 0)
		    System.out.print(".");
	    }

	    analyzed_mm.add(mm_work);
	    analyze_meta_method();
	}

	all_meta_methods.addAll(analyzed_mm);

	if(COUNTER)
	    System.out.println(" " + mm_count + " analyzed meta-method(s)");
    }

    private Collection calls = null;

    private void analyze_meta_method() {
	if(DEBUG)
	    System.out.println("\n\n%%: " + mm_work);

	HMethod hm = mm_work.getHMethod();

	// native method, no code for analysis  -> return immediately
	if(Modifier.isNative(hm.getModifiers())) return;

	MethodData  md  = get_method_data(hm);
	SCComponent scc = md.first_scc;
	ets2et = md.ets2et;
	calls  = md.calls;

	if(DEBUG){
	    System.out.println("Call sites: =======");
	    for(Iterator it = calls.iterator(); it.hasNext(); ){
		CALL q = (CALL) it.next();
		System.out.println(q.getLineNumber() + "\t" + q);
	    }
	    System.out.println("===================");
	}


	if(DEBUG){
	    System.out.println("SCC of ExactTemp definitions: =====");
	    for(SCComponent scc2=scc; scc2!=null; scc2=scc2.nextTopSort())
		System.out.println(scc2.toString());
	    System.out.println("===================================");
	}

	set_parameter_types(mm_work, hcf.convert(hm));

	compute_types(scc);
	analyze_calls();

	// before quiting this meta-method, remove the types we computed
	// for the ExactTemps. The component graph of ExactTemps (and so,
	// the ExactTemps themselves) is specific to an HMethod and so, 
	// they can be shared by many meta-methods derived from the same
	// HMethod. Of course, I don't want the next analyzed meta-method
	// to use the types computed for this one.
	for(SCComponent scc2 = scc; scc2 != null; scc2 = scc2.nextTopSort()) {
	    Object[] ets = scc2.nodes();
	    for(int i = 0; i < ets.length; i++)
		((ExactTemp) ets[i]).clearTypeSet();
	}
    }


    // after the types of all the interesting ExactTemps are known,
    // examine each call site from this metamethod and build the
    // meta-call-graph.
    private void analyze_calls(){
	for(Iterator it_calls = calls.iterator();it_calls.hasNext();)
	    analyze_one_call((CALL) it_calls.next());
    }

    ////// "analyze_one_call" related stuff BEGIN ==========

    // Records the fact that mcaller could call mcallee at the call site cs.
    private void record_call(MetaMethod mcaller, CALL cs, MetaMethod mcallee){
	callees1.add(mcaller,mcallee);
	Relation rel = (Relation) callees2.get(mcaller);
	if(rel == null)
	    callees2.put(mcaller,rel = new RelationImpl());
	rel.add(cs,mcallee);
    }

    // the specialized types will be stored here.
    private GenType[] param_types = null;

    // counts the number of metamethods called at a "virtual" call site.
    private int nb_meta_methods;

    // determine the exact meta-method which is called at the call site "cs",
    // based on the specialized types found in "param_types".
    private void specialize_the_call(HMethod hm, CALL cs) {
	if(DEBUG) {
	    System.out.println("hm  = " + hm);
	    System.out.println("cs  = " + cs);
	    System.out.print("param_types = [ ");
	    for(int i = 0; i < param_types.length ; i++)
		System.out.print(param_types[i] + " ");
	    System.out.println("]");
	}
	MetaMethod mm_callee = new MetaMethod(hm,param_types);
	nb_meta_methods++;
	record_call(mm_work, cs, mm_callee);
	if(!analyzed_mm.contains(mm_callee))
	    WMM.add(mm_callee);
    }

    // "rec" generates all the possible combinations of types for the
    // parameters of "hm", generates metamethods, records the caller-callee
    // interactions and add the new metamethods to the worklist of metamethods.
    private void rec(HMethod hm, CALL cs, int pos, int max_pos){
	if(pos == max_pos){
	    // all the types have been specialized
	    specialize_the_call(hm,cs);
	    return;
	}

	if(cs.paramType(pos).isPrimitive()){
	    param_types[pos] = new GenType(cs.paramType(pos), GenType.MONO);
	    rec(hm, cs, pos+1, max_pos);
	    return;
	}

	// For any possible ExactType T of the pos-th param, set
	// param_types[pos] to it and recurse on the remaining params.
	ExactTemp et = getExactTemp(cs.params(pos), cs, ExactTemp.USE);

	assert !et.getTypeSet().isEmpty() :
	    "\nNo possible type detected for " + et.t +
	    "\n in method " + cs.getFactory().getMethod() +
	    "\n at instr  " + cs.getSourceFile() + ":" +
	    cs.getLineNumber() + " " + cs;

	for(Iterator it_types = et.getTypes(); it_types.hasNext(); ) {
	    GenType gt = (GenType) it_types.next();
	    param_types[pos] = gt;
	    rec(hm, cs, pos+1, max_pos);
	}

    }


    private HClass jl_Thread =
	Loader.systemLinker.forName("java.lang.Thread");
    private HClass jl_Runnable =
	Loader.systemLinker.forName("java.lang.Runnable");

    // Checks whether calling "hm" starts a thread or not.
    private boolean thread_start_site(HMethod hm){
	String name = hm.getName();
	if(hm.getParameterNames().length != 0) return false;
	if((name==null) || !name.equals("start")) return false;
	if(hm.isStatic()) return false;
	HClass hclass = hm.getDeclaringClass();
	return hclass.getName().equals("java.lang.Thread");
    }

    // Examine a possible thread start site
    private boolean check_thread_start_site(CALL cs){
	if(!thread_start_site(cs.method())) return false;

	boolean found_a_run = false;

	// etbase.t points to the receiver class
	ExactTemp etbase = getExactTemp(cs.params(0), cs, ExactTemp.USE);

	for(Iterator it_gt = etbase.getTypes(); it_gt.hasNext(); ) {
	    GenType gt = (GenType) it_gt.next();
	    Set cls = gt.isPOLY() ?
		get_instantiated_children(gt.getHClass()) :
		Collections.singleton(gt.getHClass());

	    for(Iterator it_cls = cls.iterator(); it_cls.hasNext(); ) {
		boolean hr = has_a_run(cs, (HClass) it_cls.next());
		found_a_run = found_a_run || hr;
	    }
	}
	
	// The 2nd possibility of launching a thread a Java: calling
	// start() on a Thread object that doesn't implement run() but
	// points to a Runnable object.
	// We have to do a very conservative analysis here: examine ANY
	// object that implements Runnable and take its run method.

	Set runnables = get_instantiated_children(jl_Runnable);
	for(Iterator it_cls = runnables.iterator(); it_cls.hasNext(); ) {
	    boolean hr = has_a_run(cs, (HClass) it_cls.next());
	    found_a_run = found_a_run || hr;
	}

	assert found_a_run : "No run method was found for " + cs;

	return true;
    }

    // Check if hclass has a "run" method. If positive, this method
    // could be started as a thread body; it must be put in the meta method
    // worklist if it hasn't been seen before.
    private boolean has_a_run(CALL cs, HClass hclass){
	HMethod run = null;

	HMethod[] hms = hclass.getMethods();
	for(int i = 0; i < hms.length ; i++){
	    if(!hms[i].getName().equals("run")) continue;
	    int modifs = hms[i].getModifiers();
	    if(Modifier.isAbstract(modifs) ||
	       Modifier.isStatic(modifs) ||
	       Modifier.isNative(modifs) ||
	       !Modifier.isPublic(modifs)) continue;
	    if(hms[i].getParameterNames().length != 0) continue;
	    run = hms[i];
	    break;
	}
	
	if(run == null) return false;

	// create a new MetaMethod corresponding to the run method and try
	// to put it into the worklist if it's really new.
	GenType gtbase = new GenType(hclass,GenType.MONO);
	MetaMethod mm  = new MetaMethod(run,new GenType[]{gtbase});
	if(!analyzed_mm.contains(mm))
	    WMM.add(mm);

	run_mms.add(mm);

	// if(DEBUG)
	    System.out.println("\nTHREAD START SITE:" + 
			       cs.getSourceFile() + ":" + 
			       cs.getLineNumber() + " " + 
			       cs + " => " + mm);
	return true;
    }

    private long call_time = 0L;
    private int nb_calls = 0;
    private int nb_big_calls = 0;

    // analyze the CALL site "cs" inside the MetaMethod "mm".
    private void analyze_one_call(CALL cs) {
	HMethod hm = cs.method();
	int nb_params = cs.paramsLength();
	param_types = new GenType[nb_params];

	if(DEBUG) System.out.println("$$:analyze_call(" + cs + ")");
	
	// for 'special' invocations, we know the method exactly
	if(!cs.isVirtual() || cs.isStatic()){
	    if(DEBUG) System.out.println("//: " + cs);
	    if(Modifier.isNative(hm.getModifiers()))
		check_thread_start_site(cs);
	    rec(hm,cs,0,nb_params);
	    return;
	}

	// for native methods, specialization doesn't make any sense
	// because we cannot analyze their body.
	if(Modifier.isNative(hm.getModifiers())){
	    check_thread_start_site(cs);
	    param_types[0] = new GenType(hm.getDeclaringClass(),GenType.POLY);
	    HClass[] types = hm.getParameterTypes();
	    for(int i = 0; i < types.length; i++)
		param_types[i+1] = new GenType(types[i],GenType.POLY);
	    specialize_the_call(hm, cs);
	    return;
	}

	nb_meta_methods = 0;

	if(nb_params == 0)
	    assert false : "Non static method with no parameters " + cs;

	// the first parameter (the method receiver) must be treated specially
	ExactTemp etbase = getExactTemp(cs.params(0), cs, ExactTemp.USE);
	if(CAUTION && etbase.getTypeSet().isEmpty())
	    assert false : "No possible type detected for " + etbase;

	boolean poly = false;

	long start = time();

	for(Iterator it_types = etbase.getTypes(); it_types.hasNext(); ) {
	    GenType gt = (GenType) it_types.next();
	    if(gt.isPOLY()) {
		poly = true;
		treat_poly_base(hm, cs, gt);
	    }
	    else
		treat_mono_base(hm, cs, gt);
	}

	call_time += time() - start;
	nb_calls++;

	if(DEBUG)
	    System.out.println("||: " + cs + " calls " + nb_meta_methods +
			       " meta-method(s)");

	if(DEBUG_CH && (nb_meta_methods == 0)) {
	    System.out.println("ALARM!<\n" + "  mm = " + mm_work + 
			       "\n  " +
			       (poly ? "POLY" : "MONO") + 
			       " cs = " + Debug.code2str(cs) +
			       "> 0 callees!");
	    // display the SCC of exact temps and their types
	    display_mm_data(mm_work);
	}
	
	param_types = null; // enable the GC
    }

    private void display_mm_data(MetaMethod mm) {
	HMethod hm = mm_work.getHMethod();
	MethodData md = get_method_data(hm);
	System.out.println("DATA FOR " + mm_work + ":");
	System.out.println(md);
	System.out.println("COMPUTED TYPES:");
	for(SCComponent scc = md.first_scc; scc != null; 
	    scc = scc.nextTopSort())
	    for(Iterator it = scc.nodeSet().iterator(); it.hasNext(); ) {
		ExactTemp et = (ExactTemp) it.next();
		System.out.println("< " + et.t + ", " + 
				   ((et.ud == ExactTemp.USE)?"USE":"DEF") +
				   ", " + Debug.code2str(et.q) +
				   " > has type(s) {");
		for(Iterator it2 = et.getTypes(); it2.hasNext(); )
		    System.out.println("\t" + ((GenType) it2.next()));
		System.out.println("}");
	    }

	System.out.println("CODE:");
	HCode hcode = hcf.convert(hm);
	hcode.print(new java.io.PrintWriter(System.out, true));
	System.out.println("--------------------------------------");
    }


    // Treat the case of a polymorphic type for the receiver of the method hm
    private void treat_poly_base(HMethod hm, CALL cs, GenType gt) {
	Set classes = get_possible_classes(gt.getHClass(), hm);

	// compute the set of objects that actually provide an
	// implementation for the method hm
	implementers.clear();
	nb_cls = 0;
	for(Iterator it = classes.iterator(); it.hasNext(); ) {
	    HClass c = (HClass) it.next();
	    // fix some strange bug
	    if(c.isPrimitive()) continue;
	    HMethod callee = c.getMethod(hm.getName(), hm.getDescriptor());
	    HClass dc = callee.getDeclaringClass();
	    implementers.add(dc);
	    cls[nb_cls]  = c;
	    impl[nb_cls] = dc;
	    nb_cls++;
 	}

	nb_big_calls++;

	for(Iterator it = implementers.iterator(); it.hasNext(); ) {
	    HClass c = (HClass) it.next();
	    HMethod callee = c.getMethod(hm.getName(), hm.getDescriptor());

	    nb_kids = 0;
	    for(int i = 0; i < nb_cls; i++)
		if(c.equals(impl[i]))
		    kids[nb_kids++] = cls[i];

	    /*
	    System.out.print("  " + c + " -> kids = [ ");
	    for(int i = 0; i < nb_kids; i++)
		System.out.print(kids[i] + " ");
	    System.out.println("]");
	    */

	    if(nb_kids > SPEC_BOUND) {
		param_types[0] = new GenType(c, GenType.POLY);
		rec(callee, cs, 1, cs.paramsLength());
	    }
	    else {
		for(int i = 0; i < nb_kids; i++) {
		    param_types[0] = new GenType(kids[i], GenType.MONO);
		    rec(callee, cs, 1, cs.paramsLength());
		}
	    }
	}
    }
    private Set implementers = new HashSet();
    private HClass kids[] = new HClass[1000];
    private int nb_kids = 0;
    private HClass cls[]  = new HClass[1000];
    private HClass impl[] = new HClass[1000];
    private int nb_cls = 0;

    // Returns the set of all the instantiated (sub)classes of root
    // (including root) that implement the method "hm".
    private Set get_possible_classes(HClass root, HMethod hm) {
	Set children = 
	    get_instantiated_children(root);
	Set possible_classes = new HashSet();

	for(Iterator it = children.iterator(); it.hasNext(); ) {
	    HClass c = (HClass) it.next();
	    if(Modifier.isAbstract(c.getModifiers()))
		continue;
	    boolean implemented = true;
	    HMethod callee = null;
	    try{
		callee = c.getMethod(hm.getName(), hm.getDescriptor());
	    }catch(NoSuchMethodError nsme){
		implemented = false;
	    }
	    if(implemented && !Modifier.isAbstract(callee.getModifiers()))
		possible_classes.add(c);	    
	}
	
	return possible_classes;
    }
    
    // Returns the set of all the subclasses of class root that could
    // be instantiated by the program.
    Set get_instantiated_children(HClass root){
	Set children = new HashSet();
	PAWorkList Wc = new PAWorkList();
	Wc.add(root);
	while(!Wc.isEmpty()) {
	    HClass c = (HClass) Wc.remove();
	    if(instantiated_classes.contains(c))
		children.add(c);
	    for(Iterator it = ch.children(c).iterator(); it.hasNext(); )
		Wc.add(it.next());
	}
	return children;
    }

    // Treat the case of a monomorphyc type for the receiver of the method hm
    private void treat_mono_base(HMethod hm, CALL cs, GenType gt) {
	HClass c = gt.getHClass();
	// fix some strange bug: HClass.Void keeps appearing here!
	if(c.isPrimitive()) return;

	//System.out.println("SMALL call " + cs);

	HMethod callee = null;
	boolean implements_hm = true;
	try{
	    callee = c.getMethod(hm.getName(), hm.getDescriptor());
	}
	catch(NoSuchMethodError nsme){
	    System.out.println("treat_mono_base: " + 
			       Debug.code2str(cs) + " " + nsme);
	    implements_hm = false;
	}

	//System.out.println(" callee = " + callee);

	if(implements_hm && !Modifier.isAbstract(callee.getModifiers())){
	    param_types[0] = gt;
	    rec(callee, cs, 1, cs.paramsLength());
	}
    }

    ////// "analyze_one_call" related stuff END ===========


    // Computes the set of the exact temps we are interested in
    // (the arguments of the CALLs from the code of the analyzed method).
    private final Set get_initial_set(ReachingDefs rdef, Collection calls) {
	Set initial_set = new HashSet();
	for(Iterator it = calls.iterator(); it.hasNext(); ) {
	    CALL q = (CALL) it.next();
	    int nb_params = q.paramsLength();
	    for(int i = 0; i < nb_params; i++)
		if(!q.paramType(i).isPrimitive()) {
		    Temp t = q.params(i);
		    initial_set.add(getExactTemp(t, q, ExactTemp.USE));
		}
	}
	
	return initial_set;
    }

    // Data attached to a method
    private class MethodData {
	// The first scc (in decreasing topological order)
	SCComponent first_scc;
	// The ets2et map for this method (see the comments around ets2et)
	Map ets2et;
	// The set of CALL quads occuring in the code of the method
	Collection calls;

	MethodData(SCComponent first_scc, Map ets2et, Collection calls) {
	    this.first_scc = first_scc;
	    this.ets2et   = ets2et;
	    this.calls    = calls;
	}

	public String toString() {
	    StringBuffer buff = new StringBuffer();
	    buff.append("SCC(s):\n" );
	    for(SCComponent scc = first_scc; scc != null;
		scc = scc.nextTopSort()) {
		buff.append(scc.toString());
	    }
	    buff.append("CALL(s):");
	    for(Iterator it = calls.iterator(); it.hasNext(); )
		buff.append("\n  " + it.next().toString());
	    return buff.toString();
	}
    }

    // Map<HMethod,MethodData>
    private Map mh2md = new HashMap();
    // Adds some caching over "compute_md".
    private MethodData get_method_data(HMethod hm){
	MethodData md = (MethodData) mh2md.get(hm);
	if(md == null) {  // if not in the cache, build it from scratch
	    HCode hcode = hcf.convert(hm);
	    Collection calls = ((Code) hcode).selectCALLs();
	    // initialize the ets2et map (see the comments near its definition)
	    ets2et = new HashMap();
	    ReachingDefs rdef = new ReachingDefsImpl(hcode);
	    Set initial_set = get_initial_set(rdef, calls);
	    SCComponent scc = compute_scc(initial_set, rdef);
	    md = new MethodData(scc, ets2et, calls);
	    mh2md.put(hm, md);
	}
	return md;
    }


    // an "exact" temp: the Temp "t" defined in the Quad "q"
    private class ExactTemp {

	Temp t; // the Temp t that is
	int ud; // defined/used in
	Quad q; // quad q

	static final int DEF = 0;
	static final int USE = 1;

	// Set<GenType> - the possible types of this ExactTemp.
	Set gtypes;

	// The ExactTemps whose types are influenced by the type of this one.
	ExactTemp[] next = new ExactTemp[0];
	// The ExactTemps whose types influence the type of this one.
	ExactTemp[] prev = new ExactTemp[0];

	ExactTemp(Temp t, Quad q, int ud){
	    this.t  = t;
	    this.q  = q;
	    this.ud = ud;
	    this.gtypes = new HashSet();
	}

	/** Returns an iterator over the set of all the possible types
	    for <code>this</code> <code>ExactTemp</code>. */
	Iterator getTypes(){
	    return gtypes.iterator();
	}

	/** Returns the set of all the possible types
	    for <code>this</code> <code>ExactTemp</code>. */
	Set getTypeSet(){
	    return gtypes;
	}

	/** Clears the set of possible types for <code>this</code> ExactTemp.*/
	void clearTypeSet(){
	    gtypes.clear();
	}

	/** Adds the type <code>gt</code> to the set of possible
	    types for <code>this</code> <code>ExactTemp</code>. */
	void addType(GenType type){

	    if(type.isPOLY()){
		// TODO : add some caching here
		Set children = get_instantiated_children(type.getHClass());
		if(children.size() == 1)
		    type = new GenType((HClass)children.iterator().next(),
				       GenType.MONO);
	    }

	    List to_remove = new LinkedList();

	    for(Iterator it = gtypes.iterator() ; it.hasNext() ;){
		GenType gt = (GenType) it.next();
		// no new information here
		if(type.included(gt, ch))
		    return;
		// the new information we add makes some old one redundant
		if(gt.included(type, ch))
		    to_remove.add(type);
	    }
	    
	    for(Iterator it = to_remove.iterator(); it.hasNext(); )
		gtypes.remove((GenType) it.next());
	    
	    gtypes.add(type);
	}

	/** Adds the types from the set <code>gts</code> to the set of possible
	    types for <code>this</code> <code>ExactTemp</code>. */
	void addTypes(Set gts){
	    for(Iterator it = gts.iterator(); it.hasNext();)
		addType((GenType) it.next());
	}

	String shortDescription(){
	    return "<" + t.toString() + ", " +
		((ud == ExactTemp.DEF) ? "DEF" : "USE") + ", " +
		Debug.code2str(q) + ">";
	}

	public String toString(){
	    StringBuffer buffer = new StringBuffer();
	    buffer.append(shortDescription());
	    if((next.length == 0) && (prev.length == 0)){
		buffer.append("\n");
		return buffer.toString();
	    }
	    buffer.append("(\n");
	    if(next.length > 0){
		buffer.append("The following defs depends on this one:\n");
		for(int i = 0 ; i < next.length ; i++)
		    buffer.append("  " + 
				  (next[i]==null?"null":
				   next[i].shortDescription()) + "\n");
	    }
	    if(prev.length > 0){
		buffer.append("Depends on the following defs:\n");
		for(int i = 0 ; i < prev.length ; i++)
		    buffer.append("  " + prev[i].shortDescription() + "\n");
	    }
	    buffer.append(")");
	    return buffer.toString();
	}
    }


    ////// DEPENDENCY DETECTION (getDependencies) - BEGIN

    // Quad visitor class for the dependency detection. This class is
    // declared here (and not inside getDependencies as it were more elegant)
    // such that we are able to allocate a single QuadVisitorDD for the 
    // entire meta call graph construction (instead of one for each analyzed
    // ExactTemp). 
    private class QuadVisitorDD extends QuadVisitor {

	ExactTemp[]  deps = null;
	Temp            t = null;
	int            ud = ExactTemp.DEF;
	ReachingDefs rdef = null;
	
	public void visit(MOVE q) {
	    if(CAUTION && !t.equals(q.dst())) stop_no_def(q);
	    add_deps(q.src(), q);
	}
	
	public void visit(AGET q) {
	    if(CAUTION && !t.equals(q.dst())) stop_no_def(q);
	    add_deps(q.objectref(), q);
	}
	
	public void visit(CALL q) {
	    if(ud == ExactTemp.DEF) {
		if(t.equals(q.retval()) || t.equals(q.retex())) {
		    // t's type will be determined by method signature
		}
		else
		    visit((SIGMA) q);

		return;
	    }

	    Set rdefs = rdef.reachingDefs(q, t);
	    deps = new ExactTemp[rdefs.size()];
	    Iterator it = rdefs.iterator();
	    for(int i = 0; i < rdefs.size(); i++) {
		Quad qdef = (Quad) it.next();
		deps[i] = getExactTemp(t, qdef, ExactTemp.DEF);
	    }
	}
	
	public void visit(NEW q) {
	    if(CAUTION && !t.equals(q.dst())) stop_no_def(q);
	}
	
	public void visit(ANEW q) {
	    if(CAUTION && !t.equals(q.dst())) stop_no_def(q);
	}
	
	public void visit(TYPECAST q) {
	    /*
	    System.out.println
		("\n\ttypecast: " +
		 harpoon.Analysis.PointerAnalysis.Debug.code2str(q) + "\n");
	    */

	    if(CAUTION && !t.equals(q.objectref())) stop_no_def(q);
	}
	
	public void visit(GET q) {
	    if(CAUTION && !t.equals(q.dst())) stop_no_def(q);
	}
	
	public void visit(METHOD q) {
	    // do nothing: the type of "et" defined here does not
	    // depend on any other ExactType's type.
	    if(CAUTION) {
		boolean found = false;
		for(int i = 0; i < q.paramsLength(); i++)
		    if(q.params(i).equals(t)) {
			found = true;
			break;
		    }
		if(!found) stop_no_def(q);
	    }
	}
	
	public void visit(CONST q) {
	    if(CAUTION && !t.equals(q.dst())) stop_no_def(q);
	}


	// SSI additions BEGIN
	public void visit(PHI q) {
	    int numPhis = q.numPhis();
	    for(int i = 0; i < numPhis; i++)
		if(t.equals(q.dst(i))) {
		    add_deps(q.src(i), q);
		    return;
		}
	    
	    stop_no_def(q);
	}


	// recognize the translation of a TYPECAST in QuadSSI:
	// a CJMP t2 preceded by a t2 = t INSTANCEOF hclass; treat it
	// as a TYPECAST
	public void visit(CJMP q) {
	    if(is_typecast(q, t) != null) {
		// do nothing; t's type will be determined by the typecast
		/*
		System.out.println
		    ("\n\ttypecast " +
		     harpoon.Analysis.PointerAnalysis.Debug.code2str(q.prev(0))
		     + " \n" + 
		     harpoon.Analysis.PointerAnalysis.Debug.code2str(q) +
		     "\n");
		*/
	    }
	    else
		visit((SIGMA) q);
	}


	public void visit(SIGMA q) {
	    int numSigmas = q.numSigmas();
	    for(int i = 0; i < numSigmas; i++) {
		// src(i) goes into dst(i)[0] ... dst(i)[dst.length-1]
		Temp dst[] = q.dst(i);
		for(int j = 0; j < dst.length; j++)
		    if(t.equals(dst[j])) {
			add_deps(q.src(i), q);
			return;
		    }
	    }

	    stop_no_def(q);
	}
	// SSI additions END

	// catch the forgotten quads
	public void visit(Quad q) {
	    assert false : "Unsupported Quad " + q;
	}
	
	// crash the system in the most spectacular way
	private void stop_no_def(Quad q) {
	    assert false : q + " doesn't define " + t;
	}
	
	// The Temp "tdep" is used in quad "q" to define "t" and
	// can affect its type. This function qoes along the list
	// of possible definitions for tdep and put the corresponding
	// ExactTemp's in the array of dependencies for <t,q>.
	private void add_deps(Temp tdep, Quad q) {
	    Set set = new HashSet();
	    collect_deps(tdep, q, set);
	    deps = (ExactTemp[]) set.toArray(new ExactTemp[set.size()]);
	}

	private void add_deps(Temp src[], Quad q) {
	    Set set = new HashSet();
	    for(int i = 0; i < src.length; i++)
		collect_deps(src[i], q, set);
	    deps = (ExactTemp[]) set.toArray(new ExactTemp[set.size()]);
	}

	// collect into set the exact temps that flow into <tdep, q>
	private void collect_deps(Temp tdep, Quad q, Set set) {
	    Set reaching_defs = rdef.reachingDefs(q, tdep);

	    assert
		!reaching_defs.isEmpty() :
		"Temp " + tdep + " in " + q + " has no reaching definition!";

	    for(Iterator it = reaching_defs.iterator(); it.hasNext(); ) {
		Quad qdef = (Quad) it.next();
		set.add(getExactTemp(tdep, qdef)); 
	    }
	}


    };
    private QuadVisitorDD qvis_dd = new QuadVisitorDD();


    private void show_deps(ExactTemp et, ExactTemp deps[]) {
	System.out.print("\nDeps for " + et.shortDescription() + " = { ");
	for(int i = 0; (deps != null) && (i < deps.length); i++)
	    System.out.print(deps[i].shortDescription() + " ");
	System.out.println(" } ");
    }


    // Returns all the ExactTemps whose types influence the type of the
    // ExactType et.
    private ExactTemp[] getDependencies(ExactTemp et, ReachingDefs rdef) {
	qvis_dd.t    = et.t;
	qvis_dd.ud   = et.ud;
	qvis_dd.deps = null;
	qvis_dd.rdef = rdef;

	et.q.accept(qvis_dd);

	if(qvis_dd.deps == null)
	    return (new ExactTemp[0]);
	return qvis_dd.deps;
    }


    // Checks whether q corresponds to a typecast that defines t to be
    // the restriction of some temp t2 to some class hclass. This means:
    // 1. q has a single predecessor: tb = t2 INSTANCEOF hclass
    // 2. q has the form              CJMP tb 
    // 3. t is t2 variant on branch 1: < _ , t > = sigma(t2)
    // If all these conditions are true, return hclass, o.w. return null
    private static HClass is_typecast(CJMP q, Temp t) {
	if(q.prevLength() == 1) {
	    Quad prev = q.prev(0);
	    if(prev instanceof INSTANCEOF) {
		INSTANCEOF inst = (INSTANCEOF) prev;
		Temp tb = inst.dst();
		if(tb.equals(q.test())) {
		    Temp tested = inst.src();
		    int i = getSigmaForTemp(q, tested);

		    if(i == -1) return null;
		    //assert (i != -1) : tested + " is not defined by " + q;

		    Temp dst[] = q.dst(i);
		    if(t.equals(dst[1])) {
			// this is actually a typecast !
			return inst.hclass();
		    }
		}
	    }
	}
	// not a typecast
	return null;
    }

    // returns the index of the sigma function that has t_src as a
    // source in the big SIGMA node q (with potentially many sigma
    // functions)
    private static int getSigmaForTemp(SIGMA q, Temp t_src) {
	for(int i = 0; i < q.numSigmas(); i++) {
	    if(q.src(i).equals(t_src))
		return i; // we found it!
	}
	// t is not defined by this SIGMA
	return -1;
    }

    ////// DEPENDENCY DETECTION (getDependencies) - END


    // The following code is responsible for assuring that at most one
    // ExactTemp object representing the temp t defined/used at quad q exists
    // at the execution time. This is necessary since we keep the next and
    // prev info *into* the ExactTemp structure and not in a map (for
    // efficiency reasons), so all the info related to a conceptual ExactTemp
    // must be in a single place.
    // This is enforced by having a Map<ExactTempS,ExactTemp> that maps
    // an ExactTempS (just a <t, q, ud> tuple) to the full ExactTemp object.
    // When we need the ExactTemp for a given <t, q, ud>
    // pair, we search <t, q, ud> in that map; if found we return the existent 
    // object, otherwise we create a new one and put it into the map.
    // Of course, no part of the program should dirrectly allocate an
    // ExactTemp, instead the "getExactTemp" function should be called. 
    private class ExactTempS { // S for short
	Temp t;
	Quad q;
	int ud;
	ExactTempS(Temp t, Quad q, int ud) {
	    this.t  = t;
	    this.q  = q;
	    this.ud = ud;
	}
	public int hashCode(){
	    return t.hashCode() + q.hashCode() + ud;
	}
	public boolean equals(Object o){
	    ExactTempS ets2 = (ExactTempS) o;
	    return
		(this.t  == ets2.t) &&
		(this.q  == ets2.q) && 
		(this.ud == ets2.ud);
	}
    }
    private Map ets2et = null;
    private ExactTempS ets_test = new ExactTempS(null, null, ExactTemp.DEF);
    private ExactTemp getExactTemp(Temp t, Quad q, int ud) {
	ets_test.t  = t;
	ets_test.q  = q;
	ets_test.ud = ud;
	ExactTemp et = (ExactTemp) ets2et.get(ets_test);
	if(et == null){
	    et = new ExactTemp(t, q, ud);
	    ExactTempS ets = new ExactTempS(t, q, ud);
	    ets2et.put(ets, et);
	}
	return et;
    }
    private ExactTemp getExactTemp(Temp t, Quad q) {
	return getExactTemp(t, q, ExactTemp.DEF);
    }

    // the end of "unique ExactTemp" code.
    

    // Computes the topologically sorted list of strongly connected
    // components containing the definitions of the "interesting" temps.
    // The edges between this nodes models the relation
    // "the type of ExactType x influences the type of the ExactType y"
    // Returns the first element of the sorted list
    // (in reverse topological order)
    // (the rest can be retrieved by navigating with nextTopSort())
    private SCComponent compute_scc(Set initial_set, ReachingDefs rdef) {
	// 1. Compute the graph: ExactTemps, successors and predecessors.
	// predecessors are put into the prev field of the ExactTemps;
	// successors are not put into the ExactTemps, but into a separate
	// relation - "next_rel" - as they are gradually discovered.
	// ReachingDefs gives us the prev part.
	Relation next_rel = new RelationImpl();
	Set already_visited = new HashSet();
	PAWorkList W = new PAWorkList();

	W.addAll(initial_set);
	while(!W.isEmpty()){
	    ExactTemp et = (ExactTemp) W.remove();
	    already_visited.add(et);

	    et.prev = getDependencies(et, rdef);

	    for(int i = 0; i < et.prev.length; i++){
		ExactTemp et2 = (ExactTemp) et.prev[i];

		assert (et2 != null) : "Something wrong with " + et;

		next_rel.add(et2, et);
		if(!already_visited.contains(et2))
		    W.add(et2);
	    }
	}

	// now, put the predecessors in the ExactTemps
	for(Iterator it = next_rel.keys().iterator(); it.hasNext(); ) {
	    ExactTemp et = (ExactTemp) it.next();
	    Set next = next_rel.getValues(et);
	    et.next = (ExactTemp[]) next.toArray(new ExactTemp[next.size()]);
	}

	// 2. Build the component graph and sort it topollogically.

	// Navigator into the graph of ExactTemps. prev returns the ExactTemps
	// whose types influence the type of "node" ExactTemp; next returns
	// the ExactTemps whose types are affected by the type of node.  
	SCComponent.Navigator et_navigator =
	    new SCComponent.Navigator() {
		    public Object[] next(Object node){
			return ((ExactTemp) node).next;
		    }
		    public Object[] prev(Object node){
			return ((ExactTemp) node).prev;
		    }
		};

	Object[] roots_for_scc =
	    already_visited.toArray(new Object[already_visited.size()]);
	Set scc_set = SCComponent.buildSCC(roots_for_scc, et_navigator);
	SCCTopSortedGraph ts_scc = SCCTopSortedGraph.topSort(scc_set);

	return ts_scc.getFirst();
    }


    // Set the types of the parameters of the method underlying mm, using the
    // appropriate type specializations.
    private void set_parameter_types(MetaMethod mm, HCode hcode){
	METHOD m = (METHOD) ((HEADER) hcode.getRootElement()).next(1);

	int nb_params = m.paramsLength();
	if(CAUTION && (nb_params != mm.nbParams()))
	    assert false : "Wrong number of params in " + m;

	for(int i = 0; i < nb_params ; i++)
	    getExactTemp(m.params(i), m).addType(mm.getType(i));
    }


    // computes the types of the interesting ExactTemps, starting 
    // with those in the strongly connected component "scc".
    private void compute_types(SCComponent scc) {
	for( ; scc != null; scc = scc.nextTopSort())
	    process_scc(scc);
    }


    // TYPE INFERENCE QUAD VISITOR - BEGIN

    private class QuadVisitorTI extends QuadVisitor {

	Temp t       = null;
	ExactTemp et = null;
	
	public void visit(MOVE q) {
	    if(CAUTION && !t.equals(q.dst()))
		stop_no_def(q);
	    treat_move_use();
	}

	public void visit(GET q) {
	    if(CAUTION && !t.equals(q.dst()))
		stop_no_def(q);
	    et.addType(new GenType(q.field().getType(), GenType.POLY));
	}

	public void visit(AGET q) {
	    if(CAUTION && !t.equals(q.dst()))
		stop_no_def(q);
	    
	    // For all the possible types for the source array, take
	    // the type of the component 
	    for(int i = 0; i < et.prev.length; i++) {
		ExactTemp eta = et.prev[i];
		for(Iterator it_types = eta.getTypes(); it_types.hasNext(); ) {
		    HClass c = ((GenType) it_types.next()).getHClass();
		    if(c.equals(HClass.Void))
			et.addType(new GenType(HClass.Void, GenType.MONO));
		    else {
			HClass hcomp = c.getComponentType();
			assert
			    hcomp != null :
			    q.objectref() + " could have non-array types in "
			    + q;
			et.addType(new GenType(hcomp, GenType.POLY));
		    }
		}
	    }
	}
	
	// Aux. data for visit(CALL q).
	// Any method can throw an exception that is subclass of these
	// two classes (without explicitly declaring it).
	private final HClass jl_RuntimeException =
	    Loader.systemLinker.forName("java.lang.RuntimeException");
	private final HClass jl_Error =
	    Loader.systemLinker.forName("java.lang.Error");

	public void visit(CALL q) {
	    if(et.ud == ExactTemp.DEF) {
		if(t.equals(q.retval())) {
		    et.addType(new GenType(q.method().getReturnType(),
					   GenType.POLY));
		    return;
		}
		if(t.equals(q.retex())) {
		    HClass[] excp = q.method().getExceptionTypes();
		    for(int i = 0; i < excp.length; i++)
			et.addType(new GenType(excp[i], GenType.POLY));
		    // According to the JLS, exceptions that are subclasses of
		    // java.lang.RuntimeException and java.lang.Error need
		    // not be explicitly declared; they can be thrown by any
		    // method.
		    et.addType(new GenType(jl_RuntimeException, GenType.POLY));
		    et.addType(new GenType(jl_Error, GenType.POLY));
		    return;
		}

		visit((SIGMA) q);
	    }
	    else    // it's a USE; we just have to merge the reaching defs
		treat_move_use();
	}
	
	public void visit(NEW q) {
	    if(CAUTION && !t.equals(q.dst()))
		stop_no_def(q);
	    et.addType(new GenType(q.hclass(),GenType.MONO));
	}
	
	public void visit(ANEW q) {
	    if(CAUTION && !t.equals(q.dst()))
		stop_no_def(q);
	    et.addType(new GenType(q.hclass(),GenType.MONO));
	}
	    
	public void visit(TYPECAST q) {
	    if(CAUTION && !t.equals(q.objectref()))
		stop_no_def(q);
	    et.addType(new GenType(q.hclass(), GenType.POLY));
	}
	
	public void visit(METHOD q) {
	    // do nothing; the types of the parameters have been
	    // already set by set_parameter_types.
	    if(CAUTION) {
		boolean found = false;
		for(int i = 0; i < q.paramsLength(); i++)
		    if(q.params(i).equals(t)){
			found = true;
			break;
		    }
		if(!found) stop_no_def(q);
	    }
	}
	
	public void visit(CONST q) {
	    if(CAUTION) {
		if(!t.equals(q.dst())) stop_no_def(q);
	    }
	    et.addType(new GenType(q.type(), GenType.MONO));
	}

	// SSI additions BEGIN
	public void visit(PHI q) {
	    treat_move_use();
	}

	// recognize the translation of a TYPECAST in QuadSSI:
	// a CJMP t2 preceded by a t2 = t INSTANCEOF hclass
	public void visit(CJMP q) {
	    HClass hclass = is_typecast(q, t);
	    if(hclass != null)
		et.addType(new GenType(hclass, GenType.POLY));
	    else
		visit((SIGMA) q);
	}

	public void visit(SIGMA q) {
	    treat_move_use();
	}
	// SSI additions END
	
	// treat MOVE-like instructions
	private void treat_move_use() {
	    for(int i = 0; i < et.prev.length; i++)
		et.addTypes(et.prev[i].getTypeSet());	    
	}
	
	public void visit(Quad q) {
	    assert false : "Unsupported Quad " + q;
	}
	
	private void stop_no_def(Quad q) {
	    assert false : q + " doesn't define " + t;
	}
	
    };
    private QuadVisitorTI qvis_ti = new QuadVisitorTI();
    
    // TYPE INFERENCE QUAD VISITOR - END
    
    
    private void process_scc(SCComponent scc) {
	final PAWorkList W = new PAWorkList();

	if(DEBUG)
	    System.out.println("Processing " + scc);
	
	W.addAll(scc.nodeSet());
	while(!W.isEmpty()) {
	    ExactTemp et = (ExactTemp) W.remove();

	    // This inelegant modality of passing parameters to the
	    // type inference quad visitor is motivated by our struggle to
	    // create a single QuadVisitorTI object.
	    qvis_ti.t  = et.t;
	    qvis_ti.et = et;

	    Set old_gen_type = new HashSet(et.getTypeSet()); 
	    et.q.accept(qvis_ti);
	    Set new_gen_type = et.getTypeSet();

	    if(!new_gen_type.equals(old_gen_type))
		for(int i = 0; i < et.next.length; i++){
		    ExactTemp et2 = et.next[i];
		    if(scc.contains(et2)) W.add(et2);
		}
	}

	if(DEBUG)
	    for(Iterator it = scc.nodeSet().iterator(); it.hasNext();){
		ExactTemp et = (ExactTemp) it.next();
		System.out.println("##:< " + et.shortDescription() + 
				   " -> " + et.getTypeSet() + " >");
	    }
    }


    private static long time() {
	return System.currentTimeMillis();
    }
}

