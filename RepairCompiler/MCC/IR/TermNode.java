package MCC.IR;

class TermNode {
    public final static int CONJUNCTION=1;
    public final static int ABSTRACT=2;
    public final static int UPDATE=3;
    public final static int RULESCOPE=4;
    public final static int CONSEQUENCE=5;

    ConsequenceNode conseqnode;
    Constraint constr;
    Conjunction conj;
    int type;
    AbstractRepair repair;
    ScopeNode scope;
    MultUpdateNode update;

    public int getType() {
	return type;
    }

    public TermNode(Constraint constr, Conjunction conj) {
	this.constr=constr;
	this.conj=conj;
	type=CONJUNCTION;
    }

    public TermNode(AbstractRepair ar) {
	repair=ar;
	type=ABSTRACT;
    }

    public TermNode(ConsequenceNode cn) {
	conseqnode=cn;
	type=CONSEQUENCE;
    }

    public TermNode(ScopeNode sn) {
	scope=sn;
	type=RULESCOPE;
    }

    public TermNode(MultUpdateNode un) {
	update=un;
	type=UPDATE;
    }

    public ConsequenceNode getConsequence() {
	if (type!=CONSEQUENCE)
	    throw new Error("Not Consequence Node!");
	else
	    return conseqnode;
    }

    public Conjunction getConjunction() {
	if (type!=CONJUNCTION)
	    throw new Error("Not Conjunction Node!");
	else
	    return conj;
    }

    public Constraint getConstraint() {
	if (type!=CONJUNCTION)
	    throw new Error("Not Conjunction Node!");
	else
	    return constr;
    }

    public MultUpdateNode getUpdate() {
	if (type!=UPDATE)
	    throw new Error("Not Update Node!");
	else
	    return update;
    }

    public ScopeNode getScope() {
	if (type!=RULESCOPE)
	    throw new Error("Not Scope Node!");
	else
	    return scope;
    }

    public AbstractRepair getAbstract() {
	if (type!=ABSTRACT)
	    throw new Error("Not Abstract Repair Node!");
	else
	    return repair;
    }
}

