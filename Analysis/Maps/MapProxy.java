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
 * @version $Id: MapProxy.java,v 1.2 2002-02-25 20:58:10 cananian Exp $
 */
class MapProxy {
    private final Map n2oE;
    private final TempMap n2oT;
    /** Creates a <code>MapProxy</code> from an <code>HCodeAndMaps</code>. */
    MapProxy(HCodeAndMaps hcam) {
	this.n2oE = hcam.ancestorElementMap();
	this.n2oT = hcam.ancestorTempMap();
    }
    HCodeElement n2o(HCodeElement hce) {
	return (HCodeElement) n2oE.get(hce);
    }
    Temp n2o(Temp t) { return (Temp) n2oT.tempMap(t); }
    // utility for Quad forms.
    Edge n2o(Edge e) {
	Quad from = (Quad) n2o(e.from());
	int which_succ = e.which_succ();
	return from.nextEdge(which_succ);
    }
}
