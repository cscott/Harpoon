// InstrumentedAllocationStrategy.java, created Fri Feb  7 11:33:53 2003 by salcianu
// Copyright (C) 2000  <salcianu@MIT.EDU>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Instrumentation.AllocationStatistics;

import harpoon.Analysis.Maps.AllocationInformation.AllocationProperties;
import harpoon.Backend.Generic.Frame;
import harpoon.Backend.Runtime1.MallocAllocationStrategy;

import harpoon.IR.Tree.DerivationGenerator;
import harpoon.IR.Tree.TreeFactory;

import harpoon.IR.Tree.Stm;
import harpoon.IR.Tree.StmList;
import harpoon.IR.Tree.ExpList;
import harpoon.IR.Tree.Exp;
import harpoon.IR.Tree.CALL;
import harpoon.IR.Tree.CONST;
import harpoon.IR.Tree.TEMP;
import harpoon.IR.Tree.MOVE;
import harpoon.IR.Tree.LABEL;
import harpoon.IR.Tree.NAME;
import harpoon.IR.Tree.SEQ;
import harpoon.IR.Tree.ESEQ;
import harpoon.IR.Tree.Typed;

import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HCodeElement;
import harpoon.Temp.Temp;
import harpoon.Temp.TempFactory;
import harpoon.Temp.Label;

import java.util.List;
import java.util.ArrayList;


/**
 * <code>InstrumentedAllocationStrategy</code>
 * 
 * @author   <salcianu@MIT.EDU>
 * @version $Id: InstrumentedAllocationStrategy.java,v 1.1 2003-02-08 23:22:39 salcianu Exp $
 */
public class InstrumentedAllocationStrategy extends MallocAllocationStrategy {
    
    /** Creates a <code>InstrumentedAllocationStrategy</code>. */
    public InstrumentedAllocationStrategy(Frame f) { 
	super(f, "GC_malloc");
    }
    
    public Exp memAlloc(TreeFactory tf, HCodeElement source,
			DerivationGenerator dg,
			AllocationProperties ap,
			Exp length) {

	int id = ap.getUniqueID();
	if(id == -1) return super.memAlloc(tf, source, dg, ap, length);
	
	TempFactory tempFact = tf.tempFactory();
	
       	TEMP tlength = 
	    (TEMP) DECLARE(dg, HClass.Int,
			   new TEMP(tf, source,
				    Typed.INT, new Temp(tempFact)));
	
	MOVE move = new MOVE(tf, source, tlength, length);
    
	TEMP texcp = /* bogus variable to store the exception in */
	    (TEMP) DECLARE(dg, HClass.Void, 
			   new TEMP(tf, source,
				    Typed.POINTER, new Temp(tempFact)));
	
	String cont_label_name = getUniqueName();
	NAME continuation = new NAME(tf, source, new Label(cont_label_name));
	
	Exp func = null; // TODO: this should be the invoked method

	CALL call = new CALL(tf, source,
			     null, /* no return value */
			     texcp,
			     func,
			     new ExpList
			     (new CONST(tf, source, id),
			      new ExpList(tlength, null)),
			     /* exception handler = normal continuation */
			     continuation,
			     false /* not a tail call*/);

	LABEL label = new LABEL(tf, source, new Label(cont_label_name), false);
	Exp allocCall = super.memAlloc(tf, source, dg, ap, tlength);

	/*
	  tlength = length;
	  call to instrumentation method(index, tlength);
	  label: allocCall(..., tlength);
	 */
	return
	    new ESEQ(new SEQ(move, new SEQ(call, label)), allocCall);
    }

    protected static Exp DECLARE(DerivationGenerator dg, HClass hc, Exp exp) {
	if (dg!=null) dg.putType(exp, hc);
	return exp;
    }

    private String getUniqueName() {
	return "cont" + uniqueNameCounter++;
    }
    private static int uniqueNameCounter = 0;
}
