// InterThreadPA.java, created Mon Jan 31 20:52:46 2000 by salcianu
// Copyright (C) 2000 Alexandru SALCIANU <salcianu@MIT.EDU>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.PointerAnalysis;

import java.util.Set;
import java.util.Enumeration;


import harpoon.Util.Util;
import harpoon.ClassFile.HMethod;
import harpoon.Temp.Temp;

/**
 * <code>InterThreadPA</code>
 * 
 * @author  Alexandru SALCIANU <salcianu@MIT.EDU>
 * @version $Id: InterThreadPA.java,v 1.1.2.1 2000-02-07 02:08:25 salcianu Exp $
 */
abstract class InterThreadPA {
    
    public static void resolve_threads(ParIntGraph pig, PointerAnalysis pa){
    }


    // Computes the interactions with all the threads launched by the node
    // nt. If tau(nt)==1 (at most one such thread could exist), this method
    // computes the interaction of the Starter with only one instance of
    // an nt-thread. If tau(nt)==2 (there could be more than one thread,
    // anything between 0 and infinity); a fixed-point algorithm is necessary
    // in this case.
    private static ParIntGraph interaction_nt(ParIntGraph pig, PANode nt,
					HMethod[] ops, PointerAnalysis pa){
	boolean only_once = (pig.tau.getValue(nt)==1);

	adjust_escape_and_tau(pig,nt);

	if(only_once)
	    // a single interaction takes place
	    pig = interact_once(pig,nt,ops,pa);
	else{
	    // a fixed-point algorithm is necessary in this case
	    while(true){
		ParIntGraph previous_pig = (ParIntGraph)pig.clone();
		pig = interact_once(pig,nt,ops,pa);
		if(pig.equals(previous_pig)) break;
	    }
	}

	return remove_empty_loads(pig);
    }


    // Computes the interaction with a SINGLE instance of a thread launched
    // by the nt node. This incolves separately computing the interactions
    // with all the possible run() methods (the body of the thread) and
    // joining the results.
    private static ParIntGraph interact_once(ParIntGraph pig, PANode nt,
				      HMethod[] ops, PointerAnalysis pa){
	int nb_ops = ops.length;
	Util.assert(nb_ops > 0, "No run method for the thread" + nt);

	// special, optimized case: only one run method to analyze
	if(nb_ops == 1)
	    return interact_once_op(pig,nt,ops[0],pa);

	// general case: many possible run() method. The following code could
	// seem too complicate but everything has been done to reduce the
	// costly ParIntGraph.clone() operation to the minimum

	// compute the first term of the join operation:
	// the interaction with the first run() method
	ParIntGraph pig_after = 
	    interact_once_op((ParIntGraph)pig.clone(),nt,ops[0],pa);
	
	// join to it all the other terms (interactions with all the
	// run() methods except the first and the last ones).
	for(int i = 1 ; i < nb_ops - 1; i++)
	    pig_after.join(
		interact_once_op((ParIntGraph)pig.clone(),nt,ops[i],pa)); 

	// compute and join the interction with the last possible run() method
	pig_after.join(interact_once_op(pig,nt,ops[nb_ops-1],pa));

	return pig_after;
    }


    // Computes the interaction between the Starter and a SINGLE thread having
    // the node nt as a receiver and op as the run() body function.
    private static ParIntGraph interact_once_op(ParIntGraph pig_starter,
						PANode nt,
					 HMethod op, PointerAnalysis pa){
	ParIntGraph pig[] = new ParIntGraph[2];
	pig[0] = pig_starter;
	pig[1] = pa.getExtParIntGraph(op);
	
	PANode[] params = pa.getParamNodes(op);
	
	Relation mu[] = compute_initial_mappings(pig,nt,params);
	concretize_loads(pig,mu);
	
	compute_final_mappings(pig,mu,nt);
	ParIntGraph new_pig = build_new_pig(pig,mu);

	// compute the escape function for the new graph
	new_pig.G.propagate(new_pig.G.e.escapedNodes());

	return new_pig;
    }

    // In Starter: e(n) = e(n) - {nt}, for all n and tau(nt) = 0
    private static void adjust_escape_and_tau(ParIntGraph pig, PANode nt){
	pig.G.e.removeNodeHoleFromAll(nt);
	pig.tau.setToZero(nt);
    }

    /** Set the initial mappings: class nodes, parameter->thread node.
	Parameters:<br><ul>
	<li>pig[0] - the parallel interaction graph of the Starter;
	<li>pig[1] - the parallel interaction graph of the Startee.
	</ul><br>
	Returns:<br><ul>
	<li>mu[0] - the mapping of the nodes from the Starter <br>;
	<li>mu[1] - the mapping of the nodes from the Startee <br>.
	</ul>
    */    
    private static Relation[] compute_initial_mappings(ParIntGraph[] pig,
						       PANode nt,
						       PANode[] params){
	// Paranoic debug! Trust no one!
	Util.assert(params.length == 1, "Thread function with too many args");

	Relation mu0 = new Relation();
	map_static_nodes(pig[0],mu0);

	Relation mu1 = new Relation();
	mu1.add(params[0],nt);
	map_static_nodes(pig[1],mu1);

	return (new Relation[]{mu0,mu1});
    }


    /* Maps the static nodes that appear in pig to themselves. Only those
       static nodes that appear as sources of arcs need to be mapped; if
       necessary, the others wil be mapped by the rest of the algorithm.
       (the matching goes always "forward" on the edges, never "backward", so
       it's necessary to trigger it just in the sources of the edges. */
    private static void map_static_nodes(ParIntGraph pig,Relation mu){
	Enumeration enum = pig.G.O.allSourceNodes();
	while(enum.hasMoreElements()){
	    PANode node = (PANode) enum.nextElement();
	    if(node.type == PANode.STATIC)
		mu.add(node,node);
	}
	enum = pig.G.I.allSourceNodes();
	while(enum.hasMoreElements()){
	    PANode node = (PANode) enum.nextElement();
	    if(node.type == PANode.STATIC)
		mu.add(node,node);
	}
    }


    /** Computes the mappings by matching ouside edges from one graph
	against inside edges from the other one. */
    private static void concretize_loads(ParIntGraph[] pig, Relation[] mu){
	PAWorkList W[] = { new PAWorkList(), new PAWorkList() };
	Relation new_info[] = { (Relation)(mu[0].clone()),
				(Relation)(mu[1].clone()) };
	
	W[0].addAll(mu[0].keySet());
	W[1].addAll(mu[1].keySet());

	while(true){
	    int i,ib;
	    if(!W[0].isEmpty()) { i=0; ib=1; }
	    else 
		if(!W[1].isEmpty()) { i=1; ib=0; }
		else break;

	    PANode node = (PANode) W[i].remove();

	    Matching.rule0(node,pig,W,mu,new_info,i,ib);
	    Matching.rule2(node,pig,W,mu,new_info,i,ib);
	    Matching.rule3(node,pig,W,mu,new_info,i,ib);
	}
    }


    // Compute the final mappings. Every node from the Starter and the Startee
    // will be put in the new graph (node-mu->node) except for the parameter
    // node of the Startee run() method; this one will be mapped to nt.
    private static void compute_final_mappings(final ParIntGraph[] pig,
					       final Relation[] mu,
					       final PANode nt){
	PANodeVisitor visitor_starter = new PANodeVisitor(){
		public void visit(PANode node){
		    mu[0].add(node,node);
		}
	    };

	pig[0].G.forAllNodes(visitor_starter);

	PANodeVisitor visitor_startee = new PANodeVisitor(){
		public void visit(PANode node){
		    int type = node.type();
		    if(type == PANode.PARAM)
			mu[1].add(node,nt);
		    else
			mu[0].add(node,node);
		}
	    };

	pig[1].G.forAllNodes(visitor_startee);
    }


    // Build the new graph using the graphs from the starter and the startee
    // and the mu mappings.
    private static ParIntGraph build_new_pig(ParIntGraph[] pig, Relation[] mu){

	ParIntGraph new_pig = new ParIntGraph();

	translate_edges(new_pig,pig[0],mu[0]);
	translate_edges(new_pig,pig[1],mu[1]);

	return new_pig;
    }


    private static void translate_edges(final ParIntGraph new_pig,
					ParIntGraph pig, final Relation mu){
	// visitor for the outside edges
	PAEdgeVisitor visitor_O = new PAEdgeVisitor(){
		public void visit(Temp var, PANode node){}
		public void visit(PANode node1,String f, PANode node2){
		    Set mu_node1 = mu.getValuesSet(node1);
		    new_pig.G.O.addEdges(mu_node1,f,node2);
		}
	    };

	pig.G.O.forAllEdges(visitor_O);

	// visitor for the inside edges
	PAEdgeVisitor visitor_I = new PAEdgeVisitor(){
		public void visit(Temp var, PANode node){
		    Set mu_node = mu.getValuesSet(node);
		    new_pig.G.I.addEdges(var,mu_node);
		}
		public void visit(PANode node1,String f, PANode node2){
		    Set mu_node1 = mu.getValuesSet(node1);
		    Set mu_node2 = mu.getValuesSet(node2);
		    new_pig.G.I.addEdges(mu_node1,f,mu_node2);
		}
	    };

	pig.G.I.forAllEdges(visitor_I);

    }

    // Remove from a parallel interaction graph the load nodes that doesn't
    // escape anywhere; as a load nodes nl abstracts all the objects that could
    // are reachable from the holes e(nl), a load node with e(nl) empty doesn't
    // represent anything and can be removed.
    private static ParIntGraph remove_empty_loads(ParIntGraph pig){
	return pig;
    }

}
