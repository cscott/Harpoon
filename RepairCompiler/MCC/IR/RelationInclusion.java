package MCC.IR;

import java.util.*;

public class RelationInclusion extends Inclusion {
    
    Expr leftelementexpr, rightelementexpr;
    RelationDescriptor relation;

    public RelationInclusion(Expr leftelementexpr, Expr rightelementexpr, RelationDescriptor relation) {
        this.leftelementexpr = leftelementexpr;
        this.rightelementexpr = rightelementexpr;
        this.relation = relation;
    }

    public Set getTargetDescriptors() {
        HashSet v = new HashSet();
        v.add(relation);
        return v;
    }

    public Set getRequiredDescriptors() {
        Set v = leftelementexpr.getRequiredDescriptors();
        v.addAll(rightelementexpr.getRequiredDescriptors());
        v.add(relation.getDomain());
        v.add(relation.getRange());
        return v;
    }

    public void generate(CodeWriter writer) {
        VarDescriptor ld = VarDescriptor.makeNew("leftele");
        leftelementexpr.generate(writer, ld);
        VarDescriptor rd = VarDescriptor.makeNew("rightele");
        rightelementexpr.generate(writer, rd);
        writer.outputline(relation.getSafeSymbol() + "_hash->add((int)" + ld.getSafeSymbol() + ", (int)" + rd.getSafeSymbol() + ");");
        writer.outputline(relation.getSafeSymbol() + "_hashinv->add((int)" + rd.getSafeSymbol() + ", (int)" + ld.getSafeSymbol() + ");");
        //writer.outputline("printf(\"" + relation.getSafeSymbol() + " (add): <%d, %d>\\n\", " + ld.getSafeSymbol() + ", " + rd.getSafeSymbol() + ");");
    }

    public boolean typecheck(SemanticAnalyzer sa) {
        TypeDescriptor ld = leftelementexpr.typecheck(sa);
        TypeDescriptor rd = rightelementexpr.typecheck(sa);
        
        if (ld == null || rd == null) {
            return false;
        }

        boolean ok = true;

        if (ld != relation.getDomain().getType()) {
            sa.getErrorReporter().report(null, "Type of left element '" + ld.getSymbol() + "' must match domain type '" + relation.getDomain().getType().getSymbol() + "'");
            ok = false;
        }

        if (rd != relation.getRange().getType()) {
            assert rd.getSymbol() != null;
            assert relation.getRange().getType() != null : relation.getRange().getSymbol();
            sa.getErrorReporter().report(null, "Type of right element '" + rd.getSymbol() + "' must match range type '" + relation.getRange().getType().getSymbol() + "'");
            ok = false;
        }

        return ok;
    }

}
