// IR_Mode.java, created by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.AIRE;

/**
 * <code>IR_Mode</code>:
 * An enumerated type must be provided, called <code>IR_Mode</code>,
 * which specifies various options associated with predefined 
 * <code>IIR_InterfaceDeclaration</code> classes.
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IR_Mode.java,v 1.5 2002-02-25 21:03:59 cananian Exp $
 */
public class IR_Mode {
    public final static IR_Mode IR_UNKNOWN_MODE = _(0);
    public final static IR_Mode IR_IN_MODE = _(1);
    public final static IR_Mode IR_OUT_MODE = _(2);
    public final static IR_Mode IR_INOUT_MODE = _(3);
    public final static IR_Mode IR_BUFFER_MODE = _(4);
    public final static IR_Mode IR_LINKAGE_MODE = _(5);

    public String toString() {
	if (this==IR_UNKNOWN_MODE) return "IR_UNKNOWN_MODE";
	if (this==IR_IN_MODE) return "IR_IN_MODE";
	if (this==IR_OUT_MODE) return "IR_OUT_MODE";
	if (this==IR_INOUT_MODE) return "IR_INOUT_MODE";
	if (this==IR_BUFFER_MODE) return "IR_BUFFER_MODE";
	if (this==IR_LINKAGE_MODE) return "IR_LINKAGE_MODE";
	throw new Error("Unknown IR_Mode: "+_mode);
    }

    // private implementation
    private final int _mode;
    private IR_Mode(int mode) { _mode = mode; }
    private static IR_Mode _(int m) { return new IR_Mode(m); }
}
