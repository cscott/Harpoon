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
import harpoon.ClassFile.HClassSyn;
import harpoon.ClassFile.HCode;
import harpoon.ClassFile.HConstructor;
import harpoon.ClassFile.HField;
import harpoon.ClassFile.HMethod;
import harpoon.ClassFile.HMethodSyn;
import harpoon.ClassFile.UpdateCodeFactory;
import harpoon.IR.Quads.CALL;
import harpoon.IR.Quads.Code;
import harpoon.IR.Quads.Edge;
import harpoon.IR.Quads.FOOTER;
import harpoon.IR.Quads.GET;
import harpoon.IR.Quads.HEADER;
import harpoon.IR.Quads.METHOD;
import harpoon.IR.Quads.NEW;
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
 * @version $Id: AsyncCode.java,v 1.1.2.11 2000-01-05 18:13:46 bdemsky Exp $
 */
public class AsyncCode {

    /** Creates a <code>AsyncCode</code>. 
     *
     *  @param hc
     *         the <code>HCode</code> from which to build this
     *         <code>AsyncCode</code>.
     *  @param liveness
     *         results of liveness analysis
     *  @param ucf
     *         <code>UpdateCodeFactory</code> with which to register new
     *         <code>HCode</code>
     */

    public static void buildCode(HCode hc, Map old2new, Set async_todo, 
			   QuadLiveness liveness,
			   Set blockingcalls, 
			   UpdateCodeFactory ucf, Map classMap, ToAsync.BlockingMethods bm) 
	throws NoClassDefFoundError
    {
	System.out.println("Entering AsyncCode.buildCode()");

	Quad root = (Quad)hc.getRootElement();

	WorkSet cont_todo=new WorkSet();
	cont_todo.add(root);

	//contmap maps blocking calls->continuations
	HashMap cont_map=new HashMap();
	HashMap env_map=new HashMap();
	
	while(!cont_todo.isEmpty()) {
	    Quad quadc=(Quad) cont_todo.pop();
	    ContVisitor cv=new ContVisitor(cont_todo, async_todo, 
					   old2new, cont_map, 
					   env_map, liveness,
					   blockingcalls, hc.getMethod(), 
					   classMap,hc, ucf,bm);
	    quadc.accept(cv);
	}
    }

    static class ContVisitor extends QuadVisitor {
	WorkSet cont_todo;
	Map old2new, cont_map, env_map;
	Set blockingcalls, async_todo;
	HMethod hmethod;
	boolean header;
	Map classMap;
	CloningVisitor clonevisit;
	QuadLiveness liveness;
	UpdateCodeFactory ucf;

	public ContVisitor(WorkSet cont_todo, Set async_todo, 
			   Map old2new, Map cont_map, 
			   Map env_map, QuadLiveness liveness,
			   Set blockingcalls, HMethod hmethod, 
			   Map classMap, HCode hc, UpdateCodeFactory ucf,
			   ToAsync.BlockingMethods bm) {
	    this.liveness=liveness;
	    this.env_map=env_map;
	    this.cont_todo=cont_todo;
	    this.async_todo=async_todo;
	    this.old2new=old2new;
	    this.cont_map=cont_map;
	    this.blockingcalls=blockingcalls;
	    this.hmethod=hmethod;
	    this.classMap=classMap;
	    this.header=false;
	    this.ucf=ucf;
	    this.clonevisit=new CloningVisitor(blockingcalls, cont_todo,
					       cont_map, env_map, liveness,
					       async_todo, old2new,
					       classMap,hc,ucf,bm);
	}

	public void visit(Quad q) {
	    System.out.println("ERROR: "+q+" in ContVisitory!!!");
	}

	public void visit(HEADER q) {
	    //need to build continuation for this Header...
	    //nmh is the HMethod we wish to attach this HCode to
	    HMethod nhm=(HMethod)old2new.get(hmethod);
	    //mark header flag
	    header=true;
	    clonevisit.reset(nhm,q.getFactory().tempFactory(),false);
	    copy(q,-1);
	    ucf.update(nhm, clonevisit.getCode());
	}

	public void visit(CALL q) {
	    //need to build continuation for this CALL
	    //nmh is the HMethod we wish to attach this HCode to
	    HClass hclass=(HClass) cont_map.get(q);
	    HClass throwable=HClass.forName("java.lang.Throwable");
	    HMethod resume=
		hclass.getDeclaredMethod("resume",
					 new HClass[] {q.method().getReturnType()});
	    HMethod exception=
		hclass.getDeclaredMethod("exception",
					 new HClass[] {throwable});

	    //Resume method
	    clonevisit.reset(resume,q.getFactory().tempFactory(), true);
	    copy(q,0);
	    //addEdges should add appropriate headers
	    ucf.update(resume, clonevisit.getCode());

	    //Exception method
	    clonevisit.reset(exception, q.getFactory().tempFactory(), true);
	    copy(q,1);
	    //addEdges should add appropriate headers
	    ucf.update(exception, clonevisit.getCode());
	}

	public void copy(Quad q, int resumeexception) {
	    //-1 normal
	    //0 resume
	    //1 exception
	    WorkSet todo=new WorkSet();
	    WorkSet done=new WorkSet();
	    if (resumeexception==-1)
		todo.push(q);
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
			    todo.push(q);
		}
	    }
	    clonevisit.addEdges(q,resumeexception);
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
	Map classMap;
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

	public CloningVisitor(Set blockingcalls, Set cont_todo,
			      Map cont_map, Map env_map, 
			      QuadLiveness liveness, Set async_todo,
			      Map old2new, Map classMap, 
			      HCode hc, UpdateCodeFactory ucf,
			      ToAsync.BlockingMethods bm) {
	    this.liveness=liveness;
	    this.blockingcalls=blockingcalls;
	    this.cont_todo=cont_todo;
	    this.cont_map=cont_map;
	    this.async_todo=async_todo;
	    this.old2new=old2new;
	    this.classMap=classMap;
	    this.env_map=env_map;
	    this.ucf=ucf;
	    this.hc=hc;
	    this.bm=bm;
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
					  q, liveness.getLiveOutArray(q))).makeEnv();
	    env_map.put(q, nhclass);
	    return nhclass;
	}

	public void addEdges(Quad q, int resumeexception) {
	    if (resumeexception!=-1) {
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
		    for (int i=0;i<next.length;i++) {
			if (quadmap.containsKey(next[i])) {
			    //this quad was cloned
			    if (!done.contains(next[i]))
				todo.push(next[i]);
			    Quad cn=(Quad)quadmap.get(next[i]);
			    //add the edge in
			    Quad.addEdge(cnq,i,nq,q.nextEdge(i).which_pred());
			}
		    }
		}
		//Need to build header
		QuadFactory qf=hcode.getFactory();
		TempFactory tf=qf.tempFactory();
		Quad first=(Quad) quadmap.get(q.next(resumeexception));
		Temp oldrtemp=
		    (resumeexception == 0) ?
		    ((CALL)q).retval():((CALL)q).retex();
		Temp newrtemp=ctmap.tempMap(oldrtemp);
		Temp[] params=null;
		if (oldrtemp==null)
		    params=new Temp[1];
		else {
		    params=new Temp[2];
		    params[1]=newrtemp;
		}
		params[0]=tthis;
		HEADER header=new HEADER(qf,first);
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
      		Temp[] liveout=liveness.getLiveOutArray(q);

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
		hcode.quadSet(header);
	    } else {
		WorkSet done=new WorkSet();
		WorkSet todo=new WorkSet();
		todo.push(q);
		while (!todo.isEmpty()) {
		    Quad nq=(Quad)todo.pop();
		    done.add(nq);
		    Quad cnq=(Quad)quadmap.get(nq);
		    Quad[] next=nq.next();
		    for (int i=0;i<next.length;i++) {
			if (quadmap.containsKey(next[i])) {
			    //this quad was cloned
			    if (!done.contains(next[i]))
				todo.push(next[i]);
			    Quad cn=(Quad)quadmap.get(next[i]);
			    //add the edge in
			    Quad.addEdge(cnq,i,nq,q.nextEdge(i).which_pred());
			}
		    }
		}
		hcode.quadSet((Quad)quadmap.get(q));
	    }
	}

	public void visit(RETURN q) {
	    TempFactory tf=hcode.getFactory().tempFactory();
	    if (isCont) {
		HClass hclass=hcode.getMethod().getDeclaringClass();
		HField hfield=hclass.getField("next");
		HMethod resume=null;
		if (hc.getMethod().getReturnType()!=HClass.Void)
		    resume=
			hfield.getType().getDeclaredMethod("resume",
							   new HClass[] {hc.getMethod().getReturnType()});
		else
		    resume=hfield.getType().getDeclaredMethod("resume",
							      new HClass[0]);
		Temp tnext=new Temp(tf);
		GET get=new GET(hcode.getFactory(),q,
				tnext, hfield, tthis);
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
		Quad.addEdge(get,0,call,0);
		THROW qthrow=new THROW(hcode.getFactory(),q,retex);
		Quad.addEdge(call,1,qthrow,0);
		RETURN qreturn=new RETURN(hcode.getFactory(),q,null);
		Quad.addEdge(call,0,qreturn,0);
		linkFooters.add(qthrow);
		linkFooters.add(qreturn);
		quadmap.put(q, get);
	    }
	    else {
		HClass rettype=hc.getMethod().getReturnType();
		String pref = 
		    ContBuilder.getPrefix(rettype);
		HClass continuation = HClass.forName
		    ("harpoon.Analysis.ContBuilder." + pref + "DoneContinuation");
		HClass ret=rettype.isPrimitive()?rettype:HClass.forName("java.olang.Object");
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
				  true,false,new Temp[0]);
		} else {
		    //***** Tailcall eventually
		    call=new CALL(hcode.getFactory(), q, constructor,
				  new Temp[] {newt}, null, retex,
				  true, false, new Temp[0]);
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
		HClass throwable=HClass.forName("java.lang.Throwable");
		HMethod resume=
		    hfield.getType().getDeclaredMethod("exception",
						       new HClass[] {throwable});
		Temp tnext=new Temp(tf);
		GET get=new GET(hcode.getFactory(),q,
				tnext, hfield, tthis);
		CALL call=null;
		Temp retex=new Temp(tf);
		Temp nretval=ctmap.tempMap(q.throwable());
		//***** Tailcall eventually
		//need to do get next first
		call=new CALL(hcode.getFactory(), q, resume,
			      new Temp[] {tnext,nretval},null,retex,
			      true,false,new Temp[0]);
		Quad.addEdge(get,0,call,0);
		THROW qthrow=new THROW(hcode.getFactory(),q,retex);
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
		HClass continuation = HClass.forName
		    ("harpoon.Analysis.ContBuilder." + pref + "DoneContinuation");
		HClass ret=HClass.forName("java.lang.Throwable");
		HConstructor constructor=continuation.getConstructor(new HClass[]{ret});
		Temp newt=new Temp(tf);
		NEW newq=new NEW(hcode.getFactory(),q,newt, continuation);
		CALL call;
		//***** Tailcall eventually
		    //need to do get next first
		Temp nretval=ctmap.tempMap(q.throwable());
		Temp retex=new Temp(tf);
		call=new CALL(hcode.getFactory(), q, constructor,
			      new Temp[] {newt,nretval}, null,retex,
			      true,false,new Temp[0]);
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

	public void visit(CALL q) {
	    if (blockingcalls.contains(q.method())) {
		if (!cont_map.containsKey(q)) {
		    cont_todo.add(q);
		    HClass hclass=createContinuation(q.method(),  q,
						     ucf); 
		    cont_map.put(q,hclass);
		    HMethod hm=((CALL) q).method();
		    if (!old2new.containsKey(hm)) {
			if (bm.swop(hm)!=null) {
			    //handle actual blocking call swapping
			    old2new.put(hm, bm.swop(hm));
			} else {
			    async_todo.add(hm);
			    HMethod temp=makeAsync(old2new, q.method(),
						   ucf, classMap);
			}
		    }
		}
		//rewrite blocking call
		

		followchildren=false;
	    } else {
		followchildren=true;
		//need to check if swop necessary

		quadmap.put(q, q.clone(hcode.getFactory(), ctmap));
	    }
	}
    }

//      // swop out all the calls to Socket.getInputStream and 
//      // replace with calls to Socket.getAsyncInputStream:
//      private void swop(Set s, Map quadmap) {
//  	final HMethod gais = 
//  	    HClass.forName("java.net.Socket").getDeclaredMethod
//  	    ("getAsyncInputStream", new HClass[0]);
//  	for (Iterator i=s.iterator(); i.hasNext(); ) {
//  	    CALL cts = (CALL)quadmap.get(i.next()); // call to swop
//  	    CALL replacement = new CALL(this.qf, cts, gais, cts.params(),
//  					cts.retval(), cts.retex(), 
//  					cts.isVirtual(), cts.isTailCall(),
//  					new Temp[0]);
//  	    // hook up all incoming edges
//  	    Edge[] pe = cts.prevEdge();
//  	    for (int j=0; j<pe.length; j++) {
//  		Quad.addEdge((Quad)pe[j].from(), pe[j].which_succ(), 
//  			     replacement, pe[j].which_pred());
//  	    }
//  	    // hook up outgoing edges
//  	    Edge[] ne = cts.nextEdge();
//  	    for (int j=0; j<ne.length; j++) {
//  		Quad.addEdge(replacement, ne[j].which_succ(),
//  			     (Quad)ne[j].to(), ne[j].which_pred());
//  	    }
//  	}
//      }

    // create asynchronous version of HMethod to replace blocking version
    // does not create HCode that goes w/ it...
    public static HMethod makeAsync(Map old2new, HMethod original,
			      UpdateCodeFactory ucf, Map classmap)
    {
	//	final HMethod original = blocking.method();
	final HClass originalClass = original.getDeclaringClass();
	HClassSyn replacementClass;

	// create a new HClassSyn that replaces the original HClass
	if (classmap.containsKey(originalClass))
	    replacementClass=(HClassSyn) classmap.get(originalClass);
	else {
	    replacementClass=new HClassSyn(originalClass, true);
	    // clone HMethods from original class
	    HMethod[] toClone = originalClass.getDeclaredMethods();
	    for (int i = 0; i < toClone.length; i++) {
		HMethod clone = replacementClass.getDeclaredMethod
		    (toClone[i].getName(), toClone[i].getParameterTypes());
		ucf.update(clone, 
			   ((QuadNoSSA)ucf.convert(toClone[i])).clone(clone));
	    }
	    classmap.put(originalClass, replacementClass);
	}
	    
	// use the return type of the original HMethod to get the String 
	// prefix for the type of Continuation we want as the new return type
	final String pref = ContBuilder.getPrefix(original.getReturnType());

	// get the return type for the replacement HMethod
	final HClass newReturnType = HClass.forName
	    ("harpoon.Analysis.ContBuilder" + pref + "Continuation");

	// find a unique name for the replacement HMethod
	final String methodNamePrefix = original.getName() + "Async_";
	String newMethodName = methodNamePrefix;
	int i = 0;
	while(true) {
	    try {
		newMethodName = methodNamePrefix + i++; 
		replacementClass.getMethod(newMethodName, 
					   original.getParameterTypes());
	    } catch (NoSuchMethodError e) {
		break;
	    }
	}
	
	// create replacement method
	final HMethodSyn replacement = 
	    new HMethodSyn(replacementClass, newMethodName, 
			   original.getParameterTypes(), newReturnType);

	replacement.setExceptionTypes(original.getExceptionTypes());
	replacement.setModifiers(original.getModifiers());
	replacement.setParameterNames(original.getParameterNames());
	replacement.setSynthetic(original.isSynthetic());
	old2new.put(original, replacement);
	return replacement;
    }

    // creates the HClass and constructor for the continuation
    private static HClass createContinuation(HMethod blocking, CALL callsite,
				      UpdateCodeFactory ucf) 
	throws NoClassDefFoundError
    {
	final HClass template = 
	    HClass.forName("harpoon.Analysis.ContBuilder.ContTemplate");

	final HClassSyn continuationClass = new HClassSyn(template);
	final int numConstructors = continuationClass.getConstructors().length;
	Util.assert(numConstructors == 1,
		    "Found " + numConstructors + " constructors in " +
		    "ContTemplate. Expected one");

	// use the return type of the blocking HMethod to get the String 
	// prefix for the superclass for the continuation we want to create
	final String superPref = 
	    ContBuilder.getPrefix(blocking.getReturnType());

	// get the superclass for the continuation
	final HClass superclass = HClass.forName
	    ("harpoon.Analysis.ContBuilder." + superPref + "Continuation");

	continuationClass.setSuperclass(superclass);

	// we want the return type of the blocking call
	// this gives us the interface that our continuation should implement
	final String interPref = 
	    ContBuilder.getPrefix(callsite.method().getReturnType());

	// get the interface that the continuation needs to implement
	final HClass inter = HClass.forName("harpoon.Analysis.ContBuilder." + 
					    interPref + "ResultContinuation");
	
	final HClass environment = HClass.forName
	    ("harpoon.Analysis.EnvBuilder.Environment");

	// clone template's constructor HCode
	HConstructor hc = null;
	HConstructor nhc = null;
	try {
	    hc = template.getConstructor(new HClass[] {environment});
	    nhc = continuationClass.getConstructor(new HClass[] {environment});
	    HCode hchc = ((Code)ucf.convert(hc)).clone(nhc);
	    ucf.update(nhc, hchc);
	} catch (NoSuchMethodError e) {
	    System.err.println("Missing constructor for environment template");
	}

	// create resume method but HCode not yet created
	HMethod hm = null;
	HMethodSyn nhm = null;
	try {
	    hm = continuationClass.getDeclaredMethod("resume", new HClass[0]);
	    nhm = new HMethodSyn(continuationClass, hm, true);
	    continuationClass.removeDeclaredMethod(hm);
	} catch (NoSuchMethodError e) {
	    System.err.println("Missing resume() from constructor template");
	}

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
		parameterTypes[0] = HClass.forName("java.lang.Object");
	    }
	    nhm.setParameterNames(parameterNames);
	    nhm.setParameterTypes(parameterTypes);
	}

	return continuationClass;
    }
}
