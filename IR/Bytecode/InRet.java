// InRet.java, created Mon Dec 21 15:48:53 1998 by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.Bytecode;

/**
 * <code>InRet</code> is an InCti with an operand.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: InRet.java,v 1.1.2.4 2001-09-17 21:47:33 cananian Exp $
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

    public String toString() {
	return Op.toString(opcode)+" "+operand;
    }
}
