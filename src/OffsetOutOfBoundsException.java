// OffsetOutOfBoundsException.java, created by wbeebee
// Copyright (C) 2001 Wes Beebee <wbeebee@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package javax.realtime;

/** Thrown if the constructor of an <code>ImmortalPhysicalMemory,
 *  LTPhysicalMemory, VTPhysicalMemory, RawMemoryAccess</code>, or
 *  <code>RawMemoryFloatAccess</code> is given an invalid address.
 *
 * @author Wes Beebee <<a href="mailto:wbeebee@mit.edu">wbeebee@mit.edu</a>>
 */
public class OffsetOutOfBoundsException extends Exception {

    /** A constructor for <code>OffsetOutOfBoundsException</code>. */
    public OffsetOutOfBoundsException() {
	super();
    }

    /** A descriptive constructor for <code>OffsetOutOfBoundsException</code>.
     *
     *  @param s A description of the execution.
     */
    public OffsetOutOfBoundsException(String s) {
	super(s);
    }
}
