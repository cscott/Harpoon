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
import harpoon.ClassFile.Linker;
import harpoon.ClassFile.Loader;
import harpoon.ClassFile.Relinker;
import harpoon.ClassFile.UpdateCodeFactory;
import harpoon.Util.HClassUtil;
import harpoon.Util.WorkSet;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Set;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.PrintStream;
import java.io.FileOutputStream;
import harpoon.IR.Jasmin.Jasmin;

/**
 * <code>EventDriven</code>
 * 
 * @author Karen K. Zee <kkzee@alum.mit.edu>
 * @version $Id: EventDriven.java,v 1.1.2.4 2000-01-13 23:48:17 cananian Exp $
 */

public abstract class EventDriven extends harpoon.IR.Registration {
    public static final Linker linker = new Relinker(Loader.systemLinker);

    public static final void main(String args[]) {
	java.io.PrintWriter out = new java.io.PrintWriter(System.out, true);
	HMethod m = null;

        if (args.length < 1) {
            System.err.println("Needs class name.");
            return;
        }

	out.println("Class: "+args[0]);

        {
            HClass cls = linker.forName(args[0]);
            HMethod hm[] = cls.getDeclaredMethods();
            for (int i=0; i<hm.length; i++) {
                if (hm[i].getName().equals("main")) {
                    m = hm[i];
                    break;
                }
	    }
        }

	System.out.println("Doing QuadSSI");
        HCodeFactory ccf = harpoon.IR.Quads.QuadSSI.codeFactory();
	System.out.println("Doing QuadNoSSA with types");
	ccf = 
	    harpoon.IR.Quads.QuadNoSSA.codeFactoryWithTypes(ccf);
	System.out.println("Doing UpdatingCodeFactory");
	UpdateCodeFactory hcf = new UpdateCodeFactory(ccf);

	Collection c = new WorkSet();
	c.addAll(harpoon.Backend.Runtime1.Runtime.runtimeCallableMethods
		 (linker));
	c.addAll(knownBlockingMethods());
	c.add(m);


	System.out.println("Getting ClassHierarchy");
        ClassHierarchy ch = new QuadClassHierarchy(linker, c, hcf);
	HCode hc = hcf.convert(m);
	System.out.println("Done w/ set up");

	harpoon.Analysis.EventDriven.EventDriven ed = 
	    new harpoon.Analysis.EventDriven.EventDriven(hcf, hc, ch);
	
	HMethod mconverted=ed.convert();

	System.out.println("Converted");
	System.out.println("Setting up HCodeFactories");
	//	HCodeFactory hcf2=hcf;
		HCodeFactory hcf2=harpoon.IR.Quads.QuadSSI.codeFactory(hcf);
		hcf2=harpoon.IR.Quads.QuadWithTry.codeFactory(hcf);

	System.out.println("Preparing to run second stage");

	System.out.println("Running ClassHierarchy On converted hierarchy");
	WorkSet todo=new WorkSet();
	todo.add(mconverted);
	ch=null;
	ClassHierarchy ch1=new QuadClassHierarchy(linker,todo,hcf);

	//========================================================
	//Jasmin stuff below here:
	Set cmx=ch1.classes();
	Iterator iterate=cmx.iterator();
	WorkSet classes=new WorkSet();
	while(iterate.hasNext()) {
	    HClass cl=(HClass)iterate.next();
	    if ((!cl.isPrimitive())&&(!cl.isArray())) {
		classes.add(cl);
	    }
	}



	HClass interfaceClasses[] = new HClass[classes.size()];
	iterate=classes.iterator();
	int index=0;
	System.out.println("Compiling following classes:");
	while (iterate.hasNext()) {
	    interfaceClasses[index++]=(HClass)iterate.next();
	    System.out.println(interfaceClasses[index-1]);
	}

	// Do something intelligent with these classes. XXX
	for (int i=0; i<interfaceClasses.length; i++) {
	    HMethod hm1[] = interfaceClasses[i].getDeclaredMethods();
	    WorkSet hmo=new WorkSet();
	    System.out.println(interfaceClasses[i]+":");
	    for (int ind=0;ind<hm1.length;ind++) {
		//reflection seems a little too commonplace to allow this
		//sort of thing as a default
		hmo.add(hm1[ind]);
	    }
	    HMethod hm[] = new HMethod[hmo.size()];
	    Iterator hmiter=hmo.iterator();
	    int hindex=0;
	    while (hmiter.hasNext()) {
		hm[hindex++]=(HMethod)hmiter.next();
		System.out.println(hm[hindex-1]);
	    }

	    HCode hca[] = new HCode[hm.length];
	    for (int j=0; j<hm.length; j++) {
		System.out.println("Converting "+hm[j]);
		try {
		    hca[j] = hcf2.convert(hm[j]);
		} catch (Exception e) {
		    System.out.println("********");
		    e.printStackTrace();
		    hca[j] = hcf.convert(hm[j]);
		}
		//remove to help with Memory usage
		hcf.remove(hm[j]);
		if (hca[j]!=null) hca[j].print(out);
	    }
	    Jasmin jasmin=new Jasmin(hca, hm,interfaceClasses[i]);
	    FileOutputStream file=null;
	    try {
	    if (interfaceClasses.length!=1)
		file=new FileOutputStream("out"+i+".j");
	    else
		file=new FileOutputStream("out.j");
	    } catch (Exception e) {System.out.println(e);}
	    PrintStream tempstream=new PrintStream(file);
	    jasmin.outputClass(tempstream);
	    try {
	    file.close();
	    } catch (Exception e) {System.out.println(e);}
	}
    }

    private static Collection knownBlockingMethods() {
	final HClass is = linker.forName("java.io.InputStream");
	final HClass ss = linker.forName("java.net.ServerSocket");
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





