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
import harpoon.Analysis.QuadSSA.Profile;

import harpoon.IR.QuadNoSSA.*;
/**
 * <code>Main</code> is the command-line interface to the compiler.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: ProfileMain.java,v 1.2.4.3 1998-11-27 23:22:31 mfoltz Exp $
 */

//<<<<<<< Main.java
//public final class Main extends harpoon.IR.Registration{
    // hide away constructor.
//private Main() { }
//=======
public class ProfileMain extends harpoon.IR.Registration {
    //>>>>>>> 1.7

    /** The compiler should be invoked with the names of classes
     *  extending <code>java.lang.Thread</code>.  These classes
     *  define the external interface of the machine. */
    public static void main(String args[]) {
	java.io.PrintWriter out;
	String title;
	boolean profile = false;

	if (args[0].equals("1")) profile = true;

	HClass interfaceClasses[] = new HClass[args.length-1];
	for (int i=1; i<args.length; i++)
	    interfaceClasses[i-1] = HClass.forName(args[i]);
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
		try {
		//insert profiling stuff -- mfoltz
		if (profile) harpoon.Analysis.QuadSSA.Profile.optimize(hc1);
		title = interfaceClasses[i].getName()+":"+hm[j].getName();
		out = new PrintWriter( 
				      new FileOutputStream 
				      (interfaceClasses[i].getName().replace('.','/')+":"+hm[j].getName()+".vcg"));
		harpoon.Util.Graph.printCFG(hc1,out,title);
		out.close();
		
		harpoon.IR.QuadNoSSA.Code hc = new harpoon.IR.QuadNoSSA.Code((harpoon.IR.QuadSSA.Code)hc1);
		//		System.out.println ("Right before calling create Java on: " +
		//				    hm[j].getName());

		  //NMethod method = hc.createJavaByte (scc, hm[j].getCode("quad-ssa"));
		  NMethod method = hc.createJavaByte (new TypeInfo(), hm[j].getCode("quad-ssa"));
		  nclass.addMethod (method);
		} catch (Exception e){
		  e.printStackTrace();
		}
	      }
	      
	    }
	    try {
	      PrintWriter c_out = new PrintWriter (new FileOutputStream (interfaceClasses[i].getName().replace('.','/')+".j"));
	      nclass.writeClass (c_out);
	      c_out.close();
	    } catch (Exception e){
	      e.printStackTrace();
	    }
	}
    }
}

