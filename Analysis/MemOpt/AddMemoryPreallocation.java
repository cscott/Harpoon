// AddMemoryPreallocation.java, created Wed Nov 27 18:44:55 2002 by salcianu
// Copyright (C) 2000 Alexandru Salcianu <salcianu@MIT.EDU>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.MemOpt;

import java.util.Map;
import java.util.Iterator;
import java.util.Collection;

import java.io.PrintWriter;

import harpoon.Analysis.Transformation.MethodMutator;

import harpoon.ClassFile.HCodeFactory;
import harpoon.ClassFile.HCodeElement;
import harpoon.ClassFile.HField;
import harpoon.ClassFile.HMethod;
import harpoon.ClassFile.Linker;
import harpoon.ClassFile.HCode;
import harpoon.ClassFile.HClass;

import harpoon.IR.Tree.CanonicalTreeCode;
import harpoon.Backend.Generic.Frame;
import harpoon.Backend.Generic.Runtime;

import harpoon.IR.Tree.Stm;
import harpoon.IR.Tree.SEQ;
import harpoon.IR.Tree.NAME;
import harpoon.IR.Tree.ExpList;
import harpoon.IR.Tree.CONST;
import harpoon.IR.Tree.NATIVECALL;
import harpoon.IR.Tree.TEMP;
import harpoon.IR.Tree.MOVE;
import harpoon.IR.Tree.MEM;
import harpoon.IR.Tree.BINOP;
import harpoon.IR.Tree.CONST;
import harpoon.IR.Tree.TreeFactory;
import harpoon.IR.Tree.Type;
import harpoon.IR.Tree.Tree;
import harpoon.IR.Tree.DerivationGenerator;
import harpoon.IR.Tree.Code;
import harpoon.IR.Tree.Exp;
import harpoon.IR.Tree.Bop;

import harpoon.Temp.Temp;
import harpoon.Temp.Label;

/**
 * <code>AddMemoryPreallocation</code> is a code factory that provides
 * the code for the static method that allocates the pre-allocated
 * chunks of memory that are used by the unitary sites.
 * 
 * @author  Alexandru Salcianu <salcianu@MIT.EDU>
 * @version $Id: AddMemoryPreallocation.java,v 1.7 2003-02-14 17:17:08 salcianu Exp $ */
class AddMemoryPreallocation implements HCodeFactory {

    /** Creates a <code>AddMemoryPreallocation</code> code factory: it
        behaves like <code>parent_hcf</code> for all methods, except
        for the special memory preallocation methdo
        <code>init_method</code>.  For this method, it generates code
        to (pre)allocate some memory chunks and store references to
        them in the static fields that are the keys of
        <code>field2size</code>.

	@param parent_hcf parent code factory; this factory provides
	the code for all methods, except <code>init_method</code>

	@param init_method handle of the method that does all pre-allocation

	@param field2size maps a field to the size of the preallocated
	chunk of memory that it shold point to at runtime

	@param frame frame containing all the backend details */
    public AddMemoryPreallocation
	(HCodeFactory parent_hcf, HMethod init_method, 
	 Map/*<HField,Integer>*/ field2size, Frame frame) {
	assert parent_hcf.getCodeName().equals(CanonicalTreeCode.codename) : 
	    "AddMemoryPreallocation only works on CanonicalTree form";
	this.parent_hcf    = parent_hcf;
	this.field2size    = field2size;
	this.runtime       = frame.getRuntime();
	this.init_method   = init_method;
    }

    public String getCodeName() { return parent_hcf.getCodeName(); }
    public void clear(HMethod m) { parent_hcf.clear(m); }


    private final HCodeFactory parent_hcf;
    private final Map/*<HField,Integer>*/ field2size;
    private final Runtime runtime;
    private final HMethod init_method;


    public HCode convert(HMethod m) {
	// we don't change any method, except ...
	if(!m.equals(init_method))
	    return parent_hcf.convert(m);

	// ... the method that preallocates memory
	Code code = (Code) parent_hcf.convert(m);
	DerivationGenerator dg = 
	    (DerivationGenerator) code.getTreeDerivation();

	// At the beginning of that method, we add code that
	// 1. allocates one chunk of memory
	// 2. make the static fields to point inside it (by using a little
	// pointer arithmetic - this is OK in Tree form).
	// Doing n additions is faster than n-1 malloc calls.
	SEQ start = (SEQ) ((SEQ) code.getRootElement()).getRight();
	Temp tmem = new Temp(start.getFactory().tempFactory(), "tmem");

	// As insertCode adds stuff immediately after start, we insert
	// code in reverse order: first, item 2 from the list above
	int offset = 0;
	for(Iterator it = field2size.keySet().iterator(); it.hasNext(); ) {
	    HField hfield = (HField) it.next();
	    int size = ((Integer) field2size.get(hfield)).intValue();
	    insertCode(start,
		       getFieldAssignment(hfield, tmem, offset, start, dg));
	    offset += size;
	}
	// offset is now equal to total length of preallocated memory
	// Now, code for item 1 from the list above
	insertCode(start, getAllocCall(tmem, offset, start, dg));

	System.out.println("After  modifications:");
	code.print(new PrintWriter(System.out));

	return code;
    }


    // produces a native call that allocates "length" bytes of memory;
    // the returned value (the pointed to the newly allocated piece of
    // memory) is stored in "tmem"
    private Stm getAllocCall(Temp tmem, int length,
			     Tree start, DerivationGenerator dg) {
	TreeFactory tf = start.getFactory();
	
	return
	    new NATIVECALL
	    (tf, start, 
	     (TEMP)
	     DECLARE(dg, HClass.Void,
		     new TEMP(tf, start, Type.POINTER, tmem)),
	     new NAME(tf, start, new Label("GC_malloc")),
	     new ExpList(new CONST(tf, start, length),
			 null));
    }
    
    // generate code that initializes the field hfield with a pointer
    // that is computed by adding offset to tmem.
    private Stm getFieldAssignment(HField hfield, Temp tmem, int offset,
				   Tree start, DerivationGenerator dg) {
	// TODO: we should be able to get the DerivationGenerator
	// (a method-wide thing) from "start".
	TreeFactory tf = start.getFactory();

	// "field = tmem + offset;"
	return
	    new MOVE
	    (tf, start,
	     DECLARE(dg, HClass.Void, 
		     new MEM
		     (tf, start, Type.POINTER,
		      new NAME(tf, start, runtime.getNameMap().label(hfield)))),
	     new BINOP(tf, start, Type.POINTER, Bop.ADD,
		       (TEMP)
		       DECLARE(dg, HClass.Void,
			       new TEMP(tf, start, Type.POINTER, tmem)),
		       new CONST(tf, start, offset)));
    }


    // insert a statement right after "start"
    private SEQ insertCode(SEQ start, Stm code) {
	Stm former_right = start.getRight();
	former_right.unlink();
	start.setRight(new SEQ(code, former_right));
	return start;
    }


    // type declaration helper methods
    protected static Exp DECLARE(DerivationGenerator dg, HClass hc, Exp exp) {
	dg.putType(exp, hc);
	return exp;
    }
}
