// TypeFilter.java, created Tue Jul 19 11:08:13 2005 by salcianu
// Copyright (C) 2003 Alexandru Salcianu <salcianu@alum.mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.PA2;

import java.util.Collections;
import java.util.Collection;
import java.util.Set;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.LinkedList;


import jpaul.DataStructs.Pair;
import jpaul.DataStructs.DSUtil;
import jpaul.Graphs.DiGraph;
import jpaul.Graphs.ForwardNavigator;

import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HField;

import harpoon.Analysis.ClassHierarchy;

/**
 * <code>TypeFilter</code>
 * 
 * @author  Alexandru Salcianu <salcianu@alum.mit.edu>
 * @version $Id: TypeFilter.java,v 1.1 2005-08-10 02:58:19 salcianu Exp $
 */
public abstract class TypeFilter {

    public static void initClassHierarchy(ClassHierarchy ch) { TypeFilter.ch = ch; }
    public static void releaseClassHierarchy() { TypeFilter.ch = null; }

    private static ClassHierarchy ch;


    public static boolean compatible(PANode node, HClass hClass) {
	Map<HClass,Boolean> class2compat = node2class2compat.get(node);
	if(class2compat == null) {
	    class2compat = new HashMap<HClass,Boolean>();
	    node2class2compat.put(node, class2compat);
	}
	Boolean result = class2compat.get(hClass);
	if(result == null) {
	    result = Boolean.valueOf(_compatible(node, hClass));
	    class2compat.put(hClass, result);
	    if(Flags.VERBOSE_TYPE_FILTER)
		System.out.println("compatible(" + node + ", " + hClass + ") = " + result);
	}
	return result.booleanValue();
    }

    // this is not kosher, as we may cache results used with a different linker ...
    // TODO: make this method non-static
    private static Map<PANode,Map<HClass,Boolean>> node2class2compat = 
	new HashMap<PANode,Map<HClass,Boolean>>();


    private static boolean _compatible(PANode node, HClass hClass) {
	if(node.type == null) return true;

	if(node.type.isPOLY()) {
	    HClass nodeClass = node.type.getHClass();
	    if(isObject(nodeClass)) {
		return true;
	    }

	    for(HClass child : getChildren(nodeClass)) {
		if(hClass.isAssignableFrom(child)) {
		    return true;
		}
	    }
	    return false;
	}
	else {
	    // exact type, has to be a subtype of hClass
	    return hClass.isAssignableFrom(node.type.getHClass());
	}
	
    }


    static Collection<HClass> getChildren(HClass hClass) {
	Set<HClass> children = hClass2children.get(hClass);
	if(children == null) {
	    children = 
		DiGraph.diGraph
		(Collections.<HClass>singleton(hClass),
		 new ForwardNavigator<HClass>() {
		     public List<HClass> next(HClass hc) {
			 return new LinkedList<HClass>(ch.children(hc));
		     }
		 }).
		transitiveSucc(hClass);
	    hClass2children.put(hClass, children);
	}
	return children;
    }
    private static Map<HClass,Set<HClass>> hClass2children = new HashMap<HClass,Set<HClass>>();


    private static boolean hasChildArray(HClass hClass) {
	Boolean answer = cacheHasChildArray.get(hClass);
	if(answer == null) {
	    answer = _hasChildArray(hClass);
	    cacheHasChildArray.put(hClass, Boolean.valueOf(answer));
	}
	return answer.booleanValue();
    }
    private static Map<HClass,Boolean> cacheHasChildArray = new HashMap<HClass,Boolean>();

    private static boolean _hasChildArray(HClass hClass) {
	if(hClass.equals(hClass.getLinker().forName("java.lang.Object"))) 
	    return true;
	try {
	    if(hClass.equals(hClass.getLinker().forName("java.lang.Iterable")))
		return true;
	}
	catch(Exception e) { } // do nothing 

	return 
	    hClass.isArray() && !hClass.getComponentType().isPrimitive();
    }
    

    public static boolean mayHaveField(PANode node, HField hf) {
	HClass declClass = hf.getDeclaringClass();
	if(declClass == null) {
	    if(node.type != null) {
		if(node.type.isPOLY()) {
		    return hasChildArray(node.type.getHClass());
		}
		else {
		    return node.type.getHClass().isArray();
		}
	    }
	    return true;
	}
	if(compatible(node, declClass)) {
	    return true;
	}
	else {
	    if(Flags.VERBOSE_TYPE_FILTER)
		System.out.println("No " + hf + " in " + node + ";\n\tdeclClass = " + declClass);
	    return false;
	}	    
    }


    public static boolean mayPointTo(HField hf, PANode node) {
	if(compatible(node, hf.getType())) {
	    return true;
	}
	else {
	    if(Flags.VERBOSE_TYPE_FILTER)
		System.out.println(hf + " cannot point to " + node);
	    return false;
	}
    }


    private static boolean isObject(HClass hClass) {
	return hClass.equals(hClass.getLinker().forName("java.lang.Object"));
    }
}
