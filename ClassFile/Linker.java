// Linker.java, created Mon Dec 27 18:54:16 1999 by cananian
// Copyright (C) 1999 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.ClassFile;

import harpoon.Util.Util;

import java.util.HashMap;
import java.util.Map;
/**
 * A <code>Linker</code> object manages the association of symbolic names
 * to code/data/object descriptions.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: Linker.java,v 1.1.2.2 2000-01-11 08:28:17 cananian Exp $
 */
public abstract class Linker {
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
    Util.assert(hc.getLinker()==this);
    Util.assert(!descCache.containsKey(hc.getDescriptor()));
    descCache.put(hc.getDescriptor(), hc);
  }
  
  /**
   * Sub-classes will provide implementation for the 
   * <code>forDescriptor0</code> method in order to implement
   * a linking strategy.  This method is only passed descriptors for
   * class types; never array or primitive type descriptors.
   * (Hence neither primitive types or array types can be
   *  re-linked, which might violate java language semantics.)
   * @exception NoClassDefFoundError
   *            if the class could not be found.
   */
  protected abstract HClass forDescriptor0(String descriptor)
    throws NoClassDefFoundError;

  /**
   * Returns the <code>HClass</code> object associated with the
   * ComponentType descriptor given.  Throws <code>NoClassDefFoundError</code>
   * if the descriptor references a class that cannot be found.  Throws
   * <code>Error</code> if an invalid descriptor is given.
   * @exception Error
   *            if an invalid descriptor is given.
   * @exception NoClassDefFoundError
   *            if the class could not be found.
   */
  public final HClass forDescriptor(String descriptor)
    throws NoClassDefFoundError {
    Util.assert(descriptor.indexOf('.')==-1); // should be desc, not name.
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
      Util.assert(!descCache.containsKey(descriptor));
      Util.assert(descriptor.equals(cls.getDescriptor()));
      descCache.put(descriptor, cls);
    }
    return cls;    
  }
  // helper function for the above; split apart to make the caching
  // behavior of the above clearer.
  private HClass _forDescriptor_(String descriptor) {
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
	return new HClassArray(this, basetype, d);
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
  public final HClass forName(String className) throws NoClassDefFoundError {
    if (className.charAt(0)=='[') {
      Util.assert(className.indexOf('.')==-1); // should be desc, not name.
      return forDescriptor(className);
    } else {
      Util.assert(className.indexOf('/')==-1); // should be name, not desc.
      return forDescriptor("L"+className.replace('.','/')+";");
    }
  }

  /** 
   * Returns the <code>HClass</code> object associated with the given java 
   * <code>Class</code> object.  If (for some reason) the class file
   * cannot be found, the method throws a <code>NoClassDefFoundError</code>.
   * @return the <code>HClass</code> descriptor for this <code>Class</code>.
   * @exception NoClassDefFoundError
   *            if the classfile could not be found.
   * @deprecated Don't use java.lang.Class objects if you can help it.
   */
  public final HClass forClass(Class cls) throws NoClassDefFoundError {
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
      throw new Error("Unknown class primitive.");
    }
    // otherwise...
    return forName(cls.getName());
  }
}

// set emacs indentation style.
// Local Variables:
// c-basic-offset:2
// End:
