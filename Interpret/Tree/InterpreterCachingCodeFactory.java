// CachingCodeFactory.java, created Sun Jan 31 16:45:26 1999 by cananian
// Copyright (C) 1999 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Interpret.Tree;

import harpoon.ClassFile.CachingCodeFactory;
import harpoon.ClassFile.HCode;
import harpoon.ClassFile.HCodeFactory;
import harpoon.ClassFile.HMethod;


/**
 */
public class InterpreterCachingCodeFactory extends CachingCodeFactory { 
    public InterpreterCachingCodeFactory(HCodeFactory parent) {
        super(parent);
    }

    /** Convert a method to an <code>HCode</code>, caching the result.
     *  Cached representations of <code>m</code> in <code>parent</code> are
     *  cleared when this <code>CachingCodeFactory</code> adds the
     *  converted representation of <code>m</code> to its cache. */
    public HCode convert(HMethod m) {
	HCode hc;

	if (m.getName().equals("<clinit>")) 
	    hc = parent.convert(m);	    
	else 
	    hc = super.convert(m);
	
	return hc;
    }
}
