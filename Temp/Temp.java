// Temp.java, created by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Temp;

import java.util.Hashtable;
import harpoon.Util.ArrayFactory;

/** 
 * The <code>Temp</code> class represents a temporary
 * variable.  This class maintains static state to allow us to allocate
 * guaranteed-unique names for our temps.
 *
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: Temp.java,v 1.14.2.1 1998-11-30 21:21:03 cananian Exp $
 * @see harpoon.Analysis.Maps.TypeMap
 * @see harpoon.Analysis.Maps.ConstMap
 * @see TempList
 */
public class Temp {
  private static Hashtable table = new Hashtable();

  private String name;
  private int index;

  private static final String prefix = "t";

  /** Creates a unique temporary variable, using default prefix ("t").
   */
  public Temp() { 
    this(prefix); /* use default prefix. */
  }
  /** Creates a unique temporary with a suggested name.
   *  Trailing underscores will be stripped from the suggested name,
   *  and a digit string will be appended to make the name unique.
   *  @param m_prefix the name prefix.
   *                  <code>m_prefix</code> may not be null.
   */
  public Temp(String m_prefix) {
    // Strip trailing underscores.
    while (m_prefix.charAt(m_prefix.length()-1)=='_')
      m_prefix = m_prefix.substring(0, m_prefix.length()-1);
    // Look up appropriate suffix from table.
    Integer i = (Integer) table.get(m_prefix);
    if (i==null) i = new Integer(0);

    // Initialize the fields of this temp.
    this.name = m_prefix;
    this.index= i.intValue();

    // update the table.
    table.put(m_prefix, new Integer(i.intValue()+1));
  }
  /** Creates a new temp based on the name of an existing temp. */
  public Temp(Temp t) {
    this(t.name);
  }

  /** Returns the name of this temporary */
  public String name() { return name + "_" + index; }

  /** Returns a string representation of this temporary. */
  public String toString() { return name(); }

  /** Returns a hashcode for this temporary.
   *  The hashcode is the same as the hashcode of the hashcode's name.
   */
  public int hashCode() { return name().hashCode(); }

  // Array Factory interface:

  /** Returns an array of <code>Temp</code>s. */
  public static final ArrayFactory arrayFactory =
    new ArrayFactory() {
      public Object[] newArray(int len) { return new Temp[len]; }
    };
  /** Returns an array of <code>Temp[]</code>s. */
  public static final ArrayFactory doubleArrayFactory =
    new ArrayFactory() {
      public Object[] newArray(int len) { return new Temp[len][]; }
    };

  /** For debugging purposes: reset all temp variable counters to zero. */
  //public static void clear() { table.clear(); }
}
// set emacs indentation style.
// Local Variables:
// c-basic-offset:2
// End:
