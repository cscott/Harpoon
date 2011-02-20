// MapProxy.java, created Wed Nov 15 20:41:05 2000 by cananian
// Copyright (C) 2000 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.Maps;

import harpoon.ClassFile.HCodeAndMaps;
import harpoon.ClassFile.HCodeElement;
import harpoon.IR.Quads.Edge;
import harpoon.IR.Quads.Quad;
import harpoon.Temp.Temp;
import harpoon.Temp.TempMap;

import java.util.Map;
/**
 * <code>MapProxy</code> is an abstract class which contains code common
 * to various types of map proxies.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: MapProxy.java,v 1.3 2002-09-02 19:23:26 cananian Exp $
 */
class MapProxy<HCE extends HCodeElement> {
    private final Map<HCE,HCE> n2oE;
    private final TempMap n2oT;
    /** Creates a <code>MapProxy</code> from an <code>HCodeAndMaps</code>. */
    MapProxy(HCodeAndMaps<HCE> hcam) {
	this.n2oE = hcam.ancestorElementMap();
	this.n2oT = hcam.ancestorTempMap();
    }
    HCE n2o(HCE hce) {
	return n2oE.get(hce);
    }
    Temp n2o(Temp t) { return n2oT.tempMap(t); }
    // utility for Quad forms.
    Edge n2o(Edge e) {
	Quad from = (Quad) n2o((HCE)e.from());
	int which_succ = e.which_succ();
	return from.nextEdge(which_succ);
    }
}
