package MCC.IR;
import java.util.*;

class Termination {
    HashSet conjunctions;
    Hashtable conjunctionmap;

    HashSet abstractrepair;

    State state;

    public Termination(State state) {
	this.state=state;
	generateconjunctionnodes();
	generaterepairnodes();
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

    void generaterepairnodes() {
	for(Iterator conjiterator=conjunctions.iterator();conjiterator.hasNext();) {
	    GraphNode gn=(GraphNode)conjiterator.next();
	    TermNode tn=(TermNode)gn.getOwner();
	    Conjunction conj=tn.getConjunction();
	    for(int i=0;i<conj.size();i++) {
		DNFPredicate dp=conj.get(i);
		int[] array=dp.getPredicate().getRepairs();
		for {int j=0;j<array.length;j++) {
		    AbstractRepair ar=new AbstractRepair(dp,array[j]);
		    TermNode tn=new TermNode(ar);
		    GraphNode gn2=new GraphNode(gn.getLabel()+"-"+i+","+j,tn);
		    Edge e=new Edge("abstract",gn2);
		    gn.addEdge(e);
		    abstractrepair.add(gn2);
		}
	    }
	}
    }
}

class AbstractRepair {
    public final static int ADDTOSET=1;
    public final static int REMOVEFROMSET=2;
    public final static int ADDTORELATION=3;
    public final static int REMOVEFROMRELATION=4;
    public final static int MODIFYRELATION=5;

    DNFPredicate torepair;
    int type;

    public AbstractRepair(DNFPredicate dp,int typ) {
	torepair=dp;
	type=typ;
    }
}

class TermNode {
    public final static int CONJUNCTION=1;
    public final static int ABSTRACT=2;

    Constraint constr;
    Conjunction conj;
    int type;
    AbstractRepair repair;

    public TermNode(Constraint constr, Conjunction conj) {
	this.constr=constr;
	this.conj=conj;
	type=CONJUNCTION;
    }

    public TermNode(AbstractRepair ar) {
	repair=ar;
	type=ABSTRACT;
    }

    public Conjunction getConjunction() {
	return conj;
    }
}

