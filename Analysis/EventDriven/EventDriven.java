// EventDriven.java, created Fri Nov 12 14:03:59 1999 by kkz
// Copyright (C) 1999 Karen K. Zee <kkzee@alum.mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.EventDriven;

import harpoon.Analysis.ClassHierarchy;
import harpoon.Analysis.Quads.CallGraph;

import harpoon.Analysis.MetaMethods.MetaCallGraph;

import harpoon.ClassFile.CachingCodeFactory;
import harpoon.ClassFile.HCodeFactory;
import harpoon.ClassFile.HCode;
import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HMethod;
import harpoon.ClassFile.Linker;
import harpoon.IR.Quads.METHOD;
import harpoon.IR.Quads.HEADER;
import harpoon.IR.Quads.Quad;
import harpoon.IR.Quads.QuadSSI;
import harpoon.Temp.Temp;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import harpoon.Util.WorkSet;
/**
 * <code>EventDriven</code>
 * 
 * @author Karen K. Zee <kkzee@alum.mit.edu>
 * @version $Id: EventDriven.java,v 1.1.2.9 2000-03-21 20:57:24 bdemsky Exp $
 */
public class EventDriven {
    protected final CachingCodeFactory ucf;
    protected final HCode hc;
    protected final ClassHierarchy ch;
    protected Map classmap;
    protected final Linker linker;
    protected boolean optimistic;

    /** Creates a <code>EventDriven</code>. The <code>CachingCodeFactory</code>
     *  needs to have been created from a <code>QuadSSI</code> that contains
     *  type information.
     *
     *  <code>HCode</code> needs to be the <code>HCode</code> for main.
     */
    public EventDriven(CachingCodeFactory ucf, HCode hc, ClassHierarchy ch, Linker linker, boolean optimistic) {
        this.ucf = ucf;
	this.hc = hc;
	this.ch = ch;
	this.linker=linker;
	this.optimistic=optimistic;
    }

    /** Returns the converted main
     */
    public HMethod convert(MetaCallGraph mcg) {
	// Clone the class that main was in, and replace it
	HMethod oldmain=hc.getMethod();
	HClass origClass = oldmain.getDeclaringClass();


//  	CallGraph cg=new CallGraph(ch, ucf);
//  	WorkSet todo=new WorkSet();
//  	WorkSet done=new WorkSet();
//  	todo.add(hc.getMethod());
//  	while(!todo.isEmpty()) {
//  	    HMethod hm=(HMethod)todo.pop();
//  	    done.add(hm);
//  	    System.out.println(hm);
//  	    System.out.println("-------------------------------------");
//  	    HMethod[] hma=cg.calls(hm);
//  	    for (int i=0;i<hma.length;i++) {
//  		System.out.println("calls "+hma[i]);
//  		if (!done.contains(hma[i]))
//  		    todo.add(hma[i]);
//  	    }
//  	}

	ToAsync as = new ToAsync(ucf,hc ,ch ,linker,optimistic,mcg);
	// transform main to mainAsync
	HMethod newmain = as.transform();

	if (newmain == null) System.err.println("EventDriven.convert(): " +
						"newmain is null");

	Temp[] params = null;
	HEADER header=(HEADER) (this.hc.getRootElement());
	METHOD method=(METHOD) (header.next(1));
	params=method.params();

	ucf.put(oldmain, new EventDrivenCode(oldmain, newmain, params, linker));
	return oldmain;
    }
}




