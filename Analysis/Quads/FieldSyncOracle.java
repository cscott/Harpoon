// FieldSyncOracle.java, created Sun Jan 14 00:56:29 2001 by cananian
// Copyright (C) 2000 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.Quads;

import harpoon.Analysis.CallGraph;
import harpoon.Analysis.ClassHierarchy;
import harpoon.Analysis.CallGraph;
import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HCode;
import harpoon.ClassFile.HCodeFactory;
import harpoon.ClassFile.HField;
import harpoon.ClassFile.HMethod;
import harpoon.IR.Quads.GET;
import harpoon.IR.Quads.MONITORENTER;
import harpoon.IR.Quads.MONITOREXIT;
import harpoon.IR.Quads.Quad;
import harpoon.IR.Quads.SET;
import harpoon.Util.WorkSet;
import harpoon.Util.Collections.AggregateSetFactory;
import harpoon.Util.Collections.BitSetFactory;
import harpoon.Util.Collections.GenericInvertibleMultiMap;
import harpoon.Util.Collections.GenericMultiMap;
import harpoon.Util.Collections.InvertibleMultiMap;
import harpoon.Util.Collections.MultiMap;
import harpoon.Util.Collections.SetFactory;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
/**
 * A <code>FieldSyncOracle</code> tells which fields a given method could
 * possibly access (either directly, or via a method call), and whether
 * the given method will ever acquire/release a lock (either directly, or
 * via a method call).  This analysis is useful for determining which
 * memory optimizations are safe across a given method call (and
 * other things).
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: FieldSyncOracle.java,v 1.1.2.4 2001-11-07 15:54:06 cananian Exp $
 */
public class FieldSyncOracle {
    final MultiMap fieldsRead, fieldsWritten;
    final Set syncMethods = new HashSet();
    /** Returns <code>true</code> if <code>HMethod</code>
        <code>hm</code> will ever acquire/release a lock (either
        directly, or via a method call). */
    public boolean isSync(HMethod hm) {
	return syncMethods.contains(hm);
    }
    /** Returns <code>true</code> if <code>HMethod</code>
	<code>hm</code> will ever read <code>HField</code>
	<code>hf</code>, either directly or via a method call. */
    public boolean isRead(HMethod hm, HField hf) {
	return fieldsRead.contains(hm, hf);
    }
    /** Returns <code>true</code> if <code>HMethod</code>
	<code>hm</code> will ever read <code>HField</code>
	<code>hf</code>, either directly or via a method call. */
    public boolean isWritten(HMethod hm, HField hf) {
	return fieldsWritten.contains(hm, hf);
    }
    /** Creates a <code>FieldSyncOracle</code>. */
    public FieldSyncOracle(HCodeFactory hcf, ClassHierarchy ch, CallGraph cg) {
	Set s = new HashSet();
	/* compute field universe */
	for (Iterator it=ch.classes().iterator(); it.hasNext(); ) {
	    HClass hc = (HClass) it.next();
	    s.addAll(Arrays.asList(hc.getDeclaredFields()));
	}
	/* initialize maps. */
	SetFactory sf =new BitSetFactory(s);
	this.fieldsRead = new GenericMultiMap(sf);
	this.fieldsWritten = new GenericMultiMap(sf);

	/* analyze all callable methods */
	for (Iterator it=ch.callableMethods().iterator(); it.hasNext(); )
	    analyze((HMethod)it.next(), hcf);
	/* compute transitive closure */
	transClose(ch, callGraphMap(ch, cg));
	/* done! */
	int sum=0, n=0;
	for (Iterator it=fieldsRead.keySet().iterator(); it.hasNext(); n++)
	    sum+=fieldsRead.getValues(it.next()).size();
	System.out.println("FIELDS read: (avg) "+((float)sum/n)+
			   " of "+s.size());
	sum=0; n=0;
	for (Iterator it=fieldsWritten.keySet().iterator(); it.hasNext(); n++)
	    sum+=fieldsWritten.getValues(it.next()).size();
	System.out.println("FIELDS written: (avg) "+((float)sum/n)+
			   " of "+s.size());
	System.out.println("SYNC methods: "+syncMethods.size()+
			   " of "+ch.callableMethods().size());
    }

    //----------------------------------------------------------------
    //  method analysis
    void analyze(HMethod hm, HCodeFactory hcf) {
	HCode hc = hcf.convert(hm);
	if (hc==null) return; // abstract.
	for (Iterator it=hc.getElementsI(); it.hasNext(); ) {
	    Quad q = (Quad) it.next();
	    /* analyze q */
	    if (q instanceof MONITORENTER || q instanceof MONITOREXIT)
		syncMethods.add(hm);
	    if (q instanceof GET)
		fieldsRead.add(hm, ((GET)q).field());
	    if (q instanceof SET)
		fieldsWritten.add(hm, ((SET)q).field());
	}
    }
    // make invertible multimap out of the callgraph
    InvertibleMultiMap callGraphMap(ClassHierarchy ch, CallGraph cg) {
	InvertibleMultiMap imm =
	    new GenericInvertibleMultiMap(new AggregateSetFactory());
	for (Iterator it=ch.callableMethods().iterator(); it.hasNext(); ) {
	    HMethod hm = (HMethod) it.next();
	    imm.addAll(hm, Arrays.asList(cg.calls(hm)));
	}
	return imm;
    }

    //----------------------------------------------------------------
    // compute transitive closure.
    void transClose(ClassHierarchy ch, InvertibleMultiMap calls) {
	// create an invertable multimap from the call graph.
	WorkSet w = new WorkSet(ch.callableMethods());
	while (!w.isEmpty()) {
	    HMethod hm = (HMethod) w.pop();
	    boolean changed = false;
	    for (Iterator it=calls.getValues(hm).iterator(); it.hasNext(); ) {
		HMethod callee = (HMethod) it.next();
		// add all fields read/written by callee to this.
		if (fieldsRead.addAll(hm, fieldsRead.getValues(callee)))
		    changed = true;
		if (fieldsWritten.addAll(hm, fieldsWritten.getValues(callee)))
		    changed = true;
		// close on syncMethods.
		if (syncMethods.contains(callee))
		    if (syncMethods.add(hm))
			changed = true;
	    }
	    // if hm's data was modified, add all callers to the worklist.
	    if (changed)
		w.addAll(calls.invert().getValues(hm));
	}
	// done.
    }
}
