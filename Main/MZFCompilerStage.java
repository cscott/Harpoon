// MZFCompilerStage.java, created Sat Apr 12 13:07:52 2003 by salcianu
// Copyright (C) 2003 Alexandru Salcianu <salcianu@MIT.EDU>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Main;

import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HMethod;
import harpoon.Analysis.Quads.QuadClassHierarchy;
import harpoon.Util.Options.Option;

import java.util.Iterator;
import java.util.List;
import java.util.Collections;
import java.util.Arrays;

/**
 * <code>MZFCompilerStage</code>
 * 
 * @author  Alexandru Salcianu <salcianu@MIT.EDU>
 * @version $Id: MZFCompilerStage.java,v 1.2 2003-04-22 00:09:57 salcianu Exp $
 */
public class MZFCompilerStage extends CompilerStageEZ {
    
    /** Creates a <code>MZFCompilerStage</code>. */
    public MZFCompilerStage() { super("mzf"); }

    public List/*<Option>*/ getOptions() {
	// no command line options: uses system properties instead
	return Collections.EMPTY_LIST;
    }

    // a bit bogus: real_action will do the real tests
    public boolean enabled() { return true; }

    protected void real_action() {
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
    }
}
