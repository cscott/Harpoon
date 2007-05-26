// TreePostPass.java, created Thu Jan 11 16:04:23 2001 by cananian
// Copyright (C) 2000 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.Transactions;

import harpoon.Analysis.Maps.Derivation;
import harpoon.Backend.Generic.Frame;
import harpoon.Backend.Generic.Runtime.TreeBuilder;
import harpoon.Backend.Maps.NameMap;
import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HCodeElement;
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
 * @version $Id: TreePostPass.java,v 1.9 2007-05-26 19:50:31 cananian Exp $
 */
class TreePostPass extends harpoon.Analysis.Tree.Simplification {
    private final List<Rule> RULES = new ArrayList<Rule>(); 
    final Map<Label,HField> label2field = new HashMap<Label,HField>();
    final TreeBuilder tb;
    TreePostPass(final Frame f, final long FLAG_VALUE, HField HFflagvalue,
		 final MethodGenerator gen, final Set<HField> transFields) {
	final Label Lflagvalue = f.getRuntime().getNameMap().label(HFflagvalue);
	final NameMap nm = f.getRuntime().getNameMap();
	// map generated methods to labels.
	final Map<Label,HMethod> label2method = new HashMap<Label,HMethod>();
	for (HMethod hm : gen.generatedMethodSet) {
	    label2method.put(nm.label(hm), hm);
	}
	// map referenced fields to labels.
	for (HField hf : transFields) {
	    label2field.put(nm.label(hf, "obj"), hf);
	}
	// cache tree builder
	tb = f.getRuntime().getTreeBuilder();

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
		String baseName = gen.baseName(label2method.get(name.label));
		return methodTransformer.containsKey(baseName);
	    }
	    public Stm apply(TreeFactory tf, Stm s, DerivationGenerator dg) {
		CALL call = (CALL) s;
		NAME name = (NAME) call.getFunc();
		HMethod hm = label2method.get(name.label);
		String baseName = gen.baseName(hm);
		Transformer t = methodTransformer.get(baseName);
		String methodName = "EXACT_"+baseName;
		HClass suf = t.functionSuffix(hm, call.getArgs());
		if (suf!=null)
		    methodName += "_" + 
			titlecase(suf.isPrimitive()? suf.getName() : "Object");
		Label Lmethod = new Label(nm.c_function_name(methodName));

		return new NATIVECALL(tf, s, call.getRetval(),
				      new NAME(tf, s, Lmethod),
				      t.transformArgs(tf, s, dg, hm,
						      call.getArgs()));
	    }
        });
    }
    private final String titlecase(String s) {
	if (s.length()==0) return s;
	return s.substring(0,1).toUpperCase()+s.substring(1);
    }

    abstract class Transformer {
	abstract HClass functionSuffix(HMethod methodVersion,
				       ExpList javaArgs);
	abstract ExpList transformArgs(TreeFactory tf, HCodeElement source,
				       DerivationGenerator dg,
				       HMethod hm, ExpList javaArgs);
	// helper functions.
	Exp extract(ExpList args, int index) {
	    while (index!=0) {
		args = args.tail;
		index--;
	    }
	    return args.head;
	}
	ExpList subst(ExpList args, Exp e, int index) {
	    if (index==0) return new ExpList(e, args.tail);
	    else return new ExpList(args.head, subst(args.tail, e, index-1));
	}
	HField findField(Exp e) {
	    NAME name = (NAME) e;
	    return label2field.get(name.label);
	}
    }
    final Transformer nullTransformer = new Transformer() {
	    HClass functionSuffix(HMethod hm, ExpList javaArgs) {
		return null;
	    }
	    ExpList transformArgs(TreeFactory tf, HCodeElement source,
				  DerivationGenerator dg,
				  HMethod hm, ExpList javaArgs) {
		return javaArgs;
	    }
	};
    class FieldTransformer extends Transformer {
	    HClass functionSuffix(HMethod hm, ExpList javaArgs) {
		return findField(extract(javaArgs, 1)).getType();
	    }
	    ExpList transformArgs(TreeFactory tf, HCodeElement source,
				  DerivationGenerator dg,
				  HMethod hm, ExpList javaArgs) {
		// args are <obj, field, ...>
		// we need to calculate offset from fields
		HField objF = findField(extract(javaArgs, 1));
		Exp offsetE= tb.fieldOffset(tf, source, dg, objF).unEx(tf);
		return subst(javaArgs, offsetE, 1);
	    }
    }
    final Transformer fieldTransformer = new FieldTransformer();

    class ArrayTransformer extends Transformer {
	    HClass functionSuffix(HMethod hm, ExpList javaArgs) {
		return hm.getParameterTypes()[0].getComponentType();
	    }
	    ExpList transformArgs(TreeFactory tf, HCodeElement source,
				  DerivationGenerator dg,
				  HMethod hm, ExpList javaArgs) {
		HClass objType = hm.getParameterTypes()[0];
		// args are <obj, index, ...>
		// we need to calculate offset from index.
		Exp indexE = extract(javaArgs, 1);
		Exp offsetE = tb.arrayOffset
		    (tf, source, dg, objType, new Translation.Ex(indexE))
		    .unEx(tf);
		return subst(javaArgs, offsetE, 1);
	    }
    }
    final Transformer arrayTransformer = new ArrayTransformer();

    final Map<String,Transformer> methodTransformer =
	new HashMap<String,Transformer>();
    {
	// VALUETYPE TA(EXACT_readT)(struct oobj *obj, int offset,
	//			     struct vinfo *version,
	//                           struct commitrec *cr);
	methodTransformer.put("readT", new FieldTransformer() {
		HClass functionSuffix(HMethod hm, ExpList javaArgs) {
		    return hm.getReturnType();
		}
	    });
	methodTransformer.put("readT_Array", new ArrayTransformer() {
		HClass functionSuffix(HMethod hm, ExpList javaArgs) {
		    return hm.getReturnType();
		}
	    });
	// VALUETYPE TA(EXACT_readNT)(struct oobj *obj, int offset);
	methodTransformer.put("readNT", fieldTransformer);
	methodTransformer.put("readNT_Array", arrayTransformer);
	// void TA(EXACT_writeT)(struct oobj *obj, int offset,
	//		         VALUETYPE value, struct vinfo *version);
	methodTransformer.put("writeT", new FieldTransformer() {
		HClass functionSuffix(HMethod hm, ExpList javaArgs) {
		    return hm.getParameterTypes()[2];
		}
	    });
	methodTransformer.put("writeT_Array", new ArrayTransformer() {
		HClass functionSuffix(HMethod hm, ExpList javaArgs) {
		    return hm.getParameterTypes()[2];
		}
	    });
	// void TA(EXACT_writeNT)(struct oobj *obj, int offset,
	//			  VALUETYPE value)
	methodTransformer.put("writeNT", new FieldTransformer() {
		HClass functionSuffix(HMethod hm, ExpList javaArgs) {
		    return hm.getParameterTypes()[2];
		}
	    });
	methodTransformer.put("writeNT_Array", new ArrayTransformer() {
		HClass functionSuffix(HMethod hm, ExpList javaArgs) {
		    return hm.getParameterTypes()[2];
		}
	    });
	// struct vinfo *EXACT_ensureReader(struct oobj *obj,
	//                                  struct commitrec *cr);
	// struct vinfo *EXACT_ensureWriter(struct oobj *obj,
	//				    struct commitrec *cr);
	methodTransformer.put("ensureReader", nullTransformer);
	methodTransformer.put("ensureWriter", nullTransformer);
	// void TA(EXACT_checkReadField)(struct oobj *obj, int offset);
	methodTransformer.put("checkReadField", fieldTransformer);
	methodTransformer.put("checkReadField_Array", arrayTransformer);
	// void TA(EXACT_checkWriteField)(struct oobj *obj, int offset);
	methodTransformer.put("checkWriteField", fieldTransformer);
	methodTransformer.put("checkWriteField_Array", arrayTransformer);

	// methods related to hardware transaction mechanism.
	methodTransformer.put("XACTION_BEGIN", nullTransformer);
	methodTransformer.put("XACTION_END", nullTransformer);
	// void TA(EXACT_traceRead)(struct oobj *obj, int offset, int istran);
	// void TA(EXACT_traceWrite)(struct oobj *obj, int offset, int istran);
	methodTransformer.put("traceRead", fieldTransformer);
	methodTransformer.put("traceRead_Array", arrayTransformer);
	methodTransformer.put("traceWrite", fieldTransformer);
	methodTransformer.put("traceWrite_Array", arrayTransformer);

	// methods related to counters
	
	methodTransformer.put("objectSize", nullTransformer);
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
