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
import harpoon.IR.Quads.CALL;
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
 * @version $Id: AllocationNumbering.java,v 1.2 2002-02-25 20:58:38 cananian Exp $
 */
public class AllocationNumbering implements java.io.Serializable {
    private final CachingCodeFactory hcf;
    //tbu
    public final Map alloc2int = new HashMap();
    public final Map call2int;
    int n=0,c=0;
    
    /** Creates a <code>AllocationNumbering</code>. */
    public AllocationNumbering(HCodeFactory hcf, ClassHierarchy ch, boolean callSites) {
	if (callSites)
	    call2int=new HashMap();
	else call2int=null;
        this.hcf = new CachingCodeFactory(hcf, true);
	int n = 0, c=0;
	for (Iterator it = ch.callableMethods().iterator(); it.hasNext(); ) {
	    HMethod hm = (HMethod) it.next();
	    number(this.hcf.convert(hm), callSites);
	}
    }
    /** Return the (caching) code factory this numbering was created on. */
    public HCodeFactory codeFactory() { return hcf; }
    
    /** Return an integer identifying this allocation site. */
    public int allocID(Quad q) {
	if (!alloc2int.containsKey(q)) throw new Error("Quad unknown: "+q);
	return ((Integer) alloc2int.get(q)).intValue();
    }

    /** Return an integer identifying this allocation site. */
    public int callID(Quad q) {
	if (!call2int.containsKey(q)) throw new Error("Quad unknown: "+q);
	return ((Integer) call2int.get(q)).intValue();
    }

    /* hard part: the numbering */
    private void number(HCode hc, boolean callSites) {
	if (hc!=null)
	    for (Iterator it=hc.getElementsI(); it.hasNext(); ) {
		Quad q = (Quad) it.next();
		if (q instanceof ANEW || q instanceof NEW)
		    alloc2int.put(q, new Integer(n++));
		else if (callSites&&(q instanceof CALL))
		    call2int.put(q, new Integer(c++));
	    }
    }
}
