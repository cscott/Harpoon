// Reachable.java, created Tue Nov 11 14:18:19 2003 by cananian
// Copyright (C) 2003 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis;

import harpoon.ClassFile.HCode;
import harpoon.ClassFile.HCodeElement;
import harpoon.IR.Properties.CFGrapher;
import net.cscott.jutil.WorkSet;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
/**
 * <code>Reachable</code>
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: Reachable.java,v 1.4 2004-02-08 03:19:12 cananian Exp $
 */
public final class Reachable<HCE extends HCodeElement> {
    public final Set<HCE> reachable;

    public Reachable(HCode<HCE> hcode) {
	this(hcode, (CFGrapher<HCE>) CFGrapher.DEFAULT);
    }
    public Reachable(HCode<HCE> hcode, CFGrapher<HCE> grapher) {
	Set<HCE> reachable = new HashSet<HCE>();
	// initialize worklist.
	WorkSet<HCE> work = new WorkSet<HCE>();
	work.addAll(Arrays.asList(grapher.getFirstElements(hcode)));
	// iterate until worklist is empty.
	while (!work.isEmpty()) {
	    HCE hce = work.removeFirst();
	    reachable.add(hce);
	    
	    for(HCE succ : grapher.succElemC(hce)) {
		if (!reachable.contains(succ))
		    work.add(succ);
	    }
	}
	// done!
	this.reachable = Collections.unmodifiableSet(reachable);
    }
    public boolean isReachable(HCE hce) {
	return reachable.contains(hce);
    }
}
