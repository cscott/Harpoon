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
 * @version $Id: HMember.java,v 1.5.2.3 2000-01-13 23:47:47 cananian Exp $
 * @see HClass
 * @see HField
 * @see HMethod
 * @see HConstructor
 */
public interface HMember {
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
   * Indicates whether this field or method is 'real' or if it has
   * been synthesized by the compiler in order to implement scoping
   * of inner classes.
   */
  public abstract boolean isSynthetic();

  /** Array factory: returns new <code>HMember[]</code>. */
  public static final ArrayFactory arrayFactory =
    Factories.hmemberArrayFactory;
}
