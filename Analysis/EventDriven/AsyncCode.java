// AsyncCode.java, created Thu Nov 11 15:17:54 1999 by kkz
// Copyright (C) 1999 Karen K. Zee <kkzee@alum.mit.edu>
// 		      and Brian Demsky <bdemsky@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.EventDriven;

import harpoon.Analysis.ContBuilder.ContBuilder;
import harpoon.Analysis.EnvBuilder.EnvBuilder;
import harpoon.Analysis.Quads.QuadLiveness;
import harpoon.Analysis.ClassHierarchy;
import harpoon.Analysis.Quads.Unreachable;
import harpoon.Analysis.Maps.TypeMap;
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
import harpoon.IR.Quads.SET;
import harpoon.IR.Quads.HEADER;
import harpoon.IR.Quads.METHOD;
import harpoon.IR.Quads.NEW;
import harpoon.IR.Quads.OPER;
import harpoon.IR.Quads.Qop;
import harpoon.IR.Quads.PHI;
import harpoon.IR.Quads.Quad;
import harpoon.IR.Quads.QuadNoSSA;
import harpoon.IR.Quads.QuadFactory;
import harpoon.IR.Quads.QuadSSI;
import harpoon.IR.Quads.QuadRSSI;
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
import java.lang.reflect.Modifier;

/**
 * <code>AsyncCode</code>
 * 
 * @author Karen K. Zee <kkzee@alum.mit.edu>
 * @version $Id: AsyncCode.java,v 1.1.2.53 2000-02-13 04:39:35 bdemsky Exp $
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
     *         <code>CachingCodeFactory</code> with which to register new
     *         <code>HCode</code>
     *  @param bm
     *         <code>ToAsync.BlockingMethods</code> object which tells
     *         the analysis information about lowest level blocking calls
     */

    public static void buildCode(HCode hc, Map old2new, Set async_todo, 
			   QuadLiveness liveness,
			   Set blockingcalls, 
			   CachingCodeFactory ucf, ToAsync.BlockingMethods bm,
			   HMethod mroot, Linker linker, ClassHierarchy ch,
			   Set other, Set done_other, boolean methodstatus,
			   TypeMap typemap) 
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
					   hc, ucf,bm,mroot, linker,ch,
					   other, done_other,methodstatus,
					   typemap);
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
	CachingCodeFactory ucf;
	Linker linker;
	boolean methodstatus;

	public ContVisitor(WorkSet cont_todo, Set async_todo, 
			   Map old2new, Map cont_map, 
			   Map env_map, QuadLiveness liveness,
			   Set blockingcalls, HMethod hmethod, 
			   HCode hc, CachingCodeFactory ucf,
			   ToAsync.BlockingMethods bm, HMethod mroot, 
			   Linker linker, ClassHierarchy ch,
			   Set other, Set done_other, boolean methodstatus,
			   TypeMap typemap) {
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
	    this.methodstatus=methodstatus;
	    this.clonevisit=new CloningVisitor(blockingcalls, cont_todo,
					       cont_map, env_map, liveness,
					       async_todo, old2new,
					       hc,ucf,bm,mroot, linker,ch,
					       other, done_other,methodstatus,
					       typemap);
	}

	void addCode(HMethod hm, HCode hc) {
	    if (clonevisit.needsRepair()) {
		ucf.put(hm,hc);
	    } else
		ucf.put(hm,new ContCodeSSI(new ContCodeNoSSA((QuadSSI)hc)));
	}

	public void visit(Quad q) {
	    System.out.println("ERROR: "+q+" in ContVisitory!!!");
	}

	public void visit(HEADER q) {
	    //Handles building HCode's for the HEADER quad...
	    //need to build continuation for this Header...
	    //nmh is the HMethod we wish to attach this HCode to
	    HMethod nhm=methodstatus?hmethod:(HMethod)old2new.get(hmethod);
	    //mark header flag
	    System.out.println("ContVisiting"+ q);
	    header=true;
	    clonevisit.reset(nhm,q.getFactory().tempFactory(),false);
	    System.out.println("Reset clone visitor");
	    copy(q,-1);
	    System.out.println("Finished copying");
	    addCode(nhm, clonevisit.getCode());
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
					 new HClass[] {q.method().getReturnType().isPrimitive()?q.method().getReturnType():linker.forName("java.lang.Object")});
	    HMethod exception=
		hclass.getDeclaredMethod("exception",
					 new HClass[] {throwable});

	    //Resume method
	    //have to reset state in visitor class
	    clonevisit.reset(resume,q.getFactory().tempFactory(), true);
	    copy(q,0);
	    System.out.println("Finished resume copying");
	    //addEdges should add appropriate headers
	    addCode(resume, clonevisit.getCode());
	    //Exception method
	    clonevisit.reset(exception, q.getFactory().tempFactory(), true);
	    copy(q,1);
	    System.out.println("Finished exception copying");
	    //addEdges should add appropriate headers
	    addCode(exception, clonevisit.getCode());
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
    static HMethod makeAsync(Map old2new, HMethod original,
			      CachingCodeFactory ucf, Linker linker)
    {
	//	final HMethod original = blocking.method();
	HClass originalClass = original.getDeclaringClass();
	HClassMutator originalMutator=originalClass.getMutator();
	Util.assert(originalMutator!=null);
  
	// use the return type of the original HMethod to get the String 
	// prefix for the type of Continuation we want as the new return type
	final String pref = ContBuilder.getPrefix(original.getReturnType());

	// get the return type for the replacement HMethod
	final HClass newReturnType = linker.forName
	    ("harpoon.Analysis.ContBuilder." + pref + "Continuation");

	// find a unique name for the replacement HMethod
	String methodNamePrefix = original.getName() + "_Async";
	String newMethodName = methodNamePrefix;
	boolean threadrun=false;
	if (linker.forName("java.lang.Runnable").
	    isSuperinterfaceOf(originalClass)&&
	    originalClass.getMethod("run",new HClass[0]).equals(original)) {
	    try {
		newMethodName = methodNamePrefix;
		originalClass.getDeclaredMethod(newMethodName, 
					   original.getParameterTypes());
		if (originalClass.getName().compareTo("java.lang.Thread")!=0)
		    throw new RuntimeException("Name collision with run_Async method");
		else
		    threadrun=true;
	    } catch (NoSuchMethodError e) {
	    }
	} else {
	    try {
		newMethodName = methodNamePrefix + "$$$"; 
		originalClass.getDeclaredMethod(newMethodName, 
					original.getParameterTypes());
		throw new RuntimeException("Name collision with "+newMethodName+ " method");
	    } catch (NoSuchMethodError e) {
	    }
	}
	// create replacement method
	HMethod replacement=null;
	if (!threadrun) {
	    replacement=originalMutator.addDeclaredMethod(newMethodName, 
							  original.getParameterTypes(),
							  newReturnType);
	    HMethodMutator rmutator=replacement.getMutator();
	    rmutator.setExceptionTypes(original.getExceptionTypes());
	    rmutator.setModifiers(original.getModifiers());
	    rmutator.setParameterNames(original.getParameterNames());
	    rmutator.setSynthetic(original.isSynthetic());
	} else {
	    replacement=originalClass.getDeclaredMethod(newMethodName,
							original.getParameterTypes());
	    HMethodMutator rmutator=replacement.getMutator();
	    rmutator.setReturnType(newReturnType);
	}

	old2new.put(original, replacement);
	return replacement;
    }

    // creates the HClass and constructor for the continuation
    static HClass createContinuation(HMethod blocking, CALL callsite,
				      CachingCodeFactory ucf, Linker linker) 
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
	contMutator.addInterface(inter);

	HMethod hmethods[]=template.getDeclaredMethods();
	for(int i=0;i<hmethods.length;i++) {
	    try {
		HMethod nhm=continuationClass
		    .getDeclaredMethod(hmethods[i].getName(),
				       hmethods[i].getDescriptor());
		HCode hchc = ((Code)ucf.convert(hmethods[i])).clone(nhm);
		(new ChangingVisitor(template,continuationClass))
		    .reName((Quad)hchc.getRootElement());
		ucf.put(nhm, hchc);
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
