// QuadInterferenceGraph.java, created Thu Nov 23 13:14:33 2000 by cananian
// Copyright (C) 2000 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.Quads;

import harpoon.Analysis.InterferenceGraph;
import harpoon.Analysis.Liveness;
import harpoon.IR.Quads.Code;
import harpoon.IR.Quads.Quad;
import harpoon.Temp.Temp;
import harpoon.Util.Collections.AggregateSetFactory;
import harpoon.Util.Collections.GenericMultiMap;
import harpoon.Util.Collections.MultiMap;
import harpoon.Util.Grapher;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
/**
 * <code>QuadInterferenceGraph</code> constructs a variable-liveness
 * interference graph from a <code>Quad.Code</code>.  There is an
 * edge between <code>Temp</code> <code>t1</code> and
 * <code>Temp</code> <code>t2</code> if <code>t1</code> and
 * <code>t2</code> are ever live at the same point.
 * <p>
 * <code>QuadInterferenceGraph</code> is a <code>Grapher</code>
 * for <code>Temp</code>s.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: QuadInterferenceGraph.java,v 1.1.2.2 2000-12-17 18:08:11 cananian Exp $
 */
public class QuadInterferenceGraph implements InterferenceGraph {
    private final MultiMap mm;
    
    /** Creates a <code>QuadInterferenceGraph</code>. */
    public QuadInterferenceGraph(Code code) {
	this(code, new QuadLiveness(code));
    }
    public QuadInterferenceGraph(Code code, Liveness live) {
        this.mm = new GenericMultiMap(new AggregateSetFactory());
	for (Iterator it=code.getElementsI(); it.hasNext(); ) {
	    Quad q = (Quad) it.next();
	    for (Iterator it2=live.getLiveOut(q).iterator(); it2.hasNext(); ) {
		Temp t = (Temp) it2.next();
		for (Iterator it3=q.defC().iterator(); it3.hasNext(); ) {
		    Temp d = (Temp) it3.next();
		    if (!t.equals(d)) {
			mm.add(t, d);
			mm.add(d, t);
		    }
		}
	    }
	}
    }
    /** unimplemented.  always returns 1. */
    public int spillCost(Temp t) { return 1; }
    /** unimplemented.  always returns 0-element list. */
    public List moves() { return Collections.EMPTY_LIST; }

    public boolean isEdge(Object from, Object to) {
	return mm.contains(from, to);
    }
    public Set succSet(Object node) { return (Set) mm.getValues(node); }
    public Set predSet(Object node) { return (Set) mm.getValues(node); }
}
