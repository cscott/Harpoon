// WorkTempMap.java, created Thu Jul 8 11:25:09 1999 by bdemsky
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
 * @version $Id: WorkTempMap.java,v 1.1.2.1 1999-07-09 15:24:23 bdemsky Exp $
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
