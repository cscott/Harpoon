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

/**
 * <code>PAMain</code>
 * 
 * @author  Alexandru SALCIANU <salcianu@retezat.lcs.mit.edu>
 * @version $Id: PAMain.java,v 1.1.2.2 2000-01-27 06:19:10 salcianu Exp $
 */
public abstract class PAMain {

    static PointerAnalysis pa = null;
    static HMethod[] hm = null;

    public static final void main(String[] params){
	if(params.length < 2){
	    System.out.println("Usage:\n" +
			       "\tjava harpoon.Main.PAMain <main_class_name>" +
			       " <root_function> [<analyzed_function>]\n" +
			       "Example:\n" +
			       "\tjava harpoon.Main.PAMain " + 
			       "harpoon.Test.PA.multisetElement " +
			       "insert insert\n" +
	     "Warning:\n\t\"Quite fast for small programs!\"" + 
	     " [Moolly Sagiv]\n" +
	     "\t\t... and only for them :-(");
	    System.exit(1);
	}
	
	Linker linker = Loader.systemLinker;

	HClass hclass = linker.forName(params[0]);
	hm  = hclass.getDeclaredMethods();

	String root_method = params[1];
	// search for the main method
	HMethod hroot = null;
	for(int i = 0;i<hm.length;i++)
	    if(hm[i].getName().equals(root_method))
		hroot = hm[i];
	if(hroot == null){
	    System.out.println("Sorry, the root method was not found\n");
	    System.exit(1);
	}

	HCodeFactory hcf  = 
	    new CachingCodeFactory(harpoon.IR.Quads.QuadNoSSA.codeFactory());
	ClassHierarchy ch = 
	    new QuadClassHierarchy(linker,Collections.singleton(hroot),hcf); 

	System.out.print("Constructing CallGraph + AllCallers ... ");
	long begin_time = new Date().getTime();
	CallGraph  cg = new CallGraph(ch,hcf);
	AllCallers ac = new AllCallers(ch,hcf);
	long total_time = new Date().getTime() - begin_time;
	System.out.println(total_time + "ms");

	System.out.println("Starting the Pointer Analysis ..."); 
	begin_time = new Date().getTime();
	pa = new PointerAnalysis(cg,ac,hcf,hroot);
	total_time = new Date().getTime() - begin_time;
	System.out.println("The entire Pointer Analysis done in " 
			   + total_time + "ms");

	//System.out.println("===== NODES ========================");

	//System.out.println(pa.nodes);

	//System.out.println("===== RESULTS ======================");

	if(params.length>2){
	    for(int i=2;i<params.length;i++)
		display_method(params[i]);
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
		display_method(method_name);
	    }
	}
    }
    
    private static void display_method(String method_name){
	HMethod hmethod = null;		
	for(int i = 0;i<hm.length;i++)
	    if(hm[i].getName().equals(method_name))
		hmethod = hm[i];
	if(hmethod == null){
	    System.out.println("Sorry, method not found\n");
	    return;
	}
	System.out.println("METHOD " + hmethod.toString());
	System.out.print("POINTER PARAMETERS: ");
	PANode[] nodes = pa.getParamNodes(hmethod);
	System.out.print("[ ");
	for(int i=0;i<nodes.length;i++)
	    System.out.print(nodes[i] + " ");
	System.out.println("]");
	System.out.print("INTERNAL GRAPH AT THE END OF THE METHOD:");
	System.out.println(pa.getIntParIntGraph(hmethod));
	System.out.print("EXTERNAL GRAPH AT THE END OF THE METHOD:");
	System.out.println(pa.getExtParIntGraph(hmethod));
    }
}
