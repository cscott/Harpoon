package MCC.IR;
import MCC.State;
import java.util.*;

class ModelRuleDependence {
    State state;
    HashSet nodes;
    HashMap ruletonode, nodetorule;
    // Stores references to negated edges
    HashSet negatededges;
    GraphNode.SCC scc;
    HashMap sccCache;

    private final int NODEPENDENCY=0;
    private final int NORMDEPENDENCY=1;
    private final int NEGDEPENDENCY=2;
    
    private ModelRuleDependence(State state) {
	this.state=state;
	this.nodes=new HashSet();
	this.ruletonode=new HashMap();
	this.nodetorule=new HashMap();
	this.negatededges=new HashSet();
	this.sccCache=new HashMap();
    }
    
    public static ModelRuleDependence doAnalysis(State state) {
	ModelRuleDependence mrd=new ModelRuleDependence(state);
	mrd.generateNodes();
	mrd.generateEdges();
	mrd.scc=GraphNode.DFS.computeSCC(mrd.nodes);
	if (mrd.checkForNegatedDependences())
	    throw new Error("Negated Dependence");
	return mrd;
    }

    public int numSCC() {
	return scc.numSCC();
    }

    /** Gives strongly connected components in reverse topological
     * order */
    public Set getSCC(int i) {
	Integer in=new Integer(i);
	if (sccCache.containsKey(in))
	    return (Set) sccCache.get(in);
	Set nodescc=scc.getSCC(i);
	HashSet rulescc=new HashSet();
	for (Iterator nodeit=nodescc.iterator();nodeit.hasNext();) {
	    GraphNode gn=(GraphNode) nodeit.next();
	    Rule r=(Rule)nodetorule.get(gn);
	    rulescc.add(r);
	}
	sccCache.put(in,rulescc);
	return rulescc;
    }

    public boolean hasCycle(Rule r) {
	return hasCycle(getComponent(r));
    }

    public boolean hasCycle(int i) {
	return scc.hasCycle(i);
    }

    public int getComponent(Rule r) {
	return scc.getComponent((GraphNode)ruletonode.get(r));
    }

    /** Returns true if there are any negated dependence cycles, false
     * otherwise. */
    private boolean checkForNegatedDependences() {
	for(Iterator it=negatededges.iterator();it.hasNext();) {
	    GraphNode.Edge e=(GraphNode.Edge)it.next();
	    int scc1=scc.getComponent(e.getSource());
	    int scc2=scc.getComponent(e.getTarget());
	    if (scc1==scc2)
		return true;
	}
	return false;
    }

    /** Build mapping between nodes and rules */
    private void generateNodes() {
	for(int i=0;i<state.vRules.size();i++) {
	    GraphNode gn=new GraphNode("Rule"+i);
	    ruletonode.put(state.vRules.get(i),gn);
	    nodetorule.put(gn,state.vRules.get(i));
	    nodes.add(gn);
	}
    }
    
    /** Generate edges between rule nodes */
    private void generateEdges() {
	for(int i=0;i<state.vRules.size();i++) {
	    for(int j=0;j<state.vRules.size();j++) {
		Rule r1=(Rule)state.vRules.get(i);
		Rule r2=(Rule)state.vRules.get(j);
		generateEdge(r1,r2);
	    }
	}
    }

    private void generateEdge(Rule r1,Rule r2) {
	Descriptor d=(Descriptor) r1.getInclusion().getTargetDescriptors().iterator().next();
	int dep=checkBody(d,r2.getDNFGuardExpr());
	if (dep==NODEPENDENCY) {
	    SetDescriptor bsd=null;
	    if (d instanceof SetDescriptor) {
		SetInclusion si=(SetInclusion)r1.getInclusion();
		if (si.getExpr() instanceof VarExpr) {
		    VarDescriptor vd=((VarExpr)si.getExpr()).getVar();
		    bsd=vd.getSet();
		}
	    }
	    dep=checkQuantifiers(bsd,d,r2);
	}
	if (dep==NODEPENDENCY)
	    return;
	GraphNode gn1=(GraphNode) ruletonode.get(r1);
	GraphNode gn2=(GraphNode) ruletonode.get(r2);
	GraphNode.Edge edge=new GraphNode.Edge("dependency",gn2);
	gn1.addEdge(edge);
	if (dep==NEGDEPENDENCY)
	    negatededges.add(edge);
    }

    private int checkBody(Descriptor d, DNFRule drule) {
	boolean dependency=false;
	for(int i=0;i<drule.size();i++) {
	    RuleConjunction rconj=drule.get(i);
	    for(int j=0;j<rconj.size();j++){ 
		DNFExpr dexpr=rconj.get(j);
		Expr e=dexpr.getExpr();
		if (e.usesDescriptor(d)) {
		    boolean negated=dexpr.getNegation();
		    if (negated)
			return NEGDEPENDENCY;
		    else
			dependency=true;
		}
	    }
	}
	if (dependency)
	    return NORMDEPENDENCY;
	else 
	    return NODEPENDENCY;
    }

    private int checkQuantifiers(SetDescriptor bsd, Descriptor d, Quantifiers qs) {
	for (int i=0;i<qs.numQuantifiers();i++) {
	    Quantifier q=qs.getQuantifier(i);
	    if (q instanceof SetQuantifier&&
		d instanceof SetDescriptor) {
		SetQuantifier sq=(SetQuantifier)q;
		SetDescriptor sd=(SetDescriptor)d;
		if (sq.getSet().isSubset(sd)&&
		    ((bsd==null)||!bsd.isSubset(sq.getSet())))
		    return NORMDEPENDENCY;
	    } else if (q.getRequiredDescriptors().contains(d))
		return NORMDEPENDENCY;
	}
	return NODEPENDENCY;
    }
}
