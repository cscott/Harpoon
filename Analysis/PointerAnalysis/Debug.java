// Debug.java, created Thu Feb 10 19:06:16 2000 by salcianu
// Copyright (C) 2000 Alexandru SALCIANU <salcianu@retezat.lcs.mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.PointerAnalysis;

import java.util.Set;
import java.util.Map;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Collection;

import harpoon.Util.UComp;

import jpaul.Graphs.TopSortedCompDiGraph;

import harpoon.ClassFile.HMethod;
import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HCodeElement;
import harpoon.Analysis.MetaMethods.MetaMethod;
import harpoon.Analysis.MetaMethods.MetaCallGraph;

import harpoon.Util.LightBasicBlocks.CachingSCCLBBFactory;
import harpoon.Util.LightBasicBlocks.LightBasicBlock;
import jpaul.Graphs.SCComponent;
import harpoon.ClassFile.HCodeElement;

import harpoon.Util.DataStructs.Relation;

/**
 * <code>Debug</code>
 * 
 * @author  Alexandru SALCIANU <salcianu@retezat.lcs.mit.edu>
 * @version $Id: Debug.java,v 1.11 2005-08-17 17:33:41 salcianu Exp $
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

    public static void show_lbb_scc
	(MetaMethod mm, TopSortedCompDiGraph<LightBasicBlock> ts_sccs) {

	System.out.println("THE CODE FOR :" + mm.getHMethod());

	for(SCComponent scc : ts_sccs.decrOrder()) {
	    System.out.println("SCC" + scc.getId() + "{");
	    Object nodes[] = scc.nodes().toArray();
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
    public static String sccToString(SCComponent<MetaMethod> scc, MetaCallGraph mcg) {
	StringBuffer buffer = new StringBuffer();

	buffer.append("SCC" + scc.getId() + " (size " + scc.size() + ") {\n");

	for(MetaMethod mm : scc.nodes()) {
	    buffer.append(" ").append(mm).append("\n");
	    int k = 0;
	    for(MetaMethod callee : mcg.getCallees(mm)) {
		if(scc.contains(callee)) {
		    buffer.append("   ").append(callee).append("\n");
		    k++;
		}
	    }
	    if(k > 0)
		buffer.append("\n");
	}
	buffer.append("}\n");

	if(scc.prev().size() > 0) {
	    buffer.append("Prev:");
	    for(SCComponent<MetaMethod> prev : scc.prev()) {
		buffer.append(" SCC" + prev.getId());
	    }
	    buffer.append("\n");
	}
	
	if(scc.next().size() > 0) {
	    buffer.append("Next:");
	    for(SCComponent<MetaMethod> next : scc.next()) {
		buffer.append(" SCC" + next.getId());
	    }
	    buffer.append("\n");
	}

	return buffer.toString();
    }


    // Displays all stringly connected components of methods
    static void display_mm_sccs
	(TopSortedCompDiGraph<MetaMethod> ts_mmethods,
	 MetaCallGraph mcg, long time) {
	int counter  = 0;
	int mmethods = 0;
	
	if(PointerAnalysis.DEBUG_SCC)
	    System.out.println("===== SCCs of methods =====");
	
	for(SCComponent scc : ts_mmethods.incrOrder()) {
	    if(PointerAnalysis.DEBUG_SCC)
		System.out.print(Debug.sccToString(scc, mcg));
	    counter++;
	    mmethods += scc.nodes().size();
	}
	
	if(PointerAnalysis.DEBUG_SCC)
	    System.out.println("===== END SCCs ============");
	
	if(PointerAnalysis.TIMING)
	    System.out.println(counter + " component(s); " +
			       mmethods + " meta-method(s); " +
			       time + "ms processing time");
    }

    // prints method name and size information
    static void print_method_stats
	(MetaMethod mm, CachingSCCLBBFactory scc_lbb_factory) {
	HMethod hm = mm.getHMethod();

	int nb_sccs = 0;
	int nb_lbbs = 0;
	int nb_instrs = 0;

	for(SCComponent scc : scc_lbb_factory.computeSCCLBB(hm).decrOrder()) {
	    nb_sccs++;
	    nb_lbbs += scc.nodes().size();
	    for(Object lbb0 : scc.nodes())
		nb_instrs += ((LightBasicBlock) lbb0).getElements().length;
	}

	System.out.println("METHOD: " + hm);
	System.out.println("size: " +
			   nb_sccs + " SCCs; " + 
			   nb_lbbs + " LBBs; " + 
			   nb_instrs + " instrs");
    }

    static void print_lbb(LightBasicBlock lbb, Map lbb2passes) {
	Integer ipass = (Integer) lbb2passes.get(lbb);
	int pass = ((ipass == null) ? 0 : ipass.intValue()) + 1;
	lbb2passes.put(lbb, new Integer(pass));
	System.out.println
	    ("\nBEGIN: Analyze_basic_block " + lbb + " pass: " + pass);
	System.out.print("Prev BBs: ");
	Object[] prev_lbbs = lbb.getPrevLBBs();
	Arrays.sort(prev_lbbs, UComp.uc);
	for(int i = 0 ; i < prev_lbbs.length ; i++)
	    System.out.print((LightBasicBlock) prev_lbbs[i] + " ");
	System.out.println();
    }
}
