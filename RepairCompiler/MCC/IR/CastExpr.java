package MCC.IR;

import java.util.*;

public class CastExpr extends Expr {
    
    TypeDescriptor type;
    Expr expr;

    public CastExpr(TypeDescriptor type, Expr expr) {
        this.type = type;
        this.expr = expr;
    }

    public Set getRequiredDescriptors() {
        return expr.getRequiredDescriptors();
    }

    public void generate(CodeWriter writer, VarDescriptor dest) {
        VarDescriptor vd = VarDescriptor.makeNew("expr");
        expr.generate(writer, vd);
        writer.outputline("int " + dest.getSafeSymbol() + " = (int) " + vd.getSafeSymbol() + ";");
    }

    public void prettyPrint(PrettyPrinter pp) {
        pp.output("cast(" + type.getSafeSymbol() + ", ");
        expr.prettyPrint(pp);
        pp.output(")");
    }

    public TypeDescriptor getType() {
        return type;
    }

    public TypeDescriptor typecheck(SemanticAnalyzer sa) {
        TypeDescriptor td = expr.typecheck(sa);

        if (td == null) {
            return null;
        }

        if (!type.isSubtypeOf(td)) {
            sa.getErrorReporter().report(null, "Expression type '" + td.getSymbol() + "' is not a parent of the cast type '" + type.getSymbol() + "'");
            return null;
        }

        this.td = type;
        return type;
    }

}








