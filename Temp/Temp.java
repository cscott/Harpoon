// Temp.java, created Tue Jul 28  1:09:44 1998 by cananian
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
 * @version $Id: Temp.java,v 1.14.2.12 1999-08-04 21:46:12 pnkfelix Exp $
 * @see harpoon.Analysis.Maps.TypeMap
 * @see harpoon.Analysis.Maps.ConstMap
 * @see TempList
 */
public class Temp implements Cloneable, Comparable {
  /*final*/ TempFactory tf;
  /*final*/ String name;
  /*final*/ int hashcode;

  /** Default temp name. */
  private static final String default_name = "t";

  /** Creates a unique temporary variable, using default prefix ("t").
   */
  public Temp(final TempFactory tf) { 
    this(tf, null); /* use default prefix. */
  }
  /** Creates a new temp based on the name of an existing temp. */
  public Temp(final Temp t) {
    this(t.tf, t.name);
  }
  /** Creates a unique temporary with a suggested name.
   *  Trailing digits will be stripped from the suggested name,
   *  and a digit string will be appended to make the name unique
   *  within the scope of the <code>TempFactory</code>.
   *  @param prefix the name prefix.
   *                  <code>prefix</code> may not be null.
   */
  public Temp(final TempFactory tf, final String prefix) {
    this.tf = tf;
    this.name = tf.getUniqueID(prefix!=null?prefix:default_name);
    this.hashcode = tf.getScope().hashCode() ^ name.hashCode();
  }

  /** Returns the full name of this temporary, including scope information. */
  public String fullname() { return tf.getScope()+":"+name; }

  /** Returns the common name of this <code>Temp</code>; scope information
   *  not included. */
  public String name() { return name; }
  /** Returns a string representation of this temporary. */
  public String toString() { return name; }
  /** Returns the tempFactory of this temporary. */
  public TempFactory tempFactory() { return tf; }

  /** Clones a <code>Temp</code> into a different <code>TempFactory</code>. */
  public Temp clone(TempFactory tf) {
    return new Temp(tf, this.name);
  }
  /** Clones a <code>Temp</code> using the same <code>TempFactory</code>. */
  public Object clone() { return clone(this.tf); }

  /** Returns a hashcode for this temporary.
   *  The hashcode is formed from the scope name and the temporary name.
   */
  public int hashCode() { return hashcode; }

  /** Comparable interface: sorted by fullname(). */
  public int compareTo(Object o) {
    return fullname().compareTo(((Temp)o).fullname());
  }

  /** Returns a new <code>TempFactory</code> with the given scope. */
  public static TempFactory tempFactory(final String scope) {
    return new TempFactory() {
      private Hashtable table = new Hashtable();
      public String getScope() { return scope; }
      protected synchronized String getUniqueID(String suggestion) {
	// strip digits from the end of the suggestion.
	while (suggestion.charAt(suggestion.length()-1)>='0' &&
	       suggestion.charAt(suggestion.length()-1)<='9')
	  suggestion = suggestion.substring(0, suggestion.length()-1);
	// look up appropriate suffix.
	Integer i = (Integer) table.get(suggestion);
	if (i==null) i = new Integer(0);
	// update the table.
	table.put(suggestion, new Integer(i.intValue()+1));
	// return the unique identifier.
	return suggestion+i;
      }
    };
  }

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

}
// set emacs indentation style.
// Local Variables:
// c-basic-offset:2
// End:
