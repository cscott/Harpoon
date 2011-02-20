// Label.java, created Tue Jul 28  1:09:44 1998 by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Temp;

/**
 * A <code>Label</code> represents a (symbolic) address in assembly language.
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: Label.java,v 1.4 2002-02-25 21:07:05 cananian Exp $
 */

public class Label  {
   public final String name;
   private static int count;

  /**
   * a printable representation of the label, for use in assembly 
   * language output.  Note that the returned <code>String</code>
   * consists only of a name; any platform specific modifiers (such as
   * a colon) need to be appended by the code calling
   * <code>toString()</code>.
   */
   public String toString() {return name;}

  /**
   * Makes a new label that prints as "name".
   * Repeated calls to <tt>new Label(s)</tt> with
   * the same name <tt>s</tt> will return labels which
   * are equal to each other according to the <code>equals()</code>
   * method, but not reference equivalent.
   */
   public Label(String n) {
	name=n;
   }

  /**
   * Makes a new label with an arbitrary name.
   */
   public Label() {
	this(".L" + count++);
   }

   public boolean equals(Object o) {
       Label l;
       if (this==o) return true;
       if (null==o) return false;
       try { l=(Label) o; } catch (ClassCastException e) { return false; }
       return name.equals(l.name);
   }
   // make hashCode consistent with equals()
   public int hashCode() { return name.hashCode(); }
}
