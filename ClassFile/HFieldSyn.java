// HFieldSyn.java, created Fri Oct 16  2:21:03 1998 by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.ClassFile;

import java.lang.reflect.Modifier;

import harpoon.Util.Util;
/**
 * A <code>HFieldSyn</code> provides information about a single field of a
 * class
 * or an interface.  The reflected field may be a class (static) field or
 * an instance field.
 *
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: HFieldSyn.java,v 1.5.2.1 2002-02-27 08:35:44 cananian Exp $
 * @see HMember
 * @see HClass
 */
class HFieldSyn extends HFieldImpl implements HFieldMutator {

  /** Create a new field like the <code>template</code>, 
   *  but in class <code>parent</code> and named <code>name</code>. */
  public HFieldSyn(HClassSyn parent, String name, HField template) {
    this.parent = parent;
    this.type = template.getType();
    this.name = name;
    this.modifiers = template.getModifiers();
    this.constValue = template.getConstant();
    this.isSynthetic = template.isSynthetic();
    // ensure linker information is consistent.
    assert checkLinker((HClass)type);
  }
  /** Create a new field with the specified name, class and descriptor. */
  public HFieldSyn(HClassSyn parent, String name, String descriptor) {
    this(parent, name, parent.getLinker().forDescriptor(descriptor));
  }
  /** Create a new field of the specified name, class, and type. */
  public HFieldSyn(HClassSyn parent, String name, HPointer type) {
    this.parent = parent;
    this.type = type;
    this.name = name;
    this.modifiers = 0;
    this.constValue = null;
    this.isSynthetic = false;
    // ensure linker information is consistent.
    if (type instanceof HClass) assert checkLinker((HClass)type);
  }

  public HFieldMutator getMutator() { return this; }

  public void addModifiers(int m) { setModifiers(getModifiers()|m); }
  public void removeModifiers(int m) { setModifiers(getModifiers()&(~m)); }
  public void setModifiers(int m) {
    if (this.modifiers != m) parent.hasBeenModified = true;
    this.modifiers = m;
  }

  public void setType(HClass type) {
    if (this.type != type) parent.hasBeenModified = true;
    this.type = type;
    assert checkLinker(type);
  }
  public void setConstant(Object co) {
    if ((co!=null) ? (!co.equals(this.constValue)) : (this.constValue!=null))
      parent.hasBeenModified = true;
    this.constValue=co;
  }
  public void setSynthetic(boolean isSynthetic) {
    if (this.isSynthetic != isSynthetic) parent.hasBeenModified = true;
    this.isSynthetic=isSynthetic;
  }

  // assertion helper.
  private boolean checkLinker(HClass hc) {
    return hc.isPrimitive() || hc.getLinker()==parent.getLinker();
  }
}
// set emacs indentation style.
// Local Variables:
// c-basic-offset:2
// End:
