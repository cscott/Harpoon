// HeapCheckAdder.java, created by wbeebee
// Copyright (C) 2001 Wes Beebee <wbeebee@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.Realtime;

import harpoon.Analysis.Transformation.MethodMutator;

import harpoon.ClassFile.HCodeFactory;
import harpoon.ClassFile.HCode;
import harpoon.ClassFile.HCodeAndMaps;
import harpoon.ClassFile.HClass;

import harpoon.IR.Tree.TreeVisitor;
import harpoon.IR.Tree.TreeFactory;
import harpoon.IR.Tree.DerivationGenerator;
import harpoon.IR.Tree.MEM;
import harpoon.IR.Tree.NATIVECALL;
import harpoon.IR.Tree.NAME;
import harpoon.IR.Tree.TEMP;
import harpoon.IR.Tree.Tree;
import harpoon.IR.Tree.TreeCode;
import harpoon.IR.Tree.Exp;
import harpoon.IR.Tree.Typed;
import harpoon.IR.Tree.Type;
import harpoon.IR.Tree.Stm;
import harpoon.IR.Tree.Code;

import harpoon.Temp.Label;
import harpoon.Temp.Temp;

import harpoon.Util.Util;

import java.io.PrintWriter;

public class HeapCheckAdder extends MethodMutator
{
    /** Construct a HeapCheckAdder to add checks for heap references to
     *  Tree form code.
     */
    public HeapCheckAdder(HCodeFactory hcf) 
    {
	super(hcf);
	Util.assert(hcf.getCodeName().equals(TreeCode.codename),
		    "HeapCheckAdder only works on Tree form");
    }

    /** Every memory reference needs to be checked to see if it's pointing
     *  to the heap if we're in a NoHeapRealtimeThread. 
     *  Doesn't this just SOUND extremely inefficient?
     */
    
    protected HCode mutateHCode(HCodeAndMaps input) {
	final HCode hc = input.hcode();
	if (hc == null) {
	    return hc;
	}
	
	TreeVisitor visitor = new TreeVisitor() {
		public void visit(MEM e) {
		    e.replace(heapCheck(hc, e));
		}
		
		public void visit(Tree e) {}
	    };

	Tree[] tl = (Tree[]) hc.getElements();
	
	for (int i=0; i<tl.length; i++) {
	    tl[i].accept(visitor);
	}
	
	return hc;
    }

    protected Stm heapCheck(HCode hc, MEM e) {
	TreeFactory tf = e.getFactory();
	Label func = new Label(tf.getFrame().getRuntime().nameMap
			       .c_function_name("heapCheck"));
	DerivationGenerator dg = (DerivationGenerator)(((Code)hc).getTreeDerivation());
	NATIVECALL nc = 
	    new NATIVECALL(tf, e, null, 
			   (NAME)
			   DECLARE(dg, HClass.Void,
				   new NAME(tf, e, func)),
			   null);
	/** bogus */
	return nc;
    }
    protected static Exp DECLARE(DerivationGenerator dg, HClass hc, Exp exp) {
	if (dg != null) dg.putType(exp, hc);
	return exp;
    }
    protected static Exp DECLARE(DerivationGenerator dg, HClass hc, Temp t,
			       Exp exp) {
	if (dg!=null) dg.putTypeAndTemp(exp, hc, t);
	return exp;
    }

}
