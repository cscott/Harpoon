package MCC.IR;

import java.util.*;

public abstract class Expr {
    
    TypeDescriptor td = null;

    public Expr() {}

    public abstract boolean equals(Map remap, Expr e);

    public abstract Set getRequiredDescriptors();

    public abstract void generate(CodeWriter writer, VarDescriptor dest);

    public TypeDescriptor getType() {
        assert td != null : toString();
        return td;
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

    public int[] getRepairs(boolean negated) {
	return new int[0];
    }

    public boolean inverted() {
	return false;
    }

    public boolean usesDescriptor(RelationDescriptor rd) {
	return false;
    }

}
