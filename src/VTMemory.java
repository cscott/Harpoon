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

    /** Creates a <code>VTMemory</code> with the given parameters.
     *
     *  @param initial The size in bytes of the memory to initially allocate for
     *                 this area.
     *  @param maximum The maximum size in bytes of this memory area to which
     *                 the size may grow.
     */
    public VTMemory(long initialSizeInBytes, long maxSizeInBytes) {
	super(maxSizeInBytes);
    }

    /** Creates a <code>VTMemory</code> with the given parameters.
     *
     *  @param initial The size in bytes of the memory to initially allocate for
     *                 this area.
     *  @param maximum The maximum size in bytes of this memory area to which
     *                 the size may grow.
     *  @param logic An instance of <code>java.lang.Runnable</code> whose
     *               <code>run()</code> method will use <code>this</code> as its
     *               initial memory area.
     */
    public VTMemory(long initialSizeInBytes, long maxSizeInBytes,
		    Runnable logic) {
	this(initialSizeInBytes, maxSizeInBytes);
	this.logic = logic;
    }

    /** Creates a <code>VTMemory</code> with the given parameters.
     *
     *  @param initial The size in bytes of the memory to initially allocate for
     *                 this area.
     *  @param maximum The maximum size in bytes of this memory area to which
     *                 the size may grow estimated by an instance of
     *                 <code>SizeEstimator</code>.
     */
    public VTMemory(SizeEstimator initial, SizeEstimator maximum) {
	this(initial.getEstimate(), maximum.getEstimate());
    }

    /** Creates a <code>VTMemory</code> with the given parameters.
     *
     *  @param initial The size in bytes of the memory to initially allocate for
     *                 this area.
     *  @param maximum The maximum size in bytes of this memory area to which
     *                 the size may grow estimated by an instance of
     *                 <code>SizeEstimator</code>.
     *  @param logic An instance of <code>java.lang.Runnable</code> whose
     *               <code>run()</code> method will use <code>this<code> as its
     *               initial memory area.
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

    /** Gets the value of the maximum size to which <code>this</code> can grow.
     *
     *  @return The maximum size value.
     */
    public long getMaximumSize() {
	return size;
    }

    /** Create a string representing the name of <code>this</code>.
     *
     *  @return A string representing the name of <code>this</code>.
     */
    public String toString() {
	return "VTMemory: " + super.toString();
    }

    /** Initialize the native component of this VTMemory. */
    protected native void initNative(long sizeInBytes);
}
