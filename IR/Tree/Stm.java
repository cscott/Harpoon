// Stm.java, created Wed Jan 13 21:14:57 1999 by cananian
package harpoon.IR.Tree;

/**
 * <code>Stm</code> objects are statements which perform side effects and
 * control flow.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>, based on
 *          <i>Modern Compiler Implementation in Java</i> by Andrew Appel.
 * @version $Id: Stm.java,v 1.1.2.2 1999-01-15 17:56:41 duncan Exp $
 */
abstract public class Stm {
    /** Return a list of subexpressions of thie <code>Stm</code>. */
    abstract public ExpList kids();
    /** Build an <code>Stm</code> of this type from the given list of
     *  subexpressions. */
    abstract public Stm build(ExpList kids);
    /** Accept a visitor */
    public abstract void visit(TreeVisitor v);
}

