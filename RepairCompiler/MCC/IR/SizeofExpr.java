package MCC.IR;

import java.util.*;

public class SizeofExpr extends Expr {

    SetExpr setexpr;

    public SizeofExpr(SetExpr setexpr) {
        if (setexpr == null) {
            throw new NullPointerException();
        }

        this.setexpr = setexpr;
    }

    public Set getRequiredDescriptors() {
        return setexpr.getRequiredDescriptors();
    }

    public void generate(CodeWriter writer, VarDescriptor dest) {
        setexpr.generate_size(writer, dest);
    }

    public void prettyPrint(PrettyPrinter pp) {
        pp.output("sizeof(");
        setexpr.prettyPrint(pp);
        pp.output(")");
    }

    public TypeDescriptor typecheck(SemanticAnalyzer sa) {
        this.td = ReservedTypeDescriptor.INT;
        return this.td;        
    }

        
}
