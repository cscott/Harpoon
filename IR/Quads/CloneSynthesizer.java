// CloneSynthesizer.java, created Fri Oct 20 18:46:05 2000 by cananian
// Copyright (C) 2000 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.Quads;

import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HCode;
import harpoon.ClassFile.HCodeFactory;
import harpoon.ClassFile.HMethod;
import harpoon.Temp.Temp;
import harpoon.Util.Util;

import java.util.HashMap;
import java.util.Map;
/**
 * <code>CloneSynthesizer</code> adds synthetic implementations for
 * array clone methods.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: CloneSynthesizer.java,v 1.3 2002-02-26 22:45:56 cananian Exp $
 */
class CloneSynthesizer implements HCodeFactory, java.io.Serializable {
    /** Parent code factory. */
    final HCodeFactory parent;
    /** Representation cache. */
    final Map cache = new HashMap();

    /** Creates a <code>CloneSynthesizer</code>. */
    public CloneSynthesizer(HCodeFactory parent) { 
	Util.ASSERT(parent.getCodeName().equals(QuadWithTry.codename));
	this.parent = parent;
    }
    public String getCodeName() { return parent.getCodeName(); }
    public void clear(HMethod m) { cache.remove(m); parent.clear(m); }
    public HCode convert(HMethod m) {
	// check cache first
	if (cache.containsKey(m)) return (HCode) cache.get(m);
	// now see if we need to synthesize a code for this method.
	HClass hc = m.getDeclaringClass();
	HClass oa = hc.getLinker().forDescriptor("[Ljava/lang/Object;");
	if (hc.isArray() &&
	    !hc.getComponentType().isPrimitive() && // not primitive array
	    !hc.equals(oa) && // not java.lang.Object[].clone
	    m.getName().equals("clone") &&
	    m.getDescriptor().equals("()Ljava/lang/Object;")) {
	    // Create a Code which turns around and calls Object[].clone().
	    QuadWithTry qwt = new QuadWithTry(m, null) { };
	    HMethod hm = oa.getMethod("clone", new HClass[0]);
	    Temp thisT = new Temp(qwt.qf.tempFactory());
	    Quad q0 = new HEADER(qwt.qf, null);
	    Quad q1 = new METHOD(qwt.qf, null, new Temp[] { thisT }, 1);
	    Quad q2 = new CALL(qwt.qf, null, hm, new Temp[] { thisT },
	    		       thisT, null, false, true, new Temp[0]);
	    Quad q3 = new RETURN(qwt.qf, null, thisT);
	    Quad q4 = new FOOTER(qwt.qf, null, 2);
	    Quad.addEdge(q0, 0, q4, 0);
	    Quad.addEdge(q0, 1, q1, 0);
	    Quad.addEdges(new Quad[] { q1, q2, q3 });
	    Quad.addEdge(q3, 0, q4, 1);
	    qwt.quads = q0;
	    cache.put(m, qwt);
	    return qwt;
	} else return parent.convert(m); // not synthetic: use parent's code.
    }
}
