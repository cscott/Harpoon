// ArrayCopyInliner.java, created by wbeebee
// Copyright (C) 2002 Wes Beebee <wbeebee@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.Quads;

import harpoon.Analysis.ClassHierarchy;
import harpoon.ClassFile.HCodeFactory;
import harpoon.ClassFile.HMethod;
import net.cscott.jutil.MultiMap;

/**
 * <code>ArrayCopyInliner</code> will inline array copy.
 *
 * @author  Wes Beebee <wbeebee@mit.edu>
 * 
 */
public class ArrayCopyInliner extends SmallMethodInliner {
    public ArrayCopyInliner(HCodeFactory hcf, ClassHierarchy ch) {
	super(hcf, ch);
    }

    protected int score(HMethod hm, IntMap methodSize, MultiMap callSites) {
	if ((hm != null)&&hm.getName().startsWith("arraycopy")) {
	    return -4;
	} 
	return methodSize.getInt(hm);
    }
}
