// HMethod.java, created by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.ClassFile;

import java.lang.reflect.Modifier;
import java.util.Hashtable;
import harpoon.Util.Util;

/**
 * An <code>HMethod</code> provides information about, and access to, a 
 * single method on a class or interface.  The reflected method
 * may be a class method or an instance method (including an abstract
 * method).
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: HMethod.java,v 1.30 1998-11-10 00:44:38 cananian Exp $
 * @see HMember
 * @see HClass
 */
public abstract class HMethod implements HMember {
  HClass parent;
  String name;
  int modifiers;
  HClass returnType;
  HClass[] parameterTypes;
  String[] parameterNames;
  HClass[] exceptionTypes;
  boolean isSynthetic;

  /** Make a unique method name from a given suggestion. */
  static String uniqueName(HClass parent, 
			   String suggestion, String descriptor) 
  {
    if (suggestion==null || suggestion.equals("")) suggestion="MAGICm";
    // remove trailing dollar-signs.
    while (suggestion.charAt(suggestion.length()-1)=='$')
      suggestion = suggestion.substring(0, suggestion.length()-1);
    // remove anything after a double dollar sign.
    if (suggestion.indexOf("$$")!=-1)
      suggestion = suggestion.substring(0, suggestion.lastIndexOf("$$"));
    // find lowest unique number for method.
  L1:
    for (int i=-1; true; i++) {
      String methodname = (i<0)?suggestion:(suggestion+"$$"+i);
      // search class for existing method.
      HMethod[] hm = parent.getDeclaredMethods();
      for (int j=0; j<hm.length; j++)
	if (hm[j].getName().equals(methodname) &&
	    hm[j].getDescriptor().equals(descriptor))
	  continue L1;
      // found a valid name.
      return methodname;
    }
  }

  /** Register an <code>HCodeFactory</code> to be used by the
   *  <code>getCode</code> method. */
  public static void register(HCodeFactory f) {
    factories.put(f.getCodeName(), f);
  }
  static Hashtable factories = new Hashtable();

  /**
   * Returns an object representing the executable code of this method.
   * The only <code>codetype</code> defined by default is "bytecode",
   * which returns an <code>harpoon.IR.Bytecode.Code</code> object,
   * but other <code>codetype</code>s can be registered using the
   * <code>register()</code> method.
   * The <code>getCode()</code> method will use any registered
   * <code>HCodeFactory</code>s in order to create the <code>HCode</code>
   * requested.
   * @param codetype a string representing the code representation
   *                 you would like.
   * @return the code representation you requested, or <code>null</code>
   *         if no factory for the <code>codetype</code> can be found.
   *         <code>null</code> is typically also returned for native methods.
   * @see putCode
   */
  public HCode getCode(String codetype) {
    // Check the cache.
    HCode hc = (HCode) codetable.get(codetype);
    if (hc != null) return hc;
    // not cached. Make from scratch
    HCodeFactory f = (HCodeFactory) factories.get(codetype);
    if (f == null) return null; // oops! Can't find factory!
    // convert, cache, and return.
    hc = f.convert(this);
    if (hc != null)
      putCode(hc); // cache if conversion was successful.
    return hc;
  }
  /**
   * Add a new code representation for this method, or replace
   * a previously existing one.<p>
   * The 'codetype' string used for <code>getCode</code> is
   * the value of the <code>getName</code> method of 
   * <code>codeobj</code>.
   * @param codeobj  an object representing the code, or <code>null</code>
   *                 to delete a previously existing representation.
   * @see getCode
   * @see HCode#getName
   */
  public void putCode(HCode codeobj) {
    codetable.put(codeobj.getName(), codeobj);
  }
  Hashtable codetable = new Hashtable();

  /**
   * Returns the <code>HClass</code> object representing the class or 
   * interface that declares the method represented by this
   * <code>HMethod</code> object. 
   */
  public HClass getDeclaringClass() {
    return parent;
  }

  /**
   * Returns the name of the method represented by this <code>HMethod</code>
   * object, as a <code>String</code>.
   */
  public String getName() {
    return name;
  }

  /**
   * Returns the Java language modifiers for the method represented by this
   * <code>HMethod</code> object, as an integer.  The 
   * <code>java.lang.reflect.Modifier</code>
   * class should be used to decode the modifiers.
   * @see java.lang.reflect.Modifier
   */
  public int getModifiers() {
    return modifiers;
  }

  /**
   * Returns a <code>HClass</code> object that represents the formal
   * return type of the method represented by this <code>HMethod</code>
   * object.
   */
  public HClass getReturnType() { return returnType; }

  /**
   * Returns the descriptor for this method.
   */
  public String getDescriptor() {
    StringBuffer sb = new StringBuffer("(");
    HClass[] pt = getParameterTypes();
    for (int i=0; i<pt.length; i++)
      sb.append(pt[i].getDescriptor());
    sb.append(')');
    sb.append(getReturnType().getDescriptor());
    return sb.toString();
  }

  /**
   * Returns an array of <code>HClass</code> objects that represent the
   * formal parameter types, in declaration order, of the method
   * represented by this <code>HMethod</code> object.  Returns an array
   * of length 0 is the underlying method takes no parameters.
   */
  public HClass[] getParameterTypes() {
    return HClass.copy(parameterTypes);
  }

  /**
   * Returns an array of <code>String</code> objects giving the declared
   * names of the formal parameters of the method.  The length of the
   * returned array is equal to the number of formal parameters.
   * If there is no <code>LocalVariableTable</code> attribute available
   * for this method, then every element of the returned array will be
   * <code>null</code>.
   */
  public String[] getParameterNames() {
    return (String[]) Util.copy(parameterNames);
  }

  /**
   * Returns an array of <code>HClass</code> objects that represent the
   * types of the checked exceptions thrown by the underlying method
   * represented by this <code>HMethod</code> object.  Returns an array
   * of length 0 if the method throws no checked exceptions.
   */
  public HClass[] getExceptionTypes() {
    return HClass.copy(exceptionTypes);
  }

  /**
   * Determines whether this <code>HMethod</code> is synthetic.
   */
  public boolean isSynthetic() { return isSynthetic; }

  /** Determines whether this <code>HMethod</code> is an interface method. */
  public boolean isInterfaceMethod() {
    return parent.isInterface();
  }
  /** Determines whether this is a static method. */
  public boolean isStatic() {
    return Modifier.isStatic(getModifiers());
  }

  /**
   * Compares this <code>HMethod</code> against the specified object.
   * Returns <code>true</code> if the objects are the same.  Two
   * <code>HMethod</code>s are the same if they were declared by the same
   * class and have the same name and formal parameter types.
   */ // in actual practice, I think HMethods are unique.
  public boolean equals(Object obj) {
    if (obj==null) return false;
    if (this==obj) return true; // common case.
    if (!(obj instanceof HMethod)) return false;
    HMethod method = (HMethod) obj;
    if (parent != method.parent) return false;
    if (!getName().equals(method.getName())) return false;
    HClass hc1[] = getParameterTypes();
    HClass hc2[] = method.getParameterTypes();
    if (hc1.length != hc2.length) return false;
    for (int i=0; i<hc1.length; i++)
      if (hc1[i] != hc2[i])
	return false;
    return true;
  }

  /**
   * Returns a hashcode for thie <code>HMethod</code>.  The hashcode
   * is computed as the exclusive-or of the hashcodes for the
   * underlying method's declaring class, the method's name,
   * and the method's descriptor string.
   */
  public int hashCode() {
    return parent.hashCode()^getName().hashCode()^getDescriptor().hashCode();
  }

  /**
   * Returns a string describing this <code>HMethod</code>.  The string
   * is formatted as the method access modifiers, if any, followed by
   * the method return type, followed by a space, followed by the class
   * declaring the method, followed by a period, followed by the method
   * name, followed by a parenthesized, comma-separated list of the
   * method's formal parameter types.  If the method throws checked
   * exceptions, the parameter list is followed by a space, followed
   * by the word throws followed by a comma-separated list of the
   * throws exception types.  For example:<p>
   * <DL>
   * <DD><CODE>public boolean java.lang.Object.equals(java.lang.Object)</CODE>
   * </DL><p>
   * The access modifiers are placed in canonical order as specified by
   * "The Java Language Specification."  This is
   * <code>public</code>, <code>protected</code>, or <code>private</code>
   * first, and then other modifiers in the following order:
   * <code>abstract</code>, <code>static</code>, <code>final</code>, 
   * <code>synchronized</code>, <code>native</code>.
   */
  public String toString() {
    StringBuffer r = new StringBuffer();
    int m = getModifiers();
    if (m!=0) {
      r.append(Modifier.toString(m));
      r.append(' ');
    }
    r.append(getTypeName(getReturnType()));
    r.append(' ');
    r.append(getTypeName(parent));
    r.append('.');
    r.append(getName());
    r.append('(');
    HClass hcp[] = getParameterTypes();
    for (int i=0; i<hcp.length; i++) {
      r.append(getTypeName(hcp[i]));
      if (i < hcp.length-1)
	r.append(',');
    }
    r.append(')');
    HClass ecp[] = getExceptionTypes();
    if (ecp.length > 0) {
      r.append(" throws ");
      for (int i=0; i<ecp.length; i++) {
	r.append(ecp[i].getName()); // can't be primitive or array type.
	if (i < ecp.length-1)
	  r.append(',');
      }
    }
    return r.toString();
  }
  
  static String getTypeName(HClass hc) {
    // cheat.  We already implemented this function once.
    return HField.getTypeName(hc);
  }

  static HMethod[] copy(HMethod[] src) {
    if (src.length==0) return src;
    HMethod[] dst = new HMethod[src.length];
    System.arraycopy(src,0,dst,0,src.length);
    return dst;
  }
}

// set emacs indentation style.
// Local Variables:
// c-basic-offset:2
// End:
