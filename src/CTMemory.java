// CTMemory.java, created by wbeebee
// Copyright (C) 2001 Wes Beebee <wbeebee@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package javax.realtime;

/** <code>CTMemory</code> represents a constant-sized memory scope.  
 *  It uses stack allocation and is very fast and has predictable performance.
 *
 * @author Wes Beebee <<a href="mailto:wbeebee@mit.edu">wbeebee@mit.edu</a>>
 */

public class CTMemory extends ScopedMemory {

    /** This constructs a CTMemory of the appropriate size (the maximum allowed
     *  to be allocated in the scope).  The performance
     *  hit of allocating a large block of memory is taken when this constructor
     *  is called, or reused after refCount drops to zero.  
     */

    private long minimum;

    public CTMemory(long size) {
	super(size, size);
	initNative(size, size);
	postSetup();
    }
  
    public CTMemory(long minimum, long maximum) {
	super(maximum);
	initNative(minimum, maximum);
	postSetup();
    }
  
    /** Returns a representation of this CTMemory object 
     */
    
    public String toString() {
	return "CTMemory: " + super.toString();
    }

    /** Initialize the native component of this MemoryArea 
     *  (set up the MemBlock) 
     */
    
    protected void initNative(long sizeInBytes) {}

    private native void initNative(long minimum, long maximum);

    /** Invoke this method when you're finished with the MemoryArea 
     *	(could be a finalizer if we had finalizers...) 
     */

    public void done() {
      doneNative();
    }

    /** This will actually free the memory (if refcount = 0). 
     */

    private native void doneNative();
}
