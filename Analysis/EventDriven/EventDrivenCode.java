// EventDrivenCode.java, created Fri Nov 12 14:40:55 1999 by kkz
// Copyright (C) 1999 Karen K. Zee <kkz@alum.mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.EventDriven;

import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HCode;
import harpoon.ClassFile.HCodeAndMaps;
import harpoon.ClassFile.HMethod;
import harpoon.ClassFile.Loader;
import harpoon.ClassFile.Linker;
import harpoon.IR.Quads.CALL;
import harpoon.IR.Quads.Code;
import harpoon.IR.Quads.FOOTER;
import harpoon.IR.Quads.HEADER;
import harpoon.IR.Quads.METHOD;
import harpoon.IR.Quads.THROW;
import harpoon.IR.Quads.Quad;
import harpoon.IR.Quads.RETURN;
import harpoon.Temp.Temp;
import harpoon.Temp.TempFactory;

/**
 * <code>EventDrivenCode</code>
 *
 * @author Karen K. Zee <kkz@alum.mit.edu>
 * @version $Id: EventDrivenCode.java,v 1.2 2002-02-25 20:57:01 cananian Exp $
 */
public class EventDrivenCode extends harpoon.IR.Quads.QuadSSI {
    /** Creates a <code>EventDrivenCode</code>. */
    public EventDrivenCode(HMethod parent, HMethod newmain, Temp[] params,
			   Linker linker) {
        super(parent, null);
	this.quads = buildCode(newmain, params, linker);
    }

    private EventDrivenCode(HMethod parent) {
	super(parent, null);
    }

    /** Clone this code representation.  The clone has its own copy of
     *  the quad graph. 
     */
    public HCodeAndMaps clone(HMethod newMethod) {
	return cloneHelper(new EventDrivenCode(newMethod));
    }

    /**
     * Return the name of this code view.
     * @return the name of the <code>parent</code>'s code view.
     */
    public String getName() {
	return harpoon.IR.Quads.QuadSSI.codename;
    }

    private Quad buildCode(HMethod newmain, Temp[] params, Linker linker) {
	TempFactory tf=qf.tempFactory();
	System.out.println("Entering EventDrivenCode.buildCode()");
	HEADER h = new HEADER(qf, null);
	FOOTER f = new FOOTER(qf, null, 4);
	Quad.addEdge(h, 0, f, 0);
	Temp[] params2=new Temp[params.length];
	for (int i=0;i<params.length;i++)
	    params2[i]=new Temp(tf,params[i].name());
	METHOD m = new METHOD(qf, null, params2, 1);
	Quad.addEdge(h, 1, m, 0);

	if (newmain == null) System.out.println("newmain is null");

	// call to new main method (mainAsync)
	Temp t=new Temp(tf);
	Temp exc=new Temp(tf);

	CALL c1 = new CALL(qf, null, newmain, params2, t, exc, true,
			   false, new Temp[0]);
	THROW throwq=new THROW(qf, null, exc);
	Quad.addEdge(c1, 1, throwq,0);
	Quad.addEdge(m, 0, c1, 0);

	// call to scheduler
	HMethod schloop = null;
	try {
	    final HClass sch = 
		linker.forName("harpoon.Analysis.ContBuilder.Scheduler");
	    schloop = sch.getDeclaredMethod("loop", new HClass[0]);
	} catch (Exception e) {
	    System.err.println
		("Cannot find harpoon.Analysis.EventDriven.Scheduler");
	}
	

	Temp exc2=new Temp(tf);
	CALL c2 = new CALL(qf, null, schloop, new Temp[0], null, exc2, 
			   true, false, new Temp[0]);
	THROW throwq2=new THROW(qf, null, exc2);
	Quad.addEdge(c1, 0, c2, 0);
	Quad.addEdge(c2, 1, throwq2,0);


	RETURN r = new RETURN(qf, null, null);
	Quad.addEdge(c2,0,r,0);
	Quad.addEdge(r, 0, f, 1);
	Quad.addEdge(throwq,0,f,2);
	Quad.addEdge(throwq2,0, f,3);
	System.out.println("Leaving EventDrivenCode.buildCode()");
	return h;
    }    
}
