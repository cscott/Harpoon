// Exp.java, created Wed Jan 13 21:14:57 1999 by cananian
package harpoon.IR.Tree;

import harpoon.Temp.CloningTempMap;
import harpoon.Util.HashSet;
import harpoon.Util.Set;

/**
 * <code>Exp</code> objects are expressions which stand for the computation
 * of some value (possibly with side effects).
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>, based on
 *          <i>Modern Compiler Implementation in Java</i> by Andrew Appel.
 * @version $Id: Exp.java,v 1.1.2.5 1999-04-05 21:50:44 duncan Exp $
 */
abstract public class Exp extends Tree {
    protected Exp(TreeFactory tf, harpoon.ClassFile.HCodeElement source) {
	super(tf, source);
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
    
    public abstract Tree rename(TreeFactory tf, CloningTempMap ctm);
}

