package MCC.IR;

import java.util.*;

public abstract class Predicate extends LogicStatement {
    protected Predicate() {}
    public DNFConstraint constructDNF() {
	return new DNFConstraint(this);
    }
    public int[] getRepairs(boolean negated) {}
}
    
