package MCC.IR;
import java.util.*;

public class DNFRule {
   Vector ruleconjunctions;

    public DNFRule(Expr e) {
	ruleconjunctions=new Vector();
	ruleconjunctions.add(new RuleConjunction(new DNFExpr(true,e)));
    }

    public DNFRule(RuleConjunction conj) {
	ruleconjunctions=new Vector();
	ruleconjunctions.add(conj);
    }

    public DNFRule(Vector conj) {
	ruleconjunctions=conj;
    }
    
    DNFRule() {
	ruleconjunctions=new Vector();
    }

    int size() {
	return ruleconjunctions.size();
    }

    RuleConjunction get(int i) {
	return (RuleConjunction)ruleconjunctions.get(i);
    }

    void add(RuleConjunction c) {
	ruleconjunctions.add(c);
    }

    public DNFRule copy() {
	Vector vector=new Vector();
	for (int i=0;i<size();i++) {
	    vector.add(get(i).copy());
	}
	return new DNFRule(vector);
    }

    public DNFRule and(DNFRule c) {
	DNFRule newdnf=new DNFRule();
	for(int i=0;i<size();i++) {
	    for(int j=0;j<c.size();j++) {
		newdnf.add(get(i).append(c.get(j))); //Cross product
	    }
	}
	return newdnf;
    }

    public DNFRule or(DNFRule c) {
	DNFRule copy=copy();
	for(int i=0;i<c.size();i++) {
	    copy.add(c.get(i).copy()); //Add in other conjunctions
	}
	return copy;
    }

    public DNFRule not() {
	DNFRule copy=copy();
	for (int i=0;i<size();i++) {
	    RuleConjunction conj=get(i);
	    for (int j=0;j<conj.size();j++) {
		DNFExpr dp=conj.get(j);
		dp.negatePred();
	    }
	}
	return copy;
   }
}


