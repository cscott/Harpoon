// HClassSyn.java, created by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.ClassFile;

import java.lang.reflect.Modifier;
import java.io.InputStream;
import java.io.BufferedInputStream;
import java.util.Hashtable;
import java.util.Vector;

import harpoon.Util.UniqueVector;
import harpoon.Util.Util;

/**
 * Instances of the class <code>HClassSyn</code> represent modifiable
 * classes and interfaces of a java program.  Arrays and primitive types
 * are not modifiable, and thus are not represented by 
 * <code>HClassSyn</code>.  <code>HClassSyn</code> objects are assigned
 * unique names automagically on creation.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: HClassSyn.java,v 1.1 1998-10-14 20:03:26 cananian Exp $
 * @see harpoon.ClassFile.HClass
 */
public class HClassSyn extends HClass {
  // DIMS is always equal to 0 in HCLASSSYN.
  /** Fully qualified name of the class represented by this 
   *  <code>HClassSyn</code> object. */
  String name;
  /** Superclass of this <code>HClassSyn</code>. */
  HClass superclass;
  /** Interfaces of this <code>HClassSyn</code>. */
  HClass interfaces[];
  /** Access flags for this class. */
  int modifiers;
  /** List of fields in this <code>HClassSyn</code> object. */
  HField[] declaredFields = new HField[0];
  /** List of methods in this <code>HClassSyn</code> object. */
  HMethod[] declaredMethods = new HMethod[0];
  /** Name of the source file for this class. */
  String sourcefile;

  /** Create an <code>HClass</code> from a raw classfile. */
  protected HClassSyn(HClass template) {
    this.dims = 0;
    this.name = template.getName();
    this.superclass = template.getSuperclass();
    this.interfaces = template.getInterfaces();
    this.modifiers  = template.getModifiers();
    this.declaredFields = template.getDeclaredFields();
    this.declaredMethods= template.getDeclaredMethods();
    this.sourcefile = template.getSourceFile();
  }

  /** 
   * Returns the fully-qualified name of the type (class, interface,
   * array, or primitive) represented by this <code>HClass</code> object,
   * as a <code>String</code>. 
   * @return the fully qualified name of the class or interface
   *         represented by this object.
   */
  public String getName() { return name; }

  public void setName(String name) { this.name = name; }

  /**
   * Returns a ComponentType descriptor for the type represented by this
   * <code>HClass</code> object.
   */
  public String getDescriptor() { return "L"+getName().replace('.','/')+";"; }
  
  /**
   * Returns a <code>HField</code> object that reflects the specified
   * declared field of the class or interface represented by this
   * <code>HClassSyn</code> object.  The <code>name</code> parameter is a 
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
   * <code>HClass</code> object.  This includes <code>public</code>,
   * <code>protected</code>, default (<code>package</code>) access,
   * and <code>private</code> fields, but excludes inherited fields.
   * Returns an array of length 0 if the class or interface declares
   * no fields.
   * @see "The Java Language Specification,, sections 8.2 and 8.3"
   * @see HField
   */
  public HField[] getDeclaredFields() {
    return HField.copy(declaredFields);
  }

  public void addDeclaredField(HField f) { }
  public void removeDeclaredField(HField f) { }

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
    throw new NoSuchMethodError(name);
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
    throw new NoSuchMethodError(name);
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
    return HMethod.copy(declaredMethods);
  }

  public void addDeclaredMethod(HMethod m) { }
  public void removeDeclaredMethod(HMethod m) { }

  /**
   * Returns the Java language modifiers for this class or interface,
   * encoded in an integer.  The modifiers consist of the Java Virtual
   * Machine's constants for public, protected, private, final, and 
   * interface; they should be decoded using the methods of class Modifier.
   * @see "The Java Virtual Machine Specification, table 4.1"
   * @see java.lang.reflect.Modifier
   */
  public int getModifiers() { return modifiers; }

  public void setModifiers(int m) { }

  /**
   * If this object represents any class other than the class 
   * <code>Object</code>, then the object that represents the superclass of 
   * that class is returned. 
   * <p> If this object is the one that represents the class
   * <code>Object</code> or this object represents an interface, 
   * <code>null</code> is returned.
   * @return the superclass of the class represented by this object.
   */
  public HClass getSuperclass() { return superclass; }

  public void setSuperclass(HClass sc) { }

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
  public HClass[] getInterfaces() { return interfaces; }

  public void addInterface(HClass in) {
  }
  public void removeInterface(HClass in) {
  }

  /**
   * Return the name of the source file for this class, or a
   * zero-length string if the information is not available.
   */
  public String getSourceFile() { return sourcefile==null?"":sourcefile; }

  /**
   * Set the source file name for this class.
   */
  public void setSourceFile(String sf) { this.sourcefile = sf; }

  /**
   * @return <code>false</code>.
   */
  public boolean isArray() { return false; }

  /**
   * @return <code>false</code>.
   */
  public boolean isPrimitive() { return false; }

}
// set emacs indentation style.
// Local Variables:
// c-basic-offset:2
// End:
