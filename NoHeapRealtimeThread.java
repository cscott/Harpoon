// NoHeapRealtimeThread.java, created by wbeebee
// Copyright (C) 2001 Wes Beebee <wbeebee@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package javax.realtime;

/** <code>NoHeapRealtimeThread</code>
 *
 * @author Wes Beebee <<a href="mailto:wbeebee@mit.edu">wbeebee@mit.edu</a>>
 */

public class NoHeapRealtimeThread extends RealtimeThread {

    /** */

    NoHeapRealtimeThread(MemoryArea area) 
	throws IllegalArgumentException 
    {
	this(area, null);
    }

    /** */

    NoHeapRealtimeThread(MemoryArea area, Runnable logic) 
	throws IllegalArgumentException 
    {
	super(area, logic);
	if ((area == null) || area.heap) {
	    throw new IllegalArgumentException("invalid MemoryArea in " +
					       "NoHeapRealtimeThread constructor");
	} else {
	    mem = area;
	}
	noHeap = true;
    }

    /** */
    
    public void checkNoHeapWrite(Object obj) {
	if ((obj != null) && (obj.memoryArea != null) && obj.memoryArea.heap) {
	    throw new IllegalAssignmentError("Cannot assign " +
					     obj.memoryArea.toString() +
					     " from " + toString());
	}
    }

    /** */

    public void checkNoHeapRead(Object obj) {
	if ((obj != null) && (obj.memoryArea != null) && obj.memoryArea.heap) {
	    throw new MemoryAccessError("Cannot read " + 
					obj.memoryArea.toString() +
					" from " + toString());
	}
    }

    public String toString() {
	return "NoHeapRealtimeThread";
    }
}
