// POPER.java, created Wed Jan 20 23:39:35 1999 by cananian
// Copyright (C) 1999 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.LowQuad;

import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HCodeElement;
import harpoon.Temp.Temp;
import harpoon.Temp.TempMap;
import harpoon.Util.Util;
/**
 * <code>POPER</code> is an extended version of
 * <code>harpoon.IR.Quads.OPER</code>, with new opcodes defined in
 * <code>LQop</code>.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: POPER.java,v 1.3.2.1 2002-02-27 08:36:27 cananian Exp $
 */
public class POPER extends harpoon.IR.Quads.OPER {
    
    /** Creates a <code>POPER</code>. */
    public POPER(LowQuadFactory qf, HCodeElement source, 
		 int opcode, Temp dst, Temp[] operands) {
        super(qf, source, opcode, dst, operands);
	if (kind()==LowQuadKind.POPER) // allow subclassing
	    assert LQop.isValid(opcode);
    }
    public int kind() { return LowQuadKind.POPER; }

    public harpoon.IR.Quads.Quad rename(harpoon.IR.Quads.QuadFactory qf,
					TempMap defMap, TempMap useMap) {
	return new POPER((LowQuadFactory)qf, this, opcode,
			 map(defMap, dst), map(useMap, operands));
    }

    public void accept(harpoon.IR.Quads.QuadVisitor v) {
	((LowQuadVisitor)v).visit(this);
    }

    /** Returns a human-readable representation of this Quad. */
    public String toString() {
	StringBuffer sb = new StringBuffer(dst.toString());
	sb.append(" = POPER " + LQop.toString(opcode) + "(");
	for (int i=0; i<operands.length; i++) {
	    sb.append(operands[i].toString());
	    if (i<operands.length-1)
		sb.append(", ");
	}
	sb.append(')');
	return sb.toString();
    }

    // -------------------------------------------------------
    //   Evaluation functions.

    /** Determines the result type of an <code>OPER</code>. */
    public HClass evalType() {
	return LQop.resultType(opcode);
    }

    /** Evaluates a constant value for the result of an <code>OPER</code>, 
     *  given constant values for the operands. */
    public Object evalValue(Object[] opvalues) { 
	return LQop.evaluate(opcode, opvalues);
    }
}
