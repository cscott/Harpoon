// IR_SignalKind.java, created by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.AIRE;

/**
 * <code>IR_SignalKind</code>:
 * An enumerated type must be provided, called <code>IR_SignalKind</code>,
 * which specifies various options associated with predefined 
 * <code>IIR_Signal</code> and <code>IIR_SignalInterfaceDeclaration</code>
 * classes.  The enumeration may be implemented as a true enumerated type
 * or (preferred) as an integer and constant set.  In either case, the
 * type must include the following labels prior to any labels associated
 * with completely new, instantiable IIR extension classes:
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IR_SignalKind.java,v 1.6 2002-02-25 21:03:59 cananian Exp $
 */
public class IR_SignalKind {
    public final static IR_SignalKind IR_NO_SIGNAL_KIND = _(0);
    public final static IR_SignalKind IR_REGISTER_KIND = _(1);
    public final static IR_SignalKind IR_BUS_KIND = _(2);

    public String toString() {
	if (this==IR_NO_SIGNAL_KIND) return "IR_NO_SIGNAL_KIND";
	if (this==IR_REGISTER_KIND) return "IR_REGISTER_KIND";
	if (this==IR_BUS_KIND) return "IR_BUS_KIND";
	throw new Error("Unknown signal kind: "+_signal_kind);
    }

    // Private implementation.
    private final int _signal_kind;
    private IR_SignalKind(int signal_kind) { _signal_kind = signal_kind; }
    private static IR_SignalKind _(int sk) { return new IR_SignalKind(sk); }
}
