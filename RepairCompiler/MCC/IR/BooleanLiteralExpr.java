package MCC.IR;

import MCC.State;
import java.util.Map;

public class BooleanLiteralExpr extends LiteralExpr {
    
    boolean value;
    
    public BooleanLiteralExpr(boolean value) {
        this.value = value;
        td = ReservedTypeDescriptor.INT;
    }

    public boolean usesDescriptor(Descriptor d) {
	return false;
    }

    public String name() {
	if (value)
	    return "true";
	else return "false";
    }

    public boolean equals(Map remap, Expr e) {
	if (e==null)
	    return false;
	else if (!(e instanceof BooleanLiteralExpr))
	    return false;
	else return (((BooleanLiteralExpr)e).value==value);
    }

    public boolean getValue() {
        return value;
    }

    public void generate(CodeWriter writer, VarDescriptor dest) {
        writer.outputline("int " + dest.getSafeSymbol() + " = " + (value ? "1" : "0") + ";");
    }

    public void prettyPrint(PrettyPrinter pp) {
        pp.output(value ? "true" : "false");
    }

    public TypeDescriptor typecheck(SemanticAnalyzer sa) {
        td = ReservedTypeDescriptor.INT;
        return td;
    }

}
