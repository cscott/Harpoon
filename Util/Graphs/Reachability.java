// Reachability.java, created Mon Apr  1 21:24:04 2002 by salcianu
// Copyright (C) 2000 Alexandru SALCIANU <salcianu@MIT.EDU>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Util.Graphs;

import java.util.List;
import java.util.LinkedList;
import java.util.Set;
import java.util.HashSet;
import java.util.Iterator;

/**
 * The <code>Reachability</code> class computes reachability in graphs
 * given only by a navigator (a way of obtaining in/out edges).
 * 
 * @author  Alexandru SALCIANU <salcianu@MIT.EDU>
 * @version $Id: Reachability.java,v 1.2 2003-05-08 01:10:59 cananian Exp $
 */
public abstract class Reachability {
    
    /** Creates a <code>Reachability</code>. */
    private Reachability() { }

    /** Interface for passing a method as parameter to a
     *  <code>Reachability</code> object.
     *  @see forAllReachable */
    public interface Action {
	public void visit(Object obj);
    }

    /** Performs an action on each object reachanle from a set of
        roots. */
    public static void forAllReachable(Set roots, Navigator nav,
				       Action action) {
	Set seen = new HashSet(roots);
	for(Iterator it = roots.iterator(); it.hasNext(); ) {
	    Object obj = it.next();
	    action.visit(obj);
	}
	
	LinkedList list = new LinkedList(roots);
	while(!list.isEmpty()) {
	    Object obj = list.removeFirst();
	    Object next[] = nav.next(obj);
	    for(int i = 0; i < next.length; i++) {
		Object succ = next[i];
		if(seen.add(succ)) {
		    list.addLast(succ);
		    action.visit(succ);
		}
	    }
	}
    }

}
