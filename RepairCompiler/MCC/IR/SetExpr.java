package MCC.IR;

import java.util.*;

public class SetExpr extends Expr {

    SetDescriptor sd;

    public SetExpr(SetDescriptor sd) {
        this.sd = sd;
    }
    
    public SetExpr() {
        this.sd = null;
    }

    public Set getRequiredDescriptors() {
        HashSet v = new HashSet();
        v.add(sd);
        return v;
    }

    public void generate(CodeWriter writer, VarDescriptor vd) {
        throw new IRException("unsupported");
    }

    public void generate_set(CodeWriter writer, VarDescriptor vd) {
        writer.outputline("Set " + vd.getSafeSymbol() + " = " + sd.getSafeSymbol() + "_hash;");
    }    

    public void prettyPrint(PrettyPrinter pp) {
        pp.output(sd.getSafeSymbol());
    }

    public TypeDescriptor typecheck(SemanticAnalyzer sa) {
        this.td = sd.getType();
        return this.td;
    }

}
