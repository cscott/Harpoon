// HConstructorSyn.java, created Fri Oct 16  3:30:52 1998 by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.ClassFile;

import harpoon.Util.Util;

/**
 * An <code>HConstructorSyn</code> is a mutable representation of a
 * single constructor for a class.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: HConstructorSyn.java,v 1.5 2002-04-10 03:04:15 cananian Exp $
 */
class HConstructorSyn extends HMethodSyn implements HConstructor {

  /** Create a new constructor likes the template, but in
   *  class <code>parent</code>.
   */
  HConstructorSyn(HClassSyn parent, HConstructor template) {
    super(parent, "<init>", template);
    assert this.returnType.actual()==HClass.Void;
  }

  /** Create a new empty constructor for the specified class
   *  with the specified descriptor that
   *  throws no checked exceptions.
   *  You must putCode to make this constructor valid.
   */
  HConstructorSyn(HClassSyn parent, String descriptor) {
    super(parent, "<init>", descriptor);
    assert this.returnType.actual()==HClass.Void;
  }
  /** Create a new empty constructor in the specified class
   *  with the specified parameter and return types
   *  that throws no checked exceptions.
   */
  HConstructorSyn(HClassSyn parent, HClass[] paramTypes) {
    super(parent, "<init>", paramTypes, HClass.Void);
    assert this.returnType.actual()==HClass.Void;
  }

  public void setReturnType(HClass returnType) {
    assert returnType==HClass.Void;
  }
  public int hashCode() { return HConstructorImpl.hashCode(this); }
  public String toString() { return HConstructorImpl.toString(this); }
}

// set emacs indentation style.
// Local Variables:
// c-basic-offset:2
// End:
