package MCC.IR;
import java.util.*;

public class DNFConstraint {
   Vector conjunctions;

    public DNFConstraint(Predicate p) {
	conjunctions=new Vector();
	conjunctions.add(new Conjunction(new DNFPredicate(false,p)));
    }

    public DNFConstraint(DNFPredicate dp) {
	conjunctions=new Vector();
	conjunctions.add(new Conjunction(dp));
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
        DNFConstraint notconst=null;
	for (int i=0;i<size();i++) {
	    Conjunction conj=copy.get(i);
            DNFConstraint newconst=null;
	    for (int j=0;j<conj.size();j++) {
		DNFPredicate dp=conj.get(j);
		dp.negatePred();
                if (newconst==null)
                   newconst=new DNFConstraint(dp);
                else
                   newconst=newconst.or(new DNFConstraint(dp));
	    }
            if (notconst==null)
               notconst=newconst;
            else
               notconst=notconst.and(newconst);
	}
	return notconst;
   }
}
