// EventDriven.java, created Fri Nov 12 14:03:59 1999 by kkz
// Copyright (C) 1999 Karen K. Zee <kkzee@alum.mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.EventDriven;

import harpoon.Analysis.ClassHierarchy;
import harpoon.ClassFile.HCode;
import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HMethod;
import harpoon.ClassFile.Linker;
import harpoon.ClassFile.UpdateCodeFactory;
import harpoon.IR.Quads.METHOD;
import harpoon.IR.Quads.HEADER;
import harpoon.IR.Quads.Quad;
import harpoon.IR.Quads.QuadNoSSA;
import harpoon.Temp.Temp;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * <code>EventDriven</code>
 * 
 * @author Karen K. Zee <kkzee@alum.mit.edu>
 * @version $Id: EventDriven.java,v 1.1.2.5 2000-01-13 23:51:10 bdemsky Exp $
 */
public class EventDriven {
    protected final UpdateCodeFactory ucf;
    protected final HCode hc;
    protected final ClassHierarchy ch;
    protected Map classmap;
    protected final Linker linker;

    /** Creates a <code>EventDriven</code>. The <code>UpdateCodeFactory</code>
     *  needs to have been created from a <code>QuadNoSSA</code> that contains
     *  type information.
     *
     *  <code>HCode</code> needs to be the <code>HCode</code> for main.
     */
    public EventDriven(UpdateCodeFactory ucf, HCode hc, ClassHierarchy ch, Linker linker) {
        this.ucf = ucf;
	this.hc = hc;
	this.ch = ch;
	this.linker=linker;
    }

    /** Returns the converted main
     */
    public HMethod convert() {
	// Clone the class that main was in, and replace it
	HMethod oldmain=this.hc.getMethod();
	HClass origClass = oldmain.getDeclaringClass();


	ToAsync as = new ToAsync(this.ucf, this.hc, this.ch, linker);
	// transform main to mainAsync
	HMethod newmain = as.transform();

	if (newmain == null) System.err.println("EventDriven.convert(): " +
						"newmain is null");

	Temp[] params = null;
	HEADER header=(HEADER) (this.hc.getRootElement());
	METHOD method=(METHOD) (header.next(1));
	params=method.params();

	this.ucf.update(oldmain, new EventDrivenCode(oldmain, newmain, params, linker));
	return oldmain;
    }
}


