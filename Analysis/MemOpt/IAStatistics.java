// IAStatistics.java, created Tue Apr 23 19:00:41 2002 by salcianu
// Copyright (C) 2000 Alexandru Salcianu <salcianu@MIT.EDU>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.MemOpt;

import java.util.Set;
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

import harpoon.Analysis.ClassHierarchy;
import harpoon.Analysis.Quads.QuadClassHierarchy;
import harpoon.Analysis.MetaMethods.SmartCallGraph;
import harpoon.Analysis.MetaMethods.MetaCallGraphImpl;
import harpoon.Analysis.Quads.CallGraph;
import harpoon.Analysis.PointerAnalysis.AllocationStatistics;
import harpoon.Analysis.PointerAnalysis.Debug;

import harpoon.Analysis.MemOpt.IncompatibilityAnalysis;

import harpoon.IR.Quads.Quad;
import harpoon.IR.Quads.QuadNoSSA;
import harpoon.IR.Quads.QuadSSI;
import harpoon.IR.Quads.NEW;
import harpoon.IR.Quads.ANEW;

/**
 * <code>IAStatistics</code>
 * 
 * @author  Alexandru Salcianu <salcianu@MIT.EDU>
 * @version $Id: IAStatistics.java,v 1.1 2002-12-01 06:29:24 salcianu Exp $
 */
public abstract class IAStatistics {
    
    private static class TypeStat {
	HClass hclass;
	public long[][] count = new long[2][2];

	TypeStat(HClass hclass) { this.hclass = hclass; }

	public String toString() {
	    return
		count[SITE][HEAP] + " " + count[SITE][PREALLOC] + " | " +
		count[OBJECT][HEAP] + " " + count[OBJECT][PREALLOC] + " || " +
		hclass;
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

    public static void printStatistics(IncompatibilityAnalysis ia,
				       AllocationStatistics as,
				       Collection allocs) {

	buildStatistics(ia, as, allocs);

	long[][] total_count = new long[2][2];

	for(Iterator it = hclass2stat.values().iterator(); it.hasNext(); ) {
	    TypeStat ts = (TypeStat) it.next();
	    System.out.println((TypeStat) it.next());
	    for(int i = 0; i < 2; i++)
		for(int j = 0; j < 2; j++)
		    total_count[i][j] += ts.count[i][j];
	}

	System.out.println("TOTALS:");
	System.out.println
	    ("SITES:   PREALLOC: " + total_count[SITE][PREALLOC] + " (" +
	     proportion(total_count[SITE][PREALLOC],
			total_count[SITE][HEAP], 5, 2));
	System.out.println
	    ("OBJECTS: PREALLOC: " + total_count[OBJECT][PREALLOC] + " (" +
	     proportion(total_count[OBJECT][PREALLOC],
			total_count[OBJECT][HEAP], 5, 2));
    }

    private static String proportion
	(long a, long b, int digits, int decimals) {
	double frac = (a + 0.0) / (b + 0.0);
	return Debug.doubleRep(frac, digits, decimals);
    }

    private static void buildStatistics(IncompatibilityAnalysis ia,
					AllocationStatistics as,
					Collection allocs) {
	
	for(Iterator it = allocs.iterator(); it.hasNext(); ) {
	    Quad q = (Quad) it.next();
	    HMethod hm = q.getFactory().getMethod();

	    if(!ia.allMethods().contains(hm)) {
		System.out.println("Warn: " + hm + " not in the ssi cache!");
		continue;
	    }

	    TypeStat ts = get_type_stat(allocatedClass(q));
	    if(selfIncompatible(q, ia)) {
		ts.count[OBJECT][HEAP] += as.getCount(q);
		ts.count[SITE][HEAP]++;
	    }
	    else {
		ts.count[OBJECT][PREALLOC] += as.getCount(q);
		ts.count[SITE][HEAP]++;
	    }
	}
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
	Quad qssi = ia.getSSIQuad(q);
	assert qssi != null : "null qssi";
	return ia.isSelfIncompatible(qssi);
    }

}
