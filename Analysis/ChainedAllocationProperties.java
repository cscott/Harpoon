// ChainedAllocationProperties.java, created Sat Feb  8 16:39:04 2003 by salcianu
// Copyright (C) 2000 Alexandru Salcianu <salcianu@MIT.EDU>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis;

import harpoon.Analysis.Maps.AllocationInformation.AllocationProperties;

import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HField;
import harpoon.Temp.Temp;

/** <code>ChainedAllocationProperties</code> allows us to change
    several properties of an already existing
    <code>AllocationProperties</code>.  By default, it forwards each
    request to the original <code>AllocationProperties</code> that
    it's chained with (that object is passed to the
    <code>ChainedAllocationProperties</code> constructor).  By
    overriding some of its methods, we can change only several
    allocation properties attached with a specific allocation site.

    @author  Alexandru Salcianu <salcianu@MIT.EDU>
    @version $Id: ChainedAllocationProperties.java,v 1.1 2003-02-08 23:13:27 salcianu Exp $ */
public class ChainedAllocationProperties implements AllocationProperties {

    /** Createsv a new <code>ChainedAllocationProperties</code>
        object 

	@param oap original <code>AllocationProperties</code>; the
	created <code>ChainedAllocationProperties</code> is chained
	with <code>oap</code>.  By default, all method calls are
	forwarded toward <code>oap</code>.  */
    public ChainedAllocationProperties(AllocationProperties oap) {
	this.oap = oap;
    }
    private final AllocationProperties oap;
    public boolean hasInteriorPointers() {
	return oap.hasInteriorPointers();
    }
    public boolean canBeStackAllocated() {
	return oap.canBeStackAllocated();
    }
    public boolean canBeThreadAllocated() {
	return oap.canBeThreadAllocated();
    }
    public boolean makeHeap() { return oap.makeHeap(); }
    public Temp allocationHeap() { return oap.allocationHeap(); }
    public HClass actualClass() {return oap.actualClass(); }
    public boolean noSync() { return oap.noSync(); }
    public boolean setDynamicWBFlag() { return oap.setDynamicWBFlag(); }
    public HField getMemoryChunkField() {
	return oap.getMemoryChunkField();
    }
    public int getUniqueID() { return oap.getUniqueID(); }
}
