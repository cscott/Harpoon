// LabelList.java, created Fri Aug 28  1:09:44 1998 by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Temp;

/**
 * A <code>LabelList</code> is a simple singly-linked list of
 * <code>Label</code>s.
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: LabelList.java,v 1.3.2.4 1999-08-04 05:52:37 cananian Exp $
 */
public final class LabelList {
    /* The head of the list. */
   public final Label head;
    /* The tail of the list. */
   public final LabelList tail;
    /* Constructor. */
   public LabelList(Label head, LabelList tail)
   { this.head=head; this.tail=tail; }
}

