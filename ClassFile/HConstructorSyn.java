// HConstructorSyn.java, created Fri Oct 16  3:30:52 1998 by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.ClassFile;

import java.lang.reflect.Modifier;
import java.util.Hashtable;
import java.util.Vector;

/**
 * An <code>HConstructorSyn</code> is a mutable representation of a
 * single constructor for a class.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: HConstructorSyn.java,v 1.2.2.3 1999-08-07 04:14:20 cananian Exp $
 * @see HMember
 * @see HClass
 */
public class HConstructorSyn extends HMethod {

  /** Create a new method based on a template. */
  public HConstructorSyn(HConstructor template) {
    this.parent = template.getDeclaringClass();
    //this.name = template.getName(); // superclass inits.
    // XXX ensure uniqueness (parameter types?)
    this.modifiers = template.getModifiers();
    //this.returnType = template.getReturnType(); // superclass inits.
    this.parameterTypes = template.getParameterTypes();
    this.parameterNames = template.getParameterNames();
    this.exceptionTypes = template.getExceptionTypes();
    this.isSynthetic = template.isSynthetic();
    ((HClassSyn)parent).addDeclaredMethod(this);
  }
  /** Create a new empty constructor for the specified class
   *  with the specified descriptor that
   *  throws no checked exceptions.
   *  You must putCode to make this constructor valid.
   */
  public HConstructorSyn(HClass parent, String descriptor) {
    this.parent = parent;
    //this.name = name; // superclass inits.
    // XXX ensure uniqueness?
    this.modifiers = 0;
    //this.returnType = HClass.Void; // superclass inits.
    { // parse descriptor for parameter types.
      String desc = descriptor.substring(1, descriptor.lastIndexOf(')'));
      Vector v = new Vector();
      for (int i=0; i<desc.length(); i++) {
	v.addElement(new ClassPointer(desc.substring(i)));
	while (desc.charAt(i)=='[') i++;
	if (desc.charAt(i)=='L') i=desc.indexOf(';', i);
      }
      this.parameterTypes = new HPointer[v.size()];
      v.copyInto(this.parameterTypes);
    }
    this.parameterNames = new String[parameterTypes.length];
    this.exceptionTypes = new HClass[0];
    this.isSynthetic = false;
    ((HClassSyn)parent).addDeclaredMethod(this);
  }

  public void setModifiers(int m) { this.modifiers = m; }

  public void setParameterTypes(HClass[] parameterTypes) {
    this.parameterTypes = parameterTypes;
  }
  public void setParameterType(int which, HClass type) {
    this.parameterTypes[which] = type;
  }

  public void setParameterNames(String[] parameterNames) {
    this.parameterNames = parameterNames;
  }
  public void setParameterName(int which, String name) {
    this.parameterNames[which] = name;
  }

  public void setExceptionTypes(HClass[] exceptionTypes) {
    this.exceptionTypes = exceptionTypes;
  }
  public void setExceptionType(int which, HClass type) {
    this.exceptionTypes[which] = type;
  }

  public void setSynthetic(boolean isSynthetic) {
    this.isSynthetic = isSynthetic;
  }

  /** Serializable interface. */
  public Object writeReplace() { return this; }
  /** Serializable interface. */
  public void writeObject(java.io.ObjectOutputStream out)
    throws java.io.IOException {
    // resolve class name pointers.
    this.returnType = this.returnType.actual();
    for (int i=0; i<this.parameterTypes.length; i++)
      this.parameterTypes[i] = this.parameterTypes[i].actual();
    for (int i=0; i<this.exceptionTypes.length; i++)
      this.exceptionTypes[i] = this.exceptionTypes[i].actual();
    // intern strings.
    this.name = this.name.intern();
    for (int i=0; i<this.parameterNames.length; i++)
      this.parameterNames[i] = this.parameterNames[i].intern();
    // write data
    out.defaultWriteObject();
  }
}

// set emacs indentation style.
// Local Variables:
// c-basic-offset:2
// End:
