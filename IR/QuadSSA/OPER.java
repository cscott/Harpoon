// OPER.java, created Wed Aug  5 06:47:58 1998
package harpoon.IR.QuadSSA;

import harpoon.ClassFile.*;
import harpoon.Temp.Temp;
/**
 * <code>OPER</code> objects represent arithmetic/logical operations,
 * including mathematical operators such as add and subtract, 
 * conversion operators such as double-to-int, and comparison
 * operators such as greater than and equals.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: OPER.java,v 1.3 1998-08-20 22:43:23 cananian Exp $
 */

public class OPER extends Quad {
    /** The operation to be performed, as a string. */
    public String opcode;
    /** Operands of the operation, in left-to-right order. */
    public Temp[] operands;
    /** Creates a <code>OPER</code>. */
    public OPER(String sourcefile, int linenumber,
		String opcode, Temp[] operands) {
	super(sourcefile, linenumber);
	this.opcode = opcode;
	this.operands = operands;;
    }
    /** Returns a human-readable representation of this Quad. */
    public String toString() {
	StringBuffer sb = new StringBuffer("OPER " + opcode + "(");
	for (int i=0; i<operands.length; i++) {
	    sb.append(operands[i].toString());
	    if (i<operands.length-1)
		sb.append(", ");
	}
	sb.append(')');
	return sb.toString();
    }
}
