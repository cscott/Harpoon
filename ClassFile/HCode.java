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
 * @version $Id: HCode.java,v 1.15 2002-08-31 00:27:51 cananian Exp $
 * @see HMethod
 * @see HCodeElement
 * @see harpoon.IR.Bytecode.Code
 * @see harpoon.IR.Bytecode.Instr
 */
public abstract class HCode<HCE extends HCodeElement> {
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
  public HCE[] getElements() {
    java.util.List<HCE> l = getElementsL();
    HCE[] r = elementArrayFactory().newArray(l.size());
    return l.toArray(r);
  }
  /**
   * Return an ordered <code>Collection</code> (a <code>List</code>) of
   * the component objects making up this code view.  If there is a
   * 'root' to the code view, it should be the first element in the
   * List.  Note that the object returned need not be static; i.e.
   * the contents of the <code>List</code> may (or may not) change as
   * the underlying representation is modified.  If you need a
   * static view, see <code>SnapshotIterator</code>; if you need
   * a dynamic view, don't cache the returned <code>List</code>.
   * <p>
   * Either <code>getElementsI()</code> or <code>getElementsE()</code>
   * must have been implemented for the default implementation to work
   * properly.
   */
  // the default implementation gives a snapshot view, for what it's worth.
  public java.util.List<HCE> getElementsL() {
    java.util.List<HCE> l = new java.util.ArrayList<HCE>();
    for (Iterator<HCE> i = getElementsI(); i.hasNext(); )
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
  public Enumeration<HCE> getElementsE() {
    return new harpoon.Util.IteratorEnumerator<HCE>(getElementsI());
  }
  /**
   * Return an Iterator over the component objects making up this
   * code view.  If there is a 'root' to the code view, it should
   * be the first element enumerated.<p>
   * Implementations must implement at least one of
   * <code>getElementsE()</code>, or <code>getElementsI()</code>.
   */
  public Iterator<HCE> getElementsI() {
    return new harpoon.Util.EnumerationIterator<HCE>(getElementsE());
  }

  /**
   * Return the 'root' element of this code view.
   * @return root of the code view, or <code>null</code> if this notion
   *         is not applicable.
   */
  public HCE getRootElement() {
    return getElementsI().next();
  }
  /**
   * Return the 'leaves' of this code view; that is,
   * the elements with no successors.
   * @return leaves of the code view, or <code>null</code> if this notion
   *         is not applicable.
   */
  public HCE[] getLeafElements() { return null; }

  /**
   * Return an <code>ArrayFactory</code> for the <code>HCodeElement</code>s
   * composing this <code>HCode</code>.
   */
  public abstract ArrayFactory<HCE> elementArrayFactory();

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
  public HCodeAndMaps clone(HMethod newMethod)
    throws CloneNotSupportedException {
    throw new CloneNotSupportedException(this.toString());
  }
  /**
   * Clone this <code>HCode</code> and all associated 
   * <code>HCodeElement</code>s.
   * Calls <code>clone(getMethod())</code>.
   */
  public Object clone() throws CloneNotSupportedException {
    return clone(getMethod()).hcode();
  }

  /**
   * Pretty-print this code view using an empty callback.
   */
  public final void print(java.io.PrintWriter pw) {
    print(pw, new PrintCallback<HCE>());
  }
  /**
   * Pretty-print this code view using the specified callback.
   */
  public void print(java.io.PrintWriter pw, PrintCallback<HCE> callback) {
    if (callback==null) callback = new PrintCallback<HCE>(); // nop callback
    pw.println("Codeview \""+getName()+"\" for "+getMethod()+":");
    for (Iterator<HCE> it = getElementsI(); it.hasNext(); ) {
      HCE hce = it.next();
      callback.printBefore(pw, hce);
      pw.println("  #"+hce.getID()+"/"+
		 hce.getSourceFile()+":"+hce.getLineNumber()+" - " +
		 hce.toString());
      callback.printAfter(pw, hce);
    }
    pw.println();
    pw.flush();
  }

  /** Returns a human-readable representation of this <code>HCode</code>. */
  public String toString() {
    return "codeview " + getName() + " for " + getMethod();
  }

  /** Callback interface for annotating pretty-prints of <code>HCode</code>s.*/
  public static class PrintCallback<HCE extends HCodeElement> {
    /** This method is called right *before* each <code>HCodeElement</code>
     *  is output. */
    public void printBefore(java.io.PrintWriter pw, HCE hce) { }
    /** This method is called right *after* each <code>HCodeElement</code>
     *  is output. */
    public void printAfter(java.io.PrintWriter pw, HCE hce) { }
  }
}

// set emacs indentation style.
// Local Variables:
// c-basic-offset:2
// End:
