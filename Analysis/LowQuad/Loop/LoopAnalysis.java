// LoopAnalysis.java, created Thu Jun 24 11:45:07 1998 by bdemsky
// Copyright (C) 1999 Brian Demsky <bdemsky@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.LowQuad.Loop;

import harpoon.Analysis.UseDef;
import harpoon.Analysis.Loops.LoopFinder;
import harpoon.Util.WorkSet;
import harpoon.ClassFile.*;
import harpoon.IR.LowQuad.*;
import harpoon.IR.Quads.*;
import harpoon.Analysis.Loops.*;
import harpoon.Temp.Temp;
import harpoon.Analysis.SSITOSSAMap;
import harpoon.Temp.TempMap;
import harpoon.IR.Properties.HasEdges;
import harpoon.Analysis.LowQuad.Loop.AllInductions;
import harpoon.Analysis.LowQuad.Loop.BasicInductions;
import harpoon.Analysis.LowQuad.Loop.LoopInvariance;
import harpoon.Analysis.Maps.AllInductionsMap;
import harpoon.Analysis.Maps.BasicInductionsMap;
import harpoon.Analysis.Maps.InvariantsMap;

import java.util.HashMap;
import java.util.ArrayList;
import java.util.Set;
import java.util.Iterator;

/**
 * <code>LoopAnalysis</code> implements <code>AllInductionsMap</code>,
 * <code>BasicInductionsMap</code>, and <code>InvariantsMap</code>.
 * 
 * @author  Brian Demsky
 * @version $Id: LoopAnalysis.java,v 1.1.2.5 1999-06-29 20:47:48 bdemsky Exp $
 */

public class LoopAnalysis implements AllInductionsMap, BasicInductionsMap, InvariantsMap {

    HCode lasthc;
    TempMap tm;
    HashMap aimap, bimap, invmap;
    LoopFinder rtloop;

    /** Creates a <code>Loop</code>. */
    public LoopAnalysis(TempMap tm) {
	this.tm=tm;
    }

    /*-----------------------------*/
    // Class state.

    /*---------------------------*/
    // public information accessor methods.

    public Loops rootloop(HCode hc) {
	analyze(hc);
	return rtloop;
    }

    public Set allInductionsMap(HCode hc, Loops lp) {
	analyze(hc);
	return (Set) aimap.get(lp);
    }

    public Set basicInductionsMap(HCode hc, Loops lp) {
	analyze(hc);
	return (Set) bimap.get(lp);
    }

    public Set invariantsMap(HCode hc, Loops lp) {
	analyze(hc);
	return (Set) invmap.get(lp);
    }


    /*---------------------------*/
    // Analysis code.

    /** Set of analyzed methods. */

    /** Main analysis method. */
    void analyze(HCode hc) {
	if (lasthc!=hc) {
	    invmap=new HashMap();
	    aimap=new HashMap();
	    bimap=new HashMap();
	    lasthc=hc;
	    rtloop=new LoopFinder(hc);
	    analyzetree(hc,rtloop,"");
	}
    } // end analysis.
    
    void analyzetree(HCode hc, Loops lp, String st) {
	WorkSet kids=(WorkSet)lp.nestedLoops();
	Iterator iterate=kids.iterator();

	while (iterate.hasNext()) {
	    analyzetree(hc, (Loops)iterate.next(),st+" ");
	}


	//Find loop invariants
	WorkSet elements=(WorkSet)lp.loopIncelements();
	LoopInvariance invar=new LoopInvariance(tm,hc);
	WorkSet invariants=invar.invariants(elements);

	//Find basic induction variables

	BasicInductions binductor=new BasicInductions(tm,hc);
	HashMap basicinduction=binductor.doInduction(lp,invariants);


	//Find all induction variables
	AllInductions ainductor=new AllInductions(tm,hc);
	HashMap allInductions=ainductor.doAllInductions(lp, invariants, basicinductions);

	//Add to our maps
	aimap.put(lp, allInductions);
	bimap.put(lp, basicinductions);
	invmap.put(lp,invar);


	//Show the user everything
	iterate=invariants.iterator();
	System.out.println(st+"Invariants:");
	while (iterate.hasNext()) {
	    System.out.println(st+((Quad)iterate.next()).toString());
	}
	iterate=elements.iterator();
	System.out.println(st+"Noninvariants:");
	while (iterate.hasNext()) {
	    System.out.println(st+((Quad)iterate.next()).toString());
	}
	iterate=(basicinductions.keySet()).iterator();

	System.out.println(st+"Basic induction variables:");
	while (iterate.hasNext()) {
	    Temp tmp=(Temp) iterate.next();
	    System.out.println(st+tmp.toString());
	    System.out.println(st+((Induction)basicinductions.get(tmp)).toString());
	}
	iterate=(allInductions.keySet()).iterator();

	System.out.println(st+"All induction variables:");
	while (iterate.hasNext()) {
	    Temp tmp=(Temp) iterate.next();
	    System.out.println(st+tmp.toString());
	    System.out.println(st+((Induction)allInductions.get(tmp)).toString());
	}
    }
}


