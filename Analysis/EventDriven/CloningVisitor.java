// CloningVisitor.java, created Mon Feb  7 17:04:30 2000 by bdemsky
// Copyright (C) 2000 Brian Demsky <bdemsky@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.EventDriven;


import harpoon.Analysis.ContBuilder.ContBuilder;
import harpoon.Analysis.EnvBuilder.EnvBuilder;
import harpoon.Analysis.Quads.QuadLiveness;
import harpoon.Analysis.ClassHierarchy;
import harpoon.Analysis.Maps.TypeMap;
import harpoon.Analysis.AllCallers;
import harpoon.ClassFile.CachingCodeFactory;
import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HClassMutator;
import harpoon.ClassFile.HMethod;
import harpoon.ClassFile.HMethodMutator;
import harpoon.ClassFile.HCode;
import harpoon.ClassFile.HConstructor;
import harpoon.ClassFile.HField;
import harpoon.ClassFile.Loader;
import harpoon.ClassFile.Linker;
import harpoon.ClassFile.Relinker;
import harpoon.ClassFile.UniqueName;

import harpoon.IR.Quads.CALL;
import harpoon.IR.Quads.CJMP;
import harpoon.IR.Quads.CONST;
import harpoon.IR.Quads.Code;
import harpoon.IR.Quads.Edge;
import harpoon.IR.Quads.FOOTER;
import harpoon.IR.Quads.GET;
import harpoon.IR.Quads.INSTANCEOF;
import harpoon.IR.Quads.NOP;
import harpoon.IR.Quads.SET;
import harpoon.IR.Quads.HEADER;
import harpoon.IR.Quads.METHOD;
import harpoon.IR.Quads.NEW;
import harpoon.IR.Quads.ANEW;
import harpoon.IR.Quads.OPER;
import harpoon.IR.Quads.Qop;
import harpoon.IR.Quads.PHI;
import harpoon.IR.Quads.Quad;
import harpoon.IR.Quads.QuadFactory;
import harpoon.IR.Quads.QuadSSI;
import harpoon.IR.Quads.RETURN;
import harpoon.IR.Quads.THROW;
import harpoon.IR.Quads.TYPECAST;
import harpoon.IR.Quads.QuadVisitor;
import harpoon.Temp.Temp;
import harpoon.Temp.TempFactory;
import harpoon.Temp.CloningTempMap;
import harpoon.Temp.TempMap;
import harpoon.Util.Util;
import harpoon.Util.Collections.WorkSet;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Arrays;
import java.util.Vector;
import java.util.Collections;
import java.lang.reflect.Modifier;

import harpoon.Analysis.AllocationInformationMap;
import harpoon.Analysis.Maps.AllocationInformation;

/**
 * <code>CloningVisitor</code>
 * 
 * @author  Brian Demsky <bdemsky@mit.edu>
 * @version $Id: CloningVisitor.java,v 1.3 2002-02-26 22:40:06 cananian Exp $
 */
public class CloningVisitor extends QuadVisitor {
    boolean isCont, followchildren, methodstatus;
    CachingCodeFactory ucf;
    ClassHierarchy ch;
    CloningTempMap ctmap;
    harpoon.IR.Quads.Code hcode;
    HashMap quadmap;
    HCode hc;
    HMethod mroot;
    Linker linker;
    Map cont_map, old2new, env_map;	
    Set blockingcalls, cont_todo, async_todo, phiset;
    Set addedCall, other, done_other;
    Temp tthis;
    BMethod bm;
    QuadLiveness liveness;
    WorkSet linkFooters;
    TypeMap typemap;
    QuadFactory qf;
    boolean stillokay;
    boolean optimistic;
    boolean recycle;
    Set blocking;
    CALL origCall;
    Set cclasses;
    AllocationInformation oldai;
    AllocationInformationMap newai;

    public CloningVisitor(Set blockingcalls, Set cont_todo,
			  Map cont_map, Map env_map, 
			  QuadLiveness liveness, Set async_todo,
			  Map old2new, 
			  HCode hc, CachingCodeFactory ucf,
			  AllCallers.MethodSet bm, HMethod mroot, 
			  Linker linker, ClassHierarchy ch,
			  Set other, Set done_other, 
			  boolean methodstatus, TypeMap typemap,
			  boolean optimistic, boolean recycle, Set cclasses) {
	this.liveness=liveness;
	this.blockingcalls=blockingcalls;
	this.cont_todo=cont_todo;
	this.cont_map=cont_map;
	this.async_todo=async_todo;
	this.old2new=old2new;
	this.env_map=env_map;
	this.ucf=ucf;
	this.hc=hc;
	this.bm=(BMethod)bm;
	this.mroot=mroot;
	this.linker=linker;
	this.ch=ch;
	this.other=other;
	this.done_other=done_other;
	this.methodstatus=methodstatus;
	this.typemap=typemap;
	this.optimistic=optimistic;
	this.recycle=recycle;
	this.cclasses=cclasses;
	this.oldai=((QuadSSI)hc).getAllocationInformation();
    }

    public void reset(HMethod nhm, TempFactory otf, boolean isCont, CALL origCall) {
	followchildren=true;
	hcode=new ContCodeSSI(nhm);
	if (oldai!=null) {
	    newai=new AllocationInformationMap();
	    ((QuadSSI)hcode).setAllocationInformation(newai);
	}
	cclasses.add(nhm.getDeclaringClass());
	qf=((ContCodeSSI)hcode).getFactory();
	stillokay=methodstatus;
	ctmap=new CloningTempMap(otf,qf.tempFactory());
	quadmap=new HashMap();
	this.isCont=isCont;
	linkFooters=new WorkSet();
	if (isCont)
	    tthis=new Temp(qf.tempFactory());
	else
	    tthis=null;
	phiset=new WorkSet();
	addedCall=new WorkSet();
	if (optimistic)
	    blocking=new WorkSet();
	else
	    blocking=null;
	this.origCall=origCall;
    }
    
    public HCode getCode() {
	return hcode;
    }

    public boolean follow() {
	return followchildren;
    }
    
    HClass getEnv(CALL q) {
	if (env_map.containsKey(q))
	    return (HClass) env_map.get(q);
	HClass nhclass=(new EnvBuilder(ucf, hc, 
				       q, getEnvTemps(q),linker,
				       typemap,recycle)).makeEnv();
	//force outputting of this class
	cclasses.add(nhclass);
	env_map.put(q, nhclass);
	return nhclass;
    }

    
    //Adds edges between quads and environment loading code
    public void addEdges(Quad q, int resumeexception, Set dontfollow) {
	HEADER header;
	if (resumeexception!=-1) {
	    //Doing addedges for CALL continuation
	    //Need to build headers here....[continuation]
	    //Need to load environment object and result codes
	    
	    addEdges(q.next(resumeexception),dontfollow);
	    header=buildEnvironmentExtracter((CALL)q, resumeexception);
	    
	    fixphis();
	    ((ContCodeSSI)hcode).quadSet(header);

	} else {
	    //Doing addEdges for HEADER
	    Quad.addEdge((Quad)quadmap.get(q),1,
			 (Quad)quadmap.get(q.next(1)),q.nextEdge(1).which_pred());
	    addEdges(q.next(1),dontfollow);


	    header=(HEADER) quadmap.get(q);
	    fixphis();
	   
	    ((ContCodeSSI)hcode).quadSet(header);
	   
	}
	FOOTER footer=new FOOTER(qf, q, linkFooters.size()+1);
	Quad.addEdge(header,0,footer,0);
	Iterator fiterator=linkFooters.iterator();
	int count=1;
	while(fiterator.hasNext())
	    Quad.addEdge((Quad)fiterator.next(), 0, footer, count++);
    }

    public void visit(RETURN q) {
	if (methodstatus) {
	    //We are just doing swaps on this method, so just clone
	    Object nq=q.clone(qf, ctmap);
	    quadmap.put(q, nq);
	    linkFooters.add(nq);
	} else if (isCont)
	    buildReturnContinuation(q, q.retval(), true);
	else 
	    buildDoneContinuation(q, q.retval(),true);
	followchildren=false;
    }
	
    public void visit(THROW q) {
	TempFactory tf=qf.tempFactory();
	if (methodstatus) {
	    //We are just doing swaps on this method, so just clone
	    Object nq=q.clone(qf, ctmap);
	    quadmap.put(q, nq);
	    linkFooters.add(nq);
	} else if (isCont)
	    buildReturnContinuation(q, q.throwable(), false);
	else if (optimistic) {
	    Object nq=q.clone(qf,ctmap);
	    quadmap.put(q,nq);
	    linkFooters.add(nq);
 	} else
	    buildDoneContinuation(q, q.throwable(),false);
	followchildren=false;
    }
    
    public void visit(Quad q) {
	followchildren=true;
	quadmap.put(q, q.clone(qf, ctmap));
    }

    public void visit(NEW q) {
	//DONE AIMAP[if !methodstatus then change stack-->Thread Heap] 
	followchildren=true;
	Quad qc=(Quad)q.clone(qf,ctmap);
	quadmap.put(q, qc);
	if (newai!=null)
	    if (methodstatus) {
		newai.transfer(qc,q,ctmap, oldai);
	    } else {
		AllocationInformation.AllocationProperties aiprop=oldai.query(q);
		newai.associate(qc,new AllocationInformationMap.AllocationPropertiesImpl(aiprop.hasInteriorPointers(),
											 false,
											 aiprop.canBeThreadAllocated()||aiprop.canBeStackAllocated(),
											 aiprop.makeHeap(),
											 false, // DEFAULT
											 (aiprop.allocationHeap()!=null)?ctmap.tempMap(aiprop.allocationHeap()) : null,
											 ((NEW)qc).hclass()));
	    }
    }

    public void visit(ANEW q) {
	//DONE AIMAP[if !methodstatus then change stack-->Thread Heap] 
	followchildren=true;
	Quad qc=(Quad)q.clone(qf,ctmap);
	quadmap.put(q, qc);
	if (newai!=null)
	    if (methodstatus) {
		newai.transfer(qc,q,ctmap, oldai);
	    } else {
		AllocationInformation.AllocationProperties aiprop=oldai.query(q);
		newai.associate(qc,new AllocationInformationMap.AllocationPropertiesImpl(aiprop.hasInteriorPointers(),
											 false,
											 aiprop.canBeThreadAllocated()||aiprop.canBeStackAllocated(),
											 aiprop.makeHeap(),
											 false, // DEFAULT
											 (aiprop.allocationHeap()!=null)?ctmap.tempMap(aiprop.allocationHeap()) : null,
											 ((ANEW)qc).hclass()));
	    }
    }
    
    public void visit(PHI q) {
	followchildren=true;
	Object qc=q.clone(qf, ctmap);
	quadmap.put(q, qc);
	phiset.add(qc);
    }
    
    public void visit(CALL q) {
	checkdoneothers(q);
	if ((methodstatus==false)&&
	    (!q.method().getName().equals("<init>"))&&
	    isBlocking(q)) {
	    if (!cont_map.containsKey(q)) {
		//Add this CALL to list of calls to build continuations for
		cont_todo.add(q);
		//Build Class for the continuation
		HClass hclass=AsyncCode.createContinuation(hc.getMethod(),  q,
						 ucf, linker,getEnv(q)); 
		//Add mapping of call->class
		cont_map.put(q,hclass);
		//Schedule blocking method for transformation
		scheduleMethods(q.method());
	    }
	    if (optimistic)
		blocking.add(q);
	    handleBlocking(q);
	} else
	    handleNonBlocking(q);
    }
    
    private void addEdges(Quad q, Set dontfollow) {
	WorkSet done=new WorkSet();
	WorkSet todo=new WorkSet();
	todo.push(q);
	while (!todo.isEmpty()) {
	    Quad nq=(Quad)todo.pop();
	    done.add(nq);
	    Quad cnq=(Quad)quadmap.get(nq);
	    Quad[] next=nq.next();
	    if (addedCall.contains(cnq)) {
		//Handling call with additional call added to it
		//0 edge is different...we want to link cnq.next(0) instead of cnqq
		if (!done.contains(next[0]))
		    todo.push(next[0]);
		Quad cn=(Quad)quadmap.get(next[0]);
		//add the edge in [phi node]
		Quad cnq2=cnq.next(0).next(0);
		Quad.addEdge(cnq2,0,cn,nq.nextEdge(0).which_pred());
		
		//1 edge is as normal
		if (!done.contains(next[1]))
		    todo.push(next[1]);
		cn=(Quad)quadmap.get(next[1]);
		//add the exception edge in
		Quad.addEdge(cnq,1,cn,nq.nextEdge(1).which_pred());
	    } else if (!dontfollow.contains(nq))
		if (optimistic&&blocking.contains(nq)) {
		    //do 0 edge
		    if (!done.contains(next[0]))
			todo.push(next[0]);
		    Quad cn0=(Quad)quadmap.get(next[0]);
		    if ((!((CALL)nq).method().getReturnType().isPrimitive())&&
			(!((CALL)nq).method().getReturnType().equals(linker.forName("java.lang.Object")))) 
			Quad.addEdge(cnq.next(0).next(0).next(1).next(0).next(0).next(0).next(1),0,cn0,nq.nextEdge(0).which_pred());
		    else
			Quad.addEdge(cnq.next(0).next(0).next(1),0,cn0,nq.nextEdge(0).which_pred());
		    //do 1 edge
		    if (!done.contains(next[1]))
			todo.push(next[1]);
		    Quad cn=(Quad)quadmap.get(next[1]);
		    Quad.addEdge(cnq,1,cn,nq.nextEdge(1).which_pred());
		} else
		    for (int i=0;i<next.length;i++) {
			//this quad was cloned
			if (!done.contains(next[i]))
			    todo.push(next[i]);
			Quad cn=(Quad)quadmap.get(next[i]);
			//add the edge in
			Quad.addEdge(cnq,i,cn,nq.nextEdge(i).which_pred());
		    }
	}
    }

    private HEADER buildEnvironmentExtracter(CALL q, int resumeexception){
	TempFactory tf=qf.tempFactory();
	//-----------------------------------------------------------------
	//Build HEADER
	
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
	
	//-----------------------------------------------------------------
	//Build METHOD quad
	//
		    
	METHOD method=new METHOD(qf,first,params,1);
	Quad.addEdge(header,1,method,0);
	Temp tenv=new Temp(tf);
	    
	//-----------------------------------------------------------------
	//Build GET quad for environment
	//
	    
	GET get=
	    new GET(qf,first,tenv,
		    hcode.getMethod().getDeclaringClass().getField("e"),
		    method.params(0));
	Quad.addEdge(method,0,get,0);
	    
	//-----------------------------------------------------------------
	//GET Temp's out of environment
	//
	// assign each field in the Environment to the appropriate Temp
	// except for the assignment we want to suppress
	
	Temp suppress =
	    (resumeexception == 0) ?
	    ((CALL)q).retval() : ((CALL)q).retex();
	Quad qnext=q.next(resumeexception);
	Set livein;
	if (qnext.prevLength()!=1) {
	    PHI phi=(PHI)qnext;
	    livein=liveness.getLiveOut(qnext);
	    int edge=q.nextEdge(resumeexception).which_pred();
	    for (int j=0;j<phi.numPhis();j++) {
		livein.remove(phi.dst(j));
	    }
	    for (int j=0;j<phi.numPhis();j++) {
		livein.add(phi.src(j,edge));
	    }
	} else
	    livein=liveness.getLiveIn(qnext);
	Temp[] liveenv=getEnvTemps(q);
	Quad prev = get;
	HField[] envfields=getEnv(q).getDeclaredFields();

	//Build string of CONST's and GET's.
	    
	for (Iterator ii=livein.iterator();ii.hasNext();) {
	    Temp t=(Temp)ii.next();
	    if (suppress == null || !suppress.equals(t)) {
		Quad ng;
		Temp ts=t;
		if (typemap.typeMap(q, t)!=HClass.Void) {
		    for (int k=0;k<q.numSigmas();k++)
			if (q.dst(k,resumeexception)==ts) {
			    ts=q.src(k);
			    break;
			}
		    int index=-1;
		    for (int j=0;j<liveenv.length;j++)
			if (liveenv[j]==ts) {
			    index=j;
			    break;
			}
		    if (index==-1)
			hc.print(new java.io.PrintWriter(System.out, true));
		    Util.ASSERT(index!=-1, "Couldn't find "+ts+ " of "+q+" in "+liveenv);
		    ng=new GET(qf, first, ctmap.tempMap(t),
			       envfields[index], tenv);
		} else {
		    ng=new CONST(qf, first, ctmap.tempMap(t),
				 null, HClass.Void);
		}
		Quad.addEdge(prev, 0, ng, 0);
		prev = ng;
	    }
	}
	//-----------------------------------------------------------------
	// Typecast the argument to resume if necessary

	if (!((CALL)q).method().getReturnType().isPrimitive()&&
	    !((CALL)q).method().getReturnType()
	    .equals(linker.forName("java.lang.Object"))&&(resumeexception==0)) {
	    Temp tresult=new Temp(tf), tnull=new Temp(tf), tresultn=new Temp(tf);
	   
	    CONST cn = new CONST(qf, first, tnull,null, HClass.Void);
	    OPER op = new OPER(qf, first, Qop.ACMPEQ, tresultn,
			       new Temp[]{ctmap.tempMap(((CALL)q).retval()), tnull});
	    CJMP cjmp1=new CJMP(qf,first, tresultn, new Temp[0]);
	    PHI phi=new PHI(qf, first, new Temp[0],2);

	    INSTANCEOF io = new INSTANCEOF (qf, first,
					    tresult, 
					    ctmap.tempMap(((CALL)q).retval()),
					    ((CALL)q).method().getReturnType());
	    CJMP cjmp=new CJMP(qf, first, tresult, new Temp[0]);
			       
	    //String of quads
	    Quad.addEdges(new Quad[] {prev, cn, op, cjmp1, io, cjmp});
	    //Okay cases...
	    Quad.addEdge(cjmp1,1,phi,0);
	    Quad.addEdge(cjmp, 1, phi, 1);
	    prev=phi;

	    //Build Exception Thrower
	    HClass HCex=linker.forName("java.lang.ClassCastException");
	    Temp tex=new Temp(tf), tex2=new Temp(tf), tex3=new Temp(tf);
	    //XXXX AIMAP [HEAP]
	    NEW nquad=new NEW(qf, first, tex,
			      HCex);
	    if (newai!=null)
		newai.associate(nquad, new AllocationInformationMap.AllocationPropertiesImpl(true,
											     false, false,false,
											     false, // DEFAULT
											     null, nquad.hclass()));
	    Temp t1ex=new Temp(tf),t2=new Temp(tf);
	    CALL call=new CALL(qf, first, HCex.getConstructor(new HClass[0]),
			       new Temp[]{tex}, null, tex2, false, false,
			       new Temp[][] {{t1ex,t2}},new Temp[] {tex});
	    PHI phi2=new PHI(qf, first, new Temp[] {tex3}, new Temp[][]{{t1ex,tex2}},2);
	    THROW qthrow=new THROW(qf, first, tex3);

	    Quad.addEdge(cjmp,0,nquad,0);
	    Quad.addEdges(new Quad[] {nquad,call, phi2,qthrow});
	    Quad.addEdge(call,1,phi2,1);
	    linkFooters.add(qthrow);
	}
	//-----------------------------------------------------------------
	//Link in header code to body of method
	int linkinedge=q.nextEdge(resumeexception).which_pred();
	if (first.prevEdge(linkinedge)==null)
	    Quad.addEdge(prev,0,
			 first,
			 q.nextEdge(resumeexception).which_pred());
	else {
	    PHI phi=new PHI(qf,first, new Temp[0],2);
	    Quad.addEdge(prev,0,phi,0);
	    int linkinedge2=q.nextEdge(resumeexception).which_succ();
	    Quad.addEdge(first.prev(linkinedge),linkinedge2,phi,1);
	    Quad.addEdge(phi,0,first,linkinedge);
	}
	return header;
    }
	

    /**Shrinks PHI's as needed.*/
    
    private void fixphis() {
	Iterator phiit=phiset.iterator();
	while(phiit.hasNext()) {
	    PHI phi=(PHI) phiit.next();
	    for (int i=phi.arity()-1;i>=0;i--)
		if (phi.prevEdge(i)==null) {
		    phi=phi.shrink(i);
		}
	}
    }
    
    /**On some methods, we need to check to see if we are top level
     *so that we return instead of resume.*/
    
    private boolean needsCheck() {
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

    private void buildReturnContinuation(Quad q, Temp retthrowtemp, boolean isReturn) {
	TempFactory tf=qf.tempFactory();
	//--------------------------------------------------------------------
	//build GET next Quad
	HClass hclass=hcode.getMethod().getDeclaringClass();
	HField hfield=hclass.getField("next");
	Temp tnext=new Temp(tf);
	GET get=new GET(qf,q,
			tnext, hfield, tthis);
	
	//---------------------------------------------------------------------
	//We may need to add null check on next field
	//If null (ie run_Async, main method), we need to return instead
	//of calling resume
	Temp t21=null,t22=null;
	Quad nq=get;
	Temp retthrow=(retthrowtemp==null)?null:ctmap.tempMap(retthrowtemp);
	if (needsCheck()) {
	    Temp tnull=new Temp(tf);
	    CONST qconst=
		new CONST(qf, q, tnull, null, HClass.Void);
	    Quad.addEdge(nq,0,qconst,0);
	    Temp tcomp=new Temp(tf);
	    OPER qoper=
		new OPER(qf,q,Qop.ACMPEQ,tcomp, 
			 new Temp[] {tnext, tnull});
	    Quad.addEdge(qconst, 0, qoper, 0);
	    //Build temps for SSI sigma function
	    Temp t1=new Temp(tf),t2=new Temp(tf);
	    CJMP cjmp=(isReturn&&hc.getMethod().getReturnType()==HClass.Void)?
		new CJMP(qf, q, tcomp, new Temp[][]{{t1,t2}},
		new Temp[]{tnext}):
		new CJMP(qf, q, tcomp, new Temp[][]{{t1,t2},{t21=new Temp(tf),t22=new Temp(tf)}},
		new Temp[]{tnext,retthrow});
	    //Note SSI renaming
	    tnext=t1;
	    retthrow=t21;
	    Quad.addEdge(qoper, 0, cjmp, 0);
	    nq=cjmp;
	}

	//---------------------------------------------------------------------
	//Build the call to resume
	    
	Temp retex1=new Temp(tf);
	CALL call=null;
	if (!isReturn) {
	    HClass throwable=linker.forName("java.lang.Throwable");
	    HMethod resume=hfield.getType().getMethod("exception",
						      new HClass[] {throwable});
	    call=new CALL(qf, q, resume,
			  new Temp[] {tnext,retthrow},null,retex1,
			  true,false,new Temp[0]);
	} else if (hc.getMethod().getReturnType()!=HClass.Void) {
	    HMethod resume=(hc.getMethod().getReturnType().isPrimitive())?
		hfield.getType().getMethod("resume",
					   new HClass[] {hc.getMethod().getReturnType()}):
		hfield.getType().getMethod("resume",
					   new HClass[] {linker.forName("java.lang.Object")});
	    //***** Tailcall eventually
	    call=new CALL(qf, q, resume,
			  new Temp[] {tnext,retthrow},null,retex1,
			  true,false,new Temp[0]);
	} else {
	    HMethod resume=hfield.getType().getMethod("resume", new HClass[0]);
	    //***** Tailcall eventually
	    call=new CALL(qf, q, resume,
			  new Temp[] {tnext}, null, retex1,
			  true, false, new Temp[0]);
	}
	    
	//---------------------------------------------------------------------
	//Build THROW and RETURN quads
	
	Quad.addEdge(nq,0,call,0);
	Temp retex=(isReturn||(!needsCheck()))?retex1:new Temp(tf);
	//Temp retex=retex1;
	THROW qthrow=new THROW(qf,q,retex);
	RETURN qreturn=new RETURN(qf,q,null);
	if (isReturn)
	    Quad.addEdge(call,1,qthrow,0);
	else
	    Quad.addEdge(call,0, qreturn,0);
	linkFooters.add(qthrow);
	linkFooters.add(qreturn);
	
	//---------------------------------------------------------------------
	//If top level [main or run] link in possible RETURN edges
	//otherwise just link in 0 edge of the call to resume
	    
	if (needsCheck()) {
	    PHI phi=isReturn?new PHI(qf, q, new Temp[0], 2):
		new PHI(qf,q, new Temp[] {retex}, new Temp[][]{{t22,retex1}},2);
	    Quad.addEdge(nq, 1, phi, 0);
	    if (isReturn) {
		Quad.addEdge(call,0,phi,1);
		Quad.addEdge(phi,0,qreturn,0);
	    } else {
		Quad.addEdge(call,1,phi,1);
		Quad.addEdge(phi,0,qthrow,0);
	    }
	} else
	    if (isReturn)
		Quad.addEdge(call,0,qreturn,0);
	    else
		Quad.addEdge(call,1,qthrow,0);
	quadmap.put(q, get);
    }
    
    private void buildDoneContinuation(Quad q, Temp retthrowtemp, boolean isReturn) {
	TempFactory tf=qf.tempFactory();
	//---------------------------------------------------------------------
	//Build NEW temp for DoneContinuation
	
	HClass rettype=hc.getMethod().getReturnType();
	String pref = 
	    ContBuilder.getPrefix(rettype);
	HClass continuation = optimistic?linker.forName("harpoon.Analysis.ContBuilder."+pref+"ContinuationOpt")
	    :linker.forName
	    ("harpoon.Analysis.ContBuilder." + pref + "DoneContinuation");
	Temp newt=new Temp(tf);
	//XXXX AIMAP [CURRENT THREAD]
	NEW newq=new NEW(qf,q,newt, continuation);
	if (newai!=null)
	    newai.associate(newq, new AllocationInformationMap.AllocationPropertiesImpl(true, false, true, false,
											false, // DEFAULT
											null, newq.hclass()));
	
	//---------------------------------------------------------------------
	//Build CALL to DoneContinuation constructor

	Temp retex=new Temp(tf);
	HClass ret=isReturn?
	    (rettype.isPrimitive()?rettype:linker.forName("java.lang.Object"))
	    :linker.forName("java.lang.Throwable");
	HConstructor constructor=(ret!=HClass.Void)?continuation.getConstructor(new HClass[]{ret}):
	continuation.getConstructor(new HClass[0]);
	CALL call;
	Temp t1=new Temp(tf),t2=new Temp(tf);
	if (retthrowtemp!=null) {
	    //***** Tailcall eventually
	    Temp nretval=ctmap.tempMap(retthrowtemp);
	    call=new CALL(qf, q, constructor,
			  new Temp[] {newt,nretval}, null,retex,
			  false,false,new Temp[][]{{t1,t2}},new Temp[]{newt});
	} else {
	    //***** Tailcall eventually
	    call=new CALL(qf, q, constructor,
			  new Temp[] {newt}, null, retex,
			  false, false, new Temp[][]{{t1,t2}},new Temp[]{newt});
	}

	//---------------------------------------------------------------------
	//Add return and throw quads
	    
	Quad.addEdge(newq,0,call,0);
	THROW qthrow=new THROW(qf,q,retex);
	
	Quad.addEdge(call,1,qthrow,0);
	RETURN qreturn=new RETURN(qf,q,t1);
	Quad.addEdge(call,0,qreturn,0);
	linkFooters.add(qthrow);
	linkFooters.add(qreturn);
	quadmap.put(q, newq);
    }
    
    /** isBlocking tells whether a given <code>CALL</code>
     *  is blocking or not.  Currently based on a class hierarchy
     *  analysis.*/

    private boolean isBlocking(CALL q) {
	HMethod hm=q.method();
	if (blockingcalls.contains(hm))
	    return true;
	HClass bclass=hm.getDeclaringClass();
	Set hchilds=ch.children(bclass);
	Iterator childit=hchilds.iterator();
	while (childit.hasNext()) {
	    try {
		HClass child=(HClass)childit.next();
		HMethod hmtest=child.getDeclaredMethod(hm.getName(),
						       hm.getParameterTypes());
		if (blockingcalls.contains(hmtest))
		    return true;
	    } catch (NoSuchMethodError e) {
	    }
	}
	return false;
    }

    /** checkdoneothers sees that for a given call statement, that we've
     *  rewritten normal versions of any methods it might directly call.
     *  [meaning that we applied the swapTo and swapAdd substitutions.]*/

    private void checkdoneothers(CALL q) {
	HMethod ohm=q.method();
	//make sure we actually rewrite all methods that we can reach...
	if (!done_other.contains(ohm)) {
	    HMethod hm=ohm;
	    HClass hcl=hm.getDeclaringClass();
	    if ((!linker.forName("java.lang.Thread").equals(hcl))||
		(!hm.getName().equals("run_Async"))) {
		Set classes=ch.children(hcl);
		Iterator childit=classes.iterator();
		HMethod parent=ohm;
		while (childit.hasNext()|(hm!=null)) {
		    try {
			if (hm==null) {
			    HClass child=(HClass)childit.next();
			    hm=child.getDeclaredMethod(parent.getName(),
						       parent.getParameterTypes());
			    if (done_other.contains(hm)) {
				hm=null;
				continue;
			    }
			}
		    } catch (NoSuchMethodError e) {
		    }
		    if (hm!=null) {
			other.add(hm);
			hm=null;
		    }
		}
	    }
	}
    }





    /** scheduleMethods takes a given method that is being called,
     * either directly or by a start->run type connection, etc,
     * and schedules Asynchronous versions to be made.  This is currently
     * base on a class hierarchy scheme.
     */

    private void scheduleMethods(HMethod hm) {
	HMethod hmorig=hm;
	if (!old2new.containsKey(hm)) {
	    HClass hcl=hm.getDeclaringClass();
	    Set classes=ch.children(hcl);
	    Iterator childit=classes.iterator();
	    HMethod parent=hm;
	    while (childit.hasNext()|(hm!=null)) {
		try {
		    if (hm==null) {
			HClass child=(HClass)childit.next();
			hm=child.getDeclaredMethod(parent.getName(),
						   parent.getParameterTypes());
			if (old2new.containsKey(hm)) {
			    hm=null;
			    continue;
			}
			if (!ch.callableMethods().contains(hm))
			    continue;
			//don't need to build subclass methods that aren't
			//callable
		    }
		    if (bm.swop(hm)!=null) {
			//handle actual blocking call swapping
			old2new.put(hm, bm.swop(hm));
		    } else {
			if (hm.getName().compareTo("<init>")!=0) {
			    HCode toConvert=ucf.convert(hm);
			    if (toConvert!=null)
				async_todo.add(toConvert);
			    //else if (Modifier.isNative(hm.getModifiers())) {
			    //System.out.println("XXX:ERROR Native blocking: "+hm);
			    //System.exit(1);
			    //}
			    HMethod temp=AsyncCode.makeAsync(old2new, hm,
							     ucf,linker,optimistic);
			    if (Modifier.isNative(hm.getModifiers()))
				buildNativeWrapper(hm);
			    } else {
			    System.out.println("XXX:ERROR "+hm+" is blocking!");
			}
		    }
		} catch (NoSuchMethodError e) {
		}
		hm=null;
	    }
	}
    }

    private void buildNativeWrapper(HMethod hm) {
	System.out.println("Building native wrapper for "+hm);
	HMethod newhm=(HMethod)old2new.get(hm);
	ContCodeSSI wrappercode=new ContCodeSSI(newhm);
	QuadFactory qf=wrappercode.getFactory();
	TempFactory tf=qf.tempFactory();
	HClass[] methodTypes=hm.getParameterTypes();
	Temp[] params=new Temp[methodTypes.length+(hm.isStatic()?0:1)];
	boolean isVirtual=hm.isStatic()?false:((hm.getName().equals("<init>")||
						hm.getName().equals("<clinit>"))?false:true);
	boolean isVoid=hm.getReturnType()==HClass.Void;
	Temp retval=isVoid?null:new Temp(tf); 
	Temp retex=new Temp(tf);
	for (int i=0;i<params.length;i++)
	    params[i]=new Temp(tf);

	HEADER header=new HEADER(qf,null);
	METHOD method=new METHOD(qf, null, params, 1);
	CALL call=new CALL(qf, null, hm, params,retval, retex,
			   isVirtual, false, new Temp[0]);
	FOOTER footer=new FOOTER(qf, null, 3);
	Quad.addEdge(header,0,footer,0);
	Quad.addEdge(header,1,method,0);
	Quad.addEdge(method,0,call,0);
	wrappercode.quadSet(header);

	if (optimistic) {
	    Temp tnew=new Temp(tf);
	    String pref = 
		ContBuilder.getPrefix(hm.getReturnType());
	    HClass contClass=linker.forName("harpoon.Analysis.ContBuilder."+pref+"ContinuationOpt");
	    Temp retex2=new Temp(tf), retex3=new Temp(tf);
	    Temp retval1=new Temp(tf),retval2=new Temp(tf);

	    NEW qnew=new NEW(qf, null, tnew, contClass);
	    CALL call2=new CALL(qf, null, contClass.getConstructor(isVoid?new HClass[0]:new HClass[]{hm.getReturnType()}), isVoid?new Temp[] {tnew}:new Temp[] {tnew, retval},
	    null, retex2, false, false,new Temp[][]{{retval1,retval2}} ,new Temp[]{tnew});
	    PHI phi=new PHI(qf, null, new Temp[] {retex3},new Temp[][] {{retex,retex2}}, 2);
	    THROW qthrow=new THROW(qf, null, retex3);
	    RETURN qreturn=new RETURN(qf, null, retval1);
	    Quad.addEdge(call, 0, qnew,0);
	    Quad.addEdge(call, 1, phi, 0);
	    Quad.addEdge(qnew,0, call2,0);
	    Quad.addEdge(call2,0,qreturn,0);
	    Quad.addEdge(call2,1,phi,1);
	    Quad.addEdge(phi,0,qthrow,0);
	    Quad.addEdge(qreturn,0, footer,1);
	    Quad.addEdge(qthrow,0,footer,2);
	} else {
	    Temp tnew1=new Temp(tf),tnew2=new Temp(tf);
	    String pref = 
		ContBuilder.getPrefix(hm.getReturnType());
	    HClass contClass=linker.forName("harpoon.Analysis.ContBuilder."+pref+"DoneContinuation");
	    Temp retex1=new Temp(tf), retex2=new Temp(tf);
	    Temp retexa=new Temp(tf), retexb=new Temp(tf);
	    Temp retval1=new Temp(tf),retval2=new Temp(tf);
	    Temp finret=new Temp(tf), finex=new Temp(tf);
	    //Non Exception edge
	    NEW qnew1=new NEW(qf, null, tnew1, contClass);
	    CALL call21=new CALL(qf, null, contClass.getConstructor(isVoid?new HClass[0]:new HClass[]{hm.getReturnType()}), isVoid?new Temp[] {tnew1}:new Temp[] {tnew1, retval},
	    null, retexa, false, false,new Temp[][]{{retval1,retval2}} ,new Temp[]{tnew1});

	    //Exception edge
	    NEW qnew2=new NEW(qf, null, tnew2, contClass);
	    CALL call22=new CALL(qf, null, contClass.getConstructor(new HClass[]{linker.forName("java.lang.Throwable")}), new Temp[] {tnew2, retex},
	    null, retexb, false, false,new Temp[][]{{retex1,retex2}} ,new Temp[]{tnew2});


	    PHI phix=new PHI(qf, null, new Temp[] {finex},new Temp[][] {{retexa,retexb}}, 2);

	    PHI phir=new PHI(qf, null, new Temp[] {finret},new Temp[][] {{retval1,retex1}}, 2);
	    THROW qthrow=new THROW(qf, null, finex);
	    RETURN qreturn=new RETURN(qf, null, finret);

	    Quad.addEdge(call, 0, qnew1,0);
	    Quad.addEdge(call, 1, qnew2, 0);
	    Quad.addEdge(qnew1,0, call21,0);
	    Quad.addEdge(qnew2,0, call22,0);
	    Quad.addEdge(call21,0,phir,0);
	    Quad.addEdge(call22,0,phir,1);
	    Quad.addEdge(call21,1,phix,0);
	    Quad.addEdge(call22,1,phix,1);
	    Quad.addEdge(phir,0,qreturn,0);
	    Quad.addEdge(phix,0,qthrow,0);
	    Quad.addEdge(qreturn,0, footer,1);
	    Quad.addEdge(qthrow,0,footer,2);
	}
	System.out.println("Adding "+newhm);
	ucf.put(newhm, wrappercode);
	wrappercode.print(new java.io.PrintWriter(System.out, true));
    }



    //---------------------------------------------------------
    //Need to ->SSI
    private void handleBlocking(CALL q) {
	TempFactory tf=qf.tempFactory();
	//---------------------------------------------------------------------
	//Build array of temps to pass into Enviroment's constructor
	//We need it to determine what temps we will need
	//Then build dst array for sigma function

	Temp srco[]=getEnvTemps(q);

	Temp src[]=map(ctmap,srco);
	Temp srcarray[]=q.src();
	Vector vsrc=new Vector(Arrays.asList(srco));
	if (optimistic) {
	    Vector vsrc2=new Vector(Arrays.asList(srcarray));
	    vsrc2.removeAll(vsrc);
	    vsrc.addAll(vsrc2);
	}
	Temp[] csrc=new Temp[vsrc.size()];
	vsrc.copyInto(csrc);
	Temp[] src2=map(ctmap,csrc);
	
	Temp dst[][]=new Temp[src.length][2];
	Temp dst2[][]=new Temp[src2.length][2];

	for (int i=0;i<src2.length;i++) {
	    boolean flag=true;
	    if (optimistic)
		for (int j=0;j<q.numSigmas();j++)
		    if (csrc[i]==srcarray[j])
			if (flag) {
			    srcarray[j]=null;
			    dst2[i][0]=ctmap.tempMap(q.dst(j,0));
			    dst2[i][1]=ctmap.tempMap(q.dst(j,1));
			    if (i<src.length) {
				dst[i][0]=dst2[i][0];
				dst[i][1]=dst2[i][1];
			    }
			    flag=false;
			}
	    if (flag) {
		dst2[i][0]=new Temp(tf);
		dst2[i][1]=new Temp(tf);
		if (i<src.length) {
		    dst[i][0]=dst2[i][0];
		    dst[i][1]=dst2[i][1];
		}
	    }
	}
	
      	//--------------------------------------------------------------------
	//Rewrite the Blocking Call
	
	int numexc=5;
	int offset=0;
	if (!isCont) numexc--;
	if (optimistic) {
	    numexc--;
	    offset--;
	}


	Temp[] newt=map(ctmap, q.params());
	Temp pretex=new Temp(tf);
	Temp[] retex=new Temp[numexc];
	for (int i=0;i<numexc;i++)
	    retex[i]=new Temp(tf);

	Temp retcont=new Temp(tf);
	HMethod calleemethod=(HMethod)old2new.get(q.method());

        CALL call=new CALL(qf,q,
			   calleemethod, newt,
			   retcont,optimistic?ctmap.tempMap(q.retex()):retex[0],
			   q.isVirtual(), q.isTailCall(),
			   dst2,src2);
	Quad cnext=call;
	
	//---------------------------------------------------------------------
	//Build phi node for exception handler


	PHI phi=new PHI(qf, q, new Temp[]{pretex}, new Temp[][]{retex}, numexc);

	if (!optimistic) {
	    Quad.addEdge(cnext,1,phi,0);
	} else {
	    Temp isdone=new Temp(tf);
	    HField donefield=calleemethod.getReturnType().getField("done");
	    GET get=new GET(qf,q,isdone,donefield,retcont);
	    CJMP cjmp=new CJMP(qf,q,isdone,new Temp[0]);
	    Quad.addEdge(cnext,0,get,0);
	    Quad.addEdge(get,0,cjmp,0);
	    if (q.retval()!=null) {
		HField resultfield=calleemethod.getReturnType().getField("result");
		GET get2=new GET(qf,q, ctmap.tempMap(q.retval())  ,resultfield,retcont);
		Quad.addEdge(cjmp,1,get2,0);
		if ((!q.method().getReturnType().isPrimitive())&&
		    (!q.method().getReturnType().equals(linker.forName("java.lang.Object")))) {
		    //Have to TYPECAST
		    Temp tresult=new Temp(tf), tnull=new Temp(tf), tresultn=new Temp(tf);
		    
		    CONST cn = new CONST(qf, q, tnull,null, HClass.Void);
		    OPER op = new OPER(qf, q, Qop.ACMPEQ, tresultn,
				       new Temp[]{ctmap.tempMap(((CALL)q).retval()), tnull});
		    CJMP cjmp1=new CJMP(qf,q, tresultn, new Temp[0]);
		    PHI phia=new PHI(qf, q, new Temp[0],2);

		    INSTANCEOF io = new INSTANCEOF (qf, q,
						    tresult, 
						    ctmap.tempMap(q.retval()),
						    ((CALL)q).method().getReturnType());
		    CJMP cjmpa=new CJMP(qf, q, tresult, new Temp[0]);
		    
		    //String of quads
		    Quad.addEdges(new Quad[] {get2, cn, op, cjmp1, io, cjmpa});
		    //Okay cases...
		    Quad.addEdge(cjmp1,1,phia,0);
		    Quad.addEdge(cjmpa, 1, phia, 1);

		    //Build Exception Thrower
		    HClass HCex=linker.forName("java.lang.ClassCastException");
		    Temp tex=new Temp(tf), tex2=new Temp(tf), tex3=new Temp(tf);
		    //XXXX AIMAP [HEAP]
		    NEW nquad=new NEW(qf, q, tex,
				      HCex);
		    if (newai!=null)
			newai.associate(nquad, new AllocationInformationMap.AllocationPropertiesImpl(true, false, false, false,
												     false, // DEFAULT
												     null, nquad.hclass()));
		    Temp t1ex=new Temp(tf),t2=new Temp(tf);
		    CALL calla=new CALL(qf, q, HCex.getConstructor(new HClass[0]),
				       new Temp[]{tex}, null, tex2, false, false,
				       new Temp[][] {{t1ex,t2}},new Temp[] {tex});
		    PHI phi2=new PHI(qf, q, new Temp[] {tex3}, new Temp[][]{{t1ex,tex2}},2);
		    THROW qthrow=new THROW(qf, q, tex3);
		    
		    Quad.addEdge(cjmpa,0,nquad,0);
		    Quad.addEdges(new Quad[] {nquad,calla, phi2,qthrow});
		    Quad.addEdge(calla,1,phi2,1);
		    linkFooters.add(qthrow);
		} 

	    } else {
		NOP nop=new NOP(qf,q);
		Quad.addEdge(cjmp,1,nop,0);
	    }
	    cnext=cjmp;
	}
		
	//---------------------------------------------------------------------
	//Build array for initializer

	Temp tenv=new Temp(tf);
	Temp params[]=new Temp[src.length+1];
	params[0]=tenv;
	for (int i=0;i<src.length;i++)
	    params[i+1]=dst[i][0];
	if (recycle&&isCont&&(q==origCall)) {
	    //We can recycle!!!
	    //tthis has the temp for our object
	    Temp tretex=new Temp(tf);
	    HField envfield=hcode.getMethod().getDeclaringClass().getField("e");
	    GET get1=new GET(qf,q,tenv,envfield,tthis);
	    HClass[] envparams=getEnv(q).getConstructors()[0].getParameterTypes();
	    HMethod hrecycle=getEnv(q).getDeclaredMethod("recycle", envparams);
	    CALL callrec=new CALL(qf,q, hrecycle, params, null,tretex,
				  true,false, new Temp[0]);

	    PHI qphi=new PHI(qf,q,new Temp[0],2);
	    


	    String pref = 
		ContBuilder.getPrefix(q.method().getReturnType());
	    HClass[] nextarray=
		new HClass[] {linker.forName("harpoon.Analysis.ContBuilder."+pref+"ResultContinuation")};
	    HMethod setnextmethod=
		calleemethod.getReturnType().getMethod("setNext",nextarray);
	    Util.ASSERT(setnextmethod!=null,"no setNext method found");
	    CALL callrec2=new CALL(qf, q,
				   setnextmethod, new Temp[] {retcont, tthis},
				   null, tretex, true, false, 
				   new Temp[0]);
	    RETURN qret=new RETURN(qf, q, null);
    	    THROW qthrow=new THROW(qf,q,tretex);
	
	    Quad.addEdge(cnext,0,get1,0);
	    Quad.addEdge(get1,0,callrec,0);
	    Quad.addEdge(callrec,0,callrec2,0);
	    Quad.addEdge(callrec2,0,qret,0);
	    Quad.addEdge(callrec,1,qphi,0);
	    Quad.addEdge(callrec2,1,qphi,1);
	    Quad.addEdge(qphi,0,qthrow,0);
	    linkFooters.add(qthrow);
	    linkFooters.add(qret);
	    if (!optimistic) {
		//toss away the old phi...
		THROW throwq=new THROW(qf,q,retex[0]);    
		Quad.addEdge(cnext,1,throwq,0);
		linkFooters.add(throwq);
	    }
	} else { 
	    //---------------------------------------------------------------------
	    //Build Environment NEW & CALL to init
	    HClass env=getEnv(q);
	    //XXXXXXXXXX
	    //AIMAP[Current Thread]
	    NEW envq=new NEW(qf, q, tenv, env);
	    if (newai!=null)
		newai.associate(envq, new AllocationInformationMap.AllocationPropertiesImpl(true, false, true, false,
											    false, // DEFAULT
											    null, envq.hclass()));
	    Quad.addEdge(cnext,0,envq,0);
	    Temp t1=new Temp(tf),t2=new Temp(tf),t21=new Temp(tf),t22=new Temp(tf);
	    CALL callenv=new CALL(qf, q, env.getConstructors()[0],
				  params, null, retex[1+offset], false, false, 
				  new Temp[][]{{t1,t2},{t21,t22}},
				  new Temp[] {tenv,retcont});
	    tenv=t1;retcont=t21;
	    Quad.addEdge(envq,0,callenv,0);
	    Quad.addEdge(callenv,1,phi,1+offset);
	    
	    //---------------------------------------------------------------------
	    //Build Continuation NEW & CALL to init
	    
	    Temp tcont=new Temp(tf);
	    HClass contclass=(HClass)cont_map.get(q);
	    //XXX AIMAP[Current Thread]
	    NEW newc=new NEW(qf, q, tcont, contclass);
	    if (newai!=null)
		newai.associate(newc, new AllocationInformationMap.AllocationPropertiesImpl(true, false, true,false,
											    false, // DEFAULT
											    null, newc.hclass()));
	    Quad.addEdge(callenv,0,newc,0);
	    //	HClass environment=linker.forName("harpoon.Analysis.EnvBuilder.Environment");
	    HConstructor call2const=contclass.getConstructor(new HClass[]{
		env});
	    Temp nt1=new Temp(tf),nt2=new Temp(tf),nt21=new Temp(tf),nt22=new Temp(tf);
	    CALL call2=new CALL(qf,q,
				call2const,
				new Temp[] {tcont, tenv}, null, retex[2+offset], 
				false, false, new Temp[][]{{nt1,nt2},{nt21,nt22}},
				new Temp[]{tcont,retcont});
	    tcont=nt1;retcont=nt21;
	    Quad.addEdge(newc,0,call2,0);
	    Quad.addEdge(call2,1,phi,2+offset);
	    
	    //---------------------------------------------------------------------
	    //Link new Continuation onto Continuation returned by blocking call
	    String pref = 
		ContBuilder.getPrefix(q.method().getReturnType());
	    HClass[] nextarray=
		new HClass[] {linker.forName("harpoon.Analysis.ContBuilder."+pref+"ResultContinuation")};
	    HMethod setnextmethod=
		calleemethod.getReturnType().getMethod("setNext",nextarray);
	    Util.ASSERT(setnextmethod!=null,"no setNext method found");
	    Temp nnt1=new Temp(tf),nnt2=new Temp(tf);
	    CALL call3=new CALL(qf, q,
				setnextmethod, new Temp[] {retcont, tcont},
				null, retex[3+offset], true, false, 
			    new Temp[][]{{nnt1,nnt2}},new Temp[]{tcont});
	    tcont=nnt1;
	    Quad.addEdge(call2, 0, call3, 0);
	    Quad.addEdge(call3, 1, phi, 3+offset);
	    
	    
	    //---------------------------------------------------------------------
	    //Two possibilities:
	    //1)  We are a continuation, so we want to do a setNext(this.next)
	    //2)  We are a Async method, so we want to return continuation
	    
	    if (isCont) {
		Temp tnext=new Temp(tf);
		HClass hclass=hcode.getMethod().getDeclaringClass();
		HField hfield=hclass.getField("next");
		GET get=new GET(qf,q,
				tnext, hfield, tthis);
		Quad.addEdge(call3,0,get,0);
		String pref2 =
		    ContBuilder.getPrefix(hc.getMethod().getReturnType());
		HMethod setnextmethod2=
		    contclass.getMethod("setNext",
					new HClass[] {linker.forName("harpoon.Analysis.ContBuilder."+pref2+"ResultContinuation")});
		Util.ASSERT(setnextmethod2!=null,"no setNext method found");
		CALL call4=new CALL(qf, q,
				    setnextmethod2, new Temp[] {tcont, tnext},
				    null, retex[4+offset], true, false, new Temp[0]);
		Quad.addEdge(get,0,call4,0);
		Quad.addEdge(call4,1,phi,4+offset);
		RETURN returnq=new RETURN(qf, q, null);
		Quad.addEdge(call4,0,returnq,0);
		linkFooters.add(returnq);
	    } else {
		RETURN returnq=new RETURN(qf,q,tcont);
		Quad.addEdge(call3, 0, returnq,0);
		linkFooters.add(returnq);
	    }
	    //---------------------------------------------------------------
	    //Do THROW of exceptions from phi node
	    
	    THROW throwq=new THROW(qf,q,pretex);    
	    Quad.addEdge(phi,0,throwq,0);
	    linkFooters.add(throwq);
	}	//end *******
	quadmap.put(q,call);
	if (optimistic)
	    followchildren=true;
	else
	    followchildren=false;
    }
    //Knows about SSI
    private Temp[] getEnvTemps(CALL q) {
	Set liveoutx = liveness.getLiveOut(q);
	Set livein = liveness.getLiveIn(q);
	WorkSet env=new WorkSet();
	for (Iterator ii=liveoutx.iterator();ii.hasNext();) {
	    boolean search=true;
	    Temp t=(Temp)ii.next();
	    for (int i=0;(i<q.arity())&&search;i++)
		for (int j=0;j<q.numSigmas();j++) {
		    if (q.dst(j,i)==t) {
			if (typemap.typeMap(q,t)!=HClass.Void) {
			    env.add(q.src(j));
			}
			search=false;
			break;
		    }
		}
	    if (search)
		if (livein.contains(t)) {
		    if (typemap.typeMap(q,t)!=HClass.Void)
			env.add(t);
		}
	}

	Temp [] params = (Temp[]) env.toArray(new Temp[0]);
	Collections.sort(Arrays.asList(params));
	return params;
    }
    
    private void handleNonBlocking(CALL q) {
	followchildren=true;
	TempFactory tf=qf.tempFactory();
	//need to check if swop necessary
	if (swapTo(q.method())!=null) {
	    //need to swop this method for replacement
	    quadmap.put(q,new CALL(qf, q, swapTo(q.method()),
				   map(ctmap, q.params()),
				   (q.retval()==null)?null:ctmap.tempMap(q.retval()),
				   (q.retex()==null)?null:ctmap.tempMap(q.retex()), q.isVirtual(),
				   q.isTailCall(), map(ctmap, q.dst()),
				   map(ctmap, q.src())));
	} else if (swapAdd(q.method())!=null) {
	    //need to add an additional call [for makeAsync's, etc]

	    CALL cq=(CALL)q.clone(qf, ctmap);
	    int index=-1;
	    for (int i=0;i<q.numSigmas();i++)
		if (q.src(i)==q.params(0))
		    index=i;
	    Temp retex=new Temp(tf);
	    if (index==-1)
		stillokay=false;
	    Temp t=(index==-1)?ctmap.tempMap(q.params(0)):ctmap.tempMap(q.dst(index,0));
	    CALL nc=new CALL(qf, q,
			     swapAdd(q.method()), 
			     new Temp[]{t},
			     null, retex, true, false, new Temp[0]);
	    Quad.addEdge(cq,0,nc, 0);

	    PHI phi=new PHI(qf,q,new Temp[0],2);

	    Quad.addEdge(nc,0,phi,0);
	    Quad.addEdge(nc,1,phi,1);
	    addedCall.add(cq);
	    quadmap.put(q, cq);
	} else
	    quadmap.put(q, q.clone(qf, ctmap));
    }

    public boolean needsRepair() {
	return stillokay;
    }

    protected final static Temp[] map(TempMap tm, Temp[] ta) {
        Temp[] r = new Temp[ta.length];
        for (int i=0; i<r.length; i++)
            r[i] = tm.tempMap(ta[i]);
        return r;
    }

    protected final static Temp[][] map(TempMap tm, Temp[][] taa) {
        Temp[][] r = new Temp[taa.length][];
        for (int i=0; i<r.length; i++)
            r[i] = map(tm, taa[i]);
        return r;
    }


    /**
     * swapAdd takes in an <code>HMethod</code> and returns an <code>HMethod</code>
     * that is to be called following the first method is called.
     */
    public HMethod swapAdd(HMethod old) {
	HClass ss=linker.forName("java.net.ServerSocket");
	HClass fis=linker.forName("java.io.FileInputStream");
	HClass fos=linker.forName("java.io.FileOutputStream");
	if (old.equals(ss.getConstructor(new HClass[] {HClass.Int,
							   HClass.Int,
							   linker.forName("java.net.InetAddress")})))
	    return ss.getDeclaredMethod("makeAsync", new HClass[0]);
	if (old.equals(fis.getConstructor(new HClass[] {linker.forName("java.lang.String")}))||
	    old.equals(fis.getConstructor(new HClass[] {linker.forName("java.io.FileDescriptor")})))
	    return fis.getMethod("makeAsync", new HClass[0]);
	if (old.equals(fos.getConstructor(new HClass[] {linker.forName("java.lang.String"), HClass.Boolean}))||
	    old.equals(fos.getConstructor(new HClass[] {linker.forName("java.io.FileDescriptor")})))
	    return fos.getMethod("makeAsync", new HClass[0]);
	return null;
    }
    
    /**
     * swapTo takes in an <code>HMethod</code> and returns an <code>HMethod</code>
     * that is to be called following the first method is called.
     */
    
    public HMethod swapTo(HMethod old) {
	//Handle Socket getInputStream calls
	HMethod gis=linker.forName("java.net.Socket")
	    .getDeclaredMethod("getInputStream", new HClass[0]);
	if (gis.equals(old))
	    return linker.forName("java.net.Socket").getDeclaredMethod
		("getAsyncInputStream", new HClass[0]);
	

	//Handle Socket getOutputStream calls
	HMethod  gos=linker.forName("java.net.Socket")
	    .getDeclaredMethod("getOutputStream", new HClass[0]);
	if (gos.equals(old))
	    return linker.forName("java.net.Socket").getDeclaredMethod
		("getAsyncOutputStream", new HClass[0]);
	

	//Handle start calls
	HClass HCthrd=linker.forName("java.lang.Thread");
	if (old.equals(HCthrd.getMethod("start",
					new HClass[0]))) {
	    HMethod hmrun=old.getDeclaringClass().getMethod("run",
							    new HClass[0]);
	    scheduleMethods(hmrun);
	    return old.getDeclaringClass().getMethod("start_Async",
						     new HClass[0]);
	}
	
	//No match found
	return null;
    }
}
