// Set.java, created Tue Sep 15 19:28:05 1998 by cananian
package harpoon.Util;

import java.util.Hashtable;
import java.util.Enumeration;
/**
 * <code>Set</code>
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: Set.java,v 1.1 1998-09-16 00:45:06 cananian Exp $
 */

public class Set implements Worklist {
    Hashtable h;
    /** Creates a <code>SetTable</code>. */
    public Set() {
        h = new Hashtable();
    }
    public void remove(Object o) {
	h.remove(o);
    }
    public void union(Object o) {
	h.put(o, o);
    }
    public Object push(Object o) {
	return h.put(o, o);
    }
    public boolean contains(Object o) {
	return h.containsKey(o);
    }
    public boolean isEmpty() {
	return h.isEmpty();
    }
    public int size() { 
	return h.size(); 
    }
    public Object pull() {
	Object o = h.keys().nextElement();
	h.remove(o);
	return o;
    }
    public void copyInto(Object[] oa) {
	int i=0;
	for(Enumeration e = h.keys(); e.hasMoreElements(); )
	    oa[i++] = e.nextElement();
    }
    public Enumeration elements() { return h.keys(); }
}
