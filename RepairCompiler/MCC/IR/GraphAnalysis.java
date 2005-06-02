package MCC.IR;
import java.util.*;
import java.io.*;
import MCC.State;
import MCC.Compiler;

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

    private boolean safetransclosure(GraphNode gn, Set removed, Set cantremove, Set couldremove) {
	Stack workset=new Stack();
	HashSet closureset=new HashSet();
	boolean needcyclecheck=false;
	HashSet dependent=new HashSet();

	/* Compute dependent set */
	workset.push(gn);
	while(!workset.empty()) {
	    GraphNode gn2=(GraphNode)workset.pop();
	    for(Iterator edgeit=gn2.edges();edgeit.hasNext();) {
		GraphNode gn3=((GraphNode.Edge)edgeit.next()).getTarget();
		if (removed.contains(gn3))
		    continue;
		if (!termination.conjunctions.contains(gn3)&&!dependent.contains(gn3)) {
		    dependent.add(gn3);
		    workset.push(gn3);
		}
	    }
	}

	/* Compute the closure set */
	workset.push(gn);
	while(!workset.empty()) {
	    GraphNode gn2=(GraphNode)workset.pop();
	    if (!closureset.contains(gn2)) {
		closureset.add(gn2);
		for(Iterator edgeit=gn2.edges();edgeit.hasNext();) {
		    GraphNode gn3=((GraphNode.Edge)edgeit.next()).getTarget();
		    if (removed.contains(gn3))
			continue;
		    workset.push(gn3);
		}
	    }
	}

	/* Check for harmful cycles through gn */
	Set cycles=GraphNode.findcycles(closureset);
	if (cycles.contains(gn))
	    return false;

	/* Check for harmful cycles being introduced in dependent nodes */
	cycles=GraphNode.findcycles(dependent);
	for(Iterator it=cycles.iterator();it.hasNext();) {
	    GraphNode gn2=(GraphNode)it.next();
	    if (termination.abstractrepair.contains(gn2)||
		termination.conjunctions.contains(gn2)||
		termination.updatenodes.contains(gn2))
		return false;
	}
	
	/* Make sure all abstractrepairs/consequence nodes in the dependent nodes
	   are well formed. */
    outerloop:
	for(Iterator it=dependent.iterator();it.hasNext();) {
	    GraphNode gn2=(GraphNode)it.next();
	    if (termination.abstractrepair.contains(gn2)||
		termination.scopenodes.contains(gn2)) {
		boolean ismodify=false;
		int numadd=0;
		int numremove=0;
		
		if (termination.abstractrepair.contains(gn2)&&
		    ((TermNode)gn2.getOwner()).getAbstract().getType()==AbstractRepair.MODIFYRELATION)
		    ismodify=true;
   
		innerloop:
		for(Iterator edgeit=gn2.edges();edgeit.hasNext();) {
		    GraphNode gn3=((GraphNode.Edge)edgeit.next()).getTarget();
		    if (removed.contains(gn3))
			continue innerloop;
		    if (cantremove.contains(gn3)||
			!couldremove.contains(gn3)) {
			if (ismodify) {
			    TermNode tn3=(TermNode)gn3.getOwner();
			    MultUpdateNode mun=tn3.getUpdate();
			    if (mun.getType()==MultUpdateNode.ADD)
				numadd++;
			    if (mun.getType()==MultUpdateNode.REMOVE)
				numremove++;
			    if (mun.getType()==MultUpdateNode.MODIFY)
				continue outerloop;
			    if ((numadd>0)&&(numremove>0||!((TermNode)gn2.getOwner()).getAbstract().needsRemoves(termination.state)))
				continue outerloop;
			} else
			    if (termination.consequence.contains(gn3)||
				termination.updatenodes.contains(gn3))
				continue outerloop;
		    }
		}
		return false;
	    }
	}
	return true;
    }

    public Set doAnalysis() {
	HashSet cantremove=new HashSet();
	HashSet mustremove=new HashSet();

	cantremove.addAll(termination.scopenodes);
	cantremove.addAll(termination.abstractrepair);

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
	    couldremove.retainAll(nodes);


	    /* Check for consequence nodes which are fine */

	    for(Iterator it=termination.consequencenodes.iterator();it.hasNext();) {
		GraphNode gn=(GraphNode) it.next();
		if (safetransclosure(gn, mustremove,cantremove, couldremove)) {
		    couldremove.remove(gn);
		}
	    }

	    /* Check for update nodes which are fine */

	    for(Iterator it=termination.updatenodes.iterator();it.hasNext();) {
		GraphNode gn=(GraphNode) it.next();
		if (safetransclosure(gn, mustremove,cantremove, couldremove)) {
		    couldremove.remove(gn);
		}
	    }

	    /* Check for conjunction nodes which are fine */

	    for(Iterator it=termination.conjunctions.iterator();it.hasNext();) {
		GraphNode gn=(GraphNode) it.next();
		if (mustremove.contains(gn)||cantremove.contains(gn))
		    continue;
		if (!safetransclosure(gn, mustremove,cantremove, couldremove))
                    continue;

		boolean allgood=true;
		for(Iterator edgeit=gn.edges();edgeit.hasNext();) {
		    GraphNode gn2=((GraphNode.Edge)edgeit.next()).getTarget();
		    TermNode tn2=(TermNode)gn2.getOwner();
		    assert tn2.getType()==TermNode.ABSTRACT;
		    boolean foundupdate=false;
		    for(Iterator edgeit2=gn2.edges();edgeit2.hasNext();) {
			GraphNode gn3=((GraphNode.Edge)edgeit2.next()).getTarget();
			if (!couldremove.contains(gn3)&&!mustremove.contains(gn3)) {
			    TermNode tn3=(TermNode)gn3.getOwner();
			    if (tn3.getType()==TermNode.UPDATE)
				foundupdate=true;
			}
		    }
		    if (!foundupdate)
			allgood=false;
		}
		if (allgood) {
		    couldremove.remove(gn);
		    if (Compiler.PRUNEQUANTIFIERS) {
			TermNode tn=(TermNode)gn.getOwner();
			Constraint constr=tn.getConstraint();
			for(Iterator consit=((Set)termination.conjunctionmap.get(constr)).iterator();consit.hasNext();) {
			    GraphNode gn4=(GraphNode)consit.next();
			    TermNode tn4=(TermNode)gn4.getOwner();
			    if (tn4.getquantifiernode()) {
				mustremove.add(gn4); /* This node is history */
				System.out.println("Eliminating: "+gn4.getTextLabel());
			    }
			}
		    }
		}
	    }


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


	    /* Search through conjunction nodes which must be
	       satisfied, and see if there are any data structure
	       updates that must exist. */

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
			if (nodes.contains(gn2)&&
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
			    if (updateset.size()==1)
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

	    if (Compiler.AGGRESSIVESEARCH) {
		/* Aggressively remove compensation nodes */
		for(Iterator scopeit=termination.scopenodes.iterator();scopeit.hasNext();) {
		    GraphNode gn=(GraphNode)scopeit.next();
		    HashSet tmpset=new HashSet();
		    boolean doremove=false;
		    for(Iterator edgeit=gn.edges();edgeit.hasNext();) {
			GraphNode.Edge e=(GraphNode.Edge)edgeit.next();
			GraphNode gn2=e.getTarget();
			if (termination.consequencenodes.contains(gn2)) {
			    if (((!couldremove.contains(gn2))||cantremove.contains(gn2))&&
				!mustremove.contains(gn2)) {
				doremove=true;
			    } else
				break;
			} else tmpset.add(gn2);
		    }
		    if (doremove)
			mustremove.addAll(tmpset);
		}
	    }

	    Set cycles2=GraphNode.findcycles(cantremove);
	    for(Iterator it=cycles2.iterator();it.hasNext();) {
		GraphNode gn=(GraphNode)it.next();
		if (termination.conjunctions.contains(gn)) {
		    try {
			GraphNode.DOTVisitor.visit(new FileOutputStream("graphdebug.dot"),cantremove);
		    } catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		    }

		    System.out.println("Cycle through conjunction "+gn.getTextLabel() +" which can't be removed.");
		    System.out.println("CANTREMOVE");
		    for(Iterator it2=cantremove.iterator();it2.hasNext();) {
			GraphNode gn2=(GraphNode)it2.next();
			System.out.println(gn2.getTextLabel());
		    }
		    System.out.println("MUSTREMOVE");
		    for(Iterator it2=mustremove.iterator();it2.hasNext();) {
			GraphNode gn2=(GraphNode)it2.next();
			System.out.println(gn2.getTextLabel());
		    }
		    return null; // Out of luck
		}
	    }

	    /* Search through abstract repair actions & correspond data structure updates */
	    for(Iterator it=termination.abstractrepairadd.iterator();it.hasNext();) {
		GraphNode gn=(GraphNode)it.next();
		TermNode tn=(TermNode)gn.getOwner();

		for (Iterator edgeit=gn.edges();edgeit.hasNext();) {
		    GraphNode.Edge e=(GraphNode.Edge)edgeit.next();
		    GraphNode gn2=e.getTarget();
		    TermNode tn2=(TermNode)gn2.getOwner();
		    if (tn2.getType()!=TermNode.UPDATE)
			continue;

		    boolean containsgn=cantremove.contains(gn);
		    boolean containsgn2=cantremove.contains(gn2);

		    cantremove.add(gn);
		    cantremove.add(gn2);

		    Set cycle=GraphNode.findcycles(cantremove);
		    if (cycle.contains(gn2)) {
			if (!mustremove.contains(gn2)) {
			    change=true;
			    mustremove.add(gn2);
			}
		    }
		    if (!containsgn)
			cantremove.remove(gn);
		    if (!containsgn2)
			cantremove.remove(gn2);
		}
	    }

	    /* Searches individual conjunctions + abstract action +updates for cycles */
	    for(Iterator it=termination.conjunctions.iterator();it.hasNext();) {
		GraphNode gn=(GraphNode)it.next();
		boolean foundnocycle=false;
		
		for (Iterator edgeit=gn.edges();edgeit.hasNext();) {
		    GraphNode.Edge e=(GraphNode.Edge)edgeit.next();
		    GraphNode gn2=e.getTarget();
		    TermNode tn2=(TermNode)gn2.getOwner();
		    if (tn2.getType()!=TermNode.ABSTRACT)
			continue;
		    AbstractRepair ar=tn2.getAbstract();
		    boolean ismodify=ar.getType()==AbstractRepair.MODIFYRELATION;
		    int numadd=0;int numremove=0;
		    
		    for (Iterator edgeit2=gn2.edges();edgeit2.hasNext();) {
			GraphNode.Edge e2=(GraphNode.Edge)edgeit2.next();
			GraphNode gn3=e2.getTarget();
			TermNode tn3=(TermNode)gn3.getOwner();
			if (tn3.getType()!=TermNode.UPDATE)
			    continue;
			
			boolean containsgn=cantremove.contains(gn);
			boolean containsgn2=cantremove.contains(gn2);
			boolean containsgn3=cantremove.contains(gn3);
			cantremove.add(gn);
			cantremove.add(gn2);
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
			    if (ismodify) {
				MultUpdateNode mun=tn3.getUpdate();
				if (mun.getType()==MultUpdateNode.ADD)
				    numadd++;
				if (mun.getType()==MultUpdateNode.REMOVE)
				    numremove++;
			    }
			}
			if (!containsgn)
			    cantremove.remove(gn);
			if (!containsgn2)
			    cantremove.remove(gn2);
			if (!containsgn3)
			    cantremove.remove(gn3);
		    }
		    if (ismodify&&((numadd==0)||(numremove==0&&ar.needsRemoves(termination.state)))) {
			for (Iterator edgeit2=gn2.edges();edgeit2.hasNext();) {
			    GraphNode.Edge e2=(GraphNode.Edge)edgeit2.next();
			    GraphNode gn3=e2.getTarget();
			    TermNode tn3=(TermNode)gn3.getOwner();
			    if (tn3.getType()!=TermNode.UPDATE)
				continue;
			    MultUpdateNode mun=tn3.getUpdate();
			    if (((mun.getType()==MultUpdateNode.ADD)||
				(mun.getType()==MultUpdateNode.REMOVE))&&
				(!mustremove.contains(gn3)))
				mustremove.add(gn3);
			}
		    }
		}

		if(!foundnocycle) {
		    if (!mustremove.contains(gn)) {
			change=true;
			mustremove.add(gn);
		    }
		}
	    }

	    /* Searches scope nodes + compensation nodes */
	    for(Iterator it=termination.scopenodes.iterator();it.hasNext();) {
		GraphNode gn=(GraphNode)it.next();
		if (nodes.contains(gn)) {
    		    for (Iterator edgeit=gn.edges();edgeit.hasNext();) {
			GraphNode.Edge e=(GraphNode.Edge)edgeit.next();
			GraphNode gn2=e.getTarget();
			TermNode tn2=(TermNode)gn2.getOwner();

			if (tn2.getType()!=TermNode.UPDATE)
			    continue;
			/* We have a compensation node */
			boolean containsgn=cantremove.contains(gn);
			boolean containsgn2=cantremove.contains(gn2);
			cantremove.add(gn);
			cantremove.add(gn2);

			Set cycle=GraphNode.findcycles(cantremove);
			if (cycle.contains(gn2)) {
			    if (!mustremove.contains(gn2)) {
				change=true;
				mustremove.add(gn2);
			    }
			} 
			if (!containsgn)
			    cantremove.remove(gn);
			if (!containsgn2)
			    cantremove.remove(gn2);
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

	    try {
		GraphNode.DOTVisitor.visit(new FileOutputStream("graphsearch.dot"),nodes,couldremove);
	    } catch (Exception e) {
		e.printStackTrace();
		System.exit(-1);
	    }

	    try {
		GraphNode.DOTVisitor.visit(new FileOutputStream("graphcycle.dot"),GraphNode.findcycles(nodes),couldremove);
	    } catch (Exception e) {
		e.printStackTrace();
		System.exit(-1);
	    }

	    System.out.println("Searching set of "+couldremove.size()+" nodes.");
	    System.out.println("Eliminated must "+mustremove.size()+" nodes");
	    System.out.println("Eliminated cant "+cantremove.size()+" nodes");
	    System.out.println("Searching following set:");
	    for(Iterator it=couldremove.iterator();it.hasNext();) {
		GraphNode gn=(GraphNode)it.next();
		System.out.println(gn.getTextLabel());
	    }
	    System.out.println("Must remove set:");
	    for(Iterator it=mustremove.iterator();it.hasNext();) {
		GraphNode gn=(GraphNode)it.next();
		System.out.println(gn.getTextLabel());
	    }
	    System.out.println("Cant remove set:");
	    for(Iterator it=cantremove.iterator();it.hasNext();) {
		GraphNode gn=(GraphNode)it.next();
		System.out.println(gn.getTextLabel());
	    }

	    System.out.println("==================================================");
	    while(true) {
		if (illegal(combination,couldremovevector))
		    break;
		Set combinationset=buildset(combination,couldremovevector);
                System.out.println("---------------------------");
                for(Iterator it=combinationset.iterator();it.hasNext();) {
                    System.out.println(((GraphNode)it.next()).getTextLabel());
                }
                System.out.println("---------------------------");
		checkmodify(combinationset);
		combinationset.addAll(mustremove);
		if (combinationset!=null) {
                    int checkabstract=checkAbstract(combinationset);
                    int checkrep=checkRepairs(combinationset);
                    int checkall=checkAll(combinationset);

		    System.out.println("Checkabstract="+checkabstract);
		    System.out.println("Checkrepairs="+checkrep);
		    System.out.println("Checkall="+checkall);

		    if (checkabstract==0&&
			checkrep==0&&
			checkall==0) {
			return combinationset;
		    }
		}
		increment(combination,couldremovevector);
	    }
	    System.out.println("Search failed!");
	    return null;
	}
    }

    private void checkmodify(Set removednodes) {
	for (Iterator it=termination.abstractrepair.iterator();it.hasNext();) {
	    GraphNode gn=(GraphNode)it.next();
	    TermNode tn=(TermNode)gn.getOwner();
	    AbstractRepair ar=tn.getAbstract();

	    /* Has MODIFYRELATION */
	    if (ar.getType()==AbstractRepair.MODIFYRELATION) {
		int numadd=0;
		int numremove=0;
		for(Iterator it2=gn.edges();it2.hasNext();) {
		    GraphNode.Edge edge=(GraphNode.Edge)it2.next();
		    GraphNode gn2=edge.getTarget();
		    TermNode tn2=(TermNode)gn2.getOwner();
		    if (!removednodes.contains(gn2)&&
			tn2.getType()==TermNode.UPDATE) {
			MultUpdateNode mun=tn2.getUpdate();

			if (mun.getType()==MultUpdateNode.ADD)
			    numadd++;
			if (mun.getType()==MultUpdateNode.REMOVE)
			    numremove++;
		    }
		}
		if ((numadd==0)||(numremove==0&&ar.needsRemoves(termination.state))) {
		    for(Iterator it2=gn.edges();it2.hasNext();) {
			GraphNode.Edge edge=(GraphNode.Edge)it2.next();
			GraphNode gn2=edge.getTarget();
			TermNode tn2=(TermNode)gn2.getOwner();
			if (!removednodes.contains(gn2)&&
			    tn2.getType()==TermNode.UPDATE) {
			    MultUpdateNode mun=tn2.getUpdate();
			    if ((mun.getType()==MultUpdateNode.ADD)
				||(mun.getType()==MultUpdateNode.REMOVE)) {
				removednodes.add(gn2);
			    }
			}
		    }
		}
	    }
    	}
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
	    System.out.println("Expanding to: "+combination.size());
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
