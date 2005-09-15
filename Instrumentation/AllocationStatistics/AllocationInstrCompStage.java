// AllocationInstrCompStage.java, created Tue Apr 15 17:31:35 2003 by salcianu
// Copyright (C) 2003 Alexandru Salcianu <salcianu@MIT.EDU>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Instrumentation.AllocationStatistics;

import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HCodeFactory;
import harpoon.ClassFile.CachingCodeFactory;

import harpoon.IR.Quads.QuadNoSSA;

import harpoon.Analysis.Quads.QuadClassHierarchy;

import harpoon.Main.CompilerState;
import harpoon.Main.CompilerStage;

import harpoon.Util.Options.Option;

import java.util.List;
import java.util.LinkedList;
import java.io.ObjectOutputStream;
import java.io.FileOutputStream;

/**
 * <code>AllocationInstrCompStage</code>
 * 
 * @author  Alexandru Salcianu <salcianu@MIT.EDU>
 * @version $Id: AllocationInstrCompStage.java,v 1.4 2005-09-15 04:01:42 salcianu Exp $
 */
public class AllocationInstrCompStage extends CompilerStage {

    /** Creates a <code>AllocationInstrCompStage</code>. */
    public AllocationInstrCompStage() { super("allocation-instrumentation"); }

    private boolean READ_ALLOC_STATS = false;
    private String allocNumberingFileName;
    private String instrumentationResultsFileName;
    public static boolean INSTRUMENT_ALLOCS = false;
    // 1 - Brian's instrumentation
    // 2 - Alex's  instrumentation
    public static int INSTRUMENT_ALLOCS_TYPE = 2;
    // TODO: add command line options about instrumenting syncs/calls
    private boolean INSTRUMENT_SYNCS = false;
    private boolean INSTRUMENT_CALLS = false;
    private boolean INSTRUMENT_ALLOCS_STUB = false;
    private String IFILE = null;
    private boolean PRINT_ALLOC_STATS = false;

    private AllocationStatistics as = null;
    public AllocationStatistics getAllocationStatistics() { return as; }

    public List/*<Option>*/ getOptions() {
	List/*<Option>*/ opts = new LinkedList/*<Option>*/();

	opts.add(new Option("N", "<fileName>",
			    "Serialize compiler state + allocation numbering object to <fileName>") {
	    public void action() { 
		INSTRUMENT_ALLOCS = true;
		IFILE = getArg(0);
	    }
	});

	opts.add(new Option("W", "<fileName>",
			    "Same as -N, but writes only a small text file (an AllocationNumberingStub).  This file can be read with the -Z option.  Use if serialization makes troubles.") {
	    public void action() {
		INSTRUMENT_ALLOCS = true;
		INSTRUMENT_ALLOCS_STUB = true;
		IFILE = getArg(0);
	    }
	});

	opts.add(new Option("Z",
			    "<allocNumberingFileName> <instrResultFileName>",
			    "Reads in allocation numbering stub and instrumentation results") {
	    public void action() {
		READ_ALLOC_STATS = true;
		allocNumberingFileName = getArg(0);
		instrumentationResultsFileName = getArg(1);
		System.out.println("allocNumberingFileName = " + 
				   allocNumberingFileName);
		System.out.println("instrumentationResultsFileName = " +
				   instrumentationResultsFileName);
	    }
	});

	opts.add(new Option("print-alloc-stats", "prints dynamic memory allocation statistics") {
	    public void action() { PRINT_ALLOC_STATS = true; }
	});

	return opts;
    }

    public boolean enabled() {
	return INSTRUMENT_ALLOCS || READ_ALLOC_STATS;
    }

    public CompilerState action(CompilerState cs) {
	assert !PRINT_ALLOC_STATS || READ_ALLOC_STATS :
	    "-print-alloc-stats requires -Z";

	if (INSTRUMENT_ALLOCS)
	    return instrument_allocations(cs);
	// Try not to add anything in between instrument_allocs and
	// read_allocation_statistics.  When we load the allocation
	// stats, we want to be in the same place where we were when
	// we instrumented the code.
	if (READ_ALLOC_STATS)
	    return read_allocation_statistics(cs);

	return cs;
    }

    private CompilerState instrument_allocations(CompilerState cs) {
	// A: make sure we have a caching NoSSA code factory
	cs = ensure_caching_NoSSA(cs);

	// create the allocation numbering
	AllocationNumbering an =
	    new AllocationNumbering((CachingCodeFactory) cs.getCodeFactory(),
				    cs.getClassHierarchy(), INSTRUMENT_CALLS);
	
	try {
	    if(INSTRUMENT_ALLOCS_STUB) { // "textualize" only a stub
		System.out.println
		    ("Writing AllocationNumbering into " + IFILE);
		AllocationNumberingStub.writeToFile(an, IFILE, cs.getLinker());
	    }
	    else { // classic INSTRUMENT_ALLOCS: serialize serious stuff
		ObjectOutputStream oos =
		    new ObjectOutputStream(new FileOutputStream(IFILE));
		oos.writeObject(cs);
		oos.writeObject(an);
		oos.close();
	    }
	} catch (java.io.IOException e) {
	    System.out.println(e + " was thrown:");
	    e.printStackTrace(System.out);
	    System.exit(1);
	}
	
	switch(INSTRUMENT_ALLOCS_TYPE) {
	case 1: // Brian's instr.
	    cs = cs.changeCodeFactory
		((new InstrumentAllocs(cs.getCodeFactory(), cs.getMain(),
				       cs.getLinker(), an,
				       INSTRUMENT_SYNCS,
				       INSTRUMENT_CALLS)).codeFactory());
	    break;
	case 2: // Alex's instr. (no support for counting syncs)
	    cs = cs.changeCodeFactory
		((new InstrumentAllocs2(cs.getCodeFactory(), cs.getMain(),
					cs.getLinker(), an)).codeFactory());
	    // TODO: this is NOT functional ...
	    cs.getRoots().add(InstrumentAllocs.getMethod
			      (cs.getLinker(),
			       "harpoon.Runtime.CounterSupport", "count2",
			       new HClass[]{HClass.Int, HClass.Int}));
	    break;
	default:
	    assert false :
		"Illegal INSTRUMENT_ALLOCS_TYPE" + INSTRUMENT_ALLOCS_TYPE;
	}
	
	cs = cs.changeCodeFactory(new CachingCodeFactory(cs.getCodeFactory()));
	cs = cs.changeClassHierarchy
	    (new QuadClassHierarchy(cs.getLinker(), cs.getRoots(),
				    cs.getCodeFactory()));
	return cs;
    }


    private CompilerState read_allocation_statistics(CompilerState cs) {
	// B: make sure we have the same code factory as in A above
	cs = ensure_caching_NoSSA(cs);

	as = new AllocationStatistics(cs.getLinker(),
				      allocNumberingFileName,
				      instrumentationResultsFileName);
	if(PRINT_ALLOC_STATS)
	    as.printStatistics
		(AllocationStatistics.getAllocs
		 (cs.getClassHierarchy().callableMethods(),
		  cs.getCodeFactory()));
	
	return cs;
    }


    private CompilerState ensure_caching_NoSSA(CompilerState cs) {
	// make sure we have a caching NoSSA code factory
	HCodeFactory hcf = QuadNoSSA.codeFactory(cs.getCodeFactory());
	hcf = new CachingCodeFactory(hcf, true);
	return cs.changeCodeFactory(hcf);
    }

}
