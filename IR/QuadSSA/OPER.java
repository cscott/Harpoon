// OPER.java, created Wed Aug  5 06:47:58 1998
package harpoon.IR.QuadSSA;

import harpoon.ClassFile.*;
import harpoon.Temp.Temp;
/**
 * <code>OPER</code> objects represent arithmetic/logical operations,
 * including mathematical operators such as add and subtract, 
 * conversion operators such as double-to-int, and comparison
 * operators such as greater than and equals.
 * <p>
 * <code>OPER</code> quads never throw exceptions.  Any exception thrown
 * implicitly by the java bytecode opcode corresponding to an OPER is
 * rewritten as an explicit test and throw in the Quad IR.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: OPER.java,v 1.8 1998-09-08 14:38:39 cananian Exp $
 */

public class OPER extends Quad {
    /** The temp in which to store the result of the operation. */
    public Temp dst;
    /** The operation to be performed, as a string. */
    public String opcode;
    /** Operands of the operation, in left-to-right order. */
    public Temp[] operands;
    /** Creates a <code>OPER</code>. */
    public OPER(HCodeElement source,
		String opcode, Temp dst, Temp[] operands) {
	super(source);
	this.opcode = opcode;
	this.dst = dst;
	this.operands = operands;
    }

    /** Returns the Temps used by this OPER. */
    public Temp[] use() { return (Temp[]) operands.clone(); }
    /** Returns the Temps defined by this OPER. */
    public Temp[] def() { return new Temp[] { dst }; }
    /** Returns a human-readable representation of this Quad. */
    public String toString() {
	StringBuffer sb = new StringBuffer(dst.toString());
	sb.append(" = OPER " + opcode + "(");
	for (int i=0; i<operands.length; i++) {
	    sb.append(operands[i].toString());
	    if (i<operands.length-1)
		sb.append(", ");
	}
	sb.append(')');
	return sb.toString();
    }
}
