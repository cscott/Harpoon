// PAMain.java, created Fri Jan 14 10:54:16 2000 by salcianu
// Copyright (C) 1999 Alexandru SALCIANU <salcianu@MIT.EDU>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Main;

import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.Date;
import java.util.Set;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.LinkedList;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.ObjectOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import gnu.getopt.Getopt;
import gnu.getopt.LongOpt;

import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HCode;
import harpoon.ClassFile.HMethod;
import harpoon.ClassFile.HCodeFactory;
import harpoon.ClassFile.HCodeElement;
import harpoon.ClassFile.CachingCodeFactory;
import harpoon.ClassFile.Linker;
import harpoon.ClassFile.Loader;

import harpoon.IR.Quads.Quad;
import harpoon.IR.Quads.QuadVisitor;

import harpoon.Analysis.Quads.QuadClassHierarchy;
import harpoon.Analysis.Quads.CallGraph;
import harpoon.Analysis.Quads.CallGraphImpl;
import harpoon.Analysis.AllCallers;
import harpoon.Analysis.ClassHierarchy;
import harpoon.Analysis.BasicBlock;

import harpoon.Analysis.PointerAnalysis.PointerAnalysis;
import harpoon.Analysis.PointerAnalysis.PANode;
import harpoon.Analysis.PointerAnalysis.ParIntGraph;
import harpoon.Analysis.PointerAnalysis.Relation;
import harpoon.Analysis.PointerAnalysis.MAInfo;

import harpoon.Analysis.MetaMethods.MetaMethod;
import harpoon.Analysis.MetaMethods.MetaCallGraph;
import harpoon.Analysis.MetaMethods.FakeMetaCallGraph;
import harpoon.Analysis.MetaMethods.MetaCallGraphImpl;
import harpoon.Analysis.MetaMethods.MetaAllCallers;

import harpoon.Util.BasicBlocks.BBConverter;
import harpoon.Util.BasicBlocks.CachingBBConverter;
import harpoon.Util.LightBasicBlocks.LightBasicBlock;
import harpoon.Util.LightBasicBlocks.LBBConverter;
import harpoon.Util.LightBasicBlocks.CachingLBBConverter;
import harpoon.Util.Graphs.SCComponent;
import harpoon.Util.Graphs.SCCTopSortedGraph;

import harpoon.Analysis.PointerAnalysis.Debug;
import harpoon.Analysis.MetaMethods.SmartCallGraph;

import harpoon.IR.Quads.CALL;


/**
 * <code>PAMain</code> is a simple Pointer Analysis top-level class.
 * It is designed for testing and evaluation only.
 * 
 * @author  Alexandru SALCIANU <salcianu@retezat.lcs.mit.edu>
 * @version $Id: PAMain.java,v 1.1.2.62 2000-06-19 16:56:39 salcianu Exp $
 */
public abstract class PAMain {

    // use the real meta call graph
    private static boolean METAMETHODS = false;
    // use FakeMetaCallGraph(SmartCallGraph)
    private static boolean SMART_CALL_GRAPH = false;
    // debug the class hierarchy
    private static boolean SHOW_CH = false;

    // load the meta call graph from a file.
    private static boolean LOAD_MCG = false;
    private static String  MCG_FILE_NAME = "";

    // show the call graph
    private static boolean SHOW_CG = false;
    // show the split relation HMethod -> MetaMethods
    private static boolean SHOW_SPLIT = false;
    // show some details/statistics about the analysis
    private static boolean SHOW_DETAILS = false;
    // show some statistics about the method hole of the analyzed program
    private static boolean HOLE_STATS = false;

    // make the program to analyse some method;
    private static boolean DO_ANALYSIS = false;
    // turns on the interactive analysis
    private static boolean DO_INTERACTIVE_ANALYSIS = false;

    // turns on the computation of the memory allocation policies
    private static boolean MA_MAPS = false;
    // the name of the file into which the memory allocation policies
    // will be serialized
    private static String MA_MAPS_OUTPUT_FILE = null;
    // use the inter-thread stage of the analysis while determining the
    // memory allocation policies
    private static boolean USE_INTER_THREAD = true;

    private static boolean DEBUG = false;

    private static String[] examples = {
	"java -mx200M harpoon.Main.PAMain -a multiplyAdd --ccs=2 --wts" + 
	"harpoon.Test.PA.Test1.complex",
	"java -mx200M harpoon.Main.PAMain -s -a run " + 
	"harpoon.Test.PA.Test2.Server",
	"java -mx200M harpoon.Main.PAMain -s " + 
	"-a harpoon.Test.PA.Test3.multisetElement.insert" + 
	" harpoon.Test.PA.Test3.multiset ",
	"java -mx200M harpoon.Main.PAMain -s -a sum " +
	"harpoon.Test.PA.Test4.Sum",
	"java harpoon.Main.PAMain -a foo harpoon.Test.PA.Test5.A"
    };

    private static String[] options = {
	"-m, --meta     uses the real MetaMethod",
	"-s, --smart    uses the SmartCallGrapph",
	"-d, --dumb     uses the simplest CallGraph (default)",
	"-c, --showch   shows debug info about ClassHierrachy",
	"-l file        load a precomputed MetaCallGraph from a file",
	"--showcg       shows the (meta) call graph",
	"--showsplit    shows the split relation",
	"--details      shows details/statistics: analyzed methods, nodes etc",
	"--ccs=depth    activate call context sensitivity with a maximum",
	"              call chain depth of depth",
	"--ts           activates full thread sensitivity",
	"--wts          activates weak thread sensitivity",
	"--ls           activates loop sensitivity",
	"--mamaps=file  computes the allocation policy map and serializes",
	"              the CachingCodeFactory (and implicitly the allocation",
	"              map) and the linker to disk",
	"--mastats      shows some statistics about memory allocation",
	"--holestats    shows statistics about holes",
	"-a method      analyzes he given method. If the method is in the",
	"              same class as the main method, the name of the class",
	"              can be ommited",
	"-i             interactive analysis of methods",
	"--noit         just interprocedural analysis, no interthread",
	"--inline       use method inlining to enable more stack allocation",
	"              (makes sense only with --mamaps)",
	"--sat=file     generates dummy sets of the .start() and .join() that",
	"              must be changed (for the thread inlining). Don't try",
	"              to use it seriously",
	"--notg         no thread group facility is necessary. In the future,",
	"              this will be automatically generated by the analysis."
    };


    private static boolean DO_SAT = false;
    private static String SAT_FILE = null;

    private static PointerAnalysis pa = null;
    // the main method
    private static HMethod hroot = null;

    // list to maintain the methods to be analyzed
    private static List mm_to_analyze = new LinkedList();
    
    private static class Method{
	String name  = null;
	String declClass = null;
    }

    private static Method root_method = new Method();

    private static MetaCallGraph  mcg = null;
    private static MetaAllCallers mac = null;
    private static Relation split_rel = null;

    private static Set roots = null;

    private static Linker linker = Loader.systemLinker;

    // the code factory (in fact a quad factory) generating the code of
    // the methods.
    private static HCodeFactory hcf = null;


    // global variables used for benchmarking (19/05/2000).
    // TODO: Remove with the first occasion!
    private static long g_tstart = 0L;
    private static long g_tend   = 0L;

    public static final void main(String[] params){

	int optind = get_options(params);
	int nbargs = params.length - optind;

	if(nbargs < 1){
	    System.out.println("Usage:\n" +
	     "\tjava harpoon.Main.PAMain [options] <main_class>\n");

	    System.out.println("Options:");
	    for(int i = 0; i < options.length; i++)
		System.out.println("\t" + options[i]);


	    System.out.println("Examples:");
	    for(int i = 0; i < examples.length; i++)
		System.out.println("\t" + examples[i]);

	    System.out.println("Suggestion:\n" +
	     "\tYou might consider the \"-mx\" flag of the JVM to satisfy\n" +
	     "\tthe huge memory requirements of the pointer analysis.\n" +
	     "Warning:\n\t\"Quite fast for small programs!\"" + 
	     " [Moolly Sagiv]\n" +
	     "\t\t... and only for them :-(");

	    System.exit(1);
	}

	print_options();

	// variables for the timing stuff.
	long tstart = 0;
	long tstop  = 0;

	long start_pre_time = System.currentTimeMillis();

	//B/
	g_tstart = System.currentTimeMillis();


	root_method.name = "main";
	root_method.declClass = params[optind];
	optind++;
	HClass hclass = linker.forName(root_method.declClass);
	HMethod[] hm  = hclass.getDeclaredMethods();

	// search for the main method
	hroot = null;
	for(int i = 0;i<hm.length;i++)
	    if(hm[i].getName().equals(root_method.name))
		hroot = hm[i];
	if(hroot == null){
	    root_method.name = "init"; // maybe it's an applet
	    hroot = null;
	    for(int i = 0;i<hm.length;i++)
		if(hm[i].getName().equals(root_method.name))
		    hroot = hm[i];

	    if(hroot == null){
		System.out.println("Sorry, the root method was not found\n");
		System.exit(1);
	    }
	}

	System.out.println("Root method: " + root_method.declClass + "." +
			   root_method.name);

	hcf = new CachingCodeFactory(harpoon.IR.Quads.QuadNoSSA.codeFactory(),
				     true);

	Set roots = new HashSet();
	roots.add(hroot);
	roots.addAll(
	    harpoon.Backend.Runtime1.Runtime.runtimeCallableMethods(linker));

	if(SHOW_CH){
	    System.out.println("Set of roots: {");
	    for(Iterator it = roots.iterator(); it.hasNext(); ){
		Object o = it.next();
		if(o instanceof HMethod)
		    System.out.println(" m: " + o);
		else
		    System.out.println(" c: " + o);
	    }
	    System.out.println("}");
	}

	System.out.print("ClassHierarchy ... ");
	tstart = System.currentTimeMillis();
	ClassHierarchy ch = new QuadClassHierarchy(linker, roots, hcf);
	    // new QuadClassHierarchy(linker,Collections.singleton(hroot),hcf);
	tstop  = System.currentTimeMillis();
	System.out.println((tstop - tstart) + "ms");

	if(SHOW_CH){
	    System.out.println("Root method = " + hroot);	    
	    System.out.println("Instantiated classes: {");
	    Set inst_cls = ch.instantiatedClasses();
	    for(Iterator it = inst_cls.iterator(); it.hasNext(); )
		System.out.println(" " + it.next());
	    System.out.println("}");
	}

	// I think our set of roots is too conservative and pollutes
	// our call graph too much. I'm considering only the methods
	// that could be called (directly or indirectly) by main
	// or by threads (transitively) launched by main.
	/*
	  Set mroots = extract_method_roots(roots);
	  mroots.add(hroot);
	*/

	CachingBBConverter bbconv = new CachingBBConverter(hcf);
	LBBConverter lbbconv = new CachingLBBConverter(bbconv);

	if(METAMETHODS){ // real meta-methods
	    System.out.print("MetaCallGraph ... ");
	    tstart = System.currentTimeMillis();
	    mcg = new MetaCallGraphImpl(bbconv, ch,
					Collections.singleton(hroot));
	    // mcg = new MetaCallGraphImpl(bbconv, ch, mroots);
	    tstop  = System.currentTimeMillis();
	    System.out.println((tstop - tstart) + "ms");

	    System.out.print("MetaAllCallers ... ");
	    tstart = System.currentTimeMillis();
	    mac = new MetaAllCallers(mcg);
	    tstop = System.currentTimeMillis();
	    System.out.println((tstop - tstart) + "ms");
	}
	else{
	    // the set of "run()" methods (the bodies of threads)
	    Set run_mms = null;
	    CallGraph cg = null;
	    if(SMART_CALL_GRAPH){ // smart call graph!
		System.out.print("MetaCallGraph ... ");
		tstart = System.currentTimeMillis();

		MetaCallGraph fmcg = 
		    new MetaCallGraphImpl(bbconv, ch,
					  Collections.singleton(hroot));
		tstop  = System.currentTimeMillis();
		System.out.println((tstop - tstart) + "ms");

		run_mms = fmcg.getRunMetaMethods();

		System.out.print("SmartCallGraph ... ");
		tstart = System.currentTimeMillis();
		cg = new SmartCallGraph(fmcg);
		tstop  = System.currentTimeMillis();
		System.out.println((tstop - tstart) + "ms");
	    }
	    else
		cg = new CallGraphImpl(ch, hcf);

	    System.out.print("FakeMetaCallGraph ... ");
	    tstart = System.currentTimeMillis();
	    mcg = new FakeMetaCallGraph(cg, cg.callableMethods(), run_mms);
	    tstop  = System.currentTimeMillis();
	    System.out.println((tstop - tstart) + "ms");
	    
	    System.out.print("(Fake)MetaAllCallers ... ");
	    tstart = System.currentTimeMillis();
	    mac = new MetaAllCallers(mcg);
	    tstop  = System.currentTimeMillis();
	    System.out.println((tstop - tstart) + "ms");
	}

	/* JOIN STATS
	   join_stats(lbbconv, mcg);
	   System.exit(1);
	*/

	System.out.print("SplitRelation ... ");
	tstart = System.currentTimeMillis();
	split_rel = mcg.getSplitRelation();
	tstop  = System.currentTimeMillis();
	System.out.println((tstop - tstart) + "ms");

	if(SHOW_CG){
	    System.out.println("MetaCallGraph:");
	    mcg.print(new java.io.PrintWriter(System.out, true), true,
		      new MetaMethod(hroot, true));
	}

	if(SHOW_SPLIT){
	    System.out.println("Split relation:");
	    Debug.show_split(mcg.getSplitRelation());
	}

	//B/
	System.out.println("Total pre-analysis time : " +
			   (System.currentTimeMillis() - start_pre_time) +
			   "ms");

	pa = new PointerAnalysis(mcg, mac, lbbconv);

	if(DO_ANALYSIS){
	    for(Iterator it = mm_to_analyze.iterator(); it.hasNext(); ){
		Method analyzed_method = (Method) it.next();
		if(analyzed_method.declClass == null)
		    analyzed_method.declClass = root_method.declClass;
		display_method(analyzed_method);
	    }
	}

	if(DO_INTERACTIVE_ANALYSIS){
	    BufferedReader d = 
		new BufferedReader(new InputStreamReader(System.in));
	    while(true){
		System.out.print("Method name:");
		String method_name = null;
		try{
		    method_name = d.readLine();
		}catch(IOException e){}
		if(method_name == null){
		    System.out.println();
			break;
		}
		Method analyzed_method = getMethodName(method_name);
		if(analyzed_method.declClass == null)
		    analyzed_method.declClass = root_method.declClass;
		display_method(analyzed_method);
	    }
	}
    
	if(MA_MAPS)
	    ma_maps(pa, hroot);

	if(SHOW_DETAILS)
	    pa.print_stats();

	if(DO_SAT)
	    do_sat();
    }
    
    private static void display_method(Method method){
	HClass hclass = linker.forName(method.declClass);
	HMethod[] hm  = hclass.getDeclaredMethods();
	int nbmm = 0;

	HMethod hmethod = null;		
	for(int i = 0; i < hm.length; i++)
	    if(hm[i].getName().equals(method.name)){
		hmethod = hm[i];

		// look for all the meta-methods originating into this method
		// and do all the analysis stuff on them.
		for(Iterator it = split_rel.getValues(hmethod); it.hasNext();){
		    nbmm++;
		    MetaMethod mm = (MetaMethod) it.next();
		    System.out.println("HMETHOD " + hmethod
				       + " ->\n META-METHOD " + mm);
		    ParIntGraph int_pig = pa.getIntParIntGraph(mm);
		    ParIntGraph ext_pig = pa.getExtParIntGraph(mm);
		    ParIntGraph pig_inter_thread = pa.threadInteraction(mm);
		    PANode[] nodes = pa.getParamNodes(mm);
		    System.out.println("META-METHOD " + mm);
		    System.out.print("POINTER PARAMETERS: ");
		    System.out.print("[ ");
		    for(int j = 0; j < nodes.length; j++)
			System.out.print(nodes[j] + " ");
		    System.out.println("]");
		    System.out.print("INT. GRAPH AT THE END OF THE METHOD:");
		    System.out.println(int_pig);
		    System.out.print("EXT. GRAPH AT THE END OF THE METHOD:");
		    System.out.println(ext_pig);
		    System.out.print("INT. GRAPH AT THE END OF THE METHOD" +
				     " + INTER-THREAD ANALYSIS:");
		    System.out.println(pig_inter_thread);
		}
	    }

	if(hmethod == null){
	    System.out.println("Oops!" + method.declClass + "." +
			       method.name + " not found");
	    return;
	}

	if(nbmm == 0)
	    System.out.println("Oops!" + method.declClass + "." +
			       method.name +
			       " seems not to be called at all");
	else
	    System.out.println(nbmm + " ANALYZED META-METHOD(S)");

    }

    // receives a "class.name" string and cut it into pieces, separating
    // the name of the class from the name of the method.
    private static Method getMethodName(String str){
	Method method = new Method();
	int point_pos = str.lastIndexOf('.');
	method.name           = str.substring(point_pos+1);
	if(point_pos == -1) return method;
	method.declClass      = str.substring(0,point_pos);
	return method;
    }

    // process the command line options; returns the starting index of
    // the non-option arguments
    private static int get_options(String[] argv){
	int c, c2;
	String arg;
	LongOpt[] longopts = new LongOpt[]{
	    new LongOpt("meta",      LongOpt.NO_ARGUMENT,       null, 'm'),
	    new LongOpt("smartcg",   LongOpt.NO_ARGUMENT,       null, 's'),
	    new LongOpt("showch",    LongOpt.NO_ARGUMENT,       null, 'c'),
	    new LongOpt("ccs",       LongOpt.REQUIRED_ARGUMENT, null, 5),
	    new LongOpt("ts",        LongOpt.NO_ARGUMENT,       null, 6),
	    new LongOpt("wts",       LongOpt.NO_ARGUMENT,       null, 7),
	    new LongOpt("ls",        LongOpt.NO_ARGUMENT,       null, 8),
	    new LongOpt("showcg",    LongOpt.NO_ARGUMENT,       null, 9),
	    new LongOpt("showsplit", LongOpt.NO_ARGUMENT,       null, 10),
	    new LongOpt("details",   LongOpt.NO_ARGUMENT,       null, 11),
	    new LongOpt("holestats", LongOpt.NO_ARGUMENT,       null, 13),
	    new LongOpt("mamaps",    LongOpt.REQUIRED_ARGUMENT, null, 14),
	    new LongOpt("noit",      LongOpt.NO_ARGUMENT,       null, 15),
	    new LongOpt("inline",    LongOpt.NO_ARGUMENT,       null, 16),
	    new LongOpt("sat",       LongOpt.REQUIRED_ARGUMENT, null, 17),
	    new LongOpt("notg",      LongOpt.NO_ARGUMENT,       null, 18)};

	Getopt g = new Getopt("PAMain", argv, "l:mscoa:i", longopts);

	while((c = g.getopt()) != -1)
	    switch(c){
	    case 'm':
		SMART_CALL_GRAPH = false;
		METAMETHODS = true;
		break;
	    case 's':
		METAMETHODS = false;
		SMART_CALL_GRAPH = true;
		break;
	    case 'c':
		SHOW_CH = true;
		break;
	    case 'a':
		DO_ANALYSIS = true;
		mm_to_analyze.add(getMethodName(g.getOptarg()));
		break;
	    case 'i':
		DO_INTERACTIVE_ANALYSIS = true;
		break;
	    case 'l':
		LOAD_MCG = true;
		SMART_CALL_GRAPH = false;
		METAMETHODS = false;
		break;
	    case 5:
		arg = g.getOptarg();
		PointerAnalysis.CALL_CONTEXT_SENSITIVE = true;
		PointerAnalysis.MAX_SPEC_DEPTH = new Integer(arg).intValue();
		break;
	    case 6:
		PointerAnalysis.THREAD_SENSITIVE = true;
		PointerAnalysis.WEAKLY_THREAD_SENSITIVE = false;
		break;
	    case 7:
		PointerAnalysis.WEAKLY_THREAD_SENSITIVE = true;
		PointerAnalysis.THREAD_SENSITIVE = false;
		break;
	    case 8:
		PointerAnalysis.LOOP_SENSITIVE = true;
		break;
	    case 9:
		SHOW_CG = true;
		break;
	    case 10:
		SHOW_SPLIT = true;
		break;
	    case 11:
		SHOW_DETAILS = true;
		break;
	    case 13:
		HOLE_STATS = true;
		break;
	    case 14:
		MA_MAPS = true;
		MA_MAPS_OUTPUT_FILE = new String(g.getOptarg());
		break;
	    case 15:
		USE_INTER_THREAD = false;
		break;
	    case 16:
		MAInfo.DO_METHOD_INLINING = true;
		break;
	    case 17:
		DO_SAT = true;
		SAT_FILE = new String(g.getOptarg());
		break;
	    case 18:
		MAInfo.NO_TG = true;
		break;
	    }

	return g.getOptind();
    }

    private static void print_options(){
	if(METAMETHODS && SMART_CALL_GRAPH){
	    System.out.println("Call Graph Type Ambiguity");
	    System.exit(1);
	}
	System.out.print("Execution options:");

	if(LOAD_MCG)
	    System.out.print(" Unsupported yet! LOAD_MCG");
	if(METAMETHODS)
	    System.out.print(" METAMETHODS");
	if(SMART_CALL_GRAPH)
	    System.out.print(" SMART_CALL_GRAPH");
	if(!(METAMETHODS || SMART_CALL_GRAPH))
	    System.out.print(" DumbCallGraph");

	if(PointerAnalysis.CALL_CONTEXT_SENSITIVE)
	    System.out.print(" CALL_CONTEXT_SENSITIVE=" +
			     PointerAnalysis.MAX_SPEC_DEPTH);
	
	if(PointerAnalysis.THREAD_SENSITIVE)
	    System.out.print(" THREAD SENSITIVE");
	if(PointerAnalysis.WEAKLY_THREAD_SENSITIVE)
	    System.out.print(" WEAKLY_THREAD_SENSITIVE");

	if(PointerAnalysis.LOOP_SENSITIVE)
	    System.out.println(" LOOP_SENSITIVE");

	if(SHOW_CH)
	    System.out.print(" SHOW_CH");

	if(SHOW_CG)
	    System.out.print(" SHOW_CG");

	if(SHOW_DETAILS)
	    System.out.print(" SHOW_DETAILS");

	if(HOLE_STATS)
	    System.out.print(" HOLE_STATS");

	if(DO_ANALYSIS){
	    if(mm_to_analyze.size() == 1){
		Method method = (Method) (mm_to_analyze.iterator().next());
		System.out.println(" DO_ANALYSIS (" +
				   method.declClass + "." + method.name);
	    }
	    else{
		System.out.println("\nDO_ANALYSIS");
		for(Iterator it = mm_to_analyze.iterator(); it.hasNext();){
		    Method method = (Method) it.next();
		    System.out.println("  " + method.declClass + "." +
				       method.name);
		}
		System.out.println("}");
	    }
	}

	if(DO_INTERACTIVE_ANALYSIS)
	    System.out.print(" DO_INTERACTIVE_ANALYSIS");

	if(MA_MAPS){
	    System.out.print(" MA_MAPS (");
	    if(MAInfo.DO_METHOD_INLINING)
		System.out.print("inline; ");
	    System.out.print(MA_MAPS_OUTPUT_FILE + ")");
	}

	if(USE_INTER_THREAD)
	    System.out.print(" USE_INTER_THREAD");
	else
	    System.out.print(" (just inter proc)");

	if(DO_SAT)
	    System.out.print(" DO_SAT (" + SAT_FILE + ")");

	if(MAInfo.NO_TG)
	    System.out.println(" NO_TG");

	System.out.println();
    }

    // Generates the memory allocation policies.
    private static void ma_maps(PointerAnalysis pa, HMethod hroot){
	MetaCallGraph mcg = pa.getMetaCallGraph();
	MetaMethod mroot = new MetaMethod(hroot, true);

	// analyze just the method tree rooted in the main method, i.e.
	// just the user program, not the other methods called by the
	// JVM before "main".
	Set mms = new HashSet();
	Set roots = new HashSet(mcg.getRunMetaMethods());
	roots.add(mroot);

	for(Iterator it = roots.iterator(); it.hasNext(); ){
	    MetaMethod mm = (MetaMethod) it.next();
	    mms.add(mm);
	    mms.addAll(mcg.getTransCallees(mm));
	}

	if(DEBUG){
	    System.out.println("ROOT META-METHOD: " + mroot);
	    System.out.println("RELEVANT META-METHODs:{");
	    for(Iterator it = mms.iterator(); it.hasNext(); ){
		MetaMethod mm = (MetaMethod) it.next();
		if(pa.analyzable(mm.getHMethod()))
		    System.out.println("  " + mm);
	    }
	    System.out.println("}");
	}

	// this should analyze everything
	if(USE_INTER_THREAD) {
	    pa.getIntParIntGraph(mroot);
	    pa.getExtParIntGraph(mroot);
	    pa.threadInteraction(mroot);

	    for(Iterator it = mcg.getAllMetaMethods().iterator();
		it.hasNext(); ) {
		MetaMethod mm = (MetaMethod) it.next();
		HMethod hm = mm.getHMethod();

		if(java.lang.reflect.Modifier.isNative(hm.getModifiers()))
		    continue;
		pa.threadInteraction(mm); 
	    }
	}
	else {
	    Set tops = new HashSet(mcg.getRunMetaMethods());
	    tops.add(mroot);
	    //if(DEBUG) { ////// TODO: remove
		System.out.println("TOP METHODS:");
		for(Iterator it = tops.iterator(); it.hasNext(); )
		    System.out.println(it.next());
		//} 
	    for(Iterator it = tops.iterator(); it.hasNext(); )
		pa.getIntParIntGraph( (MetaMethod) it.next());
	}
	
	//B/
	System.out.println("PRE-ANALYSIS + ANALYSIS TIME : " +
			   (System.currentTimeMillis() - g_tstart) + "ms");

	g_tstart = System.currentTimeMillis(); //B/

	MAInfo mainfo = new MAInfo(pa, hcf, mms, USE_INTER_THREAD);

	//B/
	System.out.println("GENERATION OF MA INFO TIME  : " +
			   (System.currentTimeMillis() - g_tstart) + "ms");

	if(SHOW_DETAILS){
	    // show the allocation policies
	    System.out.println();
	    mainfo.print();
	    System.out.println("===================================");
	}

	g_tstart = System.currentTimeMillis();

	System.out.print("Dumping the code factory + maps into the file " +
			 MA_MAPS_OUTPUT_FILE + " ... ");
	try{
	    ObjectOutputStream oos = new ObjectOutputStream
		(new FileOutputStream(MA_MAPS_OUTPUT_FILE));
	    mainfo.prepareForSerialization();
	    // write the CachingCodeFactory on the disk
	    oos.writeObject(hcf);
	    // write the Linker on the disk
	    oos.writeObject(linker);
	    oos.close();
	}
	catch(IOException e){
	    System.err.println(e);
	}


	System.out.println((System.currentTimeMillis() - g_tstart) + "ms");
    }

    // extract the method roots from the set of all the roots
    // (methods and classes)
    private static Set extract_method_roots(Set roots){
	Set mroots = new HashSet();
	for(Iterator it = roots.iterator(); it.hasNext(); ){
	    Object obj = it.next();
	    if(obj instanceof HMethod)
		mroots.add(obj);
	}
	return mroots;
    }


    // One of my new ideas: while doing the intra-procedural analysis of
    // method M, instead of keeping a graph in each basic block, let's
    // keep one just for "join" points or for BB that contain a CALL.
    // The goal of this statistic is to see how many BBs fall in this category
    private static void join_stats(LBBConverter lbbconv, MetaCallGraph mcg) {

	System.out.println("\nPOTENTIAL MEMORY REDUCTION STATISTICS:\n");

	for(Iterator it = mcg.getAllMetaMethods().iterator(); it.hasNext();){
	    MetaMethod mm = (MetaMethod) it.next();
	    HMethod hm = mm.getHMethod();
	    if(Modifier.isNative(hm.getModifiers()))
		continue;
	    
	    LightBasicBlock.Factory lbbf = lbbconv.convert2lbb(hm);
	    LightBasicBlock lbbs[] = lbbf.getAllBBs();

	    int nb_lbbs = lbbs.length;
	    int nb_calls = 0; // nb. of blocks finished in CALLs
	    int nb_joins = 0; // nb. of blocks that are join points
	    int nb_total = 0; // nb. of blocks that are either CALLs or joins

	    for(int i = 0; i < nb_lbbs; i++){
		LightBasicBlock lbb = lbbs[i];
		boolean is_join = (lbb.getPrevLBBs().length > 1);

		HCodeElement elems[] = lbb.getElements();
		boolean is_call = (elems[elems.length - 1] instanceof CALL);

		if(is_call) nb_calls++;
		if(is_join) nb_joins++;
		if(is_call || is_join) nb_total++;
	    }

	    double pct = (float)(100 * nb_total)/(double)nb_lbbs;
	    HClass hc = hm.getDeclaringClass();
	    String method_name = hc.getName() + "." + hm.getName();

	    System.out.println(method_name + " \t" +
			       nb_lbbs  + " LBBs  " +
			       nb_total + " Full (" +
			       Debug.doubleRep(pct, 5, 2) + "%)  " +
			       nb_joins + " Joins " +
			       nb_calls + " Calls ");
	}

    }


    private static void do_sat() {
	System.out.println(" Generating the \"start()\" and \"join()\" maps");
	System.out.println(" DUMMY VERSION");
	
	sat_starts = new HashSet();
	sat_joins  = new HashSet();
	
	for(Iterator it = mcg.getAllMetaMethods().iterator(); it.hasNext(); )
	    do_sat_analyze_mmethod((MetaMethod) it.next());

	System.out.println("Dumping the results into " + SAT_FILE);

	try{
	    ObjectOutputStream oos = new ObjectOutputStream
		(new FileOutputStream(SAT_FILE));

	    // write the CachingCodeFactory on the disk
	    System.out.print(" Dumping the code factory ... ");
	    oos.writeObject(hcf);
	    System.out.println("Done");

	    // write the Linker on the disk
	    System.out.print(" Dumping the linker ... ");
	    oos.writeObject(linker);
	    System.out.println("Done");

	    // write the set of .start() sites that need to be inlined
	    System.out.print(" Dumping the set of .start() ... ");
	    oos.writeObject(sat_starts);
	    System.out.println("Done");

	    // write the set of .join() sites that need to be modified
	    System.out.print(" Dumping the set of .join() ... ");
	    oos.writeObject(sat_joins);
	    System.out.println("Done");

	    oos.close();
	}
	catch(IOException e){
	    System.err.println(e);
	}
    }

    private static Set sat_starts = null;
    private static Set sat_joins  = null;

    private static QuadVisitor sat_qv = new QuadVisitor() {
	    public void visit(Quad q) { // do nothing
	    }
	    public void visit(CALL q) {
		HMethod method = q.method();
		if(isEqual(method, "java.lang.Thread", "start")) {
		    System.out.println("START: " + Debug.code2str(q));
		    sat_starts.add(q);
		}
		if(isEqual(method, "java.lang.Thread", "join") &&
		   (q.paramsLength() == 1)) {
		    System.out.println("JOIN: " + Debug.code2str(q));
		    sat_joins.add(q);
		}
	    }
	};
    
    // tests whether the method hm is the same thing as
    // class_name.method_name
    private static boolean isEqual(HMethod hm, String class_name,
				   String method_name) {
	HClass hclass = hm.getDeclaringClass();
	return(hm.getName().equals(method_name) &&
	       hclass.getName().equals(class_name));
    }

    private static void do_sat_analyze_mmethod(MetaMethod mm) {
	HMethod hm = mm.getHMethod();
	HCode hcode = hcf.convert(hm);
	if(hcode == null) return;
	for(Iterator it = hcode.getElementsI(); it.hasNext(); ) {
	    Quad q = (Quad) it.next();
	    q.accept(sat_qv);
	}
    }

}

