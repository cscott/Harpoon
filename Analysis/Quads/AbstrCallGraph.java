// AbstrCallGraph.java, created Wed Apr 10 23:37:29 2002 by salcianu
// Copyright (C) 2000 Alexandru SALCIANU <salcianu@MIT.EDU>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.Quads;

import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.Iterator;

import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HMethod;
import harpoon.ClassFile.HCodeFactory;
import harpoon.IR.Quads.Code;
import harpoon.IR.Quads.Quad;
import harpoon.IR.Quads.CALL;


/**
 * <code>AbstrCallGraph</code> contains some common code for several
 * implementations of <code>CallGraph</code>.
 * 
 * @author  Alexandru SALCIANU <salcianu@MIT.EDU>
 * @version $Id: AbstrCallGraph.java,v 1.7 2004-02-08 04:53:33 cananian Exp $
 */
abstract class AbstrCallGraph extends CallGraph {

    // the code factory that produces the code of the methods
    protected HCodeFactory hcf;
    
    /** Returns a list of all <code>CALL</code>s quads in the code 
	of <code>hm</code>. */
    public CALL[] getCallSites(final HMethod hm) {
	CALL[] retval = cache_cs.get(hm);
	if(retval == null) {
	    Code code = (Code) hcf.convert(hm);
	    if(code == null) {
		retval = new CALL[0];
	    }
	    else {
		List<Quad> l = code.selectCALLs();
		retval = l.toArray(new CALL[l.size()]);
	    }
	    cache_cs.put(hm, retval);
	}
	return retval;
    }
    final private Map<HMethod,CALL[]> cache_cs = new HashMap<HMethod,CALL[]>();
    final private static CALL[] empty_array = new CALL[0];

    /** Safe implementation of <code>getRunMethods</code>: returns the
        set of callable methods named &quot;run&quot;, with no
        argument and declared by a subclass of
        <code>java.lang.Thread</code>. */
    public Set<HMethod> getRunMethods() {
	Set<HMethod> runs = new HashSet<HMethod>();
	for(HMethod hm : callableMethods()) {
	    if((hm.getParameterNames().length == 0) &&
	       hm.getName().equals("run") && 
	       isThread(hm.getDeclaringClass()))
		runs.add(hm);
	}
	return runs;
    }

    private static boolean isThread(HClass hclass) {
	HClass jlt = hclass.getLinker().forName("java.lang.Thread");
	return hclass.isInstanceOf(jlt);
    }
}
