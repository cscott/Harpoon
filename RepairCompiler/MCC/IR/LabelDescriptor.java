package MCC.IR;

/**
 * LabelDescriptor
 *
 * a label descriptor represents a label in a structure which is just
 * a shorthand notation for another field
 */

public class LabelDescriptor extends FieldDescriptor {

    FieldDescriptor fd;
    Expr index;

    public LabelDescriptor(String name) {
        super(name);
        index = null;
    }

    public FieldDescriptor getField() {
        return fd;
    }

    public void setField(FieldDescriptor fd) {
        this.fd = fd;
    }

    public Expr getIndex() {
        return index;
    }

    public void setIndex(Expr index) {
        this.index = index;
    }

    public Expr getBaseSizeExpr() {
        throw new IRException();
    }
}
