// DefaultAllocationInformationMap.java, created Sat Feb  8 14:21:19 2003 by salcianu
// Copyright (C) 2000 Alexandru Salcianu <salcianu@MIT.EDU>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis;

import harpoon.Analysis.Maps.AllocationInformation.AllocationProperties;
import harpoon.Analysis.AllocationInformationMap;
import harpoon.Analysis.DefaultAllocationInformation;
import harpoon.IR.Quads.Quad;

import harpoon.ClassFile.HCodeElement;

/** <code>DefaultAllocationInformationMap</code> is a combination of
    <code>AllocationInformationMap</code> and
    <code>DefaultAllocationInformation</code>.  Like
    <code>AllocationInformationMap</code>, it is a growable map from
    allocation sites to their corresponding allocation properties.
    However, if no <code>AllocationProperties</code> object is
    associated with a specific allocation site, <code>query</code>
    will return a default one, as indicated by
    <code>DefaultAllocationInformation</code>.

    @author  Alexandru Salcianu <salcianu@MIT.EDU>
    @version $Id: DefaultAllocationInformationMap.java,v 1.2 2003-03-11 17:49:52 cananian Exp $ */
public class DefaultAllocationInformationMap
    extends AllocationInformationMap<Quad> implements java.io.Serializable {
    
    /** Creates a <code>DefaultAllocationInformationMap</code>. */
    public DefaultAllocationInformationMap() { }

    /** Returns the <code>AllocationProperties</code> for an
        allocation site.  If no such object exists, it returns a
        default <code>AllocationProperties</code> object. */
    public AllocationProperties query(Quad allocationSite) {
	AllocationProperties ap = super.query(allocationSite);
	if(ap == null)
	    ap = DefaultAllocationInformation.SINGLETON.query(allocationSite);
	return ap;
    }
}
