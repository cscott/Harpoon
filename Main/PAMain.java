// PAMain.java, created Fri Jan 14 10:54:16 2000 by salcianu
// Copyright (C) 1999 Alexandru SALCIANU <salcianu@retezat.lcs.mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Main;

import java.util.Collections;
import java.util.Date;
import java.util.Set;
import java.util.HashSet;
import java.util.Iterator;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.PrintWriter;

import gnu.getopt.Getopt;
import gnu.getopt.LongOpt;

import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HMethod;
import harpoon.ClassFile.HCodeFactory;
import harpoon.ClassFile.CachingCodeFactory;
import harpoon.ClassFile.Linker;
import harpoon.ClassFile.Loader;

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

import harpoon.Analysis.MetaMethods.MetaMethod;
import harpoon.Analysis.MetaMethods.MetaCallGraph;
import harpoon.Analysis.MetaMethods.FakeMetaCallGraph;
import harpoon.Analysis.MetaMethods.MetaCallGraphImpl;
import harpoon.Analysis.MetaMethods.MetaAllCallers;

import harpoon.Util.BasicBlocks.BBConverter;
import harpoon.Util.BasicBlocks.CachingBBConverter;
import harpoon.Util.LightBasicBlocks.LBBConverter;
import harpoon.Util.LightBasicBlocks.CachingLBBConverter;
import harpoon.Util.Graphs.SCComponent;
import harpoon.Util.Graphs.SCCTopSortedGraph;

import harpoon.Analysis.PointerAnalysis.Debug;
import harpoon.Analysis.MetaMethods.SmartCallGraph;

/**
 * <code>PAMain</code> is a simple Pointer Analysis top-level class.
 * It is designed for testing and evaluation only.
 * 
 * @author  Alexandru SALCIANU <salcianu@retezat.lcs.mit.edu>
 * @version $Id: PAMain.java,v 1.1.2.21 2000-03-25 05:17:35 salcianu Exp $
 */
public abstract class PAMain {

    // use the real meta call graph
    private static boolean METAMETHODS = false;
    // use FakeMetaCallGraph(SmartCallGraph)
    private static boolean SMART_CALL_GRAPH = false;
    // debug the class hierarchy
    private static boolean SHOW_CH = false;
    // stop after printing the class hierarchy debug info 
    private static boolean SHOW_CH_ONLY = false;

    // show the call graph
    private static boolean SHOW_CG = false;
    // show the split relation HMethod -> MetaMethods
    private static boolean SHOW_SPLIT = false;

    private static String[] examples = {
	"java harpoon.Main.PAMain harpoon.Test.PA.Test1.complex multiplyAdd",
	"java -mx200M harpoon.Main.PAMain harpoon.Test.PA.Test2.Server run",
	"java harpoon.Main.PAMain harpoon.Test.PA.Test3.multiset " +
	"harpoon.Test.PA.Test3.multisetElement.insert",
	"java harpoon.Main.PAMain harpoon.Test.PA.Test4.Sum sum",
	"java harpoon.Main.PAMain harpoon.Test.PA.Test5.A foo"
    };

    private static String[] options = {
	"-m, --meta     uses the real MetaMethod",
	"-s, --smart    uses the SmartCallGrapph",
	"-d, --dumb     uses the simplest CallGraph (default)",
	"-c, --showch   shows debug info about ClassHierrachy",
	"-o, --onlych   shows debug info about ClassHierarchy and stop",
	"--showcg       shows the (meta) call graph",
	"--showsplit    shows the split relation",
	"--ccs=depth    activate call context sensitivity with a maximum",
	"              call chain depth of depth",
	"--ts           activates full thread sensitivity",
	"--wts          activates weak thread sensitivity",
	"--ls           activates loop sensitivity"
    };

    static PointerAnalysis pa = null;
    
    private static class Method{
	String name  = null;
	String declClass = null;
    };

    private static Method root_method = new Method();

    private static MetaCallGraph  mcg = null;
    private static MetaAllCallers mac = null;
    private static Relation split_rel = null;

    public static final void main(String[] params){

	int optind = get_options(params);
	int nbargs = params.length - optind;

	if(nbargs < 1){
	    System.out.println("Usage:\n" +
	     "\tjava harpoon.Main.PAMain [options] <main_class> " +
	     "([<class>].<analyzed_method>)*\n" +
	     " If no class if given for the analyzed method, " +
	     "<main_class> is taken by default.");

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
	Linker linker = Loader.systemLinker;
	root_method.name = "main";
	root_method.declClass = params[optind];
	optind++;
	HClass hclass = linker.forName(root_method.declClass);
	HMethod[] hm  = hclass.getDeclaredMethods();

	// search for the main method
	HMethod hroot = null;
	for(int i = 0;i<hm.length;i++)
	    if(hm[i].getName().equals(root_method.name))
		hroot = hm[i];
	if(hroot == null){
	    System.out.println("Sorry, the root method was not found\n");
	    System.exit(1);
	}

	HCodeFactory hcf  = 
	    new CachingCodeFactory(harpoon.IR.Quads.QuadNoSSA.codeFactory());

	Set root_methods = new HashSet();
	root_methods.add(hroot);
	root_methods.addAll(
	    harpoon.Backend.Runtime1.Runtime.runtimeCallableMethods(linker));

	if(SHOW_CH){
	    System.out.println("Set of roots: {");
	    for(Iterator it = root_methods.iterator(); it.hasNext(); ){
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
	ClassHierarchy ch = 
	    new QuadClassHierarchy(linker,Collections.singleton(hroot),hcf);
	tstop  = System.currentTimeMillis();
	System.out.println((tstop - tstart) + "ms");

	if(SHOW_CH){
	    System.out.println("Root method = " + hroot);	    
	    System.out.println("Instantiated classes: {");
	    Set inst_cls = ch.instantiatedClasses();
	    for(Iterator it = inst_cls.iterator(); it.hasNext(); )
		System.out.println(" " + it.next());
	    System.out.println("}");
	    if(SHOW_CH_ONLY) System.exit(1);
	}

	CachingBBConverter bbconv = new CachingBBConverter(hcf);
	LBBConverter lbbconv = new CachingLBBConverter(bbconv);

	if(METAMETHODS){ // real meta-methods
	    System.out.print("MetaCallGraph ... ");
	    tstart = System.currentTimeMillis();
	    mcg = new MetaCallGraphImpl(bbconv, ch, hroot);
	    tstop  = System.currentTimeMillis();
	    System.out.println((tstop - tstart) + "ms");

	    System.out.print("MetaAllCallers ... ");
	    tstart = System.currentTimeMillis();
	    mac = new MetaAllCallers(mcg);
	    tstop = System.currentTimeMillis();
	    System.out.println((tstop - tstart) + "ms");
	}
	else{
	    CallGraph cg = null;
	    if(SMART_CALL_GRAPH){ // smart call graph!
		System.out.print("MetaCallGraph ... ");
		tstart = System.currentTimeMillis();
		MetaCallGraph fmcg = new MetaCallGraphImpl(bbconv, ch, hroot);
		tstop  = System.currentTimeMillis();
		System.out.println((tstop - tstart) + "ms");

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
	    mcg = new FakeMetaCallGraph(cg, cg.callableMethods());
	    tstop  = System.currentTimeMillis();
	    System.out.println((tstop - tstart) + "ms");
	    
	    System.out.print("(Fake)MetaAllCallers ... ");
	    tstart = System.currentTimeMillis();
	    mac = new MetaAllCallers(mcg);
	    tstop  = System.currentTimeMillis();
	    System.out.println((tstop - tstart) + "ms");
	}
	

	System.out.print("SplitRelation ... ");
	tstart = System.currentTimeMillis();
	split_rel = mcg.getSplitRelation();
	tstop  = System.currentTimeMillis();
	System.out.println((tstop - tstart) + "ms");

	if(SHOW_CG){
	    System.out.println("MetaCallGraph:");
	    mcg.print(new java.io.PrintWriter(System.out, true), true);
	}

	if(SHOW_SPLIT){
	    System.out.println("Split relation:");
	    Debug.show_split(mcg.getSplitRelation());
	}

	pa = new PointerAnalysis(mcg, mac, lbbconv);

	if(optind < params.length){
	    for(; optind < params.length; optind++ ){
		Method analyzed_method = getMethodName(params[optind]);
		if(analyzed_method.declClass == null)
		    analyzed_method.declClass = root_method.declClass;
		display_method(analyzed_method);
	    }
	}
	else{
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

	pa.print_stats();
    }
    
    private static void display_method(Method method){
	Linker linker = Loader.systemLinker;
	HClass hclass = linker.forName(method.declClass);
	HMethod[] hm  = hclass.getDeclaredMethods();

	HMethod hmethod = null;		
	for(int i = 0; i < hm.length; i++)
	    if(hm[i].getName().equals(method.name))
		hmethod = hm[i];

	if(hmethod == null){
	    System.out.println("Sorry, method " + method.declClass +
			       " . " + method.name + " not found\n");
	    return;
	}

	int nbmm = 0;

	// look for all the meta-methods originating into this method
	// and do all the analysis stuff on them.
	for(Iterator it = split_rel.getValues(hmethod); it.hasNext(); ){
	    nbmm++;
	    MetaMethod mm = (MetaMethod) it.next();
	    System.out.println("HMETHOD " +hmethod+ " ->\n META-METHOD " + mm);
	    ParIntGraph int_pig = pa.getIntParIntGraph(mm);
	    ParIntGraph ext_pig = pa.getExtParIntGraph(mm);
	    ParIntGraph pig_inter_thread = pa.threadInteraction(mm);
	    PANode[] nodes = pa.getParamNodes(mm);
	    System.out.println("META-METHOD " + mm);
	    System.out.print("POINTER PARAMETERS: ");
	    System.out.print("[ ");
	    for(int i = 0; i < nodes.length; i++)
		System.out.print(nodes[i] + " ");
	    System.out.println("]");
	    System.out.print("INTERNAL GRAPH AT THE END OF THE METHOD:");
	    System.out.println(int_pig);
	    System.out.print("EXTERNAL GRAPH AT THE END OF THE METHOD:");
	    System.out.println(ext_pig);
	    System.out.print("INTERNAL GRAPH AT THE END OF THE METHOD" +
			     " + INTER-THREAD ANALYSIS:");
	    System.out.println(pig_inter_thread);
	}

	if(nbmm == 0)
	    System.out.println("Oops! " + hmethod +
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
	LongOpt[] longopts = new LongOpt[10];
	longopts[0] = new LongOpt("meta", LongOpt.NO_ARGUMENT, null, 'm');
	longopts[1] = new LongOpt("smartcg", LongOpt.NO_ARGUMENT, null, 's');
	longopts[2] = new LongOpt("showch", LongOpt.NO_ARGUMENT, null, 'c');
	longopts[3] = new LongOpt("onlych", LongOpt.NO_ARGUMENT, null, 'o');
	longopts[4] = new LongOpt("ccs", LongOpt.REQUIRED_ARGUMENT, null, 5);
	longopts[5] = new LongOpt("ts", LongOpt.NO_ARGUMENT, null, 6);
	longopts[6] = new LongOpt("wts", LongOpt.NO_ARGUMENT, null, 7);
	longopts[7] = new LongOpt("ls", LongOpt.NO_ARGUMENT, null, 8);
	longopts[8] = new LongOpt("showcg", LongOpt.NO_ARGUMENT, null, 9);
	longopts[9] = new LongOpt("showsplit", LongOpt.NO_ARGUMENT, null, 10);


	Getopt g = new Getopt("PAMain", argv, "msco", longopts);

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
	    case 'o':
		SHOW_CH = true;
		SHOW_CH_ONLY = true;
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
	    }

	return g.getOptind();
    }

    private static void print_options(){
	if(METAMETHODS && SMART_CALL_GRAPH){
	    System.out.println("Call Graph Type Ambiguity");
	    System.exit(1);
	}
	System.out.print("Execution options:");
	if(SHOW_CH_ONLY) 
	    System.out.print(" SHOW_CH_ONLY");
	else 
	    if(SHOW_CH)
		System.out.print(" SHOW_CH");

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
	    System.out.print(" WEAKLY_THREAD SENSITIVE");

	if(PointerAnalysis.LOOP_SENSITIVE)
	    System.out.println(" LOOP_SENSITIVE");

	System.out.println();
    }

}

