// ConstMapProxy.java, created Wed Nov 15 21:37:39 2000 by cananian
// Copyright (C) 2000  <cananian@alumni.princeton.edu>
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
 * @author   <cananian@alumni.princeton.edu>
 * @version $Id: ConstMapProxy.java,v 1.1.2.1 2000-11-16 04:55:04 cananian Exp $
 */
public class ConstMapProxy extends MapProxy implements ConstMap {
    private ConstMap cm;
    /** Creates a <code>ConstMapProxy</code>. */
    public ConstMapProxy(HCodeAndMaps hcam, ConstMap cm) {
        super(hcam);
	this.cm = cm;
    }
    public boolean isConst(HCodeElement hce, Temp t) {
	return cm.isConst(n2o(hce), n2o(t));
    }
    public Object constMap(HCodeElement hce, Temp t) {
	return cm.constMap(n2o(hce), n2o(t));
    }
}
