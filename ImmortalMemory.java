// ImmortalMemory.java, created by wbeebee
// Copyright (C) 2001 Wes Beebee <wbeebee@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package javax.realtime;

/** <code>ImmortalMemory</code> is a <code>MemoryArea</code> which allows
 *  access from anywhere, access to the heap, and all objects allocated
 *  in this live for the life of the program.
 * @author Wes Beebee <<a href="mailto:wbeebee@mit.edu">wbeebee@mit.edu</a>>
 */

public class ImmortalMemory extends MemoryArea {
    private static ImmortalMemory immortalMemory;
   
    /* Don't use this! */
    public ImmortalMemory() {
	super(1000000000); // Totally bogus
    }

    /** */

    protected native void initNative(long sizeInBytes);
    
    /** Returns the only ImmortalMemory instance (which was itself allocated
     *  out of ImmortalMemory) 
     */ 
    public static ImmortalMemory instance() {
	if (immortalMemory == null) {
	    immortalMemory = (ImmortalMemory)((new ImmortalMemory()).shadow);
	    immortalMemory.shadow = immortalMemory.memoryArea = immortalMemory;
	}
	return immortalMemory;
    }

    /** */

    public String toString() {
	return "ImmortalMemory: " + super.toString();
    }
}
