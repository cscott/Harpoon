// TempList.java, created by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Temp;

/** A singly-linked list of temporary variables.
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: TempList.java,v 1.3.2.1 1999-08-04 04:34:12 cananian Exp $
 */
public class TempList {
   public Temp head;
   public TempList tail;
   public TempList(Temp h, TempList t) {head=h; tail=t;}
}

