// TreeBuilder.java, created Sat Sep 25 07:23:21 1999 by cananian
// Copyright (C) 1999 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Backend.Runtime1;

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

import java.util.HashSet;
import java.util.List;
import java.util.Set;
/**
 * <code>Runtime1.TreeBuilder</code> is an implementation of
 * <code>Generic.Runtime.TreeBuilder</code> which creates
 * accessor expressions for the <code>Runtime1</code> runtime.
 * <p>Pretty straightforward.  No weird hacks.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: TreeBuilder.java,v 1.1.2.37 2001-06-12 21:50:44 cananian Exp $
 */
public class TreeBuilder extends harpoon.Backend.Generic.Runtime.TreeBuilder {
    // allocation strategy to use.
    final AllocationStrategy as;

    // integer constant sizes:
    protected final int WORD_SIZE;
    protected final int LONG_WORD_SIZE;
    protected final int POINTER_SIZE;
    protected final int OBJECT_HEADER_SIZE;
    // integer constant offsets:
    // layout of oobj
    protected final int OBJ_CLAZ_OFF;
    protected final int OBJ_HASH_OFF;
    protected final int OBJ_ALENGTH_OFF;
    protected final int OBJ_AZERO_OFF;
    protected final int OBJ_FZERO_OFF;
    // layout of claz
    protected final int CLAZ_INTERFACES_OFF;
    protected final int CLAZ_CLAZINFO;
    protected final int CLAZ_COMPONENT_OFF;
    protected final int CLAZ_INTERFZ_OFF;
    protected final int CLAZ_SIZE_OFF;
    protected final int CLAZ_GCENTRY_OFF;
    protected final int CLAZ_DEPTH_OFF;
    protected final int CLAZ_DISPLAY_OFF;
    protected final int CLAZ_METHODS_OFF;
    
    // helper maps.
    protected final Runtime runtime;
    protected final Linker linker;
    protected final ClassDepthMap cdm;
    protected final MethodMap imm;
    protected final MethodMap cmm;
    protected final FieldMap  cfm;

    // set of string references made
    final Set stringSet = new HashSet();

    protected TreeBuilder(Runtime runtime, Linker linker, ClassHierarchy ch,
			  AllocationStrategy as, boolean pointersAreLong) {
	this.runtime = runtime;
	this.linker = linker;
	this.as  = as;
	this.cdm = new harpoon.Backend.Maps.DefaultClassDepthMap(ch);
	this.imm = new harpoon.Backend.Analysis.InterfaceMethodMap(ch);
	this.cmm = new harpoon.Backend.Analysis.ClassMethodMap();
	this.cfm = new harpoon.Backend.Analysis.ClassFieldMap() {
	    public int fieldSize(HField hf) {
		HClass type = hf.getType();
		return (!type.isPrimitive()) ? POINTER_SIZE :
		    (type==HClass.Double||type==HClass.Long) ? LONG_WORD_SIZE :
		    (type==HClass.Int||type==HClass.Float) ? WORD_SIZE :
		    (type==HClass.Short||type==HClass.Char) ? 2 : 1;
	    }
	    public int fieldAlignment(HField hf) {
		// every field is aligned to its size
		return fieldSize(hf);
	    }
	};
	// ----------    INITIALIZE SIZES AND OFFSETS    -----------
	WORD_SIZE = 4; // at least 32 bits.
	LONG_WORD_SIZE = 8; // at least 64 bits.
	POINTER_SIZE = pointersAreLong ? LONG_WORD_SIZE : WORD_SIZE;

	OBJECT_HEADER_SIZE = 2 * POINTER_SIZE;
	// layout of oobj
	// (note that the hashcode is actually pointer size, because it is
	//  used to point to an inflated_oobj structure after inflation)
	OBJ_CLAZ_OFF    = 0 * POINTER_SIZE;
	OBJ_HASH_OFF    = OBJ_CLAZ_OFF + 1 * POINTER_SIZE;
	OBJ_FZERO_OFF   = OBJ_HASH_OFF + 1 * POINTER_SIZE;
	OBJ_ALENGTH_OFF = OBJ_FZERO_OFF +
	    // add (non-header) size of java.lang.Object, since arrays
	    // inherit from it (allows us to add fields to Object)
	    objectSize(linker.forName("java.lang.Object"));
	OBJ_AZERO_OFF   = OBJ_ALENGTH_OFF + 1 * WORD_SIZE;
	// layout of claz
	CLAZ_INTERFACES_OFF = -1 * POINTER_SIZE;
	CLAZ_CLAZINFO    = 0 * POINTER_SIZE;
	CLAZ_COMPONENT_OFF=1 * POINTER_SIZE;
	CLAZ_INTERFZ_OFF = 2 * POINTER_SIZE;
	CLAZ_SIZE_OFF	 = 3 * POINTER_SIZE;
	CLAZ_GCENTRY_OFF = 3 * POINTER_SIZE + 1 * WORD_SIZE;
	CLAZ_DEPTH_OFF   = 4 * POINTER_SIZE + 1 * WORD_SIZE;
	CLAZ_DISPLAY_OFF = 4 * POINTER_SIZE + 2 * WORD_SIZE;
	CLAZ_METHODS_OFF = CLAZ_DISPLAY_OFF + (1+cdm.maxDepth())*POINTER_SIZE;
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

    // use the field offset map to get the object size (not including header)
    public int objectSize(HClass hc) {
	List l = cfm.fieldList(hc);
	if (l.size()==0) return 0;
	HField lastfield = (HField) l.get(l.size()-1);
	return cfm.fieldOffset(lastfield) + cfm.fieldSize(lastfield);
    }

    // allocate 'length' bytes plus object header; fill in object header.
    // shift return pointer appropriately for an object reference.
    public Exp objAlloc(TreeFactory tf, HCodeElement source,
			DerivationGenerator dg,
			AllocationProperties ap,
			HClass objectType, Exp length) {
	Temp Tobj = new Temp(tf.tempFactory(), "rt");
	return new ESEQ
	    (tf, source,
	     new SEQ
	     (tf, source,
	      new MOVE // allocate memory; put pointer in Tobj.
	      (tf, source,
	       DECLARE(dg, objectType/*not an obj yet*/, Tobj,
	       new TEMP(tf, source, Type.POINTER, Tobj)),
	       as.memAlloc
	       (tf, source, dg, ap,
		new BINOP
		(tf, source, Type.INT, Bop.ADD,
		 length,
		 new CONST(tf, source, OBJECT_HEADER_SIZE)))),
	      new SEQ
	      (tf, source,
	       new MOVE // assign the new object a hashcode.
	       (tf, source,
		DECLARE(dg, HClass.Void/*hashcode, not an object*/,
		new MEM
		(tf, source, Type.POINTER, /* hashcode is pointer size */
		 new BINOP
		 (tf, source, Type.POINTER, Bop.ADD,
		  DECLARE(dg, objectType/*not an obj yet*/, Tobj,
		  new TEMP(tf, source, Type.POINTER, Tobj)),
		  new CONST(tf, source, OBJ_HASH_OFF)))),
		new BINOP // set the low bit to indicate an uninflated object.
		(tf, source, Type.POINTER, Bop.ADD,
		 DECLARE(dg, objectType/*not an obj yet*/, Tobj,
		 new TEMP(tf, source, Type.POINTER, Tobj)),
		 new CONST(tf, source, 1))),
	       new MOVE // assign the new object a class pointer.
	       (tf, source,
		DECLARE
		(dg, HClass.Void/*claz pointer*/,
		 new MEM
		 (tf, source, Type.POINTER,
		  new BINOP
		  (tf, source, Type.POINTER, Bop.ADD,
		   DECLARE(dg, objectType/*still not an obj*/, Tobj,
			   new TEMP(tf, source, Type.POINTER, Tobj)),
		   new CONST(tf, source, OBJ_CLAZ_OFF)))),
		new NAME(tf, source, runtime.nameMap.label(objectType))))),
	     // result of ESEQ is new object pointer
	     DECLARE(dg, objectType/*finally an obj*/, Tobj,
	     new TEMP(tf, source, Type.POINTER, Tobj)));
    }

    public Translation.Exp arrayLength(TreeFactory tf, HCodeElement source,
				       DerivationGenerator dg,
				       Translation.Exp arrayRef) {
	return new Translation.Ex
	   (new MEM  
	    (tf, source, Type.INT, // The "length" field is of type INT
	     new BINOP
	     (tf, source, Type.POINTER, Bop.ADD,
	      arrayRef.unEx(tf),
	      new CONST
	      (tf, source, OBJ_ALENGTH_OFF)))); // offset from array base ptr.
    }
    public Translation.Exp arrayNew(TreeFactory tf, HCodeElement source,
				    DerivationGenerator dg,
				    AllocationProperties ap,
				    HClass arrayType, Translation.Exp length) {
	Util.assert(arrayType.isArray());
	// temporary storage for created array.
	Temp Tarr = new Temp(tf.tempFactory(), "rt");
	// temporary storage for supplied length
	Temp Tlen = new Temp(tf.tempFactory(), "rt");
	// type of array components
	HClass comType = arrayType.getComponentType();
	// size of elements in array
	int elementSize = !comType.isPrimitive() ? POINTER_SIZE :
	    (comType==HClass.Double || comType==HClass.Long) ? (WORD_SIZE*2) :
	    (comType==HClass.Byte || comType==HClass.Boolean) ? 1 :
	    (comType==HClass.Char || comType==HClass.Short) ? 2 :
	    WORD_SIZE;
        Stm stm =
	     new SEQ
	     (tf, source,
	      new SEQ
	      (tf, source,
	       new MOVE // save 'length' in Tlen.
	       (tf, source,
		new TEMP(tf, source, Type.INT, Tlen),
		length.unEx(tf)),
	       new MOVE // save result in Tarr
	       (tf, source,
		DECLARE(dg, arrayType/* not an obj yet*/, Tarr,
			new TEMP(tf, source, Type.POINTER, Tarr)),
		objAlloc // allocate array data
		(tf, source, dg, ap, arrayType,
		 new BINOP // compute array data size:
		 (tf, source, Type.INT, Bop.ADD,
		  new BINOP // multiply...
		  (tf, source, Type.INT, Bop.MUL,
		   // ...array length by ...
		   new TEMP(tf, source, Type.INT, Tlen),
		   // ...element size...
		   new CONST(tf, source, elementSize)),
		  // and add WORD_SIZE (and more) for length field (and others)
		  new CONST(tf, source, OBJ_AZERO_OFF - OBJ_FZERO_OFF))))),
	      new MOVE // now set length field of newly-created array.
	      (tf, source,
	       new MEM
	       (tf, source, Type.INT, // length field is of type INT.
		new BINOP // offset array base to get location of length field
		(tf, source, Type.POINTER, Bop.ADD,
		 DECLARE(dg, arrayType/*not an obj yet*/, Tarr,
			 new TEMP(tf, source, Type.POINTER, Tarr)),
		 new CONST(tf, source, OBJ_ALENGTH_OFF))),
	       new TEMP(tf, source, Type.INT, Tlen))); // length from Tlen
        /* now, if there are fields of java.lang.Object that need to be
         * initialized, zero fill them. */
        if (OBJ_ALENGTH_OFF != OBJ_FZERO_OFF) {
            stm = new SEQ
                (tf, source, stm,
                 new NATIVECALL
                 (tf, source, null,
                  DECLARE(dg, HClass.Void/*c library function*/,
                  new NAME(tf, source, new Label
                           (runtime.nameMap.c_function_name("memset")))),
                  new ExpList
                  (new BINOP
                   (tf, source, Type.POINTER, Bop.ADD,
                    DECLARE(dg, arrayType/*not an obj yet*/, Tarr,
                    new TEMP(tf, source, Type.POINTER, Tarr)),
                    new CONST(tf, source, OBJ_FZERO_OFF)),
                   new ExpList
                   (new CONST(tf, source, 0),
                    new ExpList
                    (new CONST(tf, source, OBJ_ALENGTH_OFF-OBJ_FZERO_OFF),
                     null)))));
        }
        return new Translation.Ex
            (new ESEQ
             (tf, source, stm,
	     // result of whole expression is the array pointer, in Tarr
	     DECLARE(dg, arrayType/*finally an obj*/, Tarr,
		     new TEMP(tf, source, Type.POINTER, Tarr))));
    }

    public Translation.Exp componentOf(TreeFactory tf, HCodeElement source,
				       DerivationGenerator dg,
				       Translation.Exp arrayref,
				       Translation.Exp componentref) {
	// component clazz pointer of arrayref
	Exp e0 = DECLARE(dg, HClass.Void/*component claz ptr*/,
                 new MEM(tf, source, Type.POINTER,
			 new BINOP // offset to get component type pointer
			 (tf, source, Type.POINTER, Bop.ADD,
			  new CONST(tf, source, CLAZ_COMPONENT_OFF),
			  // dereference object to claz structure.
			  DECLARE(dg, HClass.Void/*claz ptr*/,
			  new MEM(tf, source, Type.POINTER,
				  new BINOP // offset to get claz pointer
				  (tf, source, Type.POINTER, Bop.ADD,
				   arrayref.unEx(tf),
				   new CONST(tf, source, OBJ_CLAZ_OFF)))))));
	// class pointer of componentref
	Exp e1 = DECLARE(dg, HClass.Void/*claz ptr*/,
                 new MEM(tf, source, Type.POINTER,
			 new BINOP // offset to get claz pointer
			 (tf, source, Type.POINTER, Bop.ADD,
			  componentref.unEx(tf),
			  new CONST(tf, source, OBJ_CLAZ_OFF))));
	// move claz pointer of arrayref component to a temporary variable.
	Temp Tac = new Temp(tf.tempFactory(), "rt");
	Stm s0 = new MOVE(tf, source,
			  DECLARE(dg, HClass.Void/*component claz ptr*/, Tac,
				  new TEMP(tf, source, Type.POINTER, Tac)),
			  e0); // now use Tac instead of e0.
	// class depth of arrayref component type.
	Exp e2 = new MEM(tf, source, Type.INT,
			 new BINOP // offset to class depth.
			 (tf, source, Type.POINTER, Bop.ADD,
			  new CONST(tf, source, CLAZ_DEPTH_OFF),
			  DECLARE(dg, HClass.Void/*component claz ptr*/, Tac,
				  new TEMP(tf, source, Type.POINTER, Tac))));
	// we assert that MEM(e0+e2)==e0 by definition
	// that is, element of class display at class_depth is the class itself
	// so, the component-of check is just whether MEM(e1+e2)==e0
	Exp e3 = new BINOP
	    (tf, source, Type.POINTER, Bop.CMPEQ,
	     DECLARE(dg, HClass.Void/*claz ptr in display*/,
	     new MEM
	     (tf, source, Type.POINTER,
	      new BINOP
	      (tf, source, Type.POINTER, Bop.ADD,
	       new BINOP
	       (tf, source, Type.POINTER, Bop.ADD,
		new CONST(tf, source, CLAZ_DISPLAY_OFF),
		e2),
	       e1))),
	     DECLARE(dg, HClass.Void/*component claz ptr*/, Tac,
		     new TEMP(tf, source, Type.POINTER, Tac)));

	return new Translation.Ex(new ESEQ(tf, source, s0, e3));
    }

    public Translation.Exp instanceOf(final TreeFactory tf,
				      final HCodeElement source,
				      final DerivationGenerator dg,
				      final Translation.Exp objref,
				      final HClass classType)
    {
	final Label Lclaz = runtime.nameMap.label(classType);
	// two cases: class or interface type.
	if (HClassUtil.baseClass(classType).isInterface()) {
	    // interface type: linear search through interface list.
	    // compile as:
	    //    for (il=obj->claz->interfz; *il!=null; il++)
	    //       if (*il==classTypeLabel) return true;
	    //    return false;

	    // make our iteration variable.
	    final Temp Til = new Temp(tf.tempFactory(), "rt"); // il
	    // three labels
	    final Label Ladv = new Label();
	    final Label Ltop = new Label();
	    final Label Ltst = new Label();

	    return new Translation.Cx() {
		public Stm unCxImpl(TreeFactory xxx, Label iftrue, Label iffalse) {
		    // initialize Til.
		    Stm s0 = new MOVE
			(tf, source,
			 DECLARE(dg, HClass.Void/*interface list ptr*/,
			 new TEMP(tf, source, Type.POINTER, Til)),
			 // dereference claz structure for interface list ptr
			 DECLARE(dg, HClass.Void/*interface list ptr*/,
			 new MEM(tf, source, Type.POINTER,
				 new BINOP // offset to get interface pointer
				 (tf, source, Type.POINTER, Bop.ADD,
				  // dereference object to claz structure.
				  DECLARE(dg, HClass.Void/*claz ptr*/,
				  new MEM(tf, source, Type.POINTER,
					  new BINOP // offset to get claz ptr
					  (tf, source, Type.POINTER, Bop.ADD,
					   objref.unEx(tf),
					   new CONST
					   (tf, source, OBJ_CLAZ_OFF)))),
				  new CONST(tf, source, CLAZ_INTERFZ_OFF)))));
		    // loop body: test *il against Lclaz.
		    Stm s1 = new CJUMP
			(tf, source,
			 new BINOP
			 (tf, source, Type.POINTER, Bop.CMPEQ,
			  DECLARE(dg, HClass.Void/*claz ptr for interface*/,
			  new MEM(tf, source, Type.POINTER,
				  DECLARE(dg, HClass.Void/*intrfce lst ptr*/,
				  new TEMP(tf, source, Type.POINTER, Til)))),
			  DECLARE(dg, HClass.Void/*hardwired claz ptr*/,
			  new NAME(tf, source, Lclaz))),
			 iftrue, Ladv);
		    // advance il
		    Stm s2 = new MOVE
			(tf, source,
			 DECLARE(dg, HClass.Void/*intrfce lst ptr*/, Til,
			 new TEMP(tf, source, Type.POINTER, Til)),
			 new BINOP
			 (tf, source, Type.POINTER, Bop.ADD,
			  DECLARE(dg, HClass.Void/*intrfce lst ptr*/, Til,
			  new TEMP(tf, source, Type.POINTER, Til)),
			  new CONST(tf, source, POINTER_SIZE)));
		    // loop guard: test *il against null.
		    Stm s3 = new CJUMP
			(tf, source,
			 new BINOP
			 (tf, source, Type.POINTER, Bop.CMPEQ,
			  DECLARE(dg, HClass.Void/*claz ptr, maybe null*/,
			  new MEM(tf, source, Type.POINTER,
				  DECLARE(dg, HClass.Void/*in lst ptr*/, Til,
				  new TEMP(tf, source, Type.POINTER, Til)))),
			  new CONST(tf, source) /*null constant*/),
			 iffalse, Ltop);
		    // string 'em all together to make result stm.
		    //   ( s0 -> jmp Ltst -> Ltop -> s1 -> Ladv -> 
		    //           s2 -> Ltst -> s3 )
		    return new SEQ
			(tf, source,
			 new SEQ
			 (tf, source,
			  new SEQ(tf, source,
				  s0,
				  new JUMP(tf, source, Ltst)),
			  new SEQ(tf, source,
				  new LABEL(tf, source, Ltop, false),
				  s1)),
			 new SEQ
			 (tf, source,
			  new SEQ(tf, source,
				  new LABEL(tf, source, Ladv, false),
				  s2),
			  new SEQ(tf, source,
				  new LABEL(tf, source, Ltst, false),
				  s3))
			 );
		}
	    };
	} else {
	    // class type: single lookup and comparison.
	    // compile as:
	    //    return obj->claz->display[CONST_OFF(classType)]==classType;
	    int class_offset = cdm.classDepth(classType) * POINTER_SIZE;

	    return new Translation.Ex
		(new BINOP
		 (tf, source, Type.POINTER, Bop.CMPEQ,
		  DECLARE(dg, HClass.Void/*hardwired claz ptr*/,
		  new NAME(tf, source, Lclaz)), // claz pointer
		  // dereference claz structure for class display ptr
		  DECLARE(dg, HClass.Void/*claz ptr from display*/,
		  new MEM(tf, source, Type.POINTER,
			  new BINOP // offset to get display pointer
			  (tf, source, Type.POINTER, Bop.ADD,
			   new CONST(tf, source,CLAZ_DISPLAY_OFF+class_offset),
			   // dereference object to claz structure.
			   DECLARE(dg, HClass.Void/*claz ptr*/,
			   new MEM(tf, source, Type.POINTER,
				   new BINOP // offset to get claz pointer
				   (tf, source, Type.POINTER, Bop.ADD,
				    objref.unEx(tf),
				    new CONST(tf, source, OBJ_CLAZ_OFF)))))))
		  ));
	}
    }

    // XXX in single-threaded mode, this can be a NOP.
    public Translation.Exp monitorEnter(TreeFactory tf, HCodeElement source,
					DerivationGenerator dg,
					Translation.Exp objectref) {
	if (Boolean.getBoolean("harpoon.runtime1.nosync"))
	    return new Translation.Ex(new CONST(tf, source, 0));
	// call FNI_MonitorEnter()
	return new Translation.Nx(_call_FNI_Monitor(tf, source, dg, objectref,
						    true));
    }
    // XXX in single-threaded mode, this can be a NOP.
    public Translation.Exp monitorExit(TreeFactory tf, HCodeElement source,
				       DerivationGenerator dg,
				       Translation.Exp objectref) {
	if (Boolean.getBoolean("harpoon.runtime1.nosync"))
	    return new Translation.Ex(new CONST(tf, source, 0));
	// call FNI_MonitorExit()
	return new Translation.Nx(_call_FNI_Monitor(tf, source, dg, objectref,
						    false));
    }
    /** wrap objectref and then call FNI_Monitor{Enter|Exit}() */
    private Stm _call_FNI_Monitor(TreeFactory tf, HCodeElement source,
				  DerivationGenerator dg,
				  Translation.Exp objectref,
				  boolean isEnter/*else exit*/) {
	// keep this synchronized with StubCode.java
	// and Runtime/include/jni-private.h
	final int REF_OFFSET = 3 * POINTER_SIZE;

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
	       (objectref.unEx(tf), null))));

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
	
	return new SEQ(tf, source, result0, result1);
    }

    public Translation.Exp objectNew(TreeFactory tf, HCodeElement source,
				     DerivationGenerator dg,
				     AllocationProperties ap,
				     HClass classType, boolean initialize) {
	Util.assert(!classType.isArray());
	Util.assert(!classType.isPrimitive());
	int length = objectSize(classType);
	Exp object = objAlloc(tf, source, dg, ap, classType,
			      new CONST(tf, source, length));
	if (initialize) {
	    // use memset to initialize all fields to 0.
	    final Temp t = new Temp(tf.tempFactory());
	    object = new ESEQ
		(tf, source,
		 new SEQ
		 (tf, source,
		  new MOVE
		  (tf, source,
		   DECLARE(dg, classType, t,
		   new TEMP(tf, source, Type.POINTER, t)),
		   object),
		  new NATIVECALL
		  (tf, source, null,
		   DECLARE(dg, HClass.Void/*c library function*/,
		   new NAME(tf, source, new Label
			    (runtime.nameMap.c_function_name("memset")))),
		   new ExpList
		   (new BINOP
		    (tf, source, Type.POINTER, Bop.ADD,
		     DECLARE(dg, classType, t,
		     new TEMP(tf, source, Type.POINTER, t)),
		     new CONST(tf, source, OBJ_FZERO_OFF)),
		    new ExpList
		    (new CONST(tf, source, 0),
		     new ExpList
		     (new CONST(tf, source, length),
		      null))))),
		 DECLARE(dg, classType, t,
		 new TEMP(tf, source, Type.POINTER, t)));
	}
	return new Translation.Ex(object);
    }

    public Translation.Exp classConst(TreeFactory tf, HCodeElement source,
			  DerivationGenerator dg, HClass classData) {
	Exp clsref = new NAME(tf, source,
			      runtime.nameMap.label(classData, "classobj"));
	DECLARE(dg, linker.forName("java.lang.Class"), clsref);
	return new Translation.Ex(clsref);
    }
    public Translation.Exp fieldConst(TreeFactory tf, HCodeElement source,
			  DerivationGenerator dg, HField fieldData) {
	Exp fldref = new NAME(tf, source,
			      runtime.nameMap.label(fieldData, "obj"));
	DECLARE(dg, linker.forName("java.lang.reflect.Field"), fldref);
	return new Translation.Ex(fldref);
    }
    public Translation.Exp methodConst(TreeFactory tf, HCodeElement source,
			   DerivationGenerator dg, HMethod methodData) {
	Exp mthref = new NAME(tf, source,
			      runtime.nameMap.label(methodData, "obj"));
	DECLARE(dg, linker.forName("java.lang.reflect.Method"), mthref);
	return new Translation.Ex(mthref);
    }

    public Translation.Exp stringConst(TreeFactory tf, HCodeElement source,
				       DerivationGenerator dg,
				       String stringData) {
	stringSet.add(stringData);
	Exp strref = new NAME(tf, source, runtime.nameMap.label(stringData));
	DECLARE(dg, linker.forName("java.lang.String"), strref);
	return new Translation.Ex(strref);
    }

    public Translation.Exp arrayBase(TreeFactory tf, HCodeElement source,
				     DerivationGenerator dg,
				     Translation.Exp objectref) {
	return new Translation.Ex
	    (new BINOP
	     (tf, source, Type.POINTER, Bop.ADD,
	      objectref.unEx(tf),
	      new CONST(tf, source, OBJ_AZERO_OFF)));
    }
    public Translation.Exp arrayOffset(TreeFactory tf, HCodeElement source,
				       DerivationGenerator dg,
				       HClass arrayType, Translation.Exp index)
    {
	Util.assert(arrayType.isArray());
	HClass compType = arrayType.getComponentType();
	int elementsize = POINTER_SIZE;
	if (compType.isPrimitive())
	    elementsize =
		(compType==HClass.Long || compType==HClass.Double) 
		? LONG_WORD_SIZE :
	        (compType==HClass.Int || compType==HClass.Float)
		? WORD_SIZE :
	        (compType==HClass.Short || compType==HClass.Char)
		? 2 : 1;
	return new Translation.Ex
	    (new BINOP
	     // should this type be POINTER or INT? [consider long arrays]
	     (tf, source, Type.INT, Bop.MUL,
	      index.unEx(tf),
	      new CONST(tf, source, elementsize)));
    }
    public Translation.Exp fieldBase(TreeFactory tf, HCodeElement source,
				     DerivationGenerator dg,
				     Translation.Exp objectref) {
	return new Translation.Ex
	    (new BINOP
	     (tf, source, Type.POINTER, Bop.ADD,
	      objectref.unEx(tf),
	      new CONST(tf, source, OBJ_FZERO_OFF)));
    }
    public Translation.Exp fieldOffset(TreeFactory tf, HCodeElement source,
					DerivationGenerator dg,
				       HField field) {
	Util.assert(!field.isStatic());
	return new Translation.Ex
	    (new CONST(tf, source, cfm.fieldOffset(field)));
    }
    public Translation.Exp methodBase(TreeFactory tf, HCodeElement source,
					DerivationGenerator dg,
				      Translation.Exp objectref) {
	return new Translation.Ex
	    (new BINOP
	     (tf, source, Type.POINTER, Bop.ADD,
	      DECLARE(dg, HClass.Void/*claz pointer*/,
	      new MEM
	      (tf, source, Type.POINTER,
	       new BINOP
	       (tf, source, Type.POINTER, Bop.ADD,
		objectref.unEx(tf),
		new CONST(tf, source, OBJ_CLAZ_OFF)))),
	      new CONST(tf, source, CLAZ_METHODS_OFF)));
    }
    public Translation.Exp methodOffset(TreeFactory tf, HCodeElement source,
					DerivationGenerator dg,
					HMethod method) {
	Util.assert(!method.isStatic());
	if (method.isInterfaceMethod()) {
	    // use interface method map.
	    return new Translation.Ex
		(new CONST(tf, source,
			   CLAZ_INTERFACES_OFF - CLAZ_METHODS_OFF -
			   imm.methodOrder(method) * POINTER_SIZE));
	} else { 
	    // use class method map.
	    return new Translation.Ex
		(new CONST(tf, source,
			   cmm.methodOrder(method) * POINTER_SIZE));
	}
    }
}
