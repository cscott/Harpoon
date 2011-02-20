// InstrumentedAllocationStrategy.java, created Fri Feb  7 11:33:53 2003 by salcianu
// Copyright (C) 2000 Alexandru Salcianu <salcianu@MIT.EDU>
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
import harpoon.ClassFile.HMethod;
import harpoon.ClassFile.HCodeElement;
import harpoon.Temp.Temp;
import harpoon.Temp.TempFactory;
import harpoon.Temp.Label;

import harpoon.Backend.Maps.NameMap;

import java.util.List;
import java.util.ArrayList;


/**
 * <code>InstrumentedAllocationStrategy</code>
 * 
 * @author  Alexandru Salcianu <salcianu@MIT.EDU>
 * @version $Id: InstrumentedAllocationStrategy.java,v 1.4 2003-02-11 20:16:01 salcianu Exp $
 */
public class InstrumentedAllocationStrategy extends MallocAllocationStrategy {
    
    /** Creates a <code>InstrumentedAllocationStrategy</code>. */
    public InstrumentedAllocationStrategy(Frame f) { 
	super(f, "GC_malloc");
	instrumMethod = InstrumentAllocs.getMethod
	    (f.getLinker(),
	     "harpoon.Runtime.CounterSupport",
	     "count2",
	     new HClass[]{HClass.Int, HClass.Int});
    }

    private final HMethod instrumMethod;

    /* Generates the following sequence of instructions for each
       allocation site:

         tlength = length;
         call to instrumentation method(index, tlength);
          (both normal and exceptional exits go to label)
       label:
         allocCall(..., tlength);
    */
    public Exp memAlloc(TreeFactory tf, HCodeElement source,
			DerivationGenerator dg,
			AllocationProperties ap,
			Exp length) {
	int id = ap.getUniqueID();
	// id == -1 corresponds to allocation sites that are part of
	// our instrumenting code
	if(id == -1) return super.memAlloc(tf, source, dg, ap, length);
	
	TempFactory tempFact = tf.tempFactory();
	
       	TEMP tlength = new TEMP(tf, source, TEMP.INT, new Temp(tempFact));
	dg.putType(tlength, HClass.Int);

	/* bogus variable to store the exception from the call */
	TEMP texcp = new TEMP(tf, source, Typed.POINTER, new Temp(tempFact));
	dg.putType(texcp, HClass.Void);

	MOVE move = new MOVE(tf, source, tlength, length);
	
	Label contLabel = new Label(getUniqueName());
	NAME continuation = new NAME(tf, source, contLabel);

	CALL call = new CALL
	    (tf, source,
	     null,  /* no return value */
	     texcp, /* exceptions go to texcp (unused) */
	     /* method to call */
	     new NAME(tf, source,
		      frame.getRuntime().getNameMap().label(instrumMethod)),
	     /* 2 arguments: */
	     new ExpList(new CONST(tf, source, id), /* allocation ID */
			 new ExpList(tlength,       /* memory length */
				     null)),
	     /* exception handler = normal continuation */
	     continuation,
	     false /* not a tail call*/);

	LABEL label = new LABEL(tf, source, contLabel, false);
	Exp allocCall = super.memAlloc(tf, source, dg, ap, tlength);

	Exp wholeSequence =
	    new ESEQ(new SEQ(move, new SEQ(call, label)), allocCall);

	/*
	System.out.println("IT's COMING!");
	System.out.println(wholeSequence);
	System.exit(1);
	*/

	return wholeSequence;
    }

    protected static Exp DECLARE(DerivationGenerator dg, HClass hc, Exp exp) {
	if (dg != null) dg.putType(exp, hc);
	return exp;
    }

    private String getUniqueName() {
	return "cont" + uniqueNameCounter++;
    }
    private static int uniqueNameCounter = 0;
}
