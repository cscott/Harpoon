// NAME.java, created Wed Jan 13 21:14:57 1999 by cananian
package harpoon.IR.Tree;

import harpoon.ClassFile.HCodeElement;
import harpoon.Temp.Label;
import harpoon.Util.Util;

/**
 * <code>NAME</code> objects are expressions which stand for symbolic
 * constants.  They usually correspond to some assembly language label.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>, based on
 *          <i>Modern Compiler Implementation in Java</i> by Andrew Appel.
 * @version $Id: NAME.java,v 1.1.2.3 1999-02-05 11:48:50 cananian Exp $
 */
public class NAME extends Exp {
    /** The label which this NAME refers to. */
    public final Label label;
    /** Constructor. */
    public NAME(TreeFactory tf, HCodeElement source,
		Label label) {
	super(tf, source);
	this.label=label;
	Util.assert(label!=null);
    }
    public ExpList kids() { return null; }
    public Exp build(ExpList kids) { return this; }
    /** Accept a visitor */
    public void visit(TreeVisitor v) { v.visit(this); }
}

