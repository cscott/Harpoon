// CHStats.java, created Mon Aug  2 11:15:22 1999 by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Main;

import harpoon.ClassFile.CachingCodeFactory;
import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HCodeFactory;
import harpoon.ClassFile.HMethod;
import harpoon.ClassFile.Linker;
import harpoon.ClassFile.Loader;
import harpoon.Util.Collections.UniqueVector;

import java.util.Collections;
import java.util.Iterator;
/**
 * <code>CHStats</code> computes interesting statistics of the
 * compiler class hierarchy for inclusion in papers and theses.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: CHStats.java,v 1.2 2002-02-25 21:06:05 cananian Exp $
 */

public abstract class CHStats extends harpoon.IR.Registration {

    public static final void main(String args[]) {
	java.io.PrintWriter out = new java.io.PrintWriter(System.out, true);
	Linker linker = Loader.systemLinker;
	HMethod m = null;

	if (args.length < 2) {
	    System.err.println("Needs class and method name.");
	    return;
	}

	{
	    HClass cls = linker.forName(args[0]);
	    HMethod hm[] = cls.getDeclaredMethods();
	    for (int i=0; i<hm.length; i++)
		if (hm[i].getName().equals(args[1])) {
		    m = hm[i];
		    break;
		}
	}

	HCodeFactory hcf =
	    new CachingCodeFactory(harpoon.IR.Quads.QuadNoSSA.codeFactory());
	harpoon.Analysis.ClassHierarchy ch = 
	    new harpoon.Analysis.Quads.QuadClassHierarchy
	    (linker, Collections.singleton(m), hcf);
	System.out.println("For call graph rooted at "+m.getName()+":");
	int totalclasses=0, depthsum=0, maxdepth=-1; HClass maxc=null;
	for (Iterator it=ch.classes().iterator();it.hasNext(); totalclasses++){
	    HClass c = (HClass) it.next();
	    int depth=depth(c);
	    if (depth > maxdepth) { maxdepth = depth; maxc=c; }
	    depthsum+=depth;
	}
	System.out.println("  Total classes: "+totalclasses);
	System.out.println("  Maximum depth: "+maxdepth+" ("+maxc+")");
	System.out.println("  Average depth: "+((float)depthsum/totalclasses));
    }
    private static java.util.Map dm = new java.util.HashMap();
    private static int depth(HClass c) {
	if (dm.containsKey(c)) { // use cached value.
	    Integer dI = (Integer) dm.get(c);
	    if (dI!=null) return dI.intValue();
	    System.err.println("Circular reference to "+c);
	    dm.put(c, new Integer(0));
	    return 0;
	}
	dm.put(c, null);
	if (c.isInterface()) {
	    int depth=0;
	    HClass in[] = c.getInterfaces();
	    for (int i=0; i<in.length; i++) {
		int d = 1 + depth(in[i]);
		if (d>depth) depth=d;
	    }
	    dm.put(c, new Integer(depth));
	    return depth;
	} else {
	    int depth = 0;
	    HClass sc=c.getSuperclass();
	    if (sc!=null) depth = 1 + depth(sc);
	    dm.put(c, new Integer(depth));
	    return depth;
	}
    }
}
