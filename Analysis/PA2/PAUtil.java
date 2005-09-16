// PAUtil.java, created Tue Jun 28 12:37:11 2005 by salcianu
// Copyright (C) 2003 Alexandru Salcianu <salcianu@alum.mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.PA2;

import java.util.Collections;
import java.util.Collection;
import java.util.Set;
import java.util.HashSet;
import java.util.List;
import java.util.LinkedList;
import java.util.Comparator;
import java.util.Map;
import java.util.HashMap;
import java.util.Arrays;

import jpaul.DataStructs.WorkSet;
import jpaul.DataStructs.WorkList;
import jpaul.DataStructs.DSUtil;
import jpaul.Graphs.DiGraph;
import jpaul.Graphs.ForwardNavigator;

import harpoon.ClassFile.HMember;
import harpoon.ClassFile.HField;
import harpoon.ClassFile.HFieldMutator;
import harpoon.ClassFile.HMethod;
import harpoon.ClassFile.HConstructor;
import harpoon.ClassFile.HType;
import harpoon.ClassFile.HClass;
import harpoon.ClassFile.Linker;
import harpoon.ClassFile.HCode;
import harpoon.ClassFile.CachingCodeFactory;

import harpoon.IR.Quads.Quad;
import harpoon.IR.Quads.CALL;
import harpoon.IR.Quads.HEADER;
import harpoon.IR.Quads.METHOD;

import harpoon.Temp.Temp;

import harpoon.Util.Util;

/**
 * <code>PAUtil</code>
 * 
 * @author  Alexandru Salcianu <salcianu@alum.mit.edu>
 * @version $Id: PAUtil.java,v 1.6 2005-09-16 14:47:35 salcianu Exp $
 */
public abstract class PAUtil {

    public static boolean isNative(HMethod hm) {
	return java.lang.reflect.Modifier.isNative(hm.getModifiers());
    }

    public static boolean isAbstract(HMethod hm) {
	return java.lang.reflect.Modifier.isAbstract(hm.getModifiers());
    }

    public static boolean trivialEscape(PANode node) {
	switch(node.kind) {
	case PARAM:
	case GBL: 
	case LOAD:
	    return true;
	default:
	    return false;
	}
    }

    public static boolean escapeAny(Collection<PANode> S, Collection<PANode> F) {
	for(PANode n : S) {
	    if(trivialEscape(n)) {
		return true;
	    }
	}

	// technical check
	if(F == null) return false;

	// we can fusion these two loops, but the check performed by
	// the first one is much cheaper than the test performed by
	// the second one.
	for(PANode n : S) {
	    if(F.contains(n))
		return true;
	}
	return false;
    }

    public static boolean escape(PANode n, Collection<PANode> F) {
	if(trivialEscape(n)) {
	    return true;
	}
	if(F == null) return false;
	return F.contains(n);
    }


    private static boolean newEscape(PANode node, Collection<PANode> preF /*, Collection<PANode> postF*/) {
	return 	
	    ((preF == null)  || !PAUtil.escape(node, preF))
	    /* && ((postF == null) || !PAUtil.escape(node, postF)) */;
    }


    static Set<PANode> findNewEsc(Collection<PANode> dirEsc, DiGraph<PANode> I, Collection<PANode> preF /*, Collection<PANode> postF*/) {
	Set<PANode> newEsc = DSFactories.nodeSetFactory.create();
	for(PANode node : dirEsc) {
	    if(newEscape(node, preF/*, postF*/)) {
		newEsc.add(node);
	    }
	}

	if(I == null) return newEsc;

	ForwardNavigator<PANode> fnav = I.getForwardNavigator();

	LinkedList<PANode> workSet = new LinkedList<PANode>();
	workSet.addAll(newEsc);
	while(!workSet.isEmpty()) {
	    PANode node = workSet.removeFirst();
	    for(PANode node2 : fnav.next(node)) {
		if(newEscape(node2, preF/*, postF*/)) {
		    if(newEsc.add(node2)) {
			workSet.addLast(node2);
		    }
		}
	    }
	}

	return newEsc;
    }

    static PAEdgeSet fixNull(PAEdgeSet es) {
	if(es == null) {
	    es = PAEdgeSet.IMM_EMPTY_EDGE_SET;
	}
	return es;
    }

    static Set<PANode> fixNull(Set<PANode> ns) {
	if(ns == null) {
	    ns = Collections.<PANode>emptySet();
	}
	return ns;
    }


    static PAEdgeSet fixNullM(PAEdgeSet es) {
	if(es == null) {
	    es = DSFactories.edgeSetFactory.create();
	}
	return es;
    }

    static Set<PANode> fixNullM(Set<PANode> ns) {
	if(ns == null) {
	    ns = DSFactories.nodeSetFactory.create();
	}
	return ns;
    }

    // Comparator for objects where equals is object identity.
    // We compare the two objects addresses (the identity hash codes).
    // 
    // WARNING: IT IS A MAJOR ERROR TO USE THIS FOR OBJECTS WHERE
    // EQUALS IS NOT OBJECT IDENTITY!
    static class IdentityComparator<T> implements Comparator<T> {
	public int compare(T o1, T o2) {
	    int i1 = System.identityHashCode(o1);
	    int i2 = System.identityHashCode(o2);
	    if(i1 == i2) return 0;
	    if(i1 < i2) return -1;
	    return +1;
	}
	public boolean equals(Object o) {
	    return this == o;
	}
    }

    static Comparator<HField> fieldComparator = new IdentityComparator<HField>();
    static Comparator<PANode> nodeComparator  = new IdentityComparator<PANode>();


    static HMethod getMethod(Quad q) {
	return q.getFactory().getMethod();
    }

    static Linker getLinker(Quad q) {
	return q.getFactory().getMethod().getDeclaringClass().getLinker();
    }


    // return a list with the types of all arguments of hm (including this, for a non-static method)
    public static List<HClass> getParamTypes(HMethod hm) {
	LinkedList<HClass> pTypes = new LinkedList<HClass>();
	if(!hm.isStatic()) {
	    pTypes.add(hm.getDeclaringClass());
	}
	    
	HClass[] pt = hm.getParameterTypes();
	for(HClass type : hm.getParameterTypes()) {
	    pTypes.add(type);
	}
	return pTypes;
    }

    /** Returns a list of the types of the object (=non-primitive)
        parameters of method <code>hm</code>.  This lists include the
        type of the receiver (for non-static methods) and respects the
        order in which params are declared.  */
    public static List<HClass> getObjParamTypes(HMethod hm) {
	LinkedList<HClass> objParamTypes = new LinkedList<HClass>();
	for(HClass hClass : getParamTypes(hm)) {
	    if(!hClass.isPrimitive()) {
		objParamTypes.addLast(hClass);
	    }
	}
	return objParamTypes;
    }

    static synchronized HField getArrayField(final Linker linker) {
	if(arrayField == null) {
	    final HClass objClass = linker.forName("java.lang.Object");
	    arrayField = new AbstrMyHField() {
		// IDEA: one could make one array field for each
		// element type, e.g., one array field for A[], and a
		// different one for B[].  In that case. the
		// declaringClass would return the class for "A[]",
		// and getType would return "A".
		public HClass getDeclaringClass() {
		    return null;
		    /*
		    if(declaringClass == null) {
			declaringClass = harpoon.Util.HClassUtil.arrayClass(linker, objClass, 1);
		    }
		    return declaringClass;
		    */
		}
		private HClass declaringClass;
		    
		public String getName()   { return "[]"; }
		public int getModifiers() { return 0; }
		public HClass getType()   { return objClass; }
		    
		public String getDescriptor() { return "bogus"; }
		public Object getConstant()  { return null; }
		public boolean isConstant()  { return false; }
		public boolean isSynthetic() { return false; }
		public boolean isStatic()    { return false; }
		public HFieldMutator getMutator()  {
		    // should never be executed
		    assert false;
		    return null;
		}
		public String  toString()          { return "[*]"; }
		public HType   getGenericType()    { 
		    // should never be executed
		    assert false;
		    return null;
		}
	    };
	}
	return arrayField;
    }
    private static HField arrayField;


    static synchronized HField getUniqueField(HField hf) {
	HFieldWrap result = uniquehf.get(hf);
	if(result == null) {
	    result = new HFieldWrap(hf);
	    uniquehf.put(hf, result);
	}
	return result;
    }

    private static Map<HField,HFieldWrap> uniquehf = new HashMap<HField,HFieldWrap>();
	
    private static abstract class AbstrMyHField implements HField {
	public int compareTo(HMember o) {
	    int h1 = System.identityHashCode(this);
	    int h2 = System.identityHashCode(o);
	    if(h1 < h2) return -1;
	    else if (h1 > h2) return +1;
	    else return 0;
	}
    }
	
    private static class HFieldWrap extends AbstrMyHField {
	public HFieldWrap(HField hf) { this.hf = hf; }
	private final HField hf;
	    
	public HClass  getDeclaringClass() { return hf.getDeclaringClass(); }
	public String  getName()           { return hf.getName(); }
	public int     getModifiers()      { return hf.getModifiers(); }
	public HClass  getType()           { return hf.getType(); }
	public String  getDescriptor()     { return hf.getDescriptor(); }
	public Object  getConstant()       { return hf.getConstant(); }
	public boolean isConstant()        { return hf.isConstant(); }
	public boolean isSynthetic()       { return hf.isSynthetic(); }
	public boolean isStatic()          { return hf.isStatic(); }
	public HFieldMutator getMutator()  { return hf.getMutator(); }
	public String  toString()          { return hf.toString(); }
	public HType   getGenericType()    { return hf.getGenericType(); }
    };


    public static boolean isException(HClass hClass) {
	if(jlThrowable == null) {
	    jlThrowable = hClass.getLinker().forName("java.lang.Throwable");
	}
	return hClass.isInstanceOf(jlThrowable);
    }
    private static HClass jlThrowable;

    
    static final boolean VERBOSE = true;


    private static final int[] nodeStats(InterProcAnalysisResult ar, boolean stop) {
	int nbInside = 0;
	int nbLoad = 0;
	int nbParam = 0;
	int nbOther = 0;

	// need to create the set to get rid of duplicates from the Iterable
	for(PANode node : new HashSet<PANode>(DSUtil.iterable2coll(ar.getAllNodes()))) {
	    switch(node.kind) {
	    case INSIDE:
	    case IMM:
		nbInside++;
		break;
	    case LOAD:
		nbLoad++;
		break;
	    case PARAM:
		nbParam++;
		break;
	    default:
		nbOther++;
		break;
	    }
	}

	/*
	if(stop && (nbInside > 100)) {
	    System.out.println(ar);

	    System.out.println("\n\nLABA");
	}
	*/

	return 
	    new int[] { nbInside, nbLoad, nbParam, nbOther };
    }

    static final String graphSizeStats(InterProcAnalysisResult ar) {
	return graphSizeStats(ar, false);
    }

    static final String graphSizeStats(InterProcAnalysisResult ar, boolean stop) {
	int[] ns = nodeStats(ar, stop);
	return
	    "(I:" + ns[0] + " O:" + ns[1] + " P:" + ns[2] + " t:" + ns[3] +
	    " IE:" + ar.eomI().size().right.longValue() + 
	    " OE:" + ar.eomO().size().right.longValue() + ")";
    }

    static boolean exceptionInitializer(HMethod hm) {
	return 
	    (hm instanceof HConstructor) && 
	    PAUtil.isException(hm.getDeclaringClass());
    }

    static boolean exceptionInitializerCall(CALL cs) {
	HMethod method = cs.method();
	HClass hclass = method.getDeclaringClass();
	return 
	    exceptionInitializer(method) &&
	    hclass.getName().startsWith("java.") &&
	    // to improve the mutation analysis, we decide to still
	    // analyze calls to exception constructors, if they occur
	    // inside other exception initializers
	    !exceptionInitializer(Util.quad2method(cs));
    }


    static boolean isOldAndMutable(PANode node) {
	switch(node.kind) {
	case PARAM:
	case LOAD:
	case GBL:
	    return true;
	default:
	    return false;
	}
    }

}
