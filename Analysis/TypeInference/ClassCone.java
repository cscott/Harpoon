// ClassCone.java, created Mon Nov 23 22:26:40 1998 by marinov
// Copyright (C) 1998 Darko Marinov <marinov@lcs.mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.TypeInference;

import harpoon.Analysis.ClassHierarchy;
import harpoon.ClassFile.HClass;
import harpoon.Util.Worklist;
import harpoon.Util.WorkSet;
import harpoon.Util.HClassUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Iterator;
/**
 * <code>ClassCone</code>
 * 
 * @author  Darko Marinov <marinov@lcs.mit.edu>
 * @version $Id: ClassCone.java,v 1.1.2.6 2000-01-17 11:10:16 cananian Exp $
 */

public class ClassCone  {
    
    /** Creates a <code>ClassCone</code>. */
    public ClassCone(ClassHierarchy ch) { this.ch = ch; }

    static ClassHierarchy ch;
    static Map map = new HashMap();
    static SetHClass cone(HClass c) { 
	/* ??? it might be better not to expand the cone immediately
	 */
	SetHClass s = (SetHClass)map.get(c);
	if (s==null) {
	    s = new SetHClass();
	    int dims = HClassUtil.dims(c);
	    Worklist wl = new WorkSet();
	    wl.push(HClassUtil.baseClass(c));
	    while (!wl.isEmpty()) {
		HClass cl = (HClass)wl.pull();
		s.add(HClassUtil.arrayClass(cl, dims));
		for (Iterator it=ch.children(cl).iterator(); it.hasNext(); )
		    wl.push(it.next());
	    }
	    map.put(c, s);
	}
	return s.copy(); 
    }
    
}
