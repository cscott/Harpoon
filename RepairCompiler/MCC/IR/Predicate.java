package MCC.IR;

import java.util.*;

public abstract class Predicate extends LogicStatement {
    protected Predicate() {}
    public DNFConstraint constructDNF() {
	return new DNFConstraint(this);
    }
    abstract public int[] getRepairs(boolean negated, Termination t);
    abstract public Descriptor getDescriptor();
    abstract public boolean inverted();
    public boolean usesDescriptor(RelationDescriptor rd) {
	return false;}
}
    
