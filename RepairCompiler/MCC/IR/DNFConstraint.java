package MCC.IR;
import java.util.*;

public class DNFConstraint {
   Vector conjunctions;

    public DNFConstraint(Predicate p) {
	conjunctions=new Vector();
	conjunctions.add(new Conjunction(new DNFPredicate(true,p)));
    }

    public DNFConstraint(Conjunction conj) {
	conjunctions=new Vector();
	conjunctions.add(conj);
    }

    public DNFConstraint(Vector conj) {
	conjunctions=conj;
    }
    
    DNFConstraint() {
	conjunctions=new Vector();
    }

    int size() {
	return conjunctions.size();
    }

    Conjunction get(int i) {
	return (Conjunction)conjunctions.get(i);
    }

    void add(Conjunction c) {
	conjunctions.add(c);
    }

    public DNFConstraint copy() {
	Vector vector=new Vector();
	for (int i=0;i<size();i++) {
	    vector.add(get(i).copy());
	}
	return new DNFConstraint(vector);
    }

    public DNFConstraint and(DNFConstraint c) {
	DNFConstraint newdnf=new DNFConstraint();
	for(int i=0;i<size();i++) {
	    for(int j=0;j<c.size();j++) {
		newdnf.add(get(i).append(c.get(j))); //Cross product
	    }
	}
	return newdnf;
    }

    public DNFConstraint or(DNFConstraint c) {
	DNFConstraint copy=copy();
	for(int i=0;i<c.size();i++) {
	    copy.add(c.get(i).copy()); //Add in other conjunctions
	}
	return copy;
    }

    public DNFConstraint not() {
	DNFConstraint copy=copy();
	for (int i=0;i<size();i++) {
	    Conjunction conj=get(i);
	    for (int j=0;j<conj.size();j++) {
		DNFPredicate dp=conj.get(j);
		dp.negatePred();
	    }
	}
	return copy;
   }
}


