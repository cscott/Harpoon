// VTMemory.java, created by wbeebee
// Copyright (C) 2001 Wes Beebee <wbeebee@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package javax.realtime;

/**
 * @author Wes Beebee <<a href="mailto:wbeebee@mit.edu">wbeebee@mit.edu</a>>
 */

/** The execution time of an allocation from a <code>VTMemory</code> area may
 *  take a variable amount of time. However, since <code>VTMemory</code> areas
 *  are not subject to garbage collection and objects within it may not be moved,
 *  these areas can be used by instances of <code>NoHeapRealtimeThread</code>.
 */
public class VTMemory extends ScopedMemory {

    /** The logic associated with <code>this</code> */
    Runnable logic;

    /** Creates a <code>VTMemory</code> of the given size. */
    public VTMemory(long initialSizeInBytes, long maxSizeInBytes) {
	super(maxSizeInBytes);
    }

    /** Creates a <code>VTMemory</code> of the given size and logic. */
    public VTMemory(long initialSizeInBytes, long maxSizeInBytes,
		    Runnable logic) {
	this(initialSizeInBytes, maxSizeInBytes);
	this.logic = logic;
    }

    /** Creates a <code>VTMemory</code> of the given size estimated by
     *  two instances of <code>SizeEstimator</code>.
     */
    public VTMemory(SizeEstimator initial, SizeEstimator maximum) {
	this(initial.getEstimate(), maximum.getEstimate());
    }

    /** Creates a <code>VTMemory</code> of the given size estimated by
     *  two instances of <code>SizeEstimator</code> and logic.
     */
    public VTMemory(SizeEstimator initial, SizeEstimator maximum,
		    Runnable logic) {
	this(initial, maximum);
	this.logic = logic;
    }

    /** Alternate constructor, with no limits */
    public VTMemory() {
	super(0);
    }

    /** Return the value which defines the maximum size to which this can grow. */
    public long getMaximumSize() {
	return size;
    }

    /** Return a helpful string describing this VTMemory. */
    public String toString() {
	return "VTMemory: " + super.toString();
    }

    /** Initialize the native component of this VTMemory. */
    protected native void initNative(long sizeInBytes);
}
