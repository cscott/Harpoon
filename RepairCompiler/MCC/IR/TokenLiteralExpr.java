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
        
        if (tokens.contains(token)) {
            num = (Integer) tokens.get(token);
        } else {
            num = new Integer(count++);
            tokens.put(token, num);
        }           
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
