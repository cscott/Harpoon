// EventDriven.java, created Fri Nov 12 14:03:59 1999 by kkz
// Copyright (C) 1999 Karen K. Zee <kkzee@alum.mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.EventDriven;

import harpoon.Analysis.ClassHierarchy;
import harpoon.ClassFile.HCode;
import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HMethod;
import harpoon.ClassFile.HMethodSyn;
import harpoon.ClassFile.HClassSyn;
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
 * @version $Id: EventDriven.java,v 1.1.2.4 2000-01-02 22:15:19 bdemsky Exp $
 */
public class EventDriven {
    protected final UpdateCodeFactory ucf;
    protected final HCode hc;
    protected final ClassHierarchy ch;
    protected Map classmap;

    /** Creates a <code>EventDriven</code>. The <code>UpdateCodeFactory</code>
     *  needs to have been created from a <code>QuadNoSSA</code> that contains
     *  type information.
     *
     *  <code>HCode</code> needs to be the <code>HCode</code> for main.
     */
    public EventDriven(UpdateCodeFactory ucf, HCode hc, ClassHierarchy ch) {
        this.ucf = ucf;
	this.hc = hc;
	this.ch = ch;
	this.classmap=new HashMap();
    }

    /** Returns the converted main
     */
    public HMethod convert() {
	// Clone the class that main was in, and replace it
    
	HClass origClass = this.hc.getMethod().getDeclaringClass();
	HClassSyn hcs = 
	    new HClassSyn(origClass, true);

	classmap.put(origClass, hcs);

	// clone methods
	HMethod[] toClone = 
	    this.hc.getMethod().getDeclaringClass().getDeclaredMethods();

	HMethod copyOfMain = null;
	for (int i=0; i<toClone.length; i++) {
	    HMethod curr = 
		hcs.getDeclaredMethod(toClone[i].getName(),
				      toClone[i].getParameterTypes());
	    this.ucf.update(curr, 
			    ((QuadNoSSA)ucf.convert(toClone[i])).clone(curr));
	    if (toClone[i].compareTo(this.hc.getMethod()) == 0)
		copyOfMain = curr;
	}

	// Clone main and replace it
	HMethodSyn hms = new HMethodSyn(hcs, copyOfMain, true);
	ToAsync as = new ToAsync(this.ucf, this.hc, this.ch, this.classmap);
	// transform main to mainAsync
	HMethod newmain = as.transform();

	if (newmain == null) System.err.println("EventDriven.convert(): " +
						"newmain is null");

	Temp[] params = null;
	HEADER header=(HEADER) (this.hc.getRootElement());
	METHOD method=(METHOD) (header.next(1));
	params=method.params();

	this.ucf.update(hms, new EventDrivenCode(hms, newmain, params));
	return hms;
    }
}
