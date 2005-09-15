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

import harpoon.Util.ParseUtil;
import harpoon.Util.ParseUtil.BadLineException;
import java.io.IOException;

/**
 * <code>SpecialInterProc</code>
 * 
 * @author  Alexandru Salcianu <salcianu@alum.mit.edu>
 * @version $Id: SpecialInterProc.java,v 1.7 2005-09-15 14:25:56 salcianu Exp $
 */
public class SpecialInterProc {

    // Quasi-safe methods.  These methods (usually natives) are
    // treated specially (basically, by describing their lack of
    // effect on the alising and the mutations they perform), in order
    // to improve precision.
    //
    // These methods do not create any externally visible aliasing to
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
    // Due to this mutation possibility, we call them "quasi-safe" :)

    // map quasiSafe methods -> list of indices of params whose
    // (transitively reachable) non-object fields may be mutated
    private static Map<HMethod,List<Integer>> quasiSafe = null;

    // map quasiSafe methods -> list of types of non-primitive parameters
    private static Map<HMethod,List<HClass>> quasiSafeObjTypes = null;


    private static String quasiSafePropertiesFileName() {	
	return 
	    "harpoon/Analysis/PA2/quasi-safe." + 
	    harpoon.Main.Settings.getStdLibVerName() +
	    ".properties";
    }

    // exceptions thrown by initQuasiSafe use this prefix
    private static String quasiSafeErrorPrefix() {
	return
	    "Error reading quasi-safe methods from " +
	    quasiSafePropertiesFileName();
    }

    static void initQuasiSafe(final Linker linker) {
	quasiSafe = new HashMap<HMethod,List<Integer>>();
	quasiSafeObjTypes = new HashMap<HMethod,List<HClass>>();

	try {
	    ParseUtil.readResource
		(quasiSafePropertiesFileName(),
		 new ParseUtil.StringParser() {
		    public void parseString(String s) throws BadLineException {
			int equals = s.indexOf('=');
			String mName = null;
			final List<Integer> mutatedParams = new LinkedList<Integer>();
			if(equals == -1) {
			    mName = s;
			}
			else {
			    mName = s.substring(0, equals);
			    ParseUtil.parseSet
				(s.substring(equals+1),
				 new ParseUtil.StringParser() {
				    public void parseString(String s) {
					try {
					    int i = Integer.parseInt(s.trim());
					    if(i < -2) throw new NumberFormatException();
					    mutatedParams.add(new Integer(i));
					}
					catch(NumberFormatException ex) {
					    throw new RuntimeException
						(quasiSafeErrorPrefix() + 
						 "; not a valid param number " + s,
						 ex);
					}
				    }
				});
			}
			
			HMethod hm = ParseUtil.parseMethod(linker, mName.trim());
			checkMutatedParams(hm, mutatedParams);

			quasiSafe.put(hm, mutatedParams);
			if(!mutatedParams.isEmpty())
			    quasiSafeObjTypes.put(hm, PAUtil.getObjParamTypes(hm));
			
			System.out.println
			    ("quasiSafe: " + hm +
			     (mutatedParams.isEmpty() ? 
			      "" : 
			      ("\n\tmutated params = " + mutatedParams.toString())));
		    }


		    public void checkMutatedParams(HMethod hm, List<Integer> mutatedParams) throws BadLineException {
			int objParams = PAUtil.getObjParamTypes(hm).size();
			for(Integer index : mutatedParams) {
			    int i = index.intValue();
			    if(i < 0) {
				continue; // must be one of the special IO effects
			    }
			    if(i >= objParams) {
				throw new BadLineException
				    ("Obj. param. index too big: " + i + "\n\tmethod = " + hm);
			    }
			}
		    }
		});
	}
	catch(IOException ex) {
	    throw new RuntimeException(quasiSafeErrorPrefix(), ex);
	}
    }


    public static boolean canModel(HMethod hm) {
	if(isQuasiSafe(hm)) return true;
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
	// we assume that all overriders of a quasi-safe native are also quasi-safe
    }



    /** @return <code>null</code> if we cannot model the call to the
	special method <code>callee</code>. */
    static boolean model(CALL cs,
			 HMethod callee,
			 List<LVar> paramVars,
			 IntraProc intraProc,
			 Collection<Constraint> newCons) {
	if(modelQuasiSafeNative(cs, callee, paramVars, intraProc, newCons))
	    return true;

	if(modelSpecialMethod(cs, callee, paramVars, intraProc, newCons))
	    return true;

	if(modelArrayCopy(cs, callee, paramVars, intraProc, newCons))
	    return true;

	if(modelClone(cs, callee, paramVars, intraProc, newCons))
	    return true;

	return false;
    }


    private static boolean isQuasiSafe(HMethod hm) {
	if(quasiSafe == null) 
	    initQuasiSafe(hm.getDeclaringClass().getLinker());
	return quasiSafe.containsKey(hm);
    }


    private static boolean modelQuasiSafeNative(CALL cs,
						HMethod callee,
						List<LVar> paramVars,
						IntraProc intraProc,
						Collection<Constraint> newCons) {
	if(!isQuasiSafe(callee)) return false;

	// System.out.println("Quasi safe native: " + callee);

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
	if(quasiSafe == null) {
	    initQuasiSafe(callee.getDeclaringClass().getLinker());
	}
	List<Integer> mutatedParams = quasiSafe.get(callee);
	if((mutatedParams == null) || mutatedParams.isEmpty()) return;

	List<HClass> objParamTypes = quasiSafeObjTypes.get(callee);

	for(Integer index : mutatedParams) {
	    int i = index.intValue();
	    if(i < 0) {
		continue; // must be one of the IO effects
	    }
	    HClass hClass = objParamTypes.get(i);
	    HField hf = 
		hClass.isArray() ? 
		PAUtil.getArrayField(callee.getDeclaringClass().getLinker()) :
		null; 
	    // TODO: null field means the entire object state is
	    // potentially changed.  Maybe we should have a special
	    // field for "non-object fields of the object state".

	    newCons.add(new WriteConstraint(paramVars.get(i),
					    hf,
					    intraProc.vWrites()));
	}


	/*
	if(callee.getDeclaringClass().getName().equals("java.io.FileInputStream")) {
	    if(callee.isStatic()) return;
	    if(callee.getName().equals("available"))
		return;
	    
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
	*/
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
	     // TODO: check semantics of clone for multidimensional arrays.
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
