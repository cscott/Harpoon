// ParIntGraph.java, created Sun Jan  9 15:40:59 2000 by salcianu
// Copyright (C) 2000 Alexandru SALCIANU <salcianu@MIT.EDU>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.PointerAnalysis;


import java.util.Set;
import java.util.Iterator;
import java.util.HashSet;
import java.util.Collections;


import harpoon.Temp.Temp;

/**
 * <code>ParIntGraph</code> Parallel Interaction Graph
 * 
 * @author  Alexandru SALCIANU <salcianu@MIT.EDU>
 * @version $Id: ParIntGraph.java,v 1.1.2.12 2000-02-21 04:47:59 salcianu Exp $
 */
public class ParIntGraph {

    private final static boolean DEBUG = true;

    /** Default (empty) graph. It doesn't contain any information.  */
    public static final ParIntGraph EMPTY_GRAPH = new ParIntGraph();

    /** Points-to escape graph that summarizes the points-to and escape
	information for the current thread. */
    public PointsToGraph G;
    
    /** The parralel thread map; it attaches to each thread node nT, an 
	integer from the set {0,1,2} where 2 signifies the possibility
	that multiple instances of nT execute in parallel with the current
	thread. */
    public PAThreadMap tau;
    
    /** Maintains the actions executed by the analysed code and the parallel\
	action relation. <code>alpha</code> and <code>pi</code> from the 
	original paper have been merged into this single field for efficiency
	reasons. */
    public ActionRepository ar;

    /** Maintains the (conservative) ordering relations between the inside
	and the outside edges. <code>before(ei,eo)</code> is true if the
	inside edge ei might be created before the ouside edge eo was read.<br>
    */
    public EdgeOrdering eo;
    
    /** Creates a <code>ParIntGraph</code>. */
    public ParIntGraph() {
	G   = new PointsToGraph();
	tau = new PAThreadMap();
	ar  = new ActionRepository();
	eo  = new EdgeOrdering();
    }
    

    /** <code>join</code> combines two <code>ParIntGraph</code>s in \
	a control-flow join point. */
    public void join(ParIntGraph pig2){
	G.join(pig2.G);
	tau.join(pig2.tau);
	ar.join(pig2.ar);
	eo.join(pig2.eo);
    }


    /** Inserts the image of <code>pig2</code> parallel interaction graph
	through the <code>mu</code> node mapping into <code>this</code> object.
	This method is designed to be called at the end of the caller/callee
	or starter/startee interaction. It is *not* manipulating the action
	repository; this manipulation is too complex and variate to be done
	here. */ 
    void insertAllButAr(ParIntGraph pig2, Relation mu,
			boolean principal, Set noholes){
	G.insert(pig2.G,mu,principal,noholes);
	tau.insert(pig2.tau,mu);
	// ar.insert(pig2.ar,mu);
    }

    /** Convenient function equivalent to 
	<code>insertAllButAr(pig2,mu,Collections.EMPTY_SET). */
    void insertAllButAr(ParIntGraph pig2, Relation mu, boolean principal){
	insertAllButAr(pig2,mu,principal,Collections.EMPTY_SET);
    }


    /** Check the equality of two <code>ParIntGraph</code>s. */
    public boolean equals(Object o){
	if(o==null) return false;
	ParIntGraph pig2 = (ParIntGraph)o;
	// return G.equals(pig2.G) && tau.equals(pig2.tau);
	if(!G.equals(pig2.G)){
	    if(PointerAnalysis.DEBUG2)
		System.out.println("The graphs are different");
	    return false;
	}
	if(!tau.equals(pig2.tau)){
	    if(PointerAnalysis.DEBUG2)
		System.out.println("The tau's are different");
	    return false;
	}
	if(!ar.equals(pig2.ar)){
	    if(PointerAnalysis.DEBUG2)
		System.out.println("The ar's are different");
	    return false;
	}
	if(!eo.equals(pig2.eo)){
	    if(PointerAnalysis.DEBUG2)
		System.out.println("The eo's are different");
	    return false;
	}
	return true;
    }

    /** Private constructor for <code>clone</code> and 
	<code>keepTheEssential</code>. */
    private ParIntGraph(PointsToGraph G, PAThreadMap tau,
			ActionRepository ar, EdgeOrdering eo){
	this.G   = G;
	this.tau = tau;
	this.ar  = ar;
	this.eo  = eo;
    }

    /** <code>clone</code> produces a copy of the <code>this</code>
     * Parallel Interaction Graph. */
    public Object clone(){
	return new ParIntGraph((PointsToGraph)G.clone(),
			       (PAThreadMap)tau.clone(),
			       (ActionRepository)ar.clone(),
			       (EdgeOrdering)eo.clone());
    }

    
    /** Produces a <code>ParIntGraph</code> containing only the \
	nodes that could be reached from the outside.
	(i.e. via parameters,
	class nodes, normally or exceptionally returned nodes or the
	started thread nodes) */
    public ParIntGraph keepTheEssential(PANode[] params, boolean is_main){
	HashSet remaining_nodes = new HashSet();
	remaining_nodes.addAll(tau.activeThreadSet());

	PointsToGraph _G = 
	    G.keepTheEssential(params, remaining_nodes, is_main);

	PAThreadMap _tau = (PAThreadMap) tau.clone();

	//TODO: find something more intelligent!
	ActionRepository _ar = (ActionRepository) ar.clone();

	EdgeOrdering _eo = eo.keepTheEssential(remaining_nodes);
	return new ParIntGraph(_G,_tau,_ar,_eo);
    }

    // Visits all the nodes from set_nodes.
    private void forSet(Set set_nodes, PANodeVisitor visitor){
	Iterator it_nodes = set_nodes.iterator();
	while(it_nodes.hasNext())
	    visitor.visit((PANode) it_nodes.next());
    }

    /** Visits all the nodes that appear in <code>this</code> graph. */
    public void forAllNodes(final PANodeVisitor visitor){
	G.O.forAllNodes(visitor);
	G.I.forAllNodes(visitor);
	forSet(G.r,visitor);
	forSet(G.excp,visitor);
	forSet(G.e.escapedNodes(),visitor);
	forSet(tau.activeThreadSet(),visitor);

	ar.forAllActions(new ActionVisitor(){
		public void visit_ld(PALoad load){
		    visitor.visit(load.n1);
		    visitor.visit(load.n2);
		    visitor.visit(load.nt);
		}
		public void visit_sync(PANode n, PANode nt){
		    visitor.visit(n);
		    visitor.visit(nt);
		}
	    });

	// the edgess appearing in the edge ordering relation are also
	// present into G.O and G.I and so, they have been already visited
    }

    /** Removes the nodes from <code>nodes</code> from <code>this</code>
	graph. */
    public void remove(Set nodes){
	G.remove(nodes);
	tau.remove(nodes);
	ar.removeNodes(nodes);
	eo.removeNodes(nodes);
    }

    /** Simplify <code>this</code> parallel interaction graph by removing the
	loads that don't escape anywhere (and hence, don't represent any
	object). In addition, the <code>&lt;&lt;n1,f&gt;,n2&gt;</code>
	ouside (load) edges where <code>n1</code> is an unescaped node are
	removed too. */ 
    public void removeEmptyLoads(){
	final Set empty_loads = new HashSet();
	final Set fake_outside_edges = new HashSet();
	
	G.O.forAllEdges(new PAEdgeVisitor(){
		public void visit(Temp var, PANode node){}
		public void visit(PANode node1, String f, PANode node2){
		    if(!G.e.hasEscaped(node1))
			fake_outside_edges.add(new PAEdge(node1,f,node2));
		    if(!G.e.hasEscaped(node2))
			empty_loads.add(node2);
		}
	    });

	if(DEBUG){
	    System.out.println("Empty loads:" + empty_loads);
	    System.out.println("Fake outside edges: " + fake_outside_edges);
	}

	remove(empty_loads);

	Iterator it_edges = fake_outside_edges.iterator();
	while(it_edges.hasNext()){
	    PAEdge edge = (PAEdge) it_edges.next();
	    G.O.removeEdge(edge.n1,edge.f,edge.n2);
	}

	ar.removeEdges(fake_outside_edges);
	eo.removeEdges(fake_outside_edges);
    }

    /** Pretty-print function for debug purposes. 
	Two equal <code>ParIntGraph</code>s are guaranteed to have the same
	string representation. */
    public String toString(){
	return "\nParIntGraph{\n" + G + " " + tau + ar + eo + "}"; 
    }

}










