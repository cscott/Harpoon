// TypeInference.java, created Sun Apr  2 14:42:04 2000 by salcianu
// Copyright (C) 2000 Alexandru SALCIANU <salcianu@MIT.EDU>
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
import harpoon.Analysis.PointerAnalysis.Relation;

import harpoon.Temp.Temp;
import harpoon.Util.Util;

import harpoon.Analysis.ReachingDefs;
import harpoon.Analysis.ReachingDefsImpl;

import harpoon.IR.Quads.Quad;
import harpoon.IR.Quads.QuadVisitor;
import harpoon.IR.Quads.HEADER;
import harpoon.IR.Quads.METHOD;
import harpoon.IR.Quads.CALL;
import harpoon.IR.Quads.MOVE;
import harpoon.IR.Quads.GET;
import harpoon.IR.Quads.AGET;
import harpoon.IR.Quads.NEW;
import harpoon.IR.Quads.ANEW;
import harpoon.IR.Quads.TYPECAST;
import harpoon.IR.Quads.CONST;

/**
 * <code>TypeInference</code>
 * 
 * @author  Alexandru SALCIANU <salcianu@MIT.EDU>
 * @version $Id: TypeInference.java,v 1.1.2.1 2000-04-03 02:29:15 salcianu Exp $
 */
public class TypeInference {

    /** Creates a <code>TypeInference</code>. This object is
	supposed to provide type information about the exact temps
	from the set <code>ivars</code>. All this exact temps are
	supposed to be defined in <code>hcode</code>*/
    public TypeInference(HMethod hm, HCode hcode, Set ivars) {
        analyze(hm, hcode, ivars);
    }

    Relation types = new Relation();

    public Set getType(ExactTemp et){
	return types.getValuesSet(et);
    }

    ///////////////// QUERY METHODS /////////////////////////////
    public boolean isArrayOfNonPrimitives(ExactTemp et){
	Set set = getType(et);

	for(Iterator it = getType(et).iterator(); it.hasNext(); ){
	    HClass hclass = (HClass) it.next();
	    HClass compt  = hclass.getComponentType();
	    if(compt == null) continue;
	    if(!compt.isPrimitive())
		return true;
	}

	return false;
    }

    public boolean isArrayofNonPrimitives(HCodeElement hce, Temp t){
	return isArrayofNonPrimitives(hce, t);
    }
    ///////////////// QUERY METHODS END //////////////////////////
    
    ReachingDefs rdef = null;

    final PAWorkList W = new PAWorkList(); 
    Relation dependencies = null;

    private void analyze(HMethod hm, HCode hcode, Set ivars){
	rdef = new ReachingDefsImpl(hcode);
	dependencies  = new Relation(); 

	fill_in_dep_and_W(ivars);
	
	HEADER header = (HEADER) hcode.getRootElement();
	METHOD method = (METHOD) header.next(1);
	set_parameter_types(hm, method);

	compute_types();

	compute_interesting_types(ivars);

	// enable the GC
	rdef = null;
	dependencies = null;
    }


    private class Wrapper{
	ExactTemp et;
    };
    private final Wrapper wrapper = new Wrapper();

    private void fill_in_dep_and_W(Set ivars){

	final Relation dep = dependencies;
	final Set seen = new HashSet();
	final PAWorkList W2 = new PAWorkList();

	for(Iterator it = ivars.iterator(); it.hasNext(); ){
	    ExactTemp et = (ExactTemp) it.next();

	    Iterator it_rdef = rdef.reachingDefs(et.q, et.t).iterator();
	    while(it_rdef.hasNext()){
		Quad quad = (Quad) it_rdef.next();
		ExactTemp et_def = new ExactTemp(quad, et.t);
		if(seen.add(et_def))
		    W2.add(et_def);
	    }
	}

	final QuadVisitor dep_detector = new QuadVisitor(){
		public void visit(MOVE q){
		    put_deps(q, q.src());
		}
		
		public void visit(AGET q){
		    put_deps(q, q.objectref());
		}
	    
		public void visit(CALL q){
		    types.add(wrapper.et, q.method().getReturnType());
		    W.add(wrapper.et);
		}

		public void visit(NEW q){
		    types.add(wrapper.et, q.hclass());
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
		    // do nothing; these are treated somewhere else
		}
		
		public void visit(CONST q){
		    types.add(wrapper.et, q.type());
		    W.add(wrapper.et);
		}
		
		public void visit(Quad q){
		    Util.assert(false, "Untreated quad " + q);
		}

		public void put_deps(Quad q, Temp t){
		    for(Iterator itq = rdef.reachingDefs(q,t).iterator();
			itq.hasNext();){
			Quad qdef = (Quad) itq.next();
			ExactTemp et_def = new ExactTemp(qdef,t);
			dep.add(et_def, wrapper.et);
			if(seen.add(et_def))
			    W2.add(et_def);
		    }
		}
		
	    };

	while(!W2.isEmpty()){
	    ExactTemp et = (ExactTemp) W2.remove();
	    wrapper.et = et;
	    et.q.accept(dep_detector);
	}

    }

    // set the types for the arguments of the method
    private void set_parameter_types(HMethod hm, METHOD method){
	HClass[] aptypes = null;
	HClass[] paramtypes = hm.getParameterTypes();

	if(Modifier.isStatic(hm.getModifiers()))
	    aptypes = paramtypes;
	else{
	    aptypes = new HClass[hm.getParameterTypes().length + 1];
	    aptypes[0] = hm.getDeclaringClass();
	    for(int i = 0; i < paramtypes.length; i++)
		aptypes[i+1] = paramtypes[i];
	}

	Temp[] params = method.params();
	Util.assert(aptypes.length == params.length,
		    " set_parameter_types is broken");

	for(int i = 0; i < params.length; i++){
	    ExactTemp et = new ExactTemp(method, params[i]);
	    types.add(et, aptypes[i]);
	    W.add(et);
	}
    }



    private class Wrapper2{
	ExactTemp et1;
	ExactTemp et2;
    };
    private final Wrapper2 wrapper2 = new Wrapper2();

    private void compute_types(){

	QuadVisitor type_calculator = new QuadVisitor(){

		public void visit(MOVE q){
		    boolean modified = false;

		    for(Iterator it = types.getValues(wrapper2.et1);
			it.hasNext(); ){
			HClass hclass = (HClass) it.next();
			if(types.add(wrapper2.et2, hclass))
			    modified = true;
		    }

		    if(modified) W.add(wrapper2.et2);
		}
		
		public void visit(AGET q){
		    boolean modified = false;
		    
		    for(Iterator it = types.getValues(wrapper2.et1);
			it.hasNext();){
			HClass hclass = (HClass) it.next();
			HClass hcomp = hclass.getComponentType();
			if(hcomp != null)
			    if(types.add(wrapper2.et2, hclass))
				modified = true;
		    }

		    if(modified) W.add(wrapper2.et2);
		}

		public void visit(Quad q){
		    Util.assert(false, "untreated quad!");
		}
	    };

	while(!W.isEmpty()){
	    ExactTemp et1 = (ExactTemp) W.remove();
	    wrapper2.et1 = et1;
	    for(Iterator it = dependencies.getValues(et1); it.hasNext(); ){
		ExactTemp et2 = (ExactTemp) it.next();
		et2.q.accept(type_calculator);
	    }
	}
    }

    // Computes the types of the exact temps from the set ivars.
    private void compute_interesting_types(Set ivars){
	Relation types2 = new Relation();

	for(Iterator it = ivars.iterator(); it.hasNext(); ){
	    ExactTemp et = (ExactTemp) it.next();

	    for(Iterator it_rdef = rdef.reachingDefs(et.q, et.t).iterator();
		it_rdef.hasNext(); ){
		Quad q = (Quad) it_rdef.next();
		types2.addAll(et, types.getValuesSet(new ExactTemp(q, et.t)));
	    }
	}

	types = types2;
    }

}

