// TreeBuilder.java, created Sat Sep 25 07:23:21 1999 by cananian
// Copyright (C) 1999 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Backend.Runtime1;

import harpoon.Backend.Maps.ClassDepthMap;
import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HCodeElement;
import harpoon.IR.Tree.TreeFactory;
import harpoon.IR.Tree.Stm;
import harpoon.IR.Tree.Exp;
import harpoon.IR.Tree.Bop;
import harpoon.IR.Tree.Uop;
import harpoon.IR.Tree.Type;
import harpoon.IR.Tree.BINOP;
import harpoon.IR.Tree.CALL;
import harpoon.IR.Tree.CJUMP;
import harpoon.IR.Tree.CONST;
import harpoon.IR.Tree.DATA;
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

/**
 * <code>Runtime1.TreeBuilder</code> is an implementation of
 * <code>Generic.Runtime.TreeBuilder</code> which creates
 * accessor expressions for the <code>Runtime1</code> runtime.
 * <p>Pretty straightforward.  No weird hacks.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: TreeBuilder.java,v 1.1.2.1 1999-09-29 22:19:33 cananian Exp $
 */
public class TreeBuilder extends harpoon.Backend.Generic.Runtime.TreeBuilder {
    static final int WORD_SIZE = 4;
    static final int POINTER_SIZE = WORD_SIZE; // or twice that.
    static final int OBJECT_HEADER_SIZE = WORD_SIZE + 1 * POINTER_SIZE;
    // layout of oobj
    static final int OBJ_HASH_OFF    = -1 * POINTER_SIZE;
    static final int OBJ_CLAZ_OFF    = 0 * POINTER_SIZE;
    static final int OBJ_ALENGTH_OFF = 1 * POINTER_SIZE;
    // layout of claz
    static final int CLAZ_COMPONENT_OFF=1 * POINTER_SIZE;
    static final int CLAZ_INTERFZ_OFF = 2 * POINTER_SIZE;
    static final int CLAZ_DEPTH_OFF   = 3 * POINTER_SIZE;
    static final int CLAZ_DISPLAY_OFF = 3 * POINTER_SIZE + 1 * WORD_SIZE;

    Runtime runtime = null;
    ClassDepthMap cdm = null;

    // allocate 'length' bytes plus object header; fill in object header.
    // shift return pointer appropriately for an object reference.
    public Exp objAlloc(TreeFactory tf, HCodeElement source,
			HClass objectType, Exp length) {
	throw new Error("Scott is lazy.");
    }

    public Exp memAlloc(TreeFactory tf, HCodeElement source, Exp length) {
	throw new Error("Scott is lazy.");
    }

    public Exp arrayLength(TreeFactory tf, HCodeElement source,
			   Exp arrayRef) {
	return
	    new MEM  
	    (tf, source, Type.INT, // The "length" field is of type INT
	     new BINOP
	     (tf, source, Type.POINTER, Bop.ADD,
	      arrayRef,
	      new CONST
	      (tf, source, OBJ_ALENGTH_OFF))); // offset from array base ptr.
    }
    public Exp arrayNew(TreeFactory tf, HCodeElement source,
			HClass arrayType, Exp length) {
	Util.assert(arrayType.isArray());
	// temporary storage for created array.
	Temp Tarr = new Temp(tf.tempFactory(), "rt");
	// temporary storage for supplied length
	Temp Tlen = new Temp(tf.tempFactory(), "rt");
	// type of array components
	HClass comType = arrayType.getComponentType();
	// size of elements in array
	int elementSize = comType.isPrimitive() ?
	    ((comType==HClass.Double || comType==HClass.Long) ?
	     WORD_SIZE * 2 : WORD_SIZE) : POINTER_SIZE;
	return
	    new ESEQ
	    (tf, source,
	     new SEQ
	     (tf, source,
	      new SEQ
	      (tf, source,
	       new MOVE // save 'length' in Tlen.
	       (tf, source,
		new TEMP(tf, source, Type.INT, Tlen),
		length),
	       new MOVE // save result in Tarr
	       (tf, source,
		new TEMP(tf, source, Type.POINTER, Tarr),
		objAlloc // allocate array data
		(tf, source, arrayType,
		 new BINOP // compute array data size:
		 (tf, source, Type.INT, Bop.MUL,
		  // array length, times...
		  new TEMP(tf, source, Type.INT, Tlen),
		  // element size.
		  new CONST(tf, source, elementSize))))),
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
	     new TEMP(tf, source, Type.POINTER, Tarr));
    }

    public Stm componentOf(TreeFactory tf, HCodeElement source,
			   Exp arrayref, Exp componentref,
			   Label iftrue, Label iffalse) {
	// component clazz pointer of arrayref
	Exp e0 = new MEM(tf, source, Type.POINTER,
			 new BINOP // offset to get component type pointer
			 (tf, source, Type.POINTER, Bop.ADD,
			  new CONST(tf, source, CLAZ_COMPONENT_OFF),
			  // dereference object to claz structure.
			  new MEM(tf, source, Type.POINTER,
				  new BINOP // offset to get claz pointer
				  (tf, source, Type.POINTER, Bop.ADD,
				   arrayref,
				   new CONST(tf, source, OBJ_CLAZ_OFF)))));
	// class pointer of componentref
	Exp e1 = new MEM(tf, source, Type.POINTER,
			 new BINOP // offset to get claz pointer
			 (tf, source, Type.POINTER, Bop.ADD,
			  componentref,
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

	return new SEQ(tf, source, s0,
		       new CJUMP(tf, source, e3, iftrue, iffalse));
    }

    public Stm instanceOf(TreeFactory tf, HCodeElement source,
			  Exp objref, HClass classType,
			  Label iftrue, Label iffalse) {
	Label Lclaz = runtime.nameMap.label(classType);
	// two cases: class or interface type.
	if (classType.isInterface()) {
	    // interface type: linear search through interface list.
	    // compile as:
	    //    for (il=obj->claz->interfz; *il!=null; il++)
	    //       if (*il==classTypeLabel) return true;
	    //    return false;

	    // make our iteration variable.
	    Temp Til = new Temp(tf.tempFactory(), "rt"); // il
	    // three labels
	    Label Ladv = new Label();
	    Label Ltop = new Label();
	    Label Ltst = new Label();
	    // initialize it.
	    Stm s0 = new MOVE
		(tf, source,
		 new TEMP(tf, source, Type.POINTER, Til),
		 // dereference claz structure for interface list ptr
		 new MEM(tf, source, Type.POINTER,
			 new BINOP // offset to get interface pointer
			 (tf, source, Type.POINTER, Bop.ADD,
			  // dereference object to claz structure.
			  new MEM(tf, source, Type.POINTER,
				  new BINOP // offset to get claz pointer
				  (tf, source, Type.POINTER, Bop.ADD,
				   objref,
				   new CONST(tf, source, OBJ_CLAZ_OFF))),
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
	    //   ( s0 -> jmp Ltst -> Ltop -> s1 -> Ladv -> s2 -> Ltst -> s3 )
	    return new SEQ
		(tf, source,
		 new SEQ
		 (tf, source,
		  new SEQ(tf, source, s0, new JUMP(tf, source, Ltst)),
		  new SEQ(tf, source, new LABEL(tf, source, Ltop, false), s1)),
		 new SEQ
		 (tf, source,
		  new SEQ(tf, source, new LABEL(tf, source, Ladv, false), s2),
		  new SEQ(tf, source, new LABEL(tf, source, Ltst, false), s3))
		 );
	} else {
	    // class type: single lookup and comparison.
	    // compile as:
	    //    return obj->claz->display[CONST_OFF(classType)]==classType;
	    int class_offset = cdm.classDepth(classType);

	    return new CJUMP
		(tf, source,
		 new BINOP
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
				    objref,
				    new CONST(tf, source, OBJ_CLAZ_OFF)))))),
		 iftrue, iffalse);
	}
    }
}
