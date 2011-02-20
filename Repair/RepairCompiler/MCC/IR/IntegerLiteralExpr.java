package MCC.IR;
import java.util.Map;

public class IntegerLiteralExpr extends LiteralExpr {

    int value;

    public boolean usesDescriptor(Descriptor d) {
	return false;
    }

    public IntegerLiteralExpr(int value) {
        this.value = value; 
        td = ReservedTypeDescriptor.INT;
    }

    public String name() {
	return (new Integer(value)).toString();
    }

    public boolean isNull() {
	return value==0;
    }

    public boolean equals(Map remap, Expr e) {
	if (e==null)
	    return false;
	else if (!(e instanceof IntegerLiteralExpr))
	    return false;
	else return (((IntegerLiteralExpr)e).value==value);
    }

    public int getValue() {
        return value;
    }

    public void generate(CodeWriter writer, VarDescriptor dest) {
        writer.addDeclaration("int", dest.getSafeSymbol());
        writer.outputline(dest.getSafeSymbol() + " = " + value + ";");
    }

    public void prettyPrint(PrettyPrinter pp) {
        pp.output("" + value);
    }

    public TypeDescriptor typecheck(SemanticAnalyzer sa) {
        td = ReservedTypeDescriptor.INT;
        return td;
    }
}
