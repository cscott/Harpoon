// PointerAnalysisCompStage.java, created Thu Apr 17 15:24:32 2003 by salcianu
// Copyright (C) 2003 Alexandru Salcianu <salcianu@MIT.EDU>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.PointerAnalysis;

import harpoon.ClassFile.CachingCodeFactory;
import harpoon.ClassFile.HMethod;
import harpoon.ClassFile.HCode;
import harpoon.ClassFile.HCodeFactory;
import harpoon.ClassFile.Linker;

import harpoon.IR.Quads.QuadNoSSA;

import harpoon.Analysis.Quads.CallGraph;
import harpoon.Analysis.Quads.CallGraphImpl;
import harpoon.Analysis.Quads.CachingCallGraph;
import harpoon.Analysis.ClassHierarchy;

import harpoon.Analysis.MetaMethods.MetaCallGraph;
import harpoon.Analysis.MetaMethods.FakeMetaCallGraph;
import harpoon.Analysis.MetaMethods.MetaAllCallers;
import harpoon.Analysis.MetaMethods.SmartCallGraph;

import harpoon.Util.LightBasicBlocks.CachingLBBConverter;
import harpoon.Util.LightBasicBlocks.CachingSCCLBBFactory;
import harpoon.Util.BasicBlocks.CachingBBConverter;

import harpoon.Main.CompilerStageEZ;
import harpoon.Main.CompilerState;
import harpoon.Util.Options.Option;
import harpoon.Util.Timer;

import java.util.List;
import java.util.LinkedList;
import java.util.Set;
import java.util.Iterator;

import java.io.Serializable;
import java.io.IOException;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;

/**
 * <code>PointerAnalysisCompStage</code>
 * 
 * @author  Alexandru Salcianu <salcianu@MIT.EDU>
 * @version $Id: PointerAnalysisCompStage.java,v 1.1 2003-04-18 16:27:16 salcianu Exp $
 */
public class PointerAnalysisCompStage extends CompilerStageEZ {
    
    public PointerAnalysisCompStage() { super("pointer-analysis"); }


    public List/*<Option>*/ getOptions() {
	List/*<Option>*/ opts = new LinkedList/*<Option>*/();

	opts.add(new Option("pa:s", "Use Smart CallGraph") {
	    public void action() { SMART_CALL_GRAPH = true; }
	});
	
	opts.add(new Option("pa:load-pre-analysis", "<fileName>",
			    "Deserialize pre-analysis results from a file") {
	    public void action() {
		LOAD_PRE_ANALYSIS = true;
		preAnalysisFileName = getArg(0);
	    }
	});

	opts.add(new Option("pa:save-pre-analysis", "<fileName>",
			    "Serialize pre-analysis results into a file") {
	    public void action() {
		SAVE_PRE_ANALYSIS = true;
		preAnalysisFileName = getArg(0);
	    }
	});

	opts.add(new Option("pa:timing", "Time Pointer Analysis") {
	    public void action() { TIMING = true; }
	});
	
	return opts;
    }
    
    private boolean LOAD_PRE_ANALYSIS = false;
    private boolean SAVE_PRE_ANALYSIS = false;
    private String preAnalysisFileName = null;

    private boolean SMART_CALL_GRAPH  = false;

    private boolean TIMING = false;

    private PointerAnalysis pa = null;

    protected boolean enabled() { return true; }

    protected void real_action() {

	PreAnalysisRes pre_analysis =
	    LOAD_PRE_ANALYSIS ? load_pre_analysis() : do_pre_analysis();

	if(SAVE_PRE_ANALYSIS)
	    save_pre_analysis(pre_analysis);

	pa = new PointerAnalysis(pre_analysis.mcg,
				 pre_analysis.caching_scc_lbb_factory,
				 linker);
    }

    private static class PreAnalysisRes implements java.io.Serializable {
	public final MetaCallGraph  mcg;
	public final CachingSCCLBBFactory caching_scc_lbb_factory;

	public PreAnalysisRes(MetaCallGraph mcg, CachingSCCLBBFactory fact) {
	    this.mcg = mcg;
	    this.caching_scc_lbb_factory = fact;
	}
    }


    private PreAnalysisRes load_pre_analysis() {
	try {
	    ObjectInputStream ois = new ObjectInputStream
		(new FileInputStream(preAnalysisFileName));
	    _UNPACK_CS((CompilerState) ois.readObject()); // compiler srate
	    PreAnalysisRes pre_analysis = (PreAnalysisRes) ois.readObject();
	    ois.close();
	    return pre_analysis;
	} catch (Exception e) {
	    handle_fatal_error(e, "Error while deserializing PA pre-analysis");
	    return null;
	}
    }


    private void save_pre_analysis(PreAnalysisRes pre_analysis) {
	try {
	    ObjectOutputStream oos = new ObjectOutputStream
		(new FileOutputStream(preAnalysisFileName));
	    oos.writeObject(_PACK_CS()); // compiler state
	    oos.writeObject(pre_analysis);
	    oos.close();
	} catch (IOException e) {
	    handle_fatal_error(e, "Error while serializing PA pre-analysis");
	}
    }


    private void handle_fatal_error(Exception e, String message) {
	System.err.println(message + " " + e);
	e.printStackTrace();
	System.exit(1);
    }


    private PreAnalysisRes do_pre_analysis() {

	hcf = new CachingCodeFactory(QuadNoSSA.codeFactory(), true);
	
	if(TIMING)
	    time_ir_generation();
	
	// there is something crazy here: too messy & too much caching
	CachingSCCLBBFactory scc_lbb_factory = 
	    new CachingSCCLBBFactory
	    (new CachingLBBConverter
	     (new CachingBBConverter(hcf)));
	
	if(TIMING)
	    time_scc_lbb_generation(scc_lbb_factory);
	
	MetaCallGraph mcg = get_meta_call_graph();
	
	return
	    new PreAnalysisRes(mcg, scc_lbb_factory);
    }


    private void time_ir_generation() {
	System.out.print(hcf.getCodeName() + " IR generation ... ");
	long tstart = time();
	
	for(Iterator it = classHierarchy.callableMethods().iterator();
	    it.hasNext(); ) {
	    hcf.convert((HMethod) it.next());
	}

	System.out.println((time() - tstart) + " ms");
    }

    
    private void time_scc_lbb_generation(CachingSCCLBBFactory scc_lbb_fact) {
	System.out.print("SCC LBB generation ... ");
	long tstart = time();

	for(Iterator it = classHierarchy.callableMethods().iterator();
	    it.hasNext(); ) {
	    HMethod hm = (HMethod) it.next();
	    HCode hcode = hcf.convert(hm);
	    if(hcode != null)
		scc_lbb_fact.computeSCCLBB(hm);
	}

	System.out.println((time() - tstart) + " ms");
    }


    private MetaCallGraph get_meta_call_graph() {
	long tstart = 0L;
	
	if(TIMING)
	    tstart = time();
	
	CallGraph cg =
	    SMART_CALL_GRAPH ?
	    (CallGraph) 
	    (new SmartCallGraph((CachingCodeFactory) hcf, linker,
				classHierarchy, roots)) :	    
	    (CallGraph)
	    (new CachingCallGraph(new CallGraphImpl(classHierarchy, hcf)));
	
	MetaCallGraph mcg = new FakeMetaCallGraph(cg);
	
	if(TIMING)
	    System.out.println("(Fake)MetaCallGraph construction " +
			       (time() - tstart) + " ms");

	return mcg;
    }

    private static long time() {
	return System.currentTimeMillis();
    }
}
