// TreeBuilder.java, created Sat Sep 25 07:23:21 1999 by cananian
// Copyright (C) 1999 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Backend.RuntimeTiny;

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
 * <code>RuntimeTiny.TreeBuilder</code> extends
 * <code>Runtime1.TreeBuilder</code> to implement a more-compressed
 * (but slower) object layout.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: TreeBuilder.java,v 1.1.2.6 2002-03-16 13:23:24 cananian Exp $
 */
public class TreeBuilder extends harpoon.Backend.Runtime1.TreeBuilder { 
    final Runtime runtime;

    protected TreeBuilder(Runtime runtime,
			  Linker linker,
			  AllocationStrategy as, boolean pointersAreLong) {
	super(runtime, linker, as, pointersAreLong, 0/* hard-code ptr algn of zero*/);
	this.runtime = runtime;
	// XXX should really readjust offsets to account for claz being INT
	// not pointer.
    }
    // byte-align all fields.
    protected FieldMap initClassFieldMap() {
	final FieldMap sfm = super.initClassFieldMap();
	final Runtime runtime = (Runtime) super.runtime;
	if (!runtime.byteAlign) return sfm;
	return new TinyPackedClassFieldMap(runtime/*-4+runtime.clazBytes*/) {
		public int fieldOffset(HField hf) {
		    // hack to allow allocating fields in the empty
		    // space left by the small claz index.
		    int off = super.fieldOffset(hf);
		    //if (off<0) off-=4;
		    return off;
		}
		public int fieldSize(HField hf) { return sfm.fieldSize(hf); }
		// conservative gc requires pointers to be aligned.
		public int fieldAlignment(HField hf) {
		    if (hf.getType().isPrimitive()) return 1;
		    return sfm.fieldAlignment(hf);
		}
		// "try hard" to align other types of fields.
		public int fieldPreferredAlignment(HField hf) {
		    return sfm.fieldAlignment(hf);
		}
	    };
    }
    protected FieldMap getClassFieldMap() { return cfm; }

    public int objectSize(HClass hc) {
	int sz = super.objectSize(hc);
	// because we can allocate fields at negative offsets, the
	// superclass sometimes says we've got negative size!
	// now, we know that ain't true: it just means that we need
	// no fields past the header, i.e. that "non-header size"==0.
	return (sz<0) ? 0 : sz;
    }

    // allocate 'length' bytes plus object header; fill in object header.
    // shift return pointer appropriately for an object reference.
    public Exp objAlloc(TreeFactory tf, HCodeElement source,
			DerivationGenerator dg,
			AllocationProperties ap,
			HClass objectType, Exp length) {
	if (!runtime.clazShrink)
	    return super.objAlloc(tf, source, dg, ap, objectType, length);
	Temp Tobj = new Temp(tf.tempFactory(), "rt");
	// masked version of object pointer.
	Temp Tmasked = new Temp(tf.tempFactory(), "rt");
	Derivation.DList maskedDL = new Derivation.DList(Tobj, true, null);
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
	       new MOVE // save a masked version of the pointer.
	       (tf, source,
		DECLARE(dg, maskedDL,
		new TEMP(tf, source, Type.POINTER, Tmasked)),
		PTRMASK(tf, source, dg,
		DECLARE(dg, objectType/*not an obj yet*/, Tobj,
		new TEMP(tf, source, Type.POINTER, Tobj)))),
	      new SEQ
	      (tf, source,
	       new MOVE // assign the new object a hashcode.
	       (tf, source,
		DECLARE(dg, HClass.Void/*hashcode, not an object*/,
		new MEM
		(tf, source, Type.POINTER, /* hashcode is pointer size */
		 new BINOP
		 (tf, source, Type.POINTER, Bop.ADD,
		  DECLARE(dg, maskedDL,
		  new TEMP(tf, source, Type.POINTER, Tmasked)),
		  new CONST(tf, source, OBJ_HASH_OFF)))),
		new BINOP // set the low bit to indicate an uninflated object.
		(tf, source, Type.POINTER, Bop.ADD,
		 DECLARE(dg, maskedDL,
		 new TEMP(tf, source, Type.POINTER, Tmasked)),
		 new CONST(tf, source, 1))),
	       new MOVE // assign the new object a class *index*.
	       (tf, source,
		 new MEM
		 (tf, source, Type.INT, /* index, not pointer anymore */
		  new BINOP
		  (tf, source, Type.POINTER, Bop.ADD,
		   DECLARE(dg, maskedDL,
			   new TEMP(tf, source, Type.POINTER, Tmasked)),
		   new CONST(tf, source, OBJ_CLAZ_OFF))),
		new CONST(tf, source, runtime.cn.clazNumber(objectType)))))),
	     // result of ESEQ is new object pointer
	     DECLARE(dg, objectType/*finally an obj*/, Tobj,
	     new TEMP(tf, source, Type.POINTER, Tobj)));
    }

    protected Exp _claz_(TreeFactory tf, HCodeElement source,
		       DerivationGenerator dg,
		       Translation.Exp objectref) {
	if (!runtime.clazShrink)
	    return super._claz_(tf, source, dg, objectref);
	// okay, load a compressed claz pointer, using the claz table.
	Exp index_pointer =
	    new BINOP
	    (tf, source, Type.POINTER, Bop.ADD,
	     PTRMASK(tf, source, dg, objectref.unEx(tf)),
	     new CONST(tf, source, OBJ_CLAZ_OFF));
	int bitwidth=runtime.clazBytes * 8;
	if (bitwidth>16) bitwidth=32; // XXX no 24-bit types
	Exp index_value =
	    new MEM
	    (tf, source, bitwidth, false,
	     index_pointer);
	// now look up index in claz table
	Exp claz_pointer_pointer =
	    new BINOP
	    (tf, source, Type.POINTER, Bop.ADD,
	     new BINOP
	     (tf, source, Type.INT, Bop.MUL,
	      index_value,
	      new CONST(tf, source, POINTER_SIZE)),
	     new NAME
	     (tf, source, new Label
	      (runtime.getNameMap().c_function_name("FNI_claz_table"))));
	return DECLARE(dg, HClass.Void/*claz pointer*/,
		       new MEM
		       (tf, source, Type.POINTER, claz_pointer_pointer));
    }
}
