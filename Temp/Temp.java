package Temp;

import Type.Type;
import Type.TypedObject;
import java.util.Hashtable;

/** 
 * The <code>Temp</code> class represents a (strongly-typed) temporary
 * variable.  This class maintains static state to allow us to allocate
 * guaranteed-unique names for our temps.
 *
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: Temp.java,v 1.1 1998-07-28 05:09:44 cananian Exp $
 * @see TempList
 */
public class Temp implements TypedObject {
  private static Hashtable table = new Hashtable();

  private String name;
  private Type type;
  private static final String prefix = "t";

  /** Creates a unique temporary with the specified type.
   *  @param type the type.
   */
  public Temp(Type type) { 
    this(null, type);
  }
  /** Creates a unique temporary with a suggested name and specified type.
   *  Trailing underscores will be stripped from the suggested name.
   *  @param m_prefix the name prefix.
   *                  If m_prefix is null, "t" will be used.
   *  @param type     the type.
   */
  public Temp(String m_prefix, Type type) {
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

    this.type = type;
  }

  /** Returns the name of this temporary */
  public String name() { return name; }
  /** Returns the datatype of this temporary. */
  public Type   type() { return type; }

  /** Returns a string representation of this temporary. */
  public String toString() { return name(); }
}

