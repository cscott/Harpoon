// SSICallGraph.java, created Mon Apr  8 22:51:08 2002 by salcianu
// Copyright (C) 2000 Alexandru SALCIANU <salcianu@MIT.EDU>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.Quads;

import java.util.Set;
import java.util.Vector;
import java.util.Iterator;
import java.util.Map;
import java.util.HashMap;

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
 * @version $Id: SSICallGraph.java,v 1.2 2002-04-11 00:41:41 cananian Exp $
 */
public class SSICallGraph implements CallGraph {

    private CallGraphImpl2 cg;
    private ClassHierarchy ch;
    private HCodeFactory  hcf;
    
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

	CALL[] calls = Util.selectCALLs(hcode);
	for(int i = 0; i < calls.length; i++)
	    cs2callees.put(calls[i], cg.calls(calls[i], rd, etm));

	return (HMethod[]) cs2callees.get(cs);
    }
    private Map cs2callees = new HashMap();


    public CALL[] getCallSites(final HMethod hm) {
	CALL[] retval = (CALL[]) cache_cs.get(hm);
	if(retval == null) {
	    retval = Util.selectCALLs((Code)hcf.convert(hm));
	    cache_cs.put(hm, retval);
	}
	return retval;
    }
    final private Map cache_cs = new HashMap();


    public Set callableMethods() {
	return ch.callableMethods();
    }

}
