// HClassSyn.java, created by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.ClassFile;

import java.lang.reflect.Modifier;

import harpoon.Util.Util;

/**
 * Instances of the class <code>HClassSyn</code> represent modifiable
 * classes and interfaces of a java program.  Arrays and primitive types
 * are not modifiable, and thus are not represented by 
 * <code>HClassSyn</code>.  <code>HClassSyn</code> objects are assigned
 * unique names automagically on creation.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: HClassSyn.java,v 1.2 1998-10-16 06:21:03 cananian Exp $
 * @see harpoon.ClassFile.HClass
 */
public class HClassSyn extends HClassCls {
  /** Create an <code>HClassSync</code> from an <code>HClass</code>. */
  public HClassSyn(HClass template) {
    this.name = template.getName(); // FIXME: rename.
    this.superclass = template.getSuperclass();
    this.interfaces = template.getInterfaces();
    this.modifiers  = template.getModifiers();
    this.declaredFields = template.getDeclaredFields();
    this.declaredMethods= template.getDeclaredMethods();
    this.sourcefile = template.getSourceFile();
  }

  /**
   * Sets the fully-qualified name of this class to <code>name</code>.
   */
  public void setName(String name) { this.name = name; }

  /**
   * Adds the given <code>HField</code> to the class represented by
   * this <code>HClassSyn</code>.
   */
  public void addDeclaredField(HFieldSyn f) {
    declaredFields = 
      (HField[]) Util.grow(declaredFields, f, declaredFields.length);
  }
  public void removeDeclaredField(HField f) throws NoSuchFieldError {
    for (int i=0; i<declaredFields.length; i++) {
      if (declaredFields[i].equals(f)) {
	declaredFields = (HField[]) Util.shrink(declaredFields, i);
	return;
      }
    }
    throw new NoSuchFieldError(f.toString());
  }

  public void addDeclaredMethod(HMethodSyn m) {
    declaredMethods = 
      (HMethod[]) Util.grow(declaredMethods, m, declaredMethods.length);
  }
  public void removeDeclaredMethod(HMethod m) throws NoSuchMethodError {
    for (int i=0; i<declaredMethods.length; i++) {
      if (declaredMethods[i].equals(m)) {
	declaredMethods = (HMethod[]) Util.shrink(declaredMethods, i);
	return;
      }
    }
    throw new NoSuchMethodError(m.toString());
  }

  public void setModifiers(int m) { 
    if (Modifier.isInterface(m) != Modifier.isInterface(modifiers))
      throw new Error("Can't turn a class into an interface or vice versa.");
    modifiers = m;
  }

  public void setSuperclass(HClass sc) {
    // XX FIXME: sanity check?
    superclass = sc;
  }

  public void addInterface(HClass in) {
    if (!in.isInterface()) throw new Error("Not an interface.");
    interfaces = (HClass[]) Util.grow(interfaces, in, interfaces.length);
  }
  public void removeInterface(HClass in) throws NoClassDefFoundError {
    for (int i=0; i<interfaces.length; i++) {
      if (interfaces[i].equals(in)) {
	interfaces = (HClass[]) Util.shrink(interfaces, i);
	return;
      }
    }
    throw new NoClassDefFoundError(in.toString());
  }

  /**
   * Set the source file name for this class.
   */
  public void setSourceFile(String sf) { this.sourcefile = sf; }

}
// set emacs indentation style.
// Local Variables:
// c-basic-offset:2
// End:
