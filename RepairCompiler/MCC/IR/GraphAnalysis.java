package MCC.IR;
import java.util.*;
import MCC.State;

public class GraphAnalysis {
    Termination termination;
    static final int WORKS=0;
    static final int ERR_NOREPAIR=1;
    static final int ERR_CYCLE=2;
    static final int ERR_RULE=3;
    static final int ERR_ABSTRACT=4;

    public GraphAnalysis(Termination t) {
	termination=t;
    }
    /* This function checks that
       1) All abstract repairs have a corresponding data structure update
          that isn't removed.
       2) All scope nodes have either a consequence node or a compensation
          node that isn't removed.
     */
    public int checkRepairs(Set removed) {
	Set nodes=new HashSet();
	nodes.addAll(termination.conjunctions);
	nodes.removeAll(removed);
	GraphNode.computeclosure(nodes,removed);
	Set toretain=new HashSet();
	toretain.addAll(termination.abstractrepair);
	toretain.addAll(termination.scopenodes);
	nodes.retainAll(toretain);
	/* Nodes is now the reachable set of abstractrepairs */
	/* Check to see that each has an implementation */
	for(Iterator it=nodes.iterator();it.hasNext();) {
	    GraphNode gn=(GraphNode)it.next();
	    TermNode tn=(TermNode)gn.getOwner();
	    if (tn.getType()==TermNode.RULESCOPE) {
		boolean foundnode=false;
		for (Iterator edgeit=gn.edges();it.hasNext();) {
		    GraphNode.Edge edge=(GraphNode.Edge)edgeit.next();
		    GraphNode gn2=edge.getTarget();
		    if (!removed.contains(gn2)) {
			TermNode tn2=(TermNode)gn2.getOwner();
			if (tn2.getType()==TermNode.CONSEQUENCE||
			    tn2.getType()==TermNode.UPDATE) {
			    foundnode=true;
			    break;
			}
		    }
		}
		if (!foundnode)
		    return ERR_RULE;
	    } else if (tn.getType()==TermNode.ABSTRACT) {
		boolean foundnode=false;
		for (Iterator edgeit=gn.edges();it.hasNext();) {
		    GraphNode.Edge edge=(GraphNode.Edge)edgeit.next();
		    GraphNode gn2=edge.getTarget();
		    if (!removed.contains(gn2)) {
			TermNode tn2=(TermNode)gn2.getOwner();
			if (tn2.getType()==TermNode.UPDATE) {
			    foundnode=true;
			    break;
			}
		    }
		}
		if (!foundnode)
		    return ERR_ABSTRACT;
	    } else throw new Error("Unanticipated Node");
	}
	return WORKS;
    }
    /* This method checks that all constraints have a conjunction nodes
       and that there are no bad cycles in the abstract portion of the graph.
     */
    public int checkAbstract(Set removed) {
	Vector constraints=termination.state.vConstraints;
	for(int i=0;i<constraints.size();i++) {
	    Constraint c=(Constraint)constraints.get(i);
	    Set conjunctionset=(Set)termination.conjunctionmap.get(c);

   	    boolean foundrepair=false;
	    for(Iterator it=conjunctionset.iterator();it.hasNext();) {
		GraphNode gn=(GraphNode)it.next();
		if (!removed.contains(gn)) {
		    foundrepair=true;
		}
   	    }
	    if (!foundrepair)
		return ERR_NOREPAIR;
	}
	Set abstractnodes=new HashSet();
       	abstractnodes.addAll(termination.conjunctions);
	abstractnodes.removeAll(removed);
	GraphNode.computeclosure(abstractnodes,removed);

	Set tset=new HashSet();
	tset.addAll(termination.conjunctions);
	tset.addAll(termination.abstractrepair);
	tset.addAll(termination.scopenodes);
	tset.addAll(termination.consequencenodes);
	abstractnodes.retainAll(tset);
	Set cycles=GraphNode.findcycles(abstractnodes);

	for(Iterator it=cycles.iterator();it.hasNext();) {
	    GraphNode gn=(GraphNode)it.next();
	    TermNode tn=(TermNode)gn.getOwner();
	    switch(tn.getType()) {
	    case TermNode.CONJUNCTION:
	    case TermNode.ABSTRACT:
		return ERR_CYCLE;
	    case TermNode.UPDATE:
		throw new Error("No Update Nodes should be here");
	    default:
	    }
	}
	return WORKS;
    }
}
