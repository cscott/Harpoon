package MCC.IR;

/**
 * TypeDescriptor 
 *
 * represents types in the language (bit, short, etc. as well as structures)
 */

public abstract class TypeDescriptor extends Descriptor {

    public TypeDescriptor(String name) {
        super(name);
    }

    public boolean isSubtypeOf(TypeDescriptor td) {
        return false;
    }

    public TypeDescriptor getGenerateType() {
        return this;
    }

    public abstract Expr getSizeExpr();

}
