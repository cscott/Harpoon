// HField.java, created by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.ClassFile;

import gnu.bytecode.*;
import java.lang.reflect.Modifier;

/**
 * A <code>HField</code> provides information about a single field of a class
 * or an interface.  The reflected field may be a class (static) field or
 * an instance field.
 *
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: HField.java,v 1.13 1998-10-16 11:15:38 cananian Exp $
 * @see HMember
 * @see HClass
 */
public abstract class HField implements HMember {
  HClass parent;
  HClass type;
  String name;
  int modifiers;
  Object constValue;
  boolean isSynthetic;

  /** Subclass must provide implementation. */
  protected HField() { /* no implementation */ }

  /** Make a unique field name from a given suggestion. */
  protected static String uniqueName(HClass parent, String suggestion)
  {
    if (suggestion==null || suggestion.equals("")) suggestion="MAGICf";
    // remove trailing dollar-signs.
    while (suggestion.charAt(suggestion.length()-1)=='$')
      suggestion=suggestion.substring(0, suggestion.length()-1);
    // remove anything after a double dollar sign.
    if (suggestion.indexOf("$$")!=-1)
      suggestion=suggestion.substring(0, suggestion.lastIndexOf("$$"));
    // find lowest number for method.
  L1:
    for (int i=-1; true; i++) {
      String fieldname = (i<0)?suggestion:(suggestion+"$$"+i);
      // search class for existing field.
      HField[] hf = parent.getDeclaredFields();
      for (int j=0; j<hf.length; j++)
	if (hf[j].getName().equals(fieldname)) continue L1;
      // found a valid name.
      return fieldname;
    }
  }

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
    return type;
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

  /** 
   * Compares this <code>HField</code> against the specified object.
   * Returns <code>true</code> if the objects are the same.  Two
   * <code>HFields</code> are the same if they were declared by the same
   * class and have the same name and type.
   */
  public boolean equals(Object object) {
    if (object != null && object instanceof HField) {
      HField field = (HField) object;
      if (parent == field.parent &&
	  getName().equals(field.getName()) &&
	  type == field.type)
	return true;
    }
    return false;
  }
  /**
   * Returns a hashcode for this <code>HField</code>.  This is
   * computed as the exclusive-or of the hashcodes for the
   * underlying field's declaring class name and its name.
   */
  public int hashCode() {
    return parent.getDescriptor().hashCode() ^ getName().hashCode();
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
  public String toString() {
    StringBuffer r = new StringBuffer();
    int m = getModifiers();
    if (m!=0) {
      r.append(Modifier.toString(m));
      r.append(' ');
    }
    r.append(getTypeName(type));
    r.append(' ');
    r.append(getTypeName(parent));
    r.append('.');
    r.append(getName());
    return r.toString();
  }

  static String getTypeName(HClass hc) {
    if (hc.isArray()) {
      StringBuffer r = new StringBuffer();
      HClass sup = hc;
      int i=0;
      for (; sup.isArray(); sup = sup.getComponentType())
	i++;
      r.append(sup.getName());
      for (int j=0; j<i; j++)
	r.append("[]");
      return r.toString();
    }
    return hc.getName();
  }
  
  static HField[] copy(HField[] src) {
    if (src.length==0) return src;
    HField[] dst = new HField[src.length];
    System.arraycopy(src,0,dst,0,src.length);
    return dst;
  }
}
// set emacs indentation style.
// Local Variables:
// c-basic-offset:2
// End:
