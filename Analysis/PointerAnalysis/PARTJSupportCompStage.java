// PARTJSupportCompStage.java, created Tue Apr 22 16:24:48 2003 by salcianu
// Copyright (C) 2003 Alexandru Salcianu <salcianu@MIT.EDU>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.PointerAnalysis;

import harpoon.ClassFile.HClass;
import harpoon.ClassFile.NoSuchClassException;
import harpoon.ClassFile.HMethod;
import harpoon.ClassFile.HCode;

import harpoon.Analysis.MetaMethods.MetaMethod;

import harpoon.IR.Quads.Quad;
import harpoon.IR.Quads.CALL;

import harpoon.Util.TypeInference.TypeInference;
import harpoon.Util.TypeInference.ExactTemp;

import harpoon.Main.CompilerStage;
import harpoon.Main.CompilerStageEZ;
import harpoon.Main.CompStagePipeline;
import harpoon.Util.Options.Option;

import harpoon.Util.Util;

import java.util.List;
import java.util.LinkedList;
import java.util.Set;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Collections;

import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 * <code>PARTJSupportCompStage</code>
 * 
 * @author  Alexandru Salcianu <salcianu@MIT.EDU>
 * @version $Id: PARTJSupportCompStage.java,v 1.2 2004-02-08 03:20:03 cananian Exp $
 */
public class PARTJSupportCompStage extends CompilerStageEZ {
    
    /** Creates a <code>PARTJSupportCompStage</code>. */
    public PARTJSupportCompStage() { super("pa-4-rtj"); }

    /** Returns a compiler stage consisting of the sequential
        composition of a pointer analysis stage and a
        <code>PARTJSupportCompStage</code>. */
    public static CompilerStage getFullStage() {
	final CompilerStage rtjSupp = new PARTJSupportCompStage();
	return 
	    new CompStagePipeline(new PointerAnalysisCompStage(true),
				  rtjSupp) {
	    public boolean enabled() { return rtjSupp.enabled(); }
	};
    }


    // activates support for RTJ debug
    private boolean RTJ_DEBUG = false;

    // keep all the rtj related memory assignments checks
    private final static int RTJ_CR_KEEP_ALL_CHECKS   = 0;
    // use the inter-proc analysis for check removal
    private final static int RTJ_CR_INTER_PROC   = 1;
    // use the inter-thread analysis for check removal
    private final static int RTJ_CR_INTER_THREAD = 2;
    // policy for RTJ check removal: should be one of the previous three
    private int RTJ_CR_POLICY = RTJ_CR_KEEP_ALL_CHECKS;

    // examine all run() methods
    private final static int RTJ_RI_ALL_RUNS = 0;
    // examine only the run() methods belonging to classes that are
    // passed as arguments to MemoryArea.enter()
    private final static int RTJ_RI_ENTER    = 1;
    // policy for identifying the relevant run() methods
    private int RTJ_RI_POLICY = RTJ_RI_ENTER;


    public List/*<Option>*/ getOptions() {
	List/*<Option>*/ opts = new LinkedList/*<Option>*/();
	opts.add(new Option("rtj-debug", "RTJ debug (interactive method inspecyion), with the help of Pointer Analysis") {
	    public void action() { RTJ_DEBUG = true; }
	});

	opts.add(new Option("rtj-check-removal", "", "<runs> wit", "Try to use Pointer Analysis to remove all RTJ memory checks.  Optional argument <runs> = {allruns,enter} and tells how the relevant run methods are identified (default is enter).  If the optional argument \"wit\" is present, then use inter-thread analysis (by default, just inter-procedural pointer analysis") {
	    public void action() {
		RTJ_CR_POLICY = RTJ_CR_INTER_PROC;
    
		if(getOptionalArg(0) != null) {
		    String runs = getOptionalArg(0);
		    if(runs.equals("allruns"))
			RTJ_RI_POLICY = RTJ_RI_ALL_RUNS;
		    else if(runs.equals("enter"))
			RTJ_RI_POLICY = RTJ_RI_ENTER;
		    else {
			System.err.println("Unknown <runs> options" + runs);
			System.exit(1);
		    }

		    if(getOptionalArg(1) != null) {
			if(getOptionalArg(1).equals("wit"))
			    RTJ_CR_POLICY = RTJ_CR_INTER_THREAD;
			else {
			    System.err.println("Unknown optional arg" + 
					       getOptionalArg(1));
			    System.exit(1);
			}
		    }
		}
	    }
	});
	return opts;
    }


    public boolean enabled() {
	return 
	    RTJ_DEBUG || (RTJ_CR_POLICY != RTJ_CR_KEEP_ALL_CHECKS);	    
    }


    public void real_action() {
	pa = (PointerAnalysis) attribs.get("PointerAnalysis");
	assert pa != null : "No PointerAnalysis object";

	if(RTJ_DEBUG) {
	    do_rtj_debug();
	    System.exit(0);
	}

	switch(RTJ_CR_POLICY) {
	case RTJ_CR_KEEP_ALL_CHECKS:
	    break;
	case RTJ_CR_INTER_PROC:
	case RTJ_CR_INTER_THREAD:
	    java_lang_Runnable = linker.forName("java.lang.Runnable");
	    if(can_remove_all_checks()) {
		System.out.println("RTJ: can remove all checks!");
		String rtj_options = "all";
		// TODO: let the RTJ stage know that it may remove
		// all memory checks!
	    }
	    else
		System.out.println("RTJ: cannot remove all checks!");
	    java_lang_Runnable = null;
	    break;
	default:
	    System.err.println("Unknown RTJ_CR_POLICY " + RTJ_CR_POLICY);
	    System.exit(1);
	}
    }

    private PointerAnalysis pa = null;
    private HClass java_lang_Runnable = null;


    //////////////////////////////////////////////////////////////////////
    /////////////////////// RTJ DEBUG STUFF STARTS ///////////////////////

    private void do_rtj_debug() {
	BufferedReader d = 
	    new BufferedReader(new InputStreamReader(System.in));
	System.out.println("\nRTJ interactive method inspection\n");

	while(true) {
	    System.out.print("Method name:");

	    String method = null;
	    try {
		method = d.readLine();
	    } catch(IOException e) {
		System.err.println("Error reading from System.in " + e);
		e.printStackTrace();
		System.exit(1);
	    }

	    if(method == null) { // EOF received
		System.out.println();
		break;
	    }

	    rtj_inspect_method(method);
	}
    }


    private void rtj_inspect_method(String method) {
	int point_pos = method.lastIndexOf('.');
	String declClassName = 
	    (point_pos != -1) ?
	    method.substring(0, point_pos) :
	    // by default, same class as root method
	    mainM.getDeclaringClass().getName();
	String methodName    = method.substring(point_pos + 1);

	HClass hclass = null;
	try {
	    hclass = linker.forName(declClassName);
	} catch (NoSuchClassException e) {
	    System.err.println("Class " + declClassName + " not found!");
	    return;
	}

	HMethod[] hm  = hclass.getDeclaredMethods();
	HMethod hmethod = null;		
	for(int i = 0; i < hm.length; i++) {
	    if(!hm[i].getName().equals(methodName)) continue;

	    hmethod = hm[i];
	    MetaMethod mm = hm2mm(hmethod);

	    ParIntGraph ext_pig = pa.getExtParIntGraph(mm);
	    System.out.println("METHOD " + hmethod);
	    display_pointer_parameters(hmethod, pa);
	    System.out.print("EXT. GRAPH AT THE END OF THE METHOD:");
	    System.out.println(ext_pig);
	    
	    Set/*<PANode>*/ esc_nodes = ext_pig.allNodes();
	    Set/*<PANode>*/ esc_inside_nodes = new HashSet/*<PANode>*/();
	    
	    // if one of the elements of the set nodes is an INSIDE
	    // node, some objects are leaking out of the memory scope.
	    for(Object nodeO : esc_nodes) {
		PANode node = (PANode) nodeO;
		if((node.type == PANode.INSIDE) && not_exception(node))
		    esc_inside_nodes.add(node);
	    }
	    
	    if(esc_inside_nodes.isEmpty())
		System.out.println("\tnothing escapes!");
	    else
		display_escaping_nodes(esc_inside_nodes);
	}
    
	if(hmethod == null)
	    System.out.println(declClassName + "." + methodName +" not found");
    }

    
    private void display_escaping_nodes(Set/*<PANode>*/ nodes) {
	System.out.println("Escaping inside nodes:");
	for(Iterator/*<PANode>*/ it = nodes.iterator(); it.hasNext(); ) {
	    PANode node = (PANode) it.next();
	    System.out.println(" " + node);
	    System.out.println
		("  CREATED IN: " + 
		 Util.code2str
		 (pa.getNodeRepository().node2Code(node.getRoot())));
	}
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

    // Checks whether node is an exception node or not.
    private boolean not_exception(PANode node) {
	System.out.println("not_excp: " + node);

	HClass hclass = pa.getNodeRepository().getInsideNodeType(node);
	if(java_lang_Throwable == null)
	    java_lang_Throwable = linker.forName("java.lang.Throwable");
	return ! ( java_lang_Throwable.isSuperclassOf(hclass) ); 
    }
    private HClass java_lang_Throwable = null;

    /////////////////////// RTJ DEBUG STUFF ENDS /////////////////////////
    //////////////////////////////////////////////////////////////////////
    


    //////////////////////////////////////////////////////////////////////
    //////////////////// RTJ CHECK REMOVAL STARTS ////////////////////////

    private static boolean DEBUG_RT = true;

    private boolean can_remove_all_checks() {
	long start = time();
	boolean result = can_remove_all_checks2();
	System.out.println("RTJ: can_remove_all_checks ... " + 
			   (time() - start) + " ms");
	return result;
    }

    private boolean can_remove_all_checks2() {
	for(Object hmO : get_relevant_runs()) {
	    HMethod hm = (HMethod) hmO;
	    if(!nothing_escapes(hm))
		return false;
	}
	return true;
    }
    

    private static MetaMethod hm2mm(HMethod hm) {
	return new MetaMethod(hm, true);
    }

    // Returns a set that contains all the run methods of all
    // the Runnable classes that might be passed as arguments to
    // javax.realtime.CTMemory.enter
    private Set/*<HMethod>*/ get_relevant_runs() {
	Set/*<HMethod>*/ result = Collections.EMPTY_SET;

	if(RTJ_RI_POLICY == RTJ_RI_ALL_RUNS)
	    result = get_all_runs();
	else if(RTJ_RI_POLICY == RTJ_RI_ENTER)
	    result = get_entered_runs();
	else
	    assert false : "RTJ: Unknown run identification policy!";
	
	if(result.isEmpty())
	    System.out.println("RTJ: WARNING: no run() was found!");
	else if(DEBUG_RT)
	    Util.print_collection(result, "RTJ: run() methods", "RTJ: ");

	return result;
    }


    // Returns the set of all the run() methods of classes that implements
    // java.lang.Runnable
    private Set/*<HMethod>*/ get_all_runs() {
	Set/*<HMethod>*/ runs = new HashSet();
	for(Iterator/*<HClass>*/ it = 
		classHierarchy.instantiatedClasses().iterator();
	    it.hasNext(); ) {
	    HMethod run = extract_run((HClass) it.next());
	    if(run != null)
		runs.add(run);
	}
	return runs;
    }


    /////////////////// get_entered_runs START ////////////////////////

    // Returns the set of run() methods of Runnable objects that are
    // passed to the enter method of some subclass of MemoryArea.
    private Set/*<HMethod>*/ get_entered_runs() {
	Set/*<HMethod>*/ result = new HashSet();
	Set/*<HMethod>*/ enters = get_enter_methods();
	for(Iterator/*<HMethod>*/ it = enters.iterator(); it.hasNext(); ) {
	    HMethod enter = (HMethod) it.next();

	    MetaMethod[] callers = 
		pa.getMetaAllCallers().getCallers(hm2mm(enter));

	    for(int i = 0; i < callers.length; i++)
		result.addAll(get_entered_runs(callers[i].getHMethod(),enter));
	}
	return result;
    }


    // Returns the set of enter methods declared in subclasses of MemoryArea.
    private Set/*<HMethod>*/ get_enter_methods() {
	Set/*<HMethod>*/ enters = new HashSet/*<HMethod>*/();

	HClass javax_realtime_MemoryArea = 
	    linker.forName("javax.realtime.MemoryArea");
	Set/*<HClass>*/ children = 
	    classHierarchy.children(javax_realtime_MemoryArea);

	for(Iterator/*<HClass>*/ it = children.iterator(); it.hasNext(); ) {
	    HClass hclass = (HClass) it.next();
	    if(!classHierarchy.instantiatedClasses().contains(hclass))
		it.remove();
	}

	if(DEBUG_RT)
	    Util.print_collection
		(children, "RTJ: Subclasses of javax.realtime.MemoryArea",
		 "RTJ: ");

	for(Object hclassO : children) {
	    HClass hclass = (HClass) hclassO;
	    HMethod[] hms = hclass.getMethods();
	    for(int i = 0; i < hms.length; i++) {
		if(hms[i].getName().equals("enter") &&
		   (hms[i].getParameterTypes().length == 1))
		    enters.add(hms[i]);
	    }
	}

	if(DEBUG_RT)
	    Util.print_collection(enters, "RTJ: enter() methods", "RTJ: ");

	return enters;
    }


    // Goes over all the calls to method enter inside the body of hm
    // and collect all the run methods that enter will implicitly call
    // (enter is supposed to be a javax.realtime.MemoryArea.enter style
    // method)
    private Set/*<HMethod>*/ get_entered_runs(HMethod hm, HMethod enter) {
	Set/*<HMethod>*/ result = new HashSet/*<HMethod>*/();

	if(DEBUG_RT)
	    System.out.println("RTJ: get_interesting_runs(" + hm +
			       "," + enter + ") entered");

	Set/*<CALL>*/ calls = get_calls_to_enter(hm, enter);
	
	if(DEBUG_RT)
	    Util.print_collection(calls, "Interesting calls ", "RTJ: ");

	TypeInference ti =
	    new TypeInference(hm, hcf.convert(hm), get_ietemps(calls));

	for(Iterator/*<CALL>*/ it = calls.iterator(); it.hasNext(); ) {
	    CALL cs = (CALL) it.next();
	    ExactTemp et = new ExactTemp(cs, cs.params(1));
	    Set types = ti.getType(et);

	    if(DEBUG_RT)
		Util.print_collection(types, "Possible types for "+et,"RTJ: ");

	    for(Object hclassO : types) {
		HClass hclass = (HClass) hclassO;		
		Set children = new HashSet(classHierarchy.children(hclass));

		children.add(hclass);

		if(DEBUG_RT)
		    Util.print_collection(children, "Children for " + hclass,
					  "RTJ: ");

		for(Object childO : children) {
		    HClass child = (HClass) childO;
		    if(classHierarchy.instantiatedClasses().contains(child)) {
			HMethod run = extract_run(child);
			if(run != null)
			    result.add(run);
		    }
		}
	    }
	}
	return result;
    }


    // Returns the set of the CALLs to method "enter" inside the code of hm. 
    private Set/*<CALL>*/ get_calls_to_enter(HMethod hm, HMethod enter) {
	if(DEBUG_RT)
	    System.out.println("RTJ: get_interesting_calls(" +
			       hm + "," + enter + ") entered");

	Set/*<CALL>*/ calls = new HashSet/*<CALL>*/();
	HCode hcode = hcf.convert(hm);
	for(Iterator it = hcode.getElementsI(); it.hasNext(); ) {
	    Quad quad = (Quad) it.next();
	    if(quad instanceof CALL) {
		CALL cs = (CALL) quad;
		MetaMethod[] callees = 
		    pa.getMetaCallGraph().getCallees(hm2mm(hm), cs);
		for(int i = 0; i < callees.length; i++) {
		    if(callees[i].getHMethod().equals(enter)) {
			calls.add(cs);
			break;
		    }
		}
	    }
	}
	return calls;
    }


    // Given a class, verify that it's implementing java.lang.Runnable and
    // extract its run() method. Return null if the verification fails or
    // no run() method exists in hclass.
    private HMethod extract_run(HClass hclass) {
	if(!hclass.isInstanceOf(java_lang_Runnable))
	    return null;

	if(DEBUG_RT)
	    System.out.println("RTJ: extract_run(" + hclass + ") entered");

	HMethod[] hms = hclass.getMethods();
	for(int i = 0; i < hms.length; i++) {
	    if(hms[i].getName().equals("run") &&
	       (hms[i].getParameterTypes().length == 0)) {
		
		if(DEBUG_RT)
		    System.out.println("\t" + hms[i]);
		
		return hms[i];
	    }
	}

	return null;
    }


    // Returns a set that contains all the Temps that appear in the first
    // position (ie the this pointer) in one of the CALLs in calls.
    private Set/*<ExactTemp>*/ get_ietemps(Set/*<CALL>*/ calls) {
	Set/*<ExactTemp>*/ temps = new HashSet/*<ExactTemp>*/();
	for(Iterator/*<CALL>*/ it = calls.iterator(); it.hasNext(); ) {
	    CALL cs = (CALL) it.next();
	    temps.add(new ExactTemp(cs, cs.params(1)));
	}
	return temps;
    }

    /////////////////// get_entered_runs END  ////////////////////////


    private boolean nothing_escapes(HMethod hm) {
	// if(DEBUG_RT)
	    System.out.println("RTJ: nothing_escapes(" + hm + ") entered");

	ParIntGraph pig = null;

	if(RTJ_CR_POLICY == RTJ_CR_INTER_PROC)
	    pig = pa.getExtParIntGraph(hm2mm(hm));
	else if(RTJ_CR_POLICY == RTJ_CR_INTER_THREAD)
	    pig = pa.threadInteraction(hm2mm(hm));
	else {
	    System.out.println("Unknown RTJ_CR_POLICY !");
	    System.exit(1);
	}

	pig = (ParIntGraph) pig.clone();
	// we don't care about the exceptions; if an exception is thrown
	// out of the run method of a thread, the program is gonna stop with
	// an exception anyway.
	pig.G.excp.clear();

	//TODO: some of the native methods are not harmful:
	//   java.lang.Object.getClass()
	//   java.lang.Thread.isAlive() etc.
	// make sure we clean the graph a bit before looking at it
	// (there should be more info about this in MAInfo)
	pig.G.flushCaches();
	pig.G.e.removeMethodHoles
	    (harpoon.Analysis.PointerAnalysis.InterProcPA.
	     getUnharmfulMethods());

	if(DEBUG_RT)
	    System.out.println("pig = " + pig + "\n\n");

	Set/*<PANode>*/ nodes = pig.allNodes();
	for(Iterator/*<PANode>*/ it = nodes.iterator(); it.hasNext(); ) {
	    PANode node = (PANode) it.next();
	    if((node.type() == PANode.INSIDE) &&
	       !pig.G.captured(node)) {
		System.out.println
		    ("RTJ: " + node + " created at " + 
		     Util.code2str(pa.getNodeRepository().node2Code
				   (node.getRoot())) +
		     " escapes -> false");
		return false;
	    }
	}

	if(DEBUG_RT)
	    System.out.println("RTJ: Nothing escapes from " + hm + " !!!");
	return true;
    }

    //////////////////// RTJ CHECK REMOVAL ENDS //////////////////////////
    //////////////////////////////////////////////////////////////////////

    private static long time() {
	return System.currentTimeMillis();
    }
}
