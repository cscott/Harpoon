// AsyncCode.java, created Thu Nov 11 15:17:54 1999 by kkz
// Copyright (C) 1999 Karen K. Zee <kkzee@alum.mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.EventDriven;

import harpoon.Analysis.Quads.Unreachable;
import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HCode;
import harpoon.ClassFile.HConstructor;
import harpoon.ClassFile.HMethod;
import harpoon.IR.Quads.CALL;
import harpoon.IR.Quads.Code;
import harpoon.IR.Quads.Edge;
import harpoon.IR.Quads.FOOTER;
import harpoon.IR.Quads.HEADER;
import harpoon.IR.Quads.NEW;
import harpoon.IR.Quads.Quad;
import harpoon.IR.Quads.RETURN;
import harpoon.Temp.Temp;
import harpoon.Temp.TempFactory;
import harpoon.Util.Util;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * <code>AsyncCode</code>
 * 
 * @author Karen K. Zee <kkzee@alum.mit.edu>
 * @version $Id: AsyncCode.java,v 1.1.2.3 1999-11-19 23:52:26 bdemsky Exp $
 */
public class AsyncCode extends Code {

    /** Creates a <code>AsyncCode</code>. */
    public AsyncCode(HMethod parent, HCode hc, CALL c, HMethod toCall,
		     HClass env, HClass cont, Temp[] liveout, Set toSwop) {
	super(parent, null);
	this.quads = buildCode(hc, c, toCall, env, cont, liveout, toSwop);
    }

    private AsyncCode(HMethod parent) {
	super(parent, null);
    }

    /** Clone this code representation.  The clone has its own copy of
     *  the quad graph. 
     */
    public HCode clone(HMethod newMethod) {
	AsyncCode ac = new AsyncCode(newMethod);
	ac.quads = Quad.clone(ac.qf, this.quads);
	return ac;
    }

    /**
     * Return the name of this code view.
     * @return the name of the <code>parent</code>'s code view.
     */
    public String getName() {
	return harpoon.IR.Quads.QuadNoSSA.codename;
    }

    private Quad buildCode(HCode hc, CALL c, HMethod toCall, HClass env,
			   HClass cont, Temp[] liveout, Set toSwop) {
	Quad root = (Quad)hc.getRootElement();
	System.out.println("Entering AsyncCode.buildCode()");
	Object[] maps = Quad.cloneMaps(this.qf, root);
	Map quadmap = (Map)maps[0];

	System.out.println("AsyncCode.buildCode() 1");

	TempFactory tf = this.qf.tempFactory();

	// swop out all the calls to Socket.getInputStream and replace
	// with calls to Socket.getAsyncInputStream:
	final HMethod gais = 
	    HClass.forName("java.net.Socket").getDeclaredMethod
	    ("getAsyncInputStream", new HClass[0]);
	for (Iterator i=toSwop.iterator(); i.hasNext(); ) {
	    CALL cts = (CALL)quadmap.get(i.next()); // call to swop
	    CALL replacement = new CALL(this.qf, cts, gais, cts.params(),
					cts.retval(), cts.retex(), 
					cts.isVirtual(), cts.isTailCall(),
					new Temp[0]);
	    // hook up all incoming edges
	    Edge[] pe = cts.prevEdge();
	    for (int j=0; j<pe.length; j++) {
		Quad.addEdge((Quad)pe[j].from(), pe[j].which_succ(), 
			     replacement, pe[j].which_pred());
	    }

	    Edge[] ne = cts.nextEdge();
	    for (int j=0; j<ne.length; j++) {
		Quad.addEdge(replacement, ne[j].which_succ(),
			     (Quad)ne[j].to(), ne[j].which_pred());
	    }
	}
       
	System.out.println("AsyncCode.buildCode() 2");

	CALL cc = (CALL)quadmap.get(c); // cloned CALL

	System.out.println("AsyncCode.buildCode() 3");

	// we want to remove this cloned CALL and replace it
	// with a call to the asynchronous version of the method
	Temp async = new Temp(tf);
	CALL nc = new CALL(this.qf, cc, toCall, cc.params(), async,
			   new Temp(tf), true, false, new Temp[0]);

	System.out.println("AsyncCode.buildCode() 4");

	// hook up all incoming edges
	Edge[] e = cc.prevEdge();
	for (int j=0; j<e.length; j++) {
	    Quad.addEdge((Quad)e[j].from(), e[j].which_succ(), 
			 nc, e[j].which_pred());
	}

	System.out.println("AsyncCode.buildCode() 5");

	Quad prev = nc;
	Quad curr = null;

	System.out.println("AsyncCode.buildCode() 6");

	// create new environment
	Temp newenv = new Temp(tf);
	curr = new NEW(this.qf, cc, newenv, env);
	Quad.addEdge(prev, 0, curr, 0);
	prev = curr;

	// create params (need to add receiver object)
	Temp[] params = new Temp[liveout.length+1];
	params[0] = newenv;
	for (int i=0;i<liveout.length;i++) {
	    params[i+1]=liveout[i];
	}

	// call constructor
	Util.assert(env.getConstructors().length == 1,
		    "AsyncCode.buildCode(): " + env.getConstructors().length +
		    " constructor(s) found for environment.");

	// since this is a call to the constructor, we mark it as not virtual
	curr = new CALL(this.qf, cc, env.getConstructors()[0], 
				   params, null, null, false, 
				   false, new Temp[0]);
	Quad.addEdge(prev, 0, curr, 0);
	prev = curr;

	System.out.println("AsyncCode.buildCode() 7");

	// create new continuation
	Temp newcont = new Temp(tf);
	curr = new NEW(this.qf, cc, newcont, cont);
	Quad.addEdge(prev, 0, curr, 0);
	prev = curr;

	// call constructor
	Util.assert(cont.getConstructors().length == 1,
		    "AsyncCode.buildCode(): " + cont.getConstructors().length +
		    " constructor(s) found for continuation.");

	// call to constructor is not virtual
	curr = new CALL(this.qf, cc, cont.getConstructors()[0],
			new Temp[] {newcont, newenv}, null, null,
			false, false, new Temp[0]);
	Quad.addEdge(prev, 0, curr, 0);
	prev = curr;

	System.out.println("AsyncCode.buildCode() 8");

	System.out.println("Attempting Access to "+toCall.getReturnType()+" "+
			   cont);

	//BCD start
	HClass[] interfaces=cont.getInterfaces();
	for (int cci=0;cci<interfaces.length;cci++)
	    System.out.println("implements " + interfaces[cci]);
	//BCD stop
	if (cont.getSuperclass()!=null)
	    System.out.println("super "+cont.getSuperclass());

	// setNext(<continuation>);

	HMethod setnextmethod=null;
	HMethod[] allMethods=toCall.getReturnType().getMethods();
	for(int sMethod=0;sMethod<allMethods.length;sMethod++)
	    if (allMethods[sMethod].getName().compareTo("setNext")==0)
		//We found a possible method
		if (allMethods[sMethod].getParameterTypes().length==1) {
		    HClass param1=allMethods[sMethod].getParameterTypes()[0];
		    if (param1.isAssignableFrom(cont)) {
			setnextmethod=allMethods[sMethod];
			break;
		    }
		}
	Util.assert(setnextmethod!=null,"no setNext method found");

	//HMethod setnextmethod = 
	//    toCall.getReturnType().getMethod("setNext", 
	//				     new HClass[] {cont});
	// this is a tail call, but that's not supported yet,
	// so we mark it as not a tail call.
	curr = new CALL(this.qf, cc, setnextmethod, 
				  new Temp[] {async, newcont}, null, 
				  null, true, false, new Temp[0]);
	Quad.addEdge(prev, 0, curr, 0);
	prev = curr;
	
	// return(<continuation>);
	curr = new RETURN(this.qf, cc, newcont);
	Quad.addEdge(prev, 0, curr, 0);
	prev = curr;

	System.out.println("AsyncCode.buildCode() 9");

	// find FOOTER
	Quad q = null;
	for (Iterator i=hc.getElementsI(); i.hasNext(); ) {
	    q = (Quad)i.next();
	    if (q instanceof FOOTER)
		break;
	}
	FOOTER f = (FOOTER)quadmap.get(q); // cloned FOOTER
	Quad.addEdge(prev, 0, f, f.arity());

	System.out.println("AsyncCode.buildCode() 10");

       	HEADER h = (HEADER)quadmap.get(root); // cloned HEADER
	Unreachable.prune(h);
	System.out.println("Leaving AsyncCode.buildCode()");
	return h;
    }
}
