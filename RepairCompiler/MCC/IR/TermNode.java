package MCC.IR;

class TermNode {
    public final static int CONJUNCTION=1;
    public final static int ABSTRACT=2;
    public final static int UPDATE=3;
    public final static int RULESCOPE=4;
    

    Constraint constr;
    Conjunction conj;
    int type;
    AbstractRepair repair;
    ScopeNode scope;


    public TermNode(Constraint constr, Conjunction conj) {
	this.constr=constr;
	this.conj=conj;
	type=CONJUNCTION;
    }

    public TermNode(AbstractRepair ar) {
	repair=ar;
	type=ABSTRACT;
    }

    public TermNode(ScopeNode sn) {
	scope=sn;
	type=RULESCOPE;
    }

    public Conjunction getConjunction() {
	if (type!=CONJUNCTION)
	    throw new Error("Not Conjunction Node!");
	else
	    return conj;
    }

    public AbstractRepair getAbstract() {
	if (type!=ABSTRACT)
	    throw new Error("Not Abstract Repair Node!");
	else
	    return repair;
    }
}

