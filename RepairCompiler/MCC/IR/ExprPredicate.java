package MCC.IR;

import java.util.*;

public class ExprPredicate extends Predicate {
    
    Expr expr;


    public static final int SIZE=1;
    public static final int COMPARISON=2;

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

    public int leftsize() {
	return ((IntegerLiteralExpr)((OpExpr)expr).right).getValue();
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

    public int[] getRepairs(boolean negated) {
	return expr.getRepairs(negated);
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
    
