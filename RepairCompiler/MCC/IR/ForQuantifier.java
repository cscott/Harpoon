package MCC.IR;

import java.util.*;

public class ForQuantifier extends Quantifier {

    VarDescriptor var = null;
    Expr lower = null;
    Expr upper = null;

    public ForQuantifier() {}

    public void setVar(VarDescriptor vd) {
        this.var = vd;
    }

    public void setBounds(Expr lower, Expr upper) {
        this.lower = lower;
        this.upper = upper;
    }

    public Set getRequiredDescriptors() {
        return new HashSet();
    }

    public String toString() {
        return "for quantifier " + var.getSymbol() + " = " + lower + " to " + upper;
    }

    public void generate_open(CodeWriter writer) {
        VarDescriptor ld = VarDescriptor.makeNew();
        VarDescriptor ud = VarDescriptor.makeNew();
        lower.generate(writer, ld);
        upper.generate(writer, ud);
        
        writer.outputline("for (int " + var.getSafeSymbol() + " = " + ld.getSafeSymbol() + "; " + var.getSafeSymbol() + " <= " + ud.getSafeSymbol() + "; " + var.getSafeSymbol() + "++)");
        writer.startblock();
    }

    public boolean typecheck(SemanticAnalyzer sa) {
        TypeDescriptor lt = lower.typecheck(sa);
        TypeDescriptor ut = upper.typecheck(sa);
        
        if (lt == null || ut == null) {
            return false;
        }

        boolean ok = true;

        if (lt != ReservedTypeDescriptor.INT) {
            sa.getErrorReporter().report(null, "Lower bound of for quantifier must be of type 'int'");
            ok = false;
        }

        if (ut != ReservedTypeDescriptor.INT) {
            sa.getErrorReporter().report(null, "Upper bound of for quantifier must be of type 'int'");
            ok = false;
        }

        return ok;       
    } 

}










