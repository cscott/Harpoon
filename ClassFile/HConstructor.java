package harpoon.ClassFile;

import java.lang.reflect.Modifier;

/**
 * A <code>HConstructor</code> provides information about a single
 * constructor for a class.
 *
 * @author  C. Scott Ananian (cananian@alumni.princeton.edu)
 * @version $Id: HConstructor.java,v 1.3 1998-08-01 22:50:01 cananian Exp $
 * @see HMethod
 * @see HMember
 * @see HClass
 */
public class HConstructor extends HMethod {

  /** Create an <code>HConstructor</code> from a raw method_info structure. */
  protected HConstructor(HClass parent,
			 harpoon.ClassFile.Raw.MethodInfo methodinfo) {
    super(parent, methodinfo);
  }

  /**
   * Returns the name of this constructor, as a string.  This is always
   * the string "<code>&lt;init&gt;</code>".
   */
  public String getName() { return "<init>"/*hclass.getName()*/; }

  /**
   * Returns a hashcode for this Constructor.  The hashcode is the same as
   * the hashcode for the underlying constructor's declaring class name.
   */
  public int hashCode() { return hclass.getName().hashCode(); }

  /**
   * Return a string describing this Constructor.  The string is formatted
   * as the constructor access modifiers, if any, followed by the
   * fully-qualified name of the declaring class, followed by a 
   * parenthesized, comma-separated list of the constructor's formal
   * parameter types.  For example: <p>
   * <DL><DD><CODE>public java.util.Hashtable(int,float)</CODE></DL><p>
   * The only possible modifiers for constructors are the access modifiers
   * <code>public</code>, <code>protected</code>, or <code>private</code>.
   * Only one of these may appear, or none if the constructor has default
   * (<code>package</code>) access.
   */
  public String toString() {
    StringBuffer r = new StringBuffer();
    int m = getModifiers();
    if (m!=0) {
      r.append(Modifier.toString(m));
      r.append(' ');
    }
    r.append(getTypeName(hclass));
    r.append('(');
    HClass hcp[] = getParameterTypes();
    for (int i=0; i<hcp.length; i++) {
      r.append(getTypeName(hcp[i]));
      if (i<hcp.length-1)
	r.append(',');
    }
    r.append(')');
    HClass ecp[] = getExceptionTypes();
    if (ecp.length > 0) {
      r.append(" throws ");
      for (int i=0; i<ecp.length; i++) {
	r.append(ecp[i].getName()); // can't be primitive or array type.
	if (i<ecp.length-1)
	  r.append(',');
      }
    }
    return r.toString();
  }

  static HConstructor[] copy(HConstructor[] src) {
    if (src.length==0) return src;
    HConstructor[] dst = new HConstructor[src.length];
    System.arraycopy(src,0,dst,0,src.length);
    return dst;
  }
}
