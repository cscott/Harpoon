package MCC.IR;

import java.util.*;

public abstract class Expr {
    
    TypeDescriptor td = null;

    public Expr() {}
    /* Remap this's variables using the remap mapping */
    public abstract boolean equals(Map remap, Expr e);

    public abstract Set getRequiredDescriptors();

    public abstract void generate(CodeWriter writer, VarDescriptor dest);

    public TypeDescriptor getType() {
        assert td != null : toString();
        return td;
    }

    public String name() {
	System.out.println(this.getClass().getName());
	return "?";
    }

    public abstract TypeDescriptor typecheck(SemanticAnalyzer sa);

    public abstract void prettyPrint(PrettyPrinter pp);

    public Set getInversedRelations() {
        throw new IRException("unsupported");
    }

    public DNFRule constructDNF() {
	return new DNFRule(this);
    }

    public Descriptor getDescriptor() {
	return null;
    }

    public boolean isValue() {
	return false;
    }

    public int[] getRepairs(boolean negated, Termination t) {
	System.out.println(this.getClass().getName());
	throw new Error("Unrecognized EXPR");
    }

    public boolean inverted() {
	return false;
    }

    public Set useDescriptor(Descriptor d) {
	return new HashSet();
    }

    public boolean usesDescriptor(Descriptor rd) {
	System.out.println(this.getClass().getName());
	throw new Error("UNIMPLEMENTED");
    }
    public boolean isNull() {
	return false;
    }
    public boolean isNonNull() {
	return false;
    }
    public Set freeVars() {
	return null;
    }

    public void findmatch(Descriptor d, Set s) {
    }

    public Set getfunctions() {
	return null;
    }

    public SetDescriptor getSet() {
	return null; /* unknown value */
    }

    public boolean isSafe() {
	return true;
    }

    public Expr getLower() {
	return null;
    }

    public Expr getUpper() {
	return null;
    }
}
