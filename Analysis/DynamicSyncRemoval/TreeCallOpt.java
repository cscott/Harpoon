// TreeCallOpt.java, created Thu Jan 11 16:04:23 2001 by cananian
// Copyright (C) 2000 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.DynamicSyncRemoval;

import harpoon.Backend.Generic.Frame;
import harpoon.Backend.Maps.NameMap;
import harpoon.ClassFile.Linker;
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
import harpoon.IR.Tree.MEM;
import harpoon.IR.Tree.MOVE;
import harpoon.IR.Tree.NAME;
import harpoon.IR.Tree.NATIVECALL;
import harpoon.IR.Tree.Stm;
import harpoon.IR.Tree.TreeFactory;
import harpoon.IR.Tree.Type;
import harpoon.Temp.Label;
import harpoon.Util.Util;

import java.util.ArrayList;
import java.util.List;
/**
 * <code>TreeCallOpt</code> performs some low-level transformations to
 * tree form to make the calls inserted by <code>SyncRemover</code>
 * more efficient.
 * <p>
 * This optimization may not be safe if you are using precise garbage
 * collection.
 *
 * @author   C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: TreeCallOpt.java,v 1.4 2003-07-10 03:54:59 cananian Exp $
 */
class TreeCallOpt extends harpoon.Analysis.Tree.Simplification {
    private final List<Rule> RULES = new ArrayList<Rule>(); 
    TreeCallOpt(final Frame f) {
	Linker l = f.getLinker();
	final NameMap nm = f.getRuntime().getNameMap();

	HMethod checkMethod =
	    l.forName("harpoon.Runtime.DynamicSyncImpl")
	    .getMethod("isNoSync",new HClass[]{l.forName("java.lang.Object")});
	final Label LisSync = nm.label(checkMethod);
	final Label Lnative_isSync = new Label
	    (nm.c_function_name("DYNSYNC_isNoSync"));

	// add all rules to rule set

	// turn calls to certain native methods into direct NATIVECALLs
	// CALL(retval,retex,NAME(LisSync),ExpList(arg,null),handler,false) ->
	//    NATIVECALL(retval,NAME(Lnative_isSync),ExpList(arg,null))
	// or
	//    MOVE(retval,BINOP(AND,MEM(BINOP(ADD,arg,CONST(OBJ_HASH_OFF))),2))
	RULES.add(new Rule("isSyncRewrite2") {
	    public boolean match(Stm s) {
		if (!contains(_KIND(s), _CALL)) return false;
		CALL call = (CALL) s;
		if (!contains(_KIND(call.getFunc()), _NAME)) return false;
		NAME name = (NAME) call.getFunc();
		if (!name.label.equals(LisSync)) return false;
		if (call.getArgs()==null ||
		    call.getArgs().tail!=null) return false;
		// yup, this is a match!
		return true;
	    }
	    public Stm apply(TreeFactory tf, Stm s, DerivationGenerator dg) {
		CALL call = (CALL) s;
		if (f.getRuntime().getTreeBuilder() instanceof
		    harpoon.Backend.Runtime2.TreeBuilder) {
		    Exp hash =
			((harpoon.Backend.Runtime2.TreeBuilder)
			 f.getRuntime().getTreeBuilder())
			.fetchHash(tf, s, call.getArgs().head);
		    return new MOVE
			(tf, s, call.getRetval(),
			 new BINOP
			 (tf, s, Type.INT, Bop.AND,
			  hash,
			  new CONST(tf, s, 2)));
		} else {
		    return new NATIVECALL(tf, s, call.getRetval(),
					  new NAME(tf, s, Lnative_isSync),
					  call.getArgs());
		}
	    }
        });
    }
    /** Code factory for applying the post pass to the given tree
     *  form.  Clones the tree before processing it in-place. */
    public HCodeFactory codeFactory(final HCodeFactory parent) {
	return codeFactory(parent, RULES);
    }
}
