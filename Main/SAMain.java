// SAMain.java, created Mon Aug  2 19:41:06 1999 by pnkfelix
// Copyright (C) 1999 Felix S. Klock II <pnkfelix@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Main;

import harpoon.ClassFile.CachingCodeFactory;
import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HCode;
import harpoon.ClassFile.HCodeEdge;
import harpoon.ClassFile.HCodeFactory;
import harpoon.ClassFile.HMethod;
import harpoon.ClassFile.HCodeElement;
import harpoon.IR.Properties.HasEdges;
import harpoon.IR.Tree.CanonicalTreeCode;
import harpoon.IR.Tree.Data;
import harpoon.IR.Assem.Instr;
import harpoon.IR.Assem.InstrFactory;
import harpoon.Temp.Temp;
import harpoon.Temp.TempFactory;
import harpoon.Analysis.DataFlow.LiveVars;
import harpoon.Analysis.DataFlow.InstrSolver;
import harpoon.Analysis.DataFlow.BasicBlock;
import harpoon.Analysis.Instr.RegAlloc;
import harpoon.Backend.Generic.Frame;
import harpoon.Backend.StrongARM.SAFrame;
import harpoon.Backend.StrongARM.SACode;
import harpoon.Analysis.QuadSSA.ClassHierarchy;
import harpoon.Backend.Maps.OffsetMap;
import harpoon.Backend.Maps.OffsetMap32;
import harpoon.Util.UnmodifiableIterator;
import harpoon.Util.Util;

import gnu.getopt.Getopt;

import java.util.Iterator;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.Vector;
import java.util.Stack;
import java.util.NoSuchElementException;
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
 * @version $Id: SAMain.java,v 1.1.2.10 1999-08-18 19:21:24 pnkfelix Exp $
 */
public class SAMain extends harpoon.IR.Registration {
 
    private static boolean PRINT_ORIG = false;
    private static boolean PRINT_DATA = false;
    private static boolean PRE_REG_ALLOC = false;
    private static boolean REG_ALLOC = false;
    private static boolean LIVENESS_TEST = false;

    private static java.io.PrintWriter out = 
	new java.io.PrintWriter(System.out, true);;
        
    private static String className;
    
    private static String classHierarchyFilename;
    private static ClassHierarchy classHierarchy;
    private static Frame frame;
    private static OffsetMap offmap;

    public static void main(String[] args) {
	HCodeFactory hcf = // default code factory.
	    // harpoon.Analysis.QuadSSA.SCC.SCCOptimize.codeFactory
	    (harpoon.IR.Quads.QuadSSA.codeFactory()
	     );
	
	Getopt g = new Getopt("SAMain", args, "m:c:DOPRLAh");
	
	int c;
	String arg;
	while((c = g.getopt()) != -1) {
	    switch(c) {
	    case 'm': // serialized ClassHierarchy
		arg = g.getOptarg();
		classHierarchyFilename = arg;
		try {
		    ObjectInputStream mIS = 
			new ObjectInputStream(new FileInputStream(arg)); 
		    classHierarchy = (ClassHierarchy) mIS.readObject();
		} catch (OptionalDataException e) {
		} catch (ClassNotFoundException e) {
		} catch (IOException e) {
 		    // something went wrong; rebuild the class
		    // hierarchy and write it later.
		    classHierarchy = null;
		    System.out.println("Error reading class "+
				       "hierarchy from " + 
				       classHierarchyFilename);
		}
		break;
	    case 'D':
		PRINT_DATA = true;
		break;
	    case 'O': 
		PRINT_ORIG = true;
		break; 
	    case 'P':
		PRE_REG_ALLOC = true;
		break;
	    case 'R':
		REG_ALLOC = true;
		break;
	    case 'L':
		LIVENESS_TEST = true;
		break;
	    case 'A':
		PRE_REG_ALLOC = PRINT_ORIG = 
		    REG_ALLOC = LIVENESS_TEST = true;
		break;
	    case 'c':
		arg = g.getOptarg();
		System.out.println("Compiling: " + arg);
		className = arg;
		break;
	    case '?':
	    case 'h':
		System.out.println("usage is: [-m <mapfile>] -c <class> [-DOPRLA]");
		System.out.println();
		printHelp();
		System.exit(-1);
	    default: 
		System.out.println("getopt() returned " + c);
		System.out.println("usage is: [-m <mapfile>] -c <class> [-DOPRLA]");
		System.out.println();
		printHelp();
		System.exit(-1);
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
		HCodeFactory regAllocCF = RegAlloc.codeFactory(sahcf, frame);
		HCode rhc = regAllocCF.convert(hmethod);
		if (rhc != null) rhc.print(out);
		out.println("\t--- end INSTR FORM (register allocation)  ---");
		out.println();
	    }

	    if (PRINT_DATA) {
		final Data data = new Data(hclass, frame);
		
		if (PRINT_ORIG) {
		    out.println("\t--- TREE FORM (for DATA)---");
		    data.print(out);
		    out.println("\t--- end TREE FORM (for DATA)---");
		}		

		final String scope = data.getName();
		final Instr instr = 
		    frame.codegen().gen(data, new InstrFactory() {
			private final TempFactory tf = Temp.tempFactory(scope);
			{ Util.assert(tf != null, "TempFactory cannot be null"); }
			private int id = 0;
			public TempFactory tempFactory() { return tf; }
			public HCode getParent() { return data; }
			public Frame getFrame() { return frame; }
			public synchronized int getUniqueID() { return id++; }
			public HMethod getMethod() { return null; }
		    });
	    
		Iterator iter = new UnmodifiableIterator() {
		    Set visited = new HashSet();
		    Stack stk = new Stack();
		    { stk.push(instr); visited.add(instr); }
		    public boolean hasNext(){return !stk.empty(); }
		    public Object next() {
			if (stk.empty()) throw new NoSuchElementException();
			Instr instr2 = (Instr) stk.pop();
			HCodeEdge[] next = instr2.succ();
			for (int j=next.length-1; j>=0; j--) {
			    if (!visited.contains(next[j].to())) {
				stk.push(next[j].to());
				visited.add(next[j].to());
			    }
			}
			return instr2;
		    }
		};
		out.println("\t--- INSTR FORM (for DATA)---");
		while(iter.hasNext()) { out.println( iter.next() ); }
		out.println("\t--- end INSTR FORM (for DATA)---");
		
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
	//out.println("\t\tBeginning creation of a StrongARM Code Factory ");
	long time = -System.currentTimeMillis();
	HCode hc = qhcf.convert(m); 
	if (classHierarchy == null) {
	    classHierarchy = new ClassHierarchy(m, qhcf);
	    Util.assert(classHierarchy != null, "How the hell...");
	}
	offmap = new OffsetMap32(classHierarchy);
	frame = new SAFrame(offmap);
	sahcf = SACode.codeFactory(qhcf, frame);
	time += System.currentTimeMillis();
	//out.println("\t\tFinished creation of a StrongARM Code "+
	//    "Factory.  Time (ms): " + time);
	return sahcf;
    }

    private static void printHelp() {
	out.println("-c <class> (required)");
	out.println("\tCompile <class>");
	out.println();

	out.println("-m <file> (optional)");
	out.println("\tLoads the ClassHierarchy object from <file>.");
	out.println("\tIn the event of an error loading the object,");
	out.println("\tconstructs a new ClassHierarchy and stores it");
	out.println("\tin <file>");
	
	out.println("-D");
	out.println("\tOutputs DATA information for <class>");

	out.println("-O");
	out.println("\tOutputs Original Tree IR for <class>");

	out.println("-P");
	out.println("\tOutputs Pre-Register Allocated Instr IR for <class>");

	out.println("-L");
	out.println("\tOutputs Liveness info for BasicBlocks of Instr IR");
	out.println("\tfor <class>");

	out.println("-R");
	out.println("\tOutputs Register Allocated Instr IR for <class>");

	out.println("-A");
	out.println("\tSame as -OPLR");
	
    }
}
