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
 * @version $Id: TreeBuilder.java,v 1.6 2003-07-15 03:33:52 cananian Exp $
 */
public class TreeBuilder extends harpoon.Backend.Runtime1.TreeBuilder {
    protected TreeBuilder(harpoon.Backend.Runtime1.Runtime runtime,
			  Linker linker,
		AllocationStrategy as, boolean pointersAreLong,
		int pointerAlignment) {
	super(runtime, linker, as, pointersAreLong, pointerAlignment);
    }

    public Exp fetchHash(TreeFactory tf, HCodeElement source,
			 Exp object) {
	return new MEM(tf, source, Type.INT,
		       new BINOP(tf, source, Type.POINTER, Bop.ADD,
				 object,
				 new CONST(tf, source, OBJ_HASH_OFF)));
    }

    // allocate 'length' bytes plus object header; fill in object header.
    // shift return pointer appropriately for an object reference.
    public Exp objAlloc(TreeFactory tf, HCodeElement source,
			DerivationGenerator dg,
			AllocationProperties ap,
			HClass objectType, Exp length) {
	Exp old=super.objAlloc(tf,source,dg,ap,objectType,length);
	Temp Tobj = new Temp(tf.tempFactory(), "BRIANTEMP");
	// If the noSync flag is on, then the second bit of the hashcode will
	// be set, allowing locks on this object to fall through.
	if (ap.noSync()) {
	    return new ESEQ(tf,source, 
		 new SEQ(tf,source,
		   new MOVE(tf,source,
		     DECLARE(dg,objectType,Tobj,
		     new TEMP(tf,source,Type.POINTER,Tobj)), old),
		   new MOVE(tf,source,
		     fetchHash(tf, source, 
			       DECLARE(dg,objectType,Tobj,
			       new TEMP(tf,source,Type.POINTER,Tobj))),
		   new BINOP(tf, source, Type.INT, Bop.ADD,
		     fetchHash(tf, source, 
			       DECLARE(dg,objectType,Tobj,
			       new TEMP(tf,source,Type.POINTER,Tobj))),
		   new CONST(tf, source, 2)))),
		 DECLARE(dg,objectType, Tobj,
		 new TEMP(tf,source,Type.POINTER,Tobj)));
	}
	else return old;
    }
    // XXX in single-threaded mode, this can be a NOP.
    public Translation.Exp monitorEnter(TreeFactory tf, HCodeElement source,
					DerivationGenerator dg,
					Translation.Exp objectref) {
	// call FNI_MonitorEnter()
	return new Translation.Nx(_call_FNI_Monitor(tf, source, dg, objectref,
						    true));
    }
    // XXX in single-threaded mode, this can be a NOP.
    public Translation.Exp monitorExit(TreeFactory tf, HCodeElement source,
				       DerivationGenerator dg,
				       Translation.Exp objectref) {
	// call FNI_MonitorExit()
	return new Translation.Nx(_call_FNI_Monitor(tf, source, dg, objectref,
						    false));
    }
    protected Stm _call_FNI_Monitor(TreeFactory tf, HCodeElement source,
				  DerivationGenerator dg,
				  Translation.Exp objectref,
				  boolean isEnter/*else exit*/) {
	// if we're using the explicit DynamicSyncRemoval pass, we've already
	// done this transformation (in quad form) and don't need to repeat
	// it here.
	if (Boolean.getBoolean("harpoon.runtime2.skip_monitor_test"))
	    return super._call_FNI_Monitor(tf, source, dg, objectref, isEnter);
	// otherwise...

	HClass HCobj = linker.forName("java.lang.Object");
	Temp object = new Temp(tf.tempFactory(), "BRIANSOBJECT");

	MOVE move = new MOVE
	    (tf, source,
	     DECLARE(dg, HCobj, object,
		     new TEMP(tf, source, Type.POINTER, object)),
	     objectref.unEx(tf));

	Stm old = super._call_FNI_Monitor
	    (tf, source, dg,
	     new Translation.Ex
	     (DECLARE(dg, HCobj, object,
		      new TEMP(tf, source, Type.POINTER, object))),
	     isEnter);

	Label DLabel=new Label();
	Label SLabel=new Label();
	Exp lockvalue=fetchHash
	    (tf, source, 
	     DECLARE(dg, HCobj, object,
		     new TEMP(tf,source,Type.POINTER,object)));
	Exp testcond=new BINOP(tf, source, Type.INT, Bop.AND, lockvalue,
		       new CONST(tf, source, 2));
	Stm dotest=new CJUMP(tf,source,testcond,SLabel,DLabel);
	
	Stm monitor=new SEQ(tf,source,new LABEL(tf,source,DLabel,false),old);
	Stm labels=new SEQ(tf,source,monitor,new LABEL(tf, source, SLabel,false));
	
	Stm testit=new SEQ(tf,source,dotest,labels);

	Stm myresult=new SEQ(tf,source,move,testit);

	return myresult;
    }
}
