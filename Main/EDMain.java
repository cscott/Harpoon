// EDMain.java, created Mon Aug  2 19:41:06 1999 by pnkfelix
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
import java.util.Collection;
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
import java.io.Serializable;

import harpoon.ClassFile.Linker;
import harpoon.ClassFile.Loader;
import harpoon.ClassFile.Relinker;
import harpoon.Analysis.MetaMethods.MetaAllCallers;
import harpoon.Analysis.MetaMethods.MetaCallGraph;
import harpoon.Analysis.MetaMethods.MetaCallGraphImpl;
import harpoon.Analysis.MetaMethods.MetaMethod;
import harpoon.Util.BasicBlocks.CachingBBConverter;
import harpoon.Util.Collections.WorkSet;



/**
 * <code>EDMain</code> is a program to compile java classes to some
 * approximation of StrongARM assembly.  It is for development testing
 * purposes, not production use.
 * 
 * @author  Felix S. Klock II <pnkfelix@mit.edu>
 * @version $Id: EDMain.java,v 1.2 2002-02-25 21:06:05 cananian Exp $
 */
public class EDMain extends harpoon.IR.Registration {
 
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
    private static String  singleClass = null; // for testing single classes
    private static final int STRONGARM_BACKEND = 0;
    private static final int MIPS_BACKEND = 1;
    private static final int SPARC_BACKEND = 2;
    private static final int PRECISEC_BACKEND = 3;
    private static int     BACKEND = STRONGARM_BACKEND;
    
    private static java.io.PrintWriter out = 
	new java.io.PrintWriter(System.out, true);
        
    private static String className;
    private static String classHierarchyFilename;

    private static String methodName;

    private static ClassHierarchy classHierarchy;
    private static CallGraph callGraph;
    private static Frame frame;

    private static File ASSEM_DIR = null;

    private static boolean recycle=false, optimistic=false;


    static class Stage1 implements Serializable {
	HMethod mo;
	Linker linker;
	HCodeFactory hco;
	ClassHierarchy chx;
	Stage1(Linker linker) {
	    this.linker = linker; this.mo = mo;

	    Util.assert(className!= null, "must pass a class to be compiled");

	    HClass cls = linker.forName(className);
	    HMethod hm[] = cls.getDeclaredMethods();
	    for (int i=0; i<hm.length; i++) {
		if (hm[i].getName().equals("main")) {
		    mo = hm[i];
		    break;
		}
	    }

	    hco = 
		new harpoon.ClassFile.CachingCodeFactory(harpoon.IR.Quads.QuadNoSSA.codeFactory(), true);
	    
	    Collection cc = new WorkSet();
	    cc.addAll(harpoon.Backend.Runtime1.Runtime.runtimeCallableMethods
		      (linker));
	    cc.add(mo);
	    System.out.println("Getting ClassHierarchy");
	    chx = new QuadClassHierarchy(linker, cc, hco);
	}
    }
    static class Stage2 implements Serializable {
	HMethod mo;
	Linker linker;
	HCodeFactory hco;
	MetaCallGraph mcg;
	ClassHierarchy chx;
	Stage2(Stage1 stage1) {
	    linker = stage1.linker;
	    hco = stage1.hco;
	    chx = stage1.chx;//carry forward
	    mo = stage1.mo;
	    CachingBBConverter bbconv=new CachingBBConverter(stage1.hco);

	    // costruct the set of all the methods that might be called by 
	    // the JVM (the "main" method plus the methods which are called by
	    // the JVM before main) and next pass it to the MetaCallGraph
	    // constructor. [AS]
	    Set mroots = extract_method_roots(
	    harpoon.Backend.Runtime1.Runtime.runtimeCallableMethods(linker));
	    mroots.add(mo);

	    mcg = new MetaCallGraphImpl
		(new CachingCodeFactory(hco), stage1.chx, mroots);
	    //using hcf for now!
	}
    }
    static class Stage3 implements Serializable {
	HMethod mo;
	Linker linker;
	HCodeFactory hcfe;
	ClassHierarchy ch;
	MetaCallGraph mcg;
	Stage3(Stage2 stage2) {
	    linker = stage2.linker;
	    mcg = stage2.mcg;
	    mo = stage2.mo;
	    HCodeFactory ccf=harpoon.IR.Quads.QuadSSI.codeFactory(stage2.hco);
	    System.out.println("Doing CachingCodeFactory");
	    hcfe = new CachingCodeFactory(ccf, true);

	    Collection c = new WorkSet();
	    c.addAll(harpoon.Backend.Runtime1.Runtime.runtimeCallableMethods
		     (linker));
	    c.add(mo);
	    System.out.println("Getting ClassHierarchy");


	    ch = new QuadClassHierarchy(linker, c, hcfe);
	    ch = stage2.chx; // discard new ch, use old ch.

	    //	System.out.println("CALLABLE METHODS");
	    //	Iterator iterator=ch.callableMethods().iterator();
	    //	while (iterator.hasNext())
	    //	    System.out.println(iterator.next());
	    //System.out.println("Classes");
	    //iterator=ch.classes().iterator();
	    //while (iterator.hasNext())
	    //    System.out.println(iterator.next());
	    //System.out.println("Instantiated Classes");
	    //iterator=ch.instantiatedClasses().iterator();
	    //while (iterator.hasNext())
	    //    System.out.println(iterator.next());
	    //System.out.println("------------------------------------------");
	}
    }
    static class Stage4 implements Serializable {
	HMethod mo;
	Linker linker;
	HCodeFactory hcf;
	HMethod mconverted;
	Stage4(Stage3 stage3) {
	    linker = stage3.linker;
	    mo = stage3.mo;
	    CachingCodeFactory hcfe = (CachingCodeFactory) stage3.hcfe;
	    HCode hc = hcfe.convert(mo);
	    System.out.println("Starting ED");

	    harpoon.Analysis.EventDriven.EventDriven ed = 
		new harpoon.Analysis.EventDriven.EventDriven(hcfe, hc, stage3.ch, linker,optimistic,recycle);
	    this.mconverted=ed.convert(stage3.mcg);

	    this.hcf=hcfe;

	    System.out.println("Finished ED");
	}
    }

    static Object load(File f) throws IOException, ClassNotFoundException {
	Object o = null;
	System.out.println("Loading "+f+".");
	ObjectInputStream ois =
	    new ObjectInputStream(new FileInputStream(f));
	try {
	    o = ois.readObject();
	} catch (java.io.WriteAbortedException discard) { /* fail */ }
	ois.close();
	return o;
    }
    static void save(File f, Object o) throws IOException {
	System.out.println("Saving "+f+".");
	ObjectOutputStream oos =
	    new ObjectOutputStream(new FileOutputStream(f));
	oos.writeObject(o);
	oos.close();
    }

    public static void main(String[] args) throws IOException, ClassNotFoundException {
	Linker linker;
	parseOpts(args);

	File stage4file = new File("stage-4");
	Stage4 stage4 = null;
	if (stage4file.exists()) stage4=(Stage4)load(stage4file);
	if (stage4==null) {
	    File stage3file = new File("stage-3");
	    Stage3 stage3 = null;
	    if (stage3file.exists()) stage3=(Stage3)load(stage3file);
	    if (stage3==null) {
		File stage2file = new File("stage-2");
		Stage2 stage2 = null;
		if (stage2file.exists()) stage2=(Stage2)load(stage2file);
		if (stage2==null) {
		    File stage1file = new File("stage-1");
		    Stage1 stage1 = null;
		    if (stage1file.exists()) stage1=(Stage1)load(stage1file);
		    if (stage1==null) {
			linker = new Relinker(Loader.systemLinker);
			stage1 = new Stage1(linker); save(stage1file, stage1);
		    }
		    // done with stage 1.
		    stage2 = new Stage2(stage1); save(stage2file, stage2);
		}
		// done with stage 2.
		stage3 = new Stage3(stage2); save(stage3file, stage3);
	    }
	    // done with stage 3.
	    stage4 = new Stage4(stage3); save(stage4file, stage4);
	}
	// done with stage 4.
	linker = stage4.linker;
	HCodeFactory hcf = stage4.hcf;

	if (OPTIMIZE) {
	    hcf = harpoon.Analysis.Quads.SCC.SCCOptimize.codeFactory(hcf);
	}
	hcf = new harpoon.ClassFile.CachingCodeFactory(hcf, true);

	HClass hcl = linker.forName(className);
	HMethod[] hm = hcl.getDeclaredMethods();
	HMethod mainM = stage4.mconverted;

	Util.assert(mainM != null, "Class " + className + 
		    " has no main method");

	// create the target Frame way up here!
	// the frame specifies the combination of target architecture,
	// runtime, and allocation strategy we want to use.
	switch(BACKEND) {
	case STRONGARM_BACKEND:
	    frame = new harpoon.Backend.StrongARM.Frame(mainM);
	    break;
	case SPARC_BACKEND:
	    frame = new harpoon.Backend.Sparc.Frame(mainM);
	    break;
	case MIPS_BACKEND:
	    frame = new harpoon.Backend.MIPS.Frame(mainM);
	    break;
	case PRECISEC_BACKEND:
	    frame = new harpoon.Backend.PreciseC.Frame(mainM);
	    break;
	default: throw new Error("Unknown Backend: "+BACKEND);
	}

	if (classHierarchy == null) {
	    // ask the runtime which roots it requires.
	    Set roots = new java.util.HashSet
		(frame.getRuntime().runtimeCallableMethods());
	    // and our main method is a root, too...
	    roots.add(mainM);
	    classHierarchy = new QuadClassHierarchy(linker, roots, hcf);
	    Util.assert(classHierarchy != null, "How the hell...");
	}
	callGraph = new CallGraphImpl(classHierarchy, hcf);
	frame.setClassHierarchy(classHierarchy);
	frame.setCallGraph(callGraph);
	callGraph=null;// memory management.

	hcf = harpoon.IR.Tree.TreeCode.codeFactory(hcf, frame);
	hcf = frame.getRuntime().nativeTreeCodeFactory(hcf);
	hcf = harpoon.IR.Tree.CanonicalTreeCode.codeFactory(hcf, frame);
	hcf = harpoon.Analysis.Tree.AlgebraicSimplification.codeFactory(hcf);
	//hcf = harpoon.Analysis.Tree.DeadCodeElimination.codeFactory(hcf);
	hcf = harpoon.Analysis.Tree.JumpOptimization.codeFactory(hcf);
	hcf = new harpoon.ClassFile.CachingCodeFactory(hcf);
    
	HCodeFactory sahcf = frame.getCodeFactory(hcf);
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
		if (singleClass!=null && singleClass.equals(hclass.getName()))
		    continue;
		messageln("Compiling: " + hclass.getName());
		
		try {
		    String filename = frame.getRuntime().getNameMap().mangle(hclass);
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
		    outputClassData(linker, hclass, out);
		    
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
	    if (makefile.exists())
		System.err.println("WARNING: not overwriting pre-existing "+
				   "file "+makefile);
	    else if ((templateStream=ClassLoader.getSystemResourceAsStream
		      ("harpoon/Support/nativecode-makefile.template"))==null)
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
	} else { // ONLY_COMPILE_MAIN
	    // hcl is our class
	    // mainM is our method

	    try {
		String filename = frame.getRuntime().getNameMap().mangle(hcl);
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

	// free memory associated with this method's IR:
	hcf.clear(hmethod);
	sahcf.clear(hmethod);
    }
    
    public static void outputClassData(Linker linker, HClass hclass, PrintWriter out) 
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

    private static void message(String msg) {
	if(!QUIET) System.out.print(msg);
    }

    private static void messageln(String msg) {
	if(!QUIET) System.out.println(msg);
    }
    
    private static void parseOpts(String[] args) {

	Getopt g = new Getopt("EDMain", args, "m:c:o:DOPFHRLArphq1::C:");
	
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
		
	    case 'r':
		recycle=true;
		break;
	    case 'p':
		optimistic=true;
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
		    singleClass = optclassname;
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

	out.println("-r");
	out.println("\tRecycle continuations");

	out.println("-p");
	out.println("\toPtimistic");

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
    // extract the method roots from the set of all the roots
    // (methods and classes)
    private static Set extract_method_roots(Collection roots){
	Set mroots = new HashSet();
	for(Iterator it = roots.iterator(); it.hasNext(); ){
	    Object obj = it.next();
	    if(obj instanceof HMethod)
		mroots.add(obj);
	}
	return mroots;
    }
    private static void info(String str) {
	if(OUTPUT_INFO) out.println(str);
    }
}
