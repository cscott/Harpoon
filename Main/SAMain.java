// SAMain.java, created Mon Aug  2 19:41:06 1999 by pnkfelix
// Copyright (C) 1999 Felix S. Klock II <pnkfelix@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Main;

import harpoon.ClassFile.CachingCodeFactory;
import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HCode;
import harpoon.ClassFile.HCodeFactory;
import harpoon.ClassFile.HMethod;
import harpoon.ClassFile.HCodeElement;
import harpoon.IR.Properties.HasEdges;
import harpoon.IR.Tree.CanonicalTreeCode;
import harpoon.Analysis.DataFlow.LiveVars;
import harpoon.Analysis.DataFlow.InstrSolver;
import harpoon.Analysis.DataFlow.BasicBlock;
import harpoon.Analysis.Instr.RegAlloc;
import harpoon.Backend.StrongARM.SAFrame;
import harpoon.Backend.StrongARM.SACode;
import harpoon.Analysis.QuadSSA.ClassHierarchy;
import harpoon.Backend.Maps.OffsetMap32;
import harpoon.Util.Util;

import java.util.Iterator;
import java.util.HashMap;
/**
 * <code>SAMain</code> is a program to compile java classes to some
 * approximation of StrongARM assembly.  It is for development testing
 * purposes, not production use.
 * 
 * @author  Felix S. Klock II <pnkfelix@mit.edu>
 * @version $Id: SAMain.java,v 1.1.2.4 1999-08-17 19:15:40 pnkfelix Exp $
 */
public class SAMain extends harpoon.IR.Registration {
 
    private static boolean PRINT_ORIG = true;
    private static boolean PRE_REG_ALLOC = true;
    private static boolean REG_ALLOC = true;
    private static boolean LIVENESS_TEST = true;

    private static java.io.PrintWriter out = new java.io.PrintWriter(System.out, true);;
        
    public static void main(String[] args) {
	HCodeFactory hcf = // default code factory.
	    harpoon.Analysis.QuadSSA.SCC.SCCOptimize.codeFactory
	    (harpoon.IR.Quads.QuadSSA.codeFactory()
	     );

	int n=0;  // count # of args/flags processed.
	// rest of command-line options are class names.
	HClass classes[] = new HClass[args.length-n];
	for (int i=0; i<args.length-n; i++)
	    classes[i] = HClass.forName(args[n+i]);
	// Do something intelligent with these classes. XXX
	for (int i=0; i<classes.length; i++) {
	    HMethod hm[] = classes[i].getDeclaredMethods();

	    if (PRINT_ORIG) {
		for (int j=0; j<hm.length; j++) {
		    HCode hc = hcf.convert(hm[j]);

		    out.println("\t--- TREE FORM ---");
		    if (hc!=null) hc.print(out);
		    out.println("\t--- end TREE FORM ---");
		    out.println();
		}

		out.flush();
	    }
	    

	    if (PRE_REG_ALLOC) {
		for (int j=0; j<hm.length; j++) {
		    HCodeFactory sahcf = saFactory(hm[j], hcf);
		    HCode hc = sahcf.convert(hm[j]);
		
		    out.println("\t--- INSTR FORM (no register allocation)  ---");
		    if (hc!= null) hc.print(out);
		    out.println("\t--- end INSTR FORM (no register allocation)  ---");
		    out.println();
		}
		
		out.flush();
	    }


	    if (LIVENESS_TEST) {
		for (int j=0; j<hm.length; j++) {
		    HCodeFactory sahcf = saFactory(hm[j], hcf);
		    HCode hc = sahcf.convert(hm[j]);

		    out.println("\t--- INSTR FORM (basic block check)  ---");
		    HCodeElement root = hc.getRootElement();
		    BasicBlock block = 
			BasicBlock.computeBasicBlocks((HasEdges)root);
		    Iterator iter= BasicBlock.basicBlockIterator(block);
		    LiveVars livevars = new LiveVars(iter); 
		    InstrSolver.worklistSolver
			(BasicBlock.basicBlockIterator(block), livevars);
		    out.println(livevars.dump());
		    out.println("\t--- end INSTR FORM (basic block check)  ---");
		}

		out.flush();
	    }
		
	    
	    if (REG_ALLOC) {
		for (int j=0; j<hm.length; j++) {
		    HCodeFactory sahcf = saFactory(hm[j], hcf);
		    HCode hc = sahcf.convert(hm[j]);
			
		    out.println("\t--- INSTR FORM (register allocation)  ---");
		    HCodeFactory regAllocCF = RegAlloc.codeFactory(sahcf, new SAFrame());
		    HCode rhc = regAllocCF.convert(hm[j]);
		    if (rhc != null) rhc.print(out);
		    out.println("\t--- end INSTR FORM (register allocation)  ---");
		    out.println();
		}

		out.flush();
	    }
		
 
	}
    }



    /* part of the problem with speed here is that this method needs
       to recreate SAFactories, which means that we need to remake the
       ClassHierarchy each time.  Later I may add code to Serialize
       the ClassHierarchy, but in the mean time I'll speed this up by
       making the system pay the cost of generating an SAFactory occur
       only once per method lookup, by using a method->saFactory map.
    */
    
    private static HashMap methToSAFactMap = new HashMap();
    
    private static HCodeFactory saFactory(HMethod m, HCodeFactory qhcf) {
	HCodeFactory sahcf;
	sahcf = (HCodeFactory) methToSAFactMap.get(m);
	if (sahcf == null) {
	    out.println("\t\tBeginning creation of a StrongARM Code Factory ");
	    long time = -System.currentTimeMillis();
	    HCode hc = qhcf.convert(m); 
	    ClassHierarchy cha = new ClassHierarchy(m, qhcf);
	    Util.assert(cha != null, "How the hell...");
	    HCodeFactory tcf = CanonicalTreeCode.codeFactory
		( qhcf, new SAFrame(new OffsetMap32(cha)) );
	    sahcf = SACode.codeFactory(tcf);
	    time += System.currentTimeMillis();
	    out.println("\t\tFinished creation of a StrongARM Code Factory.  Time (ms): " + time);
	    methToSAFactMap.put(m, sahcf);
	}
	return sahcf;
    }
}
