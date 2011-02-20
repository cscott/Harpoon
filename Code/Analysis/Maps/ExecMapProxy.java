// ExecMapProxy.java, created Wed Nov 15 21:42:10 2000 by cananian
// Copyright (C) 2000 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.Maps;

import harpoon.ClassFile.HCodeAndMaps;
import harpoon.ClassFile.HCodeEdge;
import harpoon.ClassFile.HCodeElement;
import harpoon.IR.Quads.Edge;
/**
 * An <code>ExecMapProxy</code> implements an <code>ExecMap</code> for
 * a cloned <code>HCode</code> given the <code>HCodeAndMaps</code> which
 * specifies its relationship to an <code>HCode</code> for which a
 * <code>ExecMap</code> is known.
 * 
 * @author   C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: ExecMapProxy.java,v 1.3 2002-09-02 19:23:26 cananian Exp $
 */
public class ExecMapProxy<HCE extends HCodeElement>
    extends MapProxy<HCE> implements ExecMap<HCE> {
    private ExecMap<HCE> em;
    /** Creates an <code>ExecMapProxy</code>. */
    public ExecMapProxy(HCodeAndMaps<HCE> hcam, ExecMap<HCE> em) {
        super(hcam);
	this.em = em;
    }
    public boolean execMap(HCE node) {
	return em.execMap(n2o(node));
    }
    public boolean execMap(HCodeEdge<HCE> edge) {
	if (edge instanceof Edge)
	    return em.execMap((HCodeEdge)n2o((Edge)edge));
	throw new Error("No consistent way to map edges between IRs.");
    }
}
