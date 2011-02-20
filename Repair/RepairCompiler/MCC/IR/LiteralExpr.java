package MCC.IR;

import java.util.*;

public abstract class LiteralExpr extends Expr {

    public Set getRequiredDescriptors() {
        return new HashSet();
    }

    public Set getInversedRelations() {
        return new HashSet();
    }

    public boolean isInvariant(Set vars) {
	return true;
    }

    public Set findInvariants(Set vars) {
	return new HashSet(); /* We won't lift literals...gcc can do this */
    }
}
