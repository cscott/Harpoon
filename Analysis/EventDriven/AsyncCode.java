// AsyncCode.java, created Thu Nov 11 15:17:54 1999 by kkz
// Copyright (C) 1999 Karen K. Zee <kkzee@alum.mit.edu>
// 		      and Brian Demsky <bdemsky@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.EventDriven;

import harpoon.Analysis.ContBuilder.ContBuilder;
import harpoon.Analysis.EnvBuilder.EnvBuilder;
import harpoon.Analysis.Quads.QuadLiveness;
import harpoon.Analysis.Quads.Unreachable;
import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HClassMutator;
import harpoon.ClassFile.HMethod;
import harpoon.ClassFile.HMethodMutator;
//import harpoon.ClassFile.HClassSyn;
import harpoon.ClassFile.HCode;
import harpoon.ClassFile.HConstructor;
import harpoon.ClassFile.HField;
import harpoon.ClassFile.Loader;
import harpoon.ClassFile.Linker;
import harpoon.ClassFile.Relinker;
import harpoon.ClassFile.UniqueName;


//import harpoon.ClassFile.HMethodSyn;
import harpoon.ClassFile.UpdateCodeFactory;
import harpoon.IR.Quads.CALL;
import harpoon.IR.Quads.CJMP;
import harpoon.IR.Quads.CONST;
import harpoon.IR.Quads.Code;
import harpoon.IR.Quads.Edge;
import harpoon.IR.Quads.FOOTER;
import harpoon.IR.Quads.GET;
import harpoon.IR.Quads.SET;
import harpoon.IR.Quads.HEADER;
import harpoon.IR.Quads.METHOD;
import harpoon.IR.Quads.NEW;
import harpoon.IR.Quads.OPER;
import harpoon.IR.Quads.Qop;
import harpoon.IR.Quads.PHI;
import harpoon.IR.Quads.Quad;
import harpoon.IR.Quads.QuadFactory;
import harpoon.IR.Quads.QuadNoSSA;
import harpoon.IR.Quads.RETURN;
import harpoon.IR.Quads.THROW;
import harpoon.IR.Quads.TYPECAST;
import harpoon.IR.Quads.QuadVisitor;
import harpoon.Temp.Temp;
import harpoon.Temp.TempFactory;
import harpoon.Temp.CloningTempMap;
import harpoon.Util.Util;
import harpoon.Util.WorkSet;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;


/**
 * <code>AsyncCode</code>
 * 
 * @author Karen K. Zee <kkzee@alum.mit.edu>
 * @version $Id: AsyncCode.java,v 1.1.2.26 2000-01-14 07:56:26 bdemsky Exp $
 */
public class AsyncCode {

    /** Creates a <code>AsyncCode</code>. 
     *
     *  @param hc
     *         the <code>HCode</code> from which to build this
     *         <code>AsyncCode</code>.
     *  @param old2new
     *         the <code>Map</code> mapping blocking methods to the
     *         new Asynchronous version
     *  @param async_todo
     *         the <code>Set</code> of old HCodes for methods to build HCodes 
     *         for the asynchronous versions
     *  @param liveness
     *         results of liveness analysis
     *  @param blockingcalls
     *         set of methods that block
     *  @param ucf
     *         <code>UpdateCodeFactory</code> with which to register new
     *         <code>HCode</code>
     *  @param bm
     *         <code>ToAsync.BlockingMethods</code> object which tells
     *         the analysis information about lowest level blocking calls
     */

    public static void buildCode(HCode hc, Map old2new, Set async_todo, 
			   QuadLiveness liveness,
			   Set blockingcalls, 
			   UpdateCodeFactory ucf, ToAsync.BlockingMethods bm, HMethod mroot, Linker linker) 
	throws NoClassDefFoundError
    {
	System.out.println("Entering AsyncCode.buildCode()");

	Quad root = (Quad)hc.getRootElement();

	//Cont_todo is the set of quads we need to build continuations for

	WorkSet cont_todo=new WorkSet();
	cont_todo.add(root);

	//contmap maps blocking calls->continuations
	//envmap maps blocking calls->environment class
	HashMap cont_map=new HashMap();
	HashMap env_map=new HashMap();
	
	//iterate through all of the necessary continuations for this HMethod
	while(!cont_todo.isEmpty()) {
	    Quad quadc=(Quad) cont_todo.pop();
	    System.out.println("AsyncCode building continuation for "+quadc);
	    ContVisitor cv=new ContVisitor(cont_todo, async_todo, 
					   old2new, cont_map, 
					   env_map, liveness,
					   blockingcalls, hc.getMethod(), 
					   hc, ucf,bm,mroot, linker);
	    quadc.accept(cv);
	}
    }

    static class ContVisitor extends QuadVisitor {
	WorkSet cont_todo;
	Map old2new, cont_map, env_map;
	Set blockingcalls, async_todo;
	HMethod hmethod;
	boolean header;
	CloningVisitor clonevisit;
	QuadLiveness liveness;
	UpdateCodeFactory ucf;
	Linker linker;

	public ContVisitor(WorkSet cont_todo, Set async_todo, 
			   Map old2new, Map cont_map, 
			   Map env_map, QuadLiveness liveness,
			   Set blockingcalls, HMethod hmethod, 
			   HCode hc, UpdateCodeFactory ucf,
			   ToAsync.BlockingMethods bm, HMethod mroot, Linker linker) {
	    this.liveness=liveness;
	    this.env_map=env_map;
	    this.cont_todo=cont_todo;
	    this.async_todo=async_todo;
	    this.old2new=old2new;
	    this.cont_map=cont_map;
	    this.blockingcalls=blockingcalls;
	    this.hmethod=hmethod;
	    this.header=false;
	    this.ucf=ucf;
	    this.linker=linker;
	    this.clonevisit=new CloningVisitor(blockingcalls, cont_todo,
					       cont_map, env_map, liveness,
					       async_todo, old2new,
					       hc,ucf,bm,mroot, linker);
	}

	public void visit(Quad q) {
	    System.out.println("ERROR: "+q+" in ContVisitory!!!");
	}

	public void visit(HEADER q) {
	    //Handles building HCode's for the HEADER quad...

	    //need to build continuation for this Header...
	    //nmh is the HMethod we wish to attach this HCode to
	    HMethod nhm=(HMethod)old2new.get(hmethod);
	    //mark header flag
	    System.out.println("ContVisiting"+ q);
	    header=true;
	    clonevisit.reset(nhm,q.getFactory().tempFactory(),false);
	    System.out.println("Reset clone visitor");
	    copy(q,-1);
	    System.out.println("Finished copying");
	    ucf.update(nhm, clonevisit.getCode());
	}

	public void visit(CALL q) {
	    //builds continuation for CALLs
	    //nmh is the HMethod we wish to attach this HCode to
	    System.out.println("ContVisiting"+ q);
	    HClass hclass=(HClass) cont_map.get(q);
	    HClass throwable=linker.forName("java.lang.Throwable");
	    HMethod resume=(q.method().getReturnType()==HClass.Void)?
		hclass.getDeclaredMethod("resume",new HClass[0])
		:hclass.getDeclaredMethod("resume",
					 new HClass[] {q.method().getReturnType()});
	    HMethod exception=
		hclass.getDeclaredMethod("exception",
					 new HClass[] {throwable});

	    //Resume method
	    //have to reset state in visitor class
	    clonevisit.reset(resume,q.getFactory().tempFactory(), true);
	    copy(q,0);
	    System.out.println("Finished resume copying");
	    //addEdges should add appropriate headers
	    ucf.update(resume, clonevisit.getCode());
	    //Exception method
	    clonevisit.reset(exception, q.getFactory().tempFactory(), true);
	    copy(q,1);
	    System.out.println("Finished exception copying");
	    //addEdges should add appropriate headers
	    ucf.update(exception, clonevisit.getCode());
	}

	//copies the necessary quads from the original HCode
	public void copy(Quad q, int resumeexception) {
	    //-1 normal
	    //0 resume
	    //1 exception
	    WorkSet todo=new WorkSet();
	    WorkSet done=new WorkSet();
	    WorkSet dontfollow=new WorkSet();

	    if (resumeexception==-1) {
		//Don't want to visit the footer
		done.add(q.next(0));
		todo.push(q);
	    }
	    else
		todo.push(q.next(resumeexception));
	    while (!todo.isEmpty()) {
		Quad nq=(Quad) todo.pop();
		done.add(nq);
		nq.accept(clonevisit);
		if (clonevisit.follow()) {
		    Quad[] next=nq.next();
		    for (int i=0; i<next.length;i++)
			if (!done.contains(next[i]))
			    todo.push(next[i]);
		} else
		    dontfollow.add(nq);
	    }
	    System.out.println("Start addEdges");
	    clonevisit.addEdges(q,resumeexception,dontfollow);
	    System.out.println("Finished addEdges");
	}

	public boolean isHeader() {
	    return header;
	}
    }

    static class CloningVisitor extends QuadVisitor {
	boolean followchildren;
	Set blockingcalls;
	Set cont_todo;
	Map cont_map;
	Set async_todo;
	Map old2new;
	ContCode hcode;
	CloningTempMap ctmap;
	HashMap quadmap;
	boolean isCont;
	Map env_map;
	QuadLiveness liveness;
	UpdateCodeFactory ucf;
	HCode hc;
	WorkSet linkFooters;
	Temp tthis;
	ToAsync.BlockingMethods bm;
	Set phiset;
	HMethod mroot;
	Linker linker;

	public CloningVisitor(Set blockingcalls, Set cont_todo,
			      Map cont_map, Map env_map, 
			      QuadLiveness liveness, Set async_todo,
			      Map old2new, 
			      HCode hc, UpdateCodeFactory ucf,
			      ToAsync.BlockingMethods bm, HMethod mroot, 
			      Linker linker) {
	    this.liveness=liveness;
	    this.blockingcalls=blockingcalls;
	    this.cont_todo=cont_todo;
	    this.cont_map=cont_map;
	    this.async_todo=async_todo;
	    this.old2new=old2new;
	    this.env_map=env_map;
	    this.ucf=ucf;
	    this.hc=hc;
	    this.bm=bm;
	    this.mroot=mroot;
	    this.linker=linker;
	}

	public void reset(HMethod nhm, TempFactory otf, boolean isCont) {
	    followchildren=true;
	    hcode=new ContCode(nhm);
	    ctmap=new CloningTempMap(otf,hcode.getFactory().tempFactory());
	    quadmap=new HashMap();
	    this.isCont=isCont;
	    this.linkFooters=new WorkSet();
	    if (isCont)
		tthis=new Temp(hcode.getFactory().tempFactory());
	    else
		tthis=null;
	    phiset=new WorkSet();
	}

	public HCode getCode() {
	    return hcode;
	}

	public boolean follow() {
	    return followchildren;
	}

	HClass getEnv(Quad q) {
	    if (env_map.containsKey(q))
		return (HClass) env_map.get(q);
	    HClass nhclass=(new EnvBuilder(ucf, hc, 
					  q, liveness.getLiveInandOutArray(q),linker)).makeEnv();
	    env_map.put(q, nhclass);
	    return nhclass;
	}

	//Adds edges between quads and environment loading code
	public void addEdges(Quad q, int resumeexception, Set dontfollow) {
	    QuadFactory qf=hcode.getFactory();
	    TempFactory tf=qf.tempFactory();
	    FOOTER footer=new FOOTER(qf, q, linkFooters.size()+1);
	    Iterator fiterator=linkFooters.iterator();
	    int count=1;
	    while(fiterator.hasNext())
		Quad.addEdge((Quad)fiterator.next(), 0, footer, count++);
	    if (resumeexception!=-1) {
		//Doing addedges for CALL continuation
		//Need to build headers here....[continuation]
		//Need to load environment object and result codes
		WorkSet done=new WorkSet();
		WorkSet todo=new WorkSet();
		todo.push(q.next(resumeexception));
		while (!todo.isEmpty()) {
		    Quad nq=(Quad)todo.pop();
		    done.add(nq);
		    Quad cnq=(Quad)quadmap.get(nq);
		    Quad[] next=nq.next();
		    if (!dontfollow.contains(nq))
			for (int i=0;i<next.length;i++) {
			    //this quad was cloned
			    if (!done.contains(next[i]))
				todo.push(next[i]);
			    Quad cn=(Quad)quadmap.get(next[i]);
			    //add the edge in
			    Quad.addEdge(cnq,i,cn,nq.nextEdge(i).which_pred());
			}
		}
		//Need to build header
		Quad first=(Quad) quadmap.get(q.next(resumeexception));
		Temp oldrtemp=
		    (resumeexception == 0) ?
		    ((CALL)q).retval():((CALL)q).retex();
		Temp[] params=null;
		if (oldrtemp==null)
		    params=new Temp[1];
		else {
		    params=new Temp[2];
		    params[1]=ctmap.tempMap(oldrtemp);
		}
		params[0]=tthis;
		HEADER header=new HEADER(qf,first);
		Quad.addEdge(header,0,footer,0);
		METHOD method=new METHOD(qf,first,params,1);
		Quad.addEdge(header,1,method,0);
		Temp tenv=new Temp(tf);
		GET get=
		    new GET(qf,first,tenv,
			    hcode.getMethod().getDeclaringClass().getField("e"),
			    method.params(0));
		Quad.addEdge(method,0,get,0);
		// assign each field in the Environment to the appropriate Temp
		// except for the assignment we want to suppress
		Temp suppress =
		    (resumeexception == 0) ?
		    ((CALL)q).retval() : ((CALL)q).retex();
      		Temp[] liveout=liveness.getLiveInandOutArray(q);

		Quad prev = get;
		HField[] envfields=getEnv(q).getDeclaredFields();
		for(int i=0; i<liveout.length; i++) {
		    if (suppress == null || !suppress.equals(liveout[i])) {
			GET ng = new GET(qf, first, ctmap.tempMap(liveout[i]),
					 envfields[i], tenv);
			Quad.addEdge(prev, 0, ng, 0);
			prev = ng;
		    }
		}

		// typecast the argument if necessary
		if (!((CALL)q).method().getReturnType().isPrimitive()) {
		    TYPECAST tc = 
			new TYPECAST(qf, first, 
				     ((CALL)q).retval(),
				     ((CALL)q).method().getReturnType());
		    Quad.addEdge(prev, 0, tc, 0);
		    prev = tc;
		}
		Quad.addEdge(prev,0,
			     (Quad)quadmap.get(q.next(resumeexception)),
			     q.nextEdge(resumeexception).which_pred());
		fixphis();
		hcode.quadSet(header);
	    } else {
		//Doing addEdges for HEADER
		WorkSet done=new WorkSet();
		WorkSet todo=new WorkSet();
		todo.push(q.next(1));
		Quad.addEdge((Quad)quadmap.get(q),1,
			     (Quad)quadmap.get(q.next(1)),q.nextEdge(1).which_pred());
		while (!todo.isEmpty()) {
		    Quad nq=(Quad)todo.pop();
		    done.add(nq);
		    Quad cnq=(Quad)quadmap.get(nq);
		    Quad[] next=nq.next();
		    if (!dontfollow.contains(nq))
			for (int i=0;i<next.length;i++) {
			    //this quad was cloned
			    if (!done.contains(next[i]))
				todo.push(next[i]);
			    Quad cn=(Quad)quadmap.get(next[i]);
				//add the edge in
			    Quad.addEdge(cnq,i,cn,nq.nextEdge(i).which_pred());
			}
		}
		//add in new footer
		Quad.addEdge((Quad) quadmap.get(q),0, footer,0);
		fixphis();
		hcode.quadSet((Quad)quadmap.get(q));
	    }
	}

	void fixphis() {
	    Iterator phiit=phiset.iterator();
	    while(phiit.hasNext()) {
		PHI phi=(PHI) phiit.next();
		for (int i=phi.arity()-1;i>=0;i--)
		    if (phi.prevEdge(i)==null) {
			phi=phi.shrink(i);
		    }
	    }
	}

	boolean needsCheck() {
	    HMethod m=hc.getMethod();
	    HClass hcl=m.getDeclaringClass();

	    if (linker.forName("java.lang.Runnable").
		isSuperinterfaceOf(hcl)&&
		hcl.getMethod("run",new HClass[0]).equals(m))
		return true;
	    if (m.equals(mroot))
		return true;
	    return false;
	}

	public void visit(RETURN q) {
	    TempFactory tf=hcode.getFactory().tempFactory();
	    if (isCont) {
		HClass hclass=hcode.getMethod().getDeclaringClass();
		HField hfield=hclass.getField("next");
		HMethod resume=null;
		if (hc.getMethod().getReturnType()!=HClass.Void)
		    resume=
			hfield.getType().getMethod("resume",
						   new HClass[] {hc.getMethod().getReturnType()});
		else
		    resume=hfield.getType().getMethod("resume",
						      new HClass[0]);
		Temp tnext=new Temp(tf);
		GET get=new GET(hcode.getFactory(),q,
				tnext, hfield, tthis);
		Quad nq=get;
		if (needsCheck()) {
		    Temp tnull=new Temp(tf);
		    CONST qconst=
			new CONST(hcode.getFactory(), q, tnull, null, HClass.Void);
		    Quad.addEdge(nq,0,qconst,0);
		    Temp tcomp=new Temp(tf);
		    OPER qoper=
			new OPER(hcode.getFactory(),q,Qop.ACMPEQ,tcomp, 
				 new Temp[] {tnext, tnull});
		    Quad.addEdge(qconst, 0, qoper, 0);
		    CJMP cjmp=
			new CJMP(hcode.getFactory(), q, tcomp, new Temp[0]);
		    Quad.addEdge(qoper, 0, cjmp, 0);
		    nq=cjmp;
		}
		Temp nretval=null;
		CALL call=null;
		Temp retex=new Temp(tf);
		if (q.retval()!=null) {
		    nretval=ctmap.tempMap(q.retval());
		    //***** Tailcall eventually
		    //need to do get next first
		    call=new CALL(hcode.getFactory(), q, resume,
				  new Temp[] {tnext,nretval},null,retex,
				  true,false,new Temp[0]);
		} else {
		    //***** Tailcall eventually
		    call=new CALL(hcode.getFactory(), q, resume,
				  new Temp[] {tnext}, null, retex,
				  true, false, new Temp[0]);
		}
		Quad.addEdge(nq,0,call,0);
		THROW qthrow=new THROW(hcode.getFactory(),q,retex);
		Quad.addEdge(call,1,qthrow,0);
		RETURN qreturn=new RETURN(hcode.getFactory(),q,null);
		if (needsCheck()) {
		    PHI phi=new PHI(hcode.getFactory(), q, new Temp[0], 2);
		    Quad.addEdge(nq, 1, phi, 0);
		    Quad.addEdge(call,0,phi,1);
		    Quad.addEdge(phi,0,qreturn,0);
		} else
		    Quad.addEdge(call,0,qreturn,0);
		linkFooters.add(qthrow);
		linkFooters.add(qreturn);
		quadmap.put(q, get);
	    }
	    else {
		HClass rettype=hc.getMethod().getReturnType();
		String pref = 
		    ContBuilder.getPrefix(rettype);
		HClass continuation = linker.forName
		    ("harpoon.Analysis.ContBuilder." + pref + "DoneContinuation");
		HClass ret=rettype.isPrimitive()?rettype:linker.forName("java.lang.Object");
		HConstructor constructor=(ret!=HClass.Void)?continuation.getConstructor(new HClass[]{ret}):
		    continuation.getConstructor(new HClass[0]);
		Temp newt=new Temp(tf);
		NEW newq=new NEW(hcode.getFactory(),q,newt, continuation);
		Temp retex=new Temp(tf);
		CALL call;
		if (q.retval()!=null) {
		    //***** Tailcall eventually
		    //need to do get next first
		    Temp nretval=ctmap.tempMap(q.retval());
		    call=new CALL(hcode.getFactory(), q, constructor,
				  new Temp[] {newt,nretval}, null,retex,
				  false,false,new Temp[0]);
		} else {
		    //***** Tailcall eventually
		    call=new CALL(hcode.getFactory(), q, constructor,
				  new Temp[] {newt}, null, retex,
				  false, false, new Temp[0]);
		}
		Quad.addEdge(newq,0,call,0);
		THROW qthrow=new THROW(hcode.getFactory(),q,retex);

		Quad.addEdge(call,1,qthrow,0);
		RETURN qreturn=new RETURN(hcode.getFactory(),q,null);
		Quad.addEdge(call,0,qreturn,0);
		linkFooters.add(qthrow);
		linkFooters.add(qreturn);
		quadmap.put(q, newq);
	    }
	    followchildren=false;
	}
	
	public void visit(THROW q) {
	    TempFactory tf=hcode.getFactory().tempFactory();
	    if (isCont) {
		HClass hclass=hcode.getMethod().getDeclaringClass();
		HField hfield=hclass.getField("next");
		HClass throwable=linker.forName("java.lang.Throwable");
		HMethod resume=
		    hfield.getType().getMethod("exception",
						       new HClass[] {throwable});
		Temp tnext=new Temp(tf);
		GET get=new GET(hcode.getFactory(),q,
				tnext, hfield, tthis);
		Quad nq=get;
		if (needsCheck()) {
		    Temp tnull=new Temp(tf);
		    CONST qconst=
			new CONST(hcode.getFactory(), q, tnull, null, HClass.Void);
		    Quad.addEdge(nq,0,qconst,0);
		    Temp tcomp=new Temp(tf);
		    OPER qoper=
			new OPER(hcode.getFactory(),q,Qop.ACMPEQ,tcomp, 
				 new Temp[] {tnext, tnull});
		    Quad.addEdge(qconst, 0, qoper, 0);
		    CJMP cjmp=
			new CJMP(hcode.getFactory(), q, tcomp, new Temp[0]);
		    Quad.addEdge(qoper, 0, cjmp, 0);
		    nq=cjmp;
		}

		CALL call=null;
		Temp nretval=ctmap.tempMap(q.throwable());
		Temp retex=nretval;
		//reuse the Temp...so we only have to throw one...

		//***** Tailcall eventually
		//need to do get next first
		call=new CALL(hcode.getFactory(), q, resume,
			      new Temp[] {tnext,nretval},null,retex,
			      true,false,new Temp[0]);
		Quad.addEdge(nq,0,call,0);
		THROW qthrow=new THROW(hcode.getFactory(),q,retex);

		if (needsCheck()) {
		    PHI phi=new PHI(hcode.getFactory(), q, new Temp[0], 2);
		    Quad.addEdge(nq, 1, phi, 0);
		    Quad.addEdge(call,1,phi,1);
		    Quad.addEdge(phi,0,qthrow,0);
		} else
		    Quad.addEdge(call,1,qthrow,0);
		
		RETURN qreturn=new RETURN(hcode.getFactory(),q,null);
		Quad.addEdge(call,0,qreturn,0);
		linkFooters.add(qthrow);
		linkFooters.add(qreturn);
		quadmap.put(q, get);
	    } else {
		HClass rettype=hc.getMethod().getReturnType();
		String pref = 
		    ContBuilder.getPrefix(rettype);
		HClass continuation = linker.forName
		    ("harpoon.Analysis.ContBuilder." + pref + "DoneContinuation");
		HClass ret= linker.forName("java.lang.Throwable");
		HConstructor constructor=continuation.getConstructor(new HClass[]{ret});
		Temp newt=new Temp(tf);
		NEW newq=new NEW(hcode.getFactory(),q,newt, continuation);
		//***** Tailcall eventually
		    //need to do get next first
		Temp nretval=ctmap.tempMap(q.throwable());
		Temp retex=new Temp(tf);
		CALL call=new CALL(hcode.getFactory(), q, constructor,
			      new Temp[] {newt,nretval}, null,retex,
			      false,false,new Temp[0]);
		Quad.addEdge(newq,0,call,0);
		THROW qthrow=new THROW(hcode.getFactory(),q,retex);
		Quad.addEdge(call,1,qthrow,0);
		RETURN qreturn=new RETURN(hcode.getFactory(),q,null);
		Quad.addEdge(call,0,qreturn,0);
		linkFooters.add(qthrow);
		linkFooters.add(qreturn);
		quadmap.put(q, newq);
	    }
	    followchildren=false;
	}

	public void visit(Quad q) {
	    followchildren=true;
	    quadmap.put(q, q.clone(hcode.getFactory(), ctmap));
      	}

	public void visit(PHI q) {
	    followchildren=true;
	    Object qc=q.clone(hcode.getFactory(), ctmap);
	    quadmap.put(q, qc);
	    phiset.add(qc);
      	}

	public void visit(CALL q) {
	    TempFactory tf=hcode.getFactory().tempFactory();
	    if (blockingcalls.contains(q.method())) {
		if (!cont_map.containsKey(q)) {
		    cont_todo.add(q);
		    HClass hclass=createContinuation(hc.getMethod(),  q,
						     ucf, linker); 
		    cont_map.put(q,hclass);
		    HMethod hm=q.method();
		    if (!old2new.containsKey(hm)) {
			if (bm.swop(hm)!=null) {
			    //handle actual blocking call swapping
			    old2new.put(hm, bm.swop(hm));
			} else {
			    async_todo.add(ucf.convert(hm));
			    HMethod temp=makeAsync(old2new, q.method(),
						   ucf,linker);
			}
		    }
		}
		//rewrite blocking call
		Temp[] newt=new Temp[q.paramsLength()];
		Temp retex=new Temp(tf);
		Temp retcont=new Temp(tf);
		for(int i=0;i<q.paramsLength();i++)
		    newt[i]=ctmap.tempMap(q.params(i));
		HMethod calleemethod=(HMethod)old2new.get(q.method());
		CALL call=new CALL(hcode.getFactory(),q,
				   calleemethod, newt,
				   retcont,retex, q.isVirtual(), q.isTailCall(),
				   new Temp[0]);
		PHI phi=new PHI(hcode.getFactory(), q, new Temp[0], isCont?5:4);
		Quad.addEdge(call,1,phi,0);
		//build environment
		HClass env=getEnv(q);
		Temp[] liveout = liveness.getLiveInandOutArray(q);
		Temp tenv=new Temp(tf);
		NEW envq=new NEW(hcode.getFactory(), q, tenv, env);
		Quad.addEdge(call,0,envq,0);

		Temp [] params = new Temp[liveout.length+1];
		params[0]=tenv;
		for (int j=0,i=1;j<liveout.length;j++) {
		    params[i++]=ctmap.tempMap(liveout[j]); 
		}
		CALL callenv=new CALL(hcode.getFactory(), q, env.getConstructors()[0],
				      params, null, retex, false, false, new Temp[0]);
		Quad.addEdge(envq,0,callenv,0);
		Quad.addEdge(callenv,1,phi,1);
		
		Temp tcont=new Temp(tf);
		HClass contclass=(HClass)cont_map.get(q);
		NEW newc=new NEW(hcode.getFactory(), q, tcont, contclass);
		Quad.addEdge(callenv,0,newc,0);
		//XXXXXXXXXXXXX
		HClass environment=linker.forName("harpoon.Analysis.EnvBuilder.Environment");
		//constructor call=not virtual
		System.out.println(environment);
		System.out.println(environment.getLinker());
		HConstructor call2const=contclass.getConstructor(new HClass[]{
		    environment});
		//HConstructor call2const=contclass.getConstructors()[0];
		//XXXXXXXXXX
		CALL call2=new CALL(hcode.getFactory(),q,
				    call2const,
				    new Temp[] {tcont, tenv}, null, retex, 
				    false, false, new Temp[0]);
		Quad.addEdge(newc,0,call2,0);
		Quad.addEdge(call2,1,phi,2);
		String pref = 
		    ContBuilder.getPrefix(q.method().getReturnType());
		HMethod setnextmethod=
		    calleemethod.getReturnType().getMethod("setNext",
							  new HClass[] {linker.forName("harpoon.Analysis.ContBuilder."+pref+"ResultContinuation")});
		Util.assert(setnextmethod!=null,"no setNext method found");
		CALL call3=new CALL(hcode.getFactory(), q,
				    setnextmethod, new Temp[] {retcont, tcont},
				    null, retex, true, false, new Temp[0]);
		Quad.addEdge(call2, 0, call3, 0);
		Quad.addEdge(call3, 1, phi, 3);
		if (isCont) {
		    Temp tnext=new Temp(tf);
		    HClass hclass=hcode.getMethod().getDeclaringClass();
		    HField hfield=hclass.getField("next");
		    GET get=new GET(hcode.getFactory(),q,
				    tnext, hfield, tthis);
		    Quad.addEdge(call3,0,get,0);
		    //XXXX TEST
		    String pref2 =
			ContBuilder.getPrefix(hc.getMethod().getReturnType());
		    //Debug
		    System.out.println("-------------");
		    System.out.println(contclass);
		    System.out.println(contclass.getSuperclass());
		    System.out.println(pref2);
		    HMethod [] hmethods=contclass.getMethods();
		    for (int i=0;i<hmethods.length;i++)
			System.out.println(hmethods[i]);
		    HMethod setnextmethod2=
			contclass.getMethod("setNext",
					    new HClass[] {linker.forName("harpoon.Analysis.ContBuilder."+pref2+"ResultContinuation")});
		    Util.assert(setnextmethod2!=null,"no setNext method found");
		    CALL call4=new CALL(hcode.getFactory(), q,
					setnextmethod2, new Temp[] {tcont, tnext},
					null, retex, true, false, new Temp[0]);
		    Quad.addEdge(get,0,call4,0);
		    Quad.addEdge(call4,1,phi,4);
		    RETURN returnq=new RETURN(hcode.getFactory(), q, null);
		    Quad.addEdge(call4,0,returnq,0);
		    linkFooters.add(returnq);
		} else {
		    RETURN returnq=new RETURN(hcode.getFactory(),q,tcont);
		    Quad.addEdge(call3, 0, returnq,0);
		    linkFooters.add(returnq);
		}
		THROW throwq=new THROW(hcode.getFactory(),q,retex);    
		Quad.addEdge(phi,0,throwq,0);
		linkFooters.add(throwq);
		quadmap.put(q,call);
		followchildren=false;
	    } else {
		followchildren=true;
		//need to check if swop necessary
		if (swapTo(q.method())!=null) {
		    //need to swop
		    Temp tstream=ctmap.tempMap(q.params(0));
		    quadmap.put(q,new CALL(hcode.getFactory(), q, swapTo(q.method()),
				       new Temp[]{tstream}, 
				       ctmap.tempMap(q.retval()),
				       ctmap.tempMap(q.retex()), q.isVirtual(),
				       q.isTailCall(), new Temp[0]));
		}
		else
		    quadmap.put(q, q.clone(hcode.getFactory(), ctmap));
	    }
	}

	public HMethod swapTo(HMethod old) {
	    HMethod gis=linker.forName("java.net.Socket").getDeclaredMethod("getInputStream", new HClass[0]);
	    if (gis.equals(old))
		return linker.forName("java.net.Socket").getDeclaredMethod
		    ("getAsyncInputStream", new HClass[0]);

	    HClass HCthrd=linker.forName("java.lang.Thread");
	    if (HCthrd.equals(old.getDeclaringClass())&&
		(old.equals(old.getDeclaringClass().getMethod("start",
							      new HClass[0]))))
		return old.getDeclaringClass().getMethod("start_Async",
							 new HClass[0]);

	    return null;
	}
    }


    static class ChangingVisitor extends QuadVisitor {
	HClass oldn, newn;
	WorkSet done;
	ChangingVisitor(HClass oldn, HClass newn) {
	    this.oldn=oldn;
	    this.newn=newn;
	}

	public void visit(Quad q) {
	    done.add(q);
	}

	public void visit(GET q) {
	    if (q.field().getDeclaringClass()==oldn) {
		HField hfield=
		    newn.getDeclaredField(q.field().getName());
		GET get=new GET(q.getFactory(),q,
				q.dst(),hfield,q.objectref());
		done.add(get);
		Quad.addEdge(q.prev(0),q.prevEdge(0).which_succ(),
			     get,0);
		Quad.addEdge(get,0,
			     q.next(0),q.nextEdge(0).which_pred());
	    }
	}

	public void visit(SET q) {
	    if (q.field().getDeclaringClass()==oldn) {
		HField hfield=
		    newn.getDeclaredField(q.field().getName());
		SET set=new SET(q.getFactory(),q,
				hfield,q.objectref(),q.src());
		done.add(set);
		Quad.addEdge(q.prev(0),q.prevEdge(0).which_succ(),
			     set,0);
		Quad.addEdge(set,0,
			     q.next(0),q.nextEdge(0).which_pred());
	    }
	}

	public void reName(Quad q) {
	    done=new WorkSet();
	    WorkSet todo=new WorkSet();
	    todo.add(q);
	    while(!todo.isEmpty()) {
		Quad qq=(Quad)todo.pop();
		Quad[] next=qq.next();
		for(int i=0;i<next.length;i++)
		    if (!done.contains(next[i]))
			todo.add(next[i]);
		qq.accept(this);
	    }
	}
    }



    // create asynchronous version of HMethod to replace blocking version
    // does not create HCode that goes w/ it...
    public static HMethod makeAsync(Map old2new, HMethod original,
			      UpdateCodeFactory ucf, Linker linker)
    {
	//	final HMethod original = blocking.method();
	HClass originalClass = original.getDeclaringClass();
	HClassMutator originalMutator=originalClass.getMutator();
	Util.assert(originalMutator!=null);

//  	// create a new HClassSyn that replaces the original HClass
//  	if (classmap.containsKey(originalClass))
//  	    replacementClass=(HClassSyn) classmap.get(originalClass);
//  	else {
//  	    replacementClass=new HClassSyn(originalClass, true);
//  	    // clone HMethods from original class
//  	    HMethod[] toClone = originalClass.getDeclaredMethods();
//  	    for (int i = 0; i < toClone.length; i++) {
//  		HMethod clone = replacementClass.getDeclaredMethod
//  		    (toClone[i].getName(), toClone[i].getParameterTypes());
//  		ucf.update(clone, 
//  			   ((QuadNoSSA)ucf.convert(toClone[i])).clone(clone));
//  	    }
//  	    classmap.put(originalClass, replacementClass);
//  	}
	    
	// use the return type of the original HMethod to get the String 
	// prefix for the type of Continuation we want as the new return type
	final String pref = ContBuilder.getPrefix(original.getReturnType());

	// get the return type for the replacement HMethod
	final HClass newReturnType = linker.forName
	    ("harpoon.Analysis.ContBuilder." + pref + "Continuation");

	// find a unique name for the replacement HMethod
	String methodNamePrefix = original.getName() + "_Async";
	String newMethodName = methodNamePrefix;
	if (linker.forName("java.lang.Runnable").
	    isSuperinterfaceOf(originalClass)&&
	    originalClass.getMethod("run",new HClass[0]).equals(original)) {
	    try {
		newMethodName = methodNamePrefix;
		originalClass.getMethod(newMethodName, 
					   original.getParameterTypes());
		throw new RuntimeException("Name collision with run_Async method");
	    } catch (NoSuchMethodError e) {
	    }
	} else {
	    int i = 0;
	    while(true) {
		try {
		    newMethodName = methodNamePrefix + i++; 
		    originalClass.getMethod(newMethodName, 
					       original.getParameterTypes());
		} catch (NoSuchMethodError e) {
		    break;
		}
	    }
	}
	// create replacement method
	HMethod replacement=originalMutator.addDeclaredMethod(newMethodName, 
					  original.getParameterTypes(),
					  newReturnType);
	HMethodMutator rmutator=replacement.getMutator();

	rmutator.setExceptionTypes(original.getExceptionTypes());
	rmutator.setModifiers(original.getModifiers());
	rmutator.setParameterNames(original.getParameterNames());
	rmutator.setSynthetic(original.isSynthetic());
	old2new.put(original, replacement);
	return replacement;
    }

    // creates the HClass and constructor for the continuation
    private static HClass createContinuation(HMethod blocking, CALL callsite,
				      UpdateCodeFactory ucf, Linker linker) 
	throws NoClassDefFoundError
    {
	final HClass template = 
	    linker.forName("harpoon.Analysis.ContBuilder.ContTemplate");

	String cname=UniqueName.uniqueClassName("harpoon.Analysis.ContBuilder.ContTemplate"
						,linker);
	HClass continuationClass = linker.createMutableClass(cname,template);
	Util.assert(template.getLinker()==linker &&
		    continuationClass.getLinker()==linker);
	HClassMutator contMutator=continuationClass.getMutator();
	    //new HClassSyn(template);
	final int numConstructors = continuationClass.getConstructors().length;
	Util.assert(numConstructors == 1,
		    "Found " + numConstructors + " constructors in " +
		    "ContTemplate. Expected one");

	// use the return type of the blocking HMethod to get the String 
	// prefix for the superclass for the continuation we want to create
	final String superPref = 
	    ContBuilder.getPrefix(blocking.getReturnType());

	// get the superclass for the continuation
	final HClass superclass = linker.forName
	    ("harpoon.Analysis.ContBuilder." + superPref + "Continuation");

	contMutator.setSuperclass(superclass);

	// we want the return type of the blocking call
	// this gives us the interface that our continuation should implement
	final String interPref = 
	    ContBuilder.getPrefix(callsite.method().getReturnType());

	// get the interface that the continuation needs to implement
	final HClass inter = linker.forName("harpoon.Analysis.ContBuilder." + 
					    interPref + "ResultContinuation");
	
	final HClass environment = linker.forName
	    ("harpoon.Analysis.EnvBuilder.Environment");
	System.out.println(continuationClass+"    "+
			   continuationClass.getLinker()
			   +"   "+environment.getLinker());
	// clone template's constructor HCode
//  	HConstructor hc = null;
//  	HConstructor nhc = null;
//  	try {
//  	    hc = template.getConstructor(new HClass[] {environment});
//  	    nhc = continuationClass.getConstructor(new HClass[] {environment});
//  	    HCode hchc = ((Code)ucf.convert(hc)).clone(nhc);
//  	    ucf.update(nhc, hchc);
//  	} catch (NoSuchMethodError e) {
//  	    System.err.println("Missing constructor for environment template");
//  	}

	HMethod hmethods[]=template.getDeclaredMethods();
	for(int i=0;i<hmethods.length;i++) {
	    try {
		HMethod nhm=continuationClass.getDeclaredMethod(hmethods[i].getName(),
								hmethods[i].getDescriptor());
		HCode hchc = ((Code)ucf.convert(hmethods[i])).clone(nhm);
		(new ChangingVisitor(template,continuationClass))
		    .reName((Quad)hchc.getRootElement());
		ucf.update(nhm, hchc);
	    } catch (NoSuchMethodError e) {
		System.err.println(e);
	    }
	}

	HMethod hm = continuationClass.getDeclaredMethod("resume", new HClass[0]);
	HMethodMutator hmMutator=hm.getMutator();

	// get the return value of the blocking call
	// this is the parameter of the resume method, if any
	HClass rettype = callsite.method().getReturnType();
	Temp retval = callsite.retval();
	boolean hasParameter = false;
	if (retval != null) {
	    hasParameter = true;
	    String[] parameterNames = new String[1];
	    parameterNames[0] = retval.name();
	    HClass[] parameterTypes = new HClass[1];
	    if (rettype.isPrimitive())
		parameterTypes[0] = rettype;
	    else {
		parameterTypes[0] = linker.forName("java.lang.Object");
	    }
	    hmMutator.setParameterNames(parameterNames);
	    hmMutator.setParameterTypes(parameterTypes);
	}
	return continuationClass;
    }
}


