// RoleInference.java, created Thu May 17 13:40:49 2001 by bdemsky
// Copyright (C) 2000 root <bdemsky@lcs.mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.RoleInference;


import harpoon.Analysis.Quads.QuadLiveness;
import harpoon.Analysis.ReachingDefs;
import harpoon.Analysis.ReachingDefsImpl;
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
import harpoon.IR.Quads.NEW;
import harpoon.IR.Quads.MOVE;
import harpoon.IR.Quads.PHI;
import harpoon.IR.Quads.RETURN;
import harpoon.IR.Quads.THROW;
import harpoon.IR.Quads.Quad;
import harpoon.IR.Quads.QuadKind;
import harpoon.IR.Quads.QuadVisitor;
import harpoon.Temp.Temp;
import harpoon.Util.TypeInference.TypeInference;
import harpoon.Util.TypeInference.ExactTemp;
import harpoon.Util.WorkSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;


/**
 * <code>RoleInference</code>
 * 
 * @author  bdemsky <bdemsky@mit.edu>
 * @version $Id: RoleInference.java,v 1.1.2.5 2001-06-07 15:16:25 bdemsky Exp $
 */
public class RoleInference extends harpoon.Analysis.Transformation.MethodMutator {
    final Linker linker;
    public RoleInference(HCodeFactory parent, Linker linker) {
	super(parent);
	this.linker=linker;
    }

    protected HCode mutateHCode(HCodeAndMaps input) {
	HCode hcode=input.hcode();
	//System.out.println(hcode.getMethod().getName());

	if ((hcode.getMethod().getModifiers()&java.lang.reflect.Modifier.NATIVE)!=0) 
	    return hcode;
	RoleVisitor rv=new RoleVisitor(linker, hcode);
	
	if (transform(hcode.getMethod())) {
	    List list=hcode.getElementsL();
	    for (int i=0;i<list.size();i++)
		rv.setoftypes((Quad)list.get(i));
	    rv.dotyping();
	    
	    for (int i=0;i<list.size(); i++) {
		Quad q=(Quad)list.get(i);
		rv.nonlive(q);
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
	HMethod objectinitmethod;
	HMethod arrayassignmethod;
	HMethod fieldassignmethod;
	HMethod marklocalmethod;
	HMethod killlocalmethod;
	HMethod returnmethod;
	HMethod invokemethod;
	HClass strclass;
	HClass fieldclass;
	HClass methodclass;
	HClass clsclass;
	QuadLiveness liveness;
	ReachingDefs reachingdef;
	Set exacttemps;
	TypeInference ti;
	HCode hc;
	LocalVariableNamer lvn;

	public RoleVisitor(Linker linker, HCode hc) {
	    liveness=new QuadLiveness(hc);
	    reachingdef=new ReachingDefsImpl(hc);
	    exacttemps=new WorkSet();
	    lvn=new LocalVariableNamer(hc.getMethod());
	    this.hc=hc;
	    HClass objclass=linker.forName("java.lang.Object");
	    clsclass=linker.forName("java.lang.Class");
	    strclass=linker.forName("java.lang.String");
	    HClass roleclass=linker.forName("java.lang.RoleInference");
	    fieldclass=linker.forName("java.lang.reflect.Field");
	    methodclass=linker.forName("java.lang.reflect.Method");
	    arrayinitmethod=roleclass.getDeclaredMethod("arrayassignUID", 
							new HClass[] {objclass, HClass.Int});
	    objectinitmethod=objclass.getDeclaredMethod("assignUID", 
							new HClass[] {clsclass});
	    arrayassignmethod=roleclass.getDeclaredMethod("arrayassign",
							  new HClass[] {objclass, HClass.Int, objclass});
	    fieldassignmethod=roleclass.getDeclaredMethod("fieldassign",
							  new HClass[] {objclass, fieldclass, objclass});
	    marklocalmethod=roleclass.getDeclaredMethod("marklocal",
							new HClass[] {strclass, objclass});
	    killlocalmethod=roleclass.getDeclaredMethod("killlocal",
							new HClass[] {strclass});
	    returnmethod=roleclass.getDeclaredMethod("returnmethod", new HClass[0]);
	    invokemethod=roleclass.getDeclaredMethod("invokemethod", new HClass[] {methodclass});
	}

	public void visit(Quad q) {
	}

	public void nonlive(Quad q) {
	    int kind=q.kind();
	    if ((kind!=QuadKind.HEADER)&&(kind!=QuadKind.FOOTER)&&(kind!=QuadKind.RETURN)&&(kind!=QuadKind.THROW)) {
		Set livein=liveness.getLiveIn(q);
		livein.addAll(q.defC());//wanna do kills of useless locals
		livein.removeAll(liveness.getLiveOut(q));
		for(Iterator it=livein.iterator();it.hasNext();) {
		    Temp t=(Temp)it.next();
		    Set possibletypes=ti.getType(new ExactTemp(q,t));
		    boolean couldbeobject=false;
		    for(Iterator typeit=possibletypes.iterator();typeit.hasNext();)
			if (!((HClass)typeit.next()).isPrimitive())
			    couldbeobject=true;
		    if (couldbeobject) {
			for(int i=0;i<q.nextLength();i++) {
			    //Live In, not Live Out
			    //Needs to be object also
			    Temp tname=new Temp(q.getFactory().tempFactory());
			    String name=buildname(q,t);
			    CONST nameconst=new CONST(q.getFactory(), q, tname, name,
						      strclass);
			    Temp texc=new Temp(q.getFactory().tempFactory());
			    CALL nc=new CALL(q.getFactory(),q,killlocalmethod,
					     new Temp[] {tname}, null,texc,
					     false,false, new Temp[0]);
			    PHI phi=new PHI(q.getFactory(),q, new Temp[0], 2);
			    
			    Quad.addEdge(phi,0, q.next(i),q.nextEdge(i).which_pred());
			    Quad.addEdge(nc,0,phi,0);
			    Quad.addEdge(nc,1,phi,1);
			    Quad.addEdge(q,i,nameconst,0);
			    Quad.addEdge(nameconst,0, nc,0);
			}
		    }
		}
	    }
	}

	public void setoftypes(Quad q) {
	    Set livein=liveness.getLiveIn(q);
	    livein.addAll(q.defC());
	    livein.removeAll(liveness.getLiveOut(q));
	    //live in now contains newly dead local vars
	    for(Iterator it=livein.iterator();it.hasNext();) {
		Temp t=(Temp)it.next();
		exacttemps.add(new ExactTemp(q,t));
	    }
	    if (q instanceof MOVE) {
		exacttemps.add(new ExactTemp(q,((MOVE)q).src()));
	    }
	}

	public void dotyping() {
	    ti=new TypeInference(hc.getMethod(),hc,exacttemps);
	}

	public String buildname(Quad q, Temp t) { 
	    String othername="$$$unk";
	    String name=t.name();
	    int linenumber=q.getLineNumber();
	    if ((name.length()>2)&&(name.charAt(0)=='l')&&(name.charAt(1)=='v')) {
		int endindex=name.indexOf('_');
		String number=name.substring(2, endindex);
		int lvnumber=Integer.parseInt(number);
		//System.out.println(lvnumber+":"+linenumber+" "+name);
		String ts=lvn.lv_name(lvnumber,linenumber);
		if (ts!=null) {
		    othername=ts;
		    //System.out.println("*********"+ts);
		}
	    }
	    return t.name()+" "+linenumber+" "+othername;
	}

	public void visit(ANEW q) {
	    //FIXME::Needs to do Marklocal
	    int dims=q.dimsLength();
	    Temp dst=q.dst();
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
	    
	    Quad q2=phi;
	    Temp tname=new Temp(q.getFactory().tempFactory());
	    String name=buildname(q,dst);
	    CONST nameconst=new CONST(q.getFactory(), q, tname, name,
				      strclass);
	    texc=new Temp(q.getFactory().tempFactory());
	    nc=new CALL(q.getFactory(),q,marklocalmethod,
			new Temp[] {tname,dst}, null,texc,
			false,false, new Temp[0]);
	    phi=new PHI(q.getFactory(),q, new Temp[0], 2);
	    Quad.addEdge(phi,0, q2.next(0),q2.nextEdge(0).which_pred());
	    Quad.addEdge(nc,0,phi,0);
	    Quad.addEdge(nc,1,phi,1);
	    Quad.addEdge(q2,0,nameconst,0);
	    Quad.addEdge(nameconst,0, nc,0);
	}

	public void visit(NEW q) {
	    Temp t=q.dst();
	    Temp texc=new Temp(q.getFactory().tempFactory()), 
		tcls=new Temp(q.getFactory().tempFactory());
	    
	    CONST cc=new CONST(q.getFactory(), q, tcls, q.hclass(),
			       clsclass);
	    CALL nc=new CALL(q.getFactory(), q, objectinitmethod,
	    	     new Temp[] {t,tcls}, null, texc, true,
	    		     false, new Temp[0]);
	    PHI phi=new PHI(q.getFactory(),q, new Temp[0], 2);
	    
	    Quad.addEdge(phi,0, q.next(0),q.nextEdge(0).which_pred());
	    Quad.addEdge(nc,0,phi,0);
	    Quad.addEdge(nc,1,phi,1);
	    Quad.addEdge(q,0,cc,0);
	    Quad.addEdge(cc,0,nc,0);

	    Quad q2=phi;
	    Temp tname=new Temp(q.getFactory().tempFactory());
	    String name=buildname(q,t);
	    CONST nameconst=new CONST(q.getFactory(), q, tname, name,
				      strclass);
	    texc=new Temp(q.getFactory().tempFactory());
	    nc=new CALL(q.getFactory(),q,marklocalmethod,
			new Temp[] {tname,t}, null,texc,
			false,false, new Temp[0]);
	    phi=new PHI(q.getFactory(),q, new Temp[0], 2);
	    Quad.addEdge(phi,0, q2.next(0),q2.nextEdge(0).which_pred());
	    Quad.addEdge(nc,0,phi,0);
	    Quad.addEdge(nc,1,phi,1);
	    Quad.addEdge(q2,0,nameconst,0);
	    Quad.addEdge(nameconst,0, nc,0);
	}

       	public void visit(ASET q) {
	    if (!q.type().isPrimitive()) {
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
	}

	public void visit(AGET q) {
	    //Potential Local Variable Assignment
	    if (!q.type().isPrimitive()) {
		Temp t=q.dst();
		Temp tname=new Temp(q.getFactory().tempFactory());
		String name=buildname(q,t);
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
	}

	public void visit(SET q) {
	    //Potential Global Variable Assignment or
	    //Heap modification
	    if (!q.field().getType().isPrimitive()) {
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
	}

	public void visit(GET q) {
	    //Potential Local Variable Assignment
	    if (!q.field().getType().isPrimitive()) {
		Temp t=q.dst();
		Temp tname=new Temp(q.getFactory().tempFactory());
		String name=buildname(q,t);
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
	}

	public void visit(MOVE q) {
	    //Potential Local Variable Assignment
	    Temp t=q.dst();
	    Temp torig=q.src();

	    Set possibletypes=ti.getType(new ExactTemp(q,torig));
	    boolean couldbeobject=false;
	    for(Iterator typeit=possibletypes.iterator();typeit.hasNext();)
		if (!((HClass)typeit.next()).isPrimitive())
		    couldbeobject=true;
	    
	    if (couldbeobject) {
		Temp tname=new Temp(q.getFactory().tempFactory());
		String name=buildname(q,t);
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
	}

	public void visit(CALL q) {
	    //Potential Local Variable Assignment
	    Temp t=q.retval();
	    if ((t!=null)&&(!q.method().getReturnType().isPrimitive())) {
		Temp tname=new Temp(q.getFactory().tempFactory());
		String name=buildname(q,t);
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
	    String name=buildname(q,t);
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


	    Quad old=null;
	    {
		Temp texc=new Temp(q.getFactory().tempFactory());
		Temp tmethod=new Temp(q.getFactory().tempFactory());
		HMethod method=q.getFactory().getMethod();
		CONST mconst=new CONST(q.getFactory(),q, tmethod, method,
				       methodclass);
		CALL cn=new CALL(q.getFactory(), q, invokemethod,
				 new Temp[]{tmethod}, null, texc,
				 false, false, new Temp[0]);
		PHI phi=new PHI(q.getFactory(),q, new Temp[0], 2);
		Quad.addEdge(mconst, 0, cn,0);
		Quad.addEdge(cn,0,phi,0);
		Quad.addEdge(cn,1,phi,1);
		Quad.addEdge(phi,0,q.next(0),q.nextEdge(0).which_pred());
		Quad.addEdge(q,0,mconst,0);
		old=phi;
	    }

	    for(int i=0;i<q.paramsLength();i++) {
		if (((i==0)&&(!hc.getMethod().isStatic()))||(!hc.getMethod().getParameterTypes()[i-(hc.getMethod().isStatic()?0:1)].isPrimitive())) {
		    Temp t=q.params(i);
		    Temp tname=new Temp(q.getFactory().tempFactory());
		    String name=buildname(q,t);
		    CONST nameconst=new CONST(q.getFactory(), q, tname, name,
					      strclass);
		    Temp texc=new Temp(q.getFactory().tempFactory());
		    CALL nc=new CALL(q.getFactory(),q,marklocalmethod,
				     new Temp[] {tname,t}, null,texc,
				     false,false, new Temp[0]);
		    PHI phi=new PHI(q.getFactory(),q, new Temp[0], 2);
		    
		    Quad.addEdge(phi,0, old.next(0),old.nextEdge(0).which_pred());
		    Quad.addEdge(nc,0,phi,0);
		    Quad.addEdge(nc,1,phi,1);
		    Quad.addEdge(old,0,nameconst,0);
		    Quad.addEdge(nameconst,0, nc,0);
		    old=phi;
		}
	    }
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
