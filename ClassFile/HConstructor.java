// HConstructor.java, created Sat Aug  1  4:54:58 1998 by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.ClassFile;

import harpoon.Util.ArrayFactory;

/**
 * An <code>HConstructor</code> provides information about a single
 * constructor for a class.
 *
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: HConstructor.java,v 1.10.2.1 2002-03-10 08:01:57 cananian Exp $
 * @see HMethod
 * @see HMember
 * @see HClass
 */
public interface HConstructor extends HMethod {
  /**
   * Returns the name of this constructor, as a string.  This is always
   * the string "<code>&lt;init&gt;</code>".
   */
  public String getName();

  /**
   * Returns a hashcode for this Constructor.  The hashcode is computed as
   * the exclusive-or of the hashcodes for the underlying constructor's 
   * declaring class and the constructor's descriptor string.
   */
  public int hashCode();

  /**
   * Return a string describing this Constructor.  The string is formatted
   * as: the constructor access modifiers, if any, followed by the
   * fully-qualified name of the declaring class, followed by a 
   * parenthesized, comma-separated list of the constructor's formal
   * parameter types.  For example: <p>
   * <DL><DD><CODE>public java.util.Hashtable(int,float)</CODE></DL><p>
   * The only possible modifiers for constructors are the access modifiers
   * <code>public</code>, <code>protected</code>, or <code>private</code>.
   * Only one of these may appear, or none if the constructor has default
   * (<code>package</code>) access.
   */
  public String toString();

  /** Array factory: returns new <code>HConstructor[]</code>. */
  public static final ArrayFactory<HConstructor> arrayFactory =
    Factories.hconstructorArrayFactory;
}
