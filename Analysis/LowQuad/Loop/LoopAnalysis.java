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


import java.util.HashMap;
import java.util.ArrayList;
import java.util.Set;
import java.util.Iterator;
/**
 * <code>LoopAnalysis</code> implements 
 * 
 * @author  Brian Demsky
 * @version $Id: LoopAnalysis.java,v 1.1.2.4 1999-06-29 19:05:15 bdemsky Exp $
 */

public class LoopAnalysis {

    HCode hc;
    LoopFinder loopf;
    TempMap tm;

    /** Creates a <code>Loop</code>. */
    public LoopAnalysis(TempMap tm) {
	this.tm=tm;
    }

    /*-----------------------------*/
    // Class state.

    /*---------------------------*/
    // public information accessor methods.

    public void test(HCode hc) {
	analyze(hc);
    }

    /*---------------------------*/
    // Analysis code.

    /** Set of analyzed methods. */

    /** Main analysis method. */
    void analyze(HCode hc) {
	this.hc=hc;
	loopf=new LoopFinder(hc);
	printtree(loopf,"");
    } // end analysis.
    
    void printtree(Loops lp, String st) {
	WorkSet kids=(WorkSet)lp.nestedLoops();
	Iterator iterate=kids.iterator();
	while (iterate.hasNext()) {
	    printtree((Loops)iterate.next(),st+" ");
	}
	//Find loop invariants
	WorkSet elements=(WorkSet)lp.loopIncelements();
	LoopInvariance invar=new LoopInvariance(tm,hc);

	WorkSet invariants=invar.invariants(elements);

	//Find basic induction variables
	BasicInductions binductor=new BasicInductions(tm,hc);
	HashMap basicinduction=binductor.doInduction(lp,invariants);

	AllInductions ainductor=new AllInductions(tm,hc);
	HashMap allInductions=ainductor.doAllInductions(lp, invariants, basicinduction);


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
	iterate=(basicinduction.keySet()).iterator();

	System.out.println(st+"Basic induction variables:");
	while (iterate.hasNext()) {
	    Temp tmp=(Temp) iterate.next();
	    System.out.println(st+tmp.toString());
	    System.out.println(st+((Induction)basicinduction.get(tmp)).toString());
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


