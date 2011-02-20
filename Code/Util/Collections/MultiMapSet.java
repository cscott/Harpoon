// MultiMapSet.java, created Sat Nov  3 15:36:03 2001 by cananian
// Copyright (C) 2000 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Util.Collections;

import java.util.Collection;
/**
 * A <code>MultiMapSet</code> is a <code>java.util.Set</code> of
 * <code>Map.Entry</code>s which can also be accessed as a
 * <code>MultiMap</code>.  Use the <code>entrySet()</code> method
 * of the <code>MultiMap</code> to get back the <code>MultiMapSet</code>.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: MultiMapSet.java,v 1.3 2002-04-10 03:07:13 cananian Exp $
 */
public interface MultiMapSet<K,V> extends MapSet<K,V> {
    public MultiMap<K,V> asMap();
    public MultiMap<K,V> asMultiMap();
}
