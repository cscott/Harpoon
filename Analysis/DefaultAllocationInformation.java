// DefaultAllocationInformation.java, created Mon Apr  3 18:46:15 2000 by cananian
// Copyright (C) 2000 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis;

import harpoon.Analysis.Maps.AllocationInformation;
import harpoon.Analysis.Maps.AllocationInformation.AllocationProperties;
import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HCodeElement;
import harpoon.ClassFile.HField;
import harpoon.Temp.Temp;
import harpoon.Util.Util;
/**
 * <code>DefaultAllocationInformation</code> returns a simple
 * no-analysis <code>AllocationInformation</code> structure which
 * works for allocation sites in quad form.  The
 * <code>DefaultAllocationInformation</code> will conservatively say
 * that nothing can be stack or thread-locally allocated.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: DefaultAllocationInformation.java,v 1.3.2.1 2002-02-27 08:30:23 cananian Exp $
 */
public class DefaultAllocationInformation
    implements AllocationInformation, java.io.Serializable {
    
    /** A static instance of the singleton 
     *  <code>DefaultAllocationInformation</code> object. */
    public static final AllocationInformation SINGLETON =
	new DefaultAllocationInformation();

    /** Creates a <code>DefaultAllocationInformation</code>. */
    private DefaultAllocationInformation() { }

    /** Return an <code>AllocationProperties</code> object for the given
     *  allocation site.  The allocation site must be either a
     *  <code>harpoon.IR.Quads.NEW</code> or a
     *  <code>harpoon.IR.Quads.ANEW</code>. */
    public AllocationProperties query(HCodeElement allocationSite) {
	if (allocationSite instanceof harpoon.IR.Quads.NEW)
	    return _hasInteriorPointers
		(((harpoon.IR.Quads.NEW)allocationSite).hclass());
	if (allocationSite instanceof harpoon.IR.Quads.ANEW)
	    return _hasInteriorPointers
		(((harpoon.IR.Quads.ANEW)allocationSite).hclass());
	assert false : "not a NEW or ANEW quad.";
	return null;
    }
    /** Return an AllocationProperties object matching the allocated object
     *  type specified by the parameter. */
    private static AllocationProperties _hasInteriorPointers(HClass cls) {
	return hasInteriorPointers(cls) ?
	    (AllocationProperties) new MyAP(cls) { 
	        public boolean hasInteriorPointers() { return true; }
	    } :
	    (AllocationProperties) new MyAP(cls) {
		public boolean hasInteriorPointers() { return false; }
	    };
    }
    /** Return true iff the specified object type has no interior pointers;
     *  that is, iff all its fields are primitive. */
    public static boolean hasInteriorPointers(HClass cls) {
	assert !cls.isInterface() && !cls.isPrimitive();
	if (cls.isArray()) return !cls.getComponentType().isPrimitive();
	// okay, it's an object.  see if it has any non-primitive fields.
	for (HClass sc=cls; sc!=null; sc=sc.getSuperclass()) {
	    HField[] hf = sc.getDeclaredFields();
	    for (int i=0; i<hf.length; i++)
		if (!hf[i].getType().isPrimitive()) return true;
	}
	return false; // no interior pointers.
    }
	
    // serialization support.
    private Object readResolve() throws java.io.ObjectStreamException {
	return SINGLETON; // maintain singleton.
    }

    /* convenience class for code reuse. */
    private static abstract class MyAP
	implements AllocationProperties, java.io.Serializable {
	private HClass actualClass;
	public MyAP(HClass actualClass) { this.actualClass = actualClass; }
	public abstract boolean hasInteriorPointers();
	public boolean canBeStackAllocated() { return false; }
	public boolean canBeThreadAllocated() { return false; }
	public boolean makeHeap() { return false; }
	public boolean noSync()   { return false; }
	public Temp allocationHeap() { return null; }
	public HClass actualClass() { return actualClass; }
    }
}




