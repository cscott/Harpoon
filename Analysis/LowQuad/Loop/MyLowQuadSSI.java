// MyLowQuadSSI.java, created Fri Jul  9 14:13:48 1999 by root
// Copyright (C) 1999 Brian Demsky <bdemsky@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.LowQuad.Loop;

import harpoon.Analysis.Maps.Derivation;
import harpoon.Analysis.Maps.Derivation.DList;
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
 * @version $Id: MyLowQuadSSI.java,v 1.5 2003-03-11 18:22:36 cananian Exp $
 */

public class MyLowQuadSSI extends harpoon.IR.LowQuad.LowQuadSSI
    implements Derivation<Quad>
    /* ergh.  implementing Derivation directly is bad.  bdemsky should fix. */
{
    HashMap<Temp,DList> dT;
    HashMap<Temp,HClass> tT;
    public static final String codename = "low-quad-ssa";
    Derivation<Quad> parentDerivation;
    Map<Quad,Quad> quadmap;
    TempMap tempMap;
    Map<Quad,Quad> quadmapchanges;
    
    MyLowQuadSSI(final LowQuadSSI code) {
	super(code.getMethod(),null);
	parentDerivation=code.getDerivation();

	HCodeAndMaps<Quad> hcam = cloneHelper(code, this);
	quadmap=hcam.elementMap();
	tempMap=hcam.tempMap();

	dT=new HashMap<Temp,DList>();
	tT=new HashMap<Temp,HClass>();
	quadmapchanges=new HashMap<Quad,Quad>();
	buildmaps(code);
	setDerivation(this);
    }
    
    private void buildmaps(final HCode<Quad> code) {
	Iterator<Quad> iterate=code.getElementsI();
	while (iterate.hasNext()) {
	    Quad q=iterate.next();
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
	dT=new HashMap<Temp,DList>();
	tT=new HashMap<Temp,HClass>();
	quadmapchanges=new HashMap<Quad,Quad>();
	setDerivation(this);
    }
    
    public Quad quadMap(Quad q) {
	if (quadmapchanges.containsKey(q))
	    return quadmapchanges.get(q);
	else
	    return quadmap.get(q);
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
    
    public Derivation.DList derivation(Quad hce, Temp t) {
	assert hce!=null && t!=null;
	return dT.get(t);
    }
    

    public void addType(Temp t, HClass type) {
	tT.put(t, type);
    }

    public HClass typeMap(Quad hce, Temp t) {
	assert t!=null;
	HClass type = tT.get(t);
	//	if (type==null)
	//  System.out.println("TYPE UNKNOWN for :"+hce+","+t+"in MyLowQuadSSI");
	return type; 
    }

    public HCodeAndMaps<Quad> clone(HMethod newMethod) {
	MyLowQuadSSI lqs=new MyLowQuadSSI(newMethod, null);
	HCodeAndMaps<Quad> hcam = cloneHelper(lqs);
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
