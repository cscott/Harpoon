package MCC.IR;

import java.util.*;

public class InclusionPredicate extends Predicate {

    Expr expr;
    SetExpr setexpr;

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
            
}
    
