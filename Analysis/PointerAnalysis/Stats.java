// Stats.java, created Tue Feb 29 11:55:35 2000 by salcianu
// Copyright (C) 2000 Alexandru SALCIANU <salcianu@MIT.EDU>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.PointerAnalysis;


import java.util.List;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Arrays;
import java.util.Set;
import java.util.HashSet;
import java.util.Comparator;

import harpoon.ClassFile.HMethod;
import harpoon.ClassFile.HCode;
import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HCodeFactory;

import harpoon.Util.LightBasicBlocks.LightBasicBlock;

import harpoon.Analysis.MetaMethods.MetaMethod;
import harpoon.Util.Graphs.SCComponent;

/**
 * <code>Stats</code> centralizes some pointer-analysis related statistics.
 * 
 * @author  Alexandru SALCIANU <salcianu@MIT.EDU>
 * @version $Id: Stats.java,v 1.1.2.15 2001-03-04 17:00:43 salcianu Exp $
 */
abstract class Stats implements java.io.Serializable {

    private static HashMap info = new HashMap();
    
    static class MetaMethodInfo{
	int nb_instrs = 0;
	int nb_bbs = 0;
	int nb_sccs = 0;
	int nb_passes = 0;
	int nb_params = 0;
    }

    private static MetaMethodInfo getMetaMethodInfo(MetaMethod mm){
	MetaMethodInfo mmi = (MetaMethodInfo) info.get(mm);
	if(mmi == null){
	    mmi = new MetaMethodInfo();
	    info.put(mm,mmi);
	}
	return mmi;
    }

    public static void record_mmethod(MetaMethod mm, SCComponent scc){
	MetaMethodInfo mmi = getMetaMethodInfo(mm);

	// grab statistics about the number of SCC and BB
	int nb_sccs = 0;
	int nb_bbs  = 0;
	int nb_instrs = 0;
	while(scc != null){
	    nb_sccs++;
	    nb_bbs += scc.nodeSet().size();
	    for(Iterator it_bbs = scc.nodeSet().iterator(); it_bbs.hasNext();){
		LightBasicBlock bbk = (LightBasicBlock) it_bbs.next();
		nb_instrs += bbk.getElements().length;
	    }
	    scc = scc.nextTopSort();
	}
	Stats.record_mmethod_instrs(mm, nb_instrs);
	Stats.record_mmethod_bbs(mm, nb_bbs);
	Stats.record_mmethod_sccs(mm, nb_sccs);
    }

    public static void record_mmethod_instrs(MetaMethod mm, int nb_instrs){
	MetaMethodInfo mmi = getMetaMethodInfo(mm);
	mmi.nb_instrs = nb_instrs;
    }

    public static void record_mmethod_bbs(MetaMethod mm, int nb_bbs){
	MetaMethodInfo mmi = getMetaMethodInfo(mm);
	mmi.nb_bbs = nb_bbs;
    }

    public static void record_mmethod_sccs(MetaMethod mm, int nb_sccs){
	MetaMethodInfo mmi = getMetaMethodInfo(mm);
	mmi.nb_sccs = nb_sccs;
    }
    
    public static void record_mmethod_pass(MetaMethod mm){
	MetaMethodInfo mmi = getMetaMethodInfo(mm);
	mmi.nb_passes++;
    }

    public static void record_mmethod_params(MetaMethod mm, int nb_params){
	MetaMethodInfo mmi = getMetaMethodInfo(mm);
	mmi.nb_params = nb_params;
    }

    private static int maxim_nb_instrs = -1;
    private static int maxim_nb_passes = -1;
    private static int maxim_nb_params = -1; // the maximum nb of parameters
    
    public static void print_stats(){
	System.out.println("STATISTICS:");

	int nb_mmethods = info.size();
	System.out.println(nb_mmethods + " meta-method(s)");

	int total_instrs = 0;
	int total_bbs = 0;
	int total_sccs = 0;
	int total_passes = 0;
	int total_params = 0; // the total number of parameters
	// the set of all the analyzed methods (a method can be split
	// into many metamethods)
	Set methods = new HashSet();

	maxim_nb_instrs = -1;
	maxim_nb_passes = -1;
	maxim_nb_params = -1;

	for(Iterator it = info.keySet().iterator(); it.hasNext(); ){
	    MetaMethod mm = (MetaMethod) it.next();
	    MetaMethodInfo mmi = (MetaMethodInfo) info.get(mm);

	    methods.add(mm.getHMethod());

	    mmi.nb_params = compute_nb_params(mm.getHMethod());

	    System.out.println(mm);
	    System.out.println(" " + mmi.nb_instrs + " instrs\t" +
			       mmi.nb_params + " obj param(s)\t" +
			       mmi.nb_passes + " pass(es)\t" + 
			       mmi.nb_bbs + " BBs\t" +
			       mmi.nb_sccs + " SCCs\t"); 

	    total_params += mmi.nb_params;
	    total_instrs += mmi.nb_instrs;
	    total_bbs    += mmi.nb_bbs;
	    total_sccs   += mmi.nb_sccs;
	    total_passes += mmi.nb_passes;

	    if(mmi.nb_instrs > maxim_nb_instrs)
		maxim_nb_instrs = mmi.nb_instrs;
	    if(mmi.nb_passes > maxim_nb_passes)
		maxim_nb_passes = mmi.nb_passes;
	    if(mmi.nb_params > maxim_nb_params)
		maxim_nb_params = mmi.nb_params;
	}

	// show some statistics about the size (in bytecode instrs) 
	// of all the analyzed methods.
	int total_b_instrs = print_bytecode_size(methods);

	System.out.println("--TOTALS----------------------------------");
	System.out.println("Nb. of analyzed meta-methods : " + nb_mmethods);
	System.out.println("Total nb. of IR instrs       : " + total_instrs);
	System.out.println("Total nb. of Bytecode instrs : " + total_b_instrs);
	System.out.println("Total nb. of BBs             : " + total_bbs);
	System.out.println("Total nb. of SCCs            : " + total_sccs);

	System.out.println("--AVERAGES--------------------------------");
	double appmm = (double)total_passes/(double)nb_mmethods;
	System.out.println("Average Passes/Meta-Method      : " +
			   Debug.doubleRep(appmm, 6, 2));
	double aipmm = (double)total_instrs/(double)nb_mmethods;
	System.out.println("Average Instrs/Meta-Method      : " +
			   Debug.doubleRep(aipmm, 6, 2));
	double abbpscc = (double)total_bbs/(double)total_sccs;
	System.out.println("Average BB(s)/SCC               : " + 
			   Debug.doubleRep(abbpscc, 6, 2));
	double aipbb = (double)total_instrs/(double)total_bbs;
	System.out.println("Average Instrs/BB               : " + 
			   Debug.doubleRep(aipbb, 6, 2));
	double aoppmm = (double)total_params/(double)nb_mmethods;
	System.out.println("Average Obj. Params/Meta-Method : " + 
			   Debug.doubleRep(aoppmm, 6, 2));

	System.out.println("--EXTREMES--------------------------------");
	System.out.println("Biggest nb of obj. params : " + maxim_nb_params);
	System.out.println("Maximum meta-method size  : " + maxim_nb_instrs +
			   " quad(s)");
	System.out.println("Maximum nb of pass(es) over a meta-method : " + 
			   maxim_nb_passes);

	print_extremes_examples();
	
	System.out.println("--PARAMS----------------------------------");

	int[] mwp = new int[maxim_nb_params + 1];
	for(int i = 0; i < maxim_nb_params; i++) mwp[i] = 0;

	for(Iterator it = info.keySet().iterator(); it.hasNext(); ){
	    MetaMethod mm = (MetaMethod) it.next();
	    MetaMethodInfo mmi = (MetaMethodInfo) info.get(mm);
	    mwp[mmi.nb_params]++;
	}

	for(int i=0;i<mwp.length;i++) 
	    if(mwp[i]!=0) System.out.println("Meta-Methods with " + i + 
					     " param(s): " + mwp[i]);
    }


    // display the size of the analyzed methods (in bytecode) along with
    // the total size
    private static final int print_bytecode_size(Set methods) {
	HCodeFactory bcf = harpoon.IR.Bytecode.Code.codeFactory();
	int total = 0;
	Set cls = new HashSet();
	System.out.println("ANALYZED METHODS SIZE (IN BYTECODE INSTRS)");
	for(Iterator it = methods.iterator(); it.hasNext(); ) {
	    HMethod hm = (HMethod) it.next();
	    HCode hcode = bcf.convert(hm);
	    if(hcode == null) {
		System.out.println("WARNING: " + hm + " has no HCode");
		continue;
	    }
	    int bsize = hcode.getElementsL().size();
	    total += bsize;
	    System.out.println(hm + " " + bsize + " bytecode instrs");
	    cls.add(hm.getDeclaringClass());
	}
	System.out.println("TOTAL: " + total + " bytecode instrs");
	System.out.println();

	System.out.println("ANALYZED CLASSES:");
	for(Iterator it = cls.iterator(); it.hasNext(); ) {
	    HClass hclass = (HClass) it.next();
	    System.out.println("CLS " + hclass.getName());
	    StringBuffer buff = new StringBuffer(hclass.getPackage());
	    for(int i = 0; i < buff.length(); i++)
		if(buff.charAt(i) == '.')
		    buff.setCharAt(i, '/');
	    if(buff.length() == 0) buff.append(".");

	    System.out.println("SRC " + buff + "/" + hclass.getSourceFile());
	}
	return total;
    }

    // computes the total number of parameters of the method hm;
    // it counts even the this parameter (for the non-static methods)
    private static final int compute_nb_params(HMethod hm){
	return hm.getParameterTypes().length + (hm.isStatic()?0:1);
    }

    private static class MethodInfo{
	HMethod hm;
	int nbi;
	MethodInfo(HMethod hm, int nbi){
	    this.hm  = hm;
	    this.nbi = nbi;
	}
	public boolean equals(Object o){
	    MethodInfo mi2 = (MethodInfo) o;
	    return hm.equals(mi2.hm) && (nbi == mi2.nbi);
	}
	public int hashCode(){
	    return hm.hashCode() + nbi;
	}
    };

    private static final void print_extremes_examples(){
	System.out.println("--EXTREMES-2-(EXAMPLES)-------------------");
	System.out.println("Biggest method(s) :");
	// 1. select the biggest method(s)
	Set big_procs = new HashSet();
	for(Iterator it = info.keySet().iterator(); it.hasNext(); ){
	    MetaMethod mm = (MetaMethod) it.next();
	    MetaMethodInfo mmi = (MetaMethodInfo) info.get(mm);
	    if(mmi.nb_instrs >= (int) (0.5 * maxim_nb_instrs))
		big_procs.add(new MethodInfo(mm.getHMethod(),mmi.nb_instrs));
	}
	// 2. sort them
	MethodInfo[] big_procs_array = (MethodInfo[])
	    big_procs.toArray(new MethodInfo[big_procs.size()]);
	Arrays.sort(big_procs_array,new Comparator(){
		public int compare(Object o1,Object o2){
		    MethodInfo mi1 = (MethodInfo) o1;
		    MethodInfo mi2 = (MethodInfo) o2;
		    if(mi1.nbi < mi2.nbi) return +1;
		    if(mi1.nbi > mi2.nbi) return -1;
		    return 0;
		}
		public boolean equals(Object obj){
		    return this == obj;
		}
	    });
	// 3. print them
	for(int i = 0; i < big_procs_array.length; i++){
	    MethodInfo mi = big_procs_array[i];
	    System.out.println("  " + mi.hm + " (" + mi.nbi + ")");
	}
	
	System.out.println("Most visited meta-method(s) :");
	// 1. select the most visited methods
	Set most_visited = new HashSet();
	for(Iterator it = info.keySet().iterator(); it.hasNext(); ){
	    MetaMethod mm = (MetaMethod) it.next();
	    MetaMethodInfo mmi = (MetaMethodInfo) info.get(mm);
	    if(mmi.nb_passes >= (int) (0.5 * maxim_nb_passes))
		most_visited.add(mm);
	}
	// 2. sort them
	MetaMethod[] most_visited_array = (MetaMethod[])
	    most_visited.toArray(new MetaMethod[most_visited.size()]);
	Arrays.sort(most_visited_array,new Comparator(){
		public int compare(Object o1,Object o2){
		    MetaMethodInfo mmi1 = 
			(MetaMethodInfo) info.get((MetaMethod) o1);
		    MetaMethodInfo mmi2 = 
			(MetaMethodInfo) info.get((MetaMethod) o2);
		    if(mmi1.nb_passes < mmi2.nb_passes) return +1;
		    if(mmi1.nb_passes > mmi2.nb_passes) return -1;
		    return 0;
		}
		public boolean equals(Object obj){
		    return this == obj;
		}
	    });
	// 3. print them
	for(int i = 0; i < most_visited_array.length; i++){
	    MetaMethod mm = most_visited_array[i];
	    MetaMethodInfo mmi = (MetaMethodInfo) info.get(mm);
	    System.out.println("  " + mm + " (" + mmi.nb_passes + ")");
	}
    }

}
