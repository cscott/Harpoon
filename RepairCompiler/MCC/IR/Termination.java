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
		GraphNode gn=new GraphNode("Conjunction"+i+"B"+j,tn);
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
		    GraphNode gnconseq=(GraphNode)consequence.get(sn);
		    gnconseq.addEdge(e);
		}
		if (ConcreteInterferes.interferes(mun,r,false)) {
		    GraphNode scopenode=(GraphNode)scopefalsify.get(r);
		    GraphNode.Edge e=new GraphNode.Edge("interferes",scopenode);
		    GraphNode gnconseq=(GraphNode)consequence.get(sn);
		    gnconseq.addEdge(e);
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
		    GraphNode gn2=new GraphNode(gn.getLabel()+"A"+i+"B"+j,tn2);
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
		generateaddtoset(gn,ar);
	    } else if (ar.getType()==AbstractRepair.REMOVEFROMSET) {
		generateremovefromset(gn,ar);
	    } else if (ar.getType()==AbstractRepair.ADDTORELATION) {
		generateaddtorelation(gn,ar);
	    } else if (ar.getType()==AbstractRepair.REMOVEFROMRELATION) {
		generateremovefromrelation(gn,ar);
	    } else if (ar.getType()==AbstractRepair.MODIFYRELATION) {
		generatemodifyrelation(gn,ar);
	    }
	}
    }

    void generateremovefromset(GraphNode gn,AbstractRepair ar) {
	Vector possiblerules=new Vector();
	for(int i=0;i<state.vRules.size();i++) {
	    Rule r=(Rule) state.vRules.get(i);
	    if ((r.getInclusion() instanceof SetInclusion)&&
		(ar.getDescriptor()==((SetInclusion)r.getInclusion()).getSet()))
		possiblerules.add(r);
	}
	int[] count=new int[possiblerules.size()];
	while(remains(count,possiblerules)) {
	    MultUpdateNode mun=new MultUpdateNode(ar);
	    for(int i=0;i<possiblerules.size();i++) {
		UpdateNode un=new UpdateNode();
		mun.addUpdate(un);
		/* CODE HERE*/
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

    void generateremovefromrelation(GraphNode gn,AbstractRepair ar) {}
    void generateaddtorelation(GraphNode gn,AbstractRepair ar) {}
    void generatemodifyrelation(GraphNode gn, AbstractRepair ar) {}

    static int addtocount=0;
    void generateaddtoset(GraphNode gn, AbstractRepair ar) {
	for(int i=0;i<state.vRules.size();i++) {
	    Rule r=(Rule) state.vRules.get(i);
	    if (r.getInclusion() instanceof SetInclusion) {
		if (ar.getDescriptor()==((SetInclusion)r.getInclusion()).getSet()) {
		    //Generate add instruction
		    DNFRule dnfrule=r.getDNFGuardExpr();
		    for(int j=0;j<dnfrule.size();j++) {
			Inclusion inc=r.getInclusion();
			UpdateNode un=new UpdateNode();
			/* First solve for quantifiers */
			boolean goodupdate=true;
			for(Iterator iterator=r.quantifiers();iterator.hasNext();) {
			    Quantifier q=(Quantifier)iterator.next();
			    boolean foundall=true;
			    if ((q instanceof SetQuantifier)||(q instanceof ForQuantifier)) {
				VarDescriptor vd=null;
				if (q instanceof SetQuantifier)
				    vd=((SetQuantifier)q).getVar();
				else
				    vd=((ForQuantifier)q).getVar();
				if(inc instanceof SetInclusion) {
				    SetInclusion si=(SetInclusion)inc;
				    if ((si.elementexpr instanceof VarExpr)&&
					(((VarExpr)si.elementexpr).getVar()==vd)) {
					/* Can solve for v */
					Binding binding=new Binding(vd,0);
					un.addBinding(binding);
				    } else
					foundall=false;
				} else if (inc instanceof RelationInclusion) {
				    RelationInclusion ri=(RelationInclusion)inc;
				    boolean f1=true;
				    boolean f2=true;
				    if ((ri.getLeftExpr() instanceof VarExpr)&&
					(((VarExpr)ri.getLeftExpr()).getVar()==vd)) {
					/* Can solve for v */
					Binding binding=new Binding(vd,0);
					un.addBinding(binding);
				    } else f1=false;
				    if ((ri.getRightExpr() instanceof VarExpr)&&
					(((VarExpr)ri.getRightExpr()).getVar()==vd)) {
					/* Can solve for v */
					Binding binding=new Binding(vd,0);
					un.addBinding(binding);
				    } else f2=false;
				    if (!(f1||f2))
					foundall=false;
				} else throw new Error("Inclusion not recognized");
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
					    un.addBinding(binding);
					} else
					    foundall=false;
				    } else if (inc instanceof RelationInclusion) {
					RelationInclusion ri=(RelationInclusion)inc;
					boolean f1=true;
					boolean f2=true;
					if ((ri.getLeftExpr() instanceof VarExpr)&&
					    (((VarExpr)ri.getLeftExpr()).getVar()==vd)) {
					    /* Can solve for v */
					    Binding binding=new Binding(vd,0);
					    un.addBinding(binding);
					} else f1=false;
					if ((ri.getRightExpr() instanceof VarExpr)&&
						   (((VarExpr)ri.getRightExpr()).getVar()==vd)) {
					    /* Can solve for v */
					    Binding binding=new Binding(vd,0);
					    un.addBinding(binding);
					} else f2=false;
					if (!(f1||f2))
					    foundall=false;
				    } else throw new Error("Inclusion not recognized");
				}
			    } else throw new Error("Quantifier not recognized");
			    if (!foundall) {
				goodupdate=false;
				break;
			    }
			    /* Now build update for tuple/set inclusion condition */
			    if(inc instanceof SetInclusion) {
				SetInclusion si=(SetInclusion)inc;
				if (!(si.elementexpr instanceof VarExpr)) {
				    Updates up=new Updates(si.elementexpr,0);
				    un.addUpdate(up);
				}
			    } 
			    if (inc instanceof RelationInclusion) {
				RelationInclusion ri=(RelationInclusion)inc;
				if (!(ri.getLeftExpr() instanceof VarExpr)) {
				    Updates up=new Updates(ri.getLeftExpr(),0);
				    un.addUpdate(up);
				}
				if (!(ri.getRightExpr() instanceof VarExpr)) {
				    Updates up=new Updates(ri.getRightExpr(),0);
				    un.addUpdate(up);
				}
			    }
			    //Finally build necessary updates to satisfy conjunction
			    RuleConjunction ruleconj=dnfrule.get(j);
			    for(int k=0;k<ruleconj.size();k++) {
				DNFExpr de=ruleconj.get(k);
				Expr e=de.getExpr();
				if (e instanceof OpExpr) {
				    OpExpr ex=(OpExpr)de.getExpr();
				    Opcode op=ex.getOpcode();
				    if (de.getNegation()) {
					/* remove negation through opcode translation */
					if (op==Opcode.GT)
					    op=Opcode.LE;
					else if (op==Opcode.GE)
					    op=Opcode.LT;
					else if (op==Opcode.EQ)
					    op=Opcode.NE;
					else if (op==Opcode.NE)
					    op=Opcode.EQ;
					else if (op==Opcode.LT)
					    op=Opcode.GE;
					else if (op==Opcode.LE)
					    op=Opcode.GT;
				    }
				    Updates up=new Updates(ex.left,ex.right,op);
				    un.addUpdate(up);
				} else if (e instanceof ElementOfExpr) {
				    Updates up=new Updates(e,de.getNegation());
				    un.addUpdate(up);
				} else if (e instanceof TupleOfExpr) {
				    Updates up=new Updates(e,de.getNegation());
				    un.addUpdate(up);
				} else throw new Error("Error #213");
			    }
			}
			MultUpdateNode mun=new MultUpdateNode(ar);
			mun.addUpdate(un);
			TermNode tn=new TermNode(mun);
			GraphNode gn2=new GraphNode("Update"+addtocount,tn);
			GraphNode.Edge e=new GraphNode.Edge("abstract"+addtocount,gn2);
			addtocount++;
			gn.addEdge(e);
			updatenodes.add(gn2);
		    }
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
}
