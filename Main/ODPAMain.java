// ODPAMain.java, created Fri Jan 14 10:54:16 2000 by salcianu
// Copyright (C) 1999 Alexandru SALCIANU <salcianu@MIT.EDU>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Main;

//First two by FV
import java.util.HashMap;
import java.util.Map;
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
import harpoon.ClassFile.Loader;
import harpoon.ClassFile.Relinker;

import harpoon.IR.Quads.Quad;
import harpoon.IR.Quads.QuadVisitor;
import harpoon.IR.Quads.QuadWithTry;
import harpoon.IR.Quads.QuadFactory;
import harpoon.IR.Quads.HEADER;
import harpoon.IR.Quads.METHOD;

import harpoon.Analysis.Quads.QuadClassHierarchy;
import harpoon.Analysis.Quads.CallGraph;
import harpoon.Analysis.Quads.CallGraphImpl;
import harpoon.Analysis.AllCallers;
import harpoon.Analysis.ClassHierarchy;
import harpoon.Analysis.BasicBlock;

import harpoon.Analysis.PointerAnalysis.PointerAnalysis;
import harpoon.Analysis.PointerAnalysis.ODPointerAnalysis;
import harpoon.Analysis.PointerAnalysis.PANode;
import harpoon.Analysis.PointerAnalysis.ODParIntGraph;
import harpoon.Util.DataStructs.Relation;
import harpoon.Analysis.PointerAnalysis.ODMAInfo;
import harpoon.Analysis.PointerAnalysis.ODNodeStatus;
import harpoon.Analysis.PointerAnalysis.SyncElimination;
import harpoon.Analysis.PointerAnalysis.InstrumentSyncOps;
import harpoon.Analysis.PointerAnalysis.AllocationNumbering;
import harpoon.Analysis.PointerAnalysis.PAWorkList;

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
import harpoon.Util.Util;
import harpoon.Util.WorkSet;

import harpoon.Analysis.PointerAnalysis.Debug;
import harpoon.Analysis.MetaMethods.SmartCallGraph;

import harpoon.IR.Quads.CALL;

import harpoon.IR.Jasmin.Jasmin;
import harpoon.Util.DataStructs.LightRelation;
import harpoon.Util.DataStructs.LightMap;


/**
 * <code>ODPAMain</code> is a simple Pointer Analysis top-level class.
 * It is designed for testing and evaluation only.
 * 
 * @author  Alexandru SALCIANU <salcianu@retezat.lcs.mit.edu>
 * @version $Id: ODPAMain.java,v 1.1.2.1 2000-12-11 22:56:41 vivien Exp $
 */
public abstract class ODPAMain {

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
    private static boolean SHOW_DETAILS = true;

    // make the program to analyse some method;
    private static boolean DO_ANALYSIS = false;
    // turns on the interactive analysis
    private static boolean DO_INTERACTIVE_ANALYSIS = false;

    // force the creation of the memory allocation info.
    private static boolean MA_MAPS = false;
    // gen the stack allocation hints
    private static boolean DO_STACK_ALLOCATION  = false;
    // gen the thread allocation hints
    private static boolean DO_THREAD_ALLOCATION = false;
    // gen the "no sync" hints
    private static boolean GEN_SYNC_FLAG        = false;
    // the name of the file into which the memory allocation policies
    // will be serialized
    private static String MA_MAPS_OUTPUT_FILE = null;

    private static boolean USE_OLD_CLINIT_STRATEGY = true;       
    private static boolean LOADED_LINKER = false;

    // use the inter-thread stage of the analysis while determining the
    // memory allocation policies
    private static boolean USE_INTER_THREAD = true;

    // Displays some help messages.
    private static boolean DEBUG = false;

    private static boolean DO_SAT = false;
    private static String SAT_FILE = null;

    private static boolean ELIM_SYNCOPS = false;
    private static boolean INST_SYNCOPS = false;

    private static boolean SYNC_INFO = false;
    private static boolean CALL_INFO = false;

    private static boolean DUMP_JAVA = false;

    private static boolean ANALYZE_ALL_ROOTS = false;
    private static boolean SYNC_ELIM_ALL_ROOTS = false;

    private static boolean COMPILE = false;
    
    // Load the preanalysis results from PRE_ANALYSIS_IN_FILE
    private static boolean LOAD_PRE_ANALYSIS = false;
    private static String PRE_ANALYSIS_IN_FILE = null;

    // Save the preanalysis results into PRE_ANALYSIS_OUT_FILE
    private static boolean SAVE_PRE_ANALYSIS = false;
    private static String PRE_ANALYSIS_OUT_FILE = null;

    //FV
    private static ODPointerAnalysis pa = null;
    private static int WHO_TO_ANALYZE   = 90;
    private static boolean MEMORY_OPTIMIZATION = true;
    private static boolean PRODUCTION_PROFILE = false;
    // the main method
    private static HMethod hroot = null;

    private static AllocationNumbering an=null;
    private static long[]  alloc_profile=null;
    private static long[]   sync_profile=null;
    private static int[]  call_profile_alloc=null;
    private static int[]  call_profile_call =null;
    private static long[] call_profile_count=null;
    private static long[]  prod_alloc_profile=null;
    private static long[]   prod_sync_profile=null;
    private static int[]  prod_call_profile_alloc=null;
    private static int[]  prod_call_profile_call =null;
    private static long[] prod_call_profile_count=null;
    private static int FREQ_THRESHOLD = 1;
    private static int sync_in_prog = 0;
    private static int mem_in_prog  = 0;
    private static int tsync  = 0;
    private static int tstack = 0;



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


    private static Linker linker = Loader.systemLinker;
    private static HCodeFactory hcf = null;
    private static MetaCallGraph  mcg = null;
    private static MetaAllCallers mac = null;
    private static Relation split_rel = null;
    private static CachingBBConverter bbconv = null;
    private static LBBConverter lbbconv = null;
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
    private static Set runtime_callable = null;

    // global variable used for timing measurements
    private static long g_tstart = 0L;

    public static void main(String[] params) throws IOException {
	int optind = get_options(params);
	int nbargs = params.length - optind;
	System.out.println("Inside main... " + nbargs +" arg(s)\n");
	if(nbargs < 1){
	    show_help();
	    System.exit(1);
	}
	print_options();

 	if (!USE_OLD_CLINIT_STRATEGY && !LOADED_LINKER)
 	    linker = new Relinker(linker);  

	runtime_callable = new HashSet
	    (harpoon.Backend.Runtime1.Runtime.runtimeCallableMethods(linker));

	get_root_method(params[optind]);
	if(hroot == null){
	    System.out.println("Sorry, the root method was not found\n");
	    System.exit(1);
	}

	System.err.println("*********************");
	System.err.println("     Hacked code");
	System.err.println("*********************");
	System.out.println("*********************");
	System.out.println("     Hacked code");
	System.out.println("*********************");
	System.out.println("Root method: " + root_method.declClass + "." +
			   root_method.name);

	if(LOAD_PRE_ANALYSIS)
	    load_pre_analysis();
	else {
	    pre_analysis();
	    if(SAVE_PRE_ANALYSIS)
		save_pre_analysis();
	}
	
	/* JOIN STATS
	   join_stats(lbbconv, mcg);
	   System.exit(1);
	*/

	pa = new ODPointerAnalysis(mcg, mac, lbbconv);

	if(DO_ANALYSIS)
	    do_analysis();

        if (ANALYZE_ALL_ROOTS) 
            analyze_all_roots();

        if (pa.BOUNDED_ANALYSIS_DEPTH){ 
//             boundedly_analyze_all_roots();
// 	    find_inside_nodes();
	    if (ODPAMain.WHO_TO_ANALYZE==0)
		analyze_non_exception_nodes();
	    else
		find_interesting_nodes();
	    
	}

        if (SYNC_ELIM_ALL_ROOTS) 
            sync_elim_all_roots();

	if(DO_INTERACTIVE_ANALYSIS)
	    do_interactive_analysis();
    
	if(MA_MAPS){
// 	    ma_maps();
	}

	if(DO_SAT)
	    do_sat();

// 	if(SHOW_DETAILS)
// 	    pa.print_stats();

//         if (ELIM_SYNCOPS)
//             do_elim_syncops();

	if(DUMP_JAVA)
	    dump_java(get_classes(pa.getMetaCallGraph().getAllMetaMethods()));

	if(COMPILE) {
// 	    System.out.println("MEGA TEST!");
// 	    ODMAInfo.do_additional_testing(hcf);
	    
	    SAMain.hcf = hcf;
	    SAMain.linker = linker;
//  	    SAMain.rootSetFilename="./Support/_202_jess-root-set";
	    //SAMain.HACKED_REG_ALLOC = true;
// 	    SAMain.BACKEND=SAMain.PRECISEC_BACKEND;

	    SAMain.USE_OLD_CLINIT_STRATEGY = USE_OLD_CLINIT_STRATEGY;
	    SAMain.className = root_method.declClass; // params[optind];
	    SAMain.do_it();
	}
    }
    
    // Constructs some data structures used by the analysis: the code factory
    // providing the code of the methods, the class hierarchy, call graph etc.
    private static void pre_analysis() {
	g_tstart = System.currentTimeMillis();
	//We might have loaded in a code factory w/o a preanalysis.
	if (hcf==null)
	    hcf = harpoon.IR.Quads.QuadNoSSA.codeFactory();
	System.out.println("Before CachingCodeFactory");
	hcf = new CachingCodeFactory(hcf, true);
	System.out.println("Before CachingBBConverter");
	bbconv = new CachingBBConverter(hcf);
	System.out.println("Before CachingLBBConverter");
	lbbconv = new CachingLBBConverter(bbconv);
	System.out.println("AFter all that stuff");
	if (isCoherent())
	    System.out.println("Was coherent before call to construct_class_hierarchy()");
	else
	    System.out.println("Was INcoherent before call to construct_class_hierarchy()");


	construct_class_hierarchy();

	System.out.println("after class_hierarchy");
	if (isCoherent())
	    System.out.println("Was coherent after call to construct_class_hierarchy()");
	else
	    System.out.println("Was INcoherent after call to construct_class_hierarchy()");
	construct_mroots();
	System.out.println("after mroots");
	construct_meta_call_graph();

	if (check_mcg())
	    System.out.println("MCG coherent with quads");
	else
	    System.out.println("MCG NOT coherent with quads !!!!");


	System.out.println("after meta call graph");
	construct_split_relation();
	
	System.out.println("Total pre-analysis time : " +
			   (time() - g_tstart) + "ms");

	if (isCoherent())
	    System.out.println("Was coherent after pre analysis");
	else
	    System.out.println("Was INcoherent after pre analysis");
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
	lbbconv   = (LBBConverter) ois.readObject();
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
	oos.writeObject(lbbconv);
	oos.writeObject(ch);
	oos.writeObject(mroots);
	oos.writeObject(mcg);
	oos.writeObject(mac);
	oos.writeObject(split_rel);
	oos.flush();
	oos.close();
    }


    // Finds the root method: the "main" method of "class".
    private static void get_root_method(String root_class) {
/*
        if (root_class.indexOf(".") > 0) { 
          root_method.name = root_class.substring(root_class.indexOf(".")+1);
          root_method.declClass = root_class.substring(0, root_class.indexOf("."));
        } else { 
*/
	  root_method.name = "main";
	  root_method.declClass = root_class;
/*
        }
*/
	  System.err.println(root_method.declClass);
	HClass hclass = linker.forName(root_method.declClass);
	HMethod[] hm  = hclass.getDeclaredMethods();

	// search for the main method
	hroot = null;
	for(int i = 0;i<hm.length;i++)
	    if(hm[i].getName().equals(root_method.name))
		hroot = hm[i];
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
		    ODParIntGraph int_pig = pa.getIntParIntGraph(mm);
		    ODParIntGraph ext_pig = pa.getExtParIntGraph(mm);
		    ODParIntGraph pig_inter_thread = pa.getIntThreadInteraction(mm);
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

	if (INST_SYNCOPS)
	    do_inst_syncops(hmethod);
	
// 	if (ELIM_SYNCOPS)
// 	    do_elim_syncops(hmethod);
    
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
	BufferedReader br = null;
	int c, c2;
	String arg;
	LongOpt[] longopts = new LongOpt[]{
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
	    new LongOpt("noit",          LongOpt.NO_ARGUMENT,       null, 15),
	    new LongOpt("inline",        LongOpt.NO_ARGUMENT,       null, 16),
	    new LongOpt("sat",           LongOpt.REQUIRED_ARGUMENT, null, 17),
	    new LongOpt("notg",          LongOpt.NO_ARGUMENT,       null, 18),
	    new LongOpt("loadpre",       LongOpt.REQUIRED_ARGUMENT, null, 19),
	    new LongOpt("savepre",       LongOpt.REQUIRED_ARGUMENT, null, 20),
	    new LongOpt("syncelim",      LongOpt.NO_ARGUMENT,       null, 21),
	    new LongOpt("instsync",      LongOpt.NO_ARGUMENT,       null, 22),
	    new LongOpt("dumpjava",      LongOpt.NO_ARGUMENT,       null, 23),
	    new LongOpt("analyzeroots",  LongOpt.NO_ARGUMENT,       null, 24),
	    new LongOpt("maxdepth",      LongOpt.REQUIRED_ARGUMENT, null, 29),
	    new LongOpt("maxup",         LongOpt.REQUIRED_ARGUMENT, null, 30),
	    new LongOpt("percent",       LongOpt.REQUIRED_ARGUMENT, null, 31),
	    new LongOpt("nomemopt",      LongOpt.NO_ARGUMENT,       null, 32),
	    new LongOpt("rootset",       LongOpt.REQUIRED_ARGUMENT, null, 33),
	    new LongOpt("odaprecise",    LongOpt.NO_ARGUMENT,       null, 34),
	    new LongOpt("syncelimroots", LongOpt.NO_ARGUMENT,       null, 25),
	    new LongOpt("backend",       LongOpt.REQUIRED_ARGUMENT, null, 'b'),
	    new LongOpt("output",        LongOpt.REQUIRED_ARGUMENT, null, 'o'),
	    new LongOpt("sa",            LongOpt.REQUIRED_ARGUMENT, null, 26),
	    new LongOpt("ta",            LongOpt.REQUIRED_ARGUMENT, null, 27),
	    new LongOpt("ns",            LongOpt.REQUIRED_ARGUMENT, null, 28),
	};

	Getopt g = new Getopt("ODPAMain", argv, "mscoa:iN:P:Q:", longopts);

	while((c = g.getopt()) != -1)
	    switch(c) {
	    case 'P':
		System.out.println("loading Profile");
		arg=g.getOptarg();
		try {
		    br = new BufferedReader(new FileReader(arg));
		    String in=br.readLine();
 		    int size=Integer.parseInt(in);
		    alloc_profile=new long[size];
		    for(int i=0;i<size;i++) {
			in=br.readLine();
			alloc_profile[i]=Long.parseLong(in);
		    }
		} catch (Exception e) {
		    System.out.println(e + " was thrown");
		    System.exit(1);
		}
		try {
		    String in=br.readLine();
 		    int size=Integer.parseInt(in);
		    sync_profile=new long[size];
		    for(int i=0;i<size;i++) {
			in=br.readLine();
			sync_profile[i]=Long.parseLong(in);
		    }
		    SYNC_INFO = true;
		} catch (Exception e) {
		    System.out.println(e + " was thrown");
		}
		try {
		    String in=br.readLine();
 		    int size=Integer.parseInt(in);
		    call_profile_alloc =new int [size];
		    call_profile_call  =new int [size];
		    call_profile_count =new long[size];

		    for(int i=0;i<size;i++){
			in=br.readLine();
			call_profile_alloc[i]=Integer.parseInt(in);
			in=br.readLine();
			call_profile_call [i]=Integer.parseInt(in);
			in=br.readLine();
			call_profile_count[i]=Long.parseLong(in);
		    }
		    CALL_INFO = true;
		} catch (Exception e) {
		    System.out.println(e + " was thrown");
		}
		try {
		    br.close();
		} catch (Exception e) {
		    System.out.println(e + " was thrown");
		    System.exit(1);
		}
		ODPointerAnalysis.ON_DEMAND_ANALYSIS = true;
		ODPointerAnalysis.NODES_DRIVEN = true;
                break;
	    case 'Q':
		Util.assert(ODPointerAnalysis.NODES_DRIVEN);
		System.out.println("loading production Profile");
		arg=g.getOptarg();
		try {
		    br = new BufferedReader(new FileReader(arg));
		    String in=br.readLine();
 		    int size=Integer.parseInt(in);
		    prod_alloc_profile=new long[size];
		    for(int i=0;i<size;i++) {
			in=br.readLine();
			prod_alloc_profile[i]=Long.parseLong(in);
		    }
		} catch (Exception e) {
		    System.out.println(e + " was thrown");
		    System.exit(1);
		}
		if(SYNC_INFO){
		    try {
			String in=br.readLine();
			int size=Integer.parseInt(in);
			prod_sync_profile=new long[size];
			for(int i=0;i<size;i++) {
			    in=br.readLine();
			    prod_sync_profile[i]=Long.parseLong(in);
			}
		    } catch (Exception e) {
			System.out.println(e + " was thrown");
			System.exit(1);
		    }
		}
		if(CALL_INFO){
		    try {
			String in=br.readLine();
			int size=Integer.parseInt(in);
			prod_call_profile_alloc =new int [size];
			prod_call_profile_call  =new int [size];
			prod_call_profile_count =new long[size];

			for(int i=0;i<size;i++){
			    in=br.readLine();
			    prod_call_profile_alloc[i]=Integer.parseInt(in);
			    in=br.readLine();
			    prod_call_profile_call [i]=Integer.parseInt(in);
			    in=br.readLine();
			    prod_call_profile_count[i]=Long.parseLong(in);
			}
		    } catch (Exception e) {
			System.out.println(e + " was thrown");
			System.exit(1);
		    }
		}
		try {
		    br.close();
		} catch (Exception e) {
		    System.out.println(e + " was thrown");
		    System.exit(1);
		}
		PRODUCTION_PROFILE = true;
                break;
	    case 'N':
		arg=g.getOptarg();
		System.out.println("loading "+arg);
		try {
		    ObjectInputStream ois =
			new ObjectInputStream(new FileInputStream(arg));
		    System.err.println("OPENED!");
		    an=(AllocationNumbering)ois.readObject();
		    System.err.println("SUCCEEDED!");
		    hcf=an.codeFactory();
		    linker=(Linker)ois.readObject();
		    LOADED_LINKER=true;
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
	    case 5:
		arg = g.getOptarg();
		PointerAnalysis.CALL_CONTEXT_SENSITIVE = true;
		ODPointerAnalysis.CALL_CONTEXT_SENSITIVE = true;
		ODPointerAnalysis.MAX_SPEC_DEPTH = new Integer(arg).intValue();
		PointerAnalysis.MAX_SPEC_DEPTH = ODPointerAnalysis.MAX_SPEC_DEPTH;
		break;
	    case 6:
		ODPointerAnalysis.THREAD_SENSITIVE = true;
		ODPointerAnalysis.WEAKLY_THREAD_SENSITIVE = false;
		break;
	    case 7:
		ODPointerAnalysis.WEAKLY_THREAD_SENSITIVE = true;
		ODPointerAnalysis.THREAD_SENSITIVE = false;
		break;
	    case 8:
		ODPointerAnalysis.LOOP_SENSITIVE = true;
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
		DO_STACK_ALLOCATION  = true;
		DO_THREAD_ALLOCATION = true;
		break;
	    case 26:
		DO_STACK_ALLOCATION  = (Integer.parseInt(g.getOptarg()) == 1);
		break;
	    case 27:
		DO_THREAD_ALLOCATION = (Integer.parseInt(g.getOptarg()) == 1);
		break;
	    case 28:
		GEN_SYNC_FLAG = (Integer.parseInt(g.getOptarg()) == 1);
		break;
	    case 15:
		USE_INTER_THREAD = false;
		break;
	    case 16:
		ODMAInfo.DO_METHOD_INLINING = true;
		break;
	    case 17:
		DO_SAT = true;
		SAT_FILE = new String(g.getOptarg());
		break;
	    case 18:
		ODMAInfo.NO_TG = true;
		break;
	    case 19:
		LOAD_PRE_ANALYSIS = true;
		PRE_ANALYSIS_IN_FILE = new String(g.getOptarg());
		break;
	    case 20:
		SAVE_PRE_ANALYSIS = true;
		PRE_ANALYSIS_OUT_FILE = new String(g.getOptarg());
		break;		
	    case 21:
		System.out.println("Old option syncelim -> fail");
// 		System.exit(1);
		ELIM_SYNCOPS = true;
		break;
	    case 22:
		System.out.println("Old option instsync -> fail");
		System.exit(1);
		INST_SYNCOPS = true;
		break;
	    case 23:
		DUMP_JAVA = true;
		break;
	    case 24:
		System.out.println("Old option analyzeroots -> fail");
		System.exit(1);
		ANALYZE_ALL_ROOTS = true;
		break;
	    case 25:
		System.out.println("Old option syncelimroots -> fail");
		System.exit(1);
		SYNC_ELIM_ALL_ROOTS = true;
		break;
	    case 29:
		arg = g.getOptarg();
		ODPointerAnalysis.BOUNDED_ANALYSIS_DEPTH = true;
		ODPointerAnalysis.ON_DEMAND_ANALYSIS = true;
		ODPointerAnalysis.MAX_ANALYSIS_DEPTH = new Integer(arg).intValue();
		break;
	    case 30:
		arg = g.getOptarg();
		ODPointerAnalysis.BOUNDED_ANALYSIS_DEPTH = true;
		ODPointerAnalysis.ON_DEMAND_ANALYSIS = true;
		ODPointerAnalysis.MAX_ANALYSIS_ABOVE_SPEC = new Integer(arg).intValue();
		break;
	    case 31:
		arg = g.getOptarg();
		ODPointerAnalysis.BOUNDED_ANALYSIS_DEPTH = true;
		ODPointerAnalysis.ON_DEMAND_ANALYSIS = true;
		ODPAMain.WHO_TO_ANALYZE = new Integer(arg).intValue();
		break;
	    case 32:
		ODPAMain.MEMORY_OPTIMIZATION = false;
		break;
	    case 33:
		SAMain.rootSetFilename = g.getOptarg();
		break;
	    case 34:
		ODPointerAnalysis.ODA_precise = true;
		break;
	    case 'o':
		SAMain.ASSEM_DIR = new java.io.File(g.getOptarg());
		harpoon.Util.Util.assert(SAMain.ASSEM_DIR.isDirectory(),
			    "" + SAMain.ASSEM_DIR + " must be a directory");
		break;
	    case 'b':
		COMPILE = true;
		String backendName = g.getOptarg().toLowerCase().intern();
		if (backendName == "strongarm"){
		    SAMain.BACKEND = SAMain.STRONGARM_BACKEND;
		    SAMain.HACKED_REG_ALLOC = true;
		}
		if (backendName == "sparc")
		    SAMain.BACKEND = SAMain.SPARC_BACKEND;
		if (backendName == "mips")
		    SAMain.BACKEND = SAMain.MIPS_BACKEND;
		if (backendName == "precisec")
		    SAMain.BACKEND = SAMain.PRECISEC_BACKEND;
		break;
	    }
	
	// Consistency checker
	boolean allfound = true;
// 	Set allTheQuads = an.alloc2int.keySet();
	Set allTheQuads = new HashSet();
	for(Iterator quad_it= allTheQuads.iterator(); quad_it.hasNext(); ){
	    Quad quad = (Quad) quad_it.next();
	    QuadFactory qf = quad.getFactory();
	    HMethod hm = qf.getMethod();
	    HCodeFactory myhcf=an.codeFactory();
	    HCode hc = myhcf.convert(hm);
	    boolean found = false;
	    for(Iterator q_it=hc.getElementsI(); (q_it.hasNext())&&(!found); ){
		Quad q = (Quad) q_it.next();
		if (quad.equals(q)) found = true;
	    }
	    if (!found)
		allfound = false;

// 	    if (found)
// 		System.err.println("Quad found");
// 	    else
// 		System.err.println("Missing Quad");
	    
	} 
	if (allfound)
		System.err.println("All Quads found");
	    else
		System.err.println("Missing Quads !!!!!!!!");
	

	return g.getOptind();
    }

    private static void print_options(){
	if(METAMETHODS && SMART_CALL_GRAPH){
	    System.out.println("Call Graph Type Ambiguity");
	    System.exit(1);
	}
	System.out.println("Execution options:");

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

	if(ODPointerAnalysis.CALL_CONTEXT_SENSITIVE)
	    System.out.print(" CALL_CONTEXT_SENSITIVE=" +
			     ODPointerAnalysis.MAX_SPEC_DEPTH);
	
	if(ODPointerAnalysis.THREAD_SENSITIVE)
	    System.out.print(" THREAD SENSITIVE");
	if(ODPointerAnalysis.WEAKLY_THREAD_SENSITIVE)
	    System.out.print(" WEAKLY_THREAD_SENSITIVE");

	if(ODPointerAnalysis.LOOP_SENSITIVE)
	    System.out.println(" LOOP_SENSITIVE");

	if(SHOW_CH)
	    System.out.println("\tSHOW_CH");

	if(SHOW_CG)
	    System.out.println("\tSHOW_CG");

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
	    System.out.println("\tDO_INTERACTIVE_ANALYSIS");

	if(MA_MAPS){
	    System.out.println("\tMA_MAPS in \"" + MA_MAPS_OUTPUT_FILE + "\"");
	    if(ODMAInfo.DO_METHOD_INLINING)
		System.out.println("\t\tDO_METHOD_INLINING");
	    System.out.println("\t\tDO_STACK_ALLOCATION " +
			       (DO_STACK_ALLOCATION ? "on" : "off"));
	    System.out.println("\t\tDO_THREAD_ALLOCATION " +
			       (DO_THREAD_ALLOCATION ? "on" : "off"));
	    System.out.println("\t\tGEN_SYNC_FLAG " +
			       (GEN_SYNC_FLAG ? "on" : "off"));
	    if(USE_INTER_THREAD)
		System.out.println("\t\tUSE_INTER_THREAD");
	    else
		System.out.println("\t\tJust inter procedural analysis");
	}


	if(DO_SAT)
	    System.out.println("\tDO_SAT (" + SAT_FILE + ")");

	if(ANALYZE_ALL_ROOTS)
	    System.out.println("\tANALYZE_ALL_ROOTS");

	if(SYNC_ELIM_ALL_ROOTS)
	    System.out.println("\tSYNC_ELIM_ALL_ROOTS");
	
	if(pa.BOUNDED_ANALYSIS_DEPTH)
	    System.out.println(" BOUNDED_ANALYSIS_DEPTH " + pa.MAX_ANALYSIS_DEPTH);

	if(INST_SYNCOPS)
	    System.out.println("\tINST_SYNCOPS");

	if(ELIM_SYNCOPS)
	    System.out.println("\tELIM_SYNCOPS");
	
	if(INST_SYNCOPS)
	    System.out.println("\tINST_SYNCOPS");
	
	if(ODMAInfo.NO_TG)
	    System.out.println(" NO_TG");

	System.out.println("\t\tSYNCHRONIZATION ELIMINATION " +
			   (ELIM_SYNCOPS ? "on" : "off"));
	System.out.println("\t\tMEMORY ALLOCATION OPTIMIZATION " +
			   (MEMORY_OPTIMIZATION ? "on" : "off"));

	System.out.println();
    }


    private static boolean analyzable(MetaMethod mm) {
	HMethod hm = mm.getHMethod();
	if(java.lang.reflect.Modifier.isNative(hm.getModifiers()))
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
	System.err.println("Beginning of analysis " + System.currentTimeMillis());
	for(Iterator it = allmms.iterator(); it.hasNext(); ) {
            MetaMethod mm = (MetaMethod) it.next();
            if(!analyzable(mm)) continue;
            pa.getIntParIntGraph(mm);
        }
	System.err.println("End of analysis " + System.currentTimeMillis());
        System.out.println("Intrathread Analysis time: " +
                           (time() - g_tstart) + "ms");

        if (USE_INTER_THREAD) {
          g_tstart = System.currentTimeMillis();
          for(Iterator it = allmms.iterator(); it.hasNext(); ) {
            MetaMethod mm = (MetaMethod) it.next();
            if(!analyzable(mm)) continue;
            pa.getIntThreadInteraction(mm);
          }
          System.out.println("Interthread Analysis time: " +
                           (time() - g_tstart) + "ms");
        }

	System.err.println("Beginning of MA INFO " + System.currentTimeMillis());
	g_tstart = time();
	//  ODPAMain.java
	// 	ODMAInfo mainfo = new ODMAInfo(pa, hcf, allmms, USE_INTER_THREAD);
	// =======
	ODMAInfo mainfo = 
	    new ODMAInfo(pa, hcf, allmms,
		       USE_INTER_THREAD,
		       DO_STACK_ALLOCATION,
		       DO_THREAD_ALLOCATION,
		       GEN_SYNC_FLAG);
	System.err.println("End of MA INFO " + System.currentTimeMillis());
	System.out.println("GENERATION OF MA INFO TIME  : " +
			   (time() - g_tstart) + "ms");

	if(SHOW_DETAILS) { // show the allocation policies	    
	    System.out.println();
	    mainfo.print();
	    System.out.println("===================================");
	}

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


    // Analyzes the methods given with the "-a" flag.
    private static void do_analysis() {
	for(Iterator it = mm_to_analyze.iterator(); it.hasNext(); ) {
	    Method analyzed_method = (Method) it.next();
	    if(analyzed_method.declClass == null)
		analyzed_method.declClass = root_method.declClass;
	    display_method(analyzed_method);
	}
    }


    private static void find_inside_nodes(){
	MetaCallGraph mcg = pa.getMetaCallGraph();
	Set allmms = mcg.getAllMetaMethods();
	System.err.println("Beginning of find_inside_nodes");

	// Allocation of the hash tables used to store the results of
	// the analyzes
	pa.hash_proc_int_d = new HashMap[1+pa.MAX_ANALYSIS_DEPTH];
	pa.hash_proc_ext_d = new HashMap[1+pa.MAX_ANALYSIS_DEPTH];

	for(int i = 0; i< 1+pa.MAX_ANALYSIS_DEPTH; i++) {
	    pa.hash_proc_int_d[i] =  new HashMap();
	    pa.hash_proc_ext_d[i] =  new HashMap();
	}

	g_tstart = time();
	ODMAInfo mainfo = 
	    new ODMAInfo(pa, hcf, allmms, USE_INTER_THREAD,
			 DO_STACK_ALLOCATION,
			 DO_THREAD_ALLOCATION,
			 GEN_SYNC_FLAG);
	if (ELIM_SYNCOPS) ODMAInfo.SYNC_ELIM = true;
	ODMAInfo.MEM_OPTIMIZATION = ODPAMain.MEMORY_OPTIMIZATION;

	for(Iterator it = allmms.iterator(); it.hasNext(); ) {
	    MetaMethod mm = (MetaMethod) it.next();
	    if(pa.analyzable(mm.getHMethod()))
		if(pa.create_inside_nodes(mm)){
		    mainfo.analyze_mm(mm);

	    }
	}
	System.out.println("GENERATION OF MA INFO TIME  : " +
			   (time() - g_tstart) + "ms");
	System.err.println("End of find_inside_nodes");


	if(SHOW_DETAILS) { // show the allocation policies	    
	    System.out.println();
	    mainfo.print();
	    System.out.println("===================================");
	}

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


    private static void analyze_non_exception_nodes(){
	MetaCallGraph mcg = pa.getMetaCallGraph();
	Set allmms = mcg.getAllMetaMethods();
	System.err.println("Beginning of analyze_non_exception_nodes");

	ODPointerAnalysis.NODES_DRIVEN = false;
	
	// Allocation of the hash tables used to store the results of
	// the analyzes
	pa.hash_proc_int_d = new HashMap[2];
	pa.hash_proc_ext_d = new HashMap[2];

	for(int i = 0; i< 2; i++) {
	    pa.hash_proc_int_d[i] =  new HashMap();
	    pa.hash_proc_ext_d[i] =  new HashMap();
	}

	// Computation of the number of creation sites
	int numberOfCreationSites = 0;
	for(Iterator it = allmms.iterator(); it.hasNext(); ) {
	    MetaMethod mm = (MetaMethod) it.next();
	    if(pa.analyzable(mm.getHMethod()))
		numberOfCreationSites += pa.count_creation_sites(mm);
	}
	// Computation of the number of call sites
	int numberOfCallSites = 0;
	for(Iterator it = allmms.iterator(); it.hasNext(); ) {
	    MetaMethod mm = (MetaMethod) it.next();
	    for(Iterator cit = mcg.getCallSites(mm).iterator(); cit.hasNext(); ) {
		CALL cs = (CALL) cit.next();
		MetaMethod[] callees = mcg.getCallees(mm, cs);
		numberOfCallSites += callees.length;
	    }
	}

	//===============================
	// Begining of time 
	//===============================
	g_tstart = time();
	ODMAInfo mainfo = 
	    new ODMAInfo(pa, hcf, allmms, USE_INTER_THREAD,
			 DO_STACK_ALLOCATION,
			 DO_THREAD_ALLOCATION,
			 GEN_SYNC_FLAG);
	ODMAInfo.Nodes2Status = new LightMap();
	ODPointerAnalysis.current_analysis_depth=1;
	if (ELIM_SYNCOPS) ODMAInfo.SYNC_ELIM = true;
	ODMAInfo.MEM_OPTIMIZATION = ODPAMain.MEMORY_OPTIMIZATION;

	for(Iterator it = allmms.iterator(); it.hasNext(); ) {
	    MetaMethod mm = (MetaMethod) it.next();
	    if(pa.analyzable(mm.getHMethod()))
		if(pa.create_inside_nodes(mm)){
		    mainfo.analyze_mm(mm);

	    }
	}
	if(mainfo.DO_METHOD_INLINING) {
	    System.err.println("Inlining...");
	    System.out.println("Inlining...");
	    mainfo.do_the_inlining();
	}

	float stack_occurences  = 0;
	float thread_occurences = 0;
	float global_occurences = 0;
	for(Iterator node_it=ODMAInfo.Nodes2Status.keySet().iterator();node_it.hasNext(); )
	    {
		PANode n = (PANode) node_it.next();
		System.out.print("Node: " + n + "\t ");
		ODNodeStatus status = (ODNodeStatus) ODMAInfo.Nodes2Status.get(n);
		if (status==null){
		    System.err.println("Nodes with no status !!!");
		    System.out.println("Nodes with no status !!! " + n);
		    continue;
		}
		if (status.onStack==true){
		    stack_occurences += 1;
		    System.out.println("Stack  " + 1);
		    if (status.onLocalHeap){
			System.err.println("BUG in NodesStatus...");
			System.out.println("BUG in NodesStatus..." + n);
		    }
		}
		if  (status.onLocalHeap){
		    if (status.nInlines!=0){
			if (status.nCallers==0){
			    System.err.println("BUG in NodesStatus !!!");
			    System.out.println("BUG in NodesStatus !!!" + n);
			}
			stack_occurences += 
			    (status.nInlines*1.0)
			    /(status.nCallers);
			System.out.print("Stack  " + 
					 (status.nInlines*1.0)
					 /(status.nCallers));
			thread_occurences += 
			    ((status.nCallers-status.nInlines)*1.0)
			    /(status.nCallers);
			System.out.print("\tThread " + 
					 ((status.nCallers-status.nInlines)
					  *1.0)
					 /(status.nCallers));
			System.out.println("\tInlining " + 
					   status.nInlines + "/" +
					   status.nCallers);
		    }
		    else{
			System.out.println("Thread " + 1);
			thread_occurences += 1;
		    }
		}
			
		if ((!status.onStack)&&(!status.onLocalHeap)){
		    if (status.nInlines!=0){
			if (status.nCallers==0){
			    System.err.println("BUG in NodesStatus !!!");
			    System.out.println("BUG in NodesStatus !!!" + n);
			}
			System.out.print("Stack  " + 
					 (status.nInlines*1.0)
					 /(status.nCallers));
			stack_occurences += 
			    (status.nInlines*1.0)
			    /(status.nCallers);
			System.out.print("\tGlobal " + 
					 ((status.nCallers-status.nInlines)
					  *1.0)
					 /(status.nCallers));
			global_occurences += 
			    ((status.nCallers-status.nInlines)*1.0)
			    /(status.nCallers);
			System.out.println("Inlining " + 
					   status.nInlines + "/" +
					   status.nCallers);
		    }
		    else{
			System.out.println("Global " + 1);
			global_occurences += 1;
		    }
		}
	    }
	
	// Printing out the statistics
	System.out.println("===============================");
	System.out.println("Specialization depth " 
			   + ODPointerAnalysis.MAX_SPEC_DEPTH);
	System.out.println("Analysis depth       " 
			   + ODPointerAnalysis.MAX_ANALYSIS_DEPTH);
	System.out.println("Above spec depth     " 
			   + ODPointerAnalysis.MAX_ANALYSIS_ABOVE_SPEC);

	System.out.println("Total number of allocation sites " 
			   + numberOfCreationSites);
	System.out.println("Total number of sites analyzed   " 
			   +  ODMAInfo.nStudiedNode +
			   "\t (" + (ODMAInfo.nStudiedNode*100.0)/numberOfCreationSites + "%)");
	int total_count = ODMAInfo.Nodes2Status.keySet().size();
	System.out.println("Total number of objects " + total_count);
	System.out.println("STACK  allocated " + stack_occurences
			   + "\t  (" + (stack_occurences*100/total_count)
			   + "%)");
	System.out.println("THREAD allocated " + thread_occurences
			   + "\t  (" + (thread_occurences*100/total_count)
			   + "%)");
	System.out.println("GLOBAL allocated " + global_occurences
			   + "\t  (" + (global_occurences*100/total_count)
			   + "%)");

	System.out.println("GENERATION OF MA INFO TIME  : " +
			   (time() - g_tstart) + "ms");
	System.out.println("Total number of MetaMethods:    " + allmms.size());
	System.out.println("Number of MetaMethods analyzed: " + 
			   ODPointerAnalysis.number_of_mm_analyzed +
			   " (" + 
			   ((ODPointerAnalysis.number_of_mm_analyzed*100.0)/allmms.size())
			   + "%)");
	System.out.println("Total number of call sites:    " + numberOfCallSites);
	System.out.println("Number of call sites analyzed: " +
			   ODPointerAnalysis.number_of_mapups + " ("+
			   ((ODPointerAnalysis.number_of_mapups*100.0)/numberOfCallSites)
			   + "%)");
	System.out.println("===============================");


	System.err.println("End of analyze_non_exception_nodes");
	if (isCoherent())
	    System.out.println("Was coherent leaving analyze_non_exception_nodes");
	else
	    System.out.println("Was INcoherent leaving analyze_non_exception_nodes");


	if(SHOW_DETAILS) { // show the allocation policies	    
	    System.out.println();
	    mainfo.print();
	    System.out.println("===================================");
	}

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
  
// 	System.exit(1);
    }



    private static int findNodes(long [] profile,
				 int [] top_quads,
				 long [] top_alloc,
				 int percentage)
    {
	// Computation of the number of non-null values 
	int n_nonnull = 0;
	int i;
	for(i=0; i<profile.length; i++)
	    if (profile[i]!=0) n_nonnull++;
	System.err.println("   (" + n_nonnull + " non null values)");

	for(i=0; i<n_nonnull;i++){
	    top_quads[i] = -1;
	    top_alloc[i] = -1;
	}

	int stored = 0;
	int total  = 0;
	for(i=0; i<profile.length; i++){
	    int j=0;
	    if (profile[i]==0) continue;
	    while((j<n_nonnull)&&(profile[i]<top_alloc[j])) j++;
	    if (j<n_nonnull){
		for(int k=stored-1; k>=j; k--){
		    top_quads[k+1] = top_quads[k];
		    top_alloc[k+1] = top_alloc[k];
		}
		top_quads[j] = i; 
		top_alloc[j] = profile[i];
		stored++;
		total += profile[i];
	    }
	}
	Util.assert(stored==n_nonnull,
	       "Error: n_nonnull = " + n_nonnull + 
	       "=\\=" + stored + " = stored.");
	
	int n_nodes = 0;
	Util.assert(percentage>=0, 
	       "Error: percentage should be nonnegative");
	Util.assert(percentage<=100, 
	       "Error: percentage should be less than 100");

	if (percentage==100)
	    n_nodes = n_nonnull;
	else {
	    float minimum = ((float)total*percentage)/100;
	    float current = 0;
	    System.err.println("Total = " + total);
	    System.err.println("Percentage = " + percentage);
	    System.err.println("Result should be at least = " + minimum);
	    for(n_nodes=1; 
		(n_nodes<=n_nonnull)&&(current<minimum)
		    &&(top_alloc[n_nodes-1]>FREQ_THRESHOLD); n_nodes++){
		current +=top_alloc[n_nodes-1];
	    }
	    n_nodes--;
	    System.err.println("Result is = " + current);
	    if (n_nodes>n_nonnull) n_nodes = n_nonnull;
	}

	return n_nodes;
    }




    private static void analyzeNodes(ODMAInfo mainfo,
				     int n_nodes,
				     int  [] top_quads_curr,
				     Quad [] id2Quad,
				     boolean tryThread,
				     ODNodeStatus [] theresults
				     )
    {
	long m_end, m_start;
	int beg_mapup, end_mapup;
	int beg_mm, end_mm;
	LightMap mm2nodes = new LightMap();
	float stack_occurences  = 0;
	float thread_occurences = 0;
	float global_occurences = 0;
	long alloc_count, sync_count;
	
	for(int i=0; i<n_nodes; i++){
	    m_start = System.currentTimeMillis();
	    int quadId = top_quads_curr[i];
	    Quad quad  = id2Quad[quadId];
	    QuadFactory qf = quad.getFactory();
	    HMethod hm = qf.getMethod();
	    MetaMethod mm = new MetaMethod(hm, true);
	    beg_mapup = ODPointerAnalysis.number_of_mapups;
	    beg_mm    = ODPointerAnalysis.number_of_mm_analyzed;
	    if (!pa.isAnalyzed(mm)){
		System.out.println("Now analyze " + mm);
		pa.analyze_intra_proc(mm);
	    }
	    PANode node = (PANode) pa.Quad2Node.get(quad);
	    if (node==null){
		System.err.println("\n**Node null for quad " + 
				   quad + " in mm " + mm + "\n"); 
		System.out.println("**Node null for quad " + 
				   quad + "\n in mm " + mm +
				   "with quad table " + pa.Quad2Node); 
	    }
	    mainfo.analyze_mm(mm,node,tryThread);
	    m_end = System.currentTimeMillis();
	    System.out.print("Node: " + node + "\t");
	    System.err.print("Node: " + node + "\t");
	    end_mapup = ODPointerAnalysis.number_of_mapups;
	    end_mm    = ODPointerAnalysis.number_of_mm_analyzed;
	    ODNodeStatus status =
		(ODNodeStatus) ODMAInfo.Nodes2Status.get(node);
	    if (status.onStack){
		if (tryThread) {
		    if (!status.memalloc_phase)
			status.syncelim_phase = true;
		}
		else if (!status.syncelim_phase)
		    status.memalloc_phase = true;
	    }
	    if (!tryThread) 
		status.touched_memalloc_phase = true;
	    else
		status.touched_syncelim_phase = true;
	    status.mapups  = end_mapup - beg_mapup;
	    status.methods = end_mm - beg_mm;
	    status.quad = quad;
	    if (status==null){
		System.err.println("Nodes with no status !!!");
		System.out.println("Nodes with no status !!! " + node);
		continue;
	    }
	    status.total_time += m_end-m_start;
	    if(tryThread)
		status. sync_time += m_end-m_start;
	    else
		status.alloc_time += m_end-m_start;
		

	    if (PRODUCTION_PROFILE){
		alloc_count = prod_alloc_profile[quadId];
 		if ((SYNC_INFO)&&(an.allocID(quad)<prod_sync_profile.length))
		    sync_count = prod_sync_profile[an.allocID(quad)];
		else
		    sync_count = 0;
	    }
	    else{
		alloc_count = alloc_profile[quadId];
 		if ((SYNC_INFO)&&(an.allocID(quad)<sync_profile.length))
		    sync_count = sync_profile[an.allocID(quad)];
		else
		    sync_count = 0;
	    }

	    if (status.onStack==true){
		status.stack   = alloc_count;
		status.synchro =  sync_count;
		stack_occurences += status.stack;
		System.out.print("Stack  " + status.stack);
		System.err.print("Stack  " + status.stack);
		System.out.print("\tSynchro  " + status.synchro);
		System.err.print("\tSynchro  " + status.synchro);
		if (status.onLocalHeap){
		    System.err.println("BUG in NodesStatus...");
		    System.out.println("BUG in NodesStatus..." + node);
		}
	    }
	    if  (status.onLocalHeap){
		status.synchro =  sync_count;
		if (status.nInlines!=0){
		    Util.assert(false);
		    if (status.nCallers==0){
			System.err.println("BUG in NodesStatus !!!");
			System.out.println("BUG in NodesStatus !!!" + node);
		    }
// 		    status.stack = (alloc_count*status.nInlines*1.0)/(status.nCallers);
		    stack_occurences += status.stack;
		    System.out.print("Stack  " + status.stack);
		    System.err.print("Stack  " + status.stack);
		    
// 		    status.thread = (alloc_count*(status.nCallers-status.nInlines)*1.0)
// 			/(status.nCallers);
		    thread_occurences += status.thread;
		    System.out.print("\tThread " + status.thread);
		    System.err.print("\tThread " + status.thread);
		    
		    System.out.print("\tInlining " + 
				     status.nInlines + "/" +
				     status.nCallers);
		    System.err.print("\tInlining " + 
				     status.nInlines + "/" +
				     status.nCallers);
		}
		else{
		    status.thread = alloc_count;
		    System.out.print("Thread " + status.thread);
		    System.err.print("Thread " + status.thread);
		    thread_occurences += status.thread;
		}
		System.out.print("\tSynchro  " + status.synchro);
		System.err.print("\tSynchro  " + status.synchro);
	    }
	    if ((!status.onStack)&&(!status.onLocalHeap)){
		if (status.nInlines!=0){
		    Util.assert(false);
		    if (status.nCallers==0){
			System.err.println("BUG in NodesStatus !!!");
			System.out.println("BUG in NodesStatus !!!" + node);
		    }
// 		    status.stack = (alloc_count*status.nInlines*1.0)/(status.nCallers);
		    System.out.print("Stack  " + status.stack);
		    System.err.print("Stack  " + status.stack);
		    stack_occurences += status.stack;
		    
// 		    status.global = (alloc_count
// 				     *(status.nCallers-status.nInlines)
// 				     *1.0)
// 			/(status.nCallers);
		    System.out.print("\tGlobal " + status.global);
		    System.err.print("\tGlobal " + status.global);
		    global_occurences += status.global;
		    System.out.print("\tInlining " + 
				     status.nInlines + "/" +
				     status.nCallers);
		    System.err.print("\tInlining " + 
				     status.nInlines + "/" +
				     status.nCallers);
		}
		else{
		    System.out.print("Global " + alloc_count);
		    System.err.print("Global " + alloc_count);
		    status.global = alloc_count;
		    global_occurences += alloc_count;
		}
		status.synchro =  sync_count;
		System.out.print("\tSynchro  0/" + sync_count);
		System.err.print("\tSynchro  0/" + sync_count);
	    }
	    if(tryThread){
		System.out.print("\ttime: " + status. sync_time + "ms.");
		System.err.print("\ttime: " + status. sync_time + "ms.");
	    }
	    else{
		System.out.print("\ttime: " + status.alloc_time + "ms.");
		System.err.print("\ttime: " + status.alloc_time + "ms.");
	    }
	    System.out.print("\tmapups " + status.mapups);
	    System.err.print("\tmapups " + status.mapups);
	    System.out.println("\tmethods " + status.methods);
	    System.err.println("\tmethods " + status.methods);
	    status.node = node;
	    theresults[i] = status;
	}
    }

    private static void analyzeCallSites(ODMAInfo mainfo,
					 int n_sites,
					 int  [] top_quads_curr,
					 Quad [] id2QuadAlloc,
					 Quad [] id2QuadCS,
					 ODNodeStatus [] theresults
					 )
    {
	long m_end, m_start;
	int beg_mapup, end_mapup;
	int beg_mm, end_mm;
	LightMap mm2nodes = new LightMap();
	float stack_occurences  = 0;
	float thread_occurences = 0;
	float global_occurences = 0;
	long alloc_count, sync_count;
	int stored = 0;
	int unknowns = 0;
	
	for(int i=0; i<n_sites; i++){
	    // In fact, already processed
	    if(theresults[i]!=null) {
		System.out.println("Already processed");
		continue;
	    }
	    m_start = System.currentTimeMillis();
	    int Id = top_quads_curr[i];
	    Quad quadAlloc = id2QuadAlloc[call_profile_alloc[Id]];
	    int quadCSid = call_profile_call[Id];
	    if (quadCSid==0) {
		System.err.println("Caller unknown");
		System.out.println("Caller unknown");
		unknowns++;
		continue;
	    }
	    Quad quadCS    = id2QuadCS[quadCSid-1];
	    CALL cs = (CALL) quadCS;
	    QuadFactory qf_caller = quadCS.getFactory();
	    QuadFactory qf_callee = quadAlloc.getFactory();
	    HMethod hm_caller = qf_caller.getMethod();
	    HMethod hm_callee = qf_callee.getMethod();
	    MetaMethod mm_caller = new MetaMethod(hm_caller, true);
	    MetaMethod mm_callee = new MetaMethod(hm_callee, true);

	    System.out.println("Callees ");
	    MetaMethod[] callees = mcg.getCallees(mm_caller, cs);
	    for(int j=0; j<callees.length; j++)
		System.out.println(callees[j]);
	    System.out.println("The callee " + mm_callee);
	    
	    Set otherNodes = new HashSet();
	    Set indexes    = new HashSet();
	    for(int j=i+1; j<n_sites; j++){
		int quadbisid = call_profile_call[top_quads_curr[j]];
		if (quadbisid==0) continue;
		if(id2QuadCS[quadbisid-1]==quadCS){
		    indexes.add(new Integer(top_quads_curr[j]));
		    otherNodes.add(id2QuadAlloc[call_profile_alloc[top_quads_curr[j]]]);
		}
	    }
	    beg_mapup = ODPointerAnalysis.number_of_mapups;
	    beg_mm    = ODPointerAnalysis.number_of_mm_analyzed;
// 	    if (!pa.isAnalyzed(mm_caller)){
// 		System.out.println("Now analyze " + mm_caller);
// 		pa.analyze_intra_proc(mm_caller);
// 	    }
	    if (!pa.isAnalyzed(mm_callee)){
		System.out.println("Now analyze " + mm_callee);
		pa.analyze_intra_proc(mm_callee);
	    }
	    
	    // Selection of the nodes we are trying to stack allocate
	    // by inlining the current call site.
	    PANode node = (PANode) pa.Quad2Node.get(quadAlloc);
	    Util.assert(node!=null, "\n**Node null for quad " + 
			quadAlloc + " in mm " + mm_callee + "\n");
	    Set allTheNodes = new HashSet();
	    allTheNodes.add(node);

	    // All the other nodes
	    for(Iterator on_it=otherNodes.iterator(); on_it.hasNext(); ){
		Quad  on_q = (Quad) on_it.next();
		PANode on = (PANode) pa.Quad2Node.get(on_q);
		Util.assert(on!=null, "\n**Node null for quad " + 
			    on_q + " in mm " + mm_callee + "\n");
		allTheNodes.add(on);
	    }

	    // We try to stack allocate the node as the work we are
	    // going to do here will be done in any case by the
	    // inlining. We take the chance to do better at lower
	    // cost!
	    for(Iterator n_it=allTheNodes.iterator(); n_it.hasNext(); )
		mainfo.analyze_mm(mm_callee,(PANode) n_it.next(),false);

	    System.err.println(allTheNodes);
	    Set benefactors = new HashSet(); 
	    mainfo.generate_inlining_hints(mm_callee, 
					   allTheNodes, benefactors, 
					   mm_caller, cs);
	    m_end = System.currentTimeMillis();
	    end_mapup = ODPointerAnalysis.number_of_mapups;
	    end_mm    = ODPointerAnalysis.number_of_mm_analyzed;

	    for(Iterator n_it=allTheNodes.iterator(); n_it.hasNext(); ){
		PANode n = (PANode) n_it.next();
		System.out.print("Node: " + n + "\t");
		System.err.print("Node: " + n + "\t");

		// Creation of the structure used to stored the result
		// of the tentative of inlining
		ODNodeStatus status = new ODNodeStatus();
		status.node = n;

		// Structure used to store all the data about the
		// current node.
		boolean new_nodestatus;
		ODNodeStatus nodestatus = (ODNodeStatus) ODMAInfo.Nodes2Status.get(n);
		if(nodestatus==null){
		    // This is the first time we try to optimize this
		    // node. We create the relevant data structure.
		    nodestatus = new ODNodeStatus();
		    nodestatus.node = n;
		    ODMAInfo.Nodes2Status.put(n, nodestatus);
		    new_nodestatus = true;
		}
		else{
		    new_nodestatus = false;
		    if ((nodestatus.onStack)&&
			(!nodestatus.memalloc_phase)&&
			(!nodestatus.syncelim_phase))
			{
			    nodestatus.inlining_phase = true;
			}
		}

		// Computing the index of the current node
		if(node==n){
		    status.index = top_quads_curr[i];
		}
		else{
		    boolean found = false;
		    for(Iterator k_it=indexes.iterator(); k_it.hasNext() && !found; ){
			int k = ((Integer) k_it.next()).intValue();
			PANode tmp_n = (PANode) 
			    pa.Quad2Node.get(id2QuadAlloc[call_profile_alloc[k]]);
			Util.assert(tmp_n!=null);
			if (tmp_n==n){
			    found = true;
			    status.index = k;
			}
		    }
		    Util.assert(found);
		}
		//This field is the allocation quad
		status.quad = id2QuadAlloc[call_profile_alloc[status.index]];
		if(new_nodestatus)
		    nodestatus.quad = status.quad;

		if(node==n){
		    // All the computational time is considered to be
		    // for the main node...
		    status.total_time = m_end-m_start;
		    status.alloc_time = m_end-m_start;
		    status.mapups  = end_mapup - beg_mapup;
		    status.methods = end_mm - beg_mm;
		}
		else{
		    status.alloc_time = 0;
		    status.total_time = 0;
		    status.mapups  = 0;
		    status.methods = 0;
		}

		alloc_count = 0;
		if (PRODUCTION_PROFILE){
		    boolean idfound=false;
		    int k;
		    for(k=0; (k<prod_call_profile_alloc.length)&&(!idfound); k++){
			if((prod_call_profile_alloc[k]==call_profile_alloc[status.index])&&
			   (prod_call_profile_call [k]==call_profile_call [status.index])){
			    idfound = true;
			    alloc_count = prod_call_profile_count[k];
			}
		    }
		    Util.assert(idfound);

		    if(new_nodestatus)
			nodestatus.global = 
			    prod_alloc_profile[call_profile_alloc[status.index]];
		}
		else{
		    alloc_count = call_profile_count[status.index];

		    if(new_nodestatus)
			nodestatus.global = 
			    alloc_profile[call_profile_alloc[status.index]];
		}

		if ((benefactors.contains(n))||(nodestatus.inlining_phase)){
		    status.stack   = alloc_count;
		    status.synchro =  0;
		    if(nodestatus.onLocalHeap)
			nodestatus.thread -= alloc_count;
		    else
			nodestatus.global -= alloc_count;
		    nodestatus.stack  += alloc_count;
		    stack_occurences += status.stack;
		    System.out.print("Stack  " + status.stack);
		    System.err.print("Stack  " + status.stack);
		}
		else{
		    System.out.print("Global " + alloc_count);
		    System.err.print("Global " + alloc_count);
		    status.global = alloc_count;
		    global_occurences += alloc_count;
		}

		System.out.print("\ttime: " + status.total_time + "ms.");
		System.err.print("\ttime: " + status.total_time + "ms.");

		System.out.print("  mapups " + status.mapups);
		System.err.print("  mapups " + status.mapups);
		System.out.println("  methods " + status.methods);
		System.err.println("  methods " + status.methods);

		theresults[stored] = status;
		stored++;
	    }
	}
	Util.assert(stored+unknowns==n_sites,stored + " values stored, instead of " + n_sites);
    }


    private static void printing(ODNodeStatus[] theresults,
				 boolean alloc)
    {
// 	double tstack  = 0;
	double tthread = 0;
	double tglobal = 0;
	long   ttime = 0;
// 	long   tsync = 0;
	int tmapups  = 0;
	int tmethods = 0;
	for(int i=0; i<theresults.length; i++ ){
	    ODNodeStatus status = theresults[i];
	    System.out.print("Node " + status.node + "\t");
	    if (status.onStack){
		if ((alloc&&status.memalloc_phase)||
		    ((!alloc)&&(status.syncelim_phase))){
			System.out.print(" Stack  " + status.stack + "\t");
			tstack  += status.stack;
		}
		else
		    System.out.print("Already optimized");
	    }
	    else{
		if(status.onLocalHeap){
		    System.out.print(" Thread " + status.thread + "\t");
		    tthread += status.thread;
		}
		else{
		    System.out.print(" Global " + 
				     (status.stack + status.thread + status.global) 
				     + "\t");
		    tglobal += status.stack + status.thread + status.global;
		}
	    }

	    if (alloc||(!status.touched_memalloc_phase))
		mem_in_prog += status.stack + status.thread + status.global;

	    if(alloc){
		if(status.memalloc_phase){
		    System.out.print("Sync " + status.synchro);
		    tsync += status.synchro;
		    sync_in_prog += status.synchro;
		}
		else{
		    System.out.print("Sync " + "0/" + status.synchro);
		    sync_in_prog += status.synchro;
		}
	    }
	    else{
		if(((status.onStack)&&(status.syncelim_phase))||(status.onLocalHeap)){
		    System.out.print("Sync " + status.synchro);
		    tsync += status.synchro;
		    if (!status.touched_memalloc_phase)
			sync_in_prog += status.synchro;
		}
		else if (!status.onStack){
		    System.out.print("Sync " + 0 + "/" + status.synchro);
		    if (!status.touched_memalloc_phase)
			sync_in_prog += status.synchro;
		}
		else{
		    System.out.print("Already optimized");
		}
	    }

	    if(alloc){
		System.out.print(" Time " + status.alloc_time + "ms\n");
		ttime   += status.alloc_time;
	    }
	    else {
		System.out.print(" Time " + status.sync_time + "ms\n");
		ttime   += status.sync_time;
	    }

// 	    if (alloc||(!status.touched_memalloc_phase)){
// 		tstack  += status.stack;
// 		tthread += status.thread;
// 		tglobal += status.global;
// 	    }
// 	    else{
// 		tglobal -= status.thread;
// 		tthread += status.thread;
// 	    }

	    tmapups  += status.mapups;
	    tmethods += status.methods;
	    System.out.print("Stack  " + tstack + "/" + mem_in_prog + "\t");
	    System.out.print("Thread " + tthread + "/" + mem_in_prog + "\t");
	    System.out.print("Global " + tglobal + "/" + mem_in_prog + "\t");
	    System.out.print("Sync " + tsync + "/" + sync_in_prog + "\t");
	    System.out.print("Time " + ttime + "\t");
	    System.out.print("MapUps " + tmapups + "\t");
	    System.out.println("Methods " + tmethods + "\t");

	    System.out.println("<tr>");
	    System.out.print("<td align=center> " + status.node + "</td> \n");
	    
	    if (status.onStack){
		if ((alloc&&status.memalloc_phase)||
		    ((!alloc)&&(status.syncelim_phase))){
		    System.out.print("<td align=center> " 
				     + status.stack + "/" + 
				     (status.stack + status.thread + status.global) 
				     + "</td> \n");
		}
		else
		System.out.print("<td align=center> Already optimized</td> \n");
	    }
	    else{
		if(status.onLocalHeap){
		    System.out.print("<td align=center> " 
				     + "0/" + 
				     (status.stack + status.thread + status.global) 
				     + "</td> \n");
		}
		else{
		    System.out.print("<td align=center> " 
				     + "0/" + 
				     (status.stack + status.thread + status.global) 
				     + "</td> \n");
		}
	    }
	    

	    System.out.print("<td align=center> " );


	    if(alloc){
		if(status.memalloc_phase){
		    System.out.print(status.synchro + "/" + status.synchro);
		}
		else{
		    System.out.print("0/" + status.synchro);
		}
	    }
	    else{
		if(((status.onStack)&&(status.syncelim_phase))||(status.onLocalHeap)){
		    System.out.print(status.synchro + "/" + status.synchro);
		}
		else if (!status.onStack){
		    System.out.print("0/" + status.synchro);
		}
		else{
		    System.out.print("Already optimized");
		}
	    }
	    System.out.print("</td> \n");
	    System.out.print("<td align=center> " );
	    if(alloc){
		System.out.print(status.alloc_time + "</td>\n");
	    }
	    else {
		System.out.print(status.sync_time + "</td>\n");
	    }
	    System.out.print("<td align=center> " + tmapups + "</td>\n");
	    System.out.print("<td align=center> " + tmethods + "</td>\n");
	    System.out.print("<td align=center> " + tstack + "/" + mem_in_prog + "</td>\n");
	    System.out.print("<td align=center> " + tsync + "/" + sync_in_prog + "</td>\n");
	    System.out.print("<td align=center> " + ttime + "</td>\n</tr>\n");
	}
// 	rem_sync_in_prog += tsync;
// 	stck_mem_in_prog += sync_in_prog;
    }

    private static void printing_inlining(ODNodeStatus[] theresults)
    {
	double tthread = 0;
	double tglobal = 0;
	long   ttime = 0;
	int tmapups  = 0;
	int tmethods = 0;
	for(int i=0; i<theresults.length; i++ ){
	    ODNodeStatus status = theresults[i];
	    ODNodeStatus gen_status = 
		(ODNodeStatus) ODMAInfo.Nodes2Status.get(status.node);
	    System.out.print("Node " + status.node + "\t");

	    if ((gen_status.onStack)&&
		((gen_status.touched_memalloc_phase)||
		 (gen_status.touched_syncelim_phase)))
		{
		    System.out.print("Already optimized ");
		    System.out.print("Already optimized ");
		}
	    else{
		System.out.print("gen_status.onStack " + gen_status.onStack);
		if (status.stack!=0){
		    System.out.print(" Stack  " + status.stack + "\t");
		}
		if (status.thread!=0) System.out.print(" Thread " 
						       + status.thread + "\t");
		if (status.global!=0) System.out.print(" Global " 
						   + status.global + "\t");

		if ((!gen_status.touched_memalloc_phase)&&
		    (!gen_status.touched_syncelim_phase))
		    mem_in_prog += status.stack + status.thread + status.global;
	    }
		   
	    System.out.print(" Time " + status.alloc_time + "ms\n");
	    ttime   += status.alloc_time;

	    if ((!status.touched_syncelim_phase)||(!status.touched_memalloc_phase)){
		tstack  += status.stack;
		tthread += status.thread;
		tglobal += status.global;
	    }
	    else{
		tglobal -= status.stack;
		tthread += status.stack;
	    }

	    tmapups  += status.mapups;
	    tmethods += status.methods;
	    System.out.print("Stack  " + tstack + "/" + mem_in_prog + "\t");
	    System.out.print("Thread " + tthread + "/" + mem_in_prog + "\t");
	    System.out.print("Global " + tglobal + "/" + mem_in_prog + "\t");
	    System.out.print("Sync " + tsync + "/" + sync_in_prog + "\t");
	    System.out.print("Time " + ttime + "\t");
	    System.out.print("MapUps " + tmapups + "\t");
	    System.out.println("Methods " + tmethods + "\t");

	    System.out.println("<tr>");
	    System.out.print("<td align=center> " + status.node + "</td> \n");
	    if ((gen_status.onStack)&&
		((gen_status.touched_memalloc_phase)||
		 (gen_status.touched_syncelim_phase)))
		{
		System.out.print("<td align=center> Already optimized</td> \n");
		System.out.print("<td align=center> Already optimized</td> \n");
		}
	    else{
		if (status.stack!=0){
		    System.out.print("<td align=center> " 
				     + status.stack + "/" + 
				     (status.stack + status.thread + status.global) 
				     + "</td> \n");
		    System.out.print("<td align=center> - </td>" );
		}
		else{
		    System.out.print("<td align=center> " 
				     + 0 + "/" + 
				     (status.stack + status.thread + status.global) 
				     + "</td> \n");
		    System.out.print("<td align=center> - </td>" );
		}
	    }

	    System.out.print("<td align=center> " );
	    System.out.print(status.alloc_time + "</td>\n");
	    System.out.print("<td align=center> " + tmapups + "</td>\n");
	    System.out.print("<td align=center> " + tmethods + "</td>\n");
	    System.out.print("<td align=center> " + tstack + "/" + mem_in_prog + "</td>\n");
	    System.out.print("<td align=center> " + tsync + "/" + sync_in_prog + "</td>\n");
	    System.out.print("<td align=center> " + ttime + "</td>\n</tr>\n");
	}
// 	rem_sync_in_prog += tsync;
// 	stck_mem_in_prog += sync_in_prog;
    }


//     private static void printing(ODNodeStatus[] theresults,
// 				 boolean alloc)
//     {
// 	double tstack  = 0;
// 	double tthread = 0;
// 	double tglobal = 0;
// 	long   ttime = 0;
// 	long   tsync = 0;
// 	int tmapups  = 0;
// 	int tmethods = 0;
// 	int sync_in_prog = 0;
// 	int mem_in_prog  = 0;
// 	for(int i=0; i<theresults.length; i++ ){
// 	    ODNodeStatus status = theresults[i];
// 	    System.out.print("Node " + status.node + "\t");
// 	    if (status.stack!=0)  System.out.print(" Stack  " 
// 						   + status.stack + "\t");
// 	    if (status.thread!=0) System.out.print(" Thread " 
// 						   + status.thread + "\t");
// 	    if (status.global!=0) System.out.print(" Global " 
// 						   + status.global + "\t");
// 	    mem_in_prog += status.stack + status.thread + status.global;
// 	    if(alloc){
// // 		if(status.onStack){
// 		    System.out.print("Sync " + status.synchro);
// 		    tsync += status.synchro;
// // 		}
// // 		else
// // 		    System.out.print("Sync " + 0);
// 	    }
// 	    else{
// 		if((status.onStack)||(status.onLocalHeap))
// 		    System.out.print("Sync " + 0 + "/" + status.synchro);
// 		else{
// 		    System.out.print("Sync " + status.synchro);
// 		    tsync += status.synchro;
// 		}
// 	    }
// 	    sync_in_prog += status.synchro;
// 	    if(alloc){
// 		System.out.print(" Time " + status.alloc_time + "ms\n");
// 		ttime   += status.alloc_time;
// 	    }
// 	    else {
// 		System.out.print(" Time " + status.sync_time + "ms\n");
// 		ttime   += status.sync_time;
// 	    }
// 	    tstack  += status.stack;
// 	    tthread += status.thread;
// 	    tglobal += status.global;
// 	    tmapups  += status.mapups;
// 	    tmethods += status.methods;
// 	    System.out.print("Stack  " + tstack + "/" + mem_in_prog + "\t");
// 	    System.out.print("Thread " + tthread + "/" + mem_in_prog + "\t");
// 	    System.out.print("Global " + tglobal + "/" + mem_in_prog + "\t");
// 	    System.out.print("Sync " + tsync + "/" + sync_in_prog + "\t");
// 	    System.out.print("Time " + ttime + "\t");
// 	    System.out.print("MapUps " + tmapups + "\t");
// 	    System.out.println("Methods " + tmethods + "\t");
// 	}
//     }


    private static void find_interesting_nodes(){
	// Necessary allocations
	if (isCoherent())
	    System.out.println("Was coherent entering find_interesting_nodes");
	else
	    System.out.println("Was INcoherent entering find_interesting_nodes");

	boolean allfound = true;
	boolean sndfound = true;
	Set TheQuads = an.alloc2int.keySet();
	for(Iterator quad_it= TheQuads.iterator(); quad_it.hasNext(); ){
	    Quad quad = (Quad) quad_it.next();
	    QuadFactory qf = quad.getFactory();
	    HMethod hm = qf.getMethod();
	    LBBConverter mylbbconv = pa.scc_lbb_factory.getLBBConverter();
	    LightBasicBlock.Factory mylbbf = mylbbconv.convert2lbb(hm);
	    HCode hc = mylbbf.getHCode();
	    boolean found = false;
	    for(Iterator q_it=hc.getElementsI(); (q_it.hasNext())&&(!found); ){
		Quad q = (Quad) q_it.next();
		if (quad.equals(q)) found = true;
	    }
	    if (found==false)
		allfound = false;

	    found = false;
	    SCComponent scc = 
		pa.scc_lbb_factory.computeSCCLBB(hm).getFirst();
	    LightBasicBlock first_bb = (LightBasicBlock)scc.nodes().next();
	    HEADER first_hce = (HEADER) first_bb.getElements()[0];
	    METHOD m  = (METHOD) first_hce.next(1);
	    while(scc != null){
		PAWorkList W_intra_proc = new PAWorkList();
		W_intra_proc.addAll(scc.nodeSet());
		while(!W_intra_proc.isEmpty()){
		    LightBasicBlock lbb = (LightBasicBlock) W_intra_proc.remove();
		    HCodeElement[] instrs = lbb.getElements();
		    int len = instrs.length;
		    for(int i = 0; i < len; i++){
			Quad q = (Quad) instrs[i];
			if (quad.equals(q)) found = true;
		    }
		}
		scc = scc.nextTopSort();
	    }
	    if (found==false)
		sndfound = false;
	} 
	if (allfound)
	    System.err.println("All Quads found  in find_interesting_nodes");
	else
	    System.err.println("Missing Quads !!!!!!!! in find_interesting_nodes");
	if (sndfound)
	    System.err.println("All Quads found  in find_interesting_nodes");
	else
	    System.err.println("Missing Quads !!!!!!!! in find_interesting_nodes");

	
	// Initializations...
	MetaCallGraph mcg = pa.getMetaCallGraph();
	Set allmms = mcg.getAllMetaMethods();	

	// Computation of the number of creation sites
	int numberOfCreationSites = 0;
	for(Iterator it = allmms.iterator(); it.hasNext(); ) {
	    MetaMethod mm = (MetaMethod) it.next();
	    if(pa.analyzable(mm.getHMethod()))
		numberOfCreationSites += pa.count_creation_sites(mm);
	}
	// Computation of the number of possible mapups
	int numberOfCallSites = 0;
	for(Iterator it = allmms.iterator(); it.hasNext(); ) {
	    MetaMethod mm = (MetaMethod) it.next();
	    for(Iterator cit = mcg.getCallSites(mm).iterator(); cit.hasNext(); ) {
		CALL cs = (CALL) cit.next();
		MetaMethod[] callees = mcg.getCallees(mm, cs);
		numberOfCallSites += callees.length;
	    }
	}



	//***************************
	// Beginning of time !!!!!!!!
	//***************************
	g_tstart = time();

	ODPointerAnalysis.interestingQuads = new HashSet();
	ODPointerAnalysis.interestingNodes = new HashSet();
	ODPointerAnalysis.Quad2Nodes = new LightRelation();

	System.err.println("Beginning of find_interesting_nodes");
	
	// Allocation of the hash tables used to store the results of
	// the analyzes
	pa.hash_proc_int_d = new HashMap[1+pa.MAX_ANALYSIS_DEPTH];
	pa.hash_proc_ext_d = new HashMap[1+pa.MAX_ANALYSIS_DEPTH];

	for(int i = 0; i< 1+pa.MAX_ANALYSIS_DEPTH; i++) {
	    pa.hash_proc_int_d[i] =  new HashMap();
	    pa.hash_proc_ext_d[i] =  new HashMap();
	}

	ODMAInfo mainfo = 
	    new ODMAInfo(pa, hcf, allmms, USE_INTER_THREAD,
			 DO_STACK_ALLOCATION,
			 DO_THREAD_ALLOCATION,
			 GEN_SYNC_FLAG);
	ODMAInfo.Nodes2Status = new LightMap();
	ODPointerAnalysis.current_analysis_depth=1;
	if (ELIM_SYNCOPS) ODMAInfo.SYNC_ELIM = true;
	ODMAInfo.MEM_OPTIMIZATION = ODPAMain.MEMORY_OPTIMIZATION;


	// Building the numeric identifier to quad table.
	Set allTheQuads = an.alloc2int.keySet();
	Quad [] id2Quad = new Quad[alloc_profile.length+1];
	for(Iterator quad_it= allTheQuads.iterator(); quad_it.hasNext(); ){
	    Quad quad = (Quad) quad_it.next();
	    id2Quad[an.allocID(quad)] = quad;
	}
	System.err.println("Size of allTheQuads: " + allTheQuads.size());


	//
	// Tentative stack allocation of the most allocated creation
	// sites
	//
	ODNodeStatus [] theresults_alloc = null;
	int n_nodes_alloc = 0;
	if (ODMAInfo.MEM_OPTIMIZATION){
	    System.err.println("\nStack allocation");
	    Util.assert(alloc_profile!=null,
			"find_interesting_nodes called with null profile");
	    Util.assert(an!=null,
			"find_interesting_nodes called with null an");

	    int  [] top_quads_alloc = new int  [alloc_profile.length];
	    long [] top_count_alloc = new long [alloc_profile.length];
	    
	    for(int i=0; i<alloc_profile.length; i++)
		System.err.print(alloc_profile[i] + " ");
	    System.err.println("");

	    n_nodes_alloc = findNodes(alloc_profile,
				      top_quads_alloc,
				      top_count_alloc,
				      ODPAMain.WHO_TO_ANALYZE);

	    System.err.println("Top values: ");
	    for(int i=0; i<n_nodes_alloc; i++)
		System.err.print(top_count_alloc[i] + " ");
	    System.err.println("");

	    theresults_alloc = new ODNodeStatus[n_nodes_alloc];

	    analyzeNodes(mainfo,
			 n_nodes_alloc,
			 top_quads_alloc,
			 id2Quad,
			 false,
			 theresults_alloc
			 );
	}
	

	//
	// Tentative thread allocation of the most synchronized on
	// creation sites
	//
	ODNodeStatus [] theresults_sync = null;
	int n_nodes_sync = 0;
	int [] top_quads_sync = null;

	if((ODMAInfo.SYNC_ELIM)&&(SYNC_INFO)){
	    System.err.println("\nSynchronization elimination");

	    for(int i=0; i<sync_profile.length; i++)
		System.err.print(sync_profile[i] + " ");
	    System.err.println("");
	    
	    top_quads_sync = new int  [sync_profile.length];
	    long [] top_count_sync = new long [sync_profile.length];
	    
	    n_nodes_sync = findNodes(sync_profile,
				     top_quads_sync, top_count_sync,
				     ODPAMain.WHO_TO_ANALYZE);
	    
	    System.err.println("Top values: ");
	    for(int i=0; i<n_nodes_sync; i++)
		System.err.print(top_count_sync[i] + " ");
	    System.err.println("");
	    
	    theresults_sync = new ODNodeStatus[n_nodes_sync];

	    analyzeNodes(mainfo,
			 n_nodes_sync,
			 top_quads_sync,
			 id2Quad,
			 true,
			 theresults_sync
			 );
	}


	//
	// Tentative inlining of the call sites resposible to the
	// larger number of dynamic object allocations.
	//
	ODNodeStatus [] theresults_cs = null;
	int n_cs = 0;
	if((ODMAInfo.MEM_OPTIMIZATION)&&(CALL_INFO)){
	    System.err.println("\nInlining");

	    for(int i=0; i<call_profile_count.length; i++)
		System.err.print(call_profile_count[i] + " ");
	    System.err.println("");


	    int  [] top_quads_cs = new int  [call_profile_alloc.length];
	    long [] top_count_cs = new long [call_profile_alloc.length];
	    long [] profile_cs   = new long [call_profile_alloc.length];

	    n_cs = findNodes(call_profile_count,
			     top_quads_cs, top_count_cs,
			     ODPAMain.WHO_TO_ANALYZE);

	    System.err.println("Top values: ");
	    for(int i=0; i<n_cs; i++)
		System.err.print(top_count_cs[i] + " ");
	    System.err.println("");

	    // Building the numeric identifier to quad callsite table.
	    Set allTheQuadsCS = an.call2int.keySet();
	    int max = 0;
	    for(Iterator quad_it= allTheQuadsCS.iterator(); quad_it.hasNext(); ){
		int id = an.callID((Quad) quad_it.next());
		if (max<id) max = id;
	    }
	    Quad [] id2QuadCS = new Quad[1+max];
	    for(Iterator quad_it= allTheQuadsCS.iterator(); quad_it.hasNext(); ){
		Quad quad = (Quad) quad_it.next();
		id2QuadCS[an.callID(quad)] = quad;
	    }

	    for(int i=0; i<call_profile_count.length; i++){
		System.out.print(call_profile_alloc[i] + "  " +
				 (call_profile_call[i]-1) + " = " +
				 call_profile_count[i]);
		System.out.print("\nAlloc ID " + call_profile_alloc[i]);
		Quad quadAlloc = id2Quad[call_profile_alloc[i]];
		QuadFactory qf_callee = quadAlloc.getFactory();
		HMethod hm_callee = qf_callee.getMethod();
		System.out.println(" " + hm_callee);
		System.out.print("Quad  ID " + (call_profile_call[i]-1));
		int quadCSid = call_profile_call[i];
		if(quadCSid==0){
		    System.out.println("Unknown caller");
		    continue;
		}
		Quad quadCS    = id2QuadCS[quadCSid-1];
		CALL cs = (CALL) quadCS;
		QuadFactory qf_caller = quadCS.getFactory();
		HMethod hm_caller = qf_caller.getMethod();
		MetaMethod mm_caller = new MetaMethod(hm_caller, true);
		MetaMethod[] callees = mcg.getCallees(mm_caller, cs);
		for(int j=0; j<callees.length; j++)
		    System.out.println(callees[j]);
	    }


	    theresults_cs = new ODNodeStatus[n_cs];
	    
	    analyzeCallSites(mainfo, n_cs,
			     top_quads_cs,
			     id2Quad,
			     id2QuadCS,
			     theresults_cs
			     );
	}

	



	// Inlining
	if(mainfo.DO_METHOD_INLINING) {
	    System.err.println("Inlining...");
	    System.out.println("Inlining...");
	    mainfo.do_the_inlining();
	}

	// Thread heap allocation
	for(Iterator it = allmms.iterator(); it.hasNext(); ) {
	    MetaMethod mm = (MetaMethod) it.next();
	    if(pa.analyzable(mm.getHMethod())){
		pa.make_thread_heap(mm, mainfo);
	    }
	}

	long g_tend = time();

	double tstack  = 0;
	double tthread = 0;
	double tglobal = 0;
	long   ttime = 0;
	int tmapups  = 0;
	int tmethods = 0;
	//
	// Printing out the statistics
	//
	System.out.println("===============================");
	if (ODMAInfo.MEM_OPTIMIZATION){
	    System.out.println("Stack allocation");
	    printing(theresults_alloc, true);
	}
	if((ODMAInfo.SYNC_ELIM)&&(SYNC_INFO)){
	    System.out.println("\nSynchronization elimination");
	    printing(theresults_sync, false);
	}
	if((ODMAInfo.MEM_OPTIMIZATION)&&(CALL_INFO)){
	    System.out.println("\nInlining");
	    printing_inlining(theresults_cs);
	}

	System.out.println("===============================");
	System.out.println("Specialization depth " 
			   + ODPointerAnalysis.MAX_SPEC_DEPTH);
	System.out.println("Analysis depth       " 
			   + ODPointerAnalysis.MAX_ANALYSIS_DEPTH);
	System.out.println("Above spec depth     " 
			   + ODPointerAnalysis.MAX_ANALYSIS_ABOVE_SPEC);

	int total_count_alloc = 0;
	int total_count_sync = 0;
	if (PRODUCTION_PROFILE){
	    System.out.println("PRODUCTION_PROFILE");
	    for(int i=0; i<prod_alloc_profile.length; i++) 
		total_count_alloc += prod_alloc_profile[i];
	    if(SYNC_INFO)
		for(int i=0; i<prod_sync_profile.length; i++) 
		    total_count_sync += prod_sync_profile[i];
	}
	else{
	    System.out.println("no... PRODUCTION_PROFILE");
	    for(int i=0; i<alloc_profile.length; i++) 
		total_count_alloc += alloc_profile[i];
	    if(SYNC_INFO)
		for(int i=0; i<sync_profile.length; i++) 
		    total_count_sync += sync_profile[i];
	    
	}
// 	if (PRODUCTION_PROFILE){
// 	    System.out.println("PRODUCTION_PROFILE");
// 	    for(int i=0; i<n_nodes_alloc; i++) 
// 		total_count_alloc += prod_alloc_profile[top_quads_alloc[i]];
// 	    if(SYNC_INFO)
// 		for(int i=0; i<n_nodes_sync; i++) 
// 		    total_count_sync += prod_sync_profile[top_quads_sync[i]];
// 	}
// 	else{
// 	    System.out.println("no... PRODUCTION_PROFILE");
// 	    for(int i=0; i<n_nodes_alloc; i++) 
// 		total_count_alloc += top_count_alloc[i];
// 	    if(SYNC_INFO)
// 		for(int i=0; i<n_nodes_sync; i++) 
// 		    total_count_sync += sync_profile[top_quads_sync[i]];
	    
// 	}
	long stack_occurences  = 0;
	long thread_occurences = 0;
	long global_occurences = 0;
	long sync_occurences   = 0;
	for(Iterator it=ODMAInfo.Nodes2Status.values().iterator(); it.hasNext(); ){
	    ODNodeStatus status = (ODNodeStatus) it.next();
	    // This means that this node was just implied in inlining...
	    if(status.quad==null) continue;
	    int id = an.allocID(status.quad);
	    stack_occurences  += status.stack;
	    thread_occurences += status.thread;
	    if((!status.onStack)&&(!status.onLocalHeap))
		sync_occurences   += status.synchro;

// 	    if(status.onStack){
// 		if (PRODUCTION_PROFILE){
// 		    stack_occurences +=  prod_alloc_profile[id];
// 		    sync_occurences  +=  prod_sync_profile[id];
// 		}
// 		else {
// 		    stack_occurences +=  alloc_profile[id];
// 		    sync_occurences  +=   sync_profile[id];
// 		}
// 	    }
// 	    if(status.onLocalHeap){
// 		if (PRODUCTION_PROFILE){
// 		    thread_occurences +=  prod_alloc_profile[id];
// 		    sync_occurences   +=  prod_sync_profile[id];
// 		}
// 		else {
// 		    thread_occurences +=  alloc_profile[id];
// 		    sync_occurences   +=   sync_profile[id];
// 		}
// 	    }
// 	    if ((!status.onStack)&&(!status.onLocalHeap)){
// 		if (PRODUCTION_PROFILE){
// 		    global_occurences +=  prod_alloc_profile[id];
// 		}
// 		else {
// 		    global_occurences +=  alloc_profile[id];
// 		}
// 	    }
	}		

// 	for(int i=0; i<theresults_cs.length; i++){
// 	    ODNodeStatus status = theresults_cs[i];
// 	    int id = status.index;
// 	    stack_occurences  += status.stack;
// 	    global_occurences += status.global;
// 	}		

	int n_analyzed = ODMAInfo.Nodes2Status.values().size();
	System.out.println("Total number of allocation sites " 
			   + numberOfCreationSites);
	System.out.println("Total number of sites analyzed   " 
			   + n_analyzed +
			   "\t (" + (n_analyzed*100.0)/numberOfCreationSites + "%)");
	System.out.println("Total number of objects allocated " + total_count_alloc);
	System.out.println("STACK  allocated " + stack_occurences
			   + "\t  (" + (stack_occurences*100/total_count_alloc)
			   + "%)");
	System.out.println("THREAD allocated " + thread_occurences
			   + "\t  (" + (thread_occurences*100/total_count_alloc)
			   + "%)");
	global_occurences = total_count_alloc - thread_occurences - stack_occurences;
	System.out.println("GLOBAL allocated " + global_occurences
			   + "\t  (" + (global_occurences*100/total_count_alloc)
			   + "%)");
	System.out.println("Total number of synchronizations " + total_count_sync);
	System.out.println("SYNCHRO eliminated " + sync_occurences
			   + "\t  (" + (sync_occurences*100/total_count_sync)
			   + "%)");

	System.out.println("GENERATION OF MA INFO TIME  : " +
			   (g_tend - g_tstart) + "ms");
	System.out.println("Total number of MetaMethods:    " + allmms.size());
	System.out.println("Number of MetaMethods analyzed: " + 
			   ODPointerAnalysis.number_of_mm_analyzed +
			   " (" + 
			   ((ODPointerAnalysis.number_of_mm_analyzed*100.0)/allmms.size())
			   + "%)");
	System.out.println("Total number of call sites:    " + numberOfCallSites);
	System.out.println("Number of call sites analyzed: " +
			   ODPointerAnalysis.number_of_mapups + " ("+
			   ((ODPointerAnalysis.number_of_mapups*100.0)/numberOfCallSites)
			   + "%)");
	System.out.println("===============================");


	System.err.println("End of find_interesting_nodes");
	if (isCoherent())
	    System.out.println("Was coherent leaving find_interesting_nodes");
	else
	    System.out.println("Was INcoherent leaving find_interesting_nodes");


	if(SHOW_DETAILS) { // show the allocation policies	    
	    System.out.println();
	    mainfo.print();
	    System.out.println("===================================");
	}

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
  
// 	System.exit(1);
  }


    private static void boundedly_analyze_all_roots() {
	System.out.println("A new spiffy adventure just started!");

	if (ODPointerAnalysis.CALL_CONTEXT_SENSITIVE)
	    System.out.println("CONTEXT_SENSITIVE");
	else
	    System.out.println("CONTEXT_insensitive :-(");


	pa.FIRST_ANALYSIS = true;
	DEBUG = true;

	MetaCallGraph mcg = pa.getMetaCallGraph();
	Set allmms = mcg.getAllMetaMethods();

	// Allocation of the hash tables used to store the results of
	// the analyzes
	pa.hash_proc_int_d = new HashMap[1+pa.MAX_ANALYSIS_DEPTH];
	pa.hash_proc_ext_d = new HashMap[1+pa.MAX_ANALYSIS_DEPTH];

	for(int i = 0; i< 1+pa.MAX_ANALYSIS_DEPTH; i++) {
	    pa.hash_proc_int_d[i] =  new HashMap();
	    pa.hash_proc_ext_d[i] =  new HashMap();
	}

	//	for(int i = 0; i<= pa.MAX_ANALYSIS_DEPTH; i++) {
	for(int i = 0; i<= 0; i++) {
	    pa.current_analysis_depth = i;
	    
	    // The following loop has just the purpose of timing the
	    // analysis of the entire program. Doing it here, before
	    // any memory allocation optimization, allows us to time
	    // it accurately.
	    g_tstart = System.currentTimeMillis();
	    for(Iterator it = allmms.iterator(); it.hasNext(); ) {
		MetaMethod mm = (MetaMethod) it.next();
		if(!analyzable(mm)) continue;
// 		System.out.println("Normal analysis (" +  i + ")");
// 		ODPointerAnalysis.BOUNDED_ANALYSIS_DEPTH = false;
// 		pa.analyze_intra_proc(mm);
		System.out.println("Modified analysis (" +  i + ")");
// 		ODPointerAnalysis.BOUNDED_ANALYSIS_DEPTH = true;
		pa.analyze_intra_proc(mm);
	    }
	    System.out.println("Intrathread Analysis time: " +
			       (time() - g_tstart) + "ms");
	}

	//FV What about the code below ?

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

	pa.FIRST_ANALYSIS = false;
	if (pa.MAX_ANALYSIS_DEPTH>0){
	    pa.current_analysis_depth = 1;
	    for(Iterator it = allmms.iterator(); it.hasNext(); ) {
		MetaMethod mm = (MetaMethod) it.next();
		if(!analyzable(mm)) continue;
		pa.hash_proc_int_d[1].put(mm,((ODParIntGraph)pa.hash_proc_int_d[0].get(mm)).clone());
		pa.hash_proc_ext_d[1].put(mm,((ODParIntGraph)pa.hash_proc_ext_d[0].get(mm)).clone());
	    }
	}

	
	if (ODPointerAnalysis.CALL_CONTEXT_SENSITIVE)
	    System.out.println("CONTEXT_SENSITIVE end of boundedly_analyze_all_roots");
	else
	    System.out.println("CONTEXT_insensitive :-( end of boundedly_analyze_all_roots");
    }




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

    private static void sync_elim_all_roots() {
	//tbu
	//	SyncElimination se = new SyncElimination(pa);
	SyncElimination se = null;
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
                ODParIntGraph ext_pig = pa.getExtParIntGraph(mm);
                ODParIntGraph int_pig = pa.getIntParIntGraph(mm);
		ODParIntGraph pig_inter_thread = pa.getIntThreadInteraction(mm);
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

    static void do_elim_syncops() {
	MetaCallGraph mcg = pa.getMetaCallGraph();
	MetaMethod mroot = new MetaMethod(hroot, true);
	Set allmms = mcg.getAllMetaMethods();
	SyncElimination se = null;
	//tbu 	SyncElimination se = new SyncElimination(pa);
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
	
	SyncElimination se = null;
	//tbu	SyncElimination se = new SyncElimination(pa);

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
	
	InstrumentSyncOps se = null;
	//TBU	InstrumentSyncOps se = new InstrumentSyncOps(pa);

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

    private static String[] examples = {
	"java -mx200M harpoon.Main.ODPAMain -a multiplyAdd --ccs=2 --wts" + 
	"harpoon.Test.PA.Test1.complex",
	"java -mx200M harpoon.Main.ODPAMain -s -a run " + 
	"harpoon.Test.PA.Test2.Server",
	"java -mx200M harpoon.Main.ODPAMain -s " + 
	"-a harpoon.Test.PA.Test3.multisetElement.insert" + 
	" harpoon.Test.PA.Test3.multiset ",
	"java -mx200M harpoon.Main.ODPAMain -s -a sum " +
	"harpoon.Test.PA.Test4.Sum",
	"java harpoon.Main.ODPAMain -a foo harpoon.Test.PA.Test5.A"
    };

    private static String[] options = {
	"-m, --meta      Uses the real MetaMethods (unsupported yet).",
	"-s, --smart     Uses the SmartCallGrapph.",
	"-d, --dumb      Uses the simplest CallGraph (default).",
	"-c, --showch    Shows debug info about ClassHierrachy.",
	"--loadpre file  Loads the precomputed preanalysis results from disk.",
	"--savepre file  Saves the preanalysis results to disk.",
	"--showcg        Shows the (meta) call graph.",
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
	"                 by default, it activates the stack and thread alloc",
	"--sa 0|1        Turns on/off the stack allocation",
	"--ta 0|1        Turns on/off the thread allocation",
	"--ns 0|1        Turns on/off the generation of \"no sync\" hints",
	"-a method       Analyzes he given method. If the method is in the",
	"                 same class as the main method, the name of the",
	"                 class can be ommited. More than one \"-a\" flags",
	"                 can be used on the same command line.",
	"-i              Interactive analysis of methods.",
	"--noit          Just interprocedural analysis, no interthread.",
	"--inline        Use method inlining to enable more stack allocation",
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
	    "\tjava harpoon.Main.ODPAMain [options] <main_class>\n");
	
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


    // Constructs the class hierarchy of the analyzed program.
    private static void construct_class_hierarchy() {
	Set roots = new HashSet();
	roots.add(hroot);
	roots.addAll
	    (harpoon.Backend.Runtime1.Runtime.runtimeCallableMethods(linker));

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
	long tstart = time();

	ch = new QuadClassHierarchy(linker, roots, hcf);

	System.out.println((time() - tstart) + "ms");

	if(SHOW_CH){
	    System.out.println("Root method = " + hroot);	    
	    System.out.println("Instantiated classes: {");
	    Set inst_cls = ch.instantiatedClasses();
	    for(Iterator it = inst_cls.iterator(); it.hasNext(); )
		System.out.println(" " + it.next());
	    System.out.println("}");
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
	mroots.addAll(select_methods(runtime_callable));
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

    public static boolean isCoherent(){
	boolean allfound = true;
// 	Set allTheQuads = an.alloc2int.keySet();
// 	for(Iterator quad_it= allTheQuads.iterator(); quad_it.hasNext(); ){
// 	    Quad quad = (Quad) quad_it.next();
// 	    QuadFactory qf = quad.getFactory();
// 	    HMethod hm = qf.getMethod();
// 	    HCode hc = hcf.convert(hm);
// 	    boolean found = false;
// 	    for(Iterator q_it=hc.getElementsI(); (q_it.hasNext())&&(!found); ){
// 		Quad q = (Quad) q_it.next();
// 		if (quad.equals(q)) found = true;
// 	    }
// 	    if (!found)
// 		allfound = false;
// 	} 
// 	if (allfound)
// 		System.err.println("All Quads found");
// 	    else
// 		System.err.println("Missing Quads !!!!!!!!");
	


	return allfound;
    }

    public static boolean check_mcg(){
	boolean allfound = true;
// 	Set allTheQuads = an.alloc2int.keySet();
// 	for(Iterator quad_it= allTheQuads.iterator(); quad_it.hasNext(); ){
// 	    Quad quad = (Quad) quad_it.next();
// 	    QuadFactory qf = quad.getFactory();
// 	    HMethod hm = qf.getMethod();
// 	    HCode hc = hcf.convert(hm);
// 	    boolean found = false;
// 	    for(Iterator q_it=hc.getElementsI(); (q_it.hasNext())&&(!found); ){
// 		Quad q = (Quad) q_it.next();
// 		if (quad.equals(q)) found = true;
// 	    }
// 	    if (!found)
// 		allfound = false;
// 	} 
// 	if (allfound)
// 	    System.err.println("All Quads found");
// 	else
// 	    System.err.println("Missing Quads !!!!!!!!");
	


	return allfound;
    }
    
}
