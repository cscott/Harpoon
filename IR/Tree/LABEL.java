// LABEL.java, created Wed Jan 13 21:14:57 1999 by cananian
package harpoon.IR.Tree;

import harpoon.Temp.Label;

/**
 * <code>LABEL</code> objects define the constant value of the given
 * label to be the current machine code address.  This is like a label
 * definition in assembly language.  The value <code>NAME(Label l)</code>
 * may be the target of jumps, calls, etc.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>, based on
 *          <i>Modern Compiler Implementation in Java</i> by Andrew Appel.
 * @version $Id: LABEL.java,v 1.1.2.2 1999-01-15 17:56:40 duncan Exp $
 */
public class LABEL extends Stm { 
    /** The symbolic name to define. */
    public final Label label;
    /** Constructor. */
    public LABEL(Label label) { this.label=label; }
    public ExpList kids() {return null;}
    public Stm build(ExpList kids) {
	return this;
    }
    /** Accept a visitor */
    public void visit(TreeVisitor v) { v.visit(this); }
}

