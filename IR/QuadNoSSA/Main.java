// Main.java, created Fri Aug  7 10:22:20 1998 by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.QuadNoSSA;

import java.io.*;

import harpoon.Analysis.QuadSSA.SCC.*;
import harpoon.Analysis.*;
import harpoon.ClassFile.*;
import harpoon.IR.QuadSSA.*;
import harpoon.Analysis.TypeInfo;
import harpoon.Analysis.Maps.TypeMap;

import harpoon.IR.QuadNoSSA.*;
/**
 * <code>Main</code> is the command-line interface to the compiler.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: Main.java,v 1.1 1998-10-14 07:07:58 nkushman Exp $
 */

//<<<<<<< Main.java
//public final class Main extends harpoon.IR.Registration{
    // hide away constructor.
//private Main() { }
//=======
public abstract class Main extends harpoon.IR.Registration {
    //>>>>>>> 1.7

    /** The compiler should be invoked with the names of classes
     *  extending <code>java.lang.Thread</code>.  These classes
     *  define the external interface of the machine. */
    public static void main(String args[]) {
	//java.io.PrintWriter out = new java.io.PrintWriter(System.out, true);

	HClass interfaceClasses[] = new HClass[args.length];
	for (int i=0; i<args.length; i++)
	    interfaceClasses[i] = HClass.forName(args[i]);
	// Do something intelligent with these classes. XXX
	for (int i=0; i<interfaceClasses.length; i++) {
	    NClass nclass = new NClass(interfaceClasses[i]);
	    HMethod hm[] = interfaceClasses[i].getDeclaredMethods();
	    for (int j=0; j<hm.length; j++) {
	      //SCCAnalysis scc = new SCCAnalysis (new UseDef());
	      //SCCOptimize sco = new SCCOptimize (scc,scc,scc);
	      if (hm[j].isInterfaceMethod()){
		nclass.addMethod (new NMethod(hm[j], new java.util.Hashtable()));
	      } else {
		HCode hc1 =  hm[j].getCode("quad-ssa");
		
		if (hc1 == null) {
		  System.out.println ("Yep.. this is Null!");
		}
		//sco.optimize(hc1);
		
		harpoon.IR.QuadNoSSA.Code hc = new harpoon.IR.QuadNoSSA.Code((harpoon.IR.QuadSSA.Code)hc1);
		System.out.println ("Right before calling create Java on: " +
				    hm[j].getName());
		try {
		  //NMethod method = hc.createJavaByte (scc, hm[j].getCode("quad-ssa"));
		  NMethod method = hc.createJavaByte (new TypeInfo(), hm[j].getCode("quad-ssa"));
		  nclass.addMethod (method);
		} catch (Exception e){
		  e.printStackTrace();
		}
	      }
	      
	    }
	    try {
	      PrintWriter out = new PrintWriter (new FileOutputStream ("nate.j"));
	      nclass.writeClass (out);
	      out.close();
	    } catch (Exception e){
	      e.printStackTrace();
	    }
	}
    }
}

