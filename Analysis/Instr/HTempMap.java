// HTempMap.java, created Mon Aug 21 20:02:09 2000 by pnkfelix
// Copyright (C) 2000 Felix S. Klock II <pnkfelix@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.Instr;

import harpoon.Util.Collections.GenericInvertibleMap;
import harpoon.Temp.TempMap;
import harpoon.Temp.Temp;

/**
 * <code>HTempMap</code>
 * 
 * @author  Felix S. Klock II <pnkfelix@mit.edu>
 * @version $Id: HTempMap.java,v 1.1.2.2 2001-06-17 22:29:52 cananian Exp $
 */
class HTempMap extends GenericInvertibleMap implements TempMap {
    public Temp tempMap(Temp t) {
	return (Temp) this.get(t);
    }
    public Object get(Object key) {
	Object o = super.get(key);
	return (o == null) ? key : o;
    }
}
