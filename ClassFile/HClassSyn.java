// HClassSyn.java, created Wed Oct 14 16:03:26 1998 by cananian
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
 * @version $Id: HClassSyn.java,v 1.6.2.6 1999-08-07 11:17:21 cananian Exp $
 * @see harpoon.ClassFile.HClass
 */
public class HClassSyn extends HClassCls {
  /** Create an <code>HClassSyn</code> from an <code>HClass</code>. */
  public HClassSyn(HClass template) {
    Util.assert(!template.isArray());
    Util.assert(!template.isPrimitive());
    this.name = uniqueName(template.getName()); register();
    this.superclass = template.getSuperclass();
    this.interfaces = template.getInterfaces();
    this.modifiers  = template.getModifiers();
    this.sourcefile = template.getSourceFile();

    this.declaredFields = new HFieldSyn[0];
    HField fields[] = template.getDeclaredFields();
    for (int i=0; i < fields.length; i++) {
      new HFieldSyn(this, fields[i]);
    }
    Util.assert(fields.length == declaredFields.length);

    this.declaredMethods= new HMethodSyn[0];
    HMethod methods[] = template.getDeclaredMethods();
    for (int i = 0; i < methods.length; i++){
      new HMethodSyn(this, methods[i]);
    }
    Util.assert(methods.length == declaredMethods.length);
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
      (HField[]) Util.grow(HField.arrayFactory,
			   declaredFields, f, declaredFields.length);
    fields=null; // invalidate cache.
  }
  public void removeDeclaredField(HField f) throws NoSuchFieldError {
    for (int i=0; i<declaredFields.length; i++) {
      if (declaredFields[i].equals(f)) {
	declaredFields = (HField[]) Util.shrink(HField.arrayFactory,
						declaredFields, i);
	fields=null; // invalidate cache.
	return;
      }
    }
    throw new NoSuchFieldError(f.toString());
  }

  public void addDeclaredMethod(HMethod m) {
    // only one class initializer ever.
    if (m instanceof HInitializer) Util.assert(getClassInitializer()==null);
    declaredMethods = 
      (HMethod[]) Util.grow(HMethod.arrayFactory,
			    declaredMethods, m, declaredMethods.length);
    methods=null; // invalidate cache.
    constructors=null;
  }
  public void removeDeclaredMethod(HMethod m) throws NoSuchMethodError {
    for (int i=0; i<declaredMethods.length; i++) {
      if (declaredMethods[i].equals(m)) {
	declaredMethods = (HMethod[]) Util.shrink(HMethod.arrayFactory,
						  declaredMethods, i);
	methods=null; // invalidate cache.
	constructors=null;
	return;
      }
    }
    throw new NoSuchMethodError(m.toString());
  }
  public void setClassInitializer(HInitializer m) {
    HInitializer hi = getClassInitializer();
    if (hi!=null) removeDeclaredMethod(hi);
    Util.assert(getClassInitializer()==null);
    addDeclaredMethod(m);
  }

  public void setModifiers(int m) { 
    // are we changing an interface to a class?
    if ( Modifier.isInterface(modifiers) && !Modifier.isInterface(m)) {
      // make sure there are no superclasses or superinterfaces.
      Util.assert(superclass==null); // should be true for interfaces.
      if (interfaces.length!=0)
	throw new Error("Can't change a subinterface to a class. "+
			"Remove the inheritance first. ("+this+")");
      // inherit from java.lang.Object.
      superclass = HClass.forName("java.lang.Object");
      // tag all the methods as abstract.
      for (int i=0; i<declaredMethods.length; i++)
	((HMethodSyn)declaredMethods[i]).setModifiers
	  (declaredMethods[i].getModifiers() | Modifier.ABSTRACT);
    }
    // are we changing a class to an interface?
    if (!Modifier.isInterface(modifiers) &&  Modifier.isInterface(m)) {
      // make sure there are no superclasses or superinterfaces.
      if (superclass != null && 
	  superclass.actual() != HClass.forName("java.lang.Object"))
	throw new Error("Can't change a subclass to an interface. "+
			"Remove the inheritance first. ("+this+")");
      if (interfaces.length!=0)
	throw new Error("Can't change a class implementing interfaces "+
			"to an interface itself. Remove the inheritance "+
			"first. ("+this+")");
      // interfaces have no superclass.
      superclass = null;
      // tag all the methods as abstract & strip the code.
      for (int i=0; i<declaredMethods.length; i++) {
	HMethodSyn hm = (HMethodSyn) declaredMethods[i];
	hm.setModifiers(hm.getModifiers() | Modifier.ABSTRACT);
	hm.removeAllCode();
      }
    }
    modifiers = m;
  }

  public void setSuperclass(HClass sc) {
    if (sc==null)
      throw new Error("This is very odd.  " +
		      "Do you realize you're trying to create a new "+
		      "top-level object?  I'm not sure I should allow this."+
		      "  Please mail me at cananian@alumni.princeton.edu and "+
		      "tell me why you think it's a good idea.");
    // XXX more sanity checks?
    superclass = sc;
  }

  public void addInterface(HClass in) {
    if (!in.isInterface()) throw new Error("Not an interface.");
    interfaces = (HClass[]) Util.grow(HClass.arrayFactory,
				      interfaces, in, interfaces.length);
  }
  public void removeInterface(HClass in) throws NoClassDefFoundError {
    for (int i=0; i<interfaces.length; i++) {
      if (interfaces[i].equals(in)) {
	interfaces = (HClass[]) Util.shrink(HClass.arrayFactory,
					    interfaces, i);
	return;
      }
    }
    throw new NoClassDefFoundError(in.toString());
  }
  public void removeAllInterfaces() {
    // wheee.
    interfaces = new HClass[0];
  }

  /**
   * Set the source file name for this class.
   */
  public void setSourceFile(String sf) { this.sourcefile = sf; }

  /** Serializable interface. */
  public Object writeReplace() { return this; }
  /** Serializable interface. */
  public void writeObject(java.io.ObjectOutputStream out)
    throws java.io.IOException {
    // resolve class name pointers.
    this.superclass = this.superclass.actual();
    for (int i=0; i<this.interfaces.length; i++)
      this.interfaces[i] = this.interfaces[i].actual();
    // intern strings.
    this.name = this.name.intern();
    this.sourcefile = this.sourcefile.intern();
    // write class data.
    out.defaultWriteObject();
  }
  /** Serializable interface. */
  public void readObject(java.io.ObjectInputStream in)
    throws java.io.IOException, ClassNotFoundException {
    // read class data.
    in.defaultReadObject();
    // make name unique & register it.
    this.name = uniqueName(name); register();
  }
}
// set emacs indentation style.
// Local Variables:
// c-basic-offset:2
// End:
