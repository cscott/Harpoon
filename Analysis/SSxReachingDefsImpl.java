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
 * @version $Id: SSxReachingDefsImpl.java,v 1.5 2004-02-08 01:49:03 cananian Exp $
 */
public class SSxReachingDefsImpl<HCE extends HCodeElement>
    extends ReachingDefs<HCE> {
    private final Map<Temp,HCE> m = new HashMap<Temp,HCE>();
    /** Create an <code>SSxReachingDefs</code> using the default
     *  <code>UseDefer</code>. */
    public SSxReachingDefsImpl(HCode<HCE> hc) {
	this(hc, (UseDefer<HCE>) UseDefer.DEFAULT);
    }
    /** Create an <code>SSxReachingDefs</code> for <code>hc</code>
     *  using the specified <code>UseDefer</code>. */
    public SSxReachingDefsImpl(HCode<HCE> hc, UseDefer<HCE> ud) {
	super(hc);
	for (Iterator<HCE> it = hc.getElementsI(); it.hasNext(); ) {
	    HCE hce = (HCE) it.next();
	    for (Iterator<Temp> it2=ud.defC(hce).iterator(); it2.hasNext(); ) {
		Temp t = it2.next();
		assert !m.containsKey(t) : "not in SSA/SSI form!";
		m.put(t, hce);
	    }
	}
    }
    public Set<HCE> reachingDefs(HCE hce, Temp t) {
	if (!m.containsKey(t)) return Collections.EMPTY_SET;
	return Collections.singleton(m.get(t));
    }
}
