// InRet.java, created Mon Dec 21 15:48:53 1998 by cananian
package harpoon.IR.Bytecode;

/**
 * <code>InRet</code> is an InCti with an operand.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: InRet.java,v 1.1.2.2 1998-12-21 21:19:21 cananian Exp $
 */
public class InRet extends InCti {
    final OpLocalVariable operand;
    /** Creates a <code>InRet</code>. */
    public InRet(String sourcefile, int linenumber, byte[] code, int pc) {
        super(sourcefile, linenumber, code, pc);
	operand = new OpLocalVariable(InGen.u1(code, pc+1));
    }
    /** Returns the local variable operand of the RET instruction. */
    public OpLocalVariable getOperand() { return operand; }
}
