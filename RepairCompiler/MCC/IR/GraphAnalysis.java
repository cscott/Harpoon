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

    public Set doAnalysis() {
	HashSet cantremove=new HashSet();
	HashSet mustremove=new HashSet();
	HashSet optionalabstractrepair=new HashSet();

	for (Iterator it=termination.abstractrepair.iterator();it.hasNext();) {
	    GraphNode gn=(GraphNode)it.next();
	    TermNode tn=(TermNode)gn.getOwner();
	    AbstractRepair ar=tn.getAbstract();
	    DNFPredicate dpred=ar.getPredicate();
	    Set repairnodes=(Set)termination.predtoabstractmap.get(dpred);
	    if ((repairnodes.size()>1)&&
		containsmodify(repairnodes)) {
		optionalabstractrepair.add(gn);
	    }
   	}

	cantremove.addAll(termination.scopenodes);
	cantremove.addAll(termination.abstractrepair);
	cantremove.removeAll(optionalabstractrepair);

	while(true) {
	    boolean change=false;
	    HashSet nodes=new HashSet();
	    nodes.addAll(termination.conjunctions);
	    nodes.removeAll(mustremove);
	    GraphNode.computeclosure(nodes,mustremove);
	    Set cycles=GraphNode.findcycles(nodes);
	    Set couldremove=new HashSet();
    	    couldremove.addAll(termination.conjunctions);
	    couldremove.addAll(termination.updatenodes);
	    couldremove.addAll(termination.consequencenodes);
	    couldremove.addAll(optionalabstractrepair);
	    couldremove.retainAll(cycles);


	    /* Look for constraints which can only be satisfied one way */
	    
	    Vector constraints=termination.state.vConstraints;
	    for(int i=0;i<constraints.size();i++) {
		Constraint c=(Constraint)constraints.get(i);
		Set conjunctionset=(Set)termination.conjunctionmap.get(c);
		HashSet tmpset=new HashSet();
		tmpset.addAll(conjunctionset);
		tmpset.removeAll(mustremove);
		if (tmpset.size()==1) {
		    int oldsize=cantremove.size();
		    cantremove.addAll(tmpset);
		    if (cantremove.size()!=oldsize)
			change=true;
		}
	    }

	    /* Search through conjunction which must be satisfied, and attempt
	       to generate appropriate repair actions
	     */
	    HashSet newset=new HashSet();
	    for(Iterator cit=cantremove.iterator();cit.hasNext();) {
		GraphNode gn=(GraphNode)cit.next();
		boolean nomodify=true;
		HashSet toremove=new HashSet();
		if (termination.conjunctions.contains(gn)) {
		    for(Iterator edgeit=gn.edges();edgeit.hasNext();) {
			GraphNode.Edge e=(GraphNode.Edge)edgeit.next();
			GraphNode gn2=e.getTarget();
			TermNode tn2=(TermNode)gn2.getOwner();
			if (nodes.contains(gn2)&&!mustremove.contains(gn2)&&
			    tn2.getType()==TermNode.ABSTRACT) {

			    HashSet updateset=new HashSet();
			    for(Iterator upit=gn2.edges();upit.hasNext();) {
				GraphNode.Edge e2=(GraphNode.Edge)upit.next();
				GraphNode gn3=e2.getTarget();
				TermNode tn3=(TermNode)gn3.getOwner();
				if (tn3.getType()==TermNode.UPDATE)
				    updateset.add(gn3);
			    }
			    updateset.removeAll(mustremove);

			    AbstractRepair ar=tn2.getAbstract();
			    DNFPredicate dpred=ar.getPredicate();
			    Set repairnodes=(Set)termination.predtoabstractmap.get(dpred);
			    if (repairnodes.size()>1&&
				containsmodify(repairnodes)) {
				/* We are modifying a relation */
				HashSet retainednodes=new HashSet();
				retainednodes.addAll(repairnodes);
				retainednodes.retainAll(nodes);

				if (ar.getType()==AbstractRepair.MODIFYRELATION) {
				    if (updateset.size()==0) {
					if (retainednodes.size()>1) {
					    mustremove.add(gn);
					    change=true;
					} else return null; /* Out of luck */
				    }
				    if (updateset.size()==1&&retainednodes.size()==1)
					toremove.addAll(updateset); /* Required update */
				} else {
				    /* Addition or removal to relation */
				    assert (ar.getType()==AbstractRepair.ADDTORELATION)||(ar.getType()==AbstractRepair.REMOVEFROMRELATION);
				    if (updateset.size()==0) {
					if (containsmodify(retainednodes)) {
					    /* Both ADD & REMOVE are no good */
					    for(Iterator it=retainednodes.iterator();it.hasNext();) {
						GraphNode gnit=(GraphNode)it.next();
						TermNode tnit=(TermNode)gnit.getOwner();
						AbstractRepair arit=tnit.getAbstract();
						if (arit.getType()!=AbstractRepair.MODIFYRELATION) {
						    mustremove.add(gnit);
						    change=true;
						}
					    }
					} else
					    return null; /* Out of luck */
				    }
				    if (updateset.size()==1&&retainednodes.size()==2)
					toremove.addAll(updateset); /* Required update */
				}
			    } else if (updateset.size()==1)
				toremove.addAll(updateset);
			}
		    }
		    newset.addAll(toremove);
		}
	    }
	    {
		int oldsize=cantremove.size();
		cantremove.addAll(newset);
		if (cantremove.size()!=oldsize)
		    change=true;
	    }

	    /* Look for required actions for scope nodes */
	    for(Iterator scopeit=termination.scopenodes.iterator();scopeit.hasNext();) {
		GraphNode gn=(GraphNode)scopeit.next();
		HashSet tmpset=new HashSet();
		for(Iterator edgeit=gn.edges();edgeit.hasNext();) {
		    GraphNode.Edge e=(GraphNode.Edge)edgeit.next();
		    tmpset.add(e.getTarget());
		}
		tmpset.removeAll(mustremove);
		if (tmpset.size()==1) {
		    int oldsize=cantremove.size();
		    cantremove.addAll(tmpset);
		    if (cantremove.size()!=oldsize)
			change=true;
		}
	    }
	    
	    Set cycles2=GraphNode.findcycles(cantremove);
	    for(Iterator it=cycles2.iterator();it.hasNext();) {
		GraphNode gn=(GraphNode)it.next();
		if (termination.conjunctions.contains(gn))
		    return null; // Out of luck
	    }
	    
	    
	    for(Iterator it=termination.conjunctions.iterator();it.hasNext();) {
		GraphNode gn=(GraphNode)it.next();
		boolean foundnocycle=false;

		for (Iterator edgeit=gn.edges();edgeit.hasNext();) {
		    GraphNode.Edge e=(GraphNode.Edge)edgeit.next();
		    GraphNode gn2=e.getTarget();
		    TermNode tn2=(TermNode)gn2.getOwner();
		    if (tn2.getType()!=TermNode.ABSTRACT)
			continue;
		    for (Iterator edgeit2=gn2.edges();edgeit2.hasNext();) {
			GraphNode.Edge e2=(GraphNode.Edge)edgeit2.next();
			GraphNode gn3=e2.getTarget();
			TermNode tn3=(TermNode)gn3.getOwner();
			if (tn3.getType()!=TermNode.UPDATE)
			    continue;
			boolean containsgn=cantremove.contains(gn);
			boolean containsgn3=cantremove.contains(gn3);
			cantremove.add(gn);
			cantremove.add(gn3);
			Set cycle=GraphNode.findcycles(cantremove);
			if (cycle.contains(gn3)) {
			    if (!mustremove.contains(gn3)) {
				change=true;
				mustremove.add(gn3);
			    }
			}
			if (!mustremove.contains(gn3)&&!cycle.contains(gn)) {
			    foundnocycle=true;
			}
			if (!containsgn)
			    cantremove.remove(gn);
			if (!containsgn3)
			    cantremove.remove(gn3);
		    }
		}
		if(!foundnocycle) {
		    if (!mustremove.contains(gn)) {
			change=true;
			mustremove.add(gn);
		    }
		}
	    }
	    couldremove.removeAll(mustremove);
	    couldremove.removeAll(cantremove);
	    
	    Vector couldremovevector=new Vector();
	    couldremovevector.addAll(couldremove);
	    Vector combination=new Vector();
	    if(change)
		continue; //recalculate

	    System.out.println("Searching set of "+couldremove.size()+" nodes.");
	    System.out.println("Eliminated must "+mustremove.size()+" nodes");
	    System.out.println("Eliminated cant "+cantremove.size()+" nodes");
	    System.out.println("Searching following set:");
	    for(Iterator it=couldremove.iterator();it.hasNext();) {
		GraphNode gn=(GraphNode)it.next();
		System.out.println(gn.getTextLabel());
	    }
	    
	    
	    while(true) {
		if (illegal(combination,couldremovevector))
		    break;
		Set combinationset=buildset(combination,couldremovevector);
		checkmodify(combinationset);
		combinationset.addAll(mustremove);
		if (combinationset!=null) {
		    System.out.println("Checkabstract="+checkAbstract(combinationset));
		    System.out.println("Checkrepairs="+checkRepairs(combinationset));
		    System.out.println("Checkall="+checkAll(combinationset));
		    
		    if (checkAbstract(combinationset)==0&&
			checkRepairs(combinationset)==0&&
			checkAll(combinationset)==0) {
			return combinationset;
		    }
		}
		increment(combination,couldremovevector);
	    }
	    return null;
	}
    }

    private void checkmodify(Set removednodes) {
	for (Iterator it=removednodes.iterator();it.hasNext();) {
	    GraphNode gn=(GraphNode)it.next();
	    TermNode tn=(TermNode)gn.getOwner();
	    if (tn.getType()==TermNode.ABSTRACT) {
		AbstractRepair ar=tn.getAbstract();
		DNFPredicate dpred=ar.getPredicate();
		Set repairnodes=(Set)termination.predtoabstractmap.get(dpred);
		/* Has MODIFYRELATION */
		if (containsmodify(repairnodes)&&
		    (repairnodes.size()>1)&&
		    (ar.getType()==AbstractRepair.REMOVEFROMRELATION||
		     ar.getType()==AbstractRepair.ADDTORELATION)) {
		    for(Iterator it2=repairnodes.iterator();it2.hasNext();) {
			GraphNode gn2=(GraphNode)it2.next();
			TermNode tn2=(TermNode)gn2.getOwner();
			AbstractRepair ar2=tn2.getAbstract();
			if (ar2.getType()==AbstractRepair.REMOVEFROMRELATION||
			    ar2.getType()==AbstractRepair.ADDTORELATION) {
			    removednodes.add(gn2);
			}
		    }
		}
	    }
    	}
    }

    private static boolean containsmodify(Set repairnodes) {
	for (Iterator it=repairnodes.iterator();it.hasNext();) {
	    GraphNode gn=(GraphNode) it.next();
	    TermNode tn=(TermNode)gn.getOwner();
	    AbstractRepair ar=tn.getAbstract();
	    if (ar.getType()==AbstractRepair.MODIFYRELATION)
		return true;
	}
	return false;
    }

    private static Set buildset(Vector combination, Vector couldremove) {
	Set s=new HashSet();
	for(int i=0;i<combination.size();i++) {
	    int index=((Integer)combination.get(i)).intValue();
	    Object o=couldremove.get(index);
	    if (s.contains(o))
		return null;
	    else
		s.add(o);
	}
	return s;
    }

    private static boolean illegal(Vector combination, Vector couldremove) {
	if (combination.size()>couldremove.size())
	    return true;
	else return false;
    }
    private static void increment(Vector combination, Vector couldremove) {
	boolean incremented=false;
	boolean forcereset=false;
	for(int i=0;i<combination.size();i++) {
	    int newindex=((Integer)combination.get(i)).intValue()+1;
	    if (newindex==couldremove.size()||forcereset) {
		forcereset=false;
		if ((i+1)==combination.size()) {
		    newindex=1;
		} else
		    newindex=((Integer)combination.get(i+1)).intValue()+2;
		for(int j=i;j>=0;j--) {
		    combination.set(j,new Integer(newindex));
		    newindex++;
		}
		if (newindex>couldremove.size())
		    forcereset=true;
	    } else {
		incremented=true;
		combination.set(i,new Integer(newindex));
		break;
	    }
	}
	if (incremented==false) /* Increase length */ {
	    combination.add(new Integer(0));
	    System.out.println("Expanding to :"+combination.size());
	}
    }

    /* This function checks the graph as a whole for bad cycles */
    public int checkAll(Collection removed) {
	Set nodes=new HashSet();
	nodes.addAll(termination.conjunctions);
	nodes.removeAll(removed);
	GraphNode.computeclosure(nodes,removed);
	Set cycles=GraphNode.findcycles(nodes);
	for(Iterator it=cycles.iterator();it.hasNext();) {
	    GraphNode gn=(GraphNode)it.next();
	    TermNode tn=(TermNode)gn.getOwner();
	    switch(tn.getType()) {
	    case TermNode.UPDATE:
	    case TermNode.CONJUNCTION:
		return ERR_CYCLE;
	    case TermNode.ABSTRACT:
	    case TermNode.RULESCOPE:
	    case TermNode.CONSEQUENCE:
	    default:
		break;
	    }
	}
	return WORKS;
    }

    /* This function checks that
       1) All abstract repairs have a corresponding data structure update
          that isn't removed.
       2) All scope nodes have either a consequence node or a compensation
          node that isn't removed.
     */
    public int checkRepairs(Collection removed) {
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
		for (Iterator edgeit=gn.edges();edgeit.hasNext();) {
		    GraphNode.Edge edge=(GraphNode.Edge)edgeit.next();
		    GraphNode gn2=edge.getTarget();
		    if (!removed.contains(gn2)) {
			TermNode tn2=(TermNode)gn2.getOwner();
			if ((tn2.getType()==TermNode.CONSEQUENCE)||
			    (tn2.getType()==TermNode.UPDATE)) {
			    foundnode=true;
			    break;
			}
		    }
		}
		if (!foundnode) {
		    System.out.println(gn.getTextLabel());
		    return ERR_RULE;
		}
	    } else if (tn.getType()==TermNode.ABSTRACT) {
		boolean foundnode=false;
		for (Iterator edgeit=gn.edges();edgeit.hasNext();) {
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
    /* This method checks that all constraints have conjunction nodes
       and that there are no bad cycles in the abstract portion of the graph.
     */
    public int checkAbstract(Collection removed) {
	Vector constraints=termination.state.vConstraints;
	for(int i=0;i<constraints.size();i++) {
	    Constraint c=(Constraint)constraints.get(i);
	    Set conjunctionset=(Set)termination.conjunctionmap.get(c);

   	    boolean foundrepair=false;
	    for(Iterator it=conjunctionset.iterator();it.hasNext();) {
		GraphNode gn=(GraphNode)it.next();
		if (!removed.contains(gn)) {
		    foundrepair=true;
		    for(Iterator edgeit=gn.edges();edgeit.hasNext();) {
			GraphNode.Edge edge=(GraphNode.Edge) edgeit.next();
			GraphNode gn2=edge.getTarget();
			TermNode tn2=(TermNode) gn2.getOwner();
			if (tn2.getType()==TermNode.ABSTRACT) {
			    /* Abstract node */
			    AbstractRepair ar=tn2.getAbstract();
			    DNFPredicate dpred=ar.getPredicate();
			    Set repairnodes=(Set)termination.predtoabstractmap.get(dpred);
			    if (containsmodify(repairnodes)&&
				(repairnodes.size()>1)) {
				HashSet retainednodes=new HashSet();
				retainednodes.addAll(repairnodes);
				retainednodes.removeAll(removed);
				if (!containsmodify(retainednodes)&&
				    (retainednodes.size()<2))
				    return ERR_NOREPAIR;
			    } else if (removed.contains(gn2))
				return ERR_NOREPAIR;
			}
		    }
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
	    System.out.println("NODE: "+gn.getTextLabel());
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
