// LTMemory.java, created by wbeebee
// Copyright (C) 2001 Wes Beebee <wbeebee@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package javax.realtime;

/** <code>LTMemory</code>
 *
 * @author Wes Beebee <<a href="mailto:wbeebee@mit.edu">wbeebee@mit.edu</a>>
 */

public class LTMemory extends ScopedMemory {
    /** */

    public LTMemory(long initialSizeInBytes, long maxSizeInBytes) {
	super(maxSizeInBytes);
    }

    /** */

    protected native void initNative(long sizeInBytes);

    /** */
    
    protected native void newMemBlock(RealtimeThread rt);

    /** */

    public String toString() {
	return "LTMemory: " + super.toString();
    }

}
