// IllegalAssignmentError.java, created by wbeebee
// Copyright (C) 2001 Wes Beebee <wbeebee@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package javax.realtime;

/**
 * @author Wes Beebee <<a href="mailto:wbeebee@mit.edu">wbeebee@mit.edu</a>>
 */

/** Thrown if the constructor of an <code>ImmortalPhysicalMemory,
 *  LTPhyscalMemory, VTPhysicalMemory, RawMemoryAccess</code>, or
 *  <code>RawMemoryFloatAccess</code> is given an invalid size or
 *  if an accessor method on one of the above classes cause access
 *  to an invalid address.
 */
public class IllegalAssignmentError extends Error {

    /** A constructor for <code>IllegalAssignmentError</code>. */
    public IllegalAssignmentError() {
	super();
    }

    /** A descriptive constructor for <code>IllegalAssignmentError</code>.
     *
     *  @param s The description of the exception.
     */
    public IllegalAssignmentError(String s) {
	super(s);
    }
}
