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
import harpoon.Util.Collections.BinaryHeap;
import harpoon.Util.Collections.GenericMultiMap;
import harpoon.Util.Collections.Heap;
import harpoon.Util.Collections.MultiMap;

import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
/**
 * <code>SmallMethodInliner</code> will inline small methods until
 * the code is bloated by the specified amount.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: SmallMethodInliner.java,v 1.3 2002-09-13 18:22:41 wbeebee Exp $
 */
public class SmallMethodInliner extends MethodInliningCodeFactory {
    static final int NPERCENT= // default to 10 percent bloat.
	Integer.parseInt(System.getProperty("harpoon.inliner.percent", "25"));

    /** Creates a <code>SmallMethodInliner</code>. */
    public SmallMethodInliner(HCodeFactory hcf, ClassHierarchy ch) {
	this(new CachingCodeFactory(hcf), ch);
    }
    // scan ccf for appropriate call sites.
    private SmallMethodInliner(CachingCodeFactory ccf, ClassHierarchy ch) {
	super(ccf);
	long totalsize = 0;

	IntMap methodSize = new IntMap();
	MultiMap callSites = new GenericMultiMap();
	Map call2caller = new HashMap();

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
		call2caller.put(call, hm);
	    }
	    // keep track of total program size
	    totalsize+=methodSize.getInt(hm);
	}
	Heap h = new BinaryHeap();
	Map method2entry = new HashMap();
	for (Iterator it=ch.callableMethods().iterator(); it.hasNext(); ) {
	    HMethod hm = (HMethod) it.next();
	    // compute score for hm.
	    int score = score(hm, methodSize, callSites);
	    // add to heap, and to method-to-entry map.
	    if (callSites.getValues(hm).size()>0) // unless no sites to inline
		method2entry.put(hm, h.insert(new Integer(score), hm));
	}
	// inline to bloat the program by the specified percentage of
	// total program size.
	long bloat = 0;
	while (bloat < (NPERCENT*totalsize/100)) {
	    if (h.isEmpty()) break;
	    HMethod hm = (HMethod) h.extractMinimum().getValue();//inline this!
	    method2entry.remove(hm);
	    if (Modifier.isNative(hm.getModifiers())) continue;//unless native
	    int size = methodSize.getInt(hm);
	    Iterator it2=callSites.getValues(hm).iterator();
	    if (it2.hasNext())
		System.err.println("INLINING all nonvirtual calls to "+hm);
	    while (it2.hasNext()) { 
		CALL call = (CALL) it2.next();
		inline(call);
		bloat+=size;
		// add 'size' to caller's method size...
		HMethod caller = (HMethod) call2caller.get(call);
		methodSize.putInt(caller, methodSize.getInt(caller)+size);
		// ...and adjust caller's key in pqueue.
		Map.Entry me = (Map.Entry) method2entry.get(caller);
		if (me!=null) {
		    int score = score(hm, methodSize, callSites);
		    h.updateKey(me, new Integer(score));
		}
	    }
	}
	// done!
	System.out.println("ACTUAL INLINING BLOAT: " +
			   ((1+2*100*bloat)/(2*totalsize)) + "%");
    }

    /** Override this if you want to define your own scoring function
     *  for inlining methods.
     */
    protected int score(HMethod hm, IntMap methodSize, MultiMap callSites)
    {
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
