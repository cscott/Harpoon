package harpoon.IR.Tree;

/**
 * The <code>PreciselyTyped</code> interface allows access to type 
 * information for expressions which have a type which cannot be expressed
 * by the standard types in the <code>Typed</code> interface. 
 * Only <code>CONST</code>, <code>MEM</code>, and <code>ESEQ</code> implement
 * <code>PreciselyTyped</code>.  All other <code>Tree.Exp</code>s deal
 * with register-native types.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @author  Duncan Bryce     <duncan@lcs.mit.edu>
 * @version $Id: PreciselyTyped.java,v 1.1.2.3 1999-09-09 15:02:28 cananian Exp $
 */
public interface PreciselyTyped extends Typed  {
    /** Returns <code>true</code> if this expression is a small type,
     *  <code>false</code> otherwise. */
    public boolean isSmall();

    /** Returns the size of the expression, in bits.
     *  Only valid if the <code>isSmall()==true</code>. */
    public int     bitwidth();
    
    /** Returns true if this is a signed expression, false otherwise.
     *  Only valid if the <code>isSmall()==true</code>. */
    public boolean signed();
}
