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
import java.util.Map;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.ObjectOutputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.FileReader;
import gnu.getopt.Getopt;
import gnu.getopt.LongOpt;

import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HCode;
import harpoon.ClassFile.HMethod;
import harpoon.ClassFile.HCodeFactory;
import harpoon.ClassFile.HCodeElement;
import harpoon.ClassFile.CachingCodeFactory;
import harpoon.ClassFile.Linker;
import harpoon.ClassFile.Relinker;
import harpoon.ClassFile.Loader;

import harpoon.IR.Quads.Quad;
import harpoon.IR.Quads.QuadVisitor;
import harpoon.IR.Quads.QuadWithTry;

import harpoon.Analysis.Quads.QuadClassHierarchy;
import harpoon.Analysis.Quads.CallGraph;
import harpoon.Analysis.Quads.CallGraphImpl;
import harpoon.Analysis.AllCallers;
import harpoon.Analysis.ClassHierarchy;
import harpoon.Analysis.BasicBlock;

import harpoon.Analysis.PointerAnalysis.PointerAnalysis;
import harpoon.Analysis.PointerAnalysis.PANode;
import harpoon.Analysis.PointerAnalysis.ParIntGraph;
import harpoon.Util.DataStructs.Relation;
import harpoon.Analysis.PointerAnalysis.MAInfo;
import harpoon.Analysis.PointerAnalysis.SyncElimination;
import harpoon.Analysis.PointerAnalysis.InstrumentSyncOps;
import harpoon.Analysis.PointerAnalysis.AllocationNumbering;

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
import harpoon.Util.LightBasicBlocks.CachingSCCLBBFactory;
import harpoon.Util.Graphs.SCComponent;
import harpoon.Util.Graphs.SCCTopSortedGraph;
import harpoon.Util.WorkSet;
import harpoon.Util.ParseUtil;

import harpoon.Util.TypeInference.TypeInference;
import harpoon.Util.TypeInference.ExactTemp;

import harpoon.Util.Util;

import harpoon.Analysis.PointerAnalysis.Debug;
import harpoon.Analysis.MetaMethods.SmartCallGraph;

import harpoon.IR.Quads.CALL;

import harpoon.IR.Jasmin.Jasmin;

import harpoon.Analysis.Realtime.Realtime;

import harpoon.Analysis.Quads.QuadCounter;

/**
 * <code>PAMain</code> is a simple Pointer Analysis top-level class.
 * It is designed for testing and evaluation only.
 * 
 * @author  Alexandru SALCIANU <salcianu@retezat.lcs.mit.edu>
 * @version $Id: PAMain.java,v 1.1.2.100 2001-04-24 15:03:06 salcianu Exp $
 */
public abstract class PAMain {

    // use the real meta call graph
    private static boolean METAMETHODS = false;
    // use FakeMetaCallGraph(SmartCallGraph)
    private static boolean SMART_CALL_GRAPH = false;
    // debug the class hierarchy
    private static boolean SHOW_CH = false;

    // show the call graph
    private static boolean SHOW_CG = false;
    // show the split relation HMethod -> MetaMethods
    private static boolean SHOW_SPLIT = false;
    // show some details/statistics about the analysis
    private static boolean SHOW_DETAILS = false;

    // make the program to analyse some method;
    private static boolean DO_ANALYSIS = false;
    // turns on the interactive analysis
    private static boolean DO_INTERACTIVE_ANALYSIS = false;
    // turn on elaborated details during the interactive analysis
    private static boolean INTERACTIVE_ANALYSIS_DETAILS = false;

    // force the creation of the memory allocation info.
    private static boolean MA_MAPS = false;
    // the name of the file into which the memory allocation policies
    // will be serialized
    private static String MA_MAPS_OUTPUT_FILE = null;

    private static boolean CHECK_NO_CALLEES = false;

    // Displays some help messages.
    private static boolean DEBUG = false;

    private static boolean DO_SAT = false;
    private static String SAT_FILE = null;

    private static boolean ELIM_SYNCOPS = false;
    private static boolean INST_SYNCOPS = false;

    private static boolean DUMP_JAVA = false;

    //private static boolean ANALYZE_ALL_ROOTS = false;
    //private static boolean SYNC_ELIM_ALL_ROOTS = false;
    
    private static boolean COMPILE = false;
    
    // Load the preanalysis results from PRE_ANALYSIS_IN_FILE
    private static boolean LOAD_PRE_ANALYSIS = false;
    private static String  PRE_ANALYSIS_IN_FILE = null;

    // Save the preanalysis results into PRE_ANALYSIS_OUT_FILE
    private static boolean SAVE_PRE_ANALYSIS = false;
    private static String  PRE_ANALYSIS_OUT_FILE = null;


    // Load the preanalysis results from PRE_ANALYSIS_IN_FILE
    private static boolean LOAD_ANALYSIS = false;
    private static String  ANALYSIS_IN_FILE = null;

    // Save the preanalysis results into PRE_ANALYSIS_OUT_FILE
    private static boolean SAVE_ANALYSIS = false;
    private static String  ANALYSIS_OUT_FILE = null;

    private static boolean RTJ_REMOVE_CHECKS = false;
    private static boolean RTJ_SUPPORT = false;

    // the name of the file that contains additional roots
    private static String rootSetFilename = null;

    private static PointerAnalysis pa = null;
    // the main method
    private static HMethod hroot = null;

    private static AllocationNumbering an=null;
    private static long[] profile=null;

    // list to maintain the methods to be analyzed
    private static List mm_to_analyze = new LinkedList();
    
    private static class Method implements java.io.Serializable {
	String name  = null;
	String declClass = null;
	public boolean equals(Object o) {
	    if((o == null) || !(o instanceof Method))
		return false;
	    Method m2 = (Method) o;
	    return 
		str_equals(name, m2.name) &&
		str_equals(declClass, m2.declClass);
	}
	public static boolean str_equals(String s1, String s2) {
	    return (s1 == null) ? (s2 == null) : s1.equals(s2); 
	}
	public String toString() {
	    return declClass + "." + name;
	}
    }

    private static Method root_method = new Method();


    private static Linker linker = new Relinker(Loader.systemLinker);
    private static HCodeFactory hcf = null;
    private static MetaCallGraph  mcg = null;
    private static MetaAllCallers mac = null;
    private static Relation split_rel = null;
    private static CachingBBConverter bbconv = null;
    private static LBBConverter lbbconv = null;
    private static CachingSCCLBBFactory caching_scc_lbb_factory = null;
    // the class hierarchy of the analyzed program
    private static ClassHierarchy ch = null;


    // The set of method roots, i.e. those methods that represents
    // entry points into the call graph:
    //  1. the methods that might be called by the runtime system
    //  2. the static initializers of all the instantiated classes and
    //  3. the "main" method of the analyzed program.
    private static Set mroots = null;

    // The set of classes and methods that are instantiated/called by
    // the current implementation of the runtime.
    // private static Set runtime_callable = null;
    private static Set program_roots = null;

    // global variable used for timing measurements
    private static long g_tstart = 0L;

    // options for the MAInfo module.
    private static MAInfo.MAInfoOptions mainfo_opts = 
	new MAInfo.MAInfoOptions();

    public static void main(String[] params) throws IOException {
	int optind = get_options(params);
	int nbargs = params.length - optind;
	if(nbargs < 1) {
	    show_help();
	    System.exit(1);
	}
	print_options();

	if(mainfo_opts.USE_INTER_THREAD)
	    PointerAnalysis.RECORD_ACTIONS = true;

	get_root_method(params[optind]);

	if(RTJ_SUPPORT)
	    Realtime.setupObject(linker);

	if(LOAD_ANALYSIS) load_analysis();
	else {
	    if(LOAD_PRE_ANALYSIS)
		load_pre_analysis();
	    else {
		pre_analysis();
		if(SAVE_PRE_ANALYSIS)
		    save_pre_analysis();
	    }
	    pa = new PointerAnalysis(mcg, mac,
				     caching_scc_lbb_factory, linker);
	}

	if(RTJ_REMOVE_CHECKS) {
	    System.out.println( can_remove_all_checks() ?
				"can remove all checks!" :
				"cannot remove all checks!" );
	    return;
	}

	if(CHECK_NO_CALLEES)
	    check_no_callees();

	if(DO_ANALYSIS)
	    do_analysis();

	if(DO_INTERACTIVE_ANALYSIS)
	    do_interactive_analysis();
    
	if(MA_MAPS)
	    ma_maps();

	/*
	if(DO_SAT)
	    do_sat();
	*/

	if(SHOW_DETAILS)
	    pa.print_stats();

	/*
        if(ELIM_SYNCOPS)
            do_elim_syncops();
	*/

	if(SAVE_ANALYSIS)
	    save_analysis();

	/*
	if(DUMP_JAVA)
	    dump_java(get_classes(pa.getMetaCallGraph().getAllMetaMethods()));
	*/

	if(COMPILE) {
	    System.out.println("\n\n\tCOMPILE!\n");

	    g_tstart = System.currentTimeMillis();
	    // the transformation associated with the new strategy has
	    // already been applied while performing the pre-analysis
	    // (so that the analysis can see the modified/added code)
	    SAMain.USE_OLD_CLINIT_STRATEGY = !USE_OLD_STYLE;
	    SAMain.linker = linker;
	    SAMain.hcf    = hcf;
	    SAMain.className = root_method.declClass; // params[optind];
	    SAMain.rootSetFilename = rootSetFilename;

	    SAMain.do_it();

	    System.out.println("Backend time: " +
			       (time() - g_tstart) + "ms");
	}

	if(RTJ_SUPPORT)
	    Realtime.printStats();
    }

    // For debug purposes,
    // we sometimes use the old static initializers strategy
    private static final boolean USE_OLD_STYLE = false;
    
    // Constructs some data structures used by the analysis: the code factory
    // providing the code of the methods, the class hierarchy, call graph etc.
    private static void pre_analysis() {
	g_tstart = System.currentTimeMillis();

	//We might have loaded in a code factory w/o a preanalysis.
	if (hcf==null) {
	    if(USE_OLD_STYLE) {
		System.out.println("Use old style class initializers!");
		hcf = harpoon.IR.Quads.QuadNoSSA.codeFactory();
		construct_class_hierarchy();
	    }
	    else {
		System.out.println("Use new style class initializers!");
		hcf = harpoon.IR.Quads.QuadWithTry.codeFactory();
		construct_class_hierarchy();
		
		String resource =
		    "harpoon/Backend/Runtime1/init-safe.properties";
		hcf = new harpoon.Analysis.Quads.InitializerTransform
		    (hcf, ch, linker, resource).codeFactory();
		
		hcf = harpoon.IR.Quads.QuadNoSSA.codeFactory(hcf);
		construct_class_hierarchy();
	    }
	}
	else
	    construct_class_hierarchy();
	
	hcf = new CachingCodeFactory(hcf, true);
	ir_generation();
	
	bbconv = new CachingBBConverter(hcf);
	lbbconv = new CachingLBBConverter(bbconv);
	lbb_generation();

	caching_scc_lbb_factory = new CachingSCCLBBFactory(lbbconv);
	scc_lbb_generation();

	construct_mroots();
	construct_meta_call_graph();
	construct_split_relation();
	
	System.out.println("Total pre-analysis time : " +
			   (time() - g_tstart) + "ms");
    }


    // Generates the intermediate representation for the entire program.
    // As hcf is a CachingCodeFactory, by generating all the IR here, we're
    // able to time it accurately.
    private static void ir_generation() {
	long istart = time();
	System.out.print("IR generation (" + hcf.getCodeName() + ") ... ");
	for(Iterator it = ch.callableMethods().iterator(); it.hasNext(); )
	    hcf.convert((HMethod) it.next());
	System.out.println((time() - istart) + " ms");
    }

    // Generates the light basic block representation for the entire program.
    // As lbbconv caches its data, by doing all the conversion here, we're
    // able to time it accurately.
    private static void lbb_generation() {
	long istart = time();
	System.out.print("Light Basic Blocks generation ... ");
	for(Iterator it = ch.callableMethods().iterator(); it.hasNext(); ) {
	    HMethod hm  = (HMethod) it.next();
	    HCode hcode = hcf.convert(hm);
	    if(hcode != null)
		lbbconv.convert2lbb(hm);
	}
	System.out.println((time() - istart) + " ms");
    }

    // Generates the SCC light basic block representation for the
    // entire program (for each method we compute the component graph
    // of its (light) basic blocks).  As caching_scc_lbb_factory
    // caches its data, by doing all the conversion here, we're able
    // to time it accurately.
    private static void scc_lbb_generation() {
	long istart = time();
	System.out.print("SCC Light Basic Blocks generation ... ");
	for(Iterator it = ch.callableMethods().iterator(); it.hasNext(); ) {
	    HMethod hm  = (HMethod) it.next();
	    HCode hcode = hcf.convert(hm);
	    if(hcode != null)
		caching_scc_lbb_factory.computeSCCLBB(hm);
	}
	System.out.println((time() - istart) + " ms");
    }


    

    private static final int MAX_CALLEES = 100;
    private static void check_no_callees() {
	long nb_nvirtual_calls  = 0L;
	long nb_virtual_calls = 0L;
	long vcalls[] = new long[MAX_CALLEES];
	long nb_total_callees = 0L;
	for(int i = 0; i < MAX_CALLEES; i++)
	    vcalls[i] = 0L;

	for(Iterator it = mcg.getAllMetaMethods().iterator(); it.hasNext(); ) {
	    MetaMethod mm = (MetaMethod) it.next();
	    HMethod hm = mm.getHMethod();
	    Set calls = get_calls(hm);
	    for(Iterator it_calls = calls.iterator(); it_calls.hasNext(); ) {
		CALL call = (CALL) it_calls.next();
		MetaMethod[] callees = mcg.getCallees(mm, call);
		int nb_callees = callees.length;
		if(callees.length == 0)
		    System.out.println("EMPTY CALL " + Debug.code2str(call) +
				       "\n  in " + mm);
		nb_total_callees += nb_callees;
		if(call.isVirtual()) {
		    nb_virtual_calls++;
		    if(nb_callees >= MAX_CALLEES)
			nb_callees = MAX_CALLEES-1;
		    vcalls[nb_callees]++;
		}
		else
		    nb_nvirtual_calls++;
	    }
	}

	long nb_calls = nb_virtual_calls + nb_nvirtual_calls;
	System.out.println("\nCALL SITES STATISTICS:\n");
	System.out.println("Total calls       = " + nb_calls);
	System.out.println("Non-virtual calls = " + nb_nvirtual_calls + "\t" +
			   Debug.get_perct(nb_nvirtual_calls, nb_calls));
	System.out.println("Virtual calls     = " + nb_virtual_calls + "\t" +
			   Debug.get_perct(nb_virtual_calls, nb_calls));
	for(int i = 0; i < MAX_CALLEES; i++)
	    if(vcalls[i] > 0)
		System.out.println("  " + i + " callee(s) = " + vcalls[i] +
		      "\t" + Debug.get_perct(vcalls[i], nb_virtual_calls));
	System.out.println("Average callees/call site = " + 
		Debug.doubleRep(((double) nb_total_callees) / nb_calls, 2));
	System.out.println("-----------------------------------------------");
    }

    private static Set get_calls(HMethod hm) {
	Set result = new HashSet();
	HCode hcode = hcf.convert(hm);
	if(hcode == null) return result;
	for(Iterator it = hcode.getElementsI(); it.hasNext(); ) {
	    Quad quad = (Quad) it.next();
	    if(quad instanceof CALL)
		result.add(quad);
	}
	return result;
    }

    // load the results of the preanalysis from the disk
    private static void load_pre_analysis() {
	try{
	    System.out.print("Loading preanalysis results from " + 
			     PRE_ANALYSIS_IN_FILE + " ... ");
	    g_tstart = time();
	    load_pre_analysis2();
	    System.out.println((time() - g_tstart) + "ms");
	} catch(Exception e){
	    System.err.println("\nError while loading pre-analysis results!");
	    System.err.println(e);
	    System.exit(1);
	}
    }
    
    // do the real job behind load_pre_analysis
    private static void load_pre_analysis2()
	throws IOException, ClassNotFoundException {
	ObjectInputStream ois = new ObjectInputStream
	    (new FileInputStream(PRE_ANALYSIS_IN_FILE));
	Method m2 = (Method) ois.readObject();
	if((m2 == null) || !m2.equals(root_method)) {
	    System.err.println("\nDifferent root method: " + m2 + "!");
	    System.exit(1);
	}
	linker    = (Linker) ois.readObject();
	hcf       = (CachingCodeFactory) ois.readObject();
	bbconv    = (CachingBBConverter) ois.readObject();
	caching_scc_lbb_factory = (CachingSCCLBBFactory) ois.readObject();
	ch        = (ClassHierarchy) ois.readObject();
	mroots    = (Set) ois.readObject();
	mcg       = (MetaCallGraph) ois.readObject();
	mac       = (MetaAllCallers) ois.readObject();
	split_rel = (Relation) ois.readObject();
	ois.close();
    }


    // save the results of the preanalysis for future use
    private static void save_pre_analysis() {
	try{
	    System.out.print("Saving preanalysis results into " + 
			     PRE_ANALYSIS_OUT_FILE + " ... ");
	    g_tstart = time();
	    save_pre_analysis2();
	    System.out.println((time() - g_tstart) + "ms");
	} catch(IOException e){
	    System.err.println("\nError while saving pre-analysis results!");
	    System.err.println(e);
	    System.exit(1);
	}
    }

    // do the real job  behind save_pre_analysis
    private static void save_pre_analysis2() throws IOException {
	ObjectOutputStream oos = new ObjectOutputStream
	    (new FileOutputStream(PRE_ANALYSIS_OUT_FILE));
	oos.writeObject(root_method);
	oos.writeObject(linker);
	oos.writeObject(hcf);
	oos.writeObject(bbconv);
	oos.writeObject(caching_scc_lbb_factory);
	oos.writeObject(ch);
	oos.writeObject(mroots);
	oos.writeObject(mcg);
	oos.writeObject(mac);
	oos.writeObject(split_rel);
	oos.flush();
	oos.close();
    }


    private static void load_analysis() {
	long start = time();
	System.out.print("Loading the PA from " + ANALYSIS_IN_FILE + 
			  " ... ");

	try {
	    ObjectInputStream ois = new ObjectInputStream
		(new FileInputStream(ANALYSIS_IN_FILE));
	    root_method = (Method) ois.readObject();
	    linker      = (Linker) ois.readObject();
	    hcf         = (CachingCodeFactory) ois.readObject();
	    bbconv      = (CachingBBConverter) ois.readObject();
	    // lbbconv     = (LBBConverter) ois.readObject();
	    caching_scc_lbb_factory = (CachingSCCLBBFactory) ois.readObject();
	    ch          = (ClassHierarchy) ois.readObject();
	    mroots      = (Set) ois.readObject();
	    mcg         = (MetaCallGraph) ois.readObject();
	    mac         = (MetaAllCallers) ois.readObject();
	    split_rel   = (Relation) ois.readObject();
	    pa          = (PointerAnalysis) ois.readObject();
	    ois.close();
	} catch(Exception e) {
	    System.err.println("\nError while loading the PA objects!");
	    System.err.println(e);
	    System.exit(1);
	}

	System.out.println((time() - start) + "ms");
    }


    private static void save_analysis() {
	long start = time();
	System.out.print("Saving the PA into " + ANALYSIS_OUT_FILE + 
			  " ... ");

	try {
	    ObjectOutputStream oos = new ObjectOutputStream
		(new FileOutputStream(ANALYSIS_OUT_FILE));
	    oos.writeObject(root_method);
	    oos.writeObject(linker);
	    oos.writeObject(hcf);
	    oos.writeObject(bbconv);
	    oos.writeObject(caching_scc_lbb_factory);
	    // oos.writeObject(lbbconv);
	    oos.writeObject(ch);
	    oos.writeObject(mroots);
	    oos.writeObject(mcg);
	    oos.writeObject(mac);
	    oos.writeObject(split_rel);
	    oos.writeObject(pa);
	    oos.flush();
	    oos.close();
	} catch(Exception e) {
	    System.err.println("\nError while saving the PA objects!");
	    System.err.println(e);
	    System.exit(1);
	}

	System.out.println((time() - start) + "ms");
    }


    // Finds the root method: the "main" method of "root_class".
    private static void get_root_method(String root_class) {
	root_method.name = "main";
	root_method.declClass = root_class;

	HClass hclass = linker.forName(root_method.declClass);
	Util.assert(hclass != null, "Class " + root_class + " not found!");

	HMethod[] hm  = hclass.getDeclaredMethods();
	// search for the main method
	hroot = null;
	for(int i = 0; i < hm.length; i++)
	    if(hm[i].getName().equals(root_method.name)) {
		Util.assert(hroot == null, "Ambiguous root method!");
		hroot = hm[i];
	    }
	
	Util.assert(hroot != null, "Root method \"" + root_class + "." +
		    root_method.name + "\" not found!");
	System.out.println("Root method: " + root_method.declClass + "." +
			   root_method.name);
    }


    private static void display_method(Method method)
	throws harpoon.ClassFile.NoSuchClassException {

	HClass hclass = linker.forName(method.declClass);
	HMethod[] hm  = hclass.getDeclaredMethods();
	int nbmm = 0;

	HMethod hmethod = null;		
	for(int i = 0; i < hm.length; i++)
	    if(hm[i].getName().equals(method.name)){
		hmethod = hm[i];

		// look for all the meta-methods originating into this method
		// and do all the analysis stuff on them.
		for(Iterator it = split_rel.getValues(hmethod).iterator();
		    it.hasNext();){
		    nbmm++;
		    MetaMethod mm = (MetaMethod) it.next();
		    System.out.println("HMETHOD " + hmethod
				       + " ->\n META-METHOD " + mm);
		    ParIntGraph int_pig = pa.getIntParIntGraph(mm);
		    ParIntGraph ext_pig = pa.getExtParIntGraph(mm);
		    PANode[] nodes = pa.getParamNodes(mm);
		    System.out.println("META-METHOD " + mm);
		    System.out.print("POINTER PARAMETERS: ");
		    System.out.print("[ ");
		    for(int j = 0; j < nodes.length; j++)
			System.out.print(nodes[j] + " ");
		    System.out.println("]");
		    System.out.print("INT. GRAPH AT THE END OF THE METHOD:");
		    System.out.println(int_pig);
		    //System.out.print("EXT. GRAPH AT THE END OF THE METHOD:");
		    //System.out.println(ext_pig);
		    
		    if(mainfo_opts.USE_INTER_THREAD) {
			ParIntGraph pig_inter_thread =
			    pa.getIntThreadInteraction(mm);
			System.out.println("\n\n");
			System.out.print("INT. GRAPH AT THE END OF THE METHOD"
					 + " + INTER-THREAD ANALYSIS:");
			System.out.println(pig_inter_thread);
		    }

		    if(INTERACTIVE_ANALYSIS_DETAILS) {
			HCode hcode = hcf.convert(mm.getHMethod());
			for(Iterator itq = hcode.getElementsI();
			    itq.hasNext(); ) {

			    Quad q = (Quad) itq.next();
			    System.out.println("Graph just before <<" + 
					       Debug.code2str(q) + ">>: " +
					       pa.getPIGAtQuad(mm, q));
			}
		    }
		    
		}
	    }

	/*
	if (INST_SYNCOPS)
	    do_inst_syncops(hmethod);
	
	if (ELIM_SYNCOPS)
	    do_elim_syncops(hmethod);
	*/

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
    private static int get_options(String[] argv) {
	int c, c2;
	String arg;
	LongOpt[] longopts = new LongOpt[] {
	    new LongOpt("meta",          LongOpt.NO_ARGUMENT,       null, 'm'),
	    new LongOpt("smartcg",       LongOpt.NO_ARGUMENT,       null, 's'),
	    new LongOpt("showch",        LongOpt.NO_ARGUMENT,       null, 'c'),
	    new LongOpt("ccs",           LongOpt.REQUIRED_ARGUMENT, null, 5),
	    new LongOpt("ts",            LongOpt.NO_ARGUMENT,       null, 6),
	    new LongOpt("wts",           LongOpt.NO_ARGUMENT,       null, 7),
	    new LongOpt("ls",            LongOpt.NO_ARGUMENT,       null, 8),
	    new LongOpt("showcg",        LongOpt.NO_ARGUMENT,       null, 9),
	    new LongOpt("showsplit",     LongOpt.NO_ARGUMENT,       null, 10),
	    new LongOpt("details",       LongOpt.NO_ARGUMENT,       null, 11),
	    new LongOpt("mamaps",        LongOpt.REQUIRED_ARGUMENT, null, 14),
	    new LongOpt("wit",           LongOpt.NO_ARGUMENT,       null, 15),
	    new LongOpt("inline_depth",  LongOpt.REQUIRED_ARGUMENT, null, 16),
	    new LongOpt("sat",           LongOpt.REQUIRED_ARGUMENT, null, 17),
	    new LongOpt("notg",          LongOpt.NO_ARGUMENT,       null, 18),
	    new LongOpt("loadpre",       LongOpt.REQUIRED_ARGUMENT, null, 19),
	    new LongOpt("savepre",       LongOpt.REQUIRED_ARGUMENT, null, 20),
	    new LongOpt("syncelim",      LongOpt.NO_ARGUMENT,       null, 21),
	    new LongOpt("instsync",      LongOpt.NO_ARGUMENT,       null, 22),
	    new LongOpt("dumpjava",      LongOpt.NO_ARGUMENT,       null, 23),
	    new LongOpt("analyzeroots",  LongOpt.NO_ARGUMENT,       null, 24),
	    new LongOpt("syncelimroots", LongOpt.NO_ARGUMENT,       null, 25),
	    new LongOpt("backend",       LongOpt.REQUIRED_ARGUMENT, null,'b'),
	    new LongOpt("output",        LongOpt.REQUIRED_ARGUMENT, null,'o'),
	    new LongOpt("sa",            LongOpt.REQUIRED_ARGUMENT, null, 26),
	    new LongOpt("ta",            LongOpt.REQUIRED_ARGUMENT, null, 27),
	    new LongOpt("ns",            LongOpt.REQUIRED_ARGUMENT, null, 28),
	    new LongOpt("check_nc",      LongOpt.NO_ARGUMENT,       null, 29),
	    new LongOpt("loadpa",        LongOpt.REQUIRED_ARGUMENT, null, 30),
	    new LongOpt("savepa",        LongOpt.REQUIRED_ARGUMENT, null, 31),
	    new LongOpt("prealloc",      LongOpt.NO_ARGUMENT,       null, 32),
	    new LongOpt("rtjchecks",     LongOpt.NO_ARGUMENT,       null, 33),
	    new LongOpt("rtj",           LongOpt.REQUIRED_ARGUMENT, null, 34),
	    new LongOpt("old_inlining",  LongOpt.NO_ARGUMENT,       null, 35),
	    new LongOpt("inline_for_sa", LongOpt.REQUIRED_ARGUMENT, null, 36),
	    new LongOpt("inline_for_ta", LongOpt.REQUIRED_ARGUMENT, null, 37)
	};

	Getopt g = new Getopt("PAMain", argv, "mscor:a:iIN:P:", longopts);

	while((c = g.getopt()) != -1)
	    switch(c) {
	    case 'P':
		System.out.println("loading Profile");
		arg=g.getOptarg();
		try {
		    BufferedReader br =
			new BufferedReader(new FileReader(arg));
		    String in=br.readLine();
		    int size=Integer.parseInt(in);
		    profile=new long[size];
		    for(int i=0;i<size;i++) {
			in=br.readLine();
			profile[i]=Long.parseLong(in);
		    }
		    br.close();
		} catch (Exception e) {
		    System.out.println(e + " was thrown");
		    System.exit(1);
		}
                break;
	    case 'N':
		arg=g.getOptarg();
		System.out.println("loading "+arg);
		try {
		    ObjectInputStream ois =
			new ObjectInputStream(new FileInputStream(arg));
		    an=(AllocationNumbering)ois.readObject();
		    hcf=an.codeFactory();
		    linker=(Linker)ois.readObject();
		    ois.close();
		} catch (Exception e) {
		    System.out.println(e + " was thrown");
		    e.printStackTrace();
		    System.exit(1);
		}
                break;
	    case 'm':
		SMART_CALL_GRAPH = false;
		METAMETHODS = true;
		break;
	    case 'd':
		SMART_CALL_GRAPH = false;
		METAMETHODS = false;
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
	    case 'r':
		rootSetFilename = g.getOptarg();
		break;
	    case 'I':
		DO_INTERACTIVE_ANALYSIS = true;
		INTERACTIVE_ANALYSIS_DETAILS = true;
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
	    case 14:
		MA_MAPS = true;
		MA_MAPS_OUTPUT_FILE = new String(g.getOptarg());
		mainfo_opts.DO_STACK_ALLOCATION  = true;
		mainfo_opts.DO_THREAD_ALLOCATION = true;
		break;
	    case 26:
		int sap = Integer.parseInt(g.getOptarg());
		if(sap == 0)
		    mainfo_opts.DO_STACK_ALLOCATION = false;
		else {
		    if((sap != MAInfo.MAInfoOptions.STACK_ALLOCATE_ALWAYS) &&
		       (sap !=
			MAInfo.MAInfoOptions.STACK_ALLOCATE_NOT_IN_LOOPS)) {
			System.err.println("Unknown option for sa!");
			System.exit(1);
		    }
		    mainfo_opts.DO_STACK_ALLOCATION     = true;
		    mainfo_opts.STACK_ALLOCATION_POLICY = sap;
		}
		break;
	    case 27:
		mainfo_opts.DO_THREAD_ALLOCATION =
		    (Integer.parseInt(g.getOptarg()) == 1);
		break;
	    case 28:
		mainfo_opts.GEN_SYNC_FLAG =
		    (Integer.parseInt(g.getOptarg()) == 1);
		break;
	    case 15:
		mainfo_opts.USE_INTER_THREAD = true;
		break;
	    case 16:
		mainfo_opts.MAX_INLINING_LEVEL =
		    Integer.parseInt(g.getOptarg());
		break;
	    case 17:
		DO_SAT = true;
		SAT_FILE = new String(g.getOptarg());
		break;
	    case 18:
		MAInfo.NO_TG = true;
		break;
	    case 19:
		LOAD_PRE_ANALYSIS = true;
		PRE_ANALYSIS_IN_FILE = new String(g.getOptarg());
		break;
	    case 20:
		SAVE_PRE_ANALYSIS = true;
		PRE_ANALYSIS_OUT_FILE = new String(g.getOptarg());
		break;		
	    case 30:
		LOAD_ANALYSIS = true;
		ANALYSIS_IN_FILE = new String(g.getOptarg());
		break;
	    case 31:
		SAVE_ANALYSIS = true;
		ANALYSIS_OUT_FILE = new String(g.getOptarg());
		break;		
	    case 21:
		System.out.println("Old option syncelim -> fail");
		System.exit(1);
		//ELIM_SYNCOPS = true;
		break;
	    case 22:
		System.out.println("Old option instsync -> fail");
		System.exit(1);
		//INST_SYNCOPS = true;
		break;
	    case 23:
		DUMP_JAVA = true;
		break;
	    case 24:
		System.out.println("Old option analyzeroots -> fail");
		System.exit(1);
		//ANALYZE_ALL_ROOTS = true;
		break;
	    case 25:
		System.out.println("Old option syncelimroots -> fail");
		System.exit(1);
		//SYNC_ELIM_ALL_ROOTS = true;
		break;
	    case 29:
		CHECK_NO_CALLEES = true;
		break;
	    case 'o':
		SAMain.ASSEM_DIR = new java.io.File(g.getOptarg());
		Util.assert(SAMain.ASSEM_DIR.isDirectory(),
			    SAMain.ASSEM_DIR + " must be a directory");
		break;
	    case 'b':
		COMPILE = true;
		String backendName = g.getOptarg().toLowerCase().intern();
		if (backendName == "strongarm") {
		    SAMain.HACKED_REG_ALLOC = true;
		    SAMain.BACKEND = SAMain.STRONGARM_BACKEND;
		}
		if (backendName == "sparc")
		    SAMain.BACKEND = SAMain.SPARC_BACKEND;
		if (backendName == "mips")
		    SAMain.BACKEND = SAMain.MIPS_BACKEND;
		if (backendName == "precisec") {
		    SAMain.BACKEND = SAMain.PRECISEC_BACKEND;
		    SAMain.HACKED_REG_ALLOC = false;
		}
		break;
	    case 32:
		mainfo_opts.DO_PREALLOCATION = true;
		break;
	    case 33:
		RTJ_REMOVE_CHECKS = true;
		break;
	    case 34:
		RTJ_SUPPORT = true;
		Realtime.REALTIME_JAVA = true;
		String option = g.getOptarg().toLowerCase();
		if(option.equals("simple"))
		    Realtime.ANALYSIS_METHOD = Realtime.SIMPLE;
		else {
		    if(option.equals("all"))
			Realtime.ANALYSIS_METHOD = Realtime.ALL;
		    else {
			System.out.println("Unknown option " + option);
			System.exit(1);
		    }
		}
		break;
	    case 35:
		mainfo_opts.USE_OLD_INLINING = true;
		break;
	    case 36:
		mainfo_opts.DO_INLINING_FOR_SA =
		    (Integer.parseInt(g.getOptarg()) == 1);
		break;
	    case 37:
		mainfo_opts.DO_INLINING_FOR_TA =
		    (Integer.parseInt(g.getOptarg()) == 1);
		break;
	    }

	return g.getOptind();
    }

    private static void print_options() {
	if(METAMETHODS && SMART_CALL_GRAPH){
	    System.out.println("Call Graph Type Ambiguity");
	    System.exit(1);
	}
	System.out.println("Execution options:");

	if(rootSetFilename != null)
	    System.out.println("\tLoad extra roots from \"" +
			       rootSetFilename + "\"");

	if(LOAD_ANALYSIS)
	    System.out.println("\tLOAD_ANALYSIS from \"" + 
			       ANALYSIS_IN_FILE + "\"");
	if(SAVE_ANALYSIS)
	    System.out.println("\tSAVE_ANALYSIS in \"" +
			       ANALYSIS_OUT_FILE + "\"");


	if(LOAD_PRE_ANALYSIS)
	    System.out.println("\tLOAD_PRE_ANALYSIS from \"" + 
			       PRE_ANALYSIS_IN_FILE + "\"");
	if(SAVE_PRE_ANALYSIS)
	    System.out.println("\tSAVE_PRE_ANALYSIS in \"" +
			       PRE_ANALYSIS_OUT_FILE + "\"");
	if(METAMETHODS)
	    System.out.println("\tMETAMETHODS");

	if(SMART_CALL_GRAPH)
	    System.out.println("\tSMART_CALL_GRAPH");

	if(!(METAMETHODS || SMART_CALL_GRAPH))
	    System.out.println("\tDumbCallGraph");

	if(PointerAnalysis.CALL_CONTEXT_SENSITIVE)
	    System.out.println("\tCALL_CONTEXT_SENSITIVE = " +
			       PointerAnalysis.MAX_SPEC_DEPTH);
	
	if(PointerAnalysis.THREAD_SENSITIVE)
	    System.out.println("\tTHREAD SENSITIVE");
	if(PointerAnalysis.WEAKLY_THREAD_SENSITIVE)
	    System.out.println("\tWEAKLY_THREAD_SENSITIVE");

	if(PointerAnalysis.LOOP_SENSITIVE)
	    System.out.println("\tLOOP_SENSITIVE not implemented yet!");

	if(SHOW_CH)
	    System.out.println("\tSHOW_CH");

	if(SHOW_CG)
	    System.out.println("\tSHOW_CG");

	if(CHECK_NO_CALLEES)
	    System.out.println("\tCheck for CALLs with no callees");

	if(SHOW_DETAILS)
	    System.out.println("\tSHOW_DETAILS");

	if(DO_ANALYSIS){
	    if(mm_to_analyze.size() == 1){
		Method method = (Method) (mm_to_analyze.iterator().next());
		System.out.println("\tDO_ANALYSIS " +
				   method.declClass + "." + method.name);
	    }
	    else{
		System.out.println("\tDO_ANALYSIS");
		for(Iterator it = mm_to_analyze.iterator(); it.hasNext(); ) {
		    Method method = (Method) it.next();
		    System.out.println("\t\t" + method.declClass + "." +
				       method.name);
		}
	    }
	}

	if(DO_INTERACTIVE_ANALYSIS)
	    System.out.println("\tDO_INTERACTIVE_ANALYSIS" +
			       (INTERACTIVE_ANALYSIS_DETAILS?"(details)":""));

	if(MA_MAPS){
	    System.out.println("\tMA_MAPS in \"" + MA_MAPS_OUTPUT_FILE + "\"");
	    mainfo_opts.print("\t\t");
	}

	if(DO_SAT)
	    System.out.println("\tDO_SAT (" + SAT_FILE + ")");

	/*
	if(ANALYZE_ALL_ROOTS)
	    System.out.println("\tANALYZE_ALL_ROOTS");

	if(SYNC_ELIM_ALL_ROOTS)
	    System.out.println("\tSYNC_ELIM_ALL_ROOTS");
	
	if(INST_SYNCOPS)
	    System.out.println("\tINST_SYNCOPS");

	if(ELIM_SYNCOPS)
	    System.out.println("\tELIM_SYNCOPS");
	
	if(INST_SYNCOPS)
	    System.out.println("\tINST_SYNCOPS");
	*/
	
	if(MAInfo.NO_TG)
	    System.out.println("\tNO_TG");

	if(COMPILE)
	    System.out.println("\tCOMPILE");

	if(RTJ_REMOVE_CHECKS)
	    System.out.println("\tRTJ_REMOVE_CHECKS");

	if(RTJ_SUPPORT) {
	    System.out.print("\tRTJ_SUPPORT ");
	    if(Realtime.ANALYSIS_METHOD == Realtime.SIMPLE)
		System.out.println("keep all checks");
	    else
		if(Realtime.ANALYSIS_METHOD == Realtime.ALL)
		    System.out.println("remove all checks");
		else {
		    System.out.println("unknown analysis method -> FAIL");
		    System.exit(1);
		}
	}

	System.out.println();
    }


    private static boolean analyzable(MetaMethod mm) {
	HMethod hm = mm.getHMethod();
	if(java.lang.reflect.Modifier.isNative(hm.getModifiers()))
	    return false;
	if(java.lang.reflect.Modifier.isAbstract(hm.getModifiers()))
	    return false;
	return true;
    }

    // Generates the memory allocation policies: each allocation sites
    // is assigned one of the following allocation policy:
    // on the stack, on the thread specific heap or on the global heap.
    // The HcodeFactory (and implicitly the allocation pollicies maps as hcf
    // contains a pointer to the maps) and the linker are serialized into
    // a file.
    private static void ma_maps() {
	MetaCallGraph mcg = pa.getMetaCallGraph();
	MetaMethod mroot = new MetaMethod(hroot, true);
	Set allmms = mcg.getAllMetaMethods();
	
	// The following loop has just the purpose of timing the analysis of
	// the entire program. Doing it here, before any memory allocation
	// optimization, allows us to time it accurately.
	g_tstart = System.currentTimeMillis();
	for(Iterator it = allmms.iterator(); it.hasNext(); ) {
            MetaMethod mm = (MetaMethod) it.next();
            if(!analyzable(mm)) continue;
            pa.getIntParIntGraph(mm);
        }
        System.out.println("Intrathread Analysis time: " +
                           (time() - g_tstart) + "ms");
	System.out.println("===================================\n");

	if (mainfo_opts.USE_INTER_THREAD) {
	    g_tstart = System.currentTimeMillis();
	    for(Iterator it = allmms.iterator(); it.hasNext(); ) {
		MetaMethod mm = (MetaMethod) it.next();
		if(!analyzable(mm)) continue;
		pa.getIntThreadInteraction(mm);
	    }
	    System.out.println("Interthread Analysis time: " +
			       (time() - g_tstart) + "ms");
	    System.out.println("===================================\n");
	}

	g_tstart = time();
	MAInfo mainfo = 
	    new MAInfo(pa, hcf, linker, allmms, mainfo_opts);
	System.out.println("GENERATION OF MA INFO TIME  : " +
			   (time() - g_tstart) + "ms");
	System.out.println("===================================\n");

	if(SHOW_DETAILS) { // show the allocation policies	    
	    System.out.println();
	    mainfo.print();
	    System.out.println("===================================");
	}

	if(!COMPILE) {
	    g_tstart = time();
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
		oos.flush();
		oos.close();
	    } catch(IOException e){ System.err.println(e); }
	    System.out.println((time() - g_tstart) + "ms");
	}
    }


    /*
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
    */

    // Analyzes the methods given with the "-a" flag.
    private static void do_analysis() {
	for(Iterator it = mm_to_analyze.iterator(); it.hasNext(); ) {
	    Method analyzed_method = (Method) it.next();
	    if(analyzed_method.declClass == null)
		analyzed_method.declClass = root_method.declClass;
	    display_method(analyzed_method);
	}
    }

    /*
    private static void analyze_all_roots() {
	MetaCallGraph mcg = pa.getMetaCallGraph();
	Set allmms = mcg.getAllMetaMethods();

	// The following loop has just the purpose of timing the analysis of
	// the entire program. Doing it here, before any memory allocation
	// optimization, allows us to time it accurately.
       g_tstart = System.currentTimeMillis();
       for(Iterator it = allmms.iterator(); it.hasNext(); ) {
            MetaMethod mm = (MetaMethod) it.next();
            if(!analyzable(mm)) continue;
            pa.getIntParIntGraph(mm);
        }
        System.out.println("Intrathread Analysis time: " +
                           (time() - g_tstart) + "ms");

        if (USE_INTER_THREAD) {
          g_tstart = System.currentTimeMillis();
          for(Iterator it = allmms.iterator(); it.hasNext(); ) {
            MetaMethod mm = (MetaMethod) it.next();
            if(!analyzable(mm)) continue;
            System.out.println("Thread Interaction for: " + mm);
            pa.getIntThreadInteraction(mm);
          }
          System.out.println("Tnterthread Analysis time: " +
                           (time() - g_tstart) + "ms");
        }
    }
    */

    /*
    private static void sync_elim_all_roots() {
	SyncElimination se = new SyncElimination(pa);
	for(Iterator mit = mroots.iterator(); mit.hasNext(); ) {
            HMethod hm = (HMethod) mit.next();
	    System.out.println("\n sync elim root " + hm);
    	    for(Iterator it = split_rel.getValues(hm).iterator();
		it.hasNext(); ) {
		MetaMethod mm = (MetaMethod) it.next();
		if(!analyzable(mm)) continue;
		if (USE_INTER_THREAD) { 
		    se.addRoot_interthread(mm);
		} else { 
		    se.addRoot_intrathread(mm);
		}
	    }
	}
        if (!USE_INTER_THREAD) {
          Set s = ch.callableMethods();
	  for(Iterator mit = s.iterator(); mit.hasNext(); ) {
            HMethod hm = (HMethod) mit.next();
            // Should really check to see that hm is in a runnable class
            // or one that inherits from Thread
            if (hm.getName().equals("run") &&
                (hm.getParameterTypes().length == 0)) { 
    	      for(Iterator it = split_rel.getValues(hm).iterator();
		  it.hasNext(); ) {
		  MetaMethod mm = (MetaMethod) it.next();
		  if(!analyzable(mm)) continue;
		  se.addRoot_intrathread(mm);
              }
            } 
          }
	}

	se.calculate();
	java.io.PrintWriter out = new java.io.PrintWriter(System.out, true);
	HCodeFactory hcf_nosync = SyncElimination.codeFactory(hcf, se);
        Set s = ch.callableMethods();
	for(Iterator mit = s.iterator(); mit.hasNext(); ) {
            HMethod hm = (HMethod) mit.next();
	    System.out.println("sync elim converting " + hm);
	    HCode hcode = hcf_nosync.convert(hm);
	    if (hcode != null) hcode.print(out);
            for(Iterator it = split_rel.getValues(hm).iterator();
		it.hasNext(); ) {
                MetaMethod mm = (MetaMethod) it.next();
                if(!analyzable(mm)) continue;
                ParIntGraph ext_pig = pa.getExtParIntGraph(mm);
                ParIntGraph int_pig = pa.getIntParIntGraph(mm);
		ParIntGraph pig_inter_thread = pa.getIntThreadInteraction(mm);
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
    }
    */

    // Analyzes the methods given interactively by the user.
    private static void do_interactive_analysis() {
	BufferedReader d = 
	    new BufferedReader(new InputStreamReader(System.in));
	while(true) {
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
	    try {
		display_method(analyzed_method);
	    }
	    catch(harpoon.ClassFile.NoSuchClassException e) {
		System.out.println("Class not found: \"" +
				   analyzed_method.declClass + "\"");
	    }
	}
    }

    /*
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
    */
    
    // tests whether the method hm is the same thing as
    // class_name.method_name
    private static boolean isEqual(HMethod hm, String class_name,
				   String method_name) {
	HClass hclass = hm.getDeclaringClass();
	return(hm.getName().equals(method_name) &&
	       hclass.getName().equals(class_name));
    }

    /*
    private static void do_sat_analyze_mmethod(MetaMethod mm) {
	HMethod hm = mm.getHMethod();
	HCode hcode = hcf.convert(hm);
	if(hcode == null) return;
	for(Iterator it = hcode.getElementsI(); it.hasNext(); ) {
	    Quad q = (Quad) it.next();
	    q.accept(sat_qv);
	}
    }
    */

    /* 
    static void do_elim_syncops() {
	MetaCallGraph mcg = pa.getMetaCallGraph();
	MetaMethod mroot = new MetaMethod(hroot, true);
	Set allmms = mcg.getAllMetaMethods();
	SyncElimination se = new SyncElimination(pa);
	if (USE_INTER_THREAD)
	    se.addRoot_interthread(mroot);
	else
	    se.addRoot_intrathread(mroot);
	se.calculate();
	
	HCodeFactory hcf_nosync = SyncElimination.codeFactory(hcf, se);
	for(Iterator it = allmms.iterator(); it.hasNext(); ) {
            MetaMethod mm = (MetaMethod) it.next();
            HMethod m = mm.getHMethod();
            System.out.println("Eliminating Sync Ops in Method "+m);
	    HCode hcode = hcf_nosync.convert(m);
        }
    }

    static void do_elim_syncops(HMethod hm) {
	System.out.println("\nEliminating unnecessary synchronization operations.");
	
	SyncElimination se = new SyncElimination(pa);

    	for(Iterator it = split_rel.getValues(hm).iterator(); it.hasNext();){
	    MetaMethod mm = (MetaMethod) it.next();
            if (USE_INTER_THREAD) { 
              se.addRoot_interthread(mm);
            } else { 
	      se.addRoot_intrathread(mm);
            }
	}
	
	se.calculate();
	
	HCodeFactory hcf_nosync = SyncElimination.codeFactory(hcf, se);
	
	//try {
	    java.io.PrintWriter out = new java.io.PrintWriter(System.out, true);
	    MetaCallGraph mcg = pa.getMetaCallGraph();
	    Set allmm = mcg.getAllMetaMethods();
	    Iterator it = allmm.iterator();
	    while (it.hasNext()) {
	        MetaMethod mm = (MetaMethod)it.next();
	        HMethod m = mm.getHMethod();
	        System.out.println("Transforming method "+m);
	        HCode hcode = hcf_nosync.convert(m);
	        if (hcode != null) hcode.print(out);
	    }
	//} catch (IOException x) {}

    }

    static void do_inst_syncops(HMethod hm) {
	System.out.println("\nInstrumenting synchronization operations.");
	
	InstrumentSyncOps se = new InstrumentSyncOps(pa);

    	for(Iterator it = split_rel.getValues(hm).iterator(); it.hasNext();){
	    MetaMethod mm = (MetaMethod) it.next();
	    se.addRoot(mm);
	}
	
	se.calculate();
	
	HCodeFactory hcf_instsync = InstrumentSyncOps.codeFactory(hcf, se);
	
	//try {
	    java.io.PrintWriter out = new java.io.PrintWriter(System.out, true);
	    MetaCallGraph mcg = pa.getMetaCallGraph();
	    Set allmm = mcg.getAllMetaMethods();
	    Iterator it = allmm.iterator();
	    while (it.hasNext()) {
	        MetaMethod mm = (MetaMethod)it.next();
	        HMethod m = mm.getHMethod();
	        System.out.println("Transforming method "+m);
	        HCode hcode = hcf_instsync.convert(m);
	        if (hcode != null) hcode.print(out);
	    }
	//} catch (IOException x) {}

    }
    */

    /*
    static HClass[] get_classes(Set allmm) {
	HashSet ll = new HashSet();
	Iterator it = allmm.iterator();
	while (it.hasNext()) {
	    MetaMethod mm = (MetaMethod)it.next();
	    HMethod m = mm.getHMethod();
	    HClass hc = m.getDeclaringClass();
	    if (hc.isArray()) continue;
	    ll.add(hc);
	}
	return (HClass[])ll.toArray(new HClass[ll.size()]);
    }
    
    static void dump_java(HClass[] interfaceClasses) 
    throws IOException {

	java.io.PrintWriter out = new java.io.PrintWriter(System.out, true);
	for (int i=0; i<interfaceClasses.length; i++) {
	    HMethod hm1[] = interfaceClasses[i].getDeclaredMethods();
	    WorkSet hmo=new WorkSet();
	    System.out.println(interfaceClasses[i]+":");
	    for (int ind=0;ind<hm1.length;ind++) {
		hmo.add(hm1[ind]);
	    }
	    HMethod hm[] = new HMethod[hmo.size()];
	    Iterator hmiter=hmo.iterator();
	    int hindex=0;
	    while (hmiter.hasNext()) {
		hm[hindex++]=(HMethod)hmiter.next();
		System.out.println(hm[hindex-1]);
	    }

	    HCode hc[] = new HCode[hm.length];
	    HCodeFactory hcf2 = QuadWithTry.codeFactory(hcf);
	    for (int j=0; j<hm.length; j++) {
		hc[j] = hcf2.convert(hm[j]);
		if (hc[j]!=null) hc[j].print(out);
	    }
	    Jasmin jasmin=new Jasmin(hc, hm, interfaceClasses[i]);
	    FileOutputStream file;
	    if (interfaceClasses.length!=1)
		file=new FileOutputStream("out"+i+".j");
	    else
		file=new FileOutputStream("out.j");
	    PrintStream tempstream=new PrintStream(file);
	    jasmin.outputClass(tempstream);
	    file.close();
	}
    }
    */

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
	"-m, --meta      Uses the real MetaMethods (unsupported yet).",
	"-s, --smart     Uses the SmartCallGrapph.",
	"-d, --dumb      Uses the simplest CallGraph (default).",
	"-c, --showch    Shows debug info about ClassHierrachy.",
	"--loadpre file  Loads the precomputed preanalysis results from disk.",
	"--savepre file  Saves the preanalysis results to disk.",
	"--showcg        Shows the (meta) call graph.",
	"--check_nc      Check for CALLs with no detected callees",
	"--showsplit     Shows the split relation.",
	"--details       Shows details/statistics.",
	"--ccs=depth     Activates call context sensitivity with a given",
	"                 maximum call chain depth.",
	"--ts            Activates full thread sensitivity.",
	"--wts           Activates weak thread sensitivity.",
	"--ls            Activates loop sensitivity.",
	"--mamaps=file   Computes the allocation policy map and serializes",
	"                 the CachingCodeFactory (and implicitly the",
	"                 allocation map) and the linker to disk.",
	"                 It turns on the stack and thread alloc.",
	"--sa 0|1|2      Sets the stack allocation policy:",
	"                 0 - no stack allocation",
	"                 1 - do stack allocation, but not in loops (default)",
	"                 2 - do stack allocation, wherever it's possible",
	"--ta 0|1        Turns on/off the thread allocation.",
	"--ns 0|1        Turns on/off the generation of \"no sync\" hints.",
	"--prealloc      Activates the pre-allocation (default off).",
	"-a method       Analyzes he given method. If the method is in the",
	"                 same class as the main method, the name of the",
	"                 class can be ommited. More than one \"-a\" flags",
	"                 can be used on the same command line.",
	"-i              Interactive analysis of methods.",
	"-I              Interactive analysis of methods (more details).",
	"--wit           Use the inter-thread analysis while generating the",
	"                 fancy memory allocation hints",
	"--inline nb     Use method inlining to enable more stack allocation;",
	"                 inlining chains of up to nb call sites",
	"                 (makes sense only with --mamaps).",
	"--sat=file      Generates dummy sets of calls to .start() and",
	"                 .join() that must be changed (for the thread",
	"                 inlining). Don't try to use it seriously!",
	"--notg          No thread group facility is necessary. In the",
	"                 future, this will be automatically detected by",
	"                 the analysis.",
	"-N filename     Read in Instrumentation code factory",
	"-P filename     Read in profile information"
    };


    private static void show_help() {
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
    }

    // Fills in the set of program_roots (methods & classes)
    private static void construct_program_roots() {
        program_roots = new HashSet();
	program_roots.add(hroot);
	program_roots.addAll
	    (harpoon.Backend.Runtime1.Runtime.runtimeCallableMethods(linker));

	if(RTJ_SUPPORT)
	    program_roots.addAll(Realtime.getRoots(linker));

	// load additional roots (if any)
	if (rootSetFilename != null)
	    addToRootSet(program_roots, rootSetFilename);
	
	if(SHOW_CH)
	    Util.print_collection(program_roots, "Set of roots");
    }

    // Constructs the class hierarchy of the analyzed program.
    private static void construct_class_hierarchy() {
	construct_program_roots();
	
	System.out.print("ClassHierarchy ... ");
	long tstart = time();

	ch = new QuadClassHierarchy(linker, program_roots, hcf);

	System.out.println((time() - tstart) + "ms");


	if(SHOW_CH) {
	    System.out.println("Root method = " + hroot);
	    Util.print_collection(ch.instantiatedClasses(),
				  "Instantiated classes");
	    Util.print_collection(ch.callableMethods(), "Callable methods");
	}
    }


    private static void addToRootSet(final Set roots, String filename) {
	try {
	    System.out.print("Loading extra roots from " + filename + "... ");
	    ParseUtil.readResource
		(filename,
		 new ParseUtil.StringParser() {
			 public void parseString(String s)
			     throws ParseUtil.BadLineException {
			     if (s.indexOf('(') < 0) // parse as class name.
				 roots.add(ParseUtil.parseClass(linker, s));
			     else // parse as method name.
				 roots.add(ParseUtil.parseMethod(linker, s));
			 }
		     });

	    System.out.println("done");
	} catch (IOException ex) {
	    Util.assert(false, "Error reading " + filename + ": " + ex);
	}
    }


    // Returns the intersection of the set received as a parameter with
    // the set of methods.
    private static Set select_methods(Set set) {
	Set retval = new HashSet();
	for(Iterator it = set.iterator(); it.hasNext(); ) {
	    Object obj = it.next();
	    if(obj instanceof HMethod)
		retval.add(obj);
	}
	return retval;
    }


    // Returns the set of static initializers of all the instanciated classes.
    private static Set get_static_initializers() {
	Set retval = new HashSet();
	for(Iterator it = ch.classes().iterator(); it.hasNext(); ) {
	    HClass hclass = (HClass) it.next();
	    HMethod hm = hclass.getClassInitializer();
	    if(hm != null)
		retval.add(hm);
	}
	return retval;
    }


    // Constructs the set of method roots (see the comments around mroots)
    private static void construct_mroots() {
	mroots = new HashSet();
	mroots.addAll(select_methods(program_roots));
	mroots.addAll(get_static_initializers());
	mroots.add(hroot);

	if(SHOW_DETAILS) {
	    System.out.println("Method roots: {");
	    for(Iterator it = mroots.iterator(); it.hasNext(); )
		System.out.println(" " + (HMethod) it.next());
	    System.out.println("}");
	}
    }


    // Constructs the meta call graph and the meta all callers objects
    // for the currently analyzed program.
    private static void construct_meta_call_graph() {
	long tstart = 0L;

	if(METAMETHODS){ // real meta-methods
	    System.out.print("MetaCallGraph ... ");
	    tstart = time();
	    mcg = new MetaCallGraphImpl(bbconv, ch, mroots);
	    System.out.println((time() - tstart) + "ms");

	    System.out.print("MetaAllCallers ... ");
	    tstart = time();
	    mac = new MetaAllCallers(mcg);
	    System.out.println((time() - tstart) + "ms");
	}
	else{
	    // the set of "run()" methods (the bodies of threads)
	    Set run_mms = null;
	    CallGraph cg = null;

	    if(SMART_CALL_GRAPH){ // smart call graph!
		System.out.print("MetaCallGraph ... ");
		tstart = time();
		MetaCallGraph fmcg = new MetaCallGraphImpl(bbconv, ch, mroots);
		System.out.println((time() - tstart) + "ms");

		run_mms = fmcg.getRunMetaMethods();

		System.out.print("SmartCallGraph ... ");
		tstart = time();
		cg = new SmartCallGraph(fmcg);
		System.out.println((time() - tstart) + "ms");
	    }
	    else
		cg = new CallGraphImpl(ch, hcf);

	    System.out.print("FakeMetaCallGraph ... ");
	    tstart = time();
	    mcg = new FakeMetaCallGraph(cg, cg.callableMethods(), run_mms);
	    System.out.println((time() - tstart) + "ms");
	    
	    System.out.print("(Fake)MetaAllCallers ... ");
	    tstart = time();
	    mac = new MetaAllCallers(mcg);
	    System.out.println((time() - tstart) + "ms");
	}

	if(SHOW_CG){
	    System.out.println("MetaCallGraph:");
	    mcg.print(new java.io.PrintWriter(System.out, true), true,
		      new MetaMethod(hroot, true));
	}
    }


    // Constructs the split relation attached to the current meta call graph.
    private static void construct_split_relation() {
	System.out.print("SplitRelation ... ");
	long tstart = time();
	split_rel = mcg.getSplitRelation();
	System.out.println((time() - tstart) + "ms");

	if(SHOW_SPLIT){
	    System.out.println("Split relation:");
	    Debug.show_split(mcg.getSplitRelation());
	}
    }


    private static long time() {
	return System.currentTimeMillis();
    }


    //////////////////////////////////////////////////////////////////////
    ////////////////// REALTIME STUFF STARTS /////////////////////////////

    private static boolean DEBUG_RT = true;

    private static HClass java_lang_Runnable = null;

    private static boolean can_remove_all_checks() {
	long start = time();
	boolean result = can_remove_all_checks2();
	System.out.println("can_remove_all_checks ... " + 
			   (time() - start) + " ms");
	return result;
    }

    // does the real job
    private static boolean can_remove_all_checks2() {
	java_lang_Runnable = linker.forName("java.lang.Runnable");
	Util.assert(java_lang_Runnable != null,
		    "java.lang.Runnable not found!");

	Set runs = get_interesting_runs();

	if(runs.isEmpty()) {
	    System.out.println("Pattern 1 unfound, switch to pattern 2");
	    Set all_runs = get_all_runs();
	    if(DEBUG_RT)
		print_set(all_runs, "All runs in the program");
	    for(Iterator it = all_runs.iterator(); it.hasNext(); ) {
		HMethod hmethod = (HMethod) it.next();
		if(!nothing_escapes_intra_thread(hmethod))
		    return false;
	    }
	    return true;
	}

	if(DEBUG_RT)
	    print_set(runs, "Interesting runs");

	for(Iterator it = runs.iterator(); it.hasNext(); ) {
	    HMethod hm = (HMethod) it.next();
	    if(!nothing_escapes(hm))
		return false;
	}

	return true;
    }


    private static MetaMethod hm2mm(HMethod hm) {
	return new MetaMethod(hm, true);
    }

    private static HMethod enter = null;

    private static Set get_interesting_runs() {
	Set result = new HashSet();
	enter = get_enter_method();
	MetaMethod[] callers = mac.getCallers(new MetaMethod(enter, true));
	for(int i = 0; i < callers.length; i++)
	    result.addAll(get_interesting_runs(callers[i].getHMethod()));
	return result;
    }


    private static Set get_interesting_runs(HMethod hm) {
	Set result = new HashSet();

	if(DEBUG_RT)
	    System.out.println("get_interesting_runs(" + hm + ")");

	Set calls = get_interesting_calls(hm);
	
	if(DEBUG_RT)
	    print_set(calls, "Interesting calls for " + hm);

	TypeInference ti =
	    new TypeInference(hm, hcf.convert(hm), get_ietemps(calls));

	for(Iterator it = calls.iterator(); it.hasNext(); ) {
	    CALL cs = (CALL) it.next();
	    ExactTemp et = new ExactTemp(cs, cs.params(1));
	    Set types = ti.getType(et);
	    if(DEBUG_RT)
		print_set(types, "Possible types for " + et);
	    for(Iterator it_t = types.iterator(); it_t.hasNext(); ) {
		HClass hclass = (HClass) it_t.next();		
		Set children = new HashSet(ch.children(hclass));
		children.add(hclass);
		if(DEBUG_RT)
		    print_set(children, "Children for " + hclass);
		for(Iterator it_c = children.iterator(); it_c.hasNext(); ) {
		    HClass child = (HClass) it_c.next();
		    if(ch.instantiatedClasses().contains(child)) {
			HMethod run = extract_run(child);
			if(run != null)
			    result.add(run);
		    }
		}
	    }
	}
	return result;
    }

    private static HMethod extract_run(HClass hclass) {
	if(DEBUG_RT)
	    System.out.println("extract_run(" + hclass + ")");

	HMethod result = null;
	if(!hclass.isInstanceOf(java_lang_Runnable))
	    return null;
	HMethod[] hms = hclass.getMethods();
	for(int i = 0; i < hms.length; i++)
	    if(hms[i].getName().equals("run") &&
	       (hms[i].getParameterTypes().length == 0)) {
		if(DEBUG_RT)
		    System.out.println("\t" + hms[i]);
		if(result == null)
		    result = hms[i];
		else
		    Util.assert(false, "too many run methods!");
	    }
	return result;
    }

    private static Set get_ietemps(Set calls) {
	Set result = new HashSet();
	for(Iterator it = calls.iterator(); it.hasNext(); ) {
	    CALL cs = (CALL) it.next();
	    result.add(new ExactTemp(cs, cs.params(1)));
	}
	return result;
    }

    private static Set get_interesting_calls(HMethod hm) {
	if(DEBUG_RT)
	    System.out.println("get_interesting_calls(" + hm + ")");
	Set result = new HashSet();
	HCode hcode = hcf.convert(hm);
	for(Iterator it = hcode.getElementsI(); it.hasNext(); ) {
	    Quad quad = (Quad) it.next();
	    if(quad instanceof CALL) {
		CALL cs = (CALL) quad;
		MetaMethod[] callees =
		    mcg.getCallees(hm2mm(hm), cs);
		for(int i = 0; i < callees.length; i++)
		    if(callees[i].getHMethod().equals(enter))
			result.add(cs);
	    }
	}
	return result;
    }

    private static HMethod get_enter_method() {
	Set methods = get_methods("javax.realtime.CTMemory", "enter");
	if(DEBUG_RT)
	    print_set(methods, "enter methods");
	for(Iterator it = methods.iterator(); it.hasNext(); ) {
	    HMethod hm = (HMethod) it.next();
	    if(hm.getParameterTypes().length != 1) {
		if(DEBUG_RT)
		    System.out.println("irrelevant method " + hm);
		it.remove();
	    }
	}
	if(DEBUG_RT)
	    print_set(methods, "good enter methods");
	Util.assert(methods.size() == 1, "Too many enter methods!");
	return (HMethod) methods.iterator().next();
    }

    // Returns all the methods called mthd_name from class cls_name.
    private static Set get_methods(String cls_name, String mthd_name) {
	Set result = new HashSet();
	HClass hclass = linker.forName(cls_name);
	Util.assert(hclass != null, cls_name + " was not found!");

	HMethod[] hms = hclass.getMethods();
	for(int i = 0; i < hms.length; i++)
	    if(hms[i].getName().equals(mthd_name))
		result.add(hms[i]);

	return result;
    }


    private static boolean nothing_escapes(HMethod hm) {
	if(DEBUG_RT)
	    System.out.println("nothing_escapes(" + hm + ")");

	ParIntGraph pig = pa.threadInteraction(hm2mm(hm));
	pig = (ParIntGraph) pig.clone();
	// we don't care about the exceptions; if an exception is thrown
	// out of the run method of a thread, the program is gone stop with
	// an exception anyway.
	pig.G.excp.clear();

	//TODO: some of the native methods are not harmful:
	//   java.lang.Object.getClass()
	//   java.lang.Thread.isAlive() etc.
	// make sure we clean the graph a bit before looking at it
	// (there should be more info about this in MAInfo)

	if(DEBUG_RT)
	    System.out.println("threadExtInteraction = " + pig + "\n\n");
	Set nodes = pig.allNodes();
	for(Iterator it = nodes.iterator(); it.hasNext(); ) {
	    PANode node = (PANode) it.next();
	    if((node.type() == PANode.INSIDE) &&
	       !pig.G.captured(node)) {
		System.out.println
		    (node + " created at " + 
		     Debug.code2str(pa.getNodeRepository().node2Code
				    (node.getRoot())) +
		     " escapes -> false");
		return false;
	    }
	}
	return true;
    }


    private static void print_set(Set set, String set_name) {
	System.out.println(set_name + " {");
	for(Iterator it = set.iterator(); it.hasNext(); )
	    System.out.println("\t" + it.next());
	System.out.println("}");
    }


    private static Set get_all_runs() {
	Set runs = new HashSet();
	for(Iterator it = mcg.getAllMetaMethods().iterator(); it.hasNext(); ) {
	    HMethod hm = ((MetaMethod) it.next()).getHMethod();
	    HClass hclass = hm.getDeclaringClass();
	    if(hm.getName().equals("run") &&
	       hclass.isInstanceOf(java_lang_Runnable)) 
		runs.add(hm);
	}
	return runs;
    }

    private static boolean nothing_escapes_intra_thread(HMethod hm) {
	if(!PointerAnalysis.analyzable(hm))
	    return true;

	MetaMethod mm = hm2mm(hm);
	// the set of nodes appearing in the external pig is the
	// set of the escaping objects
	ParIntGraph pig = pa.getExtParIntGraph(mm);
	if(DEBUG_RT)
	    System.out.println("ExtPIG(" + hm + ")\n" + pig);
	Set nodes = pig.allNodes();

	// if one of the elements of the set nodes is an INSIDE node,
	// some objects are leaking out of the memory scope ...
	for(Iterator it = nodes.iterator(); it.hasNext(); ) {
	    PANode node = (PANode) it.next();
	    if(node.type == PANode.INSIDE) return false;
	}
	// nothing escapes!
	return true;
    }
}
