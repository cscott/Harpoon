package MCC.IR;

import java.util.*;

public abstract class LiteralExpr extends Expr {

    public Set getRequiredDescriptors() {
        return new HashSet();
    }

}
