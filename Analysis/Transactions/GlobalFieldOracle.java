// GlobalFieldOracle.java, created Sun Jan 14 00:56:29 2001 by cananian
// Copyright (C) 2000 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.Transactions;

import harpoon.Analysis.ClassHierarchy;
import harpoon.Analysis.DomTree;
import harpoon.Analysis.ReachingDefs;
import harpoon.Analysis.SSxReachingDefsImpl;
import harpoon.Analysis.Maps.ExactTypeMap;
import harpoon.Analysis.Quads.CallGraphImpl2;
import harpoon.Analysis.Quads.TypeInfo;
import harpoon.ClassFile.HCode;
import harpoon.ClassFile.HCodeFactory;
import harpoon.ClassFile.HField;
import harpoon.ClassFile.HInitializer;
import harpoon.ClassFile.HMethod;
import harpoon.IR.Quads.CALL;
import harpoon.IR.Quads.GET;
import harpoon.IR.Quads.MONITORENTER;
import harpoon.IR.Quads.MONITOREXIT;
import harpoon.IR.Quads.Quad;
import harpoon.IR.Quads.SET;
import harpoon.Util.ArrayIterator;
import harpoon.Util.WorkSet;
import harpoon.Util.Collections.AggregateSetFactory;
import harpoon.Util.Collections.GenericMultiMap;
import harpoon.Util.Collections.MultiMap;
import harpoon.Util.Collections.SetFactory;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
/**
 * A <code>GlobalFieldOracle</code> does a global analysis to determine
 * which fields can possibly be accessed in unsynchronized and
 * synchronized contexts.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: GlobalFieldOracle.java,v 1.1.2.4 2001-06-17 22:31:54 cananian Exp $
 */
class GlobalFieldOracle extends FieldOracle {
    Set syncRead = new HashSet(); Set syncWrite = new HashSet();
    Set unsyncRead = new HashSet(); Set unsyncWrite = new HashSet();
    public boolean isSyncRead(HField hf) { return syncRead.contains(hf); }
    public boolean isSyncWrite(HField hf) { return syncWrite.contains(hf); }
    public boolean isUnsyncRead(HField hf) { return unsyncRead.contains(hf); }
    public boolean isUnsyncWrite(HField hf) { return unsyncWrite.contains(hf);}
    
    /** Creates a <code>GlobalFieldOracle</code>. */
    public GlobalFieldOracle(ClassHierarchy ch, HMethod mainM, Set roots,
			     HCodeFactory hcf) {
	/* Add callable static initializers to the root set */
	Set myroots = new HashSet(roots);
	for (Iterator it=ch.callableMethods().iterator(); it.hasNext(); ) {
	    HMethod hm = (HMethod) it.next();
	    if (hm instanceof HInitializer)
		myroots.add(hm);
	}
        /* for every method, compile lists of methods and fields which
	 * are accessed in synchronized and unsynchronized contexts. */
	MethodInfo mi = analyzeMethods(ch, hcf);
	/* now determine which methods can possibly ever be called in
	 * syncronized and unsynchronized contexts. */
	CallContextInfo cci = new CallContextInfo(mi, mainM, myroots);
	/* and finally, determine "possibly ever accessed" info for
	 * fields from the above. */
	transCloseFields(mi, cci);
	{
	    Set sync = new HashSet(syncRead); sync.addAll(syncWrite);
	    Set unsync = new HashSet(unsyncRead); unsync.addAll(unsyncWrite);
	    Set common = new HashSet(sync); common.retainAll(unsync);
	    sync.removeAll(common); unsync.removeAll(common);
	    System.out.println("GLOBAL FIELD ORACLE: "+
			       sync.size()+" fields exclusively sync, "+
			       unsync.size()+" fields exclusively unsync");
	}
    }

    //----------------------------------------------------------------
    //  method analysis
    MethodInfo analyzeMethods(ClassHierarchy ch, HCodeFactory hcf) {
	MethodInfo result = new MethodInfo();
	CallGraphImpl2 cg = new CallGraphImpl2(ch, hcf);
	for (Iterator it=ch.callableMethods().iterator(); it.hasNext(); ) {
	    HMethod hm = (HMethod) it.next();
	    HCode hc = hcf.convert(hm);
	    if (hc==null) continue;
	    analyzeOneMethod(result, hm, hc, cg);
	}
	return result;
    }
    private void analyzeOneMethod(MethodInfo mi, HMethod hm, HCode hc,
				  CallGraphImpl2 cg) {
	/* MONITORENTER must dominate all associated MONITOREXITs and
	 * monitors must be properly nested. */
	DomTree dt = new DomTree(hc, false);
	ExactTypeMap etm = new TypeInfo((harpoon.IR.Quads.QuadSSI)hc);
	ReachingDefs rd = new SSxReachingDefsImpl(hc);
	analyzeOneQuad(mi, hm, (Quad)hc.getRootElement(), dt, etm, rd, cg, 0);
    }
    private void analyzeOneQuad(MethodInfo mi, HMethod hm, Quad q, DomTree dt,
				ExactTypeMap etm, ReachingDefs rd,
				CallGraphImpl2 cg, int synccount) {
	/* analyze q */
	if (q instanceof MONITORENTER) synccount++;
	if (q instanceof MONITOREXIT) synccount--;
	if (q instanceof GET) {
	    MultiMap mm = (synccount>0) ? mi.readSync : mi.readUnsync;
	    mm.add(hm, ((GET)q).field());
	}
	if (q instanceof SET) {
	    MultiMap mm = (synccount>0) ? mi.writeSync : mi.writeUnsync;
	    mm.add(hm, ((SET)q).field());
	}
	if (q instanceof CALL) {
	    MultiMap mm = (synccount>0) ? mi.calledSync : mi.calledUnsync;
	    mm.addAll(hm, Arrays.asList(cg.calls((CALL)q, rd, etm)));
	}
	    
	/* recurse down the dominator tree */
	for (Iterator it=new ArrayIterator(dt.children(q)); it.hasNext(); )
	    analyzeOneQuad(mi,hm, (Quad)it.next(), dt,etm,rd,cg, synccount);
    }
    private class MethodInfo {
	/** fields read/written in synchronized/unsynchronized contexts. */
	final MultiMap readSync, readUnsync, writeSync, writeUnsync;
	/** methods called in synchronized/unsynchronized contexts. */
	final MultiMap calledSync, calledUnsync;
	MethodInfo() {
	    SetFactory sf = new AggregateSetFactory();
	    readSync = new GenericMultiMap(sf);
	    readUnsync = new GenericMultiMap(sf);
	    writeSync = new GenericMultiMap(sf);
	    writeUnsync = new GenericMultiMap(sf);
	    calledSync = new GenericMultiMap(sf);
	    calledUnsync = new GenericMultiMap(sf);
	}
    }
    //----------------------------------------------------------------
    // compute transitive closure on methods called.
    private class CallContextInfo {
	final Set calledSync = new HashSet();
	final Set calledUnsync = new HashSet();
	/* compute call context info */
	CallContextInfo(MethodInfo mi, HMethod mainM, Set roots) {
	    WorkSet W = new WorkSet();
	    // ever called unsync?
	    //   if in calledUnsync list of a method ever called unsync.
	    W.addAll(roots); // all roots called unsync.
	    while (!W.isEmpty()) {
		HMethod hm = (HMethod) W.pop();
		if (calledUnsync.contains(hm)) continue;
		calledUnsync.add(hm);
		W.addAll(mi.calledUnsync.getValues(hm));
	    }
	    // ever called sync?
	    //   if in any calledSync list
	    //   or in calledUnsync list of a method ever called sync.
	    W.addAll(roots); // all roots called sync
	    W.remove(mainM); // except main method.
	    for (Iterator it=W.iterator(); it.hasNext(); )
		if (((HMethod)it.next()) instanceof HInitializer)
		    it.remove(); // and except static initializers.
	    for (Iterator it=mi.calledSync.keySet().iterator(); it.hasNext();){
		HMethod hm = (HMethod) it.next();
		W.addAll(mi.calledSync.getValues(hm));
	    } // add all calledSync to worklist.
	    while (!W.isEmpty()) {
		HMethod hm = (HMethod) W.pop(); // a method called sync.
		if (calledSync.contains(hm)) continue;//done already.
		calledSync.add(hm);
		// all unsync calls of a method called sync are called sync.
		W.addAll(mi.calledUnsync.getValues(hm));
	    }
	    // done.
	}
    }
    //----------------------------------------------------------------
    // compute transitive closure on fields accessed.
    void transCloseFields(MethodInfo mi, CallContextInfo cci) {
	// ever accessed unsync?
	//  in fields accessedUnsync list of method called unsync.
	for (Iterator it=cci.calledUnsync.iterator(); it.hasNext(); ) {
	    HMethod hm = (HMethod) it.next();
	    unsyncRead.addAll(mi.readUnsync.getValues(hm));
	    unsyncWrite.addAll(mi.writeUnsync.getValues(hm));
	}
	// ever accessed sync?
	//  in any accessedSync list
	//  or in accessedUnsync list of a method called sync.
	for (Iterator it=cci.calledUnsync.iterator(); it.hasNext(); ) {
	    HMethod hm = (HMethod) it.next();
	    syncRead.addAll(mi.readSync.getValues(hm));
	    syncWrite.addAll(mi.writeSync.getValues(hm));
	}
	for (Iterator it=cci.calledSync.iterator(); it.hasNext(); ) {
	    HMethod hm = (HMethod) it.next();
	    syncRead.addAll(mi.readSync.getValues(hm));
	    syncRead.addAll(mi.readUnsync.getValues(hm));
	    syncWrite.addAll(mi.writeSync.getValues(hm));
	    syncWrite.addAll(mi.writeUnsync.getValues(hm));
	}
	// done!
    }
    //----------------------------------------------------------------
}
