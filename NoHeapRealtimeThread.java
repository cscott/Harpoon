// NoHeapRealtimeThread.java, created by wbeebee
// Copyright (C) 2001 Wes Beebee <wbeebee@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package javax.realtime;

/** <code>NoHeapRealtimeThread</code> is a <code>RealtimeThread</code> that
 *  cannot access the heap or write to the heap or manipulate references to
 *  the heap, but does have a higher priority than the 
 *  <code>GarbageCollector</code>.  Remember, you can't .start a 
 *  heap-allocated <code>NoHeapRealtimeThread</code>.
 * @author Wes Beebee <<a href="mailto:wbeebee@mit.edu">wbeebee@mit.edu</a>>
 */

public class NoHeapRealtimeThread extends RealtimeThread {

    /** Construct a <code>NoHeapRealtimeThread</code> which will execute in the
     *  given <code>MemoryArea</code>
     */
    public NoHeapRealtimeThread(MemoryArea area) 
	throws IllegalArgumentException 
    {
	super(area);
	setup(area);
    }

    /** Construct a <code>NoHeapRealtimeThread</code> which will execute 
     *  <code>logic</code> in the given <code>MemoryArea</code> 
     */
    public NoHeapRealtimeThread(MemoryArea area, Runnable logic) 
	throws IllegalArgumentException 
    {
	super(area, logic);
	setup(area);
    }

    /** Setup some state for the constructors */
    private void setup(MemoryArea area) throws IllegalArgumentException {
	if ((area == null) || area.heap) {
	    throw new IllegalArgumentException("invalid MemoryArea");
	} else {
	    mem = area;
	}
	noHeap = true;
    }

    /** Construct a new <code>NoHeapRealtimeThread</code> that will inherit
     *  the properties described in <code>MemoryParameters</code> and will
     *  run <code>logic</code>.
     */
    public NoHeapRealtimeThread(MemoryParameters mp, Runnable logic) {
	this(mp.getMemoryArea(), logic);
    }

    /** Check to see if a write is possible to the given object. 
     *  Warning: this method can only be used when we're not really running
     *  <code>NoHeapRealtimeThreads</code> for real, because you can't access
     *  the object at all in a real <code>NoHeapRealtimeThread</code>. 
     */
    
    public void checkNoHeapWrite(Object obj) {
	if ((obj != null) && (obj.memoryArea != null) && obj.memoryArea.heap) {
	    throw new IllegalAssignmentError("Cannot assign " +
					     obj.memoryArea.toString() +
					     " from " + toString());
	}
    }

    /** Check to see if a read is possible from the given object.
     *  Warning: this method can only be used when we're not really running
     *  <code>NoHeapRealtimeThreads</code> for real, because you can't access
     *  the object at all in a real <code>NoHeapRealtimeThread</code>.
     */

    public void checkNoHeapRead(Object obj) {
	if ((obj != null) && (obj.memoryArea != null) && obj.memoryArea.heap) {
	    throw new MemoryAccessError("Cannot read " + 
					obj.memoryArea.toString() +
					" from " + toString());
	}
    }

    /** Return a String describing this thread.
     */

    public String toString() {
	return "NoHeapRealtimeThread";
    }

    /** A print method that's safe to call from a NoHeapRealtimeThread.
     */
    public native static void print(String s);
    public native static void print(double d);
    public native static void print(int n);
    public native static void print(long l);
}
