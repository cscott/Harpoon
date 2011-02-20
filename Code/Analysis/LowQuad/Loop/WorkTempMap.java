// WorkTempMap.java, created Thu Jul 8 11:25:09 1999 by bdemsky
// Copyright (C) 1998 Brian Demsky <bdemsky@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.LowQuad.Loop;

import harpoon.Temp.Temp;
import harpoon.Temp.TempMap;
import harpoon.Temp.WritableTempMap;

import java.util.HashMap;
/**
 * <code>WorkTempMap</code> is an implementation of a
 * <code>WritableTempMap</code>.
 * 
 * @author  Brian Demsky <bdemsky@mit.edu>
 * @version $Id: WorkTempMap.java,v 1.2 2002-02-25 20:57:58 cananian Exp $
 */

class WorkTempMap implements WritableTempMap {
    HashMap hm;
    WorkTempMap() {
	hm=new HashMap();
    }

    /** Add a mapping from <code>Temp</code> <code>Told</code> to
     *  <code>Temp</code> <code>Tnew</code>. */
    public void associate(Temp Told, Temp Tnew) {
	hm.put(Told,Tnew);
    }

    public Temp tempMap(Temp Told) {
	return (Temp) hm.get(Told);
    }
}
