// IR_Mode.java, created by cananian
package harpoon.IR.AIRE;

/**
 * <code>IR_Mode</code>:
 * An enumerated type must be provided, called <code>IR_Mode</code>,
 * which specifies various options associated with predefined 
 * <code>IIR_InterfaceDeclaration</code> classes.
 */
public abstract class IR_Mode {
    public static int IR_UNKNOWN_MODE = 0;
    public static int IR_IN_MODE = 1;
    public static int IR_OUT_MODE = 2;
    public static int IR_INOUT_MODE = 3;
    public static int IR_BUFFER_MODE = 4;
    public static int IR_LINKAGE_MODE = 5;
}
