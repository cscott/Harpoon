// PAMain.java, created Fri Jan 14 10:54:16 2000 by salcianu
// Copyright (C) 1999 Alexandru SALCIANU <salcianu@retezat.lcs.mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Main;

import java.util.Collections;
import java.util.Date;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;

import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HMethod;
import harpoon.ClassFile.HCodeFactory;
import harpoon.ClassFile.CachingCodeFactory;
import harpoon.ClassFile.Linker;
import harpoon.ClassFile.Loader;

import harpoon.Analysis.Quads.QuadClassHierarchy;
import harpoon.Analysis.Quads.CallGraph;
import harpoon.Analysis.AllCallers;
import harpoon.Analysis.ClassHierarchy;
import harpoon.Analysis.BasicBlock;

import harpoon.Analysis.PointerAnalysis.PointerAnalysis;
import harpoon.Analysis.PointerAnalysis.PANode;
import harpoon.Analysis.PointerAnalysis.ParIntGraph;


/**
 * <code>PAMain</code> is a simple Pointer Analysis top-level class.
 * It is designed for testing and evaluation only.
 * 
 * @author  Alexandru SALCIANU <salcianu@retezat.lcs.mit.edu>
 * @version $Id: PAMain.java,v 1.1.2.7 2000-03-02 22:55:44 salcianu Exp $
 */
public abstract class PAMain {

    private static String[] example ={
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
	     "Examples:\n" +
	     "\t" + example[0] + "\n" +
	     "\t" + example[1] + "\n" +
	     "\t" + example[2] + "\n" +
	     "\t" + example[3] + "\n" +
	     "\t" + example[4] + "\n" +
	     "Suggestion:\n" +
	     "\tYou might consider the \"-Xmx\" flag of the JVM to satisfy\n" +
	     "\tthe huge memory requirements of the pointer analysis.\n" +
	     "Warning:\n\t\"Quite fast for small programs!\"" + 
	     " [Moolly Sagiv]\n" +
	     "\t\t... and only for them :-(");
	    System.exit(1);
	}

	System.out.print("Pre-PointerAnalysis stuff ... ");
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
	ClassHierarchy ch = 
	    new QuadClassHierarchy(linker,Collections.singleton(hroot),hcf); 

	long total_pre_time = System.currentTimeMillis() - start_pre_time;
	System.out.println(total_pre_time + "ms");

	System.out.print("Constructing CallGraph + AllCallers ... ");
	long begin_time = System.currentTimeMillis();
	CallGraph  cg   = new CallGraph(ch,hcf);
	AllCallers ac   = new AllCallers(ch,hcf);
	long total_time = System.currentTimeMillis() - begin_time;

	if(PointerAnalysis.DETERMINISTIC && !PointerAnalysis.TIMING)
	    total_time = -1;

	System.out.println(total_time + "ms");

	pa = new PointerAnalysis(cg,ac,hcf);

	if(params.length>1){
	    for(int i=1;i<params.length;i++){
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

	ParIntGraph int_pig = pa.getIntParIntGraph(hmethod);
	ParIntGraph ext_pig = pa.getExtParIntGraph(hmethod);
	ParIntGraph pig_inter_thread = pa.threadInteraction(hmethod);
	PANode[] nodes = pa.getParamNodes(hmethod);
	System.out.println("METHOD " + hmethod);
	System.out.print("POINTER PARAMETERS: ");
	System.out.print("[ ");
	for(int i=0;i<nodes.length;i++)
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












