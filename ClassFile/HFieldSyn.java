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
 * @version $Id: HFieldSyn.java,v 1.2 1998-10-16 11:15:38 cananian Exp $
 * @see HMember
 * @see HClass
 */
public class HFieldSyn extends HField {
  public HFieldSyn(HField template) {
    this.parent = template.getDeclaringClass();
    this.type = template.getType();
    this.name = uniqueName(parent, template.getName());
    this.modifiers = template.getModifiers();
    this.constValue = template.getConstant();
    this.isSynthetic = template.isSynthetic();
    ((HClassSyn)parent).addDeclaredField(this);
  }
  /** Create a new field of the specified name, class, and type. */
  public HFieldSyn(HClass parent, HClass type, String name) {
    this.parent = parent;
    this.type = type;
    this.name = uniqueName(parent, name);
    this.modifiers = 0;
    this.constValue = null;
    this.isSynthetic = false;
    ((HClassSyn)parent).addDeclaredField(this);
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
