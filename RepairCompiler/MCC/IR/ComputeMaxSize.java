package MCC.IR;
import MCC.State;
import java.util.*;

/** This class computes the maximum size of sets and relations */

public class ComputeMaxSize {
    State state;
    Hashtable sizemap; /* -1 means infinity */


    public ComputeMaxSize(State state) {
	this.state=state;
	sizemap=new Hashtable();
	computesizes();
    }
    
    /** This method compute relation and set maximum sizes */
    private void computesizes() {
	Vector rules=state.vRules;
	boolean change=true;
	Set descriptorset=new HashSet();
	descriptorset.addAll(state.stSets.getAllDescriptors());
	descriptorset.addAll(state.stRelations.getAllDescriptors());
	while(change) {
	    change=false;
	    for(Iterator dit=descriptorset.iterator();dit.hasNext();) {
		Descriptor d=(Descriptor)dit.next();
		if (d instanceof ReservedSetDescriptor)
		    continue;
		int totalsize=0;
		for(int i=0;i<rules.size();i++) {
		    Rule r=(Rule)rules.get(i);
		    if (r.getInclusion().getTargetDescriptors().contains(d)) {
			/* This rule may add items to this set or relation */
			int rulesize=1;
			for(int j=0;j<r.numQuantifiers();j++) {
			    Quantifier q=r.getQuantifier(j);
			    int size=0;
			    if (q instanceof RelationQuantifier) {
				Descriptor d2=((RelationQuantifier)q).getRelation();
				if (sizemap.containsKey(d2)) {
				    size=((Integer)sizemap.get(d2)).intValue();
				}
				if ((size!=0)&&(d==d2))
				    size=-1;
			    } if (q instanceof SetQuantifier) {
				Descriptor d2=((SetQuantifier)q).getSet();
				if (sizemap.containsKey(d2)) {
				    size=((Integer)sizemap.get(d2)).intValue();
				}
				if ((size!=0)&&(d==d2))
				    size=-1;
			    } else {
				size=-1;
			    }
			    if ((rulesize!=0)&&((size==-1)||(rulesize==-1)))
				rulesize=-1;
			    else
				rulesize=rulesize*size;
			}
			
			if ((rulesize==-1)||(totalsize==-1))
			    totalsize=-1;
			else
			    totalsize+=rulesize;
		    }
		}
		if (!sizemap.containsKey(d)||((Integer)sizemap.get(d)).intValue()!=totalsize) {
		    change=true;
		    sizemap.put(d,new Integer(totalsize));
		}
	    }
   	}
    }
    int getsize(Descriptor d) {
	return ((Integer)sizemap.get(d)).intValue();
    }
}

