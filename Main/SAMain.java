// SAMain.java, created Mon Aug  2 19:41:06 1999 by pnkfelix
// Copyright (C) 1999 Felix S. Klock II <pnkfelix@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Main;

import harpoon.Analysis.AbstractClassFixupRelinker;
import harpoon.ClassFile.CachingCodeFactory;
import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HCodeFactory;
import harpoon.ClassFile.HMethod;
import harpoon.ClassFile.Linker;
import harpoon.ClassFile.Loader;
import harpoon.ClassFile.NoSuchClassException;
import harpoon.ClassFile.Relinker;
import harpoon.IR.Quads.QuadWithTry;
import harpoon.IR.Quads.QuadNoSSA;
import harpoon.IR.Quads.QuadSSI;
import harpoon.Backend.Backend;
import harpoon.Backend.Generic.Frame;
import harpoon.Analysis.ClassHierarchy;
import harpoon.Analysis.CallGraph;
import harpoon.Analysis.Quads.CallGraphImpl;
import harpoon.Analysis.Quads.CallGraphImpl2;
import harpoon.Analysis.Quads.QuadClassHierarchy;

import net.cscott.jutil.CombineIterator;
import net.cscott.jutil.Default;
import harpoon.Util.ParseUtil;
import harpoon.Util.Util;

import harpoon.Backend.Runtime1.AllocationStrategy;
import harpoon.Backend.Runtime1.AllocationStrategyFactory;

import harpoon.IR.Quads.Quad;

import harpoon.Instrumentation.AllocationStatistics.AllocationInstrCompStage;
import harpoon.Analysis.Realtime.Realtime;
import harpoon.Analysis.MemOpt.PreallocOpt;
import harpoon.Analysis.PointerAnalysis.PointerAnalysisCompStage;
import harpoon.Analysis.PointerAnalysis.AllocSyncOptCompStage;

import harpoon.Analysis.PA2.WPPointerAnalysisCompStage;
import harpoon.Analysis.PA2.AllocSync.WPAllocSyncCompStage;
import harpoon.Analysis.PA2.WPMutationAnalysisCompStage;

import harpoon.Util.Options.Option;

import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collections;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import java.util.HashSet;
import java.util.List;
import java.util.LinkedList;
import java.util.Map;
import java.util.HashMap;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintStream;

/**
 * <code>SAMain</code> is a program to compile java classes to some
 * approximation of StrongARM assembly.  It is for development testing
 * purposes, not production use.
 * 
 * @author  Felix S. Klock II <pnkfelix@mit.edu>
 * @version $Id: SAMain.java,v 1.65 2005-09-01 00:01:43 salcianu Exp $
 */
public class SAMain extends harpoon.IR.Registration {
 
    static boolean OPTIMIZE = false;
    static boolean LOOPOPTIMIZE = false;
    static boolean QUIET = false;

    /** Backend kind.  Has to be one of the constants defined in
	<code>harpoon.Backend.Backend</code>;
	<code>Backend.PRECISEC</code> by default.  */
    static String BACKEND = Backend.PRECISEC;

    static String className;
    static String rootSetFilename;

    // Support for precise garbage collection
    static boolean PRECISEGC = false;
    static boolean MULTITHREADED = false;

    private static List<CompilerStage> stages;

    public static void main(String[] args) {
	buildCompilerPipeline();
	
	List<Option> allOptions = getAllOptions();
	parseOpts(args, allOptions);
	
	if(className == null) {
	    System.err.println("must pass a class to be compiled");
	    System.err.println("Use option \"-h\" to find all options");
	    System.exit(1);
	}
	
	checkOptionConsistency();
	
	CompilerState cs = CompilerState.EMPTY_STATE;

	for(CompilerStage stage : stages) {
	    if(stage.enabled()) {
		System.out.println("Execute stage " + stage.name());
		cs = stage.action(cs);
	    }
	}
    }


    private static void addStage(CompilerStage cs) { stages.add(cs); }
    
    private static void buildCompilerPipeline() {
	stages = new LinkedList<CompilerStage>();

	// build a "nascent" compiler state: 
	// main method, root set, linker and frame
	addStage(new BuildInitialCompState());

	// add code factory, i.e., parse the code into QuadWithTry IR;
	// also build class hierarchy
	addStage(new BuildQuadForm());
	// At this point in the pipeline, we have a full compiler
	// state (including class hierarchy). Let it roll!

	// quad-form analyses
	buildQuadFormPipeline();

	// convert quad IR -> tree IR
	addStage(new LowQuadToTree()); 

	// tree-form analyses
	buildTreeFormPipeline();

	// final compilation stage
	addStage(new CodeGenerator());
    }


    private static void buildQuadFormPipeline() {
	// Pointer Analysis v2 + Stack allocation
	addStage(WPAllocSyncCompStage.getFullStage());
	addStage(new WPMutationAnalysisCompStage());

	// adds a stage that does pointer analysis and uses its
	// results for memory allocation, sync removal, and/or RTJ
	// programs debug and optimization
	addStage(PointerAnalysisCompStage.getPAAndAppsStage());

	// allocation instrumentation stage
	AllocationInstrCompStage aics = new AllocationInstrCompStage();
	addStage(aics);
	// memory preallocation via incompatibility analysis
	addStage(new PreallocOpt.QuadPass(aics));

	addStage(new EventDrivenTransformation.QuadPass1());
	addStage(new RoleInference());
	addStage(new DynamicSyncRemoval.QuadPass());
	addStage(new Transactions.QuadPass());
	addStage(new Realtime.QuadPass());
	addStage(new MZFCompilerStage());
	addStage(new MIPSOptimizations.QuadPass());

	// general quad optimizations (disabled by default)
        addStage(new GeneralQuadOptimizations());

	addStage(new EventDrivenTransformation.QuadPass2());
	addStage(new WriteBarriers.WBQuadPass());
	addStage(new WriteBarriers.DynamicWBQuadPass());

	// common processing and small optimizations
	addStage(new RegularQuadPass());
    }


    private static void buildTreeFormPipeline() {
	addStage(new WriteBarriers.WBDynamicWBTreePass());
	addStage(new PreallocOpt.TreePass());
	addStage(new Realtime.TreePass());
	addStage(new DynamicSyncRemoval.TreePass());
	addStage(new Transactions.TreePass());

	addStage(new RegularTreePass());

	// special case: the DACache MIPS optimizations allocated
	// special DACache registers during this phase; I *think* that
	// we shouldn't touch the tree form after this allocation has
	// been done, which means it must be the very last thing we
	// do.  In general, your tree form passes shouldn't be this
	// fragile!  [CSA 08-apr-2003]
	addStage(new MIPSOptimizations.TreePass());

	// perform basic sanity checks on the produced tree form
	addStage(new RegularCompilerStageEZ("sanity-check-tree") {
	    protected void real_action() {
		hcf = harpoon.Analysis.Tree.DerivationChecker.codeFactory(hcf);
		hcf = new CachingCodeFactory(hcf);
	    }
	});
    }

    

    // ADD YOUR OPTION CONSISTENCY TESTS HERE
    private static void checkOptionConsistency() {
	// Check for compatibility of precise gc options.
	if (PRECISEGC)
	    assert (BACKEND == Backend.PRECISEC) : 
	    "Precise gc is only implemented for the precise C backend.";
	if (MULTITHREADED) {
	    assert PRECISEGC || Realtime.REALTIME_JAVA :
		"Multi-threaded option is valid only for precise gc.";
	    assert WriteBarriers.wbOptLevel == 0 : 
		"Write barrier removal not supported " +
		"for multi-threaded programs.";
	}
	if (WriteBarriers.WRITEBARRIERS || WriteBarriers.DYNAMICWBS)
	    assert PRECISEGC : 
	    "Write barrier options are valid only for precise gc.";
    }


    private static void parseOpts(String[] args,
				  List<Option> allOptions) {
	PRECISEGC = System.getProperty("harpoon.alloc.strategy", 
				       "malloc").equalsIgnoreCase("precise");
	args = Option.parseOptions(allOptions, args);
	if(args.length != 0) {
	    System.err.println("Don't know what to do with " + args[0]);
	    System.err.println("Use option \"-h\" to find all options");
	    System.exit(1);
	}
    }


    private static List<Option> getAllOptions() {
	List<Option> opts = new LinkedList<Option>();
	Map<String,String> opt2stage = new HashMap<String,String>();
	for(CompilerStage stage : stages){
	    addOptions(stage, opts, opt2stage);
	}
	return opts;
    }


    private static void addOptions(CompilerStage stage,
				   List<Option> allOpts,
				   Map<String,String> opt2stage) {
	for(Option option : stage.getOptions()) {
	    String old_stage = 
		opt2stage.put(option.optionName(), stage.name());
	    if(old_stage != null) {
		System.err.println
		    ("Ambiguity: Option " + option +
		     " is defined in two compiler stages: " + 
		     old_stage + " and " + stage.name());
		System.exit(1);
	    }
	    allOpts.add(option);
	}
    }


    // construct list of top level compiler options
    private static List<Option> getTopLevelOptions() {
	return Arrays.asList
	    (new Option[] {
		new Option("c", "<class>", "Compile <class> (required)") {
		    public void action() { className = getArg(0); }
		},
		new Option("r", "<rootSetFileName>",
			   "Read additional roots (classes and/or methods) from file") {
		    public void action() { rootSetFilename = getArg(0); }
		},
		new Option("b", "<backendName>",
			   "Supported backends are StrongARM (default), MIPS, Sparc, or PreciseC") {
		    public void action() {
			BACKEND = Options.getBackendID(getArg(0));
		    }
		},
		new Option("F", "Enable standard optimizations") {
		    public void action() { OPTIMIZE = true; }
		},
		new Option("l", "Loop Optimizations") {
		    public void action() { LOOPOPTIMIZE = true; }
		},
		new Option("q", "Quiet mode (status messages not output)") {
		    public void action() { QUIET = true; }
		},
		new Option("h", "Print help") {
		    public void action() {
			SAMain.printHelp(System.out);
			System.exit(1);
		    }
		},
		new Option("m", "Multi-threading support for PreciseGC") {
		    public void action() {
			MULTITHREADED = true;
			System.out.println
			    ("Compiling with precise gc for multiple threads.");
		    }
		},
		new Option("verbose-load", "Show where each class is loaded from") {
		    public void action() { harpoon.ClassFile.Loader.VERBOSE = true; }
		}
	    });
    }

    private static void printHelp(PrintStream ps) {
	ps.println("Usage:\n\tjava "+SAMain.class.getName()+" <options>*");
	ps.println("Options:");
	for(Iterator<CompilerStage> it = stages.iterator(); it.hasNext(); ) {
	    printStageOptions(it.next());
	}
    }

    private static void printStageOptions(CompilerStage stage) {
	for(Option option : stage.getOptions()) {
	    option.printHelp(System.out);
	}
    }

    protected static void printHelp() {
	printHelp(System.out);
    }

    protected static void message(String msg) {
	if(!QUIET) System.out.print(msg);
    }

    protected static void messageln(String msg) {
	if(!QUIET) System.out.println(msg);
    }



    private static class BuildInitialCompState extends RegularCompilerStageEZ {
	public BuildInitialCompState() { super("compiler-initialization"); }
	public List<Option> getOptions() { return getTopLevelOptions(); }
	
	protected void real_action() { 
	    // create an initial compiler state
	    linker = new AbstractClassFixupRelinker(Loader.systemLinker);
	    mainM  = getMainMethod(linker);          // main method
	    frame  = construct_frame(mainM);         // target frame
	    roots  = getRoots(linker, mainM, frame); // set of roots
	}

	// returns the main method of the program to compile
	private static HMethod getMainMethod(Linker linker) {
	    // find main method
	    HClass hcl = linker.forName(className);
	    HMethod mainM;
	    try {
		mainM = hcl.getDeclaredMethod("main","([Ljava/lang/String;)V");
	    } catch (NoSuchMethodError e) {
		throw new Error("Class " + className + " has no main method");
	    }
	    assert mainM != null;
	    assert Modifier.isStatic(mainM.getModifiers()) : 
		"main is not static";
	    assert Modifier.isPublic(mainM.getModifiers()) : 
		"main is not public";
	    
	    return mainM;
	}
	

	// constructs and returns the target Frame
	// the frame specifies the combination of target architecture,
	// runtime, and allocation strategy we want to use.
	// ADD YOUR FRAME SETTING CODE HERE
	private static Frame construct_frame(HMethod mainM) {
	    Frame frame = Backend.getFrame(BACKEND, mainM,
					   getAllocationStrategyFactory());
	
	    // check the configuration of the runtime.
	    // (in particular, the --with-precise-c option)
	    if (BACKEND == Backend.PRECISEC)
		frame.getRuntime().configurationSet.add
		    ("check_with_precise_c_needed");
	    else
		frame.getRuntime().configurationSet.add
		    ("check_with_precise_c_not_needed");
	    
	    return frame;
	}


	// construct the set of roots for the program we compile
	static Set getRoots(Linker linker, HMethod mainM, Frame frame) {
	    // ask the runtime which roots it requires.
	    Set roots = new java.util.HashSet
		(frame.getRuntime().runtimeCallableMethods());
	    
	    // and our main method is a root, too...
	    roots.add(mainM);
	    
	    // load roots from file (if any)
	    if (rootSetFilename!=null)
		addToRootSet(roots, rootSetFilename, linker);
	    
	    // other optimization specific roots
	    if (EventDrivenTransformation.EVENTDRIVEN) {
		roots.add(linker.forName
			  ("harpoon.Analysis.ContBuilder.Scheduler")
			  .getMethod("loop",new HClass[0]));
	    }
	    
	    if (Realtime.REALTIME_JAVA)
		roots.addAll(Realtime.getRoots(linker));
	    
	    return roots;
	}

	private static void addToRootSet(final Set roots,
					 final String fileName,
					 final Linker linker) {
	    try {
		ParseUtil.readResource(fileName, new ParseUtil.StringParser() {
		    public void parseString(String s)
			throws ParseUtil.BadLineException {
			if (s.indexOf('(') < 0) // parse as class name.
			    roots.add(ParseUtil.parseClass(linker, s));
			else // parse as method name.
			    roots.add(ParseUtil.parseMethod(linker, s));
		    }
		});
	    } catch(IOException ex) {
		System.err.println("Error reading " + fileName + ": " + ex);
		ex.printStackTrace();
		System.exit(1);
	    }
	}
    }


    private static AllocationStrategyFactory getAllocationStrategyFactory() {
	
	return new AllocationStrategyFactory() {
	    public AllocationStrategy getAllocationStrategy(Frame frame) {
		
		System.out.print("Allocation strategy: ");
		
		if(AllocationInstrCompStage.INSTRUMENT_ALLOCS && 
		   (AllocationInstrCompStage.INSTRUMENT_ALLOCS_TYPE == 2)) {
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
		    System.getProperty("harpoon.alloc.strategy", "bdw");

		System.out.println(alloc_strategy);

		if (frame instanceof harpoon.Backend.PreciseC.Frame) {
		    // these strategies only work with the PreciseC backend
		    if(alloc_strategy.equalsIgnoreCase("nifty"))
			return new harpoon.Backend.PreciseC.
			    PGCNiftyAllocationStrategy(frame);
		    
		    if(alloc_strategy.equalsIgnoreCase("niftystats"))
			return new harpoon.Backend.PreciseC.	    
			    PGCNiftyAllocationStrategyWithStats(frame);
		} else {
		    // non-precisec version of the 'nifty' alloc strategy
		    if(alloc_strategy.equalsIgnoreCase("nifty"))
			return new harpoon.Backend.Runtime1.
			    NiftyAllocationStrategy(frame);
		}
		
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

		assert alloc_strategy.equalsIgnoreCase("malloc") :
		    "unknown allocation strategy " + alloc_strategy;

		// default, "malloc" strategy.
		return
		    new harpoon.Backend.Runtime1.MallocAllocationStrategy
		    (frame,
		     System.getProperty("harpoon.alloc.func", "malloc"));
	    }
	};
    };



    private static class BuildQuadForm extends RegularCompilerStageEZ {
	public BuildQuadForm() { super("build-quad-form"); }

	protected void real_action() {
	    // default code factory.
	    hcf = new CachingCodeFactory
		(harpoon.IR.Quads.QuadWithTry.codeFactory());
	
	    // the new mover will try to put NEWs closer to their
	    // constructors.  in the words of a PLDI paper, this
	    // reduces "drag time".  it also improves some analysis
	    // results. =)
	    hcf = new harpoon.Analysis.Quads.NewMover(hcf).codeFactory();

	    if (Realtime.REALTIME_JAVA)
		Realtime.setupObject(linker); 
	    
	    if(PreallocOpt.PREALLOC_OPT) {
		PreallocOpt.updateRoots(roots, linker);
		hcf = PreallocOpt.getHCFWithEmptyInitCode(hcf);
	    }
	    
	    // make a rough class hierarchy.
	    hcf = new CachingCodeFactory(hcf);
	    classHierarchy = new QuadClassHierarchy(linker, roots, hcf);
	    
	    // use the rough class hierarchy to devirtualize as many call
	    // sites as possible.
	    hcf = new harpoon.Analysis.Quads.Nonvirtualize
		(hcf, new harpoon.Backend.Maps.CHFinalMap(classHierarchy),
		 classHierarchy).codeFactory();
	    
	    handle_class_initializers();
	}

	// Class initialization is delicate in an ahead of time compiler.
	// The JVM deals with it by explicitly testing before each each
	// class member access whether the class is initialized or not; we
	// try to be more effcient: we run the initializers of all the
	// classes from the program before the main method.  Only the code
	// of the static initializers checks for un-initialized classes;
	// when the main method is called, all the relevant classes have
	// been initialized.
	private void handle_class_initializers() {
	    // transform the class initializers using the class hierarchy.
	    String resource = frame.getRuntime().resourcePath
		("init-safe.properties");
	    hcf = new harpoon.Analysis.Quads.InitializerTransform
		(hcf, classHierarchy, linker, resource).codeFactory();
	    // recompute the hierarchy after transformation.
	    hcf = new CachingCodeFactory(hcf);
	    classHierarchy = new QuadClassHierarchy(linker, roots, hcf);
	    // config checking
	    frame.getRuntime().configurationSet.add
		("check_with_init_check_needed");
	}
    }


    private static class RegularQuadPass extends RegularCompilerStageEZ {
	public RegularQuadPass() { super("regular-quad-pass"); }

	protected void real_action() {
	    // now we can finally set the (final?) classHierarchy and
	    // callGraph which the frame will use (among other things,
	    // for vtable numbering)
	    frame.setClassHierarchy(classHierarchy);

	    // virtualize any uncallable methods using the final classhierarchy
	    // (this keeps us from later getting link errors)
	    hcf = new harpoon.Analysis.Quads.Virtualize(hcf, classHierarchy)
		.codeFactory();
	    // non-virtualize any final methods -- this used to be done
	    // in the low-quad translation, but doing so is unsafe unless
	    // you've got a classHierarchy handy -- you could inadventently
	    // create a new dangling reference to an uncallable method.
	    hcf = new harpoon.Analysis.Quads.Nonvirtualize
		(hcf, new harpoon.Backend.Maps.CHFinalMap(classHierarchy),
		 classHierarchy).codeFactory();
	    
	    if (LOOPOPTIMIZE)
		loop_optimizations();
	    
	    hcf = harpoon.IR.LowQuad.LowQuadSSA.codeFactory(hcf);
	}

	private void loop_optimizations() {
	    // XXX: you might have to add a TypeSwitchRemover here, if
	    //      LoopOptimize doesn't handle TYPESWITCHes. --CSA
	    System.out.println("Loop Optimizations On");
	    hcf = harpoon.IR.LowQuad.LowQuadSSI.codeFactory(hcf);
	    hcf = harpoon.Analysis.LowQuad.Loop.LoopOptimize.codeFactory(hcf);
	    hcf = harpoon.Analysis.LowQuad.DerivationChecker.codeFactory(hcf);
	}
    }


    // 1. sparse (i.e., SSI) conditional constant propagation
    // 2. represent native method arraycopy into quad IR (such that
    // we can optimize it)
    // 3. inline small methods
    private static class GeneralQuadOptimizations extends CompilerStageEZ {
	public GeneralQuadOptimizations() { 
	    super("general-quad-optimizations");
	}

	public boolean enabled() { return OPTIMIZE; }

	public void real_action() {
	    // COMMENTED UNTIL BUG DISCOVERED
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
		hcf = new CachingCodeFactory(hcf);
		classHierarchy = new QuadClassHierarchy(linker, roots, hcf);
		hcf = new harpoon.Analysis.Quads.ArrayCopyInliner
		    (hcf, classHierarchy);
	    } else {
		// TODO: why is there a XOR between these two optimizations?
		// just inline small methods.
		hcf = new harpoon.Analysis.Quads.SmallMethodInliner
		    (hcf, classHierarchy);
	    }
	    hcf = harpoon.IR.Quads.QuadSSI.codeFactory(hcf);
	    hcf = harpoon.Analysis.Quads.SCC.SCCOptimize.codeFactory(hcf);
	    hcf = new harpoon.ClassFile.CachingCodeFactory(hcf);
	}
    }

    
    private static class LowQuadToTree extends RegularCompilerStageEZ {
	public LowQuadToTree() { super("lowquad-to-tree"); }

	protected void real_action() {
	    // low quad -> tree form
	    // XXX: ToTree doesn't handle TYPESWITCHes right now.
	    hcf =
		new harpoon.Analysis.Quads.TypeSwitchRemover(hcf).
		codeFactory();
	    hcf = harpoon.IR.Tree.TreeCode.codeFactory(hcf, frame);
	    // Implement the native method call interface. (JNI)
	    hcf = frame.getRuntime().nativeTreeCodeFactory(hcf);
	    // put it in canonical form
	    hcf = harpoon.IR.Tree.CanonicalTreeCode.codeFactory(hcf, frame);
	    // sanity check
	    hcf = harpoon.Analysis.Tree.DerivationChecker.codeFactory(hcf);
	}
    }


    private static class RegularTreePass extends RegularCompilerStageEZ {
	public RegularTreePass() { super("regular-tree-pass"); }

	protected void real_action() {
	    if (MULTITHREADED) /* pass to insert GC polling calls */
		hcf = harpoon.Backend.Analysis.MakeGCThreadSafe.
		    codeFactory(hcf, frame);
	    
	    // -- general tree form optimizations --

	    hcf = 
		harpoon.Analysis.Tree.AlgebraicSimplification.codeFactory(hcf);
	    
	    // CSA 08-apr-2003: this code was turned off in feb 17, 2000 with
	    //  the comment: "Turn off DeadCodeElimination, as it seems to be
	    //  generating bogus code --- removing too many moves?"
	    // Hopefully this bug will be fixed soon and this pass re-enabled.
	    //hcf = harpoon.Analysis.Tree.DeadCodeElimination.codeFactory(hcf);
	    
	    // CSA 07-11-2002: Temporarily turn off JumpOptimization.  It
	    // has a bug but I can't get the assertion to fail anymore.
	    // demo.grid.Server from the JacORB 1.3.30 distribution
	    // compiles to an infinite loop with JumpOptimization enabled,
	    // however.
	    //hcf = harpoon.Analysis.Tree.JumpOptimization.codeFactory(hcf);
	}
    }
}
