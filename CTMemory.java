// CTMemory.java, created by wbeebee
// Copyright (C) 2001 Wes Beebee <wbeebee@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package javax.realtime;

/** <code>CTMemory</code>
 *
 * @author Wes Beebee <<a href="mailto:wbeebee@mit.edu">wbeebee@mit.edu</a>>
 */

public class CTMemory extends ScopedMemory {

    /** */

    public CTMemory(long size) {
	super(size);
    }

    /** */
    
    public String toString() {
	return "CTMemory: " + super.toString();
    }

    /** */

    protected native void initNative(long sizeInBytes);

    /** */
    
    protected native void newMemBlock(RealtimeThread rt);
}
