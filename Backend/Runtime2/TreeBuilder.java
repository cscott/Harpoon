// TreeBuilder.java, created Sat Sep 25 07:23:21 1999 by cananian
// Copyright (C) 1999 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Backend.Runtime2;

import harpoon.Analysis.ClassHierarchy;
import harpoon.Analysis.Maps.AllocationInformation.AllocationProperties;
import harpoon.Analysis.Maps.Derivation;
import harpoon.Backend.Maps.ClassDepthMap;
import harpoon.Backend.Maps.FieldMap;
import harpoon.Backend.Maps.MethodMap;
import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HCodeElement;
import harpoon.ClassFile.HField;
import harpoon.ClassFile.HMethod;
import harpoon.ClassFile.Linker;
import harpoon.IR.Tree.TreeFactory;
import harpoon.IR.Tree.Stm;
import harpoon.IR.Tree.Exp;
import harpoon.IR.Tree.ExpList;
import harpoon.IR.Tree.Bop;
import harpoon.IR.Tree.Uop;
import harpoon.IR.Tree.Translation;
import harpoon.IR.Tree.Type;
import harpoon.IR.Tree.BINOP;
import harpoon.IR.Tree.CALL;
import harpoon.IR.Tree.CJUMP;
import harpoon.IR.Tree.CONST;
import harpoon.IR.Tree.DATUM;
import harpoon.IR.Tree.DerivationGenerator;
import harpoon.IR.Tree.ESEQ;
import harpoon.IR.Tree.EXPR;
import harpoon.IR.Tree.INVOCATION;
import harpoon.IR.Tree.JUMP;
import harpoon.IR.Tree.LABEL;
import harpoon.IR.Tree.MEM;
import harpoon.IR.Tree.METHOD;
import harpoon.IR.Tree.MOVE;
import harpoon.IR.Tree.NAME;
import harpoon.IR.Tree.NATIVECALL;
import harpoon.IR.Tree.OPER;
import harpoon.IR.Tree.RETURN;
import harpoon.IR.Tree.SEGMENT;
import harpoon.IR.Tree.SEQ;
import harpoon.IR.Tree.TEMP;
import harpoon.IR.Tree.THROW;
import harpoon.IR.Tree.UNOP;
import harpoon.Temp.Label;
import harpoon.Temp.Temp;
import harpoon.Util.HClassUtil;
import harpoon.Util.Util;

import harpoon.Backend.Runtime1.AllocationStrategy;


import java.util.HashSet;
import java.util.List;
import java.util.Set;
/**
 * <code>Runtime2.TreeBuilder</code> is an implementation of
 * <code>Generic.Runtime.TreeBuilder</code> which creates
 * accessor expressions for the <code>Runtime1</code> runtime.
 * <p>Pretty straightforward.  No weird hacks.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: TreeBuilder.java,v 1.1.2.1 2000-11-10 21:49:48 bdemsky Exp $
 */
public class TreeBuilder extends harpoon.Backend.Runtime1.TreeBuilder {
    TreeBuilder(Runtime runtime, Linker linker, ClassHierarchy ch,
		AllocationStrategy as, boolean pointersAreLong) {
	super(runtime,linker,ch,as,pointersAreLong);
    }
    // allocate 'length' bytes plus object header; fill in object header.
    // shift return pointer appropriately for an object reference.
    public Exp objAlloc(TreeFactory tf, HCodeElement source,
			DerivationGenerator dg,
			AllocationProperties ap,
			HClass objectType, Exp length) {
	Exp old=super.objAlloc(tf,source,dg,ap,objectType,length);
	Temp Tobj = new Temp(tf.tempFactory(), "BRIANTEMP");
	if (ap.noSync())
	    return new ESEQ(tf,source, 
		 new SEQ(tf,source,
		   new MOVE(tf,source,
		     DECLARE(dg,objectType,Tobj,
		     new TEMP(tf,source,Type.POINTER,Tobj)), old),
		   new MOVE(tf,source,
		     new MEM(tf,source,Type.POINTER,
		       new BINOP(tf, source,Type.POINTER,Bop.ADD,
		         DECLARE(dg,objectType,Tobj,
			 new TEMP(tf,source,Type.POINTER,Tobj)),
		           new CONST(tf, source, OBJ_HASH_OFF))),
		   new BINOP(tf, source, Type.POINTER, Bop.ADD,
		     new MEM(tf,source,Type.POINTER,
	               new BINOP(tf, source,Type.POINTER,Bop.ADD,
			 DECLARE(dg,objectType,Tobj,
	                 new TEMP(tf,source,Type.POINTER,Tobj)),
                         new CONST(tf, source, OBJ_HASH_OFF))),
		   new CONST(tf, source, 2)))),
		 DECLARE(dg,objectType, Tobj,
		 new TEMP(tf,source,Type.POINTER,Tobj)));
	else return old;
    }
}
