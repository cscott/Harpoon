// SAMain.java, created Mon Aug  2 19:41:06 1999 by pnkfelix
// Copyright (C) 1999 Felix S. Klock II <pnkfelix@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Main;

import harpoon.ClassFile.CachingCodeFactory;
import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HCode;
import harpoon.ClassFile.HCodeElement;
import harpoon.ClassFile.HCodeFactory;
import harpoon.ClassFile.HData;
import harpoon.ClassFile.HMethod;
import harpoon.ClassFile.Linker;
import harpoon.ClassFile.Loader;
import harpoon.IR.Properties.CFGrapher;
import harpoon.IR.Tree.Data;
import harpoon.IR.Assem.Instr;
import harpoon.IR.Assem.InstrFactory;
import harpoon.Temp.Temp;
import harpoon.Temp.TempFactory;
import harpoon.Analysis.DataFlow.LiveTemps;
import harpoon.Analysis.DataFlow.InstrSolver;
import harpoon.Analysis.Instr.RegAlloc;
import harpoon.Backend.StrongARM.Code;
import harpoon.Backend.StrongARM.Frame;
import harpoon.Analysis.BasicBlock;
import harpoon.Analysis.ClassHierarchy;
import harpoon.Analysis.Quads.CallGraph;
import harpoon.Analysis.Quads.QuadClassHierarchy;
import harpoon.Backend.Maps.NameMap;
import harpoon.Backend.Maps.OffsetMap;
import harpoon.Util.CombineIterator;
import harpoon.Util.Default;
import harpoon.Util.Util;

import gnu.getopt.Getopt;

import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OptionalDataException;
import java.io.PrintStream;
import java.io.PrintWriter;


/**
 * <code>SAMain</code> is a program to compile java classes to some
 * approximation of StrongARM assembly.  It is for development testing
 * purposes, not production use.
 * 
 * @author  Felix S. Klock II <pnkfelix@mit.edu>
 * @version $Id: SAMain.java,v 1.1.2.55 2000-01-17 11:52:10 cananian Exp $
 */
public class SAMain extends harpoon.IR.Registration {
 
    private static boolean PRINT_ORIG = false;
    private static boolean PRINT_DATA = false;
    private static boolean PRE_REG_ALLOC = false;
    private static boolean REG_ALLOC = false;
    private static boolean HACKED_REG_ALLOC = false;
    private static boolean LIVENESS_TEST = false;
    private static boolean OUTPUT_INFO = false;
    private static boolean QUIET = false;
    private static boolean OPTIMIZE = false;

    private static boolean ONLY_COMPILE_MAIN = false; // for testing small stuff
    private static HClass  singleClass = null; // for testing single classes
    
    private static Linker linker = Loader.systemLinker;

    private static java.io.PrintWriter out = 
	new java.io.PrintWriter(System.out, true);
        
    private static String className;
    private static String classHierarchyFilename;

    private static String methodName;

    private static ClassHierarchy classHierarchy;
    private static CallGraph callGraph;
    private static Frame frame;
    private static OffsetMap offmap;

    private static File ASSEM_DIR = null;


    public static void main(String[] args) {
	HCodeFactory hcf = // default code factory.
	    new harpoon.ClassFile.CachingCodeFactory(
	    harpoon.IR.Quads.QuadNoSSA.codeFactory()
	    );

	parseOpts(args);
	Util.assert(className!= null, "must pass a class to be compiled");

	if (OPTIMIZE) {
	    hcf = harpoon.IR.Quads.QuadSSI.codeFactory(hcf);
	    hcf = harpoon.Analysis.Quads.SCC.SCCOptimize.codeFactory(hcf);
	    hcf = new harpoon.ClassFile.CachingCodeFactory(hcf);
	}

	HClass hcl = linker.forName(className);
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
	    // XXX: this is non-ideal!  Really, we want to use a non-static
	    // method in Frame.getRuntime() to initialize the class hierarchy
	    // roots with.  *BUT* Frame requires a class hierarchy in its
	    // constructor.  How do we ask the runtime which roots to use
	    // before the runtime's been created?
	    // Punting on this question for now, and using a hard-coded
	    // static method. [CSA 27-Oct-1999]
	    Set roots =new java.util.HashSet
		(harpoon.Backend.Runtime1.Runtime.runtimeCallableMethods(linker));
	    // and our main method is a root, too...
	    roots.add(mainM);
	    classHierarchy = new QuadClassHierarchy(linker, roots, hcf);
	    Util.assert(classHierarchy != null, "How the hell...");
	}
	callGraph = new CallGraph(classHierarchy, hcf);
	frame = new Frame(mainM, classHierarchy, callGraph);
	offmap = frame.getOffsetMap();
	hcf = harpoon.IR.Tree.TreeCode.codeFactory(hcf, frame);
	hcf = frame.getRuntime().nativeTreeCodeFactory(hcf);
	hcf = harpoon.IR.Tree.CanonicalTreeCode.codeFactory(hcf, frame);
	//hcf = harpoon.IR.Tree.OptimizedTreeCode.codeFactory(hcf, frame);
	hcf = new harpoon.ClassFile.CachingCodeFactory(hcf);
    
	HCodeFactory sahcf = Code.codeFactory(hcf, frame);
	sahcf = new harpoon.ClassFile.CachingCodeFactory(sahcf);

	if (classHierarchyFilename != null) {
	    try {
		ObjectOutputStream mOS = new
		    ObjectOutputStream(new FileOutputStream
				       (classHierarchyFilename));
		mOS.writeObject(classHierarchy);
		mOS.close();
	    } catch (IOException e) {
		System.err.println("Error outputting class "+
				   "hierarchy to " + 
				   classHierarchyFilename);
	    }
	}

	Set methods = classHierarchy.callableMethods();
	Iterator classes = new TreeSet(classHierarchy.classes()).iterator();


	if (singleClass!=null || !ONLY_COMPILE_MAIN) {
	    while(classes.hasNext()) {
		HClass hclass = (HClass) classes.next();
		if (singleClass!=null && singleClass!=hclass) continue;//skip
		messageln("Compiling: " + hclass.getName());
		
		try {
		    String filename = frame.getRuntime().nameMap.mangle(hclass);
		    out = new PrintWriter
			(new BufferedWriter
			 (new FileWriter
			  (new File(ASSEM_DIR, filename + ".s"))));
		    
		    HMethod[] hmarray = hclass.getDeclaredMethods();
		    Set hmset = new TreeSet(Arrays.asList(hmarray));
		    hmset.retainAll(methods);
		    Iterator hms = hmset.iterator();
		    message("\t");
		    while(!hclass.isInterface() && hms.hasNext()) {
			HMethod m = (HMethod) hms.next();
			message(m.getName());
			if (!Modifier.isAbstract(m.getModifiers()))
			    outputMethod(m, hcf, sahcf, out);
			if (hms.hasNext()) message(", ");
		    }
		    messageln("");
		    
		    out.println();
		    messageln("Writing data for " + hclass.getName());
		    outputClassData(hclass, out);
		    
		    out.close();
		} catch (IOException e) {
		    System.err.println("Error outputting class "+
				       hclass.getName());
		    System.exit(-1);
		}
	    }
	} else { // ONLY_COMPILE_MAIN
	    // hcl is our class
	    // mainM is our method

	    try {
		String filename = frame.getRuntime().nameMap.mangle(hcl);
		out = new PrintWriter
		    (new BufferedWriter
		     (new FileWriter
		      (new File(ASSEM_DIR, filename + ".s"))));
		message("\t");
		message(mainM.getName());
		outputMethod(mainM, hcf, sahcf, out);
		messageln("");

		out.println();
		out.close();
	    } catch (IOException e) {
		System.err.println("Error outputting class "+
				   hcl.getName());
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
	    out.flush();
	}
	    
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
	    out.flush();
	}
	
	if (LIVENESS_TEST) {
	    HCode hc = sahcf.convert(hmethod);
	    
	    info("\t--- INSTR FORM (basic block check)  ---");
	    if (hc != null) {
		HCodeElement root = hc.getRootElement();
		BasicBlock block = 
		    BasicBlock.computeBasicBlocks(root, CFGrapher.DEFAULT);
		Iterator iter= BasicBlock.basicBlockIterator(block);

		// wrong but makes it compile for now
		LiveTemps livevars = 
		    new LiveTemps(iter, Collections.EMPTY_SET); 
		InstrSolver.worklistSolver
		    (BasicBlock.basicBlockIterator(block), livevars);
		out.println(livevars.dump());
	    } else {
		info("null returned for " + hmethod);
	    }
	    info("\t--- end INSTR FORM (basic block check)  ---");
	    out.flush();
	}
	
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
	    out.flush();
	}

	if (HACKED_REG_ALLOC) {
	    HCode hc = sahcf.convert(hmethod);
	    info("\t--- INSTR FORM (hacked register allocation)  ---");
	    HCode rhc = (hc==null) ? null :
		new harpoon.Backend.CSAHack.RegAlloc.Code(hmethod, (Instr)
					       hc.getRootElement(), frame);
	    if (rhc != null) {
		info("Codeview \""+rhc.getName()+"\" for "+
		     rhc.getMethod()+":");
		rhc.print(out);
	    } else {
		info("null returned for " + hmethod);
	    }
	    info("\t--- end INSTR FORM (register allocation)  ---");
	    out.println();
	    out.flush();
	}

	// free memory associated with this method's IR:
	hcf.clear(hmethod);
	sahcf.clear(hmethod);
    }
    
    public static void outputClassData(HClass hclass, PrintWriter out) 
	throws IOException {
      Iterator it=frame.getRuntime().classData(hclass).iterator();
      // output global data with the java.lang.Object class.
      if (hclass==linker.forName("java.lang.Object")) {
	  HData data=frame.getLocationFactory().makeLocationData(frame);
	  it=new CombineIterator(new Iterator[]
				 { it, Default.singletonIterator(data) });
      }
      while (it.hasNext() ) {
	final Data data = (Data) it.next();
	
	if (PRINT_ORIG) {
	    info("\t--- TREE FORM (for DATA)---");
	    data.print(out);
	    info("\t--- end TREE FORM (for DATA)---");
	}		
	
	if (!PRE_REG_ALLOC && !LIVENESS_TEST && !REG_ALLOC && !HACKED_REG_ALLOC) continue;

	if (data.getRootElement()==null) continue; // nothing to do here.

	final Instr instr = 
	    frame.getCodeGen().gen((harpoon.IR.Tree.Data)data, new InstrFactory() {
		private int id = 0;
		public TempFactory tempFactory() { return null; }
		public HCode getParent() { return null/*data*/; }// FIXME!
		public harpoon.Backend.Generic.Frame getFrame() { return frame; }
		public synchronized int getUniqueID() { return id++; }
		public HMethod getMethod() { return null; }
		public int hashCode() { return data.hashCode(); }
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
      }
    }

    private static void message(String msg) {
	if(!QUIET) System.out.print(msg);
    }

    private static void messageln(String msg) {
	if(!QUIET) System.out.println(msg);
    }
    
    private static void parseOpts(String[] args) {

	Getopt g = new Getopt("SAMain", args, "m:c:o:DOPFHRLAhq1::C:");
	
	int c;
	String arg;
	while((c = g.getopt()) != -1) {
	    switch(c) {
	    case 'm': // serialized ClassHierarchy
		arg = g.getOptarg();
		classHierarchyFilename = arg;
		try {
		    ObjectInputStream mIS = 
			new ObjectInputStream
			(new BufferedInputStream
			 (new FileInputStream(arg)));
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
	    case 'H':
		HACKED_REG_ALLOC = true;
		break;
	    case 'R':
		REG_ALLOC = true;
		break;
	    case 'L':
		OUTPUT_INFO = LIVENESS_TEST = true;
		break;
	    case 'F':
		OPTIMIZE = true;
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
	    case 'M':
		methodName = g.getOptarg();
		break;
	    case 'q':
		QUIET = true;
		break;
	    case 'C':
	    case '1':  
		String optclassname = g.getOptarg();
		if (optclassname!=null) {
		    singleClass = linker.forName(optclassname);
		} else {
		    ONLY_COMPILE_MAIN = true;
		}
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

	out.println("-1 <optional class name>"); 
	out.println("\tCompiles only a single method or class.  Without a classname, only compiles main()");

	out.println("-h");
	out.println("\tPrints out this help message");
	
    }

    private static void info(String str) {
	if(OUTPUT_INFO) out.println(str);
    }
}
