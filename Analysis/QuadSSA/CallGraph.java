// CallGraph.java, created Sun Oct 11 12:56:36 1998 by cananian
package harpoon.Analysis.QuadSSA;

import harpoon.ClassFile.*;
import harpoon.IR.QuadSSA.*;
import harpoon.Util.Set;
import harpoon.Util.Worklist;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;
/**
 * <code>CallGraph</code> constructs a simple directed call graph.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: CallGraph.java,v 1.1 1998-10-11 23:43:21 cananian Exp $
 */

public class CallGraph  {
    final ClassHierarchy ch;
    /** Creates a <code>CallGraph</code>. */
    public CallGraph(ClassHierarchy ch) { this.ch = ch; }
    
    public HMethod[] calls(final HMethod m) {
	HMethod[] retval = (HMethod[]) cache.get(m);
	if (retval==null) {
	    final Vector v = new Vector();
	    final HCode hc = m.getCode("quad-ssa");
	    for (Enumeration e = hc.getElementsE(); e.hasMoreElements(); ) {
		Quad q = (Quad) e.nextElement();
		if (!(q instanceof CALL)) continue;
		HMethod cm = ((CALL)q).method;
		// all methods of children of this class are reachable.
		Worklist W = new Set();
		W.push(cm.getDeclaringClass());
		while (!W.isEmpty()) {
		    HClass c = (HClass) W.pull();
		    // if this class overrides the method, add it to vector.
		    try {
			v.addElement(c.getDeclaredMethod(cm.getName(),
							 cm.getDescriptor()));
		    } catch (NoSuchMethodError nsme) { }
		    // recurse through all children of this method.
		    HClass[] child = ch.children(c);
		    for (int i=0; i<child.length; i++)
			W.push(child[i]);
		}
	    }
	    // finally, copy result vector to retval array.
	    retval = new HMethod[v.size()];
	    v.copyInto(retval);
	    // and cache result.
	    cache.put(m, retval);
	}
	return retval;
    }
    final private Hashtable cache = new Hashtable();
}
