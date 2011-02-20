// HMethod.java, created Fri Jul 31 22:02:43 1998 by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.ClassFile;

import harpoon.Util.Util;
import harpoon.Util.ArrayFactory;

import java.lang.reflect.Modifier;
import java.util.Hashtable;
/**
 * <code>HMethodImpl</code> is the basic implementation of 
 * <code>HMethod</code>.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: HMethodImpl.java,v 1.4 2003-03-18 02:27:02 cananian Exp $
 * @see HMethod
 */
abstract class HMethodImpl
  implements HMethod, java.io.Serializable, java.lang.Comparable<HMember> {
  HClass parent;
  String name;
  int modifiers;
  HPointer returnType;
  HPointer[] parameterTypes;
  String[] parameterNames;
  HPointer[] exceptionTypes;
  boolean isSynthetic;

  /**
   * Returns the <code>HClass</code> object representing the class or 
   * interface that declares the method represented by this
   * <code>HMethod</code> object. 
   */
  public HClass getDeclaringClass() {
    return parent;
  }

  /**
   * Returns the name of the method represented by this <code>HMethod</code>
   * object, as a <code>String</code>.
   */
  public String getName() {
    return name;
  }

  /**
   * Returns the Java language modifiers for the method represented by this
   * <code>HMethod</code> object, as an integer.  The 
   * <code>java.lang.reflect.Modifier</code>
   * class should be used to decode the modifiers.
   * @see java.lang.reflect.Modifier
   */
  public int getModifiers() {
    return modifiers;
  }

  /**
   * Returns a <code>HClass</code> object that represents the formal
   * return type of the method represented by this <code>HMethod</code>
   * object.
   */
  public HClass getReturnType() {
    try {
      return (HClass) returnType;
    } catch (ClassCastException e) { // returnType was ClassPointer
      HClass rt = returnType.actual();
      returnType = rt;
      return rt;
    }
  }

  /**
   * Returns the descriptor for this method.
   */
  public String getDescriptor() {
    StringBuffer sb = new StringBuffer("(");
    for (int i=0; i<parameterTypes.length; i++)
      sb.append(parameterTypes[i].getDescriptor());
    sb.append(')');
    sb.append(returnType.getDescriptor());
    return sb.toString();
  }

  /**
   * Returns an array of <code>HClass</code> objects that represent the
   * formal parameter types, in declaration order, of the method
   * represented by this <code>HMethod</code> object.  Returns an array
   * of length 0 is the underlying method takes no parameters.
   */
  public HClass[] getParameterTypes() {
    HClass[] pt;
    try {
      pt = (HClass[]) parameterTypes;
    } catch (ClassCastException e) { // parameterTypes was ClassPointer[]
      pt = new HClass[parameterTypes.length];
      for (int i=0; i<pt.length; i++)
	pt[i] = parameterTypes[i].actual();
      parameterTypes = pt;
    }
    return (HClass[]) Util.safeCopy(HClass.arrayFactory, pt);
  }

  /**
   * Returns an array of <code>String</code> objects giving the declared
   * names of the formal parameters of the method.  The length of the
   * returned array is equal to the number of formal parameters.
   * If there is no <code>LocalVariableTable</code> attribute available
   * for this method, then every element of the returned array will be
   * <code>null</code>.
   */
  public String[] getParameterNames() {
    return Util.safeCopy(new ArrayFactory<String>() {
      public String[] newArray(int len) { return new String[len]; }
    }, parameterNames);
  }

  /**
   * Returns an array of <code>HClass</code> objects that represent the
   * types of the checked exceptions thrown by the underlying method
   * represented by this <code>HMethod</code> object.  Returns an array
   * of length 0 if the method throws no checked exceptions.
   */
  public HClass[] getExceptionTypes() {
    HClass[] et;
    try {
      et = (HClass[]) exceptionTypes;
    } catch (ClassCastException e) { // exceptionTypes was ClassPointer
      et = new HClass[exceptionTypes.length];
      for (int i=0; i<et.length; i++)
	et[i] = exceptionTypes[i].actual();
      exceptionTypes = et;
    }
    return (HClass[]) Util.safeCopy(HClass.arrayFactory, et);
  }

  /**
   * Determines whether this <code>HMethod</code> is synthetic.
   */
  public boolean isSynthetic() { return isSynthetic; }

  /** Determines whether this <code>HMethod</code> is an interface method. */
  public boolean isInterfaceMethod() {
    return parent.isInterface() && !(this instanceof HInitializer);
  }
  /** Determines whether this is a static method. */
  public boolean isStatic() {
    return Modifier.isStatic(getModifiers());
  }

  /* ------- JSR-14 extensions. ----------- */
  public HType[] getGenericParameterTypes() {
    throw new RuntimeException("Unimplemented.");
  }
  public HType getGenericReturnType() {
    throw new RuntimeException("Unimplemented.");
  }
  public HMethodTypeVariable[] getTypeParameters() {
    throw new RuntimeException("Unimplemented.");
  }


  /** Returns a mutator for this <code>HMethod</code>, or <code>null</code>
   *  if the object is immutable. */
  public HMethodMutator getMutator() { return null; }

  /**
   * Compares this <code>HMethod</code> against the specified object.
   * Returns <code>true</code> if the objects are the same.  Two
   * <code>HMethod</code>s are the same if they were declared by the same
   * class and have the same name and formal parameter types.
   */ // in actual practice, I think HMethods are unique.
  public boolean equals(Object obj) { return equals(this, obj); }
  // factored out for re-use.
  static boolean equals(HMethod _this_, Object obj) {
    HMethod method;
    if (obj==null) return false;
    if (_this_==obj) return true; // common case.
    try { method = (HMethod) obj; }
    catch (ClassCastException e) { return false; }
    if (!_this_.getDeclaringClass().getDescriptor().equals
	(method.getDeclaringClass().getDescriptor())) return false;
    if (!_this_.getName().equals(method.getName())) return false;
    HClass hc1[] = _this_.getParameterTypes();
    HClass hc2[] = method.getParameterTypes();
    if (hc1.length != hc2.length) return false;
    for (int i=0; i<hc1.length; i++)
      if (!hc1[i].getDescriptor().equals(hc2[i].getDescriptor()))
	return false;
    return true;
  }

  public int hashCode() { return hashCode(this); }
  // factored out for re-use
  static int hashCode(HMethod hm) {
    return
      hm.getDeclaringClass().hashCode() ^
      hm.getName().hashCode() ^
      hm.getDescriptor().hashCode();
  }

  /**
   * Returns a string describing this <code>HMethod</code>.  The string
   * is formatted as the method access modifiers, if any, followed by
   * the method return type, followed by a space, followed by the class
   * declaring the method, followed by a period, followed by the method
   * name, followed by a parenthesized, comma-separated list of the
   * method's formal parameter types.  If the method throws checked
   * exceptions, the parameter list is followed by a space, followed
   * by the word throws followed by a comma-separated list of the
   * throws exception types.  For example:<p>
   * <DL>
   * <DD><CODE>public boolean java.lang.Object.equals(java.lang.Object)</CODE>
   * </DL><p>
   * The access modifiers are placed in canonical order as specified by
   * "The Java Language Specification."  This is
   * <code>public</code>, <code>protected</code>, or <code>private</code>
   * first, and then other modifiers in the following order:
   * <code>abstract</code>, <code>static</code>, <code>final</code>, 
   * <code>synchronized</code>, <code>native</code>.
   */
  public String toString() { return toString(this); }
  /** For re-use by other classes implementing HMethod. */
  static String toString(HMethod hm) {
    StringBuffer r = new StringBuffer();
    int m = hm.getModifiers();
    if (m!=0) {
      r.append(Modifier.toString(m));
      r.append(' ');
    }
    r.append(HClass.getTypeName(hm.getReturnType()));
    r.append(' ');
    r.append(HClass.getTypeName(hm.getDeclaringClass()));
    r.append('.');
    r.append(hm.getName());
    r.append('(');
    HPointer hcp[] = hm.getParameterTypes();
    for (int i=0; i<hcp.length; i++) {
      r.append(HClass.getTypeName(hcp[i]));
      if (i < hcp.length-1)
	r.append(',');
    }
    r.append(')');
    HPointer ecp[] = hm.getExceptionTypes();
    if (ecp.length > 0) {
      r.append(" throws ");
      for (int i=0; i<ecp.length; i++) {
	r.append(ecp[i].getName()); // can't be primitive or array type.
	if (i < ecp.length-1)
	  r.append(',');
      }
    }
    return r.toString();
  }
  
  /** Serializable interface. */
  public Object writeReplace() { return new HMethodStub(this); }
  static final class HMethodStub implements java.io.Serializable {
    private HClass parent;
    private String name, descriptor;
    HMethodStub(HMethod m) {
      this.parent=m.getDeclaringClass();
      this.name=m.getName().intern();
      this.descriptor=m.getDescriptor().intern();
    }
    public Object readResolve() {
      return parent.getDeclaredMethod(name, descriptor);
    }
  }
  // Comparable interface
  /** Compares two <code>HMethod</code>s lexicographically; first by
   *  declaring class, then by name, and lastly by descriptor. */
  public int compareTo(HMember o) {
    return memberComparator.compare(this, o);
  }
}

// set emacs indentation style.
// Local Variables:
// c-basic-offset:2
// End:
