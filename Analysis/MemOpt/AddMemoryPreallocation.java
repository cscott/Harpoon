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
import harpoon.IR.Tree.TreeFactory;
import harpoon.IR.Tree.Type;
import harpoon.IR.Tree.Tree;
import harpoon.IR.Tree.DerivationGenerator;
import harpoon.IR.Tree.Code;
import harpoon.IR.Tree.Exp;

import harpoon.Temp.Temp;
import harpoon.Temp.Label;

/**
 * <code>AddMemoryPreallocation</code> is a code factory that provides
 * the code for the static method that allocates the pre-allocated
 * chunks of memory that are used by the unitary sites.
 * 
 * @author  Alexandru Salcianu <salcianu@MIT.EDU>
 * @version $Id: AddMemoryPreallocation.java,v 1.4 2003-01-07 15:05:02 salcianu Exp $ */
class AddMemoryPreallocation implements HCodeFactory {

    /** Creates a <code>AddMemoryPreallocation</code> code factory: it
        behaves like <code>parent_hcf</code> for all methods, except
        for the special memory preallocation methdo
        <code>init_method</code>.  For this method, it generates code
        to (pre)allocate some memory chunks and store references to
        them in the static fields that are the keys of
        <code>field2classes</code>.

	@param parent_hcf parent code factory; this factory provides
	the code for all methods, except <code>init_method</code>
	@param init_method handle of the method that does all pre-allocation
	@param field2classes maps a field to the set of unitary
	allocation sites that reuse the preallocated memory chunk
	pointed to by that field.
	@param frame frame containing all the backend details*/
    public AddMemoryPreallocation
	(HCodeFactory parent_hcf, HMethod init_method, 
	 Map field2classes, Frame frame) {
	assert parent_hcf.getCodeName().equals(CanonicalTreeCode.codename) : 
	    "AddMemoryPreallocation only works on CanonicalTree form";
	this.parent_hcf    = parent_hcf;
	this.field2classes = field2classes;
	this.runtime       = frame.getRuntime();
	this.init_method   = init_method;
    }

    private final HCodeFactory parent_hcf;
    private final Map field2classes;
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

	/*
	System.out.println("Before modifications:");
	code.print(new PrintWriter(System.out));
	*/

	SEQ start = (SEQ) ((SEQ) code.getRootElement()).getRight();
	for(Iterator it = field2classes.keySet().iterator(); it.hasNext(); ) {
	    HField hfield = (HField) it.next();
	    Collection classes = (Collection) field2classes.get(hfield);
	    generate_code(start, hfield, sizeForClasses(runtime, classes), dg);
	}

	System.out.println("After  modifications:");
	code.print(new PrintWriter(System.out));

	return code;
    }

    public String getCodeName() { return parent_hcf.getCodeName(); }
    public void clear(HMethod m) { parent_hcf.clear(m); }


    // generate code (linked immediately after start) that initializes
    // the field hfield with a pointer that points to a preallocated
    // chunk of memory of size "size".
    private void generate_code
	(SEQ start, HField hfield, int size, DerivationGenerator dg) {
	// TODO: we should be able to get the DerivationGenerator
	// (a method-wide thing) from "start".

	TreeFactory tf = start.getFactory();
	Temp tmem = new Temp(tf.tempFactory(), "tmem");

	// 1. generate "tmem = GC_malloc_atomic(size);"
	NATIVECALL call_malloc =
	    new NATIVECALL
	    (tf, start, 
	     (TEMP)
	     DECLARE(dg, HClass.Void,
		     new TEMP(tf, start, Type.POINTER, tmem)),
	     new NAME(tf, start, new Label("GC_malloc")),
	     new ExpList
	     (new CONST(tf, start, size),
	      null));

	// 2. generate "field = tmem;"
	MOVE set_field = 
	    new MOVE
	    (tf, start,
	     DECLARE(dg, HClass.Void, 
	      new MEM
	      (tf, start, Type.POINTER,
	       new NAME(tf, start, runtime.getNameMap().label(hfield)))),
	     (TEMP)
	     DECLARE(dg, HClass.Void,
		     new TEMP(tf, start, Type.POINTER, tmem)));

	// 3. insert the generated code code right after start
	Stm former_right = start.getRight();
	former_right.unlink();
	start.setRight
	    (new SEQ(tf, start, call_malloc,
		     new SEQ(tf, start, set_field, former_right)));
    }


    // type declaration helper methods
    protected static Exp DECLARE(DerivationGenerator dg, HClass hc, Exp exp) {
	dg.putType(exp, hc);
	return exp;
    }


    // compute the size of the memory chunk that can hold an object of
    // any of the classes from the collection passed as an argument
    static int sizeForClasses(Runtime runtime, Collection classes) {
	int max = -1;
	for(Iterator it = classes.iterator(); it.hasNext(); ) {
	    int size = sizeForClass(runtime, (HClass) it.next());
	    if(size > max) max = size;
	}
	return max;
    }

    // compute the total size occupied by an object of class hclass
    static int sizeForClass(Runtime runtime, HClass hclass) {
	Runtime.TreeBuilder tree_builder = runtime.getTreeBuilder();
	int size =
	    tree_builder.objectSize(hclass) +
	    tree_builder.headerSize(hclass);
	// we allocate only multiples of 4 bytes
	while((size % 4) != 0)
	    size++;
	return size;
    }

    // debug only  // TODO: remove
    private void print(HCode hcode) {
	System.out.println("Instructions in " + hcode.getMethod());

	int k = 0;
	for(Iterator it = hcode.getElementsL().iterator(); it.hasNext(); ) {
	    HCodeElement hce = (HCodeElement) it.next();
	    System.out.println(">: " + hce.getID() + " " + hce);
	    k++;
	}
    }
}
