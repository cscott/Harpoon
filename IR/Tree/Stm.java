// Stm.java, created Wed Jan 13 21:14:57 1999 by cananian
package harpoon.IR.Tree;

import harpoon.Temp.CloningTempMap;
import harpoon.Util.Util;

import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

/**
 * <code>Stm</code> objects are statements which perform side effects and
 * control flow.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>, based on
 *          <i>Modern Compiler Implementation in Java</i> by Andrew Appel.
 * @version $Id: Stm.java,v 1.1.2.9 1999-08-03 21:12:58 duncan Exp $
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
    abstract public Stm build(TreeFactory tf, ExpList kids);

    // Overridden by MOVE and INVOCATION
    protected Set defSet() { return new HashSet(); }
    protected Set useSet() { return ExpList.useSet(kids()); }

    public abstract Tree rename(TreeFactory tf, CloningTempMap ctm);

    /** Returns a tree-based representation of <code>list</code>.  
     *
     * <br><b>Requires:</b> foreach element, <code>l</code>, of 
     *                      <code>list</code>, 
     *                      <code>(l != null) && (l instanceof Stm)</code>.
     * <br><b>Modifies:</b>
     * <br><b>Effects: </b> returns a tree-based representation of 
     *                      <code>list</code>.  If <code>list</code> is null,
     *                      returns null.  
     */
    public static Stm toStm(List list) { 
	if (list==null) return null;
	int size = list.size();
	if      (size==0) { return null; }
	else if (size==1) { return (Stm)list.get(0); } 
	else { 
	    Stm          hce = (Stm)list.get(0); 
	    TreeFactory  tf  = hce.getFactory();
	    SEQ s=new SEQ(tf,hce,(Stm)list.get(size-2),(Stm)list.get(size-1));
	    for (ListIterator li=list.listIterator(size-2);li.hasPrevious();) {
		Stm previous = (Stm)li.previous();
		Util.assert(previous.getFactory()==tf);
		s = new SEQ(tf, hce, previous, s);
	    }
	    return s;
	}		
    }
}

