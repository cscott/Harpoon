// TreeBuilder.java, created Sat Sep 25 07:23:21 1999 by cananian
// Copyright (C) 1999 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Backend.Runtime1;

import harpoon.Analysis.ClassHierarchy;
import harpoon.Backend.Maps.ClassDepthMap;
import harpoon.Backend.Maps.FieldMap;
import harpoon.Backend.Maps.MethodMap;
import harpoon.Backend.Maps.OffsetMap;
import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HCodeElement;
import harpoon.ClassFile.HField;
import harpoon.ClassFile.HMethod;
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
import harpoon.IR.Tree.ESEQ;
import harpoon.IR.Tree.EXP;
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
 * @version $Id: TreeBuilder.java,v 1.1.2.16 2000-01-11 18:47:16 cananian Exp $
 */
public class TreeBuilder extends harpoon.Backend.Generic.Runtime.TreeBuilder {
    // allocation strategy to use.
    final AllocationStrategy as;

    // integer constant sizes:
    final int WORD_SIZE;
    final int LONG_WORD_SIZE;
    final int POINTER_SIZE;
    final int OBJECT_HEADER_SIZE;
    // integer constant offsets:
    // layout of oobj
    final int OBJ_CLAZ_OFF;
    final int OBJ_HASH_OFF;
    final int OBJ_ALENGTH_OFF;
    final int OBJ_AZERO_OFF;
    final int OBJ_FZERO_OFF;
    // layout of claz
    final int CLAZ_INTERFACES_OFF;
    final int CLAZ_CLAZINFO;
    final int CLAZ_COMPONENT_OFF;
    final int CLAZ_INTERFZ_OFF;
    final int CLAZ_SIZE_OFF;
    final int CLAZ_DEPTH_OFF;
    final int CLAZ_DISPLAY_OFF;
    final int CLAZ_METHODS_OFF;
    
    // helper maps.
    final Runtime runtime;
    final ClassDepthMap cdm;
    final MethodMap imm;
    final MethodMap cmm;
    final FieldMap  cfm;

    // set of string references made
    final Set stringSet = new HashSet();

    TreeBuilder(Runtime runtime, ClassHierarchy ch,
		AllocationStrategy as, boolean pointersAreLong) {
	this.runtime = runtime;
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

	OBJECT_HEADER_SIZE = WORD_SIZE + 1 * POINTER_SIZE;
	// layout of oobj
	OBJ_CLAZ_OFF    = 0 * POINTER_SIZE;
	OBJ_HASH_OFF    = OBJ_CLAZ_OFF + 1 * POINTER_SIZE;
	OBJ_FZERO_OFF   = OBJ_HASH_OFF + 1 * WORD_SIZE;
	OBJ_ALENGTH_OFF = OBJ_HASH_OFF + 1 * WORD_SIZE;
	OBJ_AZERO_OFF   = OBJ_ALENGTH_OFF + 1 * WORD_SIZE;
	// layout of claz
	CLAZ_INTERFACES_OFF = -1 * POINTER_SIZE;
	CLAZ_CLAZINFO    = 0 * POINTER_SIZE;
	CLAZ_COMPONENT_OFF=1 * POINTER_SIZE;
	CLAZ_INTERFZ_OFF = 2 * POINTER_SIZE;
	CLAZ_SIZE_OFF	 = 3 * POINTER_SIZE;
	CLAZ_DEPTH_OFF   = 3 * POINTER_SIZE + 1 * WORD_SIZE;
	CLAZ_DISPLAY_OFF = 3 * POINTER_SIZE + 2 * WORD_SIZE;
	CLAZ_METHODS_OFF = CLAZ_DISPLAY_OFF + (1+cdm.maxDepth())*POINTER_SIZE;
    }
    // use the field offset map to get the object size (not including header)
    int objectSize(HClass hc) {
	List l = cfm.fieldList(hc);
	if (l.size()==0) return 0;
	HField lastfield = (HField) l.get(l.size()-1);
	return cfm.fieldOffset(lastfield) + cfm.fieldSize(lastfield);
    }

    // allocate 'length' bytes plus object header; fill in object header.
    // shift return pointer appropriately for an object reference.
    public Exp objAlloc(TreeFactory tf, HCodeElement source,
			HClass objectType, Exp length) {
	Temp Tobj = new Temp(tf.tempFactory(), "rt");
	return new ESEQ
	    (tf, source,
	     new SEQ
	     (tf, source,
	      new MOVE // allocate memory; put pointer in Tobj.
	      (tf, source,
	       new TEMP(tf, source, Type.POINTER, Tobj),
	       as.memAlloc
	       (tf, source,
		new BINOP
		(tf, source, Type.POINTER, Bop.ADD,
		 length,
		 new CONST(tf, source, OBJECT_HEADER_SIZE)))),
	      new SEQ
	      (tf, source,
	       new MOVE // assign the new object a hashcode.
	       (tf, source,
		new MEM
		(tf, source, Type.INT,
		 new BINOP
		 (tf, source, Type.POINTER, Bop.ADD,
		  new TEMP(tf, source, Type.POINTER, Tobj),
		  new CONST(tf, source, OBJ_HASH_OFF))),
		new UNOP(tf, source, Type.POINTER, Uop._2I,
			 new TEMP(tf, source, Type.POINTER, Tobj))),
	       new MOVE // assign the new object a class pointer.
	       (tf, source,
		new MEM
		(tf, source, Type.POINTER,
		 new BINOP
		 (tf, source, Type.POINTER, Bop.ADD,
		  new TEMP(tf, source, Type.POINTER, Tobj),
		  new CONST(tf, source, OBJ_CLAZ_OFF))),
		new NAME(tf, source, runtime.nameMap.label(objectType))))),
	     // result of ESEQ is new object pointer
	     new TEMP(tf, source, Type.POINTER, Tobj));
    }

    public Translation.Exp arrayLength(TreeFactory tf, HCodeElement source,
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
	return new Translation.Ex
	   (new ESEQ
	    (tf, source,
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
		new TEMP(tf, source, Type.POINTER, Tarr),
		objAlloc // allocate array data
		(tf, source, arrayType,
		 new BINOP // compute array data size:
		 (tf, source, Type.INT, Bop.ADD,
		  new BINOP // multiply...
		  (tf, source, Type.INT, Bop.MUL,
		   // ...array length by ...
		   new TEMP(tf, source, Type.INT, Tlen),
		   // ...element size...
		   new CONST(tf, source, elementSize)),
		  // and add WORD_SIZE for length field.
		  new CONST(tf, source, WORD_SIZE))))),
	      new MOVE // now set length field of newly-created array.
	      (tf, source,
	       new MEM
	       (tf, source, Type.INT, // length field is of type INT.
		new BINOP // offset array base to get location of length field
		(tf, source, Type.POINTER, Bop.ADD,
		 new TEMP(tf, source, Type.POINTER, Tarr),
		 new CONST(tf, source, OBJ_ALENGTH_OFF))),
	       new TEMP(tf, source, Type.INT, Tlen))), // length from Tlen
	     // result of whole expression is the array pointer, in Tarr
	     new TEMP(tf, source, Type.POINTER, Tarr)));
    }

    public Translation.Exp componentOf(TreeFactory tf, HCodeElement source,
				       Translation.Exp arrayref,
				       Translation.Exp componentref) {
	// component clazz pointer of arrayref
	Exp e0 = new MEM(tf, source, Type.POINTER,
			 new BINOP // offset to get component type pointer
			 (tf, source, Type.POINTER, Bop.ADD,
			  new CONST(tf, source, CLAZ_COMPONENT_OFF),
			  // dereference object to claz structure.
			  new MEM(tf, source, Type.POINTER,
				  new BINOP // offset to get claz pointer
				  (tf, source, Type.POINTER, Bop.ADD,
				   arrayref.unEx(tf),
				   new CONST(tf, source, OBJ_CLAZ_OFF)))));
	// class pointer of componentref
	Exp e1 = new MEM(tf, source, Type.POINTER,
			 new BINOP // offset to get claz pointer
			 (tf, source, Type.POINTER, Bop.ADD,
			  componentref.unEx(tf),
			  new CONST(tf, source, OBJ_CLAZ_OFF)));
	// move claz pointer of arrayref component to a temporary variable.
	Temp Tac = new Temp(tf.tempFactory(), "rt");
	Stm s0 = new MOVE(tf, source,
			  new TEMP(tf, source, Type.POINTER, Tac),
			  e0); // now use Tac instead of e0.
	// class depth of arrayref component type.
	Exp e2 = new MEM(tf, source, Type.INT,
			 new BINOP // offset to class depth.
			 (tf, source, Type.POINTER, Bop.ADD,
			  new CONST(tf, source, CLAZ_DEPTH_OFF),
			  new TEMP(tf, source, Type.POINTER, Tac)));
	// we assert that MEM(e0+e2)==e0 by definition
	// that is, element of class display at class_depth is the class itself
	// so, the component-of check is just whether MEM(e1+e2)==e0
	Exp e3 = new BINOP
	    (tf, source, Type.POINTER, Bop.CMPEQ,
	     new MEM
	     (tf, source, Type.POINTER,
	      new BINOP
	      (tf, source, Type.POINTER, Bop.ADD,
	       new BINOP
	       (tf, source, Type.POINTER, Bop.ADD,
		new CONST(tf, source, CLAZ_DISPLAY_OFF),
		e2),
	       e1)),
	     new TEMP(tf, source, Type.POINTER, Tac));

	return new Translation.Ex(new ESEQ(tf, source, s0, e3));
    }

    public Translation.Exp instanceOf(final TreeFactory tf,
				      final HCodeElement source,
				      final Translation.Exp objref,
				      final HClass classType)
    {
	final Label Lclaz = runtime.nameMap.label(classType);
	// two cases: class or interface type.
	if (classType.isInterface()) {
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
			 new TEMP(tf, source, Type.POINTER, Til),
			 // dereference claz structure for interface list ptr
			 new MEM(tf, source, Type.POINTER,
				 new BINOP // offset to get interface pointer
				 (tf, source, Type.POINTER, Bop.ADD,
				  // dereference object to claz structure.
				  new MEM(tf, source, Type.POINTER,
					  new BINOP // offset to get claz ptr
					  (tf, source, Type.POINTER, Bop.ADD,
					   objref.unEx(tf),
					   new CONST
					   (tf, source, OBJ_CLAZ_OFF))),
				  new CONST(tf, source, CLAZ_INTERFZ_OFF))));
		    // loop body: test *il against Lclaz.
		    Stm s1 = new CJUMP
			(tf, source,
			 new BINOP
			 (tf, source, Type.POINTER, Bop.CMPEQ,
			  new MEM(tf, source, Type.POINTER,
				  new TEMP(tf, source, Type.POINTER, Til)),
			  new NAME(tf, source, Lclaz)),
			 iftrue, Ladv);
		    // advance il
		    Stm s2 = new MOVE
			(tf, source,
			 new TEMP(tf, source, Type.POINTER, Til),
			 new BINOP
			 (tf, source, Type.POINTER, Bop.ADD,
			  new TEMP(tf, source, Type.POINTER, Til),
			  new CONST(tf, source, POINTER_SIZE)));
		    // loop guard: test *il against null.
		    Stm s3 = new CJUMP
			(tf, source,
			 new BINOP
			 (tf, source, Type.POINTER, Bop.CMPEQ,
			  new MEM(tf, source, Type.POINTER,
				  new TEMP(tf, source, Type.POINTER, Til)),
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
		  new NAME(tf, source, Lclaz), // claz pointer
		  // dereference claz structure for class display ptr
		  new MEM(tf, source, Type.POINTER,
			  new BINOP // offset to get display pointer
			  (tf, source, Type.POINTER, Bop.ADD,
			   new CONST(tf, source,CLAZ_DISPLAY_OFF+class_offset),
			   // dereference object to claz structure.
			   new MEM(tf, source, Type.POINTER,
				   new BINOP // offset to get claz pointer
				   (tf, source, Type.POINTER, Bop.ADD,
				    objref.unEx(tf),
				    new CONST(tf, source, OBJ_CLAZ_OFF)))))));
	}
    }

    // MONITORENTER NOT IMPLEMENTED
    public Translation.Exp monitorEnter(TreeFactory tf, HCodeElement source,
					Translation.Exp objectref) {
	return objectref; // nop
    }
    // MONITOREXIT NOT IMPLEMENTED
    public Translation.Exp monitorExit(TreeFactory tf, HCodeElement source,
				       Translation.Exp objectref) {
	return objectref; // nop
    }

    public Translation.Exp objectNew(TreeFactory tf, HCodeElement source,
				     HClass classType, boolean initialize) {
	Util.assert(!classType.isArray());
	Util.assert(!classType.isPrimitive());
	int length = objectSize(classType);
	Exp object = objAlloc(tf, source, classType,
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
		   new TEMP(tf, source, Type.POINTER, t),
		   object),
		  new NATIVECALL
		  (tf, source, null,
		   new NAME(tf, source, new Label("_memset")),
		   new ExpList
		   (new BINOP
		    (tf, source, Type.POINTER, Bop.ADD,
		     new TEMP(tf, source, Type.POINTER, t),
		     new CONST(tf, source, OBJ_FZERO_OFF)),
		    new ExpList
		    (new CONST(tf, source, 0),
		     new ExpList
		     (new CONST(tf, source, length),
		      null))))),
		 new TEMP(tf, source, Type.POINTER, t));
	}
	return new Translation.Ex(object);
    }

    public Translation.Exp stringConst(TreeFactory tf, HCodeElement source,
				       String stringData) {
	stringSet.add(stringData);
	Exp strref = new NAME(tf, source, runtime.nameMap.label(stringData));
	return new Translation.Ex(strref);
    }

    public Translation.Exp arrayBase(TreeFactory tf, HCodeElement source,
				     Translation.Exp objectref) {
	return new Translation.Ex
	    (new BINOP
	     (tf, source, Type.POINTER, Bop.ADD,
	      objectref.unEx(tf),
	      new CONST(tf, source, OBJ_AZERO_OFF)));
    }
    public Translation.Exp arrayOffset(TreeFactory tf, HCodeElement source,
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
				     Translation.Exp objectref) {
	return new Translation.Ex
	    (new BINOP
	     (tf, source, Type.POINTER, Bop.ADD,
	      objectref.unEx(tf),
	      new CONST(tf, source, OBJ_FZERO_OFF)));
    }
    public Translation.Exp fieldOffset(TreeFactory tf, HCodeElement source,
				       HField field) {
	Util.assert(!field.isStatic());
	return new Translation.Ex
	    (new CONST(tf, source, cfm.fieldOffset(field)));
    }
    public Translation.Exp methodBase(TreeFactory tf, HCodeElement source,
				      Translation.Exp objectref) {
	return new Translation.Ex
	    (new BINOP
	     (tf, source, Type.POINTER, Bop.ADD,
	      new MEM
	      (tf, source, Type.POINTER,
	       new BINOP
	       (tf, source, Type.POINTER, Bop.ADD,
		objectref.unEx(tf),
		new CONST(tf, source, OBJ_CLAZ_OFF))),
	      new CONST(tf, source, CLAZ_METHODS_OFF)));
    }
    public Translation.Exp methodOffset(TreeFactory tf, HCodeElement source,
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
