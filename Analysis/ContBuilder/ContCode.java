// ContCode.java, created Wed Nov  3 21:43:30 1999 by kkz
// Copyright (C) 1999 Karen K. Zee <kkzee@alum.mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.ContBuilder;

import harpoon.Analysis.Quads.Unreachable;
import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HCode;
import harpoon.ClassFile.HField;
import harpoon.ClassFile.HMethod;
import harpoon.IR.Quads.CALL;
import harpoon.IR.Quads.Code;
import harpoon.IR.Quads.Edge;
import harpoon.IR.Quads.FOOTER;
import harpoon.IR.Quads.GET;
import harpoon.IR.Quads.HEADER;
import harpoon.IR.Quads.METHOD;
import harpoon.IR.Quads.PHI;
import harpoon.IR.Quads.RETURN;
import harpoon.IR.Quads.THROW;
import harpoon.IR.Quads.TYPECAST;
import harpoon.IR.Quads.Quad;
import harpoon.Temp.Temp;
import harpoon.Temp.TempFactory;
import harpoon.Util.Util;

import java.util.Iterator;
import java.util.Map;

/**
 * <code>ContCode</code> builds the code for a <code>Continuation</code>
 * using <code>quad-no-ssa</code> <code>HCode</code>.
 * 
 * @author Karen K. Zee <kkzee@alum.mit.edu>
 * @version $Id: ContCode.java,v 1.1.2.6 1999-11-22 21:00:35 bdemsky Exp $
 */
public class ContCode extends harpoon.IR.Quads.QuadNoSSA {

    /** Creates a <code>ContCode</code> for an <code>HMethod</code> using
     *  the <code>HCode</code> from which we want to build the continuation
     *  and the <code>CALL</code> at which we want the continuation built.
     *  The <code>HCode</code> must be <code>quad-no-ssa</code>.
     *
     *  <code>liveout</code> is the array of <code>Temp</code>s that we need
     *  to assign from the environment, which is the <code>HField</code>
     *  <code>env</code>. <code>envfields</code> is an array of 
     *  <code>HField</code>s that hold the values of the variables we need
     *  to assign.
     *
     *  <code>i</code> determines whether we are building the code
     *  for resume (0) or for exception (1). <code>resume</code> should be
     *  the resume <code>HMethod</code> if <code>i</code> is (0) or the
     *  exception <code>HMethod</code> if <code>i</code> is (1).
     */
    public ContCode(HMethod parent, HCode hc, CALL c, boolean hasParameter,
		    HMethod resume, Temp[] liveout, HField env,
		    HField[] envfields, int i) {
        super(parent, null);
	Util.assert(liveout.length == envfields.length,
		    "Number of fields in environment must match number of " +
		    "Temps to be assigned: " + liveout.length + " != " + 
		    envfields.length);
	Util.assert((i == 0 || i == 1), "i can only be 0 or 1");
	this.quads = 
	    buildCode(hc, c, hasParameter, resume, liveout, env, envfields, i);
    }

    private ContCode(HMethod parent) {
	super(parent, null);
    }

    /** Clone this code representation.  The clone has its own copy of
     *  the quad graph. 
     */
    public HCode clone(HMethod newMethod) {
	ContCode cc = new ContCode(newMethod);
	cc.quads = Quad.clone(cc.qf, this.quads);
	return cc;
    }

    /**
     * Return the name of this code view.
     * @return the name of the <code>parent</code>'s code view.
     */
    public String getName() {
	return harpoon.IR.Quads.QuadNoSSA.codename;
    }

    private final int RESUME = 0;
    private final int EXCEPTION = 1;
    private Quad buildCode(HCode hc, CALL c, boolean hasParameter, 
			   HMethod resume, Temp[] liveout, HField env, 
			   HField[] envfields, int indicator) {
	System.out.println("Entering ContCode.buildCode()");
	Quad root = (Quad)hc.getRootElement();

	// clone HCode
	Object[] maps = Quad.cloneMaps(this.qf, root);
	Map quadmap = (Map)maps[0];

	// get cloned header
	HEADER h = (HEADER)quadmap.get(root);

	// create new TempFactory to use in new Quads
	TempFactory tf = this.qf.tempFactory();

	// start building things to use in new METHOD
	Temp[] params = null;
	if (hasParameter) {
	    params = new Temp[2];
	    params[1] = (indicator == 0)?c.retval():c.retex();
	} else {
	    params = new Temp[1];
	}
	params[0] = new Temp(tf);

	METHOD m = new METHOD(this.qf, h.next(1), params, 1);
	Quad.addEdge(h, 1, m, 0);

	// do a GET on the Environment field
	Temp e = new Temp(tf);
	GET g = new GET(this.qf, h.next(1), e, env, m.params(0));
	Quad.addEdge(m, 0, g, 0);

	// assign each field in the Environment to the appropriate Temp
	// except for the assignment we want to suppress
	Temp suppress = (indicator == RESUME) ? c.retval() : c.retex();
	Quad prev = g;
	for(int i=0; i<liveout.length; i++) {
	    if (suppress == null || !suppress.equals(liveout[i])) {
		GET ng = new GET(this.qf, h.next(1), liveout[i], 
				 envfields[i], e);
		Quad.addEdge(prev, 0, ng, 0);
		prev = ng;
	    }
	}

	// typecast the argument if necessary
	if (!c.method().getReturnType().isPrimitive()) {
	    TYPECAST tc = new TYPECAST(this.qf, h.next(1), c.retval(), 
				       c.method().getReturnType());
	    Quad.addEdge(prev, 0, tc, 0);
	    prev = tc;
	}

	CALL nc = (CALL)((Map)maps[0]).get(c);

	PHI p = new PHI(this.qf, nc, new Temp[0], 2);

	Quad.addEdge(p, 0, nc.next(indicator), 
		     nc.nextEdge(indicator).which_pred());
	Quad.addEdge(prev, 0, p, 0);
	Quad.addEdge(nc, indicator, p, 1);

	Quad q = null;
	for (Iterator i=hc.getElementsI(); i.hasNext(); ) {
	    q = (Quad)i.next();
	    if (indicator == RESUME) {
		if (q instanceof RETURN) break;
	    } else if (indicator == EXCEPTION) {
		if (q instanceof THROW) break;
	    } else {
		System.err.println("Can't happen in Analysis.ContBuilder." +
				   "ContCode.buildCode");
	    }
	}

	Quad r = (Quad)quadmap.get(q); 
	
	Temp retval;
	CALL rc;
	if (indicator == RESUME) {
	    retval = ((RETURN)r).retval();
	} else {
	    retval = ((THROW)r).throwable();
	}

	// add "next.resume(retval)" or "next.exception(retval)" CALL
	Temp exc=new Temp(tf);
	if (retval != null) {
	    rc = new CALL(this.qf, r, resume, 
			  new Temp[] {new Temp(tf), retval}, null, exc, 
			  true, false, new Temp[0]);
	} else {
	    rc = new CALL(this.qf, r, resume, new Temp[] {new Temp(tf)}, 
	    null, exc, true, false, new Temp[0]);
	}

	Edge[] prevs = r.prevEdge();

	for (int i=0; i<prevs.length; i++) {
	    Quad.addEdge((Quad)prevs[i].from(), prevs[i].which_succ(), 
			 rc, prevs[i].which_pred());
	}

	THROW throwq=new THROW(this.qf, nc, exc);
	Quad.addEdge(rc, 1, throwq, 0);

	RETURN nr = new RETURN(this.qf, nc, null);



	Quad.addEdge(rc, 0, nr, 0);
	Quad.addEdge(nr, 0, r.next(0), r.nextEdge(0).which_pred());
	//add throw quad to footer
	FOOTER footer=(FOOTER) h.next(0);
	footer.attach(throwq,0);
      
	Unreachable.prune(h);
	
	System.out.println("Leaving ContCode.buildCode()");
	return h;
    }    
}


