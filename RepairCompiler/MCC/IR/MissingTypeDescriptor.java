package MCC.IR;

/**
 * MissingTypeDescriptor
 *
 * a placeholder for type descriptors that haven't been found yet
 */

public class MissingTypeDescriptor extends TypeDescriptor {

    public MissingTypeDescriptor(String name) {
        super(name);
    }

    public Expr getSizeExpr() {
        throw new IRException("invalid");
    }
    
}
