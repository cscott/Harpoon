package harpoon.Temp;

/**
 * A <code>Label</code> represents a (symbolic) address in assembly language.
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: Label.java,v 1.3.2.5 1999-08-04 04:34:12 cananian Exp $
 */

public class Label  {
   private String name;
   private static int count;

  /**
   * a printable representation of the label, for use in assembly 
   * language output.
   */
   public String toString() {return name;}

  /**
   * Makes a new label that prints as "name".
   * Warning: avoid repeated calls to <tt>new Label(s)</tt> with
   * the same name <tt>s</tt>.
   */
   public Label(String n) {
	name=n;
   }

  /**
   * Makes a new label with an arbitrary name.
   */
   public Label() {
	this("L" + count++);
   }

   public boolean equals(Object o) {
       Label l;
       if (this==o) return true;
       if (null==o) return false;
       try { l=(Label) o; } catch (ClassCastException e) { return false; }
       return name.equals(l.name);
   }
}
