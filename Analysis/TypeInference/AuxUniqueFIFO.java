// AuxUniqueFIFO.java, created Sun Dec  6 18:45:39 1998 by marinov
// Copyright (C) 1998 Darko Marinov <marinov@lcs.mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.TypeInference;

import java.util.HashSet;
import java.util.List;
import java.util.LinkedList;
import java.util.Set;
import java.util.EmptyStackException;
/**
 * <code>AuxUniqueFIFO</code>
 * 
 * @author  Darko Marinov <marinov@lcs.mit.edu>
 * @version $Id: AuxUniqueFIFO.java,v 1.2 2002-02-25 21:00:36 cananian Exp $
 */

public class AuxUniqueFIFO {
    int max;
    LinkedList[] list; // elements used as FIFOs
    int cur;
    Set all = new HashSet();
    /** Creates a <code>AuxUniqueFIFO</code>. */
    public AuxUniqueFIFO() { this(8); }
    public AuxUniqueFIFO(int n) { 
	max = n; 
	list = new LinkedList[n+1];
	for (int i=0; i<=n; i++) list[i] = new LinkedList();
	cur = -1;
    }
    //Object push(Object o) { }
    void push(Object o, int p) { 
	if (!all.contains(o)) {
	    if (p>max) p = max;
	    if (p<0) p = 0;
	    list[p].add(o);
	    if (p>cur) cur = p;
	    all.add(o);
	}
    }
    Object pull() { 
	while (cur>=0) {
	    if (!list[cur].isEmpty()) {
		Object o = list[cur].removeFirst(); // use as FIFO
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
