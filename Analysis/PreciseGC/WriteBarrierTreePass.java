// WriteBarrierTreePass.java, created Tue Aug 21 20:20:44 2001 by kkz
// Copyright (C) 2000 Karen Zee <kkz@tmi.lcs.mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.PreciseGC;

import harpoon.Analysis.ClassHierarchy;
import harpoon.Analysis.Tree.Canonicalize;
import harpoon.Backend.Generic.Frame;
import harpoon.Backend.Generic.Runtime;
import harpoon.Backend.Generic.Runtime.TreeBuilder;
import harpoon.Backend.Maps.NameMap;
import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HCode;
import harpoon.ClassFile.HCodeElement;
import harpoon.ClassFile.HCodeFactory;
import harpoon.ClassFile.HField;
import harpoon.ClassFile.HMethod;
import harpoon.ClassFile.Linker;
import harpoon.IR.Properties.CFGrapher;
import harpoon.IR.Tree.BINOP;
import harpoon.IR.Tree.Bop;
import harpoon.IR.Tree.CALL;
import harpoon.IR.Tree.CanonicalTreeCode;
import harpoon.IR.Tree.CONST;
import harpoon.IR.Tree.DATUM;
import harpoon.IR.Tree.DerivationGenerator;
import harpoon.IR.Tree.Exp;
import harpoon.IR.Tree.ExpList;
import harpoon.IR.Tree.LABEL;
import harpoon.IR.Tree.METHOD;
import harpoon.IR.Tree.NAME;
import harpoon.IR.Tree.NATIVECALL;
import harpoon.IR.Tree.Print;
import harpoon.IR.Tree.SEGMENT;
import harpoon.IR.Tree.SEQ;
import harpoon.IR.Tree.Stm;
import harpoon.IR.Tree.Tree;
import harpoon.IR.Tree.TreeFactory;
import harpoon.IR.Tree.TEMP;
import harpoon.IR.Tree.Translation;
import harpoon.IR.Tree.Type;
import harpoon.Temp.Label;
import harpoon.Util.Util;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
/**
 * <code>WriteBarrierTreePass</code> performs some low-level 
 * transformations to the output of <code>WriteBarrierQuadPass</code> 
 * which cannot be done in the quad form on which 
 * <code>WriteBarrierQuadPass</code> operates. 
 * <code>>WriteBarrierTreePass</code> works on tree form.
 * <p>
 * This pass is invoked by 
 * <code>WriteBarrierQuadPass.treeCodeFactory()</code>.
 * 
 * 
 * @author  Karen Zee <kkz@tmi.lcs.mit.edu>
 * @version $Id: WriteBarrierTreePass.java,v 1.4 2002-04-10 03:00:53 cananian Exp $
 */
public abstract class WriteBarrierTreePass extends 
    harpoon.Analysis.Tree.Simplification {

    // hide constructor
    private WriteBarrierTreePass() { }

    /** Code factory for applying <code>WriteBarrierTreePass</code>
     *  to a canonical tree.  Clones the tree before doing
     *  transformation in-place. */
    static HCodeFactory codeFactory(final HCodeFactory parent,
				    final Frame f,
				    final ClassHierarchy ch,
				    final HMethod arrayHM,
				    final HMethod fieldHM) {
	assert parent.getCodeName().equals(CanonicalTreeCode.codename);
	return Canonicalize.codeFactory(new HCodeFactory() {
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
		    simplify((Stm)code.getRootElement(), dg, 
			     HCE_RULES(f, ch, arrayHM, fieldHM));
		    hc = code;
		}
		return hc;
	    }
	    public String getCodeName() { return parent.getCodeName(); }
	    public void clear(HMethod m) { parent.clear(m); }
	});
    }

    private static List HCE_RULES(final Frame f, 
				  final ClassHierarchy ch, 
				  final HMethod arrayHM, 
				  final HMethod fieldHM) {
	final NameMap nm = f.getRuntime().getNameMap();
        final Label LarrayHM = nm.label(arrayHM);
	final Label LfieldHM = nm.label(fieldHM);
	final Map fieldMap = new HashMap();
	for(Iterator it = ch.classes().iterator(); it.hasNext(); ) {
	    HField[] fields = ((HClass) it.next()).getFields();
	    for(int i = 0; i < fields.length;  i++) {
		fieldMap.put(nm.label(fields[i], "obj"), fields[i]);
	    }
	}
	final Label cfunc = 
	    new Label(nm.c_function_name("generational_write_barrier"));
	// now make rules
	return Arrays.asList(new Rule[] {
	    new Rule("replaceArraySC") {
		public boolean match(Stm stm) {
		    if (!contains(_KIND(stm), _CALL)) return false;
		    CALL call = (CALL) stm;
		    if (!contains(_KIND(call.getFunc()), _NAME)) return false;
		    return ((NAME) call.getFunc()).label.equals(LarrayHM);
		}
		public Stm apply(TreeFactory tf, Stm stm, 
				 DerivationGenerator dg) {
		    //System.out.println(Print.print(stm));
		    Runtime runtime = tf.getFrame().getRuntime();
		    TreeBuilder tb = runtime.getTreeBuilder();
		    final NAME func = new NAME(tf, stm, cfunc);
		    if (dg != null) dg.putType(func, HClass.Void);
		    // extract arguments
		    ExpList explist = ((CALL) stm).getArgs();
		    // first argument
		    TEMP objectref = (TEMP) explist.head;
		    explist = explist.tail;
		    // second argument is the array index
		    // after constant propagation, this
		    // may be either a TEMP or a CONST
		    Exp index = explist.head;
		    // ignore last 2 arguments
		    explist = explist.tail.tail;
		    // no more arguments
		    assert explist.tail == null;
		    // get array type from derivation
		    assert dg != null;
		    HClass arrayType = dg.typeMap(objectref);
		    assert arrayType != null;
		    return new NATIVECALL
			(tf, stm, null, func, new ExpList
			 (new BINOP
			  (tf, stm, Type.POINTER, Bop.ADD,
			   tb.arrayBase
			   (tf, stm, dg, new Translation.Ex
			    (objectref)).unEx(tf),
			   tb.arrayOffset
			   (tf, stm, dg, arrayType, new Translation.Ex
			    (index)).unEx(tf)), 
			  null));
		}
	    },
	    new Rule("replaceFieldSC") {
		public boolean match(Stm stm) {
		    if (!contains(_KIND(stm), _CALL)) return false;
		    CALL call = (CALL) stm;
		    if (!contains(_KIND(call.getFunc()), _NAME)) return false;
		    return ((NAME) call.getFunc()).label.equals(LfieldHM);
		}
		public Stm apply(TreeFactory tf, Stm stm, 
				 DerivationGenerator dg) {
		    Runtime runtime = tf.getFrame().getRuntime();
		    TreeBuilder tb = runtime.getTreeBuilder();
		    final NAME func =
			new NAME(tf, stm, 
				 new Label(runtime.getNameMap().c_function_name
					   ("generational_write_barrier")));
		    if (dg != null) dg.putType(func, HClass.Void);
		    // extract arguments
		    ExpList explist = ((CALL) stm).getArgs();
		    // first argument
		    TEMP objectref = (TEMP) explist.head;
		    explist = explist.tail;
		    // second argument
		    NAME name = (NAME) explist.head;
		    // ignore last 2 arguments
		    explist = explist.tail.tail;
		    // no more arguments
		    assert explist.tail == null;
		    // get HField from fieldMap
		    HField field = (HField) fieldMap.get(name.label);
		    assert field != null;
		    return new NATIVECALL
			(tf, stm, null, func, new ExpList
			 (new BINOP
			  (tf, stm, Type.POINTER, Bop.ADD, tb.fieldBase
			   (tf, stm, dg, new Translation.Ex
			    (objectref)).unEx(tf), tb.fieldOffset
			   (tf, stm, dg, field).unEx(tf)), 
			  null));
		}
	    }
	});
    }
}

