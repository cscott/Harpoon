package MCC.IR;
import java.util.*;
import java.io.*;
import MCC.State;
import MCC.Compiler;

public class Termination {
    HashSet conjunctions;
    Hashtable conjunctionmap;

    HashSet abstractrepair;
    HashSet abstractrepairadd;

    HashSet updatenodes;
    HashSet consequencenodes;

    HashSet scopenodes;
    Hashtable scopesatisfy;
    Hashtable scopefalsify;
    Hashtable consequence;
    Hashtable abstractadd;
    Hashtable abstractremove;
    Hashtable conjtonodemap;
    Hashtable predtoabstractmap;
    Set removedset;
    ComputeMaxSize maxsize;
    State state;
    AbstractInterferes abstractinterferes;
    ConcreteInterferes concreteinterferes;
    ConstraintDependence constraintdependence;
    ExactSize exactsize;
    ArrayAnalysis arrayanalysis;
    Sources sources;

    public Termination(State state) {
	this.state=state;
	conjunctions=new HashSet();
	conjunctionmap=new Hashtable();
	abstractrepair=new HashSet();
	abstractrepairadd=new HashSet();
	scopenodes=new HashSet();
	scopesatisfy=new Hashtable();
	scopefalsify=new Hashtable();
	consequence=new Hashtable();
	updatenodes=new HashSet();
	consequencenodes=new HashSet();
	abstractadd=new Hashtable();
	abstractremove=new Hashtable();
	conjtonodemap=new Hashtable();
	predtoabstractmap=new Hashtable();
	if (!Compiler.REPAIR)
	    return;
	

	for(int i=0;i<state.vRules.size();i++)
	    System.out.println(state.vRules.get(i));
	for(int i=0;i<state.vConstraints.size();i++)
	    System.out.println(state.vConstraints.get(i));

	sources=new Sources(state);
	maxsize=new ComputeMaxSize(state);
	exactsize=new ExactSize(state);
	arrayanalysis=new ArrayAnalysis(state,this);

	abstractinterferes=new AbstractInterferes(this);
	concreteinterferes=new ConcreteInterferes(this);
	generateconjunctionnodes();
	constraintdependence=new ConstraintDependence(state,this);

	generatescopenodes();
	generaterepairnodes();
	generatedatastructureupdatenodes();
	generatecompensationnodes();

	generateabstractedges();
	generatescopeedges();
	generateupdateedges();


	HashSet superset=new HashSet();
	superset.addAll(conjunctions);
	HashSet closureset=new HashSet();

	GraphNode.computeclosure(superset,closureset);
	try {
	    GraphNode.DOTVisitor.visit(new FileOutputStream("graph.dot"),superset);
	} catch (Exception e) {
	    e.printStackTrace();
	    System.exit(-1);
	}
	for(Iterator it=updatenodes.iterator();it.hasNext();) {
	    GraphNode gn=(GraphNode)it.next();
	    TermNode tn=(TermNode)gn.getOwner();
	    MultUpdateNode mun=tn.getUpdate();
	    System.out.println(gn.getTextLabel());
	    System.out.println(mun.toString());
	}
	GraphAnalysis ga=new GraphAnalysis(this);
	removedset=ga.doAnalysis();
	if (removedset==null) {
	    System.out.println("Can't generate terminating repair algorithm!");
	    System.exit(-1);
	}

	System.out.println("Removing:");
	for(Iterator it=removedset.iterator();it.hasNext();) {
	    GraphNode gn=(GraphNode)it.next();
	    System.out.println(gn.getTextLabel());
	}

	superset=new HashSet();
	superset.addAll(conjunctions);
	superset.removeAll(removedset);
	GraphNode.computeclosure(superset,removedset);
	try {
	    GraphNode.DOTVisitor.visit(new FileOutputStream("graphfinal.dot"),superset);
	} catch (Exception e) {
	    e.printStackTrace();
	    System.exit(-1);
	}
	constraintdependence.traversedependences(this);
    }


    /** This method generates a node for each conjunction in the DNF
     * form of each constraint.  It also converts the quantifiers into
     * conjunctions also - constraints can be satisfied by removing
     * items from the sets and relations they are quantified over */

    void generateconjunctionnodes() {
	Vector constraints=state.vConstraints;
	// Constructs conjunction nodes
	for(int i=0;i<constraints.size();i++) {
	    Constraint c=(Constraint)constraints.get(i);
	    DNFConstraint dnf=c.dnfconstraint;
	    for(int j=0;j<dnf.size();j++) {
		TermNode tn=new TermNode(c,dnf.get(j));
		GraphNode gn=new GraphNode("Conj"+i+"A"+j,
					   "Conj ("+i+","+j+") "+dnf.get(j).name()
					   ,tn);
		conjunctions.add(gn);
		if (!conjunctionmap.containsKey(c))
		    conjunctionmap.put(c,new HashSet());
		((Set)conjunctionmap.get(c)).add(gn);
		conjtonodemap.put(dnf.get(j),gn);
	    }
	    // Construct quantifier "conjunction" nodes
	    for(int j=0;j<c.numQuantifiers();j++) {
		Quantifier q=c.getQuantifier(j);
		if (q instanceof SetQuantifier) {
		    SetQuantifier sq=(SetQuantifier)q;
		    VarExpr ve=new VarExpr(sq.getVar());
		    InclusionPredicate ip=new InclusionPredicate(ve,new SetExpr(sq.getSet()));
		    DNFConstraint dconst=new DNFConstraint(ip);
		    dconst=dconst.not();
		    TermNode tn=new TermNode(c,dconst.get(0));
		    tn.setquantifiernode();
		    GraphNode gn=new GraphNode("Conj"+i+"AQ"+j,
					       "Conj ("+i+","+j+") "+dconst.get(0).name()
					       ,tn);
		    conjunctions.add(gn);
		    if (!conjunctionmap.containsKey(c))
			conjunctionmap.put(c,new HashSet());
		    ((Set)conjunctionmap.get(c)).add(gn);
		    conjtonodemap.put(dconst.get(0),gn);

		} else if (q instanceof RelationQuantifier) {
		    RelationQuantifier rq=(RelationQuantifier)q;
		    VarExpr ve=new VarExpr(rq.y);
		    InclusionPredicate ip=new InclusionPredicate(ve,new ImageSetExpr(rq.x,rq.getRelation()));
		    DNFConstraint dconst=new DNFConstraint(ip);
		    dconst=dconst.not();
		    TermNode tn=new TermNode(c,dconst.get(0));
		    tn.setquantifiernode();
		    GraphNode gn=new GraphNode("Conj"+i+"AQ"+j,
					       "Conj ("+i+","+j+") "+dconst.get(0).name()
					       ,tn);
		    conjunctions.add(gn);
		    if (!conjunctionmap.containsKey(c))
			conjunctionmap.put(c,new HashSet());
		    ((Set)conjunctionmap.get(c)).add(gn);
		    conjtonodemap.put(dconst.get(0),gn);

		}
	    }
	}
    }

    void generateupdateedges() {
	for(Iterator updateiterator=updatenodes.iterator();updateiterator.hasNext();) {
	    GraphNode gn=(GraphNode)updateiterator.next();
	    TermNode tn=(TermNode)gn.getOwner();
	    MultUpdateNode mun=tn.getUpdate();
	    for(int i=0;i<mun.numUpdates();i++) {
		UpdateNode un=mun.getUpdate(i);
		for(int j=0;j<un.numUpdates();j++) {
		    Updates u=un.getUpdate(j);
		    if (u.getType()==Updates.ABSTRACT) {
			Expr e=u.getLeftExpr();
			boolean negated=u.negate;
			if (e instanceof TupleOfExpr) {
			    TupleOfExpr toe=(TupleOfExpr)e;
			    if (negated) {
				GraphNode agn=(GraphNode)abstractremove.get(toe.relation);
				GraphNode.Edge edge=new GraphNode.Edge("requires",agn);
				gn.addEdge(edge);
			    } else {
				GraphNode agn=(GraphNode)abstractadd.get(toe.relation);
				GraphNode.Edge edge=new GraphNode.Edge("requires",agn);
				gn.addEdge(edge);
			    }
			} else if (e instanceof ElementOfExpr) {
			    ElementOfExpr eoe=(ElementOfExpr)e;
			    if (negated) {
				GraphNode agn=(GraphNode)abstractremove.get(eoe.set);
				GraphNode.Edge edge=new GraphNode.Edge("requires",agn);
				gn.addEdge(edge);
			    } else {
				GraphNode agn=(GraphNode)abstractadd.get(eoe.set);
				GraphNode.Edge edge=new GraphNode.Edge("requires",agn);
				gn.addEdge(edge);
			    }
			} else throw new Error("Unrecognized Abstract Update");
		    }
		}
	    }

	    /* Cycle through the rules to look for possible conflicts */
	    for(int i=0;i<state.vRules.size();i++) {
		Rule r=(Rule) state.vRules.get(i);  
		if (concreteinterferes.interferes(mun,r,true)) {
		    GraphNode scopenode=(GraphNode)scopesatisfy.get(r);
		    GraphNode.Edge e=new GraphNode.Edge("interferes",scopenode);
		    gn.addEdge(e);
		}
		if (concreteinterferes.interferes(mun,r,false)) {
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
		Constraint cons=tn2.getConstraint();

		for(int i=0;i<conj.size();i++) {
		    DNFPredicate dp=conj.get(i);
		    System.out.println("Checking "+gn.getTextLabel()+" --> "+gn2.getTextLabel());
		    if (AbstractInterferes.interferes(ar,cons)||
			abstractinterferes.interferes(ar,dp)) {
			GraphNode.Edge e=new GraphNode.Edge("interferes",gn2);
			gn.addEdge(e);
			break;
		    }
		}
	    }

	    for(Iterator scopeiterator=scopenodes.iterator();scopeiterator.hasNext();) {
		GraphNode gn2=(GraphNode)scopeiterator.next();
		TermNode tn2=(TermNode)gn2.getOwner();
		ScopeNode sn2=tn2.getScope();
		if (AbstractInterferes.interferes(ar,sn2.getRule(),sn2.getSatisfy())) {
		    GraphNode.Edge e=new GraphNode.Edge("interferes",gn2);
		    gn.addEdge(e);
		}
	    }
	}
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
		Constraint constr=tn2.getConstraint();
		for(int i=0;i<conj.size();i++) {
		    DNFPredicate dp=conj.get(i);
		    if (abstractinterferes.interferes(sn,dp)||
			AbstractInterferes.interferes(sn.getDescriptor(),sn.getSatisfy(),constr)) {
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

    /** This method generates the abstract repair nodes. */
    void generaterepairnodes() {
	/* Generate repairs of conjunctions */
	for(Iterator conjiterator=conjunctions.iterator();conjiterator.hasNext();) {
	    GraphNode gn=(GraphNode)conjiterator.next();
	    TermNode tn=(TermNode)gn.getOwner();
	    Conjunction conj=tn.getConjunction();
	    for(int i=0;i<conj.size();i++) {
		DNFPredicate dp=conj.get(i);
		int[] array=dp.getPredicate().getRepairs(dp.isNegated(),this);
		Descriptor d=dp.getPredicate().getDescriptor();
		for(int j=0;j<array.length;j++) {
		    AbstractRepair ar=new AbstractRepair(dp,array[j],d);
		    TermNode tn2=new TermNode(ar);
		    GraphNode gn2=new GraphNode(gn.getLabel()+"A"+i+"B"+ar.type(),gn.getTextLabel()+" #"+i+" "+ar.type(),tn2);
		    GraphNode.Edge e=new GraphNode.Edge("abstract",gn2);
		    gn.addEdge(e);
		    if (!predtoabstractmap.containsKey(dp))
			predtoabstractmap.put(dp,new HashSet());
		    ((Set)predtoabstractmap.get(dp)).add(gn2);
		    abstractrepair.add(gn2);
		}
	    }
	}
	/* Generate additional abstract repairs */
	Vector setdescriptors=state.stSets.getAllDescriptors();
	for(int i=0;i<setdescriptors.size();i++) {
	    SetDescriptor sd=(SetDescriptor)setdescriptors.get(i);

	    VarExpr ve=new VarExpr("DUMMY");
	    InclusionPredicate ip=new InclusionPredicate(ve,new SetExpr(sd));
	    DNFPredicate tp=new DNFPredicate(false,ip);
	    AbstractRepair ar=new AbstractRepair(tp, AbstractRepair.ADDTOSET, sd);
	    TermNode tn=new TermNode(ar);
	    GraphNode gn=new GraphNode("AbstractAddSetRule"+i,tn);
	    if (!predtoabstractmap.containsKey(tp))
		predtoabstractmap.put(tp,new HashSet());
	    ((Set)predtoabstractmap.get(tp)).add(gn);
	    abstractrepair.add(gn);
	    abstractrepairadd.add(gn);
	    abstractadd.put(sd,gn);
	    
	    DNFPredicate tp2=new DNFPredicate(true,ip);
	    AbstractRepair ar2=new AbstractRepair(tp2, AbstractRepair.REMOVEFROMSET, sd);
	    TermNode tn2=new TermNode(ar2);
	    GraphNode gn2=new GraphNode("AbstractRemSetRule"+i,tn2);
	    if (!predtoabstractmap.containsKey(tp2))
		predtoabstractmap.put(tp2,new HashSet());
	    ((Set)predtoabstractmap.get(tp2)).add(gn2);
	    abstractrepair.add(gn2);
	    abstractrepairadd.add(gn2);
	    abstractremove.put(sd,gn2);
	}

	Vector relationdescriptors=state.stRelations.getAllDescriptors();
	for(int i=0;i<relationdescriptors.size();i++) {
	    RelationDescriptor rd=(RelationDescriptor)relationdescriptors.get(i);
	    VarDescriptor vd1=new VarDescriptor("DUMMY1");
	    VarExpr ve2=new VarExpr("DUMMY2");

	    InclusionPredicate ip=new InclusionPredicate(ve2,new ImageSetExpr(vd1, rd));
	    
	    DNFPredicate tp=new DNFPredicate(false,ip);
	    AbstractRepair ar=new AbstractRepair(tp, AbstractRepair.ADDTORELATION, rd);
	    TermNode tn=new TermNode(ar);
	    GraphNode gn=new GraphNode("AbstractAddRelRule"+i,tn);
	    if (!predtoabstractmap.containsKey(tp))
		predtoabstractmap.put(tp,new HashSet());
	    ((Set)predtoabstractmap.get(tp)).add(gn);
	    abstractrepair.add(gn);
	    abstractrepairadd.add(gn);
	    abstractadd.put(rd,gn);
	    
	    DNFPredicate tp2=new DNFPredicate(true,ip);
	    AbstractRepair ar2=new AbstractRepair(tp2, AbstractRepair.REMOVEFROMRELATION, rd);
	    TermNode tn2=new TermNode(ar2);
	    GraphNode gn2=new GraphNode("AbstractRemRelRule"+i,tn2);
	    if (!predtoabstractmap.containsKey(tp2))
		predtoabstractmap.put(tp2,new HashSet());
	    ((Set)predtoabstractmap.get(tp2)).add(gn2);
	    abstractrepair.add(gn2);
	    abstractrepairadd.add(gn2);
	    abstractremove.put(rd,gn2);
	}
    }

    int compensationcount=0;
    void generatecompensationnodes() {
	for(int i=0;i<state.vRules.size();i++) {
	    Rule r=(Rule) state.vRules.get(i);
	    Vector possiblerules=new Vector();

	    for(int j=0;j<(r.numQuantifiers()+r.getDNFNegGuardExpr().size());j++) {
		GraphNode gn=(GraphNode)scopesatisfy.get(r);
		TermNode tn=(TermNode) gn.getOwner();
		ScopeNode sn=tn.getScope();
		MultUpdateNode mun=new MultUpdateNode(sn);
		TermNode tn2=new TermNode(mun);
		GraphNode gn2=new GraphNode("CompRem"+compensationcount,tn2);
		UpdateNode un=new UpdateNode(r);

		if (j<r.numQuantifiers()) {
		    /* Remove quantifier */
		    Quantifier q=r.getQuantifier(j);
		    if (q instanceof RelationQuantifier) {
			RelationQuantifier rq=(RelationQuantifier)q;
			TupleOfExpr toe=new TupleOfExpr(new VarExpr(rq.x),new VarExpr(rq.y),rq.relation);
			toe.td=ReservedTypeDescriptor.INT;
			Updates u=new Updates(toe,true);
			un.addUpdate(u);
			if (abstractremove.containsKey(rq.relation)) {
			    GraphNode agn=(GraphNode)abstractremove.get(rq.relation);
			    GraphNode.Edge e=new GraphNode.Edge("requires",agn);
			    gn2.addEdge(e);
			} else {
			    continue; /* Abstract repair doesn't exist */
			}
		    } else if (q instanceof SetQuantifier) {
			SetQuantifier sq=(SetQuantifier)q;
			ElementOfExpr eoe=new ElementOfExpr(new VarExpr(sq.var),sq.set);
			eoe.td=ReservedTypeDescriptor.INT;
			Updates u=new Updates(eoe,true);
			un.addUpdate(u);
			if (abstractremove.containsKey(sq.set)) {
			    GraphNode agn=(GraphNode)abstractremove.get(sq.set);
			    GraphNode.Edge e=new GraphNode.Edge("requires",agn);
			    gn2.addEdge(e);
			} else {
			    continue; /* Abstract repair doesn't exist */
			}
		    } else {
			continue;
		    }
		} else {
		    /* Negate conjunction */
		    int c=j-r.numQuantifiers();
		    if (!processconjunction(un,r.getDNFNegGuardExpr().get(c))) {
			continue;
		    }
		}
		if (!un.checkupdates()) /* Make sure we have a good update */
		    continue;
		
		mun.addUpdate(un);
		GraphNode.Edge e=new GraphNode.Edge("abstract"+compensationcount,gn2);
		compensationcount++;
		gn.addEdge(e);
		updatenodes.add(gn2);
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
		/* Generate remove/add pairs */
		generateremovefromsetrelation(gn,ar);
		generateaddtosetrelation(gn,ar);
		/* Generate atomic modify */
		generatemodifyrelation(gn,ar);
	    }
	}
    }


    /** This method generates concrete data structure updates which
     * remove an object (or tuple) from a set (or relation).*/

    int removefromcount=0;
    void generateremovefromsetrelation(GraphNode gn,AbstractRepair ar) {
	/* Construct the set of all rules which could add something to the given set or relation */

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
	if (possiblerules.size()==0)
	    return;
	
	/* Loop through different ways of falsifying each of these rules */
	int[] count=new int[possiblerules.size()];
	while(remains(count,possiblerules,true)) {
	    MultUpdateNode mun=new MultUpdateNode(ar,MultUpdateNode.REMOVE);
	    TermNode tn=new TermNode(mun);
	    GraphNode gn2=new GraphNode("UpdateRem"+removefromcount,tn);

	    boolean goodflag=true;
	    for(int i=0;i<possiblerules.size();i++) {
		Rule r=(Rule)possiblerules.get(i);
		UpdateNode un=new UpdateNode(r);

		if (count[i]<r.numQuantifiers()) {
		    /* Remove quantifier */
		    Quantifier q=r.getQuantifier(count[i]);
		    if (q instanceof RelationQuantifier) {
			RelationQuantifier rq=(RelationQuantifier)q;
			TupleOfExpr toe=new TupleOfExpr(new VarExpr(rq.x),new VarExpr(rq.y),rq.relation);
			toe.td=ReservedTypeDescriptor.INT;
			Updates u=new Updates(toe,true);
			un.addUpdate(u);
			if (abstractremove.containsKey(rq.relation)) {
			    GraphNode agn=(GraphNode)abstractremove.get(rq.relation);
			    GraphNode.Edge e=new GraphNode.Edge("requires",agn);
			    gn2.addEdge(e);
			} else {
			    goodflag=false;break; /* Abstract repair doesn't exist */
			}
		    } else if (q instanceof SetQuantifier) {
			SetQuantifier sq=(SetQuantifier)q;
			ElementOfExpr eoe=new ElementOfExpr(new VarExpr(sq.var),sq.set);
			eoe.td=ReservedTypeDescriptor.INT;
			Updates u=new Updates(eoe,true);
			un.addUpdate(u);
			if (abstractremove.containsKey(sq.set)) {
			    GraphNode agn=(GraphNode)abstractremove.get(sq.set);
			    GraphNode.Edge e=new GraphNode.Edge("requires",agn);
			    gn2.addEdge(e);
			} else {
			    goodflag=false;break; /* Abstract repair doesn't exist */
			}
		    } else {goodflag=false;break;}
		} else {
		    int c=count[i]-r.numQuantifiers();
		    if (!processconjunction(un,r.getDNFNegGuardExpr().get(c))) {
			goodflag=false;break;
		    }
		}
		if (!un.checkupdates()) {
		    goodflag=false;
		    break;
		}
		mun.addUpdate(un);
	    }
	    if (goodflag) {
		GraphNode.Edge e=new GraphNode.Edge("abstract"+removefromcount,gn2);
		removefromcount++;
		gn.addEdge(e);
		updatenodes.add(gn2);
	    }
	    increment(count,possiblerules,true);
	}
    }

    /** This method increments to the next possibility. */

    static private void increment(int count[], Vector rules,boolean isremove) {
	count[0]++;
	for(int i=0;i<(rules.size()-1);i++) {
	    int num=isremove?(((Rule)rules.get(i)).numQuantifiers()+(((Rule)rules.get(i)).getDNFNegGuardExpr().size())):((Rule)rules.get(i)).getDNFGuardExpr().size();
	    if (count[i]>=num) {
		count[i+1]++;
		count[i]=0;
	    } else break;
	}
    }


    /** This method test if there remain any possibilities to loop
     * through. */
    static private boolean remains(int count[], Vector rules, boolean isremove) {
	for(int i=0;i<rules.size();i++) {
	    int num=isremove?(((Rule)rules.get(i)).numQuantifiers()+(((Rule)rules.get(i)).getDNFNegGuardExpr().size())):((Rule)rules.get(i)).getDNFGuardExpr().size();
	    if (count[i]>=num) {
		return false;
	    }
	}
	return true;
    }

    /** This method generates data structure updates to implement the
     * 	abstract atomic modification specified by ar. */

    int modifycount=0;
    void generatemodifyrelation(GraphNode gn, AbstractRepair ar) {
	RelationDescriptor rd=(RelationDescriptor)ar.getDescriptor();
	ExprPredicate exprPredicate=(ExprPredicate)ar.getPredicate().getPredicate();
	boolean inverted=exprPredicate.inverted();
	int leftindex=0;
	int rightindex=1;
	if (inverted)
	    leftindex=2;
	else 
	    rightindex=2;

	// construct set of possible rules
	Vector possiblerules=new Vector();
	for(int i=0;i<state.vRules.size();i++) {
	    Rule r=(Rule) state.vRules.get(i);
	    if ((r.getInclusion() instanceof RelationInclusion)&&
		(rd==((RelationInclusion)r.getInclusion()).getRelation()))
		possiblerules.add(r);
	}
	if (possiblerules.size()==0)
	    return;

	// increment through this set
	int[] count=new int[possiblerules.size()];
	while(remains(count,possiblerules,false)) {
	    MultUpdateNode mun=new MultUpdateNode(ar,MultUpdateNode.MODIFY);
	    TermNode tn=new TermNode(mun);
	    GraphNode gn2=new GraphNode("UpdateMod"+removefromcount,tn);

	    boolean goodflag=true;
	    for(int i=0;i<possiblerules.size();i++) {
		Rule r=(Rule)possiblerules.get(i);
		UpdateNode un=new UpdateNode(r);
		
		int c=count[i];
		if (!processconjunction(un,r.getDNFGuardExpr().get(c))) {
		    goodflag=false;break;
		}
		RelationInclusion ri=(RelationInclusion)r.getInclusion();
		if (!(ri.getLeftExpr() instanceof VarExpr)) {
		    if (ri.getLeftExpr().isValue()) {
			Updates up=new Updates(ri.getLeftExpr(),leftindex);
			un.addUpdate(up);
		    } else {
			if (inverted)
			    goodflag=false;
			else un.addInvariant(ri.getLeftExpr());
		    }
		} else {
		    VarDescriptor vd=((VarExpr)ri.getLeftExpr()).getVar();
		    if (vd.isGlobal()) {
			Updates up=new Updates(ri.getLeftExpr(),leftindex);
			un.addUpdate(up);
		    } else if (inverted)
			goodflag=false;
		}
		if (!(ri.getRightExpr() instanceof VarExpr)) {
		    if (ri.getRightExpr().isValue()) {
			Updates up=new Updates(ri.getRightExpr(),rightindex);
			un.addUpdate(up);
		    } else {
			if (!inverted)
			    goodflag=false;
			else
			    un.addInvariant(ri.getLeftExpr());
		    }
		} else {
		    VarDescriptor vd=((VarExpr)ri.getRightExpr()).getVar();
		    if (vd.isGlobal()) {
			Updates up=new Updates(ri.getRightExpr(),rightindex);
			un.addUpdate(up);
		    } else if (!inverted) 
			goodflag=false;
		}
				
		if (!un.checkupdates()) {
		    goodflag=false;
		    break;
		}
		mun.addUpdate(un);
	    }
	    if (goodflag) {
		GraphNode.Edge e=new GraphNode.Edge("abstract"+modifycount,gn2);
		modifycount++;
		gn.addEdge(e);
		updatenodes.add(gn2);
	    }
	    increment(count,possiblerules,false);
	}
    }

    /** Generate concrete data structure update to add an object(or
     * tuple) to a set (or relation). */

    static int addtocount=0;
    void generateaddtosetrelation(GraphNode gn, AbstractRepair ar) {
	for(int i=0;i<state.vRules.size();i++) {
	    Rule r=(Rule) state.vRules.get(i);
	    /* See if this is a good rule*/
	    if ((r.getInclusion() instanceof SetInclusion&&
		 ar.getDescriptor()==((SetInclusion)r.getInclusion()).getSet())||
		(r.getInclusion() instanceof RelationInclusion&&
		 ar.getDescriptor()==((RelationInclusion)r.getInclusion()).getRelation())) {
		
		/* First solve for quantifiers */
		Vector bindings=new Vector();
		/* Construct bindings */
		if (!constructbindings(bindings,r,false))
		    continue;
		//Generate add instruction
		DNFRule dnfrule=r.getDNFGuardExpr();
		for(int j=0;j<dnfrule.size();j++) {
		    Inclusion inc=r.getInclusion();
		    UpdateNode un=new UpdateNode(r);
		    un.addBindings(bindings);
		    /* Now build update for tuple/set inclusion condition */
		    if(inc instanceof SetInclusion) {
			SetInclusion si=(SetInclusion)inc;
			if (!(si.elementexpr instanceof VarExpr)) {
			    if (si.elementexpr.isValue()) {
				Updates up=new Updates(si.elementexpr,0);
				un.addUpdate(up);
			    } else {
				/* We're an add to set*/
				System.out.println("Rule: "+r);
				ArrayAnalysis.AccessPath rap=arrayanalysis.analyzeExpr(r,si.elementexpr);
				System.out.println("Attempting perform array add");
				SetDescriptor set=sources.setSource(si.getSet())?
				    sources.getSourceSet(si.getSet()):null;
				if (set==null)
				    continue;
				System.out.println("Non-null source set");
				ArrayAnalysis.AccessPath ap=arrayanalysis.getSet(set);
				if (rap==ArrayAnalysis.AccessPath.NONE)
				    continue;
				System.out.println("A");
				if (!rap.equal(ap))
				    continue;
				System.out.println("B");
				if (!constructarrayupdate(un, si.elementexpr, rap, 0))
				    continue;
				System.out.println("C");
			    }
			} else {
			    VarDescriptor vd=((VarExpr)si.elementexpr).getVar();
			    if (vd.isGlobal()) {
				Updates up=new Updates(si.elementexpr,0);
				un.addUpdate(up);
			    }
			}
		    } else if (inc instanceof RelationInclusion) {
			RelationInclusion ri=(RelationInclusion)inc;
			if (!(ri.getLeftExpr() instanceof VarExpr)) {
			    if (ri.getLeftExpr().isValue()) {
				Updates up=new Updates(ri.getLeftExpr(),0);
				un.addUpdate(up);
			    } else {
				/* We don't handly relation modifies */
				if (ar.getType()==AbstractRepair.MODIFYRELATION)
				    continue;
				/* We're an add to relation*/
				ArrayAnalysis.AccessPath rap=arrayanalysis.analyzeExpr(r,ri.getLeftExpr());
				SetDescriptor set=sources.relsetSource(ri.getRelation(),true /* Domain*/)?
				    sources.relgetSourceSet(ri.getRelation(),true):null;
				if (set==null)
				    continue;
				ArrayAnalysis.AccessPath ap=arrayanalysis.getSet(set);
				
				if (rap==ArrayAnalysis.AccessPath.NONE||
				    !rap.equal(ap)||
				    !constructarrayupdate(un, ri.getLeftExpr(), rap, 0))
				    continue;
			    }
			} else {
			    VarDescriptor vd=((VarExpr)ri.getLeftExpr()).getVar();
			    if (vd.isGlobal()) {
				Updates up=new Updates(ri.getLeftExpr(),0);
				un.addUpdate(up);
			    }
     			}
			if (!(ri.getRightExpr() instanceof VarExpr)) {
			    if (ri.getRightExpr().isValue()) {
				Updates up=new Updates(ri.getRightExpr(),1);
				un.addUpdate(up);
			    } else {
				/* We don't handly relation modifies */
				if (ar.getType()==AbstractRepair.MODIFYRELATION)
				    continue;
				/* We're an add to relation*/
				ArrayAnalysis.AccessPath rap=arrayanalysis.analyzeExpr(r,ri.getRightExpr());
				SetDescriptor set=sources.relsetSource(ri.getRelation(),false /* Range*/)?
				    sources.relgetSourceSet(ri.getRelation(),false):null;
				if (set==null)
				    continue;
				ArrayAnalysis.AccessPath ap=arrayanalysis.getSet(set);
				
				if (rap==ArrayAnalysis.AccessPath.NONE||
				    !rap.equal(ap)||
				    !constructarrayupdate(un, ri.getRightExpr(), rap, 1))
				    continue;
			    }
			} else {
			    VarDescriptor vd=((VarExpr)ri.getRightExpr()).getVar();
			    if (vd.isGlobal()) {
				Updates up=new Updates(ri.getRightExpr(),1);
				un.addUpdate(up);
			    }
			}
		    }
		    //Finally build necessary updates to satisfy conjunction
		    RuleConjunction ruleconj=dnfrule.get(j);

		    /* Add in updates for quantifiers */
		    MultUpdateNode mun=new MultUpdateNode(ar,MultUpdateNode.ADD);
		    TermNode tn=new TermNode(mun);
		    GraphNode gn2=new GraphNode("UpdateAdd"+addtocount,tn);

		    if (processquantifiers(gn2,un, r)&&
			processconjunction(un,ruleconj)&&
			un.checkupdates()) {
			mun.addUpdate(un);
			GraphNode.Edge e=new GraphNode.Edge("abstract"+addtocount,gn2);
			addtocount++;
			gn.addEdge(e);
			updatenodes.add(gn2);
		    }
		}
	    }
	}
    }

    boolean constructarrayupdate(UpdateNode un, Expr lexpr, ArrayAnalysis.AccessPath ap, int slotnumber) {
	System.out.println("Constructing array update");
	Expr e=null;
	for (int i=ap.numFields()-1;i>=0;i--) {
	    if (e==null)
		e=lexpr;
	    else 
		e=((DotExpr)e).getExpr();

	    while (e instanceof CastExpr)
		e=((CastExpr)e).getExpr();

	    DotExpr de=(DotExpr)e;
	    FieldDescriptor fd=ap.getField(i);
	    if (fd instanceof ArrayDescriptor) {
		// We have an ArrayDescriptor!
		Expr index=de.getIndex();
		if (!index.isValue()) {/* Not assignable */
		    System.out.println("ERROR:Index isn't assignable");
		    return false;
		}
		Updates updates=new Updates(index,i,ap,lexpr,slotnumber);
		un.addUpdate(updates);
	    }
	}
	return true;
    }

    /** This method constructs bindings for an update using rule
     * r. The boolean flag isremoval indicates that the update
     * performs a removal.  The function returns true if it is able to
     * generate a valid set of bindings and false otherwise. */

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
		    set=((SetQuantifier)q).getSet();
		} else {
		    vd=((ForQuantifier)q).getVar();
		}
		if(inc instanceof SetInclusion) {
		    SetInclusion si=(SetInclusion)inc;
		    if ((si.elementexpr instanceof VarExpr)&&
			(((VarExpr)si.elementexpr).getVar()==vd)) {
			/* Can solve for v */
			Binding binding=new Binding(vd,0);
			bindings.add(binding);
		    } else {
			goodupdate=false;
		    }
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
			/* Removals don't need bindings anymore
			   Binding binding=new Binding(vd);
			   bindings.add(binding);*/
			goodupdate=true;
		    } else if (q instanceof SetQuantifier) {
			/* Create new element to bind to */
			// search if the set 'set' has a size
			Binding binding=new Binding(vd,set,exactsize.getsize(set)==1);
			bindings.add(binding);
			goodupdate=true;

		    } else
			goodupdate=false;
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
			    /* Removals don't need bindings anymore
			       Binding binding=new Binding(vd);
			       bindings.add(binding);*/
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
    
    /** Adds updates that add an item to the appropriate set or
     * relation quantified over by the model definition rule.. */
    
    boolean processquantifiers(GraphNode gn,UpdateNode un, Rule r) {
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
		if (abstractadd.containsKey(rq.relation)) {
		    GraphNode agn=(GraphNode)abstractadd.get(rq.relation);
		    GraphNode.Edge e=new GraphNode.Edge("requires",agn);
		    gn.addEdge(e);
		} else {
		    return false;
		}
		
	    } else if (q instanceof SetQuantifier) {
		SetQuantifier sq=(SetQuantifier)q;
		if (un.getBinding(sq.var).getType()==Binding.SEARCH) {
		    Binding b=un.getBinding(sq.var);
		    Constraint reqc=exactsize.getConstraint(b.getSet());
		    constraintdependence.requiresConstraint(gn,reqc);
		    continue; /* Don't need to ensure addition for search */
		}

		ElementOfExpr eoe=new ElementOfExpr(new VarExpr(sq.var),sq.set);
		eoe.td=ReservedTypeDescriptor.INT;
		Updates u=new Updates(eoe,false);
		un.addUpdate(u);
		if (abstractadd.containsKey(sq.set)) {
		    GraphNode agn=(GraphNode)abstractadd.get(sq.set);
		    GraphNode.Edge e=new GraphNode.Edge("requires",agn);
		    gn.addEdge(e);
		} else {
		    return false;
		}
	    } else return false;
   	}
	return true;
    }

    /** This method generates the necessary updates to satisfy the
     * conjunction ruleconj. */

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
		throw new Error("Unrecognized Expr");
	    }
	}
	return okay;
    }

    public boolean analyzeQuantifiers(Quantifiers qs,Set set) {
	for(int i=0;i<qs.numQuantifiers();i++) {
	    Quantifier q=qs.getQuantifier(i);
	    if (set.contains(q))
		continue;
	    if (q instanceof SetQuantifier) {
		SetDescriptor sd=((SetQuantifier)q).getSet();
		if (maxsize.getsize(sd)<=1&&
		    maxsize.getsize(sd)>=0)
		    continue;
	    } else if (q instanceof RelationQuantifier) {
		RelationDescriptor rd=((RelationQuantifier)q).getRelation();
		if (maxsize.getsize(rd)<=1&&
		    maxsize.getsize(rd)>=0)
		    continue;
	    }
	    return false;
	}
	return true;
    }

    public boolean mutuallyexclusive(SetDescriptor sd1, SetDescriptor sd2) {
	if (mutualexclusive(sd1,sd2)||
	    mutualexclusive(sd2,sd1))
	    return true;
	else
	    return false;
    }

    private boolean mutualexclusive(SetDescriptor sd1, SetDescriptor sd2) {
	Vector rules=state.vRules;
	for(int i=0;i<rules.size();i++) {
	    Rule r=(Rule)rules.get(i);
	    if (r.getInclusion().getTargetDescriptors().contains(sd1)) {
		/* Rule may add items to sd1 */
		SetInclusion si=(SetInclusion)r.getInclusion();
		Expr ve=si.getExpr();
		DNFRule drule=r.getDNFGuardExpr();
		for(int j=0;j<drule.size();j++) {
		    RuleConjunction rconj=drule.get(j);
		    boolean containsexclusion=false;
		    for (int k=0;k<rconj.size();k++) {
			DNFExpr dexpr=rconj.get(k);
			if (dexpr.getNegation()&&
			    dexpr.getExpr() instanceof ElementOfExpr&&
			    ((ElementOfExpr)dexpr.getExpr()).element.equals(null,ve)) {
			    SetDescriptor sd=((ElementOfExpr)dexpr.getExpr()).set;
			    if (sd.isSubset(sd2))
				containsexclusion=true;
			}
		    }
		    if (!containsexclusion)
			return false;
		}
	    }
	}
	return true;
    }
}
