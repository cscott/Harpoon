// GCTraceStore.java, created by wbeebee
// Copyright (C) 2001 Wes Beebee <wbeebee@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Backend.Analysis;

import harpoon.Analysis.Maps.Derivation.DList;

import harpoon.Analysis.Tree.Simplification;
import harpoon.Analysis.Tree.Simplification.Rule;

import harpoon.Backend.Generic.Frame;

import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HCodeElement;
import harpoon.ClassFile.HCodeFactory;

import harpoon.IR.Tree.DerivationGenerator;
import harpoon.IR.Tree.Exp;
import harpoon.IR.Tree.ExpList;
import harpoon.IR.Tree.MEM;
import harpoon.IR.Tree.MOVE;
import harpoon.IR.Tree.NAME;
import harpoon.IR.Tree.NATIVECALL;
import harpoon.IR.Tree.Stm;
import harpoon.IR.Tree.TEMP;
import harpoon.IR.Tree.TreeFactory;
import harpoon.IR.Tree.Type;

import harpoon.Temp.Label;
import harpoon.Temp.Temp;
import harpoon.Temp.TempFactory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/** 
 * <code>GCTraceStore</code> traces stores for the purposes of garbage collection.
 * MOVE(MEM(foo), bar) -> NATIVECALL(foo, bar) where MEM.getType() == Type.POINTER.
 * @author Wes Beebee <wbeebee@mit.edu>
 */

public class GCTraceStore extends Simplification {
    private final List RULES = new ArrayList();
    private final HashSet generatedMoves = new HashSet();

    public GCTraceStore() {
	super();
	
	/* *foo=bar -> t1 = foo;
	 *             t2 = bar;
	 *             *t1 = t2;
	 *             gc_trace_store(t1, t2);
	 */

	RULES.add(new Rule("MOVE(MEM(foo), bar) -> NATIVECALL(foo, bar)") {
		public boolean match(Stm e) {
		    return (contains(_KIND(e), _MOVE)&&
			    contains(_KIND(((MOVE)e).getDst()), _MEM)&&
			    (((MEM)(((MOVE)e).getDst())).type() == Type.POINTER)&&
			    (!generatedMoves.contains(e)));
		}
		public Stm apply(TreeFactory tf, Stm e, DerivationGenerator dg) {
		    Temp t1 = new Temp(tf.tempFactory(), "ref");
		    Temp t2 = new Temp(tf.tempFactory(), "val");
		    MEM mem = (MEM)(((MOVE)e).getDst());
		    List stmList = new ArrayList();
		    Label func = new Label(tf.getFrame().getRuntime().getNameMap()
					   .c_function_name("gc_trace_store"));
		    stmList.add(new MOVE(tf, e, ref(tf, dg, e, t1), mem.getExp()));
		    stmList.add(new MOVE(tf, e, val(tf, dg, e, t2), ((MOVE)e).getSrc()));
		    HClass type;
		    MEM newMem = new MEM(tf, e, mem.type(), ref(tf, dg, e, t1));
		    dg.update(mem, newMem);
		    MOVE generated = new MOVE(tf, e, newMem, val(tf, dg, e, t2));
		    stmList.add(generated);
		    generatedMoves.add(generated);
		    stmList.add(new NATIVECALL(tf, e, null, 
					       (NAME)DECLARE(dg, HClass.Void, 
							     new NAME(tf, e, func)),
					       new ExpList(ref(tf, dg, e, t1),
							   new ExpList(val(tf, dg, e, t2),
								       null))));
		    return Stm.toStm(stmList);
		}

	    });
    }

    protected static TEMP ref(TreeFactory tf, DerivationGenerator dg, Stm e, Temp t1) {
	TEMP t = new TEMP(tf, e, Type.POINTER, t1);
	dg.update(((MEM)(((MOVE)e).getDst())).getExp(), t);
	return t;
    }
    
    protected static TEMP val(TreeFactory tf, DerivationGenerator dg, Stm e, Temp t2) {
	TEMP t = new TEMP(tf, e, Type.POINTER, t2); 
	dg.update(((MOVE)e).getSrc(), t);
	return t;
    }

    public HCodeFactory codeFactory(final HCodeFactory parent) {
	return super.codeFactory(parent, RULES);
    }

    /** Declare the type of an expression. 
     */ 
    protected static Exp DECLARE(DerivationGenerator dg, HClass hc, Exp exp) {
	if (dg != null) dg.putType(exp, hc);
	return exp;
    }

    /** Declare the type of an expression and the variable associated with it.
     */
    protected static Exp DECLARE(DerivationGenerator dg, HClass hc, Temp t,
				 Exp exp) {
	if (dg!=null) dg.putTypeAndTemp(exp, hc, t);
	return exp;
    }

    /** Declare a derived pointer. */
    protected static Exp DECLARE(DerivationGenerator dg, DList dl, Exp exp) {
	if (dg!=null) dg.putDerivation(exp, dl);
	return exp;
    }
}
