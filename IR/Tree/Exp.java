// Exp.java, created Wed Jan 13 21:14:57 1999 by cananian
package harpoon.IR.Tree;

/**
 * <code>Exp</code> objects are expressions which stand for the computation
 * of some value (possibly with side effects).
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>, based on
 *          <i>Modern Compiler Implementation in Java</i> by Andrew Appel.
 * @version $Id: Exp.java,v 1.1.2.3 1999-02-05 11:48:47 cananian Exp $
 */
abstract public class Exp extends Tree {
    protected Exp(TreeFactory tf, harpoon.ClassFile.HCodeElement source) {
	super(tf, source);
    }

    /** Build an <code>Exp</code> of this type from the given list of
     *  subexpressions. */
    abstract public Exp build(ExpList kids);
}

