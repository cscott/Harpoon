// HClassCls.java, created Fri Oct 16  2:21:02 1998 by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.ClassFile;

import harpoon.Util.Util;

import java.lang.reflect.Modifier;

/**
 * Instances of the class <code>HClassCls</code> represent modifiable
 * classes and interfaces of a java program.  Arrays and primitive types
 * are not modifiable, and thus are not represented by 
 * <code>HClassCls</code>.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: HClassCls.java,v 1.2 2002-02-25 21:03:03 cananian Exp $
 * @see harpoon.ClassFile.HClass
 */
abstract class HClassCls extends HClassImpl {
  /** Fully qualified name of the class represented by this 
   *  <code>HClassCls</code> object. */
  String name;
  /** Superclass of this <code>HClassCls</code>. */
  HPointer superclass;
  /** Interfaces of this <code>HClassCls</code>. */
  HPointer interfaces[];
  /** Access flags for this class. */
  int modifiers;
  /** List of fields in this <code>HClassCls</code> object. */
  HField[] declaredFields;
  /** List of methods in this <code>HClassCls</code> object. */
  HMethod[] declaredMethods;
  /** Name of the source file for this class. */
  String sourcefile;
  // CACHES: (reset to null to recompute)
  transient HConstructor[] constructors = null;
  transient HField[] fields = null;
  transient HMethod[] methods = null;

  /** Implementations must provide their own constructor to initialize. */
  protected HClassCls(Linker l) { super(l); }

  /** 
   * Returns the fully-qualified name of the class
   * represented by this <code>HClassCls</code> object,
   * as a <code>String</code>. 
   * @return the fully qualified name of the class or interface
   *         represented by this object.
   */
  public String getName() { return name; }

  /**
   * Returns a ComponentType descriptor for the class represented by this
   * <code>HClassCls</code> object.
   */
  public String getDescriptor() { return "L"+getName().replace('.','/')+";"; }
  
  /**
   * Returns a <code>HField</code> object that reflects the specified
   * declared field of the class or interface represented by this
   * <code>HClassCls</code> object.  The <code>name</code> parameter is a 
   * <code>String</code> that specifies the simple name of the
   * desired field.
   * @exception NoSuchFieldError
   *            if a field with the specified name is not found.
   * @see HField
   */
  public HField getDeclaredField(String name)
    throws NoSuchFieldError {
    // look for field name in master list.
    for (int i=0; i<declaredFields.length; i++)
      if (declaredFields[i].getName().equals(name))
	return declaredFields[i];
    // not found.
    throw new NoSuchFieldError(name);
  }
  /**
   * Returns an array of <code>HField</code> objects reflecting all the
   * fields declared by the class or interface represented by this
   * <code>HClassCls</code> object.  This includes <code>public</code>,
   * <code>protected</code>, default (<code>package</code>) access,
   * and <code>private</code> fields, but excludes inherited fields.
   * Returns an array of length 0 if the class or interface declares
   * no fields.
   * @see "The Java Language Specification,, sections 8.2 and 8.3"
   * @see HField
   */
  public HField[] getDeclaredFields() {
    return (HField[]) Util.safeCopy(HField.arrayFactory, declaredFields);
  }

  /**
   * Returns a <code>HMethod</code> object that reflects the specified 
   * declared method of the class or interface represented by this 
   * <code>HClass</code> object.  The <code>name</code> parameter is a
   * <code>String</code> that specifies the simple name of the desired
   * method, and the <code>parameterTypes</code> parameter is an array
   * of <code>HClass</code> objects that identify the method's formal
   * parameter types, in declared order.
   * @exception NoSuchMethodError
   *            if a matching method is not found.
   * @see HMethod
   */
  public HMethod getDeclaredMethod(String name, HClass parameterTypes[])
    throws NoSuchMethodError {
    // look for method name/type in master list.
    for (int i=0; i<declaredMethods.length; i++)
      if (declaredMethods[i].getName().equals(name)) {
	HClass[] methodParamTypes = declaredMethods[i].getParameterTypes();
	if (methodParamTypes.length == parameterTypes.length) {
	  int j; for (j=0; j<parameterTypes.length; j++)
	    if (methodParamTypes[j] != parameterTypes[j])
	      break; // oops, this one doesn't match.
	  if (j==parameterTypes.length) // hey, we made it to the end!
	    return declaredMethods[i];
	}
      }
    // didn't find a match.  Oh, well.
    StringBuffer msg = new StringBuffer(getName());
    msg.append('.'); msg.append(name); msg.append('(');
    for (int i=0; i<parameterTypes.length; i++)
      msg.append(parameterTypes[i].getDescriptor());
    msg.append(')');
    throw new NoSuchMethodError(msg.toString());
  }
  /**
   * Returns a <code>HMethod</code> object that reflects the specified 
   * declared method of the class or interface represented by this 
   * <code>HClass</code> object.  The <code>name</code> parameter is a
   * <code>String</code> that specifies the simple name of the desired
   * method, and <code>descriptor</code> is a string describing
   * the parameter types and return value of the method.
   * @exception NoSuchMethodError
   *            if a matching method is not found.
   * @see HMethod#getDescriptor
   */
  public HMethod getDeclaredMethod(String name, String descriptor)
    throws NoSuchMethodError {
    // look for method name/type in master list.
    for (int i=0; i<declaredMethods.length; i++)
      if (declaredMethods[i].getName().equals(name) &&
	  declaredMethods[i].getDescriptor().equals(descriptor))
	return declaredMethods[i];
    // didn't find a match.  Oh, well.
    throw new NoSuchMethodError(getName()+"."+name+descriptor);
  }
  /**
   * Returns an array of <code>HMethod</code> objects reflecting all the
   * methods declared by the class or interface represented by this
   * <code>HClass</code> object.  This includes <code>public</code>,
   * <code>protected</code>, default (<code>package</code>) access, and
   * <code>private</code> methods, but excludes inherited methods.
   * Returns an array of length 0 if the class or interface declares no
   * methods, or if this <code>HClass</code> object represents a primitive
   * type.<p>
   * Constructors are included.
   * @see "The Java Language Specification, section 8.2"
   * @see HMethod
   */
  public HMethod[] getDeclaredMethods() {
    return (HMethod[]) Util.safeCopy(HMethod.arrayFactory, declaredMethods);
  }

  /**
   * Returns the Java language modifiers for this class or interface,
   * encoded in an integer.  The modifiers consist of the Java Virtual
   * Machine's constants for public, protected, private, final, and 
   * interface; they should be decoded using the methods of class Modifier.
   * @see "The Java Virtual Machine Specification, table 4.1"
   * @see java.lang.reflect.Modifier
   */
  public int getModifiers() { return modifiers; }

  /**
   * If this object represents any class other than the class 
   * <code>Object</code>, then the object that represents the superclass of 
   * that class is returned. 
   * <p> If this object is the one that represents the class
   * <code>Object</code> or this object represents an interface, 
   * <code>null</code> is returned.
   * @return the superclass of the class represented by this object.
   */
  public HClass getSuperclass() {
    try {
      return (HClass) superclass; // works if superclass is null, too.
    } catch (ClassCastException e) { // superclass was ClassPointer.
      HClass sc = superclass.actual(); // loads HClass from ClassPointer.
      superclass = sc;
      return sc;
    }
  }

  /**
   * Determines the interfaces implemented by the class or interface 
   * represented by this object. 
   * <p> If this object represents a class, the return value is an
   * array containing objects representing all interfaces implemented by 
   * the class.  The order of the interface objects in the array corresponds 
   * to the order of the interface names in the implements clause of the 
   * declaration of the class represented by this object.
   * <p> If the object represents an interface, the array contains objects
   * representing all interfaces extended by the interface.  The order of
   * the interface objects in the array corresponds to the order of the
   * interface names in the extends clause of the declaration of the 
   * interface represented by this object.
   * <p> If the class or interface implements no interfaces, the method
   * returns an array of length 0.
   * @return an array of interfaces implemented by this class.
   */
  public HClass[] getInterfaces() { // should really safeCopy?
    HClass[] in;
    try {
      in = (HClass[]) interfaces;
    } catch (ClassCastException e) { // interfaces was HPointer.
      in = new HClass[interfaces.length];
      for (int i=0; i<in.length; i++)
	in[i] = interfaces[i].actual();
      interfaces = in;
    }
    return (HClass[]) Util.safeCopy(HClass.arrayFactory, in);
  }

  /**
   * Return the name of the source file for this class, or a
   * zero-length string if the information is not available.
   */
  public String getSourceFile() { return sourcefile==null?"":sourcefile; }

  /**
   * Determines if the specified <code>HClass</code> object represents an
   * interface type.
   * @return <code>true</code> is this object represents an interface;
   *         <code>false</code> otherwise.
   */
  public boolean isInterface() { 
    return Modifier.isInterface(getModifiers()); 
  }

  // CACHING CODE:
  public HConstructor[] getConstructors() {
    if (constructors==null)
      constructors = super.getConstructors();
    return (HConstructor[]) Util.safeCopy(HConstructor.arrayFactory, 
					  constructors);
  }
  public HField[] getFields() {
    if (fields==null)
      fields = super.getFields();
    return  (HField[]) Util.safeCopy(HField.arrayFactory, fields);
  }
  public HMethod[] getMethods() {
    if (methods==null)
      methods = super.getMethods();
    return (HMethod[]) Util.safeCopy(HMethod.arrayFactory, methods);
  }
}
// set emacs indentation style.
// Local Variables:
// c-basic-offset:2
// End:
