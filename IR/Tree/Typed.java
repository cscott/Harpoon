// Typed.java, created Thu Jan 14 18:59:52 1999 by cananian
package harpoon.IR.Tree;

/**
 * The <code>Typed</code> interface allows access to type information for
 * <code>TEMP</code>, <code>MEM</code>, <code>CONST</code>,
 * <code>OPER</code>, and <code>UNOP</code>.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: Typed.java,v 1.1.2.2 1999-02-05 10:40:45 cananian Exp $
 */
public interface Typed  {
    // enumerated constants.
    public int INT=Type.INT;
    public int LONG=Type.LONG;
    public int FLOAT=Type.FLOAT;
    public int DOUBLE=Type.DOUBLE;
    public int POINTER=Type.POINTER;

    /** Returns enumerated constant (INT, LONG, FLOAT, DOUBLE, or POINTER)
     *  corresponding to the type of the expression. */
    public int type();
}
