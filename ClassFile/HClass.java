package harpoon.ClassFile;

import java.lang.reflect.Modifier;
import java.io.InputStream;
import java.util.Hashtable;
import java.util.Vector;

import harpoon.ClassFile.Raw.Attribute.AttributeSourceFile;
import harpoon.Util.UniqueVector;

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
 * @version $Id: HClass.java,v 1.16 1998-08-02 09:39:12 cananian Exp $
 * @see harpoon.ClassFile.Raw.ClassFile
 */
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
      return forDescriptor("L"+className.replace('.','/')+";");
  }
  
  /**
   * Returns the <code>HClass</code> object associated with the
   * ComponentType descriptor given.  Throws <code>NoClassDefFoundError</code>
   * if the descriptor references a class that cannot be found.  Throws
   * <code>Error</code> if an invalid descriptor is given.
   */
  public static HClass forDescriptor(String descriptor) {
    // Trim descriptor.
    int i;
    for (i=0; i<descriptor.length(); i++) {
      char c = descriptor.charAt(i);
      if (c=='(' || c==')') throw new Error("Bad Descriptor: "+descriptor);
      if (c=='[') continue;
      if (c=='L') i = descriptor.indexOf(';', i);
      break;
    }
    descriptor = descriptor.substring(0, i+1);
    // Check the cache.
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
	// classname in descriptor should be '/' delimited.
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
    return forDescriptor(getDescriptor().substring(1));
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
   * Returns the package name of this <code>HClass</code>.  If this
   * <code>HClass</code> represents a primitive or array type,
   * then returns null.  Returns <code>""</code> (a zero-length string)
   * if this class is not in a package.
   */
  public String getPackage() {
    if (isPrimitive() || isArray()) return null;
    String fullname = classfile.this_class().name().replace('/','.');
    int lastdot = fullname.lastIndexOf('.');
    if (lastdot<0) return ""; // no package.
    else return fullname.substring(0, lastdot);
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
      result.append(classfile.this_class().name().replace('.','/'));
      result.append(';');
    }
    return result.toString();
  }
  
  /**
   * Returns a <code>HField</code> object that reflects the specified
   * declared field of the class or interface represented by this
   * <code>HClass</code> object.  The <code>name</code> parameter is a 
   * <code>String</code> that specifies the simple name of the
   * desired field.
   * @exception NoSuchFieldException
   *            if a field with the specified name is not found.
   * @see HField
   */
  public HField getDeclaredField(String name)
    throws NoSuchFieldException {
    // construct master declaredField list, if we haven't already.
    if (declaredFields==null) getDeclaredFields();
    // look for field name in master list.
    for (int i=0; i<declaredFields.length; i++)
      if (declaredFields[i].getName().equals(name))
	return declaredFields[i];
    // not found.
    throw new NoSuchFieldException(name);
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
   * @see "The Java Language Specification,, sections 8.2 and 8.3"
   * @see HField
   */
  public HField[] getDeclaredFields() {
    if (declaredFields==null) {
      if (isPrimitive() || isArray()) 
	declaredFields = new HField[0];
      else {
	declaredFields = new HField[classfile.fields.length];
	for (int i=0; i<declaredFields.length; i++)
	  declaredFields[i] = new HField(this, classfile.fields[i]);
      }
    }
    return HField.copy(declaredFields);
  }
  private HField[] declaredFields = null;
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
   * @exception NoSuchFieldException
   *            if a field with the specified name is not found.
   * @see HField
   */
  public HField getField(String name) throws NoSuchFieldException {
    // construct master field list, if we haven't already.
    if (fields==null) getFields();
    // look for field name in master field list.
    // look backwards to be sure we find local fields first (scoping)
    for (int i=fields.length-1; i>=0; i--)
      if (fields[i].getName().equals(name))
	return fields[i];
    // can't find it.
    throw new NoSuchFieldException(name);
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
    // Cache fields value so we only have to compute this once.
    if (fields==null) {
      if (isPrimitive() || isArray()) {
	fields = new HField[0];
      } else {
	fields = getFields(this); 
      }
    }
    return HField.copy(fields);
  }
  private HField[] fields=null;
  /* does the actual work.  Because of permissions issues, it's important
   * to know which class is asking for the fields listing.
   */
  protected HField[] getFields(HClass frmClass) {
    String frmPackage = frmClass.getPackage();
    UniqueVector v = new UniqueVector();
    // add fields from interfaces.
    HClass[] in = getInterfaces();
    for (int i=0; i<in.length; i++) {
      HField[] inf = in[i].getFields(frmClass);
      for (int j=0; j<inf.length; j++)
	v.addElement(inf[j]);
    }
    // now fields from superclasses, subject to access mode constraints.
    HClass sup = getSuperclass();
    HField supf[] = (sup==null)?new HField[0]:sup.getFields(frmClass);
    for (int i=0; i<supf.length; i++) {
      int m = supf[i].getModifiers();
      // private fields of superclasses are invisible.
      if (Modifier.isPrivate(m))
	continue; // skip this field.
      // default access is invisible if packages not identical.
      if (!Modifier.isPublic(m) && !Modifier.isProtected(m))
	if (!supf[i].getDeclaringClass().getPackage().equals(frmPackage))
	  continue;
      // all's good. Add this one.
      v.addElement(supf[i]);
    }
    // now fields from our local class.
    HField locf[] = getDeclaredFields();
    for (int i=0; i<locf.length; i++)
      v.addElement(locf[i]);
    
    // Merge into one array.
    HField[] result = new HField[v.size()];
    v.copyInto(result);
    return result;
  }

  /**
   * Returns a <code>HMethod</code> object that reflects the specified 
   * declared method of the class or interface represented by this 
   * <code>HClass</code> object.  The <code>name</code> parameter is a
   * <code>String</code> that specifies the simple name of the desired
   * method, and the <code>parameterTypes</code> parameter is an array
   * of <code>HClass</code> objects that identify the method's formal
   * parameter types, in declared order.
   * @exception NoSuchMethodException
   *            if a matching method is not found.
   * @see HMethod
   */
  public HMethod getDeclaredMethod(String name, HClass parameterTypes[])
    throws NoSuchMethodException {
    // construct master declaredMethod list, if we haven't already.
    if (declaredMethods==null) getDeclaredMethods();
    // look for method name/type in master list.
    for (int i=0; i<declaredMethods.length; i++)
      if (declaredMethods[i].getName().equals(name)) {
	HClass[] methodParamTypes = declaredMethods[i].getParameterTypes();
	if (methodParamTypes.length == parameterTypes.length) {
	  int j; for (j=0; j<parameterTypes.length; j++)
	    if (methodParamTypes[i] != parameterTypes[i])
	      break; // oops, this one doesn't match.
	  if (j==parameterTypes.length) // hey, we made it to the end!
	    return declaredMethods[i];
	}
      }
    // didn't find a match.  Oh, well.
    throw new NoSuchMethodException(name);
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
    if (declaredMethods==null) {
      if (isPrimitive() || isArray())
	declaredMethods = new HMethod[0];
      else {
	declaredMethods = new HMethod[classfile.methods.length];
	for (int i=0; i<declaredMethods.length; i++) {
	  if (classfile.methods[i].name().equals("<init>")) // constructor
	    declaredMethods[i] = new HConstructor(this, classfile.methods[i]);
	  else // plain ol' method.
	    declaredMethods[i] = new HMethod(this, classfile.methods[i]);
	}
      }
    }
    return HMethod.copy(declaredMethods);
  }
  private HMethod[] declaredMethods = null;
  /**
   * Returns a <code>HMethod</code> object that reflects the specified
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
   * @exception NoSuchMethodException if a matching method is not found.
   */
  public HMethod getMethod(String name, HClass parameterTypes[])
    throws NoSuchMethodException {
    // construct master method list, if we haven't already.
    if (methods==null) getMethods();
    // look for method name is master method list.
    // look backwards to be sure we find local methods first (scoping).
    for (int i=methods.length-1; i>=0; i--)
      if (methods[i].getName().equals(name)) {
	HClass[] methodParamTypes = methods[i].getParameterTypes();
	if (methodParamTypes.length == parameterTypes.length) {
	  int j; for (j=0; j<parameterTypes.length; j++)
	    if (methodParamTypes[i] != parameterTypes[i])
	      break; // oops, this one doesn't match.
	  if (j==parameterTypes.length) // hey, we made it to the end!
	    return methods[i];
	}
      }
    // didn't find a match. Oh, well.
    throw new NoSuchMethodException(name);
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
    // cache methods value so we only have to compute this once.
    if (methods==null) {
      if (isPrimitive() || isArray()) {
	methods = new HMethod[0];
      } else {
	methods = getMethods(this);
      }
    }
    return HMethod.copy(methods);
  }
  private HMethod[] methods=null;
  /* does the actual work.  Because of permissions issues, it's important
   * to know which class is asking for the methods listing.
   */
  protected HMethod[] getMethods(HClass frmClass) {
    String frmPackage = frmClass.getPackage();
    UniqueVector v = new UniqueVector();
    // can ignore methods from interfaces (they'll be declared methods)
    //  (this is correct from experiment.  The compiler adds
    //   abstract methods from the interface to the classfile if they're
    //   not explicitly present in the source.)
    // grab fields from superclasses, subject to access mode constraints.
    HClass sup = getSuperclass();
    HMethod supm[] = (sup==null)?new HMethod[0]:sup.getMethods(frmClass);
    for (int i=0; i<supm.length; i++) {
      int m = supm[i].getModifiers();
      // private methods of superclasses are invisible.
      if (Modifier.isPrivate(m))
	continue; // skip this method.
      // default access is invisible if packages not identical
      if (!Modifier.isPublic(m) && !Modifier.isProtected(m))
	if (!supm[i].getDeclaringClass().getPackage().equals(frmPackage))
	  continue; // skip this (inaccessible) method.
      // all's good.  Add this one.
      v.addElement(supm[i]);
    }
    // now methods we declare locally.
    HMethod[] locm = getDeclaredMethods();
    for (int i=0; i<locm.length; i++)
      v.addElement(locm[i]);

    // Merge into a single array.
    HMethod[] result = new HMethod[v.size()];
    v.copyInto(result);
    return result;
  }
  /**
   * Returns an <code>HConstructor</code> object that reflects the 
   * specified declared constructor of the class or interface represented 
   * by this <code>HClass</code> object.  The <code>parameterTypes</code>
   * parameter is an array of <code>HClass</code> objects that
   * identify the constructor's formal parameter types, in declared order.
   * @exception NoSuchMethodException if a matching method is not found.
   */
  public HConstructor getConstructor(HClass parameterTypes[])
    throws NoSuchMethodException {
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
    if (constructors == null) {
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
    }
    return HConstructor.copy(constructors);
  }
  private HConstructor[] constructors = null;

  /**
   * Returns the class initializer method, if there is one; otherwise
   * <code>null</code>.
   * @see "The Java Virtual Machine Specification, section 3.8"
   */
  public HMethod getClassInitializer() {
    try {
      return getDeclaredMethod("<clinit>", new HClass[0]);
    } catch (NoSuchMethodException e) {
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
   * Return the name of the source file for this class, or a
   * zero-length string if the information is not available.
   * @see harpoon.ClassFile.Raw.Attribute.AttributeSourceFile
   */
  public String getSourceFile() {
    if (sourcefile==null) {
      for (int i=0; i<classfile.attributes.length; i++)
	if (classfile.attributes[i] instanceof AttributeSourceFile) {
	  sourcefile = 
	    ((AttributeSourceFile)classfile.attributes[i]).sourcefile();
	  break;
	}
      if (sourcefile==null) sourcefile="";
    }
    return sourcefile;
  }
  private String sourcefile=null;

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

  /**
   * Pretty-prints that raw classfile information.
   */
  public void printRaw(java.io.PrintWriter pw) {
    classfile.print(pw);
  }
  /**
   * Prints a formatted representation of this class.
   * Output is pseudo-Java source.
   */
  public void print(java.io.PrintWriter pw) {
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
    if ((sup != null) && (sup != forName("java.lang.Object")))
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
      if (hm[i].getName().equals("<clinit>")) continue;
      StringBuffer mstr = new StringBuffer("    ");
      m = hm[i].getModifiers();
      if (m!=0) {
	mstr.append(Modifier.toString(m));
	mstr.append(' ');
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
    String tn = HField.getTypeName(cls);
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

  static HClass[] copy(HClass[] src) {
    if (src.length==0) return src;
    HClass[] dst = new HClass[src.length];
    System.arraycopy(src,0,dst,0,src.length);
    return dst;
  }
}
