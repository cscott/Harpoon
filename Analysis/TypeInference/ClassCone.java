// ClassCone.java, created Mon Nov 23 22:26:40 1998 by marinov
// Copyright (C) 1998 Darko Marinov <marinov@lcs.mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.TypeInference;

import harpoon.Analysis.QuadSSA.ClassHierarchy;
import java.util.Hashtable;
import harpoon.ClassFile.*;
import harpoon.Util.Worklist;
import harpoon.Util.Set;
import harpoon.Util.HashSet;
import harpoon.Util.HClassUtil;
/**
 * <code>ClassCone</code>
 * 
 * @author  Darko Marinov <marinov@lcs.mit.edu>
 * @version $Id: ClassCone.java,v 1.1.2.3 1999-08-04 05:52:24 cananian Exp $
 */

public class ClassCone  {
    
    /** Creates a <code>ClassCone</code>. */
    public ClassCone(ClassHierarchy ch) { this.ch = ch; }

    static ClassHierarchy ch;
    static Hashtable map = new Hashtable();
    static SetHClass cone(HClass c) { 
	/* ??? it might be better not to expand the cone immediately
	 */
	SetHClass s = (SetHClass)map.get(c);
	if (s==null) {
	    s = new SetHClass();
	    int dims = HClassUtil.dims(c);
	    Worklist wl = new HashSet();
	    wl.push(HClassUtil.baseClass(c));
	    while (!wl.isEmpty()) {
		HClass cl = (HClass)wl.pull();
		s.union(HClassUtil.arrayClass(cl, dims));
		HClass[] k = ch.children(cl);
		for (int i=0; i<k.length; i++)
		    wl.push(k[i]);
	    }
	    map.put(c, s);
	}
	return s.copy(); 
    }
    
}
