// AllocationInformationMap.java, created Mon Apr  3 19:11:39 2000 by cananian
// Copyright (C) 2000 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis;

import harpoon.Analysis.Maps.AllocationInformation;
import harpoon.Analysis.Maps.AllocationInformation.AllocationProperties;
import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HCodeElement;
import harpoon.Temp.Label;
import harpoon.Temp.Temp;
import harpoon.Temp.TempMap;
import harpoon.Util.Util;

import java.util.HashMap;
import java.util.Map;
/**
 * An <code>AllocationInformationMap</code> makes it easy to create a
 * map-based <code>AllocationInformation</code> structure.  It also
 * contains methods to facilitate transferring allocation information
 * from a different <code>AllocationInformation</code> object.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: AllocationInformationMap.java,v 1.9 2003-03-11 17:49:48 cananian Exp $
 */
public class AllocationInformationMap<HCE extends HCodeElement>
    implements AllocationInformation<HCE>, java.io.Serializable {
    private final Map<HCE,AllocationProperties> map =
	new HashMap<HCE,AllocationProperties>();
    
    /** Creates a <code>AllocationInformationMap</code>. */
    public AllocationInformationMap() { }

    /** Return the <code>AllocationProperties</code> for the given
     *  <code>allocationSite</code>. */
    public AllocationProperties query(HCE allocationSite) {
	return map.get(allocationSite);
    }
    /** Associate the given <code>allocationSite</code> with the specified
     *  <code>AllocationProperties</code>. */
    public void associate(HCE allocationSite, AllocationProperties ap)
    {
	map.put(allocationSite, ap);
    }
    /** Transfer allocation information from the oldallocsite to newallocsite
     *  using the specified <code>TempMap</code> and old 
     *  <code>AllocationInformation</code>. */
    public <HCE2 extends HCodeElement> void transfer
			 (HCE newallocsite, HCE2 oldallocsite,
			  TempMap tm, AllocationInformation<HCE2> ai) {
	AllocationProperties ap = ai.query(oldallocsite);
	associate(newallocsite, ap.allocationHeap()==null ? ap :
		  new AllocationPropertiesImpl(ap, tm));
    }
    /** A simple implementation of <code>AllocationProperties</code>. */
    public final static class AllocationPropertiesImpl
	implements AllocationProperties, java.io.Serializable {
	final boolean hasInteriorPointers;
	final boolean canBeStackAllocated;
	final boolean canBeThreadAllocated;
	final boolean makeHeap;
	final boolean noSynchronization;
	final Temp allocationHeap;
	final HClass actualClass;
	final boolean setDynamicWBFlag;
	final Label labelOfPtrToMemoryChunk;
	final int uniqueID;
	
	public AllocationPropertiesImpl(boolean hasInteriorPointers,
					boolean canBeStackAllocated,
					boolean canBeThreadAllocated,
					boolean makeHeap,
					boolean noSynchronization,
					Temp allocationHeap,
					HClass actualClass,
					boolean setDynamicWBFlag) {
	    this(hasInteriorPointers,
		 canBeStackAllocated, canBeThreadAllocated, makeHeap,
		 noSynchronization, allocationHeap,
		 actualClass, setDynamicWBFlag, null, -1);
	}

	public AllocationPropertiesImpl(boolean hasInteriorPointers,
					boolean canBeStackAllocated,
					boolean canBeThreadAllocated,
					boolean makeHeap,
					boolean noSynchronization,
					Temp allocationHeap,
					HClass actualClass,
					boolean setDynamicWBFlag,
					Label labelOfPtrToMemoryChunk,
					int uniqueID) {
	    assert !(allocationHeap!=null && !canBeThreadAllocated);
	    assert !(allocationHeap!=null && makeHeap);
	    this.hasInteriorPointers  = hasInteriorPointers;
	    this.canBeStackAllocated  = canBeStackAllocated;
	    this.canBeThreadAllocated = canBeThreadAllocated;
	    this.makeHeap             = makeHeap;
	    this.noSynchronization    = noSynchronization;
	    this.allocationHeap       = allocationHeap;
	    this.actualClass          = actualClass;
	    this.setDynamicWBFlag     = setDynamicWBFlag;
	    this.labelOfPtrToMemoryChunk = labelOfPtrToMemoryChunk;
	    this.uniqueID             = uniqueID;
	}
	public AllocationPropertiesImpl(AllocationProperties ap, 
					TempMap tm) {
	    this(ap.hasInteriorPointers(),
		 ap.canBeStackAllocated(),
		 ap.canBeThreadAllocated(),
		 ap.makeHeap(),
		 ap.noSync(),
		 ap.allocationHeap() != null ?
		 tm.tempMap(ap.allocationHeap()) : null,
		 ap.actualClass(),
		 ap.setDynamicWBFlag(),
		 ap.getLabelOfPtrToMemoryChunk(),
		 ap.getUniqueID());
	}
	public boolean hasInteriorPointers() { return hasInteriorPointers; }
	public boolean canBeStackAllocated() { return canBeStackAllocated; }
	public boolean canBeThreadAllocated(){ return canBeThreadAllocated; }
	public boolean makeHeap()            { return makeHeap; }
	public boolean noSync()              { return noSynchronization; } 
	public Temp allocationHeap()         { return allocationHeap; }
	public HClass actualClass()          { return actualClass; }
	public boolean setDynamicWBFlag()    { return setDynamicWBFlag; }
	public Label getLabelOfPtrToMemoryChunk() {
	    return labelOfPtrToMemoryChunk; }
	public int getUniqueID()             { return -1; }
    }
}
