// AllocationInformationMap.java, created Mon Apr  3 19:11:39 2000 by cananian
// Copyright (C) 2000 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis;

import harpoon.Analysis.Maps.AllocationInformation;
import harpoon.ClassFile.HCodeElement;
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
 * @version $Id: AllocationInformationMap.java,v 1.1.2.3 2000-04-04 00:43:19 cananian Exp $
 */
public class AllocationInformationMap implements AllocationInformation {
    private final Map map = new HashMap();
    
    /** Creates a <code>AllocationInformationMap</code>. */
    public AllocationInformationMap() { }

    /** Return the <code>AllocationProperties</code> for the given
     *  <code>allocationSite</code>. */
    public AllocationProperties query(HCodeElement allocationSite) {
	return (AllocationProperties) map.get(allocationSite);
    }
    /** Associate the given <code>allocationSite</code> with the specified
     *  <code>AllocationProperties</code>. */
    public void associate(HCodeElement allocationSite, AllocationProperties ap)
    {
	map.put(allocationSite, ap);
    }
    /** Transfer allocation information from the oldallocsite to newallocsite
     *  using the specified <code>TempMap</code> and old 
     *  <code>AllocationInformation</code>. */
    public void transfer(HCodeElement newallocsite, HCodeElement oldallocsite,
			 TempMap tm, AllocationInformation ai) {
	AllocationProperties ap = ai.query(oldallocsite);
	associate(newallocsite, ap.allocationHeap()==null ? ap :
		  new AllocationPropertiesProxy(ap, tm));
    }
    // copy the info from the given allocationproperties to avoid leaving
    // a long chain of live objects...
    public static class AllocationPropertiesImpl
	implements AllocationProperties {
	final boolean hasInteriorPointers;
	final boolean canBeStackAllocated;
	final boolean canBeThreadAllocated;
	final Temp allocationHeap;
	AllocationPropertiesImpl(boolean hasInteriorPointers,
				 boolean canBeStackAllocated,
				 boolean canBeThreadAllocated,
				 Temp allocationHeap) {
	    this.hasInteriorPointers = hasInteriorPointers;
	    this.canBeStackAllocated = canBeStackAllocated;
	    this.canBeThreadAllocated= canBeThreadAllocated;
	    this.allocationHeap = allocationHeap;
	}
	AllocationPropertiesImpl(AllocationProperties ap, TempMap tm) {
	    this(ap.hasInteriorPointers(),
		 ap.canBeStackAllocated(),
		 ap.canBeThreadAllocated(),
		 ap.allocationHeap() != null ?
		 tm.tempMap(ap.allocationHeap()) : null);
	}
	public boolean hasInteriorPointers() { return hasInteriorPointers; }
	public boolean canBeStackAllocated() { return canBeStackAllocated; }
	public boolean canBeThreadAllocated(){ return canBeThreadAllocated;}
	public Temp allocationHeap() { return allocationHeap; }
    }
}
