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

import harpoon.Backend.Generic.Frame;

import harpoon.Util.Util;
import harpoon.Util.Timer;

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
import harpoon.IR.Quads.QuadWithTry;
import harpoon.IR.Quads.QuadNoSSA;
import harpoon.IR.Quads.QuadSSI;
import harpoon.IR.Quads.NEW;
import harpoon.IR.Quads.ANEW;

/**
 * <code>IAStatistics</code>
 * 
 * @author  Alexandru Salcianu <salcianu@MIT.EDU>
 * @version $Id: IAStatistics.java,v 1.4 2003-01-07 15:05:02 salcianu Exp $
 */
public abstract class IAStatistics {
    
    private static class TypeStat implements Comparable, Cloneable {
	public HClass hclass;
	public long[][] count = new long[2][2];

	public TypeStat(HClass hclass) { this.hclass = hclass; }

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

	public TypeStat add(TypeStat ts) {
	    for(int i = 0; i < 2; i++)
		for(int j = 0; j < 2; j++)
		    this.count[i][j] += ts.count[i][j];
	    return this;
	}

	public TypeStat diff(TypeStat ts) {
	    for(int i = 0; i < 2; i++)
		for(int j = 0; j < 2; j++)
		    this.count[i][j] -= ts.count[i][j];
	    return this;
	}

	public Object clone() {
	    try {
		TypeStat copy = (TypeStat) super.clone();
		copy.count = new long[2][2];
		for(int i = 0; i < 2; i++)
		    for(int j = 0; j < 2; j++)
			copy.count[i][j] = this.count[i][j];
		return copy;
	    }
	    catch(CloneNotSupportedException e) {
		e.printStackTrace();
		System.exit(1);
		return null; // never executed; makes compiler happy
	    }
	}

	public static TypeStat diff(TypeStat a, TypeStat b) {
	    return ((TypeStat) a.clone()).diff(b);
	}
    }

    private static final int SITE     = 0;
    private static final int OBJECT   = 1;
    private static final int HEAP     = 0;
    private static final int PREALLOC = 1;


    /** Prints static and dynamic statisticis about the memory
	preallocation optimization through incompatibility analysis.

       @param ia incompatibility analysis object that tells whether an
       allocation site is unitary (ie, can be preallocated) or not

       @param as allocation statistics object that provides the
       dynamic data: how many times each allocation site was
       executed

       @param hcf_nossa factory used to produce the SSI factory used
       by the <code>IncompatibilityAnalysis</code> <code>ia</code>.

       @param linker Linker to get the classes

       @param frame Frame used by the back-end.  Useful for computing
       the size of the objects at runtime.
    */
    public static void printStatistics(IncompatibilityAnalysis ia,
				       AllocationStatistics as,
				       HCodeFactory hcf_nossa,
				       Linker linker,
				       Frame frame) {

	if(frame != null)
	    ia.printStatistics(frame, linker);

	Set analyzedMethods = new HashSet(ia.allMethods());

	// stats on the time required by the NoSSA -> SSI conversion
	ssiConversionStatistics(analyzedMethods, hcf_nossa);
	// stats on NEWs introduced for exception explicit handling
	withTry2NoSSAConversionStatistics(analyzedMethods, hcf_nossa);

	Collection allocs = 
	    AllocationStatistics.getAllocs(analyzedMethods, hcf_nossa);

	// stats on most executed allocation sites
	as.printStatistics(allocs, new MyVisitor(ia));

	TypeStat[] tss = buildStatistics(ia, as, allocs);

	HClass hThrowable = linker.forName("java.lang.Throwable");
	TypeStat total = new TypeStat(null);
	TypeStat total_arrays = new TypeStat(null);
	TypeStat total_throwables = new TypeStat(null);
	TypeStat total_program = new TypeStat(null);

	System.out.println("SITES\t\t|OBJECTS\t||");
	System.out.println("PREALLOC HEAP\t|PREALLOC HEAP\t||CLASS");
	System.out.println
	    ("------------------------------------------------------------");
	for(int index_ts = 0; index_ts < tss.length; index_ts++) {
	    TypeStat ts = tss[index_ts];
	    System.out.println(ts);
	    total.add(ts);
	    if(ts.hclass.isArray())
		total_arrays.add(ts);
	    if(ts.hclass.isInstanceOf(hThrowable))
		total_throwables.add(ts);
	}

	System.out.println
	    ("------------------------------------------------------------");
	System.out.println(total);

	printTotal("OVERALL",        total);
	printTotal("ARRAYS",         total_arrays);
	printTotal("NON-ARRAYS",     TypeStat.diff(total, total_arrays));
	printTotal("THROWABLES",     total_throwables);
	printTotal("NON_THROWABLES", TypeStat.diff(total, total_throwables));
	
	System.exit(1);
    }


    // prints statistics on how many allocation sites were introduced
    // by the QuadWithTry -> QuadNoSSA conversion (quite MANY!)
    private static void withTry2NoSSAConversionStatistics
	(Set methods, HCodeFactory hcf_nossa) {
	Collection allocs = AllocationStatistics.getAllocs(methods, hcf_nossa);
	Collection initial_allocs = 
	    AllocationStatistics.getAllocs(methods, QuadWithTry.codeFactory());
	
	long delta_sites = allocs.size() - initial_allocs.size();
	
	System.out.println
	    (proportion(delta_sites, initial_allocs.size(), 5, 2) +  
	     " allocations introduced by QuadWithTry -> QuadNoSSA");
    }


    private static void printTotal(String label, TypeStat total) {
	System.out.println
	    (label + " PREALLOCATED SITES:\t" +
	     proportion(total.count[SITE][PREALLOC],
			total.count[SITE][HEAP], 5, 2));
	System.out.println
	    (label + " PREALLOCATED OBJECTS:\t" +
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

    static String proportion
	(long a, long b, int digits, int decimals) {
	long total = a + b;
	double frac = (a * 100.0) / (total + 0.0);
	return 
	    a + "\tout of\t" + total + "\t = " +
	    Debug.doubleRep(frac, digits, decimals) + "%";
    }

    private static TypeStat get_type_stat(Map hclass2stat, HClass hclass) {
	TypeStat result = (TypeStat) hclass2stat.get(hclass);
	if(result == null)
	    hclass2stat.put(hclass, result = new TypeStat(hclass));
	return result;
    }

    private static TypeStat[] buildStatistics
	(IncompatibilityAnalysis ia, AllocationStatistics as,
	 Collection allocs) {
	Map hclass2stat = new HashMap();
	
	for(Iterator it = allocs.iterator(); it.hasNext(); ) {
	    Quad q = (Quad) it.next();

	    /*
	    HMethod hm = q.getFactory().getMethod();

	    // methods called only by the startup code are not analyzed;
	    // (they are not transitively called from the entry method)
	    // hence, we ignore all the allocation sites from there
	    if(!ia.allMethods().contains(hm))
		continue;
	    */

	    TypeStat ts = get_type_stat(hclass2stat, allocatedClass(q));
	    if(selfIncompatible(q, ia) ||
	       !PreallocAllocationStrategy.extraCond(q, allocatedClass(q))) {
		ts.count[OBJECT][HEAP] += as.getCount(q);
		ts.count[SITE][HEAP]++;
	    }
	    else {
		ts.count[OBJECT][PREALLOC] += as.getCount(q);
		ts.count[SITE][PREALLOC]++;
	    }
	}

	Collection tss_coll = hclass2stat.values();
	TypeStat[] tss = 
	    (TypeStat[]) tss_coll.toArray(new TypeStat[tss_coll.size()]);
	Arrays.sort(tss);		    

	return tss;
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
	return 
	    ia.isSelfIncompatible(qssi);
    }


    // prints statistics regarding the noSSA -> SSI conversion time
    // for the set of analyzed methods (not all methods; we want to
    // compare it with the analysis time)
    private static void ssiConversionStatistics(Collection methods,
						HCodeFactory hcf_nossa) {
	Timer timer = new Timer();
	timer.start();
	HCodeFactory hcf_ssi = QuadSSI.codeFactory(hcf_nossa);
	for(Iterator it = methods.iterator(); it.hasNext(); )
	    hcf_ssi.convert((HMethod) it.next());
	timer.stop();
	System.out.println("SSI conversion time: " + timer);
    }
}
