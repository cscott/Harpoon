// HCode.java, created Sun Aug  2 20:11:26 1998 by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.ClassFile;

import harpoon.Util.ArrayFactory;
import harpoon.Util.Indexer;

import java.util.Enumeration;
import java.util.Iterator;
/**
 * <code>HCode</code> is an abstract class that all views of a particular
 * method's executable code should extend.
 * <p>
 * An <code>HCode</code> corresponds roughly to a "list of instructions".
 *
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: HCode.java,v 1.12.2.6 2000-02-10 22:17:09 cananian Exp $
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
   * occupy index 0 of the <code>HCodeElement</code> array.<p>
   * Either <code>getElementsI()</code> or <code>getElementsE()</code>
   * must have been implemented for the default implementation to work
   * properly.
   * @deprecated use getElementsL() instead.
   * @see harpoon.IR.Bytecode.Instr
   */
  public HCodeElement[] getElements() {
    java.util.List l = getElementsL();
    HCodeElement[] r =
      (HCodeElement[]) elementArrayFactory().newArray(l.size());
    return (HCodeElement[]) l.toArray(r);
  }
  /**
   * Return an ordered <code>Collection</code> (a <code>List</code>) of
   * the component objects making up this code view.  If there is a
   * 'root' to the code view, it should be the first element in the
   * List.  <p>
   * Either <code>getElementsI()</code> or <code>getElementsE()</code>
   * must have been implemented for the default implementation to work
   * properly.
   */
  public java.util.List getElementsL() {
    java.util.List l = new java.util.ArrayList();
    for (Iterator i = getElementsI(); i.hasNext(); )
      l.add(i.next());
    return java.util.Collections.unmodifiableList(l);
  }
  /**
   * Return an Enumeration of the component objects making up this
   * code view.  If there is a 'root' to the code view, it should
   * be the first element enumerated.<p>
   * Implementations must implement at least one of
   * <code>getElementsE()</code>, or <code>getElementsI()</code>.
   * @deprecated use getElementsI() instead.
   * @see harpoon.IR.Bytecode.Instr
   */
  public Enumeration getElementsE() {
    return new harpoon.Util.IteratorEnumerator(getElementsI());
  }
  /**
   * Return an Iterator over the component objects making up this
   * code view.  If there is a 'root' to the code view, it should
   * be the first element enumerated.<p>
   * Implementations must implement at least one of
   * <code>getElementsE()</code>, or <code>getElementsI()</code>.
   */
  public Iterator getElementsI() {
    return new harpoon.Util.EnumerationIterator(getElementsE());
  }

  /**
   * Return the 'root' element of this code view.
   * @return root of the code view, or <code>null</code> if this notion
   *         is not applicable.
   */
  public HCodeElement getRootElement() {
    return (HCodeElement) (getElementsI().next());
  }
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
   * Return an <code>Indexer</code> for the <code>HCodeElement</code>s
   * composing this <code>HCode</code>.  The default <code>Indexer</code>
   * returned does not implement <code>Indexer.getByID()</code>.
   */
  public Indexer elementIndexer() { return _indexer; }
  private static final Indexer _indexer = new Indexer() {
    public int getID(Object o) { return ((HCodeElement)o).getID(); }
  };

  /**
   * Clone this <code>HCode</code>, possibly moving it to a different method.
   * Throws <code>CloneNotSupportedException</code> if not overridden.
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
