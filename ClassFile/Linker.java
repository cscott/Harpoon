// Linker.java, created Mon Dec 27 18:54:16 1999 by cananian
// Copyright (C) 1999 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.ClassFile;

import net.cscott.jutil.ReferenceUnique;
import harpoon.Util.Util;

import java.util.HashMap;
import java.util.Map;
/**
 * A <code>Linker</code> object manages the association of symbolic names
 * to code/data/object descriptions.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: Linker.java,v 1.7 2004-02-08 01:58:03 cananian Exp $
 */
public abstract class Linker implements ReferenceUnique {
  protected Linker() { }
  /** private linker cache, for efficiency. */
  protected final Map descCache = new HashMap();
  /**
   * Synthetic classes will use this hook to register themselves in
   * the namespace of this <code>Linker</code>. <b>The given class
   * <code>hc</code> must be unique in the namespace of this linker.
   * Not to be used if the <code>Linker</code> already contains a class
   * with the same descriptor.</b>   Use <code>Relinker</code> to do
   * global replacement of classes.
   * @see Relinker.relink
   * @see uniqueName
   */
  void register(HClass hc) {
    assert hc.getLinker()==this;
    assert !descCache.containsKey(hc.getDescriptor());
    descCache.put(hc.getDescriptor(), hc);
  }
  
  /**
   * Sub-classes will provide implementation for the 
   * <code>forDescriptor0</code> method in order to implement
   * a linking strategy.  This method is only passed descriptors for
   * class types; never array or primitive type descriptors.
   * (Hence neither primitive types or array types can be
   *  re-linked, which might violate java language semantics.)
   * @exception NoSuchClassException
   *            if the class could not be found.
   */
  protected abstract HClass forDescriptor0(String descriptor)
    throws NoSuchClassException;

  /**
   * Returns the <code>HClass</code> object associated with the
   * ComponentType descriptor given.  Throws <code>NoSuchClassException</code>
   * if the descriptor references a class that cannot be found.  Throws
   * <code>Error</code> if an invalid descriptor is given.
   * @exception Error
   *            if an invalid descriptor is given.
   * @exception NoSuchClassException
   *            if the class could not be found.
   */
  public HClass forDescriptor(String descriptor)
    throws NoSuchClassException {
    assert descriptor.indexOf('.')==-1; // should be desc, not name.
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
    // Check the cache and ensure uniqueness.
    HClass cls = (HClass) descCache.get(descriptor);
    if (cls==null) {    // not in the cache.
      cls = _forDescriptor_(descriptor); // do actual descriptor resolution.
      assert !descCache.containsKey(descriptor);
      assert descriptor.equals(cls.getDescriptor()) : "The given class name does not match the class' idea of its name\n"
                  + "Try giving the full name to -c"
                  + "\nDescriptor " + descriptor + "\ncls " + cls
                  + "\ncls descriptor " + cls.getDescriptor();
      descCache.put(descriptor, cls);
    }
    return cls;    
  }
  // helper function for the above; split apart to make the caching
  // behavior of the above clearer.
  private HClass _forDescriptor_(String descriptor)
    throws NoSuchClassException {
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
	HClass arraytype = makeArray(basetype, d);
	assert arraytype.getDescriptor().equals(descriptor);
	return arraytype;
      }
    case 'L': // object type.
      return forDescriptor0(descriptor);
    case 'B': // primitive types.
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
    default:
      break;
    }
    throw new Error("Bad Descriptor: "+descriptor);
  }
  /** Allow Linker subclass to substitute a different (mutable?)
   *  array class type. */
  protected HClass makeArray(HClass baseType, int dims) {
    return new HClassArray(this, baseType, dims);
  }

 /** 
   * Returns the <code>HClass</code> object associated with the class with
   * the given string name.  Given the fully-qualified name for a class or
   * interface, this method attempts to locate and load the class.  If it
   * succeeds, returns the <code>HClass</code> object representing the class.
   * If it fails, the method throws a <code>NoSuchClassException</code>.
   * @param className the fully qualified name of the desired class.
   * @return the <code>HClass</code> descriptor for the class with the
   *         specified name.
   * @exception NoSuchClassException
   *            if the class could not be found.
   */
  public final HClass forName(String className) throws NoSuchClassException {
    if (className.charAt(0)=='[') {
      assert className.indexOf('.')==-1 : "Class name " + className; // should be desc, not name.
      return forDescriptor(className);
    } else {
      assert className.indexOf('/')==-1:className; // should be name, not desc.
      return forDescriptor("L"+className.replace('.','/')+";");
    }
  }

  /** 
   * Returns the <code>HClass</code> object associated with the given java 
   * <code>Class</code> object.  If (for some reason) the class file
   * cannot be found, the method throws a <code>NoSuchClassException</code>.
   * @return the <code>HClass</code> descriptor for this <code>Class</code>.
   * @exception NoSuchClassException
   *            if the classfile could not be found.
   * @deprecated Don't use java.lang.Class objects if you can help it.
   */
  public final HClass forClass(Class cls) throws NoSuchClassException {
    // if cls is an array...
    if (cls.isArray())
      return forDescriptor("[" +
			   forClass(cls.getComponentType()).getDescriptor());
    // or else if it's a primitive type...
    if (cls.isPrimitive()) {
      if (cls == java.lang.Boolean.TYPE) return forDescriptor("Z");
      if (cls == java.lang.Character.TYPE) return forDescriptor("C");
      if (cls == java.lang.Byte.TYPE) return forDescriptor("B");
      if (cls == java.lang.Short.TYPE) return forDescriptor("S");
      if (cls == java.lang.Integer.TYPE) return forDescriptor("I");
      if (cls == java.lang.Long.TYPE) return forDescriptor("J");
      if (cls == java.lang.Float.TYPE) return forDescriptor("F");
      if (cls == java.lang.Double.TYPE) return forDescriptor("D");
      if (cls == java.lang.Void.TYPE) return forDescriptor("V");
      throw new NoSuchClassException("Unknown class primitive: "+cls);
    }
    // otherwise...
    return forName(cls.getName());
  }

  /** Creates a new mutable class with the given name which is
   *  based on the given template class.  <b>The <code>name</code>
   *  must be unique.</b>
   * @exception DuplicateClassException if the given name is not unique;
   *    that is, it corresponds to a loadable class.
   */
  public HClass createMutableClass(String name, HClass template)
    throws DuplicateClassException {
    assert template.getLinker()==this;
    try {
      forName(name);
      throw new DuplicateClassException(name);
    } catch (NoSuchClassException e) { /* named class not found, continue */ }
    // okay, create a mutable class...
    HClass hc = new HClassSyn(this, name, template);
    hc.hasBeenModified = true;
    register(hc);
    return hc;
  }
}

// set emacs indentation style.
// Local Variables:
// c-basic-offset:2
// End:
