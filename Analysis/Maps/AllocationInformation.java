// AllocationInformation.java, created Thu Mar 30 05:39:25 2000 by cananian
// Copyright (C) 1999 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.Maps;

import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HField;
import harpoon.ClassFile.HCodeElement;
import harpoon.Temp.Temp;
/**
 * An <code>AllocationInformation</code> maps allocation sites
 * to information about the allocation done at that site.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: AllocationInformation.java,v 1.6 2003-02-08 23:26:44 salcianu Exp $
 */
public interface AllocationInformation<HCE extends HCodeElement>  {
    
    /** <code>AllocationProperties</code> contains tests for the various
     *  possibly properties of an allocation site.  "Atomic" allocations
     *  (of objects not containing interior pointers) can be done, as well
     *  as stack allocation, thread-local allocation, and pre-thread-start
     *  thread-local allocation.  If none of these properties are true,
     *  the object must be allocated in a global heap. */
    public static interface AllocationProperties {
	/** @return <code>true</code> if the object allocated at this
	 *  site has interior pointers; that is, it is not a primitive
	 *  array, and some field in the allocated object is not primitive.
	 */
	public boolean hasInteriorPointers();
	/** @return <code>true</code> if the object can be allocated on
	 *  the stack; that is, the lifetime of the object does not
	 *  exceed the execution of the method containing the allocation.
	 */
	public boolean canBeStackAllocated();
	/** @return <code>true</code> if the object can be allocated on
	 *  a thread-local heap; that is, the lifetime of the object does
	 *  not exceed the lifetime of the thread object specified
	 *  by the <code>allocationHeap</code> method. */
	public boolean canBeThreadAllocated();
	/** @return <code>true</code> if a thread-local heap should be
	 *  associated with this object --- typically this means that
	 *  the allocation site is a thread creation.  If this is
	 *  <code>true</code> and <code>canBeThreadAllocated()</code>
	 *  is also <code>true</code>, then the new object will be
	 *  itself allocated on the created heap; otherwise the
	 *  new object will be globally allocated.
	 */
	public boolean makeHeap();
	/** @return a <code>Temp</code> which at the allocation site
	 * contains a reference to either the thread object of a
	 * thread-local allocation, or to another object whose lifetime
	 * is correlated with that of the (not-yet-created) thread object.
	 * Returns <code>null</code> if the allocation should use the
	 * heap associated with the "current" thread. 
	 * If this returns non-<code>null</code>, then
	 * <code>makeHeap()</code> should return <code>false</code> and
	 * <code>canBeThreadAllocated()</code> should return <code>true</code>.
	 */
	public Temp    allocationHeap();
	/** @return the <code>HClass</code> representing the
	 * "actual" or instantiated class of the object for which
	 * memory is being allocated (as opposed to the declared
	 * class).
	 */
	public HClass actualClass();

	/* Each sync on this object consists of NOTHING. */
	public boolean noSync();
	/** @return <code>true</code> if the dynamic write barrier 
	 *  flag needs to be set when the object is allocated--this 
	 *  typically means that one or more write barriers associated 
	 *  with stores into the object created at this allocation site
	 *  have been optimistically removed. */
	public boolean setDynamicWBFlag();

	/** @return an <code>HField</code> description of a static
	    field that points (at runtime) to a preallocated memory
	    chunk to be used for this allocation site.  Returns null
	    if we cannot preallocate this allocation site. */
	public HField getMemoryChunkField();

	/** @return an unique integer ID for the allocation site. 
	    Useful for instrumentation purposes. */
	public int getUniqueID();
    }

    /** Query the properties of the allocation at the specified site. */
    public AllocationProperties query(HCE allocationSite);
}
