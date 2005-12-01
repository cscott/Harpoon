// PointerAnalysis.java, created Sat Jan  8 23:22:24 2000 by salcianu
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
import java.util.Collection;
import java.util.List;
import java.util.LinkedList;

import java.lang.reflect.Modifier;

import harpoon.ClassFile.HCodeFactory;
import harpoon.ClassFile.HCodeElement;
import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HMethod;
import harpoon.ClassFile.HField;
import harpoon.ClassFile.HCode;
import harpoon.ClassFile.Linker;

import harpoon.Analysis.Quads.CallGraph;
import harpoon.Analysis.AllCallers;
import harpoon.Analysis.ClassHierarchy;
import harpoon.Util.LightBasicBlocks.LightBasicBlock;

import harpoon.Temp.Temp;

import harpoon.IR.Quads.Quad;
import harpoon.IR.Quads.QuadVisitor;
import harpoon.IR.Quads.HEADER;
import harpoon.IR.Quads.METHOD;
import harpoon.IR.Quads.AGET;
import harpoon.IR.Quads.ALENGTH;
import harpoon.IR.Quads.ANEW;
import harpoon.IR.Quads.CONST;
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

import harpoon.Util.LightBasicBlocks.LBBConverter;
import harpoon.Util.LightBasicBlocks.CachingSCCLBBFactory;
import jpaul.Graphs.SCComponent;
import jpaul.Graphs.Navigator;
import jpaul.Graphs.DiGraph;
import jpaul.Graphs.ForwardNavigator;
import jpaul.Graphs.TopSortedCompDiGraph;
import harpoon.Util.UComp;
import net.cscott.jutil.LinearSet;

import harpoon.Util.Util;

/**
 * <code>PointerAnalysis</code> is the main class of the Pointer Analysis
 package. It is designed to act as a <i>query-object</i>: after being
 initialized, it can be asked to provide the Parallel Interaction Graph
 valid at the end of a specific method.
 * 
 * @author  Alexandru SALCIANU <salcianu@retezat.lcs.mit.edu>
 * @version $Id: PointerAnalysis.java,v 1.24 2005-12-01 07:54:08 salcianu Exp $
 */
public class PointerAnalysis implements java.io.Serializable {
    public static final boolean DEBUG     = false;
    public static final boolean DEBUG2    = false;
    public static final boolean DEBUG_SCC = true;
    public static final boolean DEBUG_INTRA = false;

    /** crazy, isn't it? */
    public static boolean MEGA_DEBUG = false;
    public static boolean MEGA_DEBUG2 = false;
    private static boolean SHOW_INSTR = false;

    /** Turns on the recording of the actions done by the program. */
    public static boolean RECORD_ACTIONS = false;

    /** Hack to speed it up: it appears to me that the edge ordering
	relation is not extremely important: in recursive methods or in
	methods with loops, it tends to be just a cartesian product between
	I and O. */
    public static final boolean IGNORE_EO = true;


    /** Ignore what is load from a RETURN/EXCEPT node.  Anyway, we
        cannot determine what is load from there.  */
    public static boolean IGNORE_LOADS_FROM_NATIVES = true;
    
    /** If <code>false</code>, do not keep track of all un-analyzed
        methods where a node escapes.  Instead, just record the fact
        whether a node escapes (or not) in some un-analyzed method. */
    public static boolean CONDENSED_ESCAPE_INFO = true;

    /** If <code>true</code>, compress all load nodes that escape into
        an unanalyzed method and/or a static field into the single
        summar node NodeRepository.LOST_SUMMARY</code> (currently,
        this is done only at the end of a method, when producing the
        external version of the <code>ParIntGraph</code>).  This
        compression is applied to the external versions of the
        parallel interaction graphs, after the end of the fixed point
        computation for a SCC of methods. */
    public static boolean COMPRESS_LOST_NODES = true;

    public static boolean TOPLAS_PAPER = true;

    /** Same as <code>COMPRESS_LOST_NODES</code>, but done at the end
        of the analysis of each method. BREAKS MONOTONICITY! */
    public static boolean AGGRESSIVE_COMPRESS_LOST_NODES = true;
    
    /** If <code>true</code>, then each time we try to load something
	from an escaped node that already has a few load nodes (for
	the field we load), we reuse the smallest existent node,
	instead of generating a node for that LOAD instruction.
	BREAKS MONOTONICITY! */
    public static boolean REUSE_LOAD_NODES = true;

    /** Controls whether the analysis models the <code>null</code>
        references by edges to the special node <code>NULL</code>, or
        simply ignores them.  */
    public static boolean TREAT_NULL = false;

    /** Controls whether the analysis models references to constant
        Strings by edges to the special node <code>CONST</code>, or
        simply ignores them.  */
    public static boolean TREAT_CONST = false;

    /** If <code>true</code>, then we do not introduce edges about
        which we can infer that they violate the type declarations:
        e.g., an edge on the field &quot;foo&quot; from an inside node
        for <code>new Integer</code>. */
    public static boolean CONSIDER_TYPES = true;
    
    /** Turns on the save memory mode.  In this mode, some of the speed is
	sacrified for the sake of the memory consumption. More specifically,
	the <i>Interior</i>, large version of the Parallel Interaction Graph
	at the end of a method is no longer cached. */
    public static final boolean SAVE_MEMORY = false;

    /** Makes the pointer analysis deterministic to make the debug easier.
	The main source of undeterminism in our code is the intensive use of
	<code>Set</code>s which doesn't offer any guarantee about the order
	in which they iterate over their elements. */
    public static final boolean DETERMINISTIC = true;

    /** Turns on the printing of some timing info. */
    public static boolean TIMING = true;
    public static boolean FINE_TIMING = true;
    public static final boolean STATS = true;
    public static boolean SHOW_NODES = true;
    public static final boolean DETAILS2 = false;

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

    // Remove unreachable nodes
    public static final boolean DO_INTRA_PROC_TRIMMING = false;

    // meta call graph
    private MetaCallGraph  mcg;
    /** Returns the call graph graph used by <code>this</code>
	<code>PointerAnalysis</code> object. */
    public final MetaCallGraph getMetaCallGraph() { return mcg; }

    // meta all callers
    private MetaAllCallers mac;
    /** Returns the all callers graph used by <code>this</code>
	<code>PointerAnalysis</code> object. */
    public final MetaAllCallers getMetaAllCallers() { return mac; }

    // the factory that generates the SCC LBB representation (ie
    // strongly connected components of the light basic blocks of
    // quads) for the code of a method.
    private final CachingSCCLBBFactory scc_lbb_factory;
    /** Returns the SCC LBB factory used by <code>this</code>
	<code>PointerAnalysis</code> object. */
    public final CachingSCCLBBFactory getCachingSCCLBBFactory() {
	return scc_lbb_factory;
    }
    
    // linker
    public Linker linker;
    public final Linker getLinker() { return linker; }


    private Map hash_proc_interact_int = new HashMap();

    private Map hash_proc_interact_ext = new HashMap();

    // Maintains the partial points-to and escape information for the
    // analyzed methods. This info is successively refined by the fixed
    // point algorithm until no further change is possible
    // mapping MetaMethod -> ParIntGraph.
    private Map hash_proc_int = new HashMap();

    // Maintains the external view of the parallel interaction graphs
    // attached with the analyzed methods. These "bricks" are used
    // for the processing of the CALL nodes.
    // Mapping MetaMethod -> ParIntGraph.
    private Map hash_proc_ext = new HashMap();

    /** Creates a <code>PointerAnalysis</code> object.
     *
     *  @param mcg The (meta) Call Graph that models the caller-callee
     *  relation between methods.
     *  @param lbbconv The producer of the (Light) Basic Block representation
     *  of a method body. 
     *
     *</ul> */
    public PointerAnalysis(MetaCallGraph mcg,
			   CachingSCCLBBFactory caching_scc_lbb_factory,
			   Linker linker, ClassHierarchy ch) {
	this.mcg  = mcg;
	this.mac  = new MetaAllCallers(mcg);
	this.scc_lbb_factory = caching_scc_lbb_factory;
	// OLD STUFF: new CachingSCCLBBFactory(lbbconv);
	this.linker = linker;
	this.nodes = new NodeRepository(linker);

	PointerAnalysis.ch = ch;
	PointerAnalysis.java_lang_Object = linker.forName("java.lang.Object");
	
	InterProcPA.static_init(this);
    }

    static ClassHierarchy ch = null;
    static HClass java_lang_Object = null;

    // the set of already analyzed meta-methods
    private Set aamm = SAVE_MEMORY ? new HashSet() : null;

    /** Returns the full (internal) <code>ParIntGraph</code> attached
	to the method <code>hm</code> i.e. the graph at the end of the
	method.  If <code>mm</code> was not analyzed yet and
	<code>analyze</code> is true, analyze <code>mm</code> (and
	store the result of the analysis in the internal cache).  May
	return <code>null</code> if <code>mm</code> is unanalyzable,
	or if it has not been analyzed yet and <code>analyze</code> is
	false.  */
    public ParIntGraph getIntParIntGraph(MetaMethod mm, boolean analyze) {
	if(SAVE_MEMORY) {
	    if(!aamm.contains(mm)) {
		if(!analyze) return null;
		analyze(mm);
	    }
	    analyze_intra_proc(mm);
	    ParIntGraph pig = (ParIntGraph) hash_proc_int.get(mm);
	    hash_proc_int.clear();
	    return pig;
	}
	else {
	    ParIntGraph pig = (ParIntGraph) hash_proc_int.get(mm);
	    if((pig == null) && analyze) {
		analyze(mm);
		pig = (ParIntGraph) hash_proc_int.get(mm);
	    }
	    return pig;
	}
    }

    /** Equivalent to
        <code>getIntParIntGraph</code>(<code>mm</code>,<code>true</code>). */
    public ParIntGraph getIntParIntGraph(MetaMethod mm) {
	return getIntParIntGraph(mm, true);
    }

    /** That's what you probably want: equivalent to
        <code>getIntParIntGraph(new MetaMethod(hm, true))</code>. */
    public ParIntGraph getIntParIntGraph(HMethod hm) {
	return getIntParIntGraph(new MetaMethod(hm, true));
    }

    /** Returns the simplified (external) <code>ParIntGraph</code> attached to
     * the method <code>hm</code> i.e. the graph at the end of the method.
     * of which only the parts reachable from the exterior (via parameters,
     * returned objects or static classes) have been preserved. The escape
     * function do not consider the parameters of the function (anyway, this
     * graph is supposed to be inlined into the graph of the caller, so the 
     * parameters will disappear anyway).
     * Returns <code>null</code> if no such graph is available. */
    public ParIntGraph getExtParIntGraph(MetaMethod mm){
	return getExtParIntGraph(mm, true);
    }

    public ParIntGraph getExtParIntGraph(HMethod hm) {
	return getExtParIntGraph(new MetaMethod(hm, true));
    }

    // internal method doing the job
    ParIntGraph getExtParIntGraph(MetaMethod mm, boolean compute_it){
	ParIntGraph pig = (ParIntGraph) hash_proc_ext.get(mm);
	if((pig == null) && compute_it){
	    analyze(mm);
	    pig = (ParIntGraph)hash_proc_ext.get(mm);
	}
	return pig;
    }

    // Returns a version of the external graph for meta-method hm that is
    // specialized for the call site q
    ParIntGraph getSpecializedExtParIntGraph(MetaMethod mm, CALL q){
	// first, search in the cache
	Map map_mm = (Map) cs_specs.get(mm);
	if(map_mm == null){
	    map_mm = new HashMap();
	    cs_specs.put(mm,map_mm);
	}
	ParIntGraph pig = (ParIntGraph) map_mm.get(q);
	if(pig != null) return pig;

	// if the specialization was not already in the cache,
	// try to recover the original ParIntGraph (if it exists) and
	// specialize it

	ParIntGraph original_pig = getExtParIntGraph(mm);
	if(original_pig == null) return null;

	ParIntGraph new_pig = original_pig.csSpecialize(q);

	if(!SAVE_MEMORY)
	    map_mm.put(q, new_pig);

	return new_pig;	
    }
    // cache for call site sensitivity: Map<MetaMethod, Map<CALL,ParIntGraph>>
    private Map cs_specs = new HashMap();


    // Returns a specialized version of the external graph for meta-method mm;
    // mm is supposed to be the run method (the body) of a thread.
    ParIntGraph getSpecializedExtParIntGraph(MetaMethod mm){

	ParIntGraph pig = (ParIntGraph) t_specs.get(mm);
	if(pig != null) return pig;

	// if the specialization was not already in the cache,
	// try to recover the original ParIntGraph (if it exists) and
	// specialize it

	ParIntGraph original_pig = getExtParIntGraph(mm);
	if(original_pig == null) return null;

	ParIntGraph new_pig = null;
	if(THREAD_SENSITIVE)
	    new_pig = original_pig.tSpecialize(mm);
	else
	    if(WEAKLY_THREAD_SENSITIVE)
		new_pig = original_pig.wtSpecialize(mm);
	    else assert false : "The thread specialization is off!";

	t_specs.put(mm,new_pig);

	return new_pig;	
    }
    // cache for thread sensitivity: Map<MetaMethod, ParIntGraph>
    private Map t_specs = new HashMap();


    /** Returns the parameter nodes of the method <code>hm</code>. This is
     * useful for the understanding of the <code>ParIntGraph</code> attached
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
    public ParIntGraph threadInteraction(MetaMethod mm){
	System.out.println("threadInteraction for " + mm);
	ParIntGraph pig = (ParIntGraph) getIntParIntGraph(mm);
	if(pig == null)
	    return null;
	return InterThreadPA.resolve_threads(this, pig);
    }

    private ParIntGraph threadExtInteraction(MetaMethod mm){
	System.out.println("threadExtInteraction for " + mm);

	ParIntGraph pig = (ParIntGraph) getIntThreadInteraction(mm);
	boolean is_main = mm.getHMethod().getName().equals("main");
	ParIntGraph shrinked_graph = 
	    pig.keepTheEssential(getParamNodes(mm), is_main);
	
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

    public ParIntGraph getExtThreadInteraction(MetaMethod mm){
            ParIntGraph pig = (ParIntGraph)hash_proc_interact_ext.get(mm);
            if(pig == null){
                pig = threadExtInteraction(mm);
                hash_proc_interact_ext.put(mm, pig);
            }
            return pig;
    }

    public ParIntGraph getIntThreadInteraction(MetaMethod mm){
            ParIntGraph pig = (ParIntGraph) hash_proc_interact_int.get(mm);
            if(pig == null){
                pig = threadInteraction(mm);
                hash_proc_interact_int.put(mm, pig);
            }
            return pig;
    }

    // Worklist of <code>MetaMethod</code>s for the inter-procedural analysis
    private PAWorkList  W_inter_proc = new PAWorkList();

    // Worklist for the intra-procedural analysis; at any moment, it
    // contains only basic blocks from the same method.
    private PAWorkList  W_intra_proc = new PAWorkList();

    // Repository for node management.
    static NodeRepository nodes = null;
    public final NodeRepository getNodeRepository() { return nodes; }


    // Navigator for the mmethod SCC building phase. The code is complicated
    // by the fact that we are interested only in yet unexplored methods
    // (i.e. whose parallel interaction graphs are not yet in the cache).
    class MM_Navigator implements Navigator<MetaMethod>, java.io.Serializable {
	public List<MetaMethod> next(MetaMethod mm) {
	    MetaMethod[] mms  = mcg.getCallees(mm);
	    MetaMethod[] mms2 = get_new_mmethods(mms);
	    return Arrays.<MetaMethod>asList(mms2);
	}
	
	public List<MetaMethod> prev(MetaMethod mm) {
	    MetaMethod[] mms  = mac.getCallers(mm);
	    MetaMethod[] mms2 = get_new_mmethods(mms);
	    return Arrays.<MetaMethod>asList(mms2);
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

    private Navigator<MetaMethod> mm_navigator = new MM_Navigator();

    

    // Top-level procedure for the analysis. Receives the main method as
    // parameter.
    private void analyze(MetaMethod mm){
	//if(DEBUG)
	    System.out.println("ANALYZE: " + mm);

	long begin_time = TIMING ? System.currentTimeMillis() : 0;

	if(DEBUG)
	  System.out.println("Creating method SCCs");

	// SCComponent.DETERMINISTIC = DETERMINISTIC;

	// topologically sorted groups of mutually recursive methods
	// (the edges model the caller-callee interaction).
	TopSortedCompDiGraph<MetaMethod> ts_mmethods = 
	    new TopSortedCompDiGraph(DiGraph.<MetaMethod>diGraph
				     (Collections.singleton(mm),
				      mm_navigator));

	if(DEBUG_SCC || TIMING)
	    Debug.display_mm_sccs(ts_mmethods, mcg,
				  System.currentTimeMillis() - begin_time);

	for(SCComponent<MetaMethod> scc : ts_mmethods.incrOrder()) {
	    analyze_inter_proc_scc(scc);

	    if(SAVE_MEMORY) {
		for(Object mmethod : scc.vertices())
		    aamm.add((MetaMethod) mmethod);
	    }
	}

	if(TIMING)
	    System.out.println("analyze(" + mm + ") finished in " +
			  (System.currentTimeMillis() - begin_time) + "ms");
    }

    // inter-procedural analysis of a group of mutually recursive methods
    private void analyze_inter_proc_scc(SCComponent<MetaMethod> scc) {
	if(TIMING || DEBUG) {
	    System.out.print("SCC" + scc.getId() + 
			     "\t (" + scc.size() + " meta-method(s)){");
	    for(Object mm0 : scc.vertices())
		System.out.print("\n " + ((MetaMethod) mm0));
	    System.out.print("} ... ");
	}

	long b_time = TIMING ? System.currentTimeMillis() : 0;

	// if SCC composed of a native or abstract method, return immediately!
	if(!analyzable(((MetaMethod) scc.vertices().iterator().next())
		       .getHMethod())) {
	    if(TIMING)
		System.out.println((System.currentTimeMillis() - b_time) + 
				   "ms + (unanalyzable)");
	    return;
	}

	// add only the "exit" methods to the worklist (methods that
	// call methods from outside their SCC); the other methods
	// will be eventually added by the fixed point alg.
	if(scc.exitVertices().size() != 0) {
	    for(MetaMethod exit : scc.exitVertices())
		W_inter_proc.add(exit);
	}
	else // special case: leaf SCC -> add all
	    W_inter_proc.addAll(scc.vertices());

	while(!W_inter_proc.isEmpty()) {
	    // grab a method from the worklist
	    MetaMethod mm_work = (MetaMethod) W_inter_proc.remove();

	    //if(DEBUG_INTRA)
	    Debug.print_method_stats(mm_work, scc_lbb_factory);

	    ParIntGraph old_info = (ParIntGraph) hash_proc_ext.get(mm_work);
	    analyze_intra_proc(mm_work);
	    ParIntGraph new_info = (ParIntGraph) hash_proc_ext.get(mm_work);

	    if(scc.isLoop()) {
		if(REUSE_LOAD_NODES && (old_info != null)) {
		    new_info.join(old_info);
		    hash_proc_ext.put(mm_work, new_info);
		}

		if(!new_info.equals(old_info)) { // new info?

		    System.out.println("HAS CHANGED!");
		    
		    // since the original graph associated with
		    // hm_work changed, the old specializations for it
		    // are no longer actual;
		    if(CALL_CONTEXT_SENSITIVE)
			cs_specs.remove(mm_work);
		    
		    Object[] mms = mac.getCallers(mm_work);
		    if(DETERMINISTIC)
			Arrays.sort(mms, UComp.uc);
		    for(int i = 0; i < mms.length ; i++) {
			MetaMethod mm_caller = (MetaMethod) mms[i];
			if(scc.contains(mm_caller))
			    W_inter_proc.add(mm_caller);
		    }
		}
		else
		    System.out.println("HAS NOT CHANGED!");
	    }
	}

	if(COMPRESS_LOST_NODES && !AGGRESSIVE_COMPRESS_LOST_NODES)
	    post_compress(scc);

	// clear various data structures to save some memory space
	analyze_inter_proc_scc_clean(scc);

	if(TIMING)
	    System.out.println((System.currentTimeMillis() - b_time) + "ms");
    }


    private void post_compress(SCComponent scc) {
	for(Object mm0 : scc.vertices()) {
	    MetaMethod mm = (MetaMethod) mm0;

	    PANode[] nodes = getParamNodes(mm);
	    ParIntGraph pig = (ParIntGraph) hash_proc_ext.get(mm);

	    hash_proc_ext.put
		(mm, pig.compressLostNodes(getLostNodes(mm)));
	}
    }
   

    // clean work data structs used by analyze_inter_proc_scc
    private void analyze_inter_proc_scc_clean(SCComponent scc) {
	// 1. get rid of the cache from the scc_lbb factory; anyway,
	// it is useless, since scc's methods are never reanalyzed.
	scc_lbb_factory.clear();
	// 2. the meta methods of this SCC will never be revisited, so the
	// specializations generated for the call sites inside them are not
	// usefull any more
	if(CALL_CONTEXT_SENSITIVE)
	    cs_specs.clear();
	// 3. to save memory, the cache of "internal" graphs can be flushed
	// If necessary, these graphs can be reconstructed.
	if(SAVE_MEMORY) {
	    System.out.println("hash_proc_int cleared!");
	    hash_proc_int.clear();
	}
    }

    private MetaMethod current_intra_mmethod = null;
    private ParIntGraph initial_pig = null;

    private void analyze_intra_proc(MetaMethod mm) {
	_analyze_intra_proc(mm);
	// clear the LightBasicBlock -> ParIntGraph mapping
	lbb2pp.clear();
    }

    // Performs the intra-procedural pointer analysis of method mm.
    private void _analyze_intra_proc(MetaMethod mm) {
	long b_time = System.currentTimeMillis();
	if(FINE_TIMING) reset_fine_timing();

	current_intra_mmethod = mm;

	// cut the method into SCCs of basic blocks
	TopSortedCompDiGraph<LightBasicBlock> ts_sccs = 
	    scc_lbb_factory.computeSCCLBB(mm.getHMethod());

	if(DEBUG2 || MEGA_DEBUG)
	    Debug.show_lbb_scc(mm, ts_sccs);
	if(STATS)
	    Stats.record_mmethod_pass(mm, ts_sccs);

	// construct the ParIntGraph at the beginning of the method 
	initial_pig = get_mmethod_initial_pig(mm, ts_sccs);
	// propagate the information down the CFG
	for(SCComponent<LightBasicBlock> scc : ts_sccs.decrOrder())
	    analyze_intra_proc_scc(scc);

	if(FINE_TIMING) show_fine_timing(b_time);
    }


    Map/*<LightBasicBlock,ParIntGraphPair>*/ lbb2pp = new HashMap();
    private ParIntGraphPair getPIGPair(LightBasicBlock lbb) {
	return (ParIntGraphPair) lbb2pp.get(lbb);
    }
    private void putPIGPair(LightBasicBlock lbb, ParIntGraphPair pp) {
	lbb2pp.put(lbb, pp);
    }
    
    private static void reset_fine_timing() {
	InterProcPA.total_interproc_time = 0;
	InterProcPA.total_mapping_time   = 0;
	InterProcPA.total_merging_time   = 0;
	InterProcPA.total_propagate_time = 0;
	InterProcPA.total_cleaning_time  = 0;
	ParIntGraph.total_cloning_time   = 0;
	ParIntGraph.total_equals_time    = 0;
	ParIntGraph.total_join_time      = 0;
	intra_join = 0;
    }
    private static long intra_join = 0;
	
    private static void show_fine_timing(long b_time) {
	long delta = System.currentTimeMillis() - b_time;
	System.out.println
	    ("Analysis time: " + delta + " ms\n" + 
	     "(intra: "  + (delta - InterProcPA.total_interproc_time) +
	     " inter: "  + InterProcPA.total_interproc_time + 
	     " (map: "   + InterProcPA.total_mapping_time + 
	     " mrg: "    + InterProcPA.total_merging_time + 
	     " ppg: "    + InterProcPA.total_propagate_time + 
	     " cln: "    + InterProcPA.total_cleaning_time + ")" +
	     " clone: "  + ParIntGraph.total_cloning_time + 
	     " equals: " + ParIntGraph.total_equals_time + 
	     " join: "   + ParIntGraph.total_join_time + " / " + 
	     intra_join + ")\n");
    }


    // DEBUG
    private Map/*<LightBasicBlock,Integer>*/ lbb2passes = null;

    // Intra-procedural analysis of a strongly connected component of
    // basic blocks.
    private void analyze_intra_proc_scc(SCComponent<LightBasicBlock> scc){
	if(MEGA_DEBUG) {
	    lbb2passes = new HashMap();
	    for(Object lbb : scc.vertices())
		lbb2passes.put(lbb, new Integer(0));
	}
	if(DEBUG2 || MEGA_DEBUG)
	    System.out.println("\nSCC" + scc.getId());

	// add only the entry nodes to the worklist; the other basic
	// blocks will be eventually added too by the fixed point alg.
	if(scc.entryVertices().size() > 0) {
	    for(LightBasicBlock entry : scc.entryVertices())
		W_intra_proc.add(entry);
	}
	else // special case: top SCC; no entries -> add all nodes
	    W_intra_proc.addAll(scc.vertices());

	while(!W_intra_proc.isEmpty()) {
	    // grab a Basic Block from the worklist
	    LightBasicBlock lbb_work = (LightBasicBlock) W_intra_proc.remove();

	    ParIntGraphPair old_info = getPIGPair(lbb_work);
	    analyze_basic_block(lbb_work);
	    ParIntGraphPair new_info = getPIGPair(lbb_work);

	    // Some "optimizations" break the monotonicity of the
	    // transfer functions.  Make a "join" to restore it.
	    if(REUSE_LOAD_NODES || 
	       (COMPRESS_LOST_NODES && AGGRESSIVE_COMPRESS_LOST_NODES)) {
		long b_start = System.currentTimeMillis();
		if(new_info != null)
		    new_info.join(old_info);
		intra_join += System.currentTimeMillis() - b_start;
	    }

	    if(scc.isLoop()) {
		if(!ParIntGraphPair.identical(old_info, new_info)) {

		    if(MEGA_DEBUG)
			System.out.println("bb info changed!");

		    // yes! The succesors of the analyzed basic block
		    // are potentially "interesting", so they should be added
		    // to the intra-procedural worklist
		    for(LightBasicBlock lbb_next : lbb_work.getNextLBBs()) {
			if(scc.contains(lbb_next))
			    W_intra_proc.add(lbb_next);
		    }
		}
		else
		    if(MEGA_DEBUG)
			System.out.println("bb info has not changed!");
	    }
	}

	lbb2passes = null;
    }


    // The Parallel Interaction Graph which is updated by the
    // <code>analyze_basic_block</code>. This should normally be a
    // local variable of that function but it must be also accessible
    // to the <code>PAVisitor</code> class.
    private ParIntGraph lbbpig = null;

    // The pair of ParIntGraphs computed by the inter-procedural analysis
    // will be put here. 
    private ParIntGraphPair call_pp = null;

    /** QuadVisitor for the <code>analyze_basic_block</code> */
    private class PAVisitor extends QuadVisitor
	implements java.io.Serializable {

	/** Visit a general quad, other than those explicitly
	 processed by the other <code>visit</code> methods. */
	public void visit(Quad q){
	    // remove all the edges from the temps that are defined
	    // by this quad.
	    Temp[] defs = q.def();
	    for(int i = 0; i < defs.length; i++)
		lbbpig.G.I.removeEdges(defs[i]);
	}

	public void visit(HEADER q){
	    // do nothing
	}

	public void visit(METHOD q){
	    // do nothing
	}


	/** Copy statements **/
	public void visit(MOVE q){
	    lbbpig.G.I.removeEdges(q.dst());
	    Set set = lbbpig.G.I.pointedNodes(q.src());
	    lbbpig.G.I.addEdges(q.dst(),set);
	}
	
	
	// LOAD STATEMENTS
	/** Load statement; normal case */
	public void visit(GET q){
	    Temp l2 = q.objectref();
	    HField hf = q.field();

	    // do not analyze loads from non-pointer fields
	    if(hf.getType().isPrimitive()) return;

	    if(l2 != null)
		process_load(q, q.dst(), l2, getFieldName(hf));
	    else
		process_static_load(q);
	}
	

	private void process_static_load(GET q) {
	    HField hf = q.field();
	    String f = getFieldName(hf);
	    Temp l = q.dst();

	    if(TOPLAS_PAPER) {
		lbbpig.G.I.removeEdges(l);
		lbbpig.G.I.addEdge(l, NodeRepository.LOST_SUMMARY);
		return;
	    }

	    PANode static_node =
		nodes.getStaticNode(hf.getDeclaringClass().getName());

	    PANode target = 
		COMPRESS_LOST_NODES ? 
		NodeRepository.LOST_SUMMARY :
		nodes.getCodeNode(q, PANode.LOAD); 
		
	    // set local variable state
	    lbbpig.G.I.removeEdges(l);
	    lbbpig.G.I.addEdge(l, target);

	    // add outside edge
	    lbbpig.G.O.addEdge(static_node, f, target);
	    record_load_edge(static_node, f, target);

	    // update escape info: set and propagate
	    lbbpig.G.e.addNodeHole(static_node, static_node);
	    lbbpig.G.propagate(Collections.singleton(static_node));
	}

	
	/** Load statement; special case - arrays. */
	public void visit(AGET q) {
	    // AGET from an array of primitive objects (int, float, ...)
	    if(q.type().isPrimitive()) {
		if(DEBUG2)
		    System.out.println("NOT-PA RELEVANT AGET: " + 
				       q.getSourceFile() + ":" + 
				       q.getLineNumber() + " " + q);
		// not interesting for the pointer analysis
		lbbpig.G.I.removeEdges(q.dst());
		return;
	    }

	    // All the elements of an array are collapsed in a single
	    // node, referenced through a conventionally named field
	    process_load(q, q.dst(), q.objectref(), ARRAY_CONTENT);
	}

	/** Does the real processing of a load statement. */
	public void process_load(Quad q, Temp l1, Temp l2, String f) {
	    Set set_aux = lbbpig.G.I.pointedNodes(l2);
	    if(CONSIDER_TYPES)
		set_aux = selectNodesWithField(set_aux, f);

	    lbbpig.G.I.removeEdges(l1);
	    
	    // set_S will contain the nodes that l1 will point to after q
	    Set set_S = new LinearSet(lbbpig.G.I.pointedNodes(set_aux, f));
	    // set_E will contain all the nodes that escape and don't
	    // already have a load point on the f link; we need to
	    // create a new load node iff this set is non-empty
	    Set set_E = new LinearSet();

	    for(Object nodeO : set_aux) {
		PANode node = (PANode) nodeO;

		if((IGNORE_LOADS_FROM_NATIVES &&
		    ((node.type == PANode.EXCEPT) ||
		     (node.type == PANode.RETURN))) ||
		   // nodes loaded from lost nodes are lost too
		   (COMPRESS_LOST_NODES && 
		    (node == NodeRepository.LOST_SUMMARY))) {
		    lbbpig.G.O.addEdge(node, f, NodeRepository.LOST_SUMMARY);
		    set_S.add(NodeRepository.LOST_SUMMARY);
		    continue;
		}

		if(lbbpig.G.e.hasEscaped(node)) {
		    if(REUSE_LOAD_NODES) {
			Set pointed = lbbpig.G.O.pointedNodes(node, f);
			if(pointed.isEmpty())
			    set_E.add(node);
			else {
			    PANode node2 = get_min_node(pointed);
			    set_S.add(node2);
			    record_load_edge(node, f, node2);
			}
		    }
		    else // always use the load node for this load instruction
			set_E.add(node);
		}
	    }

	    if(!set_E.isEmpty()) {
		PANode load_node = nodes.getCodeNode(q, PANode.LOAD);
		set_S.add(load_node);
		lbbpig.G.O.addEdges(set_E, f, load_node);
	    }

	    lbbpig.G.I.addEdges(l1, set_S);
	    
	    if(!set_E.isEmpty())
		lbbpig.G.propagate(set_E);
	}

	// records the load action and its ordering (if necessary)
	private void record_load_edge(PANode node, String f, PANode node2) {
	    if(RECORD_ACTIONS)
		lbbpig.ar.add_ld(node, f, node2,
				 ActionRepository.THIS_THREAD,
				 lbbpig.tau.activeThreadSet());
	    if(!IGNORE_EO)
		lbbpig.eo.add(Collections.singleton(node),
			      f, node2, lbbpig.G.I);   
	}

	// Given a set of PANodes, returns the node having the minimal
	// number ID; the set of nodes is guaranteed not to be empty.
	private PANode get_min_node(Set nodes) {
	    Iterator it = nodes.iterator();
	    PANode retval = (PANode) it.next();
	    int min_id = retval.number; 
	    while(it.hasNext()) {
		PANode node = (PANode) it.next();
		if(node.number < min_id) {
		    min_id = node.number;
		    retval = node;
		}
	    }
	    return retval;
	}


	// OBJECT CREATION SITES
	/** Object creation sites; normal case */
	public void visit(NEW q) {
	    PANode node = process_new(q, q.dst());
	    if(TREAT_NULL) {
		HField[] fields = q.hclass().getFields();
		for(int i = 0; i < fields.length; i++)
		    if(!fields[i].isStatic())
			lbbpig.G.I.addEdge(node, getFieldName(fields[i]),
					   NodeRepository.NULL_NODE);
	    }
	}
	
	/** Object creation sites; special case - arrays */
	public void visit(ANEW q) {
	    PANode node = process_new(q, q.dst());
	    if(TREAT_NULL)
		lbbpig.G.I.addEdge(node, ARRAY_CONTENT,
				   NodeRepository.NULL_NODE);
	}
	
	private PANode process_new(Quad q, Temp tmp) {
	    // Kill_I = edges(I,l)
	    PANode node = nodes.getCodeNode(q,PANode.INSIDE);
	    lbbpig.G.I.removeEdges(tmp);
	    // Gen_I = {<l,n>}
	    lbbpig.G.I.addEdge(tmp,node);
	    return node;
	}
	
	public void visit(CONST q) {
	    // remove previous edges from q.dst()
	    lbbpig.G.I.removeEdges(q.dst());
	    if(TREAT_CONST) {
		// if we load a reference to a constant object, make
		// q.dst() to point to the corresponding node
		if(!q.type().isPrimitive())
		    lbbpig.G.I.addEdge(q.dst(), nodes.getConstNode(q.value()));
	    }
	}

	public void visit(TYPECAST q) {
	    Temp l = q.objectref();
	    Set set = lbbpig.G.I.pointedNodes(l);
	    lbbpig.G.I.removeEdges(l);
	    lbbpig.G.I.addEdges(l, typeFilter(set, q.hclass()));
	}


	/** Return statement: r' = I(l) */
	public void visit(RETURN q){
	    Temp tmp = q.retval();
	    // return without value; nothing to be done
	    if(tmp == null) return;
	    Set set = lbbpig.G.I.pointedNodes(tmp);
	    // TAKE CARE: r is assumed to be empty before!
	    lbbpig.G.r.addAll(set);
	}

	/** Return statement: r' = I(l) */
	public void visit(THROW q){
	    Temp tmp = q.throwable();
	    Set set = lbbpig.G.I.pointedNodes(tmp);
	    // TAKE CARE: excp is assumed to be empty before!
	    lbbpig.G.excp.addAll(set);
	}
	
	
	// STORE STATEMENTS
	/** Store statements; normal case */
	public void visit(SET q){
	    Temp l1 = q.objectref();
	    Temp l2 = q.src();  
	    HField hf = q.field();

	    String f = getFieldName(hf);

	    // do not analyze stores into non-pointer fields
	    if(hf.getType().isPrimitive()) return;

	    if(l1 == null) // special treatement of the static fields
		process_static_store(hf, l2);
	    else
		process_store(l1, f, q.src());
	}
	

	private void process_static_store(HField hf, Temp l2) {
	    if(TOPLAS_PAPER) {
		for(Object nodeO : lbbpig.G.I.pointedNodes(l2)) {
		    PANode node = (PANode) nodeO;
		    lbbpig.G.e.addNodeHole(node,
					   NodeRepository.LOST_SUMMARY);
		}
		lbbpig.G.propagate(lbbpig.G.I.pointedNodes(l2));
		return;
	    }
	    
	    // get the corresponding artificial node
	    PANode static_node =
		nodes.getStaticNode(hf.getDeclaringClass().getName());
	    lbbpig.G.I.addEdges(static_node, getFieldName(hf),
				lbbpig.G.I.pointedNodes(l2));
	    lbbpig.G.e.addNodeHole(static_node, static_node);
	    lbbpig.G.propagate(Collections.singleton(static_node));
	}


	/** Store statement; special case - array */
	public void visit(ASET q){
	    // ignore the ASETs on arrays of primitives
	    if(!q.type().isPrimitive())
		// All array elements are collapsed in a single node	
		process_store(q.objectref(), ARRAY_CONTENT, q.src());
	}
	
	/** Does the real processing of a store statement */
	public void process_store(Temp l1, String f, Temp l2){
	    Set set1 = lbbpig.G.I.pointedNodes(l1);
	    if(CONSIDER_TYPES)
		set1 = selectNodesWithField(set1, f);
	    Set set2 = lbbpig.G.I.pointedNodes(l2);
		
	    lbbpig.G.I.addEdges(set1, f, set2);
	    lbbpig.G.propagate(set1);
	}
	
	public void process_thread_start_site(CALL q){
	    if(DEBUG2)
		System.out.println("THREAD START SITE: " + 
				   q.getSourceFile() + ":" +
				   q.getLineNumber());

	    // the parallel interaction graph in the case no thread
	    // is started due to some error and an exception is returned
	    // back from the "start()"
	    ParIntGraph lbbpig_no_thread = (ParIntGraph) (lbbpig.clone());

	    Temp l = q.params(0);
	    Set set = lbbpig.G.I.pointedNodes(l);
	    lbbpig.tau.incAll(set);
	    
	    for (Object ntO : set){
		PANode nt = (PANode) ntO;
		lbbpig.G.e.addNodeHole(nt,nt);
		lbbpig.G.propagate(Collections.singleton(nt));
	    }

	    call_pp = new ParIntGraphPair(lbbpig, lbbpig_no_thread);	    
	}

	public void visit(CALL q){
	    // System.out.println("CALL " + Util.code2str(q));

	    // treat the thread start sites specially
	    if(thread_start_site(q)){
		process_thread_start_site(q);
		return;
	    }

	    call_pp = 
		InterProcPA.analyze_call(PointerAnalysis.this,
					 current_intra_mmethod,
					 q,       // the CALL site
					 lbbpig); // the graph before the call
	}

	// We are sure that the call site q corresponds to a thread start
	// site if only java.lang.Thread.start can be called there. (If
	// some other methods can be called, it is possible that some of
	// them do not start a thread.)
	private boolean thread_start_site(CALL q) {
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
	public void visit(MONITORENTER q) {
	    if(RECORD_ACTIONS)
		process_acquire_release(q, q.lock());
	}

	/** Process a release statement. */
	public void visit(MONITOREXIT q) {
	    if(RECORD_ACTIONS)
		process_acquire_release(q, q.lock());
	}

	// Does the real processing for acquire/release statements.
	private void process_acquire_release(Quad q, Temp l){
	    Set active_threads = lbbpig.tau.activeThreadSet();
	    for (Object nodeO : lbbpig.G.I.pointedNodes(l)) {
		PANode node = (PANode) nodeO;
		PASync sync = new PASync(node,ActionRepository.THIS_THREAD, q);
		lbbpig.ar.add_sync(sync, active_threads);
	    }
	}

	/** End of the currently analyzed method; store the graph
	    in the hash table. */
	public void visit(FOOTER q) {
	    // The full graph is stored in the hash_proc_int hashtable;
	    hash_proc_int.put(current_intra_mmethod,lbbpig);
	    // To obtain the external view of the method, the graph must be
	    // shrinked to the really necessary parts: only the stuff
	    // that is accessible from the "root" nodes (i.e. the nodes
	    // that can be accessed by the rest of the program - e.g.
	    // the caller).
	    if(MEGA_DEBUG)
		System.out.println("Unshrinked graph: " + lbbpig);

	    Set/*<PANode>*/ lost_nodes = 
		(COMPRESS_LOST_NODES && AGGRESSIVE_COMPRESS_LOST_NODES) ?
		getLostNodes(current_intra_mmethod) : null;

	    ParIntGraph shrinked_graph =
		lbbpig.keepTheEssential
		(getParamNodes(current_intra_mmethod), false, lost_nodes);

	    if(MEGA_DEBUG)
		System.out.println("Shrinked graph: " + shrinked_graph);

	    // The external view of the graph is stored in the
	    // hash_proc_ext hashtable;
	    hash_proc_ext.put(current_intra_mmethod, shrinked_graph);
	}
    }
    

    // Quad Visitor used by analyze_basic_block
    private PAVisitor pa_visitor = new PAVisitor();
    
    /** Analyzes a basic block - a Parallel Interaction Graph is computed at
	the beginning of the basic block, it is next updated by all the 
	instructions appearing in the basic block (in the order they appear
	in the original program). */
    private void analyze_basic_block(LightBasicBlock lbb) {
	if(DEBUG2 || MEGA_DEBUG) 
	    Debug.print_lbb(lbb, lbb2passes);

	// lbbpig is the graph at the *bb point; it will be 
	// updated till it becomes the graph at the bb* point
	lbbpig = get_initial_bb_pig(lbb);

	// go through all the instructions of this basic block
	HCodeElement[] instrs = lbb.getElements();
	for(int i = 0; i < instrs.length; i++) {
	    Quad q = (Quad) instrs[i];

	    if(DEBUG2 || MEGA_DEBUG || SHOW_INSTR)
		System.out.println("INSTR: " + Util.code2str(q));
	    
	    // update the Parallel Interaction Graph according
	    // to the current instruction
	    q.accept(pa_visitor);
	}

	// if there was a pair, store the pair computed by the inter proc
	// module, otherwise, only the first element of the pair is essential.
	if(call_pp != null) {
	    putPIGPair(lbb, call_pp);
	    call_pp = null;
	}
	else
	    putPIGPair(lbb, new ParIntGraphPair(lbbpig, null));

	if(DO_INTRA_PROC_TRIMMING) 
	    do_intra_proc_trimming(lbb);
    }

    // remove unreachable nodes
    private void do_intra_proc_trimming(LightBasicBlock lbb) {
	PANode[] params = nodes.getAllParams(current_intra_mmethod);
	ParIntGraphPair pp = getPIGPair(lbb);
	for(int i = 0; i < 2; i++) {
	    if(pp.pig[i] != null)
		pp.pig[i] = pp.pig[i].intra_proc_trimming(params);
	}
    }
    

    // For each method, maintains the set of inside nodes that are
    // merged into LOST during the analysis of that method.  These
    // nodes do not appear in the pig for the method, but they are
    // definitely not captured ....
    private Map/*<MetaMethod,Set<PANode>*/ mm2lost = 
	COMPRESS_LOST_NODES ? new HashMap() : null;

    /** @return set of inside nodes that are merged into LOST during
        the analysis of <code>mm</code>.  These are nodes that do not
        appear in the pig for the method, but clearly escape. */
    public Set/*<PANode>*/ getLostNodes(MetaMethod mm) {
	if(!COMPRESS_LOST_NODES)
	    return Collections.EMPTY_SET;
	Set lost = (Set) mm2lost.get(mm);
	if(lost == null) {
	    lost = new HashSet();
	    mm2lost.put(mm, lost);
	}
	return lost;
    }

    /** Returns the Parallel Interaction Graph at the point bb*
     *  The returned <code>ParIntGraph</code> must not be modified 
     *  by the caller. This function is used by 
     *  <code>get_initial_bb_pig</code>. */
    private ParIntGraph get_after_bb_pig(LightBasicBlock lbb,
					 LightBasicBlock lbb_son){

	ParIntGraphPair pp = getPIGPair(lbb);

	if(pp == null)
	    return ParIntGraph.EMPTY_GRAPH;

	int k = 0;
	HCodeElement last_lbb = lbb.getLastElement();
	if((last_lbb != null) && (last_lbb instanceof CALL)){
	    CALL call = (CALL) last_lbb;
	    HCodeElement first_lbb_son = lbb_son.getFirstElement();
	    if(!call.next(0).equals(first_lbb_son)) k = 1;
	}

	ParIntGraph pig = pp.pig[k];
	return (pig==null) ? ParIntGraph.EMPTY_GRAPH : pig;
    }


    /** (Re)computes the Parallel Interaction Thread associated with
     *  the beginning of the <code>LightBasicBlock</code> <code>lbb</code>.
     *  This method is recomputing the stuff (instead of just grabbing it
     *  from the cache) because the information attached with some of
     *  the predecessors has changed (that's why <code>bb</code> is 
     *  reanalyzed) */
    private ParIntGraph get_initial_bb_pig(LightBasicBlock lbb){
	LightBasicBlock[] prev = lbb.getPrevLBBs();
	int len = prev.length;
	if(len == 0) {
	    // This case is treated specially, it's about the
	    // graph at the beginning of the current method.
	    ParIntGraph pig = initial_pig;
	    return pig;
	}
	else {
	    // do the union of the <code>ParIntGraph</code>s attached to
	    // all the predecessors of this basic block
	    ParIntGraph pig = 
		(ParIntGraph) (get_after_bb_pig(prev[0], lbb)).clone();

	    for(int i = 1; i < len; i++)
		pig.join(get_after_bb_pig(prev[i], lbb));

	    return pig;
	}
    }


    /** Computes the parallel interaction graph at the beginning of a 
     *  method; an almost empty graph except for the parameter nodes
     *  for the object formal parameter (i.e. primitive type parameters
     *  such as <code>int</code>, <code>float</code> do not have associated
     *  nodes */
    private ParIntGraph get_mmethod_initial_pig
	(MetaMethod mm, TopSortedCompDiGraph<LightBasicBlock> ts_sccs) {

	LightBasicBlock first_bb = (LightBasicBlock) 	    
	    ts_sccs.decrOrder().get(0).vertices().iterator().next();
	HEADER first_hce = (HEADER) first_bb.getElements()[0];
	METHOD m  = (METHOD) first_hce.next(1);

	Temp[]  params = m.params();
	HMethod     hm = mm.getHMethod();
	HClass[] types = hm.getParameterTypes();

	ParIntGraph pig = new ParIntGraph();

	// the following code is quite messy ... The problem is that I 
	// create param nodes only for the parameter with object types;
	// unfortunately, the types could be found only in HMethod (and
	// do not include the possible this parameter for non-static nodes)
	// while the actual Temps associated with all the formal parameters
	// could be found only in METHOD. So, we have to coordinate
	// information from two different places and, even more, we have
	// to handle the missing this parameter (which is present in METHOD
	// but not in HMethod). 
	boolean isStatic = 
	    java.lang.reflect.Modifier.isStatic(hm.getModifiers());
	// if the method is non-static, the first parameter is not metioned
	// in HMethod - it's the implicit this parameter.
	int skew = isStatic ? 0 : 1;
	// number of object formal parameters = the number of param nodes
	int count = skew;
	for(int i = 0; i < types.length; i++)
	    if(!types[i].isPrimitive()) count++;
	
	nodes.addParamNodes(mm, count);
	Stats.record_mmethod_params(mm, count);

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
		// The param nodes are escaping through themselves */
		pig.G.e.addNodeHole(param_node,param_node);
		count++;
	    }

	if(TOPLAS_PAPER)
	    pig.G.e.addNodeHole(NodeRepository.LOST_SUMMARY,
				NodeRepository.LOST_SUMMARY);

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


    /** Prints some statistics. */
    public final void print_stats() {
	if(!STATS){
	    System.out.println("Statistics are deactivated.");
	    System.out.println("Turn on the PointerAnalysis.STATS flag!");
	    return;
	}

	Stats.print_stats();
	nodes.print_stats();
	System.out.println("==========================================");
	if(SHOW_NODES) {
	    System.out.println("BASIC NODES");
	    System.out.println(nodes);
	    System.out.println("NODE SPECIALIZATIONS:");
	    nodes.show_specializations();
	}
    }


    /** Returns the parallel interaction graph attached to the program point
	right after <code>q</code> in the body of meta-method
	<code>mm</code>. */
    public final ParIntGraph getPIGAtQuad(MetaMethod mm, Quad q){
	return getPigBeforeQuad(mm, q);
    }

    public final ParIntGraph getPIGAtQuad(HMethod hm, Quad q) {
	return getPIGAtQuad(new MetaMethod(hm, true), q);
    }

    
    /** Returns the parallel interaction graph attached to the program point
	right after <code>q</code> in the body of meta-method
	<code>mm</code>. */
    public final ParIntGraph getPigAfterQuad(MetaMethod mm, Quad q) {
	return getPigForQuad(mm, q, AFTER_QUAD);
    }
    
    /** Returns the parallel interaction graph attached to the program point
	right before <code>q</code> in the body of meta-method
	<code>mm</code>. */
    public final ParIntGraph getPigBeforeQuad(MetaMethod mm, Quad q) {
	return getPigForQuad(mm, q, BEFORE_QUAD);
    }
    

    public final ParIntGraph getPigForQuad(MetaMethod mm, Quad q, int moment) {
	assert mcg.getAllMetaMethods().contains(mm) : 
	    "Uncalled/unknown meta-method!";

	LightBasicBlock lbb = find_lbb4quad(mm, q);

	// as all the ParIntGraph's for methods are computed, a simple
	// intra-procedural analysis of mm is enough.
	_analyze_intra_proc(mm); // don't delete the cache

	// find the right basic block, and redo pointer analysis
	// for that block, until we hit q.
	ParIntGraph retval = analyze_lbb_for_q(lbb, q, moment);

	// clear the LightBasicBlock -> ParIntGraph cache
	lbb2pp.clear();

	return retval;
    }

    // finds the LightBasicBlock of Quad q from (meta)method mm
    private LightBasicBlock find_lbb4quad(MetaMethod mm, Quad q) {
	LBBConverter lbbconv = scc_lbb_factory.getLBBConverter();
	LightBasicBlock.Factory lbbf = lbbconv.convert2lbb(mm.getHMethod());
	assert lbbf != null : "Unanalyzable method " + mm + "?!";
	LightBasicBlock lbb = lbbf.getBlock(q);
	assert lbb != null : "No (Light)BasicBlock found for " + q;
	return lbb;
    }

    // constants for analyze_lbb_for_q
    private final static int BEFORE_QUAD = 0;
    private final static int AFTER_QUAD  = 1;

    /** Returns the parallel interaction graph valid at the program
	point right before/after <code>q</code>. <code>q</code> belongs to
	the light basic block <code>lbb</code> of the
	<code>current_intra_mmethod</code>.  The analysis is
	re-executed from the beginning of <code>lbb</code>, to the
	point immediately before/after <code>q</code> when we stop it and
	return the parallel intercation graph valid at that moment. */
    private ParIntGraph analyze_lbb_for_q(LightBasicBlock lbb, Quad q,
					  int moment) {
	lbbpig = get_initial_bb_pig(lbb);
	HCodeElement[] instrs = lbb.getElements();
	for(int i = 0; i < instrs.length; i++) {
	    Quad q_curr = (Quad) instrs[i];
	    if((moment == BEFORE_QUAD) && q_curr.equals(q))
		return lbbpig;
	    q_curr.accept(pa_visitor);
	    if((moment == AFTER_QUAD) && q_curr.equals(q))
		return lbbpig;
	}
	assert false : q + " was not in " + lbb;
	return null; // this should never happen
    }

    // TODO: remove as soon as lbb2pp works
    // activates some GC
    private final void clear_lbb2pig(MetaMethod mm) {
	// cut the method into SCCs of basic blocks
	TopSortedCompDiGraph<LightBasicBlock> ts_sccs = 
	    scc_lbb_factory.computeSCCLBB(mm.getHMethod());
	for(SCComponent/*<LightBasicBlock>*/ scc : ts_sccs.incrOrder()) {
	    for(Object/*LightBasicBlock*/ lbb0 : scc.vertices()) {
		((LightBasicBlock) lbb0).user_info = null;
	    }
	}
    }

    /** Returns the set of the nodes pointed by the temporary <code>t</code>
	at the point right before executing instruction <code>q</code>
	from the body of meta-method <code>mm</code>. */
    public final Set pointedNodes(MetaMethod mm, Quad q, Temp l){
	// 1. obtain the Parallel Interaction Graph attached to the point
	// right before the execution of q (noted .q in the article). 
	ParIntGraph pig = getPIGAtQuad(mm, q);
	// 2. look at the nodes pointed by l; as from a temporary we can
	// have only inside edges, it is enough to look in the I set.
	return pig.G.I.pointedNodes(l);
    }

    // given a quad q, returns the method q is part of 
    private final HMethod quad2method(Quad q) {
	return q.getFactory().getMethod();
    }

    public final Set pointedNodes(Quad q, Temp l) {
	return pointedNodes(new MetaMethod(quad2method(q), true), q, l);
    }


    private Collection typeFilter(Set nodes, HClass hclass) {
	List result = new LinkedList();
	for(Object nodeO : nodes) {
	    PANode node = (PANode) nodeO;
	    if(node.type != PANode.INSIDE) {
		result.add(node);
		continue;
	    }
	    HClass type = getType(node);
	    if((type == null) || type.isInstanceOf(hclass))
		result.add(node);
	}
	return result;
    }

    static HClass getType(PANode node) {
	assert node.type == PANode.INSIDE;
	HCodeElement q = nodes.node2Code(node);
	if(q == null) return null;
	if(q instanceof NEW)
	    return ((NEW) q).hclass();
	else
	    return ((ANEW) q).hclass();
    }


    private static class PathEdge {
	PathEdge(PathEdge pred, PANode start, String f, PANode end, 
		 boolean inside) {
	    this.pred = pred;
	    this.start = start;
	    this.end = end;
	    this.f = f;
	    this.inside = inside;
	}
	
	final PathEdge pred;
	final PANode start, end;
	final String f;
	final boolean inside;
    }

    private void find_trace(ParIntGraph pig) {
	final LinkedList W = new LinkedList();
	final Set reachable = new HashSet();

	W.addLast(new PathEdge(null, null, null, NodeRepository.LOST_SUMMARY,
			   false));
	reachable.add(NodeRepository.LOST_SUMMARY);
	while(!W.isEmpty()) {
	    final PathEdge edge = (PathEdge) W.removeFirst();
	    final PANode node = edge.end;

	    if(node.number == 1533) {
		print_path(edge);
		System.exit(1);
	    }

	    pig.G.I.forAllEdges
		(node,
		 new PAEdgeVisitor() {
		    public void visit(Temp v, PANode node) {}
		    public void visit(PANode node, String f, PANode node2) {
			if(reachable.contains(node2)) return;
			W.addLast(new PathEdge(edge, node, f, node2, true));
			reachable.add(node2);
		    }
		});

	    pig.G.O.forAllEdges
		(node,
		 new PAEdgeVisitor() {
		    public void visit(Temp v, PANode node) {}
		    public void visit(PANode node, String f, PANode node2) {
			if(reachable.contains(node2)) return;
			W.addLast(new PathEdge(edge, node, f, node2, false));
			reachable.add(node2);
		    }
		});
	}
    }

    private void print_path(PathEdge edge) {
	System.out.println("\n\nEscapability path for " + edge.end);
	print_path2(edge);
	System.out.println();
    }

    private void print_path2(PathEdge edge) {
	if(edge.pred != null) {
	    print_path2(edge.pred);
	    System.out.println
		((edge.inside ? "I" : "O") + 
		 ": <" + edge.start + "," + edge.f + "," + edge.end + ">");
	}
	else 
	    System.out.println("START: " + edge.end);
    }

    static Set/*<PANode>*/ selectNodesWithField(Set nodes, String f) {
	Set result = new LinearSet();
	for(Object nodeO : nodes) {
	    PANode node = (PANode) nodeO;
	    if(hasField(node, f))
		result.add(node);
	}
	return result;
    }


    static boolean hasField(PANode node, String f) {
	// now with caching!
	hasFieldQuery.init(node, f);
	Boolean answer = (Boolean) hasFieldCache.get(hasFieldQuery);
	if(answer == null) {
	    answer = new Boolean(_hasField(node, f));
	    hasFieldCache.put(new HasFieldQuery(node, f), answer);
	}
	return answer.booleanValue();
    }
    private static class HasFieldQuery {
	HasFieldQuery() { }
	HasFieldQuery(PANode node, String f) { init(node, f); }
	void init(PANode node, String f) {
	    this.node = node;
	    this.f = f;
	    this.hash = node.hashCode() ^ f.hashCode();
	}
	PANode node;
	String f;
	int hash;
	
	public int hashCode() { return hash; }
	public boolean equals(Object o) {
	    if(this.hashCode() != o.hashCode())
		return false;
	    HasFieldQuery q = (HasFieldQuery) o;
	    return
		(this.node == q.node) && 
		(this.f.equals(q.f));
	}
    }
    private static HasFieldQuery hasFieldQuery = new HasFieldQuery();
    private static Map/*<HasFieldQuery,Boolean>*/ hasFieldCache = 
	new HashMap();

    private static boolean _hasField(PANode node, String f) {
	if(f.equals(PointerAnalysis.ARRAY_CONTENT))
	    return isArrayOfObjs(node);
	
	GenType gts[] = node.getPossibleClasses();
	if(gts == null)
	    return true;

	for(int i = 0; i < gts.length; i++) {
	    GenType gt = gts[i];

	    if(gt.isPOLY()) {
		if(gt.getHClass().getName().equals("java.lang.Object"))
		    return true;
		Set/*<HClass>*/ allClasses = getAllConcreteClasses(gt);
		for(Object hclassO : allClasses) {
		    HClass hclass = (HClass) hclassO;
		    if(classHasField(hclass, f))
			return true;
		}
	    }
	    else { // monomorphic type
		if(classHasField(gt.getHClass(), f))
		    return true;
	    }
	}

	/* // debug code
	System.out.println
	    ("YYY: no " + f + " in " + node + " type: " + pp_types(gts));
	*/
	
	return false;
    }
    
    static Set<HClass> getAllConcreteClasses(GenType gt) {
	if(gt.isPOLY())
	    return
		DiGraph.diGraph
		(Collections.<HClass>singleton(gt.getHClass()),
		 new ForwardNavigator<HClass>() {
		    public List<HClass> next(HClass hClass) {
			Set<HClass> sons = ch.children(hClass);
			return new LinkedList<HClass>(sons);
		    }
		}).vertices();
	else
	    return Collections.singleton(gt.getHClass());
    }

    static Set/*<HClass>*/ getAllConcreteClasses(GenType[] gts) {
	Set result = new HashSet();
	for(int i = 0; i < gts.length; i++)
	    result.addAll(getAllConcreteClasses(gts[i]));
	return result;			  
    }

    private static boolean classHasField(HClass hclass, String f) {
	if(hclass == null) return false;

	HField fields[] = hclass.getDeclaredFields();
	for(int i = 0; i < fields.length; i++) {
	    if(getFieldName(fields[i]).equals(f))
		return true;
	}

	return classHasField(hclass.getSuperclass(), f);
    }


    static boolean isArrayOfObjs(PANode node) {
	// now with caching!
	Boolean answer = (Boolean) isArrayOfObjsCache.get(node);
	if(answer == null) {
	    answer = new Boolean(_isArrayOfObjs(node));
	    isArrayOfObjsCache.put(node, answer);
	}
	return answer.booleanValue();
    }
    private static Map/*<PANode,Boolean>*/ isArrayOfObjsCache = new HashMap();
    
    // does the real job
    private static boolean _isArrayOfObjs(PANode node) {
	GenType gts[] = node.getPossibleClasses();
	if(gts == null) // conservative answer about unknown types
	    return true;
	
	for(int i = 0; i < gts.length; i++) {
	    GenType gt = gts[i];
	    HClass hclass = gt.getHClass();
	    // an array is a special case of java.lang.Object
	    if(gt.isPOLY() && hclass.equals(java_lang_Object))
		return true;
	    
	    if(hclass.isArray() && 
	       !hclass.getComponentType().isPrimitive())
		return true;
	}
	
	/* // debug code
	System.out.println
	    ("YYY: not an array of objs: " + node + " type: " + pp_types(gts));
	*/
	
	return false;
    }


    private static String pp_types(GenType[] gts) {
	StringBuffer buff = new StringBuffer();
	for(int i = 0; i < gts.length; i++) {
	    if(i != 0) buff.append(", ");
	    buff.append(gts[i]);
	}
	return buff.toString();
    }

    // If node may be an array of objects, than return a set of
    // conservative estimates for the component types of the array
    // Otherwise, return null.
    static Set/*<HClass>*/ getObjArrayComp(PANode node) {

	GenType gts[] = node.getPossibleClasses();
	if(gts == null) // conservative answer about unknown types
	    return Collections.singleton(java_lang_Object);

	Set compTypes = new LinearSet();

	for(int i = 0; i < gts.length; i++) {
	    GenType gt = gts[i];
	    HClass hclass = gt.getHClass();
	    // an array is a special case of java.lang.Object
	    if(gt.isPOLY() && hclass.equals(java_lang_Object))
		return Collections.singleton(java_lang_Object);
	    
	    if(hclass.isArray() && !hclass.getComponentType().isPrimitive())
		compTypes.add(hclass.getComponentType());
	}

	if(compTypes.size() == 0)
	    compTypes = null;

	return compTypes;
    }


    // TODO: look at the type of the node directly
    // select only the nodes that may represent arrays of objects
    static Set/*<PANode>*/ selectArraysOfObjs(Set/*<PANode>*/ nodes) {
	Set result = new HashSet();

	for(Object nodeO : nodes) {
	    PANode node = (PANode) nodeO;
	    if(isArrayOfObjs(node))
		result.add(node);
	}

	return result;
    }

    // TODO: use HFields, instead of Strings, as edge labels
    // It's much healthier!
    // returns the string name for a field
    static String getFieldName(HField hf) {
	String name = (String) getFieldNameCache.get(hf);
	if(name == null) {
	    // it used to be only hf.getName();
	    name = hf.getDeclaringClass().getName() + "." + hf.getName();
	    getFieldNameCache.put(hf, name);
	}
	return name;
    }
    private static Map/*<HField,String>*/ getFieldNameCache = new HashMap();
}
