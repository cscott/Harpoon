// HFieldSyn.java, created by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.ClassFile;

import java.lang.reflect.Modifier;

/**
 * A <code>HFieldSyn</code> provides information about a single field of a
 * class
 * or an interface.  The reflected field may be a class (static) field or
 * an instance field.
 *
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: HFieldSyn.java,v 1.3 1998-10-21 21:50:24 cananian Exp $
 * @see HMember
 * @see HClass
 */
public class HFieldSyn extends HField {
  /** Create a new field based on a template.  The new method will be added
   *  to the class containing the template method. The parent class of
   *  the template method must be an <code>HClassSyn</code>. */
  public HFieldSyn(HField template) {
    this((HClassSyn)(template.getDeclaringClass()), template);
  }
  /** Create a new field like the <code>template</code>, 
   *  but in class <code>parent</code>. 
   *  The new field will be added to class <code>parent</code>. */
  public HFieldSyn(HClassSyn parent, HField template) {
    this.parent = parent;
    this.type = template.getType();
    this.name = uniqueName(parent, template.getName());
    this.modifiers = template.getModifiers();
    this.constValue = template.getConstant();
    this.isSynthetic = template.isSynthetic();
    parent.addDeclaredField(this);
  }
  /** Create a new field with the specified name, class and descriptor. */
  public HFieldSyn(HClassSyn parent, String name, String descriptor) {
    this(parent, name, HClass.forDescriptor(descriptor));
  }
  /** Create a new field of the specified name, class, and type. */
  public HFieldSyn(HClassSyn parent, String name, HClass type) {
    this.parent = parent;
    this.type = type;
    this.name = uniqueName(parent, name);
    this.modifiers = 0;
    this.constValue = null;
    this.isSynthetic = false;
    parent.addDeclaredField(this);
  }

  public void setModifiers(int m) { this.modifiers = m; }
  public void setType(HClass type) { this.type = type; }
  public void setConstant(Object co) { this.constValue=co; }
  public void setSynthetic(boolean isSynthetic) {this.isSynthetic=isSynthetic;}
}
// set emacs indentation style.
// Local Variables:
// c-basic-offset:2
// End:
