package harpoon.ClassFile;

/**
 * <Code>HCodeElement</code> is an interface that all views of a particular
 * method's executable instructions should implement.<p>
 * <code>HCodeElement</code>s are "components" of an <code>HCode</code>.
 * The correspond roughly to "an instruction" in the <code>HCode</code>
 * "list of instructions".  Each <code>HCodeElement</code> should be
 * traceable to an original source file and line number, and possess
 * a unique numeric identifier.
 *
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: HCodeElement.java,v 1.6 1998-09-14 02:49:15 cananian Exp $
 * @see HCode
 * @see harpoon.IR.Bytecode.Instr
 */
public interface HCodeElement {
  /** Get the original source file name that this element is derived from. */
  public String getSourceFile();
  /** Get the line in the original source file that this element is 
   *  traceable to. */
  public int getLineNumber();
  /**
   * Returns a unique numeric identifier for this element.
   */
  public int getID();
}
