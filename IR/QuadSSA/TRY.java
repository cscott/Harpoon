// TRY.java, created Wed Aug  5 06:48:25 1998
package harpoon.IR.QuadSSA;

import harpoon.ClassFile.*;
import harpoon.Temp.Temp;
/**
 * <code>TRY</code> objects represent <code>try-catch-finally</code> blocks.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: TRY.java,v 1.7 1998-09-01 18:08:53 cananian Exp $
 */

public class TRY extends Quad {
    /** Quad graph representing the code protected by this TRY. */
    public Quad tryBlock;
    /** Quad graph representing an exception handler for this TRY. */
    public Quad catchBlock;
    /** The type of exception which <code>catchBlock</code> handles. */
    public HClass catchException;
    /** Temp to hold the throw exception object inside the 
	<code>catchBlock</code> */
    public Temp catchParam;
    /** Creates a <code>TRY</code>. The <code>tryBlock</code> is executed
     *  first.  If any exceptions are thrown, they are caught in
     *  the appropriate <code>catchBlock</code>.  The thrown exception
     *  is assigned to the temp <code>catchParam</code> inside the
     *  body of the <code>catchBlock</code>. */
    public TRY(String sourcefile, int linenumber,
	       Quad tryBlock, Quad catchBlock, 
	       HClass catchException, Temp catchParam) {
	super(sourcefile, linenumber);
	this.tryBlock = tryBlock;
	this.catchBlock = catchBlock;
	this.catchException = catchException;
	this.catchParam = catchParam;
    }
    TRY(HCodeElement hce, Quad tryBlock, 
	Quad catchBlock, HClass catchException, Temp catchParam) {
	this(hce.getSourceFile(), hce.getLineNumber(),
	     tryBlock, catchBlock, catchException, catchParam);
    }
    /** Returns human-readable representation of this Quad. */
    public String toString() { return "TRY ("+catchException+")"; }
}
