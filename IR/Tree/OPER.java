// OPER.java, created Wed Jan 13 21:14:57 1999 by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.Tree;

import harpoon.ClassFile.HCodeElement;
import harpoon.Util.Util;

/**
 * <code>OPER</code> objects are expressions which stand for the result
 * of applying some operator to subexpressions.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>, based on
 *          <i>Modern Compiler Implementation in Java</i> by Andrew Appel.
 * @version $Id: OPER.java,v 1.3 2002-02-26 22:46:10 cananian Exp $
 */
public abstract class OPER extends Exp {
    /** An enumerated type encoding the operator.
     * @see Bop
     * @see Uop
     */
    public final int op;
    /** Type of the operands (not necessarily the result type). */
    public final int optype;

    public OPER(TreeFactory tf, HCodeElement source,
		int optype, int op, int arity) {
	super(tf, source, arity);
	Util.ASSERT(Type.isValid(optype));
	// subclass must verify validity of op.
	this.op = op; this.optype = optype;
    }
    /** Accept a visitor */
    public void accept(TreeVisitor v) { v.visit(this); }

    // <code>Typed</code> interface.

    /** Return result type. */
    public abstract int type();
    /** Return type of operands (not necessarily the result type). */
    public int operandType() { return optype; }
}
