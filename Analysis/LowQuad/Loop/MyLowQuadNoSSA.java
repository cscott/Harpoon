// MyLowQuadNoSSA.java, created Fri Jul  9 14:13:48 1999 by root
// Copyright (C) 1999 Brian Demsky <bdemsky@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.LowQuad.Loop;

import harpoon.Analysis.Maps.Derivation;
import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HCode;
import harpoon.ClassFile.HCodeElement;
import harpoon.ClassFile.HMethod;
import harpoon.IR.LowQuad.Code;
import harpoon.IR.LowQuad.LowQuadSSI;
import harpoon.IR.Quads.Quad;
import harpoon.IR.Quads.RSSxToNoSSA;
import harpoon.Temp.Temp;
import harpoon.Temp.TempMap;
import harpoon.Util.Util;

import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;

/**
 * <code>MyLowQuadNoSSA</code>
 * 
 * @author  Brian Demsky <bdemsky@mit.edu>
 * @version $Id: MyLowQuadNoSSA.java,v 1.3 2002-02-26 22:40:41 cananian Exp $
 */

public class MyLowQuadNoSSA extends harpoon.IR.LowQuad.LowQuadNoSSA
    implements Derivation 
    /* ergh.  implementing Derivation directly is bad.  bdemsky should fix. */
{
    HashMap dT;
    HashMap tT;
    public static final String codename = "low-quad-ssa";
    Derivation parentDerivation;
    Map quadmap;
    TempMap tempMap;


    MyLowQuadNoSSA(final LowQuadSSI code) {
	super(code.getMethod(),null);
	RSSxToNoSSA translate=new RSSxToNoSSA(qf, code);
	quads=translate.getQuads();
	tempMap=translate.tempMap();
	quadmap=translate.quadMap();
	dT=new HashMap();
	tT=new HashMap();
	parentDerivation=code.getDerivation();
	buildmaps(code, translate.newTempMap());
	setDerivation(this);
    }

    private void buildmaps(final HCode code, Map newtempmap) {
	Iterator iterate=((HCode)code).getElementsI();
	while (iterate.hasNext()) {
	    Quad q=(Quad)iterate.next();
	    Temp[] defs=q.def();
	    for(int i=0;i<defs.length;i++) {
		Derivation.DList parents=parentDerivation.derivation(q, defs[i]);
		if (parents!=null) {
		    System.out.print("NoSSA: "+q+","+defs[i]+"->"+quadmap.get(q)+","+tempMap.tempMap(defs[i])+" "+parents);
		    dT.put(tempMap.tempMap(defs[i]),Derivation.DList.rename(parents,tempMap));
		    System.out.println(dT.get(tempMap.tempMap(defs[i])));
		    tT.put(tempMap.tempMap(defs[i]), 
			   null);
		} else {
		    tT.put(tempMap.tempMap(defs[i]),parentDerivation.typeMap(null,defs[i]));
		    System.out.print("NoSSA: "+q+","+defs[i]+"->"+quadmap.get(q)+","+tempMap.tempMap(defs[i])+" "+parents);
		    System.out.println(tT.get(tempMap.tempMap(defs[i])));
		}
	    }
	}
	Iterator tempmapit=newtempmap.keySet().iterator();
	while(tempmapit.hasNext()) {
	    Temp newt=(Temp)tempmapit.next();
	    Temp oldt=(Temp)newtempmap.get(newt);
	    dT.put(newt, dT.get(oldt));
	    tT.put(newt, tT.get(oldt));
	}
    }
    
    public Derivation.DList derivation(HCodeElement hce, Temp t) {
	Util.ASSERT(hce!=null && t!=null);
	return (Derivation.DList)dT.get(t);
    }
    

    public HClass typeMap(HCodeElement hce, Temp t) {
	Util.ASSERT(t!=null);
	Object type = tT.get(t);
	//	if (type==null)
	//  System.out.println("TYPE UNKNOWN for :"+hce+","+t+"in MyLowQuadQNoSSA");
	return (HClass) type;
    }

    public String getName() {
	return codename;
    }
}
