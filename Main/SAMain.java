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

import gnu.getopt.Getopt;

import java.util.Iterator;
import java.util.HashMap;
import java.util.Set;
import java.util.Map;
import java.util.Vector;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.IOException;
import java.io.OptionalDataException;
import java.io.FileOutputStream;
import java.io.FileInputStream;


/**
 * <code>SAMain</code> is a program to compile java classes to some
 * approximation of StrongARM assembly.  It is for development testing
 * purposes, not production use.
 * 
 * @author  Felix S. Klock II <pnkfelix@mit.edu>
 * @version $Id: SAMain.java,v 1.1.2.5 1999-08-17 22:31:12 pnkfelix Exp $
 */
public class SAMain extends harpoon.IR.Registration {
 
    private static boolean PRINT_ORIG = true;
    private static boolean PRE_REG_ALLOC = true;
    private static boolean REG_ALLOC = true;
    private static boolean LIVENESS_TEST = true;

    private static java.io.PrintWriter out = new java.io.PrintWriter(System.out, true);;
        
    private static String className;

    private static String classHierarchyFilename;
    private static ClassHierarchy classHierarchy;

    public static void main(String[] args) {
	HCodeFactory hcf = // default code factory.
	    // harpoon.Analysis.QuadSSA.SCC.SCCOptimize.codeFactory
	    (harpoon.IR.Quads.QuadSSA.codeFactory()
	     );
	
	Getopt g = new Getopt("SAMain", args, "m:c:");
	
	int c;
	String arg;
	while((c = g.getopt()) != -1) {
	    switch(c) {
	    case 'm': // serialized methToSAFactMap
		arg = g.getOptarg();
		classHierarchyFilename = arg;
		try {
		    ObjectInputStream mIS = 
			new ObjectInputStream(new FileInputStream(arg)); 
		    classHierarchy = (ClassHierarchy) mIS.readObject();
		} catch (OptionalDataException e) {
		} catch (ClassNotFoundException e) {
		} catch (IOException e) {
 		    // something went wrong; rebuild the map and write
		    // it later.
		    classHierarchy = null;
		    System.out.println("Error reading map from "
				       + classHierarchyFilename);
		}
		break;
	    case 'c':
		arg = g.getOptarg();
		System.out.println("Compiling: " + arg);
		className = arg;
		break;
	    case '?':
		break; // getopt() already printed an error
	    default: 
		System.out.println("getopt() returned " + c);
		System.out.println("usage is: [-m <mapfile>] -c <class>");
	    }
	}

	HClass hclass = HClass.forName(className);
	HMethod hm[] = hclass.getDeclaredMethods();
	HMethod mainM = null;
	for (int j=0; j<hm.length; j++) {
	    if (hm[j].getName().equalsIgnoreCase("main")) {
		mainM = hm[j];
		break;
	    }
	}
	
	Util.assert(mainM != null, "Class " + className + 
		    " has no main method");


    
	HCodeFactory sahcf = saFactory(mainM, hcf);

	if (classHierarchyFilename != null) {
	    try {
		ObjectOutputStream mOS = new
		    ObjectOutputStream(new FileOutputStream
				       (classHierarchyFilename));
		mOS.writeObject(classHierarchy);
		mOS.flush();
	    } catch (IOException e) {
		System.out.println("Error outputting class "+
				   "hierarchy to " + 
				   classHierarchyFilename);
	    }
	}

	Set methods = classHierarchy.callableMethods();
	
	Iterator methodIter = methods.iterator();
	HMethod hmethod;
	while (methodIter.hasNext()) {
	    hmethod = (HMethod)methodIter.next();

	    if (PRINT_ORIG) {
		HCode hc = hcf.convert(hmethod);

		out.println("\t--- TREE FORM ---");
		if (hc!=null) hc.print(out);
		out.println("\t--- end TREE FORM ---");
		out.println();
	    }
	    
	    out.flush();

	    if (PRE_REG_ALLOC) {
		HCode hc = sahcf.convert(hmethod);
		
		out.println("\t--- INSTR FORM (no register allocation)  ---");
		if (hc!= null) hc.print(out);
		out.println("\t--- end INSTR FORM (no register allocation)  ---");
		out.println();
	    }
		
	    out.flush();

	    if (LIVENESS_TEST) {
		HCode hc = sahcf.convert(hmethod);
		
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
	    
	    if (REG_ALLOC) {
		HCode hc = sahcf.convert(hmethod);
			
		out.println("\t--- INSTR FORM (register allocation)  ---");
		HCodeFactory regAllocCF = RegAlloc.codeFactory(sahcf, new SAFrame());
		HCode rhc = regAllocCF.convert(hmethod);
		if (rhc != null) rhc.print(out);
		out.println("\t--- end INSTR FORM (register allocation)  ---");
		out.println();
	    }

	    out.flush();
	}
    }



    /* part of the problem with speed here is that this method needs
       to recreate SAFactories, which means that we need to remake the
       ClassHierarchy each time.  Later I may add code to Serialize
       the ClassHierarchy, but in the mean time I'll speed this up by
       making the system pay the cost of generating an SAFactory occur
       only once per method lookup, by using a method->saFactory map.
    */
    
    
    private static HCodeFactory saFactory(HMethod m, HCodeFactory qhcf) {
	HCodeFactory sahcf;
	out.println("\t\tBeginning creation of a StrongARM Code Factory ");
	long time = -System.currentTimeMillis();
	HCode hc = qhcf.convert(m); 
	if (classHierarchy == null) {
	    classHierarchy = new ClassHierarchy(m, qhcf);
	    Util.assert(classHierarchy != null, "How the hell...");
	}
	HCodeFactory tcf = CanonicalTreeCode.codeFactory
		( qhcf, new SAFrame(new OffsetMap32(classHierarchy)) );
	sahcf = SACode.codeFactory(tcf);
	time += System.currentTimeMillis();
	out.println("\t\tFinished creation of a StrongARM Code "+
		    "Factory.  Time (ms): " + time);
	return sahcf;
    }
}
