// PointerAnalysisCompStage.java, created Thu Apr 17 15:24:32 2003 by salcianu
// Copyright (C) 2003 Alexandru Salcianu <salcianu@MIT.EDU>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.PointerAnalysis;

import harpoon.ClassFile.CachingCodeFactory;
import harpoon.ClassFile.HClass;
import harpoon.ClassFile.NoSuchClassException;
import harpoon.ClassFile.HMethod;
import harpoon.ClassFile.HCode;
import harpoon.ClassFile.HCodeFactory;
import harpoon.ClassFile.Linker;

import harpoon.IR.Quads.Code;
import harpoon.IR.Quads.QuadNoSSA;
import harpoon.IR.Quads.Quad;
import harpoon.IR.Quads.CALL;

import harpoon.Analysis.Quads.CallGraph;
import harpoon.Analysis.Quads.CallGraphImpl;
import harpoon.Analysis.Quads.CachingCallGraph;
import harpoon.Analysis.ClassHierarchy;

import harpoon.Analysis.MetaMethods.MetaCallGraph;
import harpoon.Analysis.MetaMethods.MetaMethod;
import harpoon.Analysis.MetaMethods.FakeMetaCallGraph;
import harpoon.Analysis.MetaMethods.SmartCallGraph;

import harpoon.Util.LightBasicBlocks.CachingLBBConverter;
import harpoon.Util.LightBasicBlocks.CachingSCCLBBFactory;
import harpoon.Util.BasicBlocks.CachingBBConverter;

import harpoon.Main.CompilerStage;
import harpoon.Main.CompilerStageEZ;
import harpoon.Main.CompStagePipeline;
import harpoon.Main.CompilerState;
import harpoon.Util.Options.Option;
import harpoon.Util.Util;

import java.util.List;
import java.util.LinkedList;
import java.util.Set;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Collection;

import java.io.Serializable;
import java.io.IOException;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 * <code>PointerAnalysisCompStage</code>
 * 
 * @author  Alexandru Salcianu <salcianu@MIT.EDU>
 * @version $Id: PointerAnalysisCompStage.java,v 1.5 2003-06-04 18:44:32 salcianu Exp $
 */
public class PointerAnalysisCompStage extends CompilerStageEZ {

    /** Returns a compiler stage that is the sequential composition of
        a Pointer Analysis Construction stage
        (<code>PointerAnalysisCompStage</code>), a stage that uese
        pointer analysis information for allocation optimizations and
        synchronization removal (<code>AllocSyncOptCompStage</code>,
        and a stage that offers support for debugging and optimizing
        RTJ programs (<code>PARTJSupportCompStage</code>).

	@see PointerAnalysisCompStage
	@see AllocSyncOptCompStage
	@see PARTJSupportCompStage. */
    public static CompilerStage getPAAndAppsStage() {
	List/*<CompilerStage>*/ paStages = new LinkedList/*<CompilerStage>*/();

	paStages.add(new PointerAnalysisCompStage(true));

	final CompilerStage allocSyncOpt = new AllocSyncOptCompStage();
	paStages.add(allocSyncOpt);

	final CompilerStage rtjSupport = new PARTJSupportCompStage();
	paStages.add(rtjSupport);

	return new CompStagePipeline(paStages, "pa-and-applications") {
	    public boolean enabled() {
		return allocSyncOpt.enabled() || rtjSupport.enabled();
	    }
	};
    }

    /** Creates a <code>PointerAnalysisCompStage</code> object. 

	@param extEnabled If <code>true</code>, <code>this</code>
	compiler stage is enabled even if no direct task is indicated
	in the command line options.  In general, this corresponds to
	the case where the <code>PointerAnalysis</code> object created
	here is required by a later stage. */
    public PointerAnalysisCompStage(boolean extEnabled) { 
	super("pointer-analysis");
	this.extEnabled = extEnabled;
    }

    /** Convenient constructor: the created compiler stage is not
        externally enabled. */
    public PointerAnalysisCompStage() { this(false); }
    
    
    public List/*<Option>*/ getOptions() {
	List/*<Option>*/ opts = new LinkedList/*<Option>*/();
	
	opts.add(new Option("pa:d", "Do not use the SmartCallGraph") {
	    public void action() { SMART_CALL_GRAPH = false; }
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
	
	opts.add(new Option("pa:ccs", "<level>",
			    "Pointer analysis call context sensitivity (default none)") {
	    public void action() {
		PointerAnalysis.CALL_CONTEXT_SENSITIVE = true;
		PointerAnalysis.MAX_SPEC_DEPTH = Integer.parseInt(getArg(0));
	    }
	});

	opts.add(new Option("pa:record-actions", "Record thread actions") {
	    public void action() { PointerAnalysis.RECORD_ACTIONS = true; }
	});
 
	if(Boolean.getBoolean("debug.pa"))
	    add_debug_options(opts);
	
	return opts;
    }
    
    
    private void add_debug_options(List opts) {
	opts.add(new Option("pa:timing", 
			    "Time pre-analysis for Pointer Analysis") {
	    public void action() { TIMING = true; }
	});
	
	opts.add(new Option("pa:show-cg", "Show Call Graph") {
	    public void action() { SHOW_CG = true; }
	});
	
	opts.add(new Option("pa:a", "<method>", "<detail>", 
			    "Run Pointer Analysis over one method; if optional argument <detail> is present, the result of analysis is shown for each program point inside the method.") {
	    public void action() {
		DO_ANALYSIS = true;
		if(methodsToAnalyze == null)
		    methodsToAnalyze = new HashSet();
		methodsToAnalyze.add(getArg(0));
		if(getOptionalArg(0) != null)
		    INTERACTIVE_ANALYSIS_DETAILS = true;
	    }
	});
	
	opts.add(new Option("pa:i", "", "<detail>",
			    "Interactive Pointer Analysis; if optional argument <detail> is present, the result of analysis is shown for each program point inside a method.") {
	    public void action() {
		INTERACTIVE_ANALYSIS = true;
		if(getOptionalArg(0) != null)
		    INTERACTIVE_ANALYSIS_DETAILS = true;
	    }
	});

	opts.add(new Option("pa:cg-stats", "Call graph statistics") {
	    public void action() { CALL_GRAPH_STATS = true; }
	});

    };
    
    
    private boolean LOAD_PRE_ANALYSIS = false;
    private boolean SAVE_PRE_ANALYSIS = false;
    private String preAnalysisFileName = null;
    
    // by defult, use the SmartCallGraph
    private boolean SMART_CALL_GRAPH  = true;
    
    private boolean TIMING = false;
    private boolean SHOW_CG = false;
    
    private boolean DO_ANALYSIS = false;
    private Set methodsToAnalyze = null;
    
    private boolean INTERACTIVE_ANALYSIS = false;
    private boolean INTERACTIVE_ANALYSIS_DETAILS = false;
    
    private boolean CALL_GRAPH_STATS = false;

    public boolean enabled() {
	return extEnabled || DO_ANALYSIS || INTERACTIVE_ANALYSIS;
    }
    private boolean extEnabled = false;
    
    protected void real_action() {
	PreAnalysisRes pre_analysis =
	    LOAD_PRE_ANALYSIS ? load_pre_analysis() : do_pre_analysis();
	
	if(SAVE_PRE_ANALYSIS)
	    save_pre_analysis(pre_analysis);
	
	PointerAnalysis pa = 
	    new PointerAnalysis(pre_analysis.mcg,
				pre_analysis.caching_scc_lbb_factory,
				linker, classHierarchy);
	
	if(DO_ANALYSIS)
	    do_analysis(pa, methodsToAnalyze);
	
	if(INTERACTIVE_ANALYSIS)
	    interactive_analysis(pa);
	
	attribs = attribs.put("PointerAnalysis", pa);
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
	    CompilerState cs = _PACK_CS();
	    oos.writeObject(cs); // compiler state
	    _UNPACK_CS(cs);      // don't worry if you don't understand this
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
	
	hcf = new CachingCodeFactory(QuadNoSSA.codeFactory(hcf), true);
	
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

	if(CALL_GRAPH_STATS)
	    call_graph_stats(mcg);
	
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
	
	if(SHOW_CG) {
	    System.out.println("PA: MetaCallGraph:");
	    mcg.print(System.out, true, new MetaMethod(mainM, true));
	}
	
	return mcg;
    }
    
    
    private void do_analysis(PointerAnalysis pa, Set/*<String>*/ methods) {
	for(Iterator/**/ it = methods.iterator(); it.hasNext(); )
	    display_pa4method(pa, (String) it.next());
    }
    
    private void interactive_analysis(PointerAnalysis pa) {
	BufferedReader d = 
	    new BufferedReader(new InputStreamReader(System.in));
	while(true) {
	    System.out.print("Method name:");
	    String method = null;
	    try {
		method = d.readLine();
	    } catch(IOException e) {
		handle_fatal_error(e, "Error reading from System.in");
	    }
	    if(method == null) { // EOF received
		System.out.println();
		break;
	    }
	    display_pa4method(pa, method);
	}
    }
    
    
    private void display_pa4method(PointerAnalysis pa, String method) {
	int point_pos = method.lastIndexOf('.');
	String declClassName = 
	    (point_pos != -1) ?
	    method.substring(0, point_pos) :
	    // by default, same class as root method
	    mainM.getDeclaringClass().getName();
	String methodName = method.substring(point_pos + 1);
	
	HClass hclass = null;
	try {
	    hclass = linker.forName(declClassName);
	} catch (NoSuchClassException e) {
	    System.err.println("Class " + declClassName + " not found!");
	    return;
	}
	
	int count = 0;
	HMethod[] hms  = hclass.getDeclaredMethods();
	for (int i = 0; i < hms.length; i++) {
	    if(!hms[i].getName().equals(methodName))
		continue;
	    count++;
	    HMethod hm = hms[i];
	    
	    long tstart = TIMING ? time() : 0L;
	    ParIntGraph int_pig = pa.getIntParIntGraph(hm);
	    ParIntGraph ext_pig = pa.getExtParIntGraph(hm);
	    if(TIMING)
		System.out.println("Analysis took " + (time()-tstart) + "ms");
	    
	    System.out.println("METHOD " + hm);
	    display_pointer_parameters(hm, pa);
	    System.out.print("INT. GRAPH AT THE END OF THE METHOD:");
	    System.out.println(int_pig);
	    System.out.print("EXT. GRAPH AT THE END OF THE METHOD:");
	    System.out.println(ext_pig);
	    
	    if(INTERACTIVE_ANALYSIS_DETAILS) {
		HCode hcode = hcf.convert(hm);
		for(Iterator itq = hcode.getElementsI(); itq.hasNext(); ) {
		    Quad q = (Quad) itq.next();
		    System.out.println("Graph just before <<" + 
				       Util.code2str(q) + ">>: " +
				       pa.getPIGAtQuad(hm, q));
		}
	    }
	}
	
	if(count == 0)
	    System.err.println("Method " + method + " not found!");
    }
    
    
    // displays the parameter nodes for method hm
    // TODO: print the sources of all the nodes that appear in displayed pigs.
    private void display_pointer_parameters(HMethod hm, PointerAnalysis pa) {
	PANode[] nodes = pa.getParamNodes(new MetaMethod(hm, true));
	System.out.print("POINTER PARAMETERS: ");
	System.out.print("[ ");
	for(int j = 0; j < nodes.length; j++)
	    System.out.print(nodes[j] + " ");
	System.out.println("]");
    }
    
    
    
    private void call_graph_stats(MetaCallGraph mcg) {
	long nb_nvirtual_calls  = 0L;
	long nb_virtual_calls = 0L;
	long vcalls[] = new long[100];
	long nb_total_callees = 0L;
	for(int i = 0; i < vcalls.length; i++)
	    vcalls[i] = 0L;
	
	for(Iterator it = mcg.getAllMetaMethods().iterator(); it.hasNext();) {
	    MetaMethod mm = (MetaMethod) it.next();
	    HCode hcode = hcf.convert(mm.getHMethod());
	    if(hcode == null) continue;
	    Collection calls = ((Code) hcode).selectCALLs();
	    
	    for(Iterator it_calls = calls.iterator(); it_calls.hasNext(); ) {
		CALL call = (CALL) it_calls.next();
		MetaMethod[] callees = mcg.getCallees(mm, call);
		int nb_callees = callees.length;
		if(callees.length == 0)
		    System.out.println("EMPTY CALL " + Util.code2str(call) +
				       "\n  in " + mm);
		nb_total_callees += nb_callees;
		if(call.isVirtual()) {
		    nb_virtual_calls++;
		    if(nb_callees >= vcalls.length)
			nb_callees = vcalls.length - 1;
		    vcalls[nb_callees]++;
		}
		else
		    nb_nvirtual_calls++;
	    }
	}
	
	long nb_calls = nb_virtual_calls + nb_nvirtual_calls;
	System.out.println("\nCALL SITES STATISTICS:\n");
	System.out.println("Total calls       = " + nb_calls);
	System.out.println("Non-virtual calls = " + nb_nvirtual_calls + "\t" +
			   Util.percentage(nb_nvirtual_calls, nb_calls));
	System.out.println("Virtual calls     = " + nb_virtual_calls + "\t" +
			   Util.percentage(nb_virtual_calls, nb_calls));
	for(int i = 0; i < vcalls.length; i++)
	    if(vcalls[i] > 0)
		System.out.println
		    ("  " + i + " callee(s) = " + vcalls[i] +
		     "\t" + Util.percentage(vcalls[i], nb_virtual_calls));
	System.out.println
	    ("Average callees/call site = " + 
	     Util.doubleRep(((double) nb_total_callees) / nb_calls, 2));
	System.out.println("-----------------------------------------------");
    }
    
    
    private static long time() {
	return System.currentTimeMillis();
    }
    
}
