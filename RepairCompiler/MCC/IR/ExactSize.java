package MCC.IR;
import java.util.*;
import MCC.State;


class ExactSize {
    State state;
    private Hashtable sizemap;
    private Hashtable constraintmap;

    public ExactSize(State state) {
	this.state=state;
	this.sizemap=new Hashtable();
	this.constraintmap=new Hashtable();
	computesizes();
    }
    
    public int getsize(SetDescriptor sd) {
	if (sizemap.containsKey(sd))
	    return ((Integer)sizemap.get(sd)).intValue();
	else
	    return -1;
    }
    public Constraint getConstraint(SetDescriptor sd) {
	return (Constraint)constraintmap.get(sd);
    }

    private void computesizes() {
	for(Iterator it=state.stSets.descriptors();it.hasNext();) {
	    SetDescriptor sd=(SetDescriptor)it.next();
	    for(int i=0;i<state.vConstraints.size();i++) {
		Constraint c=(Constraint)state.vConstraints.get(i);
		if (c.numQuantifiers()!=0)
		    continue;
		DNFConstraint dconst=c.dnfconstraint;
		int oldsize=-1;
		boolean matches=true;
		for(int j=0;j<dconst.size();j++) {
		    Conjunction conj=dconst.get(j);
		    boolean goodmatch=false;
		    for(int k=0;k<conj.size();k++) {
			DNFPredicate dpred=conj.get(k);
			if (!dpred.isNegated()) {
			    Predicate p=dpred.getPredicate();
			    if (p instanceof ExprPredicate) {
				ExprPredicate ep=(ExprPredicate)p;
				if (ep.getType()==ExprPredicate.SIZE&&
				    ep.getOp()==Opcode.EQ&&
				    ep.getDescriptor()==sd&&
				    ep.isRightInt()) {
				    if (j==0) {
					oldsize=ep.rightSize();
					goodmatch=true;
					break;
				    } else {
					if (oldsize==ep.rightSize()) {
					    goodmatch=true;
					    break;
					}
				    }
				}
			    }
			}
		    }
		    if (!goodmatch) {
			matches=false;
			break; //this constraint won't work
		    }
		}
		if (matches) {
		    System.out.println("Set "+sd.toString()+" has size "+oldsize);
		    sizemap.put(sd,new Integer(oldsize));
		    constraintmap.put(sd,c);
		}
	    }
	}
    }
}
