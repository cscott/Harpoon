// ExpList.java, created Wed Jan 13 21:14:57 1999 by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.Tree;

import harpoon.Temp.CloningTempMap;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * <code>ExpList</code>s form singly-linked lists of <code>Exp</code>s.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>, based on
 *          <i>Modern Compiler Implementation in Java</i> by Andrew Appel.
 * @version $Id: ExpList.java,v 1.1.2.7 1999-08-04 05:52:30 cananian Exp $
 */
public final class ExpList {
    /** The expression at this list entry. */
    public final Exp head;
    /** The next list entry. */
    public final ExpList tail;
    /** List constructor. */
    public ExpList(Exp head, ExpList tail) { this.head=head; this.tail=tail; }

    public static ExpList rename(ExpList e, 
				 TreeFactory tf, CloningTempMap ctm) {
        if (e==null) return null;
	else
	    return new ExpList
	      ((Exp)((e.head==null)?null:e.head.rename(tf, ctm)),
	       rename(e.tail, tf, ctm));
    }
    
    public static Set useSet(ExpList expList) {
	Set use = new HashSet();
	for (;expList!=null; expList=expList.tail)
	    use.addAll(expList.head.useSet());
	return use;
    }
}



