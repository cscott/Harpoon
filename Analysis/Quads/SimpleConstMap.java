// SimpleConstMap.java, created Wed Nov 14 15:10:19 2001 by cananian
// Copyright (C) 2000 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.Quads;

import harpoon.Analysis.Maps.ConstMap;
import harpoon.ClassFile.HCode;
import harpoon.ClassFile.HCodeElement;
import harpoon.IR.Quads.CONST;
import harpoon.IR.Quads.Quad;
import harpoon.IR.Quads.QuadSSA;
import harpoon.IR.Quads.QuadSSI;
import harpoon.IR.Quads.QuadVisitor;
import harpoon.Temp.Temp;
import harpoon.Util.Util;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
/**
 * <code>SimpleConstMap</code> is a very simple implementation of
 * <code>ConstMap</code> that reports whether a given
 * <code>Temp</code> is defined by a <code>CONST</code> quad.
 * Although simple, this is sufficient if an <code>SCCOptimize</code>
 * pass has been run on the code factory at some point -- the
 * <code>SCCOptimize</code> transformation will turn more complicated
 * constant expressions into the simple ones that
 * <code>SimpleConstMap</code> detects.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: SimpleConstMap.java,v 1.3 2002-02-26 22:41:42 cananian Exp $
 */
public class SimpleConstMap implements ConstMap {
    final SSIToSSAMap ssi2ssa;
    final Map constMap = new HashMap();
    public boolean isConst(HCodeElement hce, Temp t) {
	return constMap.containsKey(map(t));
    }
    public Object constMap(HCodeElement hce, Temp t) {
	if (!isConst(hce, t)) throw new Error("not constant");
	return constMap.get(map(t));
    }
    private Temp map(Temp t) {
	return ssi2ssa==null ? t : ssi2ssa.tempMap(t);
    }
    
    /** Creates a <code>SimpleConstMap</code> which provides information
     *  about <code>HCode</code> <code>hc</code>. */
    public SimpleConstMap(HCode hc) {
	Util.ASSERT(hc.getName().equals(QuadSSI.codename) ||
		    hc.getName().equals(QuadSSA.codename));
	this.ssi2ssa = hc.getName().equals(QuadSSI.codename) ?
	    new SSIToSSAMap(hc) : null;
	QuadVisitor qv = new QuadVisitor() {
		public void visit(Quad q) { /* do nothing */ }
		public void visit(CONST q) {
		    constMap.put(map(q.dst()), q.value());
		}
	    };
        for (Iterator it=hc.getElementsI(); it.hasNext(); )
	    ((Quad)it.next()).accept(qv);
	// done!
    }
}
