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

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * <code>AsyncCode</code>
 * 
 * @author Karen K. Zee <kkzee@alum.mit.edu>
 * @version $Id: AsyncCode.java,v 1.1.2.1 1999-11-12 05:18:40 kkz Exp $
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
	Object[] maps = Quad.cloneMaps(this.qf, root);
	Map quadmap = (Map)maps[0];

	TempFactory tf = this.qf.tempFactory();

	// swop out all the calls to Socket.getInputStream and replace
	// with calls to Socket.getAsyncInputStream:
	final HMethod gais = 
	    HClass.forName("Socket").getDeclaredMethod("getAsyncInputStream",
						       new HClass[0]);
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
       
	CALL cc = (CALL)quadmap.get(c); // cloned CALL

	// we want to remove this cloned CALL and replace it
	// with a call to the asynchronous version of the method
	CALL nc = new CALL(this.qf, cc, toCall, cc.params(), cc.retval(),
			   cc.retex(), true, false, new Temp[0]);

	// hook up all incoming edges
	Edge[] e = cc.prevEdge();
	for (int j=0; j<e.length; j++) {
	    Quad.addEdge((Quad)e[j].from(), e[j].which_succ(), 
			 nc, e[j].which_pred());
	}

	Quad[] quadlist = new Quad[7];
	quadlist[0] = nc;

	// new Continuation(<params>);
	Temp[] argstoinit = new Temp[liveout.length+1];
	Temp newenv = new Temp(tf);
	argstoinit[0] = newenv;
	for (int j=0; j<liveout.length+1; j++) {
	    argstoinit[j+1] = liveout[j];
	}
	quadlist[1] = new NEW(this.qf, cc, newenv, env);
	quadlist[2] = new CALL(this.qf, cc, env.getClassInitializer(), 
			       argstoinit, new Temp(tf), new Temp(tf), true, 
			       false, new Temp[0]);

	// new Continuation(<params>);
	HConstructor contcons = 
	    cont.getConstructor(new HClass[] 
				{HClass.forName("harpoon.Analysis." +
						"ContBuilder.Continuation")});
	Temp newcont = new Temp(tf);
	quadlist[3] = new NEW(this.qf, cc, newcont, cont);
	quadlist[4] = new CALL(this.qf, cc, cont.getClassInitializer(),
			       new Temp[] {newcont, newenv}, new Temp(tf),
			       new Temp(tf), true, false, new Temp[0]);
	
	// setNext(<continuation>);
	HMethod setnextmethod = 
	    toCall.getReturnType().getDeclaredMethod("setNext", 
	       					     new HClass[] {cont});
	quadlist[5] = new CALL(this.qf, cc, setnextmethod, 
				  new Temp[] {newcont}, new Temp(tf),
				  new Temp(tf), true, false, new Temp[0]);
	
	// return(<continuation>);
	quadlist[6] = new RETURN(this.qf, cc, newcont);
	Quad.addEdges(quadlist);

	// find FOOTER
	Quad q = null;
	for (Iterator i=hc.getElementsI(); i.hasNext(); ) {
	    q = (Quad)i.next();
	    if (q instanceof FOOTER)
		break;
	}
	FOOTER f = (FOOTER)quadmap.get(q); // cloned FOOTER
	Quad.addEdge(quadlist[4], 0, f, f.arity());

       	HEADER h = (HEADER)quadmap.get(root); // cloned HEADER
	Unreachable.prune(h);
	return h;
    }
}
