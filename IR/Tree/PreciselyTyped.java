package harpoon.IR.Tree;

/**
 * The <code>PreciselyTyped</code> interface allows access to type 
 * information for expressions which have a type which cannot be expressed
 * by the standard types in the <code>Typed</code> interface. 
 * Only <code>CONST</code> and <code>MEM</code> implement
 * <code>PreciselyTyped</code>.  All other <code>Tree.Exp</code>s deal
 * with register-native types.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @author  Duncan Bryce     <duncan@lcs.mit.edu>
 * @version $Id: PreciselyTyped.java,v 1.1.2.2 1999-08-12 03:37:27 cananian Exp $
 */
public interface PreciselyTyped extends Typed  {
    // enumerated constants
    public int SMALL=PreciseType.SMALL;

    /** Returns the size of the expression, in bits.
     *  Only valid if the type of the expression is <code>SMALL</code>. */
    public int     bitwidth();
    
    /** Returns true if this is a signed expression, false otherwise.
     *  Only valid if the type of the expression is <code>SMALL</code>. */
    public boolean signed();
}
