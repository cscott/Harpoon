// Stats.java, created Wed Sep 21 09:51:40 2005 by salcianu
// Copyright (C) 2003 Alexandru Salcianu <salcianu@alum.mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.PA2;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.LinkedList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Set;
import java.util.HashSet;

import jpaul.Graphs.SCComponent;
import jpaul.DataStructs.ReverseListView;

import harpoon.ClassFile.HMethod;
import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HCodeFactory;

/**
 * <code>Stats</code>
 * 
 * @author  Alexandru Salcianu <salcianu@alum.mit.edu>
 * @version $Id: Stats.java,v 1.1 2005-09-21 19:33:42 salcianu Exp $
 */
public class Stats {

    private static Map<HMethod, Long> hm2time = 
	new HashMap<HMethod,Long>();
    private static Map<HMethod, Long> hm2tcg = 
	new HashMap<HMethod,Long>();
    private static Map<HMethod, Integer> hm2passes =
	new HashMap<HMethod,Integer>();

    private static Map<SCComponent<HMethod>, Long> scc2time = 
	new HashMap<SCComponent<HMethod>, Long>();

    static void recordMethodAnalysis(HMethod hm, long delta) {
	Long time = hm2time.get(hm);
	hm2time.put(hm, new Long(((time == null) ? 0 : time.longValue()) + delta));
	Integer count = hm2passes.get(hm);
	hm2passes.put(hm, new Integer((count == null) ? 1 : (count.intValue() + 1)));
    }

    static void recordMethodConsGen(HMethod hm, long delta) {
	Long time = hm2tcg.get(hm);
	hm2tcg.put(hm, new Long(((time == null) ? 0 : time.longValue()) + delta));
    }

    static void recordSCCAnalysis(SCComponent<HMethod> scc, long time) {
	scc2time.put(scc, new Long(time));
    }


    public static List<HMethod> analyzedMethods() {
	LinkedList<HMethod> methods = new LinkedList<HMethod>(hm2time.keySet());
	Collections.<HMethod>sort
	    (methods, Stats.<HMethod,Long>getComp(hm2time));
	return methods;
    }


    public static List<SCComponent<HMethod>> analyzedSCCs() {
	LinkedList<SCComponent<HMethod>> sccs = 
	    new LinkedList<SCComponent<HMethod>>(scc2time.keySet());
	Collections.<SCComponent<HMethod>>sort
	    (sccs, Stats.<SCComponent<HMethod>,Long>getComp(scc2time));
	return sccs;
    }

    private static <T1,T2 extends Comparable<T2>> 
	Comparator<T1> getComp(final Map<T1,T2> map) {

	return new Comparator<T1>() {
	    public int compare(T1 a, T1 b) {
		T2 val_a = map.get(a);
		assert val_a != null;
		T2 val_b = map.get(b);
		assert val_b != null;
		return val_a.compareTo(val_b);
	    };
	    public boolean equals(Object obj) {
		// equal only to itself
		return this == obj;
	    }
	};
    }


    public static void clear() {
	hm2time.clear();
	hm2tcg.clear();
	hm2passes.clear();
	scc2time.clear();
    }

    public static void printStats(int maxMethods, int maxSCCs) {
	java.io.PrintStream out = System.out;

	out.println("\n---STATS-----------------------");

	List<HMethod> methods = new ReverseListView<HMethod>(analyzedMethods());

	HCodeFactory bcf = harpoon.IR.Bytecode.Code.codeFactory();
	HCodeFactory hcf = 
	    harpoon.IR.Quads.QuadRSSx.codeFactory
	    (harpoon.IR.Quads.QuadSSA.codeFactory());
	Map<HMethod,Integer> hm2sizeBytecode = new HashMap<HMethod,Integer>();
	Map<HMethod,Integer> hm2sizeWithTry  = new HashMap<HMethod,Integer>();
	Set<HClass> classes = new HashSet<HClass>();
	int totalBytecodeSize = 0;
	int totalQuadSize = 0;
	for(HMethod hm : methods) {
	    classes.add(hm.getDeclaringClass());
	    int bytecodeSize = bcf.convert(hm).getElements().length;
	    int quadSize = hcf.convert(hm).getElements().length;
	    hm2sizeBytecode.put(hm, new Integer(bytecodeSize));
	    hm2sizeWithTry.put(hm, new Integer(quadSize));
	    totalBytecodeSize += bytecodeSize;
	    totalQuadSize += quadSize;
	}

	out.println(methods.size() + " analyzed method(s); " + 
		    classes.size() + " class(es); " + 
		    totalBytecodeSize + " bytecode(s);\t" +
		    totalQuadSize + " quad(s)");
	int count = 0;
	for(HMethod hm : methods) {
	    out.println(hm2sizeBytecode.get(hm) + " bytecodes;\t" + 
			hm2sizeWithTry.get(hm) + " quads;\t" + 
			hm2time.get(hm) + " / " + hm2tcg.get(hm) + " ms;\t" + 
			hm2passes.get(hm) + " passes:\n\t" + hm);
	    count++;
	    if(count >= maxMethods) break;
	}

	out.println("-------------------------------");
	List<SCComponent<HMethod>> sccs = new ReverseListView<SCComponent<HMethod>>(analyzedSCCs());
	out.println(sccs.size() + " analyzed SCC(s) of methods");
	count = 0;
	for(SCComponent<HMethod> scc : sccs) {
	    out.println(scc2time.get(scc) + " ms: ");
	    PAUtil.printMethodSCC(out, scc);
	    count++;
	    if(count >= maxSCCs) break;
	}

	out.println("-------------------------------\n");
    }


}
