// InstrumentateAllocs.java, created Tue Nov  7 14:29:16 2000 by root
// Copyright (C) 2000 root <root@BDEMSKY.MIT.EDU>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.PointerAnalysis;

import harpoon.Analysis.Transformation.MethodMutator;
import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HCode;
import harpoon.ClassFile.HCodeAndMaps;
import harpoon.ClassFile.HCodeFactory;
import harpoon.ClassFile.HMethod;
import harpoon.ClassFile.Linker;
import harpoon.Temp.TempFactory;
import harpoon.Temp.Temp;
import harpoon.IR.Quads.Quad;
import harpoon.IR.Quads.QuadFactory;
import harpoon.IR.Quads.NEW;
import harpoon.IR.Quads.ANEW;
import harpoon.IR.Quads.CALL;
import harpoon.IR.Quads.CONST;
import harpoon.IR.Quads.PHI;
import harpoon.IR.Quads.RETURN;
import harpoon.IR.Quads.THROW;
import harpoon.Util.WorkSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * <code>InstrumentateAllocs</code>
 * 
 * @author  root <root@BDEMSKY.MIT.EDU>
 * @version $Id: InstrumentateAllocs.java,v 1.1.2.4 2000-11-08 16:38:13 bdemsky Exp $
 */
public class InstrumentateAllocs extends MethodMutator implements java.io.Serializable {
    HashMap toint;
    HashMap toalloc;
    int count;
    HMethod main;
    Linker linker;
    HCodeFactory parenthcf;
    
    /** Creates a <code>InstrumentateAllocs</code>. */
    public InstrumentateAllocs(HCodeFactory parent, HMethod main, Linker linker) {
        super(parent);
	parenthcf=parent;
	toint=new HashMap();
	toalloc=new HashMap();
	count=0;
	this.main=main;
	this.linker=linker;
    }

    /** Maps <code>NEW</code> and <code>ANEW</code>'s to corresponding integer
     *  identifiers.*/

    public Map toint() {
	return toint;
    }

    /** Maps integer indentifiers to corresponding <code>NEW</code> and <code>ANEW</code>'s.*/

    public Map toalloc() {
	return toalloc;
    }

    public HCodeFactory parent() {
	return parenthcf;
    }

    protected HCode mutateHCode(HCodeAndMaps input) {
	HCode hc=input.hcode();
	Map ancestor=input.ancestorElementMap();
	if (!hc.getMethod().getDeclaringClass().getName().equals("harpoon.Runtime.Instrumentate")) {
	    WorkSet newset=new WorkSet();
	    Iterator it=hc.getElementsI();
	    while(it.hasNext()) {
		Quad q=(Quad)it.next();
		if ((q instanceof NEW)||(q instanceof ANEW)) {
		    newset.add(q);
		    if (!toint.containsKey(q)) {
			toint.put(ancestor.get(q),new Integer(count));
			toalloc.put(new Integer(count++),ancestor.get(q));
		    }
		}
	    }
	    Iterator setit=newset.iterator();
	    HMethod method=linker.forName("harpoon.Runtime.Instrumentate").getMethod("count",new HClass[]{HClass.Int});

	    while(setit.hasNext()) {
		Quad q=(Quad)setit.next();
		QuadFactory qf=q.getFactory();
		TempFactory tf=qf.tempFactory();
		Temp tconst=new Temp(tf);
		Temp texcept=new Temp(tf);
		CONST qconst=new CONST(qf,q,tconst,toint.get(ancestor.get(q)),HClass.Int);
		CALL qcall=new CALL(qf, q, method,new Temp[] {tconst}, null, texcept,false,false,new Temp[0][2],new Temp[0]);
		PHI qphi=new PHI(qf,q,new Temp[0],new Temp[0][2],2);
		Quad.addEdge(qconst,0,qcall,0);
		Quad.addEdge(qcall,0,qphi,0);
		Quad.addEdge(qcall,1,qphi,1);
		Quad.addEdge(q.prev(0),q.prevEdge(0).which_succ(),qconst,0);
		Quad.addEdge(qphi,0,q,0);
	    }
	}
	if (hc.getMethod().equals(main)) {
	    WorkSet exitset=new WorkSet();
	    Iterator it=hc.getElementsI();
	    while(it.hasNext()) {
		Quad q=(Quad)it.next();
		if ((q instanceof RETURN)||(q instanceof THROW))
		    exitset.add(q);
	    }
	    Iterator setit=exitset.iterator();
	    HMethod method=linker.forName("harpoon.Runtime.Instrumentate").getMethod("exit",new HClass[0]);

	    while(setit.hasNext()) {
		Quad q=(Quad)setit.next();
		QuadFactory qf=q.getFactory();
		TempFactory tf=qf.tempFactory();
		Temp texcept=new Temp(tf);
		CALL qcall=new CALL(qf, q, method,new Temp[0], null, texcept,false,false,new Temp[0][2],new Temp[0]);
		PHI qphi=new PHI(qf,q,new Temp[0],new Temp[0][2],2);
		Quad.addEdge(qcall,0,qphi,0);
		Quad.addEdge(qcall,1,qphi,1);
		Quad.addEdge(q.prev(0),q.prevEdge(0).which_succ(),qcall,0);
		Quad.addEdge(qphi,0,q,0);
	    }
	}
	return hc;
    }
}
