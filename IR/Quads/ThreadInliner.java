// ThreadInliner.java, created Mon Jun 12 13:38:59 2000 by root
// Copyright (C) 2000 root <root@bdemsky.mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.Quads;


import harpoon.Analysis.Maps.AllocationInformation;
import harpoon.Analysis.AllocationInformationMap;

import harpoon.ClassFile.HCode;
import harpoon.ClassFile.HCodeFactory;
import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HMethod;
import harpoon.Temp.TempMap;
import java.util.Map;
import java.util.Set;
import java.util.Iterator;


/**
 * <code>ThreadInliner</code>
 * 
 * @author  root <root@bdemsky.mit.edu>
 * @version $Id: ThreadInliner.java,v 1.1.2.2 2000-06-13 19:20:22 bdemsky Exp $
 */
public class ThreadInliner {
    /** Creates a <code>ThreadInliner</code>. */

    /** Returns a <code>HCodeFactory</code> that uses <code>LoopOptimize</code>. */
    public static HCodeFactory codeFactory(final HCodeFactory parent, final Set startset, final Set joinset) {
	return new HCodeFactory() {
	    public HCode convert(HMethod m) {
		HCode hc = parent.convert(m);
		if (hc!=null) {
		    return makeswaps(hc,startset,joinset);
		} else
		    return hc;
	    }
	    public String getCodeName() { return parent.getCodeName(); }
	    public void clear(HMethod m) { parent.clear(m); }
	};
    }
    
    public static HCode makeswaps(HCode hc,final Set startset,final Set joinset) {
	QuadNoSSA qns = new QuadNoSSA(hc.getMethod(), null);
	Object[]  maps= Quad.cloneMaps(qns.qf, (Quad) hc.getRootElement());
	Map quadMap=(Map) maps[0];
	TempMap tempMap=(TempMap) maps[1];
	qns.quads=(Quad)quadMap.get(hc.getRootElement());
	AllocationInformation old=((Code)hc).getAllocationInformation();
	AllocationInformationMap newai=new AllocationInformationMap();
	if (old!=null)
	    qns.setAllocationInformation(newai);
	Iterator it=hc.getElementsI();
	while (it.hasNext()) {
	    Quad qd=(Quad)it.next();
	    if (startset.contains(qd)) {
		CALL ctoswap=(CALL)quadMap.get(qd);
		CALL newcall=new CALL(ctoswap.getFactory(),ctoswap,
				      ctoswap.method().getDeclaringClass().getMethod("run",new HClass[0]),
				      ctoswap.params(), ctoswap.retval(),
				      ctoswap.retex(), ctoswap.isVirtual(),
				      ctoswap.isTailCall(),ctoswap.dst(),
				      ctoswap.src());
		Quad.replace(ctoswap, newcall);
	    } else if (joinset.contains(qd)) {
		CALL ctoswap=(CALL)quadMap.get(qd);
		CALL newcall=new CALL(ctoswap.getFactory(),ctoswap,
				      ctoswap.method().getDeclaringClass().getMethod("joinreplace",new HClass[0]),
				      ctoswap.params(), ctoswap.retval(),
				      ctoswap.retex(), ctoswap.isVirtual(),
				      ctoswap.isTailCall(),ctoswap.dst(),
				      ctoswap.src());
		Quad.replace(ctoswap, newcall);
	    } else if ((old!=null)&&((qd instanceof NEW)||(qd instanceof ANEW))&&old.query(qd)!=null) {
		newai.transfer((Quad)quadMap.get(qd),qd,tempMap,
			       old);
	    }
	}
	return qns;
    }
}
