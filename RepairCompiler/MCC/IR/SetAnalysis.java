package MCC.IR;
import java.util.*;
import MCC.State;

public class SetAnalysis {
    State state;
    Hashtable intersection;
    Hashtable subset;
    Hashtable superset;

    public SetAnalysis(State state) {
	this.state=state;
	intersection=new Hashtable();
	subset=new Hashtable();
	superset=new Hashtable();
	doanalysis();
    }

    public Set getSuperset(SetDescriptor set1) {
	return (Set)superset.get(set1);
    }

    public boolean isSubset(SetDescriptor set1, SetDescriptor set2) {
	return subset.containsKey(set1)&&((Set)subset.get(set1)).contains(set2);
    }

    public boolean noIntersection(SetDescriptor set1, SetDescriptor set2) {
	return intersection.containsKey(set1)&&((Set)intersection.get(set1)).contains(set2);
    }
    
    void doanalysis() {
	SymbolTable sets=state.stSets;
	Vector descriptors=sets.getAllDescriptors();
	for(int i=0;i<descriptors.size();i++) {
	    SetDescriptor sd=(SetDescriptor)descriptors.get(i);
	    Stack st=new Stack();
	    st.addAll(sd.getSubsets());

	    if (!subset.containsKey(sd))
		subset.put(sd,new HashSet());
	    ((HashSet)subset.get(sd)).addAll(sd.getSubsets());
	    for(Iterator it=sd.getSubsets().iterator();it.hasNext();) {
		SetDescriptor sd2=(SetDescriptor)it.next();
		if (!superset.containsKey(sd2))
		    superset.put(sd2,new HashSet());
		((HashSet)superset.get(sd2)).add(sd);
	    }

	    while(!st.empty()) {
		SetDescriptor subsetsd=(SetDescriptor)st.pop();

		st.addAll(subsetsd.getSubsets());
		if (!subset.containsKey(sd))
		    subset.put(sd,new HashSet());
		((HashSet)subset.get(sd)).addAll(subsetsd.getSubsets());
		for(Iterator it=subsetsd.getSubsets().iterator();it.hasNext();) {
		    SetDescriptor sd2=(SetDescriptor)it.next();
		    if (!superset.containsKey(sd2))
			superset.put(sd2,new HashSet());
		    ((HashSet)superset.get(sd2)).add(sd);
		}
	    }
	}
	for(int i=0;i<descriptors.size();i++) {
	    SetDescriptor sd=(SetDescriptor)descriptors.get(i);
	    if (sd.isPartition()) {
		Vector subst=sd.getSubsets();
		for(Iterator it1=subst.iterator();it1.hasNext();) {
		    SetDescriptor sd1=(SetDescriptor)it1.next();
		    for(Iterator it2=subst.iterator();it2.hasNext();) {
			SetDescriptor sd2=(SetDescriptor)it2.next();
			if (sd1!=sd2) {
			    for(Iterator it3=sd1.allSubsets().iterator();it3.hasNext();) {
				SetDescriptor sd3=(SetDescriptor)it3.next();
				
				if (!intersection.containsKey(sd3))
				    intersection.put(sd3,new HashSet());
				((HashSet)intersection.get(sd3)).addAll(sd2.allSubsets());
			    }
			}
		    }
		}
	    }
	}
    }
}
