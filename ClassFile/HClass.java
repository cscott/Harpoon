package harpoon.ClassFile;

import java.io.InputStream;
import java.util.Hashtable;

public class HClass {
  static Hashtable dsc2cls = new Hashtable();
  static Hashtable raw2cls = new Hashtable();
  /** 
   * Returns the <code>HClass</code> object associated with the class with
   * the given string name.  Given the fully-qualified name for a class or
   * interface, this method attempts to locate and load the class.  If it
   * succeeds, returns the <code>HClass</code> object representing the class.
   * If it fails, the method throws a <code>NoClassDefFoundError</code>.
   * @param className the fully qualified name of the desired class.
   * @return the <code>HClass</code> descriptor for the class with the
   *         specified name.
   * @exception NoClassDefFoundError
   *            if the class could not be found.
   */
  public static HClass forName(String className) {
    if (className.charAt(0)=='[')
      return forDescriptor(className);
    else
      return forDescriptor("L"+className+";");
  }
  
  /**
   * Returns the <code>HClass</code> object associated with the
   * ComponentType descriptor given.  Throws <code>NoClassDefFoundError</code>
   * if the descriptor references a class that cannot be found.  Throws
   * <code>Error</code> if an invalid descriptor is given.
   */
  public static HClass forDescriptor(String descriptor) {
    // First check the cache.
    HClass cls = (HClass) dsc2cls.get(descriptor);
    if (cls!=null) return cls;
    // not in the cache.
    switch(descriptor.charAt(0)) {
    case '[': // arrays.
      {
	// count dimensions
	int d;
	for (d=0; d<descriptor.length(); d++)
	  if (descriptor.charAt(d)!='[') 
	    break;
	// recurse to fetch base type.
	HClass basetype = forDescriptor(descriptor.substring(d));
	// make it.
	return new HClass(basetype.classfile, d);
      }
    case 'B':
      return HClass.Byte;
    case 'C':
      return HClass.Char;
    case 'D':
      return HClass.Double;
    case 'F':
      return HClass.Float;
    case 'I':
      return HClass.Int;
    case 'J':
      return HClass.Long;
    case 'S':
      return HClass.Short;
    case 'Z':
      return HClass.Boolean;
    case 'V':
      return HClass.Void;
    case 'L': // object type.
      {
	String className = descriptor.substring(1, descriptor.indexOf(';'));
	InputStream is = 
	  Loader.getResourceAsStream(Loader.classToResource(className));
	if (is == null) throw new NoClassDefFoundError(className);
	// OK, go ahead and load this.
	harpoon.ClassFile.Raw.ClassFile raw =
	  new harpoon.ClassFile.Raw.ClassFile(is);
	// Make a HClass with the raw classfile.
	return new HClass(raw);
      }
    default:
      break;
    }
    throw new Error("Bad Descriptor: "+descriptor);
  }
  /** 
   * Returns the <code>HClass</code> object associated with the given java 
   * <code>Class</code> object.  If (for some reason) the class file
   * cannot be found, the method throws a <code>NoClassDefFoundError</code>.
   * @return the <code>HClass</code> descriptor for this <code>Class</code>.
   * @exception NoClassDefFoundError
   *            if the classfile could not be found.
   */
  public static HClass forClass(Class cls) {
    return forName(cls.getName());
  }

  /** The underlying raw class file for this <code>HClass</code> object. */
  harpoon.ClassFile.Raw.ClassFile classfile;
  /** The number of array dimensions, or 0 if this is not an array. */
  int dims=0;

  /** Create an <code>HClass</code> from a raw classfile. */
  protected HClass(harpoon.ClassFile.Raw.ClassFile classfile) {
    this.classfile = classfile;
    this.dims = 0;
    // Add to hashtables.
    dsc2cls.put(getDescriptor(), this);
    raw2cls.put(classfile, this);
  }
  /** Create an <code>HClass</code> representing an array from a 
   *  raw classfile representing its component type. */
  protected HClass(harpoon.ClassFile.Raw.ClassFile classfile, int dims) {
    this.classfile = classfile;
    this.dims = dims;
    // Add to hashtables.
    dsc2cls.put(getDescriptor(), this);
  }

  /**
   * If this class represents an array type, returns the <code>HClass</code>
   * object representing the component type of the array; otherwise returns
   * null.
   * @see java.lang.reflect.Array
   */
  public HClass getComponentType() {
    if (!isArray()) return null;
    return forDescriptor(getDescriptor().replace('[',' ').trim());
  }

  /** 
   * Returns the fully-qualified name of the type (class, interface,
   * array, or primitive) represented by this <code>HClass</code> object,
   * as a <code>String</code>. 
   * @return the fully qualified name of the class or interface
   *         represented by this object.
   */
  public String getName() {
    // handle arrays.
    if (dims > 0) return getDescriptor(); // this is how sun's implem. works.
    // handle primitive types.
    if (this==this.Boolean) return "boolean";
    if (this==this.Byte) return "byte";
    if (this==this.Short) return "short";
    if (this==this.Int) return "int";
    if (this==this.Long) return "long";
    if (this==this.Float) return "float";
    if (this==this.Double) return "double";
    if (this==this.Char) return "char";
    if (this==this.Void) return "void";
    // all others.
    return classfile.this_class().name().replace('/','.');
  }
  /**
   * Returns a ComponentType descriptor for the type represented by this
   * <code>HClass</code> object.
   */
  public String getDescriptor() {
    StringBuffer result = new StringBuffer();
    for (int i=0; i<dims; i++)
      result.append('[');
    if ((HClass)raw2cls.get(this.classfile) == this.Boolean)
      result.append('Z');
    else if ((HClass)raw2cls.get(this.classfile) == this.Byte)
      result.append('B');
    else if ((HClass)raw2cls.get(this.classfile) == this.Short)
      result.append('S');
    else if ((HClass)raw2cls.get(this.classfile) == this.Int)
      result.append('I');
    else if ((HClass)raw2cls.get(this.classfile) == this.Long)
      result.append('J');
    else if ((HClass)raw2cls.get(this.classfile) == this.Float)
      result.append('F');
    else if ((HClass)raw2cls.get(this.classfile) == this.Double)
      result.append('D');
    else if ((HClass)raw2cls.get(this.classfile) == this.Char)
      result.append('C');
    else if ((HClass)raw2cls.get(this.classfile) == this.Void)
      result.append('V');
    else { // it's an object, not a primitive type
      result.append('L');
      result.append(classfile.this_class().name().replace('/','.'));
      result.append(';');
    }
    return result.toString();
  }
  
  /**
   * Returns the Java language modifiers for this class or interface,
   * encoded in an integer.  The modifiers consist of the Java Virtual
   * Machine's constants for public, protected, private, final, and 
   * interface; they should be decoded using the methods of class Modifier.
   * @see "The Java Virtual Machine Specification, table 4.1"
   * @see java.lang.reflect.Modifier
   */
  public int getModifiers() {
    return classfile.access_flags.access_flags;
  }

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
    if (isPrimitive() || isInterface() || classfile.super_class == 0) 
      return null;
    if (isArray()) 
      return forName("java.lang.Object");
    return forName(classfile.super_class().name().replace('/','.'));
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
  public HClass[] getInterfaces() {
    if (isPrimitive() || isArray()) return new HClass[0];
    HClass in[] = new HClass[classfile.interfaces_count()];
    for (int i=0; i< in.length; i++)
      in[i] = forName(classfile.interfaces(i).name().replace('/','.'));
    return in;
  }

  /** 
   * If this <code>HClass</code> object represents an array type, 
   * returns <code>true</code>, otherwise returns <code>false</code>.
   */
  public boolean isArray() { return (dims > 0); }

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
   */ // XXX CHECK THIS XXX
  public boolean isAssignableFrom(HClass cls) {
    if (cls==null) throw new NullPointerException();
    // test identity conversion.
    if (isPrimitive()) return (this==cls);
    // widening reference conversions from an array:
    if (cls.isArray()) {
      if (this == forName("java.lang.Object")) return true;
      if (this == forName("java.lang.Cloneable")) return true;
      if (isArray() && 
	  getComponentType().isAssignableFrom(cls.getComponentType()))
	return true;
      return false;
    }
    if (isArray()) return false; // because cls is not an array.

    // CHEATING!
    try {
      return Class.forName(this.getName()).isAssignableFrom(Class.forName(cls.getName()));
    } catch (ClassNotFoundException e) {
      throw new NoClassDefFoundError(e.toString());
    }
    /*
    // from any class type S to any class type T, 
    // provided that S is a subclass of T.
    for (HClass sup = cls; sup!=null; sup = sup.getSuperclass())
      if (this==sup) return true;
    // from any class type S to any interface type K,
    // provided that S implements K.
    // from any interface J to any interface K, 
    // provided J is a subinterface of K.  XXX DO WE REALLY DO THIS?
    HClass ins[] = cls.getInterfaces();
    for (int i=0; i<ins.length; i++)
      if (this==ins[i]) return true;
    return false;
    */
  }

  /** 
   * This method is the dynamic equivalent of the Java language
   * <code>instanceof</code> operator.  The method returns 
   * <code>true</code> if the specified <code>Object</code> argument
   * is non-null and can be cast to the reference type represented by
   * this <code>HClass</code> object without raising a
   * <code>ClassCastException</code>.  It returns <code>false</code> 
   * otherwise. <p>
   * Specifically, if this <code>HClass</code> object represents a declared
   * class, return <code>true</code> is the specified <code>Object</code>
   * argument is an instance of the represented class (or any of its
   * subclasses); <code>false</code> otherwise.  If this <code>HClass</code>
   * object represents an array class, returns <code>true</code> if the
   * specified <code>Object</code> argument can be converted to an object
   * of the array type by an identity conversion or by a widening reference
   * conversion, <code>false</code> otherwise.  If this <code>HClass</code>
   * object represents an interface, returns <code>true</code> if the
   * class or any superclass of the specified <code>Object</code> argument
   * implements this interface, <code>false</code> otherwise.  If this
   * <code>HClass</code> object represents a primitive type, returns
   * <code>false</code>.
   * @param obj The object to check.
   */
  public boolean isInstance(Object obj) {
    if (obj==null) return false;
    if (isPrimitive()) return false;
    // CHEATING!! XXX
    try {
      return Class.forName(this.getName()).isInstance(obj);
    } catch (ClassNotFoundException e) {
      throw new NoClassDefFoundError(e.toString());
    }
    // HClass objcls = forClass(obj.getClass());
    // if (isInterface()) // XXX FIXME
  }

  /**
   * Determines if the specified <code>HClass</code> object represents an
   * interface type.
   * @return <code>true</code> is this object represents an interface;
   *         <code>false</code> otherwise.
   */
  public boolean isInterface() {
    return !isPrimitive() && classfile.access_flags.isInterface();
  }

  /**
   * Determines if the specified <code>HClass</code> object represents a
   * primitive Java type. <p>
   * There are nine predefined <code>HClass</code> objects to represent
   * the eight primitive Java types and void.
   */
  public boolean isPrimitive() {
    if (isArray()) return false;
    if (this==HClass.Boolean) return true;
    if (this==HClass.Byte) return true;
    if (this==HClass.Short) return true;
    if (this==HClass.Int) return true;
    if (this==HClass.Long) return true;
    if (this==HClass.Float) return true;
    if (this==HClass.Double) return true;
    if (this==HClass.Char) return true;
    if (this==HClass.Void) return true;
    return false;
  }

  /**
   * Converts the object to a string.  The string representation is the
   * string <code>"class"</code> or <code>"interface"</code> followed by
   * a space and then the fully qualified name of the class.  If this
   * <code>HClass</code> object represents a primitive type,
   * returns the name of the primitive type.
   * @return a string representation of this class object.
   */
  public String toString() {
    if (isPrimitive()) return getName();
    if (isInterface()) return "interface "+getName();
    return "class "+getName();
  }

  /*****************************************************************/
  // Special classes for primitive types.
  // ABUSE the java 1.1 extension and create
  // NEW anonymous classes specially for each of these,
  // which shouldn't be accessible any other way.  Hee-hee! I love it!

  /** The <code>HClass</code> object representing the primitive type boolean.*/
  public static final HClass Boolean=forClass((new Object() { }).getClass());
  /** The <code>HClass</code> object representing the primitive type byte.*/
  public static final HClass Byte=forClass((new Object() { }).getClass());
  /** The <code>HClass</code> object representing the primitive type short.*/
  public static final HClass Short=forClass((new Object() { }).getClass());
  /** The <code>HClass</code> object representing the primitive type int.*/
  public static final HClass Int=forClass((new Object() { }).getClass());
  /** The <code>HClass</code> object representing the primitive type long.*/
  public static final HClass Long=forClass((new Object() { }).getClass());
  /** The <code>HClass</code> object representing the primitive type float.*/
  public static final HClass Float=forClass((new Object() { }).getClass());
  /** The <code>HClass</code> object representing the primitive type double.*/
  public static final HClass Double=forClass((new Object() { }).getClass());
  /** The <code>HClass</code> object representing the primitive type char.*/
  public static final HClass Char=forClass((new Object() { }).getClass());
  /** The <code>HClass</code> object representing the primitive type void.*/
  public static final HClass Void=forClass((new Object() { }).getClass());
  // Dig *that*, Ken Arnold!
}
