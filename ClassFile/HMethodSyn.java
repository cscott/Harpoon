// HMethodSyn.java, created Fri Oct 16  2:21:03 1998 by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.ClassFile;

import harpoon.Util.Util;

import java.lang.reflect.Modifier;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;
/**
 * An <code>HMethodSyn</code> provides information about, and access to, a 
 * single method on a class or interface.  The reflected method
 * may be a class method or an instance method (including an abstract
 * method).
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: HMethodSyn.java,v 1.8.2.1 2002-02-27 08:35:44 cananian Exp $
 * @see HMember
 * @see HClass
 */
class HMethodSyn extends HMethodImpl implements HMethodMutator {

  /** Create a new method like the <code>template</code>, but named
   *  <code>name</code>.
   *  The new method will be added to class <code>parent</code>. */
  HMethodSyn(HClassSyn parent, String name, HMethod template) {
    this ((HClass)parent, name, template);
  }
  HMethodSyn(HClassArraySyn parent, String name, HMethod template) {
    this ((HClass)parent, name, template);
  }
  // private, not-as-type-safe implementation.
  private HMethodSyn(HClass parent, String name, HMethod template) {
    this.parent = parent;
    this.name = name;
    this.modifiers = template.getModifiers();
    this.returnType = template.getReturnType();
    this.parameterTypes = template.getParameterTypes();
    this.parameterNames = template.getParameterNames();
    this.exceptionTypes = template.getExceptionTypes();
    this.isSynthetic = template.isSynthetic();
    // ensure linker information is consistent.
    assert checkLinker((HClass)this.returnType);
    for(int i=0; i<this.parameterTypes.length; i++)
      assert checkLinker((HClass)this.parameterTypes[i]);
    for(int i=0; i<this.exceptionTypes.length; i++)
      assert checkLinker((HClass)this.exceptionTypes[i]);
  }

  /** Create a new empty method in the specified class
   *  with the specified parameter and return types
   *  that throws no checked exceptions.
   */
  HMethodSyn(HClassSyn parent, String name, 
		    HClass[] paramTypes, HClass returnType) {
    this(parent, name, makeDescriptor (paramTypes, returnType));
  }
  HMethodSyn(HClassArraySyn parent, String name, 
		    HClass[] paramTypes, HClass returnType) {
    this(parent, name, makeDescriptor (paramTypes, returnType));
  }
  
  /** Create a new empty method in the specified class
   *  with the specified descriptor
   *  that throws no checked exceptions.
   */
  HMethodSyn(HClassSyn parent, String name, String descriptor) {
    this((HClass)parent, name, descriptor);
  }
  HMethodSyn(HClassArraySyn parent, String name, String descriptor) {
    this((HClass)parent, name, descriptor);
  }
  // private, not-as-type-safe implementation.
  private HMethodSyn(HClass parent, String name, String descriptor) {
    this.parent = parent;
    this.name = name;
    this.modifiers = 0;
    { // parse descriptor for return type.
      String desc = descriptor.substring(descriptor.lastIndexOf(')')+1);
      this.returnType = new ClassPointer(parent.getLinker(), desc);
    }
    { // parse descriptor for parameter types.
      String desc = descriptor.substring(1, descriptor.lastIndexOf(')'));
      Vector v = new Vector();
      for (int i=0; i<desc.length(); i++) {
	int first = i;
	while (desc.charAt(i)=='[') i++;
	if (desc.charAt(i)=='L') i=desc.indexOf(';', i);
	v.addElement(new ClassPointer(parent.getLinker(),
				      desc.substring(first, i+1)));
      }
      this.parameterTypes = new HPointer[v.size()];
      v.copyInto(this.parameterTypes);
    }
    this.parameterNames = new String[this.parameterTypes.length];
    this.exceptionTypes = new HClass[0];
    this.isSynthetic = false;
  }

  public HMethodMutator getMutator() { return this; }

  public void addModifiers(int m) { setModifiers(getModifiers()|m); }
  public void removeModifiers(int m) { setModifiers(getModifiers()&(~m)); }
  public void setModifiers(int m) {
    if (this.modifiers != m) parent.hasBeenModified = true;
    this.modifiers = m;
  }

  public void setReturnType(HClass returnType) {
    assert checkLinker(returnType);
    if (this.returnType != returnType) parent.hasBeenModified = true;
    this.returnType = returnType;
  }

  /** Warning: use can cause method name conflicts in class. */
  public void setParameterTypes(HClass[] parameterTypes) {
    for (int i=0; i<parameterTypes.length; i++)
      assert checkLinker(parameterTypes[i]);
    if (this.parameterTypes.length != parameterTypes.length)
      parent.hasBeenModified = true;
    else for (int i=0;
	      i<this.parameterTypes.length && i<parameterTypes.length; i++)
      if (this.parameterTypes[i] != parameterTypes[i])
	parent.hasBeenModified = true;
    this.parameterTypes = parameterTypes;
  }
  /** Warning: use can cause method name conflicts in class. */
  public void setParameterType(int which, HClass type) {
    assert checkLinker(type);
    if (this.parameterTypes[which] != type)
      parent.hasBeenModified = true;
    this.parameterTypes[which] = type;
  }

  public void setParameterNames(String[] parameterNames) {
    this.parameterNames = parameterNames;
  }
  public void setParameterName(int which, String name) {
    this.parameterNames[which] = name;
  }

  public void addExceptionType(HClass exceptionType) {
    assert checkLinker(exceptionType);
    for (int i=0; i<exceptionTypes.length; i++)
      if (exceptionTypes[i]==exceptionType)
	return;
    this.exceptionTypes = (HPointer[]) Util.grow(HPointer.arrayFactory,
						 exceptionTypes, exceptionType,
						 exceptionTypes.length);
    parent.hasBeenModified = true;
  }
  public void setExceptionTypes(HClass[] exceptionTypes) {
    for (int i=0; i<exceptionTypes.length; i++)
      assert checkLinker(exceptionTypes[i]);
    if (this.exceptionTypes.length != exceptionTypes.length)
      parent.hasBeenModified = true;
    else for (int i=0;
	      i<this.exceptionTypes.length && i<exceptionTypes.length; i++)
      if (this.exceptionTypes[i] != exceptionTypes[i])
	parent.hasBeenModified = true;
    this.exceptionTypes = exceptionTypes;
  }
  public void removeExceptionType(HClass exceptionType) {
    assert checkLinker(exceptionType);
    for (int i=0; i<exceptionTypes.length; i++)
      if (exceptionTypes[i].actual().equals(exceptionType)) {
	exceptionTypes = (HPointer[]) Util.shrink(HPointer.arrayFactory,
						  exceptionTypes, i);
	parent.hasBeenModified = true;
	return;
      }
  }

  public void setSynthetic(boolean isSynthetic) {
    if (this.isSynthetic != isSynthetic) parent.hasBeenModified = true;
    this.isSynthetic = isSynthetic;
  }

  //----------------------------------------------------------

  /** Make a method descriptor string given parameter and return value types.
   *  A helper function. */
  static String makeDescriptor(HClass[] paramTypes, HClass returnType){
    StringBuffer sb = new StringBuffer();
    sb.append ('(');
    for (int i = 0; i < paramTypes.length; i++){
      sb.append (paramTypes[i].getDescriptor());
    }
    sb.append (')');
    sb.append (returnType.getDescriptor());
    return  sb.toString();
  }

  //----------------------------------------------------------
  // assertion helper.
  private boolean checkLinker(HClass hc) {
    return hc.isPrimitive() || hc.getLinker()==parent.getLinker();
  }
  //----------------------------------------------------------
}

// set emacs indentation style.
// Local Variables:
// c-basic-offset:2
// End:
