package MCC.IR;
import java.util.*;
import java.io.*;
import MCC.State;

public class Termination {
    HashSet conjunctions;
    Hashtable conjunctionmap;

    HashSet abstractrepair;
    HashSet updatenodes;
    HashSet consequencenodes;

    HashSet scopenodes;
    Hashtable scopesatisfy;
    Hashtable scopefalsify;
    Hashtable consequence;

    State state;

    public Termination(State state) {
	this.state=state;
	conjunctions=new HashSet();
	conjunctionmap=new Hashtable();
	abstractrepair=new HashSet();
	scopenodes=new HashSet();
	scopesatisfy=new Hashtable();
	scopefalsify=new Hashtable();
	consequence=new Hashtable();
	updatenodes=new HashSet();
	consequencenodes=new HashSet();

	generateconjunctionnodes();
	generatescopenodes();
	generaterepairnodes();
	generatedatastructureupdatenodes();

	generateabstractedges();
	generatescopeedges();
	generateupdateedges();

	HashSet superset=new HashSet();
	superset.addAll(conjunctions);
	superset.addAll(abstractrepair);
	superset.addAll(updatenodes);
	superset.addAll(scopenodes);
	superset.addAll(consequencenodes);
	try {
	    GraphNode.DOTVisitor.visit(new FileOutputStream("graph.dot"),superset);
	} catch (Exception e) {
	    e.printStackTrace();
	    System.exit(-1);
	}
    }
    
    void generateconjunctionnodes() {
	Vector constraints=state.vConstraints;
	for(int i=0;i<constraints.size();i++) {
	    Constraint c=(Constraint)constraints.get(i);
	    DNFConstraint dnf=c.dnfconstraint;
	    for(int j=0;j<dnf.size();j++) {
		TermNode tn=new TermNode(c,dnf.get(j));
		GraphNode gn=new GraphNode("Conj"+i+"A"+j,
					   "Conj ("+i+","+j+") "+dnf.get(j).name()
					   ,tn);
		conjunctions.add(gn);
		conjunctionmap.put(c,gn);
	    }
	}
    }

    void generateupdateedges() {
	for(Iterator updateiterator=updatenodes.iterator();updateiterator.hasNext();) {
	    GraphNode gn=(GraphNode)updateiterator.next();
	    TermNode tn=(TermNode)gn.getOwner();
	    MultUpdateNode mun=tn.getUpdate();
	    /* Cycle through the rules to look for possible conflicts */
	    for(int i=0;i<state.vRules.size();i++) {
		Rule r=(Rule) state.vRules.get(i);  
		if (ConcreteInterferes.interferes(mun,r,true)) {
		    GraphNode scopenode=(GraphNode)scopesatisfy.get(r);
		    GraphNode.Edge e=new GraphNode.Edge("interferes",scopenode);
		    gn.addEdge(e);
		}
		if (ConcreteInterferes.interferes(mun,r,false)) {
		    GraphNode scopenode=(GraphNode)scopefalsify.get(r);
		    GraphNode.Edge e=new GraphNode.Edge("interferes",scopenode);
		    gn.addEdge(e);
		}
	    }
	}
    }

    void generateabstractedges() {
	for(Iterator absiterator=abstractrepair.iterator();absiterator.hasNext();) {
	    GraphNode gn=(GraphNode)absiterator.next();
	    TermNode tn=(TermNode)gn.getOwner();
	    AbstractRepair ar=(AbstractRepair)tn.getAbstract();
	
	    for(Iterator conjiterator=conjunctions.iterator();conjiterator.hasNext();) {
		GraphNode gn2=(GraphNode)conjiterator.next();
		TermNode tn2=(TermNode)gn2.getOwner();
		Conjunction conj=tn2.getConjunction();
		for(int i=0;i<conj.size();i++) {
		    DNFPredicate dp=conj.get(i);
		    if (AbstractInterferes.interferes(ar,dp)) {
			GraphNode.Edge e=new GraphNode.Edge("interferes",gn2);
			gn.addEdge(e);
			break;
		    }
		}
	    }
	}
    }
    
    void generatescopeedges() {
	for(Iterator scopeiterator=scopenodes.iterator();scopeiterator.hasNext();) {
	    GraphNode gn=(GraphNode)scopeiterator.next();
	    TermNode tn=(TermNode)gn.getOwner();
	    ScopeNode sn=tn.getScope();
	    
	    /* Interference edges with conjunctions */
	    for(Iterator conjiterator=conjunctions.iterator();conjiterator.hasNext();) {
		GraphNode gn2=(GraphNode)conjiterator.next();
		TermNode tn2=(TermNode)gn2.getOwner();
		Conjunction conj=tn2.getConjunction();
		for(int i=0;i<conj.size();i++) {
		    DNFPredicate dp=conj.get(i);
		    if (AbstractInterferes.interferes(sn.getDescriptor(),sn.getSatisfy(),dp)) {
			GraphNode.Edge e=new GraphNode.Edge("interferes",gn2);
			GraphNode gnconseq=(GraphNode)consequence.get(sn);
			gnconseq.addEdge(e);
			break;
		    }
		}
	    }

	    /* Now see if this could effect other model defintion rules */
	    for(int i=0;i<state.vRules.size();i++) {
		Rule r=(Rule) state.vRules.get(i);
		if (AbstractInterferes.interferes(sn.getDescriptor(),sn.getSatisfy(),r,true)) {
		    GraphNode scopenode=(GraphNode)scopesatisfy.get(r);
		    GraphNode.Edge e=new GraphNode.Edge("interferes",scopenode);
		    GraphNode gnconseq=(GraphNode)consequence.get(sn);
		    gnconseq.addEdge(e);
		}
		if (AbstractInterferes.interferes(sn.getDescriptor(),sn.getSatisfy(),r,false)) {
		    GraphNode scopenode=(GraphNode)scopefalsify.get(r);
		    GraphNode.Edge e=new GraphNode.Edge("interferes",scopenode);
		    GraphNode gnconseq=(GraphNode)consequence.get(sn);
		    gnconseq.addEdge(e);
		}
	    }
	}
    }


    void generaterepairnodes() {
	for(Iterator conjiterator=conjunctions.iterator();conjiterator.hasNext();) {
	    GraphNode gn=(GraphNode)conjiterator.next();
	    TermNode tn=(TermNode)gn.getOwner();
	    Conjunction conj=tn.getConjunction();
	    for(int i=0;i<conj.size();i++) {
		DNFPredicate dp=conj.get(i);
		int[] array=dp.getPredicate().getRepairs(dp.isNegated());
		Descriptor d=dp.getPredicate().getDescriptor();
		for(int j=0;j<array.length;j++) {
		    AbstractRepair ar=new AbstractRepair(dp,array[j],d);
		    TermNode tn2=new TermNode(ar);
		    GraphNode gn2=new GraphNode(gn.getLabel()+"A"+i+"B"+ar.type(),tn2);
		    GraphNode.Edge e=new GraphNode.Edge("abstract",gn2);
		    gn.addEdge(e);
		    abstractrepair.add(gn2);
		}
	    }
	}
    }

    void generatedatastructureupdatenodes() {
	for(Iterator absiterator=abstractrepair.iterator();absiterator.hasNext();) {
	    GraphNode gn=(GraphNode)absiterator.next();
	    TermNode tn=(TermNode) gn.getOwner();
	    AbstractRepair ar=tn.getAbstract();
	    if (ar.getType()==AbstractRepair.ADDTOSET) {
		generateaddtosetrelation(gn,ar);
	    } else if (ar.getType()==AbstractRepair.REMOVEFROMSET) {
		generateremovefromsetrelation(gn,ar);
	    } else if (ar.getType()==AbstractRepair.ADDTORELATION) {
		generateaddtosetrelation(gn,ar);
	    } else if (ar.getType()==AbstractRepair.REMOVEFROMRELATION) {
		generateremovefromsetrelation(gn,ar);
	    } else if (ar.getType()==AbstractRepair.MODIFYRELATION) {
		generatemodifyrelation(gn,ar);
	    }
	}
    }

    int removefromcount=0;
    void generateremovefromsetrelation(GraphNode gn,AbstractRepair ar) {
	Vector possiblerules=new Vector();
	for(int i=0;i<state.vRules.size();i++) {
	    Rule r=(Rule) state.vRules.get(i);
	    if ((r.getInclusion() instanceof SetInclusion)&&
		(ar.getDescriptor()==((SetInclusion)r.getInclusion()).getSet()))
		possiblerules.add(r);
	    if ((r.getInclusion() instanceof RelationInclusion)&&
		(ar.getDescriptor()==((RelationInclusion)r.getInclusion()).getRelation()))
		possiblerules.add(r);
	}
	int[] count=new int[possiblerules.size()];
	while(remains(count,possiblerules)) {
	    MultUpdateNode mun=new MultUpdateNode(ar);
	    boolean goodflag=true;
	    for(int i=0;i<possiblerules.size();i++) {
		Rule r=(Rule)possiblerules.get(i);
		UpdateNode un=new UpdateNode();
		/* Construct bindings */
		Vector bindings=new Vector();
		constructbindings(bindings, r,true);
		if (count[i]<r.numQuantifiers()) {
		    /* Remove quantifier */
		    Quantifier q=r.getQuantifier(count[i]);
		    if (q instanceof RelationQuantifier) {
			RelationQuantifier rq=(RelationQuantifier)q;
			TupleOfExpr toe=new TupleOfExpr(new VarExpr(rq.x),new VarExpr(rq.y),rq.relation);
			toe.td=ReservedTypeDescriptor.INT;
			Updates u=new Updates(toe,true);
			un.addUpdate(u);
		    } else if (q instanceof SetQuantifier) {
			SetQuantifier sq=(SetQuantifier)q;
			ElementOfExpr eoe=new ElementOfExpr(new VarExpr(sq.var),sq.set);
			eoe.td=ReservedTypeDescriptor.INT;
			Updates u=new Updates(eoe,true);
			un.addUpdate(u);
		    } else {goodflag=false;break;}
		} else {
		    int c=count[i]-r.numQuantifiers();
		    if (!processconjunction(un,r.getDNFNegGuardExpr().get(c))) {
			goodflag=false;break;
		    }
		}
		mun.addUpdate(un);
	    }
	    if (goodflag) {
		TermNode tn=new TermNode(mun);
		GraphNode gn2=new GraphNode("UpdateRem"+removefromcount,tn);
		GraphNode.Edge e=new GraphNode.Edge("abstract"+removefromcount,gn2);
		removefromcount++;
		gn.addEdge(e);
		updatenodes.add(gn2);
	    }
	    increment(count,possiblerules);
	}
    }

    static void increment(int count[], Vector rules) {
	count[0]++;
	for(int i=0;i<(rules.size()-1);i++) {
	    if (count[i]>=(((Rule)rules.get(i)).numQuantifiers()+(((Rule)rules.get(i)).getDNFNegGuardExpr().size()))) {
		count[i+1]++;
		count[i]=0;
	    } else break;
	}
    }

    static boolean remains(int count[], Vector rules) {
	for(int i=0;i<rules.size();i++) {
	    if (count[i]>=(((Rule)rules.get(i)).numQuantifiers()+(((Rule)rules.get(i)).getDNFNegGuardExpr().size()))) {
		return false;
	    }
	}
	return true;
    }

    void generatemodifyrelation(GraphNode gn, AbstractRepair ar) {
    }


    boolean constructbindings(Vector bindings, Rule r, boolean isremoval) {
	boolean goodupdate=true;
	Inclusion inc=r.getInclusion();
	for(Iterator iterator=r.quantifiers();iterator.hasNext();) {
	    Quantifier q=(Quantifier)iterator.next();
	    if ((q instanceof SetQuantifier)||(q instanceof ForQuantifier)) {
		VarDescriptor vd=null;
		SetDescriptor set=null;
		if (q instanceof SetQuantifier) {
		    vd=((SetQuantifier)q).getVar();
		} else
		    vd=((ForQuantifier)q).getVar();
		if(inc instanceof SetInclusion) {
		    SetInclusion si=(SetInclusion)inc;
		    if ((si.elementexpr instanceof VarExpr)&&
			(((VarExpr)si.elementexpr).getVar()==vd)) {
			/* Can solve for v */
			Binding binding=new Binding(vd,0);
			bindings.add(binding);
		    } else
			goodupdate=false;
		} else if (inc instanceof RelationInclusion) {
		    RelationInclusion ri=(RelationInclusion)inc;
		    boolean f1=true;
		    boolean f2=true;
		    if ((ri.getLeftExpr() instanceof VarExpr)&&
			(((VarExpr)ri.getLeftExpr()).getVar()==vd)) {
				/* Can solve for v */
			Binding binding=new Binding(vd,0);
			bindings.add(binding);
		    } else f1=false;
		    if ((ri.getRightExpr() instanceof VarExpr)&&
			(((VarExpr)ri.getRightExpr()).getVar()==vd)) {
				/* Can solve for v */
			Binding binding=new Binding(vd,0);
			bindings.add(binding);
		    } else f2=false;
		    if (!(f1||f2))
			goodupdate=false;
		} else throw new Error("Inclusion not recognized");
		if (!goodupdate)
		    if (isremoval) {
			Binding binding=new Binding(vd);
			bindings.add(binding);
			goodupdate=true;
		    } else
			break;
	    } else if (q instanceof RelationQuantifier) {
		RelationQuantifier rq=(RelationQuantifier)q;
		for(int k=0;k<2;k++) {
		    VarDescriptor vd=(k==0)?rq.x:rq.y;
		    if(inc instanceof SetInclusion) {
			SetInclusion si=(SetInclusion)inc;
			if ((si.elementexpr instanceof VarExpr)&&
			    (((VarExpr)si.elementexpr).getVar()==vd)) {
			    /* Can solve for v */
			    Binding binding=new Binding(vd,0);
			    bindings.add(binding);
			} else
			    goodupdate=false;
		    } else if (inc instanceof RelationInclusion) {
			RelationInclusion ri=(RelationInclusion)inc;
			boolean f1=true;
			boolean f2=true;
			if ((ri.getLeftExpr() instanceof VarExpr)&&
			    (((VarExpr)ri.getLeftExpr()).getVar()==vd)) {
			    /* Can solve for v */
			    Binding binding=new Binding(vd,0);
			    bindings.add(binding);
			} else f1=false;
			if ((ri.getRightExpr() instanceof VarExpr)&&
			    (((VarExpr)ri.getRightExpr()).getVar()==vd)) {
			    /* Can solve for v */
			    Binding binding=new Binding(vd,0);
			    bindings.add(binding);
			} else f2=false;
			if (!(f1||f2))
			    goodupdate=false;
		    } else throw new Error("Inclusion not recognized");
		    if (!goodupdate)
			if (isremoval) {
			    Binding binding=new Binding(vd);
			    bindings.add(binding);
			    goodupdate=true;
			} else
			    break;
		}
		if (!goodupdate)
		    break;
	    } else throw new Error("Quantifier not recognized");
	}
	return goodupdate;
    }

    static int addtocount=0;
    void generateaddtosetrelation(GraphNode gn, AbstractRepair ar) {
	System.out.println("Attempting to generate add to set");
	System.out.println(ar.getPredicate().getPredicate().name());
	System.out.println(ar.getPredicate().isNegated());
	for(int i=0;i<state.vRules.size();i++) {
	    Rule r=(Rule) state.vRules.get(i);
	    /* See if this is a good rule*/
	    System.out.println(r.getGuardExpr().name());
	    if ((r.getInclusion() instanceof SetInclusion&&
		ar.getDescriptor()==((SetInclusion)r.getInclusion()).getSet())||
		(r.getInclusion() instanceof RelationInclusion&&
		 ar.getDescriptor()==((RelationInclusion)r.getInclusion()).getRelation())) {

		/* First solve for quantifiers */
		Vector bindings=new Vector();
		/* Construct bindings */
		System.out.println("Attempting to generate add to set: #2");
		if (!constructbindings(bindings,r,false))
		    continue;
		System.out.println("Attempting to generate add to set: #3");
		//Generate add instruction
		DNFRule dnfrule=r.getDNFGuardExpr();
		for(int j=0;j<dnfrule.size();j++) {
		    Inclusion inc=r.getInclusion();
		    UpdateNode un=new UpdateNode();
		    un.addBindings(bindings);
		    /* Now build update for tuple/set inclusion condition */
		    if(inc instanceof SetInclusion) {
			SetInclusion si=(SetInclusion)inc;
			if (!(si.elementexpr instanceof VarExpr)) {
			    Updates up=new Updates(si.elementexpr,0);
			    un.addUpdate(up);
			} else {
			    VarDescriptor vd=((VarExpr)si.elementexpr).getVar();
			    if (un.getBinding(vd)==null) {
				Updates up=new Updates(si.elementexpr,0);
				un.addUpdate(up);
			    }
			}
		    } else if (inc instanceof RelationInclusion) {
			RelationInclusion ri=(RelationInclusion)inc;
			if (!(ri.getLeftExpr() instanceof VarExpr)) {
			    Updates up=new Updates(ri.getLeftExpr(),0);
			    un.addUpdate(up);
			} else {
			    VarDescriptor vd=((VarExpr)ri.getLeftExpr()).getVar();
			    if (un.getBinding(vd)==null) {
				Updates up=new Updates(ri.getLeftExpr(),0);
				un.addUpdate(up);
			    }
     			}
			if (!(ri.getRightExpr() instanceof VarExpr)) {
			    Updates up=new Updates(ri.getRightExpr(),1);
			    un.addUpdate(up);
			} else {
			    VarDescriptor vd=((VarExpr)ri.getRightExpr()).getVar();
			    if (un.getBinding(vd)==null) {
				Updates up=new Updates(ri.getRightExpr(),1);
				un.addUpdate(up);
			    }
			}
		    }
		    //Finally build necessary updates to satisfy conjunction
		    RuleConjunction ruleconj=dnfrule.get(j);
		    /* Add in updates for quantifiers */
		    System.out.println("Attempting to generate add to set #4");
		    if (processquantifers(un, r)&&debugdd()&&
			processconjunction(un,ruleconj)) {
			System.out.println("Attempting to generate add to set #5");
			MultUpdateNode mun=new MultUpdateNode(ar);
			mun.addUpdate(un);
			TermNode tn=new TermNode(mun);
			GraphNode gn2=new GraphNode("UpdateAdd"+addtocount,tn);
			GraphNode.Edge e=new GraphNode.Edge("abstract"+addtocount,gn2);
			addtocount++;
			gn.addEdge(e);
			updatenodes.add(gn2);}
		}
	    }
	}
    }

    boolean debugdd() {
	System.out.println("Attempting to generate add to set DD");
	return true;
    }

    boolean processquantifers(UpdateNode un, Rule r) {
	boolean goodupdate=true;
	Inclusion inc=r.getInclusion();
	for(Iterator iterator=r.quantifiers();iterator.hasNext();) {
	    Quantifier q=(Quantifier)iterator.next();
	    /* Add quantifier */
	    /* FIXME: Analysis to determine when this update is necessary */
	    if (q instanceof RelationQuantifier) {
		RelationQuantifier rq=(RelationQuantifier)q;
		TupleOfExpr toe=new TupleOfExpr(new VarExpr(rq.x),new VarExpr(rq.y),rq.relation);
		toe.td=ReservedTypeDescriptor.INT;
		Updates u=new Updates(toe,false);
		un.addUpdate(u);
	    } else if (q instanceof SetQuantifier) {
		SetQuantifier sq=(SetQuantifier)q;
		ElementOfExpr eoe=new ElementOfExpr(new VarExpr(sq.var),sq.set);
		eoe.td=ReservedTypeDescriptor.INT;
		Updates u=new Updates(eoe,false);
		un.addUpdate(u);
	    } else {goodupdate=false; break;}
   	}
	return goodupdate;
    }

    boolean  processconjunction(UpdateNode un,RuleConjunction ruleconj){
	boolean okay=true;
	for(int k=0;k<ruleconj.size();k++) {
	    DNFExpr de=ruleconj.get(k);
	    Expr e=de.getExpr();
	    if (e instanceof OpExpr) {
		OpExpr ex=(OpExpr)de.getExpr();
		Opcode op=ex.getOpcode();
		Updates up=new Updates(ex.left,ex.right,op, de.getNegation());
		un.addUpdate(up);
	    } else if (e instanceof ElementOfExpr) {
		Updates up=new Updates(e,de.getNegation());
		un.addUpdate(up);
	    } else if (e instanceof TupleOfExpr) {
		Updates up=new Updates(e,de.getNegation());
		un.addUpdate(up);
	    } else if (e instanceof BooleanLiteralExpr) { 
		boolean truth=((BooleanLiteralExpr)e).getValue();
		if (de.getNegation())
		    truth=!truth;
		if (!truth) {
		    okay=false;
		    break;
		}
	    } else {
		System.out.println(e.getClass().getName());
		throw new Error("Error #213");
	    }
	}
	return okay;
    }

    void generatescopenodes() {
	for(int i=0;i<state.vRules.size();i++) {
	    Rule r=(Rule) state.vRules.get(i);
	    ScopeNode satisfy=new ScopeNode(r,true);
	    TermNode tnsatisfy=new TermNode(satisfy);
	    GraphNode gnsatisfy=new GraphNode("SatisfyRule"+i,tnsatisfy);
	    ConsequenceNode cnsatisfy=new ConsequenceNode();
	    TermNode ctnsatisfy=new TermNode(cnsatisfy);
	    GraphNode cgnsatisfy=new GraphNode("ConseqSatisfyRule"+i,ctnsatisfy);
	    consequence.put(satisfy,cgnsatisfy);
	    GraphNode.Edge esat=new GraphNode.Edge("consequencesatisfy"+i,cgnsatisfy);
	    gnsatisfy.addEdge(esat);
	    consequencenodes.add(cgnsatisfy);
	    scopesatisfy.put(r,gnsatisfy);
	    scopenodes.add(gnsatisfy);

	    ScopeNode falsify=new ScopeNode(r,false);
	    TermNode tnfalsify=new TermNode(falsify);
	    GraphNode gnfalsify=new GraphNode("FalsifyRule"+i,tnfalsify);
	    ConsequenceNode cnfalsify=new ConsequenceNode();
	    TermNode ctnfalsify=new TermNode(cnfalsify);
	    GraphNode cgnfalsify=new GraphNode("ConseqFalsifyRule"+i,ctnfalsify);
	    consequence.put(falsify,cgnfalsify);
	    GraphNode.Edge efals=new GraphNode.Edge("consequencefalsify"+i,cgnfalsify);
	    gnfalsify.addEdge(efals);
	    consequencenodes.add(cgnfalsify);
	    scopefalsify.put(r,gnfalsify);
	    scopenodes.add(gnfalsify);
	}
    }
}
