// TestMemOpt.java, created Tue Apr  2 00:18:19 2002 by salcianu
// Copyright (C) 2000 Alexandru SALCIANU <salcianu@MIT.EDU>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.MemOpt;

import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;

import harpoon.Util.Util;
import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HCode;
import harpoon.ClassFile.HMethod;
import harpoon.ClassFile.Loader;
import harpoon.ClassFile.Linker;
import harpoon.ClassFile.Relinker;
import harpoon.ClassFile.HCodeFactory;
import harpoon.ClassFile.CachingCodeFactory;

import harpoon.Analysis.Quads.CallGraph;
import harpoon.Analysis.Quads.CallGraphImpl;
import harpoon.Analysis.Quads.SSICallGraph;
import harpoon.Analysis.Quads.CachingCallGraph;
import harpoon.Analysis.Quads.QuadClassHierarchy;
import harpoon.Analysis.ClassHierarchy;

import harpoon.Analysis.PointerAnalysis.Debug;

import harpoon.IR.Quads.Quad;
import harpoon.IR.Quads.CALL;

/** <code>TestMemOpt</code> is a simple tester for the classes from
    <code>harpoon.Analysis.MemOpt</code>. Should be removed quite
    soon.
 
 @author  Alexandru SALCIANU <salcianu@MIT.EDU>
 @version $Id: TestMemOpt.java,v 1.4 2002-04-11 18:53:45 salcianu Exp $ */
public abstract class TestMemOpt {
    
    private static Linker linker = new Relinker(Loader.systemLinker);

    private static long time() {
	return System.currentTimeMillis();
    }

    public static void main(String[] args) {
	if(args.length != 1) {
	    System.out.println("Usage \n\tjava TestMemOpt <main_class_name>");
	    System.exit(1);
	}

	long start = time();
	CachingCodeFactory hcf =
	    new CachingCodeFactory(harpoon.IR.Quads.QuadSSI.codeFactory(),
				   true);
	System.out.println("CodeFactory done in " + (time() - start) + "ms");

	start = time();
	ClassHierarchy ch =
	    new QuadClassHierarchy(linker, get_roots(args[0]), hcf);
	System.out.println("ClassHierarchy done in " +
			   (time() - start) + "ms");

	start = time();
	CachingCallGraph cg =
	    new CachingCallGraph(new SSICallGraph(ch, hcf), false, false);
	cg.load_caches();
	System.out.println("CallGraph done in " + (time() - start) + "ms");

	start = time();
	ComputeAnAe anae = new ComputeAnAe(hcf, cg);
	System.out.println("ComputeAnAe done in " +
			   + (time() - start) + "ms\n\n\n");

	//System.exit(0);

	for(Iterator it = cg.callableMethods().iterator(); it.hasNext(); ) {
	    HMethod hm = (HMethod) it.next();
	    if(!ComputeAnAe.isAnalyzable(hm)) continue;

	    System.out.println("Method " + hm);
	    System.out.println("Normal flow { ");
	    
	    for(Iterator itn = anae.getAn(hm).iterator(); itn.hasNext(); )
		System.out.println(" " + Debug.code2str((Quad) itn.next()));

	    System.out.println("}");

	    System.out.println("Exceptional flow { ");
	    
	    for(Iterator ite = anae.getAe(hm).iterator(); ite.hasNext(); )
		System.out.println(" " + Debug.code2str((Quad) ite.next()));

	    System.out.println("}\n\n");
	}

    }

    private static Set get_roots(String root_class) {
	Set roots = new HashSet();

	// find the root method
	HClass hclass = linker.forName(root_class);
	assert hclass != null : "Class " + root_class + " not found!";

	HMethod[] hm  = hclass.getDeclaredMethods();
	// search for the main method
	HMethod hroot = null;
	for(int i = 0; i < hm.length; i++)
	    if(hm[i].getName().equals("main")) {
		assert hroot == null : "Ambiguous root method!";
		hroot = hm[i];
	    }
	
	assert hroot != null : "Root method \"" + root_class + ".main"
		    + "\" not found!";

	roots.add(hroot);
	roots.addAll
	    (harpoon.Backend.Runtime1.Runtime.runtimeCallableMethods(linker));
	return roots;
    }
    
}
