// SAMain.java, created Mon Aug  2 19:41:06 1999 by pnkfelix
// Copyright (C) 1999 Felix S. Klock II <pnkfelix@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Main;

import harpoon.Analysis.AbstractClassFixupRelinker;
import harpoon.ClassFile.CachingCodeFactory;
import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HCode;
import harpoon.ClassFile.HCodeElement;
import harpoon.ClassFile.HCodeFactory;
import harpoon.ClassFile.HData;
import harpoon.ClassFile.HMethod;
import harpoon.ClassFile.Linker;
import harpoon.ClassFile.Loader;
import harpoon.ClassFile.NoSuchClassException;
import harpoon.ClassFile.Relinker;
import harpoon.IR.Properties.CFGrapher;
import harpoon.IR.Tree.Data;
import harpoon.IR.Assem.Instr;
import harpoon.IR.Assem.InstrFactory;
import harpoon.IR.Quads.QuadNoSSA;
import harpoon.Temp.Temp;
import harpoon.Temp.TempFactory;
import harpoon.Analysis.DataFlow.LiveTemps;
import harpoon.Analysis.DataFlow.InstrSolver;
import harpoon.Analysis.Instr.RegAlloc;
import harpoon.Analysis.Instr.RegAllocOpts;
import harpoon.Backend.Generic.Frame;
import harpoon.Analysis.BasicBlock;
import harpoon.Analysis.ClassHierarchy;
import harpoon.Analysis.CallGraph;
import harpoon.Analysis.Quads.CallGraphImpl;
import harpoon.Analysis.Quads.CallGraphImpl2;
import harpoon.Analysis.Quads.QuadClassHierarchy;

import harpoon.Analysis.MetaMethods.MetaAllCallers;
import harpoon.Analysis.MetaMethods.MetaCallGraph;
import harpoon.Analysis.MetaMethods.MetaCallGraphImpl;
import harpoon.Analysis.MetaMethods.MetaMethod;
import harpoon.Util.BasicBlocks.CachingBBConverter;

import harpoon.Backend.Maps.NameMap;
import harpoon.Util.CombineIterator;
import harpoon.Util.Default;
import harpoon.Util.ParseUtil;
import harpoon.Util.Util;

import harpoon.Backend.Runtime1.AllocationStrategy;
import harpoon.Backend.Runtime1.AllocationStrategyFactory;

import harpoon.IR.Quads.Quad;
import harpoon.Instrumentation.AllocationStatistics.AllocationNumbering;
import harpoon.Instrumentation.AllocationStatistics.AllocationNumberingStub;
import harpoon.Instrumentation.AllocationStatistics.InstrumentAllocs;
import harpoon.Instrumentation.AllocationStatistics.InstrumentAllocs2;
import harpoon.Instrumentation.AllocationStatistics.AllocationStatistics;

import harpoon.Analysis.PointerAnalysis.Debug;
import harpoon.Analysis.PreciseGC.MRA;
import harpoon.Analysis.PreciseGC.WriteBarrierPrePass;
import harpoon.Analysis.PreciseGC.WriteBarrierStats;
import harpoon.Analysis.PreciseGC.WriteBarrierTreePass;
import harpoon.Analysis.Realtime.Realtime;

import harpoon.Analysis.SizeOpt.BitWidthAnalysis;

import harpoon.Analysis.Transactions.SyncTransformer;

import gnu.getopt.Getopt;
import gnu.getopt.LongOpt;

import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collections;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.Map;
import java.util.HashMap;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OptionalDataException;
import java.io.PrintStream;
import java.io.PrintWriter;

import harpoon.Analysis.MemOpt.IncompatibilityAnalysis;
import harpoon.Analysis.MemOpt.PreallocOpt;

/**
 * <code>SAMain</code> is a program to compile java classes to some
 * approximation of StrongARM assembly.  It is for development testing
 * purposes, not production use.
 * 
 * @author  Felix S. Klock II <pnkfelix@mit.edu>
 * @version $Id: SAMain.java,v 1.37 2003-03-16 16:38:20 salcianu Exp $
 */
public class SAMain extends harpoon.IR.Registration {
 
    static boolean EVENTDRIVEN = false;
    static boolean recycle = false;
    static boolean optimistic = false;

    static boolean PRINT_ORIG = false;
    static boolean PRINT_DATA = false;
    static boolean PRE_REG_ALLOC = false;
    static boolean REG_ALLOC = false;
    static boolean ABSTRACT_REG_ALLOC = false;
    static boolean HACKED_REG_ALLOC = false;
    static boolean LOCAL_REG_ALLOC = false;
    static boolean OUTPUT_INFO = false;
    static boolean QUIET = false;
    static boolean OPTIMIZE = false;
    static boolean LOOPOPTIMIZE = false;
    static boolean USE_OLD_CLINIT_STRATEGY = false;
    static boolean INSTRUMENT_ALLOCS = false;
    // 1 - Brian's instrumentation
    // 2 - Alex's  instrumentation
    static int INSTRUMENT_ALLOCS_TYPE = 2;
    // TODO: add command line options about instrumenting syncs/calls
    static boolean INSTRUMENT_SYNCS = false;
    static boolean INSTRUMENT_CALLS = false;
    static boolean INSTRUMENT_ALLOCS_STUB = false;
    static String IFILE=null;
    static boolean ROLE_INFER= false;
    static boolean ONLY_COMPILE_MAIN = false; // for testing small stuff
    static String  singleClassStr = null; 
    static HClass  singleClass = null; // for testing single classes
    static final int STRONGARM_BACKEND = 0;
    static final int MIPS_BACKEND = 1;
    static final int SPARC_BACKEND = 2;
    static final int PRECISEC_BACKEND = 3;
    // MIPS with support for last line accesses is tag unchecked
    static final int MIPSYP_BACKEND = 4;
    // MIPS with support for direct address registers
    static final int MIPSDA_BACKEND = 5;
    static private int BACKEND = PRECISEC_BACKEND;
    static String  BACKEND_NAME = "precisec";

    static boolean READ_ALLOC_STATS = false;
    static String allocNumberingFileName;
    static String instrumentationResultsFileName;
    static AllocationStatistics as;

    
    public static Linker linker = null; // can specify on the command-line.
    static java.io.PrintWriter out = 
	new java.io.PrintWriter(System.out, true);
        
    static String className;

    static String rootSetFilename;

    // FSK: contains specialization options to be used during
    // RegAlloc.  Take out when no longer necessary.  
    // May be null (in which case no options are being passed).
    static String regAllocOptionsFilename; 
    static RegAlloc.Factory regAllocFactory;

    static String methodName;

    static ClassHierarchy classHierarchy;
    static CallGraph callGraph;
    static Frame frame;

    static File ASSEM_DIR = null;
    static HCodeFactory hcf;

    static Set joinset=null, startset=null;

    // Support for transactions transformation
    static boolean DO_TRANSACTIONS = false;
    static SyncTransformer syncTransformer = null;

    // Support for precise garbage collection
    static boolean DYNAMICWBS = false;
    static boolean PRECISEGC = false;
    static boolean MULTITHREADED = false;
    static boolean WRITEBARRIERS = false;
    static boolean WB_STATISTICS = false;
    static int WB_TRANSFORMS = 0; // no transformations by default
    static int wbOptLevel = 0; // no removals by default
    static FileOutputStream wbos = null;
    static PrintStream wbps = null;
    static WriteBarrierStats writeBarrierStats = null;
    // static WriteBarrierQuadPass writeBarrier = null;

    public static void main(String[] args) {
	hcf = // default code factory.
	    new harpoon.ClassFile.CachingCodeFactory(
	    harpoon.IR.Quads.QuadWithTry.codeFactory()
	    );

	// the new mover will try to put NEWs closer to their constructors.
	// in the words of a PLDI paper, this reduces "drag time".
	// it also improves some analysis results. =)
	hcf = new harpoon.Analysis.Quads.NewMover(hcf).codeFactory();

	PRECISEGC = System.getProperty("harpoon.alloc.strategy", 
				       "malloc").equalsIgnoreCase("precise");
	parseOpts(args);
	assert className!= null : "must pass a class to be compiled";

	// find main method, set up frame.
	HClass hcl = linker.forName(className);
	HMethod mainM;
	try {
	    mainM = hcl.getDeclaredMethod("main","([Ljava/lang/String;)V");
	} catch (NoSuchMethodError e) {
	    throw new Error("Class "+className+" has no main method");
	}
	assert mainM != null;
	assert Modifier.isStatic(mainM.getModifiers()) : "main is not static";
	assert Modifier.isPublic(mainM.getModifiers()) : "main is not public";

	// create the target Frame way up here!
	// the frame specifies the combination of target architecture,
	// runtime, and allocation strategy we want to use.
	frame = Options.frameFromString(BACKEND_NAME, mainM,
					getAllocationStrategyFactory());

	do_it(mainM);
    }


    private static AllocationStrategyFactory getAllocationStrategyFactory() {

	return new AllocationStrategyFactory() {
	    public AllocationStrategy getAllocationStrategy(Frame frame) {
		
		System.out.print("Allocation strategy: ");
		
		if((INSTRUMENT_ALLOCS || INSTRUMENT_ALLOCS_STUB) && 
		   (INSTRUMENT_ALLOCS_TYPE==2)) {
		    System.out.println("InstrumentedAllocationStrategy");
		    return new harpoon.Instrumentation.AllocationStatistics.
			InstrumentedAllocationStrategy(frame);
		}

		if(PreallocOpt.PREALLOC_OPT) {
		    System.out.println("PreallocAllocationStrategy");
		    return new harpoon.Analysis.MemOpt.
			PreallocAllocationStrategy(frame);
		}

		if(Realtime.REALTIME_JAVA) {
		    System.out.println("RTJ");
		    return new harpoon.Analysis.Realtime.
			RealtimeAllocationStrategy(frame);
		}

		String alloc_strategy = 
		    System.getProperty("harpoon.alloc.strategy", "malloc");

		System.out.println(alloc_strategy);

		if(alloc_strategy.equalsIgnoreCase("nifty"))
		    return new harpoon.Backend.PreciseC.
			PGCNiftyAllocationStrategy(frame);

		if(alloc_strategy.equalsIgnoreCase("niftystats"))
		    return new harpoon.Backend.PreciseC.	    
			PGCNiftyAllocationStrategyWithStats(frame);

		if(alloc_strategy.equalsIgnoreCase("bdw"))
		    return new harpoon.Backend.Runtime1.
			BDWAllocationStrategy(frame);

		if(alloc_strategy.equalsIgnoreCase("sp"))
		    return new harpoon.Backend.Runtime1.
			SPAllocationStrategy(frame);

		if(alloc_strategy.equalsIgnoreCase("precise"))
		    return new harpoon.Backend.Runtime1.
			MallocAllocationStrategy(frame, "precise_malloc");

		if(alloc_strategy.equalsIgnoreCase("heapstats"))
		    return new harpoon.Backend.Runtime1.
			HeapStatsAllocationStrategy(frame);

		System.out.println
		    ("AllocationStrategy " + alloc_strategy +
		     " unknown; use default \"malloc\" strategy instead");

		// default, "malloc" strategy.
		return
		    new harpoon.Backend.Runtime1.MallocAllocationStrategy
		    (frame,
		     System.getProperty("harpoon.alloc.func", "malloc"));
	    }
	};
    }


    // hcf is QuadWithTry at the beginning of do_it();
    static void do_it(HMethod mainM) {
	if (Realtime.REALTIME_JAVA) { 
	    Realtime.setupObject(linker); 
	}

	// set up BACKEND enumeration
	if (BACKEND_NAME == "strongarm")
	    BACKEND = STRONGARM_BACKEND;
	if (BACKEND_NAME == "sparc")
	    BACKEND = SPARC_BACKEND;
	if (BACKEND_NAME == "mips")
	    BACKEND = MIPS_BACKEND;
	if (BACKEND_NAME == "mipsyp")
	    BACKEND = MIPSYP_BACKEND;
	if (BACKEND_NAME == "mipsda")
	    BACKEND = MIPSDA_BACKEND;
	if (BACKEND_NAME == "precisec")
	    BACKEND = PRECISEC_BACKEND;

	// Check for compatibility of precise gc options.
	if (PRECISEGC)
	    assert (BACKEND==PRECISEC_BACKEND) : 
	        "Precise gc is only implemented for the precise C backend.";
	if (MULTITHREADED) {
	    assert PRECISEGC || Realtime.REALTIME_JAVA :
		"Multi-threaded option is valid only for precise gc.";
	    assert wbOptLevel == 0 : "Write barrier removal not supported "+
		"for multi-threaded programs.";
	}
	if (WRITEBARRIERS || DYNAMICWBS)
	    assert PRECISEGC : 
	        "Write barrier options are valid only for precise gc.";

	MetaCallGraph mcg=null;
	
	if (SAMain.startset!=null)
	    hcf = harpoon.IR.Quads.ThreadInliner.codeFactory
		(hcf,SAMain.startset, SAMain.joinset);
	

	// check the configuration of the runtime.
	// (in particular, the --with-precise-c option)
	if (BACKEND==PRECISEC_BACKEND)
	    frame.getRuntime().configurationSet.add
		("check_with_precise_c_needed");
	else
	    frame.getRuntime().configurationSet.add
		("check_with_precise_c_not_needed");

	// needed for creating the class hierarchy
	Set roots = getRoots(mainM);

	// okay, we've got the roots, make a rough class hierarchy.
	hcf = new harpoon.ClassFile.CachingCodeFactory(hcf);
	classHierarchy = new QuadClassHierarchy(linker, roots, hcf);
	assert classHierarchy != null : "How the hell...";
	
	// use the rough class hierarchy to devirtualize as many call sites
	// as possible.
	hcf = new harpoon.Analysis.Quads.Nonvirtualize
	    (hcf, new harpoon.Backend.Maps.CHFinalMap(classHierarchy),
	     classHierarchy).codeFactory();
	
	if (!USE_OLD_CLINIT_STRATEGY) {
	    // transform the class initializers using the class hierarchy.
	    String resource = frame.getRuntime().resourcePath
		("init-safe.properties");
	    hcf = new harpoon.Analysis.Quads.InitializerTransform
		(hcf, classHierarchy, linker, resource).codeFactory();
	    // recompute the hierarchy after transformation.
	    hcf = new harpoon.ClassFile.CachingCodeFactory(hcf);
	    classHierarchy = new QuadClassHierarchy(linker, roots, hcf);
	    // config checking
	    frame.getRuntime().configurationSet.add
		("check_with_init_check_needed");
	} else
	    frame.getRuntime().configurationSet.add
		("check_with_init_check_not_needed");

	if (EVENTDRIVEN) {
	    hcf=harpoon.IR.Quads.QuadNoSSA.codeFactory(hcf);
	    Set mroots =
		extract_method_roots(frame.getRuntime().
				     runtimeCallableMethods());
	    mroots.add(mainM);
	    mcg = new MetaCallGraphImpl
		(new CachingCodeFactory(hcf), linker, classHierarchy, mroots);
	}
	
	if (ROLE_INFER) {
	    hcf = harpoon.IR.Quads.QuadNoSSA.codeFactory(hcf);
	    hcf = (new harpoon.Analysis.RoleInference.RoleInference
		   (hcf,linker)).codeFactory();
	    classHierarchy = new QuadClassHierarchy(linker, roots, hcf);
	}
	
	handleAllocationInstrumentation(roots, mainM);

	if(PreallocOpt.PREALLOC_OPT || PreallocOpt.ONLY_SYNC_REMOVAL) {
	    hcf = PreallocOpt.preallocAnalysis
		(linker, hcf, classHierarchy, mainM, roots, as, frame);
	}
	
	if (DO_TRANSACTIONS) {
	    String resource = frame.getRuntime().resourcePath
		("transact-safe.properties");
	    hcf = harpoon.IR.Quads.QuadSSI.codeFactory(hcf);
	    hcf = new harpoon.Analysis.Transactions.ArrayCopyImplementer
		(hcf, linker);
	    hcf = new harpoon.Analysis.Transactions.CloneImplementer
		(hcf, linker, classHierarchy.classes());
	    hcf = new harpoon.ClassFile.CachingCodeFactory(hcf);
	    classHierarchy = new QuadClassHierarchy(linker, roots, hcf);
	    
	    hcf = new harpoon.Analysis.Quads.ArrayInitRemover(hcf)
		.codeFactory();
	    hcf = new harpoon.ClassFile.CachingCodeFactory(hcf);
	    
	    syncTransformer = new SyncTransformer
		(hcf, classHierarchy, linker, mainM, roots, resource);
	    hcf = syncTransformer.codeFactory();
	    hcf = harpoon.Analysis.Counters.CounterFactory
		.codeFactory(hcf, linker, mainM);
	    hcf = new harpoon.ClassFile.CachingCodeFactory(hcf);
	    classHierarchy = new QuadClassHierarchy(linker, roots, hcf);
	    // config checking
	    frame.getRuntime().configurationSet.add
		("check_with_transactions_needed");
	}
	
	if (Realtime.REALTIME_JAVA) {
	    hcf = Realtime.setupCode(linker, classHierarchy, hcf);
	    classHierarchy = new QuadClassHierarchy(linker, roots, hcf);
	    hcf = Realtime.addChecks(linker, classHierarchy, hcf, roots);
	}
	
	/* counter factory must be set up before field reducer,
	 * or it will be optimized into nothingness. */
	if (Boolean.getBoolean("size.counters") ||
	    Boolean.getBoolean("mzf.counters") ||
	    Boolean.getBoolean("harpoon.sizeopt.bitcounters")) {
	    hcf = harpoon.IR.Quads.QuadNoSSA.codeFactory(hcf);
	    hcf = harpoon.Analysis.Counters.CounterFactory
		.codeFactory(hcf, linker, mainM);
	    // recompute the hierarchy after transformation.
	    hcf = new harpoon.ClassFile.CachingCodeFactory(hcf);
	    classHierarchy = new QuadClassHierarchy(linker, roots, hcf);
	}
	/*--- size optimizations ---*/
	if (Boolean.getBoolean("mzf.compressor")) {
	    // if we're going to do mzf compression, make sure that
	    // the MZFExternalMap methods are in the root set and
	    // the class hierarchy (otherwise the FieldReducer
	    // will stub them out as uncallable).
	    HClass hcx = linker.forClass
		(harpoon.Runtime.MZFExternalMap.class);
	    roots.addAll(Arrays.asList(hcx.getDeclaredMethods()));
	    classHierarchy = new QuadClassHierarchy(linker, roots, hcf);
	}	    
	if (Boolean.getBoolean("bitwidth")) {
	    hcf = harpoon.IR.Quads.QuadSSI.codeFactory(hcf);
	    hcf = new harpoon.ClassFile.CachingCodeFactory(hcf);
	    // read field roots
	    String resource = frame.getRuntime().resourcePath
		("field-root.properties");
	    System.out.println("STARTING BITWIDTH ANALYSIS");
	    hcf = new harpoon.Analysis.SizeOpt.FieldReducer
		(hcf, frame, classHierarchy, roots, resource)
		.codeFactory();
	}
	if (Boolean.getBoolean("mzf.compressor") &&
	    System.getProperty("mzf.profile","").length()>0) {
	    hcf = harpoon.IR.Quads.QuadSSI.codeFactory(hcf);
	    if (!Boolean.getBoolean("bitwidth"))
		// SCCOptimize makes the SimpleConstMap used by
		// ConstructorClassifier more accurate.  However, if
		// we've used the FieldReducer, we're already
		// SCCOptimized, so no need to do it again.
		hcf = harpoon.Analysis.Quads.SCC.SCCOptimize
		    .codeFactory(hcf);
	    hcf = new harpoon.ClassFile.CachingCodeFactory(hcf);
	    hcf = new harpoon.Analysis.SizeOpt.MZFCompressor
		(frame, hcf, classHierarchy,
		 System.getProperty("mzf.profile")).codeFactory();
	    // START HACK: main still creates a String[], even after the
	    // Compressor has split String.  So re-add String[] to the
	    // root-set.
	    roots.add(linker.forDescriptor("[Ljava/lang/String;"));
	    // END HACK!
	    classHierarchy = new QuadClassHierarchy(linker, roots, hcf);
	}
	/* -- add counters to all allocations? -- */
	if (Boolean.getBoolean("size.counters")) {
	    hcf = new harpoon.Analysis.SizeOpt.SizeCounters(hcf, frame)
		.codeFactory();
	    hcf = new harpoon.ClassFile.CachingCodeFactory(hcf);
	    // pull everything through the size counter factory
	    for (Iterator it=classHierarchy.callableMethods().iterator();
		 it.hasNext(); )
		hcf.convert((HMethod)it.next());
	}
	/* -- find mostly-zero fields -- */
	if (Boolean.getBoolean("mzf.counters")) {
	    hcf = new harpoon.Analysis.SizeOpt.MostlyZeroFinder
		(hcf, classHierarchy, frame).codeFactory();
	    hcf = new harpoon.ClassFile.CachingCodeFactory(hcf);
	    // pull everything through the 'mostly zero finder', to make
	    // sure that all relevant counter fields show up before
		// we start emitting code.
	    for (Iterator it=classHierarchy.callableMethods().iterator();
		 it.hasNext(); )
		hcf.convert((HMethod)it.next());
	}
	
	if (BACKEND == MIPSDA_BACKEND || BACKEND == MIPSYP_BACKEND) {
	    hcf = new harpoon.Analysis.Quads.ArrayUnroller(hcf).codeFactory();
	    /*
	    hcf = new harpoon.Analysis.Quads.DispatchTreeTransformation
		(hcf, classHierarchy).codeFactory();
	    */
	    hcf = harpoon.IR.Quads.QuadSSA.codeFactory(hcf);
	    hcf = new harpoon.ClassFile.CachingCodeFactory(hcf);
	    hcf = new harpoon.Analysis.Quads.SmallMethodInliner
		(hcf, classHierarchy);
	    hcf = harpoon.IR.Quads.QuadSSI.codeFactory(hcf);
	    hcf = new harpoon.ClassFile.CachingCodeFactory(hcf);
	    hcf = new harpoon.Analysis.Quads.MemoryOptimization
		(hcf, classHierarchy, new CallGraphImpl(classHierarchy, hcf))
		.codeFactory();
	    hcf = new harpoon.ClassFile.CachingCodeFactory(hcf);
	}

	if (OPTIMIZE) {
	    /*
	    hcf = new harpoon.Analysis.Quads.DispatchTreeTransformation
		(hcf, classHierarchy).codeFactory();
	    */
	    hcf = harpoon.IR.Quads.QuadSSI.codeFactory(hcf);
	    hcf = harpoon.Analysis.Quads.SCC.SCCOptimize.codeFactory(hcf);
	    hcf = harpoon.IR.Quads.QuadSSA.codeFactory(hcf);
	    if (Boolean.getBoolean("harpoon.inline.arraycopy")) {
		// inline System.arraycopy in particular.
		hcf = new harpoon.Analysis.Transactions.ArrayCopyImplementer
		    (hcf, linker);
		hcf = new harpoon.ClassFile.CachingCodeFactory(hcf);
		classHierarchy = new QuadClassHierarchy(linker, roots, hcf);
		hcf = new harpoon.Analysis.Quads.ArrayCopyInliner
		    (hcf, classHierarchy);
	    } else {
		// just inline small methods.
		hcf = new harpoon.Analysis.Quads.SmallMethodInliner
		    (hcf, classHierarchy);
	    }
	    hcf = harpoon.IR.Quads.QuadSSI.codeFactory(hcf);
	    hcf = harpoon.Analysis.Quads.SCC.SCCOptimize.codeFactory(hcf);
	    hcf = new harpoon.ClassFile.CachingCodeFactory(hcf);
	}

	HMethod mconverted=null;
	if (EVENTDRIVEN) {
	    if (!OPTIMIZE) {
		hcf = harpoon.IR.Quads.QuadSSI.codeFactory(hcf); 
	    }
	    hcf = new harpoon.ClassFile.CachingCodeFactory(hcf,true);
	    HCode hc = hcf.convert(mainM);
	    harpoon.Analysis.EventDriven.EventDriven ed = 
		new harpoon.Analysis.EventDriven.EventDriven((harpoon.ClassFile.CachingCodeFactory)hcf, hc, classHierarchy, linker,optimistic,recycle);
	    mainM=ed.convert(mcg);
	    mcg=null; /*Memory management*/
	    hcf = new harpoon.ClassFile.CachingCodeFactory(hcf);
	    Set eroots = new java.util.HashSet
		(frame.getRuntime().runtimeCallableMethods());
	    // and our main method is a root, too...
	    eroots.add(mainM);
	    hcf = new harpoon.ClassFile.CachingCodeFactory(hcf);
	    classHierarchy = new QuadClassHierarchy(linker, eroots, hcf);
	    callGraph=new CallGraphImpl2(classHierarchy, hcf);
	} else if (true) {
	    hcf = harpoon.IR.Quads.QuadSSI.codeFactory(hcf);
	    hcf = new harpoon.ClassFile.CachingCodeFactory(hcf);
	    callGraph = new CallGraphImpl2(classHierarchy, hcf);
	} else {
	    callGraph = new CallGraphImpl(classHierarchy, hcf);//less precise
	}

	if (WRITEBARRIERS) {
	    System.out.println("Using write barriers for generational gc.");
	    if (wbOptLevel != 0)
		System.out.println
		    ("Removing write barriers at optimization level "+
		     wbOptLevel+".");
	    String rName = frame.getRuntime().resourcePath
		("writebarrier-safe.properties");
	    System.out.println(rName);
	    if (WB_TRANSFORMS == 2 || WB_TRANSFORMS == 3) {
		// transform recursive constructors
		hcf = new harpoon.Analysis.PreciseGC.RCTransformer
		    (hcf, classHierarchy, linker).codeFactory();
		// re-generate class hierarchy to handle modified methods
		classHierarchy = new QuadClassHierarchy(linker, roots, hcf);
	    }
	    if (WB_TRANSFORMS == 1 || WB_TRANSFORMS == 3) {
		// perform allocation hoisting
		System.out.println("Performing allocation hoisting.");
		hcf = new CachingCodeFactory(hcf);
		hcf = new harpoon.Analysis.PreciseGC.AllocationHoisting
		    (hcf, classHierarchy, linker, rName, 
		     (wbOptLevel != 0) ? wbOptLevel : 2).codeFactory();
		// re-generate class hierarchy to handle modified methods
		classHierarchy = new QuadClassHierarchy(linker, roots, hcf);
	    }
	    hcf = new CachingCodeFactory(hcf);
	    hcf = (new harpoon.Analysis.PreciseGC.WriteBarrierQuadPass
		   (classHierarchy, hcf, linker, rName, wbOptLevel)).
		codeFactory();
	    // re-generate class hierarchy to handle added calls
	    classHierarchy = new QuadClassHierarchy(linker, roots, hcf);
	}

	if (DYNAMICWBS) {
	    System.out.println
		("Using dynamic write barriers for generational gc.");
	    if (wbOptLevel == 1)
		System.out.println("Optimistically removing write barriers.");
	    harpoon.Analysis.PreciseGC.DynamicWBQuadPass dynamicWB = null;
	    if (wbOptLevel == 1 || WB_STATISTICS) {
		dynamicWB = new harpoon.Analysis.PreciseGC.DynamicWBQuadPass
		    (hcf, linker);
		hcf = dynamicWB.codeFactory();
	    }
	    hcf = (wbOptLevel == 0) ?
		(new harpoon.Analysis.PreciseGC.WriteBarrierInserter
		 (hcf, linker)).codeFactory() :
		(new harpoon.Analysis.PreciseGC.WriteBarrierInserter
		 (hcf, linker, dynamicWB)).codeFactory();
	    // re-generate class hierarchy to handle added calls
	    classHierarchy = new QuadClassHierarchy(linker, roots, hcf);
	}

	// now we can finally set the (final?) classHierarchy and callGraph
	// which the frame will use (among other things, for vtable numbering)
	frame.setClassHierarchy(classHierarchy);
	if (USE_OLD_CLINIT_STRATEGY) {
	    frame.setCallGraph(callGraph);
	    System.getProperties().put
		("harpoon.runtime1.order-initializers","true");
	}
	callGraph=null;// memory management.
 
	// virtualize any uncallable methods using the final classhierarchy
	// (this keeps us from later getting link errors)
	hcf = new harpoon.Analysis.Quads.Virtualize(hcf, classHierarchy)
	    .codeFactory();
	// non-virtualize any final methods -- this used to be done
	// in the low-quad translation, but doing so is unsafe unless
	// you've got a classHierarchy handy -- you could inadventently
	// create a new dangling reference to an uncallable method.
	hcf=new harpoon.Analysis.Quads.Nonvirtualize
	    (hcf, new harpoon.Backend.Maps.CHFinalMap(classHierarchy),
	     classHierarchy).codeFactory();

	if (LOOPOPTIMIZE) {
	    // XXX: you might have to add a TypeSwitchRemover here, if
	    //      LoopOptimize don't handle TYPESWITCHes. --CSA
	    System.out.println("Loop Optimizations On");
	    hcf=harpoon.IR.LowQuad.LowQuadSSI.codeFactory(hcf);
	    hcf=harpoon.Analysis.LowQuad.Loop.LoopOptimize.codeFactory(hcf);
	    hcf=harpoon.Analysis.LowQuad.DerivationChecker.codeFactory(hcf);
	}

	hcf = harpoon.IR.LowQuad.LowQuadSSA.codeFactory(hcf);

	// XXX: ToTree doesn't handle TYPESWITCHes right now.
	hcf = new harpoon.Analysis.Quads.TypeSwitchRemover(hcf).codeFactory();
	hcf = harpoon.IR.Tree.TreeCode.codeFactory(hcf, frame);
	hcf = frame.getRuntime().nativeTreeCodeFactory(hcf);

	hcf = harpoon.IR.Tree.CanonicalTreeCode.codeFactory(hcf, frame);
	hcf = harpoon.Analysis.Tree.DerivationChecker.codeFactory(hcf);
//  	hcf = (new harpoon.Backend.Analysis.GCTraceStore()).codeFactory(hcf);

	if (WRITEBARRIERS || DYNAMICWBS) {
	    // run constant propagation
	    hcf = new harpoon.Analysis.Tree.ConstantPropagation(hcf).
		codeFactory();
	    // remove write barriers for assignment to constants
	    hcf = harpoon.Analysis.PreciseGC.WriteBarrierConstElim.codeFactory
		(frame, hcf, linker);
	    if (WB_STATISTICS) {
		System.out.println("Compiling for write barrier statistics.");
		if (wbps != null)
		    wbps.println("\nWRITE BARRIER LIST FOR "+className);
		writeBarrierStats = 
		    new harpoon.Analysis.PreciseGC.WriteBarrierStats
		    (frame, hcf, classHierarchy, wbps, linker);
		hcf = writeBarrierStats.codeFactory();
	    } else {
		hcf = harpoon.Analysis.PreciseGC.WriteBarrierTreePass.
		    codeFactory(hcf, frame, classHierarchy, linker);
	    }
	}

	if(PreallocOpt.PREALLOC_OPT)
	    hcf = PreallocOpt.addMemoryPreallocation(linker, hcf, frame);
	else if(PreallocOpt.ONLY_SYNC_REMOVAL)
	    hcf = PreallocOpt.BOGUSaddMemoryPreallocation(linker, hcf, frame);

	if(Realtime.REALTIME_JAVA)
	{
	    hcf = Realtime.addNoHeapChecks(hcf);
	    hcf = Realtime.addQuantaChecker(hcf);
	    hcf = harpoon.Analysis.Tree.DerivationChecker.codeFactory(hcf);
	    hcf = new harpoon.ClassFile.CachingCodeFactory(hcf);
	}
	hcf = harpoon.Analysis.Tree.AlgebraicSimplification.codeFactory(hcf);
	//hcf = harpoon.Analysis.Tree.DeadCodeElimination.codeFactory(hcf);
	//hcf = harpoon.Analysis.Tree.JumpOptimization.codeFactory(hcf);
	if (DO_TRANSACTIONS) {
	    hcf = syncTransformer.treeCodeFactory(frame, hcf);
	}

	if (MULTITHREADED) {
	    /* pass to insert GC polling calls */
	    System.out.println("Compiling with precise gc for multiple threads.");
	    hcf = harpoon.Backend.Analysis.MakeGCThreadSafe.
		codeFactory(hcf, frame);
	}

	hcf = new harpoon.ClassFile.CachingCodeFactory(hcf);
    if(BACKEND == MIPSDA_BACKEND || BACKEND == MIPSYP_BACKEND) {
       hcf = harpoon.Analysis.Tree.MemHoisting.codeFactory(hcf);
       hcf = new harpoon.Analysis.Tree.DominatingMemoryAccess
	   (hcf, frame, classHierarchy).codeFactory();
       
    }
	hcf = harpoon.Analysis.Tree.DerivationChecker.codeFactory(hcf);
    
    
	HCodeFactory sahcf = frame.getCodeFactory(hcf);
	if (sahcf!=null)
	    sahcf = new harpoon.ClassFile.CachingCodeFactory(sahcf);

	if (LOCAL_REG_ALLOC) 
	    regAllocFactory = RegAlloc.LOCAL;
	else 
	    regAllocFactory = RegAlloc.GLOBAL;
	regAllocFactory = (new RegAllocOpts
			   (regAllocOptionsFilename)).factory(regAllocFactory);

	Set methods = classHierarchy.callableMethods();
	Iterator classes = new TreeSet(classHierarchy.classes()).iterator();

	String filesuffix = (BACKEND==PRECISEC_BACKEND) ? ".c" : ".s";
	if (ONLY_COMPILE_MAIN)
	    classes = Default.singletonIterator(mainM.getDeclaringClass());
	if (singleClassStr!=null) {
	    singleClass = linker.forName(singleClassStr);
	    classes=Default.singletonIterator(singleClass);
	}
	if (true) { // this is only here because i don't want to re-indent
	            // all the code below this point.
	    while(classes.hasNext()) {
		HClass hclass = (HClass) classes.next();
		if (singleClass!=null && singleClass!=hclass) continue;//skip
		messageln("Compiling: " + hclass.getName());
		
		try {
		    String filename = frame.getRuntime().getNameMap().mangle(hclass);
		    java.io.Writer w;
		    try {
			w = new FileWriter
			    (new File(ASSEM_DIR, filename+filesuffix));
		    } catch (java.io.FileNotFoundException ffe) {
			// filename too long?  try shorter, unique, name.
			// XXX: sun's doc for File.createTempFile() states
			// "If the prefix is too long then it will be
			// truncated" but it is actually not.  We must
			// truncate it ourselves, for now.  200-chars?
			if (filename.length()>200)
			    filename=filename.substring(0, 200);
			w = new FileWriter
			    (File.createTempFile(filename, filesuffix,
						 ASSEM_DIR));
		    }
		    out = new PrintWriter(new BufferedWriter(w));

		    if (BACKEND==PRECISEC_BACKEND)
			out = new harpoon.Backend.PreciseC.TreeToC(out);
		    
		    HMethod[] hmarray = hclass.getDeclaredMethods();
		    Set hmset = new TreeSet(Arrays.asList(hmarray));
		    hmset.retainAll(methods);
		    Iterator hms = hmset.iterator();
		    if (ONLY_COMPILE_MAIN)
			hms = Default.singletonIterator(mainM);
		    message("\t");
		    while(hms.hasNext()) {
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
				       hclass.getName()+": "+e);
		    System.exit(1);
		}
	    }

	    if (Realtime.REALTIME_JAVA) {
		Realtime.printStats();
	    }

	    // put a proper makefile in the directory.
	    File makefile = new File(ASSEM_DIR, "Makefile");
	    InputStream templateStream;
	    String resourceName="harpoon/Support/nativecode-makefile.template";
	    if (BACKEND==PRECISEC_BACKEND)
		resourceName="harpoon/Support/precisec-makefile.template";
	    if (BACKEND==MIPSDA_BACKEND)
		resourceName="harpoon/Support/mipsda-makefile.template";
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
		System.exit(1);
	    }
	}
    }


    // construct the set of roots for the program we compile
    static Set getRoots(HMethod mainM) {
	// ask the runtime which roots it requires.
	Set roots = new java.util.HashSet
	    (frame.getRuntime().runtimeCallableMethods());
	
	if(PreallocOpt.PREALLOC_OPT) {
	    PreallocOpt.updateRoots(roots, linker);
	    hcf = PreallocOpt.getHCFWithEmptyInitCode(hcf);
	}
	
	// and our main method is a root, too...
	roots.add(mainM);
	// other realtime and event-driven-specific roots.
	if (EVENTDRIVEN) {
	    roots.add(linker.forName
		      ("harpoon.Analysis.ContBuilder.Scheduler")
		      .getMethod("loop",new HClass[0]));
	}
	if (Realtime.REALTIME_JAVA) {
	    roots.addAll(Realtime.getRoots(linker));
	}
	if (rootSetFilename!=null) try {
	    addToRootSet(roots, rootSetFilename);
	} catch (IOException ex) {
	    System.err.println("Error reading "+rootSetFilename+": "+ex);
	    ex.printStackTrace();
	    System.exit(1);
	}
	
	return roots;
    }

    static void addToRootSet(final Set roots, String filename) 
	throws IOException {
	ParseUtil.readResource(filename, new ParseUtil.StringParser() {
	    public void parseString(String s)
		throws ParseUtil.BadLineException {
		if (s.indexOf('(') < 0) // parse as class name.
		    roots.add(ParseUtil.parseClass(linker, s));
		else // parse as method name.
		    roots.add(ParseUtil.parseMethod(linker, s));
	    }
	});
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
		// myPrint avoids register-allocation-dependant
		// peephole optimization code in print
		((harpoon.IR.Assem.Code)hc).myPrint(out,false); 
	    } else {
		info("null returned for " + hmethod);
	    } 
	    info("\t--- end INSTR FORM (no register allocation)  ---");
	    out.println();
	    out.flush();
	}
	
	if (ABSTRACT_REG_ALLOC) {
	    HCode hc = sahcf.convert(hmethod);
	    
	    info("\t--- INSTR FORM (register allocation)  ---");
	    HCodeFactory regAllocCF = 
		RegAlloc.abstractSpillFactory(sahcf, frame, regAllocFactory);
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
	    HCodeFactory regAllocCF;
	    
	    regAllocCF = RegAlloc.codeFactory(sahcf,frame,regAllocFactory);

	    HCode rhc = regAllocCF.convert(hmethod);
        if(BACKEND == MIPSYP_BACKEND && rhc != null) {
           harpoon.Backend.Generic.Code cd = (harpoon.Backend.Generic.Code)rhc;
           harpoon.Backend.MIPS.BypassLatchSchedule b = 
              new harpoon.Backend.MIPS.BypassLatchSchedule(cd, frame);
        }
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
	  it=new CombineIterator(it, Default.singletonIterator(data));
	  if (WB_STATISTICS) {
	      assert writeBarrierStats != null :
		  "WriteBarrierStats need to be run before WriteBarrierData.";
	      HData wbData = writeBarrierStats.getData(hclass, frame);
	      it=new CombineIterator(it, Default.singletonIterator(wbData));
	  }
	  if(PreallocOpt.PREALLOC_OPT) {
	      HData poData = PreallocOpt.getData(hclass, frame);
	      it = new CombineIterator(it, Default.singletonIterator(poData));
	  }
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

	if (!PRE_REG_ALLOC && !REG_ALLOC && !HACKED_REG_ALLOC) continue;

	if (data.getRootElement()==null) continue; // nothing to do here.

	final Instr instr = 
	    frame.getCodeGen().genData((harpoon.IR.Tree.Data)data, new InstrFactory() {
		private int id = 0;
		public TempFactory tempFactory() { return null; }
		public harpoon.IR.Assem.Code getParent() { return null/*data*/; }// FIXME!
		public harpoon.Backend.Generic.Frame getFrame() { return frame; }
		public synchronized int getUniqueID() { return id++; }
		public HMethod getMethod() { return null; }
		public int hashCode() { return data.hashCode(); }
	    });
	
	assert instr != null : "no instrs generated; check that CodeGen.java was built from spec file";
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


    private static void handleAllocationInstrumentation(Set roots,
							HMethod mainM) {
	if (INSTRUMENT_ALLOCS || INSTRUMENT_ALLOCS_STUB) {
	    hcf = QuadNoSSA.codeFactory(hcf);
	    hcf = new CachingCodeFactory(hcf, true);
	    // create the allocation numbering
	    AllocationNumbering an =
		new AllocationNumbering
		((CachingCodeFactory) hcf, classHierarchy, INSTRUMENT_CALLS);
	    try {
		if(INSTRUMENT_ALLOCS_STUB) { // "textualize" only a stub
		    System.out.println
			("Writing AllocationNumbering into " + IFILE);
		    AllocationNumberingStub.writeToFile(an, IFILE, linker);
		}
		else { // classic INSTRUMENT_ALLOCS: serialize serious stuff
		    ObjectOutputStream oos =
			new ObjectOutputStream(new FileOutputStream(IFILE));
		    oos.writeObject(hcf);
		    oos.writeObject(linker);
		    oos.writeObject(roots);
		    oos.writeObject(mainM);
		    oos.writeObject(an);
		    oos.close();
		}
	    } catch (java.io.IOException e) {
		System.out.println(e + " was thrown:");
		e.printStackTrace(System.out);
		System.exit(1);
	    }
	    switch(INSTRUMENT_ALLOCS_TYPE) {
	    case 1:
		hcf = (new InstrumentAllocs(hcf, mainM, linker, an,
					    INSTRUMENT_SYNCS,
					    INSTRUMENT_CALLS)).codeFactory();
		break;
	    case 2:
		hcf = (new InstrumentAllocs2(hcf, mainM,
					     linker, an)).codeFactory();
		roots.add(InstrumentAllocs.getMethod
			  (linker,
			   "harpoon.Runtime.CounterSupport", "count2",
			   new HClass[]{HClass.Int, HClass.Int}));
		break;
	    default:
		assert false : "Illegal INSTRUMENT_ALLOCS_TYPE" +
		    INSTRUMENT_ALLOCS_TYPE;		    
	    }

	    hcf = new CachingCodeFactory(hcf);
	    classHierarchy = new QuadClassHierarchy(linker, roots, hcf);
	}
	else if(READ_ALLOC_STATS) {
	    hcf = QuadNoSSA.codeFactory(hcf);
	    as = new AllocationStatistics(linker,
					  allocNumberingFileName,
					  instrumentationResultsFileName);
	    if(!PreallocOpt.PREALLOC_OPT)
		as.printStatistics(AllocationStatistics.getAllocs
				   (classHierarchy.callableMethods(), hcf));
	}
    }

    protected static void message(String msg) {
	if(!QUIET) System.out.print(msg);
    }

    protected static void messageln(String msg) {
	if(!QUIET) System.out.println(msg);
    }
    
    protected static void parseOpts(String[] args) {
	LongOpt[] longopts = new LongOpt[] {
	    new LongOpt("prealloc-opt", LongOpt.NO_ARGUMENT, null, 1000),
	    new LongOpt("ia-only-sync-removal", LongOpt.NO_ARGUMENT,
			null, 1001)
	};

	Getopt g = 
	    new Getopt("SAMain", args, 
		       "i:N:s:b:c:o:EefpIDOPFHR::LlABt:hq1::C:r:Td::mw::x::y::Z:W:",
		       longopts);
	
	int c;
	String arg;
	while((c = g.getopt()) != -1) {
	    switch(c) {
	    case 'E':
		EVENTDRIVEN=true;
		break;
	    case 'p':
		optimistic=true;
		break;
	    case 'f':
		recycle=true;
		break;
	    case 'T': // Transactions support (CSA)
		linker = new AbstractClassFixupRelinker(Loader.systemLinker);
		DO_TRANSACTIONS = true;
		break;
	    case 'd': // Dynamic write barriers (KKZ)
		DYNAMICWBS = true;
		assert WRITEBARRIERS == false :
		    "Only one of options -d and -w may be selected.";
		arg = g.getOptarg();
		if (arg != null) {
		    try {
			wbOptLevel = Integer.parseInt(arg);
			assert wbOptLevel == 0 || wbOptLevel == 1;
		    } catch (Exception e) {
			System.err.println(e);
			System.exit(1);
		    }
		}
		break;
	    case 'm': // Multi-threaded (KKZ)
		MULTITHREADED = true; break;
	    case 'w': // Add write barriers (KKZ)
		WRITEBARRIERS = true;
		assert DYNAMICWBS == false :
		    "Only one of options -d and -w may be selected.";
		arg = g.getOptarg();
		if (arg != null) {
		    try {
			wbOptLevel = Integer.parseInt(arg);
			assert wbOptLevel >= 0 && wbOptLevel <= 6;
		    } catch (Exception e) {
			System.err.println(e);
			System.exit(1);
		    }
		}
		break;
	    case 'x': // Compile with write barrier statistics (KKZ)
		WB_STATISTICS = true;
		arg = g.getOptarg();
		if (arg != null) {
		    try {
			wbos = new FileOutputStream(arg, true);
			wbps = new PrintStream(wbos);
			System.out.println
			    ("Writing write barrier list to file "+arg);
		    } catch (Exception e) {
			System.err.println(e);
			System.exit(1);
		    }
		}
		break;
	    case 'y': // Compile with transformations (kkz)
		arg = g.getOptarg();
		if (arg != null) {
		    // select transformation level
		    WB_TRANSFORMS = Integer.parseInt(arg);
		    assert WB_TRANSFORMS >= 0 && WB_TRANSFORMS <= 3;
		} else {
		    // both transformations on
		    WB_TRANSFORMS = 3;
		}
		break;
	    case 't': // Realtime Java extensions (WSB)
		linker = new AbstractClassFixupRelinker(Loader.systemLinker);
		Realtime.configure(g.getOptarg());
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
		    System.exit(1);
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
		    System.exit(1);
		}
		break;
	    case 'N':
		INSTRUMENT_ALLOCS=true;
		IFILE=g.getOptarg();
		break;
	    case 'W':
		INSTRUMENT_ALLOCS_STUB=true;
		IFILE=g.getOptarg();
		break;
	    case 'l':
		LOOPOPTIMIZE=true;
		break;
	    case 'e':
		ROLE_INFER=true;
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
		if (g.getOptarg() != null)
		    regAllocOptionsFilename = g.getOptarg();
		REG_ALLOC = true;
		break;
	    case 'L':
		REG_ALLOC = LOCAL_REG_ALLOC = true;
		break;
	    case 'F':
		OPTIMIZE = true;
		break;
	    case 'A':
		OUTPUT_INFO = PRE_REG_ALLOC = PRINT_ORIG = 
		    REG_ALLOC = true;
		break;
	    case 'o':
		ASSEM_DIR = new File(g.getOptarg());
		assert ASSEM_DIR.isDirectory() : ""+ASSEM_DIR+" must be a directory";
		break;
	    case 'b': {
		BACKEND_NAME = g.getOptarg().toLowerCase().intern();
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
		    singleClassStr = optclassname;
		} else {
		    ONLY_COMPILE_MAIN = true;
		}
		break;
	    case 'r':
		rootSetFilename = g.getOptarg();
		break;
	    case 'I':
		USE_OLD_CLINIT_STRATEGY = true;
		break;
	    case 1000: // prealloc-opt
		PreallocOpt.PREALLOC_OPT = true;
		// make sure we use Runtime2
		System.setProperty("harpoon.runtime","2");
		break;
	    case 1001: // ia-only-sync-removal
		PreallocOpt.ONLY_SYNC_REMOVAL = true;
		System.setProperty("harpoon.runtime","2");
		break;
	    case 'Z':
		READ_ALLOC_STATS = true;
		{
		    String names = g.getOptarg();
		    allocNumberingFileName = firstHalf(names);
		    instrumentationResultsFileName = secondHalf(names);
		    System.out.println("allocNumberingFileName = " + 
				       allocNumberingFileName);
		    System.out.println("instrumentationResultsFileName = " +
				       instrumentationResultsFileName);
		}
		break;
	    case '?':
	    case 'h':
		System.out.println(usage);
		System.out.println();
		printHelp();
		System.exit(1);
	    default: 
		System.out.println("getopt() returned " + c);
		System.out.println(usage);
		System.out.println();
		printHelp();
		System.exit(1);
	    }
	}
	if (linker==null) {
	    linker = Loader.systemLinker;
	    if (!USE_OLD_CLINIT_STRATEGY)
		linker = new AbstractClassFixupRelinker(linker);
	}
    }

    private static String firstHalf(String str) {
	int commaPos = str.indexOf(',');
	assert commaPos != -1;
	return str.substring(0, commaPos);
    }

    private static String secondHalf(String str) {
	int commaPos = str.indexOf(',');
	assert commaPos != -1;
	return str.substring(commaPos + 1);
    }

    static final String usage = 
	"usage is: -c <class>"+
	" [-eDOPRLABIhq] [-o <assembly output directory>]";


    private static String[] help_lines = new String[] {
	"-c <class> (required)",
	"\tCompile <class>\n",
	
	"-e Rol(e) Inference",
	
	"-o <dir> (optional)",
	"\tOutputs the program text to files within <dir>.",
	
	"-D",
	"\tOutputs DATA information for <class>",
	
	"-t (analysis)",
	"\tTurns on Realtime Java extensions with the optional",
	"\tanalysis method: NONE, CHEESY, REAL",
	
	"-O",
	"\tOutputs Original Tree IR for <class>",
	
	"-P",
	"\tOutputs Pre-Register Allocated Instr IR for <class>",
	
	"-B",
	"\tOutputs Abstract Register Allocated Instr IR for <class>",
	
	"-L",
	"\tOutputs Local Register Allocated Instr IR for <class>",
	
	"-R",
	"\tOutputs Global Register Allocated Instr IR for <class>",

	"-E",
	"EventDriven transformation",
	
	"-p",
	"Optimistic option for EventDriven",
	
	"-f",
	"Recycle option for EventDriven.  Environmentally (f)riendly",
	
	"-A",
	"\tSame as -OPR",
	
	"-i <filename>",
	"Read CodeFactory in from FileName",
	
	"-N <filename>",
	"\tWrite out allocation Instrumentation to FileName",
	
	"-W <filename>",
	"\tSame as -N <filename>, but writes only a small text file (an AllocationNumberingStub)",
	"\tThis file can be read with the -Z option",
	"\tUse if serialization makes if troubles.",
	
	"-Z <allocNumberingStub.filename>,<instrResults.filename>",
	"\tReads in an allocation numbering stub and an instrumentation result file; prints dynamic memory allocation statistics",
	
	"-b <backend name>",
	"\tSupported backends are StrongARM (default), MIPS, Sparc, or PreciseC",
	
	"-l",
	"Turn on Loop Optimizations",
	
	"-q",
	"\tTurns on quiet mode (status messages are not output)",
	
	"-1<optional class name>",
	"\tCompiles only a single method or class.  Without a classname, only compiles <class>.main()",
	"\tNote that you may not have whitespace between the '-1' and the classname",
	
	"-r <root set file>",
	"\tAdds additional classes/methods to the root set as specified by <root set file>.",

	"-I",
	"\tUse old simple-but-not-always-correct class init strategy.",
	"-T",
	"\tDo transactions transformation.",

	"--prealloc-opt",
	"\tPreallocation Optimization using Incompatibility Analysis.",
	"\tSyncronizations on preallocated objects are removed (prealocated",
	"\tobjects are anyway thread local.",

	"--ia-only-sync-removal",
	"\tUses the Incompatibility Analysis only to remove the",
	"\tsyncronizations on the preallocatable objects.  No actual",
	"\tpreallocation.",

	"-h",
	"\tPrints out this help message"
    };

    protected static void printHelp() {
	for(int i = 0; i < help_lines.length; i++)
	    System.out.println(help_lines[i]);
    }

    protected static void info(String str) {
	if(OUTPUT_INFO) out.println(str);
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
}
