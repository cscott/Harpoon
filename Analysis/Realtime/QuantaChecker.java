// QuantaChecker.java, created Fri Jun  8 14:02:02 2001 by wingman
// Copyright (C) 2001 Bryan Fink <wingman@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.Realtime;

import harpoon.Analysis.Transformation.MethodMutator;
import harpoon.Backend.Generic.Frame;
import harpoon.ClassFile.HCodeFactory;
import harpoon.ClassFile.HCodeElement;
import harpoon.ClassFile.HCode;
import harpoon.ClassFile.HCodeAndMaps;
import harpoon.ClassFile.HClass;
import harpoon.ClassFile.Linker;
import harpoon.ClassFile.HField;
import harpoon.ClassFile.HMethod;

import harpoon.IR.Tree.CanonicalTreeCode;
import harpoon.IR.Tree.TreeVisitor;
import harpoon.IR.Tree.TreeFactory;
import harpoon.IR.Tree.DerivationGenerator;
import harpoon.IR.Tree.METHOD;
import harpoon.IR.Tree.CJUMP;
import harpoon.IR.Tree.MEM;
import harpoon.IR.Tree.NAME;
import harpoon.IR.Tree.LABEL;
import harpoon.IR.Tree.ESEQ;
import harpoon.IR.Tree.NATIVECALL;
import harpoon.IR.Tree.CONST;
import harpoon.IR.Tree.SEQ;
import harpoon.IR.Tree.CALL;
import harpoon.IR.Tree.TEMP;
import harpoon.IR.Tree.Tree;
import harpoon.IR.Tree.Stm;
import harpoon.IR.Tree.Exp;
import harpoon.IR.Tree.ExpList;
import harpoon.IR.Tree.TreeCode;
import harpoon.IR.Tree.Typed;
import harpoon.IR.Tree.Type;

import harpoon.Temp.Label;
import harpoon.Temp.Temp;

import harpoon.Util.Collections.SnapshotIterator;

import harpoon.Util.Util;

import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.io.PrintWriter;

/**
 * @author  Bryan Fink <wingman@mit.edu>
 * @version $Id: QuantaChecker.java,v 1.8 2002-08-31 00:31:12 cananian Exp $
 */
public class QuantaChecker extends MethodMutator
{
    public QuantaChecker(HCodeFactory hcf)
    {
	super(hcf);
	assert hcf.getCodeName().equals(CanonicalTreeCode.codename) : 
	    "QuantaChecker only works on CanonicalTree form";
    }
    
    protected HCode mutateHCode(HCodeAndMaps input) {
	final HCode hc = input.hcode();
	HClass hclass = hc.getMethod().getDeclaringClass();
	if (hc == null) {
	    return hc;
	}
	final Linker linker = hclass.getLinker();
	
	TreeVisitor visitor = new TreeVisitor() {
		public void visit(METHOD e)
		{
		    TreeFactory tf = e.getFactory();
		    Stm stuff = addJump(linker, hc, tf, e);
		    //make placebo label to hold parent of e while we mess with it
		    LABEL newLabel = new LABEL(tf, e, new Label("hackLabel"), true);
		    //replace e in the tree with label
		    e.replace(newLabel);
		    //make a new sequence of e and "stuff" (the test and jump)
		    SEQ newSeq = new SEQ(tf, e, e, stuff);
		    //replace the label with e to put it back in the tree
		    newLabel.replace(newSeq);
       		}

		//put in loops later

		public void visit(Tree e)
		{}
	    };

	// note that we use a SnapshotIterator so that ti is 'static';
	// i.e. the underlying list of elements to iterate over does not
	// change as the visitor mutates the hcode.
	Iterator ti = new SnapshotIterator(hc.getElementsI());
	while (ti.hasNext()) ((Tree)(ti.next())).accept(visitor);

	return hc;
    }

    protected Stm addJump(Linker linker, HCode hc, TreeFactory tf, Stm e)
    {
	
	harpoon.IR.Tree.Code code = (harpoon.IR.Tree.Code) hc; //cast to Code
	DerivationGenerator dg = (DerivationGenerator) code.getTreeDerivation(); //get DerivationGenerator

	NATIVECALL handleFlag = addCheck(tf, e, dg); //get call to C function

	List stmlist = new ArrayList(); //create stmt list
	stmlist.add(handleFlag);        //add c func call
	return Stm.toStm(stmlist);      //create final stmt
    }

    protected NATIVECALL addCheck(TreeFactory tf, HCodeElement source,
			   DerivationGenerator dg)
    {
	CONST zero1 = new CONST(tf, source, 0);
	CONST zero2 = new CONST(tf, source, 0);
	CONST one = new CONST(tf, source, 1);
	List list = new ArrayList();
	list.add(zero1);
	list.add(zero2);
	list.add(one);
	ExpList args = ExpList.toExpList(list);

	Label func = new Label(tf.getFrame().getRuntime().getNameMap()
			       .c_function_name("CheckQuanta"));
	return new NATIVECALL
	    (tf, source,
	     null,
	     (NAME)
	     DECLARE(dg, HClass.Void/*our quanta flag handler*/,
		     new NAME(tf, source, func)),
	     args);
    }
    protected static Exp DECLARE(DerivationGenerator dg, HClass hc, Exp exp) {
	if (dg!=null) dg.putType(exp, hc);
	return exp;
    }
    protected static Exp DECLARE(DerivationGenerator dg, HClass hc, Temp t,
			       Exp exp) {
	if (dg!=null) dg.putTypeAndTemp(exp, hc, t);
	return exp;
    }

    
}

