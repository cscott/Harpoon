package MCC.IR;

/**
 * ArrayDescriptor
 *
 * wrapper descriptor for an internal field descriptor that
 * allows arrays
 */

public class ArrayDescriptor extends FieldDescriptor {

    FieldDescriptor fd;
    Expr index;

    public ArrayDescriptor(FieldDescriptor fd, Expr index) {
        super(fd.getSymbol());
        this.index = index;
        this.fd = fd;
    }

    public FieldDescriptor getField() {
        return fd;
    }

    public TypeDescriptor getType() {
        return fd.getType();
    }

    public void setType(TypeDescriptor td) {
        fd.setType(td);
    }

    public Expr getIndexBound() {
        return index;
    }

    public Expr getBaseSizeExpr() {
        throw new IRException();
    }

}
