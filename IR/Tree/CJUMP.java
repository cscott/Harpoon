// Exp.java, created Wed Jan 13 21:14:57 1999 by cananian
package harpoon.IR.Tree;

import harpoon.Temp.Label;

/**
 * <code>Exp</code> objects are expressions which stand for the computation
 * of some value (possibly with side effects).
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>, based on
 *          <i>Modern Compiler Implementation in Java</i> by Andrew Appel.
 * @version $Id: CJUMP.java,v 1.1.2.1 1999-01-14 05:54:58 cananian Exp $
 */
public class CJUMP extends Stm {
    /** An expression that evaluates into a boolean result. */
    public Exp test;
    /** The label to jump to if <code>test</code> is <code>true</code>. */
    public Label iftrue;
    /** The label to jump to if <code>test</code> is <code>false</code>. */
    public Label iffalse;
    /** Constructor. */
    public CJUMP(Exp test, Label iftrue, Label iffalse) {
	this.test = test; this.iftrue = iftrue; this.iffalse = iffalse;
    }
    public ExpList kids() {return new ExpList(test, null); }
    public Stm build(ExpList kids) {
	return new CJUMP(kids.head, iftrue, iffalse);
    }
}

