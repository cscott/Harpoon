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

import java.util.ArrayList;
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
 * @version $Id: TreeBuilder.java,v 1.8.2.1 2003-11-14 18:02:33 cananian Exp $
 */
public class TreeBuilder extends harpoon.Backend.Generic.Runtime.TreeBuilder {
    // turning on this option means that no calls to synchronization primitives
    // will be generated.  this will obviously impair correctness if such
    // primitives are actually needed by the program, but allows us to
    // make a quick-and-dirty benchmark of the maximum-possible performance
    // improvement due to synchronization optimizations.
    private static final boolean noSync =
	Boolean.getBoolean("harpoon.runtime1.nosync");
    /* turning this on means that no alignment to boundaries larger than
     * a single word will be done.  This is sufficient on some architectures.
     */
    private static final boolean singleWordAlign =
	Boolean.getBoolean("harpoon.runtime1.single-word-align");

    /** allocation strategy to use. */
    protected final AllocationStrategy as;

    // integer constant sizes:
    protected final int WORD_SIZE;
    protected final int LONG_WORD_SIZE;
    protected final int POINTER_SIZE;
    protected       int OBJECT_HEADER_SIZE;
    // integer constant offsets:
    // layout of oobj
    protected       int OBJ_CLAZ_OFF;
    protected       int OBJ_HASH_OFF;
    protected       int OBJ_ALENGTH_OFF;
    protected       int OBJ_AZERO_OFF;
    protected       int OBJ_FZERO_OFF;
    // layout of claz
    protected final int CLAZ_INTERFACES_OFF;
    protected final int CLAZ_CLAZINFO;
    protected final int CLAZ_COMPONENT_OFF;
    protected final int CLAZ_INTERFZ_OFF;
    protected final int CLAZ_SIZE_OFF;
    protected final int CLAZ_DEPTH_OFF;
    protected final int CLAZ_GCENTRY_OFF;
    protected final int CLAZ_EXTRAINFO_OFF;
    protected final int CLAZ_DISPLAY_OFF;
    protected       int CLAZ_METHODS_OFF;
    
    // helper maps.
    protected final Runtime runtime;
    protected final Linker linker;
    protected       ClassDepthMap cdm;
    protected       MethodMap imm;
    protected final MethodMap cmm;
    protected final FieldMap  cfm;

    // set of string references made
    final Set<String> stringSet = new HashSet<String>();

    // if non-zero, then all pointer values are masked before
    // dereference.  this allows us to stuff additional information
    // into the low bits of the pointer. the value specifies the
    // alignment of all object pointers; thus how many bits we
    // should/may mask.
    private final int pointerAlignment;

    // pointerAlignment==0 means don't mask pointers.
    protected TreeBuilder(Runtime runtime, Linker linker,
			  AllocationStrategy as, boolean pointersAreLong,
			  int pointerAlignment) {
	this.pointerAlignment = pointerAlignment;
	this.runtime = runtime;
	this.linker = linker;
	this.as  = as;
	this.cmm = new harpoon.Backend.Analysis.ClassMethodMap();
	this.cfm = initClassFieldMap();
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
	// ALENGTH is just a word-sized field, but we allocate POINTER_SIZE
	// to it in order to ensure that array elements are pointer-aligned
	// on 64-bit platforms.
	OBJ_AZERO_OFF   = OBJ_ALENGTH_OFF + 1 * POINTER_SIZE;
	// layout of claz
	CLAZ_INTERFACES_OFF = -1 * POINTER_SIZE;
	CLAZ_CLAZINFO    = 0 * POINTER_SIZE;
	CLAZ_COMPONENT_OFF=1 * POINTER_SIZE;
	CLAZ_INTERFZ_OFF = 2 * POINTER_SIZE;
	CLAZ_SIZE_OFF	 = 3 * POINTER_SIZE;
	CLAZ_DEPTH_OFF   = 3 * POINTER_SIZE + 1 * WORD_SIZE;
	CLAZ_GCENTRY_OFF = 3 * POINTER_SIZE + 2 * WORD_SIZE;
	CLAZ_EXTRAINFO_OFF=4 * POINTER_SIZE + 2 * WORD_SIZE; 
	CLAZ_DISPLAY_OFF = CLAZ_EXTRAINFO_OFF +
	    runtime.getExtraClazInfo().fields_size();
    }
    // hook to let our subclasses use a different classfieldmap
    protected FieldMap initClassFieldMap() {
	return new harpoon.Backend.Analysis.PackedClassFieldMap() {
	    public int fieldSize(HField hf) {
		HClass type = hf.getType();
		return (!type.isPrimitive()) ? POINTER_SIZE :
		    (type==HClass.Double||type==HClass.Long) ? LONG_WORD_SIZE :
		    (type==HClass.Int||type==HClass.Float) ? WORD_SIZE :
		    (type==HClass.Short||type==HClass.Char) ? 2 : 1;
	    }
	    // on some archs we only need to align to WORD_SIZE
	    public int fieldAlignment(HField hf) {
		int align = super.fieldAlignment(hf);
		return singleWordAlign ? Math.min(WORD_SIZE, align) : align;
	    }
       };
    }
    // this method must be called to complete initialization before
    // the tree builder is used.
    protected void setClassHierarchy(ClassHierarchy ch) {
	this.cdm = new harpoon.Backend.Maps.DefaultClassDepthMap(ch);
	this.imm = new harpoon.Backend.Analysis.InterfaceMethodMap(ch);
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

    // pointer masking.
    protected Exp PTRMASK(TreeFactory tf, HCodeElement source,
			  DerivationGenerator dg,
			  Exp e) {
	if (pointerAlignment<2) return e;
	// declare result as derived pointer.
	CONST c = (POINTER_SIZE>WORD_SIZE) ?
	    new CONST(tf, source, ~((long)(pointerAlignment-1))) :
	    new CONST(tf, source, ~((int)(pointerAlignment-1))) ;
	return new BINOP
	    (tf, source, Type.POINTER, Bop.AND, e, c);
    }
    // use the field offset map to get the object size (not including header)
    public int objectSize(HClass hc) {
	List l = cfm.fieldList(hc);
	if (l.size()==0) return 0;
	HField lastfield = (HField) l.get(l.size()-1);
	return cfm.fieldOffset(lastfield) + cfm.fieldSize(lastfield);
    }
    public int headerSize(HClass hc) { // hc is ignored
	return OBJECT_HEADER_SIZE;
    }

    // allocate 'length' bytes plus object header; fill in object header.
    // shift return pointer appropriately for an object reference.
    public Exp objAlloc(TreeFactory tf, HCodeElement source,
			DerivationGenerator dg,
			AllocationProperties ap,
			HClass objectType, Exp length) {
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
		 new CONST(tf, source, (ap.setDynamicWBFlag()?3:1)))),
	       new MOVE // assign the new object a class pointer.
	       (tf, source,
		DECLARE
		(dg, HClass.Void/*claz pointer*/,
		 new MEM
		 (tf, source, Type.POINTER,
		  new BINOP
		  (tf, source, Type.POINTER, Bop.ADD,
		   DECLARE(dg, maskedDL,
			   new TEMP(tf, source, Type.POINTER, Tmasked)),
		   new CONST(tf, source, OBJ_CLAZ_OFF)))),
		new NAME(tf, source, runtime.getNameMap().label(objectType)))))),
	     // result of ESEQ is new object pointer
	     DECLARE(dg, objectType/*finally an obj*/, Tobj,
	     new TEMP(tf, source, Type.POINTER, Tobj)));
    }

    public Stm clearHashBit(TreeFactory tf, HCodeElement source,
			    DerivationGenerator dg, Exp objExp) {
	// masked version of object pointer
	//Derivation.DList objectDL = dg.derivation(objExp);
	//assert objectDL != null : "huh?" + objExp;
	Temp Thash = new Temp(tf.tempFactory(), "cb");
	return new SEQ
	    (tf, source,
	     new MOVE // save a pointer to the hashcode field
	     (tf, source,
	      DECLARE(dg, HClass.Void/* pointer to hashcode */,
	      new TEMP(tf, source, Type.POINTER, Thash)),
	      new BINOP
	      (tf, source, Type.POINTER, Bop.ADD,
	       // DECLARE(dg, objectDL,
		       PTRMASK(tf, source, dg, objExp)/*)*/,
	       new CONST(tf, source, OBJ_HASH_OFF))),
	     new MOVE // clear next-to-low bit of hashcode
	     (tf, source,
	      DECLARE(dg, HClass.Void/*hashcode, not an object*/,
	      new MEM
	      (tf, source, Type.POINTER, /* hashcode is pointer size */
	       DECLARE(dg, HClass.Void,
	       new TEMP(tf, source, Type.POINTER, Thash)))),
	      new BINOP // mask out next-to-low bit of hashcode
	      (tf, source, Type.POINTER, Bop.AND,
	       DECLARE(dg, HClass.Void,
	       new MEM
	       (tf, source, Type.POINTER,
		DECLARE(dg, HClass.Void,
		new TEMP(tf, source, Type.POINTER, Thash)))),
	       new CONST
	       (tf, source, 
		(POINTER_SIZE>WORD_SIZE) ? ~((long)2) : ~((int)2)))));
    }

    public Translation.Exp arrayLength(TreeFactory tf, HCodeElement source,
				       DerivationGenerator dg,
				       Translation.Exp arrayRef) {
	return new Translation.Ex
	   (new MEM  
	    (tf, source, Type.INT, // The "length" field is of type INT
	     new BINOP
	     (tf, source, Type.POINTER, Bop.ADD,
	      fieldBase(tf, source, dg, arrayRef).unEx(tf),
	      new CONST
	      // offset from array base ptr.
	      (tf, source, OBJ_ALENGTH_OFF-OBJ_FZERO_OFF))));
    }
    public Translation.Exp arrayNew(TreeFactory tf, HCodeElement source,
				    DerivationGenerator dg,
				    AllocationProperties ap,
				    HClass arrayType, Translation.Exp length,
				    boolean initialize) {
	assert arrayType.isArray();
	// temporary storage for created array.
	Temp Tarr = new Temp(tf.tempFactory(), "rt");
	// temporary storage for supplied length
	Temp Tlen = new Temp(tf.tempFactory(), "rt");
	// temporary storage for length to be initialized.
	Temp Tini = new Temp(tf.tempFactory(), "rt");
	// type of array components
	HClass comType = arrayType.getComponentType();
	// size of elements in array
	int elementSize = !comType.isPrimitive() ? POINTER_SIZE :
	    (comType==HClass.Double || comType==HClass.Long) ? (WORD_SIZE*2) :
	    (comType==HClass.Byte || comType==HClass.Boolean) ? 1 :
	    (comType==HClass.Char || comType==HClass.Short) ? 2 :
	    WORD_SIZE;
	Exp size =
	    new BINOP // compute array data size:
	    (tf, source, Type.INT, Bop.ADD,
	     new BINOP // multiply...
	     (tf, source, Type.INT, Bop.MUL,
	      // ...array length by ...
	      new TEMP(tf, source, Type.INT, Tlen),
	      // ...element size...
	      new CONST(tf, source, elementSize)),
	     // and add WORD_SIZE (and more) for length field (and others)
	     new CONST(tf, source, OBJ_AZERO_OFF - OBJ_FZERO_OFF));
	if (Boolean.getBoolean("harpoon.runtime1.arraybloat")) {
	    int ptrbits = POINTER_SIZE*8;
	    // allocate additional one bit per element for transactions bloat.
	    //  size += ((Tlen+ptrbits-1) & (~(ptrbits-1))) >> 3
	    size = 
		new BINOP
		(tf, source, Type.INT, Bop.ADD,
		 size,
		 new BINOP
		 (tf, source, Type.INT, Bop.USHR,
		  new BINOP
		  (tf, source, Type.INT, Bop.AND,
		   new BINOP
		   (tf, source, Type.INT, Bop.ADD,
		    new TEMP(tf, source, Type.INT, Tlen),
		    new CONST(tf, source, ptrbits-1)),
		   new CONST(tf, source, ~(ptrbits-1))),
		  new CONST(tf, source, 3)));
	}
	if (initialize) // save the 'size' value for re-use (if needed)
	    size =
		new ESEQ
		(tf, source,
		 new MOVE // save 'size' in Tini
		 (tf, source,
		  new TEMP(tf, source, Type.INT, Tini),
		  size),
		 new TEMP(tf, source, Type.INT, Tini));
        Stm stm =
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
		 size)));
	// now initialize either just the object fields or the whole
	// array, depending on the value of the 'initialize' boolean.
	// (note this must happen before we set the length field, as
	//  the length falls between the object fields and the rest of
	//  the array)
        if (initialize || OBJ_ALENGTH_OFF != OBJ_FZERO_OFF) {
	    Exp initlen = initialize ?
		(Exp) new TEMP(tf, source, Type.INT, Tini) :
		(Exp) new CONST(tf, source, OBJ_ALENGTH_OFF-OBJ_FZERO_OFF);
            stm = new SEQ
                (tf, source, stm,
                 new NATIVECALL
                 (tf, source, null,
                  DECLARE(dg, HClass.Void/*c library function*/,
                  new NAME(tf, source, new Label
                           (runtime.getNameMap().c_function_name("memset")))),
                  new ExpList
                  (fieldBase(tf, source, dg, new Translation.Ex
			     (DECLARE(dg, arrayType/*not an obj yet*/, Tarr,
			      new TEMP(tf, source, Type.POINTER, Tarr))))
		   .unEx(tf),
                   new ExpList
                   (new CONST(tf, source, 0),
                    new ExpList
                    (initlen,
                     null)))));
	}
	// set length field of newly-created array.
	stm = new SEQ
	    (tf, source, stm,
	      new MOVE
	      (tf, source,
	       new MEM
	       (tf, source, Type.INT, // length field is of type INT.
		new BINOP // offset array base to get location of length field
		(tf, source, Type.POINTER, Bop.ADD,
		fieldBase(tf, source, dg, new Translation.Ex
			  (DECLARE(dg, arrayType/*not an obj yet*/, Tarr,
			   new TEMP(tf, source, Type.POINTER, Tarr))))
		.unEx(tf),
		 new CONST(tf, source, OBJ_ALENGTH_OFF-OBJ_FZERO_OFF))),
	       new TEMP(tf, source, Type.INT, Tlen))); // length from Tlen
	// and make an expression with the value of the array pointer.
        return new Translation.Ex
            (new ESEQ
             (tf, source, stm,
	     // result of whole expression is the array pointer, in Tarr
	     DECLARE(dg, arrayType/*finally an obj*/, Tarr,
		     new TEMP(tf, source, Type.POINTER, Tarr))));
    }

    public Translation.Exp componentOf(final TreeFactory tf,
				       final HCodeElement source,
				       final DerivationGenerator dg,
				       final Translation.Exp arrayref,
				       final Translation.Exp componentref) {
	List stmlist = new ArrayList(5);
	// component clazz pointer of arrayref
	Exp e0 = DECLARE(dg, HClass.Void/*component claz ptr*/,
                 new MEM(tf, source, Type.POINTER,
			 new BINOP // offset to get component type pointer
			 (tf, source, Type.POINTER, Bop.ADD,
			  new CONST(tf, source, CLAZ_COMPONENT_OFF),
			  // dereference object to claz structure.
			  _claz_(tf, source, dg, arrayref))));
	// move claz pointer of arrayref component to a temporary variable.
	final Temp Tac = new Temp(tf.tempFactory(), "rt");
	Stm s0 = new MOVE(tf, source,
			  DECLARE(dg, HClass.Void/*component claz ptr*/, Tac,
				  new TEMP(tf, source, Type.POINTER, Tac)),
			  e0); // now use Tac instead of e0.
	stmlist.add(s0);
	// class pointer of componentref
	Exp e1 = _claz_(tf, source, dg, componentref);
	// move claz pointer of componentref to a temporary variable.
	final Temp Tcc = new Temp(tf.tempFactory(), "rt");
	Stm s1 = new MOVE(tf, source,
			  DECLARE(dg, HClass.Void/*component claz ptr*/, Tcc,
				  new TEMP(tf, source, Type.POINTER, Tcc)),
			  e1); // now use Tcc instead of e1.
	stmlist.add(s1);
	// class depth of arrayref component type.
	Exp e2 = new MEM(tf, source, Type.INT,
			 new BINOP // offset to class depth.
			 (tf, source, Type.POINTER, Bop.ADD,
			  new CONST(tf, source, CLAZ_DEPTH_OFF),
			  DECLARE(dg, HClass.Void/*component claz ptr*/, Tac,
				  new TEMP(tf, source, Type.POINTER, Tac))));
	// if arrayref is an array with an ultimately interface base type
	// (after all the dims have been stripped away), we need to look
	// through the interfacelist to implement instanceof, instead of
	// relying on the class display table.  So we need to identify these
	// cases. [we actually look at the component type of the arrayref]
	// For arrays of interfaces and interfaces, e2==0 and MEM(e0+e2)!=e0
	// MEM(e0+e2)==e0 for all others.  check e2==0 first to avoid an
	// unnecessary memory reference (for everything except Object[]
	// arrays).
	final Temp Tcd = new Temp(tf.tempFactory(), "rt"); // class depth
	Stm s2 = new MOVE(tf, source,
			  new TEMP(tf, source, Type.INT, Tcd),
			  e2); // now use Tcd instead of e2.
	stmlist.add(s2);
    	final Label Lisinterface = new Label();
    	final Label Lmaybeinterface = new Label();
    	final Label Lnotinterface = new Label();
	stmlist.add
	    (new CJUMP(tf, source,
		       new BINOP(tf, source, Type.INT, Bop.CMPEQ,
				 new TEMP(tf, source, Type.INT, Tcd),
				 new CONST(tf, source, 0)),
		       Lmaybeinterface, Lnotinterface));
	stmlist.add
	    (new LABEL(tf, source, Lmaybeinterface, false));
	// now check whether MEM(e0+e2)==NULL (then this is interface type!)
	stmlist.add
	    (new CJUMP(tf, source,
		       new BINOP
		       (tf, source, Type.POINTER, Bop.CMPEQ,
			DECLARE(dg, HClass.Void/*component claz ptr*/, Tac,
			new TEMP(tf, source, Type.POINTER, Tac)),
			DECLARE(dg, HClass.Void/*claz ptr in display*/,
			new MEM
			(tf, source, Type.POINTER,
			 new BINOP
			 (tf, source, Type.POINTER, Bop.ADD,
			  new BINOP
			  (tf, source, Type.INT, Bop.ADD,
			   new CONST(tf, source, CLAZ_DISPLAY_OFF),
			   new TEMP(tf, source, Type.INT, Tcd)),
			  DECLARE(dg, HClass.Void/*component claz ptr*/, Tac,
			  new TEMP(tf, source, Type.POINTER, Tac)))))),
		       Lnotinterface, Lisinterface));

	/* common initialization code (selects interface/non-interface case) */
	final Stm initstm = Stm.toStm(stmlist);

	// NON-INTERFACE CASE: ---------------------------
	// we assert that MEM(e0+e2)==e0 by definition
	// that is, element of class display at class_depth is the class itself
	// so, the component-of check is just whether MEM(e1+e2)==e0
	final Translation.Exp case1exp = _instanceOf_class
	    (tf, source, dg,
	     // reference the claz structure of the component object
	     DECLARE(dg, HClass.Void/*componentref claz ptr*/, Tcc,//e1
		     new TEMP(tf, source, Type.POINTER, Tcc)),
	     // reference the component claz of the array claz
	     DECLARE(dg, HClass.Void/*component claz ptr*/, Tac,//e0
		     new TEMP(tf, source, Type.POINTER, Tac)),
	     // offset of the component claz in the display
	     new TEMP(tf, source, Type.INT, Tcd));

	// INTERFACE CASE: ------------------------------
	// interface type: linear search through interface list.
	// compile as:
	//    for (il=obj->claz->interfz; *il!=null; il++)
	//       if (*il==classTypeLabel) return true;
	//    return false;
	// [classTypeLabel=e0 ; obj->claz = e1]
	final Translation.Exp case2exp = _instanceOf_interface
	    (tf, source, dg,
	     // reference the claz structure of the component object
	     DECLARE(dg, HClass.Void/*cmpntref claz ptr*/, Tcc,
		     new TEMP(tf, source, Type.POINTER, Tcc)),//e1
	     // reference the component claz of the array claz
	     DECLARE(dg, HClass.Void/*component claz ptr*/, Tac,
		     new TEMP(tf, source, Type.POINTER, Tac)));
	
	// okay, put pieces together.

	return new Translation.Cx() {
	    public Stm unCxImpl(TreeFactory xxx, Label iftrue, Label iffalse) {
		List _stmlist_ = new ArrayList(5);
		// first comes init code
		_stmlist_.add(initstm);
		// then comes non-interface array case.
		_stmlist_.add(new LABEL(tf, source, Lnotinterface, false));
		_stmlist_.add(case1exp.unCx(tf, iftrue, iffalse));
		// finally comes interface array case:
		_stmlist_.add(new LABEL(tf, source, Lisinterface, false));
		_stmlist_.add(case2exp.unCx(tf, iftrue, iffalse));
		// done!
		return Stm.toStm(_stmlist_);
	    }
	};
    }

    private Translation.Exp _instanceOf_class(final TreeFactory tf,
					      final HCodeElement source,
					      final DerivationGenerator dg,
					      Exp this_claz_exp,
					      Exp checked_claz_exp,
					      Exp class_offset_exp) {
	// class type: single lookup and comparison.
	// compile as:
	//    return obj->claz->display[CONST_OFF(classType)]==classType;
	// let e0 = checked_claz_exp (claz ptr corresponding to classType)
	//     e1 = this_claz_exp    (obj->claz)
	//     e2 = class_offset_exp (CONST_OFF(classType))
	// we assert that MEM(e0+e2+k)==e0 by definition
	// that is, element of class display at class_depth is the class itself
	//         claz->display[CONST_OFF(claz)]==claz
	// so, the component-of check is just whether MEM(e1+e2+k)==e0
	return new Translation.Ex
	   (new BINOP
	    (tf, source, Type.POINTER, Bop.CMPEQ,
	     DECLARE(dg, HClass.Void/*claz ptr in display*/,
	     new MEM
	     (tf, source, Type.POINTER,
	      new BINOP
	      (tf, source, Type.POINTER, Bop.ADD,
	       new BINOP
	       (tf, source, Type.INT, Bop.ADD,
		new CONST(tf, source, CLAZ_DISPLAY_OFF),
		class_offset_exp),//e2
	       this_claz_exp))),//e1
	     checked_claz_exp));//e0
    }
    private Translation.Exp _instanceOf_interface(final TreeFactory tf,
						  final HCodeElement source,
						  final DerivationGenerator dg,
						  final Exp this_claz_exp,
						  final Exp checked_claz_exp) {
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
		List _stmlist_ = new ArrayList(8);
		// initialize Til.
		_stmlist_.add
		(new MOVE
		 (tf, source,
		  DECLARE(dg, HClass.Void/*interface list ptr*/,
		  new TEMP(tf, source, Type.POINTER, Til)),
		  // dereference claz structure for interface list ptr
		  DECLARE(dg, HClass.Void/*interface list ptr*/,
		  new MEM(tf, source, Type.POINTER,
			  new BINOP // offset to get interface pointer
			  (tf, source, Type.POINTER, Bop.ADD,
			   this_claz_exp,
			   new CONST(tf, source, CLAZ_INTERFZ_OFF))))));
		// jmp Ltst
		_stmlist_.add(new JUMP(tf, source, Ltst));
		// loop body: test *il against Lclaz.
		_stmlist_.add(new LABEL(tf, source, Ltop, false));
		_stmlist_.add
		(new CJUMP
		 (tf, source,
		  new BINOP
		  (tf, source, Type.POINTER, Bop.CMPEQ,
		   DECLARE(dg, HClass.Void/*claz ptr for interface*/,
		   new MEM(tf, source, Type.POINTER,
			   DECLARE(dg, HClass.Void/*intrfce lst ptr*/,
			   new TEMP(tf, source, Type.POINTER, Til)))),
		   checked_claz_exp),
		  iftrue, Ladv));
		// advance il
		_stmlist_.add(new LABEL(tf, source, Ladv, false));
		_stmlist_.add
		(new MOVE
		 (tf, source,
		  DECLARE(dg, HClass.Void/*intrfce lst ptr*/, Til,
		  new TEMP(tf, source, Type.POINTER, Til)),
		  new BINOP
		  (tf, source, Type.POINTER, Bop.ADD,
		   DECLARE(dg, HClass.Void/*intrfce lst ptr*/, Til,
		   new TEMP(tf, source, Type.POINTER, Til)),
		   new CONST(tf, source, POINTER_SIZE))));
		// loop guard: test *il against null.
		_stmlist_.add(new LABEL(tf, source, Ltst, false));
		_stmlist_.add
		(new CJUMP
		 (tf, source,
		  new BINOP
		  (tf, source, Type.POINTER, Bop.CMPEQ,
		   DECLARE(dg, HClass.Void/*claz ptr, maybe null*/,
		   new MEM(tf, source, Type.POINTER,
			   DECLARE(dg, HClass.Void/*in lst ptr*/, Til,
			   new TEMP(tf, source, Type.POINTER, Til)))),
		   new CONST(tf, source) /*null constant*/),
		  iffalse, Ltop));

		// okay, tie them together with SEQs.
		return Stm.toStm(_stmlist_);
	    }
	};
    }

    public Translation.Exp instanceOf(final TreeFactory tf,
				      final HCodeElement source,
				      final DerivationGenerator dg,
				      final Translation.Exp objref,
				      final HClass classType)
    {
	// the claz structure of objref
	Exp this_claz_exp = _claz_(tf, source, dg, objref);
	// the claz structure of classType
	Label Lclaz = runtime.getNameMap().label(classType);
	Exp checked_claz_exp = 
	    DECLARE(dg, HClass.Void/*hardwired claz ptr*/,
		    new NAME(tf, source, Lclaz)); // claz pointer
	// two cases: class or interface type.
	if (HClassUtil.baseClass(classType).isInterface()) {
	    // interface type: linear search through interface list.
	    return _instanceOf_interface
		(tf, source, dg, this_claz_exp, checked_claz_exp);
	} else {
	    // class type: single lookup and comparison.
	    // compile as:
	    //    return obj->claz->display[CONST_OFF(classType)]==classType;

	    // constant offset for classType in display:
	    int class_offset = cdm.classDepth(classType) * POINTER_SIZE;
	    Exp class_offset_exp = new CONST(tf, source, class_offset);

	    return _instanceOf_class
		(tf, source, dg,
		 this_claz_exp, checked_claz_exp, class_offset_exp);
	}
    }

    // XXX in single-threaded mode, this can be a NOP.
    public Translation.Exp monitorEnter(TreeFactory tf, HCodeElement source,
					DerivationGenerator dg,
					Translation.Exp objectref) {
	if (noSync) return objectref; // may have side-effects.
	// call FNI_MonitorEnter()
	return new Translation.Nx(_call_FNI_Monitor(tf, source, dg, objectref,
						    true));
    }
    // XXX in single-threaded mode, this can be a NOP.
    public Translation.Exp monitorExit(TreeFactory tf, HCodeElement source,
				       DerivationGenerator dg,
				       Translation.Exp objectref) {
	if (noSync) return objectref; // may have side-effects.
	// call FNI_MonitorExit()
	return new Translation.Nx(_call_FNI_Monitor(tf, source, dg, objectref,
						    false));
    }
    /** wrap objectref and then call FNI_Monitor{Enter|Exit}() */
    protected Stm _call_FNI_Monitor(TreeFactory tf, HCodeElement source,
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
		      (runtime.getNameMap().c_function_name("FNI_GetJNIEnv")))),
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
		       (runtime.getNameMap().c_function_name("FNI_NewLocalRef")))),
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
		      (runtime.getNameMap().c_function_name
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
	      new NAME(tf, source, new Label(runtime.getNameMap().c_function_name
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
	assert !classType.isArray();
	assert !classType.isPrimitive();
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
			    (runtime.getNameMap().c_function_name("memset")))),
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
			      runtime.getNameMap().label(classData, "classobj"));
	//let this NAME be HClass.Void, since it points at a static object
	//which the gc doesn't need to know about.  If we give it a type,
	//then the derivation generator will get confused by derived pointers
	//to it, since it doesn't live in a canonical temp.
	/*DECLARE(dg, linker.forName("java.lang.Class"), clsref);*/
	return new Translation.Ex(clsref);
    }
    public Translation.Exp fieldConst(TreeFactory tf, HCodeElement source,
			  DerivationGenerator dg, HField fieldData) {
	Exp fldref = new NAME(tf, source,
			      runtime.getNameMap().label(fieldData, "obj"));
	//let this NAME be HClass.Void, since it points at a static object
	//which the gc doesn't need to know about.  If we give it a type,
	//then the derivation generator will get confused by derived pointers
	//to it, since it doesn't live in a canonical temp.
	/*DECLARE(dg, linker.forName("java.lang.reflect.Field"), fldref);*/
	return new Translation.Ex(fldref);
    }
    public Translation.Exp methodConst(TreeFactory tf, HCodeElement source,
			   DerivationGenerator dg, HMethod methodData) {
	Exp mthref = new NAME(tf, source,
			      runtime.getNameMap().label(methodData, "obj"));
	//let this NAME be HClass.Void, since it points at a static object
	//which the gc doesn't need to know about.  If we give it a type,
	//then the derivation generator will get confused by derived pointers
	//to it, since it doesn't live in a canonical temp.
	/*DECLARE(dg, linker.forName("java.lang.reflect.Method"), mthref);*/
	return new Translation.Ex(mthref);
    }

    public Translation.Exp stringConst(TreeFactory tf, HCodeElement source,
				       DerivationGenerator dg,
				       String stringData) {
	stringSet.add(stringData);
	Exp strref = new NAME(tf, source, runtime.getNameMap().label(stringData));
	//let this NAME be HClass.Void, since it points at a static object
	//which the gc doesn't need to know about.  If we give it a type,
	//then the derivation generator will get confused by derived pointers
	//to it, since it doesn't live in a canonical temp.
	/*DECLARE(dg, linker.forName("java.lang.String"), strref);*/
	return new Translation.Ex(strref);
    }

    public Translation.Exp arrayBase(TreeFactory tf, HCodeElement source,
				     DerivationGenerator dg,
				     Translation.Exp objectref) {
	return new Translation.Ex
	    (new BINOP
	     (tf, source, Type.POINTER, Bop.ADD,
	      PTRMASK(tf, source, dg, objectref.unEx(tf)),
	      new CONST(tf, source, OBJ_AZERO_OFF)));
    }
    public Translation.Exp arrayOffset(TreeFactory tf, HCodeElement source,
				       DerivationGenerator dg,
				       HClass arrayType, Translation.Exp index)
    {
	assert arrayType.isArray();
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
	      PTRMASK(tf, source, dg, objectref.unEx(tf)),
	      new CONST(tf, source, OBJ_FZERO_OFF)));
    }
    public Translation.Exp fieldOffset(TreeFactory tf, HCodeElement source,
					DerivationGenerator dg,
				       HField field) {
	assert !field.isStatic();
	return new Translation.Ex
	    (new CONST(tf, source, cfm.fieldOffset(field)));
    }
    /** Returns a pointer to the claz structure associated with the
     *  given objectref. */
    protected Exp _claz_(TreeFactory tf, HCodeElement source,
		       DerivationGenerator dg,
		       Translation.Exp objectref) {
	return DECLARE(dg, HClass.Void/*claz pointer*/,
	      new MEM
	      (tf, source, Type.POINTER,
	       new BINOP
	       (tf, source, Type.POINTER, Bop.ADD,
		PTRMASK(tf, source, dg, objectref.unEx(tf)),
		new CONST(tf, source, OBJ_CLAZ_OFF))));
    }
    public Translation.Exp methodBase(TreeFactory tf, HCodeElement source,
					DerivationGenerator dg,
				      Translation.Exp objectref) {
	return new Translation.Ex
	    (new BINOP
	     (tf, source, Type.POINTER, Bop.ADD,
	      _claz_(tf, source, dg, objectref),
	      new CONST(tf, source, CLAZ_METHODS_OFF)));
    }
    /* some methods are both defined in interfaces *and* inherited from
     * java.lang.Object.  Use the java.lang.Object version. */
    private HMethod remap(HMethod hm) {
	try {
	    return linker.forName("java.lang.Object")
		.getMethod(hm.getName(), hm.getDescriptor());
	} catch (NoSuchMethodError nsme) {
	    return hm;
	}
    }
    public Translation.Exp methodOffset(TreeFactory tf, HCodeElement source,
					DerivationGenerator dg,
					HMethod method) {
	assert !method.isStatic();
	method = remap(method);//handle interface methods inherited from Object
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
    public Translation.Exp referenceEqual(TreeFactory tf, HCodeElement source,
					  DerivationGenerator dg,
					  Translation.Exp refLeft,
					  Translation.Exp refRight) {
	// have to do pointer masking before we can compare these references.
	return new Translation.Ex
	    (new BINOP(tf, source, Type.POINTER, Bop.CMPEQ,
		       PTRMASK(tf, source, dg, refLeft.unEx(tf)),
		       PTRMASK(tf, source, dg, refRight.unEx(tf))));
    }
}
