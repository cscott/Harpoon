package MCC.IR;

/**
 * ReservedTypeDescriptor
 *
 * handles reserved types: bit, byte, short, int
 */

public class ReservedTypeDescriptor extends TypeDescriptor {

    IntegerLiteralExpr size;

    public static final ReservedTypeDescriptor BIT = new ReservedTypeDescriptor("bit", 1);
    public static final ReservedTypeDescriptor BYTE = new ReservedTypeDescriptor("byte", 8);
    public static final ReservedTypeDescriptor SHORT = new ReservedTypeDescriptor("short", 16);
    public static final ReservedTypeDescriptor INT = new ReservedTypeDescriptor("int", 32);

    private ReservedTypeDescriptor(String name, int size) {
        super(name);
        this.size = new IntegerLiteralExpr(size);
    }

    public Expr getSizeExpr() {
        return size;
    }

    public String getSafeSymbol() {
        return getSymbol();
    }
}
