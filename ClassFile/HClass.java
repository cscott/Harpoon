package harpoon.ClassFile;

import java.io.InputStream;
import java.util.Hashtable;

public class HClass {
  static Hashtable str2cls = new Hashtable();
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
    HClass cls = (HClass) str2cls.get(className);
    if (cls!=null) return cls;
    // nope; first time request.
    InputStream is = 
      Loader.getResourceAsStream(Loader.classToResource(className));
    if (is == null) throw new NoClassDefFoundError(className);
    // OK, go ahead and load this.
    harpoon.ClassFile.Raw.ClassFile raw =
      new harpoon.ClassFile.Raw.ClassFile(is);
    // Make a HClass with the raw classfile.
    cls = new HClass(raw);
    // Add to hashtables.
    str2cls.put(className, cls);
    raw2cls.put(raw, cls);
    // return result.
    return cls;
  }

  /** The underlying raw class file for this <code>HClass</code> object. */
  harpoon.ClassFile.Raw.ClassFile classfile;

  /** Create an <code>HClass</code> from a raw classfile. */
  protected HClass(harpoon.ClassFile.Raw.ClassFile classfile) {
    this.classfile = classfile;
  }

  /** 
   * Returns the fully-qualified name of the type (class, interface,
   * array, or primitive) represented by this <code>HClass</code> object,
   * as a <code>String</code>. 
   * @return the fully qualified name of the class or interface
   *         represented by this object.
   */
  public String getName() {
    return classfile.this_class().name().replace('/','.');
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
    if (classfile.super_class == 0) return null;
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
    HClass in[] = new HClass[classfile.interfaces_count()];
    for (int i=0; i< in.length; i++)
      in[i] = forName(classfile.interfaces(i).name().replace('/','.'));
    return in;
  }
}
