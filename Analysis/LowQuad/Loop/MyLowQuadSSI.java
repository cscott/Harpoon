// MyLowQuadSSI.java, created Fri Jul  9 14:13:48 1999 by root
// Copyright (C) 1999 Brian Demsky <bdemsky@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.LowQuad.Loop;


import harpoon.ClassFile.*;
import harpoon.IR.Properties.Derivation;
import harpoon.IR.LowQuad.Code;
import harpoon.IR.LowQuad.LowQuadSSA;
import harpoon.IR.Quads.Quad;
import harpoon.Temp.Temp;
import harpoon.Temp.TempMap;
import harpoon.Util.Util;

import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;

/**
 * <code>MyLowQuadSSI</code>
 * 
 * @author  Brian Demsky <bdemsky@mit.edu>
 * @version $Id: MyLowQuadSSI.java,v 1.1.2.3 1999-07-17 11:53:39 cananian Exp $
 */

public class MyLowQuadSSI extends harpoon.IR.LowQuad.LowQuadSSA {
    //harpoon.IR.LowQuad.Code  {
    HashMap dT;
    HashMap tT;
    public static final String codename = "low-quad-ssa";
    harpoon.IR.LowQuad.Code parent;
    Map quadmap;
    TempMap tempMap;
    Map quadmapchanges;
    
    MyLowQuadSSI(final LowQuadSSA code) {
	super(code.getMethod(),null);
	parent=code;
	Object[] Maps=Quad.cloneMaps(qf, (Quad)code.getRootElement());
	quadmap=(Map)Maps[0];
	tempMap=(TempMap)Maps[1];
	quads=(Quad) quadmap.get(code.getRootElement());
	dT=new HashMap();
	tT=new HashMap();
	quadmapchanges=new HashMap();
	buildmaps(code);
    }
    
    private void buildmaps(final HCode code) {
	Iterator iterate=((HCode)code).getElementsI();
	while (iterate.hasNext()) {
	    Quad q=(Quad)iterate.next();
	    Temp[] defs=q.def();
	    for(int i=0;i<defs.length;i++) {
		Derivation.DList parents=parent.derivation(q, defs[i]);
		if (parents!=null) {
		    dT.put(tempMap.tempMap(defs[i]),Derivation.DList.rename(parents,tempMap));
		    tT.put(tempMap.tempMap(defs[i]), 
			   new Error("Cant type derived pointer: "+tempMap.tempMap(defs[i])));
		} else
		    tT.put(tempMap.tempMap(defs[i]),parent.typeMap(parent, defs[i]));
	    }
	}
    }
	
    private MyLowQuadSSI(HMethod method, Quad quads) {
	super (method, quads);
	dT=new HashMap();
	tT=new HashMap();
	quadmapchanges=new HashMap();
    }
    
    public Quad quadMap(Quad q) {
	if (quadmapchanges.containsKey(q))
	    return (Quad)quadmapchanges.get(q);
	else
	    return (Quad)quadmap.get(q);
    }
    
    public void addQuadMapping(Quad oldquad, Quad newquad) {
	quadmapchanges.put(oldquad, newquad);
    } 
    
    public Temp tempMap(Temp t) {
	return tempMap.tempMap(t);
    }

    public void addDerivation(Temp t, Derivation.DList dlist) {
	dT.put(t, dlist);
    }
    
    public Derivation.DList derivation(HCodeElement hce, Temp t) {
	if ((hce==null)||(t==null))
	    return null;
	else
	    return (Derivation.DList)dT.get(t);
    }
    

    public void addType(Temp t, Object type) {
	tT.put(t, type);
    }

    public HClass typeMap(HCode hc, Temp t) {
	Util.assert(qf.tempFactory()==t.tempFactory());
	Object type = tT.get(t);
	if (type instanceof Error)
	    throw (Error)((Error)type).fillInStackTrace();
	else
	    return (HClass)type;
    }

    public HCode clone(HMethod newMethod) {
	MyLowQuadSSI lqs=new MyLowQuadSSI(newMethod, null);
	lqs.quads=Quad.clone(lqs.qf, quads);
	lqs.parent=this;
	Object[] Maps=Quad.cloneMaps(lqs.qf, (Quad)this.getRootElement());
	lqs.quadmap=(Map)Maps[0];
	lqs.tempMap=(TempMap)Maps[1];
	lqs.quads=(Quad) lqs.quadmap.get(this.getRootElement());
	lqs.buildmaps(this);
	return lqs;
    }

    public String getName() {
	return codename;
    }
}
    
