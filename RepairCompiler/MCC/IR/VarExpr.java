package MCC.IR;

import java.util.*;

public class VarExpr extends Expr {

    String varname;
    VarDescriptor vd = null;
    boolean typechecked = false;

    public VarExpr(String varname) {
        this.varname = varname; 
    }

    public Set getRequiredDescriptors() {
        return new HashSet();
    }

    public void generate(CodeWriter writer, VarDescriptor dest) {        

        // #TBD#: bit of a hack, really should have been type checked properly 

        vd = (VarDescriptor) writer.getSymbolTable().get(varname);        
        assert vd != null;
        assert vd.getType() != null;
        this.td = vd.getType();

        writer.outputline(vd.getType().getGenerateType().getSafeSymbol() + " " + dest.getSafeSymbol() + 
                          " = (" + vd.getType().getGenerateType().getSafeSymbol() + ") " + vd.getSafeSymbol() + "; //varexpr");
    }

    public void prettyPrint(PrettyPrinter pp) {
        pp.output(varname);
    }

    public TypeDescriptor typecheck(SemanticAnalyzer sa) {
        typechecked = true;
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
