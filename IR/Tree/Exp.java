// Exp.java, created Wed Jan 13 21:14:57 1999 by cananian
package harpoon.IR.Tree;

import harpoon.Temp.CloningTempMap;

import java.util.HashSet;
import java.util.Set;

/**
 * <code>Exp</code> objects are expressions which stand for the computation
 * of some value (possibly with side effects).
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>, based on
 *          <i>Modern Compiler Implementation in Java</i> by Andrew Appel.
 * @version $Id: Exp.java,v 1.1.2.8 1999-08-03 21:12:57 duncan Exp $
 */
abstract public class Exp extends Tree implements Typed {
    protected Exp(TreeFactory tf, harpoon.ClassFile.HCodeElement source) {
	super(tf, source, 0);
    }
  
    // Only ESEQs can define anything, and they are not permitted in 
    // canonical form. 
    protected Set defSet() { return new HashSet(); }

    protected Set useSet() {
	return ExpList.useSet(kids());
    }

    /** Build an <code>Exp</code> of this type from the given list of
     *  subexpressions. */
    abstract public Exp build(ExpList kids);
    abstract public Exp build(TreeFactory tf, ExpList kids);

    public abstract Tree rename(TreeFactory tf, CloningTempMap ctm);

    // Typed interface:
    /** Result type. */
    public abstract int type();
    /** Returns <code>true</code> if the expression corresponds to a
     *  64-bit value. */
    public boolean isDoubleWord() { return Type.isDoubleWord(tf, type()); }
    /** Returns <code>true</code> if the expression corresponds to a
     *  floating-point value. */
    public boolean isFloatingPoint() { return Type.isFloatingPoint(type()); }
}

