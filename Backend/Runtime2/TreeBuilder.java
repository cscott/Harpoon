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
 * @version $Id: TreeBuilder.java,v 1.1.2.3 2001-05-15 16:07:07 wbeebee Exp $
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
	// If the noSync flag is on, then the second bit of the hashcode will
	// be set, allowing locks on this object to fall through.
	if (false||ap.noSync())
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
    private Stm _call_FNI_Monitor(TreeFactory tf, HCodeElement source,
				  DerivationGenerator dg,
				  Translation.Exp objectref,
				  boolean isEnter/*else exit*/) {
	// keep this synchronized with StubCode.java
	// and Runtime/include/jni-private.h

	// first get JNIEnv *
	Temp envT = new Temp(tf.tempFactory(), "env");
	Stm result0 = new NATIVECALL
	    (tf, source, (TEMP)
	     DECLARE(dg, HClass.Void/* JNIEnv * */, envT,
	     new TEMP(tf, source, Type.POINTER, envT)) /*retval*/,
	     DECLARE(dg, HClass.Void/* c function ptr */,
	     new NAME(tf, source, new Label
		      (runtime.nameMap.c_function_name("FNI_GetJNIEnv")))),
	     null/* no args*/);
	Temp object= new Temp(tf.tempFactory(), "BRIANSOBJECT");
	// wrap objectref.
	Temp objT = new Temp(tf.tempFactory(), "obj");
	result0 = new SEQ
	    (tf, source, result0,
	     new NATIVECALL
	     (tf, source, (TEMP)
	      DECLARE(dg, HClass.Void/* jobject */, objT,
	      new TEMP(tf, source, Type.POINTER, objT)) /*retval*/,
	      DECLARE(dg, HClass.Void/* c function ptr */,
	      new NAME(tf, source, new Label
		       (runtime.nameMap.c_function_name("FNI_NewLocalRef")))),
	      new ExpList
	      (DECLARE(dg, HClass.Void/* JNIEnv * */, envT,
	       new TEMP(tf, source, Type.POINTER, envT)),
	       new ExpList
	       (DECLARE(dg, HClass.Void, object,
	        new TEMP(tf,source,Type.POINTER,object)), null))));

	// call FNI_MonitorEnter or FNI_MonitorExit
	// proto is 'jint FNI_Monitor<foo>(JNIEnv *env, jobject obj);
	// i'm going to be anal and make a temp for the return value,
	// because some architectures might conceivably do weird things if
	// i just pretend the function is void.  but we don't need the retval.
	Temp disT = new Temp(tf.tempFactory(), "discard");
	Stm result1 = new NATIVECALL
	    (tf, source,
	     new TEMP(tf, source, Type.INT, disT) /*retval*/,
	     DECLARE(dg, HClass.Void/* c function ptr */,
	     new NAME(tf, source, new Label
		      (runtime.nameMap.c_function_name
		       (isEnter?"FNI_MonitorEnter":"FNI_MonitorExit")))),
	     new ExpList
	     (DECLARE(dg, HClass.Void/* JNIEnv * */, envT,
	      new TEMP(tf, source, Type.POINTER, envT)),
	      new ExpList
	      (DECLARE(dg, HClass.Void/* jobject */, objT,
	       new TEMP(tf, source, Type.POINTER, objT)),
	       null)));

	// okay, now free the localref and we're set.
	result1 = new SEQ
	    (tf, source, result1,
	     new NATIVECALL
	     (tf, source, null/*void retval*/,
	      DECLARE(dg, HClass.Void/* c function ptr */,
	      new NAME(tf, source, new Label(runtime.nameMap.c_function_name
					     ("FNI_DeleteLocalRefsUpTo")))),
	      new ExpList
	      (DECLARE(dg, HClass.Void/* JNIEnv * */, envT,
	       new TEMP(tf, source, Type.POINTER, envT)),
	       new ExpList
	       (DECLARE(dg, HClass.Void/* jobject */, objT,
		new TEMP(tf, source, Type.POINTER, objT)),
		null))));
	Stm old=new SEQ(tf,source,result0,result1);

	Label DLabel=new Label();
	Label SLabel=new Label();
	Exp lockvalue=new MEM(tf,source,Type.INT,
		       new BINOP(tf, source,Type.POINTER,Bop.ADD,
                         (DECLARE(dg, HClass.Void, object,
			 new TEMP(tf,source,Type.POINTER,object))),
		           new CONST(tf, source, OBJ_HASH_OFF)));
	Exp testcond=new BINOP(tf, source, Type.INT, Bop.AND, lockvalue,
		       new CONST(tf, source, 2));
	Stm dotest=new CJUMP(tf,source,testcond,SLabel,DLabel);
	
	Stm monitor=new SEQ(tf,source,new LABEL(tf,source,DLabel,false),old);
	Stm labels=new SEQ(tf,source,monitor,new LABEL(tf, source, SLabel,false));
	
	Stm testit=new SEQ(tf,source,dotest,labels);

	Stm myresult=new SEQ(tf,source,
                        new MOVE(tf,source,
                         (DECLARE(dg, HClass.Void, object,
			  new TEMP(tf,source,Type.POINTER,object))),
                          objectref.unEx(tf)),
			testit);

	return myresult;
    }
}
