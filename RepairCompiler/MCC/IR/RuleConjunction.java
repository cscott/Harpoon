package MCC.IR;
import java.util.*;

public class RuleConjunction {

    Vector predicates;
    public RuleConjunction(DNFExpr pred) {
	predicates=new Vector();
	predicates.add(pred);
    }
    RuleConjunction(Vector preds){
	predicates=preds       ;
    }

    int size() {
	return predicates.size();
    }
    
    DNFExpr get(int i) {
	return (DNFExpr) predicates.get(i);
    }

    void add(DNFExpr dp) {
	predicates.add(dp);
    }

    public RuleConjunction append(RuleConjunction c) {
	RuleConjunction copy=copy();
	for(int i=0;i<c.size();i++) {
	    copy.add(new DNFExpr(c.get(i)));
	}
	return copy;
    }

    public RuleConjunction copy() {
	Vector vector=new Vector();
	for (int i=0;i<=size();i++) {
	    DNFExpr dp=get(i);
	    vector.add(new DNFExpr(dp));
	}
	return new RuleConjunction(vector);
    }
}
