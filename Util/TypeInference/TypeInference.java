// TypeInference.java, created Sun Apr  2 14:42:04 2000 by salcianu
// Copyright (C) 2000 Alexandru SALCIANU <salcianu@retezat.lcs.mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Util.TypeInference;

import java.util.Iterator;
import java.util.Set;
import java.util.HashSet;

import java.lang.reflect.Modifier;

import harpoon.ClassFile.HCode;
import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HCodeElement;
import harpoon.ClassFile.HMethod;
import harpoon.Analysis.PointerAnalysis.PAWorkList;
import harpoon.Util.DataStructs.Relation;
import harpoon.Util.DataStructs.LightRelation;

import harpoon.Temp.Temp;
import harpoon.Util.Util;

import harpoon.Analysis.ReachingDefs;
import harpoon.Analysis.ReachingDefsImpl;

import harpoon.IR.Quads.Quad;
import harpoon.IR.Quads.QuadVisitor;
import harpoon.IR.Quads.HEADER;
import harpoon.IR.Quads.METHOD;
import harpoon.IR.Quads.CALL;
import harpoon.IR.Quads.COMPONENTOF;
import harpoon.IR.Quads.MOVE;
import harpoon.IR.Quads.INSTANCEOF;
import harpoon.IR.Quads.OPER;
import harpoon.IR.Quads.GET;
import harpoon.IR.Quads.AGET;
import harpoon.IR.Quads.NEW;
import harpoon.IR.Quads.ANEW;
import harpoon.IR.Quads.ALENGTH;
import harpoon.IR.Quads.TYPECAST;
import harpoon.IR.Quads.CONST;

/**
 * <code>TypeInference</code> is a very simple type inference module.
 It was written to satisfy some immediate needs in the Pointer Analysis
 (mainly determining is a temp could point to an array of non-primitive
 objects (ie not <code>int[]</code>). However, it is very general and can be
 used to compute the types of some arbitrary set of <code>ExactTemp</code>s
 (not necessarily all the <code>Temp</code>s from the body of a method).<br>
 Works for <code>quad-no-ssa</code> only (anyway, it is trivial to write the
 extensions for the other quads).
 * 
 * @author  Alexandru SALCIANU <salcianu@retezat.lcs.mit.edu>
 * @version $Id: TypeInference.java,v 1.4 2002-04-10 03:07:35 cananian Exp $
 */
public class TypeInference implements java.io.Serializable {
    // switch on the debug messages
    public static boolean DEBUG = false;

    /** Creates a <code>TypeInference</code>. This object is
	supposed to provide type information about the <code>ExactTemp</code>s
	from the set <code>ietemps</code>. All these exact temps are
	supposed to be defined in <code>hcode</code>. */
    public TypeInference(HMethod hm, HCode hcode, Set ietemps) {
        analyze(hm, hcode, ietemps);
    }

    // the private repositoy of the type information: it is filled in
    // by analyze and read by the query methods.
    private Relation types = new LightRelation();

    ///////////////// QUERY METHODS /////////////////////////////

    /** Returns the possible types for the temp <code>et.t</code> in the
	quad <code>et.q</code>. This method should be called only for
	<code>ExactTemp</code> that were in the <code>ietemps</code> set
	passed to the constructor of <code>this</code> object. */
    public Set getType(ExactTemp et){
	return types.getValues(et);
    }

    /** Checks whether the temp <code>et.t</code> in instruction
	<code>et.q</code> points to an array of non-primitive type
	components (ie non an <code>int[]</code>. This is useful in the
	PointerAnalysis stuff to see if an <code>AGET</code> deserves
	to be introduced (and possibly a new <code>LOAD</code> node). */
    public boolean isArrayOfNonPrimitives(ExactTemp et){
	Set set = getType(et);

	for(Iterator it = getType(et).iterator(); it.hasNext(); ){
	    HClass hclass = (HClass) it.next();
	    HClass comp  = hclass.getComponentType();
	    if(comp == null) continue;
	    if(DEBUG)
		System.out.println("Type of component of " + et + ":" +
				   comp);
	    if(!comp.isPrimitive())
		return true;
	}

	return false;
    }
    ///////////////// QUERY METHODS END //////////////////////////
    
    ReachingDefs rdef = null;

    // W is the worklist used for type inference. At any moment, it contains
    // ExactTemps whose possible types may have changed.
    PAWorkList W = null; 
    // The dependency relation: et1 -> et2 if the type of et1 influences
    // the type of et2. Generic type: Relation<ExactTemp, ExactTemp>
    Relation dependencies = null;

    private void analyze(HMethod hm, HCode hcode, Set ietemps){
	W    = new PAWorkList(); 
	rdef = new ReachingDefsImpl(hcode);
	dependencies  = new LightRelation(); 

	fill_in_dep_and_W(ietemps);
	
	if(DEBUG){
	    System.out.println("DEPENDENCIES:");
	    System.out.println(dependencies);
	}

	HEADER header = (HEADER) hcode.getRootElement();
	METHOD method = (METHOD) header.next(1);
	set_parameter_types(hm, method);
	
	if(DEBUG){
	    System.out.println("PARAMETER TYPES:");
	    System.out.println(types);
	}

	compute_types();

	if(DEBUG){
	    System.out.println("COMPUTED TYPES:");
	    System.out.println(types);
	}

	compute_interesting_types(ietemps);

	if(DEBUG){
	    System.out.println("THE FINAL TYPES OF THE INTERESTING TEMPS:");
	    System.out.println(types);
	}

	// enable the GC
	W    = null;
	rdef = null;
	dependencies = null;
    }


    private void fill_in_dep_and_W(Set ietemps){

	// Inner classes cannot access non-final local variables
	// we work around this through this wrapper hack: the wrapper
	// is final, but we can modify its fields!
	class Wrapper{
	    ExactTemp et;
	};
	final Wrapper wrapper = new Wrapper();

	// Used in the dependencies detection: contains the ExactTemps
	// already seen, which have been put in the worrklist (and maybe
	// even processed).
	final Set seen      = new HashSet();
	// W2 is the worklist for finding the dependencies between
	// ExactTemps. AT any moment, it contains ExactTemps whose
	// dependencies have not been explored yet.
	final PAWorkList W2 = new PAWorkList();

	for(Iterator it = ietemps.iterator(); it.hasNext(); ){
	    ExactTemp et = (ExactTemp) it.next();

	    Iterator it_rdef = rdef.reachingDefs(et.q, et.t).iterator();
	    while(it_rdef.hasNext()){
		Quad quad = (Quad) it_rdef.next();
		ExactTemp et_def = new ExactTemp(quad, et.t);
		if(seen.add(et_def))
		    W2.add(et_def);
	    }
	}

	// quad visitor for detecting the dependencies between ExactTemp types
	final QuadVisitor dep_detector = new QuadVisitor(){
		public void visit(MOVE q) {
		    put_deps(q, q.src());
		}
		
		public void visit(AGET q) {
		    put_deps(q, q.objectref());
		}

		public void visit(ALENGTH q) {
		    types.add(wrapper.et, HClass.Int);
		    W.add(wrapper.et);
		}

		public void visit(COMPONENTOF q) {
		    types.add(wrapper.et, HClass.Boolean);
		    W.add(wrapper.et);
		}

		public void visit(INSTANCEOF q) {
		    types.add(wrapper.et, HClass.Boolean);
		    W.add(wrapper.et);
		}
	    
		public void visit(CALL q){
		    types.add(wrapper.et, q.method().getReturnType());
		    W.add(wrapper.et);
		}

		public void visit(NEW q){
		    types.add(wrapper.et, q.hclass());
		    W.add(wrapper.et);
		}
		
		public void visit(OPER q) {
		    types.add(wrapper.et, q.evalType());
		    W.add(wrapper.et);
		}

		public void visit(ANEW q){
		    types.add(wrapper.et, q.hclass());
		    W.add(wrapper.et);
		}
		
		public void visit(TYPECAST q){
		    types.add(wrapper.et, q.hclass());
		    W.add(wrapper.et);
		}
	    
		public void visit(GET q){
		    types.add(wrapper.et, q.field().getType());
		    W.add(wrapper.et);
		}
		
		public void visit(METHOD q){
		    // do nothing; the parameters (defined in METHOD)
		    // are treated in set_parameter_types
		}
		
		public void visit(CONST q){
		    types.add(wrapper.et, q.type());
		    W.add(wrapper.et);
		}
		
		public void visit(Quad q){
		    assert false : "Untreated quad " + q;
		}

		public void put_deps(Quad q, Temp t){
		    for(Iterator itq = rdef.reachingDefs(q,t).iterator();
			itq.hasNext();){
			Quad qdef = (Quad) itq.next();
			ExactTemp et_def = new ExactTemp(qdef,t);
			dependencies.add(et_def, wrapper.et);
			if(seen.add(et_def))
			    W2.add(et_def);
		    }
		}
		
	    };

	// Worklist algorithm for detecting the depenmdencies between
	// ExactTemp types.
	while(!W2.isEmpty()){
	    ExactTemp et = (ExactTemp) W2.remove();
	    // Invariant: et.t is defined by et.q
	    wrapper.et = et;
	    et.q.accept(dep_detector);
	}

    }

    // set the types for the arguments of the method
    private void set_parameter_types(HMethod hm, METHOD method){
	// all parameter types
	HClass[] aptypes = null;
	// declared parameter types (no "this" parameter here)
	HClass[] dptypes = hm.getParameterTypes();

	if(Modifier.isStatic(hm.getModifiers()))
	    aptypes = dptypes;
	else{
	    // non-static methods have a hidden parameter: "this"
	    aptypes = new HClass[dptypes.length + 1];
	    aptypes[0] = hm.getDeclaringClass();
	    for(int i = 0; i < dptypes.length; i++)
		aptypes[i+1] = dptypes[i];
	}

	Temp[] params = method.params();
	assert aptypes.length == params.length : " set_parameter_types is broken";

	for(int i = 0; i < params.length; i++){
	    ExactTemp et = new ExactTemp(method, params[i]);
	    types.add(et, aptypes[i]);
	    W.add(et);
	}
    }

    // do worklist algorithm to propagate our type information. By this
    // time, W should contain all teh ExactTemps whose types are trivial to
    // determine: the parameters, destination of NEW, ANEW, TYPECAST, GET etc.
    private void compute_types(){
	// Inner classes cannot access non-final local variables
	// we work around this through this wrapper hack: the wrapper
	// is final, but we can modify its fields!
	class Wrapper2{
	    ExactTemp et1;
	    ExactTemp et2;
	};
	final Wrapper2 wrapper2 = new Wrapper2();

	// quad visitor for propagating the type information:
	// MOVE and AGET are the only ones that remained, for all the
	// other interesting quads it is trivial to determine the types
	// of their destination Temps.
	QuadVisitor type_calculator = new QuadVisitor(){

		public void visit(MOVE q){
		    boolean modified = false;

		    for(Iterator it = types.getValues(wrapper2.et1).iterator();
			it.hasNext(); ){
			HClass hclass = (HClass) it.next();
			if(types.add(wrapper2.et2, hclass))
			    modified = true;
		    }

		    if(modified) W.add(wrapper2.et2);
		}
		
		public void visit(AGET q){
		    boolean modified = false;
		    
		    for(Iterator it = types.getValues(wrapper2.et1).iterator();
			it.hasNext();){
			HClass hclass = (HClass) it.next();
			HClass hcomp = hclass.getComponentType();
			if(hcomp != null)
			    if(types.add(wrapper2.et2, hcomp))
				modified = true;
		    }

		    if(modified) W.add(wrapper2.et2);
		}

		public void visit(Quad q){
		    assert false : "untreated quad!";
		}
	    };

	while(!W.isEmpty()){
	    ExactTemp et1 = (ExactTemp) W.remove();
	    wrapper2.et1  = et1;
	    Iterator it = dependencies.getValues(et1).iterator();
	    while(it.hasNext()) {
		ExactTemp et2 = (ExactTemp) it.next();
		wrapper2.et2   = et2;
		et2.q.accept(type_calculator);
	    }
	}
    }

    // Computes the types of the exact temps from the set ietemps.
    private void compute_interesting_types(Set ietemps){
	Relation types2 = new LightRelation();

	for(Iterator it = ietemps.iterator(); it.hasNext(); ){
	    ExactTemp et = (ExactTemp) it.next();

	    for(Iterator it_rdef = rdef.reachingDefs(et.q, et.t).iterator();
		it_rdef.hasNext(); ){
		Quad q = (Quad) it_rdef.next();
		types2.addAll(et, types.getValues(new ExactTemp(q, et.t)));
	    }
	}

	types = types2;
    }

}
