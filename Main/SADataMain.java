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

import java.util.Stack;
import java.util.NoSuchElementException;
import java.util.Iterator;
import java.util.Set;
import java.util.HashSet;

/**
 * <code>SADataMain</code>
 * 
 * @author  Felix S. Klock II <pnkfelix@mit.edu>
 * @version $Id: SADataMain.java,v 1.1.2.1 1999-08-10 18:10:54 pnkfelix Exp $
 */
public class SADataMain extends harpoon.IR.Registration {
    
    /** Creates a <code>SADataMain</code>. */
    private SADataMain() {
        
    }
    
    public static void main(String[] args) {
	java.io.PrintWriter out = new java.io.PrintWriter(System.out, true);

	final SAFrame frame = new SAFrame();
	
	int n = 0; // count # of args/flags processed

	// rest of command-line options are class names
	HClass[] classes = new HClass[args.length - n];
	for (int i=n; i<args.length; i++) {
	    classes[i-n] = HClass.forName(args[i]);
	}
	
	for (int i=0; i<classes.length; i++) {
	    final Data data = new Data(classes[i], frame);

	    
	    out.println("\t--- TREE FORM ---");
	    data.print(out);
	    out.println();

	    final String scope = data.getName();
	    final Instr instr = frame.codegen().gen(data, new InstrFactory() {
		private final TempFactory tf = Temp.tempFactory(scope);
		private int id = 0;
		public TempFactory tempFactory() { return tf; }
		public HCode getParent() { return data; }
		public Frame getFrame() { return frame; }
		public synchronized int getUniqueID() { return id++; }
		public HMethod getMethod() { return null; }
	    });
	    
	    Iterator iter = new UnmodifiableIterator() {
		Set visited = new HashSet();
		Stack stk = new Stack();
		{ stk.push(instr); visited.add(instr); }
		public boolean hasNext(){return !stk.empty(); }
		public Object next() {
		    if (stk.empty()) throw new NoSuchElementException();
		    Instr instr2 = (Instr) stk.pop();
		    HCodeEdge[] next = instr2.succ();
		    for (int j=next.length-1; j>=0; j--) {
			if (!visited.contains(next[j].to())) {
			    stk.push(next[j].to());
			    visited.add(next[j].to());
			}
		    }
		    return instr2;
		}
	    };
	    out.println("\t--- INSTR FORM ---");
	    while(iter.hasNext()) { out.println( iter.next() ); }
	    out.println();
	}
    }
}
