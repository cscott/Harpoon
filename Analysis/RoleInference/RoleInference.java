// RoleInference.java, created Thu May 17 13:40:49 2001 by bdemsky
// Copyright (C) 2000 root <bdemsky@lcs.mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.RoleInference;


import harpoon.ClassFile.HCode;
import harpoon.ClassFile.HCodeAndMaps;
import harpoon.ClassFile.HCodeFactory;
import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HField;
import harpoon.ClassFile.HMethod;
import harpoon.ClassFile.Linker;
import harpoon.IR.Quads.ANEW;
import harpoon.IR.Quads.ASET;
import harpoon.IR.Quads.AGET;
import harpoon.IR.Quads.CALL;
import harpoon.IR.Quads.CONST;
import harpoon.IR.Quads.SET;
import harpoon.IR.Quads.GET;
import harpoon.IR.Quads.METHOD;
import harpoon.IR.Quads.MOVE;
import harpoon.IR.Quads.PHI;
import harpoon.IR.Quads.RETURN;
import harpoon.IR.Quads.THROW;
import harpoon.IR.Quads.Quad;
import harpoon.IR.Quads.QuadVisitor;
import harpoon.Temp.Temp;
import java.util.List;


/**
 * <code>RoleInference</code>
 * 
 * @author  bdemsky <bdemsky@mit.edu>
 * @version $Id: RoleInference.java,v 1.1.2.2 2001-05-18 18:57:14 bdemsky Exp $
 */
public class RoleInference extends harpoon.Analysis.Transformation.MethodMutator {
    final Linker linker;
    RoleVisitor rv;
    public RoleInference(HCodeFactory parent, Linker linker) {
	super(parent);
	this.linker=linker;
	rv=new RoleVisitor(linker);
    }

    protected HCode mutateHCode(HCodeAndMaps input) {
	HCode hcode=input.hcode();
	if (transform(hcode.getMethod())) {
	    List list=hcode.getElementsL();
	    for (int i=0;i<list.size(); i++) {
		Quad q=(Quad)list.get(i);
		q.accept(rv);
	    }
	}
	return hcode;
    }

    boolean transform(HMethod hc) {
	if (hc.getDeclaringClass().getName().equals("java.lang.RoleInference"))
	    return false;
	else
	    return true;
    }

    static class RoleVisitor extends QuadVisitor {
	HMethod arrayinitmethod;
	HMethod arrayassignmethod;
	HMethod fieldassignmethod;
	HMethod marklocalmethod;
	HMethod returnmethod;
	HMethod entermethod;
	HClass strclass;
	HClass fieldclass;
	HClass clsclass;

	public RoleVisitor(Linker linker) {
	    HClass objclass=linker.forName("java.lang.Object");
	    strclass=linker.forName("java.lang.String");
	    HClass roleclass=linker.forName("java.lang.RoleInference");
	    fieldclass=linker.forName("java.lang.reflect.Field");
	    clsclass=linker.forName("java.lang.Class");
	    arrayinitmethod=roleclass.getDeclaredMethod("arrayassignUID", 
							new HClass[] {objclass, HClass.Int});
	    arrayassignmethod=roleclass.getDeclaredMethod("arrayassign",
							  new HClass[] {objclass, HClass.Int, objclass});
	    fieldassignmethod=roleclass.getDeclaredMethod("fieldassign",
							  new HClass[] {objclass, fieldclass, objclass});
	    marklocalmethod=roleclass.getDeclaredMethod("marklocal",
							new HClass[] {strclass, objclass});
	    returnmethod=roleclass.getDeclaredMethod("returnmethod", new HClass[0]);
	    entermethod=roleclass.getDeclaredMethod("entermethod", new HClass[] {clsclass});
	    
	}

	public void visit(Quad q) {
	}

	public void visit(ANEW q) {
	    int dims=q.dimsLength();
	    Temp dst=q.dst();
	    //FIXME
	    Temp t=new Temp(q.getFactory().tempFactory());
	    Temp texc=new Temp(q.getFactory().tempFactory());
	    CONST c=new CONST(q.getFactory(), q, t, new Integer(dims), HClass.Int);
	    CALL nc=new CALL(q.getFactory(), q, arrayinitmethod,
	    	     new Temp[] {dst,t}, null, texc, false,
	    		     false, new Temp[0]);
	    PHI phi=new PHI(q.getFactory(),q, new Temp[0], 2);
	    
	    Quad.addEdge(phi,0, q.next(0),q.nextEdge(0).which_pred());
	    Quad.addEdge(nc,0,phi,0);
	    Quad.addEdge(nc,1,phi,1);
	    Quad.addEdge(q,0,c,0);
	    Quad.addEdge(c,0,nc,0);
	}

       	public void visit(ASET q) {
	    Temp array=q.objectref();
	    Temp index=q.index();
	    Temp component=q.src();
	    Temp texc=new Temp(q.getFactory().tempFactory());
	    CALL nc=new CALL(q.getFactory(), q, arrayassignmethod,
			     new Temp[] {array, index, component},
			     null, texc, false, false, new Temp[0]);
	    PHI phi=new PHI(q.getFactory(),q, new Temp[0], 2);
	    
	    Quad.addEdge(phi,0, q.next(0),q.nextEdge(0).which_pred());
	    Quad.addEdge(nc,0,phi,0);
	    Quad.addEdge(nc,1,phi,1);
	    Quad.addEdge(q,0,nc,0);
	}

	public void visit(AGET q) {
	    //Potential Local Variable Assignment
	    Temp t=q.dst();
	    Temp tname=new Temp(q.getFactory().tempFactory());
	    String name=t.name();
	    CONST nameconst=new CONST(q.getFactory(), q, tname, name,
				      strclass);
	    Temp texc=new Temp(q.getFactory().tempFactory());
	    CALL nc=new CALL(q.getFactory(),q,marklocalmethod,
			     new Temp[] {tname,t}, null,texc,
			     false,false, new Temp[0]);
	    PHI phi=new PHI(q.getFactory(),q, new Temp[0], 2);
	    
	    Quad.addEdge(phi,0, q.next(0),q.nextEdge(0).which_pred());
	    Quad.addEdge(nc,0,phi,0);
	    Quad.addEdge(nc,1,phi,1);
	    Quad.addEdge(q,0,nameconst,0);
	    Quad.addEdge(nameconst,0, nc,0);
	}

	public void visit(SET q) {
	    //Potential Global Variable Assignment or
	    //Heap modification
	    Temp array=q.objectref();
	    HField field=q.field();
	    Temp tfield=new Temp(q.getFactory().tempFactory());
	    Temp component=q.src();

	    CONST nconst=null;
	    if (array==null) {
		array=new Temp(q.getFactory().tempFactory());
		nconst=new CONST(q.getFactory(),q,array,null, HClass.Void);
	    }

	    CONST cfield=new CONST(q.getFactory(),q,tfield,
				   field, fieldclass);
	    Temp texc=new Temp(q.getFactory().tempFactory());
	    CALL nc=new CALL(q.getFactory(), q, fieldassignmethod,
			     new Temp[] {array, tfield, component},
			     null, texc, false, false, new Temp[0]);
	    PHI phi=new PHI(q.getFactory(),q, new Temp[0], 2);
	    
	    Quad.addEdge(phi,0, q.next(0),q.nextEdge(0).which_pred());
	    Quad.addEdge(q,0,cfield,0);
	    if (nconst!=null) {
		Quad.addEdge(cfield,0,nconst,0);
		Quad.addEdge(nconst,0,nc,0);
	    } else
		Quad.addEdge(cfield,0,nc,0);
	    Quad.addEdge(nc,0,phi,0);
	    Quad.addEdge(nc,1,phi,1);
	}

	public void visit(GET q) {
	    //Potential Local Variable Assignment
	    Temp t=q.dst();
	    Temp tname=new Temp(q.getFactory().tempFactory());
	    String name=t.name();
	    CONST nameconst=new CONST(q.getFactory(), q, tname, name,
				      strclass);
	    Temp texc=new Temp(q.getFactory().tempFactory());
	    CALL nc=new CALL(q.getFactory(),q,marklocalmethod,
			     new Temp[] {tname,t}, null,texc,
			     false,false, new Temp[0]);
	    PHI phi=new PHI(q.getFactory(),q, new Temp[0], 2);
	    
	    Quad.addEdge(phi,0, q.next(0),q.nextEdge(0).which_pred());
	    Quad.addEdge(nc,0,phi,0);
	    Quad.addEdge(nc,1,phi,1);
	    Quad.addEdge(q,0,nameconst,0);
	    Quad.addEdge(nameconst,0, nc,0);
	}

	public void visit(MOVE q) {
	    //Potential Local Variable Assignment
	    Temp t=q.dst();
	    Temp tname=new Temp(q.getFactory().tempFactory());
	    String name=t.name();
	    CONST nameconst=new CONST(q.getFactory(), q, tname, name,
				      strclass);
	    Temp texc=new Temp(q.getFactory().tempFactory());
	    CALL nc=new CALL(q.getFactory(),q,marklocalmethod,
			     new Temp[] {tname,t}, null,texc,
			     false,false, new Temp[0]);
	    PHI phi=new PHI(q.getFactory(),q, new Temp[0], 2);
	    
	    Quad.addEdge(phi,0, q.next(0),q.nextEdge(0).which_pred());
	    Quad.addEdge(nc,0,phi,0);
	    Quad.addEdge(nc,1,phi,1);
	    Quad.addEdge(q,0,nameconst,0);
	    Quad.addEdge(nameconst,0, nc,0);
	}

	public void visit(CALL q) {
	    //Potential Local Variable Assignment
	    Temp t=q.retval();
	    if (t!=null) {
		Temp tname=new Temp(q.getFactory().tempFactory());
		String name=t.name();
		CONST nameconst=new CONST(q.getFactory(), q, tname, name,
					  strclass);
		Temp texc=new Temp(q.getFactory().tempFactory());
		CALL nc=new CALL(q.getFactory(),q,marklocalmethod,
				 new Temp[] {tname,t}, null,texc,
				 false,false, new Temp[0]);
		PHI phi=new PHI(q.getFactory(),q, new Temp[0], 2);
		
		Quad.addEdge(phi,0, q.next(0),q.nextEdge(0).which_pred());
		Quad.addEdge(nc,0,phi,0);
		Quad.addEdge(nc,1,phi,1);
		Quad.addEdge(q,0,nameconst,0);
		Quad.addEdge(nameconst,0, nc,0);
	    }

	    //Potential Local Variable Assignment
	    t=q.retex();
	    Temp tname=new Temp(q.getFactory().tempFactory());
	    String name=t.name();
	    CONST nameconst=new CONST(q.getFactory(), q, tname, name,
				      strclass);
	    Temp texc=new Temp(q.getFactory().tempFactory());
	    CALL nc=new CALL(q.getFactory(),q,marklocalmethod,
			     new Temp[] {tname,t}, null,texc,
			     false,false, new Temp[0]);
	    PHI phi=new PHI(q.getFactory(),q, new Temp[0], 2);
	    
	    Quad.addEdge(phi,0, q.next(1),q.nextEdge(1).which_pred());
	    Quad.addEdge(nc,0,phi,0);
	    Quad.addEdge(nc,1,phi,1);
	    Quad.addEdge(q,1,nameconst,0);
	    Quad.addEdge(nameconst,0, nc,0);
	}

	public void visit(METHOD q) {
	    //Method invocation
	    //Potential Local Variable Assignment
	}
       
	public void visit(RETURN q) {
	    //Method return
	    Temp texc=new Temp(q.getFactory().tempFactory());

	    CALL nc=new CALL(q.getFactory(),q,returnmethod,
			     new Temp[0], null,texc,
			     false,false, new Temp[0]);
	    PHI phi=new PHI(q.getFactory(),q, new Temp[0], 2);
	    Quad.addEdge(nc,0,phi,0);
	    Quad.addEdge(nc,1,phi,1);
	    Quad.addEdge(q.prev(0),q.prevEdge(0).which_succ(), nc,0);
	    Quad.addEdge(phi,0,q,0);
	}

	public void visit(THROW q) {
	    //Method return
	    Temp texc=new Temp(q.getFactory().tempFactory());

	    CALL nc=new CALL(q.getFactory(),q,returnmethod,
			     new Temp[0], null,texc,
			     false,false, new Temp[0]);
	    PHI phi=new PHI(q.getFactory(),q, new Temp[0], 2);
	    Quad.addEdge(nc,0,phi,0);
	    Quad.addEdge(nc,1,phi,1);
	    Quad.addEdge(q.prev(0),q.prevEdge(0).which_succ(), nc,0);
	    Quad.addEdge(phi,0,q,0);
	}
    }
    
    
}
