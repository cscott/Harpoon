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
 * @version $Id: HClassSyn.java,v 1.6.2.10 2000-01-13 23:47:46 cananian Exp $
 * @see harpoon.ClassFile.HClass
 */
class HClassSyn extends HClassCls implements HClassMutator {

  /** Create an <code>HClassSyn</code> from a template <code>HClass</code>.
   *  The new <code>HClassSyn</code> will have the (fully-qualified)
   *  name specified by the <code>name</code> parameter.
   * @param template The template class to use.
   * @param name The fully-qualified name for this class object.
   */
  HClassSyn(Linker l, String name, HClass template) {
    super(l);
    Util.assert(!template.isArray());
    Util.assert(!template.isPrimitive());
    Util.assert(l==template.getLinker());
    this.name = name;
    this.superclass = template.getSuperclass();
    this.interfaces = template.getInterfaces();
    this.modifiers  = template.getModifiers();
    this.sourcefile = template.getSourceFile();

    this.declaredFields = new HFieldSyn[0];
    HField fields[] = template.getDeclaredFields();
    for (int i=0; i < fields.length; i++)
      addDeclaredField(fields[i].getName(), fields[i]);
    Util.assert(fields.length == declaredFields.length);

    this.declaredMethods= new HMethodSyn[0];
    HMethod methods[] = template.getDeclaredMethods();
    for (int i = 0; i < methods.length; i++)
      if (methods[i] instanceof HInitializer)
	addClassInitializer();
      else if (methods[i] instanceof HConstructor)
	addConstructor((HConstructor)methods[i]);
      else
	addDeclaredMethod(methods[i].getName(), methods[i]);
    Util.assert(methods.length == declaredMethods.length);

    hasBeenModified = true; // by default, mark this as 'modified'
  }
  public HClassMutator getMutator() { return this; }

  // implementation of HClassMutator.
  public HField addDeclaredField(String name, HClass type) {
    return addDeclaredField0(new HFieldSyn(this, name, type));
  }
  public HField addDeclaredField(String name, String descriptor) {
    return addDeclaredField0(new HFieldSyn(this, name, descriptor));
  }
  public HField addDeclaredField(String name, HField template) {
    return addDeclaredField0(new HFieldSyn(this, name, template));
  }
  // deal with array housekeeping.
  private HField addDeclaredField0(HField f) {
    Util.assert(f.getDeclaringClass()==this);
    for (int i=0; i<declaredFields.length; i++)
      if (declaredFields[i].equals(f))
	throw new DuplicateMemberException("Field "+f+" in "+this);
    declaredFields = 
      (HField[]) Util.grow(HField.arrayFactory,
			   declaredFields, f, declaredFields.length);
    fields=null; // invalidate cache.
    hasBeenModified=true; // flag the modification
    return f;
  }
  public void removeDeclaredField(HField f) throws NoSuchFieldError {
    for (int i=0; i<declaredFields.length; i++) {
      if (declaredFields[i].equals(f)) {
	declaredFields = (HField[]) Util.shrink(HField.arrayFactory,
						declaredFields, i);
	fields=null; // invalidate cache.
	hasBeenModified=true; // flag the modification
	return;
      }
    }
    throw new NoSuchMemberException("Field "+f);
  }

  public HInitializer addClassInitializer() {
    return (HInitializer)
      addDeclaredMethod0(new HInitializerSyn(this));
  }
  public void removeClassInitializer(HInitializer m) {
    removeDeclaredMethod(m);
  }
  public HConstructor addConstructor(String descriptor) {
    return (HConstructor)
      addDeclaredMethod0(new HConstructorSyn(this, descriptor));
  }
  public HConstructor addConstructor(HClass[] paramTypes) {
    return (HConstructor)
      addDeclaredMethod0(new HConstructorSyn(this, paramTypes));
  }
  public HConstructor addConstructor(HConstructor template) {
    return (HConstructor)
      addDeclaredMethod0(new HConstructorSyn(this, template));
  }
  public void removeConstructor(HConstructor c) {
    removeDeclaredMethod(c);
  }
  public HMethod addDeclaredMethod(String name, String descriptor) {
    Util.assert(!name.equals("<init>") && !name.equals("<clinit>"));
    return addDeclaredMethod0(new HMethodSyn(this, name, descriptor));
  }
  public HMethod addDeclaredMethod(String name, HClass[] paramTypes,
				   HClass returnType) {
    Util.assert(!name.equals("<init>") && !name.equals("<clinit>"));
    return addDeclaredMethod0(new HMethodSyn(this,name,paramTypes,returnType));
  }
  public HMethod addDeclaredMethod(String name, HMethod template) {
    Util.assert(!name.equals("<init>") && !name.equals("<clinit>"));
    return addDeclaredMethod0(new HMethodSyn(this, name, template));
  }

  private HMethod addDeclaredMethod0(HMethod m) {
    Util.assert(m.getDeclaringClass()==this);
    for (int i=0; i<declaredMethods.length; i++)
      if (declaredMethods[i].equals(m))
	throw new DuplicateMemberException("Method "+m+" in "+this);
    declaredMethods = 
      (HMethod[]) Util.grow(HMethod.arrayFactory,
			    declaredMethods, m, declaredMethods.length);
    methods=null; // invalidate cache.
    constructors=null;
    hasBeenModified=true; // flag the modification
    return m;
  }
  public void removeDeclaredMethod(HMethod m) throws NoSuchMethodError {
    for (int i=0; i<declaredMethods.length; i++) {
      if (declaredMethods[i].equals(m)) {
	declaredMethods = (HMethod[]) Util.shrink(HMethod.arrayFactory,
						  declaredMethods, i);
	methods=null; // invalidate cache.
	constructors=null;
	hasBeenModified=true; // flag the modification
	return;
      }
    }
    throw new NoSuchMemberException("Method "+m);
  }

  public void addModifiers(int m) { setModifiers(getModifiers()|m); }
  public void removeModifiers(int m){ setModifiers(getModifiers()&(~m)); }
  public void setModifiers(int m) { 
    // are we changing an interface to a class?
    if ( Modifier.isInterface(modifiers) && !Modifier.isInterface(m)) {
      // make sure there are no superclasses or superinterfaces.
      Util.assert(superclass==null); // should be true for interfaces.
      if (interfaces.length!=0)
	throw new Error("Can't change a subinterface to a class. "+
			"Remove the inheritance first. ("+this+")");
      // inherit from java.lang.Object.
      superclass = getLinker().forName("java.lang.Object");
      // tag all the methods as abstract.
      for (int i=0; i<declaredMethods.length; i++)
	((HMethodSyn)declaredMethods[i]).setModifiers
	  (declaredMethods[i].getModifiers() | Modifier.ABSTRACT);
    }
    // are we changing a class to an interface?
    if (!Modifier.isInterface(modifiers) &&  Modifier.isInterface(m)) {
      // make sure there are no superclasses or superinterfaces.
      if (superclass != null && 
	  superclass.actual() != getLinker().forName("java.lang.Object"))
	throw new Error("Can't change a subclass to an interface. "+
			"Remove the inheritance first. ("+this+")");
      if (interfaces.length!=0)
	throw new Error("Can't change a class implementing interfaces "+
			"to an interface itself. Remove the inheritance "+
			"first. ("+this+")");
      // interfaces have no superclass.
      superclass = null;
      // tag all the methods as abstract
      for (int i=0; i<declaredMethods.length; i++) {
	HMethodSyn hm = (HMethodSyn) declaredMethods[i];
	hm.setModifiers(hm.getModifiers() | Modifier.ABSTRACT);
      }
    }
    if (modifiers!=m) hasBeenModified=true; // flag the modification
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
    if (superclass != sc) hasBeenModified=true; // flag the modification
    superclass = sc;
    constructors=null;
    fields = null;
    methods = null;
  }

  public void addInterface(HClass in) {
    if (!in.isInterface()) throw new Error("Not an interface.");
    interfaces = (HClass[]) Util.grow(HClass.arrayFactory,
				      interfaces, in, interfaces.length);
    hasBeenModified = true;
  }
  public void removeInterface(HClass in) throws NoSuchClassException {
    for (int i=0; i<interfaces.length; i++) {
      if (interfaces[i].equals(in)) {
	interfaces = (HClass[]) Util.shrink(HClass.arrayFactory,
					    interfaces, i);
	hasBeenModified = true;
	return;
      }
    }
    throw new NoSuchClassException(in.toString());
  }
  public void removeAllInterfaces() {
    if (interfaces.length!=0) hasBeenModified = true;
    // wheee.
    interfaces = new HClass[0];
  }

  /**
   * Set the source file name for this class.
   */
  public void setSourceFile(String sf) {
    if (!this.sourcefile.equals(sf)) hasBeenModified = true;
    this.sourcefile = sf;
  }

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
}
// set emacs indentation style.
// Local Variables:
// c-basic-offset:2
// End:
