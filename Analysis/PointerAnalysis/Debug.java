// Debug.java, created Thu Feb 10 19:06:16 2000 by salcianu
// Copyright (C) 2000 Alexandru SALCIANU <salcianu@retezat.lcs.mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.PointerAnalysis;

import java.util.Set;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Collection;

import harpoon.Util.UComp;

import harpoon.Util.Graphs.TopSortedCompDiGraph;

import harpoon.ClassFile.HMethod;
import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HCodeElement;
import harpoon.Analysis.MetaMethods.MetaMethod;
import harpoon.Analysis.MetaMethods.MetaCallGraph;

import harpoon.Util.LightBasicBlocks.LightBasicBlock;
import harpoon.Util.Graphs.SCComponent;
import harpoon.ClassFile.HCodeElement;

import harpoon.Util.DataStructs.Relation;



/**
 * <code>Debug</code>
 * 
 * @author  Alexandru SALCIANU <salcianu@retezat.lcs.mit.edu>
 * @version $Id: Debug.java,v 1.7 2004-03-04 22:32:18 salcianu Exp $
 */
public abstract class Debug implements java.io.Serializable {

    /** Returns a sorted array containing all the objects from
	<code>set</code>. Increasing lexicographic order of the
	string representation of the objects is used. */
    public static Object[] sortedCollection(Collection col){
	Object[] objs = col.toArray(new Object[col.size()]);
	Arrays.sort(objs, UComp.uc);
	return objs;
    }

    public static Object[] sortedSet(Set set) {
	return sortedCollection(set);
    }

    
    /** Provides a string representation of a set; the elements of the
	set appear in increasing lexicographic order.
	<code>set1.equals(set2) <==> stringImg(set1).equals(stringImg(set2)).</code>*/
    public static String stringImg(Collection col){
	StringBuffer buffer = new StringBuffer();

	Object[] objs = sortedCollection(col);

	buffer.append("[ ");
	for(int i = 0; i < objs.length; i++){
	    buffer.append(objs[i]);
	    buffer.append(" ");
	}
	buffer.append("]");

	return buffer.toString();
    }

    public static String stringImg(Object[] v){

	if(v==null) return "null";

	StringBuffer buffer = new StringBuffer();

	Arrays.sort(v,UComp.uc);
	for(int i=0; i<v.length;i++){
	    buffer.append(v[i]);
	    buffer.append("\n");
	}

	return buffer.toString();
    }

    /** Displays a <i>split</i> relation (see the MetaCallGraph stuff). */
    public static void show_split(Relation split){
	for(Object hmO : split.keys()){
	    HMethod hm = (HMethod) hmO;
	    System.out.print(hm);
	    System.out.println("  (" + split.getValues(hm).size() +
			       " specialization(s))");
	    Iterator itmm = split.getValues(hm).iterator();
	    while(itmm.hasNext()) {
		System.out.print("  ");
		System.out.println((MetaMethod)itmm.next());
	    }
	}
    }


    public static void show_lbb(LightBasicBlock lbb){
	System.out.println("  " + lbb + "{");
	HCodeElement[] hces = lbb.getElements();
	for(int i = 0; i < hces.length; i++)
	    System.out.println("    " + hces[i].getSourceFile() + ":" +
			       hces[i].getLineNumber() + " " + hces[i]);

	LightBasicBlock[] prev = lbb.getPrevLBBs();
	if(prev.length != 0){
	    System.out.print("   Prev: ");
	    for(int i = 0; i < prev.length; i++)
		System.out.print(prev[i] + " ");
	    System.out.println();
	}

	LightBasicBlock[] next = lbb.getNextLBBs();
	if(next.length != 0){
	    System.out.print("   Next: ");
	    for(int i = 0; i < next.length; i++)
		System.out.print(next[i] + " ");
	    System.out.println();
	}
	
	System.out.println("  }");
    }

    public static void show_lbb_scc(TopSortedCompDiGraph ts_sccs) {
	for(Object scc0 : ts_sccs.decrOrder()) {
	    SCComponent scc = (SCComponent) scc0;

	    System.out.println("SCC" + scc.getId() + "{");
	    Object nodes[] = scc.nodes();
	    Arrays.sort(nodes, harpoon.Util.UComp.uc);
	    for(int i = 0; i < nodes.length; i++) 
		show_lbb((LightBasicBlock) nodes[i]);

	    System.out.println("}");
	}
    }


    /** Checks whether the method <code>hm</code> is the method named
	<code>mthd</code> from the class named <code>cls</code>. */
    public static boolean isThatOne(HMethod hm, String cls, String mthd) {
	return
	    hm.getName().equals(mthd) &&
	    hm.getDeclaringClass().getName().equals(cls);
    }

    /** Pretty print debug function for SCC's of <code>MetaMethod</code>s. */
    public static String sccToString(SCComponent scc, MetaCallGraph mcg) {
	StringBuffer buffer = new StringBuffer();

	buffer.append("SCC" + scc.getId() + " (size " + scc.size() + ") {\n");

	Object[] nodes_array = scc.nodes();
	Set nodes = scc.nodeSet();

	for(int i = 0; i < nodes_array.length; i++) {
	    Object o = nodes_array[i];
	    buffer.append(o);
	    buffer.append("\n");
	    Object[] next = mcg.getCallees((MetaMethod) o);
	    for(int j = 0; j < next.length; j++)
		if(nodes.contains(next[j]))
		    buffer.append("  " + next[j] + "\n");
	    buffer.append("\n");
	}
	buffer.append("}\n");

	int nb_prev = scc.prevLength();
	if(nb_prev > 0) {
	    buffer.append("Prev:");
	    for(int i = 0; i < nb_prev ; i++) {
		buffer.append(" SCC" + scc.prev(i).getId());
	    }
	    buffer.append("\n");
	}

	int nb_next = scc.nextLength();
	if(nb_next > 0) {
	    buffer.append("Next:");
	    for(int i = 0; i < nb_next ; i++) {
		buffer.append(" SCC" + scc.next(i).getId());
	    }
	    buffer.append("\n");
	}

	return buffer.toString();
    }


    // Displays all stringly connected components of methods
    static void display_mm_sccs
	(TopSortedCompDiGraph ts_mmethods, MetaCallGraph mcg, long time) {
	int counter  = 0;
	int mmethods = 0;
	
	if(PointerAnalysis.DEBUG_SCC)
	    System.out.println("===== SCCs of methods =====");
	
	for(Object scc0 : ts_mmethods.incrOrder()) {
	    SCComponent scc = (SCComponent) scc0;
	    if(PointerAnalysis.DEBUG_SCC) {
		System.out.print(Debug.sccToString(scc, mcg));
	    }
	    counter++;
	    mmethods += scc.nodeSet().size();
	}
	
	if(PointerAnalysis.DEBUG_SCC)
	    System.out.println("===== END SCCs ============");
	
	if(PointerAnalysis.TIMING)
	    System.out.println(counter + " component(s); " +
			       mmethods + " meta-method(s); " +
			       time + "ms processing time");
    }

}
