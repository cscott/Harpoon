// HMethodSyn.java, created by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.ClassFile;

import java.lang.reflect.Modifier;
import java.util.Hashtable;
import java.util.Vector;

/**
 * An <code>HMethodSyn</code> provides information about, and access to, a 
 * single method on a class or interface.  The reflected method
 * may be a class method or an instance method (including an abstract
 * method).
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: HMethodSyn.java,v 1.1 1998-10-16 06:21:03 cananian Exp $
 * @see HMember
 * @see HClass
 */
public class HMethodSyn extends HMethod {

  /** Create a new method based on a template. */
  public HMethodSyn(HMethod template) {
    this.parent = template.getDeclaringClass();
    this.name = template.getName();
    // XXX ensure uniqueness.
    this.modifiers = template.getModifiers();
    this.returnType = template.getReturnType();
    this.parameterTypes = template.getParameterTypes();
    this.parameterNames = template.getParameterNames();
    this.exceptionTypes = template.getExceptionTypes();
    this.isSynthetic = template.isSynthetic();
  }
  /** Create a new empty abstract method, that takes no parameters, returns
   *  <code>void</code>, and throws no checked exceptions.
   *  Adding code to the method will make it non-abstract.
   */
  public HMethodSyn(HClass parent, String name) {
    this.parent = parent;
    this.name = name;
    // XXX ensure uniqueness.
    this.modifiers = Modifier.ABSTRACT;
    this.returnType = HClass.Void;
    this.parameterTypes = new HClass[0];
    this.parameterNames = new String[0];
    this.exceptionTypes = new HClass[0];
    this.isSynthetic = false;
  }

  /* IMMUTABLE.
  public void setDeclaringClass(HClass parent) { this.parent = parent; }

  public void setName(String name) { this.name = name; }
  */

  public void setModifiers(int m) { this.modifiers = m; }

  public void setReturnType(HClass returnType) { this.returnType = returnType;}

  public void setParameterTypes(HClass[] parameterTypes) {
    this.parameterTypes = parameterTypes;
  }
  public void setParameterType(int which, HClass type) {
    this.parameterTypes[which] = type;
  }

  public void setParameterNames(String[] parameterNames) {
    this.parameterNames = parameterNames;
  }
  public void setParameterName(int which, String name) {
    this.parameterNames[which] = name;
  }

  public void setExceptionTypes(HClass[] exceptionTypes) {
    this.exceptionTypes = exceptionTypes;
  }
  public void setExceptionType(int which, HClass type) {
    this.exceptionTypes[which] = type;
  }

  public void setSynthetic(boolean isSynthetic) {
    this.isSynthetic = isSynthetic;
  }
}

// set emacs indentation style.
// Local Variables:
// c-basic-offset:2
// End:
