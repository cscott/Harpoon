// TempList.java, created Tue Jul 28  1:09:45 1998 by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Temp;

/** A singly-linked list of temporary variables.
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: TempList.java,v 1.4 2002-02-25 21:07:05 cananian Exp $
 */
public class TempList {
   public Temp head;
   public TempList tail;
   public TempList(Temp h, TempList t) {head=h; tail=t;}
   public String toString() {
       return head + ((tail==null)?"":", "+tail);
   }
}

