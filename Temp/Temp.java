package harpoon.Temp;

import java.util.Hashtable;

/** 
 * The <code>Temp</code> class represents a temporary
 * variable.  This class maintains static state to allow us to allocate
 * guaranteed-unique names for our temps.
 *
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: Temp.java,v 1.10 1998-09-03 01:45:19 cananian Exp $
 * @see TypeMap
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
  /** Rename this temp. */
  public void rename(String prefix) {
    Temp lamb = new Temp(prefix);
    this.name = lamb.name;
    this.index= lamb.index;
    lamb = null; // destroy the sacrificial lamb.
  }
  /** Rename this temp. */
  public void rename(Temp t) { rename(t.name); }

  /** Returns the name of this temporary */
  public String name() { return name + "_" + index; }

  /** Returns a string representation of this temporary. */
  public String toString() { return name(); }
}
// set emacs indentation style.
// Local Variables:
// c-basic-offset:2
// End:
