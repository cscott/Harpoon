package harpoon.IR.Tree;

/**
 * The <code>PreciselyTyped</code> interface allows access to type 
 * information for expressions which have a type which cannot be expressed
 * by the standard types in the <code>Typed</code> interface. 
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @author  Duncan Bryce     <duncan@lcs.mit.edu>
 * @version $Id: PreciselyTyped.java,v 1.1.2.1 1999-08-11 20:03:47 duncan Exp $
 */
public interface PreciselyTyped extends Typed  {
    /** Returns the size of the expression, in bits */
    public int     bitwidth();
    
    /** Returns true if this is a signed expression, false otherwise. */
    public boolean signed();
}
