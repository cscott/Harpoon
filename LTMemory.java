// LTMemory.java, created by wbeebee
// Copyright (C) 2001 Wes Beebee <wbeebee@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package javax.realtime;

/**
 * @author Wes Beebee <<a href="mailto:wbeebee@mit.edu">wbeebee@mit.edu</a>>
 */

/** <code>LTMemory</code> represents a memory area, allocated per
 *  <code>RealtimeThread</code>, or for a group of real-time threads,
 *  guaranteed by the system to have linear time allocation. The memory
 *  area described by a <code>LTMemory</code> instance does not exist in
 *  the Java heap, and is not subject to garbage collection. Thus, it is
 *  safe to use a <code>LTMemory</code> object as the memory area associated
 *  with a <code>NoHeapRealtimeThread</code>, or to enter the memory area
 *  using the <code>enter()</code> method within a <code>NoHeapRealtimeThread</code>.
 *  An <code>LTMemory</code> area has an initial size. Enough memory must be
 *  committed by the completion of the constructor to satisfy this initial
 *  requirement. (Committed means that this memory must always be available
 *  for allocation). The initial memory allocation must behave, with respect
 *  to successful allocation, as if it were contiguous; i.e., a correct
 *  implementation must guarantee that any sequence of object allocations
 *  that could ever without exceeding a specified initial memory size will
 *  always succeed without exceeding that initial memory size and succeed
 *  for any instance of <code>LTMemory</code> with that initial memory size.
 *  <i>(Note: It is important to understand that the above statement does
 *  <b>not require that if the initial memory size is N and
 *  (sizeof(object1) + sizeof(object2) + ... + sizeof(objectn) = N) the
 *  allocations of objects 1 through n will necessarily succeed.)</b></i>
 *  Execution time of an allocator aloocating from this initial area must
 *  be linear in the size of the allocated object. Execution time of an
 *  allocator allocating from memory between initial and maximum is
 *  allowed to vary. Furthermore, the underlying system is not required to
 *  guarantee that memory between initial and maximum will always be available.
 *  (Node: to ensure that all requested memory is available set initial and
 *  maximum to the same value).
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

    /** Create an <code>LTMemory</code> of the given size.
     *
     *  @param initialSizeInBytes The size in bytes of the memory to allocate for
     *                            this area. This memory must be committed before
     *                            the completion of the constructor.
     *  @param maxSizeInBytes The size in bytes of the memory to allocate for this area.
     */
    public LTMemory(long initialSizeInBytes,
		    long maxSizeInBytes) {
	super(maxSizeInBytes);
	initNative(initialSizeInBytes, maxSizeInBytes);
    }

    /** Create an <code>LTMemory</code> of the given size and logic.
     *
     *  @param initialSizeInBytes The size in bytes of the memory to allocate for
     *                            this area. This memory must be committed before
     *                            the completion of the constructor.
     *  @param maxSizeInBytes The size in bytes of the memory to allocate for this area.
     *  @param logic The <code>run()</code> method of the given <code>Runnable</code>
     *               will be executed using <code>this</code> as its initial memory area.
     */
    public LTMemory(long initialSizeInBytes,
		    long maxSizeInBytes,
		    Runnable logic) {
	this(initialSizeInBytes, maxSizeInBytes);
	this.logic = logic;
    }

    /** Creates a <code>LTMemory</code> of the given size estimated by two instances of
     *  <code>SizeEstimator</code>.
     *
     *  @param initial An instance of <code>SizeEstimator</code> used to give an estimate
     *                 of the initial size. This memory must be committed before the
     *                 completion of the constructor.
     *
     *  @param maximum An instance of <code>SizeEstimator</code> used to give an estimate
     *                 for the maximum bytes to allocate for this area.
     */
    public LTMemory(SizeEstimator initial,
		    SizeEstimator maximum) {
	this(initial.getEstimate(), maximum.getEstimate());
    }

    /** Creates a <code>LTMemory</code> of the given size estimated by two instances of
     *  <code>SizeEstimator</code> and logic.
     *
     *  @param initial An instance of <code>SizeEstimator</code> used to give an estimate
     *                 of the initial size. This memory must be committed before the
     *                 completion of the constructor.
     *  @param maximum An instance of <code>SizeEstimator</code> used to give an estimate
     *                 for the maximum bytes to allocate for this area.
     *  @param logic The <code>run()</code> of the given <code>Runnable</code> will be
     *               executed using <code>this</code> as its initial memory area.
     */
    public LTMemory(SizeEstimator initial,
		    SizeEstimator maximum,
		    Runnable logic) {
	this(initial, maximum);
	this.logic = logic;
    }

    /** Creates a <code>LTMemory</code> of the given size. */
    public LTMemory(long size) {
	super(size);
	initNative(size, size);
    }

    /** Gets the maximum allowable size for <code>this</code>.
     *
     *  @return The maximum size for <code>this</code..
     */
    public long getMaximumSize() {
	return size;
    }

    /** Prints the string "LTMemory".
     *
     *  @return The string "LTMemory".
     */
    public String toString() {
	return "LTMemory";
    }



    /** Initialize the native component of this MemoryArea (set up the MemBlock) */
    private native void initNative(long minimum, long maximum);

    /** Invoke this method when you're finished with the MemoryArea 
     *	(could be a finalizer if we had finalizers...)
     */

    public void done() {
	doneNative();
    }

    /** This will actually free the memory (if refcount = 0). */
    private native void doneNative();
}
