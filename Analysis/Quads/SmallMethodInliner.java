// SmallMethodInliner.java, created Mon Jun 18 13:28:16 2001 by cananian
// Copyright (C) 2000 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.Quads;

import harpoon.Analysis.ClassHierarchy;
import harpoon.ClassFile.CachingCodeFactory;
import harpoon.ClassFile.HCode;
import harpoon.ClassFile.HCodeFactory;
import harpoon.ClassFile.HMethod;
import harpoon.IR.Quads.CALL;
import harpoon.IR.Quads.Quad;
import harpoon.Util.Collections.GenericMultiMap;
import harpoon.Util.Collections.MultiMap;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
/**
 * <code>SmallMethodInliner</code> will inline small methods until
 * the code is bloated by the specified amount.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: SmallMethodInliner.java,v 1.1.2.1 2001-06-18 19:42:40 cananian Exp $
 */
public class SmallMethodInliner extends MethodInliningCodeFactory {
    static final int NPERCENT=10;

    /** Creates a <code>SmallMethodInliner</code>. */
    public SmallMethodInliner(HCodeFactory hcf, ClassHierarchy ch) {
	this(new CachingCodeFactory(hcf), ch);
    }
    // scan ccf for appropriate call sites.
    private SmallMethodInliner(CachingCodeFactory ccf, ClassHierarchy ch) {
	super(ccf);
	long totalsize = 0;
	for (Iterator it=ch.callableMethods().iterator(); it.hasNext(); ) {
	    HMethod hm = (HMethod) it.next();
	    HCode hc = ccf.convert(hm);
	    // determine method sizes
	    if (hc==null) continue;
	    // size-4: CALL, HEADER, FOOTER, METHOD.
	    methodSize.putInt(hm, hc.getElementsL().size()-4);
	    // find call sites.
	    for (Iterator it2=hc.getElementsI(); it2.hasNext(); ) {
		Quad q = (Quad) it2.next();
		if (!(q instanceof CALL)) continue;
		CALL call = (CALL) q;
		if (call.isVirtual()) continue;
		callSites.add(call.method(), call);
	    }
	    // keep track of total program size
	    totalsize+=methodSize.getInt(hm);
	}
	List mlist = new ArrayList(ch.callableMethods());
	Collections.sort(mlist, new Comparator() {
	    public int compare(Object o1, Object o2) {
		HMethod hm1 = (HMethod) o1, hm2 = (HMethod) o2;
		return score(hm1) - score(hm2);
	    }
	});
	// inline to bloat the program by the specified percentage of
	// total program size.
	long bloat = 0;
	Iterator it=mlist.iterator();
	while (it.hasNext() && bloat < (NPERCENT*totalsize/100)) {
	    HMethod hm = (HMethod) it.next(); // inline this!
	    if (Modifier.isNative(hm.getModifiers())) continue;//unless native
	    Iterator it2=callSites.getValues(hm).iterator();
	    int size = methodSize.getInt(hm);
	    while (it2.hasNext()) { 
		inline((CALL)it2.next());
		// XXX: add 'size' to caller's method size, re-sort.
		bloat+=size;
	    }
	    System.out.println("INLINING all nonvirtual calls to "+hm);
	}
	// done!
    }
    final IntMap methodSize = new IntMap();
    final MultiMap callSites = new GenericMultiMap();
    private int score(HMethod hm) {
	return methodSize.getInt(hm) * callSites.getValues(hm).size();
    }

    private static class IntMap extends HashMap {
	IntMap() { super(); }
	int getInt(Object key) {
	    Integer i = (Integer) get(key);
	    return (i==null)?0:i.intValue();
	}
	void putInt(Object key, int val) {
	    put(key, new Integer(val));
	}
    }
}
