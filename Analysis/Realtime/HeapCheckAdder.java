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
import harpoon.ClassFile.HCode;
import harpoon.ClassFile.HCodeElement;
import harpoon.ClassFile.HCodeFactory;
import harpoon.ClassFile.HField;
import harpoon.ClassFile.HMethod;

import harpoon.IR.Tree.BINOP;
import harpoon.IR.Tree.Bop;
import harpoon.IR.Tree.CALL;
import harpoon.IR.Tree.CJUMP;
import harpoon.IR.Tree.Code;
import harpoon.IR.Tree.CONST;
import harpoon.IR.Tree.DerivationGenerator;
import harpoon.IR.Tree.Exp;
import harpoon.IR.Tree.ExpList;
import harpoon.IR.Tree.ESEQ;
import harpoon.IR.Tree.LABEL;
import harpoon.IR.Tree.MEM;
import harpoon.IR.Tree.METHOD;
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
import java.util.HashSet;
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

    /** Have I already dealt with this instruction? */
    protected static HashSet seenList = new HashSet();

    /** Should I use the low bit or the high bit? */
    public static boolean USE_LOW_BIT = true;

    /** A heap check looks like this:
     *      *foo = bar;
     *
     *  =>  heapRef1 = *foo;
     *      [*javax.realtime.Stats.heapChecks = *javax.realtime.Stats.heapChecks + 1;]
     *      if (heapRef1&1) goto NoHeap0; else goto TouchedHeap0;
     *    TouchedHeap0:
     *      [*javax.realtime.Stats.heapRefs = *javax.realtime.Stats.heapRefs + 1;]
     *      heapCheck(heapRef1);
     *    NoHeap0:
     *      *foo = bar;
     *
     *      bar = *foo; 
     *  
     *  =>  heapRef1 = *foo;
     *      [*javax.realtime.Stats.heapChecks = *javax.realtime.Stats.heapChecks + 1;]
     *      if (heapRef2&1) goto NoHeap1; else goto TouchedHeap1;
     *    TouchedHeap1:
     *      [*javax.realtime.Stats.heapRefs = *javax.realtime.Stats.heapRefs + 1;]
     *      heapCheck(heapRef1);
     *    NoHeap1:
     *      bar = heapRef1;
     *
     *      foo = NATIVECALL(....);
     *
     *  =>  heapRef1 = NATIVECALL(....);
     *      [*javax.realtime.Stats.heapChecks = *javax.realtime.Stats.heapChecks + 1;]
     *      if (heapRef1&1) goto NoHeap0; else goto TouchedHeap0;
     *    TouchedHeap0:
     *      [*javax.realtime.Stats.heapChecks = *javax.realtime.Stats.heapRefs + 1;]      
     *      heapCheck(heapRef1);
     *    NoHeap0:
     *      foo = heapRef1;
     *
     *      foo = CALL(....);
     *
     *      heapRef1 = CALL(....);
     *      [*javax.realtime.Stats.heapChecks = *javax.realtime.Stats.heapChecks + 1;]
     *      if (heapRef1&1) goto NoHeap0; else goto TouchedHeap0;
     *    TouchedHeap0:
     *      [*javax.realtime.Stats.heapChecks = *javax.realtime.Stats.heapRefs + 1;]
     *      heapCheck(heapRef1);
     *    NoHeap0:
     *      foo = heapRef1;
     *
     *      METHOD(params[]);
     *
     *  =>  foreach params st. params[i] is of type POINTER:
     *          [*javax.realtime.Stats.heapChecks = *javax.realtime.Stats.heapChecks + 1;]
     *          if (heapRef1&1) goto NoHeapi; else goto TouchedHeapi;
     *        TouchedHeapi:
     *          [*javax.realtime.Stats.heapChecks = *javax.realtime.Stats.heapRefs + 1;]
     *          heapCheck(params[i]);
     *        NoHeapi:
     *          
     *  Isn't that awful?
     *
     * - On load (of a base POINTER)
     * - On method call (check formal parameters if POINTER)
     * - Return of a method CALL if POINTER
     * - Return of a NATIVECALL if POINTER
     * - On store (of POINTER type) 
     *   (possible clobbering of a root during GC ref. forwarding may clobber newly stored 
     *    value).
     */

    public HeapCheckAdder() {
	super();
	
	RULES.add(new Rule("Heap check for read") {
		// *foo => above
		// where I haven't seen this before.
		//       *foo is not in *foo = bar
		public boolean match(Exp e) {
		    return (contains(_KIND(e), _MEM)&&
			    (!(contains(_KIND(e.getParent()), _MOVE)&&
			       (((MOVE)e.getParent()).getDst()==e)))&&
			    (((MEM)e).type() == Type.POINTER)&&
			    (!seenList.contains(e)));
		}
		public Exp apply(TreeFactory tf, Exp e, DerivationGenerator dg) {
		    MEM mem = (MEM)e;
		    Exp result = UPDATE(dg, e, mem.build(tf, mem.kids()));
		    seenList.add(result);
		    seenList.add(mem);
		    Temp t = new Temp(tf.tempFactory(), "heapRef");
		    result = 
			new ESEQ(tf, e, 
				 new MOVE(tf, e, tempRef(dg, tf, mem, t), result),
				 new ESEQ(tf, e, addCheck(tf, mem, dg, t),
					  tempRef(dg, tf, mem, t)));
		    UPDATE(dg, e, result);
		    return result;
		}
	    });

	// Not sure about this rule -> profile with and without, debug with...
	RULES.add(new Rule("Heap check for write") {
		// *foo = bar => above
		// where I haven't seen this before.
		public boolean match(Stm e) {
		    return (contains(_KIND(e), _MOVE)&&
			    contains(_KIND(((MOVE)e).getDst()), _MEM)&&
			    (((MEM)(((MOVE)e).getDst())).type() == Type.POINTER)&&
			    (!seenList.contains((MEM)(((MOVE)e).getDst()))));

		}
		public Stm apply(TreeFactory tf, Stm e, DerivationGenerator dg) {
		    MEM mem = (MEM)(((MOVE)e).getDst());
		    seenList.add(mem);
		    Temp t = new Temp(tf.tempFactory(), "heapRef");
		    List stmList = new ArrayList();
		    stmList.add(new MOVE(tf, e, tempRef(dg, tf, mem, t), mem));
		    stmList.add(addCheck(tf, mem, dg, t));
		    stmList.add(new MOVE(tf, mem, mem, ((MOVE)e).getSrc()));
		    return Stm.toStm(stmList);
		}
	    });

	RULES.add(new Rule("Heap check for NATIVECALL") {
		// foo = NATIVECALL() above
		public boolean match(Stm e) {
		    return (contains(_KIND(e), _NATIVECALL)&&
			    (((NATIVECALL)e).getRetval() != null)&&
			    (((NATIVECALL)e).getRetval().type() == Type.POINTER)&&
			    (!seenList.contains(e)));
		}

		public Stm apply(TreeFactory tf, Stm e, DerivationGenerator dg) {
		    seenList.add(e);
		    NATIVECALL nc = (NATIVECALL)e;
		    TEMP ret = nc.getRetval();
		    List stmList = new ArrayList();
		    stmList.add(nc = new NATIVECALL(nc.getFactory(), nc, ret, 
						    nc.getFunc(), nc.getArgs()));
		    seenList.add(nc);
		    stmList.add(addCheck(tf, ret, dg, ret.temp));
		    return Stm.toStm(stmList);
		}

	    });
		
	RULES.add(new Rule("Heap check for CALL") {
		public boolean match(Stm e) {
		    return (contains(_KIND(e), _CALL)&&
			    (((CALL)e).getRetval() != null)&&
			    (((CALL)e).getRetval().type() == Type.POINTER)&&
			    (!seenList.contains(e)));
		}

		public Stm apply(TreeFactory tf, Stm e, DerivationGenerator dg) {
		    seenList.add(e);
		    CALL c = (CALL)e;
		    TEMP ret = c.getRetval();
		    List stmList = new ArrayList();
		    stmList.add(c = new CALL(tf, c, ret, c.getRetex(), c.getFunc(), 
					     c.getArgs(), c.getHandler(), 
					     c.isTailCall));
		    seenList.add(c);
		    stmList.add(addCheck(tf, ret, dg, ret.temp));
		    return Stm.toStm(stmList);
		}

	    });

	RULES.add(new Rule("Heap check for METHOD") {
		public boolean match(Stm e) {
		    return (contains(_KIND(e), _METHOD)&&
			    (!seenList.contains(e)));
		}

		public Stm apply(TreeFactory tf, Stm e, DerivationGenerator dg) {
		    seenList.add(e);
		    METHOD m = (METHOD)e;
		    TEMP[] params = m.getParams();
		    List stmList = new ArrayList();
		    stmList.add(m = new METHOD(tf, m, m.getMethod(),
					       m.getReturnType(), params));
		    seenList.add(m);
		    for (int i=1; i<params.length; i++) { 
			if (params[i].type == Type.POINTER) {
			    stmList.add(addCheck(tf, params[i], dg, params[i].temp));
			}
		    }		     
		    return Stm.toStm(stmList);
		}

	    });
		
    }

    /**  [*javax.realtime.Stats.heapChecks = *javax.realtime.Stats.heapChecks + 1;]
     *   if (t&1) goto NoHeap; else goto TouchedHeap;
     * TouchedHeap:
     *   [*javax.realtime.Stats.heapRefs = *javax.realtime.Stats.heapRefs + 1;]
     *   heapCheck(t);
     * NoHeap:  
     */
    protected static Stm addCheck(TreeFactory tf, Exp e, 
				  DerivationGenerator dg, Temp t) {
	Label ex = new Label("TouchedHeap"+(++count));
	Label ord = new Label("NoHeap"+count);
	List stmList = new ArrayList();
	if (Realtime.COLLECT_RUNTIME_STATS) {
	    Frame f = tf.getFrame();
	    stmList.add(incLongField(tf, e, dg, "javax.realtime.Stats", "heapChecks"));
	}
	stmList.add(new CJUMP(tf, e, 
			      DECLARE(dg, new DList(t, true, null), 
				      new BINOP(tf, e, Type.POINTER, Bop.AND, 
						tempRef(dg, tf, e, t), 
						new CONST(tf, e, USE_LOW_BIT?1:2))), ex, ord));
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
	// Not for small (Boolean) fields!
	MEM m = new MEM(tf, e, iType, 
			(NAME)DECLARE(dg, type, 
				      new NAME(tf, e, 
					       forField(tf, className, fieldName))));
	seenList.add(m);
	return (MEM)DECLARE(dg, type, m);
			    
				
    }

    /** foo.bar */
    protected static Label forField(TreeFactory tf, String className, String fieldName) {
	Frame f = tf.getFrame();
	HField field = f.getLinker().forName(className).getDeclaredField(fieldName);
	return f.getRuntime().getNameMap().label(field);
    }
  
    /** func_name(t) */
    protected static NATIVECALL nativeCall(TreeFactory tf, Exp e, 
					   DerivationGenerator dg, Temp t,
					   String func_name) {
	Label func = new Label(tf.getFrame().getRuntime().getNameMap()
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

    /** *(t&(~3)), now *t */
    protected static MEM memRef(TreeFactory tf, MEM e, DerivationGenerator dg,
				Temp t) {
	Exp exp = tempRef(dg, tf, e.getExp(), t);
	MEM m = (e.isSmall())?(new MEM(tf, e, e.bitwidth(), e.signed(), exp)):
	    (new MEM(tf, e, e.type(), exp));
	seenList.add(UPDATE(dg, e, m));
	return m;
    }
    
    /** t */
    protected static TEMP tempRef(DerivationGenerator dg, TreeFactory tf, 
				  Exp e, Temp t) {
	TEMP temp = new TEMP(tf, e, Type.POINTER, t);
	if (dg.typeMap(e) != null) {
	    return (TEMP)DECLARE(dg, dg.typeMap(e), t, temp);
	} else {
	    return (TEMP)DECLARE(dg, dg.derivation(e), temp);
	}
    }

    /** Code factory for adding heap checks to the given tree form.
     *  Clones the tree before processing it in-place. 
     */

    public HCodeFactory codeFactory(final HCodeFactory parent) {
	HCodeFactory hcf = new HCodeFactory() {
		public HCode convert(HMethod m) {
		    seenList.clear(); /* To conserve memory. */
		    return parent.convert(m);
		}
		public String getCodeName() { return parent.getCodeName(); }
		public void clear(HMethod m) { parent.clear(m); }
	    };
//  	return new PrintFactory(Canonicalize.codeFactory(codeFactory(new PrintFactory(hcf, "BEFORE"), 
//  								     RULES)), "AFTER");
	return Canonicalize.codeFactory(codeFactory(hcf, RULES));
//  	return parent;
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

    protected static Exp UPDATE(DerivationGenerator dg, Exp oldExp, Exp newExp) {
	if (dg.typeMap(oldExp) != null) {
	    return DECLARE(dg, dg.typeMap(oldExp), newExp);
	} else {
	    return DECLARE(dg, dg.derivation(oldExp), newExp);
	}
    }
}
