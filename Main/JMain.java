// JMain.java, created Fri Aug  7 10:22:20 1998 by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Main;

import harpoon.ClassFile.CachingCodeFactory;
import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HCode;
import harpoon.ClassFile.HCodeFactory;
import harpoon.ClassFile.HMethod;
import harpoon.IR.Jasmin.Jasmin;

import harpoon.Analysis.SSITOSSAMap;
import harpoon.Analysis.ToSSA;
import harpoon.Analysis.ClassHierarchy;
import harpoon.Analysis.Quads.QuadClassHierarchy;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.PrintStream;
import java.io.FileOutputStream;

import java.util.Set;
import java.util.Iterator;
import harpoon.Util.WorkSet;

/**
 * <code>JMain</code> is the command-line interface to the compiler.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: JMain.java,v 1.1.2.1 1999-11-12 20:42:21 bdemsky Exp $
 */
public abstract class JMain extends harpoon.IR.Registration {

    /** The compiler should be invoked with the names of classes to view.
     *  An optional "-code codename" option allows you to specify which
     *  codeview to use.
     */
    public static void main(String args[]) throws java.io.IOException {
	java.io.PrintWriter out = new java.io.PrintWriter(System.out, true);

	HCodeFactory hcf1 = // default code factory.
	    harpoon.IR.Quads.QuadNoSSA.codeFactory();
	HCodeFactory hcf=harpoon.IR.Quads.QuadSSI.codeFactory(hcf1);
        hcf=harpoon.IR.Quads.QuadWithTry.codeFactory(hcf1);
	int n=0;  // count # of args/flags processed.
	for (; n < args.length ; n++) {
	    if (args[n].startsWith("-pass")) {
		if (++n >= args.length)
		    throw new Error("-pass option needs codename");
		hcf = Options.cfFromString(args[n], hcf);
	    } else if (args[n].startsWith("-stat")) {
		String filename = "./phisig.data";
		if (args[n].startsWith("-stat:"))
		    filename = args[n].substring(6);
		try {
		    Options.statWriter = 
			new PrintWriter(new FileWriter(filename), true);
		} catch (IOException e) {
		    throw new Error("Could not open " + filename +
				    " for statistics: " + e.toString());
		}
	    } else break; // no more command-line options.
	}
	// rest of command-line options are class names.

	WorkSet todo=new WorkSet();
	WorkSet todor=new WorkSet(harpoon.Backend.Runtime1.Runtime.runtimeCallableMethods());
	for (int i=0; i<args.length-n; i++) {
	    HMethod hmx[]=(HClass.forName(args[n+i])).getDeclaredMethods();
	    for (int j=0; j<hmx.length; j++) {
		if (hmx[j].getName().equalsIgnoreCase("main")) {
		    todo.add(hmx[j]);
		    break;
		}
	    }
	}
	ClassHierarchy ch1=new QuadClassHierarchy(todo,hcf1);
	ClassHierarchy ch2=new QuadClassHierarchy(todor,hcf1);
	Set cm1=ch1.callableMethods();
	Set cm2=ch2.callableMethods();
	Iterator iterate=cm1.iterator();
	WorkSet classes=new WorkSet();
	while(iterate.hasNext()) {
	    classes.add(((HMethod)iterate.next()).getDeclaringClass());
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
	    for (int ind=0;ind<hm1.length;ind++) {
		if (cm1.contains(hm1[ind])||cm2.contains(hm1[ind]))
		    hmo.add(hm1[ind]);
	    }
	    HMethod hm[] = new HMethod[hmo.size()];
	    Iterator hmiter=hmo.iterator();
	    int hindex=0;
	    while (hmiter.hasNext()) {
		hm[hindex++]=(HMethod)hmiter.next();
		System.out.println(hm[hindex-1]);
	    }

	    HCode hc[] = new HCode[hm.length];
	    for (int j=0; j<hm.length; j++) {
		hc[j] = hcf.convert(hm[j]);
		if (hc[j]!=null) hc[j].print(out);
	    }
	    Jasmin jasmin=new Jasmin(hc, hm,interfaceClasses[i]);
	    FileOutputStream file;
	    if (interfaceClasses.length!=1)
		file=new FileOutputStream("out"+i+".j");
	    else
		file=new FileOutputStream("out.j");
	    PrintStream tempstream=new PrintStream(file);
	    jasmin.outputClass(tempstream);
	    file.close();
	}
	if (Options.profWriter!=null) Options.profWriter.close();
	if (Options.statWriter!=null) Options.statWriter.close();
	Runtime r=Runtime.getRuntime();
	r.exit(0);
    }
}





