package MCC.IR;
import java.util.*;
import MCC.State;

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

    private MultUpdateNode getMultUpdateNode(boolean negate, Descriptor d, RepairGenerator rg) {
	Termination termination=rg.termination;
	MultUpdateNode mun=null;
	GraphNode gn;
	if (negate)
	    gn=(GraphNode)termination.abstractremove.get(d);
	else
	    gn=(GraphNode)termination.abstractadd.get(d);
	TermNode tn=(TermNode)gn.getOwner();
	for(Iterator edgeit=gn.edges();edgeit.hasNext();) {
	    GraphNode gn2=((GraphNode.Edge) edgeit.next()).getTarget();
	    if (!rg.removed.contains(gn2)) {
		TermNode tn2=(TermNode)gn2.getOwner();
		if (tn2.getType()==TermNode.UPDATE) {
		    mun=tn2.getUpdate();
		    break;
		}
	    }
	}
	if (mun==null)
	    throw new Error("Can't find update node!");
	return mun;
    }

    public void generate_abstract(CodeWriter cr, boolean removal, String slot0, String slot1, Updates u, RepairGenerator rg) {
	State state=rg.state;
	Expr abstractexpr=u.getLeftExpr();
	boolean negated=u.negate;
	Descriptor d=null;
	Expr left=null;
	Expr right=null;
	boolean istuple=false;
	if (abstractexpr instanceof TupleOfExpr) {
	    TupleOfExpr toe=(TupleOfExpr) abstractexpr;
	    d=toe.relation;
	    left=toe.left;
	    right=toe.right;
	    istuple=true;
	} else if (abstractexpr instanceof TupleOfExpr) {
	    ElementOfExpr eoe=(ElementOfExpr) abstractexpr;
	    d=eoe.set;
	    left=eoe.element;
	    istuple=false;
	} else {
	    throw new Error("Unsupported Expr");
	}
	MultUpdateNode mun=getMultUpdateNode(negated,d,rg);
	VarDescriptor leftvar=VarDescriptor.makeNew("leftvar");
	VarDescriptor rightvar=VarDescriptor.makeNew("rightvar");
	left.generate(cr, leftvar);
	if (istuple)
	    right.generate(cr,rightvar);

	if (negated) {
	    if (istuple) {
		RelationDescriptor rd=(RelationDescriptor)d;
		boolean usageimage=rd.testUsage(RelationDescriptor.IMAGE);
		boolean usageinvimage=rd.testUsage(RelationDescriptor.INVIMAGE);
		if (usageimage)
		    cr.outputline(rg.stmodel+"->"+rd.getJustSafeSymbol() + "_hash->remove((int)" + leftvar.getSafeSymbol() + ", (int)" + rightvar.getSafeSymbol() + ");");
		if (usageinvimage)
		    cr.outputline(rg.stmodel+"->"+rd.getJustSafeSymbol() + "_hashinv->remove((int)" + rightvar.getSafeSymbol() + ", (int)" + leftvar.getSafeSymbol() + ");");
		
		for(int i=0;i<state.vRules.size();i++) {
		    Rule r=(Rule)state.vRules.get(i);
		    if (r.getInclusion().getTargetDescriptors().contains(rd)) {
			for(int j=0;j<mun.numUpdates();j++) {
			    UpdateNode un=mun.getUpdate(i);
			    if (un.getRule()==r) {
				/* Update for rule rule r */
				String name=(String)rg.updatenames.get(un);
				cr.outputline(rg.strepairtable+"->addrelation("+rd.getNum()+","+r.getNum()+","+leftvar.getSafeSymbol()+","+rightvar.getSafeSymbol()+",(int) &"+name+");");
			    }
			}
		    }
		}
	    } else {
		SetDescriptor sd=(SetDescriptor) d;
		cr.outputline(rg.stmodel+"->"+sd.getJustSafeSymbol() + "_hash->remove((int)" + leftvar.getSafeSymbol() + ", (int)" + leftvar.getSafeSymbol() + ");");

		for(int i=0;i<state.vRules.size();i++) {
		    Rule r=(Rule)state.vRules.get(i);
		    if (r.getInclusion().getTargetDescriptors().contains(sd)) {
			for(int j=0;j<mun.numUpdates();j++) {
			    UpdateNode un=mun.getUpdate(i);
			    if (un.getRule()==r) {
				/* Update for rule rule r */
				String name=(String)rg.updatenames.get(un);
				cr.outputline(rg.strepairtable+"->addset("+sd.getNum()+","+r.getNum()+","+leftvar.getSafeSymbol()+",(int) &"+name+");");
			    }
			}
		    }
		}
	    }
	} else {
	    /* Generate update */
	    if (istuple) {
		RelationDescriptor rd=(RelationDescriptor) d;
		boolean usageimage=rd.testUsage(RelationDescriptor.IMAGE);
		boolean usageinvimage=rd.testUsage(RelationDescriptor.INVIMAGE);
		if (usageimage)
		    cr.outputline(rg.stmodel+"->"+rd.getJustSafeSymbol() + "_hash->add((int)" + leftvar.getSafeSymbol() + ", (int)" + rightvar.getSafeSymbol() + ");");
		if (usageinvimage)
		    cr.outputline(rg.stmodel+"->"+rd.getJustSafeSymbol() + "_hashinv->add((int)" + rightvar.getSafeSymbol() + ", (int)" + leftvar.getSafeSymbol() + ");");

		UpdateNode un=mun.getUpdate(0);
		String name=(String)rg.updatenames.get(un);
		cr.outputline(name+"(this,"+rg.stmodel+","+rg.strepairtable+","+leftvar.getSafeSymbol()+","+rightvar.getSafeSymbol()+");");
	    } else {
		SetDescriptor sd=(SetDescriptor)d;
		cr.outputline(rg.stmodel+"->"+sd.getJustSafeSymbol() + "_hash->add((int)" + leftvar.getSafeSymbol() + ", (int)" + leftvar.getSafeSymbol() + ");");

		UpdateNode un=mun.getUpdate(0);
		/* Update for rule rule r */
		String name=(String)rg.updatenames.get(un);
		cr.outputline(name+"(this,"+rg.stmodel+","+rg.strepairtable+","+leftvar.getSafeSymbol()+");");
	    }
	}
	
    }

    public void generate(CodeWriter cr, boolean removal, String slot0, String slot1, RepairGenerator rg) {
	if (!removal)
	    generate_bindings(cr, slot0,slot1);
	for(int i=0;i<updates.size();i++) {
	    Updates u=(Updates)updates.get(i);
	    VarDescriptor right=VarDescriptor.makeNew("right");
	    if (u.getType()==Updates.ABSTRACT) {
		generate_abstract(cr, removal, slot0, slot1, u, rg);
	    }

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
	    cr.outputline("if (!("+left.getSafeSymbol()+op+right.getSafeSymbol()+"))");
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
		Expr subexpr=((DotExpr)u.getLeftExpr()).getExpr();
		Expr intindex=((DotExpr)u.getLeftExpr()).getIndex();
		VarDescriptor subvd=VarDescriptor.makeNew("subexpr");
		VarDescriptor indexvd=VarDescriptor.makeNew("index");
		subexpr.generate(cr,subvd);
		if (intindex!=null)
		    intindex.generate(cr,indexvd);
		FieldDescriptor fd=(FieldDescriptor)u.getDescriptor();
		StructureTypeDescriptor std=(StructureTypeDescriptor)subexpr.getType();
		if (fd instanceof ArrayDescriptor) {
		    fd = ((ArrayDescriptor) fd).getField();
		}

		Expr offsetbits = std.getOffsetExpr(fd);
		if (intindex != null) {
		    Expr basesize = fd.getBaseSizeExpr();
		    offsetbits = new OpExpr(Opcode.ADD, offsetbits, new OpExpr(Opcode.MULT, basesize, intindex));
		}
		Expr offsetbytes = new OpExpr(Opcode.SHR, offsetbits,new IntegerLiteralExpr(3));
		Expr byteaddress=new OpExpr(Opcode.ADD, offsetbytes, subexpr);
		VarDescriptor addr=VarDescriptor.makeNew("byteaddress");
		byteaddress.generate(cr,addr);

		if (fd.getType() instanceof ReservedTypeDescriptor && !fd.getPtr()) {
		    ReservedTypeDescriptor rtd=(ReservedTypeDescriptor)fd.getType();
		    if (rtd==ReservedTypeDescriptor.INT) {
			cr.outputline("*((int *) "+addr.getSafeSymbol()+")="+right.getSafeSymbol()+";");
		    } else if (rtd==ReservedTypeDescriptor.SHORT) {
			cr.outputline("*((short *) "+addr.getSafeSymbol()+")="+right.getSafeSymbol()+";");
		    } else if (rtd==ReservedTypeDescriptor.BYTE) {
			cr.outputline("*((char *) "+addr.getSafeSymbol()+")="+right.getSafeSymbol()+";");
		    } else if (rtd==ReservedTypeDescriptor.BIT) {
			Expr tmp = new OpExpr(Opcode.SHL, offsetbytes, new IntegerLiteralExpr(3));
			Expr offset=new OpExpr(Opcode.SUB, offsetbits, tmp);
			Expr mask=new OpExpr(Opcode.SHR, new IntegerLiteralExpr(1), offset);
			VarDescriptor maskvar=VarDescriptor.makeNew("mask");
			mask.generate(cr,maskvar);
			cr.outputline("*((char *) "+addr.getSafeSymbol()+")|="+maskvar.getSafeSymbol()+";");
			cr.outputline("if (!"+right.getSafeSymbol()+")");
			cr.outputline("*((char *) "+addr.getSafeSymbol()+")^="+maskvar.getSafeSymbol()+";");
		    } else throw new Error();
		} else {
		    /* Pointer */
		    cr.outputline("*((int *) "+addr.getSafeSymbol()+")="+right.getSafeSymbol()+";");
		}
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
