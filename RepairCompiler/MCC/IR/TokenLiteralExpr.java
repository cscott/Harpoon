package MCC.IR;

import java.util.*;

public class TokenLiteralExpr extends LiteralExpr {

    String token;
    Integer num;

    static int count = 100;
    static Hashtable tokens = new Hashtable(); 

    public TokenLiteralExpr(String token) {               
        this.token = token;
        td = ReservedTypeDescriptor.INT;
        
        if (tokens.containsKey(token)) {
            num = (Integer) tokens.get(token);
        } else {
            num = new Integer(count++);
            tokens.put(token, num);
        }           
    }

    public abstract boolean equals(Map remap, Expr e) {
	if (e==null)
	    return false;
	else if (!e instanceof TokenLiteralExpr)
	    return false;
	else return ((BooleanLiteralExpr)e).num.equals(num);
    }

    public String getValue() {
        return token;
    }

    public void generate(CodeWriter writer, VarDescriptor dest) {
        writer.outputline("int " + dest.getSafeSymbol() + " = " + num.toString().toString() + ";");
    }
    
    public void prettyPrint(PrettyPrinter pp) {
        pp.output("{" + token + " = " + num + "}"); 
    }

    public TypeDescriptor typecheck(SemanticAnalyzer sa) {
        td = ReservedTypeDescriptor.INT;
        return td;
    }

}
