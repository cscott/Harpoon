package MCC.IR;

import java.util.*;

public class ExprPredicate extends Predicate {

    Expr expr;

    public ExprPredicate(Expr expr) {
        if (expr == null) {
            throw new NullPointerException();
        }

        this.expr = expr;
    }

    public Set getInversedRelations() {
        return expr.getInversedRelations();
    }

    public Set getRequiredDescriptors() {
        return expr.getRequiredDescriptors();
    }

    public void generate(CodeWriter writer, VarDescriptor dest) {
        expr.generate(writer, dest);
    }
            
}
    
