// TRY.java, created Wed Aug  5 06:48:25 1998
package harpoon.IR.QuadSSA;

import harpoon.ClassFile.*;
/**
 * <code>TRY</code> objects represent <code>try-catch-finally</code> blocks.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: TRY.java,v 1.6 1998-08-25 07:43:03 cananian Exp $
 */

public class TRY extends Quad {
    public Quad tryBlock;
    public Quad catchBlock;
    public HClass catchException;
    /** Creates a <code>TRY</code>. The <code>tryBlock</code> is executed
     *  first.  If any exceptions are thrown, they are caught in
     *  the appropriate <code>catchBlock</code>.  Finally, the 
     *  <code>finallyBlock</code> is executed, regardless of whether
     *  the <Code>tryBlock</code> completed without exception or not. */
    public TRY(String sourcefile, int linenumber,
	       Quad tryBlock, 
	       Quad catchBlock, HClass catchException) {
	super(sourcefile, linenumber);
	this.tryBlock = tryBlock;
	this.catchBlock = catchBlock;
	this.catchException = catchException;
    }
    TRY(HCodeElement hce, Quad tryBlock, 
	Quad catchBlock, HClass catchException) {
	this(hce.getSourceFile(), hce.getLineNumber(),
	     tryBlock, catchBlock, catchException);
    }
    /** Returns human-readable representation of this Quad. */
    public String toString() { return "TRY ("+catchException+")"; }
}
