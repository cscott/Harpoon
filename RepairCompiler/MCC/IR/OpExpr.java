package MCC.IR;

import java.util.*;

public class OpExpr extends Expr {

    Expr left;
    Expr right;
    Opcode opcode;

    public OpExpr(Opcode opcode, Expr left, Expr right) {
        this.opcode = opcode;
        this.left = left;
        this.right = right;

        assert (right == null && opcode == Opcode.NOT) || (right != null);
    }

    public Set getRequiredDescriptors() {
        Set v = left.getRequiredDescriptors();
     
        if (right != null) {
            v.addAll(right.getRequiredDescriptors());
        }

        return v;
    }   

    public void generate(CodeWriter writer, VarDescriptor dest) {
        VarDescriptor ld = VarDescriptor.makeNew("leftop");
        left.generate(writer, ld);        
        VarDescriptor rd = null;

        if (right != null) {
            rd = VarDescriptor.makeNew("rightop");
            right.generate(writer, rd);
        }

        String code;
        if (opcode != Opcode.NOT) { /* two operands */
            assert rd != null;
            writer.outputline("int " + dest.getSafeSymbol() + " = " + 
                              ld.getSafeSymbol() + " " + opcode.toString() + " " + rd.getSafeSymbol() + ";");
        } else {
            writer.outputline("int " + dest.getSafeSymbol() + " = !" + ld.getSafeSymbol() + ";");
        }
    }

    public void prettyPrint(PrettyPrinter pp) {
        pp.output("(");
        if (opcode == Opcode.NOT) {
            left.prettyPrint(pp);
        } else {           
            left.prettyPrint(pp);
            pp.output(" " + opcode.toString() + " ");
            assert right != null;
            right.prettyPrint(pp);
        }
        pp.output(")");
    }

    public TypeDescriptor typecheck(SemanticAnalyzer sa) {
        TypeDescriptor lt = left.typecheck(sa);
        TypeDescriptor rt = right == null ? null : right.typecheck(sa);

        if (lt == null) {
            return null;
        } else if (right != null && rt == null) {
            return null;
        }

        boolean ok = true;

        if (lt != ReservedTypeDescriptor.INT) {
            sa.getErrorReporter().report(null, "Left hand side of expression is of type '" + lt.getSymbol() + "' but must be type 'int'");
            ok = false;
        }

        if (right != null) {
            if (rt != ReservedTypeDescriptor.INT) {
                sa.getErrorReporter().report(null, "Right hand side of expression is of type '" + rt.getSymbol() + "' but must be type 'int'");
                ok = false;
            }
        }

        if (!ok) {
            return null;
        }

        this.td = ReservedTypeDescriptor.INT;
        return this.td;
    }

}





