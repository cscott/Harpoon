// EventDriven.java, created Tue Oct 19 21:14:57 1999 by kkz
// Copyright (C) 1999 Karen K. Zee <kkzee@alum.mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Main;

import harpoon.Analysis.ClassHierarchy;
import harpoon.Analysis.Quads.QuadClassHierarchy;
import harpoon.ClassFile.CachingCodeFactory;
import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HCode;
import harpoon.ClassFile.HCodeFactory;
import harpoon.ClassFile.HMethod;
import harpoon.ClassFile.UpdateCodeFactory;
import harpoon.Util.HClassUtil;
import harpoon.Util.WorkSet;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Set;

/**
 * <code>EventDriven</code>
 * 
 * @author Karen K. Zee <kkzee@alum.mit.edu>
 * @version $Id: EventDriven.java,v 1.1.2.2 1999-11-17 00:15:32 kkz Exp $
 */

public abstract class EventDriven extends harpoon.IR.Registration {

    public static final void main(String args[]) {
	java.io.PrintWriter out = new java.io.PrintWriter(System.out, true);
	HMethod m = null;

        if (args.length < 1) {
            System.err.println("Needs class name.");
            return;
        }

	out.println("Class: "+args[0]);

        {
            HClass cls = HClass.forName(args[0]);
            HMethod hm[] = cls.getDeclaredMethods();
            for (int i=0; i<hm.length; i++) {
                if (hm[i].getName().equals("main")) {
                    m = hm[i];
                    break;
                }
	    }
        }

	System.out.println("Doing QuadSSI");
        HCodeFactory ccf =
            new CachingCodeFactory(harpoon.IR.Quads.QuadSSI.codeFactory());
	System.out.println("Doing QuadNoSSA with types");
	ccf = new CachingCodeFactory
	    (harpoon.IR.Quads.QuadNoSSA.codeFactoryWithTypes(ccf));
	System.out.println("Doing UpdatingCodeFactory");
	UpdateCodeFactory hcf = new UpdateCodeFactory(ccf);
	Collection c = new WorkSet();
	c.addAll(harpoon.Backend.Runtime1.Runtime.runtimeCallableMethods());
	c.addAll(knownBlockingMethods());
	c.add(m);
	System.out.println("Getting ClassHierarchy");
        ClassHierarchy ch = new QuadClassHierarchy(c, hcf);
	HCode hc = hcf.convert(m);
	System.out.println("Done w/ set up");

	harpoon.Analysis.EventDriven.EventDriven ed = 
	    new harpoon.Analysis.EventDriven.EventDriven(hcf, hc, ch);

	HCode converted = hcf.convert(ed.convert());
	if (converted != null) converted.print(out);
    }

    private static Collection knownBlockingMethods() {
	final HClass is = HClass.forName("java.io.InputStream");
	final HClass ss = HClass.forName("java.net.ServerSocket");
	final HClass b = HClass.Byte;

	WorkSet w = new WorkSet();
	w.add(is.getDeclaredMethod("read", new HClass[0]));
	w.add(is.getDeclaredMethod("read", new HClass[] 
				   {HClassUtil.arrayClass(b,1)}));
	w.add(is.getDeclaredMethod("read", new HClass[] 
				   {HClassUtil.arrayClass(b, 1),
					HClass.Int, HClass.Int}));
	w.add(ss.getDeclaredMethod("accept", new HClass[0]));
	return w;
    }
}

