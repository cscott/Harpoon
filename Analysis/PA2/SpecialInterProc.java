// SpecialInterProc.java, created Fri Jul  8 13:26:00 2005 by salcianu
// Copyright (C) 2003 Alexandru Salcianu <salcianu@alum.mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.PA2;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.LinkedList;
import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;

import jpaul.Constraints.Constraint;
import jpaul.Constraints.LtConstraint;
import jpaul.Constraints.CtConstraint;

import harpoon.IR.Quads.CALL;

import harpoon.ClassFile.HMethod;
import harpoon.ClassFile.HClass;
import harpoon.ClassFile.Linker;
import harpoon.ClassFile.HField;

/**
 * <code>SpecialInterProc</code>
 * 
 * @author  Alexandru Salcianu <salcianu@alum.mit.edu>
 * @version $Id: SpecialInterProc.java,v 1.4 2005-09-05 21:30:58 salcianu Exp $
 */
public class SpecialInterProc {

    // IDEA: parse these method names from a file
    //
    // Harmless native methods.
    //
    // These methods ado not create any externally visible aliasing to
    // the objects reachable from their parameters (e.g., do not store
    // any parameter in a static field, do not return objects
    // reachable from params etc.)  Therefore, the analysis can ignore
    // the effects of these methods on their params (instead of
    // marking params as escaped).  If these methods return objects,
    // then they are newly allocated ones: we can introduce inside
    // nodes for them.
    // 
    // NOTE: these methods may still mutate a non-object field of
    // their params.  The mutation analysis should model this.
    private static String[][] hna = new String[][] {
	{"java.lang.Object", "hashCode", "()I"},
	{"java.lang.Object", "equals",   "(Ljava/lang/Object;)Z"},

	{"java.lang.Double", "doubleToLongBits", "(D)J"},
	{"java.lang.Double", "longBitsToDouble", "(J)D"},

	{"java.lang.Float",  "floatToIntBits",   "(F)I"},

	{"java.lang.Math",   "floor",            "(D)D"},

	{"java.lang.Class",  "isArray",          "()Z"},
	{"java.lang.Class",  "isInterface",      "()Z"},
	{"java.lang.Class",  "isPrimitive",      "()Z"},
	{"java.lang.Class",  "getName",          "()Ljava/lang/String;"},

	{"java.lang.reflect.Constructor", "getModifiers",  "()I"},

	{"java.lang.reflect.Field",       "getModifiers",  "()I"},
	{"java.lang.reflect.Field",       "getName",       "()Ljava/lang/String;"},
	
	{"java.lang.reflect.Method",      "getModifiers",  "()I"},
	{"java.lang.reflect.Method",      "getName",       "()Ljava/lang/String;"},

	{"java.io.FileInputStream",  "open",       "(Ljava/lang/String;)V"},
	{"java.io.FileInputStream",  "available",  "()I"},
	{"java.io.FileInputStream",  "read",       "()I"},
	{"java.io.FileInputStream",  "read",       "([B)I"},
	{"java.io.FileInputStream",  "readBytes",  "([BII)I"},
	{"java.io.FileInputStream",  "skip",       "(J)J"},
	{"java.io.FileInputStream",  "close",      "()V"},

	{"java.io.File",  "length0",  "()J"},

	{"java.io.FileOutputStream", "open",       "(Ljava/lang/String;)V"},
	{"java.io.FileOutputStream", "close",      "()V"},
	{"java.io.FileOutputStream", "write",      "(I)V"},
	{"java.io.FileOutputStream", "write",      "([B)V"},
	{"java.io.FileOutputStream", "writeBytes", "([BII)V"},

	{"java.lang.System",   "currentTimeMillis",  "()J"},

	/*
	  {"java.lang.Thread",   "currentThread",      "()Ljava/lang/Thread;"},
	  {"java.lang.Thread",   "interrupt0",         "()V"},
	*/

	{"java.lang.Throwable", "printStackTrace0",  "(Ljava/lang/Object;)V"},

	{"java.lang.Runtime",   "exitInternal",      "(I)V"}
    };

    private static Set<HMethod> harmlessNatives = null;

    static void initHarmlessNatives(Linker linker) {
	harmlessNatives = new HashSet<HMethod>();
	for(int i = 0; i < hna.length; i++) {
	    HClass  hClass  = linker.forName(hna[i][0]);
	    assert hClass != null : 
		"Class " + hna[i][0] + " not found";
	    HMethod hMethod = hClass.getDeclaredMethod(hna[i][1], hna[i][2]);
	    assert hMethod != null : 
		"Method " + hna[i][0] + "." + 
		hna[i][1] + hna[i][2] + " not found";
	    harmlessNatives.add(hMethod);
	}
    }

    public static boolean canModel(HMethod hm) {
	if(harmlessNatives == null)
	    initHarmlessNatives(hm.getDeclaringClass().getLinker());
	if(harmlessNatives.contains(hm)) return true;
	if(isSpecial(hm)) return true;

	if(isArrayCopy(hm)) return true;
	if(isClone(hm)) return true;

	return false;
    }


    private static boolean isSpecial(HMethod hm) {
	if(notSpecial(hm)) return false;
	for(int i = 0; i < specialMethods.length; i++) {
	    if(hm.getName().equals(specialMethods[i][0]) &&
	       hm.getDescriptor().equals(specialMethods[i][1])) {
		return true;
	    }
	}
	return false;
    }

    private static boolean notSpecial(HMethod hm) {
	return 
	    hm.getDeclaringClass().getName().equals("java.lang.StringBuffer") &&
	    hm.getName().equals("toString");
    }


    private static String[][] specialMethods = new String[][] {
	{"hashCode",  "()I"},
	{"equals",    "(Ljava/lang/Object;)Z"},
	{"toString",  "()Ljava/lang/String;"},
	{"compareTo", "(Ljava/lang/Object;)I"}
    };


    /** @return <code>null</code> if we cannot model the call to the
	special method <code>callee</code>. */
    static boolean modelCALL(CALL cs,
			     List<LVar> paramVars,
			     IntraProc intraProc,
			     Collection<Constraint> newCons) {
	return
	    model(cs,
		  // TODO: this is a kind of very optimistic
		  cs.method(),
		  paramVars,
		  intraProc,
		  newCons);
	// we assume that all overriders of an unharmful native are also unharmful
    }



    /** @return <code>null</code> if we cannot model the call to the
	special method <code>callee</code>. */
    static boolean model(CALL cs,
			 HMethod callee,
			 List<LVar> paramVars,
			 IntraProc intraProc,
			 Collection<Constraint> newCons) {
	if(modelHarmlessNative(cs, callee, paramVars, intraProc, newCons))
	    return true;

	if(modelSpecialMethod(cs, callee, paramVars, intraProc, newCons))
	    return true;

	if(modelArrayCopy(cs, callee, paramVars, intraProc, newCons))
	    return true;

	if(modelClone(cs, callee, paramVars, intraProc, newCons))
	    return true;

	return false;
    }

    private static boolean modelHarmlessNative(CALL cs,
					       HMethod callee,
					       List<LVar> paramVars,
					       IntraProc intraProc,
					       Collection<Constraint> newCons) {
	if(harmlessNatives == null)
	    initHarmlessNatives(PAUtil.getLinker(cs));
	if(!harmlessNatives.contains(callee)) return false;

	// System.out.println("Harmless native: " + callee);

	modelSafeMethod(cs, callee, paramVars, intraProc, newCons);

	return true;
    }


    private static boolean modelSpecialMethod(CALL cs,
					      HMethod callee,
					      List<LVar> paramVars,					      
					      IntraProc intraProc,
					      Collection<Constraint> newCons) {
	if(!isSpecial(callee))
	    return false;
	modelSafeMethod(cs, callee, paramVars, intraProc, newCons);
	return true;
    }


    private static void modelSafeMethod(CALL cs,
					HMethod callee,
					List<LVar> paramVars,
					IntraProc intraProc,
					Collection<Constraint> newCons) {

	if(cs.retex() != null) {
	    PANode ngbl = intraProc.getNodeRep().getGlobalNode();
	    newCons.add(new CtConstraint(Collections.singleton(ngbl),
					 intraProc.lVar(cs.retex())));
	}

	HClass retType = callee.getReturnType();
	if((cs.retval() != null) && !retType.isPrimitive()) {
	    PAEdgeSet newEdges = DSFactories.edgeSetFactory.create();
	    PANode head = constructRetStruct(retType, cs, intraProc, newEdges,
					     new HashSet<HClass>());
	    // add the new edges
	    newCons.add(new LtConstraint(intraProc.preIVar(cs),
					 intraProc.postIVar(cs)));
	    if(!newEdges.isEmpty()) {
		newCons.add(new CtConstraint(newEdges,
					     intraProc.postIVar(cs)));
	    }

	    /*
	      System.out.println("newEdges: {");
	      newEdges.print("  ");
	      System.out.println("}");
	    */

	    // make cs.retval() point to the right node
	    newCons.add(new CtConstraint(Collections.singleton(head),
					 intraProc.lVar(cs.retval())));
	}

	if(Flags.RECORD_WRITES) {
	    addMutationConstraints4SafeMethod(cs, callee, paramVars, intraProc, newCons);
	}
    }


    private static void addMutationConstraints4SafeMethod(CALL cs,
							  HMethod callee,
							  List<LVar> paramVars,
							  IntraProc intraProc,
							  Collection<Constraint> newCons) {
	if(callee.getDeclaringClass().getName().equals("java.io.FileInputStream")) {
	    if(callee.getName().equals("available")) return;
	    assert !callee.isStatic();

	    // the state of input file stream is mutated
	    newCons.add(new WriteConstraint(paramVars.get(0), null, intraProc.vWrites()));

	    // input stream methods that take an array as first argument, write its elements
	    List<HClass> types = PAUtil.getParamTypes(callee);
	    if(types.size() >= 2) {
		HClass firstParamType = types.get(1);
		if(firstParamType.isArray()) {
		    newCons.add(new WriteConstraint(paramVars.get(1),
						    PAUtil.getArrayField(firstParamType.getLinker()),
						    intraProc.vWrites()));
		}
	    }
	}

	if(callee.getDeclaringClass().getName().equals("java.io.FileOutputStream")) {
	    // the state of input file stream is mutated
	    newCons.add(new WriteConstraint(paramVars.get(0), null, intraProc.vWrites()));	    
	}
    }


    static PANode constructRetStruct(HClass hClass, CALL cs, IntraProc intraProc, PAEdgeSet newEdges,
				     Set<HClass> seenClasses) {
	PANode root = intraProc.getNodeRep().getImmNode(cs, hClass);

	// avoid cycles: do not remodel already modeled classes (we use type granularity)
	if(!seenClasses.add(hClass))
	    return root;

	if(hClass.isArray()) {
	    HClass elemType = hClass.getComponentType();
	    if(!elemType.isPrimitive()) {
		PANode nodeElem = constructRetStruct(elemType, cs, intraProc, newEdges, seenClasses);
		newEdges.addEdge(root, PAUtil.getArrayField(hClass.getLinker()), nodeElem, true);
	    }
	}
	else {
	    for(HField hf : hClass.getFields()) {
		HClass fieldType = hf.getType();
		if(fieldType.isPrimitive()) continue;
		
		PANode nodeField = constructRetStruct(fieldType, cs, intraProc, newEdges, seenClasses);
		newEdges.addEdge(root, PAUtil.getUniqueField(hf), nodeField, true);
	    }
	}

	return root;
    }


    private static boolean isArrayCopy(HMethod hm) {
	return isThatMethod(hm,
			    "java.lang.System",
			    "arraycopy", 
			    "(Ljava/lang/Object;ILjava/lang/Object;II)V");
    }

    private static boolean isClone(HMethod hm) {
	return 
	    (hm.getDeclaringClass().getName().equals("java.lang.Object") ||
	     // arrays can have .clone() methods too;
	     // ex: int[][].clone() in JLex.SparseBitSet.clone()
	     hm.getDeclaringClass().isArray()) &&
	    hm.getName().equals("clone") &&
	    hm.getDescriptor().equals("()Ljava/lang/Object;");
    }

    private static boolean isThatMethod(HMethod hm, String className, String methodName, String desc) {
	return 
	    hm.getDeclaringClass().getName().equals(className) &&
	    hm.getName().equals(methodName) &&
	    hm.getDescriptor().equals(desc);
    }


    private static boolean modelArrayCopy(CALL cs,
					  HMethod callee,
					  List<LVar> paramVars,
					  IntraProc intraProc,
					  Collection<Constraint> newCons) {
	if(!isArrayCopy(callee)) return false;

	assert paramVars.size() == 2;
	LVar src  = paramVars.get(0);
	LVar tmp = new LVar();
	LVar dst = paramVars.get(1);

	HField arrayField = PAUtil.getArrayField(callee.getDeclaringClass().getLinker());

	LinkedList<Constraint> list = new LinkedList<Constraint>();

	// tmp = src.[]
	IntraProc.addLoadConstraints(cs, tmp, src, arrayField,
				     intraProc,
				     list);

	// dst.[] = tmp
	IntraProc.addStoreConstraints(cs, dst, arrayField, tmp,
				      intraProc,
				      list);

	if(Flags.VERBOSE_ARRAYCOPY) {
	    System.out.println("Constraints for arraycopy(" + src + "," + dst +")");
	    for(Constraint c : list) {
		System.out.println("\t" + c);
	    }
	}

	newCons.addAll(list);

	return true;
    }


    private static boolean modelClone(CALL cs,
				      HMethod callee,
				      List<LVar> paramVars,
				      IntraProc intraProc,
				      Collection<Constraint> newCons) {
	if(!isClone(callee)) return false;
	
	assert paramVars.size() == 1;
	LVar src = paramVars.get(0);
	LVar dst = null;

	newCons.add(new CloneCallConstraint(cs,
					    intraProc.lVar(cs.retval()),
					    intraProc.lVar(cs.retex()),
					    paramVars.get(0),

					    intraProc.preIVar(cs),
					    intraProc.preFVar(cs),
					    
					    intraProc.postIVar(cs),
					    intraProc.oVar(),
					    intraProc.postFVar(cs),

					    intraProc.getNodeRep()));

	return true;
    }
}
