// Factories.java, created Tue Jan 11 17:51:58 2000 by cananian
// Copyright (C) 2000 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.ClassFile;

import harpoon.Util.ArrayFactory;
/**
 * <code>Factories</code> contains various <code>ArrayFactory</code>s.
 * This file is necessary because the JDK 1.2 javac compiler does
 * not allow anonymous or non-public inner classes in an interface
 * declaration.  Therefore, the array factories accessed by
 * <code>HMethod.arrayFactory</code> (and etc.) cannot be defined
 * in the interface where the field is defined; they end up here
 * instead.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: Factories.java,v 1.2 2002-02-25 21:03:01 cananian Exp $
 */
abstract class Factories {
  /** Array factory: returns new <code>HClass[]</code>. */
  public static final ArrayFactory hclassArrayFactory =
    new ArrayFactory() {
      public Object[] newArray(int len) { return new HClass[len]; }
    };
  /** Array factory: returns new <code>HMember[]</code>. */
  public static final ArrayFactory hmemberArrayFactory =
    new ArrayFactory() {
      public Object[] newArray(int len) { return new HMember[len]; }
    };
  /** Array factory: returns new <code>HField[]</code>. */
  public static final ArrayFactory hfieldArrayFactory =
    new ArrayFactory() {
      public Object[] newArray(int len) { return new HField[len]; }
    };
  /** Array factory: returns new <code>HMethod[]</code>. */
  public static final ArrayFactory hmethodArrayFactory =
    new ArrayFactory() {
      public Object[] newArray(int len) { return new HMethod[len]; }
    };
  /** Array factory: returns new <code>HConstructor[]</code>. */
  public static final ArrayFactory hconstructorArrayFactory =
    new ArrayFactory() {
      public Object[] newArray(int len) { return new HConstructor[len]; }
    };
  /** Array factory: returns new <code>HInitializer[]</code>. */
  public static final ArrayFactory hinitializerArrayFactory =
    new ArrayFactory() {
      public Object[] newArray(int len) { return new HInitializer[len]; }
    };
}
