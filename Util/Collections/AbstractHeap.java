// AbstractHeap.java, created Sat Feb 12 09:41:17 2000 by cananian
// Copyright (C) 2000 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Util.Collections;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
/**
 * <code>AbstractHeap</code> provides a skeletal implementation of
 * the <code>Heap</code> interface, to minimize the effort required
 * to implement this interface.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: AbstractHeap.java,v 1.1.2.1 2000-02-12 14:57:15 cananian Exp $
 */
public abstract class AbstractHeap implements Heap {
    /** Sole constructor, for invocation by subclass constructors
     *  (typically implicitly). */
    protected AbstractHeap() { }

    // abstract methods:
    public abstract Map.Entry insert(Object key, Object value);
    public abstract Map.Entry minimum();
    public abstract void decreaseKey(Map.Entry me, Object newkey);
    public abstract void delete(Map.Entry me);
    public abstract int size();
    public abstract Collection entries();
    public abstract void clear();

    // methods which we helpfully provide for you:
    public Map.Entry extractMinimum() {
	Map.Entry e = minimum();
	delete(e);
	return e;
    }
    public void union(Heap h) {
	for (Iterator it=h.entries().iterator(); it.hasNext(); ) {
	    Map.Entry e = (Map.Entry) it.next();
	    insert(e.getKey(), e.getValue());
	}
	h.clear();
    }
    public boolean isEmpty() { return size()==0; }
    public int hashCode() { return 1+entries().hashCode(); }
    public boolean equals(Object o) {
	if (o instanceof Heap) return entries().equals(((Heap)o).entries());
	return false;
    }
    public String toString() { return entries().toString(); }
}
