package MCC.IR;

import java.util.*;

public class TokenSetDescriptor extends SetDescriptor {
    
    private static int count = 0;
    Vector literals = new Vector();
    
    public TokenSetDescriptor() {
        super("toketsetdescriptor" + count++);
    }
    
    public void addLiteral(LiteralExpr lit) {
        literals.addElement(lit);
    }

    public TypeDescriptor getType() {
        return ReservedTypeDescriptor.INT;
    }

    public void setType(TypeDescriptor td) {
        throw new IRException("invalid");
    }

}
