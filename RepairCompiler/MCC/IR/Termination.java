package MCC.IR;
import java.util.*;
import MCC.State;

public class Termination {
    HashSet conjunctions;
    Hashtable conjunctionmap;

    HashSet abstractrepair;

    HashSet scopenodes;
    Hashtable scopesatisfy;
    Hashtable scopefalsify;

    State state;

    public Termination(State state) {
	this.state=state;
	conjunctions=new HashSet();
	conjunctionmap=new Hashtable();
	abstractrepair=new HashSet();
	scopenodes=new HashSet();
	scopesatisfy=new Hashtable();
	scopefalsify=new Hashtable();

	generateconjunctionnodes();
	generaterepairnodes();
	generateabstractedges();
	generatedatastructureupdatenodes();
	generatescopenodes();
    }
    
    void generateconjunctionnodes() {
	Vector constraints=state.vConstraints;
	for(int i=0;i<constraints.size();i++) {
	    Constraint c=(Constraint)constraints.get(i);
	    DNFConstraint dnf=c.dnfconstraint;
	    for(int j=0;j<dnf.size();j++) {
		TermNode tn=new TermNode(c,dnf.get(j));
		GraphNode gn=new GraphNode("Conjunction"+i+","+j,tn);
		conjunctions.add(gn);
		conjunctionmap.put(c,gn);
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
		    GraphNode gn2=new GraphNode(gn.getLabel()+"-"+i+","+j,tn2);
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
		generateaddtoset(ar);
	    } else if (ar.getType()==AbstractRepair.REMOVEFROMSET) {
		generateremovefromset(ar);
	    } else if (ar.getType()==AbstractRepair.ADDTORELATION) {
		generateaddtorelation(ar);
	    } else if (ar.getType()==AbstractRepair.REMOVEFROMRELATION) {
		generateremovefromrelation(ar);
	    } else if (ar.getType()==AbstractRepair.MODIFYRELATION) {
		generatemodifyrelation(ar);
	    }
	}
    }

    void generateaddtoset(AbstractRepair ar) {
	for(int i=0;i<state.vRules.size();i++) {
	    Rule r=(Rule) state.vRules.get(i);
	    if (r.getInclusion() instanceof SetInclusion) {
		if (ar.getDescriptor()==((SetInclusion)r.getInclusion()).getSet()) {
		    //Generate add instruction
		    

		}
	    }
	}
    }

    void generatescopenodes() {
	for(int i=0;i<state.vRules.size();i++) {
	    Rule r=(Rule) state.vRules.get(i);
	    ScopeNode satisfy=new ScopeNode(r,true);
	    TermNode tnsatisfy=new TermNode(satisfy);
	    GraphNode gnsatisfy=new GraphNode("Satisfy Rule"+i,tnsatisfy);

	    ScopeNode falsify=new ScopeNode(r,false);
	    TermNode tnfalsify=new TermNode(falsify);
	    GraphNode gnfalsify=new GraphNode("Falsify Rule"+i,tnfalsify);
	    scopesatisfy.put(r,gnsatisfy);
	    scopefalsify.put(r,gnfalsify);
	    scopenodes.add(gnsatisfy);
	    scopenodes.add(gnfalsify);
	}
    }
}
