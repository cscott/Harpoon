// Stm.java, created Wed Jan 13 21:14:57 1999 by cananian
package harpoon.IR.Tree;

import harpoon.Temp.CloningTempMap;
import harpoon.Util.HashSet;
import harpoon.Util.Set;

/**
 * <code>Stm</code> objects are statements which perform side effects and
 * control flow.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>, based on
 *          <i>Modern Compiler Implementation in Java</i> by Andrew Appel.
 * @version $Id: Stm.java,v 1.1.2.5 1999-04-05 21:50:44 duncan Exp $
 */
abstract public class Stm extends Tree {
    protected Stm(TreeFactory tf, harpoon.ClassFile.HCodeElement source) {
	super(tf, source);
    }

    /** Build an <code>Stm</code> of this type from the given list of
     *  subexpressions. */
    abstract public Stm build(ExpList kids);

    protected Set defSet() { return new HashSet(); }
    protected Set useSet() { return ExpList.useSet(kids()); }

    public abstract Tree rename(TreeFactory tf, CloningTempMap ctm);
}

