package MCC.IR;

import java.util.*;

public class TupleOfExpr extends Expr {

    Expr left = null;
    Expr right = null;
    RelationDescriptor relation = null;

    public TupleOfExpr(Expr left, Expr right, RelationDescriptor relation) {
        if ((left == null) || (right == null) || (relation == null)) {
            throw new NullPointerException();
        }
        
        this.left = left;
        this.right = right;
        this.relation = relation;
    }

    public Set getRequiredDescriptors() {
        Set v = left.getRequiredDescriptors();
        v.addAll(right.getRequiredDescriptors());
        v.add(relation);
        return v;
    }

    public void generate(CodeWriter writer, VarDescriptor dest) {
        VarDescriptor ld = VarDescriptor.makeNew();
        left.generate(writer, ld);

        VarDescriptor rd = VarDescriptor.makeNew();
        right.generate(writer, rd);

        writer.outputline("int " + dest.getSafeSymbol() + " = " + 
                          relation.getSafeSymbol() + "_hash->get(" + ld.getSafeSymbol() + 
                          ") == " + rd.getSafeSymbol() + ";");
    }

    public void prettyPrint(PrettyPrinter pp) {
        pp.output("<");
        left.prettyPrint(pp);
        pp.output(", ");
        right.prettyPrint(pp);
        pp.output("> in? " + relation.getSafeSymbol());
    }

    public TypeDescriptor typecheck(SemanticAnalyzer sa) {
        TypeDescriptor ld = left.typecheck(sa);
        TypeDescriptor rd = right.typecheck(sa);
        
        if (ld == null || rd == null) {
            return null;
        }

        boolean ok = true;

        if (ld != relation.getDomain().getType()) {
            sa.getErrorReporter().report(null, "Type of left element '" + ld.getSymbol() + "' must match domain type '" + relation.getDomain().getType().getSymbol() + "'");
            ok = false;
        }

        if (rd != relation.getRange().getType()) {
            sa.getErrorReporter().report(null, "Type of right element '" + rd.getSymbol() + "' must match range type '" + relation.getRange().getType().getSymbol() + "'");
            ok = false;
        }

        if (!ok) {
            return null;
        }

        this.td = ReservedTypeDescriptor.INT;
        return this.td;
    }


}
