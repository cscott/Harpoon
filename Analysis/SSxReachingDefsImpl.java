// SSXReachingDefsImpl.java, created Tue Jun  6 22:28:11 2000 by cananian
// Copyright (C) 2000 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis;

import harpoon.ClassFile.HCode;
import harpoon.ClassFile.HCodeElement;
import harpoon.IR.Properties.UseDefer;
import harpoon.Temp.Temp;
import harpoon.Util.Util;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
/**
 * <code>SSxReachingDefsImpl</code> is a <code>ReachingDefs</code>
 * implementation that works on codeviews in SSA or SSI form.  It
 * is much more efficient (because of the SSx form) than the
 * standard <code>ReachingDefsImpl</code>.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: SSxReachingDefsImpl.java,v 1.2 2002-02-25 20:56:10 cananian Exp $
 */
public class SSxReachingDefsImpl extends ReachingDefs {
    private final Map m = new HashMap();
    /** Create an <code>SSxReachingDefs</code> using the default
     *  <code>UseDefer</code>. */
    public SSxReachingDefsImpl(HCode hc) { this(hc, UseDefer.DEFAULT); }
    /** Create an <code>SSxReachingDefs</code> for <code>hc</code>
     *  using the specified <code>UseDefer</code>. */
    public SSxReachingDefsImpl(HCode hc, UseDefer ud) {
	super(hc);
	for (Iterator it = hc.getElementsI(); it.hasNext(); ) {
	    HCodeElement hce = (HCodeElement) it.next();
	    for (Iterator it2 = ud.defC(hce).iterator(); it2.hasNext(); ) {
		Temp t = (Temp) it2.next();
		Util.assert(!m.containsKey(t), "not in SSA/SSI form!");
		m.put(t, hce);
	    }
	}
    }
    public Set reachingDefs(HCodeElement hce, Temp t) {
	if (!m.containsKey(t)) return Collections.EMPTY_SET;
	return Collections.singleton(m.get(t));
    }
}
