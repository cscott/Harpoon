package MCC.IR;

public class IntegerLiteralExpr extends LiteralExpr {

    int value;

    public IntegerLiteralExpr(int value) {
        this.value = value; 
        td = ReservedTypeDescriptor.INT;
    }

    public int getValue() {
        return value;
    }

    public void generate(CodeWriter writer, VarDescriptor dest) {
        writer.outputline("int " + dest.getSafeSymbol() + " = " + value + ";");
    }

    public void prettyPrint(PrettyPrinter pp) {
        pp.output("" + value);
    }

    public TypeDescriptor typecheck(SemanticAnalyzer sa) {
        td = ReservedTypeDescriptor.INT;
        return td;
    }

}
