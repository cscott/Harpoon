// ExactTypeMapProxy.java, created Wed Nov 15 20:49:03 2000 by cananian
// Copyright (C) 2000 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.Maps;

import harpoon.ClassFile.HCodeAndMaps;
import harpoon.ClassFile.HCodeElement;
import harpoon.Temp.Temp;
/**
 * An <code>ExactTypeMapProxy</code> implements an <code>ExactTypeMap</code>
 * for a cloned <code>HCode</code> given the <code>HCodeAndMaps</code> which
 * specifies its relationship to an <code>HCode</code> for which an
 * <code>ExactTypeMap</code> is known.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: ExactTypeMapProxy.java,v 1.1.2.2 2001-06-17 22:30:23 cananian Exp $
 */
public class ExactTypeMapProxy extends TypeMapProxy implements ExactTypeMap {
    private ExactTypeMap etm;
    /** Creates a <code>ExactTypeMapProxy</code>. */
    public ExactTypeMapProxy(HCodeAndMaps hcam, ExactTypeMap etm) {
        super(hcam, etm);
	this.etm = etm;
    }
    public boolean isExactType(HCodeElement hce, Temp t) {
	return etm.isExactType(n2o(hce), n2o(t));
    }
}
