// HClassImpl.java, created Fri Jul 31  4:33:28 1998 by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.ClassFile;

import harpoon.Util.ArrayFactory;
import harpoon.Util.ReferenceUnique;
import harpoon.Util.UniqueVector;
import harpoon.Util.Util;

import java.lang.reflect.Modifier;
import java.util.Hashtable;
import java.util.Vector;

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
 * @version $Id: HClassImpl.java,v 1.1.4.1 2000-01-13 23:47:46 cananian Exp $
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
   * Returns a <code>HField</code> object that reflects the specified
   * accessible member field of the class or interface represented by this
   * <code>HClass</code> object.  The <code>name</code> parameter is
   * a <code>String</code> specifying the simple name of the
   * desired field. <p>
   * The field to be reflected is located by searching all member fields
   * of the class or interface represented by this <code>HClass</code>
   * object (and its superclasses and interfaces) for an accessible 
   * field with the specified name.
   * @see "The Java Language Specification, sections 8.2 and 8.3"
   * @exception NoSuchFieldError
   *            if a field with the specified name is not found.
   * @see HField
   */
  public HField getField(String name) throws NoSuchFieldError {
    // construct master field list, if we haven't already.
    HField[] fields=getFields();
    // look for field name in master field list.
    // look backwards to be sure we find local fields first (scoping)
    for (int i=fields.length-1; i>=0; i--)
      if (fields[i].getName().equals(name))
	return fields[i];
    // can't find it.
    throw new NoSuchFieldError(getName()+"."+name);
  }
  /**
   * Returns an array containing <code>HField</code> objects reflecting
   * all the accessible fields of the class or interface represented by this
   * <code>HClass</code> object.  Returns an array of length 0 if the
   * class or interface has no accessible fields, or if it represents an 
   * array type or a primitive type. <p>
   * Specifically, if this <code>HClass</code> object represents a class,
   * returns the accessible fields of this class and of all its superclasses.
   * If this <code>HClass</code> object represents an interface, returns 
   * the accessible fields
   * of this interface and of all its superinterfaces.  If this 
   * <code>HClass</code> object represents an array type or a primitive
   * type, returns an array of length 0. <p>
   * The implicit length field for array types is not reflected by this
   * method.
   * @see "The Java Language Specification, sections 8.2 and 8.3"
   * @see HField
   */
  public HField[] getFields() { 
    if (isPrimitive() || isArray())
      return new HField[0];
    // do the actual work.
    UniqueVector v = new UniqueVector();
    // add fields from interfaces.
    HClass[] in = getInterfaces();
    for (int i=0; i<in.length; i++) {
      HField[] inf = in[i].getFields();
      for (int j=0; j<inf.length; j++)
	v.addElement(inf[j]);
    }
    // now fields from superclasses, subject to access mode constraints.
    HClass sup = getSuperclass();
    HField supf[] = (sup==null)?new HField[0]:sup.getFields();
    for (int i=0; i<supf.length; i++) {
      int m = supf[i].getModifiers();
      // private fields of superclasses are invisible.
      if (Modifier.isPrivate(m))
	continue; // skip this field.
      // default access is invisible if packages not identical.
      /** DISABLED: see notes in getMethods() [CSA 6-22-99] */
      /*
      if (!Modifier.isPublic(m) && !Modifier.isProtected(m))
	if (!supf[i].getDeclaringClass().getPackage().equals(frmPackage))
	  continue;
      */
      // all's good. Add this one.
      v.addElement(supf[i]);
    }
    // now fields from our local class.
    HField locf[] = getDeclaredFields();
    for (int i=0; i<locf.length; i++)
      v.addElement(locf[i]);
    
    // Merge into one array.
    return (HField[]) v.toArray(new HField[v.size()]);
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
   * Returns an <code>HMethod</code> object that reflects the specified
   * accessible method of the class or interface represented by this
   * <code>HClass</code> object.  The <code>name</code> parameter is
   * a string specifying the simple name of the desired method, and
   * the <code>parameterTypes</code> parameter is an array of
   * <code>HClass</code> objects that identify the method's formal
   * parameter types, in declared order. <p>
   * The method to reflect is located by searching all the member methods
   * of the class or interface represented by this <code>HClass</code>
   * object for an accessible method with the specified name and exactly
   * the same formal parameter types.
   * @see "The Java Language Specification, sections 8.2 and 8.4"
   * @exception NoSuchMethodError if a matching method is not found.
   */
  public HMethod getMethod(String name, HClass parameterTypes[])
    throws NoSuchMethodError {
    // construct master method list, if we haven't already.
    HMethod[] methods=getMethods();
    // look for method name in master method list.
    // look backwards to be sure we find local methods first (scoping).
    for (int i=methods.length-1; i>=0; i--)
      if (methods[i].getName().equals(name)) {
	HClass[] methodParamTypes = methods[i].getParameterTypes();
	if (methodParamTypes.length == parameterTypes.length) {
	  int j; for (j=0; j<parameterTypes.length; j++)
	    if (methodParamTypes[j] != parameterTypes[j])
	      break; // oops, this one doesn't match.
	  if (j==parameterTypes.length) // hey, we made it to the end!
	    return methods[i];
	}
      }
    // didn't find a match. Oh, well.
    throw new NoSuchMethodError(getName()+"."+name);
  }
  /**
   * Returns an <code>HMethod</code> object that reflects the specified
   * accessible method of the class or interface represented by this
   * <code>HClass</code> object.  The <code>name</code> parameter is
   * a string specifying the simple name of the desired method, and
   * the <code>descriptor</code> is a string describing the
   * parameter types and return value of the method. <p>
   * The method is located by searching all the member methods of
   * the class or interface represented by this <code>HClass</code>
   * object for an accessible method with the specified name and
   * exactly the same descriptor.
   * @see HMethod#getDescriptor
   * @exception NoSuchMethodError if a matching method is not found.
   */
  public HMethod getMethod(String name, String descriptor)
    throws NoSuchMethodError {
    // construct master method list, if we haven't already.
    HMethod[] methods=getMethods();
    // look for method name in master method list.
    // look backwards to be sure we find local methods first (scoping)
    for (int i=methods.length-1; i>=0; i--)
      if (methods[i].getName().equals(name) &&
	  methods[i].getDescriptor().equals(descriptor))
	return methods[i];
    // didn't find a match.
    throw new NoSuchMethodError(getName()+"."+name+"/"+descriptor);
  }
	
  /**
   * Returns an array containing <code>HMethod</code> object reflecting
   * all accessible member methods of the class or interface represented
   * by this <code>HClass</code> object, including those declared by
   * the class or interface and those inherited from superclasses and
   * superinterfaces.  Returns an array of length 0 if the class or
   * interface has no public member methods, or if the <code>HClass</code>
   * corresponds to a primitive type or array type.<p>
   * Constructors are included.
   * @see "The Java Language Specification, sections 8.2 and 8.4"
   */
  public HMethod[] getMethods() {
    if (isPrimitive())
      return new HMethod[0];
    // do the actual work.
    Hashtable h = new Hashtable(); // keep track of overriding
    Vector v = new Vector(); // accumulate results.

    // first methods we declare locally.
    HMethod[] locm = getDeclaredMethods();
    for (int i=0; i<locm.length; i++) {
      h.put(locm[i].getName()+locm[i].getDescriptor(), locm[i]);
      v.addElement(locm[i]);
    }
    locm=null; // free memory

    // grab fields from superclasses, subject to access mode constraints.
    HClass sup = getSuperclass();
    HMethod supm[] = (sup==null)?new HMethod[0]:sup.getMethods();
    for (int i=0; i<supm.length; i++) {
      int m = supm[i].getModifiers();
      // private methods of superclasses are invisible.
      if (Modifier.isPrivate(m))
	continue; // skip this method.
      // default access is invisible if packages not identical
      /** SKIPPING this test, because the interpreter doesn't like it.
       **  For example, harpoon.IR.Quads.OPER invokes
       **  OperVisitor.dispatch() in method visit().  But dispatch() has
       **  package visibility and thus doesn't show up in
       **  SCCAnalysis...operVisitor, and a virtual dispatch to visit()
       **  on an object of type SCCAnalysis...operVisitor fails.  Current
       **  solution is to move this check into the interpreter; see
       **  harpoon.Interpret.Quads.Method. [CSA, 6-22-99] */
      /*
      if (!Modifier.isPublic(m) && !Modifier.isProtected(m))
	if (!supm[i].getDeclaringClass().getPackage().equals(frmPackage))
	  continue; // skip this (inaccessible) method.
      */
      // skip superclass constructors.
      if (supm[i] instanceof HConstructor)
	  continue;
      // don't add methods which are overriden by locally declared methods.
      if (h.containsKey(supm[i].getName()+supm[i].getDescriptor()))
	continue;
      // all's good.  Add this one.
      h.put(supm[i].getName()+supm[i].getDescriptor(), supm[i]);
      v.addElement(supm[i]);
    }
    sup=null; supm=null; // free memory.

    // Lastly, interface methods, if not already declared.
    // [interface methods will typically be explicitly declared in classes,
    //  even if not implemented (abstract), but superinterface methods aren't
    //  declared explicitly in interfaces.]
    HClass[] intc = getInterfaces();
    for (int i=0; i<intc.length; i++) {
      HMethod intm[] = intc[i].getMethods();
      for (int j=0; j<intm.length; j++) {
	// don't add methods which are overridden by locally declared methods
	if (h.containsKey(intm[j].getName()+intm[j].getDescriptor()))
	  continue;
	v.addElement(intm[j]);
      }
    }
    intc = null; // free memory.

    // Merge into a single array.
    return (HMethod[]) v.toArray(new HMethod[v.size()]);
  }

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
   * If this <code>HClass</code> is a primitive type, return the
   * wrapper class for values of this type.  For example:<p>
   * <DL><DD><CODE>HClass.forDescriptor("I").getWrapper()</CODE></DL><p>
   * will return <code>HClass.forName("java.lang.Integer")</code>.
   * Calling <code>getWrapper</code> with a non-primitive <code>HClass</code>
   * will return the value <code>null</code>.
   */
  public HClass getWrapper() {
    if (this==this.Boolean) return getLinker().forName("java.lang.Boolean");
    if (this==this.Byte)    return getLinker().forName("java.lang.Byte");
    if (this==this.Char)    return getLinker().forName("java.lang.Character");
    if (this==this.Double)  return getLinker().forName("java.lang.Double");
    if (this==this.Float)   return getLinker().forName("java.lang.Float");
    if (this==this.Int)     return getLinker().forName("java.lang.Integer");
    if (this==this.Long)    return getLinker().forName("java.lang.Long");
    if (this==this.Short)   return getLinker().forName("java.lang.Short");
    if (this==this.Void)    return getLinker().forName("java.lang.Void");
    return null; // not a primitive type;
  }

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

  /**
   * Determines if the class or interface represented by this 
   * <code>HClass</code> object is either the same as, or is a superclass
   * or superinterface of, the class or interface represented by the 
   * specified <code>HClass</code> parameter.  It returns
   * <code>true</code> if so, <code>false</code> otherwise.  If this
   * <code>HClass</code> object represents a primitive type, returns
   * <code>true</code> if the specified <code>HClass</code> parameter is
   * exactly this <code>HClass</code> object, <code>false</code>
   * otherwise.
   * <p> Specifically, this method tests whether the type represented
   * by the specified <code>HClass</code> parameter can be converted
   * to the type represented by this <code>HClass</code> object via an
   * identity conversion or via a widening reference conversion.
   * @see "The Java Language Specification, sections 5.1.1 and 5.1.4"
   * @exception NullPointerException
   *            if the specified <code>HClass</code> parameter is null.
   */
  public boolean isAssignableFrom(HClass cls) {
    if (cls==null) throw new NullPointerException();
    // test identity conversion.
    if (cls==this) return true;
    // widening reference conversions...
    if (this.isPrimitive()) return false;
    // widening reference conversions from the null type:
    if (cls==HClass.Void) return true;
    if (cls.isPrimitive()) return false;
    // widening reference conversions from an array:
    if (cls.isArray()) {
      if (this == getLinker().forName("java.lang.Object")) return true;
      if (this == getLinker().forName("java.lang.Cloneable")) return true;
      // see http://java.sun.com/docs/books/jls/clarify.html
      if (this == getLinker().forName("java.io.Serializable")) return true;
      if (isArray() &&
	  !getComponentType().isPrimitive() &&
	  !cls.getComponentType().isPrimitive() &&
	  getComponentType().isAssignableFrom(cls.getComponentType()))
	return true;
      return false;
    }
    // widening reference conversions from an interface type.
    if (cls.isInterface()) {
      if (this.isInterface() && this.isSuperinterfaceOf(cls)) return true;
      if (this == getLinker().forName("java.lang.Object")) return true;
      return false;
    }
    // widening reference conversions from a class type:
    if (!this.isInterface() && this.isSuperclassOf(cls)) return true;
    if (this.isInterface() && this.isSuperinterfaceOf(cls)) return true;
    return false;
  }

  /**
   * Determines if this <code>HClass</code> is a superclass of a given
   * <code>HClass</code> <code>hc</code>. 
   * [Does not look at interface information.]
   * @return <code>true</code> if <code>this</code> is a superclass of
   *         <code>hc</code>, <code>false</code> otherwise.
   */
  public boolean isSuperclassOf(HClass hc) {
    Util.assert(!this.isInterface());
    for ( ; hc!=null; hc = hc.getSuperclass())
      if (this == hc) return true;
    return false;
  }

  /**
   * Determines if this <code>HClass</code> is a superinterface of a given
   * <code>HClass</code> <code>hc</code>. 
   * [does not look at superclass information]
   * @return <code>true</code> if <code>this</code> is a superinterface of
   *         <code>hc</code>, <code>false</code> otherwise.
   */
  public boolean isSuperinterfaceOf(HClass hc) {
    Util.assert(this.isInterface());
    UniqueVector uv = new UniqueVector();//unique in case of circularity 
    for ( ; hc!=null; hc = hc.getSuperclass())
      uv.addElement(hc);

    for (int i=0; i<uv.size(); i++)
      if (uv.elementAt(i) == this) return true;
      else {
	HClass in[] = ((HClass)uv.elementAt(i)).getInterfaces();
	for (int j=0; j<in.length; j++)
	  uv.addElement(in[j]);
      }
    // ran out of possibilities.
    return false;
  }

  /**
   * Determines if this <code>HClass</code> is an instance of the given
   * <code>HClass</code> <code>hc</code>.
   */
  public boolean isInstanceOf(HClass hc) {
    if (this.isArray()) {
      if (!hc.isArray()) 
	// see http://java.sun.com/docs/books/jls/clarify.html
	return (hc==getLinker().forName("java.lang.Cloneable") ||
		hc==getLinker().forName("java.io.Serializable") ||
		hc==getLinker().forName("java.lang.Object"));
      HClass SC = this.getComponentType();
      HClass TC = hc.getComponentType();
      return ((SC.isPrimitive() && TC.isPrimitive() && SC==TC) ||
	      (!SC.isPrimitive()&&!TC.isPrimitive() && SC.isInstanceOf(TC)));
    } else { // not array.
      if (hc.isInterface())
	return hc.isSuperinterfaceOf(this);
      else // hc is class.
	if (this.isInterface()) // in recursive eval of array instanceof.
	  return (hc==getLinker().forName("java.lang.Object"));
	else return hc.isSuperclassOf(this);
    }
  }

  /**
   * Converts the object to a string.  The string representation is the
   * string <code>"class"</code> or <code>"interface"</code> followed by
   * a space and then the fully qualified name of the class.  If this
   * <code>HClass</code> object represents a primitive type,
   * returns the name of the primitive type.
   * @return a string representation of this class object.
   */
  public abstract String toString();

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
