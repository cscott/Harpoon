// IR_DelayMechanism.java, created by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.AIRE;

/**
 * <code>IR_DelayMechanism</code>:
 * An enumerated type must be provided, called <code>IR_DelayMechanism</code>,
 * which specifies various options associated with predefined 
 * signal assignement statement classes.
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IR_DelayMechanism.java,v 1.5 2002-02-25 21:03:59 cananian Exp $
 */
public class IR_DelayMechanism {
    public final static IR_DelayMechanism IR_UNKNOWN_DELAY = _(0);
    public final static IR_DelayMechanism IR_INERTIAL_DELAY = _(1);
    public final static IR_DelayMechanism IR_TRANSPORT_DELAY = _(2);

    public String toString() {
	if (this==IR_UNKNOWN_DELAY) return "IR_UNKNOWN_DELAY";
	if (this==IR_INERTIAL_DELAY) return "IR_INERTIAL_DELAY";
	if (this==IR_TRANSPORT_DELAY) return "IR_TRANSPORT_DELAY";
	throw new Error("Unknown IR_DelayMechanism: "+_delay);
    }

    // private implementation
    private final int _delay;
    private IR_DelayMechanism(int delay) { _delay = delay; }
    private static IR_DelayMechanism _(int d) 
    { return new IR_DelayMechanism(d); }
}
