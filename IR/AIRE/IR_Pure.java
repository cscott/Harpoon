// IR_Pure.java, created by cananian
package harpoon.IR.AIRE;

/**
 * <code>IR_Pure</code>:
 * An enumerated type must be provided, called <code>IR_Pure</code>,
 * which specifies various options associated with predefined 
 * <code>IIR_FunctionDeclaration</code> classes.
 */
public class IR_Pure {
    public static int IR_UNKNOWN_PURE = 0;
    public static int IR_PURE_FUNCTION = 1;
    public static int IR_IMPURE_FUNCTION = 2;
    public static int IR_PURE_PROCEDURAL = 3; // IIR ONLY!
    public static int IR_IMPURE_PROCEDURAL = 4; // IIR ONLY!
}
