// Stm.java, created Wed Jan 13 21:14:57 1999 by cananian
package harpoon.IR.Tree;

import harpoon.Temp.CloningTempMap;

import java.util.HashSet;
import java.util.Set;

/**
 * <code>Stm</code> objects are statements which perform side effects and
 * control flow.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>, based on
 *          <i>Modern Compiler Implementation in Java</i> by Andrew Appel.
 * @version $Id: Stm.java,v 1.1.2.7 1999-07-07 09:47:24 duncan Exp $
 */
abstract public class Stm extends Tree {
    protected Stm(TreeFactory tf, harpoon.ClassFile.HCodeElement source) {
	super(tf, source);
    }
    
    protected Stm(TreeFactory tf, harpoon.ClassFile.HCodeElement source,
		  int next_arity) {
	super(tf, source, next_arity);
    }

    /** Build an <code>Stm</code> of this type from the given list of
     *  subexpressions. */
    abstract public Stm build(ExpList kids);

    // Overridden by MOVE and INVOCATION
    protected Set defSet() { return new HashSet(); }
    protected Set useSet() { return ExpList.useSet(kids()); }

    public abstract Tree rename(TreeFactory tf, CloningTempMap ctm);
}

