// IAStatistics.java, created Tue Apr 23 19:00:41 2002 by salcianu
// Copyright (C) 2000 Alexandru Salcianu <salcianu@MIT.EDU>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.MemOpt;

import java.util.Set;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.HashMap;
import java.util.Arrays;
import java.util.Collection;

import java.lang.Comparable;

import java.io.ObjectInputStream;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.BufferedReader;

import gnu.getopt.Getopt;

import harpoon.ClassFile.Linker;
import harpoon.ClassFile.HMethod;
import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HCode;
import harpoon.ClassFile.HCodeFactory;
import harpoon.ClassFile.CachingCodeFactory;
import harpoon.ClassFile.SerializableCodeFactory;

import harpoon.Util.Util;

import harpoon.Analysis.ClassHierarchy;
import harpoon.Analysis.Quads.QuadClassHierarchy;
import harpoon.Analysis.MetaMethods.SmartCallGraph;
import harpoon.Analysis.MetaMethods.MetaCallGraphImpl;
import harpoon.Analysis.Quads.CallGraph;
import harpoon.Analysis.PointerAnalysis.AllocationStatistics;
import harpoon.Analysis.PointerAnalysis.Debug;

import harpoon.Analysis.MemOpt.IncompatibilityAnalysis;

import harpoon.IR.Quads.Quad;
import harpoon.IR.Quads.QuadVisitor;
import harpoon.IR.Quads.QuadNoSSA;
import harpoon.IR.Quads.QuadSSI;
import harpoon.IR.Quads.NEW;
import harpoon.IR.Quads.ANEW;

/**
 * <code>IAStatistics</code>
 * 
 * @author  Alexandru Salcianu <salcianu@MIT.EDU>
 * @version $Id: IAStatistics.java,v 1.2 2002-12-02 17:07:51 salcianu Exp $
 */
public abstract class IAStatistics {
    
    private static class TypeStat implements Comparable {
	HClass hclass;
	public long[][] count = new long[2][2];

	TypeStat(HClass hclass) { this.hclass = hclass; }

	public String toString() {
	    return
		count[SITE][PREALLOC] + "\t" + count[SITE][HEAP] + "\t| " +
		count[OBJECT][PREALLOC] + "\t" + count[OBJECT][HEAP] + 
		"\t|| " + (hclass != null ? hclass.toString() : "");
	}

	public long totalAlloc(int kind) {
	    return count[kind][HEAP] + count[kind][PREALLOC];
	}

	public int compareTo(Object o) {
	    if(! (o instanceof TypeStat)) return -1;
	    TypeStat ts2 = (TypeStat) o;
	    return 
		new Long(totalAlloc(OBJECT))
		.compareTo(new Long(ts2.totalAlloc(OBJECT)));
	}
    }

    private static final int SITE     = 0;
    private static final int OBJECT   = 1;
    private static final int HEAP     = 0;
    private static final int PREALLOC = 1;

    private static TypeStat get_type_stat(HClass hclass) {
	TypeStat result = (TypeStat) hclass2stat.get(hclass);
	if(result == null)
	    hclass2stat.put(hclass, result = new TypeStat(hclass));
	return result;
    }
    private static Map hclass2stat = new HashMap();


    /** Prints static and dynamic statisticis about the memory
	preallocation optimization through incompatibility analysis.

       @param ia incompatibility analysis object that tells whether an
       allocation site is unitary (ie, can be preallocated) or not

       @param as allocation statistics object that provides the
       dynamic data: how many times each allocation site was
       executed

       @param allocs collection of all the allocation sites from the
       program; they must be produced by the caching QuadNoSSA factory
       that was used to obtain the QuadSSI factory for
       <code>ia</code>  */
    public static void printStatistics(IncompatibilityAnalysis ia,
				       AllocationStatistics as,
				       Collection allocs) {

	buildStatistics(ia, as, allocs);

	as.printStatistics(allocs, new MyVisitor(ia));

	TypeStat total = new TypeStat(null);

	Collection tss_coll = hclass2stat.values();
	TypeStat[] tss = 
	    (TypeStat[]) tss_coll.toArray(new TypeStat[tss_coll.size()]);
	Arrays.sort(tss);		    

	System.out.println("SITES\t\t|OBJECTS\t||");
	System.out.println("PREALLOC HEAP\t|PREALLOC HEAP\t||CLASS");
	System.out.println
	    ("------------------------------------------------------------");
	for(int index_ts = 0; index_ts < tss.length; index_ts++) {
	    TypeStat ts = tss[index_ts];
	    System.out.println(ts);
	    for(int i = 0; i < 2; i++)
		for(int j = 0; j < 2; j++)
		    total.count[i][j] += ts.count[i][j];
	}

	System.out.println
	    ("------------------------------------------------------------");
	System.out.println(total);

	System.out.println("TOTALS:");
	System.out.println
	    ("PREALLOCATED SITES:   " +
	     proportion(total.count[SITE][PREALLOC],
			total.count[SITE][HEAP], 5, 2));
	System.out.println
	    ("PREALLOCATED OBJECTS: " +
	     proportion(total.count[OBJECT][PREALLOC],
			total.count[OBJECT][HEAP], 5, 2));
    }


    private static class MyVisitor extends QuadVisitor {
	public MyVisitor(IncompatibilityAnalysis ia) { this.ia = ia; }

	private IncompatibilityAnalysis ia;

	public void visit(Quad quad) {
	    System.out.println
		(selfIncompatible(quad, ia) ? "\tnon-unitary" : "\tunitary");
	}
    }

    private static String proportion
	(long a, long b, int digits, int decimals) {
	long total = a + b;
	double frac = (a * 100.0) / (total + 0.0);
	return 
	    a + " \tout of " + total + " \t = " +
	    Debug.doubleRep(frac, digits, decimals) + "%";
    }

    private static void buildStatistics(IncompatibilityAnalysis ia,
					AllocationStatistics as,
					Collection allocs) {
	Set notInCache = new HashSet();

	for(Iterator it = allocs.iterator(); it.hasNext(); ) {
	    Quad q = (Quad) it.next();
	    HMethod hm = q.getFactory().getMethod();

	    if(!ia.allMethods().contains(hm)) {
		notInCache.add(hm);
		continue;
	    }

	    TypeStat ts = get_type_stat(allocatedClass(q));
	    if(selfIncompatible(q, ia)) {
		ts.count[OBJECT][HEAP] += as.getCount(q);
		ts.count[SITE][HEAP]++;
	    }
	    else {
		ts.count[OBJECT][PREALLOC] += as.getCount(q);
		ts.count[SITE][PREALLOC]++;
	    }
	}

	Util.print_collection
	    (notInCache, "Warning: some methods were not in the SSI cache");
    }

    private static HClass allocatedClass(Quad q) {
	if(q instanceof NEW) 
	    return ((NEW) q).hclass();
	else if(q instanceof ANEW)
	    return ((ANEW) q).hclass();
	else throw new IllegalArgumentException
		 ("Not an allocation " + Debug.code2str(q));
    }

    private static boolean selfIncompatible
	(Quad q, IncompatibilityAnalysis ia) {
	if(q instanceof ANEW) return true;
	assert q instanceof NEW;
	Quad qssi = ia.getSSIQuad(q);
	assert qssi != null : "null qssi";
	return ia.isSelfIncompatible(qssi);
    }
}
