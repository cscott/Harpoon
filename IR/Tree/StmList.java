// StmList.java, created Wed Jan 13 21:14:57 1999 by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.Tree;

import harpoon.Temp.TempMap;


/**
 * <code>StmList</code>s for singly-linked lists of <code>Stm</code>s.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>, based on
 *          <i>Modern Compiler Implementation in Java</i> by Andrew Appel.
 * @version $Id: StmList.java,v 1.2 2002-02-25 21:05:41 cananian Exp $
 */
public final class StmList {
    /** The statement at this list entry. */
    public final Stm head;
    /** The next list entry. */
    public final StmList tail;
    /** List constructor. */
    public StmList(Stm head, StmList tail)
    { this.head=head; this.tail=tail; }

    public static StmList rename(StmList s, TreeFactory tf, TempMap tm,
				 Tree.CloneCallback cb) {
        if (s==null) return null;
	else return new StmList
	       ((Stm)s.head.rename(tf, tm, cb),
		rename(s.tail, tf, tm, cb));
    }
}



