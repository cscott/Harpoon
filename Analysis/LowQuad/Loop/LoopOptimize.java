// LoopOptimize.java, created Thu Jun 24 11:41:44 1999 by bdemsky
// Copyright (C) 1999 Brian Demsky <bdemsky@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.LowQuad.Loop;


import harpoon.ClassFile.*;
import harpoon.IR.LowQuad.*;
import harpoon.IR.Quads.*;
import harpoon.IR.Properties.HasEdges;
import harpoon.Analysis.Loops.Loops;
import harpoon.Analysis.LowQuad.Loop.LoopAnalysis;
import harpoon.Analysis.SSITOSSAMap;
import harpoon.Analysis.Maps.AllInductionsMap;
import harpoon.Analysis.Maps.BasicInductionsMap;
import harpoon.Analysis.Maps.InvariantsMap;
import harpoon.Util.Util;
import harpoon.Util.WorkSet;

import java.util.Iterator;
/**
 * <code>LoopOptimize</code> optimizes the code after <code>LoopAnalysis</code>.
 * 
 * @author  Brian Demsky <bdemsky@mit.edu>
 * @version $Id: LoopOptimize.java,v 1.1.2.3 1999-06-29 20:47:48 bdemsky Exp $
 */
public final class LoopOptimize {
    
    AllInductionsMap aimap;
    BasicInductionsMap bimap;
    InvariantsMap invmap;
    LoopAnalysis loopanal;
    
    /** Creates an <code>LoopOptimize</code>. */
    public LoopOptimize(AllInductionsMap aimap,BasicInductionsMap bimap,InvariantsMap invmap, LoopAnalysis loopanal) {
	this.aimap=aimap;
	this.bimap=bimap;
	this.invmap=invmap;
	this.loopanal=loopanal;
    }

    public LoopOptimize(LoopAnalysis lanal) {
	this(lanal,lanal,lanal,lanal);
    }

    /** Returns a code factory that uses LoopOptimize. */
    public static HCodeFactory codeFactory(final HCodeFactory parent) {
	return new HCodeFactory() {
	    public HCode convert(HMethod m) {
		HCode hc = parent.convert(m);
		SSITOSSAMap ssitossa=new SSITOSSAMap(hc);
		if (hc!=null) {
		    (new LoopOptimize(new LoopAnalysis(ssitossa))).optimize(hc);
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
	    recursetree((Loops)iterate.next());
    }

    void recursetree(Loops lp) {
	if (lp.loopEntrances().size()==1) {
	    HCodeElement hce=(HCodeElement)(lp.loopEntrances()).toArray()[0];
	    if (((HasEdges)hce).pred().length==2) {
		doLoop(lp);
	    }
	    else System.out.println("More than one entrance.");
	} else
	    System.out.println("Multiple or No  entrance loop in LoopOptimize!");
	WorkSet kids=(WorkSet) lp.nestedLoops();
	Iterator iterate=kids.iterator();
	while (iterate.hasNext())
	    recursetree((Loops)iterate.next());
    }

    void doLoop(Loops lp) {
	

    } 
}








