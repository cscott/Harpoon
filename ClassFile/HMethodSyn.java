// HMethodSyn.java, created by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.ClassFile;

import java.lang.reflect.Modifier;
import java.util.Hashtable;
import java.util.Vector;

/**
 * An <code>HMethodSyn</code> provides information about, and access to, a 
 * single method on a class or interface.  The reflected method
 * may be a class method or an instance method (including an abstract
 * method).
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: HMethodSyn.java,v 1.4 1998-10-21 16:50:50 nkushman Exp $
 * @see HMember
 * @see HClass
 */
public class HMethodSyn extends HMethod {

  //the common constructor 
  private void init (HMethod template){
    this.modifiers = template.getModifiers();
    this.returnType = template.getReturnType();
    this.parameterTypes = template.getParameterTypes();
    this.parameterNames = template.getParameterNames();
    this.exceptionTypes = template.getExceptionTypes();
    this.isSynthetic = template.isSynthetic();
    ((HClassSyn)this.parent).addDeclaredMethod(this);
  }

  /** Create a new method based on a template. */
  public HMethodSyn(HMethod template) {
    this.parent = template.getDeclaringClass();
    this.name = uniqueName(parent, template.getName(),
			   template.getDescriptor());
    init (template);
  }

  //should be called only by HClassSyn constructor
  HMethodSyn(HMethod template, HClassSyn parent) {
    this.parent = parent;
    this.name = template.getName();
    HCode newCode = template.getCode ("quad-ssa");
    if (newCode != null){
      this.putCode (newCode);
    }
  }

  /** Create a new empty abstract method in the specified class
   *  with the specified parameter and return types
   *  that throws no checked exceptions.
   *  Adding code to the method will make it non-abstract.
   */

  public HMethodSyn(HClass parent, String name, HClass[] paramTypes, HClass returnType) {
    this(parent, name, makeDescriptor (paramTypes, returnType));
  }
  
  private String makeDescriptor (HClass[] paramTypes, HClass returnType){
    StringBuffer sb = new StringBuffer();
    sb.append ('(');
    for (int i = 0; i < paramTypes.length; i++){
      sb.append (paramTypes[i].getDescriptor());
    }
    sb.append (')');
    sb.append (returnType.getDescriptor());
    return  sb.toString();
  }

  /** Create a new empty abstract method in the specified class
   *  with the specified descriptor
   *  that throws no checked exceptions.
   *  Adding code to the method will make it non-abstract.
   */
  public HMethodSyn(HClass parent, String name, String descriptor) {
    this.parent = parent;
    this.name = uniqueName(parent, name, descriptor);
    this.modifiers = Modifier.ABSTRACT;
    { // parse descriptor for return type.
      String desc = descriptor.substring(descriptor.lastIndexOf(')')+1);
      this.returnType = HClass.forDescriptor(desc);
    }
    { // parse descriptor for parameter types.
      String desc = descriptor.substring(1, descriptor.lastIndexOf(')'));
      Vector v = new Vector();
      for (int i=0; i<desc.length(); i++) {
	v.addElement(HClass.forDescriptor(desc.substring(i)));
	while (desc.charAt(i)=='[') i++;
	if (desc.charAt(i)=='L') i=desc.indexOf(';', i);
      }
      this.parameterTypes = new HClass[v.size()];
      v.copyInto(this.parameterTypes);
    }
    this.parameterNames = new String[this.parameterTypes.length];
    this.exceptionTypes = new HClass[0];
    this.isSynthetic = false;
    ((HClassSyn)parent).addDeclaredMethod(this);
  }

  public void setModifiers(int m) { this.modifiers = m; }

  public void setReturnType(HClass returnType) { this.returnType = returnType;}

  /** Warning: use can cause method name conflicts in class. */
  public void setParameterTypes(HClass[] parameterTypes) {
    this.parameterTypes = parameterTypes;
  }
  /** Warning: use can cause method name conflicts in class. */
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

  /** Add a new code representation for this method, or replace a
   *  previously existing one.<p>
   *  An abstract method does not have code; thus <code>putCode</code> 
   *  resets the <code>abstract</code> modifier on this
   *  <code>HMethodSyn</code>.
   */
  public void putCode(HCode codeobj) {
    super.putCode(codeobj);
    // if it's got code, it's not abstract.
    this.modifiers &= ~Modifier.ABSTRACT;
  }
}

// set emacs indentation style.
// Local Variables:
// c-basic-offset:2
// End:
