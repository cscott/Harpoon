package MCC.IR;
import java.util.*;

class UpdateNode {
    Vector updates;
    Vector bindings;
    Hashtable binding;
    Rule rule;
    

    public UpdateNode(Rule r) {
	updates=new Vector();
	bindings=new Vector();
	binding=new Hashtable();
	rule=r;
    }

    public Rule getRule() {
	return rule;
    }

    public String toString() {
	String st="";
	for(int i=0;i<bindings.size();i++)
	    st+=bindings.get(i).toString()+"\n";
	st+="---------------------\n";
	for(int i=0;i<updates.size();i++)
	    st+=updates.get(i).toString()+"\n";
	return st;
    }

    public void addBindings(Vector v) {
	for (int i=0;i<v.size();i++) {
	    addBinding((Binding)v.get(i));
	}
    }

    public boolean checkupdates() {
	if (!checkconflicts()) /* Do we have conflicting concrete updates */
	    return false;
	if (computeordering()) /* Ordering exists */
	    return true;
	return false;
    }

    private boolean computeordering() {
	/* Build dependency graph between updates */
	HashSet graph=new HashSet();
	Hashtable mapping=new Hashtable();
	for(int i=0;i<updates.size();i++) {
	    Updates u=(Updates)updates.get(i);
	    GraphNode gn=new GraphNode(String.valueOf(i),u);
	    mapping.put(u, gn);
	    graph.add(gn);
	}
	for(int i=0;i<updates.size();i++) {
	    Updates u1=(Updates)updates.get(i);
	    if (u1.isAbstract())
		continue;
	    for(int j=0;j<updates.size();j++) {
		Updates u2=(Updates)updates.get(j);
		if (!u2.isExpr())
		    continue;
		Descriptor d=u1.getDescriptor();
		if (u2.getRightExpr().usesDescriptor(d)) {
		    /* Add edge for dependency */
		    GraphNode gn1=(GraphNode) mapping.get(u1);
		    GraphNode gn2=(GraphNode) mapping.get(u2);
		    GraphNode.Edge e=new GraphNode.Edge("dependency",gn2);
		    gn1.addEdge(e);
		}
	    }
	}

	if (!GraphNode.DFS.depthFirstSearch(graph))  /* DFS & check for acyclicity */
	    return false;

        TreeSet topologicalsort = new TreeSet(new Comparator() {
                public boolean equals(Object obj) { return false; }
                public int compare(Object o1, Object o2) {
                    GraphNode g1 = (GraphNode) o1;
                    GraphNode g2 = (GraphNode) o2;
                    return g2.getFinishingTime() - g1.getFinishingTime();
                }
            });
	topologicalsort.addAll(graph);
	Vector sortedvector=new Vector();
	for(Iterator sort=topologicalsort.iterator();sort.hasNext();) {
	    GraphNode gn=(GraphNode)sort.next();
	    sortedvector.add(gn.getOwner());
	}
	updates=sortedvector; //replace updates with the sorted array
	return true;
    }

    private boolean checkconflicts() {
	Set toremove=new HashSet();
	for(int i=0;i<updates.size();i++) {
	    Updates u1=(Updates)updates.get(i);
	    for(int j=0;j<updates.size();j++) {
		Updates u2=(Updates)updates.get(j);
		if (i==j)
		    continue;
		if (u1.isAbstract()||u2.isAbstract())
		    continue;  /* Abstract updates are already accounted for by graph */
		if (u1.getDescriptor()!=u2.getDescriptor())
		    continue; /* No interference - different descriptors */
		
		if ((u1.getOpcode()==Opcode.GT||u1.getOpcode()==Opcode.GE)&&
		    (u2.getOpcode()==Opcode.GT||u2.getOpcode()==Opcode.GE))
		    continue; /* Can be satisfied simultaneously */

		if ((u1.getOpcode()==Opcode.LT||u1.getOpcode()==Opcode.LE)&&
		    (u2.getOpcode()==Opcode.LT||u2.getOpcode()==Opcode.LE))
		    continue;
		if ((u1.getOpcode()==u2.getOpcode())&&
		    u1.isExpr()&&u2.isExpr()&&
		    u1.getRightExpr().equals(null, u2.getRightExpr())) {
		    /*We'll remove the second occurence*/
		    if (i>j)
			toremove.add(u1);
		    else
			toremove.add(u2);
		    continue;
		}

		/* Handle = or != NULL */
		if ((((u1.getOpcode()==Opcode.EQ)&&(u2.getOpcode()==Opcode.NE))||
		     ((u1.getOpcode()==Opcode.NE)&&(u2.getOpcode()==Opcode.EQ)))&&
		    (((u1.isExpr()&&u1.getRightExpr().isNull())&&(!u2.isExpr()||u2.getRightExpr().isNonNull()))
		     ||((!u1.isExpr()||u1.getRightExpr().isNonNull())&&(u2.isExpr()&&u2.getRightExpr().isNull())))) {
		    if (u1.getOpcode()==Opcode.NE)
			toremove.add(u1);
		    else
			toremove.add(u2);
		    continue;
		}

		/* Handle = and != to different constants */
		if ((((u1.getOpcode()==Opcode.EQ)&&(u2.getOpcode()==Opcode.NE))||
		    ((u1.getOpcode()==Opcode.NE)&&(u2.getOpcode()==Opcode.EQ)))&&
		    (u1.isExpr()&&u1.getRightExpr() instanceof LiteralExpr)&&
		    (u2.isExpr()&&u2.getRightExpr() instanceof LiteralExpr)&&
		    !u1.getRightExpr().equals(u2.getRightExpr())) {
		    if (u1.getOpcode()==Opcode.NE)
			toremove.add(u1);
		    else
			toremove.add(u2);
		    continue;
		}
		
		/* Compatible operations < & <= */
		if (((u1.getOpcode()==Opcode.LT)||(u1.getOpcode()==Opcode.LE))&&
		    ((u2.getOpcode()==Opcode.LT)||(u2.getOpcode()==Opcode.LE)))
		    continue;

		/* Compatible operations > & >= */
		if (((u1.getOpcode()==Opcode.GT)||(u1.getOpcode()==Opcode.GE))&&
		    ((u2.getOpcode()==Opcode.GT)||(u2.getOpcode()==Opcode.GE)))
		    continue;
		/* Ranges */

		//XXXXXX: TODO
		/* Equality & Comparisons */
		//XXXXXX: TODO

		return false; /* They interfere */
	    }
	}
	updates.removeAll(toremove);
	return true;
    }

    public void addBinding(Binding b) {
	bindings.add(b);
	binding.put(b.getVar(),b);
    }

    public Binding getBinding(VarDescriptor vd) {
	if (binding.containsKey(vd))
	    return (Binding)binding.get(vd);
	else
	    return null;
    }

    public void addUpdate(Updates u) {
	updates.add(u);
    }

    public int numUpdates() {
	return updates.size();
    }
    public Updates getUpdate(int i) {
	return (Updates)updates.get(i);
    }
    public void generate(CodeWriter cr, boolean removal, String slot0, String slot1) {
	if (!removal)
	    generate_bindings(cr, slot0,slot1);
	for(int i=0;i<updates.size();i++) {
	    Updates u=(Updates)updates.get(i);
	    VarDescriptor right=VarDescriptor.makeNew("right");
	    if (u.getType()==Updates.ABSTRACT)
		throw new Error("Abstract update not implemented");

	    switch(u.getType()) {
	    case Updates.EXPR:
		u.getRightExpr().generate(cr,right);
		break;
	    case Updates.POSITION:
		if (u.getRightPos()==0)
		    cr.outputline("int "+right.getSafeSymbol()+"="+slot0+";");
		else if (u.getRightPos()==1)
		    cr.outputline("int "+right.getSafeSymbol()+"="+slot1+";");
		else throw new Error("Error w/ Position");
		break;
	    default:
		throw new Error();
	    }
	    VarDescriptor left=VarDescriptor.makeNew("left");
	    u.getLeftExpr().generate(cr,left);
	    Opcode op=u.getOpcode();
	    cr.outputline("if ("+left.getSafeSymbol()+op+right.getSafeSymbol()+")");
	    cr.startblock();

	    if (op==Opcode.GT)
		cr.outputline(right.getSafeSymbol()+"++;");
	    else if (op==Opcode.GE)
		;
	    else if (op==Opcode.EQ)
		;
	    else if (op==Opcode.NE)
		cr.outputline(right.getSafeSymbol()+"++;");
	    else if (op==Opcode.LT)
		cr.outputline(right.getSafeSymbol()+"--;");
	    else if (op==Opcode.LE)
		;
	    else throw new Error();
	    if (u.isGlobal()) {
		VarDescriptor vd=((VarExpr)u.getLeftExpr()).getVar();
		cr.outputline(vd.getSafeSymbol()+"="+right.getSafeSymbol()+";");
	    } else if (u.isField()) {
		/* NEED TO FIX */
	    }
 	    cr.endblock();
	    
	}
    }
    private void generate_bindings(CodeWriter cr, String slot0, String slot1) {
	for(int i=0;i<bindings.size();i++) {
	    Binding b=(Binding)bindings.get(i);
	    if (b.search)
		throw new Error("Search not implemented for bindings");
	    VarDescriptor vd=b.getVar();
	    switch(b.getPosition()) {
	    case 0:
		cr.outputline(vd.getType().getGenerateType().getSafeSymbol()+" "+vd.getSafeSymbol()+"="+slot0+";");
		break;
	    case 1:
		cr.outputline(vd.getType().getGenerateType().getSafeSymbol()+" "+vd.getSafeSymbol()+"="+slot1+";");
		break;
	    default:
		throw new Error("Slot >1 doesn't exist.");
	    }
	}
    }
}
