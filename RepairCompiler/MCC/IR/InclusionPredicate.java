package MCC.IR;

import java.util.*;

public class InclusionPredicate extends Predicate {

    VarDescriptor var;
    SetExpr setexpr;

    public InclusionPredicate(VarDescriptor var, SetExpr setexpr) {
        if (var == null) {
            throw new NullPointerException();
        }

        if (setexpr == null) {
            throw new NullPointerException();
        }

        this.var = var;
        this.setexpr = setexpr;
    }

    public Set getRequiredDescriptors() {
        return setexpr.getRequiredDescriptors();
    }
            
}
    
