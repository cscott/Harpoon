// ODPointerAnalysis.java, created Sat Jan  8 23:22:24 2000 by salcianu
// Copyright (C) 2000 Alexandru SALCIANU <salcianu@retezat.lcs.mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.PointerAnalysis;

import java.util.HashMap;
import java.util.Map;
import java.util.Iterator;
import java.util.Enumeration;
import java.util.Set;
import java.util.HashSet;
import java.util.Date;
import java.util.Collections;
import java.util.Arrays;
import java.util.Comparator;

import java.lang.reflect.Modifier;

import harpoon.ClassFile.HCodeFactory;
import harpoon.ClassFile.HCodeElement;
import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HMethod;
import harpoon.ClassFile.HField;
import harpoon.ClassFile.HCode;
import harpoon.ClassFile.Linker;
import harpoon.ClassFile.Loader;

import harpoon.Analysis.Quads.CallGraph;
import harpoon.Analysis.AllCallers;
import harpoon.Analysis.ClassHierarchy;
import harpoon.Util.LightBasicBlocks.LightBasicBlock;

import harpoon.Temp.Temp;

import harpoon.IR.Quads.Quad;
import harpoon.IR.Quads.QuadKind;
import harpoon.IR.Quads.QuadVisitor;
import harpoon.IR.Quads.HEADER;
import harpoon.IR.Quads.METHOD;
import harpoon.IR.Quads.AGET;
import harpoon.IR.Quads.ALENGTH;
import harpoon.IR.Quads.ANEW;
import harpoon.IR.Quads.ASET;
import harpoon.IR.Quads.GET;
import harpoon.IR.Quads.MOVE;
import harpoon.IR.Quads.NEW;
import harpoon.IR.Quads.RETURN;
import harpoon.IR.Quads.THROW;
import harpoon.IR.Quads.SET;
import harpoon.IR.Quads.CALL;
import harpoon.IR.Quads.MONITORENTER;
import harpoon.IR.Quads.MONITOREXIT;
import harpoon.IR.Quads.FOOTER;
import harpoon.IR.Quads.TYPECAST;

import harpoon.Analysis.MetaMethods.MetaMethod;
import harpoon.Analysis.MetaMethods.MetaCallGraph;
import harpoon.Analysis.MetaMethods.MetaAllCallers;
import harpoon.Analysis.MetaMethods.GenType;

import harpoon.Util.TypeInference.CachingArrayInfo;

import harpoon.Util.LightBasicBlocks.LBBConverter;
import harpoon.Util.LightBasicBlocks.CachingSCCLBBFactory;
import harpoon.Util.Graphs.SCComponent;
import harpoon.Util.Graphs.Navigator;
import harpoon.Util.Graphs.TopSortedCompDiGraph;
import harpoon.Util.UComp;

import harpoon.Util.Util;

import harpoon.Util.DataStructs.Relation;
import harpoon.Util.DataStructs.LightRelation;
import harpoon.Util.DataStructs.LightMap;




/**
 * <code>ODPointerAnalysis</code> is the main class of the Pointer Analysis
 package. It is designed to act as a <i>query-object</i>: after being
 initialized, it can be asked to provide the Parallel Interaction Graph
 valid at the end of a specific method.
 * 
 * @author  Alexandru SALCIANU <salcianu@retezat.lcs.mit.edu>
 * @version $Id: ODPointerAnalysis.java,v 1.10 2004-03-05 22:18:14 salcianu Exp $
 */
public class ODPointerAnalysis {
    public static final boolean DEBUG     = false;
    public static final boolean DEBUG2    = false;
    public static final boolean DEBUG_SCC = false;

    /** Turns on the save memory mode. In this mode, some of the speed is
	sacrified for the sake of the memory consumption. More specifically,
	the <i>Interior</i>, large version of the Parallel Interaction Graph
	at the end of a method is no longer cached. */
    public static final boolean SAVE_MEMORY = false;

    /** Makes the pointer analysis deterministic to make the debug easier.
	The main source of undeterminism in our code is the intensive use of
	<code>Set</code>s which doesn't offer any guarantee about the order
	in which they iterate over their elements. */
    public static final boolean DETERMINISTIC = true;

    /** Turns on the priniting of some timing info. */
    public static boolean TIMING = true;
    public static final boolean STATS = true;
    public static boolean SHOW_NODES = true;
    public static final boolean DETAILS2 = false;


    /** Hack to speed it up: it appears to me that the edge ordering
	relation is not extremely important: in recursive methods or in
	methods with loops, it tends to be just a cartesian product between
	I and O. */
    public static final boolean IGNORE_EO = true;

    /** Controls the recording of data about the thread nodes that are
	touched after being started. */
    public static final boolean TOUCHED_THREAD_SUPPORT= false;

    // TODO: Most of the following flags should be final (which will
    // help somehow the compiler to perform some dead code elimination
    // etc. and maybe gain some speed). For the moment, they are non-final
    // so that the main modeule can modify them (according to the command
    // line options).

    /** Activates the calling context sensitivity. When this flag is
	on, the nodes from the graph of the callee are specialized for each
	call site (up to <code>MAX_SPEC_DEPTH</code> times). This increases
	the precision of the analysis but requires more time and memory. */
    public static boolean CALL_CONTEXT_SENSITIVE = false;

    /** The specialization limit. This puts a limit to the otherwise
	exponential growth of the number of nodes in the analysis. */
    public static int MAX_SPEC_DEPTH = 1;

    /** Activates the full thread sensitivity. When this flag is on, 
	the analysis makes the distinction not only between the nodes
	allocated by the current thread and those allocated by all the
	others but also between the nodes allocated by threads with
	different run methods (for the time being, we cannot make the
	distinction between two threads with the same thread node). */
    public static boolean THREAD_SENSITIVE = false;

    /** Activates the weak thread sensitivity. When this flag is
	on, the precision of the interthread analysis is increased:
	the nodes from the graph of the run method of the thread whose
	interactions with the current thread are analyzed are specialized
	to differenciate between the nodes created by that thread and
	the nodes created by the current one. This increases
	the precision of the analysis but requires more time and memory. */
    public static boolean WEAKLY_THREAD_SENSITIVE = false;

    /** Activates the loop sensitivity. When this flag is on, the precision
	of the intra-method analysis is increased by making the difference
	between the last object allocated at a specific object creation
	site inside a loop and the objects allocated at the same object
	creation site but in the previous iterations. This enambles
	some strong optimizations but requires more time and memory. */
    public static boolean LOOP_SENSITIVE = false;

    /** Array elements are modeled as fields of the array object, all of them
	with the same name since the analysis is not able to make the
	distinction between the fields. 
	this name is supposed to be "as impossible as possible", so that we
	don't have any conflict with real fields. */
    public static final String ARRAY_CONTENT = "+ae+";

    // FV
    public static boolean BOUNDED_ANALYSIS_DEPTH = false;
    public static boolean ON_DEMAND_ANALYSIS = false;
    public static boolean ODA_precise = false;
    public static boolean NODES_DRIVEN = false;
    public static boolean FIRST_ANALYSIS = false;
    public static int	  MAX_ANALYSIS_DEPTH = 0;
    public static int     MAX_ANALYSIS_ABOVE_SPEC = 0;
    public static int	  current_analysis_depth = 0;
    public static int	  number_of_mapups = 0;
    public static int	  number_of_mm_analyzed = 0;

    public static Map Quad2Node = new LightMap();

    public static boolean MartinTheWildHacker=false;

    public static HashMap [] hash_proc_int_d = null; 
    public static HashMap [] hash_proc_ext_d = null; 

    public static Set interestingQuads = null;
    public static Set interestingNodes = null;
    public static Relation Quad2Nodes  = null;

    // The HCodeFactory providing the actual code of the analyzed methods
    private final MetaCallGraph  mcg;
    public final MetaCallGraph getMetaCallGraph() { return mcg; }
    private final MetaAllCallers mac;
    public final MetaAllCallers getMetaAllCallers() { return mac; }
    public final CachingSCCLBBFactory scc_lbb_factory;

    private Map hash_proc_interact_int = new HashMap();

    private Map hash_proc_interact_ext = new HashMap();



    // Maintains the partial points-to and escape information for the
    // analyzed methods. This info is successively refined by the fixed
    // point algorithm until no further change is possible
    // mapping MetaMethod -> ODParIntGraph.
    private Map hash_proc_int = new HashMap();

    // Maintains the external view of the parallel interaction graphs
    // attached with the analyzed methods. These "bricks" are used
    // for the processing of the CALL nodes.
    // Mapping MetaMethod -> ODParIntGraph.
    private Map hash_proc_ext = new HashMap();


    // Maintains the method holes information needed for a later
    // precision enhancement.
    // mapping MetaMethod -> HashSet of MethodHole.
//     public static Map hash_method_holes    = new HashMap();
//     public static Map hash_holes_history   = new HashMap();
//     public static Map hash_in_edge_always  = new HashMap();
//     public static Map hash_out_edge_always = new HashMap();
//     public static Map hash_out_edge_maybe  = new HashMap();
//     public static Map hash_locks = new HashMap();

    public static int mh_number = 0;

    public static MethodHole BottomHole = 
	new MethodHole(null,
		       new HashSet(),
		       new MetaMethod[0],
		       new Set[0],
		       new PANode(PANode.INSIDE),
		       new PANode(PANode.INSIDE),
		       -1789,-1);

    /** Creates a <code>ODPointerAnalysis</code>.
     *
     *  @param _mcg The (meta) Call Graph that models the caller-callee
     *  relation between methods.
     *  @param _mac The dual of <code>_mcg</code> (<i>ie</i> the
     *  callee-caller relation.
     *  @param lbbconv The producer of the (Light) Basic Block representation
     *  of a method body. 
     *
     *</ul> */
    public ODPointerAnalysis(MetaCallGraph _mcg, MetaAllCallers _mac,
			     LBBConverter lbbconv, Linker linker) {
	mcg  = _mcg;
	mac  = _mac;
	scc_lbb_factory = new CachingSCCLBBFactory(lbbconv);
	this.nodes = new NodeRepository(linker);
	if(SAVE_MEMORY)
	    aamm = new HashSet();
    }

    // the set of already analyzed meta-methods
    private Set aamm = null;

    /** Returns the full (internal) <code>ODParIntGraph</code> attached to
     * the method <code>hm</code> i.e. the graph at the end of the method.
     * Returns <code>null</code> if no such graph is available. */
    public ODParIntGraph getIntParIntGraph(MetaMethod mm){
	// FV Caution...
	if(BOUNDED_ANALYSIS_DEPTH==true) {
	    ODParIntGraph pig = 
		(ODParIntGraph) hash_proc_int_d[current_analysis_depth].get(mm);
	    if (pig==null){
		System.out.println("ERROR: getIntParIntGraph " + 
				   "called in bounded analysis " + 
				   "and return null pig");
		System.out.println("current_analysis_depth= " +
				   current_analysis_depth);
	    }
	    return pig;
	}
	else
	    if(SAVE_MEMORY){
		if(!aamm.contains(mm))
		    analyze(mm);
		analyze_intra_proc(mm);
		ODParIntGraph pig = (ODParIntGraph)hash_proc_int.get(mm);
		hash_proc_int.clear();
		return pig;
	    }
	    else{
		ODParIntGraph pig = (ODParIntGraph)hash_proc_int.get(mm);
		// FV test to be modified ? In fact, do we need any
		// bufferization at all by hash_proc_int ?
		if(pig == null){
		    analyze(mm);
		    pig = (ODParIntGraph)hash_proc_int.get(mm);
		}
		return pig;
	    }
    }

    public boolean isAnalyzed(MetaMethod mm){
	ODParIntGraph pig = (ODParIntGraph) hash_proc_int.get(mm);
	if (pig==null)
	    return false;
	else
	    return true;
    }


    public ODParIntGraph getIntParIntGraph(MetaMethod mm, boolean compute_it){
	ODParIntGraph pig = null;
	// FV Caution...
	if(BOUNDED_ANALYSIS_DEPTH==true) {
	    pig = (ODParIntGraph) hash_proc_int_d[current_analysis_depth].get(mm);
	    if ((pig==null)&&(compute_it)){
		analyze_intra_proc(mm);
		pig = (ODParIntGraph) hash_proc_int_d[current_analysis_depth].get(mm);
	    }
	    else{
		if (pig==null)
		    System.out.println("ERROR: getIntParIntGraph " + 
				       "called in bounded analysis " + 
				       "and return null pig");
	    }
	}
	else pig = (ODParIntGraph) hash_proc_int.get(mm);
	if((pig == null) && compute_it){
	    analyze(mm);
	    pig = (ODParIntGraph)hash_proc_int.get(mm);
	}
	return pig;
    }



    
    /** Returns the simplified (external) <code>ODParIntGraph</code> attached to
     * the method <code>hm</code> i.e. the graph at the end of the method.
     * of which only the parts reachable from the exterior (via parameters,
     * returned objects or static classes) have been preserved. The escape
     * function do not consider the parameters of the function (anyway, this
     * graph is supposed to be inlined into the graph of the caller, so the 
     * parameters will disappear anyway).
     * Returns <code>null</code> if no such graph is available. */
    public ODParIntGraph getExtParIntGraph(MetaMethod mm){
	return getExtParIntGraph(mm,true);
    }

    // internal method doing the job
    public ODParIntGraph getExtParIntGraph(MetaMethod mm, boolean compute_it){
	ODParIntGraph pig = null;
	// FV Caution...
	if(BOUNDED_ANALYSIS_DEPTH==true) {
	    pig = (ODParIntGraph) hash_proc_ext_d[current_analysis_depth].get(mm);
	    if ((pig==null)&&(compute_it)){
		analyze_intra_proc(mm);
		pig = (ODParIntGraph) hash_proc_ext_d[current_analysis_depth].get(mm);
	    }
	    else{
		if (pig==null)
		    System.out.println("ERROR: getExtParIntGraph " + 
				       "called in bounded analysis " + 
				       "and return null pig");
	    }
	}
	else pig = (ODParIntGraph) hash_proc_ext.get(mm);
	if((pig == null) && compute_it){
	    analyze(mm);
	    pig = (ODParIntGraph)hash_proc_ext.get(mm);
	}
	return pig;
    }

    // Returns a version of the external graph for meta-method hm that is
    // specialized for the call site q
    ODParIntGraph getSpecializedExtParIntGraph(MetaMethod mm, CALL q){
	// first, search in the cache
	Map map_mm = (Map) cs_specs.get(mm);
	if(map_mm == null){
	    map_mm = new HashMap();
	    cs_specs.put(mm,map_mm);
	}

	ODParIntGraph pig = null;
	if (!ODPointerAnalysis.ON_DEMAND_ANALYSIS)
	    pig = (ODParIntGraph) map_mm.get(q);
	if(pig != null){
// 	    if (pig.isCoherent())
// 		System.out.println("PIG coherent in mem of getSpecializedExtParIntGraph");
// 	    else{
// 		System.err.println("PIG incoherent in mem of getSpecializedExtParIntGraph");
// 		System.out.println("PIG incoherent in mem of getSpecializedExtParIntGraph");
// 		System.out.println(pig);
// 	    }
	    return pig;
	}

	// if the specialization was not already in the cache,
	// try to recover the original ParIntGraph (if it exists) and
	// specialize it

	ODParIntGraph original_pig = getExtParIntGraph(mm);
	if(original_pig == null) return null;

	
	ODParIntGraph new_pig = original_pig.csSpecialize(q);

// 	if (new_pig.isCoherent())
// 		System.out.println("PIG coherent in end of getSpecializedExtParIntGraph");
// 	    else{
// 		System.err.println("PIG incoherent in end of getSpecializedExtParIntGraph");
// 		System.out.println("PIG incoherent in end of getSpecializedExtParIntGraph");
// 		System.out.println(new_pig);
// 	    }

	if(!SAVE_MEMORY)
	    map_mm.put(q,new_pig);

	return new_pig;	
    }
    // cache for call site sensitivity: Map<MetaMethod, Map<CALL,ODParIntGraph>>
    private Map cs_specs = new HashMap();


    // Returns a specialized version of the external graph for meta-method mm;
    // mm is supposed to be the run method (the body) of a thread.
    ODParIntGraph getSpecializedExtParIntGraph(MetaMethod mm){

	ODParIntGraph pig = (ODParIntGraph) t_specs.get(mm);
	if(pig != null) return pig;

	// if the specialization was not already in the cache,
	// try to recover the original ODParIntGraph (if it exists) and
	// specialize it

	ODParIntGraph original_pig = getExtParIntGraph(mm);
	if(original_pig == null) return null;

	ODParIntGraph new_pig = null;
	if(THREAD_SENSITIVE)
	    new_pig = original_pig.tSpecialize(mm);
	else
	    if(WEAKLY_THREAD_SENSITIVE)
		new_pig = original_pig.wtSpecialize(mm);
	    else assert false : "The thread specialization is off!";

	t_specs.put(mm,new_pig);

	return new_pig;	
    }
    // cache for thread sensitivity: Map<MetaMethod, ODParIntGraph>
    private Map t_specs = new HashMap();


    /** Returns the parameter nodes of the method <code>hm</code>. This is
     * useful for the understanding of the <code>ODParIntGraph</code> attached
     * to <code>hm</code> */
    public PANode[] getParamNodes(MetaMethod mm){
	return nodes.getAllParams(mm);
    }

    /** Returns the parallel interaction graph for the end of the method 
	<code>hm</code>. The interactions between <code>hm</code> and the
	threads it (transitively) starts are analyzed in order to 
	&quot;recover&quot; some of the escaped nodes.<br>
	See Section 10 <i>Inter-thread Analysis</i> in the original paper of
	Martin and John Whaley for more details. */
    public ODParIntGraph threadInteraction(MetaMethod mm){
	System.out.println("threadInteraction for " + mm);
	ODParIntGraph pig = (ODParIntGraph) getIntParIntGraph(mm);
	// TBU
	//	return InterThreadPA.resolve_threads(this, pig);
	return null;
    }

    private ODParIntGraph threadIntInteraction(MetaMethod mm){
	System.out.println("threadIntInteraction for " + mm);
	ODParIntGraph pig = (ODParIntGraph) getIntParIntGraph(mm);

	//TBU	return InterThreadPA.resolve_threads(this, pig);
	return null;
    }

    private ODParIntGraph threadExtInteraction(MetaMethod mm){
	System.out.println("threadExtInteraction for " + mm);

	ODParIntGraph pig = (ODParIntGraph) getIntThreadInteraction(mm);
            PANode[] nodes = getParamNodes(mm);
            boolean is_main =
                mm.getHMethod().getName().equals("main");

            ODParIntGraph shrinked_graph = pig.keepTheEssential(nodes,is_main);

            // We try to correct some imprecisions in the analysis:
            //  1. if the meta-method is not returning an Object, clear
            // the return set of the shrinked_graph
            HMethod hm = mm.getHMethod();
            if(hm.getReturnType().isPrimitive())
                shrinked_graph.G.r.clear();
            //  2. if the meta-method doesn't throw any exception,
            // clear the exception set of the shrinked_graph.
            if(hm.getExceptionTypes().length == 0)
                shrinked_graph.G.excp.clear();
      return(shrinked_graph);
    }

    public ODParIntGraph getExtThreadInteraction(MetaMethod mm){
            ODParIntGraph pig = (ODParIntGraph)hash_proc_interact_ext.get(mm);
            if(pig == null){
                pig = threadExtInteraction(mm);
                hash_proc_interact_ext.put(mm, pig);
            }
            return pig;
    }

    public ODParIntGraph getIntThreadInteraction(MetaMethod mm){
            ODParIntGraph pig = (ODParIntGraph)hash_proc_interact_int.get(mm);
            if(pig == null){
                pig = threadIntInteraction(mm);
                hash_proc_interact_int.put(mm, pig);
            }
            return pig;
    }





    // Worklist of <code>MetaMethod</code>s for the inter-procedural analysis
    private PAWorkStack W_inter_proc = new PAWorkStack();

    // Worklist for the intra-procedural analysis; at any moment, it
    // contains only basic blocks from the same method.
    private PAWorkList  W_intra_proc = new PAWorkList();

    // Repository for node management.
    final NodeRepository nodes;
    final NodeRepository getNodeRepository() { return nodes; }


    // Navigator for the mmethod SCC building phase. The code is complicated
    // by the fact that we are interested only in yet unexplored methods
    // (i.e. whose parallel interaction graphs are not yet in the cache).
    private Navigator mm_navigator =
	new Navigator() {
		public Object[] next(Object node){
		    MetaMethod[] mms  = mcg.getCallees((MetaMethod)node);
		    MetaMethod[] mms2 = get_new_mmethods(mms);
		    
		    if(DETERMINISTIC)
			Arrays.sort(mms2, UComp.uc);
		    
		    return mms2;
		}
		
		public Object[] prev(Object node){
		    MetaMethod[] mms  = mac.getCallers((MetaMethod)node);
		    MetaMethod[] mms2 = get_new_mmethods(mms);
		    
		    if(DETERMINISTIC)
			Arrays.sort(mms2, UComp.uc);
		    return mms2;
		}
		
		// selects only the (yet) unanalyzed methods
		private MetaMethod[] get_new_mmethods(MetaMethod[] mms){
		    int count = 0;
		    boolean good[] = new boolean[mms.length];
		    for(int i = 0 ; i < mms.length ; i++)
			if(!hash_proc_ext.containsKey(mms[i])){
			    good[i] = true;
			    count++;
			}
			else
			    good[i] = false;
		    
		    MetaMethod[] new_mms = new MetaMethod[count];
		    int j = 0;
		    for(int i = 0 ; i < mms.length ; i++)
			if(good[i])
			    new_mms[j++] = mms[i];
		    return new_mms;
		}
	    };
    

    // Top-level procedure for the analysis. Receives the main method as
    // parameter. For the moment, it is not doing the inter-thread analysis
    private void analyze(MetaMethod mm){
	if(DEBUG)
	    System.out.println("ANALYZE: " + mm);

	long begin_time = TIMING ? System.currentTimeMillis() : 0;

	if(DEBUG)
	  System.out.println("Creating the strongly connected components " +
			     "of methods ...");

	// SCComponent.DETERMINISTIC = DETERMINISTIC;

	// the topologically sorted graph of strongly connected components
	// composed of mutually recursive methods (the edges model the
	// caller-callee interaction).
	TopSortedCompDiGraph<MetaMethod> mmethod_sccs = 
	    new TopSortedCompDiGraph(Collections.singleton(mm),
				     mm_navigator);

	if(DEBUG_SCC || TIMING)
	    Debug.display_mm_sccs(mmethod_sccs, mcg,
				  System.currentTimeMillis() - begin_time);

	for(SCComponent scc : mmethod_sccs.incrOrder()) {
	    analyze_inter_proc_scc(scc);

	    if(SAVE_MEMORY) {
		Object[] mms = scc.nodes();
		for(int i = 0; i < mms.length; i++)
		    aamm.add((MetaMethod) mms[i]);
	    }
	}

	if(TIMING)
	    System.out.println("analyze(" + mm + ") finished in " +
			  (System.currentTimeMillis() - begin_time) + "ms");
    }


    // information about the interesting AGET quads
    CachingArrayInfo cai = new CachingArrayInfo();

    // inter-procedural analysis of a group of mutually recursive methods
    private void analyze_inter_proc_scc(SCComponent scc){
	if(TIMING || DEBUG){
	    System.out.print("SCC" + scc.getId() + 
			     "\t (" + scc.size() + " meta-method(s)){");
	    for(Iterator it = scc.nodeSet().iterator(); it.hasNext(); )
		System.out.print("\n " + it.next());
	    System.out.print("} ... ");
	}

	long b_time = TIMING ? System.currentTimeMillis() : 0;

	MetaMethod mmethod = (MetaMethod) scc.nodes()[0];

	// if SCC composed of a native or abstract method, return immediately!
	if(!analyzable(mmethod.getHMethod())){
	    if(TIMING)
		System.out.println(System.currentTimeMillis() - b_time + "ms");
	    if(DEBUG)
		System.out.println(scc.toString() + " is unanalyzable");
	    return;
	}

	// Initially, the worklist (actually a workstack) contains only one
	// of the methods from the actual group of mutually recursive
	// methods. The others will be added later (because they are reachable
	// in the AllCaller graph from this initial node). 
	W_inter_proc.add(mmethod);

	boolean must_check = scc.isLoop();

	while(!W_inter_proc.isEmpty()){
	    // grab a method from the worklist
	    MetaMethod mm_work = (MetaMethod) W_inter_proc.remove();

	    ODParIntGraph old_info = (ODParIntGraph) hash_proc_ext.get(mm_work);

	    analyze_intra_proc(mm_work);
	    // clear the LightBasicBlock -> ODParIntGraph data generated by
	    // analyze_intra_proc
	    //lbb2pig.clear(); // annotation
	    LBBConverter lbbconv = scc_lbb_factory.getLBBConverter();
	    LightBasicBlock.Factory lbbf = 
		lbbconv.convert2lbb(mm_work.getHMethod());
	    clear_lbb2pig(lbbf);
	    //end annotation

	    ODParIntGraph new_info = (ODParIntGraph) hash_proc_ext.get(mm_work);

	    if(must_check && !new_info.equals(old_info)) { // new info?
		// since the original graph associated with hm_work changed,
		// the old specializations for it are no longer actual;
		if(CALL_CONTEXT_SENSITIVE)
		    cs_specs.remove(mm_work);

		Object[] mms = mac.getCallers(mm_work);    

		if(DETERMINISTIC){
		    Arrays.sort(mms,UComp.uc);
		}
		for(int i = 0; i < mms.length ; i++){
		    MetaMethod mm_caller = (MetaMethod) mms[i];
		    if(scc.contains(mm_caller))
			W_inter_proc.add(mm_caller);
		}
	    }
	}

	// enable some GC:
	// 1. get rid of the cache from the scc_lbb factory; anyway, it is
	// unsuseful, since the methods from scc are never reanalyzed. 
	// 1b. the info attached to the LBBs will be collected by the GCC too
	scc_lbb_factory.clear();
	// 2. the meta methods of this SCC will never be revisited, so the
	// specializations generated for the call sites inside them are not
	// usefull any more
	if(CALL_CONTEXT_SENSITIVE)
	    cs_specs.clear();
	// 3. clear the array info
	cai.clear();
	// 4. to save memory, the cache of "internal" graphs can be flushed
	if(SAVE_MEMORY){
	    System.out.println("hash_proc_int cleared!");
	    hash_proc_int.clear();
	}

	if(TIMING)
	    System.out.println((System.currentTimeMillis() - b_time) + "ms");
    }

    private MetaMethod current_intra_mmethod = null;
    private ODParIntGraph initial_pig = null;

    // the set of the AGETs from arrays of non-primitive types.
    private Set good_agets = null;

    // Performs the intra-procedural pointer analysis.
    //FV changed from private to public
    public void analyze_intra_proc(MetaMethod mm){
	//if(DEBUG)
	System.out.println("META METHOD: " + mm);
	long start_time = System.currentTimeMillis();

	ODPointerAnalysis.number_of_mm_analyzed++;

	LBBConverter lbbconv = scc_lbb_factory.getLBBConverter();
	LightBasicBlock.Factory lbbf = lbbconv.convert2lbb(mm.getHMethod());
	HCode hcode = lbbf.getHCode();
	good_agets = cai.getInterestingAGETs(mm.getHMethod(), hcode);

	current_intra_mmethod = mm;

	// cut the method into SCCs of basic blocks
	TopSortedCompDiGraph<LightBasicBlock> ts_lbbs = 
	    scc_lbb_factory.computeSCCLBB(mm.getHMethod());

	if(DEBUG2)
	    Debug.show_lbb_scc(mm, ts_lbbs);

	if(STATS) Stats.record_mmethod_pass(mm, ts_lbbs);

	// construct the ODParIntGraph at the beginning of the method 
	LightBasicBlock first_bb = 
	    (LightBasicBlock) ts_lbbs.decrOrder().get(0).nodes()[0];
	HEADER first_hce = (HEADER) first_bb.getElements()[0];
	METHOD m  = (METHOD) first_hce.next(1);
	initial_pig = get_mmethod_initial_pig(mm,m);
// 	if (ODPointerAnalysis.ON_DEMAND_ANALYSIS)
// 	    initialize_OD_fields(mm);

	
	// analyze the SCCs in decreasing topological order
	for(SCComponent scc : ts_lbbs.decrOrder())
	    analyze_intra_proc_scc(scc);

	good_agets = null;
	long end_time = System.currentTimeMillis();
	System.out.println("Analysis time= " + (end_time - start_time) + 
			   "ms for " + mm);
    }


//     private HashSet    mm_with_inside_node = new HashSet();

    public boolean create_inside_nodes(MetaMethod mm){
	//if(DEBUG)
	    System.out.println("META METHOD: " + mm);
// 	if(STATS) Stats.record_mmethod_pass(mm);

	LBBConverter lbbconv = scc_lbb_factory.getLBBConverter();
	LightBasicBlock.Factory lbbf = lbbconv.convert2lbb(mm.getHMethod());
	LightBasicBlock[] lbbs = lbbf.getAllBBs();
	Linker linker = mm.getHMethod().getDeclaringClass().getLinker();
	HClass excp_class = linker.forName("java.lang.Exception");

	for(int i=0; (i<lbbs.length); i++){
	    HCodeElement[] instrs = lbbs[i].getElements();
	    int len = instrs.length;
	    for(int j = 0; (j < len); j++){
		Quad q = (Quad) instrs[j];
		int qk = q.kind();
		if ((qk==QuadKind.ANEW)||(qk==QuadKind.NEW)){
		    GenType type = null;
		    if (qk==QuadKind.ANEW)
			type = new GenType(((ANEW) q).hclass(), GenType.MONO);
		    else
			type = new GenType(((NEW) q).hclass(), GenType.MONO);

		    if (!excp_class.isSuperclassOf(type.getHClass()))
			return true;
		}
	    }
	}
	return false;
    }

    public void make_thread_heap(MetaMethod mm, ODMAInfo mainfo){
	if(DEBUG)
	    System.out.println("META METHOD: " + mm);
// 	if(STATS) Stats.record_mmethod_pass(mm);

	LBBConverter lbbconv = scc_lbb_factory.getLBBConverter();
	LightBasicBlock.Factory lbbf = lbbconv.convert2lbb(mm.getHMethod());
	LightBasicBlock[] lbbs = lbbf.getAllBBs();
	Linker linker = mm.getHMethod().getDeclaringClass().getLinker();
	HClass thread_class = linker.forName("java.lang.Thread");

	for(int i=0; (i<lbbs.length); i++){
	    HCodeElement[] instrs = lbbs[i].getElements();
	    int len = instrs.length;
	    for(int j = 0; (j < len); j++){
		Quad q = (Quad) instrs[j];
		int qk = q.kind();
		if (qk==QuadKind.NEW){
		    GenType type = new GenType(((NEW) q).hclass(), GenType.MONO);
		    if (thread_class.isSuperclassOf(type.getHClass()))
			{
			    System.err.println("!!!! One thread node found !!! " + mm);
 			    MyAP ap = mainfo.getAPObj((NEW) q);
 			    ap.mh = true;
			}
		}
	    }
	}
    }

    public int count_creation_sites(MetaMethod mm){
	if(DEBUG)
	    System.out.println("META METHOD: " + mm);
	
	int count = 0;

	LBBConverter lbbconv = scc_lbb_factory.getLBBConverter();
	LightBasicBlock.Factory lbbf = lbbconv.convert2lbb(mm.getHMethod());
	LightBasicBlock[] lbbs = lbbf.getAllBBs();
	
	for(int i=0; (i<lbbs.length); i++){
	    HCodeElement[] instrs = lbbs[i].getElements();
	    int len = instrs.length;
	    for(int j = 0; (j < len); j++){
		Quad q = (Quad) instrs[j];
		int qk = q.kind();
		if ((qk==QuadKind.ANEW)||(qk==QuadKind.NEW))
		    count++;
	    }
	}
	return count;
    }


    // Intra-procedural analysis of a strongly connected component of
    // basic blocks.
    private void analyze_intra_proc_scc(SCComponent scc){

	if(DEBUG2)
	    System.out.println("\nSCC" + scc.getId());

	// add ALL the BBs from this SCC to the worklist.
	if(DETERMINISTIC) {
	    Object[] objs = Debug.sortedSet(scc.nodeSet());
	    for(int i = 0; i < objs.length; i++)
		W_intra_proc.add(objs[i]);
	}
	else
	    W_intra_proc.addAll(scc.nodeSet());

	boolean must_check = scc.isLoop();

	while(!W_intra_proc.isEmpty()){
	    // grab a Basic Block from the worklist
	    LightBasicBlock lbb_work = (LightBasicBlock) W_intra_proc.remove();

	    //ODParIntGraph old_info = (ODParIntGraph) lbb2pig.get(lbb_work);
	    // annotation
	    ODParIntGraphPair old_info = (ODParIntGraphPair) lbb_work.user_info;
	    analyze_basic_block(lbb_work);
	    ODParIntGraphPair new_info = (ODParIntGraphPair) lbb_work.user_info;

	    if(must_check && !ODParIntGraphPair.identical(old_info, new_info)){
		// yes! The succesors of the analyzed basic block
		// are potentially "interesting", so they should be added
		// to the intra-procedural worklist

		LightBasicBlock[] next_lbbs = lbb_work.getNextLBBs();
		int len = next_lbbs.length;
		for(int i = 0; i < len; i++){
		    LightBasicBlock lbb_next = next_lbbs[i];
		    if(scc.contains(lbb_next))
			W_intra_proc.add(lbb_next);
		}
	    }
	}

    }


    /** The Parallel Interaction Graph which is updated by the
     *  <code>analyze_basic_block</code>. This should normally be a
     *  local variable of that function but it must be also accessible
     *  to the <code>PAVisitor</code> class */
    private ODParIntGraph lbbpig = null;

    // The pair of ODParIntGraphs computed by the inter-procedural analysis
    // will be put here. 
    private ODParIntGraphPair call_pp = null;


//     public static Map  in_edge_always    = null;
//     public static Map  out_edge_always   = null;
//     public static Map  out_edge_maybe    = null;
//     public static Set  method_holes      = null;
//     public static Relation holes_history = null;
//     public static Relation locks         = null;


    /** QuadVisitor for the <code>analyze_basic_block</code> */
    private class PAVisitor extends QuadVisitor{

	/** Visit a general quad, other than those explicitly
	 processed by the other <code>visit</code> methods. */
	public void visit(Quad q){
	    // remove all the edges from the temps that are defined
	    // by this quad.
	    Temp[] defs = q.def();
	    for(int i = 0; i < defs.length; i++)
		lbbpig.G.I.removeEdges(defs[i]);
	    //System.out.println(lbbpig);
	}
		

	public void visit(HEADER q){
	    // do nothing
	    //System.out.println(lbbpig);
	}

	public void visit(METHOD q){
	    // do nothing
	    //System.out.println(lbbpig);
	}

	public void visit(TYPECAST q){
	    // do nothing
	    //System.out.println(lbbpig);
	}

	/** Copy statements **/
	public void visit(MOVE q){
	    lbbpig.G.I.removeEdges(q.dst());
	    Set set = lbbpig.G.I.pointedNodes(q.src());
	    //System.out.println("Move " + set);
	    lbbpig.G.I.addEdges(q.dst(),set);
	    // q.dst() is a variable, do nothing more for ODA.
	    //tbu
	    //System.out.println(lbbpig);
	}
	
	
	// LOAD STATEMENTS
	/** Load statement; normal case */
	public void visit(GET q){
	    Temp l2 = q.objectref();
	    HField hf = q.field();

	    //System.out.println("GET ");

	    // do not analyze loads from non-pointer fields
	    if(hf.getType().isPrimitive()) return;

	    if(l2 == null) {
		// special treatement of the static fields
		PANode static_node =
		    nodes.getStaticNode(hf.getDeclaringClass().getName());
		// l2 is a variable, do nothing more for ODA.
		lbbpig.G.e.addNodeHole(static_node, static_node);
		process_load(q, q.dst(), Collections.singleton(static_node),
			     hf.getName());
		return;
	    }
	    process_load(q, q.dst(), lbbpig.G.I.pointedNodes(l2),
			 hf.getName());
	    //System.out.println(lbbpig);
	}
	
	/** Load statement; special case - arrays. */
	public void visit(AGET q) {
	    if(DEBUG2){
		System.out.println("AGET: " + q);
		System.out.println("good_agets: " + good_agets);
	    }
	    //System.out.println("AGET " + q);


	    // AGET from an array of primitive objects (int, float, ...)
	    if(!good_agets.contains(q)){
		if(DEBUG)
		    System.out.println("NOT-PA RELEVANT AGET: " + 
				       q.getSourceFile() + ":" + 
				       q.getLineNumber() + " " + q);
		// not interesting for the pointer analysis
		lbbpig.G.I.removeEdges(q.dst());
		// q.dst() is a variable, do nothing more for ODA.
		return;
	    }
	    // All the elements of an array are collapsed in a single
	    // node, referenced through a conventional named field

	    process_load(q, q.dst(), lbbpig.G.I.pointedNodes(q.objectref()),
			 ARRAY_CONTENT);
	    //System.out.println(lbbpig);
	}
	
	/** Does the real processing of a load statement. */
	public void process_load(Quad q, Temp l1, Set loadFrom, String f) {
	    Set set_aux = loadFrom;
	    Set set_S   = lbbpig.G.I.pointedNodes(set_aux,f);
	    HashSet set_E = new HashSet();
	    

	    //System.out.println("PROCESS_LOAD ");

	    for (Object nodeO : set_aux){
		PANode node = (PANode) nodeO;
		// hasEscaped instead of escaped (there is no problem
		// with the nodes that *will* escape - the future cannot
		// affect us).
		if(lbbpig.G.e.hasEscaped(node))
		    set_E.add(node);
	    }
	    
	    lbbpig.G.I.removeEdges(l1);


	    // create/use the load node corresponding to reading the field
	    // f from node, forall node in set_E (the escaped nodes pointed to by l2)
	    if(!set_E.isEmpty()) {
		if (ODPointerAnalysis.MartinTheWildHacker){
		    // update the action repository
		    Set active_threads = lbbpig.tau.activeThreadSet();
		    PANode load_node = null;		
		    for(Object nodeO : set_E) {
			PANode node = (PANode) nodeO;
			Set a = lbbpig.G.O.pointedNodes(node, f);
			if(a.isEmpty()) {
			    if(load_node == null) {
				load_node = nodes.getCodeNode(q,PANode.LOAD);
				set_S.add(load_node);
			    }
			    lbbpig.G.O.addEdge(node, f, load_node);
			    if (ODPointerAnalysis.ON_DEMAND_ANALYSIS){
				lbbpig.odi.addOutsideEdges(node, f, load_node);
			    }
			    lbbpig.ar.add_ld(node, f, load_node,
					     ActionRepository.THIS_THREAD,
					     active_threads);
			}
			else {
			    PANode node2 = (PANode) a.iterator().next();
			    lbbpig.G.O.addEdge(node, f, node2);
			    if (ODPointerAnalysis.ON_DEMAND_ANALYSIS){
				lbbpig.odi.addOutsideEdges(node, f, node2);
			    }
			    set_S.add(node2);
			    lbbpig.ar.add_ld(node, f, node2,
					     ActionRepository.THIS_THREAD,
					     active_threads);
			}
		    }
		
		    if(!ODPointerAnalysis.IGNORE_EO)
			assert false : "Not implemented";
		    //lbbpig.eo.add(set_E, f, load_node, lbbpig.G.I);
		    
		    lbbpig.G.propagate(set_E);
		}
		else{
		    PANode load_node = nodes.getCodeNode(q,PANode.LOAD); 
		    set_S.add(load_node);
		    lbbpig.G.O.addEdges(set_E,f,load_node);

		    if (ODPointerAnalysis.ON_DEMAND_ANALYSIS){
			lbbpig.odi.addOutsideEdges(set_E, f, load_node);
		    }

		    if(!PointerAnalysis.IGNORE_EO)
			lbbpig.eo.add(set_E, f, load_node, lbbpig.G.I);

		    lbbpig.G.propagate(set_E);
		    
		    // update the action repository
		    Set active_threads = lbbpig.tau.activeThreadSet();
		    for (Object neO : set_E){
			PANode ne = (PANode) neO;
			lbbpig.ar.add_ld(ne, f, load_node,
					 ActionRepository.THIS_THREAD,
					 active_threads);
		    }
		}
	    }

	    lbbpig.G.I.addEdges(l1,set_S);
	    // l1 is a variable : do nothing more for ODA

	    if(TOUCHED_THREAD_SUPPORT) touch_threads(set_aux);
	}


	// OBJECT CREATION SITES
	/** Object creation sites; normal case */
	public void visit(NEW q){
	    //System.out.println("NEW " + q);	    
	    process_new(q,q.dst());
	    //System.out.println(lbbpig);
	}
	
	/** Object creation sites; special case - arrays */
	public void visit(ANEW q){
	    //System.out.println("ANEW " + q);	    
	    process_new(q,q.dst());
	    //System.out.println(lbbpig);
	}
	
	private void process_new(Quad q,Temp tmp){
// 	    System.out.println("PROCESS_NEW " + q);
	    // Kill_I = edges(I,l)
	    PANode node = nodes.getCodeNode(q,PANode.INSIDE);
	    if ((ODPointerAnalysis.ON_DEMAND_ANALYSIS)&&
		(ODPointerAnalysis.NODES_DRIVEN)){
// 		System.out.println("storing...");
		PANode n = (PANode) Quad2Node.get(q);
		if(n==null){
		    Quad2Node.put(q,node);
		}
		else if (n.equals(node)){
		    //everything is OK
		}
		else {
		    System.err.println("**ERROR** : Quad with at leat to nodes " +
				       q + " " + node + " " + n);
		}
// 		if(Quad2Node==null){
// 		    Quad2Node = new LightMap();
// 		    mm2quadsNnodes.put(mm,Quad2Node);
// 		}


//  		System.err.println("New with all flags set...");
//  		System.err.println("Quad " + q + "(" + q.getID() + ")");
// 		if(ODPointerAnalysis.interestingQuads.contains(q)){
// //  		    System.err.println("... IN !!!");
// 		    ODPointerAnalysis.interestingNodes.add(node);
// 		    ODPointerAnalysis.Quad2Nodes.add(q,node);
// 		}
// 		else{
// 		    ODPointerAnalysis.interestingNodes.add(node);
//  		    System.err.println("... notin :-(");
// 		    for(Iterator it_qq=ODPointerAnalysis.interestingQuads.iterator();
// 			it_qq.hasNext(); ){
// 			Quad qq = (Quad) it_qq.next();
// 			if (qq.equals(q)){
// 			    System.err.println("... EQUALITY !!!");
// 			}
// 			else{
// 			    System.err.println("nothing new");
// 			    System.err.println("Quad " + q + "(" + q.getID() + ")");
// 			    System.err.println("Quad " + qq + "(" + qq.getID() + ")");
// 			    if (q.getID()==qq.getID()){
// 				System.err.println(q.getFactory());
// 				System.err.println(qq.getFactory());
			    
// 			    if (q.hashCode()==qq.hashCode()) 
// 				System.err.println("hashCode()");
// 			    if (q.getFactory().equals(qq.getFactory())) 
// 				System.err.println("getFactory()()");
// 			    if (q.getSourceFile().equals(qq.getSourceFile())) 
// 				System.err.println("getSourceFile()");
// 			    else{
// 				System.err.println("getSourceFile= " + q.getSourceFile() + qq.getSourceFile());
// 			    }
// 			    if (q.getLineNumber()==qq.getLineNumber()) 
// 				System.err.println("getLineNumber()");
// // 			    if (q.()==qq.()) System.err.println("()");
// 			    }
// 			}
// 		}
// 		}
	    }
	    lbbpig.G.I.removeEdges(tmp);
	    // Gen_I = {<l,n>}
	    lbbpig.G.I.addEdge(tmp,node);
	    // tmp is a variable : do nothing more for ODA
	    //System.out.println(lbbpig);
	}
	
	
	/** Return statement: r' = I(l) */
	public void visit(RETURN q){
	    //System.out.println("RETURN " + q);	    
	    Temp tmp = q.retval();
	    // return without value; nothing to be done
	    if(tmp == null) return;
	    Set set = lbbpig.G.I.pointedNodes(tmp);
	    // TAKE CARE: r is assumed to be empty before!
	    lbbpig.G.r.addAll(set);
	    //System.out.println(lbbpig);
	}

	/** Return statement: r' = I(l) */
	public void visit(THROW q){
	    //System.out.println("THROW " + q);	    
	    Temp tmp = q.throwable();
	    Set set = lbbpig.G.I.pointedNodes(tmp);
	    // TAKE CARE: excp is assumed to be empty before!
	    lbbpig.G.excp.addAll(set);
	    //System.out.println(lbbpig);
	}
	
	
	// STORE STATEMENTS

	// [AS] changed the code to remove the infamous
	// ArtificialTempFactory.  process_store takes as arguments
	// the sets of source / destination nodes, instead of l1/l2.
	// This way, we can treat the static field assignment without
	// introducing a bogus artificial temp.
	/** Store statements; normal case */
	public void visit(SET q) {
	    //System.out.println("SET " + q);	    
	    Temp   l1 = q.objectref();
	    Temp   l2 = q.src();
	    HField hf = q.field();
	    // do not analyze stores into non-pointer fields
	    if(hf.getType().isPrimitive()) return;
	    // static field -> get the corresponding artificial node
	    if(l1 == null) {
		PANode static_node =
		    nodes.getStaticNode(hf.getDeclaringClass().getName());
		// l1 is a variable : do nothing more for ODA
		lbbpig.G.e.addNodeHole(static_node,static_node);

		process_store(Collections.singleton(static_node),
			      hf.getName(),
			      lbbpig.G.I.pointedNodes(l2));
		return;
	    }
	    process_store(lbbpig.G.I.pointedNodes(l1),
			  hf.getName(),
			  lbbpig.G.I.pointedNodes(l2));
	    //System.out.println(lbbpig);
	}
	
	/** Store statement; special case - array */
	public void visit(ASET q) {
	    //System.out.println("ASET " + q);	    
	    // All the elements of an array are collapsed in a single
	    // node
	    process_store(lbbpig.G.I.pointedNodes(q.objectref()),
			  ARRAY_CONTENT,
			  lbbpig.G.I.pointedNodes(q.src()));
	    //System.out.println(lbbpig);
	}
	
	/** Does the real processing of a store statement: edges from
            any node in set1 toward any node in set2, labeled with
            field f. */
	public void process_store(Set set1, String f, Set set2) {
	    lbbpig.G.I.addEdges(set1, f, set2);
	    if (ODPointerAnalysis.ON_DEMAND_ANALYSIS){
		//System.out.println("in_edge_always");
		lbbpig.odi.addInsideEdges(set1, f, set2);
	    }
	    lbbpig.G.propagate(set1);

	    if(TOUCHED_THREAD_SUPPORT) touch_threads(set1);
	    //System.out.println(lbbpig);
	}
	
	public void process_thread_start_site(CALL q){
	    if(DEBUG2)
		System.out.println("THREAD START SITE: " + 
				   q.getSourceFile() + ":" +
				   q.getLineNumber());

	    //System.out.println("PROCESS_THREAD_START_SITE " + q);	    

	    // the parallel interaction graph in the case no thread
	    // is started due to some error and an exception is returned
	    // back from the "start()"
	    ODParIntGraph lbbpig_no_thread = (ODParIntGraph) (lbbpig.clone());

	    Temp l = q.params(0);
	    Set set = lbbpig.G.I.pointedNodes(l);
	    lbbpig.tau.incAll(set);
	    
	    for (Object ntO : set){
		PANode nt = (PANode) ntO;
		lbbpig.G.e.addNodeHole(nt,nt);
		lbbpig.G.propagate(Collections.singleton(nt));
	    }

	    call_pp = new ODParIntGraphPair(lbbpig, lbbpig_no_thread);	    
	    //System.out.println(lbbpig);
	}

	public void visit(CALL q){
	    //System.out.println("CALL " + q);	    
	    // treat the thread start sites specially
	    if(thread_start_site(q)){
		process_thread_start_site(q);
		return;
	    }

	    call_pp = 
		ODInterProcPA.analyze_call(ODPointerAnalysis.this,
					   current_intra_mmethod,
					   q,       // the CALL site
					   lbbpig); // the graph before the call
	    //System.out.println(lbbpig);
	}

	// We are sure that the call site q corresponds to a thread start
	// site if only java.lang.Thread.start can be called there. (If
	// some other methods can be called, it is possible that some of
	// them do not start a thread.)
	private boolean thread_start_site(CALL q){
	    //System.out.println("THREAD_START_SITE " + q);	    
	    MetaMethod mms[] = mcg.getCallees(current_intra_mmethod,q);
	    if(mms.length!=1) return false;

	    HMethod hm = mms[0].getHMethod();
	    String name = hm.getName();
	    if((name==null) || !name.equals("start")) return false;

	    if(hm.isStatic()) return false;
	    HClass hclass = hm.getDeclaringClass();
	    return hclass.getName().equals("java.lang.Thread");
	}

	/** Process an acquire statement. */
	public void visit(MONITORENTER q){
	    //System.out.println("MONITORENTER " + q);	    
	    process_acquire_release(q, q.lock());
	    //System.out.println(lbbpig);
	}

	/** Process a release statement. */
	public void visit(MONITOREXIT q){
	    //System.out.println("MONITOREXIT " + q);	    
	    process_acquire_release(q, q.lock());
	    //System.out.println(lbbpig);
	}

	// Does the real processing for acquire/release statements.
	private void process_acquire_release(Quad q, Temp l){
	    //System.out.println("PROCESS_ACQUIRE_RELEASE " + q);	    
	    Set active_threads = lbbpig.tau.activeThreadSet();
	    for (Object nodeO : lbbpig.G.I.pointedNodes(l)){
		PANode node = (PANode) nodeO;
		PASync sync = new PASync(node,ActionRepository.THIS_THREAD, q);
		lbbpig.ar.add_sync(sync, active_threads);
		if (ODPointerAnalysis.ON_DEMAND_ANALYSIS)
		    lbbpig.odi.addLock(sync);
	    }
	    
	    if(TOUCHED_THREAD_SUPPORT)
		touch_threads(lbbpig.G.I.pointedNodes(l));
	    //System.out.println(lbbpig);
	}

	// Records the fact that the started thread from the set 
	// "set_touched_nodes" have been touched.
	private void touch_threads(Set set_touched_nodes){
	    //System.out.println("TOUCH_THREADS");	    
	    for(Object touched_nodeO : set_touched_nodes){
		PANode touched_node = (PANode) touched_nodeO;
		if((touched_node.type == PANode.INSIDE) &&
		   lbbpig.tau.isStarted(touched_node))
		    lbbpig.touch_thread(touched_node);
	    }
	    //System.out.println(lbbpig);
	}


	/** End of the currently analyzed method; store the graph
	    in the hash table. */
	public void visit(FOOTER q){
	    // The full graph is stored in the hash_proc_int hashtable;
	    HMethod hm = current_intra_mmethod.getHMethod();
	    System.out.println(" Footer: int: " + lbbpig);
	    if(ODPointerAnalysis.ON_DEMAND_ANALYSIS){
		if (lbbpig==null){
// 		    for(int i=0; i<MAX_ANALYSIS_DEPTH; i++){
		    for(int i=0; i<2; i++){
			hash_proc_int_d[i].put(current_intra_mmethod,null);
			hash_proc_ext_d[i].put(current_intra_mmethod,null);
		    }
		}
		else {
// 		    ODParIntGraph new_pig = (ODParIntGraph) lbbpig.clone();
		    ODParIntGraph new_pig = lbbpig;
		    Linker linker =  hm.getDeclaringClass().getLinker();
		    HClass excp_class = 
			linker.forName("java.lang.RuntimeException");
		    if(hm.getReturnType().isPrimitive()){
			new_pig.G.r.clear();
		    }
		    if(hm.getExceptionTypes().length == 0){
			HashSet faulty = new HashSet();
			for(Object nO : new_pig.G.excp){
			    PANode n = (PANode) nO;
			    GenType[] types = n.getPossibleClasses();
			    if ((types!=null)&&(types.length!=0)){
				boolean false_excp = true;
				for(int i=0; (i<types.length)&&(false_excp); i++){
				    if (excp_class.isSuperclassOf(types[i].getHClass()))
					false_excp = false;
				}
				if(false_excp){
// 				    System.err.println("Type length: " + types.length);
// 				    for(int i=0; (i<types.length)&&(false_excp); i++){
// 					System.err.println("Type : " + 
// 							   types[i].getHClass());
// 					System.err.println("Remove " + n);
// 				    }
				    faulty.add(n);
				}
			    }
			}
			new_pig.G.excp.removeAll(faulty);
		    }
// 		    for(int i=0; i<MAX_ANALYSIS_DEPTH; i++){
		    for(int i=0; i<2; i++){
			hash_proc_int_d[i].put(current_intra_mmethod,new_pig.clone());
			hash_proc_ext_d[i].put(current_intra_mmethod,new_pig.clone());
		    }
		}
	    }
	    hash_proc_int.put(current_intra_mmethod,lbbpig);

	    // To obtain the external view of the method, the graph must be
	    // shrinked to the really necessary parts: only the stuff
	    // that is accessible from the "root" nodes (i.e. the nodes
	    // that can be accessed by the rest of the program - e.g.
	    // the caller).
	    // The set of root nodes consists of the param and return
	    // nodes, and (for the non-"main" methods) the static nodes.
	    // TODO: think about the static nodes vs. "main"
	    PANode[] nodes = getParamNodes(current_intra_mmethod);
	    boolean is_main = 
		current_intra_mmethod.getHMethod().getName().equals("main");
	    ODParIntGraph shrinked_graph =lbbpig.keepTheEssential(nodes,is_main);

	    // We try to correct some imprecisions in the analysis:
	    //  1. if the meta-method is not returning an Object, clear
	    // the return set of the shrinked_graph
	    if(hm.getReturnType().isPrimitive())
		shrinked_graph.G.r.clear();
	    //  2. if the meta-method doesn't throw any exception,
	    // clear the exception set of the shrinked_graph.
	    if(hm.getExceptionTypes().length == 0)
		shrinked_graph.G.excp.clear();

	    // The external view of the graph is stored in the
	    // hash_proc_ext hashtable;
	    hash_proc_ext.put(current_intra_mmethod,shrinked_graph);
// 	    System.out.println(" Footer: ext: " + shrinked_graph);
// 	    if (ODPointerAnalysis.ON_DEMAND_ANALYSIS){
// 		ODParIntGraph new_pig = (ODParIntGraph) lbbpig.clone();
// 		if(hm.getReturnType().isPrimitive()){
// 		    new_pig.G.r.clear();
// 		}
// 		if(hm.getExceptionTypes().length == 0){
// 		    HashSet faulty = new HashSet();
// 		    for(Iterator x_it= new_pig.G.excp.iterator(); x_it.hasNext(); ){
// 			PANode n = (PANode) x_it.next();
// 			GenType[] types = n.getPossibleClasses();
// 			if ((types!=null)&&(types.length!=0)){
// 			    Linker linker = Loader.systemLinker;
// 			    HClass excp_class = 
// 				linker.forName("java.lang.RuntimeException");
// 			    boolean false_excp = true;
// 			    for(int i=0; (i<types.length)&&(false_excp); i++){
// 				if (excp_class.isSuperclassOf(types[i].getHClass()))
// 				    false_excp = false;
// 			    }
// 			    if(false_excp){
// 				System.err.println("Type length: " + types.length);
// 				for(int i=0; (i<types.length)&&(false_excp); i++){
// 				    System.err.println("Type : " + 
// 						       types[i].getHClass());
// 				System.err.println("Remove " + n);
// 				faulty.add(n);
// 			    }
// 			}
// 		    }
// 		    new_pig.G.excp.removeAll(faulty);
// 		}
// 		hash_proc_ext_d[current_analysis_depth].
// 		    put(current_intra_mmethod,new_pig);
// 	    }
	}
    }
    

    // Quad Visitor used by analayze_basic_block
    private PAVisitor pa_visitor = new PAVisitor();
    
    /** Analyzes a basic block - a Parallel Interaction Graph is computed at
     *  the beginning of the basic block, it is next updated by all the 
     *  instructions appearing in the basic block (in the order they appear
     *  in the original program). */
    private void analyze_basic_block(LightBasicBlock lbb){
	if(DEBUG2){
	    System.out.println("BEGIN: Analyze_basic_block " + lbb);
	    System.out.print("Prev BBs: ");
	    Object[] prev_lbbs = lbb.getPrevLBBs();
	    Arrays.sort(prev_lbbs, UComp.uc);
	    for(int i = 0 ; i < prev_lbbs.length ; i++)
		System.out.print((LightBasicBlock) prev_lbbs[i] + " ");
	    System.out.println();
	}

	// lbbpig is the graph at the *bb point; it will be 
	// updated till it becomes the graph at the bb* point
	lbbpig = get_initial_bb_pig(lbb);

	if(DEBUG2){
	    System.out.println("Before:");
	    System.out.println(lbbpig);
	}

	// go through all the instructions of this basic block
	HCodeElement[] instrs = lbb.getElements();
	int len = instrs.length;
	for(int i = 0; i < len; i++){
	    Quad q = (Quad) instrs[i];

	    if(DEBUG2)
		System.out.println("INSTR: " + q.getSourceFile() + ":" +
				   q.getLineNumber() + " " + q);
	    
	    // update the Parallel Interaction Graph according
	    // to the current instruction
	    q.accept(pa_visitor);
	}

	// if there was a pair, store the pair computed by the inter proc
	// module, otherwise, only the first element of the pair is essential.
	if(call_pp != null){
	    lbb.user_info = call_pp;
	    call_pp = null;
	}
	else
	    lbb.user_info = new ODParIntGraphPair(lbbpig, null);

	if(DEBUG2){
	    System.out.println("After:");
	    System.out.println(lbbpig);
	    System.out.print("Next BBs: ");
	    Object[] next_lbbs = lbb.getNextLBBs();
	    Arrays.sort(next_lbbs, UComp.uc);
	    for(int i = 0 ; i < next_lbbs.length ; i++)
		System.out.print((LightBasicBlock) next_lbbs[i] + " ");
	    System.out.println("\n");
	}
    }
    
    /** Returns the Parallel Interaction Graph at the point bb*
     *  The returned <code>ODParIntGraph</code> must not be modified 
     *  by the caller. This function is used by 
     *  <code>get_initial_bb_pig</code>. */
    private ODParIntGraph get_after_bb_pig(LightBasicBlock lbb,
					 LightBasicBlock lbb_son){

	ODParIntGraphPair pp = (ODParIntGraphPair) lbb.user_info;
	if(pp == null)
	    return ODParIntGraph.EMPTY_GRAPH;

	int k = 0;
	HCodeElement last_lbb = lbb.getLastElement();
	if((last_lbb != null) && (last_lbb instanceof CALL)){
	    CALL call = (CALL) last_lbb;
	    HCodeElement first_lbb_son = lbb_son.getFirstElement();
	    if(!call.next(0).equals(first_lbb_son)) k = 1;
	}

	//ODParIntGraph pig = (ODParIntGraph) lbb2pig.get(lbb);
	ODParIntGraph pig = pp.pig[k];
	return (pig==null)?ODParIntGraph.EMPTY_GRAPH:pig;
    }


    /** (Re)computes the Parallel Interaction Thread associated with
     *  the beginning of the <code>LightBasicBlock</code> <code>lbb</code>.
     *  This method is recomputing the stuff (instead of just grabbing it
     *  from the cache) because the information attached with some of
     *  the predecessors has changed (that's why <code>bb</code> is 
     *  reanalyzed) */
    private ODParIntGraph get_initial_bb_pig(LightBasicBlock lbb){
	LightBasicBlock[] prev = lbb.getPrevLBBs();
	int len = prev.length;
	if(len == 0){
	    // This case is treated specially, it's about the
	    // graph at the beginning of the current method.
	    ODParIntGraph pig = initial_pig;
	    return pig;
	}
	else{
	    // do the union of the <code>ODParIntGraph</code>s attached to
	    // all the predecessors of this basic block
// 	    System.out.println("\n\nFrom ");
// 	    System.out.println(get_after_bb_pig(prev[0], lbb));

	    ODParIntGraph pig = 
		(ODParIntGraph) (get_after_bb_pig(prev[0], lbb)).clone();

	    for(int i = 1; i < len; i++){
// 		System.out.println("Join (1) on ");
// 		System.out.println(pig);
// 		System.out.println("\n and \n");
// 		System.out.println(get_after_bb_pig(prev[i], lbb));
		pig.join(get_after_bb_pig(prev[i], lbb));
	    }
// 	    System.out.println("\n\n\nResult");
// 	    System.out.println(pig);
	    return pig;
	}
    }


    /** Computes the parallel interaction graph at the beginning of a 
     *  method; an almost empty graph except for the parameter nodes
     *  for the object formal parameter (i.e. primitive type parameters
     *  such as <code>int</code>, <code>float</code> do not have associated
     *  nodes */
    private ODParIntGraph get_mmethod_initial_pig(MetaMethod mm, METHOD m){
	Temp[]  params = m.params();
	HMethod     hm = mm.getHMethod();
	HClass[] types = hm.getParameterTypes();

	ODParIntGraph pig = new ODParIntGraph();

	// the following code is quite messy ... The problem is that I 
	// create param nodes only for the parameter with object types;
	// unfortunately, the types could be found only in HMethod (and
	// do not include the evetual this parameter for non-static nodes)
	// while the actual Temps associated with all the formal parameters
	// could be found only in METHOD. So, we have to coordinate
	// information from two different places and, even more, we have
	// to handle the missing this parameter (which is present in METHOD
	// but not in HMethod). 
	boolean isStatic = 
	    java.lang.reflect.Modifier.isStatic(hm.getModifiers());
	// if the method is non-static, the first parameter is not metioned
	// in HMethod - it's the implicit this parameter.
	int skew = isStatic?0:1;
	// number of object formal parameters = the number of param nodes
	int count = skew;
	for(int i = 0; i < types.length; i++)
	    if(!types[i].isPrimitive()) count++;
	
	nodes.addParamNodes(mm,count);
	Stats.record_mmethod_params(mm,count);

	// add all the edges of type <p,np> (i.e. parameter to 
	// parameter node) - just for the non-primitive types (e.g. int params
	// do not clutter our analysis)
	// the edges for the static fields will
	// be added later.
	count = 0;
	for(int i = 0; i < params.length; i++)
	    if((i<skew) || ((i>=skew) && !types[i-skew].isPrimitive())){
		PANode param_node = nodes.getParamNode(mm,count);
		pig.G.I.addEdge(params[i],param_node);
		//tbu ??
		// The param nodes are escaping through themselves */
		pig.G.e.addNodeHole(param_node,param_node);
		count++;
	    }

	return pig;
    }

    /** Check if <code>hm</code> can be analyzed by the pointer analysis. */
    public static final boolean analyzable(HMethod hm){
	int modifier = hm.getModifiers();
	return ! (
	    (java.lang.reflect.Modifier.isNative(modifier)) ||
	    (java.lang.reflect.Modifier.isAbstract(modifier))
	    );
    }


    // the set of harmful native methods
    private static Set hns = new HashSet();
    //    static{
    // here should be put some initializations for the hns set
    //}

    public final boolean harmful_native(HMethod hm){
	if(!Modifier.isNative(hm.getModifiers()))
	    return false;
	return hns.contains(hm);
    }

    /** Prints some statistics. */
    public final void  print_stats(){
	if(!STATS){
	    System.out.println("Statistics are deactivated.");
	    System.out.println("Turn on the ODPointerAnalysis.STATS flag!");
	    return;
	}

	Stats.print_stats();
	nodes.print_stats();
	System.out.println("==========================================");
	if(SHOW_NODES){
	    System.out.println("BASIC NODES");
	    System.out.println(nodes);
	    System.out.println("NODE SPECIALIZATIONS:");
	    nodes.show_specializations();
	}
    }

    /** Returns the parallel interaction graph valid at the program point
	right before <code>q</code>. <code>q</code> belongs to the light basic
	block <code>lbb</code> of the <code>current_intra_mmethod</code>.
	The analysis is re-executed from the beginning of <code>lbb</code>,
	till we reach <code>q</code> when we stop it and return the 
	parallel intercation graph valid at that moment. */
    private ODParIntGraph analyze_lbb_up_to_q(LightBasicBlock lbb, Quad q){
	lbbpig = get_initial_bb_pig(lbb);

	HCodeElement[] instrs = lbb.getElements();
	for(int i = 0; i < instrs.length; i++){
	    Quad q_curr = (Quad) instrs[i];
	    if(q_curr.equals(q)) return lbbpig;
	    q_curr.accept(pa_visitor);
	}
	assert false : q + " was not in " + lbb;
	return null; // this should never happen
    }
    

    /** Returns the parallel interaction graph attached to the program point
	right before <code>q</code> in the body of meta-method
	<code>mm</code>. */
    public final ODParIntGraph getPIGAtQuad(MetaMethod mm, Quad q){
	assert mcg.getAllMetaMethods().contains(mm) : "Uncalled/unknown meta-method!";
	LBBConverter lbbconv = scc_lbb_factory.getLBBConverter();
	LightBasicBlock.Factory lbbf = lbbconv.convert2lbb(mm.getHMethod());
	assert lbbf != null : "Fatal error";
	LightBasicBlock lbb = lbbf.getBlock(q);
	assert lbb != null : "No (Light)BasicBlock found for " + q;
	// as all the ODParIntGraph's for methods are computed, a simple
	// intra-procedural analysis of mm (with some caller-callee
	// interactions) is enough.
	analyze_intra_proc(mm);
	// now, we found the right basic block, we also have the results
	// for all the basic block of the concerned method; all we need
	// is to redo the pointer analysis for that basic block, stopping
	// when we meet q.
	ODParIntGraph retval = analyze_lbb_up_to_q(lbb, q);

	// clear the LightBasicBlock -> ODParIntGraph cache generated by
	// analyze_intra_proc
	//lbb2pig.clear();
	clear_lbb2pig(lbbf); // annotation;

	return retval;
    }

    // activates the GC
    private final void clear_lbb2pig(LightBasicBlock.Factory lbbf){
	LightBasicBlock[] lbbs = lbbf.getAllBBs();
	for(int i = 0; i < lbbs.length; i++)
	    lbbs[i].user_info = null;
    }

    /* OLD CODE
    // Clears all the info attached to LBBs belonging to the code of
    // methods from the strongly connected component scc.
    private final void clear_lbb_info(SCComponent scc){
	LBBConverter lbbconv = scc_lbb_factory.getLBBConverter();

	MetaMethod[] mms = (MetaMethod[]) scc.nodes();
	for(int i = 0; i < mms.length; i++)
	    clear_lbb2pig(lbbconv.convert2lbb(mms[i].getHMethod()));
    }
    */

    /** Returns the set of the nodes pointed by the temporary <code>t</code>
	at the point right before executing instruction <code>q</code>
	from the body of meta-method <code>mm</code>. */
    public final Set pointedNodes(MetaMethod mm, Quad q, Temp l){
	// 1. obtain the Parallel Interaction Graph attached to the point
	// right before the execution of q (noted .q in the article). 
	ODParIntGraph pig = getPIGAtQuad(mm,q);
	// 2. look at the nodes pointed by l; as from a temporary we can
	// have only inside edges, it is enough to look in the I set.
	return pig.G.I.pointedNodes(l);
    }


    /*
    ////////// SPECIAL HANDLING FOR SOME NATIVE METHODS ////////////////////

    final ODParIntGraph getExpODParIntGraph(MetaMethod mm){
	HMethod hm = mm.getHMethod();

	ODParIntGraph pig = (ODParIntGraph) hash_proc_ext.get(mm);
	if(pig == null){
	    pig = ext_pig_for_native(hm);
	    if(pig != null) hash_proc_ext.put(mm, pig);
	}

	return pig;
    }

    private final ODParIntGraph ext_pig_for_native(HMethod hm){
	HClass hclass = hm.getDeclaringClass();
	String clname = hclass.getName();
	String hmname = hm.getName();

	if(clname.equals("java.lang.Object") &&
	   hmname.equals("hashCode"));
	    
    }
    */
}


