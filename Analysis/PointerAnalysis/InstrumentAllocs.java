// InstrumentAllocs.java, created Tue Nov  7 14:29:16 2000 by root
// Copyright (C) 2000 Brian Demsky <bdemsky@mit.edu>
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
import harpoon.IR.Quads.MONITORENTER;
import harpoon.IR.Quads.NEW;
import harpoon.IR.Quads.ALENGTH;
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
 * <code>InstrumentAllocs</code> adds counters to each allocation site.
 * 
 * @author  Brian Demsky <bdemsky@mit.edu>
 * @version $Id: InstrumentAllocs.java,v 1.1.2.12 2001-06-17 22:30:40 cananian Exp $
 */
public class InstrumentAllocs extends MethodMutator implements java.io.Serializable {
    int count;
    HMethod main;
    Linker linker;
    HCodeFactory parenthcf;
    AllocationNumbering an;
    boolean syncs;
    boolean callchains;

    /** Creates a <code>InstrumentAllocs</code>. */
    public InstrumentAllocs(HCodeFactory parent, HMethod main, Linker linker,AllocationNumbering an,boolean syncs, boolean callchains) {
        super(parent);
	parenthcf=parent;
	count=0;
	this.main=main;
	this.linker=linker;
	this.an=an;
	this.syncs=syncs;
	this.callchains=callchains;
    }

    public HCodeFactory parent() {
	return parenthcf;
    }

    protected HCode mutateHCode(HCodeAndMaps input) {
	HCode hc=input.hcode();
	Map ancestor=input.ancestorElementMap();
	if (!hc.getMethod().getDeclaringClass().getName().equals("harpoon.Runtime.CounterSupport")) {
	    WorkSet newset=new WorkSet();
	    Iterator it=hc.getElementsI();
	    while(it.hasNext()) {
		Quad q=(Quad)it.next();
		if ((q instanceof NEW)||(q instanceof ANEW)||(syncs&&(q instanceof MONITORENTER))||(q instanceof CALL))
		    newset.add(q);
	    }
	    Iterator setit=newset.iterator();
	    HMethod method=linker.forName("harpoon.Runtime.CounterSupport").getMethod("count",new HClass[]{HClass.Int});
	    HMethod method2=linker.forName("harpoon.Runtime.CounterSupport").getMethod("countm",new HClass[]{linker.forName("java.lang.Object")});
	    HMethod method3=linker.forName("harpoon.Runtime.CounterSupport").getMethod("label",new HClass[]{linker.forName("java.lang.Object"),HClass.Int});
	    HMethod method4=linker.forName("harpoon.Runtime.CounterSupport").getMethod("callenter",new HClass[]{HClass.Int});
	    HMethod method5=linker.forName("harpoon.Runtime.CounterSupport").getMethod("callexit",new HClass[0]);



	    while(setit.hasNext()) {
		Quad q=(Quad)setit.next();
		QuadFactory qf=q.getFactory();
		TempFactory tf=qf.tempFactory();
		Temp tconst=new Temp(tf);
		Temp texcept=new Temp(tf);
		if (q instanceof MONITORENTER) {
		    CALL qcall=new CALL(qf, q, method2,new Temp[] {((MONITORENTER)q).lock()}, null, texcept,false,false,new Temp[0][2],new Temp[0]);
		    PHI qphi=new PHI(qf,q,new Temp[0],new Temp[0][2],2);
		    Quad.addEdge(qcall,0,qphi,0);
		    Quad.addEdge(qcall,1,qphi,1);
		    Quad.addEdge(q.prev(0),q.prevEdge(0).which_succ(),qcall,0);
		    Quad.addEdge(qphi,0,q,0);
		} else if (q instanceof CALL) {
		    if (((CALL)q).method().equals(linker.forName("java.lang.System").getMethod("exit","(I)V"))) {
			HMethod methode=linker.forName("harpoon.Runtime.CounterSupport").getMethod("exit",new HClass[0]);
			Temp texc=new Temp(tf);
			CALL qcall=new CALL(qf, q, methode,new Temp[0], null, texc,false,false,new Temp[0][2],new Temp[0]);
			PHI qphi=new PHI(qf,q,new Temp[0],new Temp[0][2],2);
			Quad.addEdge(qcall,0,qphi,0);
			Quad.addEdge(qcall,1,qphi,1);
			Quad.addEdge(q.prev(0),q.prevEdge(0).which_succ(),qcall,0);
			Quad.addEdge(qphi,0,q,0);
		    }
		    if (callchains) {
			try {
			    CONST qconst=new CONST(qf,q,tconst,new Integer(an.callID((Quad)ancestor.get(q))),HClass.Int);
			    CALL qcall=new CALL(qf, q, method4,new Temp[] {tconst}, null, texcept,false,false,new Temp[0][2],new Temp[0]);
			    PHI qphi=new PHI(qf,q,new Temp[0],new Temp[0][2],2);
			    Quad.addEdge(qconst,0,qcall,0);
			    Quad.addEdge(qcall,0,qphi,0);
			    Quad.addEdge(qcall,1,qphi,1);
			    Quad.addEdge(q.prev(0),q.prevEdge(0).which_succ(),qconst,0);
			    Quad.addEdge(qphi,0,q,0);
			    
			    
			    CALL qcall2=new CALL(qf, q, method5,new Temp[] {}, null, texcept,false,false,new Temp[0][2],new Temp[0]);
			    PHI qphi2=new PHI(qf,q,new Temp[0],new Temp[0][2],2);
			    Quad.addEdge(qcall2,0,qphi2,0);
			    Quad.addEdge(qcall2,1,qphi2,1);
			    Quad.addEdge(qphi2,0,q.next(0),q.nextEdge(0).which_pred());
			    Quad.addEdge(q,0,qcall2,0);
			    
			    
			    CALL qcall3=new CALL(qf, q, method5,new Temp[] {}, null, texcept,false,false,new Temp[0][2],new Temp[0]);
			    PHI qphi3=new PHI(qf,q,new Temp[0],new Temp[0][2],2);
			    Quad.addEdge(qcall3,0,qphi3,0);
			    Quad.addEdge(qcall3,1,qphi3,1);
			    Quad.addEdge(qphi3,0,q.next(1),q.nextEdge(1).which_pred());
			    Quad.addEdge(q,1,qcall3,0);
			} catch (Error e) {
			    //ignore
			}
		    }
		} else {
		    try {
			CONST qconst=new CONST(qf,q,tconst,new Integer(an.allocID((Quad)ancestor.get(q))),HClass.Int);
			CALL qcall=new CALL(qf, q, method,new Temp[] {tconst}, null, texcept,false,false,new Temp[0][2],new Temp[0]);
			PHI qphi=new PHI(qf,q,new Temp[0],new Temp[0][2],2);
			Quad.addEdge(qconst,0,qcall,0);
			Quad.addEdge(qcall,0,qphi,0);
			Quad.addEdge(qcall,1,qphi,1);
			Quad.addEdge(q.prev(0),q.prevEdge(0).which_succ(),qconst,0);
			Quad.addEdge(qphi,0,q,0);
			if (syncs) {
			    Temp dst=(q instanceof NEW)?((NEW)q).dst():((ANEW)q).dst();
			    qcall=new CALL(qf, q, method3,new Temp[] {dst,tconst}, null, texcept,false,false,new Temp[0][2],new Temp[0]);
			    qphi=new PHI(qf,q,new Temp[0],new Temp[0][2],2);
			    Quad.addEdge(qcall,0,qphi,0);
			    Quad.addEdge(qcall,1,qphi,1);
			    Quad qq=q;
			    /*
			      Shouldn't need to actually place our stuff after
			      initializer call statement...
			    if (q instanceof NEW)
				while (!(qq instanceof CALL)) {
				    if (qq instanceof ALENGTH)
					qq=qq.next(0).next(0).next(1);
				    else
					qq=qq.next(0);
				} */
			    Quad.addEdge(qphi, 0,qq.next(0),qq.nextEdge(0).which_pred());
			    Quad.addEdge(qq,0,qcall,0);
			}
		    } catch (Error e) {
			//Ignore, means that its code called only by our instrumenting
			//code
		    }
		}
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
	    HMethod method=linker.forName("harpoon.Runtime.CounterSupport").getMethod("exit",new HClass[0]);

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
	//	hc.print(new java.io.PrintWriter(System.out,true));
	return hc;
    }
}
