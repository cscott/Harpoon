// ImmortalMemory.java, created by wbeebee
// Copyright (C) 2001 Wes Beebee <wbeebee@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package javax.realtime;

/**
 * @author Wes Beebee <<a href="mailto:wbeebee@mit.edu">wbeebee@mit.edu</a>>
 */

/** <code>ImmortalMemory</code> is a memory resource that is shared among
 *  all threads. Objects allocated in the immortal memory live until the
 *  end of the application. Objects in immoral memory are nver subjected
 *  to garbage collection, although some GC algorithms may require a scan
 *  of the immortal memory. An immortal object may only contain reference
 *  to other immortal objects or to heap objects. Unlike standard Java
 *  heap objects, immoratl objects continue to exist even after there are
 *  no other references to them.
 */
public /* specs says it's final */ class ImmortalMemory extends MemoryArea {
    private static ImmortalMemory immortalMemory;
   
    /* Don't use this! */
    public ImmortalMemory() {
	super(1000000000); // Totally bogus
    }

    /** Returns a pointer to the singleton <code>ImmortalMemory</code> space.
     *
     *  @return The singleton <code>ImmortalMemory</code> object.
     */
    public static ImmortalMemory instance() {
	if (immortalMemory == null) {
	    immortalMemory = (ImmortalMemory)((new ImmortalMemory()).shadow);
	    immortalMemory.shadow = immortalMemory.memoryArea = immortalMemory;
	}
	return immortalMemory;
    }

    protected native void initNative(long sizeInBytes);
    
    public String toString() {
	return "ImmortalMemory: " + super.toString();
    }
}
