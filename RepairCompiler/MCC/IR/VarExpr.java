package MCC.IR;

import java.util.*;

public class VarExpr extends Expr {

    String varname;
    VarDescriptor vd = null;

    public VarExpr(String varname) {
        this.varname = varname; 
    }

    public Set getRequiredDescriptors() {
        return new HashSet();
    }

    public void generate(CodeWriter writer, VarDescriptor dest) {        
        writer.outputline(vd.getType().getGenerateType().getSafeSymbol() + " " + dest.getSafeSymbol() + 
                          " = (" + vd.getType().getGenerateType().getSafeSymbol() + ") " + vd.getSafeSymbol() + ";");
    }

    public void prettyPrint(PrettyPrinter pp) {
        pp.output(varname);
    }

    public TypeDescriptor typecheck(SemanticAnalyzer sa) {
        vd = (VarDescriptor) sa.getSymbolTable().get(varname);

        if (vd == null) {
            System.out.println(varname);
            sa.getErrorReporter().report(null, "Undefined variable '" + varname + "'");
            return null;
        }
        
        assert vd.getType() != null;

        this.td = vd.getType();
        return this.td;
    }
    
}
