package MCC.IR;
import MCC.State;
import java.util.*;

public class ConstraintDependence {
    State state;
    HashSet constnodes;
    HashSet nodes;
    Hashtable constonode;
    Hashtable nodetonode;
    Termination termination;

    ConstraintDependence(State state, Termination t) {
	this.state=state;
	this.termination=t;
	constnodes=new HashSet();
	nodes=new HashSet();
	constonode=new Hashtable();
	nodetonode=new Hashtable();
	constructnodes();
	constructconjunctionnodes();
	constructconjunctionedges();
    }
    
    public void addNode(GraphNode gn) {
	GraphNode gn2=new GraphNode(gn.getLabel(),gn.getTextLabel(),gn);
	nodes.add(gn2);
	nodetonode.put(gn,gn2);
    }

    public void associateWithConstraint(GraphNode gn, Constraint c) {
	GraphNode ggn=(GraphNode)nodetonode.get(gn);
	GraphNode gc=(GraphNode)constonode.get(c);
	GraphNode.Edge e=new GraphNode.Edge("associated",ggn);
	gc.addEdge(e);
    }

    public void requiresConstraint(GraphNode gn, Constraint c) {
	GraphNode ggn=(GraphNode)nodetonode.get(gn);
	GraphNode gc=(GraphNode)constonode.get(c);
	GraphNode.Edge e=new GraphNode.Edge("requires",gc);
	ggn.addEdge(e);
    }

    /** Constructs a node for each Constraint */
    private void constructnodes() {
	for(int i=0;i<state.vConstraints.size();i++) {
	    Constraint c=(Constraint)state.vConstraints.get(i);
	    GraphNode gn=new GraphNode(c.toString(),c);
	    constonode.put(c,gn);
	    constnodes.add(gn);
  	}
    }
    
    private void constructconjunctionnodes() {
	for(Iterator it=termination.conjunctions.iterator();it.hasNext();) {
	    GraphNode conjnode=(GraphNode)it.next();
	    TermNode tn=(TermNode)conjnode.getOwner();
	    Conjunction conj=tn.getConjunction();
	    addNode(conjnode);
	}
	for(int i=0;i<state.vConstraints.size();i++) {
	    Constraint c=(Constraint)state.vConstraints.get(i);
	    Set conjset=(Set)termination.conjunctionmap.get(c);
	    for(Iterator it=conjset.iterator();it.hasNext();) {
		GraphNode gn=(GraphNode)it.next();
		associateWithConstraint(gn,c);
	    }
	}
    }

    private void constructconjunctionedges() {
	for(Iterator it=termination.conjunctions.iterator();it.hasNext();) {
	    GraphNode conjnode=(GraphNode)it.next();
	    TermNode tn=(TermNode)conjnode.getOwner();
	    Conjunction conj=tn.getConjunction();
	    for(int i=0;i<conj.size();i++) {
		DNFPredicate dpred=conj.get(i);
		Predicate pred=dpred.getPredicate();
		Expr expr=null;
		if (pred instanceof InclusionPredicate) {
		    InclusionPredicate ip=(InclusionPredicate)pred;
		    expr=ip.expr;
		} else if (pred instanceof ExprPredicate) {
		    ExprPredicate ep=(ExprPredicate)pred;
		    expr=ep.expr;
		} else throw new Error("Unrecognized Predicate");
		Set functions=expr.getfunctions();
		if (functions==null)
		    continue;
		for(Iterator fit=functions.iterator();fit.hasNext();) {
		    Function f=(Function)fit.next();
		    if (rulesensurefunction(f))
			continue; //no constraint needed to ensure

		    Set s=providesfunction(f);
		    if (s.size()==0) {
			System.out.println("Error: No constraint ensures that [forall v in "+f.getSet()+"], size(v."+(f.isInverse()?"~":"")+f.getRelation()+")=1");
			System.exit(-1);
		    }
		    Constraint c=(Constraint)s.iterator().next(); //Take the first one
		    requiresConstraint(conjnode,c);
		}
	    }
	}
    }

    private boolean rulesensurefunction(Function f) {
	boolean foundrule=false;
	for(int i=0;i<state.vRules.size();i++) {
	    Rule r=(Rule)state.vRules.get(i);
	    if (r.getInclusion().getTargetDescriptors().contains(f.getRelation())) {
		RelationInclusion ri=(RelationInclusion)r.getInclusion();
		Expr e=f.isInverse()?ri.getRightExpr():ri.getLeftExpr();
		SetDescriptor sd=e.getSet();
		if (!(sd.isSubset(f.getSet())||f.getSet().isSubset(sd)))
		    continue; /* This rule doesn't effect the function */
		if (foundrule) /* two different rules are a problem */
		    return false;
		if (!((e instanceof VarExpr)&&(r.numQuantifiers()==1)))
		    return false;
		VarExpr ve=(VarExpr)e;
		Quantifier q=r.getQuantifier(0);
		if (!(q instanceof SetQuantifier))
		    return false;
		SetQuantifier sq=(SetQuantifier)q;
		if (ve.getVar()!=sq.getVar())
		    return false;
		if (!sq.getSet().isSubset(f.getSet()))
		    return false;
		if (!((r.getGuardExpr() instanceof BooleanLiteralExpr)&&
		      ((BooleanLiteralExpr)r.getGuardExpr()).getValue()==true))
		    return false;
		Expr e2=f.isInverse()?ri.getLeftExpr():ri.getRightExpr();
		if (e2.isSafe())
		    foundrule=true;
		else
		    return false;
	    }
	}
	return foundrule;
    }
 
    private Set providesfunction(Function f) {
	HashSet set=new HashSet();
	for(int i=0;i<state.vConstraints.size();i++) {
	    Constraint c=(Constraint)state.vConstraints.get(i);
	    boolean goodconstraint=true;
	    DNFConstraint dnfconst=c.dnfconstraint;
	    for(int j=0;j<dnfconst.size();j++) {
		Conjunction conj=dnfconst.get(j);
		boolean conjprovides=false;
		for(int k=0;k<conj.size();k++) {
		    DNFPredicate dpred=conj.get(k);
		    if (!dpred.isNegated()&&
			(dpred.getPredicate() instanceof ExprPredicate)&&
			((ExprPredicate)dpred.getPredicate()).getType()==ExprPredicate.SIZE) {
			ExprPredicate ep=(ExprPredicate)dpred.getPredicate();
			if (ep.isRightInt()&&
			    ep.rightSize()==1&&
			    ep.getOp()==Opcode.EQ&&
			    ep.inverted()==f.isInverse()&&
			    ep.getDescriptor()==f.getRelation()) {
			    ImageSetExpr se=(ImageSetExpr) ((SizeofExpr) ((OpExpr)ep.expr).left).getSetExpr();
			    VarDescriptor vd=se.getVar();
			    if (c.numQuantifiers()==1) {
				for(int l=0;l<c.numQuantifiers();l++) {
				    Quantifier q=c.getQuantifier(l);
				    if (q instanceof SetQuantifier&&
					((SetQuantifier)q).getVar()==vd) {
					SetDescriptor sd=((SetQuantifier)q).getSet();
					if (sd.isSubset(f.getSet()))
					    conjprovides=true;
				    }
				}
			    } else
				break;
			}
		    }
		}
		if (!conjprovides) {
		    goodconstraint=false;
		    break;
		}
	    }
	    if (goodconstraint)
		set.add(c);
	}
	return set;
    }

    public static class Function {
	private RelationDescriptor rd;
	private SetDescriptor sd;
	private boolean inverse;

	public Function(RelationDescriptor r, SetDescriptor sd, boolean inverse) {
	    this.inverse=inverse;
	    this.sd=sd;
	    this.rd=r;
	}

	public SetDescriptor getSet() {
	    return sd;
	}

	public RelationDescriptor getRelation() {
	    return rd;
	}

	public boolean isInverse() {
	    return inverse;
	}
    }

}
