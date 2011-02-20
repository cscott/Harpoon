// ConstMapProxy.java, created Wed Nov 15 21:37:39 2000 by cananian
// Copyright (C) 2000 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.Maps;

import harpoon.ClassFile.HCodeAndMaps;
import harpoon.ClassFile.HCodeElement;
import harpoon.Temp.Temp;
/**
 * A <code>ConstMapProxy</code> implements a <code>ConstMap</code> for
 * a cloned <code>HCode</code> given the <code>HCodeAndMaps</code> which
 * specifies its relationship to an <code>HCode</code> for which a
 * <code>ConstMap</code> is known.
 * 
 * @author   C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: ConstMapProxy.java,v 1.3 2002-09-02 19:23:26 cananian Exp $
 */
public class ConstMapProxy<HCE extends HCodeElement>
    extends MapProxy<HCE> implements ConstMap<HCE> {
    private ConstMap<HCE> cm;
    /** Creates a <code>ConstMapProxy</code>. */
    public ConstMapProxy(HCodeAndMaps<HCE> hcam, ConstMap<HCE> cm) {
        super(hcam);
	this.cm = cm;
    }
    public boolean isConst(HCE hce, Temp t) {
	return cm.isConst(n2o(hce), n2o(t));
    }
    public Object constMap(HCE hce, Temp t) {
	return cm.constMap(n2o(hce), n2o(t));
    }
}
