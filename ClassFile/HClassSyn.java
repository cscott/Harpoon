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
 * @version $Id: HClassSyn.java,v 1.4 1998-10-16 11:42:57 cananian Exp $
 * @see harpoon.ClassFile.HClass
 */
public class HClassSyn extends HClassCls {
  /** Create an <code>HClassSyn</code> from an <code>HClass</code>. */
  public HClassSyn(HClass template) {
    this.name = uniqueName(template.getName()); register();
    this.superclass = template.getSuperclass();
    this.interfaces = template.getInterfaces();
    this.modifiers  = template.getModifiers();
    this.declaredFields = template.getDeclaredFields();
    this.declaredMethods= template.getDeclaredMethods();
    this.sourcefile = template.getSourceFile();
  }
  /** Create a new, empty <code>HClassSyn</code>. 
   *  Default is to create an Interface.
   */
  public HClassSyn(String name, String sourcefile) {
    this.name = uniqueName(name); register();
    this.superclass = forClass(Object.class);
    this.interfaces = new HClass[0];
    this.modifiers = Modifier.INTERFACE | 0x0020; // ACC_SUPER
    this.declaredFields = new HField[0];
    this.declaredMethods = new HMethod[0];
    this.sourcefile = sourcefile;
  }

  /**
   * Adds the given <code>HField</code> to the class represented by
   * this <code>HClassSyn</code>.
   */
  public void addDeclaredField(HField f) {
    declaredFields = 
      (HField[]) Util.grow(declaredFields, f, declaredFields.length);
    fields=null; // invalidate cache.
  }
  public void removeDeclaredField(HField f) throws NoSuchFieldError {
    for (int i=0; i<declaredFields.length; i++) {
      if (declaredFields[i].equals(f)) {
	declaredFields = (HField[]) Util.shrink(declaredFields, i);
	fields=null; // invalidate cache.
	return;
      }
    }
    throw new NoSuchFieldError(f.toString());
  }

  public void addDeclaredMethod(HMethod m) {
    declaredMethods = 
      (HMethod[]) Util.grow(declaredMethods, m, declaredMethods.length);
    methods=null; // invalidate cache.
    constructors=null;
  }
  public void removeDeclaredMethod(HMethod m) throws NoSuchMethodError {
    for (int i=0; i<declaredMethods.length; i++) {
      if (declaredMethods[i].equals(m)) {
	declaredMethods = (HMethod[]) Util.shrink(declaredMethods, i);
	methods=null; // invalidate cache.
	constructors=null;
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
