// SSICallGraph.java, created Mon Apr  8 22:51:08 2002 by salcianu
// Copyright (C) 2000 Alexandru SALCIANU <salcianu@MIT.EDU>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.Quads;

import java.util.Set;
import java.util.Vector;
import java.util.Iterator;
import java.util.Map;
import java.util.HashMap;
import java.util.List;

import harpoon.ClassFile.HMethod;
import harpoon.ClassFile.HCodeFactory;
import harpoon.ClassFile.HCode;

import harpoon.Analysis.ClassHierarchy;
import harpoon.Analysis.ReachingDefs;
import harpoon.Analysis.SSxReachingDefsImpl;
import harpoon.Analysis.Quads.SCC.SCCAnalysis;
import harpoon.Analysis.Maps.ExactTypeMap;
import harpoon.Analysis.Quads.TypeInfo;
import harpoon.IR.Quads.Code;

import harpoon.IR.Quads.CALL;
import harpoon.IR.Quads.Code;
import harpoon.IR.Quads.Quad;
import harpoon.IR.Quads.QuadKind;
import harpoon.IR.Quads.QuadSSI;

import harpoon.Util.Util;

import harpoon.Analysis.PointerAnalysis.Debug;

/**
 * <code>SSICallGraph</code>
 * 
 * @author  Alexandru SALCIANU <salcianu@MIT.EDU>
 * @version $Id: SSICallGraph.java,v 1.4 2002-04-11 18:53:50 salcianu Exp $
 */
public class SSICallGraph extends AbstrCallGraph {

    private CallGraphImpl2 cg;
    private ClassHierarchy ch;
    
    /** Creates a <code>SSICallGraph</code>. */
    public SSICallGraph(ClassHierarchy ch, HCodeFactory hcf) {
	assert hcf.getCodeName().equals(QuadSSI.codename) :
	    "works only with an SSI code factory";
	this.ch  = ch;
	this.hcf = hcf;
	cg = new CallGraphImpl2(ch, hcf);
    }
    
    public HMethod[] calls(final HMethod hm) {
	return cg.calls(hm);
    }


    public HMethod[] calls(final HMethod hm, final CALL cs) {
	return calls(cs);
    }


    public HMethod[] calls(final CALL cs) {
	HMethod[] retval = (HMethod[]) cs2callees.get(cs); 
	if(retval != null)
	    return retval;

	// For efficiency reasons, we compute the callees for all call
	// sites; this way we construct the auxiliary objects
	// "ReachingDefs rd" and "ExactTypeMap etm" once for each
	// method.
	Code hcode = cs.getFactory().getParent();
	ReachingDefs rd  = new SSxReachingDefsImpl(hcode);
	ExactTypeMap etm = new TypeInfo(hcode); 

	List<Quad> calls = hcode.selectCALLs(); 
	for(Iterator<Quad> it = calls.iterator(); it.hasNext(); ) {
	    CALL call = (CALL) it.next();
	    cs2callees.put(call, cg.calls(call, rd, etm));
	}

	return (HMethod[]) cs2callees.get(cs);
    }
    private Map cs2callees = new HashMap();


    public Set callableMethods() {
	return ch.callableMethods();
    }

}
