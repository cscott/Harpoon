package harpoon.ClassFile.Bytecode;

import harpoon.ClassFile.*;
import harpoon.ClassFile.Raw.MethodInfo;

/**
 * <code>Bytecode.Code</code> is a code view that exposes the
 * raw java classfile bytecodes.
 *
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: Code.java,v 1.1 1998-08-03 01:37:27 cananian Exp $
 * @see harpoon.ClassFile.HCode
 */
public class Code extends HCode {
  HMethod parent;
  MethodInfo methodinfo;

  /** Constructor. */
  public Code(HMethod parent, MethodInfo methodinfo) {
    this.parent = parent;
    this.methodinfo = methodinfo;
  }

  /**
   * Return the <code>HMethod</code> this codeview
   * belongs to.
   */
  public HMethod getMethod() { return parent; }

  /**
   * Return the name of this code view, <code>"bytecode"</code>.
   * @return the string <code>"bytecode"</code>.
   */
  public String getName() { return "bytecode"; }

  /**
   * Return an ordered list of the <code>Bytecode.Instr</code>s
   * making up this code view.  The first instruction to be
   * executed is in element 0 of the array.
   */
  public HCodeElement[] getElements() {
    if (elements==null) {
      // FIXME make elements.
    }
    return copy(elements);
  }
  /** Cached value of <code>getElements</code>. */
  private HCodeElement[] elements = null;

  /**
   * Convert from a different code view, by way of intermediates.
   * <code>Bytecode</code> is the basic codeview; no conversion
   * functions are implemented.
   * @return <code>null</code>, always.
   */
  public static HCode convertFrom(HCode codeview) { return null; }

  static HCodeElement[] copy(HCodeElement[] src) {
    if (src.length==0) return src;
    HCodeElement[] dst = new HCodeElement[src.length];
    System.arraycopy(src,0,dst,0,src.length);
    return dst;
  }
}
