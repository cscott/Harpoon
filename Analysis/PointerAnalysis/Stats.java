// Stats.java, created Tue Feb 29 11:55:35 2000 by salcianu
// Copyright (C) 2000 Alexandru SALCIANU <salcianu@MIT.EDU>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.PointerAnalysis;


import java.util.List;
import java.util.HashMap;
import java.util.Iterator;

import harpoon.ClassFile.HMethod;
import harpoon.ClassFile.HCode;


/**
 * <code>Stats</code> centralizes some pointer-analysis related statistics.
 * 
 * @author  Alexandru SALCIANU <salcianu@MIT.EDU>
 * @version $Id: Stats.java,v 1.1.2.2 2000-03-02 22:55:40 salcianu Exp $
 */
abstract class Stats {

    private static HashMap info = new HashMap();
    
    static class MethodInfo{
	int nb_instrs = 0;
	int nb_bbs = 0;
	int nb_sccs = 0;
	int nb_passes = 0;
    };

    private static MethodInfo getMethodInfo(HMethod hm){
	MethodInfo mi = (MethodInfo) info.get(hm);
	if(mi == null)
	    info.put(hm,mi=new MethodInfo());
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
    
    public static void print_stats(){
	System.out.println("STATISTICS:");

	int nb_methods = info.size();
	System.out.println(nb_methods + " method(s)");

	int total_instrs = 0;
	int total_bbs = 0;
	int total_sccs = 0;
	int total_passes = 0;

	int maxim_nb_instrs = -1;
	int maxim_nb_passes = -1;

	Iterator it = info.keySet().iterator();
	while(it.hasNext()){
	    HMethod hm = (HMethod) it.next();
	    MethodInfo mi = (MethodInfo) info.get(hm);
	    System.out.println(hm);
	    System.out.println(" " + mi.nb_instrs + " instrs\t" +
			       mi.nb_passes + " pass(es)\t" + 
			       mi.nb_bbs + " BBs\t" + mi.nb_sccs + " SCCs\t"); 
	    total_instrs += mi.nb_instrs;
	    total_bbs    += mi.nb_bbs;
	    total_sccs   += mi.nb_sccs;
	    total_passes += mi.nb_passes;

	    if(mi.nb_instrs > maxim_nb_instrs)
		maxim_nb_instrs = mi.nb_instrs;
	    if(mi.nb_passes > maxim_nb_passes)
		maxim_nb_passes = mi.nb_passes;
	}

	System.out.println("--TOTALS----------------------------------");
	System.out.println("Nb. of analyzed methods   : " + nb_methods);
	System.out.println("Total nb. of instructions : " + total_instrs);
	System.out.println("Total nb. of BBs          : " + total_bbs);
	System.out.println("Total nb. of SCCs         : " + total_sccs);
	System.out.println("--AVERAGES--------------------------------");
	System.out.println("Average Passes/Method     : " +
			   (double)total_passes/(double)nb_methods);
	System.out.println("Average Instrs/Method     : " +
			   (double)total_instrs/(double)nb_methods);
	System.out.println("Average BB(s)/SCC         : " + 
			   (double)total_bbs/(double)total_sccs);
	System.out.println("Average Instrs/BB         : " + 
			   (double)total_instrs/(double)total_bbs);
	System.out.println("--EXTREMES--------------------------------");
	System.out.println("Biggest procedure(s)   :");
	it = info.keySet().iterator();
	while(it.hasNext()){
	    HMethod hm = (HMethod) it.next();
	    MethodInfo mi = (MethodInfo) info.get(hm);
	    if(mi.nb_instrs >= (int) (0.5 * maxim_nb_instrs))
		System.out.println("  " + hm + " (" + mi.nb_instrs + ")");
	}
	System.out.println("Most visited method(s) :");
	it = info.keySet().iterator();
	while(it.hasNext()){
	    HMethod hm = (HMethod) it.next();
	    MethodInfo mi = (MethodInfo) info.get(hm);
	    if(mi.nb_passes >= (int) (0.5 * maxim_nb_passes))
		System.out.println("  " + hm + " (" + mi.nb_passes + ")");
	}
    }

}











