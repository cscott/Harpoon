// MOVE.java, created Wed Jan 13 21:14:57 1999 by cananian
package harpoon.IR.Tree;

/**
 * <code>MOVE</code> statements assign a value to a location.
 * <code>MOVE(TEMP <i>t</i>, <i>e</i>)</code> evaluates expression <i>e</i>
 * and assigns its value into temporary <i>t</i>.
 * <code>MOVE(MEM(<i>e1</i>, <i>k</i>), <i>e2</i>)</code> evaluates
 * expression <i>e1</i> yielding address <i>a</i>, then evaluates <i>e2</i>
 * and assigns its value into memory starting at address <i>a</i>.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>, based on
 *          <i>Modern Compiler Implementation in Java</i> by Andrew Appel.
 * @version $Id: MOVE.java,v 1.1.2.2 1999-01-15 01:17:36 cananian Exp $
 */
public class MOVE extends Stm {
    /** The expression giving the destination for the computed value. */
    public Exp dst;
    /** The expression for the computed value. */
    public Exp src;
    /** Constructor. */
    public MOVE(Exp dst, Exp src) { this.dst=dst; this.src=src; }
    public ExpList kids() {
        if (dst instanceof MEM)
	   return new ExpList(((MEM)dst).exp, new ExpList(src,null));
	else return new ExpList(src,null);
    }
    public Stm build(ExpList kids) {
        if (dst instanceof MEM)
	    return new MOVE(dst.build(new ExpList(kids.head, null)),
			    kids.tail.head);
	else return new MOVE(dst, kids.head);
    }
}

