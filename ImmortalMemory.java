// ImmortalMemory.java, created by wbeebee
// Copyright (C) 2001 Wes Beebee <wbeebee@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package javax.realtime;

/** <code>ImmortalMemory</code>
 *
 * @author Wes Beebee <<a href="mailto:wbeebee@mit.edu">wbeebee@mit.edu</a>>
 */

public class ImmortalMemory extends MemoryArea {
    private static ImmortalMemory immortalMemory;

    private ImmortalMemory() {
	super(1000000000); // Totally bogus
    }

    /** */

    protected native void initNative(long sizeInBytes);
    
    /** */

    protected native void newMemBlock(RealtimeThread rt);

    /** Returns the only ImmortalMemory instance (which was itself allocated out of
     *  ImmortalMemory - the native component is actually smart enough to not be 
     *  confused by this and RTJmalloc will allocate it out of itself).
     */ 
    public static ImmortalMemory instance() {
	if (immortalMemory == null) {
	    if (RealtimeThread.RTJ_init_in_progress) {
		return (ImmortalMemory)("Constant ImmortalMemory".memoryArea);
	    }
	    (immortalMemory = new ImmortalMemory()).enter(new Runnable() {
		    public void run() {
			(immortalMemory = new ImmortalMemory()).memoryArea = 
			    immortalMemory;
		    }
		});
	}
	return immortalMemory;
    }

    /** */

    public String toString() {
	return "ImmortalMemory: " + super.toString();
    }
}
