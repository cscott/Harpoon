// IR_Pure.java, created by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.AIRE;

/**
 * <code>IR_Pure</code>:
 * An enumerated type must be provided, called <code>IR_Pure</code>,
 * which specifies various options associated with predefined 
 * <code>IIR_FunctionDeclaration</code> classes.
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IR_Pure.java,v 1.5 2002-02-25 21:03:59 cananian Exp $
 */
public class IR_Pure {
    public final static IR_Pure IR_UNKNOWN_PURE = _(0);
    public final static IR_Pure IR_PURE_FUNCTION = _(1);
    public final static IR_Pure IR_IMPURE_FUNCTION = _(2);
    public final static IR_Pure IR_PURE_PROCEDURAL = _(3); // IIR ONLY!
    public final static IR_Pure IR_IMPURE_PROCEDURAL = _(4); // IIR ONLY!

    public String toString() {
	if (this==IR_UNKNOWN_PURE) return "IR_UNKNOWN_PURE";
	if (this==IR_PURE_FUNCTION) return "IR_PURE_FUNCTION";
	if (this==IR_IMPURE_FUNCTION) return "IR_IMPURE_FUNCTION";
	if (this==IR_PURE_PROCEDURAL) return "IR_PURE_PROCEDURAL";
	if (this==IR_IMPURE_PROCEDURAL) return "IR_IMPURE_PROCEDURAL";
	throw new Error("Unknown IR_Pure: "+_pure);
    }

    // private implementation
    private final int _pure;
    private IR_Pure(int pure) { _pure = pure; }
    private static IR_Pure _(int p) { return new IR_Pure(p); }
}
