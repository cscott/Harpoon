// ContCodeNoSSA.java, created Wed Nov  3 21:43:30 1999 by kkz
// Copyright (C) 1999 Karen K. Zee <kkz@alum.mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.EventDriven;

import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HCode;
import harpoon.ClassFile.HField;
import harpoon.ClassFile.HMethod;
import harpoon.IR.Quads.Quad;
import harpoon.IR.Quads.QuadSSI;
import harpoon.IR.Quads.QuadFactory;
import harpoon.IR.Quads.RSSxToNoSSA;
import harpoon.Temp.Temp;
import harpoon.Temp.TempFactory;
import harpoon.Util.Util;

import java.util.Iterator;
import java.util.Map;

/**
 * <code>ContCodeNoSSA</code> builds the code for a <code>Continuation</code>
 * using <code>quad-no-ssa</code> <code>HCode</code>.
 * 
 * @author Karen K. Zee <kkz@alum.mit.edu>
 * @version $Id: ContCodeNoSSA.java,v 1.1.2.6 2001-09-25 22:55:47 cananian Exp $
 */
public class ContCodeNoSSA extends harpoon.IR.Quads.QuadNoSSA {

    /** Creates a <code>ContCodeNoSSA</code> for an <code>HMethod</code> using
     *  the <code>HCode</code> from which we want to build the continuation
     *  and the <code>CALL</code> at which we want the continuation built.
     *  The <code>HCode</code> must be <code>quad-no-ssa</code>.
     *
     */
    public ContCodeNoSSA(HMethod parent) {
        super(parent, null);
    }

    public ContCodeNoSSA(QuadSSI qsa) { 
	super(qsa.getMethod(),null);
        RSSxToNoSSA translate = new RSSxToNoSSA(this.qf, qsa);
        this.quads=translate.getQuads();
	this.setAllocationInformation(translate.getAllocationInfo());
    }
    
    /**
     * Return the name of this code view.
     * @return the name of the <code>parent</code>'s code view.
     */
    public String getName() {
	return harpoon.IR.Quads.QuadNoSSA.codename;
    }

    public void quadSet(Quad q) {
        this.quads=q;
    }

    public QuadFactory getFactory() {
        return qf;
    }
}
