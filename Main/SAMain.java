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

import java.util.Iterator;

/**
 * <code>SAMain</code> is a program to compile java classes to some
 * approximation of StrongARM assembly.  It is for development testing
 * purposes, not production use.
 * 
 * @author  Felix S. Klock II <pnkfelix@mit.edu>
 * @version $Id: SAMain.java,v 1.1.2.3 1999-08-04 00:29:49 pnkfelix Exp $
 */
public class SAMain extends harpoon.IR.Registration {
 
    private static boolean PRINT_ORIG = true;
    private static boolean PRE_REG_ALLOC = true;
    private static boolean REG_ALLOC = true;
    private static boolean LIVENESS_TEST = true;
        
    public static void main(String[] args) {
	java.io.PrintWriter out = new java.io.PrintWriter(System.out, true);
	HCodeFactory hcf = // default code factory.
	    harpoon.Analysis.QuadSSA.SCC.SCCOptimize.codeFactory
	    (harpoon.IR.Quads.QuadSSA.codeFactory()
	     );
	hcf = CanonicalTreeCode.codeFactory( hcf, new SAFrame() );

	int n=0;  // count # of args/flags processed.
	// rest of command-line options are class names.
	HClass interfaceClasses[] = new HClass[args.length-n];
	for (int i=0; i<args.length-n; i++)
	    interfaceClasses[i] = HClass.forName(args[n+i]);
	// Do something intelligent with these classes. XXX
	for (int i=0; i<interfaceClasses.length; i++) {
	    HMethod hm[] = interfaceClasses[i].getDeclaredMethods();

	    if (PRINT_ORIG) {
		for (int j=0; j<hm.length; j++) {
		    HCode hc = hcf.convert(hm[j]);

		    out.println("\t--- TREE FORM ---");
		    if (hc!=null) hc.print(out);
		    out.println();
		}
		
	    }
	    if (PRE_REG_ALLOC) {
		for (int j=0; j<hm.length; j++) {
		    HCode hc = hcf.convert(hm[j]);
		    HCodeFactory sahcf = SACode.codeFactory(hcf);
		    hc = sahcf.convert(hm[j]);
		
		    out.println("\t--- INSTR FORM (no register allocation)  ---");
		    if (hc!= null) hc.print(out);
		    out.println();
		}
	    }

	    if (LIVENESS_TEST) {
		for (int j=0; j<hm.length; j++) {
		    HCode hc = hcf.convert(hm[j]);
		    HCodeFactory sahcf = SACode.codeFactory(hcf);
		    hc = sahcf.convert(hm[j]);

		    out.println("\t--- INSTR FORM (basic block check)  ---");
		    HCodeElement root = hc.getRootElement();
		    BasicBlock block = 
			BasicBlock.computeBasicBlocks((HasEdges)root);
		    Iterator iter= BasicBlock.basicBlockIterator(block);
		    LiveVars livevars = new LiveVars(iter); 
		    InstrSolver.worklistSolver
			(BasicBlock.basicBlockIterator(block), livevars);
		    out.println(livevars.dump());
		}

		if (REG_ALLOC) {
		    for (int j=0; j<hm.length; j++) {
			HCode hc = hcf.convert(hm[j]);
			HCodeFactory sahcf = SACode.codeFactory(hcf);
			hc = sahcf.convert(hm[j]);
			
			out.println("\t--- INSTR FORM (register allocation)  ---");
			HCodeFactory regAllocCF = RegAlloc.codeFactory(sahcf, new SAFrame());
			HCode rhc = regAllocCF.convert(hm[j]);
			if (rhc != null) rhc.print(out);
			out.println();
		    }
		}
		
		out.flush();
 
	    }
	}
	
    }

}
