/**
 * FieldDescriptor
 *
 * represents a field of a type
 */

package MCC.IR;

public class FieldDescriptor extends Descriptor {

    TypeDescriptor type;
    boolean ptr;

    public FieldDescriptor(String name) {
        super(name);
    }

    public TypeDescriptor getType() {
        assert type != null;
        return type;
    }

    public void setType(TypeDescriptor td) {
        assert td != null;
        type = td;
    }

    public void setPtr(boolean ptr) {
        this.ptr = ptr;
    }

    public boolean getPtr() {
        return ptr;
    }

    public Expr getBaseSizeExpr() {
        if (ptr) { /* ptrs are 32bits */
            return new IntegerLiteralExpr(32);
        } else {
            return type.getSizeExpr();
        }
    }
}
