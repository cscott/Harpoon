
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
import harpoon.Backend.Generic.Frame;
import harpoon.Analysis.BasicBlock;
import harpoon.Analysis.ClassHierarchy;
import harpoon.Analysis.Quads.CallGraph;
import harpoon.Analysis.Quads.CallGraphImpl;
import harpoon.Analysis.Quads.QuadClassHierarchy;
import harpoon.Backend.Maps.NameMap;
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
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
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
 * @version $Id: SAMain.java,v 1.1.2.86 2000-07-13 14:11:59 jwhaley Exp $
 */
public class SAMain extends harpoon.IR.Registration {
 
    static boolean PRINT_ORIG = false;
    static boolean PRINT_DATA = false;
    static boolean PRE_REG_ALLOC = false;
    static boolean REG_ALLOC = false;
    static boolean ABSTRACT_REG_ALLOC = false;
    static boolean HACKED_REG_ALLOC = false;
    static boolean LIVENESS_TEST = false;
    static boolean OUTPUT_INFO = false;
    static boolean QUIET = false;
    static boolean OPTIMIZE = false;
    static boolean LOOPOPTIMIZE = false;

    static boolean ONLY_COMPILE_MAIN = false; // for testing small stuff
    static HClass  singleClass = null; // for testing single classes
    static final int STRONGARM_BACKEND = 0;
    static final int MIPS_BACKEND = 1;
    static final int SPARC_BACKEND = 2;
    static final int PRECISEC_BACKEND = 3;
    static int     BACKEND = STRONGARM_BACKEND;
    
    static Linker linker = Loader.systemLinker;

    static java.io.PrintWriter out = 
	new java.io.PrintWriter(System.out, true);
        
    static String className;
    static String classHierarchyFilename;

    static String methodName;

    static ClassHierarchy classHierarchy;
    static CallGraph callGraph;
    static Frame frame;

    static File ASSEM_DIR = null;
    static HCodeFactory hcf;

    static Set joinset=null, startset=null;


    public static void main(String[] args) {
	hcf = // default code factory.
	    new harpoon.ClassFile.CachingCodeFactory(
	    harpoon.IR.Quads.QuadNoSSA.codeFactory()
	    );

	parseOpts(args);
	Util.assert(className!= null, "must pass a class to be compiled");

	do_it();
    }

    public static void do_it() {

	if (SAMain.startset!=null)
	    hcf=harpoon.IR.Quads.ThreadInliner.codeFactory(hcf,SAMain.startset, SAMain.joinset);
	

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
	callGraph = new CallGraphImpl(classHierarchy, hcf);

	switch(BACKEND) {
	case STRONGARM_BACKEND:
	    frame = new harpoon.Backend.StrongARM.Frame
		(mainM, classHierarchy, callGraph);
	    break;
	case SPARC_BACKEND:
	    frame = new harpoon.Backend.Sparc.Frame
		(mainM, classHierarchy, callGraph);
	    break;
	case MIPS_BACKEND:
	    frame = new harpoon.Backend.MIPS.Frame
		(mainM, classHierarchy, callGraph);
	    break;
	case PRECISEC_BACKEND:
	    frame = new harpoon.Backend.PreciseC.Frame
		(mainM, classHierarchy, callGraph);
	    break;
	default: throw new Error("Unknown Backend: "+BACKEND);
	}
 
	if (LOOPOPTIMIZE) {
	    hcf=harpoon.IR.LowQuad.LowQuadSSI.codeFactory(hcf);
	    hcf=harpoon.Analysis.LowQuad.Loop.LoopOptimize.codeFactory(hcf);
	}
	hcf = harpoon.IR.LowQuad.LowQuadSSA.codeFactory(hcf);
	hcf = harpoon.IR.Tree.TreeCode.codeFactory(hcf, frame);
	hcf = frame.getRuntime().nativeTreeCodeFactory(hcf);
	hcf = harpoon.IR.Tree.CanonicalTreeCode.codeFactory(hcf, frame);
	hcf = harpoon.Analysis.Tree.AlgebraicSimplification.codeFactory(hcf);
	//hcf = harpoon.Analysis.Tree.DeadCodeElimination.codeFactory(hcf);
	hcf = harpoon.Analysis.Tree.JumpOptimization.codeFactory(hcf);
	hcf = new harpoon.ClassFile.CachingCodeFactory(hcf);
    
	HCodeFactory sahcf = frame.getCodeFactory(hcf);
	if (sahcf!=null)
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

	String filesuffix = (BACKEND==PRECISEC_BACKEND) ? ".c" : ".s";
	if (ONLY_COMPILE_MAIN) classes=Default.singletonIterator(hcl);
	if (singleClass!=null) classes=Default.singletonIterator(singleClass);

	if (true) { // this is only here because i don't want to re-indent
	            // all the code below this point.
	    while(classes.hasNext()) {
		HClass hclass = (HClass) classes.next();
		if (singleClass!=null && singleClass!=hclass) continue;//skip
		messageln("Compiling: " + hclass.getName());
		
		try {
		    String filename = frame.getRuntime().nameMap.mangle(hclass);
		    out = new PrintWriter
			(new BufferedWriter
			 (new FileWriter
			  (new File(ASSEM_DIR, filename + filesuffix))));
		    if (BACKEND==PRECISEC_BACKEND)
			out = new harpoon.Backend.PreciseC.TreeToC(out);
		    
		    HMethod[] hmarray = hclass.getDeclaredMethods();
		    Set hmset = new TreeSet(Arrays.asList(hmarray));
		    hmset.retainAll(methods);
		    Iterator hms = hmset.iterator();
		    if (ONLY_COMPILE_MAIN)
			hms = Default.singletonIterator(mainM);
		    message("\t");
		    while(!hclass.isInterface() && hms.hasNext()) {
			HMethod m = (HMethod) hms.next();
			message(m.getName());
			if (!Modifier.isAbstract(m.getModifiers()))
			    outputMethod(m, hcf, sahcf, out);
			if (hms.hasNext()) message(", ");
		    }
		    messageln("");
		    
		    //out.println();
		    messageln("Writing data for " + hclass.getName());
		    outputClassData(hclass, out);
		    
		    out.close();
		} catch (IOException e) {
		    System.err.println("Error outputting class "+
				       hclass.getName());
		    System.exit(-1);
		}
	    }
	    // put a proper makefile in the directory.
	    File makefile = new File(ASSEM_DIR, "Makefile");
	    InputStream templateStream;
	    String resourceName="harpoon/Support/nativecode-makefile.template";
	    if (BACKEND==PRECISEC_BACKEND)
		resourceName="harpoon/Support/precisec-makefile.template";
	    if (makefile.exists())
		System.err.println("WARNING: not overwriting pre-existing "+
				   "file "+makefile);
	    else if ((templateStream=ClassLoader.getSystemResourceAsStream
		      (resourceName))==null)
		System.err.println("WARNING: can't find Makefile template.");
	    else try {
		BufferedReader in = new BufferedReader
		    (new InputStreamReader(templateStream));
		out = new PrintWriter
		    (new BufferedWriter(new FileWriter(makefile)));
		String line;
		while ((line=in.readLine()) != null)
		    out.println(line);
		in.close(); out.close();
	    } catch (IOException e) {
		System.err.println("Error writing "+makefile+".");
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
	    
	    info("\t--- INSTR FORM (liveness check)  ---");
	    if (hc != null) {
		BasicBlock.Factory bbFact = 
		    new BasicBlock.Factory(hc, CFGrapher.DEFAULT);
		// wrong but makes it compile for now
		LiveTemps livevars = 
		    new LiveTemps(bbFact, Collections.EMPTY_SET); 
		InstrSolver.worklistSolver(bbFact.blockSet().iterator(),
					   livevars);
		out.println(livevars.dump());
	    } else {
		info("null returned for " + hmethod);
	    }
	    info("\t--- end INSTR FORM (liveness check)  ---");
	    out.flush();
	}
	
	if (ABSTRACT_REG_ALLOC) {
	    HCode hc = sahcf.convert(hmethod);
	    
	    info("\t--- INSTR FORM (register allocation)  ---");
	    HCodeFactory regAllocCF = RegAlloc.abstractSpillFactory(sahcf, frame);
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
		new harpoon.Backend.CSAHack.RegAlloc.Code
		(hmethod, (Instr) hc.getRootElement(),
		 ((harpoon.IR.Assem.Code)hc).getDerivation(), frame);
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

	if (BACKEND==PRECISEC_BACKEND) {
	    HCode hc = hcf.convert(hmethod);
	    if (hc!=null)
		((harpoon.Backend.PreciseC.TreeToC)out).translate(hc);
	}

	// free memory associated with this method's IR:
	hcf.clear(hmethod);
	if (sahcf!=null) sahcf.clear(hmethod);
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
	
	if (BACKEND==PRECISEC_BACKEND)
	    ((harpoon.Backend.PreciseC.TreeToC)out).translate(data);

	if (!PRE_REG_ALLOC && !LIVENESS_TEST && !REG_ALLOC && !HACKED_REG_ALLOC) continue;

	if (data.getRootElement()==null) continue; // nothing to do here.

	final Instr instr = 
	    frame.getCodeGen().genData((harpoon.IR.Tree.Data)data, new InstrFactory() {
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

    protected static void message(String msg) {
	if(!QUIET) System.out.print(msg);
    }

    protected static void messageln(String msg) {
	if(!QUIET) System.out.println(msg);
    }
    
    protected static void parseOpts(String[] args) {

	Getopt g = new Getopt("SAMain", args, "m:i:s:b:c:o:DOPFHRLlABhq1::C:");
	
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
	    case 's':
		arg=g.getOptarg();
		try {
		    ObjectInputStream ois =
			new ObjectInputStream(new FileInputStream(arg));
		    hcf = (HCodeFactory) ois.readObject();
		    linker = (Linker) ois.readObject();
		    startset = (Set) ois.readObject();
		    joinset = (Set) ois.readObject();
		    ois.close();
		} catch (Exception e) {
		    System.out.println(e + " was thrown");
		    System.exit(-1);
		}
		break;
	    case 'i':
		arg=g.getOptarg();
		System.out.println("loading "+arg);
		try {
		    ObjectInputStream ois =
			new ObjectInputStream(new FileInputStream(arg));
		    hcf=(HCodeFactory)ois.readObject();
		    linker=(Linker)ois.readObject();
		    ois.close();
		} catch (Exception e) {
		    System.out.println(e + " was thrown");
		    System.exit(-1);
		}
		break;
	    case 'l':
		LOOPOPTIMIZE=true;
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
	    case 'B':
		OUTPUT_INFO = ABSTRACT_REG_ALLOC = true;
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
	    case 'b': {
		String backendName = g.getOptarg().toLowerCase().intern();
		if (backendName == "strongarm")
		    BACKEND = STRONGARM_BACKEND;
		if (backendName == "sparc")
		    BACKEND = SPARC_BACKEND;
		if (backendName == "mips")
		    BACKEND = MIPS_BACKEND;
		if (backendName == "precisec")
		    BACKEND = PRECISEC_BACKEND;
		break;
	    }
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
	" [-DOPRLABhq] [-o <assembly output directory>]";

    protected static void printHelp() {
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

	out.println("-B");
	out.println("\tOutputs Abstract Register Allocated Instr IR for <class>");

	out.println("-L");
	out.println("\tOutputs Liveness info for BasicBlocks of Instr IR");
	out.println("\tfor <class>");

	out.println("-R");
	out.println("\tOutputs Register Allocated Instr IR for <class>");

	out.println("-A");
	out.println("\tSame as -OPLR");

	out.println("-i <filename>");
	out.println("Read CodeFactory in from FileName");

	out.println("-b <backend name>");
	out.println("\t Supported backends are StrongARM (default), MIPS, " +
		    "Sparc, or PreciseC");

	out.println("-l");
	out.println("Turn on Loop Optimizations");

	out.println("-q");
	out.println("\tTurns on quiet mode (status messages are not output)");

	out.println("-1<optional class name>"); 
	out.println("\tCompiles only a single method or class.  Without a classname, only compiles <class>.main()");
	out.println("\tNote that you may not have whitespace between the '-1' and the classname");

	out.println("-h");
	out.println("\tPrints out this help message");
	
    }

    protected static void info(String str) {
	if(OUTPUT_INFO) out.println(str);
    }
}
