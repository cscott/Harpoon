// IR_SignalKind.java, created by cananian
package harpoon.IR.AIRE;

/**
 * <code>IR_SignalKind</code>:
 * An enumerated type must be provided, called <code>IR_SignalKind</code>,
 * which specifies various options associated with predefined 
 * <code>IIR_Signal</code> and <code>IIR_SignalInterfaceDeclaration</code>
 * classes.  The enumeration may be implemented as a true enumerated type
 * or (preferred) as an integer and constant set.  In either case, the
 * type must include the following labels prior to any labels associated
 * with completely new, instatiable IIR extension classes:
 */
public abstract class IR_SignalKind {
    public static int IR_NO_SIGNAL_KIND = 0;
    public static int IR_REGISTER_KIND = 1;
    public static int IR_BUS_KIND = 2;
}
