package harpoon.ClassFile;

import harpoon.ClassFile.Raw.Attribute.Attribute;
import harpoon.ClassFile.Raw.Attribute.AttributeCode;
import harpoon.ClassFile.Raw.Attribute.AttributeExceptions;
import harpoon.ClassFile.Raw.Constant.ConstantClass;
import java.lang.reflect.Modifier;
import java.util.Hashtable;
import java.util.Vector;

/**
 * A <code>HMethod</code> provides information about, and access to, a 
 * single method on a class or interface.  The reflected method
 * may be a class method or an instance method (including an abstract
 * method).
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: HMethod.java,v 1.5 1998-08-01 23:28:54 cananian Exp $
 * @see HMember
 * @see HClass
 */
public class HMethod implements HMember {
  HClass hclass;
  HClass returnType, parameterTypes[], exceptionTypes[];
  // raw data.
  harpoon.ClassFile.Raw.MethodInfo methodinfo;
  AttributeCode code;
  AttributeExceptions exceptions;

  /** Create an <code>HMethod</code> from a raw method_info structure. */
  protected HMethod(HClass parent, 
		    harpoon.ClassFile.Raw.MethodInfo methodinfo) {
    this.hclass = parent;
    this.methodinfo = methodinfo;
    // the following fields are computed on demand.
    this.returnType=null;
    this.parameterTypes=null;
    this.exceptionTypes=null;
    // Crunch the attribute information.
    this.code = null;  this.exceptions=null;
    for (int i=0; i<methodinfo.attributes.length; i++) {
      if (methodinfo.attributes[i] instanceof AttributeCode)
	this.code = (AttributeCode) methodinfo.attributes[i];
      if (methodinfo.attributes[i] instanceof AttributeExceptions)
	this.exceptions = (AttributeExceptions) methodinfo.attributes[i];
    }
    // Add the default code representation.
    if (this.code != null)
      putCode("bytecode", this.code.code);
  }

  /**
   * Returns an object representing the executable code of this method.
   * The only <code>codetype</code> defined by default is "bytecode",
   * which returns a byte[] array object containing the bytecode
   * for the method.
   * @param codetype a string representing the code representation
   *                 you would like.
   * @return the code representation you requested, or <code>null</code>
   *         if the given <code>codetype</code> cannot be found.
   * @see putCode
   */
  public Object getCode(String codetype) {
    return codetable.get(codetype);
  }
  /**
   * Add a new code representation for this method, or replace
   * a previously existing one.
   * @param codetype a string describing this code representation.
   * @param codeobj  an object representing the code, or <code>null</code>
   *                 to delete a previously existing representation.
   * @see getCode
   */
  public void putCode(String codetype, Object codeobj) {
    codetable.put(codetype, codeobj);
  }
  private Hashtable codetable = new Hashtable();

  /**
   * Returns the <code>HClass</code> object representing the class or 
   * interface that declares the method represented by this
   * <code>HMethod</code> object. 
   */
  public HClass getDeclaringClass() {
    return hclass;
  }
  /**
   * Returns the name of the method represented by this <code>HMethod</code>
   * object, as a <code>String</code>.
   */
  public String getName() {
    return methodinfo.name();
  }
  /**
   * Returns the Java language modifiers for the method represented by this
   * <code>HMethod</code> object, as an integer.  The <code>Modifier</code>
   * class should be used to decode the modifiers.
   * @see java.lang.reflect.Modifier
   */
  public int getModifiers() {
    return methodinfo.access_flags.access_flags;
  }
  /**
   * Returns a <code>HClass</code> object that represents the formal
   * return type of the method represented by this <code>HMethod</code>
   * object.
   */
  public HClass getReturnType() {
    if (returnType==null) {
      String desc = methodinfo.descriptor();
      desc = desc.substring(desc.lastIndexOf(')')+1); // just ret val desc.
      returnType = HClass.forDescriptor(desc);
    }
    return returnType;
  }
  /**
   * Returns an array of <code>HClass</code> objects that represent the
   * formal parameter types, in declaration order, of the method
   * represented by this <code>HMethod</code> object.  Returns an array
   * of length 0 is the underlying method takes no parameters.
   */
  public HClass[] getParameterTypes() {
    if (parameterTypes==null) {
      // parse method descriptor.
      String desc = methodinfo.descriptor();
      desc = desc.substring(1, desc.lastIndexOf(')')); // just parameters now.
      Vector v = new Vector();
      for (int i=0; i<desc.length(); i++) {
	// make HClass for first param in list.
	v.addElement(HClass.forDescriptor(desc.substring(i)));
	// skip over the one we just added.
	while (desc.charAt(i)=='[') i++;
	if (desc.charAt(i)=='L') i=desc.indexOf(';', i);
      }
      parameterTypes = new HClass[v.size()];
      v.copyInto(parameterTypes);
    }
    return HClass.copy(parameterTypes);
  }
  /**
   * Returns an array of <code>HClass</code> objects that represent the
   * types of the checked exceptions thrown by the underlying method
   * represented by this <code>HMethod</code> object.  Returns an array
   * of length 0 if the method throws no checked exceptions.
   */
  public HClass[] getExceptionTypes() {
    if (exceptionTypes==null) {
      // compute exception types from AttributeExceptions.
      if (exceptions == null)
	exceptionTypes = new HClass[0];
      else {
	Vector v = new Vector();
	for (int i=0; i<exceptions.number_of_exceptions(); i++) {
	  ConstantClass cc = exceptions.exception_index_table(i);
	  if (cc != null)
	    v.addElement(HClass.forName(cc.name().replace('/','.')));
	}
	exceptionTypes = new HClass[v.size()];
	v.copyInto(exceptionTypes);
      }
    }
    return HClass.copy(exceptionTypes);
  }

  /**
   * Compares this <code>HMethod</code> against the specified object.
   * Returns <code>true</code> if the objects are the same.  Two
   * <code>HMethod</code>s are the same if they were declared by the same
   * class and have the same name and formal parameter types.
   */
  public boolean equals(Object obj) {
    if (obj==null) return false;
    if (!(obj instanceof HMethod)) return false;
    HMethod method = (HMethod) obj;
    if (hclass != method.hclass) return false;
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
   * underlying method's declaring class name and the method's name.
   */
  public int hashCode() {
    return hclass.getName().hashCode() ^ getName().hashCode();
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
    r.append(getTypeName(hclass));
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
