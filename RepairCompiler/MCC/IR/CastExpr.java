package MCC.IR;

import java.util.*;

public class CastExpr extends Expr {
    
    TypeDescriptor type;
    Expr expr;

    public Set freeVars() {
	return expr.freeVars();
    }

    public Expr getExpr() {
	return expr;
    }

    public void findmatch(Descriptor d, Set s) {
	expr.findmatch(d,s);
    }

    public CastExpr(TypeDescriptor type, Expr expr) {
        this.type = type;
        this.expr = expr;
    }

    public String name() {
	String str="";
	str="(("+type.toString()+")"+expr.name()+")";
	return str;
    }

    public boolean equals(Map remap, Expr e) {
	if (e==null)
	    return false;
	else if (!(e instanceof CastExpr))
	    return false;
	else return ((this.type==((CastExpr)e).type)&&expr.equals(remap,((CastExpr)e).expr));
    }

    public Set useDescriptor(Descriptor d) {
	return expr.useDescriptor(d);
    }

    public boolean usesDescriptor(Descriptor d) {
	return expr.usesDescriptor(d);
    }

    public Set getRequiredDescriptors() {
        return expr.getRequiredDescriptors();
    }

    public void generate(CodeWriter writer, VarDescriptor dest) {
        VarDescriptor vd = VarDescriptor.makeNew("expr");
        expr.generate(writer, vd);
        writer.outputline("int " + dest.getSafeSymbol() + " = (int) " + vd.getSafeSymbol() + ";");
    }

    public void prettyPrint(PrettyPrinter pp) {
        pp.output("cast(" + type.getSafeSymbol() + ", ");
        expr.prettyPrint(pp);
        pp.output(")");
    }

    public TypeDescriptor getType() {
        return type;
    }

    public TypeDescriptor typecheck(SemanticAnalyzer sa) {
        TypeDescriptor td = expr.typecheck(sa);

        if (td == null) {
            return null;
        }

        if (!type.isSubtypeOf(td)) {
            sa.getErrorReporter().report(null, "Expression type '" + td.getSymbol() + "' is not a parent of the cast type '" + type.getSymbol() + "'");
            return null;
        }

        this.td = type;
        return type;
    }

}








