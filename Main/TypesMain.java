// Start.java, created Fri Sep 25 02:24:11 1998 by marinov
package harpoon.Main;

import harpoon.ClassFile.*;
import harpoon.IR.Quads.*;
import java.util.Enumeration;
import harpoon.Analysis.QuadSSA.*;
import harpoon.Analysis.TypeInference.*;
import harpoon.Util.Set;
import harpoon.Util.Worklist;
import harpoon.Temp.*;
/**
 * <code>PrintTypes</code>
 * 
 * @author  Darko Marinov <marinov@lcs.mit.edu>
 * @version $Id: TypesMain.java,v 1.1.2.1 1998-12-07 22:08:04 marinov Exp $
 */

public class TypesMain extends harpoon.IR.Registration {
    public static void main(String args[]) {
	if (args.length >= 2) {
	    java.io.PrintWriter pw = new java.io.PrintWriter(System.out, true);
	    HClass hcl = HClass.forName(args[0]);
	    HMethod hm[] = hcl.getDeclaredMethods();
	    ClassHierarchy ch = null;
	    harpoon.Analysis.QuadSSA.CallGraph cg = null;
	    InterProc ty = null;
	    boolean multiPass = args[args.length-1].equals("ADDITIONAL");
	    for (int i=1; i<args.length; i++) {
		if ((i==args.length-1)&&multiPass) continue;
		int j;
		for (j=0; j<hm.length; j++) if (args[i].equals(hm[j].getName())) break;
		if (j<hm.length) {		    
		    HCode hco = hm[j].getCode("quad-ssa");		    
		    if (i==1) {
			System.out.println("CHA-like started: " + System.currentTimeMillis());
			ch = new ClassHierarchy(hm[j]);
			cg = new harpoon.Analysis.QuadSSA.CallGraph(ch);
			System.out.println("CHA-like finished: " + System.currentTimeMillis());
			if (multiPass) {
			    System.out.println("another CHA-like started: " + System.currentTimeMillis());
			    ClassHierarchy ch1 = new ClassHierarchy(hm[j]);
			    System.out.println("another CHA-like finished: " + System.currentTimeMillis());
			    System.out.println("yet another CHA-like started: " + System.currentTimeMillis());
			    ClassHierarchy ch2 = new ClassHierarchy(hm[j]);
			    System.out.println("yet another CHA-like finished: " + System.currentTimeMillis());
			}
			System.out.println("0-CFA-like started: " + System.currentTimeMillis());
			ty = new InterProc(hco, ch); 
			ty.analyze();
			System.out.println("0-CFA-like finished: " + System.currentTimeMillis());
			if (multiPass) { 
			    System.out.println("another 0-CFA-like started: " + System.currentTimeMillis());
			    InterProc ty1 = new InterProc(hco, ch); ty1.analyze();
			    System.out.println("another 0-CFA-like finished: " + System.currentTimeMillis());
			}
		    }
		    for (int ana=0; ana<=1; ana++) {
			if (ana==0) System.out.println("******** CHA-like");
			else System.out.println("******** 0-CFA-like");
			Set fi = new Set();
			Worklist wl = new Set();
			wl.push(hm[j]);
			while (!wl.isEmpty()) {
			    HMethod m = (HMethod)wl.pull();
			    System.out.print(m.getDeclaringClass() + " " + m.getName() +
					     " (" + m.getParameterTypes().length + ") calls: ");
			    HCode hc = m.getCode("quad-ssa");
			    if (hc!=null) {
				Quad forLast = null;
				Enumeration e = hc.getElementsE();
				while (e.hasMoreElements()) {
				    Quad q = (forLast==null) ? (Quad) e.nextElement() : forLast;
				    if (!(q instanceof CALL)) continue;
				    while (e.hasMoreElements()) {
					forLast = (Quad) e.nextElement();
					if (forLast instanceof CALL) break;
				    }
				    CALL cs = (CALL)q;
				    HMethod[] mm;
				    if (ana==0) mm = cg.calls(m, cs);
				    else mm = ty.calls(m, cs, !e.hasMoreElements());
				    System.out.print(mm.length + " ");
				    for (int k=0; k<mm.length; k++)
					if (!fi.contains(mm[k])) { fi.union(mm[k]); wl.push(mm[k]); }
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
