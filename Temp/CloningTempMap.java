// CloningTempMap.java, created Sat Jan 23 02:05:02 1999 by cananian
// Copyright (C) 1999 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Temp;

import harpoon.Util.Util;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
/**
 * A <code>CloningTempMap</code> maps <code>Temp</code>s from one
 * <code>TempFactory</code> to equivalent <code>Temp</code>s in another,
 * creating new <code>Temp</code>s as necessary.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: CloningTempMap.java,v 1.3.2.1 2002-02-27 08:37:21 cananian Exp $
 */
public class CloningTempMap implements TempMap {
    private final Map h = new HashMap();
    private final TempFactory old_tf;
    private final TempFactory new_tf;
    
    /** Creates a <code>CloningTempMap</code>, given the
     * source and destination <code>TempFactory</code>s.
     */
    public CloningTempMap(TempFactory old_tf, TempFactory new_tf) {
	this.old_tf = old_tf; this.new_tf = new_tf;
        assert old_tf != null : "old temp factory is null";
        assert new_tf != null : "new temp factory is null";
    }

    /** Return a <code>Temp</code> from the <code>new_tf</code>
     *  <code>TempFactory</code> that corresponds to the specified
     *  <code>Temp</code> <code>t</code> from the <code>old_tf</code>
     *  <code>TempFactory</code>, creating it if necessary. */
    public Temp tempMap(Temp t) {
	assert t.tempFactory() == old_tf : "TempFactories should match";
	Temp r = (Temp) h.get(t);
	if (r==null) {
	    r = t.clone(new_tf);
	    h.put(t, r);
	}
	assert r.tempFactory() == new_tf;
	return r;
    }

    /** Provide access to an unmodifiable version of this 
     *  <code>TempMap</code. */
    public TempMap unmodifiable() {
	return new TempMap() {
	    public Temp tempMap(Temp t) { return (Temp) h.get(t); }
	};
    }
    /** Provide access to an unmodifiable version of this
     *  <code>Temp</code> mapping, as a <code>java.util.Map</code>. */
    public Map asMap() { return Collections.unmodifiableMap(h); }
}
