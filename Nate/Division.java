// Main.java, created Fri Aug  7 10:22:20 1998 by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Nate;

import harpoon.ClassFile.*;
import harpoon.IR.QuadSSA.*;


import java.util.Hashtable;
/**
 * <code>Main</code> is the command-line interface to the compiler.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: Division.java,v 1.1.2.1 1998-11-22 03:32:40 nkushman Exp $
 */

public abstract class Division extends harpoon.IR.Registration {
  
  /** The compiler should be invoked with the names of classes
   *  extending <code>java.lang.Thread</code>.  These classes
   *  define the external interface of the machine. */
  public static void main(String args[]) {
    
      //dynamcially add each getfield and setfield method to the object
      
      //for new we need to change the name of the class
      
      //for all method parameters and all instanceof's we need to change them 
      // to point from the class to the interface that we will create for each
      // class....
      /*System.out.println ("Well it seems to have loaded the zeroth class..");
      HClass.forName ("com.sun.java.accessibility.AccessibleAction");
      System.out.println ("Well it seems to have loaded the first class..");
      HClass.forName ("com.sun.java.accessibility.AccessibleStateSet");
      System.out.println ("Well it seems to have loaded the second class..");
      HClass.forName ("java.lang.IllegalStateException");
      System.out.println ("Well it seems to have loaded the third class..");
      HClass.forName ("java.awt.IllegalComponentStateException");
      System.out.println ("Well it seems to have loaded the fourth class..");
      HClass.forName ("com.sun.java.accessibility.AccessibleRole");
      System.out.println ("Well it seems to have loaded the fifth class..");
      HClass.forName ("com.sun.java.swing.AncestorNotifier");
      System.out.println ("Well it seems to have loaded the sixth class..");
      HClass.forName("win");*/

      HClass cls = HClass.forName(args[0]);
      HMethod hm[] = cls.getDeclaredMethods();
      HMethod m = null;
      for (int i=0; i<hm.length; i++){
	  if (hm[i].getName().equals(args[1])) {
	      m = hm[i];
	      break;
	  }
      }
      
      
      //harpoon.IR.QuadNoSSA.Code.write (cls);
      
      Divide.divide (new Hashtable(), m, cls, "input.txt");
    
      /*java.io.PrintWriter out = new java.io.PrintWriter(System.out, true);
	
      HClass interfaceClasses[] = new HClass[args.length];
      for (int i=0; i<args.length; i++)
      interfaceClasses[i] = HClass.forName(args[i]);
      // Do something intelligent with these classes.
      for (int i=0; i<interfaceClasses.length; i++) {
      HMethod hm[] = interfaceClasses[i].getDeclaredMethods();
      for (int j=0; j<hm.length; j++) {
      HCode hc = hm[j].getCode("quad-ssa");
      hc.print(out);
      }
      }
    */
  }
}

