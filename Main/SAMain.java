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
import harpoon.Util.ReverseIterator;
import harpoon.Util.Util;

import gnu.getopt.Getopt;

import java.util.Iterator;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.Vector;
import java.util.Stack;
import java.util.Arrays;
import java.util.Collections;
import java.util.NoSuchElementException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.IOException;
import java.io.OptionalDataException;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.File;
import java.io.PrintWriter;


/**
 * <code>SAMain</code> is a program to compile java classes to some
 * approximation of StrongARM assembly.  It is for development testing
 * purposes, not production use.
 * 
 * @author  Felix S. Klock II <pnkfelix@mit.edu>
 * @version $Id: SAMain.java,v 1.1.2.17 1999-09-02 19:11:20 pnkfelix Exp $
 */
public class SAMain extends harpoon.IR.Registration {
 
    private static boolean PRINT_ORIG = false;
    private static boolean PRINT_DATA = false;
    private static boolean PRE_REG_ALLOC = false;
    private static boolean REG_ALLOC = false;
    private static boolean LIVENESS_TEST = false;
    private static boolean OUTPUT_INFO = false;
    private static boolean QUIET = false;
    
    private static java.io.PrintWriter out = 
	new java.io.PrintWriter(System.out, true);;
        
    private static String className;
    private static String classHierarchyFilename;
    private static ClassHierarchy classHierarchy;
    private static Frame frame;
    private static OffsetMap offmap;

    private static File ASSEM_DIR = null;


    public static void main(String[] args) {
	HCodeFactory hcf = // default code factory.
	    // harpoon.Analysis.QuadSSA.SCC.SCCOptimize.codeFactory
	    (harpoon.IR.Quads.QuadSSA.codeFactory()
	     );

	parseOpts(args);
	Util.assert(className!= null, "must pass a class to be compiled");


	HClass hcl = HClass.forName(className);
	HMethod hm[] = hcl.getDeclaredMethods();
	HMethod mainM = null;
	for (int j=0; j<hm.length; j++) {
	    if (hm[j].getName().equalsIgnoreCase("main")) {
		mainM = hm[j];
		break;
	    }
	}
	
	Util.assert(mainM != null, "Class " + className + 
		    " has no main method");

	if (classHierarchy == null) {
	    classHierarchy = new ClassHierarchy(mainM, hcf);
	    Util.assert(classHierarchy != null, "How the hell...");
	}
	offmap = new OffsetMap32(classHierarchy);
	frame = new SAFrame(offmap);
	hcf = harpoon.IR.Tree.TreeCode.codeFactory(hcf, frame);
    
	HCodeFactory sahcf = SACode.codeFactory(hcf, frame);

	if (classHierarchyFilename != null) {
	    try {
		ObjectOutputStream mOS = new
		    ObjectOutputStream(new FileOutputStream
				       (classHierarchyFilename));
		mOS.writeObject(classHierarchy);
		mOS.flush();
	    } catch (IOException e) {
		System.err.println("Error outputting class "+
				   "hierarchy to " + 
				   classHierarchyFilename);
	    }
	}

	Set methods = classHierarchy.callableMethods();
	Iterator classes = classHierarchy.classes().iterator();

	while(classes.hasNext()) {
	    HClass hclass = (HClass) classes.next();
	    messageln("Compiling: " + hclass.getName());

	    try {
		out = new PrintWriter
		    (new FileWriter
		     (new File(ASSEM_DIR, 
			       hclass.getName() + ".s")));
		
		HMethod[] hmarray = hclass.getDeclaredMethods();
		HashSet hmset = new HashSet(Arrays.asList(hmarray));
		hmset.retainAll(methods);
		Iterator hms = hmset.iterator();
		message("\t");
		while(hms.hasNext()) {
		    HMethod m = (HMethod) hms.next();
		    message(m.getName());
		    if (hms.hasNext()) message(", ");
		    outputMethod(m, hcf, sahcf, out);
		}
		messageln("");

		out.println();
		messageln("Writing data for " + hclass.getName());
		outputClassData(hclass, out);

		out.flush();

	    } catch (IOException e) {
		System.err.println("Error outputting class "+
				   hclass.getName());
		System.exit(-1);
	    }
	}

    }

    public static void outputMethod(final HMethod hmethod, 
				    final HCodeFactory hcf,
				    final HCodeFactory sahcf,
				    final PrintWriter out) 
	throws IOException {
	if (PRINT_ORIG) {
	    HCode hc = hcf.convert(hmethod);
	    
	    info("\t--- TREE FORM ---");
	    if (hc!=null) hc.print(out); 
	    else 
		info("null returned for " + hmethod);
	    info("\t--- end TREE FORM ---");
	    out.println();
	}
	    
	out.flush();

	if (PRE_REG_ALLOC) {
	    HCode hc = sahcf.convert(hmethod);
	    
	    info("\t--- INSTR FORM (no register allocation)  ---");
	    if (hc!=null) {
		info("Codeview \""+hc.getName()+"\" for "+
		     hc.getMethod()+":");
		hc.print(out);
	    } else {
		info("null returned for " + hmethod);
	    } 
	    info("\t--- end INSTR FORM (no register allocation)  ---");
	    out.println();
	}
	
	out.flush();
	
	if (LIVENESS_TEST) {
	    HCode hc = sahcf.convert(hmethod);
	    
	    info("\t--- INSTR FORM (basic block check)  ---");
	    if (hc != null) {
		HCodeElement root = hc.getRootElement();
		BasicBlock block = 
		    BasicBlock.computeBasicBlocks((HasEdges)root);
		Iterator iter= BasicBlock.basicBlockIterator(block);

		// wrong but makes it compile for now
		LiveVars livevars = 
		    new LiveVars(iter, Collections.EMPTY_SET); 
		InstrSolver.worklistSolver
		    (BasicBlock.basicBlockIterator(block), livevars);
		out.println(livevars.dump());
	    } else {
		info("null returned for " + hmethod);
	    }
	    info("\t--- end INSTR FORM (basic block check)  ---");
	}
	
	out.flush();
	
	if (REG_ALLOC) {
	    HCode hc = sahcf.convert(hmethod);
	    
	    info("\t--- INSTR FORM (register allocation)  ---");
	    HCodeFactory regAllocCF = RegAlloc.codeFactory(sahcf, frame);
	    HCode rhc = regAllocCF.convert(hmethod);
	    if (rhc != null) {
		info("Codeview \""+rhc.getName()+"\" for "+
		     rhc.getMethod()+":");
		rhc.print(out);
	    } else {
		info("null returned for " + hmethod);
	    }
	    info("\t--- end INSTR FORM (register allocation)  ---");
	    out.println();
	}
	
	out.flush();
	
    }
    
    public static void outputClassData(HClass hclass, PrintWriter out) 
	throws IOException {
	final Data data = new Data(hclass, frame);
	
	if (PRINT_ORIG) {
	    info("\t--- TREE FORM (for DATA)---");
	    data.print(out);
	    info("\t--- end TREE FORM (for DATA)---");
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
	
	Util.assert(instr != null, "what the hell...");
	// messageln("First data instruction " + instr);


	/* trying different method. */
	Instr di = instr; 
	info("\t--- INSTR FORM (for DATA)---");
	while(di!=null) { 
	    //messageln("Writing " + di);
	    out.println(di); 
	    di = di.getNext(); 
	}
	info("\t--- end INSTR FORM (for DATA)---");

	/* old method below. */
	/*
	Iterator iter = new UnmodifiableIterator() {
	    Set visited = new HashSet();
	    Stack stk = new Stack();
	    { stk.push(instr); visited.add(instr); }
	    public boolean hasNext(){return !stk.empty(); }
	    public Object next() {
		if (stk.empty()) throw new NoSuchElementException();
		Instr instr2 = (Instr) stk.pop();
		Iterator succIter = 
		   new ReverseIterator(instr2.succC().iterator());
		while(succIter.hasNext()) {
		    HCodeEdge edge = (HCodeEdge) succIter.next();
		    if (!visited.contains(edge.to())) {
			stk.push(edge.to());
			visited.add(edge.to());
		    }
		}
		return instr2;
	    }
	};
	info("\t--- INSTR FORM (for DATA)---");
	while(iter.hasNext()) { out.println( iter.next() ); }
	info("\t--- end INSTR FORM (for DATA)---");
	*/
	
    }

    private static void message(String msg) {
	if(!QUIET) System.out.print(msg);
    }

    private static void messageln(String msg) {
	if(!QUIET) System.out.println(msg);
    }
    
    private static void parseOpts(String[] args) {
	Getopt g = new Getopt("SAMain", args, "m:c:o:DOPRLAhq");
	
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
		    System.err.println("Error reading class "+
				       "hierarchy from " + 
				       classHierarchyFilename);
		}
		break;
	    case 'D':
		OUTPUT_INFO = PRINT_DATA = true;
		break;
	    case 'O': 
		OUTPUT_INFO = PRINT_ORIG = true;
		break; 
	    case 'P':
		OUTPUT_INFO = PRE_REG_ALLOC = true;
		break;
	    case 'R':
		REG_ALLOC = true;
		break;
	    case 'L':
		OUTPUT_INFO = LIVENESS_TEST = true;
		break;
	    case 'A':
		OUTPUT_INFO = PRE_REG_ALLOC = PRINT_ORIG = 
		    REG_ALLOC = LIVENESS_TEST = true;
		break;
	    case 'o':
		ASSEM_DIR = new File(g.getOptarg());
		Util.assert(ASSEM_DIR.isDirectory(), ""+ASSEM_DIR+" must be a directory");
		break;
	    case 'c':
		className = g.getOptarg();
		break;
	    case 'q':
		QUIET = true;
		break;
	    case '?':
	    case 'h':
		System.out.println(usage);
		System.out.println();
		printHelp();
		System.exit(-1);
	    default: 
		System.out.println("getopt() returned " + c);
		System.out.println(usage);
		System.out.println();
		printHelp();
		System.exit(-1);
	    }
	}
    }

    static final String usage = 
	"usage is: [-m <mapfile>] -c <class>"+
	" [-DOPRLAhq] [-o <assembly output directory>]";

    private static void printHelp() {
	out.println("-c <class> (required)");
	out.println("\tCompile <class>");
	out.println();

	out.println("-m <file> (optional)");
	out.println("\tLoads the ClassHierarchy object from <file>.");
	out.println("\tIn the event of an error loading the object,");
	out.println("\tconstructs a new ClassHierarchy and stores it");
	out.println("\tin <file>");

	out.println("-o <dir> (optional)");
	out.println("\tOutputs the program text to files within <dir>.");
	
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

	out.println("-q");
	out.println("\tTurns on quiet mode (status messages are not output)");

	out.println("-h");
	out.println("\tPrints out this help message");
	
    }

    private static void info(String str) {
	if(OUTPUT_INFO) out.println(str);
    }
}
