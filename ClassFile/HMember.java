// HMember.java, created Fri Jul 31  9:33:48 1998 by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.ClassFile;

import harpoon.Util.ArrayFactory;
/**
 * <Code>HMember</code> is an interface that reflects identifying information
 * about a single member (a field or a method) or a constructor.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: HMember.java,v 1.6 2002-02-25 21:03:03 cananian Exp $
 * @see HClass
 * @see HField
 * @see HMethod
 * @see HConstructor
 */
public interface HMember extends java.lang.Comparable {
  /** 
   * Returns the <code>HClass</code> object representing the class or
   * interface that declares the member or constructor represented by this
   * <code>HMember</code>.
   */
  public abstract HClass getDeclaringClass();
  /**
   * Returns the type descriptor for this member.
   */
  public abstract String getDescriptor();
  /**
   * Returns the simple name of the underlying member or constructor
   * represented by this <code>HMember</code>.
   */
  public abstract String getName();
  /**
   * Returns the Java language modifiers for the member of constructor
   * represented by this <code>HMember</code>, as an integer.  The
   * <code>Modifier</code> class should be used to decode the
   * modifiers in the integer.
   * @see java.lang.reflect.Modifier
   */
  public abstract int getModifiers();

  /**
   * Returns a hashcode for this <code>HMember</code>.  This is
   * computed as the exclusive-or of the hashcodes for the
   * underlying member's declaring class, name, and descriptor
   * string.
   */
  public abstract int hashCode();

  /**
   * Indicates whether this field or method is 'real' or if it has
   * been synthesized by the compiler in order to implement scoping
   * of inner classes.
   */
  public abstract boolean isSynthetic();

  /** Compares two <code>HMember</code>s lexicographically; first by
   *  declaring class, then by name, and lastly by descriptor. */
  public abstract int compareTo(Object o);
  // implementation of a member comparator, for consistency among 
  // implementations.
  static final java.util.Comparator memberComparator = new MemberComparator();
  /** Implementation of <code>java.util.Comparator</code> for objects
   *  implementing <code>HMember</code>, for consistency among
   *  implementations.  Compares two <code>HMember</code>s lexicographically,
   *  first by declaring class, then by name, and lastly by descriptor. */
  static class MemberComparator implements java.util.Comparator {
    public int compare(Object o1, Object o2) {
	HMember hm1 = (HMember) o1, hm2 = (HMember) o2;
	int c = hm1.getDeclaringClass().compareTo(hm2.getDeclaringClass());
	if (c!=0) return c;
	c = hm1.getName().compareTo(hm2.getName());
	if (c!=0) return c;
	c = hm1.getDescriptor().compareTo(hm2.getDescriptor());
	return c;
    }
  }

  /** Array factory: returns new <code>HMember[]</code>. */
  public static final ArrayFactory arrayFactory =
    Factories.hmemberArrayFactory;
}
