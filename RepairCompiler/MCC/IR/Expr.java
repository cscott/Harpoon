package MCC.IR;

import java.util.*;

public abstract class Expr {
    
    TypeDescriptor td = null;

    public Expr() {}

    public abstract Set getRequiredDescriptors();

    public abstract void generate(CodeWriter writer, VarDescriptor dest);

    public TypeDescriptor getType() {
        assert td != null : toString();
        return td;
    }

    public abstract TypeDescriptor typecheck(SemanticAnalyzer sa);

    public abstract void prettyPrint(PrettyPrinter pp);

}
