// CheesyPACheckRemoval.java, created Tue Jan 23 16:14:25 2001 by salcianu
// Copyright (C) 2000 Alexandru SALCIANU <salcianu@retezat.lcs.mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.Realtime;

import java.util.Map;
import java.util.Hashtable;
import java.util.Set;
import java.util.HashSet;
import java.util.Iterator;

import harpoon.ClassFile.Linker;
import harpoon.ClassFile.HCodeFactory;
import harpoon.ClassFile.HMethod;
import harpoon.IR.Quads.QuadNoSSA;
import harpoon.Analysis.ClassHierarchy;

import harpoon.IR.Quads.Quad;
import harpoon.IR.Quads.SET;
import harpoon.IR.Quads.ASET;
import harpoon.IR.Quads.QuadVisitor;

import harpoon.Temp.Temp;

import harpoon.Analysis.PointerAnalysis.PointerAnalysis;
import harpoon.Analysis.PointerAnalysis.PANode;
import harpoon.Analysis.PointerAnalysis.ParIntGraph;

import harpoon.Analysis.MetaMethods.MetaCallGraph;
import harpoon.Analysis.MetaMethods.MetaMethod;
import harpoon.Analysis.MetaMethods.FakeMetaCallGraph;
import harpoon.Analysis.MetaMethods.MetaCallGraphImpl;
import harpoon.Analysis.MetaMethods.MetaAllCallers;
import harpoon.Util.LightBasicBlocks.LBBConverter;

import harpoon.ClassFile.CachingCodeFactory;
import harpoon.Util.BasicBlocks.CachingBBConverter;
import harpoon.Util.LightBasicBlocks.LightBasicBlock;
import harpoon.Util.LightBasicBlocks.LBBConverter;
import harpoon.Util.LightBasicBlocks.CachingLBBConverter;
import harpoon.Util.LightBasicBlocks.CachingSCCLBBFactory;

import harpoon.Analysis.Quads.CallGraph;
import harpoon.Analysis.Quads.CallGraphImpl;
import harpoon.Analysis.MetaMethods.SmartCallGraph;

import harpoon.Util.Util;


/**
 * <code>CheesyPACheckRemoval</code>
 * 
 * @author  Alexandru SALCIANU <salcianu@retezat.lcs.mit.edu>
 * @version $Id: CheesyPACheckRemoval.java,v 1.1.2.7 2001-06-17 22:31:33 cananian Exp $
 */
public class CheesyPACheckRemoval implements CheckRemoval {

    private static final boolean SMART_CALL_GRAPH = true;
    private static final boolean DEBUG = true;

    PointerAnalysis pa = null;

    MetaCallGraph mcg = null;

    private boolean shouldRemoveAllChecks = false;

    
    /** Creates a <code>CheesyPACheckRemoval</code>. */
    public CheesyPACheckRemoval(Linker linker, ClassHierarchy ch,
				HCodeFactory hcf, Set mroots) {
	// 1. create the pointer analysis object
	create_pa(linker, ch, hcf, mroots);
	// 2. use the pointer analysis to see if ALL the checks
	// can be removed or not
	shouldRemoveAllChecks = uselessChecks();

	if(DEBUG)
	    System.out.println("shouldRemoveAllChecks = " + 
			      shouldRemoveAllChecks);
    }


    /** Create the Pointer Analysis object. */
    private void create_pa(Linker linker, ClassHierarchy ch,
			   HCodeFactory hcf, Set mroots) {
	Util.assert(hcf.getCodeName().equals(QuadNoSSA.codename),
		    "Not a QuadNoSSA code factory");
	// we really need a caching code factory; raise an error otherwise
	CachingCodeFactory ccf = (CachingCodeFactory) hcf;
	CachingBBConverter bbconv = new CachingBBConverter(ccf);
	LBBConverter lbbconv = new CachingLBBConverter(bbconv);

	mroots = filter(mroots);

	Set run_mms = null;
	CallGraph cg = null;
	
	System.out.print("CallGraph ... ");
	long tstart = time();
	if(SMART_CALL_GRAPH){ // smart call graph!
	    MetaCallGraph fmcg = new MetaCallGraphImpl(bbconv, ch, mroots);
	    run_mms = fmcg.getRunMetaMethods();
	    cg = new SmartCallGraph(fmcg);
	}
	else
	    cg = new CallGraphImpl(ch, ccf);

	mcg = new FakeMetaCallGraph(cg, cg.callableMethods(), run_mms);

	MetaAllCallers mac = new MetaAllCallers(mcg);
	System.out.println((time() - tstart) + "ms");

	PointerAnalysis.CALL_CONTEXT_SENSITIVE = true;
	PointerAnalysis.MAX_SPEC_DEPTH = 2;

	System.out.println("PointerAnalysis ... ");
	tstart = time();
        pa = new PointerAnalysis(mcg, mac,
				 new CachingSCCLBBFactory(lbbconv),
				 linker);
	/*
	// intrathread analysis of all the callable methods
	for(Iterator it = mcg.getAllMetaMethods().iterator(); it.hasNext(); ) {
            MetaMethod mm = (MetaMethod) it.next();
            if(!analyzable(mm)) continue;
            pa.getIntParIntGraph(mm);
        }
	*/
	System.out.println((time() - tstart) + "ms");
    }


    private Set filter(Set mroots) {
	Set result = new HashSet();
	if(DEBUG) System.out.println("Root methods:");
	for(Iterator it = mroots.iterator(); it.hasNext(); ) {
	    Object obj = it.next();
	    if(obj instanceof HMethod) {
		if(DEBUG) System.out.println(" " + obj);
		result.add(obj);
	    }
	}
	return result;
    }
    
    /** Checks whether all the checks done in the program are useless or not:
	for any method m having the name "run", no inside object escapes
	from it. */
    private boolean uselessChecks() {
	Set runs = new HashSet();
	for(Iterator it = mcg.getAllMetaMethods().iterator(); it.hasNext(); ) {
	    MetaMethod mm = (MetaMethod) it.next();
	    if(mm.getHMethod().getName().equals("run"))
		runs.add(mm);
	}

	if(DEBUG) {
	    System.out.println("Run methods: ");
	    for(Iterator it = runs.iterator(); it.hasNext(); )
		System.out.println(((MetaMethod) it.next()).getHMethod());
	}
	
	for(Iterator it = runs.iterator(); it.hasNext(); ) {
	    MetaMethod mm = (MetaMethod) it.next();

	    HMethod hm  = mm.getHMethod();
	    String cls_name = hm.getDeclaringClass().getName();
	    if(cls_name.equals("javax.realtime.RealtimeThread") ||
	       cls_name.equals("java.lang.Thread")) {
		System.out.println(mm + " was skipped!");
		continue;
	    }
	    if(containsEscapingStuff(mm)) return false;
	}
	return true;
    }

    
    /** Checks whether any inside node escapes from <code>mm</code>. */
    private boolean containsEscapingStuff(MetaMethod mm) {
	if(!PointerAnalysis.analyzable(mm.getHMethod())) return true;

	// the set of nodes appearing in the external pig is the
	// set of the escaping objects
	ParIntGraph pig = pa.getExtParIntGraph(mm);
	System.out.println("ExtPIG(" + mm + ")\n" + pig);
	Set nodes = pig.allNodes();

	// if one of the elements of the set nodes is an INSIDE node,
	// some objects are leaking out of the memory scope ...
	for(Iterator it = nodes.iterator(); it.hasNext(); ) {
	    PANode node = (PANode) it.next();
	    if(node.type == PANode.INSIDE) return true;
	}
	// nothing escapes!
	return false;
    }
    
    private long time() {
	return System.currentTimeMillis();
    }


    public boolean shouldRemoveCheck(Quad instr) {
	return shouldRemoveAllChecks;
    }

}
