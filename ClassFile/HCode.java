package harpoon.ClassFile;

/**
 * <code>HCode</code> is an interface that all views of a particular
 * method's executable code should implement.
 * <p>
 * An <code>HCode</code> corresponds roughly to a "list of instructions".
 *
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: HCode.java,v 1.1 1998-08-03 00:11:26 cananian Exp $
 * @see HMethod
 * @see HCodeElement
 * @see harpoon.ClassFile.Bytecode.Code
 */
public abstract class HCode {
  /**
   * Return the <code>HMethod</code> to this this <code>HCode</code>
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
   */
  public abstract HCodeElement[] getElements();

  /**
   * Convert from a different code view, by way of intermediates.
   * It is expected that each code view will have conversion functions
   * from one or two other code views; the <code>convertFrom</code> function
   * is thus expected to recursively invoke the <code>convertFrom</code>
   * methods of the code views it understands, in an attempt to create
   * a complete conversion chain.  <code>convertFrom</code> should return
   * <code>null</code>, without throwing an exception, if it finds that
   * conversion is impossible; this will indicate to a parent 
   * <code>convertFrom</code> method that it should try a different 
   * intermediate format.
   * <p>
   * All codeviews should be accessed using the <code>getCode</code> method
   * of the appropriate <code>HMethod</code> to allow efficient caching.
   * If a conversion is done, <code>putCode</code> should be called
   * to cache the new view before <code>convertFrom</code> returns.
   * <p>
   * The default superclass implementation always returns <code>null</code>.  
   * It is expected that all subclasses will override this implementation.
   * @see HMethod#getCode
   * @see HMethod#putCode
   */
  public static HCode convertFrom(HCode codeview) { return null; }
}
