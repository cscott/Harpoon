// HClassImpl.java, created Fri Jul 31  4:33:28 1998 by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.ClassFile;

import harpoon.Util.ArrayFactory;
import harpoon.Util.ReferenceUnique;
import harpoon.Util.Collections.UniqueVector;
import harpoon.Util.Util;

import java.lang.reflect.Modifier;

/**
 * <code>HClassImpl</code> is a common parent for implementations of
 * <code>HClass</code>.  Linker proxy classes should instead have
 * <code>HClassProxy</code> as a parent.
 * <p>
 * There is no public constructor for the class <code>HClassImpl</code>.
 * <code>HClass</code> objects are created with the <code>forName</code>,
 * <code>forDescriptor</code> and <code>forClass</code> methods of a
 * <code>Linker</code> object.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: HClassImpl.java,v 1.2 2002-02-25 21:03:03 cananian Exp $
 * @see harpoon.IR.RawClass.ClassFile
 * @see java.lang.Class
 */
abstract class HClassImpl extends HClass
  implements java.io.Serializable {

  /** Protected constructor, not for external use. */
  HClassImpl(Linker l) { super(l); }

  /**
   * If this class represents an array type, returns the <code>HClass</code>
   * object representing the component type of the array; otherwise returns
   * null.
   * @see java.lang.reflect.Array
   */
  public HClass getComponentType() { return null; }

  /** 
   * Returns the fully-qualified name of the type (class, interface,
   * array, or primitive) represented by this <code>HClass</code> object,
   * as a <code>String</code>. 
   * @return the fully qualified name of the class or interface
   *         represented by this object.
   */
  public abstract String getName();

  /**
   * Returns the package name of this <code>HClass</code>.  If this
   * <code>HClass</code> represents a primitive or array type,
   * then returns null.  Returns <code>""</code> (a zero-length string)
   * if this class is not in a package.
   */
  public String getPackage() {
    if (isPrimitive() || isArray()) return null;
    String fullname = getName();
    int lastdot = fullname.lastIndexOf('.');
    if (lastdot<0) return ""; // no package.
    else return fullname.substring(0, lastdot);
  }
  /**
   * Returns a ComponentType descriptor for the type represented by this
   * <code>HClass</code> object.
   */
  public abstract String getDescriptor();

  /**
   * Returns a <code>HField</code> object that reflects the specified
   * declared field of the class or interface represented by this
   * <code>HClass</code> object.  The <code>name</code> parameter is a 
   * <code>String</code> that specifies the simple name of the
   * desired field.
   * @exception NoSuchFieldError
   *            if a field with the specified name is not found.
   * @see HField
   */
  public HField getDeclaredField(String name)
    throws NoSuchFieldError {
    // construct master declaredField list, if we haven't already.
    HField[] declaredFields=getDeclaredFields();
    // look for field name in master list.
    for (int i=0; i<declaredFields.length; i++)
      if (declaredFields[i].getName().equals(name))
	return declaredFields[i];
    // not found.
    throw new NoSuchFieldError(getName()+"."+name);
  }
  /**
   * Returns an array of <code>HField</code> objects reflecting all the
   * fields declared by the class or interface represented by this
   * <code>HClass</code> object.  This includes <code>public</code>,
   * <code>protected</code>, default (<code>package</code>) access,
   * and <code>private</code> fields, but excludes inherited fields.
   * Returns an array of length 0 if the class or interface declares
   * no fields, or if this <code>HClass</code> object represents a
   * primitive type.
   * @see "The Java Language Specification, sections 8.2 and 8.3"
   * @see HField
   */
  public abstract HField[] getDeclaredFields();

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
    // construct master declaredMethod list, if we haven't already.
    HMethod[] declaredMethods=getDeclaredMethods();
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
    throw new NoSuchMethodError(getName()+"."+name);
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
    // construct master declaredMethod list, if we haven't already.
    HMethod[] declaredMethods=getDeclaredMethods();
    // look for method name/type in master list.
    for (int i=0; i<declaredMethods.length; i++)
      if (declaredMethods[i].getName().equals(name) &&
	  declaredMethods[i].getDescriptor().equals(descriptor))
	return declaredMethods[i];
    // didn't find a match.  Oh, well.
    throw new NoSuchMethodError(getName()+"."+name+"/"+descriptor);
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
  public abstract HMethod[] getDeclaredMethods();

  /**
   * Returns an <code>HConstructor</code> object that reflects the 
   * specified declared constructor of the class or interface represented 
   * by this <code>HClass</code> object.  The <code>parameterTypes</code>
   * parameter is an array of <code>HClass</code> objects that
   * identify the constructor's formal parameter types, in declared order.
   * @exception NoSuchMethodError if a matching method is not found.
   */
  public HConstructor getConstructor(HClass parameterTypes[])
    throws NoSuchMethodError {
    return (HConstructor) getDeclaredMethod("<init>", parameterTypes);
  }

  /**
   * Returns an array of <code>HConstructor</code> objects reflecting
   * all the constructors declared by the class represented by the
   * <code>HClass</code> object.  These are <code>public</code>,
   * <code>protected</code>, default (package) access, and
   * <code>private</code> constructors.  Returns an array of length 0
   * if this <code>HClass</code> object represents an interface or a
   * primitive type.
   * @see "The Java Language Specification, section 8.2"
   */
  public HConstructor[] getConstructors() {
    HConstructor[] constructors;
    if (isPrimitive() || isArray() || isInterface())
      constructors = new HConstructor[0];
    else {
      HMethod[] hm = getMethods();
      int n=0;
      for (int i=0; i<hm.length; i++)
	if (hm[i] instanceof HConstructor)
	  n++;
      constructors = new HConstructor[n];
      for (int i=0; i<hm.length; i++)
	if (hm[i] instanceof HConstructor)
	  constructors[--n] = (HConstructor) hm[i];
    }
    return constructors;
  }

  /**
   * Returns the class initializer method, if there is one; otherwise
   * <code>null</code>.
   * @see "The Java Virtual Machine Specification, section 3.8"
   */
  public HInitializer getClassInitializer() {
    try {
      return (HInitializer) getDeclaredMethod("<clinit>", new HClass[0]);
    } catch (NoSuchMethodError e) {
      return null;
    }
  }

  /**
   * Returns the Java language modifiers for this class or interface,
   * encoded in an integer.  The modifiers consist of the Java Virtual
   * Machine's constants for public, protected, private, final, and 
   * interface; they should be decoded using the methods of class Modifier.
   * @see "The Java Virtual Machine Specification, table 4.1"
   * @see java.lang.reflect.Modifier
   */
  public abstract int getModifiers();

  /**
   * If this object represents any class other than the class 
   * <code>Object</code>, then the object that represents the superclass of 
   * that class is returned. 
   * <p> If this object is the one that represents the class
   * <code>Object</code> or this object represents an interface, 
   * <code>null</code> is returned.
   * If this object represents an array, then the <code>HClass</code>
   * representing <code>java.lang.Object</code> is returned.
   * @return the superclass of the class represented by this object.
   */
  public abstract HClass getSuperclass();

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
   * <p><b>NOTE THAT the array returned does NOT contain interfaces
   * implemented by superclasses.</b>  Thus the interface list may
   * be incomplete.  This is pretty bogus behaviour, but it's what
   * our prototype, <code>java.lang.Class</code>, does.
   * @return an array of interfaces implemented by this class.
   */
  public abstract HClass[] getInterfaces();

  /**
   * Return the name of the source file for this class, or a
   * zero-length string if the information is not available.
   * @see harpoon.IR.RawClass.AttributeSourceFile
   */
  public String getSourceFile() { return ""; }

  /**
   * If this <code>HClass</code> object represents an array type, 
   * returns <code>true</code>, otherwise returns <code>false</code>.
   */
  public boolean isArray() { return false; }
  /**
   * Determines if the specified <code>HClass</code> object represents an
   * interface type.
   * @return <code>true</code> is this object represents an interface;
   *         <code>false</code> otherwise.
   */
  public boolean isInterface() { return false; }

  /**
   * Determines if the specified <code>HClass</code> object represents a
   * primitive Java type. <p>
   * There are nine predefined <code>HClass</code> objects to represent
   * the eight primitive Java types and void.
   */
  public boolean isPrimitive() { return false; }

  /** Serializable interface. Override if implementation has information
   *  which the linker cannot reconstruct. */
  public Object writeReplace() {
    if (!hasBeenModified()) return new HClassStub(this);
    else return this; // cannot reconstruct; write this out instead
  }
  private static final class HClassStub implements java.io.Serializable {
    private final String desc; private final Linker l;
    HClassStub(HClass c) // store only descriptor and linker for resolution.
    { this.desc = c.getDescriptor().intern(); this.l = c.getLinker(); }
    public Object readResolve() { return l.forDescriptor(desc); }
  }
}
// set emacs indentation style.
// Local Variables:
// c-basic-offset:2
// End:
