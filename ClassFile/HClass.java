// HClass.java, created Fri Jul 31  4:33:28 1998 by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.ClassFile;

import harpoon.Util.ArrayFactory;
import harpoon.Util.ReferenceUnique;
import harpoon.Util.UniqueVector;
import harpoon.Util.Util;

import java.lang.reflect.Modifier;

/**
 * Instances of the class <code>HClass</code> represent classes and 
 * interfaces of a java program.  Every array also belongs to a
 * class that is reflected as a <code>HClass</code> object that is
 * shared by all arrays with the same element type and number of
 * dimensions.  Finally, the primitive Java types
 * (<code>boolean</code>, <code>byte</code>, <code>char</code>,
 * <code>short</code>, <code>int</code>, <code>long</code>,
 * <code>float</code>, and <code>double</code>) and the keyword
 * <code>void</code> are also represented as <code>HClass</code> objects.
 * <p>
 * There is no public constructor for the class <code>HClass</code>.
 * <code>HClass</code> objects are created with the <code>forName</code>,
 * <code>forDescriptor</code> and <code>forClass</code> methods of this
 * class.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: HClass.java,v 1.41.2.29.2.5 2000-01-11 15:30:36 cananian Exp $
 * @see harpoon.IR.RawClass.ClassFile
 * @see java.lang.Class
 */
public abstract class HClass extends HPointer
  implements java.lang.Comparable, ReferenceUnique {
  /** The linker responsible for the resolution of this <code>HClass</code>
   *  object. */
  private final Linker _linker;
  boolean hasBeenModified = false;

  /** Protected constructor, not for external use. */
  HClass(Linker l) { _linker = l; }

  // REMOVE ME
  public static final HClass forDescriptor(String desc) {
    return Loader.systemLinker.forDescriptor(desc);
  }
  public static final HClass forName(String name) {
    return Loader.systemLinker.forName(name);
  }
  public static final HClass forClass(Class cls) {
    return Loader.systemLinker.forClass(cls);
  }
  // REMOVE ME
  /**
   * Returns the linker responsible for the resolution of this
   * <code>HClass</code> object.
   */
  public final Linker getLinker() { return _linker; }

  /**
   * Returns a mutator for this <code>HClass</code>, or <code>null</code>
   * if this object is immutable.
   */
  public HClassMutator getMutator() { return null; }

  /**
   * Determines whether any part of this <code>HClass</code> has been
   * modified from its originally loaded state.
   */
  public boolean hasBeenModified() { return hasBeenModified; }

  /**
   * If this class represents an array type, returns the <code>HClass</code>
   * object representing the component type of the array; otherwise returns
   * null.
   * @see java.lang.reflect.Array
   */
  public abstract HClass getComponentType();

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
  public abstract String getPackage();

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
  public abstract HField getDeclaredField(String name)
    throws NoSuchFieldError;

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
  public abstract HField getField(String name)
    throws NoSuchFieldError;

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
  public abstract HField[] getFields();

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
  public abstract HMethod getDeclaredMethod(String name,
					    HClass parameterTypes[])
    throws NoSuchMethodError;

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
  public abstract HMethod getDeclaredMethod(String name, String descriptor)
    throws NoSuchMethodError;

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
  public abstract HMethod getMethod(String name, HClass parameterTypes[])
    throws NoSuchMethodError;

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
  public abstract HMethod getMethod(String name, String descriptor)
    throws NoSuchMethodError;
	
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
  public abstract HMethod[] getMethods();

  /**
   * Returns an <code>HConstructor</code> object that reflects the 
   * specified declared constructor of the class or interface represented 
   * by this <code>HClass</code> object.  The <code>parameterTypes</code>
   * parameter is an array of <code>HClass</code> objects that
   * identify the constructor's formal parameter types, in declared order.
   * @exception NoSuchMethodError if a matching method is not found.
   */
  public abstract HConstructor getConstructor(HClass parameterTypes[])
    throws NoSuchMethodError;

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
  public abstract HConstructor[] getConstructors();

  /**
   * Returns the class initializer method, if there is one; otherwise
   * <code>null</code>.
   * @see "The Java Virtual Machine Specification, section 3.8"
   */
  public abstract HInitializer getClassInitializer();

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
  public abstract String getSourceFile();

  /**
   * If this <code>HClass</code> is a primitive type, return the
   * wrapper class for values of this type.  For example:<p>
   * <DL><DD><CODE>HClass.forDescriptor("I").getWrapper()</CODE></DL><p>
   * will return <code>HClass.forName("java.lang.Integer")</code>.
   * Calling <code>getWrapper</code> with a non-primitive <code>HClass</code>
   * will return the value <code>null</code>.
   */
  public abstract HClass getWrapper();

  /**
   * If this <code>HClass</code> object represents an array type, 
   * returns <code>true</code>, otherwise returns <code>false</code>.
   */
  public abstract boolean isArray();
  /**
   * Determines if the specified <code>HClass</code> object represents an
   * interface type.
   * @return <code>true</code> is this object represents an interface;
   *         <code>false</code> otherwise.
   */
  public abstract boolean isInterface();

  /**
   * Determines if the specified <code>HClass</code> object represents a
   * primitive Java type. <p>
   * There are nine predefined <code>HClass</code> objects to represent
   * the eight primitive Java types and void.
   */
  public abstract boolean isPrimitive();

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
  public abstract boolean isAssignableFrom(HClass cls);

  /**
   * Determines if this <code>HClass</code> is a superclass of a given
   * <code>HClass</code> <code>hc</code>. 
   * [Does not look at interface information.]
   * @return <code>true</code> if <code>this</code> is a superclass of
   *         <code>hc</code>, <code>false</code> otherwise.
   */
  public abstract boolean isSuperclassOf(HClass hc);

  /**
   * Determines if this <code>HClass</code> is a superinterface of a given
   * <code>HClass</code> <code>hc</code>. 
   * [does not look at superclass information]
   * @return <code>true</code> if <code>this</code> is a superinterface of
   *         <code>hc</code>, <code>false</code> otherwise.
   */
  public abstract boolean isSuperinterfaceOf(HClass hc);

  /**
   * Determines if this <code>HClass</code> is an instance of the given
   * <code>HClass</code> <code>hc</code>.
   */
  public abstract boolean isInstanceOf(HClass hc);

  /**
   * Converts the object to a string.  The string representation is the
   * string <code>"class"</code> or <code>"interface"</code> followed by
   * a space and then the fully qualified name of the class.  If this
   * <code>HClass</code> object represents a primitive type,
   * returns the name of the primitive type.
   * @return a string representation of this class object.
   */
  public abstract String toString();

  /**
   * Prints a formatted representation of this class.
   * Output is pseudo-Java source.
   */
  public final void print(java.io.PrintWriter pw) {
    int m;
    // package declaration.
    pw.println("package " + getPackage() + ";");
    // class declaration.
    m = getModifiers() & (~32); // unset the ACC_SUPER flag.
    pw.println(((m==0)?"":(Modifier.toString(m) + " ")) + 
	       (isInterface()?"interface ":"class ") + 
	       getSimpleTypeName(this));
    // superclass
    HClass sup = getSuperclass();
    if ((sup != null) && (!sup.getName().equals("java.lang.Object")))
      pw.println("    extends " + getSimpleTypeName(sup));
    // interfaces
    HClass in[] = getInterfaces();
    if (in.length > 0) {
      if (isInterface())
	pw.print("    extends ");
      else
	pw.print("    implements ");
      for (int i=0; i<in.length; i++) {
	pw.print(getSimpleTypeName(in[i]));
	if (i<in.length-1)
	  pw.print(", ");
      }
      pw.println();
    }
    pw.println("{");
    // declared fields.
    HField hf[] = getDeclaredFields();
    for (int i=0; i<hf.length; i++) {
      m = hf[i].getModifiers();
      pw.println("    " + 
		 ((m==0)?"":(Modifier.toString(m)+" ")) + 
		 getSimpleTypeName(hf[i].getType()) + " " +
		 hf[i].getName() + ";");
    }
    // declared methods.
    HMethod hm[] = getDeclaredMethods();
    for (int i=0; i<hm.length; i++) {
      StringBuffer mstr = new StringBuffer("    ");
      m = hm[i].getModifiers();
      if (m!=0) {
	mstr.append(Modifier.toString(m));
	mstr.append(' ');
      }
      if (hm[i] instanceof HInitializer) {
	mstr.append("static {};");
	pw.println(mstr.toString());
	continue;
      }
      if (hm[i] instanceof HConstructor) {
	mstr.append(getSimpleTypeName(this));
      } else {
	mstr.append(getSimpleTypeName(hm[i].getReturnType()));
	mstr.append(' ');
	mstr.append(hm[i].getName());
      }
      mstr.append('(');
      HClass[] mpt = hm[i].getParameterTypes();
      String[] mpn = hm[i].getParameterNames();
      for (int j=0; j<mpt.length; j++) {
	mstr.append(getSimpleTypeName(mpt[j]));
	mstr.append(' ');
	// use appropriate formal parameter name.
	if (mpn[j]!=null)
	  mstr.append(mpn[j]);
	else { // don't know it; use generic.
	  mstr.append('p'); mstr.append(j);
	}
	if (j<mpt.length-1)
	  mstr.append(", ");
      }
      mstr.append(')');
      HClass[] met = hm[i].getExceptionTypes();
      if (met.length>0)
	mstr.append(" throws ");
      for (int j=0; j<met.length; j++) {
	mstr.append(getSimpleTypeName(met[j]));
	if (j<met.length-1)
	  mstr.append(", ");
      }
      mstr.append(';');
      pw.println(mstr.toString());
    }
    // done.
    pw.println("}");
  }
  private String getSimpleTypeName(HClass cls) {
    String tn = HClass.getTypeName(cls);
    while (cls.isArray()) cls=cls.getComponentType();
    if (cls.getPackage()!=null &&
	(cls.getPackage().equals(getPackage()) ||
	 cls.getPackage().equals("java.lang"))) {
      int lastdot = tn.lastIndexOf('.');
      if (lastdot < 0) return tn;
      else return tn.substring(lastdot+1);
    }
    else return tn;
  }

  /*****************************************************************/
  // Special classes for primitive types.
  // [note that these cannot be re-linked as they are hard-coded here]

  /** The <code>HClass</code> object representing the primitive type boolean.*/
  public static final HClass Boolean=new HClassPrimitive("boolean", "Z");
  /** The <code>HClass</code> object representing the primitive type byte.*/
  public static final HClass Byte=new HClassPrimitive("byte", "B");
  /** The <code>HClass</code> object representing the primitive type short.*/
  public static final HClass Short=new HClassPrimitive("short", "S");
  /** The <code>HClass</code> object representing the primitive type int.*/
  public static final HClass Int=new HClassPrimitive("int", "I");
  /** The <code>HClass</code> object representing the primitive type long.*/
  public static final HClass Long=new HClassPrimitive("long", "J");
  /** The <code>HClass</code> object representing the primitive type float.*/
  public static final HClass Float=new HClassPrimitive("float", "F");
  /** The <code>HClass</code> object representing the primitive type double.*/
  public static final HClass Double=new HClassPrimitive("double", "D");
  /** The <code>HClass</code> object representing the primitive type char.*/
  public static final HClass Char=new HClassPrimitive("char", "C");
  /** The <code>HClass</code> object representing the primitive type void.*/
  public static final HClass Void=new HClassPrimitive("void", "V");

  /** Array factory: returns new <Code>HClass[]</code>. */
  public static final ArrayFactory arrayFactory =
    new ArrayFactory() {
      public Object[] newArray(int len) { return new HClass[len]; }
    };
  /** HPointer interface. */
  final HClass actual() { return this; /* no dereferencing necessary. */ }

  // Comparable interface.
  /** Compares two <code>HClass</code>es by lexicographic order of their
   *  descriptors. */
  public final int compareTo(Object o) {
    return getDescriptor().compareTo(((HClass)o).getDescriptor());
  }

  // UTILITY CLASSES (used in toString methods all over the place)
  static String getTypeName(HPointer hc) {
    HClass hcc;
    try { hcc = (HClass) hc; }
    catch (ClassCastException e) { return hc.getName(); }
    return getTypeName(hcc);
  }
  static String getTypeName(HClass hc) {
    if (hc.isArray()) {
      StringBuffer r = new StringBuffer();
      HClass sup = hc;
      int i=0;
      for (; sup.isArray(); sup = sup.getComponentType())
	i++;
      r.append(sup.getName());
      for (int j=0; j<i; j++)
	r.append("[]");
      return r.toString();
    }
    return hc.getName();
  }
}

// set emacs indentation style.
// Local Variables:
// c-basic-offset:2
// End:
