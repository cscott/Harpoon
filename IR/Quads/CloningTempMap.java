// CloningTempMap.java, created Sat Jan 23 02:05:02 1999 by cananian
// Copyright (C) 1999 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.Quads;

import harpoon.Temp.Temp;
import harpoon.Temp.TempFactory;
import harpoon.Temp.TempMap;
import harpoon.Util.Util;

import java.util.Hashtable;
/**
 * A <code>CloningTempMap</code> maps <code>Temp</code>s from one
 * <code>TempFactory</code> to equivalent <code>Temp</code>s in another.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: CloningTempMap.java,v 1.1.2.1 1999-01-23 07:59:04 cananian Exp $
 */
public class CloningTempMap implements TempMap {
    private Hashtable h = new Hashtable();
    private final TempFactory old_tf;
    private final TempFactory new_tf;
    
    /** Creates a <code>CloningTempMap</code>, given the
     * source and destination <code>TempFactory</code>s.
     */
    public CloningTempMap(TempFactory old_tf, TempFactory new_tf) {
	this.old_tf = old_tf; this.new_tf = new_tf;
    }

    public Temp tempMap(Temp t) {
	Util.assert(t.tempFactory() == old_tf);
	Temp r = (Temp) h.get(t);
	if (r==null) {
	    r = t.clone(new_tf);
	    h.put(t, r);
	}
	Util.assert(r.tempFactory() == new_tf);
	return r;
    }
}
