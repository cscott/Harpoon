// MEMA.java, created Wed Jan 13 21:14:57 1999 by cananian
package harpoon.IR.Tree;

/**
 * <code>MEMA</code> objects are expressions which stand for the contents of
 * a address value in memory starting at the address specified by the
 * subexpression.  Note that when <code>MEMA</code> is used as the left child
 * of a <code>MOVE</code> or <code>CALL</code>, it means "store," but
 * anywhere else it means "fetch."
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>, based on
 *          <i>Modern Compiler Implementation in Java</i> by Andrew Appel.
 * @version $Id: MEMA.java,v 1.1.2.1 1999-01-15 00:19:34 cananian Exp $
 */
public class MEMA extends MEM {
    /** Constructor. */
    private boolean is64bitarch; // FIXME: make frame-dependent.
    public MEMA(Exp exp, boolean is64bitarch) { super(exp); }
    public Exp build(ExpList kids) {
	return new MEMA(kids.head, is64bitarch);
    }

    public boolean isDoubleWord() { return is64bitarch; }
    public boolean isFloatingPoint() { return false; }
}

