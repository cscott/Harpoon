// QuadsCounter.java, created Mon Jan 22 by wbeebee
// Copyright (C) 2001 Wes Beebee <wbeebee@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.Quads;

import harpoon.ClassFile.HCode;
import harpoon.ClassFile.HCodeFactory;
import harpoon.ClassFile.HMethod;

/**
 * <code>QuadCounter</code> is an <code>harpoon.ClassFile.HCodeFactory</code>
 * which counts the number of quads that have been <code>convert</code>ed
 * thus far.  <code>count()</code> returns the current count.
 * Use in conjunction with a <code>harpoon.ClassFile.CachingCodeFactory</code>
 * to create an accurate count:
 * <code>hcf = new CachingCodeFactory(new QuadCounter(hcf));</code>
 *
 * @author Wes Beebee <a href="mailto:wbeebee@mit.edu">wbeebee@mit.edu</a>
 */

public class QuadCounter implements HCodeFactory {
    private long numQuads;
    private HCodeFactory parent;

    /** Creates a new <code>QuadCounter</code> with <code>hcf</code> as its 
     *  parent.
     */ 
    public QuadCounter(HCodeFactory hcf) {
	numQuads = 0;
	parent = hcf;
    }

    /** Converts <code>HMethod</code> <code>m</code> into an 
     *  <code>HCode</code> and counts the number of <code>Quad</code>s 
     *  returned. 
     */
    public HCode convert(HMethod m) {
	HCode hc = parent.convert(m);
	if (hc != null) {
	    numQuads += hc.getElements().length;
	}
	return hc;
    }

    /** Gets the code name of this <code>HCodeFactory</code>. */

    public String getCodeName() {
	return parent.getCodeName();
    }

    /** Clears <code>HMethod</code> <code>m</code> from the cache.
     *  Note: this does not remove the <code>Quad</code>s from the total count.
     */
    public void clear(HMethod m) {
	parent.clear(m);
    }

    /** Returns the current count of the number of <code>Quad</code>s 
     *  converted. 
     */

    public long count() {
	return numQuads;
    }

    /** Print a textual representation of this <code>QuadCounter</code>. */

    public String toString() {
	return numQuads + " quads";
    }


}
