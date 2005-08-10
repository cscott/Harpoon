// LoopDetector.java, created Sun Jul 31 07:54:12 2005 by salcianu
// Copyright (C) 2003 Alexandru Salcianu <salcianu@alum.mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.PA2.AllocSync;

import java.util.Collections;
import java.util.Map;
import java.util.HashMap;
import java.util.Arrays;
import java.util.List;

import jpaul.Graphs.DiGraph;
import jpaul.Graphs.TopSortedCompDiGraph;
import jpaul.Graphs.SCComponent;
import jpaul.Graphs.Navigator;

import harpoon.ClassFile.HMethod;
import harpoon.ClassFile.CachingCodeFactory;

import harpoon.IR.Quads.Quad;
import harpoon.IR.Quads.Code;

import harpoon.Util.Util;

/**
 * <code>LoopDetector</code>
 * 
 * @author  Alexandru Salcianu <salcianu@alum.mit.edu>
 * @version $Id: LoopDetector.java,v 1.1 2005-08-10 03:03:16 salcianu Exp $
 */
public class LoopDetector {
    
    public LoopDetector(CachingCodeFactory ccf) {
	this.ccf = ccf;
    }

    private final CachingCodeFactory ccf;
    private final Map<HMethod,Map<Quad,SCComponent<Quad>>> hm2q2scc = 
	new HashMap<HMethod,Map<Quad,SCComponent<Quad>>>();

    public boolean inLoop(Quad q) {
	HMethod hm = Util.quad2method(q);
	Map<Quad,SCComponent<Quad>> q2scc = hm2q2scc.get(hm);
	if(q2scc == null) {
	    q2scc = getQuad2SccMap(hm);
	    hm2q2scc.put(hm, q2scc);
	}
	SCComponent<Quad> scc = q2scc.get(q);
	return scc.isLoop();
    }


    private Map<Quad,SCComponent<Quad>> getQuad2SccMap(HMethod hm) {
	Code code = (Code) ccf.convert(hm);
	return 
	    (new TopSortedCompDiGraph<Quad>(DiGraph.<Quad>diGraph
					    (Collections.<Quad>singleton(code.getRootElement()),
					     QUAD_NAVIGATOR))).
	    getVertex2SccMap();
    }

    private static Navigator<Quad> QUAD_NAVIGATOR = 
	new Navigator<Quad>() {
	    public List<Quad> next(Quad q) { return Arrays.asList(q.next()); }
	    public List<Quad> prev(Quad q) { return Arrays.asList(q.prev()); }
	};

}
