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
 * @version $Id: HFieldSyn.java,v 1.1 1998-10-16 06:21:03 cananian Exp $
 * @see HMember
 * @see HClass
 */
public class HFieldSyn extends HField {
  public HFieldSyn(HField template) {
    this.parent = template.getDeclaringClass();
    this.type = template.getType();
    this.name = template.getName();
    this.modifiers = template.getModifiers();
    this.constValue = template.getConstant();
    this.isSynthetic = template.isSynthetic();
  }

  public void setDeclaringClass(HClass parent) { this.parent = parent; }
  public void setName(String name) { this.name = name; }
  public void setModifiers(int m) { this.modifiers = m; }
  public void setType(HClass type) { this.type = type; }
  public void setConstant(Object co) { this.constValue=co; }
  public void setSynthetic(boolean isSynthetic) {this.isSynthetic=isSynthetic;}
}
// set emacs indentation style.
// Local Variables:
// c-basic-offset:2
// End:
