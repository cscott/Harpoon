// Start.java, created Fri Sep 25 02:24:11 1998 by marinov
// Copyright (C) 1998 Darko Marinov <marinov@lcs.mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Main;

import harpoon.ClassFile.CachingCodeFactory;
import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HCode;
import harpoon.ClassFile.HCodeFactory;
import harpoon.ClassFile.HMethod;
import harpoon.ClassFile.Linker;
import harpoon.ClassFile.Loader;
import harpoon.IR.Quads.CALL;
import harpoon.IR.Quads.Quad;
import harpoon.Analysis.ClassHierarchy;
import harpoon.Analysis.Quads.QuadClassHierarchy;
import harpoon.Analysis.TypeInference.InterProc;
import harpoon.Util.Worklist;
import harpoon.Util.Collections.WorkSet;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
/**
 * <code>PrintTypes</code>
 * 
 * @author  Darko Marinov <marinov@lcs.mit.edu>
 * @version $Id: TypesMain.java,v 1.3 2002-09-03 15:18:34 cananian Exp $
 */
public class TypesMain extends harpoon.IR.Registration {
    public static void main(String args[]) {
	Linker linker = Loader.systemLinker;
	if (args.length >= 2) {
	    java.io.PrintWriter pw = new java.io.PrintWriter(System.out, true);
	    HCodeFactory hcf =
		new CachingCodeFactory(harpoon.IR.Quads.QuadSSI.codeFactory());
	    HClass hcl = linker.forName(args[0]);
	    HMethod hm[] = hcl.getDeclaredMethods();
	    ClassHierarchy ch = null;
	    harpoon.Analysis.Quads.CallGraph cg = null;
	    InterProc ty = null;
	    boolean multiPass = args[args.length-1].equals("ADDITIONAL");
	    for (int i=1; i<args.length; i++) {
		if ((i==args.length-1)&&multiPass) continue;
		int j;
		for (j=0; j<hm.length; j++) if (args[i].equals(hm[j].getName())) break;
		if (j<hm.length) {		    
		    HCode hco = hcf.convert(hm[j]);
		    if (i==1) {
			System.out.println("CHA-like started: " + System.currentTimeMillis());
			ch = new QuadClassHierarchy(linker, Collections.singleton(hm[j]), hcf);
			cg = new harpoon.Analysis.Quads.CallGraphImpl(ch, hcf);
			System.out.println("CHA-like finished: " + System.currentTimeMillis());
			if (multiPass) {
			    System.out.println("another CHA-like started: " + System.currentTimeMillis());
			    ClassHierarchy ch1 = new QuadClassHierarchy(linker, Collections.singleton(hm[j]), hcf);
			    System.out.println("another CHA-like finished: " + System.currentTimeMillis());
			    System.out.println("yet another CHA-like started: " + System.currentTimeMillis());
			    ClassHierarchy ch2 = new QuadClassHierarchy(linker, Collections.singleton(hm[j]), hcf);
			    System.out.println("yet another CHA-like finished: " + System.currentTimeMillis());
			}
			System.out.println("0-CFA-like started: " + System.currentTimeMillis());
			ty = new InterProc(hco, ch, hcf); 
			ty.analyze();
			System.out.println("0-CFA-like finished: " + System.currentTimeMillis());
			if (multiPass) { 
			    System.out.println("another 0-CFA-like started: " + System.currentTimeMillis());
			    InterProc ty1 = new InterProc(hco, ch, hcf); ty1.analyze();
			    System.out.println("another 0-CFA-like finished: " + System.currentTimeMillis());
			}
		    }
		    for (int ana=0; ana<=1; ana++) {
			if (ana==0) System.out.println("******** CHA-like");
			else System.out.println("******** 0-CFA-like");
			Set fi = new HashSet();
			Worklist wl = new WorkSet();
			wl.push(hm[j]);
			while (!wl.isEmpty()) {
			    HMethod m = (HMethod)wl.pull();
			    System.out.print(m.getDeclaringClass() + " " + m.getName() +
					     " (" + m.getParameterTypes().length + ") calls: ");
			    HCode hc = hcf.convert(m);
			    if (hc!=null) {
				Quad forLast = null;
				Iterator e = hc.getElementsI();
				while (e.hasNext()) {
				    Quad q = (forLast==null) ? (Quad) e.next() : forLast;
				    if (!(q instanceof CALL)) continue;
				    while (e.hasNext()) {
					forLast = (Quad) e.next();
					if (forLast instanceof CALL) break;
				    }
				    CALL cs = (CALL)q;
				    HMethod[] mm;
				    if (ana==0) mm = cg.calls(m, cs);
				    else mm = ty.calls(m, cs, !e.hasNext());
				    System.out.print(mm.length + " ");
				    for (int k=0; k<mm.length; k++)
					if (!fi.contains(mm[k])) { fi.add(mm[k]); wl.push(mm[k]); }
				}
				System.out.println();
			    } else System.out.println("0 !");
			}
			if (ana==0) System.out.println("******** \\CHA-like");
			else System.out.println("******** \\0-CFA-like");
		    }
		    //SetHClass s = ty.getReturnType(hco);
		    //System.out.println("RETURN TYPE=" + s);
		} else System.err.println("method not found");	    
	    }
	    pw.close();
	} else System.err.println("usage: class method[s] [ADDITIONAL]");
    }
}
