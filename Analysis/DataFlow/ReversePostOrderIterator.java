// ReversePostOrderIterator.java, created Tue Jul 27 17:33:06 1999 by duncan
// Copyright (C) 1998 John Whaley <jwhaley@alum.mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.DataFlow;

/**
 * ReversePostOrderIterator
 *
 * @author  John Whaley <jwhaley@alum.mit.edu>
 * @version $Id: ReversePostOrderIterator.java,v 1.1.2.7 2001-06-17 23:06:45 cananian Exp $
 */

import harpoon.Analysis.BasicBlock;
import harpoon.Util.EnumerationIterator;
import harpoon.Util.Util;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.Stack;

public class ReversePostOrderIterator implements Iterator {

    public static boolean DEBUG = false;
    public static void db(String s) { System.out.println(s); }
    
    Stack order;
    
    public ReversePostOrderIterator(BasicBlock start) {
	Stack iter_stack = new Stack();
	Stack bb_stack   = new Stack();
	order = new Stack();
	Set done = new HashSet();
	done.add(start); bb_stack.push(start); 
	iter_stack.push(new EnumerationIterator(start.next()));
	while (!bb_stack.isEmpty()) {
	    Util.assert(bb_stack.size() == iter_stack.size());
	    for (Iterator i = (Iterator)iter_stack.pop(); i.hasNext();) {
		BasicBlock bb2 = (BasicBlock)i.next();
		if (!done.contains(bb2)) {
		    if (DEBUG) db("visiting "+bb2+" for the first time");
		    done.add(bb2);
		    bb_stack.push(bb2);
		    iter_stack.push(i);
		    i = new EnumerationIterator(bb2.next());
		}
	    }
	    Object o = bb_stack.pop();
	    if (DEBUG) db("leaving "+o);
	    order.push(o);
	}
    }
    
    public boolean hasNext() { 
	return !order.empty();
    }
    
    public Object next() {
	return order.pop();
    }

    public void remove() { throw new UnsupportedOperationException(); }
    
    public ReversePostOrderIterator copy() {
	ReversePostOrderIterator r = new ReversePostOrderIterator();
	r.order = (Stack)order.clone();
	return r;
    }

    private ReversePostOrderIterator() {}
    
}




