// AllocationInformation.java, created Thu Mar 30 05:39:25 2000 by cananian
// Copyright (C) 1999 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.Maps;

import harpoon.ClassFile.HCodeElement;
import harpoon.Temp.Temp;
/**
 * An <code>AllocationInformation</code> maps allocation sites
 * to information about the allocation done at that site.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: AllocationInformation.java,v 1.1.2.2 2000-04-04 01:37:40 cananian Exp $
 */
public interface AllocationInformation  {
    
    /** <code>AllocationProperties</code> contains tests for the various
     *  possibly properties of an allocation site.  "Atomic" allocations
     *  (of objects not containing interior pointers) can be done, as well
     *  as stack allocation, thread-local allocation, and pre-thread-start
     *  thread-local allocation.  If none of these properties are true,
     *  the object must be allocated in a global heap. */
    public static interface AllocationProperties {
	/** @return <code>true</code> if the object allocated at this
	 *  site has no interior pointers; that is, it is a primitive
	 *  array, or all fields in the allocated object are primitive.
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
	/** @return <code>true</code> if the object should be allocated on
	 *  *it's own* thread-local heap --- typically this means that
	 *  the allocation site is a thread creation.  If this is
	 *  <code>true</code>, the <code>canBeThreadAllocated()</code>
	 *  should also be <code>true</code>.
	 */
	public boolean useOwnHeap();
	/** @return a <code>Temp</code> which at the allocation site
	 * contains a reference to either the thread object of a
	 * thread-local allocation, or to another object whose lifetime
	 * is correlated with that of the (not-yet-created) thread object.
	 * Returns <code>null</code> if the allocation should use the
	 * heap associated with the "current" thread. 
	 * If this returns non-<code>null</code>, then
	 * <code>useOwnHeap()</code> should return <code>false</code> and
	 * <code>canBeThreadAllocated()</code> should return <code>true</code>.
	 */
	public Temp    allocationHeap();
    }

    /** Query the properties of the allocation at the specified site. */
    public AllocationProperties query(HCodeElement allocationSite);
}
