package harpoon.ClassFile;

import java.lang.reflect.Modifier;
import harpoon.ClassFile.Raw.Attribute.AttributeSynthetic;
import harpoon.ClassFile.Raw.Attribute.AttributeConstantValue;

/**
 * A <code>HField</code> provides information about a single field of a class
 * or an interface.  The reflected field may be a class (static) field or
 * an instance field.
 *
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: HField.java,v 1.7 1998-08-02 05:24:07 cananian Exp $
 * @see HMember
 * @see HClass
 */
public class HField implements HMember {
  HClass hclass;
  HClass type;
  harpoon.ClassFile.Raw.FieldInfo fieldinfo;

  /** Create an <code>HField</code> from a raw field_info structure. */
  protected HField(HClass parent,
		   harpoon.ClassFile.Raw.FieldInfo fieldinfo) {
    this.hclass = parent;
    this.fieldinfo = fieldinfo;
    this.type = HClass.forDescriptor(fieldinfo.descriptor());
  }

  /** 
   * Returns the <code>HClass</code> object representing the class or
   * interface that declares the field represented by this 
   * <code>HField</code> object. 
   */
  public HClass getDeclaringClass() {
    return hclass;
  }
  /**
   * Returns the name of the field represented by this 
   * <code>HField</code> object.
   */
  public String getName() {
    return fieldinfo.name();
  }
  /**
   * Returns the Java language modifiers for the field represented by this
   * <code>HField</code> object, as an integer.  The <code>Modifier</code>
   * class should be used to decode the modifiers.
   * @see java.lang.reflect.Modifier
   */
  public int getModifiers() {
    return fieldinfo.access_flags.access_flags;
  }
  /**
   * Returns an <code>HClass</code> object that identifies the declared
   * type for the field represented by this <code>HField</code> object.
   */
  public HClass getType() {
    return type;
  }
  /**
   * Determines whether this <code>HField</code> represents a constant
   * field.
   */
  public boolean isConstant() {
    for (int i=0; i<fieldinfo.attributes.length; i++)
      if (fieldinfo.attributes[i] instanceof AttributeConstantValue)
	return true;
    return false;
  }
  /**
   * Determines whether this <code>HField</code> is synthetic.
   */
  public boolean isSynthetic() {
    for (int i=0; i<fieldinfo.attributes.length; i++)
      if (fieldinfo.attributes[i] instanceof AttributeSynthetic)
	return true;
    return false;
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
      if (hclass == field.hclass &&
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
    return hclass.getDescriptor().hashCode() ^ getName().hashCode();
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
    r.append(getTypeName(hclass));
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
