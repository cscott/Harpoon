// InsertWriteBarrier.java, created Fri Aug  3 10:52:53 2001 by kkz
// Copyright (C) 2000 Karen Zee <kkz@tmi.lcs.mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Backend.Analysis;

import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HCode;
import harpoon.ClassFile.HCodeFactory;
import harpoon.ClassFile.HMethod;
import harpoon.IR.Tree.DerivationGenerator;
import harpoon.IR.Tree.Exp;
import harpoon.IR.Tree.ExpList;
import harpoon.IR.Tree.MEM;
import harpoon.IR.Tree.MOVE;
import harpoon.IR.Tree.NAME;
import harpoon.IR.Tree.NATIVECALL;
import harpoon.IR.Tree.SEQ;
import harpoon.IR.Tree.Stm;
import harpoon.IR.Tree.Print;
import harpoon.IR.Tree.TEMP;
import harpoon.IR.Tree.TreeFactory;
import harpoon.IR.Tree.Typed;
import harpoon.Temp.Label;
import harpoon.Temp.Temp;
import harpoon.Util.Util;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * <code>InsertWriteBarrier</code> instruments any writes to pointer
 * locations with a store check. This pass is used for generational
 * garbage collection, to keep track of pointers from older to
 * younger generations.
 * 
 * @author  Karen Zee <kkz@tmi.lcs.mit.edu>
 * @version $Id: InsertWriteBarrier.java,v 1.3.2.1 2002-02-27 08:34:01 cananian Exp $
 */
public abstract class InsertWriteBarrier extends 
    harpoon.Analysis.Tree.Simplification {
    
    // hide constructor
    private InsertWriteBarrier() { }

    /** Code factory for inserting write barriers to a tree.
     *  Clones the tree before doing transformation in-place.
     */
    public static HCodeFactory codeFactory(final HCodeFactory parent) {
	return new HCodeFactory() {
	    public HCode convert(HMethod m) {
		HCode hc = parent.convert(m);
		if (hc!=null) {
		    harpoon.IR.Tree.Code code = (harpoon.IR.Tree.Code) hc;
		    // clone code...
		    code = (harpoon.IR.Tree.Code) code.clone(m).hcode();
		    DerivationGenerator dg = null;
		    try {
			dg = (DerivationGenerator) code.getTreeDerivation();
		    } catch (ClassCastException ex) { /* i guess not */ }
		    // ...do analysis and modify cloned code in-place.
		    simplify((Stm)code.getRootElement(), dg, HCE_RULES(dg));
		    hc = code;
		}
		return hc;
	    }
	    public String getCodeName() { return parent.getCodeName(); }
	    public void clear(HMethod m) { parent.clear(m); }
	};
    }

    public static List HCE_RULES(final DerivationGenerator _dg) {
	// we keep track of MEMs we have created
	// since they don't need to be visited
	final Set created = new HashSet();
	// now make rules.
	return Arrays.asList(new Rule[] {
	    // this rule matches moves into memory
	    // when the subexpression of the MEM is
	    // already in a TEMP
	    new Rule("movesWithTemps") {
		public boolean match(Stm stm) {
		    if (!contains(_KIND(stm), _MOVE)) return false;
		    MOVE move = (MOVE) stm;
		    if (!contains(_KIND(move.getDst()), _MEM)) return false;
		    MEM mem = (MEM) move.getDst();
		    if (_dg != null) {
			HClass hc = _dg.typeMap(mem);
			if (hc != null && hc.isPrimitive()) return false;
			hc = _dg.typeMap(move.getSrc());
			if (hc != null && hc.isPrimitive()) return false;
		    }
		    assert move.type() == Typed.POINTER;
		    return (contains(_KIND(mem.getExp()), _TEMP) &&
			    !created.contains(move));
		}
		public Stm apply(TreeFactory tf, 
				 Stm stm, 
				 DerivationGenerator dg) {
		    // create reference to write-barrier
		    final NAME func = 
			new NAME(tf, stm, 
				 new Label(tf.getFrame().getRuntime().
					   getNameMap().c_function_name
					   ("generational_write_barrier")));
		    if (dg != null) dg.putType(func, HClass.Void);
		    // first, we need to clone the MOVE
		    // since it will be removed from the Tree
		    // we know from the match that the target
		    // is a MEM evaluated from a TEMP
		    final MOVE omove = (MOVE) stm;
		    final MEM mem = (MEM)omove.getDst();
		    final MOVE nmove = new MOVE(tf, stm, mem, omove.getSrc());
		    created.add(nmove);
		    // create a new TEMP and update the derivation information
		    final TEMP T1 = (TEMP)mem.getExp();
		    final Temp t = T1.temp;
		    final TEMP T2 = new TEMP(tf, stm, T1.type(), t);
		    if (dg != null) {
			HClass hc = dg.typeMap(T1);
			if (hc != null)
			    dg.putTypeAndTemp(T2, hc, t);
			else
			    dg.putDerivation(T2, dg.derivation(T1));
		    }
		    return new SEQ(tf, stm,
				   new NATIVECALL(tf, stm, null, func,
						  new ExpList(T2, null)), 
				   nmove);
		}
	    },
	    // this rule matches moves into memory
	    // when the subexpression of the MEM is
	    // not in a TEMP
	    new Rule("movesWithoutTemps") {
		public boolean match(Stm stm) {
		    if (!contains(_KIND(stm), _MOVE)) return false;
		    MOVE move = (MOVE) stm;
		    if (!contains(_KIND(move.getDst()), _MEM)) return false;
		    MEM mem = (MEM) move.getDst();
		    if (_dg != null) {
			HClass hc = _dg.typeMap(mem);
			if (hc != null && hc.isPrimitive()) return false;
				hc = _dg.typeMap(move.getSrc());
			if (hc != null && hc.isPrimitive()) return false;
		    }
		    assert move.type() == Typed.POINTER;
		    return (!contains(_KIND(mem.getExp()), _TEMP) &&
			    !created.contains(move));
		}
		public Stm apply(TreeFactory tf, 
				 Stm stm, 
				 DerivationGenerator dg) {
		    // create reference to write-barrier
		    final NAME func = 
			new NAME(tf, stm, 
				 new Label(tf.getFrame().getRuntime().
					   getNameMap().c_function_name
					   ("generational_write_barrier")));
		    if (dg != null) dg.putType(func, HClass.Void);
		    // we know from the match that we
		    // have a MOVE whose target is a MEM
		    final MOVE omove = (MOVE) stm;
		    final MEM omem = (MEM)omove.getDst();
		    // create a Temp to store the result of
		    // evaluating the subexpression for the MEM
		    final Exp exp = omem.getExp();
		    Temp t = new Temp(tf.tempFactory());
		    TEMP T1 = new TEMP(tf, stm, exp.type(), t);
		    TEMP T2 = new TEMP(tf, stm, exp.type(), t);
		    TEMP T3 = new TEMP(tf, stm, exp.type(), t);
		    // update the derivation information
		    if (dg != null) {
			HClass hc = dg.typeMap(exp);
			if (hc != null) {
			    dg.putTypeAndTemp(T1, hc, t);
			    dg.putTypeAndTemp(T2, hc, t);
			    dg.putTypeAndTemp(T3, hc, t);
			} else {
			    dg.putDerivation(T1, dg.derivation(exp));
			    dg.putDerivation(T2, dg.derivation(exp));
			    dg.putDerivation(T3, dg.derivation(exp));
			}
		    }
		    // create a new MOVE that assigns the Temp
		    MOVE move1 = new MOVE(tf, stm, T1, exp);
		    // create a new MEM that uses the Temp
		    MEM nmem = new MEM(tf, stm, omem.type(), T2);
		    // update the derivation information
		    dg.update(omem, nmem);
		    // do the original move using the new MEM
		    MOVE move2 = new MOVE(tf, stm, nmem, omove.getSrc());
		    created.add(move2);
		    return new SEQ(tf, stm,
				   new SEQ(tf, stm, move1,
					   new NATIVECALL(tf, stm, null, func,
							  new ExpList(T3, 
								      null))),
				   move2);
		}
	    }
	});
    }
}
