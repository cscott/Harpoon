// HeapCheckAdder.java, created by wbeebee
// Copyright (C) 2001 Wes Beebee <wbeebee@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.Realtime;

import harpoon.Analysis.Maps.Derivation.DList;

import harpoon.Analysis.Tree.Canonicalize;
import harpoon.Analysis.Tree.Simplification;
import harpoon.Analysis.Tree.Simplification.Rule;

import harpoon.Backend.Generic.Frame;

import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HCodeElement;
import harpoon.ClassFile.HCodeFactory;
import harpoon.ClassFile.HField;

import harpoon.IR.Tree.BINOP;
import harpoon.IR.Tree.Bop;
import harpoon.IR.Tree.CJUMP;
import harpoon.IR.Tree.CONST;
import harpoon.IR.Tree.DerivationGenerator;
import harpoon.IR.Tree.Exp;
import harpoon.IR.Tree.ExpList;
import harpoon.IR.Tree.ESEQ;
import harpoon.IR.Tree.LABEL;
import harpoon.IR.Tree.MEM;
import harpoon.IR.Tree.MOVE;
import harpoon.IR.Tree.NAME;
import harpoon.IR.Tree.NATIVECALL;
import harpoon.IR.Tree.SEQ;
import harpoon.IR.Tree.Stm;
import harpoon.IR.Tree.TEMP;
import harpoon.IR.Tree.TreeFactory;
import harpoon.IR.Tree.Type;
import harpoon.IR.Tree.Uop;
import harpoon.IR.Tree.UNOP;

import harpoon.Temp.Label;
import harpoon.Temp.Temp;
import harpoon.Temp.TempFactory;

import harpoon.Util.Util;

import java.util.ArrayList;
import java.util.List;


/** 
 * <code>HeapCheckAdder</code> adds checks to see if a NoHeapRealtimeThread is
 * touching the heap.
 * @author Wes Beebee <wbeebee@mit.edu>
 */

public class HeapCheckAdder extends Simplification {
    /** Construct a HeapCheckAdder to add checks for heap references to Tree form code.
     *  Every memory reference needs to be checked to see if it's pointing
     *  to the heap if we're in a NoHeapRealtimeThread. 
     *  Doesn't this just SOUND extremely inefficient?
     */

    private final List RULES = new ArrayList();

    /** Counter for generating unique local labels as jump points. */
    private static long count = 0;

    /** A heap check looks like this:
     *      *foo = 3;
     *
     *  =>  heapRef = foo;
     *      [*javax.realtime.Stats.heapChecks = *javax.realtime.Stats.heapChecks + 1;]
     *      if (heapRef&1) goto NoHeap; else goto TouchedHeap;
     *    TouchedHeap:
     *      [*javax.realtime.Stats.heapRefs = *javax.realtime.Stats.heapRefs + 1;]
     *      heapCheck(heapRef);
     *    NoHeap:
     *      *(heapRef&(~3)) = 3;
     *
     *      bar = *foo; 
     *  
     *  =>  heapRef = foo;
     *      [*javax.realtime.Stats.heapChecks = *javax.realtime.Stats.heapChecks + 1;]
     *      if (heapRef&1) goto NoHeap; else goto TouchedHeap;
     *    TouchedHeap:
     *      [*javax.realtime.Stats.heapRefs = *javax.realtime.Stats.heapRefs + 1;]
     *      heapCheck(heapRef);
     *    NoHeap:
     *      bar = *(heapRef&(~3);
     */
    public HeapCheckAdder() {
	super();
	
	RULES.add(new Rule("Heap check and pointer mask") {
		// *foo -> heapRef = foo;
		//         [*javax.realtime.Stats.heapChecks = *javax.realtime.Stats.heapChecks + 1;]
		//         if (heapRef&1) goto NoHeap; else goto TouchedHeap;
		//       TouchedHeap:
		//         [*javax.realtime.Stats.heapRefs = *javax.realtime.Stats.heapRefs + 1;]
		//         heapCheck(heapRef);
		//       NoHeap:
		//         *(heapRef&(~3)
		//
		// where !MATCH(foo, *(TEMP&(~3))) &&
		//       !MATCH(foo, *javax.realtime.Stats.heapChecks) &&
		//       !MATCH(foo, *javax.realtime.Stats.heapRefs) &&
		//       *foo is not in *foo = bar
		public boolean match(Exp e) {
		    return (contains(_KIND(e), _MEM)&&
			    (!(contains(_KIND(e.getParent()), _MOVE)&&
			       (((MOVE)e.getParent()).getDst()==e)))&&(!matchMask((MEM)e)));
		}
		public Exp apply(TreeFactory tf, Exp e, DerivationGenerator dg) {
		    Temp t = new Temp(tf.tempFactory(), "heapRef");
		    MEM mem = (MEM)e;
		    ESEQ es = new ESEQ(tf, e, addCheck(tf, mem, dg, t), 
				      memRef(tf, mem, dg, t));
		    dg.update(e, es);
		    return es;
		}
	    });

	RULES.add(new Rule("Heap check and pointer mask for MOVE(MEM(e), f)") {
		// *foo = bar -> heapRef = foo;
		//               [*javax.realtime.Stats.heapChecks = *javax.realtime.Stats.heapChecks + 1;]
		//               if (heapRef&1) goto NoHeap; else goto TouchedHeap;
		//             TouchedHeap:
		//               [*javax.realtime.Stats.heapRefs = *javax.realtime.Stats.heapRefs + 1;]
		//               heapCheck(heapRef);
		//             NoHeap:
		//               *(heapRef&(~3) = bar
		//
		// where !MATCH(foo, *(TEMP&(~3))) &&
		//       !MATCH(foo, *javax.realtime.Stats.heapChecks) &&
		//       !MATCH(foo, *javax.realtime.Stats.heapRefs) 
		public boolean match(Stm e) {
		    return (contains(_KIND(e), _MOVE)&&
			    contains(_KIND(((MOVE)e).getDst()), _MEM)&&
			    (!matchMask((MEM)(((MOVE)e).getDst()))));

		}
		public Stm apply(TreeFactory tf, Stm e, DerivationGenerator dg) {
		    Temp t = new Temp(tf.tempFactory(), "heapRef");
		    MEM mem = (MEM)(((MOVE)e).getDst());
		    List stmList = new ArrayList();
		    stmList.add(addCheck(tf, mem, dg, t));
		    stmList.add(new MOVE(tf, mem, memRef(tf, mem, dg, t), ((MOVE)e).getSrc()));
		    return Stm.toStm(stmList);
		}
	    });
    }

    /** MATCH(*(t&(~3))) ||
     *  MATCH(*javax.realtime.Stats.heapChecks) || 
     *  MATCH(*javax.realtime.Stats.heapRefs)
     */

    protected static boolean matchMask(MEM mem) {
	if (contains(_KIND(mem.getExp()), _BINOP)) {
	    BINOP bop = (BINOP)(mem.getExp());
	    if (contains(_OP(bop.op), _AND)&&
		contains(_KIND(bop.getRight()), _UNOP)) {
		UNOP unop = (UNOP)(bop.getRight());
		if ((unop.op == Uop.NOT)&&
		    contains(_KIND(unop.getOperand()), _CONST)) {
		    CONST c = (CONST)(unop.getOperand());
		    if (c.value().intValue() == 3) {
			return contains(_KIND(bop.getLeft()), _TEMP);
		    }
		}
	    }
	}
	if (contains(_KIND(mem.getExp()), _NAME)) {
	    Label label = ((NAME)mem.getExp()).label;
	    TreeFactory tf = mem.getFactory();
	    return ((label == forField(tf, "javax.realtime.Stats", "heapChecks"))||
		    (label == forField(tf, "javax.realtime.Stats", "heapRefs")));
	}
	return false;
    }

    /**  t = src;
     *	 [*javax.realtime.Stats.heapChecks = *javax.realtime.Stats.heapChecks + 1;]
     *   if (heapRef&1) goto NoHeap; else goto TouchedHeap;
     * TouchedHeap:
     *   [*javax.realtime.Stats.heapRefs = *javax.realtime.Stats.heapRefs + 1;]
     *   heapCheck(t);
     * NoHeap:  
     */
    protected static Stm addCheck(TreeFactory tf, MEM e, 
				DerivationGenerator dg, Temp t) {
	Label ex = new Label("TouchedHeap"+(++count));
	Label ord = new Label("NoHeap"+count);
	List stmList = new ArrayList();
	stmList.add(new MOVE(tf, e, tempRef(dg, tf, e, t), e.getExp()));
	if (Realtime.COLLECT_RUNTIME_STATS) {
	    Frame f = tf.getFrame();
	    stmList.add(incLongField(tf, e, dg, "javax.realtime.Stats", "heapChecks"));
	}
	stmList.add(new CJUMP(tf, e, 
			      DECLARE(dg, new DList(t, true, null), 
				      new BINOP(tf, e, Type.POINTER, Bop.AND, 
						tempRef(dg, tf, e, t), 
						new CONST(tf, e, 1))), ex, ord));
	stmList.add(new LABEL(tf, e, ex, false));
	if (Realtime.COLLECT_RUNTIME_STATS) {
	    stmList.add(incLongField(tf, e, dg, "javax.realtime.Stats", "heapRefs"));
	}
	stmList.add(nativeCall(tf, e, dg, t,
			       Realtime.DEBUG_REF?"heapCheckRef":"heapCheckJava"));
	stmList.add(new LABEL(tf, e, ord, false));
	return Stm.toStm(stmList);
    }
    
    /** *foo.bar = *foo.bar + 1; */
    protected static Stm incLongField(TreeFactory tf, HCodeElement e,
				    DerivationGenerator dg, 
				    String className, String fieldName) {
	return new MOVE(tf, e, 
			fieldRef(tf, e, dg, HClass.Long, 
				 Type.LONG, className, fieldName),
			new BINOP(tf, e, Type.LONG, Bop.ADD, 
				  fieldRef(tf, e, dg, HClass.Long, 
					   Type.LONG, className, fieldName),
				  new CONST(tf, e, (long)1)));
    }

    /** *foo.bar */
    protected static MEM fieldRef(TreeFactory tf, HCodeElement e,
				DerivationGenerator dg, HClass type, int iType,
				String className, String fieldName) {
	return (MEM)DECLARE(dg, type,
			    new MEM(tf, e, iType, 
				    (NAME)DECLARE(dg, type, 
						  new NAME(tf, e, 
							   forField(tf, className, 
								    fieldName)))));
				
    }

    /** foo.bar */
    protected static Label forField(TreeFactory tf, String className, String fieldName) {
	Frame f = tf.getFrame();
	HField field = f.getLinker().forName(className).getDeclaredField(fieldName);
	return f.getRuntime().nameMap.label(field);
    }
  
    /** func_name(t) */
    protected static NATIVECALL nativeCall(TreeFactory tf, MEM e, 
					 DerivationGenerator dg, Temp t,
					 String func_name) {
	Label func = new Label(tf.getFrame().getRuntime().nameMap
			       .c_function_name(func_name));
	ExpList extraArgs = null;
	if (Realtime.DEBUG_REF) {
	    extraArgs = new ExpList(new CONST(tf, e, e.getLineNumber()),
				    new ExpList(new NAME(tf, e, 
							 RealtimeAllocationStrategy
							 .fileLabel(e)),
						null));
	}
	return new NATIVECALL(tf, e, null,
			      (NAME)DECLARE(dg, HClass.Void, new NAME(tf, e, func)),
			      new ExpList(tempRef(dg, tf, e, t), extraArgs));
    }

    /** *(t&(~3)) */
    protected static MEM memRef(TreeFactory tf, MEM e, DerivationGenerator dg,
			      Temp t) {
	MEM m = new MEM(tf, e, e.type(),
			DECLARE(dg, 
				new DList(t, true, null),
				new BINOP(tf, e, Type.POINTER, Bop.AND, 
					  tempRef(dg, tf, (MEM)e, t),
					  new UNOP(tf, e, Type.INT, Uop.NOT, 
						   new CONST(tf, e, 3)))));
	dg.update(e, m);
	return m;
    }
    
    /** t */
    protected static TEMP tempRef(DerivationGenerator dg, TreeFactory tf, 
				MEM e, Temp t) {
	return (TEMP)DECLARE(dg, HClass.Void, t, new TEMP(tf, e, Type.POINTER, t));
    }

    /** Code factory for adding heap checks to the given tree form.
     *  Clones the tree before processing it in-place. 
     */

    public HCodeFactory codeFactory(final HCodeFactory parent) {
//  	return new PrintFactory(Canonicalize.codeFactory(codeFactory(new PrintFactory(parent, "BEFORE"), 
//  								     RULES)), "AFTER");
	return Canonicalize.codeFactory(codeFactory(parent, RULES));
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
