// AuxUniqueFIFO.java, created Sun Dec  6 18:45:39 1998 by marinov
// Copyright (C) 1998 Darko Marinov <marinov@lcs.mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.TypeInference;

import java.util.Hashtable;
import java.util.EmptyStackException;
import harpoon.Util.FIFO;
/**
 * <code>AuxUniqueFIFO</code>
 * 
 * @author  Darko Marinov <marinov@lcs.mit.edu>
 * @version $Id: AuxUniqueFIFO.java,v 1.1.2.4 2000-01-14 12:32:54 cananian Exp $
 */

public class AuxUniqueFIFO {
    int max;
    FIFO[] list;
    int cur;
    Hashtable all = new Hashtable();
    /** Creates a <code>AuxUniqueFIFO</code>. */
    public AuxUniqueFIFO() { this(8); }
    public AuxUniqueFIFO(int n) { 
	max = n; 
	list = new FIFO[n+1];
	for (int i=0; i<=n; i++) list[i] = new FIFO();
	cur = -1;
    }
    //Object push(Object o) { }
    void push(Object o, int p) { 
	if (!all.containsKey(o)) {
	    if (p>max) p = max;
	    if (p<0) p = 0;
	    list[p].push(o);
	    if (p>cur) cur = p;
	    all.put(o, o);
	}
    }
    Object pull() { 
	while (cur>=0) {
	    if (!list[cur].isEmpty()) {
		Object o = list[cur].pull();
		all.remove(o);
		return o;
	    }
	    cur--;
	} 
	throw new EmptyStackException();
    }
    //boolean contains(Object o) { }
    boolean isEmpty() { 
	while (cur>=0) {
	    if (!list[cur].isEmpty()) return false;
	    cur--;
	}
	return true;
    }
}
