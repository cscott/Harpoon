// NAME.java, created Wed Jan 13 21:14:57 1999 by cananian
package harpoon.IR.Tree;

import harpoon.Temp.Label;

/**
 * <code>NAME</code> objects are expressions which stand for symbolic
 * constants.  They usually correspond to some assembly language label.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>, based on
 *          <i>Modern Compiler Implementation in Java</i> by Andrew Appel.
 * @version $Id: NAME.java,v 1.1.2.1 1999-01-14 05:55:00 cananian Exp $
 */
public class NAME extends Exp {
    /** The label which this NAME refers to. */
    public final Label label;
    /** Constructor. */
    public NAME(Label label) { this.label=label; }
    public ExpList kids() { return null; }
    public Exp build(ExpList kids) { return this; }
}

