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
 * @version $Id: AllocationInformation.java,v 1.1.2.1 2000-03-30 10:53:13 cananian Exp $
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
	boolean hasInteriorPointers();
	/** @return <code>true</code> if the object can be allocated on
	 *  the stack; that is, the lifetime of the object does not
	 *  exceed the execution of the method containing the allocation.
	 */
	boolean canBeStackAllocated();
	/** @return <code>true</code> if the object can be allocated on
	 *  a thread-local heap; that is, the lifetime of the object does
	 *  not exceed the lifetime of the thread object specified
	 *  by the <code>allocationHeap</code> method. */
	boolean canBeThreadAllocated();
	/** @return a <code>Temp</code> which at the allocation site
	 * contains a reference to either the thread object of a
	 * thread-local allocation, or to another object whose lifetime
	 * is correlated with that of the (not-yet-created) thread object.
	 * Returns <code>null</code> if the allocation should use the
	 * heap associated with the "current" thread. */
	Temp    allocationHeap();
    }

    /** Query the properties of the allocation at the specified site. */
    AllocationProperties query(HCodeElement allocationSite);
}
