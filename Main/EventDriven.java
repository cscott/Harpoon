// EventDriven.java, created Tue Oct 19 21:14:57 1999 by kkz
// Copyright (C) 1999 Karen K. Zee <kkzee@alum.mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Main;

import harpoon.Analysis.ClassHierarchy;
import harpoon.Analysis.EventDriven.ToAsync;
import harpoon.Analysis.Quads.QuadClassHierarchy;
import harpoon.ClassFile.CachingCodeFactory;
import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HCode;
import harpoon.ClassFile.HCodeFactory;
import harpoon.ClassFile.HMethod;
import harpoon.ClassFile.UpdateCodeFactory;

import java.util.Collections;
import java.util.Iterator;
import java.util.Set;

/**
 * <code>EventDriven</code>
 * 
 * @author Karen K. Zee <kkzee@alum.mit.edu>
 * @version $Id: EventDriven.java,v 1.1.2.1 1999-11-12 05:18:43 kkz Exp $
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

        HCodeFactory ccf =
            new CachingCodeFactory(harpoon.IR.Quads.QuadNoSSA.codeFactory());
	UpdateCodeFactory hcf = 
	    new UpdateCodeFactory(ccf);
        ClassHierarchy ch = 
	    new QuadClassHierarchy(Collections.singleton(m), hcf);
	HCode hc = hcf.convert(m);

	ToAsync as = new ToAsync(hcf, hc, ch);
	
	HCode converted = hcf.convert(as.transform());
	if (converted != null) converted.print(out);
    }
}

