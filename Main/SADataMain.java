// SADataMain.java, created Mon Aug  9 18:26:42 1999 by pnkfelix
// Copyright (C) 1999 Felix S. Klock II <pnkfelix@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Main;

import harpoon.IR.Tree.Data;
import harpoon.IR.Assem.InstrFactory;
import harpoon.IR.Assem.Instr;
import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HMethod;
import harpoon.ClassFile.HCode;
import harpoon.ClassFile.HCodeEdge;
import harpoon.ClassFile.HCodeElement;
import harpoon.ClassFile.HCodeFactory;
import harpoon.Temp.TempFactory;
import harpoon.Temp.Temp;
import harpoon.Backend.Generic.Frame;
import harpoon.Backend.StrongARM.SAFrame;
import harpoon.Backend.StrongARM.SACode;
import harpoon.Util.UnmodifiableIterator;
import harpoon.Util.Util;

import java.util.Stack;
import java.util.Vector;
import java.util.NoSuchElementException;
import java.util.Iterator;
import java.util.Set;
import java.util.HashSet;

/**
 * <code>SADataMain</code>
 * 
 * @author  Felix S. Klock II <pnkfelix@mit.edu>
 * @version $Id: SADataMain.java,v 1.1.2.5 1999-08-18 19:21:24 pnkfelix Exp $
 */
public class SADataMain extends harpoon.IR.Registration {
    
    /** Creates a <code>SADataMain</code>. */
    private SADataMain() {
        
    }
    
    public static void main(String[] args) {
	System.out.println("SADataMain is deprecated, use 'SAMain -D' instead");
	System.exit(0);

	java.io.PrintWriter out = new java.io.PrintWriter(System.out, true);

	final SAFrame frame=null;// = new SAFrame();
	
	int n = 0; // count # of args/flags processed

	// rest of command-line options are class names
	Vector classes = new Vector();
	for (int i=n; i<args.length; i++) {
	    classes.add(HClass.forName(args[i]));
	}
	
	for (int i=0; i<classes.size(); i++) {
	    final Data data = new Data((HClass)classes.get(i), frame);

	    
	}
    }
}
