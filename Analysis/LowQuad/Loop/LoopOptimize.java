// LoopOptimize.java, created Thu Jun 24 11:41:44 1999 by bdemsky
// Copyright (C) 1999 Brian Demsky <bdemsky@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.LowQuad.Loop;


import harpoon.ClassFile.*;
import harpoon.IR.LowQuad.*;
import harpoon.IR.Quads.*;
import harpoon.IR.Properties.HasEdges;
import harpoon.Analysis.UseDef;
import harpoon.Analysis.Loops.Loops;
import harpoon.Analysis.LowQuad.Loop.LoopAnalysis;
import harpoon.Analysis.SSITOSSAMap;
import harpoon.Analysis.Maps.AllInductionsMap;
import harpoon.Analysis.Maps.BasicInductionsMap;
import harpoon.Analysis.Maps.InvariantsMap;
import harpoon.Util.Util;
import harpoon.Util.WorkSet;
import harpoon.Temp.TempMap;
import harpoon.Temp.Temp;

import java.util.Iterator;
/**
 * <code>LoopOptimize</code> optimizes the code after <code>LoopAnalysis</code>.
 * 
 * @author  Brian Demsky <bdemsky@mit.edu>
 * @version $Id: LoopOptimize.java,v 1.1.2.5 1999-06-30 20:02:49 bdemsky Exp $
 */
public final class LoopOptimize {
    
    AllInductionsMap aimap;
    BasicInductionsMap bimap;
    InvariantsMap invmap;
    LoopAnalysis loopanal;
    TempMap ssitossamap;

    /** Creates an <code>LoopOptimize</code>. */
    public LoopOptimize(AllInductionsMap aimap,BasicInductionsMap bimap,InvariantsMap invmap, LoopAnalysis loopanal, TempMap ssitossamap) {
	this.aimap=aimap;
	this.bimap=bimap;
	this.invmap=invmap;
	this.loopanal=loopanal;
	this.ssitossamap=ssitossamap;
    }

    public LoopOptimize(LoopAnalysis lanal, TempMap ssitossamap) {
	this(lanal,lanal,lanal,lanal, ssitossamap);
    }

    /** Returns a code factory that uses LoopOptimize. */
    public static HCodeFactory codeFactory(final HCodeFactory parent) {
	return new HCodeFactory() {
	    public HCode convert(HMethod m) {
		HCode hc = parent.convert(m);
		SSITOSSAMap ssitossa=new SSITOSSAMap(hc);
		if (hc!=null) {
		    (new LoopOptimize(new LoopAnalysis(ssitossa),ssitossa)).optimize(hc);
		}
		return hc;
	    }
	    public String getCodeName() { return parent.getCodeName(); }
	    public void clear(HMethod m) { parent.clear(m); }
	};
    }

    public void optimize(final HCode hc) {
    
	LowQuadVisitor visitor = new LowQuadVisitor() {
	    public void visit(Quad q) {
	    } // END VISIT quad.

	    public void visit(CONST q) { /* do nothing. */ }
	    public void visit(FOOTER q) {
	    }
	    public void visit(SIGMA q) {
	    } // end VISIT SIGMA
	    public void visit(PHI q) {
	    } // end VISIT PHI.
	};
	
	// actual traversal code.
	Loops lp=loopanal.rootloop(hc);
	WorkSet kids=(WorkSet) lp.nestedLoops();
	Iterator iterate=kids.iterator();
	while (iterate.hasNext())
	    recursetree(hc, (Loops)iterate.next(), new WorkSet());
	//      Put this in soon
	//	DeadCode.optimize(hc);
    }

    void recursetree(HCode hc, Loops lp, WorkSet usedinvariants) {
	if (lp.loopEntrances().size()==1) {
	    HCodeElement hce=(HCodeElement)(lp.loopEntrances()).toArray()[0];
	    if (((HasEdges)hce).pred().length==2) {
		doLoop(hc, lp,(Quad)hce, usedinvariants);
	    }
	    else System.out.println("More than one entrance.");
	} else
	    System.out.println("Multiple or No  entrance loop in LoopOptimize!");
	WorkSet kids=(WorkSet) lp.nestedLoops();
	Iterator iterate=kids.iterator();
	while (iterate.hasNext())
	    recursetree(hc, (Loops)iterate.next(),usedinvariants);
    }

    void doLoop(HCode hc, Loops lp,Quad header, WorkSet usedinvariants) {
	WorkSet invariants=new WorkSet(invmap.invariantsMap(hc, lp));
	UseDef ud=new UseDef();
	int linkin;
	Util.assert(((HasEdges)header).pred().length==2);

	//Only worry about headers with two edges
	if (lp.loopIncelements().contains(header.prev(0)))
	    linkin=1;
	else
	    linkin=0;

	Quad loopcaller=header.prev(linkin);
	int which_succ=header.prevEdge(linkin).which_succ();
	Quad successor=header;
	int which_pred=linkin;


	while (!invariants.isEmpty()) {
	    Iterator iterate=invariants.iterator();
	    while (iterate.hasNext()) {
		Quad q=(Quad)iterate.next();
		if (usedinvariants.contains(q)) {
		    iterate.remove();
		    break;
		}
		Temp[] uses=q.use();
		boolean okay=true;
		for (int i=0;i<uses.length;i++) {
		    HCodeElement []sources=ud.defMap(hc,ssitossamap.tempMap(uses[i]));
		    Util.assert(sources.length==1);
		    if (invariants.contains(sources[0])) {
			okay=false;
			break;
		    }
		}
		if (okay) {
		    //do evil things to SSI
		    Quad newquad=q.rename(q.getFactory(), ssitossamap, ssitossamap);
		    //we made a good quad now....
		    //Toss it  in the pile
		    Quad.addEdge(loopcaller, which_succ,newquad,0);
		    //Link the old quad away
		    Quad.addEdge(q.prev(0),q.prevEdge(0).which_succ(), q.next(0), q.nextEdge(0).which_pred());
		    usedinvariants.push(q);
      		    //Set up the next link
		    loopcaller=newquad;
		    which_succ=0;	
		    //Need to link to the loop
		    Quad.addEdge(loopcaller, which_succ, successor, which_pred);
		}
	    }
	}
    } 
}
