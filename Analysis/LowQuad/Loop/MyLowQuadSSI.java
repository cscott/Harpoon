// MyLowQuadSSI.java, created Fri Jul  9 14:13:48 1999 by root
// Copyright (C) 1999 Brian Demsky <bdemsky@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.LowQuad.Loop;

import harpoon.Analysis.Maps.Derivation;
import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HCode;
import harpoon.ClassFile.HCodeAndMaps;
import harpoon.ClassFile.HCodeElement;
import harpoon.ClassFile.HMethod;
import harpoon.IR.LowQuad.Code;
import harpoon.IR.LowQuad.LowQuadSSI;
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
 * @version $Id: MyLowQuadSSI.java,v 1.1.2.13 2001-11-26 17:59:48 bdemsky Exp $
 */

public class MyLowQuadSSI extends harpoon.IR.LowQuad.LowQuadSSI
    implements Derivation
    /* ergh.  implementing Derivation directly is bad.  bdemsky should fix. */
{
    HashMap dT;
    HashMap tT;
    public static final String codename = "low-quad-ssa";
    Derivation parentDerivation;
    Map quadmap;
    TempMap tempMap;
    Map quadmapchanges;
    
    MyLowQuadSSI(final LowQuadSSI code) {
	super(code.getMethod(),null);
	parentDerivation=code.getDerivation();

	HCodeAndMaps hcam = cloneHelper(code, this);
	quadmap=hcam.elementMap();
	tempMap=hcam.tempMap();

	dT=new HashMap();
	tT=new HashMap();
	quadmapchanges=new HashMap();
	buildmaps(code);
	setDerivation(this);
    }
    
    private void buildmaps(final HCode code) {
	Iterator iterate=((HCode)code).getElementsI();
	while (iterate.hasNext()) {
	    Quad q=(Quad)iterate.next();
	    Temp[] defs=q.def();
	    for(int i=0;i<defs.length;i++) {
		Derivation.DList parents=parentDerivation.derivation(q, defs[i]);
		if (parents!=null) {
		    //System.out.print("MySSI: "+q+","+defs[i]+"->"+quadmap.get(q)+","+tempMap.tempMap(defs[i])+" "+parents);
		    dT.put(tempMap.tempMap(defs[i]),Derivation.DList.rename(parents,tempMap));
		    //System.out.println(dT.get(tempMap.tempMap(defs[i])));
		    tT.put(tempMap.tempMap(defs[i]), 
			   null);
		} else
		    tT.put(tempMap.tempMap(defs[i]),parentDerivation.typeMap(null,defs[i]));
	    }
	}
    }
	
    private MyLowQuadSSI(HMethod method, Quad quads) {
	super (method, quads);
	dT=new HashMap();
	tT=new HashMap();
	quadmapchanges=new HashMap();
	setDerivation(this);
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
	Util.assert(hce!=null && t!=null);
	return (Derivation.DList)dT.get(t);
    }
    

    public void addType(Temp t, Object type) {
	tT.put(t, type);
    }

    public HClass typeMap(HCodeElement hce, Temp t) {
	Util.assert(t!=null);
	Object type = tT.get(t);
	//	if (type==null)
	//  System.out.println("TYPE UNKNOWN for :"+hce+","+t+"in MyLowQuadSSI");
	return (HClass)type; 
    }

    public HCodeAndMaps clone(HMethod newMethod) {
	MyLowQuadSSI lqs=new MyLowQuadSSI(newMethod, null);
	HCodeAndMaps hcam = cloneHelper(lqs);
	lqs.parentDerivation=this.getDerivation();
	lqs.quadmap = hcam.elementMap();
	lqs.tempMap = hcam.tempMap();
	lqs.buildmaps(this);
	return hcam;
    }

    public String getName() {
	return codename;
    }
}
