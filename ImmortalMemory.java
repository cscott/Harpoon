// ImmortalMemory.java, created by wbeebee
// Copyright (C) 2001 Wes Beebee <wbeebee@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package javax.realtime;

/** <code>ImmortalMemory</code>
 *
 * @author Wes Beebee <<a href="mailto:wbeebee@mit.edu">wbeebee@mit.edu</a>>
 */

public class ImmortalMemory extends MemoryArea {
    private static ImmortalMemory immortalMemory = null;

    private ImmortalMemory() {
	super(1000000000); // Totally bogus
    }

    /** */

    protected native void initNative(long sizeInBytes);
    
    /** */

    protected native void newMemBlock(RealtimeThread rt);

    /** */

    public static ImmortalMemory instance() {
	if (immortalMemory == null) {
	    immortalMemory = new ImmortalMemory();
	}
	return immortalMemory;
    }

    /** */

    public String toString() {
	return "ImmortalMemory: " + super.toString();
    }
}
