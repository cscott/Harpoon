package harpoon.ClassFile;

/**
 * <Code>HCodeElement</code> is an interface that all views of a particular
 * method's executable instructions should implement.<p>
 * <code>HCodeElement</code>s are "components" of an <code>HCode</code>.
 * The correspond roughly to "an instruction" in the <code>HCode</code>
 * "list of instructions".
 *
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: HCodeElement.java,v 1.3 1998-08-03 01:37:15 cananian Exp $
 * @see HCode
 * @see harpoon.ClassFile.Bytecode.Instr
 */
public interface HCodeElement {
  /** Get the original source file name that this element is derived from. */
  public String getSourceFile();
  /** Get the line in the original source file that this element is 
   *  traceable to. */
  public int getLineNumber();
}
