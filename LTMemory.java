// LTMemory.java, created by wbeebee
// Copyright (C) 2001 Wes Beebee <wbeebee@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package javax.realtime;

/** <code>LTMemory</code> represents a linear-time memory scope.  
 *  It uses stack allocation and is very fast and has predictable performance.
 * @author Wes Beebee <<a href="mailto:wbeebee@mit.edu">wbeebee@mit.edu</a>>
 */

public class LTMemory extends ScopedMemory {

    /** The logic associated with <code>this</code>. */
    Runnable logic;

    /** This constructs a LTMemory of the appropriate size (the maximum allowed
     *  to be allocated in the scope).  The performance hit of allocating a 
     *  large block of memory is taken when this constructor
     *  is called.  
     */

    protected void initNative(long sizeInBytes) {}

    public LTMemory(long initialSizeInBytes,
		    long maxSizeInBytes) {
	super(maxSizeInBytes);
	initNative(initialSizeInBytes, maxSizeInBytes);
    }
    
    public LTMemory(long initialSizeInBytes,
		    long maxSizeInBytes,
		    Runnable logic) {
	this(initialSizeInBytes, maxSizeInBytes);
	this.logic = logic;
    }

    public LTMemory(SizeEstimator initial,
		    SizeEstimator maximum) {
	// TODO

	// This line inserted only to make everything compile!
	super(maximum);
    }

    public LTMemory(SizeEstimator initial,
		    SizeEstimator maximum,
		    Runnable logic) {
	this(initial, maximum);
	this.logic = logic;
    }

    // CONSTRUCTORS NOT IN SPECS

    public LTMemory(long size) {
	super(size);
	initNative(size, size);
    }


    // METHODS IN SPECS

    /** Return the value which defines the maximum size to which
     *  this can grow.
     */
    public long getMaximumSize() {
	// TODO

	return 0;
    }

    /** Returns a representation of this LTMemory object */
    
    public String toString() {
	return "LTMemory: " + super.toString();
    }


    // METHODS NOT IN SPECS

    /** Initialize the native component of this MemoryArea 
     *  (set up the MemBlock) */
    
    private native void initNative(long minimum, long maximum);

    /** Invoke this method when you're finished with the MemoryArea 
     *	(could be a finalizer if we had finalizers...) */

    public void done() {
	doneNative();
    }

    /** This will actually free the memory (if refcount = 0). */
    private native void doneNative();
}
