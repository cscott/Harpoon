// HeapMemory.java, created by wbeebee
// Copyright (C) 2001 Wes Beebee <wbeebee@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package javax.realtime;
import java.lang.Runtime;

// Memory left is kinda bogus - may want to fix sometime.

/** <code>HeapMemory</code>
 *
 * @author Wes Beebee <<a href="mailto:wbeebee@mit.edu">wbeebee@mit.edu</a>>
 */

public final class HeapMemory extends MemoryArea {
    private static HeapMemory theHeap = null;
    
    private HeapMemory() {  
	super(1000000000); // Totally bogus
	//      super(Runtime.getRuntime().totalMemory());  // still bogus
	//      memoryConsumed = size - Runtime.getRuntime().freeMemory();

	heap = true;
    }

    /** */
    
    protected native void initNative(long sizeInBytes);

    /** */

    protected native void newMemBlock(RealtimeThread rt);

    /** */
 
    public static HeapMemory instance() {
	if (theHeap == null) { // Bypass static initializer problem.
	    theHeap = new HeapMemory();
	}
	return theHeap;
    }
    
    /** */

    public String toString() {
	return "HeapMemory: " + super.toString();
    }

    public synchronized Object newArray(Class type,
					int number) 
	throws IllegalAccessException, InstantiationException, OutOfMemoryError
    {
	checkNoHeap();
	return super.newArray(type, number);
    }

    public synchronized Object newArray(Class type,
					int[] dimensions) 
	throws IllegalAccessException, OutOfMemoryError
    {
	checkNoHeap();
	return super.newArray(type, dimensions);
    }

    public synchronized Object newInstance(Class type, 
					   Class[] parameterTypes,
					   Object[] parameters) 
	throws IllegalAccessException, InstantiationException,
	       OutOfMemoryError {
	checkNoHeap();
	return super.newInstance(type, parameterTypes, parameters);
    }

    public synchronized Object newInstance(Class type) 
	throws IllegalAccessException, InstantiationException,
	       OutOfMemoryError {
	checkNoHeap();
	return super.newInstance(type);
    }

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
