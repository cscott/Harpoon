package harpoon.Temp;

import java.util.Hashtable;

/** 
 * The <code>Temp</code> class represents a temporary
 * variable.  This class maintains static state to allow us to allocate
 * guaranteed-unique names for our temps.
 *
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: Temp.java,v 1.7 1998-08-22 05:46:44 cananian Exp $
 * @see TypeMap
 * @see TempList
 */
public class Temp {
  private static Hashtable table = new Hashtable();

  private String name;
  private static final String prefix = "t";

  /** Creates a unique temporary variable.
   */
  public Temp() { 
    this(null);
  }
  /** Creates a unique temporary with a suggested name.
   *  Trailing underscores will be stripped from the suggested name,
   *  and a digit string will be appended to make the name unique.
   *  @param m_prefix the name prefix.
   *                  If m_prefix is null, a default prefix ("t") will be used.
   */
  public Temp(String m_prefix) {
    // if prefix is null, use default prefix
    if (m_prefix==null) m_prefix = this.prefix;
    // Strip trailing underscores.
    while (m_prefix.charAt(m_prefix.length()-1)=='_')
      m_prefix = m_prefix.substring(0, m_prefix.length()-1);
    // Look up appropriate suffix from table.
    Integer i = (Integer) table.get(m_prefix);
    if (i==null) i = new Integer(0);
    // Create the name of this temp.
    this.name = m_prefix + "_" + i.toString();
    // update the table.
    table.put(m_prefix, new Integer(i.intValue()+1));
  }
  /** Creates a new temp based on the name of an existing temp. */
  public Temp(Temp t) {
    this(t.name().substring(0, t.name().lastIndexOf('_')));
  }

  /** Returns the name of this temporary */
  public String name() { return name; }

  /** Returns a string representation of this temporary. */
  public String toString() { return name(); }
}
// set emacs indentation style.
// Local Variables:
// c-basic-offset:2
// End:
