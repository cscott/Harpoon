// Temp.java, created Tue Jul 28  1:09:44 1998 by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Temp;

import java.util.HashMap;
import java.util.Map;
import harpoon.Util.ArrayFactory;
import harpoon.Util.ReferenceUnique;
import harpoon.Util.Util;

/** 
 * The <code>Temp</code> class represents a temporary
 * variable.  This class maintains static state to allow us to allocate
 * guaranteed-unique names for our temps.
 *
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: Temp.java,v 1.16.2.3 2002-03-14 01:58:16 cananian Exp $
 * @see harpoon.Analysis.Maps.TypeMap
 * @see harpoon.Analysis.Maps.ConstMap
 * @see TempList
 */
public class Temp implements Cloneable, Comparable<Temp>, ReferenceUnique, java.io.Serializable {
  final TempFactory tf;
  final String name;
  final int hashcode;

  /** A <code>harpoon.Util.Indexer</code> specifically for working
      <b>only</b> with <code>Temp</code>s generated by
      <code>this.tempFactory()</code>. 
  */
  public static harpoon.Util.Indexer INDEXER = 
    new harpoon.Util.Indexer() {
      public int getID(Object o) {
	return ((Temp)o).id;
      }
    };
  private int id;

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
    assert tf != null : "TempFactory cannot be null";
    this.tf = tf;
    this.name = tf.getUniqueID(prefix!=null?prefix:default_name);
    this.hashcode = tf.getScope().hashCode() ^ name.hashCode();
    this.id = tf.newID();
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
  public int compareTo(Temp o) {
    return fullname().compareTo(o.fullname());
  }

  /** Returns a new <code>TempFactory</code> with the given scope. */
  public static TempFactory tempFactory(final String scope) {
    abstract class SerializableTempFactory extends TempFactory
      implements java.io.Serializable { /* only declare inheritance */ }
    return new SerializableTempFactory() {
      // declare table as HashMap, not Map, for slight efficiency gain.
      private final HashMap table = new HashMap();
      public String getScope() { return scope; }
      protected synchronized String getUniqueID(String suggestion) {
	// strip digits from the end of the suggestion.
	int lastchar = suggestion.length();
	char c;
	do {
	  c = suggestion.charAt(--lastchar);
	} while (c >= '0' && c <= '9');
	suggestion = suggestion.substring(0, 1+lastchar);
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
  public static final ArrayFactory<Temp> arrayFactory =
    new ArrayFactory<Temp>() {
      public Temp[] newArray(int len) { return new Temp[len]; }
    };
  /** Returns an array of <code>Temp[]</code>s. */
  public static final ArrayFactory<Temp[]> doubleArrayFactory =
    new ArrayFactory<Temp[]>() {
      public Temp[][] newArray(int len) { return new Temp[len][]; }
    };

}
// set emacs indentation style.
// Local Variables:
// c-basic-offset:2
// End:
