// AllocationNumbering.java, created Wed Nov  8 19:06:08 2000 by cananian
// Copyright (C) 2000 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.PointerAnalysis;

import harpoon.Analysis.ClassHierarchy;
import harpoon.ClassFile.CachingCodeFactory;
import harpoon.ClassFile.HCode;
import harpoon.ClassFile.HCodeFactory;
import harpoon.ClassFile.HMethod;
import harpoon.IR.Quads.ANEW;
import harpoon.IR.Quads.NEW;
import harpoon.IR.Quads.Quad;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
/**
 * <code>AllocationNumbering</code>
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: AllocationNumbering.java,v 1.1.2.3 2000-11-10 19:05:02 cananian Exp $
 */
public class AllocationNumbering implements java.io.Serializable {
    private final CachingCodeFactory hcf;
    private final Map alloc2int = new HashMap();
    
    /** Creates a <code>AllocationNumbering</code>. */
    public AllocationNumbering(HCodeFactory hcf, ClassHierarchy ch) {
        this.hcf = new CachingCodeFactory(hcf, true);
	int n = 0;
	for (Iterator it = ch.callableMethods().iterator(); it.hasNext(); ) {
	    HMethod hm = (HMethod) it.next();
	    n = number(this.hcf.convert(hm), n);
	}
    }
    /** Return the (caching) code factory this numbering was created on. */
    public HCodeFactory codeFactory() { return hcf; }
    
    /** Return an integer identifying this allocation site. */
    public int allocID(Quad q) {
	if (!alloc2int.containsKey(q)) throw new Error("Quad unknown: "+q);
	return ((Integer) alloc2int.get(q)).intValue();
    }

    /* hard part: the numbering */
    private int number(HCode hc, int n) {
	if (hc!=null)
	    for (Iterator it=hc.getElementsI(); it.hasNext(); ) {
		Quad q = (Quad) it.next();
		if (q instanceof ANEW || q instanceof NEW)
		    alloc2int.put(q, new Integer(n++));
	    }
	return n;
    }
}
