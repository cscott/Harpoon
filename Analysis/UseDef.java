// UseDef.java, created Thu Sep 10 15:17:10 1998 by cananian
package harpoon.Analysis;

import harpoon.Temp.Temp;
import harpoon.ClassFile.*;
import harpoon.IR.QuadSSA.Quad;
import harpoon.Util.UniqueVector;
import harpoon.Util.Util;

import java.util.Hashtable;
import java.util.Enumeration;
/**
 * <code>UseDef</code> objects map temps to the quads which use or define
 * them.  UseDefs cache results, so you should throw away your current
 * UseDef object and make another one if you make modifications to
 * the IR.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: UseDef.java,v 1.1 1998-09-10 19:39:41 cananian Exp $
 */

public class UseDef  {
    /** Creates a new, empty <code>UseDef</code>. */
    public UseDef() { }

    Hashtable analyzed = new Hashtable();
    Hashtable useMap = new Hashtable();
    Hashtable defMap = new Hashtable();

    void analyze(HMethod method) {
	// make sure we don't analyze a method multiple times.
	if (analyzed.containsKey(method)) return;

	analyze((harpoon.IR.QuadSSA.Code) harpoon.IR.QuadSSA.Code.
		convertFrom(method.getCode("bytecode")));
    }
    void analyze(harpoon.IR.QuadSSA.Code code) {
	// make sure we don't analyze a method multiple times.
	HMethod method = code.getMethod();
	if (analyzed.containsKey(method)) return;
	analyzed.put(method, method);

	Quad[] ql = (Quad[]) code.getElements();

	for (int i=0; i<ql.length; i++) {
	    Temp[] u = ql[i].use();
	    Temp[] d = ql[i].def();

	    // store def mapping.
	    for (int j=0; j<d.length; j++) {
		// only one quad per temp definition (SSA form).
		Util.assert(defMap.get(d[j])==null);
		defMap.put(d[j], ql[i]);
	    }
	    // store use mapping.
	    for (int j=0; j<u.length; j++) {
		// multiple quads per temp use.
		UniqueVector v = (UniqueVector) useMap.get(u[j]);
		if (v==null) { v = new UniqueVector(); useMap.put(u[j], v); }
		v.addElement(ql[i]);
	    }
	}
	// replace UniqueVectors with Quad arrays (to save space)
	Hashtable h = new Hashtable();
	for (Enumeration e = useMap.keys(); e.hasMoreElements(); ) {
	    Temp u = (Temp) e.nextElement();
	    UniqueVector v = (UniqueVector) useMap.get(u);
	    Util.assert(v!=null);
	    Quad uses[] = new Quad[v.size()];
	    v.copyInto(uses);
	    h.put(u, uses);
	}
	useMap = h;
    }

    /** Return the Quad where a given Temp is defined */
    public Quad defSite(HMethod m, Temp t) {
	analyze(m);
	return (Quad) defMap.get(t);
    }
    /** Return an array of Quads where a given Temp is used. */
    public Quad[] useSites(HMethod m, Temp t) {
	analyze(m);
	return (Quad[]) useMap.get(t);
    }
}
