// RefCountArea.java, created by wbeebee
// Copyright (C) 2001 Wes Beebee <wbeebee@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package javax.realtime;

/** <code>RefCountArea</code> is a new <code>MemoryArea</code> which
 *  allows access from anywhere, access to <code>ImmortalMemory</code>
 *  and the heap, where objects live as long as there are references
 *  to them.  Performance is upper-bounded by a constant for every assignment
 *  and for every allocation.  No pauses at any other time are permitted.
 *  Cycles are strictly prohibited (use roles analysis).  Performance
 *  for this <code>MemoryArea</code> is expected to be slightly slower
 *  than for <code>VTMemory</code>'s.
 * @author Wes Beebee <<a href="mailto:wbeebee@mit.edu">wbeebee@mit.edu</a>>
 */

public class RefCountArea extends ImmortalMemory {
    private static RefCountArea refCountArea;

    /* Don't use this! */
    public RefCountArea() {
	super();
    }

    /** Initialize the native component of this <code>MemoryArea</code>. */
    
    protected native void initNative(long sizeInBytes);

    /** Returns the only RefCountArea instance, allocated out of 
     *  an ImmortalMemory. 
     */

    public static RefCountArea refInstance() {
	if (refCountArea == null) {
	    if (RealtimeThread.RTJ_init_in_progress) {
		return null;
	    }
	    try {
		refCountArea = (RefCountArea)
		    ImmortalMemory.instance().newInstance(RefCountArea.class);
	    } catch (Exception e) {
		throw new Error("Can't instantiate RefCountArea:" + e.toString());
	    }
	}
	return refCountArea;
    }

    /** These two methods will go away in the future... */
    public native void INCREF(Object o);
    public native void DECREF(Object o);

    public String toString() {
	return "RefCountArea: " + super.toString();
    }
}
