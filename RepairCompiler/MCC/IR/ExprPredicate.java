package MCC.IR;

import java.util.*;

public class ExprPredicate extends Predicate {
    Expr expr;

    public static final int SIZE=1;
    public static final int COMPARISON=2;

    public TypeDescriptor typecheck(SemanticAnalyzer sa) {
	TypeDescriptor t=expr.typecheck(sa);
	return t;
    }

    public String name() {
	return expr.name();
    }

    public int getType() {
	if (((OpExpr)expr).left instanceof SizeofExpr)
	    return SIZE;
	else if (((OpExpr)expr).left instanceof RelationExpr)
	    return COMPARISON;
	else throw new Error("Unidentifiable Type");
    }

    public Opcode getOp() {
	return ((OpExpr)expr).opcode;
    }

    public int rightSize() {
	assert isRightInt();
	return OpExpr.getInt(((OpExpr)expr).right);
    }

    public boolean isRightInt() {
	return OpExpr.isInt(((OpExpr)expr).right);
    }

    public ExprPredicate(Expr expr) {
        if (expr == null) {
            throw new NullPointerException();
        }
        this.expr = expr;
    }

    public Set getInversedRelations() {
        return expr.getInversedRelations();
    }

    public int[] getRepairs(boolean negated, Termination t) {
	return expr.getRepairs(negated,t);
    }

    public Descriptor getDescriptor() {
	return expr.getDescriptor();
    }

    public boolean inverted() {
	return expr.inverted();
    }

    public boolean usesDescriptor(RelationDescriptor rd) {
	return expr.usesDescriptor(rd);
    }

    public Set getRequiredDescriptors() {
        return expr.getRequiredDescriptors();
    }

    public void generate(CodeWriter writer, VarDescriptor dest) {
        expr.generate(writer, dest);
    }
}
    
