// HConstructor.java, created Sat Aug  1  4:54:58 1998 by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.ClassFile;

import harpoon.Util.ArrayFactory;

import java.lang.reflect.Modifier;

/**
 * An <code>HConstructorImpl</code> is a basic implementation of
 * <code>HMethod</code>.
 *
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: HConstructorImpl.java,v 1.2 2002-02-25 21:03:03 cananian Exp $
 * @see HConstructor
 */
abstract class HConstructorImpl extends HMethodImpl implements HConstructor {
  HConstructorImpl() { name="<init>"; returnType=HClass.Void; }
  /**
   * Returns the name of this constructor, as a string.  This is always
   * the string "<code>&lt;init&gt;</code>".
   */
  public String getName() { return "<init>"/*hclass.getName()*/; }

  /**
   * Returns a hashcode for this Constructor.  The hashcode is computed as
   * the exclusive-or of the hashcodes for the underlying constructor's 
   * declaring class and the constructor's descriptor string.
   */
  public int hashCode() { return hashCode(this); }
  // separated out for re-use
  static int hashCode(HConstructor hc) {
    return hc.getDeclaringClass().hashCode() ^ hc.getDescriptor().hashCode(); 
  }

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
  public String toString() { return toString(this); }
  // separated out for re-use.
  static String toString(HConstructor hc) {
    StringBuffer r = new StringBuffer();
    int m = hc.getModifiers();
    if (m!=0) {
      r.append(Modifier.toString(m));
      r.append(' ');
    }
    r.append(HClass.getTypeName(hc.getDeclaringClass()));
    r.append('(');
    HClass hcp[] = hc.getParameterTypes();
    for (int i=0; i<hcp.length; i++) {
      r.append(HClass.getTypeName(hcp[i]));
      if (i<hcp.length-1)
	r.append(',');
    }
    r.append(')');
    HClass ecp[] = hc.getExceptionTypes();
    if (ecp.length > 0) {
      r.append(" throws ");
      for (int i=0; i<ecp.length; i++) {
	r.append(ecp[i].getName()); // can't be primitive or array type.
	if (i<ecp.length-1)
	  r.append(',');
      }
    }
    return r.toString();
  }
}
