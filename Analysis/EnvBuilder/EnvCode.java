// EnvCode.java, created Sat Oct 30 20:25:43 1999 by kkz
// Copyright (C) 1999 Karen K. Zee <kkzee@alum.mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.EnvBuilder;

import harpoon.ClassFile.HCode;
import harpoon.ClassFile.HField;
import harpoon.ClassFile.HMethod;
import harpoon.IR.Quads.Code;
import harpoon.IR.Quads.FOOTER;
import harpoon.IR.Quads.HEADER;
import harpoon.IR.Quads.METHOD;
import harpoon.IR.Quads.RETURN;
import harpoon.IR.Quads.SET;
import harpoon.IR.Quads.Quad;
import harpoon.Temp.Temp;
import harpoon.Temp.TempFactory;
import harpoon.Util.Util;

/**
 * <code>EnvCode</code>
 * 
 * @author Karen K. Zee <kkzee@alum.mit.edu>
 * @version $Id: EnvCode.java,v 1.1.2.2 1999-11-12 05:18:39 kkz Exp $
 */
public class EnvCode extends Code {

    /** Creates a <code>EnvCode</code>. */
    public EnvCode(HMethod parent, HField[] fields) {
        super(parent, null);
	this.quads = buildCode(fields);
    }

    private EnvCode(HMethod parent) {
	super(parent, null);
    }

    /** Clone this code representation.  The clone has its own copy of
     *  the quad graph. 
     */
    public HCode clone(HMethod newMethod) {
	EnvCode ec = new EnvCode(newMethod);
	ec.quads = Quad.clone(ec.qf, this.quads);
	return ec;
    }

    /**
     * Return the name of this code view.
     * @return the name of the <code>parent</code>'s code view.
     */
    public String getName() {
	return harpoon.IR.Quads.QuadNoSSA.codename;
    }

    private Quad buildCode(HField[] fields) {
	System.out.println("Entering EnvCode.buildCode()");
	HEADER h = new HEADER(this.qf, null);
	FOOTER f = new FOOTER(this.qf, null, 2);
	Quad.addEdge(h, 0, f, 0);

	TempFactory tf = this.qf.tempFactory();

	Temp[] params = new Temp[fields.length+1];
	for (int i=0; i<params.length; i++) {
	    params[i] = new Temp(tf);
	}

	METHOD m = new METHOD(this.qf, null, params, 1);
	Quad.addEdge(h, 1, m, 0);

	Temp objectref = m.params(0);

	Quad[] quadList = new Quad[fields.length];
	for (int i=0; i < fields.length; i++) {
	    quadList[i] = 
		new SET(this.qf, null, fields[i], objectref, params[i+1]);
	}
	
	Quad.addEdges(quadList);
	Quad.addEdge(m, 0, quadList[0], 0);

	RETURN r = new RETURN(this.qf, null, null);
	Quad.addEdge(quadList[fields.length-1], 0, r, 0);
	Quad.addEdge(r, 0, f, 1);

	System.out.println("Leaving EnvCode.buildCode()");
	return h;
    }
}
