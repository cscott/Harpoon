// Exp.java, created Wed Jan 13 21:14:57 1999 by cananian
package harpoon.IR.Tree;

/**
 * <code>Exp</code> objects are expressions which stand for the computation
 * of some value (possibly with side effects).
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>, based on
 *          <i>Modern Compiler Implementation in Java</i> by Andrew Appel.
 * @version $Id: Exp.java,v 1.1.2.2 1999-01-15 17:56:40 duncan Exp $
 */
abstract public class Exp {
    /** Return a list of subexpressions of this <code>Exp</code>. */
    abstract public ExpList kids();
    /** Build an <code>Exp</code> of this type from the given list of
     *  subexpressions. */
    abstract public Exp build(ExpList kids);
    
  public abstract void visit(TreeVisitor v);
}

