// EventDriven.java, created Tue Oct 19 21:14:57 1999 by kkz
// Copyright (C) 1999 Karen K. Zee <kkz@alum.mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Main;

import harpoon.Analysis.ClassHierarchy;
import harpoon.Analysis.Quads.QuadClassHierarchy;
import harpoon.Backend.Generic.Frame;
import harpoon.ClassFile.CachingCodeFactory;
import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HCode;
import harpoon.ClassFile.HCodeFactory;
import harpoon.ClassFile.HMethod;
import harpoon.ClassFile.Linker;
import harpoon.ClassFile.Loader;
import harpoon.ClassFile.Relinker;
import harpoon.Util.HClassUtil;
import net.cscott.jutil.WorkSet;

import harpoon.Backend.Backend;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Set;
import java.util.HashSet;
import java.lang.reflect.Modifier;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.PrintStream;
import java.io.FileOutputStream;
import harpoon.IR.Jasmin.Jasmin;

import harpoon.Analysis.MetaMethods.MetaAllCallers;
import harpoon.Analysis.MetaMethods.MetaCallGraph;
import harpoon.Analysis.MetaMethods.MetaCallGraphImpl;
import harpoon.Analysis.MetaMethods.MetaMethod;
import harpoon.Util.BasicBlocks.CachingBBConverter;

/**
 * <code>EventDriven</code>
 * 
 * @author Karen K. Zee <kkz@alum.mit.edu>
 * @version $Id: EventDriven.java,v 1.6 2004-02-08 01:58:13 cananian Exp $
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

	// any frame will do:
	Frame frame = Backend.getFrame(Backend.PRECISEC, m);

	System.out.println("Doing QuadSSI");
	HCodeFactory hco = 
	    new CachingCodeFactory(harpoon.IR.Quads.QuadNoSSA.codeFactory());



	Collection cc = new WorkSet();
	cc.addAll(frame.getRuntime().runtimeCallableMethods());
	cc.addAll(knownBlockingMethods());
	cc.add(m);
	System.out.println("Getting ClassHierarchy");
        ClassHierarchy chx = new QuadClassHierarchy(linker, cc, hco);

	CachingBBConverter bbconv=new CachingBBConverter(hco);

	// costruct the set of all the methods that might be called by 
	// the JVM (the "main" method plus the methods which are called by
	// the JVM before main) and next pass it to the MetaCallGraph
	// constructor. [AS]
	Set mroots = extract_method_roots(
	    frame.getRuntime().runtimeCallableMethods());
	mroots.add(m);

	MetaCallGraph mcg = 
	    new MetaCallGraphImpl((CachingCodeFactory) hco, linker,
				  chx, mroots);
	//MetaAllCallers mac=new MetaAllCallers(mcg);



//using hcf for now!

//  	HMethod[] bmethods=bm.blockingMethods();

//  	WorkSet mm=new WorkSet();
//  	Relation mrelation=mcg.getSplitRelation();
//  	for (int i=0;i<bmethods.length;i++) {
//  	    mm.addAll(mrelation.getValuesSet(bmethods[i]));
//  	}
//  	blockingmm=new WorkSet();
//  	for (Iterator i=mm.iterator();i.hasNext();) {
//  	    MetaMethod[] mma=mac.getCallers((MetaMethod)i.next());
//  	    blockingmm.addAll(java.util.Arrays.asList(mma));
//  	}



        HCodeFactory ccf = harpoon.IR.Quads.QuadSSI.codeFactory(hco);
	System.out.println("Doing UpdatingCodeFactory");
	CachingCodeFactory hcf = new CachingCodeFactory(ccf);
	final HClass hcc = HClass.Char;
	final HClass hi = HClass.Int;

	Collection c = new WorkSet();
	c.addAll(frame.getRuntime().runtimeCallableMethods());
	c.addAll(knownBlockingMethods());
	//        c.add(linker.forName("java.net.PlainSocketImpl").getMethod("getInputStream",new HClass[0]));

	//c.add(linker.forName("java.io.InputStreamReader").getMethod("fill",
	//	new HClass[] {HClassUtil.arrayClass(linker, hcc,1), hi, hi}));

	//c.add(linker.forName("java.io.InputStreamReader").getMethod("read",
	//	new HClass[0]));
	c.add(m);
	System.out.println("Getting ClassHierarchy");


        ClassHierarchy ch = new QuadClassHierarchy(linker, c, hcf);



	System.out.println("CALLABLE METHODS");
	Iterator iterator=ch.callableMethods().iterator();
	while (iterator.hasNext())
	    System.out.println(iterator.next());
	System.out.println("Classes");
	iterator=ch.classes().iterator();
	while (iterator.hasNext())
	    System.out.println(iterator.next());
	System.out.println("Instantiated Classes");
	iterator=ch.instantiatedClasses().iterator();
	while (iterator.hasNext())
	    System.out.println(iterator.next());

	System.out.println("------------------------------------------");





	HCode hc = hcf.convert(m);
	System.out.println("Done w/ set up");



	harpoon.Analysis.EventDriven.EventDriven ed = 
	    new harpoon.Analysis.EventDriven.EventDriven(hcf, hc, ch, linker,true,true);


	
	HMethod mconverted=ed.convert(mcg);

	System.out.println("Converted");
	System.out.println("Setting up HCodeFactories");
	//	HCodeFactory hcf2=hcf;

	//HCodeFactory hcf1=harpoon.IR.Quads.QuadSSI.codeFactory(hcf);
	//	hcf1=new harpoon.Analysis.EventDriven.LockRemove(null,null,hcf1);
	HCodeFactory hcf2=harpoon.IR.Quads.QuadWithTry.codeFactory(hcf);

	System.out.println("Preparing to run second stage");

	System.out.println("Running ClassHierarchy On converted hierarchy");
	WorkSet todo=new WorkSet();
	todo.add(mconverted);
	todo.addAll(frame.getRuntime().runtimeCallableMethods());
	todo.addAll(knownBlockingMethods());
	ch=null;
	ClassHierarchy ch1=new QuadClassHierarchy(linker,todo,hcf);

	//========================================================
	//Jasmin stuff below here:
	Set cmx=new WorkSet(ch1.classes());
	cmx.addAll(ed.classes());
	Iterator iterate=cmx.iterator();
	WorkSet classes=new WorkSet();
	while(iterate.hasNext()) {
	    HClass cl=(HClass)iterate.next();
	    if ((!cl.isPrimitive())&&(!cl.isArray())) {
		classes.add(cl);
	    }
	}
	//Garbage collect some more stuff
	iterate=null; cmx=null; ch1=null;

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
		    hca[j].print(out);
		    //hcf1.convert(hm[j]).print(out);
		    //hcf1x.convert(hm[j]).print(out);
		}
		//remove to help with Memory usage
		if (hca[j]!=null) {
		    hca[j].print(out);
		    //hcf1.convert(hm[j]).print(out);
		}
		hcf.clear(hm[j]);
	    }
	    int andmask=
		Modifier.ABSTRACT|Modifier.FINAL|Modifier.INTERFACE|
		Modifier.NATIVE|
		Modifier.STATIC|Modifier.TRANSIENT|
		Modifier.VOLATILE;
	    int ormask=Modifier.PUBLIC;
	    Jasmin jasmin=new Jasmin(hca, hm,interfaceClasses[i],andmask, ormask);
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
				   {HClassUtil.arrayClass(linker, b, 1)}));
	w.add(is.getDeclaredMethod("read", new HClass[] 
				   {HClassUtil.arrayClass(linker, b, 1),
					HClass.Int, HClass.Int}));
	w.add(ss.getDeclaredMethod("accept", new HClass[0]));
	return w;
    }


    // extract the method roots from the set of all the roots
    // (methods and classes)
    private static Set extract_method_roots(Collection roots){
	Set mroots = new HashSet();
	for(Iterator it = roots.iterator(); it.hasNext(); ){
	    Object obj = it.next();
	    if(obj instanceof HMethod)
		mroots.add(obj);
	}
	return mroots;
    }

}
