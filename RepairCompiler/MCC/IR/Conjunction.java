package MCC.IR;
import java.util.*;

public class Conjunction {

    Vector predicates;
    public Conjunction(DNFPredicate pred) {
	predicates=new Vector();
	predicates.add(pred);
    }
    Conjunction(Vector preds){
	predicates=preds       ;
    }

    int size() {
	return predicates.size();
    }
    
    DNFPredicate get(int i) {
	return (DNFPredicate) predicates.get(i);
    }

    void add(DNFPredicate dp) {
	predicates.add(dp);
    }

    public Conjunction append(Conjunction c) {
	Conjunction copy=copy();
	for(int i=0;i<c.size();i++) {
	    copy.add(new DNFPredicate(c.get(i)));
	}
	return copy;
    }

    public Conjunction copy() {
	Vector vector=new Vector();
	for (int i=0;i<size();i++) {
	    DNFPredicate dp=get(i);
	    vector.add(new DNFPredicate(dp));
	}
	return new Conjunction(vector);
    }
}
