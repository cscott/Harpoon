// HCode.java, created by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.ClassFile;

import harpoon.Util.ArrayFactory;

import java.util.Enumeration;
/**
 * <code>HCode</code> is an abstract class that all views of a particular
 * method's executable code should extend.
 * <p>
 * An <code>HCode</code> corresponds roughly to a "list of instructions".
 *
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: HCode.java,v 1.12.2.1 1998-11-30 21:21:01 cananian Exp $
 * @see HMethod
 * @see HCodeElement
 * @see harpoon.IR.Bytecode.Code
 * @see harpoon.IR.Bytecode.Instr
 */
public abstract class HCode {
  /**
   * Return the <code>HMethod</code> to which this <code>HCode</code>
   * belongs.
   */
  public abstract HMethod getMethod();

  /**
   * Return the name of this code view.
   * The default bytecode view is named <code>"bytecode"</code>.
   * It is suggested that additional views be named in a similarly
   * human-friendly fashion.
   */
  public abstract String getName();

  /**
   * Return an ordered list of the component objects making up this
   * code view.  If there is a 'root' to the code view, it should
   * occupy index 0 of the <code>HCodeElement</code> array.
   * @see harpoon.IR.Bytecode.Instr
   */
  public abstract HCodeElement[] getElements();
  /**
   * Return an Enumeration of the component objects making up this
   * code view.  If there is a 'root' to the code view, it should
   * be the first element enumerated.
   * @see harpoon.IR.Bytecode.Instr
   */
  public abstract Enumeration getElementsE();

  /**
   * Return the 'root' element of this code view.
   * @return root of the code view, or <code>null</code> if this notion
   *         is not applicable.
   */
  public HCodeElement getRootElement() { return getElements()[0]; }
  /**
   * Return the 'leaves' of this code view; that is,
   * the elements with no successors.
   * @return leaves of the code view, or <code>null</code> if this notion
   *         is not applicable.
   */
  public HCodeElement[] getLeafElements() { return null; }

  /**
   * Return an <code>ArrayFactory</code> for the <code>HCodeElement</code>s
   * composing this <code>HCode</code>.
   */
  public abstract ArrayFactory elementArrayFactory();

  /**
   * Clone this HCode, possibly moving it to a different method.
   * Throws CloneNotSupportedException if not overridden.
   * @exception CloneNotSupportedException if it is not possible to clone
   *            this <code>HCode</code>.
   */
  public HCode clone(HMethod newMethod) throws CloneNotSupportedException {
    throw new CloneNotSupportedException(this.toString());
  }

  /**
   * Pretty-print this code view.
   */
  public void print(java.io.PrintWriter pw) {
    pw.println("Codeview \""+getName()+"\" for "+getMethod()+":");
    HCodeElement[] hce = getElements();
    for (int i=0; i<hce.length; i++)
      pw.println("  #"+hce[i].getID()+"/"+
		 hce[i].getSourceFile()+":"+hce[i].getLineNumber()+" - " +
		 hce[i].toString());
  }

  /** Returns a human-readable representation of this <code>HCode</code>. */
  public String toString() {
    return "codeview " + getName() + " for " + getMethod();
  }
}

// set emacs indentation style.
// Local Variables:
// c-basic-offset:2
// End:
