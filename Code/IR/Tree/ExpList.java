// ExpList.java, created Thu Jan 14  0:54:59 1999 by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.

package harpoon.IR.Tree;

import harpoon.Temp.TempMap;

import harpoon.Util.Collections.UnmodifiableIterator;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List; 
import java.util.ListIterator; 
import java.util.Set;

/**
 * <code>ExpList</code>s form singly-linked lists of <code>Exp</code>s.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>, based on
 *          <i>Modern Compiler Implementation in Java</i> by Andrew Appel.
 * @version $Id: ExpList.java,v 1.3 2002-08-30 22:39:44 cananian Exp $
 */
public final class ExpList {
    /** The expression at this list entry. */
    public final Exp head;
    /** The next list entry. */
    public final ExpList tail;
    /** List constructor. */
    public ExpList(Exp head, ExpList tail) { this.head=head; this.tail=tail; }

    public static ExpList toExpList(List list) { 
	ExpList THIS; 

	if (list.isEmpty()) {
	    THIS = null; 
	}
	else { 
	    THIS = null; 
	    for (ListIterator i = list.listIterator(list.size()); 
		 i.hasPrevious();) { 
		THIS = new ExpList((Exp)i.previous(), THIS); 
	    }
	}
	return THIS; 
    }

    /** FSK: didn't use standard JDK instance-local iterators because 
	ExpLists are allowed to be null, and so such a definition
	would encourage attempted method-calls on null.
    */
    public static Iterator iterator(final ExpList el) {
	return new UnmodifiableIterator() {
	    ExpList curr = el;
	    public boolean hasNext() {
		return (curr != null);
	    }
	    public Object next() {
		Object o = curr.head;
		curr = curr.tail;
		return o;
	    }
	};
    }
    public static int size(ExpList e) {
	int i=0;
	for (ExpList ep = e; ep!=null; ep=ep.tail)
	    i++;
	return i;
    }

    public static ExpList replace(ExpList e, Exp eOld, Exp eNew) { 
	if (e==null) return null; 
	else
	    return new ExpList
		(e.head == eOld ? eNew : e.head, replace(e.tail, eOld, eNew));
    }

    public static ExpList rename(ExpList e, TreeFactory tf, TempMap tm,
				 Tree.CloneCallback cb) {
        if (e==null) return null;
	else
	    return new ExpList
	      ((Exp)e.head.rename(tf, tm, cb),
	       rename(e.tail, tf, tm, cb));
    }
    
    public String toString() { 
	StringBuffer sb = new StringBuffer(); 
	sb.append("EXPLIST<"); 
	sb.append(this.head == null ? "null" : 
		  this.head.toString() + ", "); 
	sb.append(this.tail == null ? "null" : this.tail.toString()); 

	return sb.toString(); 
    }
}



