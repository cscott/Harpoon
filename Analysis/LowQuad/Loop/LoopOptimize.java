// LoopOptimize.java, created Thu Jun 24 11:41:44 1999 by bdemsky
// Copyright (C) 1999 Brian Demsky <bdemsky@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.LowQuad.Loop;


import harpoon.ClassFile.*;
import harpoon.IR.LowQuad.*;
import harpoon.IR.Quads.*;
import harpoon.Analysis.LowQuad.Loop.LoopAnalysis;
import harpoon.Analysis.SSITOSSAMap;
import harpoon.Util.Set;
import harpoon.Util.HashSet;
import harpoon.Util.Util;
/**
 * <code>LoopOptimize</code> optimizes the code after <code>LoopAnalysis</code>.
 * 
 * @author  Brian Demsky <bdemsky@mit.edu>
 * @version $Id: LoopOptimize.java,v 1.1.2.2 1999-06-29 17:24:24 bdemsky Exp $
 */
public final class LoopOptimize {
    
    /** Creates an <code>LoopOptimize</code>. */
    public LoopOptimize() {
    }

    public LoopOptimize(LoopAnalysis lanal) {}

    /** Returns a code factory that uses LoopOptimize. */
    public static HCodeFactory codeFactory(final HCodeFactory parent) {
	return new HCodeFactory() {
	    public HCode convert(HMethod m) {
		HCode hc = parent.convert(m);
		SSITOSSAMap ssitossa=new SSITOSSAMap(hc);
		if (hc!=null) {
		    (new LoopAnalysis(ssitossa)).test(hc);
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
    }
}








