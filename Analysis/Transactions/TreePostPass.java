// TreePostPass.java, created Thu Jan 11 16:04:23 2001 by cananian
// Copyright (C) 2000 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.Transactions;

import harpoon.Analysis.Maps.Derivation;
import harpoon.Backend.Generic.Frame;
import harpoon.Backend.Generic.Runtime.TreeBuilder;
import harpoon.Backend.Maps.NameMap;
import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HCodeFactory;
import harpoon.ClassFile.HField;
import harpoon.ClassFile.HMethod;
import harpoon.IR.Tree.BINOP;
import harpoon.IR.Tree.Bop;
import harpoon.IR.Tree.CALL;
import harpoon.IR.Tree.CONST;
import harpoon.IR.Tree.DerivationGenerator;
import harpoon.IR.Tree.Exp;
import harpoon.IR.Tree.ExpList;
import harpoon.IR.Tree.MEM;
import harpoon.IR.Tree.MOVE;
import harpoon.IR.Tree.NAME;
import harpoon.IR.Tree.NATIVECALL;
import harpoon.IR.Tree.SEQ;
import harpoon.IR.Tree.Stm;
import harpoon.IR.Tree.TEMP;
import harpoon.IR.Tree.Translation;
import harpoon.IR.Tree.TreeFactory;
import harpoon.IR.Tree.Type;
import harpoon.Temp.Label;
import harpoon.Temp.Temp;
import harpoon.Util.Util;

import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
/**
 * <code>TreePostPass</code> performs some low-level transformations to
 * the output of <code>SyncTransformer</code> which cannot be done in
 * the quad form which <code>SyncTransformer</code> operates on.
 * <code>TreePostPass</code> works on tree form.
 * <p>
 * This pass is invoked by <code>SyncTransformer.treeCodeFactory()</code>.
 * 
 * @author   C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: TreePostPass.java,v 1.4.2.1 2003-07-12 03:31:07 cananian Exp $
 */
class TreePostPass extends harpoon.Analysis.Tree.Simplification {
    private final List<Rule> RULES = new ArrayList<Rule>(); 
    TreePostPass(final Frame f, final long FLAG_VALUE, HField HFflagvalue,
		 final MethodGenerator gen, final Set<HField> transFields) {
	final Label Lflagvalue = f.getRuntime().getNameMap().label(HFflagvalue);
	NameMap nm = f.getRuntime().getNameMap();
	// map generated methods to labels.
	final Map<Label,HMethod> label2method = new HashMap<Label,HMethod>();
	for (Iterator<HMethod> it=gen.generatedMethodSet().iterator();
	     it.hasNext(); ) {
	    HMethod hm = it.next();
	    label2method.put(nm.label(hm), hm);
	}
	// map referenced fields to labels.
	final Map<Label,HField> label2field = new HashMap<Label,HField>();
	for (Iterator<HField> it=transFields.iterator(); it.hasNext(); ) {
	    HField hf = it.next();
	    label2field.put(nm.label(hf, "obj"), hf);
	}
	// cache tree builder
	final TreeBuilder tb = f.getRuntime().getTreeBuilder();

	// -----------------------------------------------------------
	// add all rules to rule set
	//          ...remove mentions of Object.flagValue field...
	// MEM(NAME(Label(flagValue))) -> CONST(0xCACACACAL)
	RULES.add(new Rule("constFlagValue") {
	    public boolean match(Exp e) {
		if (!contains(_KIND(e), _MEM)) return false;
		MEM mem = (MEM) e;
		if (!contains(_KIND(mem.getExp()), _NAME)) return false;
		NAME name = (NAME) mem.getExp();
		// check label.
		return name.label.equals(Lflagvalue);
	    }
	    public Exp apply(TreeFactory tf, Exp e, DerivationGenerator dg) {
		MEM e1 = (MEM) e;
		NAME e2 = (NAME) e1.getExp();
		if (dg!=null) { dg.remove(e1); dg.remove(e2); }
		CONST c = f.pointersAreLong() ?
		    new CONST(tf, e, FLAG_VALUE) :
		    new CONST(tf, e, (int) FLAG_VALUE);
		if (dg!=null) { dg.putType(c, HClass.Void); }
		return c;
	    }
	});
	// other uses of flagValue are errors.
	// NAME(Label(flagValue)) -> CONST(0) (and assert false)
	RULES.add(new Rule("assertFlagValue") {
	    public boolean match(Exp e) {
		if (contains(_KIND(e.getParent()), _MEM)) return false;
		if (!contains(_KIND(e), _NAME)) return false;
		NAME name = (NAME) e;
		// check label.
		boolean b = name.label.equals(Lflagvalue);
		assert !b : "flag value slipped past us!";
		return b;
	    }
	    // otherwise a very visible (we hope) runtime error.
	    public Exp apply(TreeFactory tf, Exp e, DerivationGenerator dg) {
		return new CONST(tf, e); // null pointer
	    }
	});

	// turn calls to certain native methods into direct NATIVECALLs
	// CALL(retval,retex,NAME(xxx),args,handler,false) ->
	//    NATIVECALL(retval,NAME(Lnative_isSync),ExpList(arg,null))
	// or
	//    MOVE(retval,BINOP(AND,MEM(BINOP(ADD,arg,CONST(OBJ_HASH_OFF))),2))
	RULES.add(new Rule("isSyncRewrite2") {
	    public boolean match(Stm s) {
		if (!contains(_KIND(s), _CALL)) return false;
		CALL call = (CALL) s;
		if (!contains(_KIND(call.getFunc()), _NAME)) return false;
		NAME name = (NAME) call.getFunc();
		if (!label2method.containsKey(name.label)) return false;
		String baseName = gen.baseName(label2method.get(name.label))
		    .intern();
		if (baseName=="arrayReadNT" || //baseName=="arrayReadT" ||
		    false/*baseName=="readNT" || baseName=="readT"*/)
		    return true; // yup, this is a match!
		return false;
	    }
	    public Stm apply(TreeFactory tf, Stm s, DerivationGenerator dg) {
		CALL call = (CALL) s;
		NAME name = (NAME) call.getFunc();
		HMethod hm = label2method.get(name.label);
		String baseName = gen.baseName(hm).intern();
		if (baseName=="arrayReadNT") {
		    ExpList args = call.getArgs();
		    Exp objPtr = args.head; args=args.tail;
		    Exp index = args.head; args=args.tail;
		    assert args==null;
		    HClass retType = hm.getReturnType();
		    String methodName = "EXACT_"+baseName+"_"+
			(retType.isPrimitive() ? retType.getName() : "Object");
		    Label Lmethod = new Label(methodName);
		    HClass objType = hm.getParameterTypes()[0];
		    // make temporary for object base pointer.
		    Temp Tobj = new Temp(tf.tempFactory(), "objBase");
		    Stm s0 = new MOVE(tf, s, DECLARE
				      (dg, objType, Tobj,
				       new TEMP(tf, s, Type.POINTER, Tobj)),
				      objPtr);
		    // compute direct pointer.
		    Exp elePtr=new BINOP(tf, s, Type.POINTER, Bop.ADD,
					 tb.arrayBase
					 (tf, s, dg,
					  new Translation.Ex
					  (DECLARE
					   (dg, objType, Tobj,
					    new TEMP(tf, s, Type.POINTER, Tobj)
					    ))).unEx(tf),
					 tb.arrayOffset
					 (tf, s, dg, objType,
					  new Translation.Ex(index)).unEx(tf));

		    Stm s1 = new NATIVECALL(tf, s, call.getRetval(),
					    new NAME(tf, s, Lmethod),
					    new ExpList
					    (DECLARE(dg, objType, Tobj,
					     new TEMP(tf, s, Type.POINTER,
						      Tobj)),
					     new ExpList(elePtr, null)));
		    return new SEQ(tf, s, s0, s1);
		} else {
		    assert false;
		    return s;
		}
	    }
        });
    }
    /** Code factory for applying the post pass to the given tree
     *  form.  Clones the tree before processing it in-place. */
    public HCodeFactory codeFactory(final HCodeFactory parent) {
	return codeFactory(parent, RULES);
    }

    // type declaration helper methods
    protected static Exp DECLARE(DerivationGenerator dg, HClass hc, Exp exp) {
	if (dg!=null) dg.putType(exp, hc);
	return exp;
    }
    protected static Exp DECLARE(DerivationGenerator dg, HClass hc, Temp t,
				 Exp exp) {
	if (dg!=null) dg.putTypeAndTemp(exp, hc, t);
	return exp;
    }
    protected static Exp DECLARE(DerivationGenerator dg, Derivation.DList dl,
				 Exp exp) {
	if (dg!=null) dg.putDerivation(exp, dl);
	return exp;
    }
}
