package MCC.IR;

public class DNFExpr {
    boolean negate;
    Expr predicate;

    public DNFExpr(DNFExpr dp) {
	this.negate=dp.negate;
	this.predicate=dp.predicate;
    }

    public DNFExpr(boolean negate,Expr predicate) {
	this.negate=negate;
	this.predicate=predicate;
    }
    void negatePred() {
	negate=!negate;
    }

    public Expr getExpr() {
	return predicate;
    }

    public boolean getNegation() {
	return negate;
    }
}
