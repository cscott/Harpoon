// AbstractMapEntry.java, created Tue Feb 23 16:34:46 1999 by cananian
// Copyright (C) 1999 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Util;

import java.util.Map;
/**
 * An <code>AbstractMapEntry</code> takes care of most of the grunge work
 * involved in subclassing <code>java.util.Map.Entry</code>.  For a 
 * modifiable entry, you must subclass <code>setValue</code>; the default
 * implementation throws an <code>UnsupportedOperationException</code>.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: AbstractMapEntry.java,v 1.1.2.1 1999-02-23 23:03:07 cananian Exp $
 */
public abstract class AbstractMapEntry implements Map.Entry {
    public abstract Object getKey();
    public abstract Object getValue();
    public Object setValue(Object value) {
	throw new UnsupportedOperationException();
    }
    public boolean equals(Object o) {
	if (!(o instanceof Map.Entry)) return false;
	Map.Entry e1 = this;
	Map.Entry e2 = (Map.Entry) o;
	return 
	    (e1.getKey()==null ?
	     e2.getKey()==null : e1.getKey().equals(e2.getKey())) &&
	    (e1.getValue()==null ?
	     e2.getValue()==null : e1.getValue().equals(e2.getValue()));
    }
    public int hashCode() {
	return
	    (getKey()==null   ? 0 : getKey().hashCode()) ^
	    (getValue()==null ? 0 : getValue().hashCode());
    }
}
