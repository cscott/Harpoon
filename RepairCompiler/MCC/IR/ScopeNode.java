package MCC.IR;

class ScopeNode {
    Rule rule;
    boolean satisfy;

    public ScopeNode(Rule r,boolean satisfy) {
	rule=r;
	this.satisfy=satisfy;
    }
    public Descriptor getDescriptor() {
	Inclusion inc=rule.getInclusion();
	if (inc instanceof SetInclusion)
	    return ((SetInclusion)inc).getSet();
	else if (inc instanceof RelationInclusion)
	    return ((RelationInclusion)inc).getRelation();
	else throw new Error("Unrecognized Inclusion");
    }
    public boolean getSatisfy() {
	return satisfy;
    }
}
