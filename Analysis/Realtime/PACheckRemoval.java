// PACheckRemoval.java, created Mon Jan 22 19:51:51 2001 by salcianu
// Copyright (C) 2000 Alexandru SALCIANU <salcianu@MIT.EDU>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.Realtime;

import java.util.Map;
import java.util.Hashtable;
import java.util.Set;
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
 * <code>PACheckRemoval</code> is a pointer analysis based
 * implementation of the <codE>CheckRemoval</code> interface.
 * 
 * @author  Alexandru SALCIANU <salcianu@MIT.EDU>
 * @version $Id: PACheckRemoval.java,v 1.1.2.5 2001-04-19 17:16:37 salcianu Exp $ */
public class PACheckRemoval implements CheckRemoval {

    PointerAnalysis pa = null;

    private static final boolean SMART_CALL_GRAPH = true;
    
    /** Creates a <code>PACheckRemoval</code>. */
    public PACheckRemoval(Linker linker, ClassHierarchy ch,
			  HCodeFactory hcf, Set mroots) {

	Util.assert(hcf.getCodeName().equals(QuadNoSSA.codename),
		    "Not a QuadNoSSA code factory");
	// we really need a caching code factory; raise an error otherwise
	CachingCodeFactory ccf = (CachingCodeFactory) hcf;
	CachingBBConverter bbconv = new CachingBBConverter(ccf);
	LBBConverter lbbconv = new CachingLBBConverter(bbconv);

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

	MetaCallGraph mcg = 
	    new FakeMetaCallGraph(cg, cg.callableMethods(), run_mms);

	MetaAllCallers mac = new MetaAllCallers(mcg);
	System.out.println((time() - tstart) + "ms");

	System.out.println("PointerAnalysis ... ");
	tstart = time();
        pa = new PointerAnalysis(mcg, mac,
				 new CachingSCCLBBFactory(lbbconv),
				 linker);
	// intrathread analysis of all the callable methods
	for(Iterator it = mcg.getAllMetaMethods().iterator(); it.hasNext(); ) {
            MetaMethod mm = (MetaMethod) it.next();
            if(!analyzable(mm)) continue;
            pa.getIntParIntGraph(mm);
        }
	System.out.println((time() - tstart) + "ms");
    }


    private boolean analyzable(MetaMethod mm) {
	HMethod hm = mm.getHMethod();
	if(java.lang.reflect.Modifier.isNative(hm.getModifiers()))
	    return false;
	return true;
    }


    private long time() {
	return System.currentTimeMillis();
    }



    // returns the a from (SET) a.f=b or (ASET) a[i]=b
    private Temp getDestTemp(Quad instr) {
	class CRQuadVisitor extends QuadVisitor {
		public Temp a;
		
		public void visit(SET q) {
		    a = q.objectref();
		}
		
		public void visit(ASET q) {
		    a = q.objectref();
		}
		
		public void visit(Quad q) {
		    Util.assert(false, "Not a SET or an ASET quad!");
		}
	    };
	CRQuadVisitor qv = new CRQuadVisitor();
	instr.accept(qv);
	return qv.a;
    }

    /* instr: SET or ASET
       a.f = b
       a[i] = b
       returns true iff a does not escape from a run method
       (conservative approx of "a does not escape from the run method of
       a memory scope). */
    public boolean shouldRemoveCheck(Quad instr) {
	Temp a = getDestTemp(instr);
	MetaMethod mm = null; // TODO

	// treat the static fields (ie global vars)
	if(a == null) return false;
	
	// the nodes that might by pointed to by a
	Set nodes = pa.pointedNodes(instr, a);
	for(Iterator it = nodes.iterator(); it.hasNext(); ) {
	    PANode node = (PANode) it.next();
	    // if any of them might escape, the check is needed
	    if(!remainInMemScope(node, mm))
		return false;
	}
	
	return true;
    }


    // attaches to each node created by the pointer analysis a Boolean that
    // conservatively says whether it remains in the memory scope run method.
    Map node2remain = new Hashtable();

    private boolean remainInMemScope(PANode node, MetaMethod mm) {
	Boolean remain = (Boolean) node2remain.get(node);
	if(remain == null) {
	    remain = new Boolean(remainInMemScope2(node, mm, ""));
	    node2remain.put(node, remain);
	}
	return remain.booleanValue();
    }


    private boolean remainInMemScope2(PANode node, MetaMethod mm, String idt) {
	return false;
    }
		
}
