// HField.java, created Fri Jul 31  9:33:47 1998 by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.ClassFile;

import harpoon.Util.ArrayFactory;

import java.lang.reflect.Modifier;

/**
 * <code>HFieldImpl</code> is the basic implementation of <code>HField</code>.
 *
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: HFieldImpl.java,v 1.4 2003-03-18 02:27:02 cananian Exp $
 * @see HField
 */
abstract class HFieldImpl
  implements HField, java.io.Serializable, java.lang.Comparable<HMember> {
  HClass parent;
  HPointer type;
  String name;
  int modifiers;
  Object constValue;
  boolean isSynthetic;

  /** 
   * Returns the <code>HClass</code> object representing the class or
   * interface that declares the field represented by this 
   * <code>HField</code> object. 
   */
  public HClass getDeclaringClass() {
    return parent;
  }
  /**
   * Returns the name of the field represented by this 
   * <code>HField</code> object.
   */
  public String getName() {
    return name;
  }
  /**
   * Returns the Java language modifiers for the field represented by this
   * <code>HField</code> object, as an integer.  The <code>Modifier</code>
   * class should be used to decode the modifiers.
   * @see java.lang.reflect.Modifier
   */
  public int getModifiers() {
    return modifiers;
  }
  /**
   * Returns an <code>HClass</code> object that identifies the declared
   * type for the field represented by this <code>HField</code> object.
   */
  public HClass getType() {
    try {
      return (HClass) type;
    } catch (ClassCastException e) { // type was ClassPointer.
      HClass t = type.actual();
      type = t;
      return t;
    }
  }
  /**
   * Return the type descriptor for this <code>HField</code> object.
   */
  public String getDescriptor() {
    return type.getDescriptor();
  }
  /**
   * Returns the constant value of this <code>HField</code>, if
   * it is a constant field.
   * @return the wrapped value, or <code>null</code> if 
   *         <code>!isConstant()</code>.
   */
  public Object getConstant() { return constValue; }

  /**
   * Determines whether this <code>HField</code> represents a constant
   * field.
   */
  public boolean isConstant() { return (constValue!=null); }

  /**
   * Determines whether this <code>HField</code> is synthetic.
   */
  public boolean isSynthetic() { return isSynthetic; }

  /** Determines whether this is a static field. */
  public boolean isStatic() {
    return Modifier.isStatic(getModifiers());
  }

  public HType getGenericType() {
    throw new RuntimeException("Unimplemented");
  }

  /** Returns a mutator for this <code>HField</code>, or <code>null</code>
   *  if the object is immutable. */
  public HFieldMutator getMutator() { return null; }

  /** 
   * Compares this <code>HField</code> against the specified object.
   * Returns <code>true</code> if the objects are the same.  Two
   * <code>HFields</code> are the same if they were declared by the same
   * class and have the same name and type.
   */
  public boolean equals(Object obj) { return equals(this, obj); }
  // factored out for re-use
  static boolean equals(HField _this_, Object obj) {
    HField field;
    if (obj==null) return false;
    if (_this_==obj) return true;
    try { field=(HField)obj; } catch (ClassCastException e) {return false; }
    if (_this_.getDeclaringClass().getDescriptor().equals
	(field.getDeclaringClass().getDescriptor()) &&
	_this_.getName().equals
	(field.getName()) &&
	_this_.getType().getDescriptor().equals
	(field.getType().getDescriptor()))
      return true;
    return false;
  }

  public int hashCode() { return hashCode(this); }
  // factored out for re-use
  static int hashCode(HField hf) {
    return
      hf.getDeclaringClass().hashCode() ^
      hf.getName().hashCode() ^
      hf.getDescriptor().hashCode();
  }

  /**
   * Return a string describing this <code>HField</code>.  The format
   * is the access modifiers for the field, if any, followed by the
   * field type, followed by a space, followed by the fully-qualified
   * name of the class declaring the field, followed by a period,
   * followed by the name of the field.  For example:<p>
   * <DL>
   * <DD><CODE>public static final int java.lang.Thread.MIN_PRIORITY</CODE>
   * <DD><CODE>private int java.io.FileDescriptor.fd</CODE>
   * </DL><p>
   * The modifiers are placed in canonical order as specified by
   * "The Java Language Specification."  This is
   * <code>public</code>, <code>protected</code>, or <code>private</code>
   * first, and then other modifiers in the following order:
   * <code>static</code>, <code>final</code>, <code>transient</code>,
   * <code>volatile</code>.
   */
  public String toString() { return toString(this); }
  /** For re-use by other classes implement HField. */
  static String toString(HField hf) {
    StringBuffer r = new StringBuffer();
    int m = hf.getModifiers();
    if (m!=0) {
      r.append(Modifier.toString(m));
      r.append(' ');
    }
    r.append(HClass.getTypeName(hf.getType()));
    r.append(' ');
    r.append(HClass.getTypeName(hf.getDeclaringClass()));
    r.append('.');
    r.append(hf.getName());
    return r.toString();
  }

  /** Serializable interface. */
  public Object writeReplace() { return new HFieldStub(this); }
  static final class HFieldStub implements java.io.Serializable {
    private HClass parent;
    private String name;
    HFieldStub(HField f) {
      this.parent = f.getDeclaringClass();
      this.name = f.getName().intern();
    }
    public Object readResolve() {
      return parent.getDeclaredField(name);
    }
  }
  // Comparable interface
  /** Compares two <code>HField</code>s lexicographically; first by
   *  declaring class, then by name. */
  public int compareTo(HMember o) {
    return memberComparator.compare(this, o);
  }
}
// set emacs indentation style.
// Local Variables:
// c-basic-offset:2
// End:
