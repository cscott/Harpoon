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

import harpoon.Analysis.MetaMethods.MetaMethod;
import harpoon.Analysis.MetaMethods.MetaCallGraph;
import harpoon.Analysis.MetaMethods.FakeMetaCallGraph;
import harpoon.Analysis.MetaMethods.MetaCallGraphImpl;
import harpoon.Analysis.MetaMethods.MetaAllCallers;

import harpoon.Tools.BasicBlocks.BBConverter;
import harpoon.Tools.BasicBlocks.CachingBBConverter;
import harpoon.Tools.Graphs.SCComponent;
import harpoon.Tools.Graphs.SCCTopSortedGraph;

import harpoon.Analysis.PointerAnalysis.Debug;
import harpoon.Analysis.MetaMethods.SmartCallGraph;

/**
 * <code>PAMain</code> is a simple Pointer Analysis top-level class.
 * It is designed for testing and evaluation only.
 * 
 * @author  Alexandru SALCIANU <salcianu@retezat.lcs.mit.edu>
 * @version $Id: PAMain.java,v 1.1.2.14 2000-03-22 05:23:11 salcianu Exp $
 */
public abstract class PAMain {

    // use the real meta call graph
    private static boolean METAMETHODS = false;
    // use FakeMetaCallGraph(SmartCallGraph)
    private static boolean SMART_CALL_GRAPH = true;
    // debug the class hierarchy
    private static boolean DEBUG_CH = false;

    private static String[] examples ={
	"java harpoon.Main.PAMain harpoon.Test.PA.Test1.complex multiplyAdd",
	"java -Xmx200M harpoon.Main.PAMain harpoon.Test.PA.Test2.Server run",
	"java harpoon.Main.PAMain harpoon.Test.PA.Test3.multiset " +
	"harpoon.Test.PA.Test3.multisetElement.insert",
	"java harpoon.Main.PAMain harpoon.Test.PA.Test4.Sum sum",
	"java harpoon.Main.PAMain harpoon.Test.PA.Test5.A foo"
    };


    static PointerAnalysis pa = null;
    
    private static class Method{
	String name  = null;
	String declClass = null;
    };

    private static Method root_method = new Method();
    private static Method analyzed_method = new Method();

    public static final void main(String[] params){
	if(params.length < 2){
	    System.out.println("Usage:\n" +
	     "\tjava harpoon.Main.PAMain <main_class>" +
	     "([<class>].<analyzed_method>)*\n" +
	     " If no class if given for the analyzed method, " +
	     "<main_class> is taken by default.\n" + 
	     "Examples:");

	    for(int i = 0; i < examples.length; i++)
		System.out.println("\t" + examples[i]);

	    System.out.println("Suggestion:\n" +
	     "\tYou might consider the \"-Xmx\" flag of the JVM to satisfy\n" +
	     "\tthe huge memory requirements of the pointer analysis.\n" +
	     "Warning:\n\t\"Quite fast for small programs!\"" + 
	     " [Moolly Sagiv]\n" +
	     "\t\t... and only for them :-(");

	    System.exit(1);
	}

	// variables for the timing stuff.
	long tstart = 0;
	long tstop  = 0;

	long start_pre_time = System.currentTimeMillis();
	Linker linker = Loader.systemLinker;
	root_method.name = "main";
	root_method.declClass = params[0];
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

	if(DEBUG_CH){
	    System.out.println("Set of roots");
	    for(Iterator it = root_methods.iterator(); it.hasNext(); ){
		Object o = it.next();
		if(o instanceof HMethod)
		    System.out.println(" m: " + o);
		else
		    System.out.println(" c: " + o);
	    }
	}

	System.out.print("ClassHierarchy ... ");
	tstart = System.currentTimeMillis();
	ClassHierarchy ch = 
	    new QuadClassHierarchy(linker,Collections.singleton(hroot),hcf);
	tstop  = System.currentTimeMillis();
	System.out.println((tstop - tstart) + "ms");

	if(DEBUG_CH){
	    System.out.println("Root method = " + hroot);	    
	    System.out.println("Instantiated classes:");
	    Set inst_cls = ch.instantiatedClasses();
	    for(Iterator it = inst_cls.iterator(); it.hasNext(); )
		System.out.println(" " + it.next());
	    
	    System.exit(1);
	}

	BBConverter bbconv = new BBConverter(hcf);

	MetaCallGraph  mcg = null;
	MetaAllCallers mac = null;
	
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
	

	//System.out.println("MetaCallGraph:");
	//mcg.print(new java.io.PrintWriter(System.out, true), true);

	//System.out.println("   ");

	//System.out.println("Split relation:");
	//Debug.show_split(mcg.getSplitRelation());
	//System.exit(1);

	pa = new PointerAnalysis(mcg, mac, bbconv);

	if(params.length > 1){
	    for(int i = 1; i < params.length; i++){
		getMethodName(params[i],analyzed_method);
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
		getMethodName(method_name, analyzed_method);
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
	for(int i = 0;i<hm.length;i++)
	    if(hm[i].getName().equals(method.name))
		hmethod = hm[i];
	if(hmethod == null){
	    System.out.println("Sorry, method not found\n");
	    return;
	}

	MetaMethod mm;

	if(METAMETHODS)
	    mm = new MetaMethod(hmethod,false);
	else
	    mm = new MetaMethod(hmethod,true);

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
	System.out.print("EXTERNAL GRAPH AT THE END OF THE METHOD" +
			 " + INTER-THREAD ANALYSIS:");
	System.out.println(pig_inter_thread);
    }

    // receives a "class.name" string and cut it into pieces, separating
    // the name of the class from the name of the method.
    private static void getMethodName(String str, Method method){
	int point_pos = str.lastIndexOf('.');
	method.name           = str.substring(point_pos+1);
	if(point_pos == -1) return;
	method.declClass      = str.substring(0,point_pos);
    }

}












