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


/**
 * <code>Stats</code> centralizes some pointer-analysis related statistics.
 * 
 * @author  Alexandru SALCIANU <salcianu@MIT.EDU>
 * @version $Id: Stats.java,v 1.1.2.3 2000-03-08 00:36:02 salcianu Exp $
 */
abstract class Stats {

    private static HashMap info = new HashMap();
    
    static class MethodInfo{
	int nb_instrs = 0;
	int nb_bbs = 0;
	int nb_sccs = 0;
	int nb_passes = 0;
	int nb_params = 0;
    };

    private static MethodInfo getMethodInfo(HMethod hm){
	MethodInfo mi = (MethodInfo) info.get(hm);
	if(mi == null){
	    mi = new MethodInfo();
	    info.put(hm,mi);
	}
	return mi;
    }

    public static void record_method_hcode(HMethod hm, HCode hcode){
	MethodInfo mi = getMethodInfo(hm);
	mi.nb_instrs = hcode.getElementsL().size();
    }

    public static void record_method_bbs(HMethod hm, int nb_bbs){
	MethodInfo mi = getMethodInfo(hm);
	mi.nb_bbs = nb_bbs;
    }

    public static void record_method_sccs(HMethod hm, int nb_sccs){
	MethodInfo mi = getMethodInfo(hm);
	mi.nb_sccs = nb_sccs;
    }
    
    public static void record_method_pass(HMethod hm){
	MethodInfo mi = getMethodInfo(hm);
	mi.nb_passes++;
    }

    public static void record_method_params(HMethod hm, int nb_params){
	MethodInfo mi = getMethodInfo(hm);
	mi.nb_params = nb_params;
    }

    private static int maxim_nb_instrs = -1;
    private static int maxim_nb_passes = -1;
    private static int maxim_nb_params = -1; // the maximum nb of parameters
    
    public static void print_stats(){
	System.out.println("STATISTICS:");

	int nb_methods = info.size();
	System.out.println(nb_methods + " method(s)");

	int total_instrs = 0;
	int total_bbs = 0;
	int total_sccs = 0;
	int total_passes = 0;
	int total_params = 0; // the total number of parameters

	maxim_nb_instrs = -1;
	maxim_nb_passes = -1;
	maxim_nb_params = -1;

	Iterator it = info.keySet().iterator();
	while(it.hasNext()){
	    HMethod hm = (HMethod) it.next();
	    MethodInfo mi = (MethodInfo) info.get(hm);

	    mi.nb_params = compute_nb_params(hm);

	    System.out.println(hm);
	    System.out.println(" " + mi.nb_instrs + " instrs\t" +
			       mi.nb_params + " obj param(s)\t" +
			       mi.nb_passes + " pass(es)\t" + 
			       mi.nb_bbs + " BBs\t" + mi.nb_sccs + " SCCs\t"); 

	    total_params += mi.nb_params;
	    total_instrs += mi.nb_instrs;
	    total_bbs    += mi.nb_bbs;
	    total_sccs   += mi.nb_sccs;
	    total_passes += mi.nb_passes;

	    if(mi.nb_instrs > maxim_nb_instrs)
		maxim_nb_instrs = mi.nb_instrs;
	    if(mi.nb_passes > maxim_nb_passes)
		maxim_nb_passes = mi.nb_passes;
	    if(mi.nb_params > maxim_nb_params)
		maxim_nb_params = mi.nb_params;
	}

	System.out.println("--TOTALS----------------------------------");
	System.out.println("Nb. of analyzed methods    : " + nb_methods);
	System.out.println("Total nb. of instructions  : " + total_instrs);
	System.out.println("Total nb. of BBs           : " + total_bbs);
	System.out.println("Total nb. of SCCs          : " + total_sccs);

	System.out.println("--AVERAGES--------------------------------");
	System.out.println("Average Passes/Method      : " +
			   (double)total_passes/(double)nb_methods);
	System.out.println("Average Instrs/Method      : " +
			   (double)total_instrs/(double)nb_methods);
	System.out.println("Average BB(s)/SCC          : " + 
			   (double)total_bbs/(double)total_sccs);
	System.out.println("Average Instrs/BB          : " + 
			   (double)total_instrs/(double)total_bbs);
	System.out.println("Average Obj. Params/Method : " + 
			   (double)total_params/(double)nb_methods);

	System.out.println("--EXTREMES--------------------------------");
	System.out.println("Biggest nb of obj. params : " +
			   maxim_nb_params);
	System.out.println("Maximum method size       : " + maxim_nb_instrs +
			   " quad(s)");
	System.out.println("Maximum nb of pass(es) over a method : " + 
			   maxim_nb_passes);

	print_extremes_examples();
	
	System.out.println("--PARAMS----------------------------------");

	int[] mwp = new int[maxim_nb_params + 1];
	for(int i=0;i<maxim_nb_params;i++) mwp[i]=0;

	it = info.keySet().iterator();
	while(it.hasNext()){
	    HMethod hm = (HMethod) it.next();
	    MethodInfo mi = (MethodInfo) info.get(hm);
	    mwp[mi.nb_params]++;
	}

	for(int i=0;i<mwp.length;i++)
	    if(mwp[i]!=0)
		System.out.println("Methods with " + i + " param(s): " +
				   mwp[i]);

    }

    // computes the total number of parameters of the method hm;
    // it counts even the this parameter (for the non-static methods)
    private static final int compute_nb_params(HMethod hm){
	return hm.getParameterTypes().length + (hm.isStatic()?0:1);
    }


    private static final void print_extremes_examples(){
	System.out.println("--EXTREMES-2-(EXAMPLES)-------------------");
	System.out.println("Biggest procedure(s) :");
	// 1. select the biggest procs
	Set big_procs = new HashSet();
	Iterator it = info.keySet().iterator();
	while(it.hasNext()){
	    HMethod hm = (HMethod) it.next();
	    MethodInfo mi = (MethodInfo) info.get(hm);
	    if(mi.nb_instrs >= (int) (0.5 * maxim_nb_instrs))
		big_procs.add(hm);
	}
	// 2. sort them
	HMethod[] big_procs_array = 
	    (HMethod[])big_procs.toArray(new HMethod[big_procs.size()]);
	Arrays.sort(big_procs_array,new Comparator(){
		public int compare(Object o1,Object o2){
		    MethodInfo mi1 = (MethodInfo) info.get((HMethod) o1);
		    MethodInfo mi2 = (MethodInfo) info.get((HMethod) o2);
		    if(mi1.nb_instrs < mi2.nb_instrs) return -1;
		    if(mi1.nb_instrs > mi2.nb_instrs) return -1;
		    return 0;
		}
		public boolean equals(Object obj){
		    return this == obj;
		}
	    });
	// 3. print them
	for(int i=0;i<big_procs_array.length;i++){
	    HMethod hm = big_procs_array[i];
	    MethodInfo mi = (MethodInfo) info.get(hm);
	    System.out.println("  " + hm + " (" + mi.nb_instrs + ")");
	}
	big_procs = null;
	
	System.out.println("Most visited method(s) :");
	// 1. select the most visited methods
	Set most_visited = new HashSet();
	it = info.keySet().iterator();
	while(it.hasNext()){
	    HMethod hm = (HMethod) it.next();
	    MethodInfo mi = (MethodInfo) info.get(hm);
	    if(mi.nb_passes >= (int) (0.5 * maxim_nb_passes))
		most_visited.add(hm);
	}
	// 2. sort them
	HMethod[] most_visited_array = 
	    (HMethod[])most_visited.toArray(new HMethod[most_visited.size()]);
	Arrays.sort(most_visited_array,new Comparator(){
		public int compare(Object o1,Object o2){
		    MethodInfo mi1 = (MethodInfo) info.get((HMethod) o1);
		    MethodInfo mi2 = (MethodInfo) info.get((HMethod) o2);
		    if(mi1.nb_passes < mi2.nb_passes) return -1;
		    if(mi1.nb_passes > mi2.nb_passes) return -1;
		    return 0;
		}
		public boolean equals(Object obj){
		    return this == obj;
		}
	    });
	// 3. print them
	for(int i=0;i<most_visited_array.length;i++){
	    HMethod hm = most_visited_array[i];
	    MethodInfo mi = (MethodInfo) info.get(hm);
	    System.out.println("  " + hm + " (" + mi.nb_passes + ")");
	}
    }

}
