// ThreadInliner.java, created Mon Jun 12 13:38:59 2000 by root
// Copyright (C) 2000 Brian Demsky <bdemsky@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.Quads;


import harpoon.Analysis.Maps.AllocationInformation;
import harpoon.Analysis.AllocationInformationMap;

import harpoon.ClassFile.HCode;
import harpoon.ClassFile.HCodeAndMaps;
import harpoon.ClassFile.HCodeFactory;
import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HMethod;
import harpoon.Temp.TempMap;
import harpoon.Util.Util;
import java.util.Map;
import java.util.Set;
import java.util.Iterator;


/**
 * <code>ThreadInliner</code>
 * 
 * @author  Brian Demsky <bdemsky@mit.edu>
 * @version $Id: ThreadInliner.java,v 1.1.2.4 2001-06-17 22:33:36 cananian Exp $
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
	HCodeAndMaps hcam = null;
	try /* I hate having to explicitly check this exception */
	    { hcam = hc.clone(hc.getMethod()); }
	catch (CloneNotSupportedException ex) { Util.assert(false, ex); }
	QuadNoSSA qns = (QuadNoSSA) hcam.hcode();
	Map quadMap = hcam.elementMap();
	TempMap tempMap = hcam.tempMap();

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
