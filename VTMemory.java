// VTMemory.java, created by wbeebee
// Copyright (C) 2001 Wes Beebee <wbeebee@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package javax.realtime;

/** <code>VTMemory</code>
 *
 * @author Wes Beebee <<a href="mailto:wbeebee@mit.edu">wbeebee@mit.edu</a>>
 */

public class VTMemory extends ScopedMemory {

    /** */

    public VTMemory(long initial, long maximum) {
	super(maximum);
    }

    /** */

    public String toString() {
	return "VTMemory: " + super.toString();
    }
}
