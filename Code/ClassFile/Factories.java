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
 * @version $Id: Factories.java,v 1.3 2002-04-10 03:04:12 cananian Exp $
 */
abstract class Factories {
  /** Array factory: returns new <code>HClass[]</code>. */
  public static final ArrayFactory<HClass> hclassArrayFactory =
    new ArrayFactory<HClass>() {
      public HClass[] newArray(int len) { return new HClass[len]; }
    };
  /** Array factory: returns new <code>HMember[]</code>. */
  public static final ArrayFactory<HMember> hmemberArrayFactory =
    new ArrayFactory<HMember>() {
      public HMember[] newArray(int len) { return new HMember[len]; }
    };
  /** Array factory: returns new <code>HField[]</code>. */
  public static final ArrayFactory<HField> hfieldArrayFactory =
    new ArrayFactory<HField>() {
      public HField[] newArray(int len) { return new HField[len]; }
    };
  /** Array factory: returns new <code>HMethod[]</code>. */
  public static final ArrayFactory<HMethod> hmethodArrayFactory =
    new ArrayFactory<HMethod>() {
      public HMethod[] newArray(int len) { return new HMethod[len]; }
    };
  /** Array factory: returns new <code>HConstructor[]</code>. */
  public static final ArrayFactory<HConstructor> hconstructorArrayFactory =
    new ArrayFactory<HConstructor>() {
      public HConstructor[] newArray(int len) { return new HConstructor[len]; }
    };
  /** Array factory: returns new <code>HInitializer[]</code>. */
  public static final ArrayFactory<HInitializer> hinitializerArrayFactory =
    new ArrayFactory<HInitializer>() {
      public HInitializer[] newArray(int len) { return new HInitializer[len]; }
    };
}
