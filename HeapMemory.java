// HeapMemory.java, created by wbeebee
// Copyright (C) 2001 Wes Beebee <wbeebee@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package javax.realtime;
import java.lang.Runtime;

/** <code>HeapMemory</code> represents garbage-collected memory.  
 *
 * @author Wes Beebee <<a href="mailto:wbeebee@mit.edu">wbeebee@mit.edu</a>>
 */

public final class HeapMemory extends MemoryArea {
    /** The one and only HeapMemory. */
    private static HeapMemory theHeap;
    
    private HeapMemory() {  
	super(1000000000); // Totally bogus
	//      super(Runtime.getRuntime().totalMemory());  // still bogus
	//      memoryConsumed = size - Runtime.getRuntime().freeMemory();

	heap = true;
    }

    // METHODS IN SPECS

    /** Return the instance of the one and only HeapMemory. */
    public static HeapMemory instance() {
	if (theHeap == null) { // Bypass static initializer problem.
	    theHeap = new HeapMemory();
	}
	return theHeap;
    }
    
    /** Overrides memoryConsumed() from <code>MemoryArea</code> */
    public long memoryConsumed() {
	// TODO

	return 0;
    }

    /** Overrides memoryRemaining() from <code>MemoryArea</code> */
    public long memoryRemaining() {
	// TODO

	return 0;
    }


    // METHODS NOT IN SPECS


    /** Initialize the native component of the HeapMemory. */
    protected native void initNative(long sizeInBytes);

    /** Print a helpful string describing this HeapMemory. */
    public String toString() {
	return "HeapMemory: " + super.toString();
    }

    /** Create a new array, checking to make sure that we're not in a 
     *  NoHeapRealtimeThread. 
     */
    public synchronized Object newArray(Class type,
					int number) 
	throws IllegalAccessException, InstantiationException, OutOfMemoryError
    {
	checkNoHeap();
	return super.newArray(type, number);
    }

    /** Create a new multi-dimensional array, checking to make sure that 
     *  we're not in a NoHeapRealtimeThread. 
     */

    public synchronized Object newArray(Class type,
					int[] dimensions) 
	throws IllegalAccessException, OutOfMemoryError
    {
	checkNoHeap();
	return super.newArray(type, dimensions);
    }

    /** Create a new object, checking to make sure that we're not in a 
     *  NoHeapRealtimeThread.
     */

    public synchronized Object newInstance(Class type, 
					   Class[] parameterTypes,
					   Object[] parameters) 
	throws IllegalAccessException, InstantiationException,
	       OutOfMemoryError {
	checkNoHeap();
	return super.newInstance(type, parameterTypes, parameters);
    }

    /** Create a new object, checking to make sure that we're not in a 
     *  NoHeapRealtimeThread.
     */

    public synchronized Object newInstance(Class type) 
	throws IllegalAccessException, InstantiationException,
	       OutOfMemoryError {
	checkNoHeap();
	return super.newInstance(type);
    }

    /** Check to make sure that we're not in a NoHeapRealtimeThread 
     */

    public void checkNoHeap() {
	RealtimeThread realtimeThread = 
	    RealtimeThread.currentRealtimeThread();
	if (realtimeThread.noHeap) {
	    throw new IllegalAssignmentError("Cannot assign to " + toString()
					     + " from " + 
					     realtimeThread.toString());
	}
    }
}
