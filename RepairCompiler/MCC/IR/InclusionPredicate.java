package MCC.IR;

import java.util.*;

public class InclusionPredicate extends Predicate {

    Expr expr;
    SetExpr setexpr;

    public boolean inverted() {
	return setexpr.inverted();
    }

    public String name() {
	return expr.name() + " in "+setexpr.name();
    }
    
    public InclusionPredicate(Expr expr, SetExpr setexpr) {
        if (expr == null) {
            throw new NullPointerException();
        }

        if (setexpr == null) {
            throw new NullPointerException();
        }

        this.expr = expr;
        this.setexpr = setexpr;
    }

    public Set getRequiredDescriptors() {
        Set v = expr.getRequiredDescriptors();
        v.addAll(setexpr.getRequiredDescriptors());
        return v;
    }

    public void generate(CodeWriter writer, VarDescriptor dest) {
        VarDescriptor var = VarDescriptor.makeNew("exprval");
        expr.generate(writer, var);
        setexpr.generate_inclusion(writer, dest, var);
    }

    public Set getInversedRelations() {

        throw new IRException("unsupported");

        //Set set = setexpr.getInversedRelations();
        //set.addAll(expr.getInversedRelations());
        //return set;
    }

    public Descriptor getDescriptor() {
	if (setexpr instanceof ImageSetExpr) {
	    return ((ImageSetExpr)setexpr).getRelation();
	} else
	    return setexpr.sd;
    }

    public int[] getRepairs(boolean negated, Termination t) {
	if (setexpr instanceof ImageSetExpr) {
	    if (negated)
		return new int[] {AbstractRepair.REMOVEFROMRELATION};
	    else
		return new int[] {AbstractRepair.ADDTORELATION};
	} else {
	    if (negated)
		return new int[] {AbstractRepair.REMOVEFROMSET};
	    else
		return new int[] {AbstractRepair.ADDTOSET};
	}
    }
}
    

