// EnvCode.java, created Sat Oct 30 20:25:43 1999 by kkz
// Copyright (C) 1999 Karen K. Zee <kkz@alum.mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.EnvBuilder;

import harpoon.ClassFile.HCode;
import harpoon.ClassFile.HCodeAndMaps;
import harpoon.ClassFile.HField;
import harpoon.ClassFile.HMethod;
import harpoon.ClassFile.HClass;
import harpoon.ClassFile.Linker;
import harpoon.IR.Quads.Code;
import harpoon.IR.Quads.FOOTER;
import harpoon.IR.Quads.HEADER;
import harpoon.IR.Quads.METHOD;
import harpoon.IR.Quads.RETURN;
import harpoon.IR.Quads.SET;
import harpoon.IR.Quads.THROW;
import harpoon.IR.Quads.PHI;
import harpoon.IR.Quads.CALL;
import harpoon.IR.Quads.Quad;
import harpoon.Temp.Temp;
import harpoon.Temp.TempFactory;
import harpoon.Util.Util;

/**
 * <code>EnvCode</code>
 * 
 * @author Karen K. Zee <kkz@alum.mit.edu>
 * @version $Id: EnvCode.java,v 1.1.2.8 2001-06-18 20:46:03 cananian Exp $
 */
public class EnvCode extends harpoon.IR.Quads.QuadSSI {

    /** Creates a <code>EnvCode</code>. */
    public EnvCode(HMethod parent, HField[] fields, Linker linker) {
        super(parent, null);
	this.quads = buildCode(fields,linker);
    }

    private EnvCode(HMethod parent) {
	super(parent, null);
    }

    /** Clone this code representation.  The clone has its own copy of
     *  the quad graph. 
     */
    public HCodeAndMaps clone(HMethod newMethod) {
	return cloneHelper(new EnvCode(newMethod));
    }

    /**
     * Return the name of this code view.
     * @return the name of the <code>parent</code>'s code view.
     */
    public String getName() {
	return harpoon.IR.Quads.QuadSSI.codename;
    }

    private Quad buildCode(HField[] fields, Linker linker) {
	System.out.println("Entering EnvCode.buildCode()");
	HEADER h = new HEADER(qf, null);
	FOOTER f = new FOOTER(qf, null,((linker!=null)?3:2));
	Quad.addEdge(h, 0, f, 0);

	TempFactory tf = qf.tempFactory();

	Temp[] params = new Temp[fields.length+1];
	Temp[][] dst = new Temp[fields.length+1][2];
	
	for (int i=0; i<params.length; i++) {
	    params[i] = new Temp(tf);
	    if (linker!=null) {
		dst[i][0]=new Temp(tf);
		dst[i][1]=new Temp(tf);
	    }
	    else dst[i][0]=params[i];
	}

	Quad m = new METHOD(qf, null, params, 1);
	Quad.addEdge(h, 1, m, 0);
	Temp retex=new Temp(tf);

	if (linker!=null) {
	    CALL c =new CALL(qf,null,linker.forName("java.lang.Object").getConstructor(new HClass[0]), 
			     new Temp[] {params[0]},null,retex,false,false,dst,params);
	    THROW th=new THROW(qf,null,retex);
	    Quad.addEdge(m,0,c,0);
	    
	    Quad.addEdge(c,1,th,0);
	    Quad.addEdge(th,0,f,2);
	    m=c;
	}

	Temp objectref = dst[0][0];

	Quad[] quadList = new Quad[fields.length];
	for (int i=0; i < fields.length; i++) {
	    quadList[i] = 
		new SET(qf, null, fields[i], objectref, dst[i+1][0]);
	}
	if (fields.length>0) {
	    Quad.addEdges(quadList);
	    Quad.addEdge(m, 0, quadList[0], 0);
	}

	RETURN r = new RETURN(qf, null, null);
	if (fields.length>0) {
	    Quad.addEdge(quadList[fields.length-1], 0, r, 0);
	} else
	    Quad.addEdge(m, 0, r, 0);
	
	Quad.addEdge(r, 0, f, 1);

	System.out.println("Leaving EnvCode.buildCode()");
	return h;
    }
}
